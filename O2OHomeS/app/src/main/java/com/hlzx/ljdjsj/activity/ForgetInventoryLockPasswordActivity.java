package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
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
public class ForgetInventoryLockPasswordActivity extends BaseActivity implements android.os.Handler.Callback{
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.phone_num_et)
    private EditText phone_num_et;
    @ViewInject(R.id.code_et)
    private EditText code_et;
    //新密码
    @ViewInject(R.id.new_pw_et)
    private EditText new_pw_et;

    @ViewInject(R.id.response_tv)
    private TextView response_tv;
    @ViewInject(R.id.load_progress)
    private ProgressBar load_progress;
    @ViewInject(R.id.next_bt)
    private Button next_bt;
    @ViewInject(R.id.get_code_bt)
    private Button get_code_bt;

    Dialog waitDialog=null;

    //是否倒计时完成
    boolean isFinishedCountDown = true;
    MyCountDownTimer countDownTimer=null;

    Handler handler = new Handler(this);
    private static final int UPDATE_UI = 1001;
    private static final int COUNT_DOWN_FINISHED = 1002;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_forget_inventory_lock_password);
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
        title_tv.setText("忘记库存锁密码");

        if (MyApplication.getInstance().getUserInfo().getPhone() != null) {
            phone_num_et.setText(MyApplication.getInstance().getUserInfo().getPhone());
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == UPDATE_UI) {
            String str = (String) msg.obj;
            get_code_bt.setText(msg.arg1 + "秒");
        }else if(msg.what==COUNT_DOWN_FINISHED)
        {
            //倒计时完成
            isFinishedCountDown = true;
            get_code_bt.setText("重新获取");

            if(countDownTimer!=null)
            {
                countDownTimer.cancel();
            }
        }
        return false;
    }

    public void getCode(final View view) {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎网络不给力");
            return;
        }

        if (!isFinishedCountDown) {
            return;
        }

        String phoneNum = phone_num_et.getText().toString();
        if (phoneNum.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("手机号码不能为空!");
            return;
        }
        /*if (!PublicUtils.checkPhone(phoneNum.trim())) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("手机号码格式不正确!");
            return;
        }*/

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token",MyApplication.getInstance().getUserInfo().getToken());
            json.put("phone", phoneNum);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

       // next_bt.setVisibility(View.GONE);
        //load_progress.setVisibility(View.VISIBLE);
        waitDialog= PublicDialog.createLoadingDialog(this,"正在获取...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.SHOP_VERIFY_CODE, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "获取验证码=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    String text = jsonObject.getString("text");
                    int status = jsonObject.getIntValue("status");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(ForgetInventoryLockPasswordActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    } else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(ForgetInventoryLockPasswordActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                getCode(view);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        //get_code_bt.setText("重新获取");

                        //倒计时开始
                        isFinishedCountDown=false;
                        countDownTimer=new MyCountDownTimer(30000,1000);
                        countDownTimer.start();

                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);

                    }
                } catch (JSONException e) {
                }

                //next_bt.setVisibility(View.VISIBLE);
                // load_progress.setVisibility(View.GONE);
                waitDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText(s);
                // next_bt.setVisibility(View.VISIBLE);
                //load_progress.setVisibility(View.GONE);
                waitDialog.dismiss();
            }
        });

    }

    public void next(final View view) {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎!网络不给力");
            return;
        }

        String code = code_et.getText().toString();
        String phoneNum=phone_num_et.getText().toString();
        final String newPw=new_pw_et.getText().toString();
        if (code.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("验证码不能为空!");
            return;
        }
        if (phoneNum.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("手机号码不能为空!");
            return;
        }
        if (newPw.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("新密码不能为空!");
            return;
        }
        /*if (!PublicUtils.checkPhone(phoneNum.trim())) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("手机号码格式不正确!");
            return;
        }*/

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token",MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type",4);
            json.put("phone", phoneNum);
            json.put("verifycode",code);
            json.put("newpassword",newPw);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        next_bt.setVisibility(View.GONE);
        load_progress.setVisibility(View.VISIBLE);
        HttpUtil.doPostRequest(UrlsConstant.SHOP_REPERTORY_LOCK, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "pw_reset=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    String text = jsonObject.getString("text");
                    int status = jsonObject.getIntValue("status");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(serverMsg);
                        Intent intent = new Intent(ForgetInventoryLockPasswordActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(ForgetInventoryLockPasswordActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                next(view);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }
                    if (status == HttpConstant.SUCCESS_CODE)
                    {
                        ForgetInventoryLockPasswordActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                putString("repertory_pw",PublicUtils.md5(newPw)).
                                commit();
                        MyApplication.getInstance().getUserInfo().setRepertory_pw(PublicUtils.md5(newPw));
                        startActivity(new Intent(ForgetInventoryLockPasswordActivity.this,
                                InventoryLockPasswordActivity.class));
                        finish();

                    } else if (status == HttpConstant.FAILURE_CODE)
                    {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                next_bt.setVisibility(View.VISIBLE);
                load_progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText(s);
                next_bt.setVisibility(View.VISIBLE);
                load_progress.setVisibility(View.GONE);
            }
        });
    }
    public void back(View view) {
        finish();
    }

    /**
     * 倒计时
     */
    class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Message msg = handler.obtainMessage(UPDATE_UI);
            msg.arg1=(int)(millisUntilFinished/1000);
            handler.sendMessage(msg);
        }

        @Override
        public void onFinish() {
            handler.sendEmptyMessage(COUNT_DOWN_FINISHED);
        }
    }
}
