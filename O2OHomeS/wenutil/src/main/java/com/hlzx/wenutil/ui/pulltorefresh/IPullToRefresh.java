
package com.hlzx.wenutil.ui.pulltorefresh;


import android.view.View;
import android.view.animation.Interpolator;


public interface IPullToRefresh<T extends View> {

	public T getRefreshableView();

	public boolean isRefreshing();

	public void onRefreshComplete();

	public void setOnRefreshListener(OnRefreshListener<T> listener);


	public void setRefreshing();

	public void setScrollAnimationInterpolator(Interpolator interpolator);


}