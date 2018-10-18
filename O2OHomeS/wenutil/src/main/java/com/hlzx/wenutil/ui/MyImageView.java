package com.hlzx.wenutil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by alan on 2016/2/15.
 */
public class MyImageView extends ImageView{

    public static int mTime;
    private Typeface mTypeFace;
    private float mTxtHLength;
    private float mTxtRLength;
    private Paint mPaint;
    private Paint mMidPaint;

    private String txtH = "连刷";
    private String txtR = "次，加油！";

    public MyImageView(Context context) {
        this(context,null);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setColor(Color.YELLOW);
        mMidPaint = new Paint();
        mMidPaint.setColor(Color.YELLOW);
        mTypeFace = Typeface.createFromAsset(context.getAssets(), "ifont.ttf");
        mMidPaint.setTypeface(mTypeFace);
        mPaint.setTypeface(mTypeFace);
        //设置文字大小
        mPaint.setTextSize(50);
        //设置数字大小
        mMidPaint.setTextSize(100);

        //测量头文字的长度
        mTxtHLength = mPaint.measureText(txtH);
        //测量尾文字的长度
        mTxtRLength = mPaint.measureText(txtR);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String count = mTime + "";
        //数字的长度
        float countLength = mMidPaint.measureText(count);
        //总体长度
        float totalLength = mTxtRLength + countLength;

        //画在正中间
        float start = getMeasuredWidth() / 2 - totalLength / 2;

        canvas.drawText(txtH, 100, getMeasuredHeight() / 2, mPaint);
        canvas.drawText(count, start + countLength / 2, getMeasuredHeight() / 2, mMidPaint);
        canvas.drawText(txtR, start + countLength / 2 + mTxtRLength / 2, getMeasuredHeight() / 2, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setHeight(int height) {
        getLayoutParams().height = height;
        requestLayout();
    }
}
