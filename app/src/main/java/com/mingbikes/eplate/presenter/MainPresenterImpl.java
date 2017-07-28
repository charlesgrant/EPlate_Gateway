package com.mingbikes.eplate.presenter;


import com.mingbikes.eplate.MainView;
import com.mingbikes.eplate.entity.BrandEntity;
import com.mingbikes.eplate.entity.BrandMacAddressEntity;
import com.mingbikes.eplate.entity.ParkSpaceEntity;
import com.mingbikes.eplate.model.repository.BrandRepository;
import com.mingbikes.eplate.model.repository.ParkRepository;
import com.mingbikes.eplate.model.repository.impl.BrandRepositotyImpl;
import com.mingbikes.eplate.model.repository.impl.ParkRepositoryImpl;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class MainPresenterImpl extends BasePresenter implements MainPresenter {

    private ParkRepository mParkRepository;
    private BrandRepository mBrandRepositoty;
    private MainView mView;

    public MainPresenterImpl(MainView view) {
        mParkRepository = new ParkRepositoryImpl();
        mBrandRepositoty = new BrandRepositotyImpl();

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
                        mView.onParkSpaceListLoad(parkSpaceEntities);
                    }
                }));
    }

    @Override
    public void getBrandList() {
        addRxTask(mBrandRepositoty.getBrandList()
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

                    }
                }));
    }

    @Override
    public void getBrandListByMacAddress(List<String> macAddressList) {
        addRxTask(mBrandRepositoty.getBrandListByMacAddress(macAddressList)
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

                    }
                }));
    }
}
