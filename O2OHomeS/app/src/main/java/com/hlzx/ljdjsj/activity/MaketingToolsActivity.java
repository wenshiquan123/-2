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
public class MaketingToolsActivity extends BaseActivity implements View.OnClickListener{

    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    @ViewInject(R.id.shopkeeper_recommend_rl)
    private RelativeLayout shopkeeper_recommend_rl;

    @ViewInject(R.id.hot_rl)
    private RelativeLayout hot_rl;

    @ViewInject(R.id.subsidy_rl)
    private RelativeLayout subsidy_rl;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_marketing_tools);
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
        title_tv.setText("营销工具");

        shopkeeper_recommend_rl.setOnClickListener(this);
        hot_rl.setOnClickListener(this);
        subsidy_rl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==shopkeeper_recommend_rl)
        {
            startActivity(new Intent(this,ShopKeeperRecommendActivity.class));
        }else if(v==hot_rl)
        {
            startActivity(new Intent(this,ShopHotActivity.class));
        }else if(v==subsidy_rl)
        {
            startActivity(new Intent(this,ShopSubsidyActivity.class));
        }
    }

    public void back(View view)
    {
        finish();
    }
    public void help(View view)
    {
        startActivity(new Intent(this,HelpCenterWebViewActivity.class));
    }
}
