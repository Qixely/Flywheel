# Qixely Flywheel branch: Spotify native Now Playing orientation compatibility

**Branch:** `fix/spotify-landscape-orientation`  
**Base:** upstream Flywheel v1.1.1  
**Integrated build:** [`palma2pro-working`](https://github.com/Qixely/Flywheel/tree/palma2pro-working)

Prevent Android from letterboxing Spotify NowPlayingActivity into a narrow portrait area on a landscape virtual display.

## Reproduction and diagnosis

Spotify browsing renders across the 1920 x 805 virtual display, but its native `NowPlayingActivity` requests portrait orientation. On the tested vehicle Android letterboxed it to approximately 338 x 805 pixels. Changing display density alone did not fix the activity window.

## What this branch changes

The existing privileged daemon is used to apply both confirmed workarounds **before** launching Spotify on the newly created virtual display:

1. `wm set-ignore-orientation-request -d <current-display-ID> true`
2. `am compat enable OVERRIDE_ANY_ORIENTATION_TO_USER com.spotify.music`

The display ID is obtained at runtime rather than hard-coded, allowing the override to be reapplied after reconnection. Failures are logged without blocking Spotify launch.

## Verification

The combined two-override configuration was confirmed to make native Spotify Now Playing display correctly in the vehicle. The fix then worked without manual ADB commands in the integrated Flywheel build, including after disconnect/reconnect. The overrides were observed working **together**; their independent necessity was not established by a controlled test. This isolated branch has not been compiled or tested separately.

## Side effects and upstream review

The compatibility override is applied to the installed Spotify package **device-wide**, not only while projected, and the current implementation does not automatically revert it. Its persistence, device support, and failure behavior should be addressed before treating this as a universal or opt-in upstream feature. The fix is Spotify-specific in this branch, not a general orientation-setting interface.

## License and attribution

The original Flywheel README, upstream authorship, and GPL-3.0 license remain intact. This branch documents a contribution to the upstream project; it is not a separate rebranding.
