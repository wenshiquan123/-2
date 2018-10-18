package com.hlzx.wenutil.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by alan on 2016/3/15.
 */
public abstract class CommonViewHolder {

    private Context mContext;
    /**
     * 当前项的view
     */
    private View mItemView;
    /**
     * 用来零时存储ViewHolder的子View和初始化它们
     */
    private CommonViewHolderHelper mHolderHelper;

    /**
     * 用来加载ViewHolder的视图View，根据layoutId
     */
    public View viewInflate(Context context, ViewGroup parent, boolean attachToRoot) {
        mContext = context;
        mItemView = LayoutInflater.from(mContext).inflate(getItemLayout(), parent, attachToRoot);
        mItemView.setTag(this);
        mHolderHelper = new CommonViewHolderHelper(mItemView);
        initItemView();
        return mItemView;
    }

    /**
     * 根据layoutI找到ItemView,这个工作只在这里声明，然后在viewInflaite里面调用，具体的实现工作交给子类去完成
     *
     * @return  发挥当前ViewHolder所需的布局或者是视图
     */
    public abstract int getItemLayout();

    /**
     * 初始化item的子View
     */
    public void initItemView() {
    }

    /**
     * 初始化View根据layoutId
     *
     * @param layoutId  需要初始化的View的id
     * @return
     */
    public <T extends View> T findViewById(int layoutId) {
        return mHolderHelper.findViewById(layoutId);
    }
}
