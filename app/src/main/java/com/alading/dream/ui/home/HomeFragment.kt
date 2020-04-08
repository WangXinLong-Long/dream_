package com.alading.dream.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alading.dream.R
import com.alading.dream.model.Feed
import com.alading.dream.ui.AbsListFragment
import com.alading.dream.utils.MyLog
import com.example.libnavannotation.FragmentDestination
import com.scwang.smartrefresh.layout.api.RefreshLayout

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
class HomeFragment : AbsListFragment<Feed,HomeViewModel>() {

    private val homeViewModel by viewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        MyLog.logD("HomeFragment::onCreateView: homeViewModel:${homeViewModel}  ")
        return root
    }

    override fun getAdapter(): PagedListAdapter<Feed, RecyclerView.ViewHolder> {
        TODO("Not yet implemented")
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        TODO("Not yet implemented")
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        TODO("Not yet implemented")
    }
}
