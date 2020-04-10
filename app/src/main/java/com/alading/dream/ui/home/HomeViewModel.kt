package com.alading.dream.ui.home

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.alading.dream.ApiResponse
import com.alading.dream.ApiService
import com.alading.dream.JsonCallback
import com.alading.dream.Request
import com.alading.dream.model.Feed
import com.alading.dream.ui.AbsViewModel
import com.alading.libcommon.utils.MyLog
import com.alibaba.fastjson.TypeReference
import java.util.*

class HomeViewModel : AbsViewModel<Feed>() {

    @Volatile
    private var witchCache: Boolean = true
    override fun createDataSource(): DataSource<*, *> {
        return mDataSource
    }

    private var mDataSource = object : ItemKeyedDataSource<Int, Feed>() {
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
        var request = ApiService.get<Feed>("/feeds/queryHotFeedsList")
            .addParam("feedType", null)
            .addParam("userId", 0)
            .addParam("feedId", key)
            .addParam("pageCount", 10)
            .responseType(object : TypeReference<ArrayList<Feed>>() {}.type)

        if (witchCache) {
            request.cacheStrategy(Request.CACHE_ONLY)
            request.execute(object : JsonCallback<List<Feed>>() {
                override fun onCacheSuccess(response: ApiResponse<List<Feed>>?) {
                    MyLog.logD("HomeViewModel::onCacheSuccess: response:${response?.body?.size}  ")
                    var body = response?.body
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
        if (key>0){
            boundaryPageData.postValue(data.isNotEmpty())
        }
    }
}