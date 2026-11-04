# Qixely Flywheel branch: Ford SYNC 4 first-connection width workaround

**Branch:** `fix/ford-first-connect-width`  
**Base:** upstream Flywheel v1.1.1  
**Integrated build:** [`palma2pro-working`](https://github.com/Qixely/Flywheel/tree/palma2pro-working)

Compensate for the anomalous initial horizontal display margin observed on one Ford SYNC 4 head unit.

## Problem

During initial projection on a 2024 Ford F-150 SYNC 4 head unit, the available content sometimes appeared narrower than on reconnect. Logs indicated a startup width/configuration mismatch. The original Ford Android Auto UI-config protocol reply is tracked independently in [upstream PR #8](https://github.com/Benjamin-Wiegand/Flywheel/pull/8).

## What this branch changes

Adds a narrowly targeted heuristic in projection setup for the observed initial Ford width signature, compensating for an approximately 586-pixel horizontal margin in the 1920 x 1080 / 240-DPI scenario. This is separate from the generic UI-config reply fix.

## Verification and limits

The workaround is present in the integrated build tested with BOOX Palma 2 Pro and Ford SYNC 4. The isolated branch has not been independently built or tested. Because the rule depends on the observed head-unit configuration, it should **not** be generalized to all head units without more protocol logs and regression tests. It is presented as a reviewable implementation rather than a proven universal fix.

## License and attribution

The original Flywheel README, upstream authorship, and GPL-3.0 license remain intact. This branch documents a contribution to the upstream project; it is not a separate rebranding.
