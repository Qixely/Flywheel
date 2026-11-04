package io.benwiegand.projection.geargrinder.settings.ui.fragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.benwiegand.projection.geargrinder.settings.SettingsManager;

/**
 * Projected APPLICATION scaling, not the car video or Flywheel dashboard.
 * A separate virtual display is created for each projected app, making
 * per-package DPI possible without changing projection resolution.
 */
public class AppDisplayScalingPreferenceFragment extends PreferenceFragmentCompat {
    private static final String SPOTIFY = "com.spotify.music";
    private static final String WAZE = "com.waze";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SettingsManager settings;

    private record AppChoice(String packageName, String label) {
        @Override public String toString() { return label; }
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        settings = new SettingsManager(requireContext());
        buildScreen();
    }

    private void buildScreen() {
        Context context = requireContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(screen);

        Preference explanation = new Preference(context);
        explanation.setTitle("Larger text and controls in projected apps");
        explanation.setSummary("Only Android app windows are scaled. Your Flywheel dashboard, custom Now Playing player, and Ford projection resolution stay unchanged. Adjust while parked; reopen an app to apply changes.");
        explanation.setSelectable(false);
        explanation.setIconSpaceReserved(false);
        screen.addPreference(explanation);

        PreferenceCategory shared = category(context, screen, "Default for projected apps");
        addDpiSlider(shared, "Default app DPI", settings.getDefaultProjectedAppDpi(), dpi -> {
            settings.setDefaultProjectedAppDpi(dpi);
            // Update the inherited-value summaries for Spotify and Waze as well.
            handler.post(this::buildScreen);
            return true;
        });

        SwitchPreferenceCompat advanced = new SwitchPreferenceCompat(context);
        advanced.setTitle("Advanced scaling (up to 640 DPI)");
        advanced.setSummary("Normally limited to 480 DPI. Very large values may switch apps into a phone-style layout.");
        advanced.setIconSpaceReserved(false);
        advanced.setPersistent(false);
        advanced.setChecked(settings.isAdvancedProjectedAppDpiEnabled());
        advanced.setOnPreferenceChangeListener((pref, value) -> {
            settings.setAdvancedProjectedAppDpiEnabled((Boolean) value);
            // Redraw after the click is processed, so the slider endpoints update cleanly.
            handler.post(this::buildScreen);
            return true;
        });
        shared.addPreference(advanced);

        PreferenceCategory perApp = category(context, screen, "Individual apps");
        addAppControls(perApp, "Spotify", SPOTIFY);
        addAppControls(perApp, "Waze", WAZE);

        Set<String> overrides = settings.getProjectedAppDpiOverriddenPackages();
        List<AppChoice> customApps = new ArrayList<>();
        for (String packageName : overrides) {
            if (SPOTIFY.equals(packageName) || WAZE.equals(packageName)) continue;
            customApps.add(new AppChoice(packageName, loadLabel(packageName)));
        }
        Collator collator = Collator.getInstance();
        customApps.sort((a, b) -> collator.compare(a.label(), b.label()));
        for (AppChoice app : customApps) addAppControls(perApp, app.label(), app.packageName());

        Preference addApp = new Preference(context);
        addApp.setTitle("Add another app override");
        addApp.setSummary("Choose any installed app to give it an independent size");
        addApp.setIconSpaceReserved(false);
        addApp.setOnPreferenceClickListener(pref -> {
            showAppPicker();
            return true;
        });
        perApp.addPreference(addApp);

        Preference reset = new Preference(context);
        reset.setTitle("Reset all scaling to 240 DPI");
        reset.setSummary("Remove every override and restore the original default");
        reset.setIconSpaceReserved(false);
        reset.setOnPreferenceClickListener(pref -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset app display scaling?")
                    .setMessage("Spotify, Waze, and all other apps will return to 240 DPI the next time you open them.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset", (d, which) -> {
                        settings.resetProjectedAppDpiSettings();
                        buildScreen();
                    }).show();
            return true;
        });
        screen.addPreference(reset);
    }

    private PreferenceCategory category(Context context, PreferenceScreen screen, String name) {
        PreferenceCategory category = new PreferenceCategory(context);
        category.setTitle(name);
        category.setIconSpaceReserved(false);
        screen.addPreference(category);
        return category;
    }

    private interface DpiSaver { boolean save(int dpi); }

    private SeekBarPreference addDpiSlider(PreferenceCategory group, String title, int selectedDpi, DpiSaver saver) {
        SeekBarPreference slider = new SeekBarPreference(requireContext());
        slider.setTitle(title);
        slider.setIconSpaceReserved(false);
        slider.setPersistent(false); // SettingsManager owns the ONLY preference store.
        slider.setMin(SettingsManager.PROJECTED_APP_DPI_MIN);
        slider.setMax(Math.max(selectedDpi, settings.isAdvancedProjectedAppDpiEnabled()
                ? SettingsManager.PROJECTED_APP_DPI_MAX_ADVANCED
                : SettingsManager.PROJECTED_APP_DPI_MAX));
        slider.setSeekBarIncrement(SettingsManager.PROJECTED_APP_DPI_STEP);
        slider.setShowSeekBarValue(true);
        slider.setUpdatesContinuously(false);
        slider.setValue(selectedDpi);
        slider.setSummary(percentageSummary(selectedDpi));
        slider.setOnPreferenceChangeListener((preference, value) -> {
            int dpi = (Integer) value;
            if (!saver.save(dpi)) return false;
            slider.setSummary(percentageSummary(dpi));
            return true;
        });
        group.addPreference(slider);
        return slider;
    }

    private String percentageSummary(int dpi) {
        return Math.round(dpi * 100f / SettingsManager.PROJECTED_APP_DPI_DEFAULT)
                + "% of the original UI scale";
    }

    private void addAppControls(PreferenceCategory group, String label, String packageName) {
        int override = settings.getProjectedAppDpiOverride(packageName);
        boolean custom = override != 0;

        SwitchPreferenceCompat enabled = new SwitchPreferenceCompat(requireContext());
        enabled.setTitle(label + " custom scaling");
        enabled.setSummary(custom ? (override + " DPI; turn off to inherit the default")
                : ("Uses default (" + settings.getDefaultProjectedAppDpi() + " DPI)"));
        enabled.setIconSpaceReserved(false);
        enabled.setPersistent(false);
        enabled.setChecked(custom);
        enabled.setOnPreferenceChangeListener((preference, value) -> {
            boolean checked = (Boolean) value;
            int proposedDpi = checked ? Math.max(300, settings.getDefaultProjectedAppDpi()) : 0;
            settings.setProjectedAppDpiOverride(packageName, proposedDpi);
            handler.post(this::buildScreen);
            return true;
        });
        group.addPreference(enabled);

        if (custom) {
            addDpiSlider(group, label + " DPI", override, dpi -> {
                settings.setProjectedAppDpiOverride(packageName, dpi);
                enabled.setSummary(dpi + " DPI; turn off to inherit the default");
                return true;
            });
        }
    }

    private String loadLabel(String packageName) {
        PackageManager pm = requireContext().getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private void showAppPicker() {
        Context context = requireContext();
        PackageManager pm = context.getPackageManager();
        List<AppChoice> choices = new ArrayList<>();
        for (ApplicationInfo info : pm.getInstalledApplications(0)) {
            if (info.packageName.equals(context.getPackageName())) continue;
            if (pm.getLaunchIntentForPackage(info.packageName) == null) continue;
            choices.add(new AppChoice(info.packageName, pm.getApplicationLabel(info).toString()));
        }
        Collator collator = Collator.getInstance();
        choices.sort((a, b) -> collator.compare(a.label(), b.label()));
        if (choices.isEmpty()) {
            new AlertDialog.Builder(context).setMessage("No launchable apps found.")
                    .setPositiveButton("OK", null).show();
            return;
        }

        int padding = (int) (16 * context.getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, 0, padding, 0);
        EditText search = new EditText(context);
        search.setHint("Search installed apps");
        search.setSingleLine(true);
        container.addView(search, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ListView list = new ListView(context);
        int listHeight = (int) (390 * context.getResources().getDisplayMetrics().density + 0.5f);
        container.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listHeight));
        ArrayAdapter<AppChoice> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, choices);
        list.setAdapter(adapter);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Choose a projected app")
                .setView(container)
                .setNegativeButton("Cancel", null)
                .create();
        list.setOnItemClickListener((parent, view, position, id) -> {
            AppChoice choice = adapter.getItem(position);
            if (choice == null) return;
            if (settings.getProjectedAppDpiOverride(choice.packageName()) == 0) {
                settings.setProjectedAppDpiOverride(choice.packageName(),
                        Math.max(300, settings.getDefaultProjectedAppDpi()));
            }
            dialog.dismiss();
            buildScreen();
        });
        dialog.show();
    }
}
