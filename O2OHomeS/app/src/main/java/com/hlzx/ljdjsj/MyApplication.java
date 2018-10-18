package com.hlzx.ljdjsj;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hlzx.ljdjsj.bean.UserInfo;
import com.hlzx.ljdjsj.service.MyPushIntentService;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.lidroid.xutils.HttpUtils;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;
import com.umeng.update.UmengUpdateAgent;

/**
 * 自定义的Application，做一些全局初始化操作
 */
public class MyApplication extends Application {
    private static MyApplication mApplication;
    private HttpUtils mHttpUtils;

    public static SharedPreferences sp;
    public static String session_id;//握手成功后保存下来
    public static String aesKey;//握手成功后保存下来
    public static MyApplication getInstance() {
        if(mApplication==null)
        {
            mApplication=new MyApplication();
        }
        return mApplication;
    }
    public static UserInfo userInfo;

    PushAgent mPushAgent=null;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        // 初始化XUtils
        initXUtils();
        AbnormalHandler crashHandler=AbnormalHandler.getInstance();
        crashHandler.init(getApplicationContext());
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        //PublicUtils.handshake(this);

        //开启推送
        mPushAgent= PushAgent.getInstance(this);
        mPushAgent.enable();
        String device_token= UmengRegistrar.getRegistrationId(this);
        //LogUtil.e("ME", "device_token=" + device_token);
        mPushAgent.setPushIntentServiceClass(MyPushIntentService.class);


    }


    /**
     * 初始化XUtils
     */
    private void initXUtils() {
        mHttpUtils = new HttpUtils("utf-8");
        mHttpUtils.configRequestRetryCount(3);
        mHttpUtils.configSoTimeout(5000);
        mHttpUtils.configRequestThreadPoolSize(5);
        mHttpUtils.configResponseTextCharset("utf-8");
    }
    /**
     * @return MyApplication对象
     */
    public static MyApplication getApplication() {
        return mApplication;
    }

    /**
     * @return HttpUtils对象
     */
    public HttpUtils getHttpUtils() {
        return mHttpUtils;
    }
    public static String getSession_id() {
        //LogUtil.e("ME", "session_id=" + session_id);
        return session_id;
    }

    public static void setAesKey(String aesKey) {
        MyApplication.aesKey= aesKey;
    }

    public static String getAesKey() {
        return aesKey;
    }

    public static void setSession_id(String session_id) {
        MyApplication.session_id = session_id;
    }


    //保存用户登录信息
    public UserInfo getUserInfo(){
        return userInfo;
    }
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
