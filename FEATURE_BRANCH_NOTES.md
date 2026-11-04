# Qixely Flywheel branch: Per-application display density

**Branch:** `feature/per-app-dpi`  
**Base:** upstream Flywheel v1.1.1  
**Integrated build:** [`palma2pro-working`](https://github.com/Qixely/Flywheel/tree/palma2pro-working)

Choose a default projected-app DPI and optional per-app overrides without changing the head-unit projection resolution.

## What this branch changes

- Adds persistent per-package display-density overrides, with an inherited default of 240 DPI.
- Adds an installed-app selection interface, reset controls, and a discrete 20-DPI slider.
- Supports 240-480 DPI normally, with an advanced option extending to 640 DPI.
- Applies the effective DPI to the virtual activity display when apps are launched or switched.

## Scope and dependencies

This focused branch is based on upstream Flywheel v1.1.1. It does **not** include the Ford UI-config handshake fix, the custom dashboard, Waze startup, Spotify player choice, or Spotify's orientation compatibility workaround.

## Verification

The feature was exercised on a BOOX Palma 2 Pro with ShizukuPlus, using wired Flywheel projection to a 2024 Ford F-150 SYNC 4. Spotify at 400 DPI provided improved browsing readability. The discrete slider fix and saved settings were confirmed as working in the integrated `palma2pro-working` build. This extracted standalone branch has **not** been compiled or tested separately; upstream integration still needs its own build and regression test.

## License and attribution

The original Flywheel README, upstream authorship, and GPL-3.0 license remain intact. This branch documents a contribution to the upstream project; it is not a separate rebranding.
