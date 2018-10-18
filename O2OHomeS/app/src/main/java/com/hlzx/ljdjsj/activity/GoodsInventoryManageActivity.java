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
public class GoodsInventoryManageActivity extends BaseActivity implements View.OnClickListener{
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.goods_message_rl)
    private RelativeLayout goods_message_rl;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_goods_inventory_manage);
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
        title_tv.setText("商品库管理");
        goods_message_rl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==goods_message_rl)
        {
           startActivity(new Intent(this,GoodsManageActivity.class));
        }
    }

    public void back(View view) {
        finish();
    }
}
