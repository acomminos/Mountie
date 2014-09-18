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

import android.util.Log;

import com.morlunk.mountie.Constants;
import com.morlunk.mountie.util.Filesystems;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by andrew on 14/09/14.
 */
public class Partition extends BlockDevice {
    private Set<Mount> mMounts;

    public Partition(String name) {
        super(name);
        mMounts = new HashSet<Mount>();
    }

    /**
     * Attempts to mount the partition by iterating over all possible filesystems.
     * On a regular Linux system with util-linux, this would be probed by blkid.
     * @param target The target of the mount.
     */
    public void mount(final String target, final MountListener listener) throws TimeoutException, RootDeniedException, IOException {
        final Shell shell = RootTools.getShell(true);
        final List<String> supportedFilesystems = Filesystems.getSupportedFilesystems();
        for (int i = 0; i < supportedFilesystems.size(); i++) {
            final String fs = supportedFilesystems.get(i);
            Log.i(Constants.TAG, "Attempting to mount as " + fs);

            // use atomic wrappers to track success.
            final AtomicBoolean result = new AtomicBoolean(false);
            final AtomicInteger numIters = new AtomicInteger(0);
            Command mountCommand = new CommandCapture(0, "mount -o fmask=0000,dmask=0000 -t " + fs + " " + getPath() + " " + target) {
                @Override
                public void commandCompleted(int id, int exitcode) {
                    super.commandCompleted(id, exitcode);
                    numIters.addAndGet(1);
                    if (exitcode == 0) {
                        result.set(true);
                        Mount mount = new Mount(Partition.this, fs, target, ""); // TODO: options
                        mMounts.add(mount);
                        listener.onMountSuccess(Partition.this, mount);
                    } else {
                        // Throw error once we've tried to mount with every FS type.
                        if (!result.get() && numIters.get() == supportedFilesystems.size()) {
                            listener.onMountError(Partition.this, null);
                        }
                    }
                }
            };
            shell.add(mountCommand);
        }
    }

    public void unmountAll(final UnmountListener listener) throws TimeoutException, RootDeniedException, IOException {
        for (final Mount mount : mMounts) {
            mount.unmount(listener);
        }
        mMounts.clear();
    }

    // TODO: add manual mounting and partitioning

    public boolean isMounted() {
        return mMounts.size() > 0;
    }

    protected void addMount(Mount mount) {
        mMounts.add(mount);
    }

    protected void removeMount(Mount mount) {
        mMounts.remove(mount);
    }

    public Collection<Mount> getMounts() {
        return mMounts;
    }

}
