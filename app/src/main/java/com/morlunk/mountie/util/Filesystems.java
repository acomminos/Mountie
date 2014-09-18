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

package com.morlunk.mountie.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class to obtain a list of filesystems.
 * Created by andrew on 01/09/14.
 */
public class Filesystems {

    /**
     * Obtains a list of supported filesystem names from /proc/filesystems.
     * @return A list of filesystems supported by the kernel.
     */
    public static List<String> getSupportedFilesystems() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("/proc/filesystems"));
        } catch (FileNotFoundException e) {
            // We must not be using a Linux kernel, how strange :)
            throw new RuntimeException(e);
        }
        try {
            List<String> filesystems = new LinkedList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("nodev")) {
                    // omit nodev, which are linux virtual filesystems
                    filesystems.add(line.substring(1, line.length())); // Remove leading tab
                }
            }
            return filesystems;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
