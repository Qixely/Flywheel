# Qixely Flywheel branch: Redesigned Flywheel home dashboard

**Branch:** `feature/home-dashboard`  
**Base:** upstream Flywheel v1.1.1  
**Integrated build:** [`palma2pro-working`](https://github.com/Qixely/Flywheel/tree/palma2pro-working)

Add a card-based home interface with navigation, media, and installed-app shortcuts.

## What this branch changes

- Introduces a card-based home/dashboard interface with navigation and media sections.
- Adds quick installed-app shortcuts and a Home action in the dock.
- Provides the associated layouts, icons, drawables, and colors.

## Scope

This is a visible UI redesign, not a small backend fix. The branch is isolated from the DPI controls, Spotify orientation handling, Waze startup, and Ford first-connection width changes. It is presented for maintainer review rather than as a demand to replace upstream's existing design.

## Verification

The dashboard was developed and tested in the working BOOX Palma 2 Pro / Ford SYNC 4 integrated build. The extracted branch has not been compiled or tested separately. Layout on other head-unit sizes and input configurations is unverified.

## License and attribution

The original Flywheel README, upstream authorship, and GPL-3.0 license remain intact. This branch documents a contribution to the upstream project; it is not a separate rebranding.
