package com.alading.dream.ui.share;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alading.dream.R;
import com.alading.dream.ui.view.PPImageView;

import java.util.List;

public class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ResolveInfo> mShareItems;
    private View.OnClickListener mListener;
    private Context context;
    private PackageManager packageManager;
    private String mShareContent;

    public ShareAdapter(List<ResolveInfo> shareItems, String shareContent, View.OnClickListener listener) {
        mShareContent = shareContent;
        mShareItems = shareItems;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        packageManager = context.getPackageManager();
        View inflate = LayoutInflater.from(context).inflate(R.layout.layout_share_item, parent, false);

        return new RecyclerView.ViewHolder(inflate) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ResolveInfo resolveInfo = mShareItems.get(position);
        PPImageView ppImageView = holder.itemView.findViewById(R.id.share_icon);
        Drawable drawable = resolveInfo.loadIcon(packageManager);

        ppImageView.setImageDrawable(drawable);
        TextView shareText = holder.itemView.findViewById(R.id.share_text);
        shareText.setText(resolveInfo.loadLabel(packageManager));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pkg = resolveInfo.activityInfo.packageName;
                String cls = resolveInfo.activityInfo.name;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setComponent(new ComponentName(pkg,cls));
                intent.putExtra(Intent.EXTRA_TEXT,mShareContent);
                context.startActivity(intent);
                if (mListener!=null){
                    mListener.onClick(v);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mShareItems == null ?0:mShareItems.size();
    }
}
