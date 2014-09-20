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

import com.morlunk.mountie.fs.Automounter;
import com.morlunk.mountie.fs.BlockDeviceObserver;
import com.morlunk.mountie.fs.Mount;
import com.morlunk.mountie.fs.MountException;
import com.morlunk.mountie.fs.MountListener;
import com.morlunk.mountie.fs.NotificationListener;
import com.morlunk.mountie.fs.Partition;
import com.morlunk.mountie.fs.UnmountListener;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Shell;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MountieService extends Service implements NotificationListener, MountListener, UnmountListener {
    /**
     * Makes the lifecycle of this service dependent on USB hotplug broadcasts.
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
            stopSelf(); // TODO: show error
        }

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File mountDir = new File(Environment.getExternalStorageDirectory(), MOUNT_DIR);
            if (!mountDir.exists() && !mountDir.mkdir()) {
                // TODO notify user of problem
            }
            mAutomounter = new Automounter(mRootShell, mountDir, this, this);
        }
        mBlockDeviceObserver = new BlockDeviceObserver(mRootShell, mAutomounter);
        mNotification = new MountieNotification(this, this);
        mNotification.show();

        mBlockDeviceObserver.startWatching();
    }

    @Override
    public void onDestroy() {
        mAutomounter.unmountAll();
        mBlockDeviceObserver.stopWatching();
        mNotification.hide();
        mNotification.unregister();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder(this);
    }

    @Override
    public void onMountSuccess(Partition partition, Mount mount) {
        mNotification.setTicker(getString(R.string.mounted_at,
                mount.getDevice().getReadableName(), mount.getTarget()));
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
            stopSelf();
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
            stopSelf();
        }
    }

    @Override
    public void onUnmountError(Mount mount, Exception e) {
        mNotification.setTicker(getString(R.string.unmount_error,
                mount.getDevice().getReadableName()));
        mNotification.show();
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
}
