# Qixely Flywheel - tested integrated build

**Branch:** `palma2pro-working`  
**Tested on:** stock BOOX Palma 2 Pro with ShizukuPlus, wired to a 2024 Ford F-150 SYNC 4  
**Known-good source date:** November 4, 2026

This fork branch retains a combined, tested version of Flywheel for the above setup. It is **not** intended to replace the upstream project's main development branch or imply that each isolated contribution has separately passed release testing.

## Features in this integrated build

| Feature | What was confirmed |
| --- | --- |
| Ford SYNC 4 UI-config reply | Correct Android Auto `UPDATE_UI_CONFIG_REQUEST` response enables full projection and Ford touch controls. Existing [PR #8](https://github.com/Benjamin-Wiegand/Flywheel/pull/8) covers this protocol change. |
| First-connection Ford width handling | Addresses the observed initial width/margin issue in this vehicle. |
| Waze auto-start | Waze opens on projection after readiness/unlock when enabled. |
| Dashboard | Redesigned home screen, navigation shortcuts and media entry points. |
| Per-app DPI | Application-specific projected display DPI with persistent settings; Spotify browsing was used at 400 DPI. |
| Native Spotify Now Playing | Automatic application and virtual-display orientation overrides resolve the narrow portrait window. |
| Optional Spotify player | Native Spotify by default; custom Flywheel panel remains selectable. |

The user reported that the completed build **worked without a hitch**, including Spotify behavior after reconnection. These are results for the stated BOOX/Ford configuration, not a device-wide compatibility guarantee.

## Individual change branches

Each contribution is also available on a focused branch based on upstream Flywheel v1.1.1, for easier comparison or cherry-picking:

- [`feature/per-app-dpi`](https://github.com/Qixely/Flywheel/tree/feature/per-app-dpi)
- [`fix/spotify-landscape-orientation`](https://github.com/Qixely/Flywheel/tree/fix/spotify-landscape-orientation)
- [`feature/waze-autostart`](https://github.com/Qixely/Flywheel/tree/feature/waze-autostart)
- [`feature/spotify-player-choice`](https://github.com/Qixely/Flywheel/tree/feature/spotify-player-choice)
- [`feature/home-dashboard`](https://github.com/Qixely/Flywheel/tree/feature/home-dashboard)
- [`fix/ford-first-connect-width`](https://github.com/Qixely/Flywheel/tree/fix/ford-first-connect-width)

**Important:** The standalone branches were extracted from the verified source and have not each been compiled or run independently. The integrated build is the one verified in the vehicle. The historical `palma2pro-2026-11-04-known-good` tag remains anchored to the source-validation commit; this README update does not move it.

## Compatibility and contribution notes

- The Spotify `am compat` override changes a setting for the Spotify package beyond Flywheel and is not currently auto-reverted.
- The Ford first-connect adjustment is a heuristic based on one head-unit configuration.
- Keep feature reviews separate from the open Ford protocol [PR #8](https://github.com/Benjamin-Wiegand/Flywheel/pull/8).
- This fork retains the upstream project's GPL-3.0 license, attribution, and original README underneath the fork banner.
