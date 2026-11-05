package io.benwiegand.projection.geargrinder.settings.ui.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import android.icu.text.Collator;
import java.util.ArrayList;
import java.util.List;

import io.benwiegand.projection.geargrinder.R;
import io.benwiegand.projection.geargrinder.pm.AppRecord;
import io.benwiegand.projection.geargrinder.settings.SettingsManager;

public class ProjectionUiPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_projection_ui, rootKey);
        populateStartupAppPicker();
    }

    /** Use the same launchable-app detection as the All Apps drawer. */
    private void populateStartupAppPicker() {
        ListPreference picker = findPreference(getString(R.string.key_auto_launch_app));
        if (picker == null) return;

        PackageManager pm = requireContext().getPackageManager();
        List<AppRecord> launchable = new ArrayList<>();
        for (PackageInfo pkg : pm.getInstalledPackages(PackageManager.MATCH_ALL)) {
            // Avoid launching Flywheel inside its own virtual display.
            if (pkg.packageName.equals(requireContext().getPackageName())) continue;
            AppRecord app = AppRecord.createForPackage(pm, pkg.packageName);
            if (app != null) launchable.add(app);
        }
        Collator collator = Collator.getInstance();
        launchable.sort((a, b) -> collator.compare(a.label(pm).toString(), b.label(pm).toString()));

        List<CharSequence> labels = new ArrayList<>(launchable.size() + 2);
        List<CharSequence> packages = new ArrayList<>(launchable.size() + 2);
        labels.add(getString(R.string.auto_launch_app_none));
        packages.add("");
        for (AppRecord app : launchable) {
            labels.add(app.label(pm));
            packages.add(app.packageName());
        }

        String selected = new SettingsManager(requireContext()).getAutoLaunchAppPackageName();
        if (!selected.isEmpty() && !packages.contains(selected)) {
            // Preserve the selection so the user can see when an app was removed.
            labels.add(getString(R.string.auto_launch_app_unavailable, selected));
            packages.add(selected);
        }
        picker.setEntries(labels.toArray(new CharSequence[0]));
        picker.setEntryValues(packages.toArray(new CharSequence[0]));
        picker.setValue(selected);
    }
}
