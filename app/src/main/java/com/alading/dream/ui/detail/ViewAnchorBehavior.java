package com.alading.dream.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.alading.dream.R;
import com.alading.libcommon.utils.PixUtils;

public class ViewAnchorBehavior extends CoordinatorLayout.Behavior<View> {

    private int anchorId;
    private int extraUsed;

    public ViewAnchorBehavior(int anchorId) {
        this.anchorId = anchorId;
        extraUsed = PixUtils.dp2px(48);
    }

    public ViewAnchorBehavior(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.view_anchor_behavior);
        anchorId = typedArray.getResourceId(R.styleable.view_anchor_behavior_anchorId, 0);
        typedArray.recycle();
        extraUsed = PixUtils.dp2px(48);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return anchorId == dependency.getId();
    }

    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent,
                                  @NonNull View child,
                                  int parentWidthMeasureSpec,
                                  int widthUsed,
                                  int parentHeightMeasureSpec,
                                  int heightUsed) {
        View anchorView = parent.findViewById(anchorId);
        if (anchorView == null) {
            return false;
        }

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        int topMargin = layoutParams.topMargin;
        int bottom = anchorView.getBottom();

        heightUsed = bottom + topMargin + extraUsed;
        parent.onMeasureChild(child, parentWidthMeasureSpec, 0, parentHeightMeasureSpec, heightUsed);
        return true;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        View anchorView = parent.findViewById(anchorId);
        if (anchorView == null) {
            return false;
        }

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        int topMargin = layoutParams.topMargin;
        int bottom = anchorView.getBottom();
        parent.onLayoutChild(child, layoutDirection);
        child.offsetTopAndBottom(bottom + topMargin);

        return true;
    }

}
