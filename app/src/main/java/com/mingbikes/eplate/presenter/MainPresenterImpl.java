package com.mingbikes.eplate.presenter;


import android.text.TextUtils;
import android.util.Log;

import com.mingbikes.eplate.MainView;
import com.mingbikes.eplate.entity.BrandEntity;
import com.mingbikes.eplate.entity.BrandMacAddressEntity;
import com.mingbikes.eplate.entity.ParkSpaceEntity;
import com.mingbikes.eplate.event.Event;
import com.mingbikes.eplate.event.PlateFoundEvent;
import com.mingbikes.eplate.model.repository.BrandRepository;
import com.mingbikes.eplate.model.repository.ParkRepository;
import com.mingbikes.eplate.model.repository.impl.BrandRepositotyImpl;
import com.mingbikes.eplate.model.repository.impl.ParkRepositoryImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class MainPresenterImpl extends BasePresenter implements MainPresenter {

    private ParkRepository mParkRepository;
    private BrandRepository mBrandRepository;
    private MainView mView;

    private Map<String, Integer> mPlateSpaceMap = new HashMap();
    private Map<String, String> mBrandMacAddressMap = new HashMap();
    private List<BrandEntity> mBrandList = new ArrayList<>();
    private List<String> mMacAddressList = new ArrayList<>();

    public MainPresenterImpl(MainView view) {
        mParkRepository = new ParkRepositoryImpl();
        mBrandRepository = new BrandRepositotyImpl();

        mView = view;
    }

    @Override
    public void getParkSpaceList() {
        addRxTask(mParkRepository.getParkSpaceList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ParkSpaceEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<ParkSpaceEntity> parkSpaceEntities) {
                        int count = parkSpaceEntities.size();
                        for(int index = 0; index < count; index ++) {
                            parkSpaceEntities.get(index).setAmount(0);
                        }
                        mView.onParkSpaceListLoad(parkSpaceEntities);
                    }
                }));
    }

    @Override
    public void getBrandList() {
        addRxTask(mBrandRepository.getBrandList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<BrandEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<BrandEntity> brandList) {
                        mView.onBrandListLoad(brandList);
                        mBrandList.clear();
                        mBrandList.addAll(brandList);
                        for (BrandEntity entity : brandList) {
                            mPlateSpaceMap.put(entity.getId(), 0);
                        }
                    }
                }));
    }

    private Map<String, String> oldMap = new HashMap<>();
    private List<PlateFoundEvent> mPlateFoundList = new ArrayList<>();

    @Override
    public void processGatewayData(Map<String, String> data) {
        checkDevices(data);
    }

    private void checkDevices(Map<String, String> newMap) {

        mPlateFoundList.clear();
        Collection<String> list = newMap.values();
        for (String s : list) {
            Log.e("新的-----",s);
            if (!oldMap.containsKey(s)) {
                mPlateFoundList.add(new PlateFoundEvent(s, Event.PLATE_IN_TYPE));
            }
        }

        Collection<String> list1 = oldMap.values();
        for (String s : list1) {
            Log.e("旧的-----",s);
            if (!newMap.containsKey(s)) {
                mPlateFoundList.add(new PlateFoundEvent(s, Event.PLATE_OUT_TYPE));
            }
        }

        oldMap = newMap;

        for (PlateFoundEvent event:mPlateFoundList) {
            Log.e("----",event.macAddress+"------"+event.type);
        }

        processPlateFoundData(mPlateFoundList);
    }

    private void processPlateFoundData(List<PlateFoundEvent> eventList) {

        mMacAddressList.clear();
        for (PlateFoundEvent event : eventList) {
            mMacAddressList.add(event.macAddress);
        }

        getBrandListByMacAddress(mMacAddressList);
    }

    private void getBrandListByMacAddress(List<String> macAddressList) {
        if(macAddressList == null || macAddressList.size() == 0) {
            return;
        }
        addRxTask(mBrandRepository.getBrandListByMacAddress(macAddressList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<BrandMacAddressEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<BrandMacAddressEntity> brandList) {
                        for (BrandMacAddressEntity entity : brandList) {
                            if(TextUtils.isEmpty(entity.getId())) {
                                continue;
                            }
                            mBrandMacAddressMap.put(entity.getMac(), entity.getId());
                        }
                        processPlateSpaceData();
                    }
                }));
    }

    private void processPlateSpaceData() {

        int parkBikeCount = 0;

        for (PlateFoundEvent event : mPlateFoundList) {

            if (event == null || TextUtils.isEmpty(event.macAddress)) {
                continue;
            }

            String brandId = mBrandMacAddressMap.get(event.macAddress);

            if (!mPlateSpaceMap.containsKey(brandId)) {
                continue;
            }

            parkBikeCount = mPlateSpaceMap.get(brandId);

            if (event.type == Event.PLATE_IN_TYPE) {
                // in
                parkBikeCount += 1;
                Log.e("===", "===>PLATE_IN_TYPE:" + event.macAddress + "," + "brandId:" + brandId);
            } else {
                // out
                parkBikeCount -= 1;
                Log.e("===", "===>PLATE_OUT_TYPE:" + event.macAddress + "," + "brandId:" + brandId);
            }

            mView.onParkExtraNotify(event.type);

            mPlateSpaceMap.put(brandId, parkBikeCount);
        }

        if (mPlateSpaceMap.size() == 0) {
            return;
        }

        int count = mBrandList.size();
        for (int index = 0; index < count; index++) {
            String id = mBrandList.get(index).getId();
            if (mPlateSpaceMap.containsKey(id)) {
                mBrandList.get(index).setParkCount(mPlateSpaceMap.get(id));
            }
        }

        mView.onParkSpaceBrandListLoad(mBrandList);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mMacAddressList != null) {
            mMacAddressList.clear();
        }
        if (mPlateSpaceMap != null) {
            mPlateSpaceMap.clear();
        }
        if (mBrandMacAddressMap != null) {
            mBrandMacAddressMap.clear();
        }
        if (mBrandList != null) {
            mBrandList.clear();
        }
    }
}
