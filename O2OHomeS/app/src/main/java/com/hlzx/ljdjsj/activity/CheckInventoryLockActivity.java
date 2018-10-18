package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class CheckInventoryLockActivity extends BaseActivity implements View.OnClickListener{
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.response_tv)
    private TextView response_tv;
    @ViewInject(R.id.password_et)
    private EditText password_et;
    //忘记库存锁密码
    @ViewInject(R.id.forget_password_tv)
    private TextView forget_password_tv;

    Dialog waitDialog=null;

    //1 代表跳到商品管理
    //2 跳到营销工具
    int action_type=0;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_inventory_lock_password);
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
        title_tv.setText("验证库存锁密码");

        forget_password_tv.setOnClickListener(this);

        action_type=getIntent().getIntExtra("action_type",0);

    }

    @Override
    public void onClick(View v) {

        if(v==forget_password_tv)
        {
            startActivity(new Intent(this,ForgetInventoryLockPasswordActivity.class));
        }

    }

    public void onCommit(View view)
    {
        String pw=password_et.getText().toString();
        if(pw.trim().equals(""))
        {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("密码不能为空!");
            return;
        }
        //验证密码
        if(!PublicUtils.md5(pw).equals(MyApplication.getInstance().getUserInfo().getRepertory_pw()))
        {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("密码错误!");
            return;
        }

        if(action_type==1)
        {
            startActivity(new Intent(this, GoodsManageActivity.class));
        }else if(action_type==2)
        {
            startActivity(new Intent(this, MaketingToolsActivity.class));
        }
        finish();



    }

    public void back(View view) {
        finish();
    }

}
