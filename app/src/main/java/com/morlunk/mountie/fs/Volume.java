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

import android.util.SparseArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by andrew on 15/09/14.
 */
public class Volume extends BlockDevice {
    private final Map<Integer, Partition> mPartitions;
//    private final int mMajorId;

    public Volume(String name) {
        super(name);
        mPartitions = new HashMap<Integer, Partition>();
    }

    public void addPartition(int logicalId, Partition partition) {
        mPartitions.put(logicalId, partition);
    }

    public Collection<Partition> getPartitions() {
        return mPartitions.values();
    }

    public void removePartition(int logicalId) {
        mPartitions.remove(logicalId);
    }

    public Partition getPartition(int logicalId) {
        return mPartitions.get(logicalId);
    }
}
