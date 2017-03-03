//package com.imall.react_native_baidumap.view;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewTreeObserver;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.ZoomControls;
//import com.baidu.location.BDLocation;
//import com.baidu.location.LocationClient;
//import com.baidu.mapapi.SDKInitializer;
//import com.baidu.mapapi.map.BaiduMap;
//import com.baidu.mapapi.map.BitmapDescriptor;
//import com.baidu.mapapi.map.BitmapDescriptorFactory;
//import com.baidu.mapapi.map.InfoWindow;
//import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
//import com.baidu.mapapi.map.MapPoi;
//import com.baidu.mapapi.map.MapStatus;
//import com.baidu.mapapi.map.MapStatusUpdateFactory;
//import com.baidu.mapapi.map.MapView;
//import com.baidu.mapapi.map.Marker;
//import com.baidu.mapapi.map.MarkerOptions;
//import com.baidu.mapapi.map.Overlay;
//import com.baidu.mapapi.map.OverlayOptions;
//import com.baidu.mapapi.map.Polyline;
//import com.baidu.mapapi.model.LatLng;
//import com.baidu.mapapi.model.inner.GeoPoint;
//import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
//import com.baidu.mapapi.navi.BaiduMapNavigation;
//import com.baidu.mapapi.navi.NaviParaOption;
//import com.baidu.mapapi.search.core.RouteNode;
//import com.baidu.mapapi.utils.OpenClientUtil;
//import com.facebook.react.bridge.Arguments;
//import com.facebook.react.bridge.LifecycleEventListener;
//import com.facebook.react.bridge.ReactApplicationContext;
//import com.facebook.react.bridge.ReadableArray;
//import com.facebook.react.bridge.ReadableMap;
//import com.facebook.react.bridge.WritableMap;
//import com.facebook.react.modules.core.DeviceEventManagerModule;
//import com.facebook.react.uimanager.SimpleViewManager;
//import com.facebook.react.uimanager.ThemedReactContext;
//import com.facebook.react.uimanager.annotations.ReactProp;
//import com.facebook.react.uimanager.events.RCTEventEmitter;
//import com.imall.react_native_baidumap.MyRouteOverlay.MyDrivingRouteOverlay;
//import com.imall.react_native_baidumap.MyRouteOverlay.MyTransitRouteOverlay;
//import com.imall.react_native_baidumap.MyRouteOverlay.MyWalkingRouteOverlay;
//import com.imall.react_native_baidumap.R;
//import com.imall.react_native_baidumap.Utils.BaiduAndGaoDeUtils;
//import com.imall.react_native_baidumap.Utils.DensityUtil;
//import com.imall.react_native_baidumap.Utils.SharedPreferencesUtils;
//import com.imall.react_native_baidumap.adapter.HomeRouteDetailAdapter;
//import com.imall.react_native_baidumap.bean.RouteMessage;
//import com.imall.react_native_baidumap.overlayutil.DrivingRouteOverlay;
//import com.imall.react_native_baidumap.overlayutil.OverlayManager;
//import com.imall.react_native_baidumap.overlayutil.TransitRouteOverlay;
//import com.imall.react_native_baidumap.overlayutil.WalkingRouteOverlay;
//import com.nineoldandroids.view.ViewHelper;
//import com.nineoldandroids.view.ViewPropertyAnimator;
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
///**
// * Created by imall on 16/8/5.
// */
//public class BaiduMapViewManager extends SimpleViewManager<View> implements LifecycleEventListener,View.OnClickListener{
//    public static final String TAG = "BaiduMapViewManager";
//    public static final String MAPVIEW = "RCTBaiduMap";
//    private ReactMapView mMapView;
//    private Context mContext;
//    private InfoWindow popWindow;
//    public static final int MAP_TYPE_NORMAL = 1;//普通图
//    public static final int MAP_TYPE_SATELLITE = 2;//卫星图
//    private List<BitmapDescriptor> bitmapList = new ArrayList<BitmapDescriptor>();
//    private int mZoomLevel = 17;
//    private RoutePlanManager routePlanManager;
//    protected double lat2 = 23.096265 ; // 经度
//    protected double lon2 = 113.329009 ; // 纬度
//    protected LatLng to = new LatLng(lat2, lon2);//广州客村
//    protected double gMenDianLat = 0.0 ; // 经度
//    protected double gMenDianLon = 0.0 ; // 纬度
//    protected double menDianLat = 0.0 ; // 经度
//    protected double menDianLon = 0.0 ; // 纬度
//    protected double currentLat = 0.0 ; // 经度
//    protected double currentLon = 0.0 ; // 纬度
//    private LinearLayout ll_map_back;
//    private LinearLayout ll_xiashang;
//    private LinearLayout ll_route_allmsg;
//    private ImageView iv_xiangxiajiantoux;
//    private LinearLayout ll_map_visible;
//    private LinearLayout ll_maplook_visible;
//
//    private TextView tv_home_routeName;
//    private TextView tv_home_routeDetail;
//    private ListView lv_home_route_details;
//    private LinearLayout ll_details;
//    private ThemedReactContext reactContext;
//    private ReactApplicationContext mEactContext;
//    private View rootView;
//    public BaiduMapViewManager(ReactApplicationContext reactContext){
//        super();
//        this.mEactContext = reactContext;
//
//    }
//    @Override
//    protected View createViewInstance(ThemedReactContext reactContext) {//   mMapView.showZoomControls(false);
//        reactContext.addLifecycleEventListener(this);
//        this.reactContext = reactContext;
//        SDKInitializer.initialize(reactContext.getApplicationContext());
//        View rootView =init(reactContext);
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this);
//        }
//        return rootView;
//    }
//    private View init(final ThemedReactContext reactContext){
//        mContext = reactContext.getApplicationContext();
//        rootView = View.inflate(reactContext,R.layout.maplayout,null);
//        MapView mapView = (MapView)rootView.findViewById(R.id.map);
//        mapView.showZoomControls(false);
//        LinearLayout ll_amp = (LinearLayout)rootView.findViewById(R.id.ll_amp);
//        LinearLayout ll_nar = (LinearLayout)rootView.findViewById(R.id.ll_nar);
//        LinearLayout ll_lookroute = (LinearLayout)rootView.findViewById(R.id.ll_lookroute);
//        LinearLayout ll_usemap = (LinearLayout)rootView.findViewById(R.id.ll_usemap);
//
//        ll_map_back = (LinearLayout)rootView.findViewById(R.id.ll_map_back);
//        ll_xiashang = (LinearLayout)rootView.findViewById(R.id.ll_xiashang);
//        ll_route_allmsg = (LinearLayout)rootView.findViewById(R.id.ll_route_allmsg);
//        iv_xiangxiajiantoux = (ImageView) rootView.findViewById(R.id.iv_xiangxiajiantoux);
//        ll_map_visible = (LinearLayout)rootView.findViewById(R.id.ll_map_visible);
//        ll_maplook_visible = (LinearLayout)rootView.findViewById(R.id.ll_maplook_visible);
//
//
//        tv_home_routeName = (TextView) rootView.findViewById(R.id.tv_home_routeName);
//        tv_home_routeDetail = (TextView) rootView.findViewById(R.id.tv_home_routeDetail);
//
//        ll_details = (LinearLayout)rootView.findViewById(R.id.ll_details);
//        ll_details.getMeasuredHeight();
//        ll_details.setVisibility(View.INVISIBLE);
//        lv_home_route_details = (ListView) rootView.findViewById(R.id.lv_home_route_details);
//
//        ll_amp.setOnClickListener(this);
//        ll_nar.setOnClickListener(this);
//        ll_lookroute.setOnClickListener(this);
//        ll_usemap.setOnClickListener(this);
//        ll_xiashang.setOnClickListener(this);
//        ll_map_back.setOnClickListener(this);
//
//        routePlanManager = new RoutePlanManager(
//                rootView,
//                tv_home_routeName,
//                tv_home_routeDetail,
//                ll_details,
//                lv_home_route_details,
//                mapView,
//                mEactContext);//路线规划
//        mMapView = new ReactMapView(mapView);//定位
//        mZoomLevel = (int)mMapView.getBaiduMap().getMapStatus().zoom;
//        getLocation();
//
//        return rootView;
//    }
//    private void setGone(){
//      //  ll_map_back.setVisibility(View.GONE);
//        ll_map_visible.setVisibility(View.GONE);
//        ll_maplook_visible.setVisibility(View.GONE);
//    }
//    private void getLocation(){
//        mMapView.setOnLocationLisenter(new ReactMapView.OnLocationCallback() {
//            @Override
//            public void onSuccess(BDLocation bdLocation) {
//                Log.e("定位----------:",bdLocation.getLatitude()+":"+bdLocation.getLongitude());
//                currentLat = bdLocation.getLatitude();
//                currentLon = bdLocation.getLongitude();
//            }
//        });
//    }
//    @Override
//    public String getName() {
//        return MAPVIEW;
//    }
//    private boolean isTopToat1 = false;
//    private boolean isTopToat2 = false;
//    @Override
//    public void onClick(View v) {//[3, 19]
//        if(v.getId() == R.id.ll_amp){//放大 小
//            mZoomLevel++;
//            if(mZoomLevel <=19){
//                mMapView.getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomTo(mZoomLevel));
//            }else{
//                if(mZoomLevel ==20 && !isTopToat1){
//                    isTopToat1 = true;
//                    isTopToat2 = false;
//                    Toast.makeText(v.getContext(),"已经放大到最大级别!",Toast.LENGTH_SHORT).show();
//                }else{
//                    mZoomLevel --;
//                }
//            }
//        }
//        if(v.getId() == R.id.ll_nar){//缩小 大
//            mZoomLevel--;
//            if(mZoomLevel >= 3){
//                mMapView.getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomTo(mZoomLevel));
//            }else{
//                if(mZoomLevel ==2 && !isTopToat2){
//                    isTopToat2 = true;
//                    isTopToat1 = false;
//                    Toast.makeText(v.getContext(),"已经缩小到最小级别!",Toast.LENGTH_SHORT).show();
//                }else{
//                    mZoomLevel ++;
//                }
//            }
//        }
//
//        if(v.getId() == R.id.ll_lookroute){//查看路线
//            mMapView.setOnLocationLisenter(new ReactMapView.OnLocationCallback() {
//                @Override
//                public void onSuccess(BDLocation bdLocation) {
//                    Log.e("定位----------:",bdLocation.getLatitude()+":"+bdLocation.getLongitude());
//                    LatLng fpoint = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
//                    LatLng tpoint = new LatLng(lat2,lon2);
//                    startSearch(fpoint,tpoint);
//                    setGone();
//                }
//            });
//        }
//
//        if(v.getId() == R.id.ll_usemap){//使用本机地图
//             goToNav(v);
//        }
//
//        if(v.getId() == R.id.ll_xiashang){//收起放下
//            int h1 =  ll_details.getMeasuredHeight();
//            int h2 = ll_xiashang.getMeasuredHeight();
//            int h3 = ll_route_allmsg.getHeight();
//            int height = h1-h2-h3;
//              if(!isXia){//放下
//                  isXia = true;
//                  iv_xiangxiajiantoux.setBackgroundResource(R.mipmap.xiangshangjiantoux);
//                  ViewPropertyAnimator.animate(ll_details).translationYBy(height-24)
//		          .setDuration(500)
//                  .setStartDelay(400)
//		          .start();
//              }else{//上升
//                  isXia = false;
//                  iv_xiangxiajiantoux.setBackgroundResource(R.mipmap.xiangxiajiantoux);
//                  ViewPropertyAnimator.animate(ll_details).translationYBy(-height+24)
//                          .setDuration(500)
//                          .setStartDelay(400)
//                          .start();
//              }
//        }
//        if(v.getId() == R.id.ll_map_back){//返回
//            WritableMap nativeEvent = Arguments.createMap();
//            nativeEvent.putString("message", "onBackClick");
//            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(rootView.getId(), "topChange",nativeEvent);
//        }
//    }
//    private boolean isXia = false;//记录收起放下
//
//    private void goToNav(final View v){
//        if(currentLat ==0.0 || currentLon == 0.0){
//            mMapView.setOnLocationLisenter(new ReactMapView.OnLocationCallback() {
//                @Override
//                public void onSuccess(BDLocation bdLocation) {
//                    currentLat = bdLocation.getLatitude();
//                    currentLon = bdLocation.getLongitude();
//                }
//            });
//        }
//        nav(v.getContext());
//    }
//    private void nav(Context context){
//        Log.e(TAG,currentLat+" "+currentLon+" "+menDianLat+" "+menDianLon);
//        if(BaiduAndGaoDeUtils.isInstallByread("com.baidu.BaiduMap")){
//            BaiduAndGaoDeUtils.openBaiduMap(currentLat,currentLon,menDianLat,menDianLon,"广州客村",context);
//        }else{
//            if(BaiduAndGaoDeUtils.isInstallByread("com.autonavi.minimap")){
//                BaiduAndGaoDeUtils.openGaoDeMap(gMenDianLat,gMenDianLon,"广州棠下",context);//服务器经纬度
//            }else{
//                BaiduAndGaoDeUtils.onpenBaiduMapInWeb(context);
//            }
//        }
//    }
//    @ReactProp(name="showsUserLocation", defaultBoolean = false)
//    public void showsUserLocation(View view, Boolean show) {
//        Log.e("showsUserLocation","show:"+show);
//        mMapView.setShowsUserLocation(show);
//    }
//    @ReactProp(name="showsCompass", defaultBoolean = false)
//    public void showsCompass(View view, Boolean show) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        mapView.getMap().getUiSettings().setCompassEnabled(show);
//    }
//    @ReactProp(name="zoomLevel", defaultInt = 17)   //[3, 19]
//    public void setZoomLevel(View view, Integer zoomLevel) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        if(zoomLevel>=3 && zoomLevel<=19){
//            mapView.getMap().setMapStatus(MapStatusUpdateFactory.zoomTo(zoomLevel));
//        }else{
//            mapView.getMap().setMapStatus(MapStatusUpdateFactory.zoomTo(17));
//        }
//    }
//    @ReactProp(name="zoomEnabled", defaultBoolean = true)
//    public void setZoomEnabled(View view, Boolean enable) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        mapView.getMap().getUiSettings().setZoomGesturesEnabled(enable);
//    }
//    @ReactProp(name="rotateGesturesEnabled", defaultBoolean = true)
//    public void setRotateGesturesEnabled(View view, Boolean enable) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        mapView.getMap().getUiSettings().setRotateGesturesEnabled(enable);
//    }
//    @ReactProp(name="scrollGesturesEnabled", defaultBoolean = true)
//    public void setScrollGesturesEnabled(View view, Boolean enable) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        mapView.getMap().getUiSettings().setScrollGesturesEnabled(enable);
//    }
//    @ReactProp(name="allGesturesEnabled", defaultBoolean = true)
//    public void setAllGesturesEnabled(View view, Boolean enable) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        mapView.getMap().getUiSettings().setAllGesturesEnabled(enable);
//    }
//    @ReactProp(name="mapType",defaultInt = 1)
//    public void setMapType(View view, Integer number) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        if(BaiduMapViewManager.MAP_TYPE_NORMAL==number){
//            mapView.getMap().setMapType(BaiduMapViewManager.MAP_TYPE_NORMAL);
//        }else if(BaiduMapViewManager.MAP_TYPE_SATELLITE==number){
//            mapView.getMap().setMapType(BaiduMapViewManager.MAP_TYPE_SATELLITE);
//        }else{
//            mapView.getMap().setMapType(BaiduMapViewManager.MAP_TYPE_NORMAL);
//        }
//    }
//    @ReactProp(name = "region")
//    public void setRegion(View view, ReadableMap center) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        if (center != null) {
//            String latitude = center.getString("latitude");
//            String longitude = center.getString("longitude");
//            Double dlatitude = Double.parseDouble(latitude);
//            Double dlongitude = Double.parseDouble(longitude);
//            menDianLat = dlatitude;
//            menDianLon = dlongitude;
//            LatLng llA = new LatLng(dlatitude, dlongitude);
//            BitmapDescriptor bdA = BitmapDescriptorFactory
//                    .fromResource(R.mipmap.dibiaox);
//            MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA).draggable(true);
//            mapView.getMap().addOverlay(ooA);
//
//            MapStatus mapStatus = new MapStatus.Builder()
//                    .target(new LatLng(dlatitude,dlongitude)).zoom(18)
//                    .build();
//            mapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
//        }
//    }
//    @ReactProp(name = "gaoDeLocation")
//    public void setGaoDeLocation(View view, ReadableMap center) {
//        if (center != null) {
//            String latitude = center.getString("latitude");
//            String longitude = center.getString("longitude");
//            Double dlatitude = Double.parseDouble(latitude);
//            Double dlongitude = Double.parseDouble(longitude);
//            gMenDianLat = dlatitude;
//            gMenDianLon = dlongitude;
//        }
//    }
//    @ReactProp(name="annotations")
//    public void setAnnotations(View view, ReadableArray array) {
//        ViewGroup rootView = (ViewGroup)view;
//        MapView mapView = (MapView) rootView.getChildAt(0);
//        String latitude = map.getString("latitude");
//                String longitude = map.getString("longitude");
//                Double dlatitude = Double.parseDouble(latitude);
//                List<OverlayOptions> overlayOptonsList = new ArrayList<OverlayOptions>();
//        if(array != null){
//            for (int i = 0; i < array.size(); i++){
//                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.dibiaox);
//                bitmapList.add(bitmap);
//                ReadableMap map = array.getMap(i);
//                uble dlongitude = Double.parseDouble(longitude);
//                LatLng point = new LatLng(dlatitude, dlongitude);
//                OverlayOptions options =  new MarkerOptions()
//                        .position(point)
//                        .icon(bitmap)
//                        .title("我是门店标题")
//                        .draggable(true);
//                overlayOptonsList.add(options);
//            }
//         initOverlayManager(overlayOptonsList,mapView);
//       }
//    }
//    private void initOverlayManager(final List<OverlayOptions> overlayOptonsList, final MapView mapView){
//        if(overlayOptonsList !=null && overlayOptonsList.size()>0){
//            final OverlayManager manager = new OverlayManager(mapView.getMap()) {
//                @Override
//                public List<OverlayOptions> getOverlayOptions() {
//                    return overlayOptonsList;
//                }
//                @Override
//                public boolean onMarkerClick(Marker marker) {
//                    initPopContent(marker,mapView);
//                    return false;
//                }
//                @Override
//                public boolean onPolylineClick(Polyline polyline) {
//                    return false;
//                }
//            };
//
//            mapView.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
//                @Override
//                public boolean onMarkerClick(Marker marker) {
//                    if(bitmapList !=null){
//                        for(int i=0;i<bitmapList.size();i++){
//                            BitmapDescriptor addBitmapDescriptor =  bitmapList.get(i);
//                            if(addBitmapDescriptor == marker.getIcon()){//手动添加的marker和地图的marker是不同的，用已经记录的addBitmapDescriptor做区别
//                                manager.onMarkerClick(marker);
//                                return true;
//                            }
//                        }
//                    }
//                    return false;
//                }
//            });
//
//            // marker拖动事件监听器
//            mapView.getMap().setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
//                @Override
//                public void onMarkerDrag(Marker marker) {
//                }
//
//                @Override
//                public void onMarkerDragEnd(Marker marker) {
//                    Toast.makeText(mContext,"当前位置：" + marker.getPosition().toString(),Toast.LENGTH_LONG).show();
//                    return;
//                }
//                @Override
//                public void onMarkerDragStart(Marker marker) {
//                }
//            });
//            manager.addToMap();
//            manager.zoomToSpan();
//        }
//    }
//    private void initPopContent(final Marker marker, final MapView mapView){
//
//        OnInfoWindowClickListener listener = new OnInfoWindowClickListener(){
//            @Override
//            public void onInfoWindowClick() {
//                mMapView.setOnLocationLisenter(new ReactMapView.OnLocationCallback() {
//                    @Override
//                    public void onSuccess(BDLocation bdLocation) {
//                        Log.e("定位----------:",bdLocation.getLatitude()+":"+bdLocation.getLongitude());
//                        Double latitude = marker.getPosition().latitude;
//                        Double longitude = marker.getPosition().longitude;
//                        Log.e("marker----------:",latitude+":"+longitude);
//                        LatLng fpoint = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
//                        LatLng tpoint = new LatLng(latitude,longitude);
//                        startSearch(fpoint,tpoint);
//                     //   showDialog(fpoint,tpoint,"");
//                    }
//                });
//                mapView.getMap().hideInfoWindow();
//            }
//        };
////初始化marker pop内容
//        View  layout =  View.inflate(mContext,R.layout.pop_layout,null);
//        ImageView round = (ImageView) layout.findViewById(R.id.round);
//        TextView tvTitle = (TextView) layout.findViewById(R.id.tv_title);
//        ImageView roads = (ImageView) layout.findViewById(R.id.roads);
//
//        LatLng ll = marker.getPosition();
//        popWindow = new InfoWindow(BitmapDescriptorFactory.fromView(layout), ll, 0, listener);
//        mapView.getMap().showInfoWindow(popWindow);
//
//        //地图点击隐藏marker
//        mapView.getMap().setOnMapClickListener(new BaiduMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                if(mapView.getMap()!=null){
//                    mapView.getMap().hideInfoWindow();
//                }
//            }
//            @Override
//            public boolean onMapPoiClick(MapPoi mapPoi) {
//                Log.e(TAG,"获取该兴趣点的名称"+mapPoi.getName());
//                Log.e(TAG,"兴趣点的位置"+mapPoi.getPosition().toString());
//                return false;
//            }
//        });
//    }
//    /**
//     * 发起路线规划入口
//     * @param fll 当前位置
//     * @param tll marker位置
//     * 行走方式 0 步行 1 自驾 2 公交 3 骑行
//     */
//    private void startSearch(LatLng fll,LatLng tll){
//        routePlanManager.Search(fll,tll);
//    }
//    /**
//     * 定位到一点
//     * @param array
//     */
//    private void showLocation(ReadableArray array){
//        if (array!=null&&array.size()>0){
//            mMapView.getBaiduMap().clear();
//            for (int i=0; i<array.size(); i++){
//                ReadableMap map = array.getMap(i);
//                double latitude = map.getDouble("latitude");
//                double longitude = map.getDouble("longitude");
//                LatLng ll = new LatLng(latitude,longitude);
//                MapStatus.Builder builder = new MapStatus.Builder();
//                builder.target(ll).zoom(18.0f);
//                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.dibiaox);
//                MarkerOptions option = new MarkerOptions();
//                option.icon(bitmap); // 设置标识物小图标
//                option.title("我是门店");
//                option.position(ll); // 设置标识物显示的位置
//                mMapView.getBaiduMap().addOverlay(option);
//                mMapView.getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//            }
//        }
//    }
//
//    @Override
//    public void onHostResume() {
//        if(mMapView.getMapView()!=null){
//            mMapView.getMapView().onResume();
//        }
//    }
//
//    @Override
//    public void onHostPause() {
//        if(mMapView.getMapView()!=null){
//            mMapView.getMapView().onPause();
//        }
//    }
//
//    private HomeRouteDetailAdapter adapter = null;
//    //接收activity消息的EventBus
//    List<String> datas = new ArrayList<>();
//    @Subscribe
//    public void helloEventBus(Object msg){
//        Log.e(TAG," helloEventBus msg");
//        if(msg instanceof Integer){
//            int msgs = (int)msg;
//            if(msgs == 1){
//                nav(mMapView.getMapView().getContext());
//            }
//            int i = 0;
//            if(msgs == 2){
//                Log.e(TAG,"helloEventBus == 2");
//                i++;
//                WritableMap nativeEvent = Arguments.createMap();
//                nativeEvent.putString("message", "upState"+i);
//                mEactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onStateChanger",nativeEvent);
//            }
//        }
//    }
//    @Override
//    public void onHostDestroy() {
//        Log.e(TAG,"onHostDestroy()>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        if(mMapView.getMapView()!=null){
//            LocationClient locationClient = mMapView.getLocationClient();
//            if(locationClient!=null && locationClient.isStarted()){
//                locationClient.stop();
//                locationClient=null;
//            }
//            mMapView.getBaiduMap().setMyLocationEnabled(false);
//            mMapView.getMapView().onDestroy();
//        }
//        for (int i=0; i<bitmapList.size(); i++){
//            BitmapDescriptor bitmap = bitmapList.get(i);
//            if(bitmap != null){
//                bitmap.recycle();
//            }
//        }
//        if(routePlanManager != null){
//            routePlanManager.destroy();
//        }
//        EventBus.getDefault().unregister(this);
//    }
//
//}
