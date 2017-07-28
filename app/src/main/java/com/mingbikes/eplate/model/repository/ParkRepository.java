package com.mingbikes.eplate.model.repository;

import com.mingbikes.eplate.entity.ParkSpaceEntity;

import java.util.List;

import rx.Observable;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public interface ParkRepository {

    Observable<List<ParkSpaceEntity>> getParkSpaceList();
}
