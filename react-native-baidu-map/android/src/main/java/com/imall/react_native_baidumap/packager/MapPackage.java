package com.imall.react_native_baidumap.packager;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.imall.react_native_baidumap.module.MapMoudle;
import com.imall.react_native_baidumap.module.ReactMapLocationModule;
import com.imall.react_native_baidumap.view.TempView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by imall on 16/8/5.
 */
public class MapPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<NativeModule>();
        modules.add(new ReactMapLocationModule(reactContext));
        modules.add(new MapMoudle(reactContext));
        return modules;
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        List<ViewManager> views = new ArrayList<ViewManager>();
        views.add(new TempView());
        return views;
    }
}
