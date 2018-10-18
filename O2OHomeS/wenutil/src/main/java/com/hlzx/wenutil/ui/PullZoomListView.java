package com.hlzx.wenutil.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * Created by alan on 2016/2/24.
 */
public class PullZoomListView  {

    private FrameLayout mHeaderContainer;

    private int mHeaderHeight;

   // private SmoothRestore mSmoothRestore;

    public static Interpolator mInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float input) {
            float f = input - 1.0F;
            return 1.0F + f * (f * (f * (f * f)));
        }
    };

    public PullZoomListView(Context context) {
        this(context,null);
    }

    public PullZoomListView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PullZoomListView(Context context, AttributeSet attrs, int defStyleAttr) {
       // super(context, attrs, defStyleAttr);
        //mSmoothRestore=new SmoothRestore();
    }

   /* @Override
    public boolean allowStart() {
        return isFirstItemVisiable();
    }

    @Override
    public void pull(int value) {
        if (mSmoothRestore != null && !mSmoothRestore.isFinish()) {
            mSmoothRestore.abort();
        }
        ViewGroup.LayoutParams params = mHeaderContainer.getLayoutParams();
        params.height = Math.abs(value) + mHeaderHeight;
        mHeaderContainer.setLayoutParams(params);
    }

    @Override
    public void smoothRestore() {
        mSmoothRestore.start(200L);
    }

    @Override
    public ListView initRootView(Context context, AttributeSet set) {
        ListView listView=new ListView(context,set);
        return listView;
    }

    @Override
    public void initHeader(TypedArray a) {
        mHeaderContainer = new FrameLayout(getContext());
        if (mZoomView != null) {
            mHeaderContainer.addView(mZoomView);
        }
        if (mHeadView != null) {
            mHeaderContainer.addView(mHeadView);
        }
        mRootView.addHeaderView(mHeaderContainer);
    }

    public void setAdapter(BaseAdapter adapter)
    {
        mRootView.setAdapter(adapter);
    }

    public void setHeaderLayoutParams(AbsListView.LayoutParams params) {
        if (mHeaderContainer != null) {
            mHeaderContainer.setLayoutParams(params);
            mHeaderHeight = params.height;
        }
    }

    public void updateHeader() {
        if (mHeaderContainer != null) {
            mRootView.removeHeaderView(mHeaderContainer);
            mHeaderContainer.removeAllViews();
            if (mZoomView != null) {
                mHeaderContainer.addView(mZoomView);
            }
            if (mHeadView != null) {
                mHeaderContainer.addView(mHeadView);
            }
            mHeaderHeight = mHeaderContainer.getHeight();
            mRootView.addHeaderView(mHeaderContainer);
        }
    }

    private boolean isFirstItemVisiable() {
        Adapter adapter = mRootView.getAdapter();
        if (null == adapter || adapter.isEmpty()) {
            return true;
        } else {
            if (mRootView.getFirstVisiblePosition() <= 1) {
                View view = mRootView.getChildAt(0);
                if (view != null) {
                    return view.getTop() >= mRootView.getTop();
                }
            }
        }
        return false;
    }


    class SmoothRestore implements Runnable
    {
        protected long duration;
        protected boolean isFinished;
        protected float scale;
        protected long starttime;

        SmoothRestore() {
        }

        public void abort()
        {
            isFinished=true;
        }

        public void start(long d)
        {
            if(mZoomView!=null)
            {
                starttime = SystemClock.currentThreadTimeMillis();
                duration = d;
                scale = (float) mHeaderContainer.getBottom() / mHeaderHeight;
                isFinished = false;
                post(this);
            }
        }

        public boolean isFinish() {
            return isFinished;
        }

        @Override
        public void run() {
            if(mZoomView!=null)
            {
                float f2;
                ViewGroup.LayoutParams params;
                if(!isFinished&&scale>1.0D)
                {
                    float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) starttime) / (float) duration;
                    f2 = scale - (scale - 1.0F) * PullZoomListView.mInterpolator.getInterpolation(f1);
                    params = mHeaderContainer.getLayoutParams();
                    if (f2 > 1.0F) {
                        params.height = (int) (f2 * mHeaderHeight);
                        mHeaderContainer.setLayoutParams(params);
                        post(this);
                        return;
                    }
                    isFinished = true;
                }
            }
        }
    }*/
}
