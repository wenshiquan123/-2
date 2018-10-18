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
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
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
public class UpdateInventoryPasswordActivity extends BaseActivity implements View.OnClickListener{

    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.forget_password_tv)
    private TextView forget_password_tv;

    //原有密码
    @ViewInject(R.id.old_password_et)
    private EditText old_password_et;
    //新密码
    @ViewInject(R.id.new_password_et)
    private EditText new_password_et;
    //重复密码
    @ViewInject(R.id.renew_password_et)
    private EditText renew_password_et;
    //信息提示
    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    Dialog waitDialog=null;
    @Override
    public void setLayout() {
        setContentView(R.layout.activity_update_inventoty_lock_password);
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
        title_tv.setText("修改库存锁密码");
        forget_password_tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==forget_password_tv)
        {
           startActivity(new Intent(this,ForgetInventoryLockPasswordActivity.class));
        }
    }

    public void commit(final View view)
    {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            return;
        }

        String oldPw=old_password_et.getText().toString();
        String newPw=new_password_et.getText().toString();
        String renewPw=renew_password_et.getText().toString();
        if(oldPw.trim().equals(""))
        {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("原有密码不能为空");
            return;
        }
        if(newPw.trim().equals(""))
        {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("新密码不能为空");
            return;
        }
        if(!renewPw.trim().equals(newPw))
        {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("两次输入密码不一致");
            return;
        }

        if(!PublicUtils.md5(oldPw).equals(MyApplication.getInstance().getUserInfo().getRepertory_pw()))
        {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("旧密码不正确");
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type",3);
            json.put("password",oldPw);
            json.put("newpassword",newPw);
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
            public void onSuccess(ResponseInfo<String> responseInfo)
            {
                String result=responseInfo.result.toString();
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    //String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent=new Intent(UpdateInventoryPasswordActivity.this,LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action",0);
                        startActivity(intent);
                        finish();
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(UpdateInventoryPasswordActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                               commit(view);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if(status== HttpConstant.SUCCESS_CODE)
                    {
                        UpdateInventoryPasswordActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                putString("repertory_pw",PublicUtils.md5(new_password_et.getText().toString())).
                                commit();
                        MyApplication.getInstance().getUserInfo().setRepertory_pw(PublicUtils.md5(new_password_et.getText().toString()));
                        finish();

                    }else if(status==HttpConstant.FAILURE_CODE)
                    {
                        showToast(text);
                    }
                }catch (Exception e)
                {}
                waitDialog.dismiss();
            }
            @Override
            public void onFailure(HttpException e, String s) {
                waitDialog.dismiss();
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText(s);
            }
        });
    }

    public void back(View view)
    {
        finish();
    }
}
