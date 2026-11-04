# Spotify landscape compatibility - experimental standalone branch

Android letterboxes Spotify NowPlayingActivity to portrait (338x805) on a
1920x805 landscape virtual display. The tested integrated build invoked
privileged shell `wm set-ignore-orientation-request -d ID true` AND
`am compat enable OVERRIDE_ANY_ORIENTATION_TO_USER com.spotify.music`.

This branch isolates the same mechanism from the dashboard and per-app DPI.
**Important:** the `am compat` override is device-wide and currently persists
after Flywheel stops. It is Spotify-specific and does not have a rollback path.
This is an example for review, **not** a ready-to-merge upstream PR.
No standalone build or device verification has been performed.
