package com.hlzx.wenutil.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by alan on 2016/2/15.
 */
public class HBListView extends ListView implements AbsListView.OnScrollListener{

    //header显示的图片
    private MyImageView mImageView;
    private Context mContext;
    //抢到红包时候的监听器
    private OnSuccessListener mOnSuccessListener;

    private boolean b = false;

    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    public interface OnSuccessListener {
        void onSuccess();

        void onScroll(float y);
    }

    public void setOnSuccessListener(OnSuccessListener onSuccessListener) {
        mOnSuccessListener = onSuccessListener;
    }

    public HBListView(Context context) {
        this(context, null);
    }

    public HBListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HBListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                Log.e("ME","height="+mImageView.getLayoutParams().height);
                if(mImageView.getLayoutParams().height>0) {
                    int top = getChildAt(1).getTop();
                    mOnSuccessListener.onScroll(top / 300f);
                }
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

        //给图片一个最大值
        if (mImageView.getHeight() < 300) {
            //是触摸状态 以及 是下滑状态
            if (isTouchEvent && deltaY < 0) {
                //动态改变imageView的大小
                mImageView.getLayoutParams().height += Math.abs(deltaY) / 2;
                mImageView.requestLayout();
                mOnSuccessListener.onScroll(mImageView.getLayoutParams().height / 300f);
            }
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = ev.getX();
                y1 = ev.getY();
                if (oa != null) {
                    if (oa.isRunning()) {
                        oa.cancel();
                    }
                }

                /*if (mImageView.getHeight() == 0) {
                    mImageView.mTime = 0;
                }*/
                break;

            case MotionEvent.ACTION_MOVE:
                float curY=ev.getY();
                float diff=curY-y1;

                if(diff<0 & mImageView.getLayoutParams().height>0)
                {
                    //Log.e("ME", "height=" + mImageView.getLayoutParams().height);
                   //mOnSuccessListener.onScroll(Math.abs(diff)/300f);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                x2 = ev.getX();
                y2 = ev.getY();
                if (y1 - y2 > 0) {
                    b = false;
                } else if (y2 - y1 > 0) {
                    b = true;
                }

                int ran = (int) (Math.random() * 100);
                if (b) {  //往下滑

                    if (ran > 3) {
                        mImageView.mTime++;
                        closeHeader();
                    } else {
                        mImageView.mTime = 0;
                        if (mOnSuccessListener != null) {
                            mOnSuccessListener.onSuccess();
                        }
                        closeHeader();
                    }
                }

                break;
        }


        return super.onTouchEvent(ev);

    }


    public void changeSize(MyImageView imageView) {
        mImageView = imageView;
    }

    private ObjectAnimator oa;

    private void closeHeader() {
        oa = ObjectAnimator.ofInt(mImageView, "height", mImageView.getHeight(), 0);
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //Log.e("ME","animation="+animation.getAnimatedValue());
                mOnSuccessListener.onScroll(((int) animation.getAnimatedValue()) / 300f);
            }
        });
        oa.start();
    }
}
