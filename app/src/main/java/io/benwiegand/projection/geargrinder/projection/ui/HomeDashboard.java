package io.benwiegand.projection.geargrinder.projection.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.benwiegand.projection.geargrinder.PackageService;
import io.benwiegand.projection.geargrinder.R;
import io.benwiegand.projection.geargrinder.callback.AppLauncherListener;
import io.benwiegand.projection.geargrinder.pm.AppCategory;
import io.benwiegand.projection.geargrinder.pm.AppRecord;

/**
 * A lightweight native home screen for the projected display.
 *
 * This class only operates on the existing PackageService app records and the
 * existing AppLauncherListener, so it launches apps through the same privileged
 * projection pipeline as the app drawer. It does not touch the video, input,
 * AA protocol, or Shizuku paths.
 */
public final class HomeDashboard implements PackageService.PackageServiceListener {
    private static final int MAX_SHORTCUTS = 5;

    private static final String[] NAVIGATION_PACKAGES = {
            "com.waze",
            "com.google.android.apps.maps",
            "app.organicmaps",
            "net.osmand.plus",
            "net.osmand",
            "com.sygic.aura"
    };

    private static final String[] MEDIA_PACKAGES = {
            "com.spotify.music",
            "org.videolan.vlc",
            "com.google.android.apps.youtube.music",
            "com.musicolet.music",
            "com.maxmpz.audioplayer",
            "com.tidal"
    };

    private final View rootView;
    private final Context context;
    private final PackageManager pm;
    private final AppLauncherListener appLauncher;
    private final Runnable openAppDrawer;
    private final LinearLayout quickApps;
    private final TextView navigationLabel;
    private final TextView mediaLabel;

    private PackageService.ServiceBinder packageBinder;
    private AppRecord navigationApp;
    private AppRecord mediaApp;

    public HomeDashboard(View rootView, AppLauncherListener appLauncher, Runnable openAppDrawer) {
        this.rootView = rootView;
        this.context = rootView.getContext();
        this.pm = context.getPackageManager();
        this.appLauncher = appLauncher;
        this.openAppDrawer = openAppDrawer;

        quickApps = rootView.findViewById(R.id.home_quick_apps);
        navigationLabel = rootView.findViewById(R.id.home_navigation_app_name);
        mediaLabel = rootView.findViewById(R.id.home_media_app_name);

        rootView.findViewById(R.id.home_navigation_card)
                .setOnClickListener(v -> launchOrOpenDrawer(navigationApp));
        rootView.findViewById(R.id.home_media_card)
                .setOnClickListener(v -> launchOrOpenDrawer(mediaApp));
        rootView.findViewById(R.id.home_apps_card)
                .setOnClickListener(v -> openAppDrawer.run());

        // App discovery may still be running during connection setup.
        addAllAppsShortcut();
    }

    public void setPackageBinder(PackageService.ServiceBinder binder) {
        if (packageBinder != null) packageBinder.unregisterListener(this);
        packageBinder = binder;
        navigationApp = null;
        mediaApp = null;
        quickApps.removeAllViews();
        addAllAppsShortcut();
        if (binder != null) binder.registerListener(this);
    }

    public void destroy() {
        if (packageBinder != null) packageBinder.unregisterListener(this);
        packageBinder = null;
    }

    private void launchOrOpenDrawer(AppRecord app) {
        if (app == null) openAppDrawer.run();
        else appLauncher.onAppSelected(app);
    }

    private AppRecord findInstalled(String[] packages, PackageService.ServiceBinder binder) {
        for (String name : packages) {
            AppRecord app = binder.getApp(name);
            if (app != null) return app;
        }
        return null;
    }

    @Override
    public void onPackageListUpdated(PackageService.ServiceBinder binder) {
        // Updates are delivered on the main looper by PackageService.
        navigationApp = findInstalled(NAVIGATION_PACKAGES, binder);
        mediaApp = findInstalled(MEDIA_PACKAGES, binder);

        if (navigationApp == null) {
            navigationLabel.setText(R.string.home_navigation_fallback);
        } else {
            navigationLabel.setText(context.getString(R.string.home_open_app, navigationApp.label(pm)));
        }
        if (mediaApp == null) {
            mediaLabel.setText(R.string.home_media_fallback);
        } else {
            mediaLabel.setText(context.getString(R.string.home_open_app, mediaApp.label(pm)));
        }
        rootView.findViewById(R.id.home_navigation_card).setContentDescription(
                context.getString(R.string.home_navigation_heading) + ". " + navigationLabel.getText());
        rootView.findViewById(R.id.home_media_card).setContentDescription(
                context.getString(R.string.home_media_heading) + ". " + mediaLabel.getText());

        // Priority: navigation, music, then other installed audio/navigation apps.
        // The launcher does not assume Waze, Maps or a media app is installed.
        Map<String, AppRecord> shortcuts = new LinkedHashMap<>();
        addShortcut(shortcuts, navigationApp);
        addShortcut(shortcuts, mediaApp);
        List<AppRecord> focused = binder.getAppsFor(AppCategory.NAVIGATION_AND_AUDIO);
        if (focused != null) {
            for (AppRecord app : focused) {
                if (shortcuts.size() >= MAX_SHORTCUTS) break;
                addShortcut(shortcuts, app);
            }
        }
        // A de-Googled phone may not advertise Android Auto app categories.
        if (shortcuts.isEmpty()) {
            for (AppRecord app : binder.getAllApps()) {
                if (shortcuts.size() >= MAX_SHORTCUTS) break;
                if (!app.packageName().equals(context.getPackageName())) {
                    addShortcut(shortcuts, app);
                }
            }
        }

        quickApps.removeAllViews();
        for (AppRecord app : shortcuts.values()) {
            addAppShortcut(app);
        }
        addAllAppsShortcut();
    }

    private void addShortcut(Map<String, AppRecord> target, AppRecord app) {
        if (app != null) target.putIfAbsent(app.packageName(), app);
    }

    private View inflateShortcut() {
        return LayoutInflater.from(context).inflate(R.layout.layout_home_quick_app, quickApps, false);
    }

    private void addAppShortcut(AppRecord app) {
        View shortcut = inflateShortcut();
        ImageView image = shortcut.findViewById(R.id.home_quick_app_icon);
        TextView text = shortcut.findViewById(R.id.home_quick_app_label);
        image.setImageDrawable(app.icon(pm));
        CharSequence label = app.label(pm);
        text.setText(label);
        shortcut.setContentDescription(label);
        shortcut.setOnClickListener(v -> appLauncher.onAppSelected(app));
        quickApps.addView(shortcut);
    }

    private void addAllAppsShortcut() {
        View shortcut = inflateShortcut();
        ImageView image = shortcut.findViewById(R.id.home_quick_app_icon);
        TextView text = shortcut.findViewById(R.id.home_quick_app_label);
        image.setImageResource(R.drawable.home_ic_apps);
        text.setText(R.string.home_all_apps);
        shortcut.setContentDescription(context.getString(R.string.home_all_apps));
        shortcut.setOnClickListener(v -> openAppDrawer.run());
        quickApps.addView(shortcut);
    }
}
