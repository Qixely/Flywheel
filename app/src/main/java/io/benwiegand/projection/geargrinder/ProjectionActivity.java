package io.benwiegand.projection.geargrinder;

import static androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import io.benwiegand.projection.geargrinder.callback.AppLauncherListener;
import io.benwiegand.projection.geargrinder.callback.IPCConnectionListener;
import io.benwiegand.projection.geargrinder.exception.UserFriendlyException;
import io.benwiegand.projection.geargrinder.makeshiftbind.MakeshiftBind;
import io.benwiegand.projection.geargrinder.PackageService;
import io.benwiegand.projection.geargrinder.callback.MakeshiftBindCallback;
import io.benwiegand.projection.geargrinder.pm.AppRecord;
import io.benwiegand.projection.geargrinder.projection.ui.BatteryIndicator;
import io.benwiegand.projection.geargrinder.projection.ui.HomeDashboard;
import io.benwiegand.projection.geargrinder.projection.ui.MediaControlsWidget;
import io.benwiegand.projection.geargrinder.projection.ui.SpotifyNowPlayingPanel;
import io.benwiegand.projection.geargrinder.projection.ui.NotificationDisplay;
import io.benwiegand.projection.geargrinder.projection.ui.PhoneCallDisplay;
import io.benwiegand.projection.geargrinder.projection.ui.task.ProjectionTask;
import io.benwiegand.projection.geargrinder.projection.ui.NetworkIndicators;
import io.benwiegand.projection.geargrinder.projection.ui.task.ProjectionTaskManager;
import io.benwiegand.projection.geargrinder.service.GeargrinderServiceConnector;
import io.benwiegand.projection.geargrinder.projection.ui.AppDock;
import io.benwiegand.projection.geargrinder.projection.ui.AppDrawer;
import io.benwiegand.projection.geargrinder.projection.ui.ProjectionModal;
import io.benwiegand.projection.geargrinder.settings.SettingsManager;
import io.benwiegand.projection.libprivd.IPrivd;

public class ProjectionActivity extends AppCompatActivity implements MakeshiftBindCallback, IPCConnectionListener, GeargrinderServiceConnector.ConnectionListener, AppDock.Listener, AppLauncherListener, ProjectionTaskManager.Listener, PackageService.PackageServiceListener {
    private static final String TAG = ProjectionActivity.class.getSimpleName();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ActivityBinder binder = new ActivityBinder();
    private MakeshiftBind makeshiftBind;

    private SettingsManager settingsManager;
    private ProjectionTaskManager taskManager;
    private AppDock appDock;
    private HomeDashboard homeDashboard;
    private AppDrawer appDrawer;
    private BatteryIndicator batteryIndicator;
    private NetworkIndicators networkIndicators;
    private MediaControlsWidget mediaControlsWidget;
    private SpotifyNowPlayingPanel spotifyNowPlayingPanel;
    private NotificationDisplay notificationDisplay;
    private PhoneCallDisplay phoneCallDisplay;

    private ProjectionModal keyguardModal;

    private GeargrinderServiceConnector connector;
    private IPrivd privd = null;
    private PackageService.ServiceBinder packageBinder;
    private boolean packageListReady = false;
    private boolean projectionUnlocked = false;
    private boolean startupAppHandled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_projection);

        WindowInsetsControllerCompat windowInsetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.displayCutout());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        findViewById(R.id.soft_back_button).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        settingsManager = new SettingsManager(this);

        // screen lock
        findViewById(R.id.projection_root).setVisibility(View.GONE);
        keyguardModal = new ProjectionModal(findViewById(R.id.root), true)
                .setTitle(R.string.keyguard_modal_title)
                .setMessage(R.string.keyguard_modal_instructions);

        // binds
        connector = new GeargrinderServiceConnector(TAG, this, this);
        connector.bindPrivdService(BIND_AUTO_CREATE | BIND_IMPORTANT);
        connector.bindPackageService(BIND_AUTO_CREATE | BIND_IMPORTANT);
        connector.bindNotificationService();

        // components
        taskManager = new ProjectionTaskManager(findViewById(R.id.content_frame), settingsManager);
        appDock = new AppDock(findViewById(R.id.app_dock), taskManager, this);
        homeDashboard = new HomeDashboard(findViewById(R.id.home_dashboard), this, this::onAppDrawerSelected);
        appDrawer = new AppDrawer(findViewById(R.id.app_drawer), this);
        batteryIndicator = new BatteryIndicator(findViewById(R.id.battery_indicator), settingsManager);
        networkIndicators = new NetworkIndicators(findViewById(R.id.network_indicators));
        mediaControlsWidget = new MediaControlsWidget(findViewById(R.id.media_controls), handler, taskManager, connector::getPackageBinder, settingsManager);
        spotifyNowPlayingPanel = new SpotifyNowPlayingPanel(
                findViewById(R.id.spotify_now_playing), handler,
                mediaControlsWidget::getSpotifyController,
                () -> spotifyNowPlayingPanel.hide());
        mediaControlsWidget.setSpotifySessionListener(spotifyNowPlayingPanel::refresh);
        notificationDisplay = new NotificationDisplay(findViewById(R.id.popup_notification_overlay), findViewById(R.id.notification_drawer), findViewById(R.id.notification_indicator), settingsManager);
        phoneCallDisplay = new PhoneCallDisplay(this, findViewById(R.id.popup_incoming_call_overlay));

        taskManager.registerListener(this);

        // bind
        makeshiftBind = new MakeshiftBind(this, new ComponentName(this, ProjectionActivity.class), this);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (notificationDisplay.closeNotificationDrawer()) return;
                if (notificationDisplay.dismissPopupNotification()) return;
                if (appDrawer.close()) return;
                if (spotifyNowPlayingPanel.hide()) return;

                ProjectionTask task = taskManager.getActiveTask();
                if (task != null) {
                    if (task.hideSplash()) return;
                    if (task.injectBackButton()) return;
                }

                Log.v(TAG, "back button consumed");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (packageBinder != null) packageBinder.unregisterListener(this);
        taskManager.unregisterListener(this);
        taskManager.destroy();

        makeshiftBind.destroy();
        connector.destroy();

        appDock.destroy();
        homeDashboard.destroy();
        appDrawer.destroy();
        batteryIndicator.destroy();
        networkIndicators.destroy();
        spotifyNowPlayingPanel.destroy();
        mediaControlsWidget.destroy();
        notificationDisplay.destroy();
        phoneCallDisplay.destroy();
    }

    private boolean focusSearch(int direction) {
        View focus = getCurrentFocus();
        if (focus == null) focus = findViewById(R.id.app_drawer_button);    // TODO: fails in touch focus mode

        View nextFocus = focus.focusSearch(direction);
        if (nextFocus == null) {
            Log.w(TAG, "couldn't find next focus");
            return false;
        }

        return nextFocus.requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT -> focusSearch(View.FOCUS_FORWARD);
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS -> focusSearch(View.FOCUS_BACKWARD);
            default -> false;
        };
        return handled || super.onKeyDown(keyCode, event);
    }

    /**
     * Start a user-selected, launchable app at most once per projection session.
     * Wait for unlock, package discovery and the privileged launcher so this
     * follows the same safe startup path as the previous Waze-only option.
     */
    private void maybeAutoLaunchApp() {
        if (startupAppHandled || isFinishing() || isDestroyed()) return;

        String packageName = settingsManager.getAutoLaunchAppPackageName();
        if (packageName.isEmpty()) {
            startupAppHandled = true; // None: stay on the Flywheel dashboard.
            return;
        }
        if (!projectionUnlocked || !packageListReady || privd == null || packageBinder == null) return;

        // Make a single decision for this projection session. Never take over
        // a task that the driver already opened, or retry missing apps.
        startupAppHandled = true;
        if (taskManager.getActiveTask() != null) {
            Log.i(TAG, "skipping startup app: another app is already active");
            return;
        }
        AppRecord startupApp = packageBinder.getApp(packageName);
        if (startupApp == null) {
            Log.w(TAG, "startup app is not installed or launchable: " + packageName);
            return;
        }
        Log.i(TAG, "automatically opening " + packageName + " on the projected display");
        taskManager.dynamicOpenSingle(startupApp);
    }

    @Override
    public void onAppSelected(AppRecord app) {
        taskManager.dynamicOpenSingle(app);
    }

    @Override
    public boolean onContentFocus() {
        boolean updated = false;
        updated |= appDrawer.close();
        updated |= notificationDisplay.closeNotificationDrawer();
        return updated;
    }

    @Override
    public void onAppDrawerSelected() {
        appDrawer.toggle();
        notificationDisplay.closeNotificationDrawer();
    }

    @Override
    public void onHomeSelected() {
        taskManager.switchToTask(null); // Preserve open tasks and their dock icons.
    }

    @Override
    public boolean onActiveDockTaskSelected(ProjectionTask task) {
        for (AppRecord app : task.getAppRecords()) {
            if ("com.spotify.music".equals(app.launchComponent().getPackageName())) {
                if (!settingsManager.isSpotifyCustomPlayerEnabled()) return false;
                // In Flywheel Player mode, the active Spotify dock item toggles
                // the custom panel without interrupting Spotify underneath.
                if (!spotifyNowPlayingPanel.hide()) spotifyNowPlayingPanel.show();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSwitchTask(ProjectionTask oldTask, boolean oldPinned, ProjectionTask newTask, boolean newPinned) {
        findViewById(R.id.home_dashboard).setVisibility(newTask == null ? View.VISIBLE : View.GONE);

        // The custom panel is opt-in. Native Spotify remains visible by default;
        // the Flywheel Player mode automatically overlays its wide Now Playing view.
        // Other apps always hide the panel.
        boolean spotifyOpened = false;
        if (newTask != null) {
            for (AppRecord app : newTask.getAppRecords()) {
                if ("com.spotify.music".equals(app.launchComponent().getPackageName())) {
                    spotifyOpened = true;
                    break;
                }
            }
        }
        if (spotifyOpened && settingsManager.isSpotifyCustomPlayerEnabled())
            spotifyNowPlayingPanel.show();
        else spotifyNowPlayingPanel.hide();
    }

    @Override
    public void onPrivdServiceConnected(PrivdService.ServiceBinder binder) {
        binder.requestDaemon(ProjectionActivity.this);
    }

    @Override
    public void onPackageServiceConnected(PackageService.ServiceBinder binder) {
        appDrawer.setPackageBinder(binder);
        homeDashboard.setPackageBinder(binder);
        notificationDisplay.setPackageServiceBinder(binder);
        taskManager.setPackageServiceBinder(binder);
        packageBinder = binder;
        binder.registerListener(this);
    }

    @Override
    public void onPackageListUpdated(PackageService.ServiceBinder binder) {
        packageListReady = true;
        handler.post(this::maybeAutoLaunchApp);
    }

    @Override
    public void onNotificationServiceConnected(NotificationService.ServiceBinder binder) {
        notificationDisplay.setNotificationServiceBinder(binder);
    }

    @Override
    public void onPrivdConnected(IPrivd privd) {
        this.privd = privd;
        taskManager.onPrivdConnected(privd);
        handler.post(this::maybeAutoLaunchApp);
    }

    @Override
    public void onPrivdDisconnected() {
        if (isFinishing() || isDestroyed()) return;
        Log.wtf(TAG, "privd connection lost, finishing");
        finish();
    }

    @Override
    public void onPrivdLaunchFailure(UserFriendlyException e) {
        onPrivdDisconnected();
    }

    @Override
    public IBinder onMakeshiftBind(Intent intent) {
        return binder;
    }

    public class ActivityBinder extends Binder {

        public void setMargins(int horizontal, int vertical) {
            runOnUiThread(() -> {
                View root = findViewById(R.id.root);
                root.setPadding(
                        horizontal / 2,
                        vertical / 2,
                        horizontal - horizontal / 2,
                        vertical - vertical / 2
                );
            });
        }

        public void onScreenUnlocked() {
            runOnUiThread(() -> {
                findViewById(R.id.projection_root).setVisibility(View.VISIBLE);
                keyguardModal.close();
                projectionUnlocked = true;
                maybeAutoLaunchApp();
            });
        }

    }
}
