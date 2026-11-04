# Qixely Flywheel branch: Choose native Spotify or Flywheel media player

**Branch:** `feature/spotify-player-choice`  
**Base:** upstream Flywheel v1.1.1  
**Integrated build:** [`palma2pro-working`](https://github.com/Qixely/Flywheel/tree/palma2pro-working)

Keep the native Spotify interface as the default while offering a larger-text Flywheel Now Playing overlay.

## What this branch changes

- Adds a Spotify-specific player-mode preference under Projection UI settings.
- **Native Spotify** is the default and does not automatically replace Spotify's own Now Playing screen.
- **Flywheel Player** presents large artwork, track information, and playback controls using the media session.
- Preserves a path back to Spotify browsing from the custom view.

## Scope and dependencies

The stand-alone branch contains the player choice and custom view, but **not** the separate Spotify landscape-orientation workaround. Therefore selecting Native Spotify by itself does not guarantee that Spotify's portrait-restricted `NowPlayingActivity` will render full width. That behavior was corrected in the complete integrated build through `fix/spotify-landscape-orientation`.

This is a Spotify-specific UX option, not a generic media-player framework.

## Verification

Both the custom panel and the player selection were exercised in the BOOX/Ford integrated build. The completed build worked with the native interface as default, and the custom panel remained available. This extracted standalone branch has not been compiled or tested separately; upstream adoption would benefit from generalized media-app support and accessibility review.

## License and attribution

The original Flywheel README, upstream authorship, and GPL-3.0 license remain intact. This branch documents a contribution to the upstream project; it is not a separate rebranding.
