package com.hlzx.yicheng.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by alan on 2016/3/28.
 */
public class TitleBar extends RelativeLayout implements View.OnClickListener{

    private Context mContext;
    private ImageView mBackImg;
    private TextView mTitleTv;
    private ImageView mRTBtn;
    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();



    }

    @Override
    public void onClick(View v) {

    }
}
