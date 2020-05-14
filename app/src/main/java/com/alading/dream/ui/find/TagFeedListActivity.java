package com.alading.dream.ui.find;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.alading.dream.R;
import com.alading.dream.databinding.ActivityTagFeedListBinding;
import com.alading.dream.databinding.LayoutTagFeedListHeaderBinding;
import com.alading.dream.exoplayer.PageListPlayDetector;
import com.alading.dream.model.Feed;
import com.alading.dream.model.TagList;
import com.alading.dream.ui.home.FeedAdapter;
import com.alading.libcommon.extention.AbsPagedListAdapter;
import com.alading.libcommon.utils.PixUtils;
import com.alading.libcommon.view.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

public class TagFeedListActivity extends AppCompatActivity implements View.OnClickListener, OnRefreshListener, OnLoadMoreListener {
    public static final String KEY_TAG_LIST = "tag_list";
    public static final String KEY_FEED_TYPE = "tag_feed_list";
    private RecyclerView recyclerView;
    private EmptyView emptyView;
    private SmartRefreshLayout refreshLayout;
    private TagList tagList;
    private TagFeedListViewModel tagFeedListViewModel;
    private PageListPlayDetector playDetector;
    private boolean shouldPause;
    private int totalScrollY;

    public static void startActivity(Context context, TagList tagList) {
        Intent intent = new Intent(context, TagFeedListActivity.class);
        intent.putExtra(KEY_TAG_LIST, tagList);
        context.startActivity(intent);
    }

    ActivityTagFeedListBinding mBinding = null;
    private AbsPagedListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tag_feed_list);
        recyclerView = mBinding.refreshLayout.recyclerView;
        emptyView = mBinding.refreshLayout.emptyView;
        refreshLayout = mBinding.refreshLayout.refreshLayout;
        mBinding.actionBack.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = (AbsPagedListAdapter) getAdapter();
        recyclerView.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.list_divider));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setItemAnimator(null);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);

        tagList = (TagList) getIntent().getSerializableExtra(KEY_TAG_LIST);
        mBinding.setTagList(tagList);
        mBinding.setOwner(this);

        tagFeedListViewModel = ViewModelProviders.of(this).get(TagFeedListViewModel.class);
        tagFeedListViewModel.setFeedType(tagList.title);
        tagFeedListViewModel.getPageData().observe(this, feeds -> submitList(feeds));
        tagFeedListViewModel.getBoundaryPageData().observe(this, hasData -> finishRefresh(hasData));

        playDetector = new PageListPlayDetector(this, recyclerView);

        addHeaderView();
    }

    private void addHeaderView() {
        LayoutTagFeedListHeaderBinding headerBinding = LayoutTagFeedListHeaderBinding.inflate(LayoutInflater.from(this), recyclerView, false);
        headerBinding.setTagList(tagList);
        headerBinding.setOwner(this);
        adapter.addHeaderView(headerBinding.getRoot());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScrollY += dy;
                boolean overHeight = totalScrollY > PixUtils.dp2px(48);
                mBinding.tagLogo.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                mBinding.tagTitle.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                mBinding.topBarFollow.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                mBinding.actionBack.setImageResource(overHeight ? R.drawable.icon_back_black : R.drawable.icon_back_white);
                mBinding.topBar.setBackgroundColor(overHeight ? Color.WHITE : Color.TRANSPARENT);
                mBinding.topLine.setVisibility(overHeight ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void submitList(PagedList<Feed> feeds) {
        if (feeds.size() > 0) {
            adapter.submitList(feeds);
        }
        finishRefresh(feeds.size() > 0);
    }

    private void finishRefresh(boolean hasData) {
        PagedList currentList = adapter.getCurrentList();
        hasData = currentList != null && currentList.size() > 0 || hasData;

        if (hasData) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }

        RefreshState state = refreshLayout.getState();
        if (state.isOpening && state.isHeader) {
            refreshLayout.finishRefresh();
        } else if (state.isOpening && state.isFooter) {
            refreshLayout.finishLoadMore();
        }
    }

    public PagedListAdapter getAdapter() {
        return new FeedAdapter(this, KEY_FEED_TYPE) {
            @Override
            public void onViewAttachedToWindow2(@NonNull ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(@NonNull ViewHolder holder) {
                playDetector.removeTarget(holder.getListPlayerView());
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                boolean isVideo = feed.itemType == Feed.TYPE_VIDEO;
                shouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                //这个方法是在我们每提交一次 pagelist对象到adapter 就会触发一次
                //每调用一次 adpater.submitlist
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        recyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList currentList = getAdapter().getCurrentList();
        finishRefresh(currentList != null && currentList.size() > 0);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        tagFeedListViewModel.getDataSource().invalidate();
    }
}
