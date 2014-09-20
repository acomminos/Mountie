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

package com.morlunk.mountie.fs;

import com.stericson.RootTools.execution.Command;

/**
 * Retrieves partition data using the system blkid command.
 * TODO: switch to JNI calls and libblkid
 * Created by andrew on 19/09/14.
 */
public class BlkidCommand extends Command {
    /**
     * Returns a whitespace separated key-value mapping containing a label, UUID,
     * and partition type
     */
    private static final String BLKID_COMMAND = "/system/bin/blkid " + "/dev/block/%s";

    private String mDevice;
    private Listener mListener;
    private Partition mPartition;

    public BlkidCommand(int id, String device, Listener listener) {
        super(id, String.format(BLKID_COMMAND, device));
        mDevice = device;
        mListener = listener;
    }

    @Override
    public void commandOutput(int id, String line) {
        // Example output (we can expect one line of output)
        // /dev/block/sda1: LABEL="Device" UUID="ABCD-EFGH" TYPE="vfat"
        String label = null;
        String uuid = null;
        String fs = null;

        String[] params = line.split(" ");
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

        mPartition = new Partition(mDevice, label, uuid, fs);
    }

    @Override
    public void commandTerminated(int id, String reason) {
        mListener.onBlkidFailure(mDevice);
    }

    @Override
    public void commandCompleted(int id, int exitCode) {
        if (mPartition != null) {
            mListener.onBlkidResult(mPartition);
        } else {
            mListener.onBlkidFailure(mDevice);
        }
    }

    public static interface Listener {
        public void onBlkidResult(Partition partition);
        public void onBlkidFailure(String device);
    }
}
