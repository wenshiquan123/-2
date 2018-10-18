package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
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
public class ResetPasswordActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.user_name_et)
    private EditText user_name_et;
    @ViewInject(R.id.new_pw_et)
    private EditText new_pw_et;
    @ViewInject(R.id.code_et)
    private EditText code_et;

    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    Dialog waitDialog=null;
    String phone="";

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_reset_password);
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
        title_tv.setText("重置密码");

        phone=getIntent().getStringExtra("phone");
    }

    public void back(View view) {
        finish();
    }

    public void commit(View view) {
        String username = user_name_et.getText().toString();
        String newpw = new_pw_et.getText().toString();
        String code = code_et.getText().toString();

        /*if(username.trim().equals(""))
        {
            response_tv.setText("用户名不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }*/
        if(newpw.trim().equals(""))
        {
            response_tv.setText("新密码不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }
        if(code.trim().equals(""))
        {
            response_tv.setText("验证码不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("phone", username);
            json.put("verifycode",code);
            json.put("phone",phone);
            json.put("password", newpw);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            //showToast(map.toString());
            LogUtil.e("ME", "long_IV=" + ClientEncryptionPolicy.getInstance().getIV());
            LogUtil.e("ME","session_id="+MyApplication.getSession_id());
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitDialog= PublicDialog.createLoadingDialog(this,"正在提交...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.PASSWORD_RESET, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "pw_reset=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    String text = jsonObject.getString("text");
                    int status = jsonObject.getIntValue("status");
                    if (status == HttpConstant.SUCCESS_CODE) {
                        showToast("重置密码成功");
                        startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                        finish();

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        response_tv.setText(text);
                        response_tv.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
}
