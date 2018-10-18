package com.hlzx.wenutil.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by alan on 2016/3/10.
 */
public class PullHeaderView extends FrameLayout{

    private Context mContext;
    private TextView mHeaderText;
    public PullHeaderView(Context context) {
        this(context,null);
    }

    public PullHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        init();
    }
    private void init()
    {
        mHeaderText=new TextView(mContext);
        mHeaderText.setTextSize(20f);
        mHeaderText.setPadding(20, 20, 20, 20);
        mHeaderText.setGravity(Gravity.CENTER);
        addView(mHeaderText);
    }

    protected void start() {
        mHeaderText.setText("下拉刷新");
    }

    protected void loading() {
        mHeaderText.setText("正在刷新...");
    }

    protected void end() {
        mHeaderText.setText("刷新结束");
    }

}
