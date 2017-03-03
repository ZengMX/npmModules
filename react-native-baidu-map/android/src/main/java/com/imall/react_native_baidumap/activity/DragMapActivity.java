package com.imall.react_native_baidumap.activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.facebook.react.bridge.WritableMap;
import com.imall.react_native_baidumap.R;
import com.imall.react_native_baidumap.Utils.GeoCoderUtils;
import com.imall.react_native_baidumap.adapter.AddressListAdapter;
import com.imall.react_native_baidumap.adapter.DragMapAdapter;
import com.imall.react_native_baidumap.bean.DragMapQueryMessage;
import com.imall.react_native_baidumap.interfaces.Constant;
import com.imall.react_native_baidumap.view.BaiduLocationListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class DragMapActivity extends AppCompatActivity implements View.OnClickListener,OnGetGeoCoderResultListener {
    public final static String TAG = DragMapActivity.class.getSimpleName();
    private ImageView iv_back;
    private ImageView iv_location;
    private TextView tv_search;
    private EditText et_searchInput;
    private LinearLayout ll_top_items;
    private MapView mapView;
    private LatLng mCurrpositon;
    private GeoCoder mSearch = null;
    private ListView listView;
    private Context mContext;
    private List<PoiInfo> infos;
    private List<DragMapQueryMessage> queryMessages = new ArrayList<DragMapQueryMessage>();
    private InputMethodManager imm;
    private boolean hasTextInput;
    private ListView lv_addresslistview;
    private AddressListAdapter addressListAdapter = null;
    private DragMapAdapter dragMapAdapter = null;
    private boolean isFirstSearch = false;
    private String currCity = "全国";
    private AsyncHttpClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_map);
        init();
        getLocation();
    }
    private void init(){
        mContext = this.getApplicationContext();

        iv_back = (ImageView)findViewById(R.id.iv_back);
        iv_location = (ImageView)findViewById(R.id.iv_location);
        tv_search = (TextView)findViewById(R.id.tv_search);
        et_searchInput = (EditText)findViewById(R.id.et_searchInput);
        mapView = (MapView) findViewById(R.id.dragmap);
        listView = (ListView) findViewById(R.id.lv_drag_listview);
        ll_top_items = (LinearLayout) findViewById(R.id.ll_top_items);
        lv_addresslistview = (ListView) findViewById(R.id.lv_addressList);
        lv_addresslistview.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        if(intent!=null){
            String mapType = intent.getStringExtra("mapTpey");
            if(mapType!=null){
                ll_top_items.setBackgroundColor(getResources().getColor(R.color.listview));
                tv_search.setBackgroundColor(getResources().getColor(R.color.listview));
                tv_search.setTextColor(getResources().getColor(R.color.myRead));
                iv_back.setBackgroundResource(R.mipmap.readfanhui);
                iv_back.getLayoutParams().height = 40;
            }
        }
        iv_back.setOnClickListener(this);
        iv_location.setOnClickListener(this);
        tv_search.setOnClickListener(this);
        mapView.getMap().setMyLocationEnabled(true);
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_searchInput,InputMethodManager.HIDE_NOT_ALWAYS);
        initListener();
    }
    private void initListener(){
        mapView.getMap().setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(mapStatus.target));
            }
        });

        et_searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e(TAG,"afterTextChanged"+s.toString());
                if(s.length()>0){
                       hasTextInput = true;
                       if(!TextUtils.isEmpty(s.toString())){
                           getDataForBaidu(s.toString());
                       }
                       tv_search.setText("取消");
                   }else{
                       hasTextInput = false;
                       tv_search.setText("搜索");
                   }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG,"infos"+infos.size());
                if(infos.size()>0){
                    PoiInfo poiInfo = infos.get(position);
                    LatLng ll = new LatLng(poiInfo.location.latitude,poiInfo.location.longitude);
                    GeoCoderUtils.getInstance().reverseGeoCode(ll);
                    GeoCoderUtils.getInstance().setOnReverseGeoCodeResultListener(new GeoCoderUtils.OnReverseGeoCodeResultListener() {
                        @Override
                        public void onSuccess(WritableMap address) {
                            EventBus.getDefault().postSticky(address);
                            DragMapActivity.this.finish();
                        }
                        @Override
                        public void onFailure(String error) {
                           Log.e(TAG,"GeoCoderUtils.getInstance().reverseGeoCode(ll) Failure");
                        }
                    });
                }
            }
        });

        lv_addresslistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(queryMessages.size()>0){
                    DragMapQueryMessage queryMessage = queryMessages.get(position);
                    if(queryMessage !=null){
                       mSearch.geocode(new GeoCodeOption().city(queryMessage.getCity()).address(queryMessage.getName()));
                 //       showLoactionInMap(queryMessage.getPosition());
                        lv_addresslistview.setVisibility(View.INVISIBLE);
                        et_searchInput.setText("");
                        imm.hideSoftInputFromWindow(et_searchInput.getWindowToken(), 0); //强制隐藏键盘
                    }
                }
            }
        });
    }
    private void getLocation(){
        final LocationClient client = new LocationClient(mContext, defaultOption());;
        BaiduLocationListener.ReactLocationCallback mLocationCallback = new BaiduLocationListener.ReactLocationCallback() {
            @Override
            public void onSuccess(final BDLocation bdLocation) {
                mCurrpositon = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                showLoactionInMap(mCurrpositon);
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
        client.setLocOption(defaultOption());
        if (!client.isStarted()) {
            client.start();
        }
    }
    public LocationClientOption defaultOption() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setScanSpan(4 * 1000);
        return option;
    }
    private void showLoactionInMap(LatLng ll){
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(16.0f);
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.icon_geo);
        mapView.getMap().setMyLocationConfigeration(
                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,mCurrentMarker));
        mapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    @Override
    public void onClick(View v) {
        if(v.getId()== R.id.iv_back){
            this.finish();
            return;
        }
        if(v.getId()== R.id.iv_location){
            if(mCurrpositon != null){
                showLoactionInMap(mCurrpositon);
            }
            return;
        }
        if(v.getId()== R.id.tv_search){
            if(hasTextInput){
                et_searchInput.setText("");
                imm.hideSoftInputFromWindow(et_searchInput.getWindowToken(), 0); //强制隐藏键盘
                if(queryMessages !=null && queryMessages.size() >0){
                    queryMessages.clear();
                }
                lv_addresslistview.setVisibility(View.GONE);
            }
            return;
        }
    }
    //http://api.map.baidu.com/place/v2/suggestion?query=天安门&region=131&output=json&ak={您的密钥}
    private void getDataForBaidu(String query){
        try {
            String q = URLEncoder.encode(query, "UTF-8");
            String r = URLEncoder.encode(currCity,"UTF-8");
            Log.e(TAG,"currCity = " + currCity);
            if(client == null){
                client = new AsyncHttpClient();
            }
            client.get("http://api.map.baidu.com/place/v2/suggestion?query="+q+"&region="+r+"&output=json&ak=zLBuvAs2uNMX23I6TMgf3AjbDviAawas&mcode="+Constant.MCODE+"", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if(statusCode == 200){
                        String msg = new String(responseBody);
                        try {
                            JSONObject object = new JSONObject(msg);
                            int status = (int)object.get("status");
                            if(status == 0){
                                JSONArray results =  (JSONArray)object.get("result");
                                if(results.length() == 0){
                                    Log.e(TAG,"没有结果");
                                    return;
                                }//没有结果不能清空


                                List<DragMapQueryMessage> Messages = new ArrayList<DragMapQueryMessage>();

                                JSONArray result =  (JSONArray)object.get("result");
                                for (int i=0;i<result.length();i++){
                                    JSONObject data =  (JSONObject)result.get(i);
                                    String name = data.getString("name");
                                    String city = data.getString("city");
                                    String address = data.getString("district");
                                    JSONObject loaction = data.getJSONObject("location");
                                    Long lat = loaction.getLong("lat");
                                    Long lng = loaction.getLong("lng");
                                    LatLng ll = new LatLng(lat,lng);
                                    DragMapQueryMessage mapQueryMessage = new DragMapQueryMessage(name,ll,city,address);
                                    Messages.add(mapQueryMessage);
                                }
                                if(Messages!=null && Messages.size()>0){
                                    Log.e(TAG,"有结果了 setAdapter"+Messages.size());
                                    queryMessages = Messages;
                                    if(addressListAdapter ==null){
                                        lv_addresslistview.setVisibility(View.VISIBLE);
                                        addressListAdapter=  new AddressListAdapter(Messages,mContext);
                                        lv_addresslistview.setAdapter(addressListAdapter);
                                    }else{
                                        addressListAdapter.setInfos(Messages);
                                        addressListAdapter.notifyDataSetChanged();
                                        lv_addresslistview.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        showLoactionInMap(geoCodeResult.getLocation());
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        infos =  reverseGeoCodeResult.getPoiList();
        if(infos !=null && infos.size()>0){
            if(!isFirstSearch){
                isFirstSearch = true;
                currCity = reverseGeoCodeResult.getAddressDetail().city;
            }
            if(dragMapAdapter == null){
                dragMapAdapter = new DragMapAdapter(infos,mContext);
                listView.setAdapter(dragMapAdapter);
            }else{
                dragMapAdapter.setInfos(infos);
                dragMapAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mSearch.destroy();
    }
}
