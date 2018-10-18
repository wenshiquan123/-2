package com.hlzx.yicheng;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.widget.RadioGroup;

import com.hlzx.yicheng.BaseFragmentActivity;
import com.hlzx.yicheng.R;
import com.hlzx.yicheng.adapter.FragmentTabAdapter;
import com.hlzx.yicheng.fragment.YCDynamicFragment;
import com.hlzx.yicheng.fragment.YCHomeFragment;
import com.hlzx.yicheng.fragment.YCMineFragment;
import com.hlzx.yicheng.fragment.YCProductFragment;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 2016/3/28.
 */
public class YCMainActivity extends BaseFragmentActivity{


    @ViewInject(R.id.radioGroup)
    private RadioGroup mRadioGroup;

    YCHomeFragment mHomeFragment;
    YCProductFragment mProductFragment;
    YCDynamicFragment mDynamicFragment;
    YCMineFragment mMineFragment;


    public List<Fragment> mFragments = new ArrayList();

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void init() {


        mFragments.add(new YCHomeFragment());
        mFragments.add(new YCProductFragment());
        mFragments.add(new YCDynamicFragment());
        mFragments.add(new YCMineFragment());

        new FragmentTabAdapter(this,mFragments,R.id.fragment_content,mRadioGroup);


    }

}
