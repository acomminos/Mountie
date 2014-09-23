/*
 * Mountie, a tool for mounting external storage on Android
 * Copyright (C) 2014 Andrew Comminos <andrew@morlunk.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.morlunk.mountie.command;

import com.morlunk.mountie.fs.Partition;
import com.stericson.RootTools.execution.Command;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieves sd[a-z0-9] partition data using the system blkid command.
 * TODO: switch to JNI calls and libblkid
 * Created by andrew on 19/09/14.
 */
public class BlkidCommand extends Command {
    private static final Pattern BLOCK_DEVICE_PATTERN = Pattern.compile("/dev/block/(sd[a-z0-9]+):");
    /**
     * Returns a whitespace separated key-value mapping containing a label, UUID,
     * and partition type
     */
    private static final String BLKID_COMMAND = "/system/bin/blkid";
    private static final String BLKID_TARGET_COMMAND = BLKID_COMMAND + " /dev/block/%s";

    private Listener mListener;
    private List<Partition> mPartitions;

    public BlkidCommand(int id, Listener listener) {
        super(id, BLKID_COMMAND);
        mListener = listener;
        mPartitions = new LinkedList<Partition>();
    }

    public BlkidCommand(int id, String device, Listener listener) {
        super(id, String.format(BLKID_TARGET_COMMAND, device));
        mListener = listener;
        mPartitions = new LinkedList<Partition>();
    }

    @Override
    public void commandOutput(int id, String line) {
        // Example output (we can expect one line of output)
        // /dev/block/sda1: LABEL="Device" UUID="ABCD-EFGH" TYPE="vfat"
        String name = null;
        String label = null;
        String uuid = null;
        String fs = null;

        String[] params = line.split(" ");

        if (params.length == 0) {
            return;
        }

        // Match device name (sdxn)
        Matcher deviceMatcher = BLOCK_DEVICE_PATTERN.matcher(params[0]);
        if (deviceMatcher.matches()) {
            name = deviceMatcher.group(1);
        } else {
            return;
        }
        for (int i = 1; i < params.length; i++) {
            String[] kv = params[i].split("=");
            if (kv.length != 2) {
                continue;
            }

            String key = kv[0];
            String value = kv[1].substring(1, kv[1].length() - 1); // trim quotes

            if ("LABEL".equals(key)) {
                label = value;
            } else if ("UUID".equals(key)) {
                uuid = value;
            } else if ("TYPE".equals(key)) {
                fs = value;
            }
        }

        mPartitions.add(new Partition(name, label, uuid, fs));
    }

    @Override
    public void commandTerminated(int id, String reason) {
        mListener.onBlkidFailure();
    }

    @Override
    public void commandCompleted(int id, int exitCode) {
        if (!mPartitions.isEmpty()) {
            mListener.onBlkidResult(mPartitions);
        } else {
            mListener.onBlkidFailure();
        }
    }

    public static interface Listener {
        public void onBlkidResult(List<Partition> partitions);
        public void onBlkidFailure();
    }
}
