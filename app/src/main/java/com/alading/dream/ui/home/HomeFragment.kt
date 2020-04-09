package com.alading.dream.ui.home

import com.alading.dream.model.Feed
import com.alading.dream.ui.AbsListFragment
import com.example.libnavannotation.FragmentDestination
import com.scwang.smartrefresh.layout.api.RefreshLayout

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
class HomeFragment : AbsListFragment<Feed,HomeViewModel>() {





    override fun getAdapter(): FeedAdapter {
        var feedType = if (arguments == null) {
            "all"
        } else {
            arguments?.getString("feedType")
        }
        return   FeedAdapter(context,feedType)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {

    }

    override fun afterCreateView() {

    }
}
