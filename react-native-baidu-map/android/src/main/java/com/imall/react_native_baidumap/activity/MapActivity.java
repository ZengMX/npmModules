package com.imall.react_native_baidumap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.imall.react_native_baidumap.R;
import com.imall.react_native_baidumap.Utils.BaiduAndGaoDeUtils;
import com.imall.react_native_baidumap.bean.AllMessage;
import com.imall.react_native_baidumap.overlayutil.OverlayManager;
import com.imall.react_native_baidumap.view.ReactMapView;
import com.imall.react_native_baidumap.view.RoutePlanManager;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewPropertyAnimator;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends Activity implements View.OnClickListener{
    public static final String TAG = "MapMoudle";
    public static final String MAPVIEW = "RCTBaiduMap";
    private ReactMapView mMapView;
    private InfoWindow popWindow;
    public static final int MAP_TYPE_NORMAL = 1;//普通图
    public static final int MAP_TYPE_SATELLITE = 2;//卫星图
    private List<BitmapDescriptor> bitmapList = new ArrayList<BitmapDescriptor>();
    private int mZoomLevel = 17;
    private RoutePlanManager routePlanManager;
    protected double gMenDianLat = 0.0 ; // 经度 高得导航用的坐标
    protected double gMenDianLon = 0.0 ; // 纬度
    protected double menDianLat = 0.0 ; // 经度  百度坐标
    protected double menDianLon = 0.0 ; // 纬度
    protected double currentLat = 0.0 ; // 经度  当前坐标
    protected double currentLon = 0.0 ; // 纬度
    private LinearLayout ll_map_back;
    private LinearLayout ll_xiashang;
    private LinearLayout ll_route_allmsg;
    private ImageView iv_xiangxiajiantoux;
    private LinearLayout ll_map_visible;
    private LinearLayout ll_maplook_visible;
    private TextView tv_home_routeName;
    private TextView tv_storeNames;
    private TextView tv_storyAddress;
    private TextView tv_home_routeDetail;
    private ListView ll_show_rootView;
    private LinearLayout ll_details;
    private View rootView;
    private BitmapDescriptor bdA;
    private MapView mapView;
    private String mStoryName;
    private  int MapType =0;
    private String orgId;
    private boolean isVisible =false;
    private String textColor = null;
    private String text = null;
    private boolean isFirstMarkerClick = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this.getApplicationContext());
        setContentView(R.layout.activity_map);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();
    }
    private void init(){
        mapView = (MapView)findViewById(R.id.map);
        mapView.showZoomControls(false);
        LinearLayout ll_amp = (LinearLayout)findViewById(R.id.ll_amp);
        LinearLayout ll_nar = (LinearLayout)findViewById(R.id.ll_nar);
        LinearLayout ll_lookroute = (LinearLayout)findViewById(R.id.ll_lookroute);
        LinearLayout ll_usemap = (LinearLayout)findViewById(R.id.ll_usemap);
        ll_map_back = (LinearLayout)findViewById(R.id.ll_map_back);
        ll_xiashang = (LinearLayout)findViewById(R.id.ll_xiashang);
        ll_route_allmsg = (LinearLayout)findViewById(R.id.ll_route_allmsg);
        iv_xiangxiajiantoux = (ImageView)findViewById(R.id.iv_xiangxiajiantoux);
        ll_map_visible = (LinearLayout)findViewById(R.id.ll_map_visible);
        ll_maplook_visible = (LinearLayout)findViewById(R.id.ll_maplook_visible);
        tv_home_routeName = (TextView)findViewById(R.id.tv_home_routeName);
        tv_home_routeDetail = (TextView)findViewById(R.id.tv_home_routeDetail);

        tv_storeNames = (TextView)findViewById(R.id.tv_storeNames);
        tv_storyAddress = (TextView)findViewById(R.id.tv_storyAddress);

        ll_details = (LinearLayout)findViewById(R.id.ll_details);
        ll_show_rootView = (ListView)findViewById(R.id.lv_home_route_details);
        ll_details.setVisibility(View.INVISIBLE);
        ll_amp.setOnClickListener(this);
        ll_nar.setOnClickListener(this);
        ll_lookroute.setOnClickListener(this);
        ll_usemap.setOnClickListener(this);
        ll_xiashang.setOnClickListener(this);
        ll_map_back.setOnClickListener(this);
        routePlanManager = new RoutePlanManager(
                rootView,
                tv_home_routeName,
                tv_home_routeDetail,
                ll_details,
                ll_show_rootView,
                mapView,
                getApplicationContext());//路线规划
        mMapView = new ReactMapView(mapView);//定位
        mZoomLevel = (int)mMapView.getBaiduMap().getMapStatus().zoom;
        getLocation();
        initData();
    }
    private ArrayList<AllMessage> allMessages;
    private void initData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle !=null) {
            String showMpaType = bundle.getString("showMapType");
            if("1".equals(showMpaType)){
                MapType = 1;
                menDianLat = bundle.getDouble("blatitude");//百度坐标
                menDianLon = bundle.getDouble("blongitude");

                gMenDianLat = bundle.getDouble("glatitude");// 高得导航用的坐标
                gMenDianLon = bundle.getDouble("glongitude");

                String storyName = bundle.getString("storyName");
                String address = bundle.getString("address");
                orgId = bundle.getString("orgId");
                text = bundle.getString("text");
                textColor = bundle.getString("textColor");
                isVisible = bundle.getBoolean("isVisible");
                mStoryName = storyName;
                tv_storeNames.setText(storyName);
                tv_storyAddress.setText(address);
                LatLng llA = new LatLng(menDianLat, menDianLon);
                bdA = BitmapDescriptorFactory
                        .fromResource(R.mipmap.dibiaox);
                MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA).draggable(true);
                mapView.getMap().addOverlay(ooA);
                MapStatus mapStatus = new MapStatus.Builder()
                        .target(new LatLng(menDianLat, menDianLon))
                        .build();
                mapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
                initListener(storyName,address);
            }else{
                setGone();
                MapType = 2;
                allMessages = (ArrayList<AllMessage>)bundle.getSerializable("allmessage");
                if(allMessages !=null && allMessages.size()>0){
                      List<OverlayOptions> overlayOptonsList = new ArrayList<OverlayOptions>();
                      for(int i=0;i<allMessages.size();i++){
                          AllMessage message = allMessages.get(i);
                          BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.dibiaox);
                          bitmapList.add(bitmap);
                          LatLng point = new LatLng(message.getBlatitude(), message.getBlongitude());
                          OverlayOptions options =  new MarkerOptions().position(point).icon(bitmap).title(i+"").draggable(false);
                          overlayOptonsList.add(options);
                      }
                    initOverlayManager(overlayOptonsList);
                }
            }
        }
    }

    private void initOverlayManager(final List<OverlayOptions> overlayOptonsList){
    if(overlayOptonsList !=null && overlayOptonsList.size()>0){
        final OverlayManager manager = new OverlayManager(mapView.getMap()) {
            @Override
            public List<OverlayOptions> getOverlayOptions() {
                return overlayOptonsList;
            }
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!isFirstMarkerClick){
                    setVisible();
                    isFirstMarkerClick = true;
                }
                initPopContent(marker);
                return false;
            }
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                return false;
            }
        };
        mapView.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(bitmapList !=null){
                    for(int i=0;i<bitmapList.size();i++){
                        BitmapDescriptor addBitmapDescriptor =  bitmapList.get(i);
                        if(addBitmapDescriptor == marker.getIcon()){//手动添加的marker和地图的marker是不同的，用已经记录的addBitmapDescriptor做区别
                            manager.onMarkerClick(marker);
                            marker.isVisible();
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        manager.addToMap();
        manager.zoomToSpan();
      }
  }

    private void initPopContent(final Marker marker){//多个门店的
        String stri = marker.getTitle();
        final int index = Integer.parseInt(stri);
        View layout = View.inflate(mapView.getContext(), R.layout.pop_layout, null);
        TextView tv_names = (TextView) layout.findViewById(R.id.tv_title_names);
        TextView tv_address = (TextView) layout.findViewById(R.id.tv_title_address);
        //弹出pop内容
        tv_names.setText(allMessages.get(index).getStoryName());
        tv_address.setText(allMessages.get(index).getAddress());
        //底部导航内容
        tv_storeNames.setText(allMessages.get(index).getStoryName());
        tv_storyAddress.setText(allMessages.get(index).getAddress());
        mStoryName = allMessages.get(index).getStoryName();
        //百度导航坐标
        menDianLat = allMessages.get(index).getBlatitude();
        menDianLon = allMessages.get(index).getBlongitude();
        //高得导航坐标
        gMenDianLat = allMessages.get(index).getGlatitude();
        gMenDianLon = allMessages.get(index).getGlongitude();

        Button bt_store = (Button)layout.findViewById(R.id.bt_store);
        bt_store.setTextSize(9);
        if(allMessages.get(index).getBtnText() != null){
            bt_store.setText(allMessages.get(index).getBtnText());
        }else{
            bt_store.setText("门店详情");
        }
        if(allMessages.get(index).getBtnTextColor() != null){
            bt_store.setTextColor(android.graphics.Color.parseColor(allMessages.get(index).getBtnTextColor()));
        }
        if(allMessages.get(index).getBtnIsVisible() !=null && allMessages.get(index).getBtnIsVisible() == true){
            bt_store.setVisibility(View.VISIBLE);
        }else{
            bt_store.setVisibility(View.GONE);
        }
        bt_store.setOnClickListener(new View.OnClickListener() {//门店详情
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(allMessages.get(index).getObject())){
                    EventBus.getDefault().postSticky(allMessages.get(index).getObject());
                    Log.e(TAG,"门店详情Object参数！"+allMessages.get(index).getObject());
                    MapActivity.this.finish();
                }else{
                    Log.e(TAG,"门店详情需要Object参数！");
                    return;
                }
            }
        });
        LatLng ll = marker.getPosition();
        popWindow = new InfoWindow(layout, ll,-150);
        mapView.getMap().showInfoWindow(popWindow);
        //地图点击隐藏marker
        mapView.getMap().setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(mapView.getMap()!=null){
                    mapView.getMap().hideInfoWindow();
                }
            }
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }
    private void initListener(final String storyName,final String address){//一个门店的
        mapView.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getIcon() == bdA) {
                    View layout = View.inflate(mapView.getContext(), R.layout.pop_layout_one_store, null);
                    TextView tv_names = (TextView) layout.findViewById(R.id.tv_title_names_one);
                    TextView tv_address = (TextView) layout.findViewById(R.id.tv_title_address_one);
                    Button bt_store_one = (Button) layout.findViewById(R.id.bt_store_one);
                    bt_store_one.setTextSize(9);
                    if(text != null && !TextUtils.isEmpty(text)){
                        bt_store_one.setText(text);
                    }else{
                        bt_store_one.setText("门店详情");
                    }
                    if(textColor!=null && TextUtils.isEmpty(textColor)){
                        bt_store_one.setTextColor(android.graphics.Color.parseColor(textColor));
                    }
                    if(isVisible != false){
                        bt_store_one.setVisibility(View.VISIBLE);
                    }else{
                        bt_store_one.setVisibility(View.GONE);
                    }
                    tv_names.setText(storyName);
                    tv_address.setText(address);
                    LatLng ll = marker.getPosition();
                    popWindow = new InfoWindow(layout, ll,-150);
                    mapView.getMap().showInfoWindow(popWindow);
                    bt_store_one.setOnClickListener(new View.OnClickListener() {//门店详情
                        @Override
                        public void onClick(View v) {
                            if(orgId!=null){
                                EventBus.getDefault().postSticky(orgId);
                                Log.e(TAG,"门店详情Object参数！"+orgId);
                                MapActivity.this.finish();
                            }else{
                                Log.e(TAG,"门店详情需要Object参数！");
                                return;
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });
        mapView.getMap().setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapView.getMap().hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }
    private void getLocation(){
        mMapView.setOnLocationLisenter(new ReactMapView.OnLocationCallback() {
            @Override
            public void onSuccess(BDLocation bdLocation) {
                Log.e("定位----------:",bdLocation.getLatitude()+":"+bdLocation.getLongitude());
                currentLat = bdLocation.getLatitude();
                currentLon = bdLocation.getLongitude();
            }
        });
    }
    private boolean isTopToat1 = false;
    private boolean isTopToat2 = false;
    private boolean isXia = false;//记录收起放下
    private boolean isAnimate = false;//动画是否在执行中
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ll_amp){                                                       //放大 小
            mZoomLevel++;
            if(mZoomLevel <=19){
                mMapView.getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomTo(mZoomLevel));
            }else{
                if(mZoomLevel ==20 && !isTopToat1){
                    isTopToat1 = true;
                    isTopToat2 = false;
                    Toast.makeText(v.getContext(),"已经放大到最大级别!",Toast.LENGTH_SHORT).show();
                }else{
                    mZoomLevel --;
                }
            }
        }
        if(v.getId() == R.id.ll_nar){                                                        //缩小 大
            mZoomLevel--;
            if(mZoomLevel >= 3){
                mMapView.getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomTo(mZoomLevel));
            }else{
                if(mZoomLevel ==2 && !isTopToat2){
                    isTopToat2 = true;
                    isTopToat1 = false;
                    Toast.makeText(v.getContext(),"已经缩小到最小级别!",Toast.LENGTH_SHORT).show();
                }else{
                    mZoomLevel ++;
                }
            }
        }

        if(v.getId() == R.id.ll_lookroute){                                                         //查看路线
            mMapView.setOnLocationLisenter(new ReactMapView.OnLocationCallback() {
                @Override
                public void onSuccess(BDLocation bdLocation) {
                    if(MapType == 1){
                        LatLng fpoint = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                        LatLng tpoint = new LatLng(menDianLat,menDianLon);
                        routePlanManager.Search(fpoint,tpoint,mStoryName);
                    }else{
                        LatLng fpoint = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                        LatLng tpoint = new LatLng(menDianLat,menDianLon);
                        routePlanManager.Search(fpoint,tpoint,mStoryName);
                    }
                    setGone();
                }
            });
        }

        if(v.getId() == R.id.ll_usemap){                                                        //使用本机地图
            goToNav(v);
        }

        if(v.getId() == R.id.ll_xiashang){//收起放下
            int h1 =  ll_details.getMeasuredHeight();
            int h2 = ll_xiashang.getMeasuredHeight();
            int h3 = ll_route_allmsg.getHeight();
            int height = h1-h2-h3;
            if(!isXia){//放下
                if(isAnimate){return;}
                iv_xiangxiajiantoux.setBackgroundResource(R.mipmap.xiangshangjiantoux);
                ViewPropertyAnimator.animate(ll_details).translationYBy(height)
                        .setDuration(400)
                        .setStartDelay(300)
                        .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isAnimate = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isXia = true;
                        isAnimate = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            }else{//上升
                if(isAnimate){return;}
                iv_xiangxiajiantoux.setBackgroundResource(R.mipmap.xiangxiajiantoux);
                ViewPropertyAnimator.animate(ll_details).translationYBy(-height)
                        .setDuration(500)
                        .setStartDelay(400)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                isAnimate = true;
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                isXia = false;
                                isAnimate = false;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).start();
            }
        }
        if(v.getId() == R.id.ll_map_back){//返回
            EventBus.getDefault().postSticky(10);
            this.finish();
        }
    }

    private void goToNav(final View v){
        if(currentLat ==0.0 || currentLon == 0.0){
            mMapView.setOnLocationLisenter(new ReactMapView.OnLocationCallback() {
                @Override
                public void onSuccess(BDLocation bdLocation) {
                    currentLat = bdLocation.getLatitude();
                    currentLon = bdLocation.getLongitude();
                }
            });
        }
        nav(v.getContext());
    }
    private void nav(Context context){
        Log.e(TAG,currentLat+" "+currentLon+" "+menDianLat+" "+menDianLon);
        if(BaiduAndGaoDeUtils.isInstallByread("com.baidu.BaiduMap")){
            BaiduAndGaoDeUtils.openBaiduMap(currentLat,currentLon,menDianLat,menDianLon,mStoryName,context);
        }else{
            if(BaiduAndGaoDeUtils.isInstallByread("com.autonavi.minimap")){
                BaiduAndGaoDeUtils.openGaoDeMap(gMenDianLat,gMenDianLon,mStoryName,context);                              //服务器经纬度
            }else{
                BaiduAndGaoDeUtils.onpenBaiduMapInWeb(context);
            }
        }
    }
    private void setGone(){
        ll_map_visible.setVisibility(View.INVISIBLE);
        ll_maplook_visible.setVisibility(View.INVISIBLE);
    }
    private void setVisible(){
        ll_map_visible.setVisibility(View.VISIBLE);
        ll_maplook_visible.setVisibility(View.VISIBLE);
    }

    //接收activity消息的EventBus
    @Subscribe
    public void helloEventBus(Object msg){
        if(msg instanceof Integer){
            int num = (Integer) msg;
            if(num == 11){//导航
                nav(mMapView.getMapView().getContext());
            }
            if(num == 111){//第二个activity返回
                setVisible();
                ll_details.setVisibility(View.INVISIBLE);
            }
            if(num == 4){//没有搜到路线
                setVisible();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMapView.getMapView()!=null){
            LocationClient locationClient = mMapView.getLocationClient();
            if(locationClient!=null && locationClient.isStarted()){
                locationClient.stop();
                locationClient=null;
            }
            mMapView.getBaiduMap().setMyLocationEnabled(false);
            mMapView.getMapView().onDestroy();
        }
        for (int i=0; i<bitmapList.size(); i++){
            BitmapDescriptor bitmap = bitmapList.get(i);
            if(bitmap != null){
                bitmap.recycle();
            }
        }
        if(routePlanManager != null){
            routePlanManager.destroy();
        }
        EventBus.getDefault().unregister(this);
      }
    }

