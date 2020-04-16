package com.alading.dream.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;

import com.alading.dream.R;
import com.alading.libcommon.global.AppGlobals;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class PageListPlay {

    public PlayerView playerView;
    public PlayerControlView controlView;
    public SimpleExoPlayer exoPlayer;
public String pageUrl;
    public PageListPlay() {

        Application application = AppGlobals.getApplication();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application,
                new DefaultRenderersFactory(application),
                new DefaultTrackSelector(),
                new DefaultLoadControl());

        playerView = (PlayerView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_view, null, false);
        controlView = (PlayerControlView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_contorller_view, null, false);
        playerView.setPlayer(exoPlayer);
        controlView.setPlayer(exoPlayer);
    }

    public void release() {
        if ((exoPlayer != null)) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop(true);
            exoPlayer.release();
            exoPlayer = null;
        }

        if ((playerView != null)) {
            playerView.setPlayer(null);
            playerView = null;
        }

        if (controlView != null) {
            controlView.setPlayer(null);
            controlView.setVisibilityListener(null);
            controlView = null;
        }
    }
}
