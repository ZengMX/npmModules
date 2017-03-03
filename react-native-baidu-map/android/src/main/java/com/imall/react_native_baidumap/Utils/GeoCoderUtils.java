package com.imall.react_native_baidumap.Utils;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

/**
 * Created by imall on 16/9/8.
 */
//地理编码
public class GeoCoderUtils implements OnGetGeoCoderResultListener {
    GeoCoder mSearch = null;
    private static GeoCoderUtils instance;
    private static final String TAG = "GeoCoderUtils";
    public static GeoCoderUtils getInstance(){
        if(instance ==null){
            instance = new GeoCoderUtils();
        }
        return instance;
    }
    private GeoCoderUtils(){
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }
    public void reverseGeoCode(LatLng ll){
        if(ll ==null){
            return;
        }
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
    }
    public void getGeoCode(String city,String address){
        if(TextUtils.isEmpty(city) || TextUtils.isEmpty(address)){
            return;
        }
        mSearch.geocode(new GeoCodeOption().city(city).address(address));
    }
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e(TAG,"未能找到结果地理对应经纬度");
            if(getGeoCodelistener!=null){
                getGeoCodelistener.onFailure("未能找到结果地理对应经纬度");
            }
            return;
        }
        String strInfo = String.format("纬度：%f 经度：%f",
                geoCodeResult.getLocation().latitude, geoCodeResult.getLocation().longitude);
        Log.e(TAG,strInfo);
        getGeoCodelistener.onSuccess(strInfo);
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e(TAG,"未能找到结果经纬度对应地理");
            if(reverseGeolistener!=null){
                reverseGeolistener.onFailure("未能找到结果经纬度对应地理");
            }
            return;
        }
        PoiInfo poiInfo = reverseGeoCodeResult.getPoiList().get(0);
        if(poiInfo !=null){
            WritableMap map = Arguments.createMap();
            map.putString("latitude",poiInfo.location.latitude+"");
            map.putString("longitude",poiInfo.location.longitude+"");
            map.putString("addressDetails",reverseGeoCodeResult.getAddress());
            map.putString("address",poiInfo.address);
            map.putString("province",reverseGeoCodeResult.getAddressDetail().province);
            map.putString("city",reverseGeoCodeResult.getAddressDetail().city);
            map.putString("district",reverseGeoCodeResult.getAddressDetail().district);
            map.putString("street",reverseGeoCodeResult.getAddressDetail().street);
            map.putString("streetNumber",reverseGeoCodeResult.getAddressDetail().streetNumber);
            reverseGeolistener.onSuccess(map);

//            Log.e(TAG,"name"+reverseGeoCodeResult.getPoiList().get(0).name);
//            Log.e(TAG,"address"+reverseGeoCodeResult.getPoiList().get(0).address);
//            Log.e(TAG,"addressDetails"+reverseGeoCodeResult.getAddress());
//            Log.e(TAG,"longitude"+reverseGeoCodeResult.getPoiList().get(0).location.longitude+"");
//            Log.e(TAG,"latitude"+reverseGeoCodeResult.getPoiList().get(0).location.latitude+"");
//            Log.e(TAG,"addressDetails"+reverseGeoCodeResult.getPoiList().get(0).location.latitude+"");
//            Log.e(TAG,"city"+reverseGeoCodeResult.getAddressDetail().city);
//            Log.e(TAG,"province："+reverseGeoCodeResult.getAddressDetail().province);
//            Log.e(TAG,"district："+reverseGeoCodeResult.getAddressDetail().district);
//            Log.e(TAG,"street："+reverseGeoCodeResult.getAddressDetail().street);
//            Log.e(TAG,"streetNumber："+reverseGeoCodeResult.getAddressDetail().streetNumber);
        }
    }
   //地理--------->经纬度
    private OnReverseGeoCodeResultListener reverseGeolistener = null;
    public void setOnReverseGeoCodeResultListener(OnReverseGeoCodeResultListener listener){
        this.reverseGeolistener = listener;
    }
    public interface OnReverseGeoCodeResultListener{
        void onSuccess(WritableMap address);
        void onFailure(String error);
    }

    //经纬度--------->地理
    private OnGetGeoCodeResultListener getGeoCodelistener = null;
    public void setOnGetGeoCodeResultListener(OnGetGeoCodeResultListener listener){
        this.getGeoCodelistener = listener;
    }
    public interface OnGetGeoCodeResultListener{
        void onSuccess(String address);
        void onFailure(String error);
    }
}
