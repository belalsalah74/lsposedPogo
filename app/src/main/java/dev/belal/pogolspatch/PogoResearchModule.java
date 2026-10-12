package dev.belal.pogolspatch;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * LSPatch-compatible legacy Xposed diagnostic module.
 * Intentionally observational: no catch, throw, animation, or server-state changes.
 */
public final class PogoResearchModule implements IXposedHookLoadPackage {
    private static final String TAG = "PogoLSPatchResearch";
    private static final String TARGET = "com.nianticlabs.pokemongo";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) return;

        log("Target process loaded: " + lpparam.packageName + " process=" + lpparam.processName
                + " sdk=" + Build.VERSION.SDK_INT + " abi=" + Build.SUPPORTED_ABIS[0]);

        // Record Java-side lifecycle/classloader information only. This is a baseline to
        // distinguish Java hooks from Unity IL2CPP methods, which Xposed cannot hook directly.
        try {
            XposedBridge.hookAllMethods(Application.class, "attach", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    Context context = (Context) param.args[0];
                    ClassLoader loader = context.getClassLoader();
                    log("Application.attach complete; loader=" + loader);
                    log("UnityPlayer class present=" + classPresent("com.unity3d.player.UnityPlayer", loader));
                    log("Assembly-CSharp Java class present=" + classPresent("Assembly-CSharp", loader));
                    logLoadedNativeLibraries();
                }
            });
        } catch (Throwable t) {
            log("Could not install Application.attach observer: " + t);
        }
    }

    private static boolean classPresent(String name, ClassLoader loader) {
        try { Class.forName(name, false, loader); return true; }
        catch (Throwable ignored) { return false; }
    }

    private static void logLoadedNativeLibraries() {
        Set<String> hits = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/maps"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".so") && (line.contains("libmain") || line.contains("il2cpp")
                        || line.contains("unity") || line.contains("Pokemongo"))) {
                    int slash = line.indexOf('/');
                    hits.add(slash >= 0 ? line.substring(slash) : line);
                }
            }
        } catch (Throwable t) {
            log("Unable to inspect process maps: " + t);
        }
        for (String path : hits) log("Mapped native library: " + path);
    }

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
        Log.i(TAG, message);
    }
}
