package com.imall.react_native_baidumap.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.core.VehicleInfo;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.imall.react_native_baidumap.MyRouteOverlay.MyBikingRouteOverlay;
import com.imall.react_native_baidumap.MyRouteOverlay.MyDrivingRouteOverlay;
import com.imall.react_native_baidumap.MyRouteOverlay.MyTransitRouteOverlay;
import com.imall.react_native_baidumap.MyRouteOverlay.MyWalkingRouteOverlay;
import com.imall.react_native_baidumap.Utils.SharedPUtils;
import com.imall.react_native_baidumap.activity.ShowActivity;
import com.imall.react_native_baidumap.adapter.HomeRouteDetailAdapter;
import com.imall.react_native_baidumap.bean.RouteMessage;
import com.imall.react_native_baidumap.bean.StepMessage;
import com.imall.react_native_baidumap.overlayutil.BikingRouteOverlay;
import com.imall.react_native_baidumap.overlayutil.DrivingRouteOverlay;
import com.imall.react_native_baidumap.overlayutil.OverlayManager;
import com.imall.react_native_baidumap.overlayutil.TransitRouteOverlay;
import com.imall.react_native_baidumap.overlayutil.WalkingRouteOverlay;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imall on 16/9/7.
 */
public class RoutePlanManager implements OnGetRoutePlanResultListener{
    public static final String TAG = "BaiduMapViewManager";
    public static final int BUS = 2;
    public static final int DRIVING = 1;
    public static final int WALKING = 0;
    private List<TransitRouteLine> mTransitLines;
    private DrivingRouteLine mDrivingLine;
    private WalkingRouteLine mWalkingRouteLine;
    private RoutePlanSearch mRoutePlanSearch;
    private MapView mapView;
    private Context mContext;
    RouteLine route = null;
    BaiduMap mBaidumap = null;
    OverlayManager routeOverlay = null;
    private String storyName;
    private TextView popupText = null; // 泡泡view
    private static RoutePlanManager instance;
    public static RoutePlanManager getInstance(){
        return instance;
    }
    private LatLng fromPoint;
    private LatLng toPoint;

    private TextView tv_home_routeName;
    private TextView tv_home_routeDetail;
    private LinearLayout ll_details;
    private ListView lv_home_route_details;
    private View rootView;
    public RoutePlanManager(View rootView,
                            TextView tv_home_routeName,
                            TextView tv_home_routeDetail,
                            LinearLayout ll_details,
                            ListView lv_home_route_details,
                            MapView mapView,
                            Context context){
        instance = this;
        this.rootView = rootView;
        this.mapView = mapView;
        this.mContext = context;

        this.lv_home_route_details = lv_home_route_details;
        this.tv_home_routeName = tv_home_routeName;
        this.tv_home_routeDetail = tv_home_routeDetail;
        this.ll_details = ll_details;

        mBaidumap = mapView.getMap();
        mRoutePlanSearch = RoutePlanSearch.newInstance();
        mRoutePlanSearch.setOnGetRoutePlanResultListener(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }
    /**
     * 路线规划发起搜索入口
     */
    public void Search(LatLng fromPoint,LatLng toPoint,String storyName){
      //  mSearchType = SharedPreferencesUtils.getInstance().getSearchType(mapView.getContext());
        this.fromPoint =fromPoint;
        this.toPoint = toPoint;
        this.storyName = storyName;
        transitSearch(fromPoint,toPoint);
    }
    //接收activity消息的EventBus
    @Subscribe
    public void helloEventBus(Object msg){
        if(msg instanceof RouteMessage){
            mBaidumap.clear();
            RouteMessage message = (RouteMessage)msg;
            Log.e(TAG,"message-------------------------------------------------------------------------------------"+message.toString());
            if(message.getType() == RoutePlanManager.WALKING) {
                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(mWalkingRouteLine);
                overlay.addToMap();
                overlay.zoomToSpan();

                tv_home_routeName.setText(message.getRouteName());
                tv_home_routeDetail.setText(message.getRouteMsg());
                showHoemRoutDetails(mWalkingRouteLine,false);
            }else if(message.getType() == RoutePlanManager.BUS){
                TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(mTransitLines.get(message.getPosition()));
                overlay.addToMap();
                overlay.zoomToSpan();

                tv_home_routeName.setText(message.getRouteName());
                tv_home_routeDetail.setText(message.getRouteMsg());

                showHoemRoutDetails(mTransitLines.get(message.getPosition()),false);
            }else if(message.getType() == RoutePlanManager.DRIVING){
                DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
                routeOverlay = overlay;
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(mDrivingLine);
                overlay.addToMap();
                overlay.zoomToSpan();

                tv_home_routeName.setText(message.getRouteName());
                tv_home_routeDetail.setText(message.getRouteMsg());
                showHoemRoutDetails(mDrivingLine,true);
            }
        }
//        if(v.getId()== R.id.tv_bus){
//            EventBus.getDefault().postSticky(2+"");
//            update(1);
//        }else if(v.getId() == R.id.tv_transit){
//            EventBus.getDefault().postSticky(1+"");
//            update(2);
//        }else if(v.getId() == R.id.tv_walking){
//            EventBus.getDefault().postSticky(0+"");
//            update(3);
//        }
        //发起搜索
        if(msg instanceof Integer){
            int type = (Integer) msg;
            if(type<=2){
                Log.e(TAG,"发起搜索");
                if(type == RoutePlanManager.BUS){
                    transitSearch(fromPoint,toPoint);
                }else if(type == RoutePlanManager.DRIVING){
                    drivingSearch(fromPoint,toPoint);
                }else if(type == RoutePlanManager.WALKING){
                    walkingSearch(fromPoint,toPoint);
                }
             }
           }
    }
    //在地图上显示详情
    private void showHoemRoutDetails(RouteLine routeLine,boolean isDrving){
         ll_details.setVisibility(View.VISIBLE);
         lv_home_route_details.setVisibility(View.VISIBLE);
         List<StepMessage> stepMessage = new ArrayList<StepMessage>();
         if(routeLine instanceof TransitRouteLine){
             TransitRouteLine line = (TransitRouteLine)routeLine;
             List<TransitRouteLine.TransitStep> steps = line.getAllStep();
             for (int i=0;i<steps.size();i++){
                 TransitRouteLine.TransitStep step = steps.get(i);
                 StepMessage message = new StepMessage();
                 if(step.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.BUSLINE
                         ||step.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.SUBWAY){
                     message.setBusStep(true);
                 }else{
                     message.setBusStep(false);
                 }
                 String Str = step.getInstructions();
                 message.setStepMessage(Str);
                 stepMessage.add(message);
             }
         }else if(routeLine instanceof DrivingRouteLine){
             List<DrivingRouteLine.DrivingStep> steps = mDrivingLine.getAllStep();
             for (int i=0;i<steps.size();i++){
                 StepMessage message = new StepMessage();
                 DrivingRouteLine.DrivingStep step = steps.get(i);
                 String Str = step.getInstructions();
                 message.setBusStep(false);
                 message.setStepMessage(Str);
                 stepMessage.add(message);
             }
         }else if(routeLine instanceof WalkingRouteLine){
             List<WalkingRouteLine.WalkingStep> steps = mWalkingRouteLine.getAllStep();
             for (int i=0;i<steps.size();i++){
                 StepMessage message = new StepMessage();
                 WalkingRouteLine.WalkingStep step = steps.get(i);
                 String Str = step.getInstructions();
                 message.setBusStep(false);
                 message.setStepMessage(Str);
                 stepMessage.add(message);
             }
         }

        Log.e(TAG,"stepMessage size"+stepMessage.size()+"stepMessage-----------------------------------"+stepMessage.toString());
        HomeRouteDetailAdapter adapter = new HomeRouteDetailAdapter(mContext,stepMessage);
        adapter.setDriving(isDrving);
        lv_home_route_details.setAdapter(adapter);
     }
    //0 步行
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        if (walkingRouteResult == null
                || SearchResult.ERRORNO.RESULT_NOT_FOUND == walkingRouteResult.error) {
            Toast.makeText(mapView.getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }
//        else{
//            Toast.makeText(mapView.getContext(), "搜索到"+walkingRouteResult.getRouteLines().size()+"条路线",Toast.LENGTH_SHORT).show();
//        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            mWalkingRouteLine = walkingRouteResult.getRouteLines().get(0);
            RouteMessage message = new RouteMessage();
            String duration;
            String distances;
            StringBuffer routeMsg = new StringBuffer();
            int time = mWalkingRouteLine.getDuration();
            if ( time / 3600 == 0 ) {
                duration = time / 60 + "分钟";
            } else {
                duration =  time / 3600 + "小时" + (time % 3600) / 60 + "分钟";
            }
            float distance = mWalkingRouteLine.getDistance();
            int pointPre = (int)distance / 1000;
            int pointNext = (int)distance % 1000;
            int  b   =  (int)pointNext/100;
            distances = pointPre+"."+b+"公里";

            message.setRouteName("步行路线");
            message.setRouteMsg(duration+" | "+distances);
            message.setType(RoutePlanManager.WALKING);
            Log.e(TAG,"this.listener:"+this.listener);
            if(listener !=null){
                ArrayList<RouteMessage> routeMessages = new ArrayList<RouteMessage>();
                routeMessages.add(message);
                listener.onSuccess(routeMessages);
            }else{
                Log.e(TAG,"listener == null");
            }
        }
    }
//2 公交
    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        if (transitRouteResult == null
                || SearchResult.ERRORNO.RESULT_NOT_FOUND == transitRouteResult.error) {
            Toast.makeText(mapView.getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }
        if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            Log.e(TAG,"起终点或途经点地址有岐义"+transitRouteResult.getSuggestAddrInfo()+"");
            return;
        }
        mTransitLines= transitRouteResult.getRouteLines();
        if(mTransitLines ==null || mTransitLines.size()==0){
            Toast.makeText(mapView.getContext(), "抱歉，未找到结果",Toast.LENGTH_SHORT).show();
            EventBus.getDefault().postSticky(4);
            return;
        }else{
            Toast.makeText(mapView.getContext(), "搜索到"+mTransitLines.size()+"条路线",Toast.LENGTH_SHORT).show();
        }

        ArrayList<RouteMessage> routeMessages = new ArrayList<RouteMessage>();
        if(mTransitLines.size() >0){
            for(int j=0;j<mTransitLines.size();j++){
                Log.e(TAG,"公交线:"+j);
                TransitRouteLine lines = mTransitLines.get(j);
                RouteMessage message = new RouteMessage();
                String duration;
                String distances;
                StringBuffer routeMsg = new StringBuffer();
                int time = lines.getDuration();
                int workDistance = 0;
                if ( time / 3600 == 0 ) {
                    duration = time / 60 + "分钟";
                } else {
                    duration =  time / 3600 + "小时" + (time % 3600) / 60 + "分钟";
                }
                float distance = lines.getDistance();
                int pointPre = (int)distance / 1000;
                int pointNext = (int)distance % 1000;
                int  b   =  (int)pointNext/100;
                distances = pointPre+"."+b+"公里";
                List<TransitRouteLine.TransitStep> step = lines.getAllStep();
                for(int i=0; i<step.size();i++){
                    TransitRouteLine.TransitStep transitStep = step.get(i);
                    if(transitStep.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.BUSLINE){
                        VehicleInfo vehicleInfo =transitStep.getVehicleInfo();
                        if(step.size()>1){
                            routeMsg.append(vehicleInfo.getTitle()+" - ");
                        }else{
                            routeMsg.append(vehicleInfo.getTitle());
                        }
                    }else if(transitStep.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING){
                        workDistance +=transitStep.getDistance();
                    }
                }
                String str = routeMsg.toString();
                String allMessage = str.substring(0,str.lastIndexOf(" -"));
                message.setRouteName(allMessage);
                message.setRouteMsg(duration+" | "+distances+" | 步行"+workDistance+"米");
                message.setType(RoutePlanManager.BUS);
                workDistance = 0;
                routeMessages.add(message);
            }

            if(!SharedPUtils.getInstance().getIsStartActivity(mContext)){
                Intent intent= new Intent(mContext,ShowActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("routeMessage",routeMessages);
                Bundle bundle = new Bundle();
                bundle.putString("storyName",storyName);
                intent.putExtras(bundle);
                SharedPUtils.getInstance().saveIsStartActivity(true,mContext);
                mContext.startActivity(intent);
            }else{
                if(listener!=null){
                    listener.onSuccess(routeMessages);
                }else{
                    Log.e(TAG,"onGetTransitRouteResult listener is null");
                }
            }
        }
    }
//1 自驾
    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        if (drivingRouteResult == null
                || SearchResult.ERRORNO.RESULT_NOT_FOUND == drivingRouteResult.error) {
            Toast.makeText(mapView.getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }
        if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            Log.e(TAG,"起终点或途经点地址有岐义"+drivingRouteResult.getSuggestAddrInfo()+"");
            return;
        }
        if(drivingRouteResult.getRouteLines() ==null || drivingRouteResult.getRouteLines().size()==0){
            Toast.makeText(mapView.getContext(), "抱歉，未找到结果",Toast.LENGTH_SHORT).show();
            return;
        }
//        else{
//            Toast.makeText(mapView.getContext(), "搜索到"+drivingRouteResult.getRouteLines().size()+"条路线",Toast.LENGTH_SHORT).show();
//        }
        if(drivingRouteResult.getRouteLines().size() >0){
            mDrivingLine  =   drivingRouteResult.getRouteLines().get(0);
            RouteMessage message = new RouteMessage();
            String duration;
            String distances;
            StringBuffer routeMsg = new StringBuffer();
            int time = mDrivingLine.getDuration();
            if ( time / 3600 == 0 ) {
                duration = time / 60 + "分钟";
            } else {
                duration =  time / 3600 + "小时" + (time % 3600) / 60 + "分钟";
            }
            float distance = mDrivingLine.getDistance();
            int pointPre = (int)distance / 1000;
            int pointNext = (int)distance % 1000;
            int  b   =  (int)pointNext/100;
            distances = pointPre+"."+b+"公里";

            message.setRouteName("驾车路线");
            message.setRouteMsg(duration+" | "+distances);
            message.setType(RoutePlanManager.DRIVING);
            if(listener !=null){
                ArrayList<RouteMessage> routeMessages = new ArrayList<RouteMessage>();
                routeMessages.add(message);
                listener.onSuccess(routeMessages);
            }else{
                Log.e(TAG,"listener == null");
            }
        }
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        if (bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mapView.getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }
//        else{
//            Toast.makeText(mapView.getContext(), "搜索到"+bikingRouteResult.getRouteLines().size()+"条路线",Toast.LENGTH_SHORT).show();
//        }
        if (bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            route = bikingRouteResult.getRouteLines().get(0);
            BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
            routeOverlay = overlay;
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(bikingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }
    //步行搜索
    private void walkingSearch(LatLng fromPoint, LatLng toPoint) {
        WalkingRoutePlanOption walkOption = new WalkingRoutePlanOption();
        PlanNode from = PlanNode.withLocation(fromPoint);
        PlanNode to = PlanNode.withLocation(toPoint);
        walkOption.from(from);
        walkOption.to(to);
        mRoutePlanSearch.walkingSearch(walkOption);
    }
    //自驾搜索

    /**
     * ECAR_AVOID_JAM
     驾车策略： 躲避拥堵
     ECAR_DIS_FIRST
     驾乘检索策略常量：最短距离
     ECAR_FEE_FIRST
     驾乘检索策略常量：较少费用
     ECAR_TIME_FIRST
     驾乘检索策略常量：时间优先
     */
    private void drivingSearch(LatLng fromPoint, LatLng toPoint) {
        // 创建搜索参数
        DrivingRoutePlanOption drivingRoutePlanOption = new DrivingRoutePlanOption();
        PlanNode from = PlanNode.withLocation(fromPoint);// 设置线程的起始点
        drivingRoutePlanOption.from(from);
        PlanNode to = PlanNode.withLocation(toPoint);
        drivingRoutePlanOption.to(to); //设置线程的终点:客村
        drivingRoutePlanOption.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST); // 设置线程的默认策略，距离最短
        mRoutePlanSearch.drivingSearch(drivingRoutePlanOption);
    }
    /**
     EBUS_NO_SUBWAY
     公交检索策略常量：不含地铁
     EBUS_TIME_FIRST
     公交检索策略常量：时间优先
     EBUS_TRANSFER_FIRST
     公交检索策略常量：最少换乘
     EBUS_WALK_FIRST
     公交检索策略常量：最少步行距离
     */
    //公交搜索
    private void transitSearch(LatLng fromPoint, LatLng toPoint) {
        TransitRoutePlanOption transitOption = new TransitRoutePlanOption();
        PlanNode from = PlanNode.withLocation(fromPoint);
        PlanNode to = PlanNode.withLocation(toPoint);
        transitOption.from(from);
        transitOption.to(to);
        transitOption.city("广州");
        transitOption.policy(TransitRoutePlanOption.TransitPolicy.EBUS_TIME_FIRST); // 设置线程的默认策略，时间优先
        mRoutePlanSearch.transitSearch(transitOption);
    }
    //骑行搜索
    private void bikingSearch(LatLng fromPoint, LatLng toPoint) {
        BikingRoutePlanOption bikingOption = new BikingRoutePlanOption();
        PlanNode from = PlanNode.withLocation(fromPoint);
        PlanNode to = PlanNode.withLocation(toPoint);
        bikingOption.from(from);
        bikingOption.to(to);
        mRoutePlanSearch.bikingSearch(bikingOption);
    }

    public interface OnRouteMessage{
         void onSuccess(ArrayList<RouteMessage> routeMessages);
    }
    private OnRouteMessage listener;
    public  void setOnRouteMessage(OnRouteMessage listener){
        Log.e(TAG,"setOnRouteMessage:"+listener);
        this.listener = listener;
        Log.e(TAG,"this.listener:"+this.listener);
    }

    public void destroy(){
        if(mRoutePlanSearch !=null){
            mRoutePlanSearch.destroy();
        }
        EventBus.getDefault().unregister(this);
    }
}
