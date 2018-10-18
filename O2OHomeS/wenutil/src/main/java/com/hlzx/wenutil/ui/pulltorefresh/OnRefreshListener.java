package com.hlzx.wenutil.ui.pulltorefresh;

import android.view.View;

/**
 * Created by alan on 2016/3/17.
 */
public interface OnRefreshListener<V extends View> {

    public void onRefresh(final PullToRefreshBase<V> refreshView);

}
