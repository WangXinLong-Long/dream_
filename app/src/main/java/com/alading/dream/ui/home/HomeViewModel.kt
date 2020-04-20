package com.alading.dream.ui.home

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList
import com.alading.dream.ApiResponse
import com.alading.dream.ApiService
import com.alading.dream.JsonCallback
import com.alading.dream.Request
import com.alading.dream.model.Feed
import com.alading.dream.ui.AbsViewModel
import com.alading.dream.ui.MutablePageKeyedDataSource
import com.alading.dream.ui.login.UserManager
import com.alading.libcommon.utils.MyLog
import com.alibaba.fastjson.TypeReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class HomeViewModel : AbsViewModel<Feed>() {

    @Volatile
    private var witchCache: Boolean = true
    val cacheLiveData = MutableLiveData<PagedList<Feed>>()
    private var loadAfter = AtomicBoolean(false)

    var mFeedType: String? = null
    override fun createDataSource(): DataSource<*, *> {
//        createDataSource ….只有当当前的数据源无效的时候，才会调用到这个方法，所以此时应该创建个新的返回回去 给paging
        return FeedDataSource()/*mDataSource*/
    }

    inner class FeedDataSource/* private var mDataSource = object*/: ItemKeyedDataSource<Int, Feed>() {
        override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Feed>
        ) {
            loadData(0, callback)
            witchCache = false
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            loadData(params.key, callback)
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            callback.onResult(Collections.emptyList())

        }

        override fun getKey(item: Feed): Int {
            return item.id
        }

    }

    private fun loadData(key: Int, callback: ItemKeyedDataSource.LoadCallback<Feed>) {
        if (key > 0) {
            loadAfter.set(true)
        }
        var request = ApiService.get<Feed>("/feeds/queryHotFeedsList")
            .addParam("feedType", mFeedType)
            .addParam("userId", UserManager.get().userId)
            .addParam("feedId", key)
            .addParam("pageCount", 10)
            .responseType(object : TypeReference<ArrayList<Feed>>() {}.type)

        if (witchCache) {
            request.cacheStrategy(Request.CACHE_ONLY)
            request.execute(object : JsonCallback<List<Feed>>() {
                override fun onCacheSuccess(response: ApiResponse<List<Feed>>?) {
                    MyLog.logD("HomeViewModel::onCacheSuccess: response:${response?.body?.size}  ")
                    var dataSource = MutablePageKeyedDataSource<Feed>()
                    dataSource.data.addAll(response?.body!!)
                    var pageList = dataSource.buildNewPagedList(config)
                    cacheLiveData.postValue(pageList)
                }
            })
        }

        val netRequest = if (witchCache) {
            request.clone()
        } else {
            request
        }
        val cacheStrategy =
            if (key == 0) {
                Request.NET_CACHE
            } else {
                Request.NET_ONLY
            }
        netRequest.cacheStrategy(cacheStrategy)
        var response: ApiResponse<List<Feed>> = netRequest.execute() as ApiResponse<List<Feed>>
        var data = if (response.body == null) {
            Collections.emptyList()
        } else {
            response.body
        }
        callback.onResult(data)
        if (key > 0) {
            boundaryPageData.postValue(data.isNotEmpty())
            loadAfter.set(false)
        }

        MyLog.logD("HomeViewModel: loadData: $key ----- ")
    }

    fun loadAfter(id: Int, loadCallback: ItemKeyedDataSource.LoadCallback<Feed>) {
        if (loadAfter.get()) {
            loadCallback.onResult(Collections.emptyList())
            return
        }
        ArchTaskExecutor.getIOThreadExecutor().execute {
            loadData(id, loadCallback)
        }
    }

    fun setFeedType(feedType: String) {
        mFeedType = feedType
    }
}