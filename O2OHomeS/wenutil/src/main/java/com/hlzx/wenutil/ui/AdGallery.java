package com.hlzx.wenutil.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

import com.hlzx.wenutil.utils.Logger;

/**
 * Created by alan on 2016/3/21.
 */
public class AdGallery extends Gallery implements android.os.Handler.Callback {

    //广告间隔
    private static final int GAP_TIME = 3000;
    private boolean mIsStartFlag = false;
    private Handler mHandler;
    private AdRunnable mRunnable;

    public AdGallery(Context context) {
        this(context, null);
    }

    public AdGallery(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdGallery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHandler = new Handler(this);
        mRunnable = new AdRunnable();
        mHandler.post(mRunnable);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                Logger.e("执行");
                onScroll(null, null, 1, 0);
                onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
                break;
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int keyCode;
        if (isScrollingLeft(e1, e2)) {
            keyCode= KeyEvent.KEYCODE_DPAD_LEFT;

        } else {
            keyCode=KeyEvent.KEYCODE_DPAD_RIGHT;
        }
        onKeyDown(keyCode,null);
        velocityX = velocityX > 0 ? 400 : -400;
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void setUnselectedAlpha(float unselectedAlpha) {
        // TODO Auto-generated method stub
        unselectedAlpha = 1.0f;
        super.setUnselectedAlpha(unselectedAlpha);
    }


    //是否向左边滚动
    private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
        return e2.getX() > e1.getX();
    }

    //停止广告轮播
    public void stop() {
        mIsStartFlag = false;
    }

    //开始广告
    public void start() {
        mIsStartFlag = true;
    }

    //广告轮播线程
    class AdRunnable implements Runnable {
        public AdRunnable() {
        }

        @Override
        public void run() {
            if (mIsStartFlag) {
                mHandler.sendEmptyMessage(1);

            }
            mHandler.postDelayed(this, GAP_TIME);
        }
    }

}
