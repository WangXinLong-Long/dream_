package com.alading.dream.exoplayer;

import android.app.Application;
import android.net.Uri;

import com.alading.libcommon.global.AppGlobals;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.util.HashMap;

public class PageListPlayManager {

    static {
        Application application = AppGlobals.getApplication();
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(application, application.getPackageName()));
        Cache cache = new SimpleCache(application.getCacheDir(), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200));
        CacheDataSinkFactory cacheDataSinkFactory = new CacheDataSinkFactory(cache, Long.MAX_VALUE);


        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                cache,
                dataSourceFactory,
                new FileDataSourceFactory(),
                cacheDataSinkFactory,
                CacheDataSource.FLAG_BLOCK_ON_CACHE,
                null);
        mediaSourceFactory = new ProgressiveMediaSource.Factory(cacheDataSourceFactory);

    }

    private static HashMap<String, PageListPlay> mPageListPlayHashMap = new HashMap<>();
    private static ProgressiveMediaSource.Factory mediaSourceFactory;

    public static MediaSource createMediaSource(String url) {
        return mediaSourceFactory.createMediaSource(Uri.parse(url));
    }

    public static PageListPlay get(String pageName) {
        PageListPlay pageListPlay = mPageListPlayHashMap.get(pageName);
        if (pageListPlay == null) {
            pageListPlay = new PageListPlay();
            mPageListPlayHashMap.put(pageName, pageListPlay);
        }
        return pageListPlay;
    }

    public static void release(String pageName) {

        PageListPlay pageListPlay = mPageListPlayHashMap.get(pageName);
        if (pageListPlay != null) {
            pageListPlay.release();
        }
    }
}
