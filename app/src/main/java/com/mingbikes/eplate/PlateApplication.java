package com.mingbikes.eplate;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class PlateApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SDKInitializer.initialize(this);
    }
}
