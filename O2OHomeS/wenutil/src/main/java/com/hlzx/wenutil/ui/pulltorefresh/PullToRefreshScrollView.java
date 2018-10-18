
package com.hlzx.wenutil.ui.pulltorefresh;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.hlzx.wenutil.R;


public class PullToRefreshScrollView extends PullToRefreshBase<ScrollView> {


	public PullToRefreshScrollView(Context context) {
		this(context,null);
	}

	public PullToRefreshScrollView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public PullToRefreshScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected ScrollView createRefreshableView(Context context, AttributeSet attrs) {
		ScrollView scrollView;
		scrollView = new ScrollView(context, attrs);
		scrollView.setId(R.id.scrollview);
		return scrollView;
	}

	@Override
	protected boolean isReadyForPullStart() {
		return mRefreshableView.getScrollY() == 0;
	}



}
