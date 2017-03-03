package com.imall.react_native_baidumap.bean;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by imall on 16/10/28.
 */
public class DragMapQueryMessage {
    private String name;
    private LatLng position;
    private String city;
    private String address;

    public DragMapQueryMessage(String name, LatLng position, String city, String address) {
        this.name = name;
        this.position = position;
        this.city = city;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "DragMapQueryMessage{" +
                "name='" + name + '\'' +
                ", position=" + position +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
