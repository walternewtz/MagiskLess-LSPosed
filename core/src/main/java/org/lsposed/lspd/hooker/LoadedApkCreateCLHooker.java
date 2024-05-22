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
 * Copyright (C) 2021 LSPosed Contributors
 */

package org.lsposed.lspd.hooker;

import static org.lsposed.lspd.core.ApplicationServiceClient.serviceClient;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;

import org.lsposed.lspd.impl.LFPosedContext;
import org.lsposed.lspd.util.Hookers;
import org.lsposed.lspd.util.MetaDataReader;
import org.lsposed.lspd.util.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.fposed.FC_MethodHook;
import de.robv.android.fposed.FC_MethodReplacement;
import de.robv.android.fposed.FposedBridge;
import de.robv.android.fposed.FposedHelpers;
import de.robv.android.fposed.FposedInit;
import de.robv.android.fposed.callbacks.FC_LoadPackage;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.annotations.AfterInvocation;
import io.github.libxposed.api.annotations.XposedHooker;

@SuppressLint("BlockedPrivateApi")
@XposedHooker
public class LoadedApkCreateCLHooker implements XposedInterface.Hooker {
    private final static Field defaultClassLoaderField;

    private final static Set<LoadedApk> loadedApks = ConcurrentHashMap.newKeySet();

    static {
        Field field = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                field = LoadedApk.class.getDeclaredField("mDefaultClassLoader");
                field.setAccessible(true);
            } catch (Throwable ignored) {
            }
        }
        defaultClassLoaderField = field;
    }

    static void addLoadedApk(LoadedApk loadedApk) {
        loadedApks.add(loadedApk);
    }

    @AfterInvocation
    public static void afterHookedMethod(XposedInterface.AfterHookCallback callback) {
        LoadedApk loadedApk = (LoadedApk) callback.getThisObject();

        if (callback.getArgs()[0] != null || !loadedApks.contains(loadedApk)) {
            return;
        }

        try {
            Hookers.logD("LoadedApk#createClassLoader starts");

            String packageName = ActivityThread.currentPackageName();
            String processName = ActivityThread.currentProcessName();
            boolean isFirstPackage = packageName != null && processName != null && packageName.equals(loadedApk.getPackageName());
            if (!isFirstPackage) {
                packageName = loadedApk.getPackageName();
                processName = ActivityThread.currentPackageName();
            } else if (packageName.equals("android")) {
                packageName = "system";
            }

            Object mAppDir = FposedHelpers.getObjectField(loadedApk, "mAppDir");
            ClassLoader classLoader = (ClassLoader) FposedHelpers.getObjectField(loadedApk, "mClassLoader");
            Hookers.logD("LoadedApk#createClassLoader ends: " + mAppDir + " -> " + classLoader);

            if (classLoader == null) {
                return;
            }

            if (!isFirstPackage && !FposedHelpers.getBooleanField(loadedApk, "mIncludeCode")) {
                Hookers.logD("LoadedApk#<init> mIncludeCode == false: " + mAppDir);
                return;
            }

            if (!isFirstPackage && !FposedInit.getLoadedModules().getOrDefault(packageName, Optional.of("")).isPresent()) {
                return;
            }

            FC_LoadPackage.LoadPackageParam lpparam = new FC_LoadPackage.LoadPackageParam(
                    FposedBridge.sLoadedPackageCallbacks);
            lpparam.packageName = packageName;
            lpparam.processName = processName;
            lpparam.classLoader = classLoader;
            lpparam.appInfo = loadedApk.getApplicationInfo();
            lpparam.isFirstApplication = isFirstPackage;

            if (isFirstPackage && FposedInit.getLoadedModules().getOrDefault(packageName, Optional.empty()).isPresent()) {
                hookNewXSP(lpparam);
            }

            IBinder moduleBinder = serviceClient.requestModuleBinder(lpparam.packageName);
            if (moduleBinder != null) {
                // Let the module to receive the binder for access to xposedservice
                // Hook is only for module, not app
                XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "getSystemService", String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (((String)param.args[0]).equals("LSPosed")) {
                            param.setResult(moduleBinder);
                        }
                    }
                });
            }

            Hookers.logD("Call handleLoadedPackage: packageName=" + lpparam.packageName + " processName=" + lpparam.processName + " isFirstPackage=" + isFirstPackage + " classLoader=" + lpparam.classLoader + " appInfo=" + lpparam.appInfo);
            FC_LoadPackage.callAll(lpparam);

            LFPosedContext.callOnPackageLoaded(new XposedModuleInterface.PackageLoadedParam() {
                @NonNull
                @Override
                public String getPackageName() {
                    return loadedApk.getPackageName();
                }

                @NonNull
                @Override
                public ApplicationInfo getApplicationInfo() {
                    return loadedApk.getApplicationInfo();
                }

                @NonNull
                @Override
                public ClassLoader getDefaultClassLoader() {
                    try {
                        return (ClassLoader) defaultClassLoaderField.get(loadedApk);
                    } catch (Throwable t) {
                        throw new IllegalStateException(t);
                    }
                }

                @NonNull
                @Override
                public ClassLoader getClassLoader() {
                    return classLoader;
                }

                @Override
                public boolean isFirstPackage() {
                    return isFirstPackage;
                }
            });
        } catch (Throwable t) {
            Hookers.logE("error when hooking LoadedApk#createClassLoader", t);
        } finally {
            loadedApks.remove(loadedApk);
        }
    }

    private static void hookNewXSP(FC_LoadPackage.LoadPackageParam lpparam) {
        int xposedminversion = -1;
        boolean xposedsharedprefs = false;
        try {
            Map<String, Object> metaData = MetaDataReader.getMetaData(new File(lpparam.appInfo.sourceDir));
            Object minVersionRaw = metaData.get("fposedminversion");
            if (minVersionRaw instanceof Integer) {
                xposedminversion = (Integer) minVersionRaw;
            } else if (minVersionRaw instanceof String) {
                xposedminversion = MetaDataReader.extractIntPart((String) minVersionRaw);
            }
            xposedsharedprefs = metaData.containsKey("xposedsharedprefs");
        } catch (NumberFormatException | IOException e) {
            Hookers.logE("ApkParser fails", e);
        }

        if (xposedminversion > 92 || xposedsharedprefs) {
            Utils.logI("New modules detected, hook preferences");
            FposedHelpers.findAndHookMethod("android.app.ContextImpl", lpparam.classLoader, "checkMode", int.class, new FC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (((int) param.args[0] & 1/*Context.MODE_WORLD_READABLE*/) != 0) {
                        param.setThrowable(null);
                    }
                }
            });
            FposedHelpers.findAndHookMethod("android.app.ContextImpl", lpparam.classLoader, "getPreferencesDir", new FC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    return new File(serviceClient.getPrefsPath(lpparam.packageName));
                }
            });
        }
    }
}
