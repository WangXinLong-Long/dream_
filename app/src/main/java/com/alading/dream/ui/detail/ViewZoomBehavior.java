package com.alading.dream.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.alading.dream.R;
import com.alading.dream.ui.view.FullScreenPlayerView;
import com.alading.libcommon.utils.PixUtils;

public class ViewZoomBehavior extends CoordinatorLayout.Behavior<FullScreenPlayerView> {

    private int scrollingId;
    private int minHeight;
    private ViewDragHelper viewDragHelper;
    private View scrollingView;
    private FullScreenPlayerView refChild;
    private int childOriginalHeight;
    private boolean canFullscreen;
    private FlingRunnable runnable;
    private OverScroller overScroller;

    public ViewZoomBehavior() {
    }

    public ViewZoomBehavior(Context context, AttributeSet attributeSet) {

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.view_zoom_behavior, 0, 0);
        minHeight = typedArray.getDimensionPixelOffset(R.styleable.view_zoom_behavior_min_height, PixUtils.dp2px(200));
        scrollingId = typedArray.getResourceId(R.styleable.view_zoom_behavior_scrolling_id, 0);
        typedArray.recycle();
        overScroller = new OverScroller(context);
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullscreen || viewDragHelper == null){
          return   super.onTouchEvent(parent, child, ev);
        }
        viewDragHelper.processTouchEvent(ev);
        return true;

    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullscreen || viewDragHelper == null){
            super.onInterceptTouchEvent(parent, child, ev);
        }
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, int layoutDirection) {

        if (viewDragHelper == null) {
            viewDragHelper = ViewDragHelper.create(parent, 1.0f, mCallback);
            scrollingView = parent.findViewById(scrollingId);
            refChild = child;
            childOriginalHeight = child.getMeasuredHeight();
            canFullscreen = childOriginalHeight > parent.getMeasuredWidth();
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {

            if (canFullscreen && refChild.getBottom() >= minHeight
                    && refChild.getBottom() <= childOriginalHeight) {
                return true;
            }
            return false;
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return viewDragHelper.getTouchSlop();
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (refChild == null || dy == 0) {
                return 0;
            }

            if (dy < 0 && refChild.getBottom() < minHeight
                    || (dy > 0 && refChild.getBottom() > childOriginalHeight)
                    || (dy > 0 && (scrollingView != null && scrollingView.canScrollVertically(-1)))) {
                return 0;
            }

            int maxConsumed = 0;
            if (dy > 0) {
                if (refChild.getBottom() + dy > childOriginalHeight) {
                    maxConsumed = childOriginalHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            } else {
                if (refChild.getBottom() + dy < minHeight) {
                    maxConsumed = minHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            }

            ViewGroup.LayoutParams layoutParams = refChild.getLayoutParams();
            layoutParams.height = layoutParams.height + maxConsumed;
            refChild.setLayoutParams(layoutParams);
            if (mViewZoomCallback!=null){
                mViewZoomCallback.onDragZoom(layoutParams.height);
            }
            return maxConsumed;
        }


        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (refChild.getBottom() > minHeight && refChild.getBottom() < childOriginalHeight && yvel != 0) {
                refChild.removeCallbacks(runnable);
                runnable = new FlingRunnable(refChild);
                runnable.fling((int) xvel, (int) yvel);
            }
        }
    };


    private class FlingRunnable implements Runnable {

        private View mFlingView;

        public FlingRunnable(View flingView) {

            mFlingView = flingView;
        }

        @Override
        public void run() {
            ViewGroup.LayoutParams layoutParams = mFlingView.getLayoutParams();
            int height = layoutParams.height;

            if (overScroller.computeScrollOffset() && height >= minHeight && height <= childOriginalHeight) {
                int newHeight = Math.min(overScroller.getCurrY(),childOriginalHeight);
                if (newHeight!=height){
                    layoutParams.height = newHeight;
                    mFlingView.setLayoutParams(layoutParams);
                    if (mViewZoomCallback!=null){
                        mViewZoomCallback.onDragZoom(newHeight);
                    }
                }
                ViewCompat.postOnAnimation(mFlingView,this);
            }else {
                mFlingView.removeCallbacks(this);
            }
        }

        public void fling(int xvel, int yvel) {
            overScroller.fling(0, mFlingView.getBottom(), xvel, yvel, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            run();
        }
    }

    private ViewZoomCallback mViewZoomCallback;

    public void setViewZoomCallback(ViewZoomCallback callback) {
        this.mViewZoomCallback = callback;
    }

    public interface ViewZoomCallback {
        void onDragZoom(int height);
    }
}
