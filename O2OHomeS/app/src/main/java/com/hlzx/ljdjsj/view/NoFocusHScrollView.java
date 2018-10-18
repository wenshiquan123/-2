package com.hlzx.ljdjsj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by alan on 2016/1/23.
 *
 *
 */
public class NoFocusHScrollView extends HorizontalScrollView{

    public NoFocusHScrollView(Context context) {
        this(context, null);
    }

    public NoFocusHScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoFocusHScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /**
         * 返回false，把事件分发给外层view处理
         */
        return false;
    }
}
