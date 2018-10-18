package com.hlzx.yicheng;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2016/3/28.
 */
public abstract class BaseFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        ViewUtils.inject(this);
    }


    /**
     * load ui
     */
    protected abstract void setContentView();

    /**
     * init ui
     */
    protected  abstract void init();

    /**
     * show toast msg
     * @param msg
     */
    protected void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
