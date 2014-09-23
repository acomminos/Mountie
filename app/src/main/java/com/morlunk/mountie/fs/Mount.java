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

import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import java.io.IOException;

/**
 * Created by andrew on 17/09/14.
 */
public class Mount {
    private final Partition mDevice;
    private final String mFilesystem;
    private final String mTarget;
    private final String mOptions;

    protected Mount(Partition device, String filesystem, String target, String options) {
        mDevice = device;
        mFilesystem = filesystem;
        mTarget = target;
        mOptions = options;
    }

    public Partition getDevice() {
        return mDevice;
    }

    public String getFilesystem() {
        return mFilesystem;
    }

    public String getTarget() {
        return mTarget;
    }

    public String getOptions() {
        return mOptions;
    }

    public void unmount(Shell shell, final UnmountListener listener) throws IOException {
        Command mountCommand = new CommandCapture(0, "umount " + getTarget()) {
            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
                if (exitcode == 0) {
                    getDevice().removeMount(Mount.this);
                    listener.onUnmountSuccess(Mount.this);
                } else {
                    listener.onUnmountError(Mount.this, null);
                }
            }
        };
        shell.add(mountCommand);
    }
}
