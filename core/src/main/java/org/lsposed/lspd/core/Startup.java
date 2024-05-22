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
 * Copyright (C) 2020 EdXposed Contributors
 * Copyright (C) 2021 - 2022 LSPosed Contributors
 */

package org.lsposed.lspd.core;

import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;

import com.android.internal.os.ZygoteInit;

import org.lsposed.lspd.deopt.PrebuiltMethodsDeopter;
import org.lsposed.lspd.hooker.AttachHooker;
import org.lsposed.lspd.hooker.CrashDumpHooker;
import org.lsposed.lspd.hooker.HandleSystemServerProcessHooker;
import org.lsposed.lspd.hooker.LoadedApkCtorHooker;
import org.lsposed.lspd.hooker.LoadedApkCreateCLHooker;
import org.lsposed.lspd.hooker.OpenDexFileHooker;
import org.lsposed.lspd.impl.LFPosedContext;
import org.lsposed.lspd.impl.LFPosedHelper;
import org.lsposed.lspd.service.ILSPApplicationService;
import org.lsposed.lspd.util.Utils;

import java.util.List;

import dalvik.system.DexFile;
import de.robv.android.fposed.FposedBridge;
import de.robv.android.fposed.FposedInit;

public class Startup {
    private static void startBootstrapHook(boolean isSystem) {
        Utils.logD("startBootstrapHook starts: isSystem = " + isSystem);
        LFPosedHelper.hookMethod(CrashDumpHooker.class, Thread.class, "dispatchUncaughtException", Throwable.class);
        if (isSystem) {
            LFPosedHelper.hookAllMethods(HandleSystemServerProcessHooker.class, ZygoteInit.class, "handleSystemServerProcess");
        } else {
            LFPosedHelper.hookAllMethods(OpenDexFileHooker.class, DexFile.class, "openDexFile");
            LFPosedHelper.hookAllMethods(OpenDexFileHooker.class, DexFile.class, "openInMemoryDexFile");
            LFPosedHelper.hookAllMethods(OpenDexFileHooker.class, DexFile.class, "openInMemoryDexFiles");
        }
        LFPosedHelper.hookConstructor(LoadedApkCtorHooker.class, LoadedApk.class,
                ActivityThread.class, ApplicationInfo.class, CompatibilityInfo.class,
                ClassLoader.class, boolean.class, boolean.class, boolean.class);
        LFPosedHelper.hookMethod(LoadedApkCreateCLHooker.class, LoadedApk.class, "createOrUpdateClassLoaderLocked", List.class);
        LFPosedHelper.hookAllMethods(AttachHooker.class, ActivityThread.class, "attach");
    }

    public static void bootstrapXposed() {
        // Initialize the Xposed framework
        try {
            startBootstrapHook(FposedInit.startsSystemServer);
            FposedInit.loadLegacyModules();
        } catch (Throwable t) {
            Utils.logE("error during Xposed initialization", t);
        }
    }

    public static void initXposed(boolean isSystem, String processName, String appDir, ILSPApplicationService service) {
        // init logger
        ApplicationServiceClient.Init(service, processName);
        FposedBridge.initXResources();
        FposedInit.startsSystemServer = isSystem;
        LFPosedContext.isSystemServer = isSystem;
        LFPosedContext.appDir = appDir;
        LFPosedContext.processName = processName;
        PrebuiltMethodsDeopter.deoptBootMethods(); // do it once for secondary zygote
    }
}
