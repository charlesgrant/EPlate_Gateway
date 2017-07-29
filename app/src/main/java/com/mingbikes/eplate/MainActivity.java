package com.mingbikes.eplate;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.mingbikes.eplate.entity.BrandEntity;
import com.mingbikes.eplate.entity.ParkSpaceEntity;
import com.mingbikes.eplate.event.Event;
import com.mingbikes.eplate.presenter.MainPresenter;
import com.mingbikes.eplate.presenter.MainPresenterImpl;
import com.mingbikes.eplate.utils.ScreenUtil;
import com.mingbikes.eplate.view.BrandAdapter;
import com.mingbikes.lock.LockDevice;
import com.mingbikes.lock.LockManager;
import com.mingbikes.lock.LockReadCmd;
import com.mingbikes.lock.callback.OnLockEventListener;
import com.mingbikes.lock.callback.scan.OnLockNearestScanListener;
import com.mingbikes.lock.domain.LockConfig;
import com.mingbikes.lock.exception.ConnLockException;
import com.mingbikes.lock.exception.ReadLockException;
import com.mingbikes.lock.exception.WriteLockException;
import com.mingbikes.lock.utils.BluetoothCheckUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    private MainPresenter mPresenter;

    private SupportMapFragment myMap;
    private MapView mMapView;
    public BaiduMap mBaiduMap;
    private UiSettings mUiSettings;
    public BitmapDescriptor parkIconBig = null;
    public BitmapDescriptor parkIconSmall = null;
    private List<Overlay> mParkPointOverlayList = new ArrayList<>();
    private BrandAdapter mBrandAdapter;
    private String mBluetoothAddress;
    private MyOnLockSingleScanListener myOnLockSingleScanListener;
    private List<ParkSpaceEntity> mParkSpaceList = new ArrayList<>();
    private float mLocationLat = 0;
    private float mLocationLng = 0;
    private ParkSpaceEntity mCurrentParkSpace;

    private ListView ls_brand;

    private TextView tv_location;
    private TextView tv_park_space_name;
    private TextView tv_park_space_address;
    private TextView tv_park_space_total_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothCheckUtil.enableBluetooth(this);

        mPresenter = new MainPresenterImpl(this);
        setPresenter(mPresenter);

        initViews();
        getPresenter().getParkSpaceList();

        myOnLockSingleScanListener = new MyOnLockSingleScanListener(60, 100 * 10000);
        LockManager.getInstance().startLockScan(myOnLockSingleScanListener);
    }

    private void initViews() {

        myMap = SupportMapFragment.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.map, myMap, "map_fragment");
        ft.commit();

        tv_location = (TextView) findViewById(R.id.tv_location);
        tv_park_space_name = (TextView) findViewById(R.id.tv_park_space_name);
        tv_park_space_address = (TextView) findViewById(R.id.tv_park_space_address);
        tv_park_space_total_count = (TextView) findViewById(R.id.tv_park_space_total_count);

        ls_brand = (ListView) findViewById(R.id.ls_brand);
        mBrandAdapter = new BrandAdapter(this);
        ls_brand.setAdapter(mBrandAdapter);
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

        parkIconBig = BitmapDescriptorFactory.fromResource(R.drawable.kongxian_big);
        parkIconSmall = BitmapDescriptorFactory.fromResource(R.drawable.kongxian);
    }

    private LockDevice mLockDevice;

    private class MyOnLockSingleScanListener extends OnLockNearestScanListener {

        public MyOnLockSingleScanListener(int rssiThreshold, int timeoutMillis) {
            super(rssiThreshold, timeoutMillis);
        }

        @Override
        public void onDeviceFound(LockDevice lockDevice) {
            mBluetoothAddress = lockDevice.getLockConfig().getLockMacAddress();

            if (TextUtils.isEmpty(mBluetoothAddress)) {
                return;
            }

            LockManager.getInstance().stopLockScan(myOnLockSingleScanListener);

            mLockDevice = lockDevice;

            showParkSpace(mParkSpaceList);

            byte[] newPsw = {0x31, 0x38, 0x36, 0x37, 0x36, 0x36};
            byte[] newKey = {18, 32, 60, 56, 20, 03, 45, 86, 72, 21, 52, 96, 06, 11, 66, 58};

            LockConfig mConfig = new LockConfig.Builder()
                    .lockKey(newKey)
                    .lockPassword(newPsw)
                    .lockMacAddress(mBluetoothAddress)
                    .build();
            mLockDevice.updateLockConfig(mConfig);

            mLockDevice.setOnEventListener("connection device", new MyOnEventListener());
            mLockDevice.connect();
        }

        @Override
        public void onDeviceNotFound() {
            Log.e("scan device", "Device not found.");
        }

        @Override
        public void onBestDeviceFound(LockDevice lockDevice) {

        }
    }

    private class MyOnEventListener extends OnLockEventListener {

        @Override
        public void onDeviceConnecting() {
        }

        @Override
        public void onDeviceConnected() {
            Toast.makeText(MainActivity.this, "网关连接成功", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDeviceDisconnected() {
            Toast.makeText(MainActivity.this, "网关连接断开", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDeviceConnectFail(ConnLockException e) {

        }

        @Override
        public void onReadDataSuccess(int bleResult) {
            switch (bleResult) {
                case LockReadCmd.GATEWAY_COMMUNCATION_SUCCESS:
                    if (mLockDevice == null) {
                        return;
                    }
                    getPresenter().processGatewayData(mLockDevice.getLockConfig().getPlateMap());
                    break;
            }
        }

        @Override
        public void onReadDataFailure(ReadLockException e) {

        }

        @Override
        public void onWriteDataSuccess() {

        }

        @Override
        public void onWriteDataFailed(WriteLockException e) {

        }
    }

    @Override
    public void onParkExtraNotify(int park) {
        if (park == Event.PLATE_IN_TYPE) {
            showAnimation(R.drawable.icon_add);
        } else {
            showAnimation(R.drawable.icon_subtract);
        }
    }

    private void showAnimation(int res) {
        if (mMapView == null) {
            return;
        }

        int dp20 = ScreenUtil.dpToPx(25);

        MapViewLayoutParams mp = new MapViewLayoutParams.Builder()
                .position(new LatLng(mLocationLat, mLocationLng))
                .width(dp20)
                .height(dp20)
                .layoutMode(MapViewLayoutParams.ELayoutMode.mapMode)
                //这一项必须指定。用.position就是mapMod，用.point就是absoluteMod。
                .build();

//        Point point = mBaiduMap.getProjection().toScreenLocation(new LatLng(mLocationLat, mLocationLng));

        final ImageView iv_park = new ImageView(this);
        iv_park.setImageResource(res);

        final int dp = ScreenUtil.dpToPx(50);

        final TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -dp20, -dp);
        translateAnimation.setDuration(2000);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mMapView.removeView(iv_park);
                mMapView.refreshDrawableState();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        iv_park.startAnimation(translateAnimation);

        mMapView.addView(iv_park, mp);
        mMapView.refreshDrawableState();
    }

    @Override
    public void onBrandListLoad(List<BrandEntity> brandList) {
        mBrandAdapter.addList(brandList);
    }

    @Override
    public void onParkSpaceBrandListLoad(List<BrandEntity> brandList) {
        if(mCurrentParkSpace != null) {
            mCurrentParkSpace.setAmount(0);
            for (BrandEntity entity : brandList) {
                mCurrentParkSpace.addAmount(entity.getParkCount());
                showTitle(mCurrentParkSpace);
            }
        }
        mBrandAdapter.addList(brandList);
    }

    @Override
    public void onParkSpaceListLoad(List<ParkSpaceEntity> parkSpaceList) {
        mParkSpaceList.clear();
        mParkSpaceList.addAll(parkSpaceList);
        showParkSpace(mParkSpaceList);
        getPresenter().getBrandList();
    }

    private void showParkSpace(List<ParkSpaceEntity> parkSpaceList) {
        if (parkSpaceList == null || parkSpaceList.size() == 0) {
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
            MarkerOptions marker;

            if (!TextUtils.isEmpty(mBluetoothAddress) && parkSpace.getMacAddress().equals(mBluetoothAddress)) {
                marker = new MarkerOptions().position(mLatLng).icon(parkIconBig)
                        .zIndex(9).draggable(true).extraInfo(b);

            } else if (i == 0 && TextUtils.isEmpty(mBluetoothAddress)) {
                marker = new MarkerOptions().position(mLatLng).icon(parkIconBig)
                        .zIndex(9).draggable(true).extraInfo(b);
            } else {
                marker = new MarkerOptions().position(mLatLng).icon(parkIconSmall)
                        .zIndex(9).draggable(true).extraInfo(b);
            }
            marker.animateType(MarkerOptions.MarkerAnimateType.grow);

            if (!TextUtils.isEmpty(mBluetoothAddress) && parkSpace.getMacAddress().equals(mBluetoothAddress)) {
                mLocationLat = lat;
                mLocationLng = lng;
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(new LatLng(lat, lng));
                builder.zoom(23f);
                myMap.getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                mCurrentParkSpace = parkSpace;
                showTitle(mCurrentParkSpace);
            } else if (i == 0 && TextUtils.isEmpty(mBluetoothAddress)) {
                mLocationLat = lat;
                mLocationLng = lng;
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(new LatLng(lat, lng));
                builder.zoom(23f);
                myMap.getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                mCurrentParkSpace = parkSpace;
                showTitle(mCurrentParkSpace);
            }

            optionsList.add(marker);
        }

        clearList(mParkPointOverlayList);
        if (mBaiduMap != null) {
            mParkPointOverlayList.addAll(mBaiduMap.addOverlays(optionsList));
        }
    }

    private void showTitle(ParkSpaceEntity entity) {
        if (entity == null) {
            return;
        }
        tv_location.setText("广州天河区");
        tv_park_space_name.setText(entity.getName());
        tv_park_space_address.setText(entity.getLocation());
        tv_park_space_total_count.setText(entity.getAmount() + "/" + entity.getCapacity());

        if (entity.getCapacity() - entity.getAmount() > 0) {
            tv_park_space_total_count.setTextColor(Color.parseColor("#ff222222"));
        } else {
            tv_park_space_total_count.setTextColor(Color.parseColor("#ffcc0000"));
        }
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
        LockManager.getInstance().stopLockScan(myOnLockSingleScanListener);
        if (parkIconBig != null) {
            parkIconBig.recycle();
            parkIconBig = null;
        }
        if (parkIconSmall != null) {
            parkIconSmall.recycle();
            parkIconSmall = null;
        }
        if(mParkSpaceList != null) {
            mParkSpaceList.clear();
        }
        clearList(mParkPointOverlayList);
        super.onDestroy();
    }
}
