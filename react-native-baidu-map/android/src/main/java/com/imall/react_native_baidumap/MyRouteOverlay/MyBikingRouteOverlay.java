package com.imall.react_native_baidumap.MyRouteOverlay;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.imall.react_native_baidumap.R;
import com.imall.react_native_baidumap.overlayutil.BikingRouteOverlay;
import com.imall.react_native_baidumap.overlayutil.TransitRouteOverlay;

/**
 * Created by imall on 16/9/8.
 */
public class MyBikingRouteOverlay extends BikingRouteOverlay {
    public MyBikingRouteOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }
    @Override
    public BitmapDescriptor getStartMarker() {
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);

    }
    @Override
    public BitmapDescriptor getTerminalMarker() {
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
    }
}
