package com.hlzx.ljdjsj.common;

import java.util.Observable;

/**
 * Created by alan on 2016/1/13.
 * 被观察者类
 */
public class MyObservable extends Observable {
    int order_status=0;
    public int getOrder_status() {
        return order_status;
    }

    public void setOrder_status(int order_status) {
        this.order_status = order_status;
        setChanged();
        notifyObservers();
    }
}
