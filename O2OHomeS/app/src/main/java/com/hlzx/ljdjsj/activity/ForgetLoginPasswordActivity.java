package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
 * Created by alan on 2015/12/14.
 */
public class ForgetLoginPasswordActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.next_bt)
    private Button next_bt;

    @ViewInject(R.id.user_name_et)
    private EditText user_name_et;

    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    Dialog waitDialog = null;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_forget_login_password);
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
        title_tv.setText("输入用户名");


    }

    @Override
    public void onClick(View v) {

    }

    /*
     *获取验证码
     */
    public void getCode(View view) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        String username = user_name_et.getText().toString();
        if (username.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("注册手机号码不能为空");
            return;
        }

        /*if (!PublicUtils.checkPhone(username)) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("手机号码格式不正确");
            return;
        }*/

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("phone", username);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            //showToast(map.toString());
            LogUtil.e("ME", "long_IV=" + ClientEncryptionPolicy.getInstance().getIV());
            LogUtil.e("ME", "session_id=" + MyApplication.getSession_id());
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText(e.toString());
        }
        waitDialog = PublicDialog.createLoadingDialog(this, "正在获取...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.GET_CODE, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "pw_reset=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    String text = jsonObject.getString("text");
                    int status = jsonObject.getIntValue("status");

                   //判断是否在别处登录
                    /*String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")) {
                        Intent intent = new Intent(ForgetLoginPasswordActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    }*/

                    if (status == HttpConstant.SUCCESS_CODE) {
                        //response_tv.setVisibility(View.VISIBLE);
                        //response_tv.setText(text);
                        showToast("获取验证码成功");
                        Intent intent = new Intent(ForgetLoginPasswordActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("phone", user_name_et.getText().toString());
                        startActivity(intent);
                        finish();

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    public void back(View view) {
        finish();
    }
}
