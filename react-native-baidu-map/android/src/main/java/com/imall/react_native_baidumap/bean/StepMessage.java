package com.imall.react_native_baidumap.bean;

/**
 * Created by imall on 16/9/22.
 */
public class StepMessage {
    private String stepMessage;
    private boolean isBusStep;

    public String getStepMessage() {
        return stepMessage;
    }

    public void setStepMessage(String stepMessage) {
        this.stepMessage = stepMessage;
    }

    public boolean isBusStep() {
        return isBusStep;
    }

    public void setBusStep(boolean busStep) {
        isBusStep = busStep;
    }

    @Override
    public String toString() {
        return "StepMessage{" +
                "stepMessage='" + stepMessage + '\'' +
                ", isBusStep=" + isBusStep +
                '}';
    }
}
