package com.hlzx.ljdjsj.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 */
public class ShopInventoryLockActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    @ViewInject(R.id.update_inventory_lock_password_rl)
    private RelativeLayout update_inventoty_lock_password_rl;
    //库存锁状态
    @ViewInject(R.id.lock_status_tb)
    private ToggleButton lock_status_tb;

    @ViewInject(R.id.lock_status_ll)
    private LinearLayout lock_status_ll;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_shop_inventory_lock);
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
        title_tv.setText("库存锁管理");
        update_inventoty_lock_password_rl.setOnClickListener(this);

        //判断库存锁状态
        if(MyApplication.getInstance().getUserInfo()!=null)
        {
            LogUtil.e("ME","lock="+MyApplication.getInstance().getUserInfo().getRepertory_lock());
            if(MyApplication.getInstance().getUserInfo().getRepertory_lock()==1)
            {
                lock_status_tb.setChecked(true);
            }else
            {
                lock_status_tb.setChecked(false);
            }
        }else
        {
            lock_status_tb.setChecked(false);
        }


        lock_status_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShopInventoryLockActivity.this, InventoryLockPasswordActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MyApplication.getInstance().getUserInfo().getRepertory_lock()==1)
        {
            lock_status_tb.setChecked(true);
        }else
        {
            lock_status_tb.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == update_inventoty_lock_password_rl) {
            startActivity(new Intent(this, UpdateInventoryPasswordActivity.class));
        }
    }

    public void back(View view) {
        finish();
    }
}
