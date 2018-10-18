package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MainActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.UserInfo;
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
import com.umeng.message.PushAgent;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener,android.os.Handler.Callback{

    @ViewInject(R.id.user_password_et)
    private EditText user_password_et;

    @ViewInject(R.id.user_name_et)
    private EditText user_name_et;

    @ViewInject(R.id.forget_password_tv)
    private TextView forget_password_tv;
    //登录按钮
    @ViewInject(R.id.login_bt)
    private Button login_bt;

    //登录服务器返回的提示
    @ViewInject(R.id.login_response_tv)
    private TextView login_response_tv;
    //登录加载帧动画
    @ViewInject(R.id.load_progress)
    private ProgressBar load_progress;

    boolean isShowPassword = false;

    Dialog waitDialog = null;
    SharedPreferences.Editor editor = null;
    SharedPreferences.Editor editorUserInfo = null;

    //action 1为跳到主页,默认为跳转主页
    private int action=1;

    private static final int SET_ALLIAS=101;
    Handler handler=new Handler(this);



    PushAgent mPushAgent=null;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_login);
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

        mPushAgent=PushAgent.getInstance(this);
        action=this.getIntent().getIntExtra("action",1);
        editorUserInfo = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();
        forget_password_tv.setOnClickListener(this);
        editor = this.getSharedPreferences("loginInfo", Context.MODE_PRIVATE).edit();
        user_name_et.setText(getSharedPreferences("loginInfo", Context.MODE_PRIVATE).getString("username", ""));
    }

    @Override
    public void onClick(View v) {
        if (v == forget_password_tv) {
            startActivity(new Intent(this, ForgetLoginPasswordActivity.class));
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what==SET_ALLIAS)//设置别名
        {
            //如果已经注册，就设置别名
            if(mPushAgent.isRegistered()) {
                addAlias();
            }
        }
        return false;
    }

    //登录
    public void login(final View view) {

        //判断是否有网络
        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            return;
        }
        final String username = user_name_et.getText().toString();
        String password = user_password_et.getText().toString();
        if (username.trim().equals("")) {
            login_response_tv.setVisibility(View.VISIBLE);
            login_response_tv.setText("用户名不能为空");
            return;
        }
        if (password.trim().equals("")) {
            login_response_tv.setVisibility(View.VISIBLE);
            login_response_tv.setText("密码不能为空");
            return;
        }
        if (MyApplication.getSession_id() == null) {
            login_response_tv.setVisibility(View.VISIBLE);
            login_response_tv.setText("非法登录");
            return;
        }
        //封装session
        Map<String, String> map =new LinkedHashMap<>();
        JSONObject json = new JSONObject();
        try {
            json.put("target_api", UrlsConstant.LOGIN);
            json.put("username", username);
            json.put("password", password);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json),"utf-8");
            map.put("data",edata1);
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());

            //showToast(map.toString());
            LogUtil.e("ME", "login_IV=" + ClientEncryptionPolicy.getInstance().getIV());
            LogUtil.e("ME", "session_id=" + MyApplication.getSession_id());
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //显示加载框
        //load_progress.setVisibility(View.VISIBLE);
        //login_bt.setVisibility(View.GONE);
        waitDialog = PublicDialog.createLoadingDialog(this, "正在登录...");
        waitDialog.show();
        user_name_et.setEnabled(false);
        user_password_et.setEnabled(false);

        HttpUtil.doPostRequest(UrlsConstant.LOGIN, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "登录返回的数据:=" + result);
                System.out.println("登录返回的数据:=" + result);
                //login_response_tv.setText(result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");
                    String msg=jsonObject.getString("msg");
                    if(msg.equals("10012"))
                    {    //重新握手
                       PublicUtils.handshake(LoginActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str)
                            {
                                    login(view);
                            }

                            @Override
                            public void onFalied(String str) {
                                    showToast(str);
                            }
                        });
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        //保存登录账号
                        editor.putString("username", username);
                        editor.commit();
                        String iv = jsonObject.getString("iv");
                        try {
                            String deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                            LogUtil.e("ME", "解密数据=" + deData);
                           /*
                            * wenshiquan
                            * 1.登录成功之后 保存用户信息，以后请求都需要token
                            */
                            com.alibaba.fastjson.JSONObject jsonObject1 = JSON.parseObject(deData);
                            UserInfo userInfo =new UserInfo();
                            userInfo.setToken(jsonObject1.getString("token"));
                            userInfo.setUsername(jsonObject1.getString("username"));
                            userInfo.setAddress(jsonObject1.getString("address"));
                            userInfo.setCtime(jsonObject1.getString("ctime"));
                            userInfo.setLast_login_ip(jsonObject1.getString("last_login_ip"));
                            userInfo.setLast_login_time(jsonObject1.getString("last_login_time"));
                            userInfo.setStatus(jsonObject1.getString("status"));
                            userInfo.setStartTime(jsonObject1.getString("starttime"));
                            userInfo.setEndTime(jsonObject1.getString("endtime"));
                            userInfo.setAudit_summary(jsonObject1.getString("audit_summary"));
                            userInfo.setAudit_time(jsonObject1.getString("audit_time"));
                            userInfo.setNotice(jsonObject1.getString("notice"));
                            userInfo.setFare_off(jsonObject1.getString("fare_off"));
                            userInfo.setFare_point(jsonObject1.getString("fare_point"));
                            userInfo.setFare_price(jsonObject1.getString("fare_price"));
                            userInfo.setLogo(UrlsConstant.BASE_PIC_URL+jsonObject1.getString("logo"));
                            userInfo.setContacts(jsonObject1.getString("contacts"));
                            userInfo.setTelphone(jsonObject1.getString("telphone"));
                            userInfo.setRepertory_lock(jsonObject1.getIntValue("repertory_lock"));
                            userInfo.setRepertory_pw(jsonObject1.getString("repertory_pw"));
                            userInfo.setPhone(jsonObject1.getString("phone"));
                            userInfo.setShop_name(jsonObject1.getString("shop_name"));
                            userInfo.setSeller_id(jsonObject1.getString("seller_id"));
                            MyApplication.getInstance().setUserInfo(userInfo);
                            //保存一份在SP
                            saveUserInfo(userInfo);
                            handler.sendEmptyMessage(SET_ALLIAS);
                        } catch (Exception e) {
                            login_response_tv.setText("解密异常");
                        }
                        if(action==1) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }else
                        {
                            finish();
                        }

                    } else if (status == HttpConstant.FAILURE_CODE)
                    {
                        login_response_tv.setVisibility(View.VISIBLE);
                        login_response_tv.setText(text);
                        //load_progress.setVisibility(View.GONE);
                        //login_bt.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    login_response_tv.setVisibility(View.VISIBLE);
                    login_response_tv.setText("服务器返回数据异常");
                    //load_progress.setVisibility(View.GONE);
                    //login_bt.setVisibility(View.VISIBLE);
                }
                user_name_et.setEnabled(true);
                user_password_et.setEnabled(true);
                waitDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                //load_progress.setVisibility(View.GONE);
                //login_bt.setVisibility(View.VISIBLE);
                login_response_tv.setVisibility(View.VISIBLE);
                login_response_tv.setText(s);
                user_name_et.setEnabled(true);
                user_password_et.setEnabled(true);
                waitDialog.dismiss();
            }
        });
    }

    private void saveUserInfo(UserInfo userInfo) {
        editorUserInfo.putString("token", userInfo.getToken());
        editorUserInfo.putString("username", userInfo.getUsername());
        editorUserInfo.putString("phone", userInfo.getPhone());
        editorUserInfo.putString("shop_name", userInfo.getShop_name());
        editorUserInfo.putString("ctime", userInfo.getCtime());
        editorUserInfo.putString("status", userInfo.getStatus());
        editorUserInfo.putString("starttime", userInfo.getStartTime());
        editorUserInfo.putString("endtime", userInfo.getEndTime());
        editorUserInfo.putString("audit_summary", userInfo.getAudit_summary());
        editorUserInfo.putString("audit_time", userInfo.getAudit_time());
        editorUserInfo.putString("notice", userInfo.getNotice());
        editorUserInfo.putString("fare_off", userInfo.getFare_off());
        editorUserInfo.putString("fare_point", userInfo.getFare_point());
        editorUserInfo.putString("fare_price", userInfo.getFare_price());
        editorUserInfo.putString("logo",userInfo.getLogo());
        //LogUtil.e("ME","保存的照片路径="+userInfo.getLogo());
        editorUserInfo.putString("contacts", userInfo.getContacts());
        editorUserInfo.putString("telphone", userInfo.getTelphone());
        editorUserInfo.putString("address", userInfo.getAddress());
        editorUserInfo.putInt("repertory_lock", userInfo.getRepertory_lock());
        editorUserInfo.putString("repertory_pw", userInfo.getRepertory_pw());
        editorUserInfo.putString("seller_id", userInfo.getSeller_id());
        /*try
        {
            editorUserInfo.putString("aesKey", ClientEncryptionPolicy.getInstance().getAesKey());
        }catch (Exception e){}*/
        editorUserInfo.commit();

    }

    public void back(View view) {
        finish();
    }

    public void temp(View view) {
        //startActivity(new Intent(this, MainActivity.class));
        String json="{\"status\":0,\"msg\":20004,\"wen\":{\"data\":\"132\",\"text\":\"哈哈哈哈哈\"}}";

        com.alibaba.fastjson.JSONObject object=JSON.parseObject(json);
        String data=object.getJSONObject("wen").toString();
        showToast(data);

    }

    //添加别名
    private void addAlias() {

        //别名
        String alias =MyApplication.getInstance().getUserInfo().getSeller_id();;
        String aliasType ="kUMessageAliasTypeFacebook";
        LogUtil.e("ME", "alisa=" + alias);
        if (TextUtils.isEmpty(alias))
        {
            showToast("请先输入Alias");
            return;
        }
        if (TextUtils.isEmpty(aliasType))
        {
            showToast("请先输入Alias Type");
            return;
        }
        if (!mPushAgent.isRegistered())
        {
            showToast("抱歉，还未注册");
            return;
        }
        new AddExclusiveAliasTask(alias,aliasType).execute();
    }

    class AddExclusiveAliasTask extends AsyncTask<Void, Void, Boolean> {

        String exclusiveAlias;
        String aliasType;

        public AddExclusiveAliasTask(String aliasString,String aliasTypeString) {
            // TODO Auto-generated constructor stub
            this.exclusiveAlias = aliasString;
            this.aliasType = aliasTypeString;
        }

        protected Boolean doInBackground(Void... params) {
            try {
                mPushAgent.removeAlias(exclusiveAlias, aliasType);
                return mPushAgent.addExclusiveAlias(exclusiveAlias, aliasType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (Boolean.TRUE.equals(result))
                LogUtil.e("ME", "exclusive alias was set successfully.");
        }
    }
}
