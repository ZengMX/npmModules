package com.imall.react_native_baidumap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.imall.react_native_baidumap.R;
import com.imall.react_native_baidumap.Utils.SharedPUtils;
import com.imall.react_native_baidumap.adapter.MyWaysAdapter;
import com.imall.react_native_baidumap.bean.RouteMessage;
import com.imall.react_native_baidumap.view.RoutePlanManager;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imall on 16/9/13.
 */
public class ShowActivity extends Activity implements View.OnClickListener,RoutePlanManager.OnRouteMessage{
    private static final String TAG = "ShowActivity";
    private ListView listView;
    private Context mContext;
    private List<RouteMessage> routeMessages = new ArrayList<RouteMessage>();
    private RoutePlanManager routePlanManager;
    private MyWaysAdapter adapter;
    private int mPosition;
    private TextView tv_bus;
    private TextView tv_transit;
    private TextView tv_walking;
    private TextView tv_storyName;
    private View v_bus;
    private View v_transit;
    private View v_walking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_activity);
        mContext = this.getApplicationContext();
        RoutePlanManager.getInstance().setOnRouteMessage(this);
        initIndicator();
        Intent intent = getIntent();//通过意图发
        routeMessages = (List<RouteMessage>)intent.getSerializableExtra("routeMessage");
        String name =intent.getExtras().getString("storyName");
        if(name !=null){
            tv_storyName.setText(name);
        }
        if(routeMessages.size() > 0){
            adapter = new MyWaysAdapter(this.getApplicationContext(),routeMessages,MyWaysAdapter.Type.OTHER_ROUTE);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RouteMessage message = routeMessages.get(position);
                    message.setPosition(position);
                    EventBus.getDefault().postSticky(message);
                    ShowActivity.this.finish();
                    SharedPUtils.getInstance().saveIsStartActivity(false,mContext);
                }
            });
        }
    }
    private void initIndicator(){
        listView = (ListView)findViewById(R.id.lv_routes);
        tv_bus = (TextView) findViewById(R.id.tv_bus);
        tv_transit = (TextView) findViewById(R.id.tv_transit);
        tv_walking = (TextView) findViewById(R.id.tv_walking);
        tv_storyName = (TextView) findViewById(R.id.tv_storyName);

        v_bus =  findViewById(R.id.v_bus);
        v_transit =  findViewById(R.id.v_transit);
        v_walking =  findViewById(R.id.v_walking);

        tv_bus.setOnClickListener(this);
        tv_transit.setOnClickListener(this);
        tv_walking.setOnClickListener(this);

        v_bus.setVisibility(View.VISIBLE);
        tv_bus.setTextColor(getResources().getColor(R.color.bule));
        v_transit.setVisibility(View.INVISIBLE);
        v_walking.setVisibility(View.INVISIBLE);
        findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowActivity.this.finish();
                EventBus.getDefault().postSticky(111);
                SharedPUtils.getInstance().saveIsStartActivity(false,mContext);
            }
        });
        findViewById(R.id.ib_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowActivity.this.finish();
                EventBus.getDefault().postSticky(111);
                SharedPUtils.getInstance().saveIsStartActivity(false,mContext);
            }
        });
    }
    @Override
    public void onClick(View v) {
         if(v.getId()==R.id.tv_bus){
             EventBus.getDefault().postSticky(2);
             update(1);
         }else if(v.getId() == R.id.tv_transit){
             EventBus.getDefault().postSticky(1);
             update(2);
         }else if(v.getId() == R.id.tv_walking){
             EventBus.getDefault().postSticky(0);
             update(3);
         }
    }

    @Override
    public void onSuccess(ArrayList<RouteMessage> routeMessages) {
        Log.e(TAG,"onSuccess");
        this.routeMessages = routeMessages;
        for (int i=0;i<routeMessages.size();i++){
            Log.e(TAG,routeMessages.get(i).toString());
        }
        if(routeMessages.size()>0){
            RouteMessage message =  routeMessages.get(0);
            if(message.getType() == RoutePlanManager.DRIVING){
                adapter.setMtype(MyWaysAdapter.Type.DRIVING_ROUTE);
                listView.setDividerHeight(0);
            }else{
                adapter.setMtype(MyWaysAdapter.Type.OTHER_ROUTE);
                listView.setDividerHeight(1);
            }
            adapter.setRoutes(routeMessages);
            adapter.notifyDataSetChanged();
        }
    }

    private void update(int tpey){
        switch (tpey){
            case 1:{
                tv_bus.setTextColor(getResources().getColor(R.color.bule));
                tv_transit.setTextColor(getResources().getColor(R.color.black));
                tv_walking.setTextColor(getResources().getColor(R.color.black));
                v_bus.setVisibility(View.VISIBLE);
                v_transit.setVisibility(View.INVISIBLE);
                v_walking.setVisibility(View.INVISIBLE);
                break;
            }
            case 2:{
                tv_bus.setTextColor(getResources().getColor(R.color.black));
                tv_transit.setTextColor(getResources().getColor(R.color.bule));
                tv_walking.setTextColor(getResources().getColor(R.color.black));
                v_bus.setVisibility(View.INVISIBLE);
                v_transit.setVisibility(View.VISIBLE);
                v_walking.setVisibility(View.INVISIBLE);
                break;
            }
            case 3:{
                tv_bus.setTextColor(getResources().getColor(R.color.black));
                tv_transit.setTextColor(getResources().getColor(R.color.black));
                tv_walking.setTextColor(getResources().getColor(R.color.bule));
                v_bus.setVisibility(View.INVISIBLE);
                v_transit.setVisibility(View.INVISIBLE);
                v_walking.setVisibility(View.VISIBLE);
                break;
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        EventBus.getDefault().postSticky(111);
        SharedPUtils.getInstance().saveIsStartActivity(false,mContext);
        return super.onKeyDown(keyCode, event);
    }
}
