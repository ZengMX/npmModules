package com.imall.react_native_alipay.module;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.alipay.sdk.app.PayTask;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.imall.react_native_alipay.util.PayResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by imall on 16/7/25.
 */
public class PlayModule extends ReactContextBaseJavaModule {

    private static final String TAG = "alipay";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;
    private Activity mActivity;
    boolean isSignOk = false;// 返回消息是否通过签名
    // boolean isExistOk = false;//客户手机是否存在支付宝认证账户
    private String order_no = null;
    private ReactApplicationContext mContext;
    private static final String ACTION = "com.imall.mypay";
    private static final Map<String, String> sResultStatus;
    static {
        sResultStatus = new HashMap<String, String>();
        sResultStatus.put("9000", "支付成功");
        sResultStatus.put("8000", "等待支付结果确认");
        sResultStatus.put("4000", "系统异常");
        sResultStatus.put("4001", "数据格式不正确");
        sResultStatus.put("4003", "该用户绑定的支付宝账户被冻结或不允许支付");
        sResultStatus.put("4004", "该用户已解除绑定");
        sResultStatus.put("4005", "绑定失败或没有绑定");
        sResultStatus.put("4006", "订单支付失败");
        sResultStatus.put("4010", "重新绑定账户");
        sResultStatus.put("6000", "支付服务正在进行升级操作");
        sResultStatus.put("6001", "用户中途取消支付操作");
        sResultStatus.put("7001", "网页支付失败");
    }
    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG,">>>>>>>>>>>>>>>>>>>>>>onReceive");
            if(ACTION.equals(action)){
                String data = intent.getStringExtra("statustext");
                Toast.makeText(context,data,Toast.LENGTH_LONG).show();
            }
        }
    }
    public PlayModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mActivity = getCurrentActivity();
        this.mContext = reactContext;
    }

    @Override
    public String getName() {
        return "PlayModule";
    }
    //调用支付
    @ReactMethod
    public void pay(ReadableMap map)
    {
        String sign = map.getString("sign");
        String orderInfo = map.getString("orderInfo");
        Log.e(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>sign:"+sign);
        Log.e(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>orderInfo:"+orderInfo);
        if (null != sign && null != orderInfo) {
            try {
                // 仅需对sign 做URL编码
                sign = URLEncoder.encode(sign, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // 完整的符合支付宝参数规范的订单信息
            final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                    + getSignType();

            Runnable payRunnable = new Runnable() {
                @Override
                public void run() {
                    // 构造PayTask 对象
                    Activity activity =PlayModule.this.getCurrentActivity();
                    PayTask alipay = new PayTask(activity);
                    // 调用支付接口
                    String result = alipay.pay(payInfo, true);
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put("result",result);
                    map.put("context",mContext);
                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = map;
                    mHandler.sendMessage(msg);
                }
            };
            // 必须异步调用
            Thread payThread = new Thread(payRunnable);
            payThread.start();
        }
        Log.d(TAG, "pay called");
    }

    // 处理支付宝支付后的结果
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    HashMap<String,Object> map = ((HashMap<String,Object>)msg.obj);
                    ReactApplicationContext mHcontext = (ReactApplicationContext)map.get("context");
                    String resultData = (String) map.get("result");
                    PayResult payResult = new PayResult(resultData);
                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();
                    String resultStatus = payResult.getResultStatus();

                    WritableMap resultdata = Arguments.createMap();
                    resultdata.putString("status",resultStatus);
                    resultdata.putString("result",resultInfo);
                    if(sResultStatus.containsKey(resultStatus)){
                        resultdata.putString("statustxt",sResultStatus.get(resultStatus));
                    }else{
                        resultdata.putString("statustxt","其他错误!");
                    }
                    resultdata.putString("order_no",order_no);
                    if (TextUtils.equals(resultStatus, "9000")) {
                        sendResultToRN(resultdata,mHcontext);
                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”
                        // 代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            sendResultToRN(resultdata,mHcontext);
                        } else {
                            sendResultToRN(resultdata,mHcontext);
                    }
                    }
                    break;
                }
            }
        };
    };
    /**
     * get the sign type we use. 获取签名方式
     *
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }

    private void sendResultToRN(WritableMap msg , ReactApplicationContext mHandlerContext){
        if(mHandlerContext != null){
            mHandlerContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("sendPlayResultMessage",msg);
            Log.e(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>send");
        }else{
            Log.e(TAG,"NULL>>>>>>>>>>>>>>>>>>>>>>>");
        }
    }
    /**
     * check whether the device has authentication alipay account.
     * 查询终端设备是否存在支付宝认证账户
     *
     */
    public void check() {
        Runnable checkRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask payTask = new PayTask(mActivity);
                // 调用查询接口，获取查询结果
                //   boolean isExist = payTask.;
                Message msg = new Message();
                msg.what = SDK_CHECK_FLAG;
                //   msg.obj = isExist;
                mHandler.sendMessage(msg);
            }
        };

        Thread checkThread = new Thread(checkRunnable);
        checkThread.start();
        Log.d(TAG, "check called");
    }

}
