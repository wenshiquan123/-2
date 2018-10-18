package com.hlzx.wenutil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.Random;

/**
 * Created by alan on 2016/2/23.
 */
public class TwoWayProgressView extends View {


    int mMeasureHeight;
    int mMeasureWidth;

    Paint mPaint;
    Paint mLastPaint;


    //进度条长度
    int mPregress;
    Rect mPregressRect;
    Rect mLastPregressRect;
    int mScreenWidth;

    Handler handler;

    public TwoWayProgressView(Context context) {
       this(context,null);
    }

    public TwoWayProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoWayProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    public void init(Context context)
    {

        mPaint=new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(3);

        mLastPaint=new Paint();
        mLastPaint.setAntiAlias(true);
        mLastPaint.setColor(Color.BLUE);
        mLastPaint.setStrokeWidth(3);

        /**
         * 获取屏幕宽
         */
        WindowManager wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        mScreenWidth=metrics.widthPixels;
        mMeasureWidth=mScreenWidth;

        mPregressRect=new Rect();

        mLastPregressRect=new Rect();

        handler=new Handler();
        //handler.post(runnable);

    }

    int i=0;
    private final Runnable runnable=new Runnable() {
        @Override
        public void run() {
            float percent=i/100f;
            if(i>=100)
            {
                //上一次的进度条
                mLastPaint.setColor(mPaint.getColor());

                Random random=new Random();
                int r=random.nextInt(255);
                int g=random.nextInt(255);
                int b=random.nextInt(255);
                mPaint.setColor(Color.rgb(r,b,g));
                i=0;
            }
            mPregressRect.set((int) (mScreenWidth / 2 - percent * (mScreenWidth / 2)), 0,
                    (int) (mScreenWidth / 2 + percent * (mScreenWidth / 2)), mMeasureHeight);
            //canvas.drawRect(mPregressRect,mPaint);
            postInvalidate();
            i=i+2;
            handler.post(this);
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            mMeasureHeight = heightSize;
        } else {
            mMeasureHeight = 10;
        }
        setMeasuredDimension(mMeasureWidth, mMeasureHeight);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //mLastPregressRect.set(0, 0, mScreenWidth, mMeasureHeight);
        //canvas.drawRect(mLastPregressRect, mLastPaint);

        canvas.drawRect(mPregressRect,mPaint);


    }

    /**
     * 设置进度条的长度，百分比
     */
    public void setPregress(float percent) {
        //float percent=(float)(progress*100);

        Log.e("ME", "progress=" + percent);
        mPregressRect.set((int) (mScreenWidth / 2 - percent * (mScreenWidth / 2)), 0,
                (int) (mScreenWidth / 2 + percent * (mScreenWidth / 2)), mMeasureHeight);
        postInvalidateDelayed(10);
    }

}
