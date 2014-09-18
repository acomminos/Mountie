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

package com.morlunk.mountie.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by andrew on 14/09/14.
 */
public class UsbDriveProvider {
    private Context mContext;
    private Set<Listener> mListeners;
    private List<UsbDevice> mDriveDevices;
    private UsbManager mUsbManager;
    private BroadcastReceiver mDriveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            UsbInterface storageInterface = null;
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                UsbInterface ui = device.getInterface(i);
                if (ui.getInterfaceClass() == UsbConstants.USB_CLASS_MASS_STORAGE) {
                    storageInterface = ui;
                    break;
                }
            }

            if (storageInterface == null) {
                return;
            }

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                for (Listener listener : mListeners) {
                    listener.onDriveAdded(null);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                for (Listener listener : mListeners) {
                    listener.onDriveRemoved(null);
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
    };

    public UsbDriveProvider(Context context) {
        mContext = context;
        mListeners = new LinkedHashSet<Listener>();
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mDriveDevices = new LinkedList<UsbDevice>();
    }

    public void startWatching() {
        for (Map.Entry<String, UsbDevice> entry : mUsbManager.getDeviceList().entrySet()) {
            if (entry.getValue().getDeviceClass() == UsbConstants.USB_CLASS_MASS_STORAGE) {
                mDriveDevices.add(entry.getValue());
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mDriveReceiver, filter);
    }

    public void stopWatching() {
        mDriveDevices.clear();
        mContext.unregisterReceiver(mDriveReceiver);
    }

    public void registerListener(Listener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        mListeners.remove(listener);
    }

    public static interface Listener {
        public void onDriveAdded(String path);
        public void onDriveRemoved(String path);
    }
}
