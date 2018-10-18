package com.hlzx.ljdjsj;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.hlzx.ljdjsj.activity.LoginActivity;
import com.hlzx.ljdjsj.common.Constants;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/8.
 * wenshiquan
 */
public class SplashActivity extends BaseFragmentActivity implements android.os.Handler.Callback {


    Handler handler = new Handler(this);
    SharedPreferences sp = null;
    Dialog dialog = null;

    @Override
    public void layout() {
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
         //友盟更新
        UmengUpdateAgent.setUpdateAutoPopup(true);
        UmengUpdateAgent.update(getApplicationContext());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handshake(SplashActivity.this);
            }
        },2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void handshake(final Context context) {
        dialog = PublicDialog.createLoadingDialog(this, "正在连接...");
        dialog.show();

        Map<String, String> map = null;
        try {
            String[] data = ClientEncryptionPolicy.getInstance().
           encryptShakeHandsData(PublicUtils.getDeviceInfo(MyApplication.getInstance().
                   getApplicationContext()).toString());
            map = new HashMap();
            map.put("sha1", data[0]);
            map.put("data", URLEncoder.encode(data[1], "utf-8"));
            LogUtil.e("ME", "sha1=" + data[0] + "|data=" + URLEncoder.encode(data[1], "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("ME", "异常=" + e.toString());
        }
        LogUtil.e("ME", "url=" + UrlsConstant.SHAKE_HAND);
        //LogUtil.e("ME", "data=" + map.toString());
        HttpUtil.doPostRequest(UrlsConstant.SHAKE_HAND,map,
                new RequestCallBack() {
                    @Override
                    public void onSuccess(ResponseInfo responseInfo) {
                        String result = responseInfo.result.toString();
                        //LogUtil.e("握手返回信息=" + result);
                        //showToast(result);
                        try {
                            JSONObject object = new JSONObject(result);
                            int status = object.getInt("status");
                            String data = object.getString("data");
                            String text = object.getString("text");
                            if (status == HttpConstant.SUCCESS_CODE) {
                                Message msg = handler.obtainMessage(1);
                                msg.obj = data;
                                handler.sendMessage(msg);
                            } else if (status == HttpConstant.FAILURE_CODE) {
                                handler.sendEmptyMessage(0);
                                showToast(text);
                            }
                        } catch (Exception e) {
                            showToast(e.toString());
                            handler.sendEmptyMessage(0);
                        }
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    }
                    @Override
                    public void onFailure(HttpException e, String s) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        showToast("服务器异常");
                        handler.sendEmptyMessage(0);
                    }
                });
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == 1) {
            String data = (String) msg.obj;
            com.alibaba.fastjson.JSONObject object = JSON.parseObject(data);
            //保存Session_id
            try
            {
            this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                    putString("security_session_id", object.getString("security_session_id")).
                    putString("aesKey",ClientEncryptionPolicy.getAesKey()).
                    commit();
            }catch (Exception e)
            {}
            MyApplication.getInstance().setSession_id(object.getString("security_session_id"));
            LogUtil.e("ME", "保存的session_id=" + object.getString("security_session_id"));
           // LogUtil.e("ME", "保存的iv=" + object.getString("security_session_id"));
           // initUserInfo(sp);
            if (MyApplication.getInstance().getUserInfo().getToken() == null
                    || MyApplication.getInstance().getUserInfo().getToken().equals(""))
            {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();

            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

        } else if (msg.what == 0)//握手失败
        {
            //initUserInfo(sp);
            //startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
