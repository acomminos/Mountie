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

import java.util.List;

/**
 * Created by andrew on 14/09/14.
 */
public class BlockDevice {
    private static final String BLOCK_DEVICE_DIR = "/dev/block/";

    private final String mName;

    public BlockDevice(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return BLOCK_DEVICE_DIR + mName;
    }
}
