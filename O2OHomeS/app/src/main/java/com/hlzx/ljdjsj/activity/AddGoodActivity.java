package com.hlzx.ljdjsj.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 * 自定义添加商品
 */
public class AddGoodActivity extends BaseActivity implements View.OnClickListener {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.add_good_detail_ll)
    private LinearLayout add_good_detail_ll;
    @ViewInject(R.id.scan_ll)
    private LinearLayout scan_ll;

    @ViewInject(R.id.add_good_with_inventory_ll)
    private LinearLayout add_good_with_inventory_ll;


    @Override
    public void setLayout() {
        setContentView(R.layout.activity_add_good);
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
        title_tv.setText("添加商品");

        add_good_detail_ll.setOnClickListener(this);
        scan_ll.setOnClickListener(this);
        add_good_with_inventory_ll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == add_good_detail_ll) {
            startActivity(new Intent(this, AddCustomGoodActivity.class));
        } else if (v == scan_ll) {
            startActivity(new Intent(this, ScanActivity.class));
        }else if(v==add_good_with_inventory_ll)
        {
            startActivity(new Intent(this,GoodInventoryActivity.class));
        }
    }

    public void back(View view) {
        finish();
    }


}
