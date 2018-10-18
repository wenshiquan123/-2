package com.hlzx.ljdjsj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * Created by alan on 2016/1/6.
 */
public class ExpandableLayout extends LinearLayout{

    private Context mContext;
    private LinearLayout mContentView;
    int mContentHeight = 0;
    private boolean isExpand;
    private Animation animationDown;
    private Animation animationUp;

    public ExpandableLayout(Context context) {
        super(context);
        this.mContext=context;
    }

    public ExpandableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mContentView = (LinearLayout)this.getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if(this.mContentHeight==0)
        {
           this.mContentView.measure(widthMeasureSpec,0);
            this.mContentHeight=mContentView.getMeasuredHeight();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public class  DropDownAnim extends Animation
    {
        //目标的高度
        private int targetHeight;
        // 目标view
        private View view;
        //是否向下展开
        private boolean down;

        public DropDownAnim(View targetview, int vieweight, boolean isdown) {
            this.view = targetview;
            this.targetHeight = vieweight;
            this.down = isdown;
        }

        //每个动画都重载了父类的applyTransformation方法这个方法的主要作用是把一些
        // 属性组装成一个Transformation类,这个方法会被父类的getTransformation方法调用
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newHeight;
            if (down) {
                newHeight = (int) (targetHeight * interpolatedTime);
            } else {
                newHeight = (int) (targetHeight * (1 - interpolatedTime));
            }
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
            if (view.getVisibility() == View.GONE) {
                view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        //动画是否影响指定的视图范围
        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
