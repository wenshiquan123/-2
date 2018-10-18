package com.hlzx.wenutil.ui.pulltorefresh;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.hlzx.wenutil.utils.Logger;
import com.hlzx.wenutil.utils.SharedPreferencesUtils;
import com.hlzx.wenutil.utils.TimeUtils;


/**
 * Created by alan on 2016/3/17.
 */
public abstract class PullToRefreshBase<T extends View> extends LinearLayout {

    private Context mContext;
    private int mTouchSlop;
    private float mLastMotionX, mLastMotionY;
    private float mInitialMotionX, mInitialMotionY;

    private boolean mIsBeingDragged = false;
    private State mState = State.PULL_TO_REFRESH;

    static final float FRICTION = 2.0f;

    T mRefreshableView;
    private FrameLayout mRefreshableViewWrapper;
    private LoadingLayout mHeaderLayout;

    private boolean isOnce = false;

    //是否可以下拉
    private boolean mIsPullRefreshEnale = true;
    public static final int SMOOTH_SCROLL_DURATION_MS = 200;

    public OnRefreshListener mOnRefreshListener;
    private SmoothScrollRunnable mCurrentSmoothScrollRunnable;

    public PullToRefreshBase(Context context) {
        this(context, null);
    }

    public PullToRefreshBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();

        mRefreshableView = createRefreshableView(mContext, attrs);

        mRefreshableViewWrapper = new FrameLayout(mContext);
        mRefreshableViewWrapper.addView(mRefreshableView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addViewInternal(mRefreshableViewWrapper, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mHeaderLayout = new FlipLodingLayout(mContext);

        if (this == mHeaderLayout.getParent()) {
            removeView(mHeaderLayout);
        }
        addViewInternal(mHeaderLayout, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!isOnce)//初始化设置头部隐藏
        {
            hideLoadingViews();
            isOnce = true;
        }
    }

    /**
     * 隐藏头部
     */
    protected final void hideLoadingViews() {
        final int maxPullScroll = (int) (getMaxPullScroll() * 0.5f);

        int pLeft = getPaddingLeft();
        int pTop = getPaddingTop();
        int pRight = getPaddingRight();
        int pBottom = getPaddingBottom();

        pTop = -mHeaderLayout.getHeight();
        //Logger.e(String.format("Setting Padding. L: %d, T: %d, R: %d, B: %d", pLeft, pTop,pRight, pBottom));
        setPadding(pLeft, pTop, pRight, pBottom);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Logger.e("addView: " + child.getClass().getSimpleName());
        final T refreshableView = getRefreshableView();
        if (refreshableView instanceof ViewGroup) {
            ((ViewGroup) refreshableView).addView(child, index, params);
        } else {
            throw new UnsupportedOperationException("Refreshable View is not a ViewGroup so can't addView");
        }
    }

    protected final void addViewInternal(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    protected final void addViewInternal(View child, ViewGroup.LayoutParams params) {
        super.addView(child, -1, params);
    }

    public final T getRefreshableView() {
        return mRefreshableView;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //Logger.e("onInterceptTouchEvent:action=" + ev.getAction());
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            return false;
        }

        if(mState==State.REFRESHING)
        {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInitialMotionX = mLastMotionX = ev.getX();
                mInitialMotionY = mLastMotionY = ev.getY();
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isReadyForPullStart()) {
                    final float y = ev.getY(), x = ev.getX();
                    final float diff, oppositeDiff, absDiff;

                    diff = y - mLastMotionY;
                    oppositeDiff = x - mLastMotionX;

                    absDiff = Math.abs(diff);
                    if (absDiff > mTouchSlop && absDiff > Math.abs(oppositeDiff)) {
                        if (diff > 1f) {
                            mLastMotionY = y;
                            mLastMotionX = x;
                            mIsBeingDragged = true;
                        }
                    }

                }
                break;
        }

        // Logger.e("mIsBeingDragged="+mIsBeingDragged);
        return mIsBeingDragged;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Logger.e("onTouch:action="+event.getAction());
        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mIsBeingDragged) {
                    mLastMotionY = mInitialMotionY = event.getY();
                    mLastMotionX = mInitialMotionX = event.getX();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mState==State.REFRESHING)
                {
                     return true;
                }
                mLastMotionY = event.getY();
                mLastMotionX = event.getX();

                int newScrollValue = Math.round(Math.min(mInitialMotionY - mLastMotionY, 0) / FRICTION);
                setHeaderScroll(newScrollValue);

                int itemDimension = mHeaderLayout.getContentSize();

                /*if (newScrollValue != 0) {
                    float scale = Math.abs(newScrollValue) / (float) itemDimension;
                    mHeaderLayout.onPull(scale);
                }*/

                if (itemDimension >= Math.abs(newScrollValue)) {

                    mState=State.PULL_TO_REFRESH;
                    mHeaderLayout.pullToRefresh();

                } else if (itemDimension < Math.abs(newScrollValue)) {
                    mState=State.RELEASE_TO_REFRESH;
                    mHeaderLayout.releaseToRefresh();
                }

                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
               if(mState==State.RELEASE_TO_REFRESH)
               {
                   mState=State.REFRESHING;

               }else
               {
                   smoothScrollTo(0);
                   reset();
               }
                setState(mState);
                return true;
        }
        return false;
    }

    private void setState(State state)
    {
        if(state==State.PULL_TO_REFRESH)
        {
            mHeaderLayout.pullToRefresh();
        }else if(state==State.RELEASE_TO_REFRESH)
        {
            mHeaderLayout.releaseToRefresh();
        }else if(state==State.REFRESHING)
        {
            smoothScrollTo(-mHeaderLayout.getHeight());
            mHeaderLayout.refreshing();
            if(mOnRefreshListener!=null) {
                mOnRefreshListener.onRefresh(this);
            }
        }else
        {
            smoothScrollTo(0);
            reset();
        }
    }

    private void reset() {
        mIsBeingDragged = false;
        mHeaderLayout.reset();
    }

    //刷新完成
    public void refreshFinished()
    {
        mState=State.PULL_TO_REFRESH;
        smoothScrollTo(0);
        reset();

        String lastTime=TimeUtils.getCurrentHHMM();
        SharedPreferencesUtils.getInstance(mContext).put("last_time",lastTime);
        mHeaderLayout.setLastUpdateLabel(lastTime);
    }

    protected final void setHeaderScroll(int value) {
        final int maxPullScroll = getMaxPullScroll();
        value = Math.min(maxPullScroll, Math.max(-maxPullScroll, value));
        if (value <= 0) {
            mHeaderLayout.setVisibility(View.VISIBLE);
        } else {
            mHeaderLayout.setVisibility(View.INVISIBLE);
        }

        //Logger.e("value="+value);
        scrollTo(0, value);
    }

    protected final void smoothScrollTo(int scrollValue) {
        smoothScrollTo(scrollValue, null);
    }

    protected final void smoothScrollTo(int scrollValue, OnSmoothScrollFinishedListener listener) {
        smoothScrollTo(scrollValue, SMOOTH_SCROLL_DURATION_MS, 0, listener);
    }

    private final void smoothScrollTo(int newScrollValue, long duration,
                                      long delayMillis, OnSmoothScrollFinishedListener listener) {
        if (null != mCurrentSmoothScrollRunnable) {
            mCurrentSmoothScrollRunnable.stop();
        }

        final int oldScrollValue;
        oldScrollValue = getScrollY();

        if (oldScrollValue != newScrollValue) {
            mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(
                    oldScrollValue, newScrollValue, duration, listener);

            if (delayMillis > 0) {
                postDelayed(mCurrentSmoothScrollRunnable, delayMillis);
            } else {
                post(mCurrentSmoothScrollRunnable);
            }
        }
    }

    //获取最大的下拉距离
    private int getMaxPullScroll() {
        return Math.round(getHeight() / FRICTION);
    }

    protected abstract T createRefreshableView(Context context, AttributeSet attrs);

    protected abstract boolean isReadyForPullStart();

    public void setOnRefreshListener(OnRefreshListener mOnRefreshListener) {
        this.mOnRefreshListener = mOnRefreshListener;
    }


    final class SmoothScrollRunnable implements Runnable {

        private Interpolator mInterpolator;
        private final int mScrollToY;
        private final int mScrollFromY;
        private final long mDuration;
        private OnSmoothScrollFinishedListener mListener;

        private boolean mContinueRunning = true;
        private long mStartTime = -1;
        private int mCurrentY = -1;

        public SmoothScrollRunnable(int fromY, int toY, long duration,
                                    OnSmoothScrollFinishedListener listener) {
            mScrollFromY = fromY;
            mScrollToY = toY;
            mInterpolator = new DecelerateInterpolator();
            mDuration = duration;
            mListener = listener;
        }

        @Override
        public void run() {
            if (mStartTime == -1) {
                mStartTime = System.currentTimeMillis();
            } else {
                long normalizedTime =  (1000 * (System.currentTimeMillis() - mStartTime)) /mDuration;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((mScrollFromY - mScrollToY) *
                        mInterpolator.getInterpolation(normalizedTime/1000f));
                mCurrentY = mScrollFromY - deltaY;
                setHeaderScroll(mCurrentY);
            }
            if (mContinueRunning && mScrollToY != mCurrentY) {
                ViewCompat.postOnAnimation(PullToRefreshBase.this, this);
            } else {
                if (null != mListener) {
                    mListener.onSmoothScrollFinished();
                }
            }
        }

        public void stop() {
            mContinueRunning = false;
            removeCallbacks(this);
        }
    }

    static interface OnSmoothScrollFinishedListener {
        void onSmoothScrollFinished();
    }

}
