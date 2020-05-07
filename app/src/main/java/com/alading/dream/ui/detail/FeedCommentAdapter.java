package com.alading.dream.ui.detail;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.alading.dream.databinding.LayoutFeedCommentListItemBinding;
import com.alading.dream.model.Comment;
import com.alading.dream.ui.login.UserManager;
import com.alading.libcommon.extention.AbsPagedListAdapter;
import com.alading.libcommon.utils.PixUtils;

public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater mInflater;

    protected FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });

        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding.inflate(mInflater, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bindData(item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private com.alading.dream.databinding.LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment item) {
            mBinding.setComment(item);
            boolean self = item.author == null ? false : UserManager.get().getUserId() == item.author.userId;
            mBinding.labelAuthor.setVisibility(
                    self ? View.VISIBLE : View.GONE);
            mBinding.commentDelete.setVisibility(
                    self ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(item.imageUrl)) {
                mBinding.commentCover.setVisibility(View.VISIBLE);
                mBinding.commentCover.bindData( item.width, item.height, 0,
                        PixUtils.dp2px(200), PixUtils.dp2px(200), item.imageUrl);

                if (!TextUtils.isEmpty(item.videoUrl)){
                    mBinding.videoIcon.setVisibility(View.VISIBLE);
                }else {
                    mBinding.videoIcon.setVisibility(View.GONE);
                }

            }else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
            }
        }
    }
}
