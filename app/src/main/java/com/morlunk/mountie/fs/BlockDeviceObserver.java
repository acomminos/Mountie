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

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.morlunk.mountie.Constants;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Monitors /dev/block for devices using inotify.
 * Keeps a database of block devices and their mounts.
 * Created by andrew on 14/09/14.
 */
public class BlockDeviceObserver extends FileObserver {
    private static final Pattern DEV_PATTERN = Pattern.compile("(sd[a-z]+)(\\d+)?");
    /** A mapping of device identifiers (i.e. sda) to volumes. */
    private Map<String, Volume> mVolumes;
    private PartitionListener mListener;
    private Handler mHandler;

    public BlockDeviceObserver(PartitionListener listener) {
        super("/dev/block/", FileObserver.CREATE | FileObserver.DELETE);
        mVolumes = new HashMap<String, Volume>();
        mListener = listener;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onEvent(int event, String relativePath) {
        Matcher matcher = DEV_PATTERN.matcher(relativePath);
        if (!matcher.matches()) {
            return;
        }

        String volumeName = matcher.group(1);
        boolean logical = matcher.group(2) != null;

        // FIXME: we assume we receive volumes before logical partition block devices.
        if (event == FileObserver.CREATE) {
            if (logical) {
                int logicalId = Integer.valueOf(matcher.group(2));
                Volume volume = mVolumes.get(volumeName);
                if (volume != null) {
                    Partition partition = new Partition(relativePath);
                    volume.addPartition(logicalId, partition);
                    mListener.onPartitionAdded(volume, partition);
                } else {
                    Log.e(Constants.TAG, "No volume found for partition " + relativePath);
                }
            } else {
                Volume volume = new Volume(volumeName);
                mVolumes.put(volumeName, volume);
                mListener.onVolumeAdded(volume);
            }
        } else if (event == FileObserver.DELETE) {
            if (logical) {
                int logicalId = Integer.valueOf(matcher.group(2));
                Volume volume = mVolumes.get(volumeName);
                if (volume != null) {
                    Partition partition = volume.getPartition(logicalId);
                    volume.removePartition(logicalId);
                    mListener.onPartitionRemoved(volume, partition);
                }
            } else {
                Volume volume = mVolumes.get(volumeName);
                mVolumes.remove(volumeName);
                mListener.onVolumeRemoved(volume);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Collection<Volume> getVolumes() {
        return mVolumes.values();
    }
}
