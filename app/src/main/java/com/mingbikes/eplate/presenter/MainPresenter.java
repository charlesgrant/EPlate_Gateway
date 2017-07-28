package com.mingbikes.eplate.presenter;

import java.util.List;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public interface MainPresenter extends Presenter {

    void getParkSpaceList();

    void getBrandList();

    void getBrandListByMacAddress(List<String> macAddressList);
}
