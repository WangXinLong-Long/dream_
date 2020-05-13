package com.alading.dream.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.alading.dream.R;
import com.alading.dream.databinding.LayoutFeedDetailTypeVideoBinding;
import com.alading.dream.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.alading.dream.model.Feed;
import com.alading.dream.ui.view.FullScreenPlayerView;
import com.alading.libcommon.utils.MyLog;

public class VideoViewHandler extends ViewHandler {

    private LayoutFeedDetailTypeVideoBinding mVideoBinding;
    private final CoordinatorLayout coordinator;
    private FullScreenPlayerView playerView;
    private boolean backPressed;


    public VideoViewHandler(FragmentActivity activity) {
        super(activity);

        mVideoBinding = DataBindingUtil.setContentView(activity, R.layout.layout_feed_detail_type_video);
        mInateractionBinding = mVideoBinding.bottomInteraction;
        mRecyclerView = mVideoBinding.recyclerView;
        playerView = mVideoBinding.playerView;
        coordinator = mVideoBinding.coordinator;

        View authorInfoView = mVideoBinding.authorInfo.getRoot();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) authorInfoView.getLayoutParams();
        layoutParams.setBehavior(new ViewAnchorBehavior(R.id.player_view));

        mVideoBinding.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
        CoordinatorLayout.LayoutParams playerViewLayoutParams = (CoordinatorLayout.LayoutParams) playerView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) playerViewLayoutParams.getBehavior();
        behavior.setViewZoomCallback(new ViewZoomBehavior.ViewZoomCallback() {
            @Override
            public void onDragZoom(int height) {
                int bottom = playerView.getBottom();
                boolean moveUp = height < bottom;

                boolean fullscreen = moveUp ? height >= coordinator.getBottom() - mInateractionBinding.getRoot().getHeight()
                        : height >= coordinator.getBottom();
                MyLog.logD("VideoViewHandler::onDragZoom:height:" + height +
                        " playerView.getBottom():" + playerView.getBottom()+
                        " coordinator.getBottom():" + coordinator.getBottom()+
                        " mInateractionBinding.getRoot().getHeight():" + mInateractionBinding.getRoot().getHeight()
                        );
                setViewAppearance(fullscreen);
            }
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mVideoBinding.setFeed(feed);


        String category = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        mVideoBinding.playerView.bindData(category, mFeed.width, mFeed.height, mFeed.cover, mFeed.url);

        mVideoBinding.playerView.post(new Runnable() {
            @Override
            public void run() {

                boolean fullscreen = playerView.getBottom() >= coordinator.getBottom();
                setViewAppearance(fullscreen);
            }
        });


        LayoutFeedDetailTypeVideoHeaderBinding headerBinding = LayoutFeedDetailTypeVideoHeaderBinding.inflate(LayoutInflater.from(mActivity), mRecyclerView, false);
        headerBinding.setFeed(feed);
        listAdapter.addHeaderView(headerBinding.getRoot());
    }

    private void setViewAppearance(boolean fullscreen) {
        mVideoBinding.setFullscreen(fullscreen);
        mInateractionBinding.setFullscreen(fullscreen);
        mVideoBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullscreen ? View.VISIBLE : View.GONE);

        int inputHeight = mInateractionBinding.getRoot().getMeasuredHeight();
        int ctrlViewHeight = playerView.getPlayController().getMeasuredHeight();
        int bottom = playerView.getPlayController().getBottom();
        playerView.getPlayController().setY(fullscreen ? bottom - inputHeight - ctrlViewHeight
                : bottom - ctrlViewHeight);
        mInateractionBinding.inputView.setBackgroundResource(fullscreen ? R.drawable.bg_edit_view2 : R.drawable.bg_edit_view);
    }

    @Override
    public void onPause() {
        if (!backPressed) {
            playerView.inActive();
        }
    }

    @Override
    public void onResume() {
        backPressed = false;
        playerView.onActive();
    }

    @Override
    public void onBackPressed() {
        backPressed = true;
        playerView.getPlayController().setTranslationY(0);
    }

}
