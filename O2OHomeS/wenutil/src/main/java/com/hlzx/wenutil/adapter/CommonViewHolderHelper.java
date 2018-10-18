package com.hlzx.wenutil.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by alan on 2016/3/15.
 */
public class CommonViewHolderHelper {

    /**
     * 用来装Viewhold 的view容器
     */
    private SparseArray<View> mViewArray=new SparseArray<View>();
    /**
     * ViewHolder的布局
     */
    private View mConvertView;

    public CommonViewHolderHelper(View view) {
        this.mConvertView = view;
    }

    public CommonViewHolderHelper(Context context, int layoutId) {
        this(context, null, layoutId);
    }

    public CommonViewHolderHelper(Context context, ViewGroup group, int layoutId) {
        this(LayoutInflater.from(context).inflate(layoutId, group, false));
    }

    public View getView() {
        return mConvertView;
    }

    /**
     * 从SparseArray找出子View根据id
     *
     * @param layoutId  返回itemview中子控件时所需的layoutid
     * @return
     */
    public <T extends View> T findViewById(int layoutId) {
        View view = mViewArray.get(layoutId);
        if (view == null) {
            view = mConvertView.findViewById(layoutId);
            if (view != null) {
                mViewArray.put(layoutId, view);
            }
        }
        return view == null ? null : (T) view;
    }

}
