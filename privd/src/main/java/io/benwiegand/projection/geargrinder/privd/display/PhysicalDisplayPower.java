package io.benwiegand.projection.geargrinder.privd.display;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Powers off the built-in panel without putting Android to sleep. This must run
 * in privd (root or shell), not in the application's unprivileged process.
 * The virtual projection display is intentionally not affected.
 */
public final class PhysicalDisplayPower {
    private static final String TAG = "PhysicalDisplayPower";
    private static final int POWER_MODE_OFF = 0;
    private static final int POWER_MODE_NORMAL = 2;
    private static final long COMMAND_TIMEOUT_MS = 3000;

    private boolean panelOffByUs = false;

    public synchronized boolean setPowered(boolean on) {
        // Android 15 introduced a shell command that targets the physical
        // display without changing the framework's interactive/lock state.
        boolean success = false;
        if (Build.VERSION.SDK_INT >= 35) success = setViaDisplayCommand(on);
        if (!success) success = setViaSurfaceControl(on);

        if (success) panelOffByUs = !on;
        return success;
    }

    public synchronized void restoreIfNecessary() {
        if (panelOffByUs && !setPowered(true)) {
            Log.e(TAG, "could not restore panel; press the physical power button twice or use adb shell cmd display power-on 0 (Android 15+)");
        }
    }

    private boolean setViaDisplayCommand(boolean on) {
        Process process = null;
        try {
            process = new ProcessBuilder("cmd", "display", on ? "power-on" : "power-off", "0")
                    .redirectErrorStream(true)
                    .start();
            if (!process.waitFor(COMMAND_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                Log.w(TAG, "timed out changing physical display power");
                process.destroyForcibly();
                return false;
            }
            if (process.exitValue() != 0) {
                Log.w(TAG, "cmd display exited with " + process.exitValue());
                return false;
            }
            Log.i(TAG, "physical panel " + (on ? "on" : "off") + " via cmd display");
            return true;
        } catch (IOException | InterruptedException | SecurityException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            Log.w(TAG, "cmd display unavailable", e);
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    private boolean setViaSurfaceControl(boolean on) {
        // Scrcpy-style fallback for builds exposing the hidden SurfaceControl
        // API. Some Android 14 vendor builds move the token methods to
        // DisplayControl; on unsupported devices this fails without altering
        // the normal projection behavior.
        try {
            Class<?> surfaceControl = Class.forName("android.view.SurfaceControl");
            Class<?> tokenProvider = surfaceControl;
            Method idsMethod;
            Method tokenMethod;
            try {
                idsMethod = tokenProvider.getMethod("getPhysicalDisplayIds");
                tokenMethod = tokenProvider.getMethod("getPhysicalDisplayToken", long.class);
            } catch (NoSuchMethodException e) {
                tokenProvider = Class.forName("com.android.server.display.DisplayControl");
                idsMethod = tokenProvider.getMethod("getPhysicalDisplayIds");
                tokenMethod = tokenProvider.getMethod("getPhysicalDisplayToken", long.class);
            }
            long[] ids = (long[]) idsMethod.invoke(null);
            if (ids == null || ids.length == 0) {
                Log.w(TAG, "no physical display IDs");
                return false;
            }
            IBinder token = (IBinder) tokenMethod.invoke(null, ids[0]);
            if (token == null) return false;
            surfaceControl.getMethod("setDisplayPowerMode", IBinder.class, int.class)
                    .invoke(null, token, on ? POWER_MODE_NORMAL : POWER_MODE_OFF);
            Log.i(TAG, "physical panel " + (on ? "on" : "off") + " via SurfaceControl");
            return true;
        } catch (ReflectiveOperationException | LinkageError | SecurityException e) {
            Log.w(TAG, "SurfaceControl panel power unsupported", e);
            return false;
        }
    }
}
