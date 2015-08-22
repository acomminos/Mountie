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

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.morlunk.mountie.fs.BlockDeviceObserver;
import com.morlunk.mountie.fs.Mount;
import com.morlunk.mountie.fs.MountException;
import com.morlunk.mountie.fs.MountListener;
import com.morlunk.mountie.fs.Partition;
import com.morlunk.mountie.fs.UnmountListener;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Shell;

import java.io.File;

/**
 * Manages the automounting of existing block devices and observation of future device additions.
 * Enters the foreground when one or more devices are mounted; otherwise, its lifecycle is
 * dependent on invocations from {@link com.morlunk.mountie.UsbHotplugReceiver}. When not in the
 * foreground, the service can stop at any time. However, UsbHotplugReceiver ensures that an
 * instance is created whenever we detect a new USB device.
 */
public class MountieService extends Service implements MountieNotification.Listener, MountListener, UnmountListener {
    /**
     * Makes the foreground status of this service dependent on USB hotplug broadcasts.
     */
    public static final String PREF_USB_LIFECYCLE = "usb_lifecycle";
    public static final boolean DEFAULT_USB_LIFECYCLE = true;

    public static final String MOUNT_DIR = "mountie";
    private BlockDeviceObserver mBlockDeviceObserver;
    private Automounter mAutomounter;
    private MountieNotification mNotification;
    private Shell mRootShell;
    private SharedPreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            mRootShell = RootTools.getShell(true);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.requires_root, Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File mountDir = new File(Environment.getExternalStorageDirectory(), MOUNT_DIR);
            if (!mountDir.exists() && !mountDir.mkdir()) {
                Toast.makeText(this, R.string.failed_mkdir, Toast.LENGTH_LONG).show();
                stopSelf();
                return;
            }

            mAutomounter = new Automounter(mRootShell, MOUNT_DIR, this, this);
        }

        mBlockDeviceObserver = new BlockDeviceObserver(mRootShell, mAutomounter);
        mBlockDeviceObserver.startWatching();
        mNotification = new MountieNotification(this, this);
    }

    @Override
    public void onDestroy() {
        if (mAutomounter != null) {
            mAutomounter.unmountAll();
        }
        if (mBlockDeviceObserver != null) {
            mBlockDeviceObserver.stopWatching();
        }
        if (mNotification != null) {
            mNotification.hide();
            mNotification.unregister();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder(this);
    }

    @Override
    public void onMountSuccess(Partition partition, Mount mount) {
        mNotification.setTicker(getString(R.string.mounted,
                mount.getDevice().getReadableName()));
        mNotification.setMounts(mAutomounter.getMounts());
        mNotification.show();
    }

    @Override
    public void onMountError(Partition partition, MountException e) {
        e.printStackTrace();
        mNotification.setTicker(getString(R.string.mount_error,
                partition.getReadableName()));
        mNotification.show();

        if (mPreferences.getBoolean(PREF_USB_LIFECYCLE, DEFAULT_USB_LIFECYCLE) &&
                mAutomounter.getMounts().size() == 0) {
            mNotification.hide();
        }
    }

    @Override
    public void onUnmountSuccess(Mount mount) {
        mNotification.setTicker(getString(R.string.unmounted,
                mount.getDevice().getReadableName()));
        mNotification.setMounts(mAutomounter.getMounts());
        mNotification.show();

        if (mPreferences.getBoolean(PREF_USB_LIFECYCLE, DEFAULT_USB_LIFECYCLE) &&
                mAutomounter.getMounts().size() == 0) {
            mNotification.hide();
        }
    }

    @Override
    public void onUnmountError(Mount mount, Exception e) {
        mNotification.setTicker(getString(R.string.unmount_error,
                mount.getDevice().getReadableName()));
        mNotification.show();

        if (mPreferences.getBoolean(PREF_USB_LIFECYCLE, DEFAULT_USB_LIFECYCLE) &&
                mAutomounter.getMounts().size() == 0) {
            mNotification.hide();
        }
    }

    @Override
    public void unmountAll() {
        mAutomounter.unmountAll();
    }

    public static class LocalBinder extends Binder {
        private MountieService mService;

        public LocalBinder(MountieService service) {
            mService = service;
        }

        public MountieService getService() {
            return mService;
        }
    }

    public void detectDevices() {
        mBlockDeviceObserver.detectDevices();
    }
}
