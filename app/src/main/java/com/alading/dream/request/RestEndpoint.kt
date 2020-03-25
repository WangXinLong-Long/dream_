package com.alading.dream.request

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RestEndpoint {

    private val DEBUG = true

    val publicAPI by lazy {
        builder.build().create(PublicApi::class.java)
    }

    private val builder by lazy {
        Retrofit.Builder().client(httpClient)
                .baseUrl("http://47.100.117.247:8080/dream/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
    }

    private val httpClient by lazy {
        OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS) //允许失败重试
                .retryOnConnectionFailure(true)
                .addInterceptor(loggingInterceptor)
                .build()
    }

    private val loggingInterceptor by lazy {
        val logInterceptor = HttpLoggingInterceptor()
        if (DEBUG) {
            logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            logInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }

        logInterceptor
    }
}