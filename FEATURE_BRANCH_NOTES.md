# Qixely Flywheel branch: Optional Waze launch when projection starts

**Branch:** `feature/waze-autostart`  
**Base:** upstream Flywheel v1.1.1  
**Integrated build:** [`palma2pro-working`](https://github.com/Qixely/Flywheel/tree/palma2pro-working)

Launch Waze automatically when projection is unlocked and ready, without stealing focus from an app already opened.

## What this branch changes

- Adds a Settings switch controlling automatic Waze launch.
- Waits until projection has unlocked, the installed package list is ready, and the privileged service is connected.
- Opens Waze once per projection session when no app is already active.
- Skips the launch gracefully if Waze is not installed or the user has already selected another app.

## Scope and behavior

The isolated contribution defaults the switch to **off** so it does not unexpectedly change upstream behavior. The integrated local build was used with Waze startup enabled. This branch targets `com.waze` specifically; selecting an arbitrary startup app would be a future enhancement, not an existing feature here.

## Verification

Waze startup was tested during development on the BOOX Palma 2 Pro and Ford SYNC 4, and the integrated Flywheel version was confirmed working. This extracted branch has not been compiled or tested separately on hardware. It does not include the Ford or Spotify changes.

## License and attribution

The original Flywheel README, upstream authorship, and GPL-3.0 license remain intact. This branch documents a contribution to the upstream project; it is not a separate rebranding.
