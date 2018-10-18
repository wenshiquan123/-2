package com.hlzx.ljdjsj.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 * 商品详情
 */
public class GoodDetailActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    //商品图片
    @ViewInject(R.id.shop_iv)
    private ImageView shop_iv;
    //商品名称

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_good_detail);
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
        title_tv.setText("商品详情");
    }

    public void back(View view) {
        finish();
    }
}
