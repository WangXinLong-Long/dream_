package com.alading.dream.ui.home

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList
import com.alading.dream.exoplayer.PageListPlayDetector
import com.alading.dream.model.Feed
import com.alading.dream.ui.AbsListFragment
import com.alading.dream.ui.MutablePageKeyedDataSource
import com.example.libnavannotation.FragmentDestination
import com.scwang.smartrefresh.layout.api.RefreshLayout

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
class HomeFragment : AbsListFragment<Feed, HomeViewModel>() {

    var playDetector: PageListPlayDetector? = null
    var feedType: String? = null
    private var shouldPause = true

    companion object {
        fun newInstance(feedType: String?): HomeFragment {
            val args = Bundle()
            args.putString("feedType", feedType)
            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
        }

    }

    override fun getAdapter(): FeedAdapter {
        feedType = if (arguments == null) {
            "all"
        } else {
            arguments?.getString("feedType")
        }
        return object : FeedAdapter(context, feedType) {
            override fun onViewAttachedToWindow2(holder: ViewHolder) {
                if (holder.isVideoItem) {
                    playDetector?.addTarget(holder.getListPlayerView())
                }
            }

            override fun onViewDetachedFromWindow2(holder: ViewHolder) {
                playDetector?.removeTarget(holder.getListPlayerView())
            }

            override fun onStartFeedDetailActivity(feed: Feed?) {
                super.onStartFeedDetailActivity(feed)
                val isVideo = feed!!.itemType == Feed.TYPE_VIDEO
                shouldPause = !isVideo
            }

            override fun onCurrentListChanged(
                previousList: PagedList<Feed>?,
                currentList: PagedList<Feed>?
            ) {
                super.onCurrentListChanged(previousList, currentList)
                if (previousList!=null && currentList!=null){
                    if (!currentList.containsAll(previousList)){
                        mRecyclerView.scrollToPosition(0)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.cacheLiveData.observe(viewLifecycleOwner, Observer { feeds ->
            submitList(feeds)
        })

        playDetector = PageListPlayDetector(this, mRecyclerView)
        mViewModel.setFeedType(feedType!!)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        var currentList = adapter.currentList
        if (currentList == null || currentList.isEmpty()) {
            finishRefresh(false)
            return
        }
        var feed = currentList[adapter.itemCount - 1]!!
        mViewModel.loadAfter(feed.id, object : ItemKeyedDataSource.LoadCallback<Feed>() {
            override fun onResult(data: List<Feed>) {
                var config = adapter.currentList!!.config
                if (data.isNotEmpty()) {
                    val dataSouse = MutablePageKeyedDataSource<Feed>()

                    //这里要把列表上已经显示的先添加到dataSource.data中
                    //而后把本次分页回来的数据再添加到dataSource.data中
                    dataSouse.data.addAll(currentList)
                    dataSouse.data.addAll(data)
                    var pagedList = dataSouse.buildNewPagedList(config)
                    submitList(pagedList)
                }
            }

        })
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel.dataSource.invalidate()
    }


    override fun onPause() {
        if (shouldPause)
            playDetector?.onPause()
        super.onPause()

    }

    override fun onResume() {
        shouldPause = true
        if (parentFragment != null) {
            if (parentFragment!!.isVisible && isVisible) {
                playDetector?.onResume()
            }
        } else {
            if (isVisible) {
                playDetector?.onResume()
            }
        }
        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            playDetector?.onPause()
        } else {
            playDetector?.onResume()
        }
    }
}
