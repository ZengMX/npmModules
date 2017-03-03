package com.imall.react_native_baidumap.module;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.imall.react_native_baidumap.Utils.GeoCoderUtils;
import com.imall.react_native_baidumap.view.BaiduLocationListener;

/**
 * Created by imall on 16/8/12.
 */
public class ReactMapLocationModule extends ReactContextBaseJavaModule {
    private LocationClient mClient;
    private ReactApplicationContext mContent;
    private final static String TAG = "ReactMapLocationModule";
    public ReactMapLocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContent = reactContext;
    }
    @Override
    public String getName() {
        return "locationObserver";
    }
    private BaiduLocationListener.ReactLocationCallback mLocationCallback = new BaiduLocationListener.ReactLocationCallback() {
        @Override
        public void onSuccess(BDLocation bdLocation) {
            Log.e(TAG,"onSuccess");
            getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("locationDidChange", locationToMap(bdLocation));
        }
        @Override
        public void onFailure(BDLocation bdLocation) {
            emitError("unable to locate, locType = " + bdLocation.getLocType());
        }
    };
    private void emitError(String error) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("locationError", error);
    }
    private  WritableMap locationToMap(BDLocation location) {
        if (location == null) {
            return null;
        }
        WritableMap map = Arguments.createMap();
        map.putString("latitude", location.getLatitude()+"");
        map.putString("longitude", location.getLongitude()+"");
        return map;
    }
    private  WritableMap locationToMap(BDLocation location,String address) {
        if (location == null) {
            return null;
        }
        WritableMap map = Arguments.createMap();
        map.putString("latitude", location.getLatitude()+"");
        map.putString("longitude", location.getLongitude()+"");
        map.putString("address", address);
        return map;
    }

    public  LocationClientOption defaultOption() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setScanSpan(4 * 1000);
        return option;
    }

    @ReactMethod
    public void getCurrentPosition(final Callback success,final Callback errors) {
        final LocationClient client = new LocationClient(mContent, defaultOption());;
        BaiduLocationListener.ReactLocationCallback mLocationCallback = new BaiduLocationListener.ReactLocationCallback() {
            @Override
            public void onSuccess(final BDLocation bdLocation) {
                LatLng ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                GeoCoderUtils.getInstance().reverseGeoCode(ll);
                GeoCoderUtils.getInstance().setOnReverseGeoCodeResultListener(new GeoCoderUtils.OnReverseGeoCodeResultListener() {
                    @Override
                    public void onSuccess(WritableMap address) {
                        success.invoke(address);
                        if(client!=null){
                            client.stop();
                        }
                    }
                    @Override
                    public void onFailure(String error) {
                        errors.invoke("unable to location");
                        if(client!=null){
                            client.stop();
                        }
                    }
                });
            }
            @Override
            public void onFailure(BDLocation bdLocation) {
                Log.e(TAG,"unable to locate, locType = " + bdLocation.getLocType());
                errors.invoke("unable to locate"+bdLocation.getLocType());
                if(client!=null){
                    client.stop();
                }
            }
        };
        new BaiduLocationListener(client, mLocationCallback);
        client.setLocOption(defaultOption());
        if (!client.isStarted()) {
            client.start();
        }
    }

    @ReactMethod
    public void startObserving() {
        LocationClientOption option = defaultOption();
        if (mClient == null) {
            mClient = new LocationClient(getReactApplicationContext().getApplicationContext(), option);
            new BaiduLocationListener(mClient, mLocationCallback);
        } else {
            mClient.setLocOption(option);
        }
        if (!mClient.isStarted()) {
            mClient.start();
        }
    }
    @ReactMethod
    public void stopObserving() {
        if (mClient != null) {
            mClient.stop();
        }
    }
}
