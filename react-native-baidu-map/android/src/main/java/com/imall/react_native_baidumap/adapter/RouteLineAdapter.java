package com.imall.react_native_baidumap.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.imall.react_native_baidumap.R;

import java.util.List;

/**
 * Created by imall on 16/9/8.
 */
public class RouteLineAdapter extends BaseAdapter {
    private List<? extends  RouteLine> routeLines;
    private LayoutInflater layoutInflater;
    private Type mtype;
    public static final String TAG = "RouteLineAdapter";
    public RouteLineAdapter(Context context, List<?extends RouteLine> routeLines, Type type) {
        this.routeLines = routeLines;
        layoutInflater = LayoutInflater.from( context);
        mtype = type;
    }

    @Override
    public int getCount() {
        return routeLines.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NodeViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.activity_transit_item, null);
            holder = new NodeViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.transitName);
            holder.lightNum = (TextView) convertView.findViewById(R.id.lightNum);
            holder.dis = (TextView) convertView.findViewById(R.id.dis);
            convertView.setTag(holder);
        } else {
            holder = (NodeViewHolder) convertView.getTag();
        }

        switch (mtype) {
            case  TRANSIT_ROUTE:
                holder.name.setText("路线" + (position + 1));
                int time = routeLines.get(position).getDuration();
                if ( time / 3600 == 0 ) {
                    holder.lightNum.setText( "大约需要：" + time / 60 + "分钟" );
                } else {
                    holder.lightNum.setText( "大约需要：" + time / 3600 + "小时" + (time % 3600) / 60 + "分钟" );
                }
                holder.dis.setText("距离大约是：" + routeLines.get(position).getDistance());
                break;

            case DRIVING_ROUTE:
                DrivingRouteLine drivingRouteLine = (DrivingRouteLine) routeLines.get(position);
                drivingRouteLine.describeContents();
                Log.e(TAG,drivingRouteLine.describeContents()+"---------describeContents()");
                Log.e(TAG,drivingRouteLine.toString()+"---------toString()");
                Log.e(TAG,drivingRouteLine.getTitle()+"---------getTitle()");
                Log.e(TAG,drivingRouteLine.getWayPoints()+"---------getWayPoints()");
                Log.e(TAG,drivingRouteLine.getStarting()+"---------getStarting()");
                Log.e(TAG,drivingRouteLine.getTerminal()+"---------getTerminal()");
                Log.e(TAG,drivingRouteLine.getDuration()+"---------getDuration()");
                holder.name.setText( "线路" + (position + 1));
                holder.lightNum.setText( "红绿灯数：" + drivingRouteLine.getLightNum());
                holder.dis.setText("拥堵距离为：" + drivingRouteLine.getCongestionDistance() + "米");
                break;
            default:
        }
        return convertView;
    }

    private class NodeViewHolder {
        private TextView name;
        private TextView lightNum;
        private TextView dis;
    }

    public enum Type {
        TRANSIT_ROUTE, // 公交
        DRIVING_ROUTE // 驾车
    }
}
