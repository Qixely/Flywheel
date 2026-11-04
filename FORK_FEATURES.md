# Qixely Flywheel local integration

This branch contains the operator-tested integrated source from 2026-11-04,
for a stock BOOX Palma 2 Pro with ShizukuPlus and a 2024 Ford SYNC 4 head unit.
It has Ford protocol UI-config handling, a Ford display-width heuristic,
Waze startup, a redesigned dashboard, per-app DPI settings, an optional
custom Spotify player, and automatic Spotify landscape compatibility.

**Testing:** The integrated build was reported working in the vehicle.
Intermediate commits are historical development snapshots, not separately
verified release points. Standalone feature branches are also supplied.

**Cautions:** Spotify orientation enables a package-wide Android compatibility
override which persists outside Flywheel. Ford display width uses a head-unit
heuristic. Do not merge the entire branch upstream without review.

**License:** Upstream Flywheel is GPL-3.0; retain existing license notices.
