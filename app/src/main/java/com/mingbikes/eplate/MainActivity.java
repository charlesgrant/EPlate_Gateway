package com.mingbikes.eplate;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
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
import com.mingbikes.lock.callback.scan.OnLockMacScanListener;
import com.mingbikes.lock.domain.LockConfig;
import com.mingbikes.lock.exception.ConnLockException;
import com.mingbikes.lock.exception.ReadLockException;
import com.mingbikes.lock.exception.WriteLockException;
import com.mingbikes.lock.utils.BluetoothCheckUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    private MainPresenter mPresenter;

    private SupportMapFragment myMap;
    private MapView mMapView;
    public BaiduMap mBaiduMap;
    private UiSettings mUiSettings;
    public BitmapDescriptor parkIconBig = null;
    public BitmapDescriptor parkIconSmall = null;
    /**
     *@Fields mInfoWindow : 弹出的窗口
     */
    private InfoWindow mInfoWindow;
    private List<Overlay> mParkPointOverlayList = new ArrayList<>();
    private BrandAdapter mBrandAdapter;
    private String mBluetoothAddress;
    private MyOnLockMacScanListener myOnLockSingleScanListener;
    private Map<String, ParkSpaceEntity> mParkSpaceByMacAddressMap = new HashMap<>();
    private float mLocationLat = 0;
    private float mLocationLng = 0;
    private ParkSpaceEntity mCurrentParkSpace;
    // 贝塞尔曲线中间过程点坐标
    private float[] mCurrentPosition = new float[2];
    private LockDevice mLockDevice;

    private RelativeLayout activity_main;
    private ListView ls_brand;

    private TextView tv_status;
    private TextView tv_park_space_name;
    private TextView tv_park_space_address;

    private LinearLayout ll_park_space_total;
    private TextView tv_park_space_total_count;
    private TextView tv_park_space_tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothCheckUtil.enableBluetooth(this);

        mPresenter = new MainPresenterImpl(this);
        setPresenter(mPresenter);

        initViews();
        setStatus("获取停车位");
        getPresenter().getParkSpaceList();
    }

    private void initViews() {

        myMap = SupportMapFragment.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.map, myMap, "map_fragment");
        ft.commit();

        activity_main = (RelativeLayout) findViewById(R.id.activity_main);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_park_space_name = (TextView) findViewById(R.id.tv_park_space_name);
        tv_park_space_address = (TextView) findViewById(R.id.tv_park_space_address);
        tv_park_space_total_count = (TextView) findViewById(R.id.tv_park_space_total_count);
        tv_park_space_tip = (TextView) findViewById(R.id.tv_park_space_tip);
        ll_park_space_total = (LinearLayout) findViewById(R.id.ll_park_space_total);

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

        //添加marker点击事件的监听
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //从marker中获取info信息
                Bundle bundle = marker.getExtraInfo();
                ParkSpaceEntity parkSpaceEntity = (ParkSpaceEntity) bundle.getSerializable("park_space");

                mBluetoothAddress = parkSpaceEntity.getMacAddress();
                mCurrentParkSpace = mParkSpaceByMacAddressMap.get(mBluetoothAddress);
                showTitle(mCurrentParkSpace);
                startScanForMacAddress();

                View view = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.view_info, null);

                TextView tv_marker_name = (TextView) view.findViewById(R.id.tv_marker_name);
                tv_marker_name.setText(parkSpaceEntity.getName());

                final LatLng ll = marker.getPosition();
                Point p = mBaiduMap.getProjection().toScreenLocation(ll);
                p.y -= 54;
                LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);

                mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(view), llInfo
                        , -54, new InfoWindow.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick() {
                        //隐藏InfoWindow
                        mBaiduMap.hideInfoWindow();
                    }
                });

                MapStatusUpdate m = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.setMapStatus(m);
                mBaiduMap.showInfoWindow(mInfoWindow);

                return true;
            }
        });
    }

    private void startScanForMacAddress() {
        if(TextUtils.isEmpty(mBluetoothAddress)) {
            return;
        }
        setStatus("正在扫描...");
        myOnLockSingleScanListener = new MyOnLockMacScanListener(100 * 10000, mBluetoothAddress);
        LockManager.getInstance().startLockScan(myOnLockSingleScanListener);
    }

    private class MyOnLockMacScanListener extends OnLockMacScanListener {

        public MyOnLockMacScanListener(long timeoutMillis, String mMac) {
            super(timeoutMillis, mMac);
        }

        @Override
        public void onDeviceFound(LockDevice lockDevice) {

            setStatus("发现设备");

            LockManager.getInstance().stopLockScan(myOnLockSingleScanListener);

            mLockDevice = lockDevice;

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
            setStatus("没有找到设备");
        }
    }

    private class MyOnEventListener extends OnLockEventListener {

        @Override
        public void onDeviceConnecting() {
            setStatus("网关连接中...");
        }

        @Override
        public void onDeviceConnected() {
            setStatus("网关连接成功");
        }

        @Override
        public void onDeviceDisconnected() {
            setStatus("网关连接断开");
        }

        @Override
        public void onDeviceConnectFail(ConnLockException e) {
            setStatus("网关连接失败");
        }

        @Override
        public void onReadDataSuccess(int bleResult) {
            switch (bleResult) {
                case LockReadCmd.END_GATEWAY_COMMUNICATION:
                    if (mLockDevice == null) {
                        return;
                    }
                    setStatus("网关通信结束");
                    getPresenter().processGatewayData(mLockDevice.getLockConfig().getPlateMap());
                    break;
                case LockReadCmd.START_GATEWAY_COMMUNICATION:
                    setStatus("开始网关通信");
                    break;
                case LockReadCmd.GATEWAY_IN_COMMUNICATION:
                    setStatus("网关通信中");
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

    private void setStatus(final String msg) {
        if (tv_status == null) {
            return;
        }

        tv_status.post(new Runnable() {
            @Override
            public void run() {
                tv_status.setText(msg);
            }
        });
    }

    @Override
    public void onParkExtraNotify(final int park, final String brandId) {
        tv_park_space_name.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (park == Event.PLATE_IN_TYPE) {
                    if ("44f17829693a4822bfe16b4bef0ce397".equals(brandId)) {
                        // ofo
                        showAnimation(R.drawable.icon_add, mBrandAdapter.tv_ofo_park_count);
                    } else if ("55c0ad15b25b459aa6e04ad3b57f5ee0".equals(brandId)) {
                        // mobike
                        showAnimation(R.drawable.icon_add, mBrandAdapter.tv_mobike_park_count);
                    } else {
                        showAnimation(R.drawable.icon_add, mBrandAdapter.tv_xiaoming_park_count);
                    }
                } else {
                    if ("44f17829693a4822bfe16b4bef0ce397".equals(brandId)) {
                        // ofo
                        showAnimation(R.drawable.icon_subtract, mBrandAdapter.tv_ofo_park_count);
                    } else if ("55c0ad15b25b459aa6e04ad3b57f5ee0".equals(brandId)) {
                        // mobike
                        showAnimation(R.drawable.icon_subtract, mBrandAdapter.tv_mobike_park_count);
                    } else {
                        showAnimation(R.drawable.icon_subtract, mBrandAdapter.tv_xiaoming_park_count);
                    }
                }
            }
        }, 10);
    }

    private PathMeasure mPathMeasure;

    private void showAnimation(int res, View view) {

        int dp25 = ScreenUtil.dpToPx(25);

        final ImageView iv_park = new ImageView(this);
        iv_park.setImageResource(res);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp25, dp25);
        activity_main.addView(iv_park, params);

        int[] parentLocation = new int[2];
        activity_main.getLocationInWindow(parentLocation);

        int startLoc[] = new int[2];
        Point point = mBaiduMap.getProjection().toScreenLocation(new LatLng(mLocationLat, mLocationLng));
        startLoc[0] = point.x;
        startLoc[1] = point.y * 2 - dp25;

        int endLoc[] = new int[2];
        view.getLocationInWindow(endLoc);

        // 起始点：
        float startX = startLoc[0] - parentLocation[0] + dp25 / 2;
        float startY = startLoc[1] - parentLocation[1] + dp25 / 2;

        // 终点坐标
        float toX = endLoc[0] - parentLocation[0] - view.getWidth() / 2;
        float toY = endLoc[1] - parentLocation[1] - view.getHeight() / 4;

        // 开始绘制贝塞尔曲线
        Path path = new Path();
        // 移动到起始点（贝塞尔曲线的起点）
        path.moveTo(startX, startY);
        path.cubicTo((startX + toX) / 2, startY, (point.x + (dp25)), (point.y), toX, toY);
        // mPathMeasure用来计算贝塞尔曲线的曲线长度和贝塞尔曲线中间插值的坐标，如果是true，path会形成一个闭环
        mPathMeasure = new PathMeasure(path, false);

        // 属性动画实现（从0到贝塞尔曲线的长度之间进行插值计算，获取中间过程的距离值）
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
        valueAnimator.setDuration(1000);

        // 匀速线性插值器
        valueAnimator.setInterpolator(new LinearInterpolator());

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                mPathMeasure.getPosTan(value, mCurrentPosition, null);
                iv_park.setTranslationX(mCurrentPosition[0]);
                iv_park.setTranslationY(mCurrentPosition[1]);
            }
        });

        // 开始执行动画
        valueAnimator.start();

        // 动画结束后的处理
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                activity_main.removeView(iv_park);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    @Override
    public void onBrandListLoad(List<BrandEntity> brandList) {

        setStatus("获取品牌成功");

        mBrandAdapter.addList(brandList);
    }

    @Override
    public void onParkSpaceBrandListLoad(List<BrandEntity> brandList) {
        if (mCurrentParkSpace != null) {
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

        setStatus("获取停车位成功");

        mParkSpaceByMacAddressMap.clear();
        for (ParkSpaceEntity entity : parkSpaceList) {
            if(TextUtils.isEmpty(entity.getMacAddress())) {
                continue;
            }
            mParkSpaceByMacAddressMap.put(entity.getMacAddress(), entity);
        }

        showParkSpace(parkSpaceList);
        setStatus("获取品牌");
        getPresenter().getBrandList();
        startScanForMacAddress();
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
            b.putSerializable("park_space", parkSpace);
            LatLng mLatLng = new LatLng(lat, lng);
            MarkerOptions marker;

            if (i == 0) {
                marker = new MarkerOptions().position(mLatLng).icon(parkIconBig)
                        .zIndex(9).draggable(true).extraInfo(b);
            } else {
                marker = new MarkerOptions().position(mLatLng).icon(parkIconSmall)
                        .zIndex(9).draggable(true).extraInfo(b);
            }
            marker.animateType(MarkerOptions.MarkerAnimateType.grow);

            if (i == 0) {
                mLocationLat = lat;
                mLocationLng = lng;
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(new LatLng(lat, lng));
                builder.zoom(23f);
                myMap.getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                mBluetoothAddress = parkSpace.getMacAddress();
                mCurrentParkSpace = parkSpace;
                showTitle(parkSpace);
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
        tv_park_space_name.setText(entity.getName());
        tv_park_space_address.setText(entity.getLocation());
        tv_park_space_total_count.setText(entity.getAmount() + "/" + entity.getCapacity());

        int freeParkSpace = entity.getCapacity() - entity.getAmount();

        if (freeParkSpace > 0) {
            ll_park_space_total.setBackgroundResource(R.drawable.bg_kongxian);
            tv_park_space_tip.setTextColor(getResources().getColor(R.color.kongxian));

            tv_park_space_tip.setText(freeParkSpace + "个空闲车位");
        } else {
            ll_park_space_total.setBackgroundResource(R.drawable.bg_baoman);
            tv_park_space_tip.setTextColor(getResources().getColor(R.color.baoman));

            tv_park_space_tip.setText("超载" + Math.abs(freeParkSpace) + "辆");
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
        if (mParkSpaceByMacAddressMap != null) {
            mParkSpaceByMacAddressMap.clear();
        }
        clearList(mParkPointOverlayList);
        super.onDestroy();
    }
}
