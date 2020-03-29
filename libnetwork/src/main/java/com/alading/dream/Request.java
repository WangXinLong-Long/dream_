package com.alading.dream;

import androidx.annotation.IntDef;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public abstract class Request<T, R extends Request> {
    protected String mUrl;

    protected HashMap<String, String> headers = new HashMap<>();
    protected HashMap<String, Object> params = new HashMap<>();

    //仅仅只访问本地缓存
    public static final int CACHE_ONLY = 1;
    //先访问缓存，同时发起网络的请求，成功后缓存到本地
    public static final int CACHE_FIRST = 2;
    //仅仅只访问服务器，不存储
    public static final int NET_ONLY = 3;
    //先访问网咯，成功后缓存到本地
    public static final int NET_CACHE = 4;
    private String cacheKey;

    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE})
    public @interface CacheStrategy {

    }

    public Request(String url) {
//user/list
        this.mUrl = url;
    }


    public R addHeader(String key, String value) {
        headers.put(key, value);
        return (R) this;
    }


    public R addParam(String key, Object value) {
        try {
            Field field = value.getClass().getField("TYPE");
            Class aClass = (Class) field.get(null);
            if (aClass.isPrimitive()) {
                params.put(key, value);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return (R) this;
    }


    public R cacheKey(String key) {
        this.cacheKey = key;
        return (R) this;
    }

    public void execute() {

    }

    public void execute(JsonCallback<T> callback) {
        getCall();

    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);

        okhttp3.Request request =  generateRequest(builder);
        Call call = ApiService.okHttpClient.newCall(request);
        return call;
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }


}
