package com.imall.react_native_baidumap.bean;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.Serializable;

/**
 * Created by imall on 16/9/27.
 */
public class AllMessage implements Serializable {
    private String address;
    private String storyName;
    private Double blatitude;
    private Double blongitude;
    private Double glatitude;
    private Double glongitude;
    private String object;
    private Boolean btnIsVisible;
    private String btnTextColor;
    private String btnText;
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStoryName() {
        return storyName;
    }

    public void setStoryName(String storyName) {
        this.storyName = storyName;
    }

    public Double getBlatitude() {
        return blatitude;
    }

    public void setBlatitude(Double blatitude) {
        this.blatitude = blatitude;
    }

    public Double getBlongitude() {
        return blongitude;
    }

    public void setBlongitude(Double blongitude) {
        this.blongitude = blongitude;
    }

    public Double getGlatitude() {
        return glatitude;
    }

    public void setGlatitude(Double glatitude) {
        this.glatitude = glatitude;
    }

    public Double getGlongitude() {
        return glongitude;
    }

    public void setGlongitude(Double glongitude) {
        this.glongitude = glongitude;
    }

    public Boolean getBtnIsVisible() {
        return btnIsVisible;
    }

    public void setBtnIsVisible(Boolean btnIsVisible) {
        this.btnIsVisible = btnIsVisible;
    }

    public String getBtnTextColor() {
        return btnTextColor;
    }

    public void setBtnTextColor(String btnTextColor) {
        this.btnTextColor = btnTextColor;
    }

    public String getBtnText() {
        return btnText;
    }

    public void setBtnText(String btnText) {
        this.btnText = btnText;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "AllMessage{" +
                "address='" + address + '\'' +
                ", storyName='" + storyName + '\'' +
                ", blatitude=" + blatitude +
                ", blongitude=" + blongitude +
                ", glatitude=" + glatitude +
                ", glongitude=" + glongitude +
                ", object='" + object + '\'' +
                '}';
    }
}
