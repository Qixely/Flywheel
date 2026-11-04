package io.benwiegand.projection.geargrinder.projection.ui;

import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

import io.benwiegand.projection.geargrinder.R;

/**
 * The Spotify browsing task runs behind this view. No Spotify UI is intercepted
 * or modified: this is a native, wide remote-control view of its MediaSession.
 * Back reveals the existing Spotify task without disrupting playback.
 */
public final class SpotifyNowPlayingPanel {
    private static final String TAG = SpotifyNowPlayingPanel.class.getSimpleName();

    private final View root;
    private final Handler handler;
    private final Supplier<MediaController> spotifyControllerSupplier;
    private final Runnable browseSpotify;
    private final ImageView art;
    private final TextView title;
    private final TextView artist;
    private final TextView album;
    private final TextView elapsed;
    private final TextView duration;
    private final SeekBar seekBar;
    private final ImageButton previous;
    private final ImageButton playPause;
    private final ImageButton next;

    private long durationMs;
    private boolean draggingSeek;
    private final Runnable positionTicker = new Runnable() {
        @Override public void run() {
            if (!isVisible()) return;
            updatePosition();
            handler.postDelayed(this, 1000);
        }
    };

    public SpotifyNowPlayingPanel(View root, Handler handler,
                                 Supplier<MediaController> spotifyControllerSupplier,
                                 Runnable browseSpotify) {
        this.root = root;
        this.handler = handler;
        this.spotifyControllerSupplier = spotifyControllerSupplier;
        this.browseSpotify = browseSpotify;
        this.art = root.findViewById(R.id.spotify_player_art);
        this.title = root.findViewById(R.id.spotify_player_title);
        this.artist = root.findViewById(R.id.spotify_player_artist);
        this.album = root.findViewById(R.id.spotify_player_album);
        this.elapsed = root.findViewById(R.id.spotify_player_elapsed);
        this.duration = root.findViewById(R.id.spotify_player_duration);
        this.seekBar = root.findViewById(R.id.spotify_player_seek);
        this.previous = root.findViewById(R.id.spotify_player_previous);
        this.playPause = root.findViewById(R.id.spotify_player_play_pause);
        this.next = root.findViewById(R.id.spotify_player_next);

        root.findViewById(R.id.spotify_player_browse).setOnClickListener(v -> browseSpotify.run());
        previous.setOnClickListener(v -> withController(c -> c.getTransportControls().skipToPrevious()));
        next.setOnClickListener(v -> withController(c -> c.getTransportControls().skipToNext()));
        playPause.setOnClickListener(v -> withController(c -> {
            PlaybackState state = c.getPlaybackState();
            int status = state != null ? state.getState() : PlaybackState.STATE_NONE;
            if (status == PlaybackState.STATE_PLAYING || status == PlaybackState.STATE_BUFFERING)
                c.getTransportControls().pause();
            else
                c.getTransportControls().play();
        }));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser && durationMs > 0)
                    elapsed.setText(formatDuration((durationMs * progress) / bar.getMax()));
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { draggingSeek = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                draggingSeek = false;
                if (durationMs <= 0) return;
                long target = (durationMs * bar.getProgress()) / bar.getMax();
                withController(c -> {
                    PlaybackState state = c.getPlaybackState();
                    if (state != null && (state.getActions() & PlaybackState.ACTION_SEEK_TO) != 0)
                        c.getTransportControls().seekTo(target);
                });
                updatePosition();
            }
        });
    }

    private interface MediaAction { void apply(MediaController controller); }

    private void withController(MediaAction action) {
        MediaController controller = spotifyControllerSupplier.get();
        if (controller == null) return;
        try {
            action.apply(controller);
        } catch (RuntimeException e) {
            Log.w(TAG, "Spotify media transport command failed", e);
        }
    }

    public boolean isVisible() {
        return root.getVisibility() == View.VISIBLE;
    }

    /** Called when the user opens Spotify via All Apps, Home or the dock. */
    public void show() {
        root.setVisibility(View.VISIBLE);
        refresh();
        handler.removeCallbacks(positionTicker);
        handler.post(positionTicker);
    }

    /** Hide the player while leaving the Spotify browsing task running. */
    public boolean hide() {
        if (!isVisible()) return false;
        root.setVisibility(View.GONE);
        handler.removeCallbacks(positionTicker);
        return true;
    }

    public void destroy() {
        hide();
        handler.removeCallbacks(positionTicker);
    }

    /** The existing MediaControlsWidget invokes this as Spotify metadata changes. */
    public void refresh() {
        if (!isVisible()) return;
        MediaController controller = spotifyControllerSupplier.get();
        MediaMetadata metadata = controller != null ? controller.getMetadata() : null;
        PlaybackState state = controller != null ? controller.getPlaybackState() : null;

        if (metadata == null) {
            title.setText("Ready when you are");
            artist.setText("Browse Spotify to choose music");
            album.setText("");
            art.setImageResource(R.drawable.music);
            durationMs = 0;
            duration.setText("--:--");
        } else {
            title.setText(firstAvailable(metadata.getString(MediaMetadata.METADATA_KEY_TITLE), "Unknown track"));
            artist.setText(firstAvailable(metadata.getString(MediaMetadata.METADATA_KEY_ARTIST), "Spotify"));
            album.setText(firstAvailable(metadata.getString(MediaMetadata.METADATA_KEY_ALBUM), ""));
            Bitmap cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (cover == null) cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
            if (cover == null) cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
            if (cover != null) art.setImageBitmap(cover);
            else art.setImageResource(R.drawable.music);
            durationMs = Math.max(0, metadata.getLong(MediaMetadata.METADATA_KEY_DURATION));
            duration.setText(durationMs > 0 ? formatDuration(durationMs) : "--:--");
        }

        int status = state != null ? state.getState() : PlaybackState.STATE_NONE;
        boolean playing = status == PlaybackState.STATE_PLAYING || status == PlaybackState.STATE_BUFFERING;
        playPause.setImageResource(playing ? R.drawable.pause : R.drawable.play_arrow);
        playPause.setContentDescription(playing ? "Pause Spotify" : "Play Spotify");
        boolean hasSession = controller != null;
        previous.setEnabled(hasSession && state != null && (state.getActions() & PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0);
        next.setEnabled(hasSession && state != null && (state.getActions() & PlaybackState.ACTION_SKIP_TO_NEXT) != 0);
        playPause.setEnabled(hasSession);
        seekBar.setEnabled(hasSession && durationMs > 0 && state != null
                && (state.getActions() & PlaybackState.ACTION_SEEK_TO) != 0);
        updatePosition();
    }

    private void updatePosition() {
        if (!isVisible() || draggingSeek) return;
        MediaController controller = spotifyControllerSupplier.get();
        PlaybackState state = controller != null ? controller.getPlaybackState() : null;
        long position = state != null ? Math.max(0, state.getPosition()) : 0;
        if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
            long sinceUpdate = SystemClock.elapsedRealtime() - state.getLastPositionUpdateTime();
            if (sinceUpdate > 0 && state.getLastPositionUpdateTime() > 0)
                position += (long) (sinceUpdate * state.getPlaybackSpeed());
        }
        position = Math.max(0, durationMs > 0 ? Math.min(durationMs, position) : position);
        elapsed.setText(formatDuration(position));
        seekBar.setProgress(durationMs > 0 ? (int) (1000L * position / durationMs) : 0);
    }

    private static String firstAvailable(@Nullable String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    private static String formatDuration(long millis) {
        long totalSeconds = millis / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }
}
