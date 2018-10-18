package com.hlzx.wenutil.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.hlzx.wenutil.R;

/**
 * Created by alan on 2016/2/24.
 */
public class GuaGuaKaView extends View {


    private Paint mOutterPaint;
    private Path mPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;

    private int mLastX;
    private int mLastY;

    private Bitmap mOutterBitmap;

    // -------------------------------

    // private Bitmap bitmap;

    private String mText;
    private Paint mBackPaint;

    /**
     * 记录刮奖信息文本的宽和高
     */
    private Rect mTextBound;
    private int mTextSize;
    private int mTextColor;

    // 判断遮盖层区域是否消除达到阈值
    private volatile boolean mComplete = false;

    /**
     * 刮刮卡刮完的回调
     */
    public interface OnGuaGuaKaCompleteListener {
        void complete();
    }

    private OnGuaGuaKaCompleteListener mListener;

    public void setOnGuaGuaKaCompleteListener(
            OnGuaGuaKaCompleteListener mListener) {
        this.mListener = mListener;
    }

    public GuaGuaKaView(Context context) {
        this(context, null);
    }

    public GuaGuaKaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuaGuaKaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        TypedArray a = null;
        try {
            a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GuaGuaKa, defStyleAttr, 0);
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);

                if (R.styleable.GuaGuaKa_text == attr) {
                    mText = a.getString(attr);
                } else if (R.styleable.GuaGuaKa_textSize == attr) {


                    mTextSize = (int) a.getDimension(attr,
                            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics()));
                } else if (R.styleable.GuaGuaKa_textColor == attr) {
                    mTextColor = a.getColor(attr, 0x000000);
                    break;

                }
            }

        } finally {
            if (a != null)
                a.recycle();
        }
    }

    private void init() {
        mOutterPaint = new Paint();
        mPath = new Path();

        mOutterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        mText = "谢谢惠顾";
        mTextBound = new Rect();
        mBackPaint = new Paint();
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics());

    }

    public void setText(String text) {
        this.mText = text;
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        // 设置绘制path画笔的一些属性
        setUpOutPaint();
        setUpBackPaint();

        // mCanvas.drawColor(Color.parseColor("#c0c0c0"));
        mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30,
                mOutterPaint);
        mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0, 0, width, height),
                null);
    }


    private void setUpOutPaint() {
        mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutterPaint.setStyle(Paint.Style.FILL);
        mOutterPaint.setStrokeWidth(20);
    }

    private void setUpBackPaint() {
        mBackPaint.setColor(mTextColor);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setTextSize(mTextSize);
        // 获得当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mLastX = x;
                mLastY = y;
                mPath.moveTo(x, y);

                break;
            case MotionEvent.ACTION_MOVE:

                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);

                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mComplete) {
                    new Thread(runnable).start();
                }
                break;
        }

        if (!mComplete) {
            invalidate();
        }

        return true;
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            int w = getWidth();
            int h = getHeight();

            float wipeArea = 0;
            float totalArea = w * h;
            Bitmap bitmap = mBitmap;
            int[] mPixels = new int[w * h];

            // 获得Bitmap上所有的像素信息
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }

            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);

                //Log.e("TAG", percent + "");

                if (percent > 90) {
                    // 清除掉图层区域
                    mComplete = true;
                    postInvalidate();

                }

            }


        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2,
                getHeight() / 2 + mTextBound.height() / 2, mBackPaint);

        if (!mComplete) {
            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        if (mComplete) {
            if (mListener != null) {
                mListener.complete();
            }
        }
    }

    private void drawPath() {
        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOutterPaint);
    }
}
