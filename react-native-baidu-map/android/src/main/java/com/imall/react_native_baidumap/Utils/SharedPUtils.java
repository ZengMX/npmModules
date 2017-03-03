package com.imall.react_native_baidumap.Utils;

import android.content.Context;
import android.content.SharedPreferences;
/**
 * Created by imall on 16/9/9.
 */
public class SharedPUtils {
    private static final String CONFIG = "myConfig";
    private static final String BAIDUCONFIG = "baiDuConfig";
    private static SharedPUtils instance;
    private SharedPUtils(){}
    public static SharedPUtils getInstance(){
        if(instance == null){
            instance = new SharedPUtils();
        }
        return instance;
    }
    public void saveSearchType(int searchType,Context context){
        if(context !=null){
            SharedPreferences sp = context.getSharedPreferences(SharedPUtils.CONFIG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("searchType",searchType);
            editor.commit();
        }
    }
    public  int getSearchType(Context context){
        if(context !=null){
            SharedPreferences sp = context.getSharedPreferences(SharedPUtils.CONFIG, Context.MODE_PRIVATE);
            return sp.getInt("searchType", -1);
        }
        return -1;
    }

    public void saveIsStartActivity(boolean isStart,Context context){
        if(context !=null){
            SharedPreferences sp = context.getSharedPreferences(SharedPUtils.CONFIG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isStartActivity",isStart);
            editor.commit();
        }
    }
    public  boolean getIsStartActivity(Context context){
        if(context !=null){
            SharedPreferences sp = context.getSharedPreferences(SharedPUtils.CONFIG, Context.MODE_PRIVATE);
            return sp.getBoolean("isStartActivity",false);
        }
        return false;
    }
    public void saveBaiduSdk(int errorCode,Context context){
        if(context !=null){
            SharedPreferences sp = context.getSharedPreferences(SharedPUtils.BAIDUCONFIG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("errorCode",errorCode);
            editor.commit();
        }
    }
    public  int getBaiduSdk(Context context){
        if(context !=null){
            SharedPreferences sp = context.getSharedPreferences(SharedPUtils.BAIDUCONFIG, Context.MODE_PRIVATE);
            return sp.getInt("errorCode", -3);
        }
        return -3;
    }
    public void saveNetState(int netCode,Context context){
        if(context !=null){
            SharedPreferences sp = context.getSharedPreferences(SharedPUtils.BAIDUCONFIG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("netCode",netCode);
            editor.commit();
        }
    }
    public  int getNetState(Context context){
        if(context !=null){
            SharedPreferences sp = context.getSharedPreferences(SharedPUtils.BAIDUCONFIG, Context.MODE_PRIVATE);
            return sp.getInt("netCode", -3);
        }
        return -3;
    }
}
