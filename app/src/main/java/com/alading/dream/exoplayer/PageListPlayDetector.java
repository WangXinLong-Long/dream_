package com.alading.dream.exoplayer;

import android.graphics.Point;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.alading.libcommon.utils.MyLog;

import java.util.ArrayList;
import java.util.List;

//看看 mTargets
//看看 delayAutoPlay
public class PageListPlayDetector {

    private List<IPlayTarget> mTarget = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private Point rvLocation;
    private IPlayTarget playingTarget;

    public void addTarget(IPlayTarget target) {
        mTarget.add(target);
    }

    public void removeTarget(IPlayTarget target) {
        mTarget.remove(target);
    }

    public PageListPlayDetector(LifecycleOwner owner, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;

        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    playingTarget = null;
//                    mTargets.clear();
                    recyclerView.removeCallbacks(runnable);
                    recyclerView.getAdapter().unregisterAdapterDataObserver(mDataObserver);
                    owner.getLifecycle().removeObserver(this);
                }
            }
        });
        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    autoPlay();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx == 0 && dy == 0) {
                    MyLog.logD("PageListPlayDetector: onScrolled: dx == 0 && dy == 0" );
                    postAutoPlay();
                }
                if (playingTarget != null && playingTarget.isPlaying() && !isTargetInBounds(playingTarget)) {
                    playingTarget.inActive();
                }
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            autoPlay();
        }
    };

    private void postAutoPlay() {
        mRecyclerView.post(runnable);
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            MyLog.logD("PageListPlayDetector: onItemRangeInserted: " );
            postAutoPlay();
        }
    };

    private void autoPlay() {

        if (mTarget.size() <= 0 || mRecyclerView.getChildCount() <= 0) {
            return;
        }


        if (playingTarget != null && playingTarget.isPlaying() && isTargetInBounds(playingTarget)) {
            return;
        }

        IPlayTarget activeTarget = null;
        for (IPlayTarget target : mTarget) {
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }

        if (activeTarget != null) {
            if (playingTarget != null && playingTarget.isPlaying()) {

                playingTarget.inActive();
            }
            playingTarget = activeTarget;
            playingTarget.onActive();
        }
    }

    private boolean isTargetInBounds(IPlayTarget target) {
        ViewGroup owner = target.getOwner();
        ensureRecyclerViewLocation();
        if (!owner.isShown() || !owner.isAttachedToWindow()) {
            return false;
        }

        int[] location = new int[2];
        owner.getLocationOnScreen(location);

        int center = location[1] + owner.getHeight() / 2;
        return center >= rvLocation.x && center <= rvLocation.y;
    }

    private Point ensureRecyclerViewLocation() {
        if (rvLocation == null) {
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);
            int top = location[1];
            int bottom = top + mRecyclerView.getHeight();
            rvLocation = new Point(top, bottom);
        }
        return rvLocation;
    }

    public void onPause() {
        if (playingTarget != null) {
            playingTarget.inActive();
        }
    }

    public void onResume() {
        MyLog.logD("PageListPlayDetector: onResume: " + (playingTarget == null));
        if (playingTarget != null) {
            playingTarget.onActive();
        }
    }
}