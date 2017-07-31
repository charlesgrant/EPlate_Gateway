package com.mingbikes.eplate.model.repository.impl;

import com.alibaba.fastjson.JSON;
import com.mingbikes.eplate.entity.ParkSpaceEntity;
import com.mingbikes.eplate.model.repository.BaseRepository;
import com.mingbikes.eplate.model.repository.ParkRepository;
import com.mingbikes.eplate.model.repository.ResponseHandler;

import org.json.JSONObject;

import java.util.List;

import rx.Observable;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class ParkRepositoryImpl extends BaseRepository implements ParkRepository {

    public ParkRepositoryImpl() {
        super();
    }

    private String host = "http://106.15.197.255:8082/elicense";

    @Override
    public Observable<List<ParkSpaceEntity>> getParkSpaceList() {

        String url = host + "/bikeParkSpace/queryAll";
        return get(url, newBaseRequestParams(), new ResponseHandler<List<ParkSpaceEntity>>() {
            @Override
            protected List<ParkSpaceEntity> parse(JSONObject response, String data) {
                return JSON.parseArray(data, ParkSpaceEntity.class);
            }
        });
    }
}
