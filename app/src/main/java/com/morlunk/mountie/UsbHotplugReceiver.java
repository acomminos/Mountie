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

package com.morlunk.mountie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.preference.PreferenceManager;

/**
 * Launches MountieService when a USB device has been plugged in.
 * It is Mountie's responsibility to stop itself once all devices have been ejected.
 *
 * Observe that this detection chain depends on the service and inotify setup occurring
 * before the kernel creates the block devices. FIXME.
 *
 * TODO: control this with setting.
 *
 * Created by andrew on 19/09/14.
 */
public class UsbHotplugReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
            context.startService(new Intent(context, MountieService.class));
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
