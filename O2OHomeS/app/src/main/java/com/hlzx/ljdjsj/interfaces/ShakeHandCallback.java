package com.hlzx.ljdjsj.interfaces;

/**
 * Created by alan on 2016/1/15.
 */
public interface ShakeHandCallback {

    //握手成功
    void onSuccessed(String str);
    //握手失败
    void onFalied(String str);

}
