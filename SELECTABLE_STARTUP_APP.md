# Feature: optional app auto-launch after connection

**Branch:** `feature/selectable-startup-app` (isolated from personal Ford, Spotify, and DPI changes).

## Why

The original experimental personal implementation exposed only **Open Waze after connecting**. Flywheel should not prefer one installed navigation app over others: users should be able to choose a supported app or stay on the dashboard.

## Behavior

- **Settings > Projection UI > Startup > Auto-launch app after connecting** presents **None (Flywheel dashboard)** and installed apps Flywheel can launch from its All Apps list; Flywheel itself is excluded.
- **None is the default for new installs and upgrades.** The retired Waze-only toggle is intentionally not migrated. A user who wants Waze must select it.
- App startup happens only after the phone is unlocked, the app list is ready, and Flywheel's privileged launcher is connected.
- Startup launches at most once per projection activity and does not replace a task the user already opened.
- If the selected app is no longer installed, the dashboard remains visible.
- This preference doesn't alter per-app display density or playback UI. Not all Android apps render ideally on a landscape virtual display.

## How to verify

1. Install this branch on a development device, verify the default reads **None** and the dashboard stays open on a new connection.
2. Select Waze, disconnect/reconnect, and verify it opens once after readiness and unlock.
3. Select another installed launchable app (e.g. Spotify) and confirm it starts on a fresh session.
4. Remove the selected app and confirm no crash or repeated startup attempts.
5. Upgrade from a version with the old Waze-specific toggle and verify startup still defaults to **None**, unless the user explicitly chooses an app.

This branch is offered for independent review without submitting a pull request. `palma2pro-working` retains this feature along with the other personal changes.
