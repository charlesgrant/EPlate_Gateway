package com.mingbikes.eplate;

import com.mingbikes.eplate.entity.BrandEntity;
import com.mingbikes.eplate.entity.ParkSpaceEntity;

import java.util.List;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public interface MainView {

    void onBrandListLoad(List<BrandEntity> brandList);

    void onParkSpaceListLoad(List<ParkSpaceEntity> parkSpaceList);

    void onParkExtraNotify(int park);

    void onParkSpaceBrandListLoad(List<BrandEntity> brandList);
}
