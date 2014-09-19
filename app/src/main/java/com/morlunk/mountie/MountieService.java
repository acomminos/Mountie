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
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import com.morlunk.mountie.fs.Automounter;
import com.morlunk.mountie.fs.BlockDeviceObserver;
import com.morlunk.mountie.fs.Mount;
import com.morlunk.mountie.fs.MountListener;
import com.morlunk.mountie.fs.NotificationListener;
import com.morlunk.mountie.fs.Partition;
import com.morlunk.mountie.fs.UnmountListener;
import com.stericson.RootTools.exceptions.RootDeniedException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MountieService extends Service implements NotificationListener, MountListener, UnmountListener {
    public static final String MOUNT_DIR = "/sdcard/mountie";
    private BlockDeviceObserver mBlockDeviceObserver;
    private Automounter mAutomounter;
    private MountieNotification mNotification;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File mountDir = new File(MOUNT_DIR);
            if (!mountDir.exists() && !mountDir.mkdir()) {
                // TODO notify user of problem
            }

            try {
                mAutomounter = new Automounter(mountDir, this, this);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RootDeniedException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        mBlockDeviceObserver = new BlockDeviceObserver(mAutomounter);
        mNotification = new MountieNotification(this, this);
        mNotification.show();

        mBlockDeviceObserver.startWatching();
    }

    @Override
    public void onDestroy() {
        mAutomounter.unmountAll();
        mBlockDeviceObserver.stopWatching();
        mNotification.hide();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder(this);
    }

    @Override
    public void onMountSuccess(Partition partition, Mount mount) {
        mNotification.setTicker(getString(R.string.mounted_at,
                mount.getDevice().getName(), mount.getTarget()));
        mNotification.setMounts(mAutomounter.getMounts());
        mNotification.show();
    }

    @Override
    public void onMountError(Partition partition, Exception e) {
        mNotification.setTicker(getString(R.string.mount_error,
                partition.getName()));
        mNotification.show();
    }

    @Override
    public void onUnmountSuccess(Mount mount) {
        mNotification.setTicker(getString(R.string.unmounted,
                mount.getDevice().getName()));
        mNotification.setMounts(mAutomounter.getMounts());
        mNotification.show();
    }

    @Override
    public void onUnmountError(Mount mount, Exception e) {
        mNotification.setTicker(getString(R.string.unmount_error,
                mount.getDevice().getName()));
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
