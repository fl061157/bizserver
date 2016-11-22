package com.handwin.bean;

import java.io.Serializable;

/**
 * Created by fangliang on 16/7/7.
 */
public class LiveResponse implements Serializable {

    // 0 Success Other -1 roomid not exists and else
    private int result;

    // 失败原因
    private String failReason;


    private int[] action; //Join or message


    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }


    public int[] getAction() {
        return action;
    }

    public void setAction(int[] action) {
        this.action = action;
    }

    public LiveResponse buildResult(int result) {
        this.result = result;
        return this;
    }

    public LiveResponse buildFailReason(String failReason) {
        this.failReason = failReason;
        return this;
    }

    public LiveResponse buildAction(int[] action) {
        this.action = action;
        return this;
    }


}
