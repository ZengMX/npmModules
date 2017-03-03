//package com.imall.react_native_baidumap.Utils;
//
//import android.util.Log;
//import android.view.View;
//
//import com.baidu.mapapi.model.LatLng;
//import com.imall.react_native_baidumap.adapter.AddressListAdapter;
//import com.imall.react_native_baidumap.bean.AddressList;
//import com.imall.react_native_baidumap.bean.DragMapQueryMessage;
//import com.imall.react_native_baidumap.interfaces.Constant;
//import com.loopj.android.http.AsyncHttpClient;
//import com.loopj.android.http.AsyncHttpResponseHandler;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.List;
//
//import cz.msebera.android.httpclient.Header;
//
///**
// * Created by imall on 16/10/29.
// */
//public class BaiduMapAddressListUtils {
//    public static ArrayList<AddressList> getBaiduDatas(String key){
//        try {
//            String q = URLEncoder.encode(key, "UTF-8");
//            String r = URLEncoder.encode("全国","UTF-8");
//            AsyncHttpClient client = new AsyncHttpClient();
//            final ArrayList<AddressList> addressLists = new ArrayList<AddressList>();
//            client.get("http://api.map.baidu.com/place/v2/suggestion?query="+q+"&region="+r+"&output=json&ak=zLBuvAs2uNMX23I6TMgf3AjbDviAawas&mcode="+ Constant.MCODE+"", new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                    if(statusCode == 200){
//                        String msg = new String(responseBody);
//                        try {
//                            JSONObject object = new JSONObject(msg);
//                            int status = (int)object.get("status");
//                            if(status == 0){
//                                if(addressLists != null && addressLists.size()>0){
//                                    addressLists.clear();
//                                }
//                                JSONArray result =  (JSONArray)object.get("result");
//                                for (int i=0;i<result.length();i++){
//                                    JSONObject data =  (JSONObject)result.get(i);
//                                    String name = data.getString("name");
//                                    String city = data.getString("city");
//                                    String address = data.getString("district");
//                                    JSONObject loaction = data.getJSONObject("location");
//                                    Long lat = loaction.getLong("lat");
//                                    Long lng = loaction.getLong("lng");
//                                    LatLng ll = new LatLng(lat,lng);
//                                    AddressList addressList  = new AddressList(name,lat+"",lng+"");
//                                    addressLists.add(addressList);
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                }
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
//            });
//            return addressLists;
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
