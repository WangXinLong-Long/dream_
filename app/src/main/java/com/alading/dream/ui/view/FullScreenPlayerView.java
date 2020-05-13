package com.alading.dream.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alading.dream.R;
import com.alading.dream.exoplayer.PageListPlay;
import com.alading.dream.exoplayer.PageListPlayManager;
import com.alading.libcommon.utils.PixUtils;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class FullScreenPlayerView extends ListPlayerView {

    private PlayerView exoPlayerView;

    public FullScreenPlayerView(@NonNull Context context) {
        this(context,null);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        exoPlayerView = (PlayerView) LayoutInflater.from(context).inflate(R.layout.layout_exo_player_view, null, false);
    }

    @Override
    protected void setSize(int width, int height) {
        if (width>height){
            super.setSize(width, height);
            return;
        }

        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = PixUtils.getScreenHeight();

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = maxWidth;
        layoutParams.height = maxHeight;
        setLayoutParams(layoutParams);

        FrameLayout.LayoutParams coverLayoutParams = (FrameLayout.LayoutParams) cover.getLayoutParams();
        coverLayoutParams.width = (int) (width/(height*1.0f/maxHeight));
        coverLayoutParams.height = maxHeight;
        coverLayoutParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(layoutParams);
    }

    @Override
    public void onActive() {
        super.onActive();
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = exoPlayerView;
        PlayerControlView controlView = pageListPlay.controlView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playerView == null) {
            return;
        }

        //此处我们需要主动调用一次 switchPlayerView，把播放器Exoplayer和展示视频画面的View ExoplayerView相关联
        //为什么呢？因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，然后和新创建的展示视频画面的View ExoplayerView相关联，达到视频无缝续播的效果
        //如果 我们再次返回列表页，则需要再次把播放器和ExoplayerView相关联
        pageListPlay.switchPlayerView(playerView, true);
        ViewParent parent = playerView.getParent();
        if (parent != this) {

            //把展示视频画面的View添加到ItemView的容器上
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
                //还应该暂停掉列表上正在播放的那个
                ((ListPlayerView) parent).inActive();
            }

            ViewGroup.LayoutParams coverParams = cover.getLayoutParams();
            this.addView(playerView, 1, coverParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            //把视频控制器 添加到ItemView的容器上
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(controlView, params);
        }

        //如果是同一个视频资源,则不需要从重新创建mediaSource。
        //但需要onPlayerStateChanged 否则不会触发onPlayerStateChanged()
        if (TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else {
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            pageListPlay.playUrl = mVideoUrl;
        }
        controlView.show();
        controlView.setVisibilityListener(this);
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
    }


    @Override
    public void inActive() {
        super.inActive();
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        pageListPlay.switchPlayerView(exoPlayerView,false);
    }
}
