package com.hlzx.ljdjsj.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.Constants;
import com.hlzx.ljdjsj.common.MyObservable;
import com.hlzx.ljdjsj.fragment.order.OrderFragment;
import com.hlzx.ljdjsj.interfaces.ViewPageListener;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by alan on 2015/12/8.
 */
public class OrderListFragment extends BaseFragment implements View.OnClickListener,
        ViewPager.OnPageChangeListener, ViewPageListener, Observer {

    private final static String statusStr[] = {
            "待发货", "配送中", "已配送", "已完成", "已拒绝"
    };
    ArrayList<OrderFragment> mFragments = new ArrayList<OrderFragment>();
    ViewPager pager;
    FragmentPagerAdapter adapter;

    Dialog dialog = null;

    //是否加载数据
    private boolean isLoad = false;
    //判断是否第一次已加载
    private boolean isFirstLoaded = false;

    boolean[] fragmentsUpdateFlag = { false, false, false, false,false};

    //广播
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ORDER_STATUS_2)) {
                if (pager != null) {
                    pager.setCurrentItem(1);
                }
            } else if (action.equals(Constants.ORDER_STATUS_3)) {
                if (pager != null) {
                    pager.setCurrentItem(2);
                }

            }
        }
    };

    private IntentFilter makeIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ORDER_STATUS_2);
        filter.addAction(Constants.ORDER_STATUS_3);
        return filter;
    }

    ;

    @Override
    public void onResume() {
        super.onResume();
        if (receiver != null)
            getActivity().registerReceiver(receiver, makeIntentFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null)
            getActivity().unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_orderlist, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initFragment();

        adapter = new OrderAdapter(getFragmentManager());
        pager = (ViewPager) getView().findViewById(R.id.mViewPager);
        pager.setOffscreenPageLimit(0);
        pager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator) getView().findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(this);
    }

    private void initFragment() {
        OrderFragment fragment1 = new OrderFragment(1, statusStr[0], this);
        OrderFragment fragment2 = new OrderFragment(2, statusStr[1], this);
        OrderFragment fragment3 = new OrderFragment(3, statusStr[2], this);
        OrderFragment fragment4 = new OrderFragment(4, statusStr[3], this);
        OrderFragment fragment5 = new OrderFragment(5, statusStr[4], this);
        mFragments.add(fragment1);
        mFragments.add(fragment2);
        mFragments.add(fragment3);
        mFragments.add(fragment4);
        mFragments.add(fragment5);
        /*for(int i=0;i<statusStr.length;i++)
        {
            OrderFragment fragment= new OrderFragment(i+1, statusStr[i],this);
            mFragments.add(fragment);
        }*/
    }

    @Override
    public void OnClick(int index) {
        if (pager != null) {
            pager.setCurrentItem(index);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    class OrderAdapter extends FragmentPagerAdapter {
        FragmentManager fm;
        public OrderAdapter(FragmentManager fm) {
            super(fm);
            this.fm=fm;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return statusStr.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return statusStr[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    /**
     * 使用一个内部观察者类，更新用于执行推送过来点击进入待发送解密
     */
    @Override
    public void update(Observable observable, Object data) {
        //showToast("观察者="+((MyObservable)observable).getOrder_status());
        pager.setCurrentItem(0);
        /**
         * 刷新
         */
        mFragments.get(0).refresh();
    }
}
