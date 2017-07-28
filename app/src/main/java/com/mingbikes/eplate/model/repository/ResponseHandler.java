package com.mingbikes.eplate.model.repository;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Subscriber;

public abstract class ResponseHandler<T> {

    private int status;
    private JSONObject response;
    private String msg;
    private Subscriber<? super T> subscriber;

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public JSONObject getResponse() {
        return response;
    }

    public void setResponse(JSONObject response) throws JSONException {
        if (response == null) {
            return;
        }
        this.response = response;
        status = response.optInt("status");
        msg = response.optString("msg");
        if (msg == null) {
            msg = "UnKnown";
        }
    }

    public void setResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            setResponse(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            msg = "服务器异常";
            status = 404;
        }
    }

    public void setSubscriber(Subscriber<? super T> subscriber) {
        this.subscriber = subscriber;
    }

    public boolean execute() {
        String data = null;
        if (response.has("data")) {
            data = response.optString("data");
        } else if (response.has("datas")) {
            data = response.optString("datas");
        }
        T t = parse(response, data);
        if (t != null) {
            subscriber.onNext(t);
        } else {
            Log.e("ResponseHandler", "返回值为空");
        }
        subscriber.onCompleted();
        return true;
    }

    protected abstract T parse(JSONObject response, String data);
}