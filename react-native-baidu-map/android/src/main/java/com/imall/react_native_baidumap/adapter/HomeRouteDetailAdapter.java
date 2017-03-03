package com.imall.react_native_baidumap.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imall.react_native_baidumap.R;
import com.imall.react_native_baidumap.bean.RouteMessage;
import com.imall.react_native_baidumap.bean.StepMessage;

import java.util.List;

/**
 * Created by imall on 16/9/21.
 */
public class HomeRouteDetailAdapter extends BaseAdapter {
    private List<StepMessage> stepMessage;
    private LayoutInflater layoutInflater;
    private Context context;
    private boolean isDriving;
    public HomeRouteDetailAdapter(Context context,List<StepMessage> stepMessage){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.stepMessage = stepMessage;
    }

    public void setDriving(boolean driving) {
        isDriving = driving;
    }

    public void setStepMessage(List<StepMessage>stepMessage) {
        this.stepMessage = stepMessage;
    }

    @Override
    public int getCount() {
        return stepMessage.size();
    }

    @Override
    public Object getItem(int position) {
        return stepMessage.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.details_adapter_items, null);
            holder = new ViewHolder();
            holder.tv_home_routeStep_details = (TextView) convertView.findViewById(R.id.tv_home_routeStep_details);
            holder.iv_ways = (ImageView) convertView.findViewById(R.id.iv_ways);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_home_routeStep_details.setText(stepMessage.get(position).getStepMessage());
        if(isDriving){
            holder.iv_ways.setBackgroundResource(R.mipmap.jiachex);
        }else{
            if(stepMessage.get(position).isBusStep()){
                holder.iv_ways.setBackgroundResource(R.mipmap.gongjiaox);
            }else{
                holder.iv_ways.setBackgroundResource(R.mipmap.buxingx);
            }
        }
        return convertView;
//        View rootView = View.inflate(context,R.layout.details_adapter_items,null);
//        TextView textView = (TextView) rootView.findViewById(R.id.tv_home_routeStep_details);
//        ImageView imageView = (ImageView) rootView.findViewById(R.id.iv_ways);
//        textView.setText(stepMessage.get(position).getStepMessage());
//        Log.e("HomeRouteDetailAdapter","-------------------stepMessage"+stepMessage.get(position).getStepMessage()+"aa");
//        if(isDriving){
//            imageView.setBackgroundResource(R.mipmap.jiachex);
//        }else{
//            if(stepMessage.get(position).isBusStep()){
//                imageView.setBackgroundResource(R.mipmap.gongjiaox);
//            }else{
//                imageView.setBackgroundResource(R.mipmap.buxingx);
//            }
//        }
//        return rootView;
    }
    private class ViewHolder {
        private TextView tv_home_routeStep_details;
        private ImageView iv_ways;
    }
}
