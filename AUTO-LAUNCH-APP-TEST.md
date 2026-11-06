# Optional startup app — test source build

This is the complete Flywheel source project based on the preceding **cellular-signal crash fix**, retaining the Ford UI-config handling, Spotify Now Playing and per-app DPI controls.

## Behavior

- **Settings > Projection UI > Startup > Auto-launch app after connecting** provides **None (Flywheel dashboard)**, Waze, Spotify and all other installed launchable applications that Flywheel's All Apps drawer can launch (except Flywheel itself). The app choices are alphabetized.
- **None is the default on new installations AND upgrades.** The old Waze-only toggle is retired and NOT migrated; users can explicitly select Waze if desired. This mainline-friendly behavior leaves the dashboard visible unless the user opts in.
- One selected app launches **once per projection activity** only when the projected phone screen is unlocked, the app list is available and the privileged launcher is connected. It does not replace a task the user already opened.
- Removing a selected application leaves the dashboard visible; the Settings list identifies that preference as unavailable until changed.
- The selected app uses its existing individual DPI override, including **Spotify at 400 DPI**. The primary projection resolution, dashboard and Now Playing player are not changed by the setting.
- **Cellular-signal crash fix preserved:** unexpected values including 5 signal bars are clamped to 4 rather than triggering an assertion.
- The existing Ford UI-config reply and other custom features are unchanged.

## Recommended checks (parked for setup)

1. Upgrade with `adb install -r` and visit the app picker. It should read **None (Flywheel dashboard)** even if the old Waze auto-start switch was enabled. Existing Spotify DPI override should still be 400.
2. Connect projection with None selected and verify the dashboard stays open.
3. Select **Waze**; disconnect/reconnect while parked and confirm Waze opens once. Then choose Spotify and verify it opens once at your saved DPI.
4. Optionally test another app from the All Apps list. Having an Android launch activity does not guarantee an app will render well on landscape virtual displays.
5. Confirm 5-bar cellular signal changes no longer crash Flywheel. For the previous crash, see `CellIndicatorIcon.update`: the assertion was removed and the existing clamp retained.

## Build (macOS)

From the project root, use Java 21 and your existing Android SDK:

```
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
chmod +x gradlew
./gradlew :app:assembleDebug
```

To preserve settings when updating, use `adb install -r` rather than uninstalling the previous version.
