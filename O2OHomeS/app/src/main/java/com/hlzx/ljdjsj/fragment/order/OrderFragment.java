package com.hlzx.ljdjsj.fragment.order;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.interfaces.ViewPageListener;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.view.OrderListView;

/**
 * Created by alan on 2015/12/9.
 */
public class OrderFragment extends OrderBaseFragment{
    OrderListView mListView;
    int m_Index;//标号
    String ord_status;//订单状态

    //标志位，标志已经初始化完成
    private boolean isPrepared;
    ViewPageListener mListener;


    public OrderFragment() {
    }
    public OrderFragment(int index, String status,ViewPageListener listener) {
        m_Index = index;
        ord_status = status;
        mListener=listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order,container,false);
        mListView = (OrderListView) view.findViewById(R.id.order_ll);
        //因为共用一个Fragment视图，所以当前这个视图已被加载到Activity中，必须先清除后再加入Activity
        ViewGroup parent = (ViewGroup)view.getParent();
        if(parent != null) {
            parent.removeView(view);
        }
        isPrepared=true;
        lazyLoad();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible ) {
            return;
        }
        LogUtil.e("ME", "加载订单状态=" + m_Index);

        //判断网络
        mListView.select(m_Index, mListener);
    }

    /**
     * 刷新
     */
    public void refresh()
    {
        mListView.select(m_Index,mListener);
    }

    @Override
    protected void onVisible() {
        super.onVisible();
    }
    @Override
    protected void onInvisible() {
        super.onInvisible();
    }

}
