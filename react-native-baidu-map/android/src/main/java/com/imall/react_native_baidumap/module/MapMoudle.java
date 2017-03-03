package com.imall.react_native_baidumap.module;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.imall.react_native_baidumap.Utils.GeoCoderUtils;
import com.imall.react_native_baidumap.Utils.SharedPUtils;
import com.imall.react_native_baidumap.activity.DragMapActivity;
import com.imall.react_native_baidumap.activity.MapActivity;
import com.imall.react_native_baidumap.bean.AllMessage;
import com.imall.react_native_baidumap.interfaces.Constant;
import com.imall.react_native_baidumap.view.BaiduLocationListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by imall on 16/9/23
 */
public class MapMoudle extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private LocationClient mClient;
    private ReactApplicationContext mContent;
    private final static String TAG = "MapMoudle";
    private Callback mCallback;
    private String city;
    private int mapState = 0;
    private  MapView mapView;
    @Override
    public String getName() {
        return "MapMoudle";
    }
    public MapMoudle(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContent =reactContext;
        mContent.addLifecycleEventListener(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @ReactMethod
    public void reverseAddressList(ReadableMap map,final Callback callback,final Callback errors){
        if(map.hasKey("latitude") && map.hasKey("longitude")){
            String latitude = map.getString("latitude");
            String longitude = map.getString("longitude");
            Double blatitude = Double.parseDouble(latitude);
            Double blongitude = Double.parseDouble(longitude);
            LatLng ll = new LatLng(blatitude,blongitude);
            GeoCoderUtils.getInstance().reverseGeoCode(ll);
            GeoCoderUtils.getInstance().setOnReverseGeoCodeResultListener(new GeoCoderUtils.OnReverseGeoCodeResultListener() {
                @Override
                public void onSuccess(WritableMap address) {
                    callback.invoke(address);
                }
                @Override
                public void onFailure(String error) {
                    errors.invoke("unable to get location");
                }
            });
        }
    }
    @ReactMethod
    public void showAddressList(ReadableMap map,final Callback callback){
        String searchKey = "广州";
        String loaction = "全国";
        if(!TextUtils.isEmpty(city)){
            loaction = city;
        }
        if(map.hasKey("inputKey")){
           searchKey = map.getString("inputKey");
        }else{
            return;
        }
        try {
            String q = URLEncoder.encode(searchKey, "UTF-8");
            String r = URLEncoder.encode(loaction,"UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.get("http://api.map.baidu.com/place/v2/suggestion?query="+q+"&region="+r+"&output=json&ak=zLBuvAs2uNMX23I6TMgf3AjbDviAawas&mcode="+Constant.MCODE+"", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if(statusCode == 200){
                        String msg = new String(responseBody);
                        try {
                            JSONObject object = new JSONObject(msg);
                            int status = (int)object.get("status");
                            if(status == 0){
                                JSONArray result =  (JSONArray)object.get("result");
                                WritableArray writableArray = Arguments.createArray();
                                for (int i=0;i<result.length();i++){
                                    JSONObject data =  (JSONObject)result.get(i);
                                    String name = data.getString("name");
                                    String city = data.getString("city");
                                    String address = data.getString("district");
                                    JSONObject loaction = data.getJSONObject("location");
                                    Long lat = loaction.getLong("lat");
                                    Long lng = loaction.getLong("lng");
                                    WritableMap map =Arguments.createMap();
                                    map.putString("address",name);
                                    map.putString("lat",lat+"");
                                    map.putString("lng",lng+"");
                                    writableArray.pushMap(map);
                                }
                                callback.invoke(writableArray);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    @ReactMethod
    public void showDragMap(Callback callback){
        mCallback = callback;
        Intent intent = new Intent(getCurrentActivity().getApplicationContext(),DragMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getCurrentActivity().startActivity(intent);
    }
    @ReactMethod
    public void showDragMapPtr(Callback callback){
        mCallback = callback;
        Intent intent = new Intent(getCurrentActivity().getApplicationContext(),DragMapActivity.class);
        intent.putExtra("mapTpey","colorMap");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getCurrentActivity().startActivity(intent);
    }
    @ReactMethod
    public void showMap(ReadableArray array,ReadableMap map){
        if (array!=null && array.size()>0 && map != null){
            Bundle bundle = new Bundle();
            for (int i=0; i<array.size(); i++){
                ReadableMap maps = array.getMap(i);
                String latitude = maps.getString("latitude");
                String longitude = maps.getString("longitude");
                String storyName = maps.getString("title");
                String address = maps.getString("subtitle");
                bundle.putString("storyName",storyName);
                bundle.putString("address",address);
                if(maps.hasKey("object")){
                    ReadableMap object = maps.getMap("object");
                    String temp = object.toString();
                    int start = temp.indexOf(":");
                    int end = temp.lastIndexOf("}");
                    String orgId = temp.substring(start+1,end);
                    bundle.putString("orgId",orgId);
                }
                if(maps.hasKey("buttonStyle")){
                    ReadableMap buttom = maps.getMap("buttonStyle");
                    if(buttom.hasKey("isVisible")){
                        bundle.putBoolean("isVisible",buttom.getBoolean("isVisible"));
                    }
                    if(buttom.hasKey("textColor")){
                        bundle.putString("textColor",buttom.getString("textColor"));
                    }
                    if(buttom.hasKey("text")){
                        bundle.putString("text",buttom.getString("text"));
                    }
                }
                Double blatitude = Double.parseDouble(latitude);
                Double blongitude = Double.parseDouble(longitude);
                bundle.putDouble("blatitude",blatitude);
                bundle.putDouble("blongitude",blongitude);
            }

            String latitude = map.getString("latitude");
            String longitude = map.getString("longitude");
            Double glatitude = Double.parseDouble(latitude);
            Double glongitude = Double.parseDouble(longitude);
            Log.e(TAG,"glatitude"+glatitude+"glongitude"+glongitude);
            bundle.putDouble("glatitude",glatitude);
            bundle.putDouble("glongitude",glongitude);
            bundle.putString("showMapType","1");
            Intent intent = new Intent(getCurrentActivity().getApplicationContext(),MapActivity.class);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getCurrentActivity().startActivity(intent);
        }
    }


    @ReactMethod
    public void showAnnotationsInMap(ReadableArray baiduList,ReadableArray gaodeList){
          if(baiduList !=null && baiduList.size()>0 && gaodeList !=null && gaodeList.size()>0){
              ArrayList<AllMessage> gList = new ArrayList<AllMessage>();
              for (int i=0; i<baiduList.size(); i++){
                  AllMessage message = new AllMessage();
                  ReadableMap bmap = baiduList.getMap(i);
                  String latitude = bmap.getString("latitude");
                  String longitude = bmap.getString("longitude");
                  String storyName = bmap.getString("title");
                  String address = bmap.getString("subtitle");
                  if(bmap.hasKey("object")){
                      ReadableMap object = bmap.getMap("object");
                      String temp = object.toString();
                      int start = temp.indexOf(":");
                      int end = temp.lastIndexOf("}");
                      String orgId = temp.substring(start+1,end);
                      message.setObject(orgId);
                  }
                  if(bmap.hasKey("buttonStyle")){
                      ReadableMap buttom = bmap.getMap("buttonStyle");
                      if(buttom.hasKey("isVisible")){
                          message.setBtnIsVisible(buttom.getBoolean("isVisible"));
                      }
                      if(buttom.hasKey("textColor")){
                          message.setBtnTextColor(buttom.getString("textColor"));
                      }
                      if(buttom.hasKey("text")){
                          message.setBtnText(buttom.getString("text"));
                      }
                  }
                  Double blatitude = Double.parseDouble(latitude);
                  Double blongitude = Double.parseDouble(longitude);

                  ReadableMap gmap = gaodeList.getMap(i);
                  String sglatitude = gmap.getString("latitude");
                  String sglongitude = gmap.getString("longitude");
                  Double glatitude = Double.parseDouble(sglatitude);
                  Double glongitude = Double.parseDouble(sglongitude);
                  message.setStoryName(storyName);
                  message.setAddress(address);

                  message.setBlatitude(blatitude);
                  message.setBlongitude(blongitude);

                  message.setGlatitude(glatitude);
                  message.setGlongitude(glongitude);
                  gList.add(message);
              }
              Bundle bundle = new Bundle();
              bundle.putSerializable("allmessage",gList);
              bundle.putString("showMapType","2");
              Intent intent = new Intent(getCurrentActivity().getApplicationContext(),MapActivity.class);
              intent.putExtras(bundle);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              getCurrentActivity().startActivity(intent);
          }
    }

    //接收activity消息的EventBus
    @Subscribe
    public void helloEventBus(Object msg){
        if(msg instanceof Integer){
            int num = (Integer) msg;
            if(num == 10){//返回
                back();
            }
        }
        if(msg instanceof WritableMap){
            WritableMap mapMessage = (WritableMap)msg;
            Log.e(TAG,"WritableMap");
            mCallback.invoke(mapMessage);
            return;
        }
        if(msg instanceof String){
            String object = (String) msg;
            showStoreInRn(object);
        }
    }
    public void showStoreInRn(String map){
        Log.e(TAG,"showStoreInRn:"+map);
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("DetailAction",map);
    }
    public void back(){
        WritableMap nativeEvent = Arguments.createMap();
        nativeEvent.putString("message", "onBackClick");
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onBackClick",nativeEvent);
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

    public LocationClientOption defaultOption() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setScanSpan(4 * 1000);
        return option;
    }
    @ReactMethod
    public void getCurrentPosition(final Callback success, final Callback errors) {
        if(SharedPUtils.getInstance().getNetState(mContent.getBaseContext()) == -1){
            errors.invoke("网络未连接!");
            return;
        }
        if(SharedPUtils.getInstance().getBaiduSdk(mContent.getBaseContext()) == -2){
            errors.invoke("key 验证出错!");
            return;
        }
        final LocationClient client = new LocationClient(mContent, defaultOption());;
        BaiduLocationListener.ReactLocationCallback mLocationCallback = new BaiduLocationListener.ReactLocationCallback() {
            @Override
            public void onSuccess(final BDLocation bdLocation) {
                LatLng ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                GeoCoderUtils.getInstance().reverseGeoCode(ll);
                if(client!=null){
                    client.stop();
                }
                GeoCoderUtils.getInstance().setOnReverseGeoCodeResultListener(new GeoCoderUtils.OnReverseGeoCodeResultListener() {
                    @Override
                    public void onSuccess(WritableMap address) {
                        city = address.getString("city");
                        success.invoke(address);
                    }
                    @Override
                    public void onFailure(String error) {
                        errors.invoke("unable to get location");
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

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {

    }
}
