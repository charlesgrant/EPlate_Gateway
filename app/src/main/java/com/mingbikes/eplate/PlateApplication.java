package com.mingbikes.eplate;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.mingbikes.lock.LockManager;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class PlateApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SDKInitializer.initialize(this);

        LockManager.getInstance().setDebugMode(true);
        LockManager.getInstance().init(this, "d5bd135eac3b423bbe808b1f2da0839e", "9d450cae0fda4ad6804748416df7ce9f");
    }
}
