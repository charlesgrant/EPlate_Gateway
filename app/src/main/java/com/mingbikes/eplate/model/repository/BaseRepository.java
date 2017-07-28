package com.mingbikes.eplate.model.repository;

import android.util.Log;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public abstract class BaseRepository {

    private final OkHttpClient mHttpClient;

    public BaseRepository() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//		builder.interceptors().add(new HttpDNSInterceptor());
        mHttpClient = builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    protected <T> Observable<T> get(final String url, final RequestParams params, final ResponseHandler<T> responseHandler) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                try {
                    String responseText = get(url, params);
                    Log.e("request", "==>" + responseText);
                    responseHandler.setResponse(responseText);
                    responseHandler.setSubscriber(subscriber);
                    responseHandler.execute();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private String request(Request request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            Response response = mHttpClient.newCall(request).execute();
            String result = response.body().string();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected RequestParams newBaseRequestParams() {
        RequestParams params = new RequestParams();
        return params;
    }

    public String get(String url, RequestParams requestParams) throws Exception {
        return getReal(url, requestParams, null);
    }

    public String getReal(String url, RequestParams requestParams, Map<String, String> headers) throws Exception {

        if (requestParams != null && requestParams.size() > 0) {
            url = url + "?" + requestParams.toEncodeString();
        }
        Request.Builder builder = new Request.Builder().get().url(url);
        if (headers != null && headers.size() > 0) {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                builder.addHeader(key, headers.get(key));
            }
        }
        return request(builder.build());
    }
}
