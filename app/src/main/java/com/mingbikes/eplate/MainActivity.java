package com.mingbikes.eplate;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.mingbikes.eplate.entity.ParkSpaceEntity;
import com.mingbikes.eplate.event.PlateFoundEvent;
import com.mingbikes.eplate.presenter.MainPresenter;
import com.mingbikes.eplate.presenter.MainPresenterImpl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    private MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        mPresenter = new MainPresenterImpl(this);
        setPresenter(mPresenter);

        initViews();
//        initMap();
        getPresenter().getParkSpaceList();
    }

    private SupportMapFragment myMap;
    private MapView mMapView;
    public BaiduMap mBaiduMap;
    private UiSettings mUiSettings;
    public BitmapDescriptor parkIconBig = null;
    public BitmapDescriptor parkIconSmall = null;
    private List<Overlay> mParkPointOverlayList = new ArrayList<>();

    TextView tv_mingbikes;
    TextView tv_m_bike;
    TextView tv_ofo;

    private void initViews() {

        myMap = SupportMapFragment.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.map, myMap, "map_fragment");
        ft.commit();

        tv_mingbikes = (TextView) findViewById(R.id.tv_mingbikes);
        tv_m_bike = (TextView) findViewById(R.id.tv_m_bike);
        tv_ofo = (TextView) findViewById(R.id.tv_ofo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMap();
    }

    private void initMap() {
        mMapView = myMap.getMapView();
        mBaiduMap = myMap.getBaiduMap();
        mUiSettings = myMap.getBaiduMap().getUiSettings();

        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setOverlookingGesturesEnabled(false);
        mBaiduMap.setMyLocationEnabled(true);

        parkIconBig = BitmapDescriptorFactory.fromResource(R.drawable.park_space_icon);
        parkIconSmall = BitmapDescriptorFactory.fromResource(R.drawable.park_space_icon_small);
    }

    private void clearList(List<Overlay> overlayList) {
        if (overlayList != null && overlayList.size() > 0) {
            for (Overlay overlay : overlayList) {
                overlay.remove();
            }
            overlayList.clear();
        }
    }

    @Override
    protected void onDestroy() {
        parkIconBig.recycle();
        parkIconSmall.recycle();
        parkIconBig = null;
        parkIconSmall = null;
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlateFoundEvent(PlateFoundEvent event) {

    }

    @Override
    public void onParkSpaceListLoad(List<ParkSpaceEntity> parkSpaceList) {
        showParkSpace(parkSpaceList);
    }

    private void showParkSpace(List<ParkSpaceEntity> parkSpaceList) {
        if (parkSpaceList == null && parkIconBig != null) {
            return;
        }
        List<OverlayOptions> optionsList = new ArrayList<>();
        for (int i = 0; i < parkSpaceList.size(); i++) {

            ParkSpaceEntity parkSpace = parkSpaceList.get(i);

            if (parkSpace == null
                    || TextUtils.isEmpty(parkSpace.getLatitude())
                    || TextUtils.isEmpty(parkSpace.getLongitude())) {
                continue;
            }

            float lat = Float.parseFloat(parkSpace.getLatitude());
            float lng = Float.parseFloat(parkSpace.getLongitude());

            Bundle b = new Bundle();
            b.putSerializable("bike", parkSpace);
            LatLng mLatLng = new LatLng(lat, lng);
            MarkerOptions marker = new MarkerOptions().position(mLatLng).icon(parkIconBig)
                    .zIndex(9).draggable(true).extraInfo(b);
            marker.animateType(MarkerOptions.MarkerAnimateType.grow);
            optionsList.add(marker);

            if (i == 0) {
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(new LatLng(lat, lng));
                builder.zoom(17f);
                myMap.getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        clearList(mParkPointOverlayList);
        if (mBaiduMap != null) {
            mParkPointOverlayList.addAll(mBaiduMap.addOverlays(optionsList));
        }
    }
}
