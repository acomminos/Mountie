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

package com.morlunk.mountie.jni;

/**
 * Created by andrew on 01/09/14.
 */
public class Native {
    static {
        System.loadLibrary("mountie_jni");
    }

    public static native String get_device_serial(String path);
    public static native String get_device_model(String path);

    // cryptsetup
//    public static native long crypt_init(String path);
//    public static native int crypt_free(long cd);
//    public static native int crypt_load(long cd, String type, ByteBuffer params);
//    public static native int crypt_activate_by_passphrase(long cd, String name, int slot, String passphrase, int flags);
//    public static native int crypt_deactivate(long cd, String name);
//    public static native String crypt_get_dir();
}
