package com.imall.react_native_baidumap.view;

import android.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.imall.react_native_baidumap.R;

/**
 * 主要用于定位
 */
public class ReactMapView {
    private final static String TAG = "ReactMapView";
    private MapView mMapView;
    private LocationClient mLocationClient;
    private boolean isFirstLoc = true; // 是否首次定位
    public ReactMapView(MapView mapView) {
        this.mMapView = mapView;
    }
    public MapView getMapView(){
        return mMapView;
    }
    public BaiduMap getBaiduMap() {
        return this.mMapView.getMap();
    }
    public void setShowsUserLocation(final boolean showsUserLocation) {
        if (getBaiduMap() == null) {
            Log.e(TAG,"getBaiduMap() == null");
            return;
        }
        if (showsUserLocation != getBaiduMap().isMyLocationEnabled()) {
            getBaiduMap().setMyLocationEnabled(showsUserLocation);
            if (mLocationClient == null && showsUserLocation) {
                mLocationClient = new LocationClient(mMapView.getContext());
                BaiduLocationListener listener = new BaiduLocationListener(mLocationClient, new BaiduLocationListener.ReactLocationCallback() {
                    @Override
                    public void onSuccess(BDLocation bdLocation) {
                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(bdLocation.getRadius())
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                                .direction(100).latitude(bdLocation.getLatitude())
                                .longitude(bdLocation.getLongitude()).build();
                        Log.e("loaction:",bdLocation.getLatitude()+":"+bdLocation.getLongitude());
                        if (isFirstLoc && showsUserLocation) {
                            showLoactionInMap(bdLocation);
                        }
                        if (getBaiduMap().isMyLocationEnabled()) {
                            getBaiduMap().setMyLocationData(locData);
                        }
                        mLocationClient.stop();//将这去掉就是一直定位
                        mLocationClient = null;
                    }
                    @Override
                    public void onFailure(BDLocation bdLocation) {
                        Log.e("RNBaidumap", "error: " + bdLocation.getLocType());
                    }
                });
                mLocationClient.setLocOption(getLocationOption());
                mLocationClient.registerLocationListener(listener);
                mLocationClient.start();
            }
        }
    }
    private void showLoactionInMap(BDLocation bdLocation){
        LatLng ll = new LatLng(bdLocation.getLatitude(),
                bdLocation.getLongitude());
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(18.0f);
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.icon_geo);
        getBaiduMap().setMyLocationConfigeration(
                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,mCurrentMarker));
        getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }
//marker获取用户的经纬度
    public void setOnLocationLisenter(OnLocationCallback callback){
        mCallback = callback;
        final LocationClient client = new LocationClient(mMapView.getContext().getApplicationContext(), getLocationOption());;
        BaiduLocationListener.ReactLocationCallback mLocationCallback = new BaiduLocationListener.ReactLocationCallback() {
            @Override
            public void onSuccess(BDLocation bdLocation) {
                Log.e(TAG,"onSuccess BDLocation bdLocation");
                if(mCallback!=null){
                    mCallback.onSuccess(bdLocation);
                }
                if(client!=null){
                    client.stop();
                }
            }
            @Override
            public void onFailure(BDLocation bdLocation) {
                Log.e(TAG,"unable to locate, locType = " + bdLocation.getLocType());
                if(client!=null){
                    client.stop();
                }
            }
        };
        new BaiduLocationListener(client, mLocationCallback);
        client.setLocOption(getLocationOption());
        if (!client.isStarted()) {
            client.start();
        }
    }
    private OnLocationCallback mCallback;
    public interface OnLocationCallback {
        void onSuccess(BDLocation bdLocation);
    }

    private LocationClientOption getLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setScanSpan(10000);//设置的扫描间隔，单位是毫秒
        option.setCoorType("bd09ll");//返回百度经纬度坐标系
        return option;
    }
    public LocationClient getLocationClient(){
        return mLocationClient;
    }
}
