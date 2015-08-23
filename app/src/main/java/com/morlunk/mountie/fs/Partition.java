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
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by andrew on 14/09/14.
 */
public class Partition extends BlockDevice {
    private static final Pattern LOGICAL_ID_PATTERN = Pattern.compile("sd[a-z]+(\\d+)");
    private Set<Mount> mMounts;
    private String mLabel;
    private String mUUID;
    private String mFilesystem;

    public Partition(String path, String label, String uuid, String filesystem) {
        super(path);
        mLabel = label;
        mUUID = uuid;
        mFilesystem = filesystem;
        mMounts = new HashSet<Mount>();
    }

    /**
     * Attempts to mount the partition by iterating over all possible filesystems.
     * On a regular Linux system with util-linux, this would be probed by blkid.
     * @param target The target of the mount.
     */
    public void mount(Shell shell, final String target, final MountListener listener) throws IOException {
        Log.i(Constants.TAG, "Attempting to mount " + getPath() + " at " + target + " as " + mFilesystem);

        StringBuilder mountOptionsSB = new StringBuilder("rw,nosuid,nodev");
        if (mFilesystem.equals("vfat") || mFilesystem.equals("ntfs"))
            mountOptionsSB.append(",fmask=0000,dmask=0000,utf8");

        String cmdline = "mount -o " + mountOptionsSB.toString() + " -t " + mFilesystem + " " + getPath() + " " + target;
        Command mountCommand = new CommandCapture(0, cmdline) {
            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
                if (exitcode == 0) {
                    Mount mount = new Mount(Partition.this, mFilesystem, target, ""); // TODO: options
                    mMounts.add(mount);
                    listener.onMountSuccess(Partition.this, mount);
                } else {
                    listener.onMountError(Partition.this, new MountException(toString()));
                }
            }
        };
        shell.add(mountCommand);
    }

    public void unmountAll(Shell rootShell, final UnmountListener listener) throws IOException {
        for (final Mount mount : mMounts) {
            mount.unmount(rootShell, listener);
        }
        mMounts.clear();
    }

    // TODO: add manual mounting and partitioning

    /**
     * Returns the partition's label if set, otherwise returns its UUID.
     * @return either the label or UUID, depending on nullity
     */
    public String getReadableName() {
        if (mLabel != null) {
            return mLabel;
        } else {
            return mUUID;
        }
    }

    public String getLabel() {
        return mLabel;
    }

    public String getUUID() {
        return mUUID;
    }

    public String getFilesystem() {
        return mFilesystem;
    }

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

    public String getVolumeName() {
        return getName().replaceAll("(sd[a-z]+)[0-9]*", "$1");
    }

    public int getLogicalId() {
        Matcher matcher = LOGICAL_ID_PATTERN.matcher(getName());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return 0;
        }
    }
}
