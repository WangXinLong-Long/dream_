package com.alading.dream.ui.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alading.libcommon.utils.PixUtils;
import com.alading.libcommon.view.CornerFrameLayout;
import com.alading.libcommon.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

public class ShareDialog extends AlertDialog {
    List<ResolveInfo> shareItems = new ArrayList<>();
    private ShareAdapter shareAdapter;
    private String mShareContent;
    private View.OnClickListener mListener;

    public ShareDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        CornerFrameLayout layout = new CornerFrameLayout(getContext());
        layout.setBackgroundColor(Color.WHITE);
        layout.setViewOutline(PixUtils.dp2px(20), ViewHelper.RADIUS_TOP);

        RecyclerView gridView = new RecyclerView(getContext());
        gridView.setLayoutManager(new GridLayoutManager(getContext(),4));

        shareAdapter = new ShareAdapter(shareItems,mShareContent,mListener);
        gridView.setAdapter(shareAdapter);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = PixUtils.dp2px(20);
        layoutParams.leftMargin = layoutParams.topMargin = layoutParams.rightMargin = layoutParams.bottomMargin = margin;
        layoutParams.gravity = Gravity.CENTER;
        layout.addView(gridView,layoutParams);
        setContentView(layout);

        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        queryShareItem();
    }

    public void setShareContent(String shareContent){

        mShareContent = shareContent;
    }
    public void setShareItemClickListener(View.OnClickListener listener) {

        mListener = listener;
    }
    private void queryShareItem() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");


        List<ResolveInfo> resolveInfos = getContext().getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (TextUtils.equals(packageName,"com.tencent.mm")||TextUtils.equals(packageName,"com.tencent.mobileqq")){
                shareItems.add(resolveInfo);
            }
        }
        shareAdapter.notifyDataSetChanged();
    }
}
