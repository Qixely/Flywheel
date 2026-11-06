# Qixely Flywheel - personal integrated build

**Branch:** `palma2pro-working`  
**Tested on:** stock BOOX Palma 2 Pro with ShizukuPlus, wired to a 2024 Ford F-150 SYNC 4  
**Latest integrated source:** November 5, 2026 (long-duration reliability under test)

This fork branch retains the combined personal Flywheel source for the above setup. Functionality has been demonstrated in short tests; sustained connection reliability is not yet established. It is **not** intended to replace the upstream project's main development branch or imply that each isolated contribution has separately passed release testing.

## Features in this integrated build

| Feature | What was confirmed |
| --- | --- |
| Ford SYNC 4 UI-config reply | Correct Android Auto `UPDATE_UI_CONFIG_REQUEST` response enables full projection and Ford touch controls. Existing [PR #8](https://github.com/Benjamin-Wiegand/Flywheel/pull/8) covers this protocol change. |
| First-connection Ford width handling | Addresses the observed initial width/margin issue in this vehicle. |
| Selectable startup app | Optional auto-launch for installed launchable apps, including Waze and Spotify; **None (dashboard) is the default** on new installs and upgrades. Retires the old Waze-only switch. |
| Cellular signal crash fix | If Android reports 5 cellular bars, the existing icon clamps to 4 instead of crashing from an assertion. |
| Dashboard | Redesigned home screen, navigation shortcuts and media entry points. |
| Per-app DPI | Application-specific projected display DPI with persistent settings; Spotify browsing was used at 400 DPI. |
| Native Spotify Now Playing | Automatic application and virtual-display orientation overrides resolve the narrow portrait window. |
| Optional Spotify player | Native Spotify by default; custom Flywheel panel remains selectable. |

Short tests confirmed Ford projection, Spotify, Waze and dashboard functionality. A later log captured a five-bar cellular-indicator crash, addressed in source here. Sustained projection reliability still requires testing.

## Individual change branches

Each contribution is also available on a focused branch based on upstream Flywheel v1.1.1, for easier comparison or cherry-picking:

- [`feature/per-app-dpi`](https://github.com/Qixely/Flywheel/tree/feature/per-app-dpi)
- [`fix/spotify-landscape-orientation`](https://github.com/Qixely/Flywheel/tree/fix/spotify-landscape-orientation)
- [`feature/waze-autostart`](https://github.com/Qixely/Flywheel/tree/feature/waze-autostart)
- [`feature/spotify-player-choice`](https://github.com/Qixely/Flywheel/tree/feature/spotify-player-choice)
- [`feature/home-dashboard`](https://github.com/Qixely/Flywheel/tree/feature/home-dashboard)
- [`fix/ford-first-connect-width`](https://github.com/Qixely/Flywheel/tree/fix/ford-first-connect-width)

- [`fix/clamp-cellular-signal-level`](https://github.com/Qixely/Flywheel/tree/fix/clamp-cellular-signal-level) - Cell signal crash guard (separate upstream-based branch)
- [`feature/selectable-startup-app`](https://github.com/Qixely/Flywheel/tree/feature/selectable-startup-app) - Optional startup app selection (replaces Waze-only in personal mainline)

**Important:** The standalone branches are provided for independent review and have not each been compiled or tested separately. The combined personal build has passed short functional checks, but long-session stability is still under evaluation. The historical `palma2pro-2026-11-04-known-good` tag remains anchored to the source-validation commit; this README update does not move it.

## Compatibility and contribution notes

- The Spotify `am compat` override changes a setting for the Spotify package beyond Flywheel and is not currently auto-reverted.
- The Ford first-connect adjustment is a heuristic based on one head-unit configuration.
- Keep feature reviews separate from the open Ford protocol [PR #8](https://github.com/Benjamin-Wiegand/Flywheel/pull/8).
- This fork retains the upstream project's GPL-3.0 license, attribution, and original README underneath the fork banner.

<!-- 2026-11-05-integrated-update -->
## Personal mainline sync: November 5, 2026

- `palma2pro-working` integrates all existing personal features, plus the cell-signal crash guard and optional startup app selector.
- The default is **None**; Waze can be selected manually. The historical `feature/waze-autostart` remains available to compare implementations.
- Upstream-based feature branches are intended for independent review, not as separate running builds. No new PRs are required.
- Older tags, including `palma2pro-2026-11-04-known-good`, remain historical and are not moved.
