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

package org.lsposed.lspd.core;

import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import org.lsposed.lspd.models.Module;
import org.lsposed.lspd.service.ILFPApplicationService;
import org.lsposed.lspd.util.Utils;

import java.util.Collections;
import java.util.List;

public class ApplicationServiceClient implements ILFPApplicationService, IBinder.DeathRecipient {
    public static ApplicationServiceClient serviceClient = null;

    final ILFPApplicationService service;

    final String processName;

    private ApplicationServiceClient(@NonNull ILFPApplicationService service, @NonNull String processName) throws RemoteException {
        this.service = service;
        this.processName = processName;
        this.service.asBinder().linkToDeath(this, 0);
    }

    synchronized static void Init(ILFPApplicationService service, String niceName) {
        var binder = service.asBinder();
        if (serviceClient == null && binder != null) {
            try {
                serviceClient = new ApplicationServiceClient(service, niceName);
            } catch (RemoteException e) {
                Utils.logE("link to death error: ", e);
            }
        }
    }

    @Override
    public IBinder requestModuleBinder(String name) {
        try {
            return service.requestModuleBinder(name);
        } catch (RemoteException | NullPointerException ignored) {
        }
        return null;
    }

    @Override
    public List<Module> getLegacyModulesList() {
        try {
            return service.getLegacyModulesList();
        } catch (RemoteException | NullPointerException ignored) {
        }
        return Collections.emptyList();
    }

    @Override
    public List<Module> getModulesList() {
        try {
            return service.getModulesList();
        } catch (RemoteException | NullPointerException ignored) {
        }
        return Collections.emptyList();
    }

    @Override
    public String getPrefsPath(String packageName) {
        try {
            return service.getPrefsPath(packageName);
        } catch (RemoteException | NullPointerException ignored) {
        }
        return null;
    }

    @Override
    public ParcelFileDescriptor requestInjectedManagerBinder(List<IBinder> binder) {
        try {
            return service.requestInjectedManagerBinder(binder);
        } catch (RemoteException | NullPointerException ignored) {
        }
        return null;
    }

    @Override
    public int requestCLIBinder(String sPin, List<IBinder> binder) {
        try {
            return service.requestCLIBinder(sPin, binder);
        } catch (RemoteException | NullPointerException ignored) {
        }
        return -1;
    }

    @Override
    public IBinder asBinder() {
        return service.asBinder();
    }

    @Override
    public void binderDied() {
        service.asBinder().unlinkToDeath(this, 0);
        serviceClient = null;
    }
}
