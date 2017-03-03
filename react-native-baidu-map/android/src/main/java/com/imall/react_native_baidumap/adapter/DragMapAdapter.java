package com.imall.react_native_baidumap.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.imall.react_native_baidumap.R;
import com.imall.react_native_baidumap.bean.RouteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by imall on 16/10/27.
 */
public class DragMapAdapter extends BaseAdapter {
    private List<PoiInfo> infos;
    private LayoutInflater layoutInflater;
    public DragMapAdapter(List<PoiInfo> infos, Context context){
        this.infos = infos;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setInfos(List<PoiInfo> infos) {
        this.infos = infos;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.dragmap_adapter_items, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv_drag_name);
            holder.address = (TextView) convertView.findViewById(R.id.tv_drag_address);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PoiInfo message = infos.get(position);
        holder.name.setText(message.name);
        holder.address.setText(message.address);

        return convertView;
    }
    private class ViewHolder {
        private TextView name;
        private TextView address;

    }
}
