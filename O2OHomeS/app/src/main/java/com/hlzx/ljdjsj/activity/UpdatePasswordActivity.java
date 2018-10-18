package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
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
public class UpdatePasswordActivity extends BaseActivity implements View.OnClickListener, android.os.Handler.Callback {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    //新密码
    @ViewInject(R.id.new_password_et)
    private EditText new_password_et;
    //重复密码
    @ViewInject(R.id.renew_password_et)
    private EditText renew_password_et;
    //手机号
    @ViewInject(R.id.phone_num_et)
    private EditText phone_num_et;
    //验证码
    @ViewInject(R.id.code_num_et)
    private EditText code_num_et;
    //获取验证码
    @ViewInject(R.id.get_code_bt)
    private Button get_code_bt;

    //提示
    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    Dialog dialog = null;

    //是否倒计时完成
    boolean isFinishedCountDown = true;
    MyCountDownTimer countDownTimer=null;

    Handler handler = new Handler(this);
    private static final int UPDATE_UI = 1001;
    private static final int COUNT_DOWN_FINISHED = 1002;
    @Override
    public void setLayout() {
        setContentView(R.layout.activity_update_password);
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
        title_tv.setText("密码修改");

        get_code_bt.setOnClickListener(this);
        if (MyApplication.getInstance().getUserInfo() != null) {
            phone_num_et.setText(MyApplication.getInstance().getUserInfo().getPhone());
        }
    }


    @Override
    public void onClick(View v) {
        if (v == get_code_bt) {
            getCode();
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

    /*
         *获取手机验证码
         */
    public void getCode() {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎网络不给力");
            return;
        }

        if (!isFinishedCountDown) {
            return;
        }

        String phone_num = phone_num_et.getText().toString();
        LogUtil.e("ME","phone="+phone_num);
        if (phone_num.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("手机号码不能为空");
            return;
        } else {
            /*if (!PublicUtils.checkPhone(phone_num)) {
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText("手机号码格式不正确");
                return;
            }*/
        }
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("phone", phone_num);
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
        }
        dialog = PublicDialog.createLoadingDialog(this, "正在获取验证码...");
        dialog.show();
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
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(UpdatePasswordActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(UpdatePasswordActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                getCode();
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }


                    if (status == HttpConstant.SUCCESS_CODE) {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);

                        //倒计时开始
                        isFinishedCountDown=false;
                        countDownTimer=new MyCountDownTimer(30000,1000);
                        countDownTimer.start();

                        //get_code_bt.setText("重新获取");
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (dialog != null)
                        dialog.dismiss();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText(s);
                if (dialog != null)
                    dialog.dismiss();
            }
        });
    }

    public void confirm(final View view) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        String new_pw = new_password_et.getText().toString();
        String renew_pw = renew_password_et.getText().toString();
        String code = code_num_et.getText().toString();
        if (new_pw.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("密码不能为空");
            return;
        }
        if (!renew_pw.trim().equals(new_pw.trim())) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("两次输入密码不一致");
            return;
        }
        if (code.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("验证码不能为空");
            return;
        }
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("phone", MyApplication.getInstance().getUserInfo().getTelphone());
            json.put("verifycode", code);
            json.put("password", new_pw);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog = PublicDialog.createLoadingDialog(this, "正在提交数据...");
        dialog.show();
        HttpUtil.doPostRequest(UrlsConstant.PASSWORD_RESET, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "result" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    String text = jsonObject.getString("text");
                    int status = jsonObject.getIntValue("status");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(UpdatePasswordActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(UpdatePasswordActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                confirm(view);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }


                    if (status == HttpConstant.SUCCESS_CODE) {
                        finish();
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText("修改密码失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText("请求失败");
                if (dialog != null) {
                    dialog.dismiss();
                }
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
