package com.mingbikes.eplate.model.repository.impl;

import com.alibaba.fastjson.JSON;
import com.mingbikes.eplate.entity.BrandEntity;
import com.mingbikes.eplate.entity.BrandMacAddressEntity;
import com.mingbikes.eplate.model.repository.BaseRepository;
import com.mingbikes.eplate.model.repository.BrandRepository;
import com.mingbikes.eplate.model.repository.RequestParams;
import com.mingbikes.eplate.model.repository.ResponseHandler;

import org.json.JSONObject;

import java.util.List;

import rx.Observable;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class BrandRepositotyImpl extends BaseRepository implements BrandRepository {

    public BrandRepositotyImpl() {
        super();
    }

    private String host = "";

    @Override
    public Observable<List<BrandMacAddressEntity>> getBrandListByMacAddress(List<String> macAddressList) {
        RequestParams params = newBaseRequestParams();
        StringBuilder builder = new StringBuilder();
        for(String macAddress : macAddressList) {
            builder.append(macAddress);
            builder.append("#");
        }
        if(builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        params.put("mac", builder.toString());
        String url = host + "";
        return get(url, params, new ResponseHandler<List<BrandMacAddressEntity>>() {
            @Override
            protected List<BrandMacAddressEntity> parse(JSONObject response, String data) {
                return JSON.parseArray(data, BrandMacAddressEntity.class);
            }
        });
    }

    @Override
    public Observable<List<BrandEntity>> getBrandList() {
        String url = host + "";
        return get(url, newBaseRequestParams(), new ResponseHandler<List<BrandEntity>>() {
            @Override
            protected List<BrandEntity> parse(JSONObject response, String data) {
                return JSON.parseArray(data, BrandEntity.class);
            }
        });
    }
}
