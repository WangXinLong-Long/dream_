package com.alading.dream.ui.home

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.ItemKeyedDataSource
import com.alading.dream.exoplayer.PageListPlayDetector
import com.alading.dream.model.Feed
import com.alading.dream.ui.AbsListFragment
import com.alading.dream.ui.MutablePageKeyedDataSource
import com.alading.libcommon.utils.MyLog
import com.example.libnavannotation.FragmentDestination
import com.scwang.smartrefresh.layout.api.RefreshLayout

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
class HomeFragment : AbsListFragment<Feed, HomeViewModel>() {

    var playDetector:PageListPlayDetector? = null

    override fun getAdapter(): FeedAdapter {
        var feedType = if (arguments == null) {
            "all"
        } else {
            arguments?.getString("feedType")
        }
        return object :FeedAdapter(context, feedType){
            override fun onViewAttachedToWindow(holder: ViewHolder) {
                super.onViewAttachedToWindow(holder)
if (holder.isVideoItem){
    playDetector?.addTarget(holder.getListPlayerView())
}
            }

            override fun onViewDetachedFromWindow(holder: ViewHolder) {
                super.onViewDetachedFromWindow(holder)
                playDetector?.removeTarget(holder.getListPlayerView())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.cacheLiveData.observe(viewLifecycleOwner, Observer { feeds ->
            submitList(feeds)
        })

         playDetector = PageListPlayDetector(this,mRecyclerView)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        var currentList = adapter.currentList
        if (currentList == null ||currentList.isEmpty()) {
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
        playDetector?.onPause()
        super.onPause()

    }

    override fun onResume() {
        MyLog.logD("HomeFragment: onResume:  ----- ${playDetector == null}")
        playDetector?.onResume()
        super.onResume()
    }
}
