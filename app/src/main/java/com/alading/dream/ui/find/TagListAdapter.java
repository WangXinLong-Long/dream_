package com.alading.dream.ui.find;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.alading.dream.databinding.LayoutTagListItemBinding;
import com.alading.dream.model.TagList;
import com.alading.dream.ui.InteractionPresenter;
import com.alading.libcommon.extention.AbsPagedListAdapter;

public class TagListAdapter extends AbsPagedListAdapter<TagList, TagListAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private ViewHolder holder;
    private int position;
    private Context mContext;

    protected TagListAdapter(Context context) {
        super(new DiffUtil.ItemCallback<TagList>() {
            @Override
            public boolean areItemsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.tagId == newItem.tagId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.equals(newItem);
            }
        });


        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected TagListAdapter.ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutTagListItemBinding mBinding = LayoutTagListItemBinding.inflate(mInflater, parent, false);

        return new ViewHolder(mBinding.getRoot(),mBinding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        TagList item = getItem(position);
        holder.bindData(item);
        holder.mBinding.actionFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InteractionPresenter.toggleTagLike((LifecycleOwner) mContext,item);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagFeedListActivity.startActivity(mContext,item);
            }
        });

    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private com.alading.dream.databinding.LayoutTagListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutTagListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(TagList item) {
            mBinding.setTagList(item);
        }
    }
}
