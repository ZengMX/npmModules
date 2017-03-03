package com.imall.react_native_baidumap.view;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

/**
 * 定位回调
 */
public class BaiduLocationListener implements BDLocationListener {
    private ReactLocationCallback mCallback;
    public interface ReactLocationCallback {
        void onSuccess(BDLocation bdLocation);
        void onFailure(BDLocation bdLocation);
    }
    public BaiduLocationListener(LocationClient client, ReactLocationCallback callback) {
        this.mCallback = callback;
        if (client != null) {
            client.registerLocationListener(this);
        }
    }
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (bdLocation == null) {
            Log.e("RNBaidumap", "receivedLocation is null!");
            return ;
        }
        int locateType = bdLocation.getLocType();
        if (locateType == BDLocation.TypeGpsLocation   //GPS定位结果
                || locateType == BDLocation.TypeNetWorkLocation  //网络定位结果
                || locateType == BDLocation.TypeOffLineLocation) {  //离线定位结果
            if (this.mCallback != null) {
                this.mCallback.onSuccess(bdLocation);
            }
        } else {
            if (this.mCallback != null) {
                this.mCallback.onFailure(bdLocation);
            }
        }
    }
}
