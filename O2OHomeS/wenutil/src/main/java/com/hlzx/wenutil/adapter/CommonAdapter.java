package com.hlzx.wenutil.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 2016/3/15.
 */
public abstract class CommonAdapter<T,H extends CommonViewHolder> extends BaseAdapter{

    private Context mContext;
    /**
     * 用来存储adapter数据的List
     */
    private List<T> mDatas = new ArrayList<T>();

    public CommonAdapter(Context context) {
        this.mContext = context;
    }

    public void setDatas(List<T> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mDatas.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        if (convertView == null) {
            convertView = initViewHolder().viewInflate(mContext, group, false);
        }
        H h = (H) convertView.getTag();
        initItemData(position, h, convertView);
        return convertView;
    }

    /**
     * 返回ViewHolder子视图的抽象方法，在getView中调用，然后在子类中具体实现
     *
     * @return 返回ViewHolder
     */
    protected abstract H initViewHolder();

    /**
     * 设置Item中的数据，抽象的方法，在getView中调调用，然后在子类中初始化
     *
     * @param position
     *            itemview的索引
     * @param viewholder
     *            实现的ViewHolder子类
     * @param root
     *            itemview 的子视图
     */
    protected abstract void initItemData(int position, H viewholder, View root);
}
