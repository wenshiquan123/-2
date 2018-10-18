package com.hlzx.ljdjsj.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 */
public class UserAnalyzeActivity extends BaseActivity implements View.OnClickListener{
    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    //待发展用户
    @ViewInject(R.id.arming_user_rl)
    private RelativeLayout arming_user_rl;


    @Override
    public void setLayout() {
        setContentView(R.layout.activity_user_analyze);
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
        title_tv.setText("用户分析");

        arming_user_rl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(arming_user_rl==v)
        {
            startActivity(new Intent(this,ArmingUserActivity.class));
        }
    }
    public void back(View view) {
        finish();
    }
}
