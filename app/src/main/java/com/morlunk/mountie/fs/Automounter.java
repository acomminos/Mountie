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

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Automatically mounts discovered partitions in /sdcard/mountie.
 * Created by andrew on 17/09/14.
 */
public class Automounter implements PartitionListener, UnmountListener, MountListener {
    private Shell mRootShell;
    private File mDirectory;
    private MountListener mMountListener;
    private UnmountListener mUnmountListener;
    private Set<Mount> mMounts;

    public Automounter(Shell rootShell, File directory, MountListener mountListener, UnmountListener unmountListener) throws IOException, RootDeniedException, TimeoutException {
        mRootShell = rootShell;
        mDirectory = directory;
        mMountListener = mountListener;
        mUnmountListener = unmountListener;
        mMounts = new HashSet<Mount>();
        cleanDirectory(); // Treat directory like a tmpfs and delete+unmount contents.
    }

    public void cleanDirectory() {
        for (final File file : mDirectory.listFiles()) {
            Command mountCommand = new CommandCapture(0, "umount " + file.getAbsolutePath()) {
                @Override
                public void commandCompleted(int id, int exitcode) {
                    super.commandCompleted(id, exitcode);
                    file.delete();
                }
            };
            try {
                mRootShell.add(mountCommand);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onVolumeAdded(Volume volume) {
    }

    @Override
    public void onVolumeRemoved(Volume volume) {
    }

    @Override
    public void onPartitionAdded(Volume volume, Partition partition) {
        try {
            partition.mount(mRootShell, getDeviceMountDir(partition).getAbsolutePath(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPartitionRemoved(Volume volume, final Partition partition) {
        if (partition.isMounted()) {
            try {
                partition.unmountAll(mRootShell, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void unmountAll() {
        for (Mount mount : mMounts) {
            try {
                mount.unmount(mRootShell, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMounts.clear();
    }

    public Collection<Mount> getMounts() {
        return mMounts;
    }

    private File getDeviceMountDir(Partition partition) throws IOException {
        File mountDir = new File(mDirectory, partition.getUUID());
        if (!mountDir.exists() && !mountDir.mkdirs()) {
            throw new IOException("Couldn't create mount dir!");
        }
        return mountDir;
    }

    @Override
    public void onUnmountSuccess(Mount mount) {
        mMounts.remove(mount);
        try {
            getDeviceMountDir(mount.getDevice()).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mUnmountListener.onUnmountSuccess(mount);
    }

    @Override
    public void onUnmountError(Mount mount, Exception e) {
        mUnmountListener.onUnmountError(mount, e);
    }

    @Override
    public void onMountSuccess(Partition partition, Mount mount) {
        mMounts.add(mount);
        mMountListener.onMountSuccess(partition, mount);
    }

    @Override
    public void onMountError(Partition partition, Exception e) {
        mMountListener.onMountError(partition, e);
    }
}
