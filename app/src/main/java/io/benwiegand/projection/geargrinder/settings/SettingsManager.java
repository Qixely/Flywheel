package io.benwiegand.projection.geargrinder.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.StringRes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.benwiegand.projection.geargrinder.R;

public class SettingsManager {
    private static final String TAG = SettingsManager.class.getSimpleName();
    private static final String PREFERENCE_NAME = "io.benwiegand.projection.geargrinder_preferences";
    private static final String RAW_BYTES_MULTI_SEPARATOR = ",";

    public static final int LIABILITY_AGREEMENT_VERSION_CURRENT = 1;
    public static final int LIABILITY_AGREEMENT_VERSION_NONE = 0;

    public static final int SETUP_VERSION_CURRENT = 1;
    public static final int SETUP_VERSION_NONE = 0;

    // These affect ONLY app-specific virtual displays, never the projection video or dashboard.
    public static final int PROJECTED_APP_DPI_DEFAULT = 240;
    public static final int PROJECTED_APP_DPI_MIN = 240;
    public static final int PROJECTED_APP_DPI_MAX = 480;
    public static final int PROJECTED_APP_DPI_MAX_ADVANCED = 640;
    public static final int PROJECTED_APP_DPI_STEP = 20;

    private static final String KEY_DEFAULT_PROJECTED_APP_DPI = "projected_app_dpi_default";
    private static final String KEY_ADVANCED_PROJECTED_APP_DPI = "projected_app_dpi_advanced";
    private static final String KEY_APP_PROJECTED_DPI_PREFIX = "projected_app_dpi_override.";

    private final Context context;
    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isValidProjectedAppDpi(int dpi) {
        return dpi >= PROJECTED_APP_DPI_MIN
                && dpi <= PROJECTED_APP_DPI_MAX_ADVANCED
                && (dpi - PROJECTED_APP_DPI_MIN) % PROJECTED_APP_DPI_STEP == 0;
    }

    /** Clamp and snap unexpected values to the nearest supported 20-DPI step. */
    public static int normalizeProjectedAppDpi(int dpi) {
        int bounded = Math.max(PROJECTED_APP_DPI_MIN,
                Math.min(PROJECTED_APP_DPI_MAX_ADVANCED, dpi));
        int steps = (bounded - PROJECTED_APP_DPI_MIN + PROJECTED_APP_DPI_STEP / 2)
                / PROJECTED_APP_DPI_STEP;
        return PROJECTED_APP_DPI_MIN + steps * PROJECTED_APP_DPI_STEP;
    }

    public int getDefaultProjectedAppDpi() {
        int dpi = prefs.getInt(KEY_DEFAULT_PROJECTED_APP_DPI, PROJECTED_APP_DPI_DEFAULT);
        return isValidProjectedAppDpi(dpi) ? dpi : PROJECTED_APP_DPI_DEFAULT;
    }

    public void setDefaultProjectedAppDpi(int dpi) {
        prefs.edit().putInt(KEY_DEFAULT_PROJECTED_APP_DPI,
                normalizeProjectedAppDpi(dpi)).apply();
    }

    public boolean isAdvancedProjectedAppDpiEnabled() {
        return prefs.getBoolean(KEY_ADVANCED_PROJECTED_APP_DPI, false);
    }

    public void setAdvancedProjectedAppDpiEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ADVANCED_PROJECTED_APP_DPI, enabled).apply();
    }

    /** A zero override means this application inherits the shared app DPI. */
    public int getProjectedAppDpiOverride(String packageName) {
        if (packageName == null || packageName.isEmpty()) return 0;
        int dpi = prefs.getInt(KEY_APP_PROJECTED_DPI_PREFIX + packageName, 0);
        return isValidProjectedAppDpi(dpi) ? dpi : 0;
    }

    public int getEffectiveProjectedAppDpi(String packageName) {
        int override = getProjectedAppDpiOverride(packageName);
        return override != 0 ? override : getDefaultProjectedAppDpi();
    }

    public void setProjectedAppDpiOverride(String packageName, int dpi) {
        if (packageName == null || packageName.isEmpty()) throw new IllegalArgumentException("package name required");
        if (dpi != 0) dpi = normalizeProjectedAppDpi(dpi);
        SharedPreferences.Editor edit = prefs.edit();
        String key = KEY_APP_PROJECTED_DPI_PREFIX + packageName;
        if (dpi == 0) edit.remove(key);
        else edit.putInt(key, dpi);
        edit.apply();
    }

    /** Only entries explicitly customized by the user. */
    public Set<String> getProjectedAppDpiOverriddenPackages() {
        Set<String> packages = new HashSet<>();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(KEY_APP_PROJECTED_DPI_PREFIX)) continue;
            String packageName = key.substring(KEY_APP_PROJECTED_DPI_PREFIX.length());
            if (getProjectedAppDpiOverride(packageName) != 0) packages.add(packageName);
        }
        return packages;
    }

    public void resetProjectedAppDpiSettings() {
        SharedPreferences.Editor edit = prefs.edit()
                .remove(KEY_DEFAULT_PROJECTED_APP_DPI)
                .remove(KEY_ADVANCED_PROJECTED_APP_DPI);
        for (String packageName : getProjectedAppDpiOverriddenPackages()) {
            edit.remove(KEY_APP_PROJECTED_DPI_PREFIX + packageName);
        }
        edit.apply();
    }

    public OperationalMode getOperationalMode() {
        return OperationalMode.parse(context, getString(R.string.key_operational_mode));
    }

    public PrivilegeMode getPrivilegeMode() {
        return PrivilegeMode.parse(context, getString(R.string.key_privilege_mode));
    }

    public boolean savePrivilegeMode(PrivilegeMode privilegeMode) {
        return saveString(R.string.key_privilege_mode, context.getString(privilegeMode.preferenceValueRes()));
    }

    public boolean allowsStartProjectionWhenLocked() {
        return getBool(R.string.key_start_projection_when_locked, R.string.start_projection_when_locked_default);
    }

    public int getProjectionResumeGracePeriod() {
        return castInt(R.string.key_projection_resume_grace_period, R.string.projection_grace_period_default);
    }

    public int getVideoBitrateMode() {
        return castInt(R.string.key_video_bitrate_mode, R.string.video_bitrate_mode_default);
    }

    public int getVideoBitrateCustom() {
        return castInt(R.string.key_video_bitrate_custom, R.string.video_bitrate_custom_default);
    }

    public boolean isVideoFrameRateOptimizationEnabled() {
        return getBool(R.string.key_video_frame_rate_optimization, R.string.video_frame_rate_optimization_default);
    }

    public boolean allowVirtualActivityLauncher() {
        return getBool(R.string.key_allow_virtual_activity_launcher, R.string.allow_virtual_activity_launcher_default);
    }

    public boolean isKeepScreenAwakeEnabled() {
        return getBool(R.string.key_keep_screen_awake, R.string.keep_screen_awake_default);
    }

    public MediaAudioCaptureMode getMediaAudioCaptureMode() {
        return MediaAudioCaptureMode.parse(context, getString(R.string.key_media_audio_capture_mode));
    }

    public boolean allowCapturePhoneAudio() {
        return getBool(R.string.key_phone_audio_capture, R.string.phone_audio_capture_default);
    }

    public boolean isPopupNotificationsEnabled() {
        return getBool(R.string.key_popup_notifications, R.string.popup_notifications_default);
    }

    public int getPopupNotificationDuration() {
        return castInt(R.string.key_popup_notification_duration, R.string.popup_notification_duration_default);
    }

    public boolean isNotificationDrawerEnabled() {
        return getBool(R.string.key_notification_drawer, R.string.notification_drawer_default);
    }

    public boolean isPauseMediaDuringNotificationTTSEnabled() {
        return getBool(R.string.key_pause_media_during_notification_tts, R.string.pause_media_during_notification_tts_default);
    }

    public boolean isShowMediaControlsEnabled() {
        return getBool(R.string.key_show_media_controls, R.string.show_media_controls_default);
    }

    public boolean isShowBatteryPercentageEnabled() {
        return getBool(R.string.key_show_battery_percentage, R.string.show_battery_percentage_default);
    }

    public boolean useImportedPhoneKeys() {
        return getBool(R.string.key_use_imported_phone_keys, R.string.use_imported_keys_default);
    }

    public byte[][] getX509CertificateChain(String key) {
        return getRawBytesMulti(key);
    }

    public byte[] getPKCS8PrivateKey(String prefKey) {
        return getRawBytes(prefKey);
    }

    public boolean saveSelfSignedPhoneX509CertificateChain(byte[][] encodedCertChain) {
        return saveRawBytes(R.string.key_self_signed_phone_x509_certificate_chain, encodedCertChain);
    }

    public byte[][] getSelfSignedPhoneX509CertificateChain() {
        return getRawBytesMulti(R.string.key_self_signed_phone_x509_certificate_chain);
    }

    public boolean saveSelfSignedPhonePKCS8PrivateKey(byte[] encodedPrivateKey) {
        return saveRawBytes(R.string.key_self_signed_phone_pkcs8_private_key, encodedPrivateKey);
    }

    public byte[] getSelfSignedPhonePKCS8PrivateKey() {
        return getRawBytes(R.string.key_self_signed_phone_pkcs8_private_key);
    }

    public boolean saveImportedPhoneX509CertificateChain(byte[][] encodedCertChain) {
        return saveRawBytes(R.string.key_imported_phone_x509_certificate_chain, encodedCertChain);
    }

    public byte[][] getImportedPhoneX509CertificateChain() {
        return getRawBytesMulti(R.string.key_imported_phone_x509_certificate_chain);
    }

    public boolean saveImportedPhonePKCS8PrivateKey(byte[] encodedPrivateKey) {
        return saveRawBytes(R.string.key_imported_phone_pkcs8_private_key, encodedPrivateKey);
    }

    public byte[] getImportedPhonePKCS8PrivateKey() {
        return getRawBytes(R.string.key_imported_phone_pkcs8_private_key);
    }

    public boolean saveDockPinned(String config) {
        return saveString(R.string.key_dock_pinned, config);
    }

    public String getDockPinned() {
        return getString(R.string.key_dock_pinned);
    }

    public boolean saveLiabilityAgreementVersion(int version) {
        return saveInt(R.string.key_liability_agreement_version, version);
    }

    public int getLiabilityAgreementVersion() {
        return getInt(R.string.key_liability_agreement_version, LIABILITY_AGREEMENT_VERSION_NONE);
    }

    public boolean saveSetupVersion(int version) {
        return saveInt(R.string.key_setup_version, version);
    }

    public int getSetupVersion() {
        return getInt(R.string.key_setup_version, SETUP_VERSION_NONE);
    }


    private int castInt(@StringRes int key, @StringRes int defaultRes) {
        String stringValue = prefs.getString(context.getString(key), null);
        if (stringValue == null) stringValue = context.getString(defaultRes);

        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            Log.wtf(TAG, "failed to cast preference value to integer", e);
            assert false;

            try {
                return Integer.parseInt(context.getString(defaultRes));
            } catch (NumberFormatException ee) {
                Log.wtf(TAG, "default value failed to parse to int", ee);
                throw new AssertionError(e);
            }
        }
    }

    private boolean getBool(@StringRes int key, @StringRes int defaultRes) {
        boolean defaultValue = Boolean.parseBoolean(context.getString(defaultRes));
        return prefs.getBoolean(context.getString(key), defaultValue);
    }

    private boolean saveString(@StringRes int key, String value) {
        return prefs.edit()
                .putString(context.getString(key), value)
                .commit();
    }

    private String getString(@StringRes int key) {
        return prefs.getString(context.getString(key), null);
    }

    private boolean saveInt(@StringRes int key, int value) {
        return prefs.edit()
                .putInt(context.getString(key), value)
                .commit();
    }

    private int getInt(@StringRes int key, int defaultValue) {
        return prefs.getInt(context.getString(key), defaultValue);
    }

    private boolean saveRawBytes(@StringRes int key, byte[]... multiData) {
        String[] encodedMultiData = new String[multiData.length];
        for (int i = 0; i < multiData.length; i++)
            encodedMultiData[i] = Base64.encodeToString(multiData[i], Base64.NO_WRAP);

        String value = String.join(RAW_BYTES_MULTI_SEPARATOR, encodedMultiData);
        return saveString(key, value);
    }

    private byte[][] getRawBytesMulti(String key) {
        String value = prefs.getString(key, null);
        if (value == null) return null;

        String[] encodedMultiData = value.split(RAW_BYTES_MULTI_SEPARATOR);
        byte[][] multiData = new byte[encodedMultiData.length][];
        for (int i = 0; i < multiData.length; i++)
            multiData[i] = Base64.decode(encodedMultiData[i], Base64.NO_WRAP);

        return multiData;
    }

    private byte[][] getRawBytesMulti(@StringRes int key) {
        return getRawBytesMulti(context.getString(key));
    }

    private byte[] getRawBytes(String key) {
        byte[][] multiData = getRawBytesMulti(key);
        if (multiData == null) return null;
        if (multiData.length == 0) return new byte[0];
        if (multiData.length > 1) throw new AssertionError("expected 1 or 0 byte arrays for preference \"" + key + "\", but found " + multiData.length);
        return multiData[0];
    }

    private byte[] getRawBytes(@StringRes int key) {
        return getRawBytes(context.getString(key));
    }

    public static <T> T enumForPref(Context context, String value, @StringRes int key, @StringRes int defaultRes, List<Pair<Integer, T>> mapping) {
        String defaultValue = context.getString(defaultRes);
        if (value == null) value = defaultValue;

        T defaultMapping = null;
        for (Pair<Integer, T> entry : mapping) {
            String entryValue = context.getString(entry.first);
            if (entryValue.equals(defaultValue)) defaultMapping = entry.second;
            if (entryValue.equals(value)) return entry.second;
        }

        if (defaultMapping == null)
            Log.wtf(TAG, "default value not present in mappings", new AssertionError());

        Log.wtf(TAG, "unhandled value for pref " + context.getString(key) + ": " + value);
        assert false;
        return defaultMapping;
    }

}
