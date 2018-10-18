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
public class InventoryLockPasswordActivity extends BaseActivity implements View.OnClickListener{
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

        if(MyApplication.getInstance().getUserInfo().getRepertory_lock()==0)
        {
            action_type=1;
        }else if(MyApplication.getInstance().getUserInfo().getRepertory_lock()==1)
        {
            action_type=2;
        }

        LogUtil.e("ME","action_type="+action_type);
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token",MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type",action_type);
            json.put("password",pw);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitDialog= PublicDialog.createLoadingDialog(this,"正在提交...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.SHOP_REPERTORY_LOCK, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result=responseInfo.result.toString();
                LogUtil.e("ME", "result=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    String text = jsonObject.getString("text");
                    int status = jsonObject.getIntValue("status");

                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(InventoryLockPasswordActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE)
                    {
                        if( action_type==1) {//开启
                            InventoryLockPasswordActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                    putInt("repertory_lock", 1).
                                    commit();
                            MyApplication.getInstance().getUserInfo().setRepertory_lock(1);
                        }else if(action_type==2)//关闭
                        {
                            InventoryLockPasswordActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                    putInt("repertory_lock",0).
                                    commit();
                            MyApplication.getInstance().getUserInfo().setRepertory_lock(0);
                        }
                        showToast(text);
                        startActivity(new Intent(InventoryLockPasswordActivity.this,ShopInventoryLockActivity.class));
                        finish();

                    }else if(status==HttpConstant.FAILURE_CODE)
                    {
                        response_tv.setText(text);
                        response_tv.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e)
                {
                    response_tv.setText(e.toString());
                    response_tv.setVisibility(View.VISIBLE);
                }
                waitDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                response_tv.setText(s);
                response_tv.setVisibility(View.VISIBLE);
                waitDialog.dismiss();
            }
        });

    }

    public void back(View view) {
        finish();
    }

}
