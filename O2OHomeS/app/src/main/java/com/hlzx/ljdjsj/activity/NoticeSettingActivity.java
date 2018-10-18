package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
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
public class NoticeSettingActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    @ViewInject(R.id.notice_et)
    private EditText notice_et;
    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    Dialog waitDialog = null;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_notice_setting);
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
        title_tv.setText("公告设置");

        if (MyApplication.getInstance().getUserInfo() != null) {
            notice_et.setText(MyApplication.getInstance().getUserInfo().getNotice());
        }
    }

    public void commit(View view) {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            return;
        }

        final String notice = notice_et.getText().toString();
        if (notice.trim().equals("")) {
            response_tv.setText("公告不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("notice", notice);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        waitDialog = PublicDialog.createLoadingDialog(this, "正在提交...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.SHOP_NOTICE, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "公告=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");

                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
                        showToast(serverMsg);
                        Intent intent = new Intent(NoticeSettingActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    }
                    if (status == HttpConstant.SUCCESS_CODE)
                    {
                        NoticeSettingActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                putString("notice",notice).
                                commit();
                        MyApplication.getInstance().getUserInfo().setNotice(notice);
                        finish();
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        response_tv.setText(text);
                        response_tv.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    response_tv.setText(e.toString());
                    response_tv.setVisibility(View.VISIBLE);
                }
                waitDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                waitDialog.dismiss();
                response_tv.setText(s);
                response_tv.setVisibility(View.VISIBLE);
            }
        });

    }

    public void back(View view) {
        finish();
    }
}
