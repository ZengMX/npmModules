package com.imall.react_native_baidumap.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.utils.OpenClientUtil;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created by imall on 16/9/20.
 */
public class BaiduAndGaoDeUtils {
    public static double[] bd09_To_Gcj02(double bd_lat, double bd_lon) {
             double[] gd_lat_lon = new double[2];
             double pi = 3.1415926535897932384626;
             double x = bd_lon - 0.0065, y = bd_lat - 0.006;
             double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
             double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
             gd_lat_lon[0] = z * Math.cos(theta);
             gd_lat_lon[1] = z * Math.sin(theta);
             Log.e("BaiduAndGaoDeUtils","lat:"+gd_lat_lon[0]+"lon:"+gd_lat_lon[1]);
             return gd_lat_lon;
    }
    public static double[] bdToGaoDe(double bd_lat, double bd_lon) {
        double[] gd_lat_lon = new double[2];
        double PI = 3.14159265358979324 * 3000.0 / 180.0;
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI);
        gd_lat_lon[0] = z * Math.cos(theta);
        gd_lat_lon[1] = z * Math.sin(theta);
        Log.e("BaiduAndGaoDeUtils","lat:"+gd_lat_lon[0]+"lon:"+gd_lat_lon[1]);
        return gd_lat_lon; //113.384808,23.125913
    }

    public static boolean isInstallByread(String packageName) {//"com.baidu.BaiduMap" com.autonavi.minimap
        return new File("/data/data/" + packageName).exists();
    }
    /**
     * @param nowlat 现在的经度
     * @param nowlon 现在的纬度
     * @param tolat  目标的经度
     * @param tolon  目标的纬度
     * @param describle 目标地点描述
     * @param context   //113.384808,23.125913 广州棠下
     */
    public static void openBaiduMap(double nowlat, double nowlon, double tolat, double tolon,String describle,Context context) {
        try {
            StringBuilder loc = new StringBuilder();
            loc.append("intent://map/direction?origin=latlng:");
            loc.append(nowlat);
            loc.append(",");
            loc.append(nowlon);
            loc.append("|name:");
            loc.append("我的位置");
            loc.append("&destination=latlng:");
            loc.append(tolat);
            loc.append(",");
            loc.append(tolon);
            loc.append("|name:");
            loc.append(describle);
            loc.append("&mode=driving&region=广州");
            loc.append("&src=thirdapp.navi.imall.future#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
            Intent intent = Intent.getIntent(loc.toString());
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context,"调用百度地图失败",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
 //移动APP调起Android百度地图方式举例
 //  intent = Intent.getIntent("intent://map/navi?location=34.264642646862,108.95108518068&type=BLK&src=
        // thirdapp.navi.yourCompanyName.yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
 //  startActivity(intent); //启动调用
    }
    public static void openGaoDeMap(double lat,double lon, String describle,Context context) {
        try {
            StringBuilder loc = new StringBuilder();
            loc.append("androidamap://navi?sourceApplication=future");
            loc.append("&poiname=");
            loc.append(describle);
            loc.append("&lat=");
            loc.append(lat);
            loc.append("&lon=");
            loc.append(lon);
            loc.append("&dev=0");
            loc.append("&style=2");
            Intent intent = new Intent("android.intent.action.VIEW",android.net.Uri.parse(loc.toString()));
            intent.setPackage("com.autonavi.minimap");
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context,"调用高得地图失败",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    public static void onpenBaiduMapInWeb(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("您需要安装地图进行导航,确认安装");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OpenClientUtil.getLatestBaiduMapApp(context);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    // 构建导航参数
    /**
     * endName(java.lang.String endName):导航终点名称 endPoint(LatLng
     * endPoint):导航终点， 百度经纬度坐标 startName(java.lang.String startName):导航起点名称
     * startPoint(LatLng startPoint):导航起点， 百度经纬度坐标
     * */
//    NaviParaOption para = new NaviParaOption().startPoint(form).endPoint(to).startName("广东软件园").endName("广州客村");
//    try {
//        /**
//         * openBaiduMapNavi(NaviParaOption para, Context context)
//         * 调起百度地图导航页面
//         * */
//        BaiduMapNavigation.openBaiduMapNavi(para,v.getContext());
//    } catch (BaiduMapAppNotSupportNaviException e) {
//        e.printStackTrace();
//        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
//        builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
//        builder.setTitle("提示");
//        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                OpenClientUtil.getLatestBaiduMapApp(v.getContext());
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.create().show();
//    }
}
