package com.hlzx.test;

import android.app.Activity;
import android.os.Bundle;

import com.hlzx.wenutil.ui.SlideBakcLayout;


/**
 * Created by alan on 2016/2/15.
 */
public abstract class BaseActivity extends Activity{

    private SlideBakcLayout mSlideBackLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSlideBackLayout = new SlideBakcLayout(this);
        mSlideBackLayout.bind();
    }
}
