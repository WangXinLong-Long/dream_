package com.alading.dream;

import androidx.annotation.IntDef;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

    public void execute(final JsonCallback<T> callback) {
        getCall().enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ApiResponse<T> response = new ApiResponse<>();
                response.msg = e.getMessage();
                callback.onError(response);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ApiResponse<T> apiResponse = parseResponse(response, callback);
                if (apiResponse.success) {
                    callback.onError(apiResponse);
                } else {
                    callback.onSuccess(apiResponse);
                }
            }
        });

    }

    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {

        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert mConvert = ApiService.mConvert;
        try {
            if (success){
                String content = response.body().string();

                if (callback!=null){
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) mConvert.convert(content,argument);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);

        okhttp3.Request request = generateRequest(builder);
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
