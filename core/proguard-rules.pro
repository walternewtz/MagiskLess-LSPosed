-keep class de.robv.android.fposed.** {*;}
-keep class io.github.libxposed.** {*;}
-keepattributes RuntimeVisibleAnnotations
-keep class android.** { *; }
-keepclasseswithmembers,includedescriptorclasses class * {
    native <methods>;
}
-keepclassmembers class org.lsposed.lspd.impl.LFPosedContext {
    public <methods>;
}
-keepclassmembers class org.lsposed.lspd.impl.LFPosedHookCallback {
    public <methods>;
}
-keep,allowoptimization,allowobfuscation @io.github.libxposed.api.annotations.* class * {
    @io.github.libxposed.api.annotations.BeforeInvocation <methods>;
    @io.github.libxposed.api.annotations.AfterInvocation <methods>;
}
-keepclassmembers class org.lsposed.lspd.impl.LFPosedBridge$NativeHooker {
    <init>(java.lang.reflect.Executable);
    callback(...);
}
-keepclassmembers class org.lsposed.lspd.impl.LFPosedBridge$HookerCallback {
    final *** beforeInvocation;
    final *** afterInvocation;
    HookerCallback(...);
}
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}
-repackageclasses
-allowaccessmodification
-dontwarn org.slf4j.impl.StaticLoggerBinder
