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

package org.lsposed.manager;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import org.lsposed.lspd.ILFPManagerService;
import org.lsposed.lspd.models.Application;
import org.lsposed.lspd.models.UserInfo;
import org.lsposed.manager.adapters.ScopeAdapter;
import org.lsposed.manager.receivers.LFPManagerServiceHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    public static boolean isBinderAlive() {
        return LFPManagerServiceHolder.getService() != null;
    }

    public static int getXposedApiVersion() {
        try {
            return LFPManagerServiceHolder.getService().getXposedApiVersion();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return -1;
        }
    }

    public static String getXposedVersionName() {
        try {
            return LFPManagerServiceHolder.getService().getXposedVersionName();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return "";
        }
    }

    public static int getXposedVersionCode() {
        try {
            return LFPManagerServiceHolder.getService().getXposedVersionCode();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return -1;
        }
    }

    public static List<PackageInfo> getInstalledPackagesFromAllUsers(int flags, boolean filterNoProcess) {
        List<PackageInfo> list = new ArrayList<>();
        try {
            list.addAll(LFPManagerServiceHolder.getService().getInstalledPackagesFromAllUsers(flags, filterNoProcess).getList());
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
        }
        return list;
    }

    public static String[] getEnabledModules() {
        try {
            return LFPManagerServiceHolder.getService().enabledModules();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return new String[0];
        }
    }

    public static boolean setModuleEnabled(String packageName, boolean enable) {
        try {
            return enable ? LFPManagerServiceHolder.getService().enableModule(packageName) : LFPManagerServiceHolder.getService().disableModule(packageName);
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean setModuleScope(String packageName, boolean legacy, Set<ScopeAdapter.ApplicationWithEquals> applications) {
        try {
            List<Application> list = new ArrayList<>();
            applications.forEach(application -> {
                Application app = new Application();
                app.userId = application.userId;
                app.packageName = application.packageName;
                list.add(app);
            });
            if (legacy) {
                Application app = new Application();
                app.userId = 0;
                app.packageName = packageName;
                list.add(app);
            }
            return LFPManagerServiceHolder.getService().setModuleScope(packageName, list);
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static List<ScopeAdapter.ApplicationWithEquals> getModuleScope(String packageName) {
        List<ScopeAdapter.ApplicationWithEquals> list = new ArrayList<>();
        try {
            var applications = LFPManagerServiceHolder.getService().getModuleScope(packageName);
            if (applications == null) {
                return list;
            }
            applications.forEach(application -> {
                if (!application.packageName.equals(packageName)) {
                    list.add(new ScopeAdapter.ApplicationWithEquals(application));
                }
            });
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
        }
        return list;
    }

    public static boolean enableStatusNotification() {
        try {
            return LFPManagerServiceHolder.getService().enableStatusNotification();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean setEnableStatusNotification(boolean enabled) {
        try {
            LFPManagerServiceHolder.getService().setEnableStatusNotification(enabled);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean isVerboseLogEnabled() {
        try {
            return LFPManagerServiceHolder.getService().isVerboseLog();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean setVerboseLogEnabled(boolean enabled) {
        try {
            LFPManagerServiceHolder.getService().setVerboseLog(enabled);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean isLogWatchdogEnabled() {
        try {
            return LFPManagerServiceHolder.getService().isLogWatchdogEnabled();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean setLogWatchdog(boolean enabled) {
        try {
            LFPManagerServiceHolder.getService().setLogWatchdog(enabled);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static ParcelFileDescriptor getLog(boolean verbose) {
        try {
            return verbose ? LFPManagerServiceHolder.getService().getVerboseLog() : LFPManagerServiceHolder.getService().getModulesLog();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    public static boolean clearLogs(boolean verbose) {
        try {
            return LFPManagerServiceHolder.getService().clearLogs(verbose);
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static PackageInfo getPackageInfo(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        try {
            var info = LFPManagerServiceHolder.getService().getPackageInfo(packageName, flags, userId);
            if (info == null) throw new PackageManager.NameNotFoundException();
            return info;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            throw new PackageManager.NameNotFoundException();
        }
    }

    public static boolean forceStopPackage(String packageName, int userId) {
        try {
            LFPManagerServiceHolder.getService().forceStopPackage(packageName, userId);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean reboot() {
        try {
            LFPManagerServiceHolder.getService().reboot();
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean uninstallPackage(String packageName, int userId) {
        try {
            return LFPManagerServiceHolder.getService().uninstallPackage(packageName, userId);
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean isSepolicyLoaded() {
        try {
            return LFPManagerServiceHolder.getService().isSepolicyLoaded();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static List<UserInfo> getUsers() {
        try {
            return LFPManagerServiceHolder.getService().getUsers();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    public static boolean installExistingPackageAsUser(String packageName, int userId) {
        final int INSTALL_SUCCEEDED = 1;
        try {
            var ret = LFPManagerServiceHolder.getService().installExistingPackageAsUser(packageName, userId);
            return ret == INSTALL_SUCCEEDED;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean isMagiskInstalled() {
        var path = System.getenv("PATH");
        if (path == null) return false;
        else return Arrays.stream(path.split(File.pathSeparator))
                .anyMatch(str -> new File(str, "magisk").exists());
    }

    public static boolean systemServerRequested() {
        try {
            return LFPManagerServiceHolder.getService().systemServerRequested();
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean dex2oatFlagsLoaded() {
        try {
            return LFPManagerServiceHolder.getService().dex2oatFlagsLoaded();
        } catch (RemoteException e) {
            return false;
        }
    }

    public static int startActivityAsUserWithFeature(Intent intent, int userId) {
        try {
            return LFPManagerServiceHolder.getService().startActivityAsUserWithFeature(intent, userId);
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return -1;
        }
    }

    public static List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
        List<ResolveInfo> list = new ArrayList<>();
        try {
            list.addAll(LFPManagerServiceHolder.getService().queryIntentActivitiesAsUser(intent, flags, userId).getList());
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
        }
        return list;
    }

    public static boolean setHiddenIcon(boolean hide) {
        try {
            LFPManagerServiceHolder.getService().setHiddenIcon(hide);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static String getApi() {
        try {
            return LFPManagerServiceHolder.getService().getApi();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return e.toString();
        }
    }

    public static List<String> getDenyListPackages() {
        List<String> list = new ArrayList<>();
        try {
            list.addAll(LFPManagerServiceHolder.getService().getDenyListPackages());
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
        }
        return list;
    }

    public static void flashZip(String zipPath, ParcelFileDescriptor outputStream) {
        try {
            LFPManagerServiceHolder.getService().flashZip(zipPath, outputStream);
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
        }
    }

    public static boolean isDexObfuscateEnabled() {
        try {
            return LFPManagerServiceHolder.getService().getDexObfuscate();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean setDexObfuscateEnabled(boolean enabled) {
        try {
            LFPManagerServiceHolder.getService().setDexObfuscate(enabled);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean isEnableCli() {
        try {
            return LFPManagerServiceHolder.getService().isEnableCli();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean setEnableCli(boolean enabled) {
        try {
            LFPManagerServiceHolder.getService().setEnableCli(enabled);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static int getSessionTimeout() {
        try {
            return LFPManagerServiceHolder.getService().getSessionTimeout();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return -2;
        }
    }

    public static boolean setSessionTimeout(int iTimeout) {
        try {
            LFPManagerServiceHolder.getService().setSessionTimeout(iTimeout);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static int getDex2OatWrapperCompatibility() {
        try {
            return LFPManagerServiceHolder.getService().getDex2OatWrapperCompatibility();
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return ILFPManagerService.DEX2OAT_CRASHED;
        }
    }

    public static boolean getAutomaticAdd(String packageName) {
        try {
            return LFPManagerServiceHolder.getService().getAutomaticAdd(packageName);
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean setAutomaticAdd(String packageName, boolean enable) {
        try {
            LFPManagerServiceHolder.getService().setAutomaticAdd(packageName, enable);
            return true;
        } catch (RemoteException e) {
            Log.e(App.TAG, Log.getStackTraceString(e));
            return false;
        }
    }
}
