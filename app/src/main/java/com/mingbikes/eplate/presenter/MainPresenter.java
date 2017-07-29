package com.mingbikes.eplate.presenter;

import java.util.Map;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public interface MainPresenter extends Presenter {

    void getParkSpaceList();

    void getBrandList();

    void processGatewayData(Map<String, String> data);
}
