package com.mingbikes.eplate.model.repository;

import com.mingbikes.eplate.entity.BrandEntity;
import com.mingbikes.eplate.entity.BrandMacAddressEntity;

import java.util.List;

import rx.Observable;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public interface BrandRepository {

    Observable<List<BrandMacAddressEntity>> getBrandListByMacAddress(List<String> macAddressList);

    Observable<List<BrandEntity>> getBrandList();
}
