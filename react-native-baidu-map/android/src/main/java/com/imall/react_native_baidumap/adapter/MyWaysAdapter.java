package com.imall.react_native_baidumap.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.imall.react_native_baidumap.R;
import com.imall.react_native_baidumap.bean.RouteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by imall on 16/9/13.
 */
public class MyWaysAdapter extends BaseAdapter {
    private List<RouteMessage> routes;
    private LayoutInflater layoutInflater;
    private Type mtype;
    private LatLng fromPoint;
    private LatLng toPont;
    private String describle;

    public void setDescrible(String describle) {
        this.describle = describle;
    }

    public MyWaysAdapter(Context context, List<RouteMessage> routes, Type mtype) {
        super();
        this.mtype = mtype;
        layoutInflater = LayoutInflater.from(context);
        this.routes = routes;
    }

    public void setMtype(Type mtype) {
        this.mtype = mtype;
    }

    public void setFromPoint(LatLng fromPoint) {
        this.fromPoint = fromPoint;
    }

    public void setToPont(LatLng toPont) {
        this.toPont = toPont;
    }

    public void setRoutes(List<RouteMessage> routes) {
        this.routes = routes;
    }
    @Override
    public int getCount() {
        return routes.size();
    }

    @Override
    public Object getItem(int position) {
        return routes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.show_adapter_items, null);
            holder = new ViewHolder();
            holder.routeName = (TextView) convertView.findViewById(R.id.tv_routeName);
            holder.routeMsg = (TextView) convertView.findViewById(R.id.tv_routeMsg);
            holder.ll_nav = (LinearLayout)convertView.findViewById(R.id.ll_nav);
            holder.bt_nav = (Button)convertView.findViewById(R.id.bt_nav);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RouteMessage message = routes.get(position);
        holder.routeName.setText(message.getRouteName());
        holder.routeMsg.setText(message.getRouteMsg());
        switch (mtype){
            case OTHER_ROUTE:{
                holder.ll_nav.setVisibility(View.GONE);
                break;
            }
            case DRIVING_ROUTE:{
                holder.ll_nav.setVisibility(View.VISIBLE);
                holder.bt_nav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("CLICK"," NAV onClick");
                        EventBus.getDefault().postSticky(11);
                    }
                });
                break;
            }
        }
        return convertView;
    }
    private class ViewHolder {
        private TextView routeName;
        private TextView routeMsg;
        private LinearLayout ll_nav;
        private Button bt_nav;
    }
    public enum Type {
        OTHER_ROUTE, // 其他
        DRIVING_ROUTE // 驾车
    }
}
