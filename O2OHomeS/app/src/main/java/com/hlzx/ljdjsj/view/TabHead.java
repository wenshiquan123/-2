package com.hlzx.ljdjsj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by alan on 2015/12/25.
 * wenshiquan
 */
public class TabHead extends HorizontalScrollView{

    public TabHead(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabHead(Context context) {
        super(context);
    }

    public TabHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


}
