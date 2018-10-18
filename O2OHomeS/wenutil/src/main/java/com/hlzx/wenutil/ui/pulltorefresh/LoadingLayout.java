package com.hlzx.wenutil.ui.pulltorefresh;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hlzx.wenutil.R;
import com.hlzx.wenutil.utils.SharedPreferencesUtils;

/**
 * Created by alan on 2016/3/17.
 */
public abstract class LoadingLayout extends FrameLayout implements ILoadingLayout
{
    private Context mContext;

    static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();

    private FrameLayout mInnerLayout;

    protected ImageView mHeaderImage;
    protected ProgressBar mHeaderProgress;

    private boolean mUseIntrinsicAnimation;

    private TextView mHeaderText;
    private TextView mSubHeaderText;

    private CharSequence mPullLabel;
    private CharSequence mRefreshingLabel;
    private CharSequence mReleaseLabel;

    public LoadingLayout(Context context) {
        this(context, null);
    }

    public LoadingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        init(attrs, defStyleAttr);

    }

    private void init(AttributeSet attrs,int defStyleAttr)
    {
        LayoutInflater.from(mContext).inflate(R.layout.pull_to_refresh_header_vertical, this);

        mInnerLayout=(FrameLayout)findViewById(R.id.fl_inner);
        mHeaderImage=(ImageView)mInnerLayout.findViewById(R.id.pull_to_refresh_image);
        mHeaderProgress=(ProgressBar)mInnerLayout.findViewById(R.id.pull_to_refresh_progress);
        mHeaderText=(TextView)mInnerLayout.findViewById(R.id.pull_to_refresh_text);
        mSubHeaderText=(TextView)mInnerLayout.findViewById(R.id.pull_to_refresh_sub_text);

        TypedArray a=mContext.obtainStyledAttributes(attrs,R.styleable.PullToRefresh,defStyleAttr,0);

        Drawable imageDrawable=null;

        if(a.hasValue(R.styleable.PullToRefresh_pull_drawable))
        {
            imageDrawable=a.getDrawable(R.styleable.PullToRefresh_pull_drawable);
        }

        if(null==imageDrawable)
        {
             imageDrawable=mContext.getResources().getDrawable(getDefaultDrawableResId());
        }
        setLoadingDrawable(imageDrawable);
        a.recycle();

        mPullLabel=mContext.getResources().getString(R.string.pull_to_refresh_pull_label);
        mReleaseLabel=mContext.getResources().getString(R.string.pull_to_refresh_release_label);
        mRefreshingLabel=mContext.getResources().getString(R.string.pull_to_refresh_refreshing_label);

        reset();
    }

    public final void reset()
    {
        if (null != mHeaderText) {
            mHeaderText.setText(mPullLabel);
        }

        mHeaderImage.setVisibility(View.VISIBLE);
        mHeaderProgress.setVisibility(View.GONE);

        if (mUseIntrinsicAnimation) {
            ((AnimationDrawable) mHeaderImage.getDrawable()).stop();
        } else {
            resetImpl();
        }

        if (null != mSubHeaderText) {
            String lastTime=SharedPreferencesUtils.getInstance(mContext).get("last_time");
            mSubHeaderText.setText("更新时间:"+lastTime);
            if (TextUtils.isEmpty(mSubHeaderText.getText())) {
                mSubHeaderText.setVisibility(View.GONE);
            } else {
                mSubHeaderText.setVisibility(View.VISIBLE);
            }
        }
    }

    public final int getContentSize() {
        return mInnerLayout.getHeight();
    }

    public final void onPull(float scaleOfLayout)
    {
       if(!mUseIntrinsicAnimation)
       {
           onPullImpl(scaleOfLayout);
       }
    }

    public final void pullToRefresh()
    {
        if(null!=mHeaderText)
        {
            mHeaderText.setText(mPullLabel);
        }
        pullToRefreshImpl();
    }

    public final void refreshing()
    {
        if (null != mHeaderText) {
            mHeaderText.setText(mRefreshingLabel);
        }

        if (mUseIntrinsicAnimation) {
            ((AnimationDrawable) mHeaderImage.getDrawable()).start();
        } else {
            // Now call the callback
            refreshingImpl();
        }

        if (null != mSubHeaderText) {
            mSubHeaderText.setVisibility(View.GONE);
        }
    }

    public final void releaseToRefresh() {
        if (null != mHeaderText) {
            mHeaderText.setText(mReleaseLabel);
        }

        releaseToRefreshImpl();
    }

    public final void setHeight(int height) {
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams)getLayoutParams();
        lp.height = height;
        requestLayout();
    }

    @Override
    public void setLastUpdateLabel(CharSequence lastUpdateLabel) {
           setSubHeaderText("更新时间："+lastUpdateLabel);
    }

    @Override
    public void setLoadingDrawable(Drawable loadingDrawable) {
         mHeaderImage.setImageDrawable(loadingDrawable);
         mUseIntrinsicAnimation=(loadingDrawable instanceof AnimationDrawable);
        onLoadingDrawableSet(loadingDrawable);

    }

    @Override
    public void setPullLabel(CharSequence pullLabel) {
         mPullLabel=pullLabel;
    }

    @Override
    public void setRefreshingLabel(CharSequence refreshingLabel) {
        mRefreshingLabel=refreshingLabel;
    }

    @Override
    public void setReleaseLabel(CharSequence releaseLabel) {
        mReleaseLabel=releaseLabel;
    }

    @Override
    public void setTextTypeface(Typeface tf) {
        mHeaderText.setTypeface(tf);
    }

    private void setSubHeaderText(CharSequence label)
    {
        if(null!=mSubHeaderText) {
            if (TextUtils.isEmpty(label)) {
                mSubHeaderText.setVisibility(View.GONE);
            }else
            {
                mSubHeaderText.setText(label);
                if(mSubHeaderText.getVisibility()==View.GONE)
                {
                    mSubHeaderText.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    protected abstract int getDefaultDrawableResId();

    protected abstract void onLoadingDrawableSet(Drawable imageDrawable);

    protected abstract void onPullImpl(float scaleOfLayout);

    protected abstract void pullToRefreshImpl();

    protected abstract void refreshingImpl();

    protected abstract void releaseToRefreshImpl();

    protected abstract void resetImpl();
}
