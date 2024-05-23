/*
 * This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2021 LSPosed Contributors
 */

package org.lsposed.manager.receivers;

import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.system.Os;

import org.lsposed.lspd.ILFPManagerService;

public class LFPManagerServiceHolder implements IBinder.DeathRecipient {
    private static LFPManagerServiceHolder holder = null;
    private static ILFPManagerService service = null;

    public static void init(IBinder binder) {
        if (holder == null) {
            holder = new LFPManagerServiceHolder(binder);
        }
    }

    public static ILFPManagerService getService() {
        return service;
    }

    private LFPManagerServiceHolder(IBinder binder) {
        linkToDeath(binder);
        service = ILFPManagerService.Stub.asInterface(binder);
    }

    private void linkToDeath(IBinder binder) {
        try {
            binder.linkToDeath(this, 0);
        } catch (RemoteException e) {
            binderDied();
        }
    }

    @Override
    public void binderDied() {
        System.exit(0);
        Process.killProcess(Os.getpid());
    }
}
