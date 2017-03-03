package com.imall.react_native_baidumap.view;

import android.view.View;

import com.baidu.mapapi.map.MapView;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

/**
 * Created by qinguoshi on 16/11/29.
 */
public class TempView extends SimpleViewManager<View>{
    @Override
    public String getName() {
        return "BaiduViews";
    }

    @Override
    protected View createViewInstance(ThemedReactContext reactContext) {
        return new MapView(reactContext.getBaseContext());
    }
}
