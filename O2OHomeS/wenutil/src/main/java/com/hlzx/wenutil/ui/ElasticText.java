package com.hlzx.wenutil.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by alan on 2016/2/26.
 */
public class ElasticText extends TextView {

    private Scroller mScroller;
    private GestureDetector mGestureDetector;

    float mYMove;
    float mYLastMove;

    public ElasticText(Context context) {
        this(context, null);
    }

    public ElasticText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElasticText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        setClickable(true);
        setLongClickable(true);
        mScroller = new Scroller(getContext());
        //mGestureDetector = new GestureDetector(getContext(), new BouncyGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP :
                smoothScrollTo(0, 0);
                break;

            case MotionEvent.ACTION_DOWN:
                mYMove=mYLastMove=event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                mYMove=event.getRawY();
                int diff=(int)(mYLastMove-mYMove);
                int distanceY=(int)((diff-0.5f)/2);
                smoothScrollBy(0,distanceY);
                mYLastMove=mYMove;
                break;

            //default:
                //return mGestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);

    }


    /**
     * 滚动到目标位置
     *
     * @param fx
     * @param fy
     */
    protected void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    /**
     * 设置滚动的相对偏移
     *
     * @param dx
     * @param dy
     */
    protected void smoothScrollBy(int dx, int dy) {
        //设置mScroller的滚动偏移量
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    @Override
    public void computeScroll() {
        //判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset()) {
            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }


    class BouncyGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int dis = (int)((distanceY-0.5)/2);
            smoothScrollBy(0, dis);
            //Log.e("ME","dis="+dis);
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
}
