# Qixely Flywheel branch guide

**Personal mainline:** [`palma2pro-working`](https://github.com/Qixely/Flywheel/tree/palma2pro-working) combines every personal feature for the BOOX Palma 2 Pro / Ford SYNC 4 build.

**Independent feature and fix branches:**

| Branch | Description |
| --- | --- |
| [`fix/clamp-cellular-signal-level`](https://github.com/Qixely/Flywheel/tree/fix/clamp-cellular-signal-level) | Fixes crash on a reported cellular level of five; leaves four-bar icon unchanged. |
| [`feature/selectable-startup-app`](https://github.com/Qixely/Flywheel/tree/feature/selectable-startup-app) | Optional startup app selection, default None. Replaces the Waze-only setting on personal mainline. |
| [`feature/waze-autostart`](https://github.com/Qixely/Flywheel/tree/feature/waze-autostart) | Historical Waze-specific startup implementation, for comparison. |
| [`feature/per-app-dpi`](https://github.com/Qixely/Flywheel/tree/feature/per-app-dpi) | Per-application virtual display density. |
| [`fix/spotify-landscape-orientation`](https://github.com/Qixely/Flywheel/tree/fix/spotify-landscape-orientation) | Native Spotify landscape behavior. |
| [`feature/spotify-player-choice`](https://github.com/Qixely/Flywheel/tree/feature/spotify-player-choice) | Choice of native Spotify versus custom Flywheel player. |
| [`feature/home-dashboard`](https://github.com/Qixely/Flywheel/tree/feature/home-dashboard) | Custom projection dashboard. |
| [`fix/ford-first-connect-width`](https://github.com/Qixely/Flywheel/tree/fix/ford-first-connect-width) | Ford first-connection width handling. |
| [`fix/ford-sync-ui-config`](https://github.com/Qixely/Flywheel/tree/fix/ford-sync-ui-config) | Ford UI configuration protocol reply. |

All feature branches are kept separate so a maintainer can inspect individual changes. The integrated personal branch contains their cumulative behavior; the same Git commit IDs need not appear in that branch when a change is applied separately. No pull requests are required to browse these branches.

**Status:** Source is under real-world reliability testing. The cell-level assertion has a confirmed stack trace and a targeted fix; this does not establish full-session USB stability.
