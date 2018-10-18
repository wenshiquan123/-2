package com.hlzx.ljdjsj.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 */
public class PublishWorkActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_about_us);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        initView();
    }

    @Override
    public void initView() {
        super.initView();
        title_tv.setText("发布任务");
    }

    public void back(View view) {
        finish();
    }
}
