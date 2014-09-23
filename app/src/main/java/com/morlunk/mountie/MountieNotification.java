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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.morlunk.mountie.fs.Mount;
import com.morlunk.mountie.fs.Partition;

import java.util.Collection;

/**
 * Created by andrew on 17/09/14.
 */
public class MountieNotification {
    public static final int NOTIFICATION_ID = 0x131;
    private static final String ACTION_UNMOUNT = "unmount";

    private Service mService;
    private Listener mListener;
    private String mTicker;
    private Collection<Mount> mMounts;

    private BroadcastReceiver mButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UNMOUNT.equals(intent.getAction())) {
                mListener.unmountAll();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    };

    public MountieNotification(Service service, Listener listener) {
        mService = service;
        mListener = listener;
        mService.registerReceiver(mButtonReceiver, new IntentFilter(ACTION_UNMOUNT));
    }

    public void show() {
        Notification.Builder builder = new Notification.Builder(mService);
        builder.setSmallIcon(R.drawable.ic_stat_mountie);
        builder.setContentTitle(mService.getString(R.string.app_name));
        builder.setTicker(mTicker);

        if (mMounts != null && mMounts.size() > 0) {
            builder.setContentText(mService.getString(R.string.devs_mounted, mMounts.size()));
            Notification.InboxStyle style = new Notification.InboxStyle();
            for (Mount mount : mMounts) {
                Partition partition = mount.getDevice();
                if (partition.getLabel() != null) {
                    style.addLine(mService.getString(R.string.notify_label, partition.getLabel()));
                }
                style.addLine(mService.getString(R.string.notify_uuid, partition.getUUID()));
                style.addLine(mService.getString(R.string.notify_dev, partition.getVolumeName()));
            }
            builder.setStyle(style);

            PendingIntent buttonIntent = PendingIntent.getBroadcast(mService, 0,
                    new Intent(ACTION_UNMOUNT), PendingIntent.FLAG_CANCEL_CURRENT);
            builder.addAction(R.drawable.ic_action_unmount,
                    mService.getString(R.string.unmount_all), buttonIntent);
        } else {
            builder.setContentText(mService.getString(R.string.no_devs_mounted));
        }

        mService.startForeground(NOTIFICATION_ID, builder.build());
    }

    public void hide() {
        mService.stopForeground(true);
    }

    public void unregister() {
        mService.unregisterReceiver(mButtonReceiver);
    }

    public void setTicker(String ticker) {
        mTicker = ticker;
    }

    public void setMounts(Collection<Mount> mounts) {
        mMounts = mounts;
    }

    /**
     * Created by andrew on 17/09/14.
     */
    public static interface Listener {
        public void unmountAll();
    }
}
