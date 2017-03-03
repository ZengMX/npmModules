package com.imall.react_native_baidumap.bean;

import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;

/**
 * Created by imall on 16/9/13.
 */
public class RouteMessage implements Serializable {
    private String routeName;
    private String routeMsg;
    private String routeWorkDistance;
    private int position;
    private int type;
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RouteMessage() {
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    public String getRouteWorkDistance() {
        return routeWorkDistance;
    }

    public void setRouteWorkDistance(String routeWorkDistance) {
        this.routeWorkDistance = routeWorkDistance;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getRouteMsg() {
        return routeMsg;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public void setRouteMsg(String routeMsg) {
        this.routeMsg = routeMsg;
    }

    @Override
    public String toString() {
        return "RouteMessage{" +
                "routeName='" + routeName + '\'' +
                ", routeMsg='" + routeMsg + '\'' +
                ", routeWorkDistance='" + routeWorkDistance + '\'' +
                ", position=" + position +
                ", type=" + type +
                '}';
    }
}
