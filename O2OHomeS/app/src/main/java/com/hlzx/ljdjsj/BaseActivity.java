package com.hlzx.ljdjsj;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.hlzx.ljdjsj.bean.UserInfo;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;

/**
 * Created by alan on 2015/12/10.
 */
public abstract class  BaseActivity extends Activity {


    SharedPreferences sp=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        sp=this.getSharedPreferences("userInfo",Context.MODE_PRIVATE);
    }

    public abstract void setLayout();
    public void initView(){}

    public void showToast(String msg)
    {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUserInfo(sp);
    }

    public void initUserInfo(SharedPreferences sp)
    {
        UserInfo userInfo=UserInfo.getInstance();
        userInfo.setToken(sp.getString("token", ""));
        userInfo.setUsername(sp.getString("username", ""));
        userInfo.setPhone(sp.getString("phone", ""));
        userInfo.setShop_name(sp.getString("shop_name", ""));
        userInfo.setCtime(sp.getString("ctime", ""));
        userInfo.setStatus(sp.getString("status", ""));
        userInfo.setStartTime(sp.getString("starttime", ""));
        userInfo.setEndTime(sp.getString("endtime", ""));
        userInfo.setAudit_summary(sp.getString("audit_summary", ""));
        userInfo.setAudit_time(sp.getString("audit_time", ""));
        userInfo.setNotice(sp.getString("notice", ""));
        userInfo.setFare_off(sp.getString("fare_off", ""));
        userInfo.setFare_price(sp.getString("fare_price", ""));
        userInfo.setFare_point(sp.getString("fare_point", ""));
        userInfo.setLogo(sp.getString("logo", ""));
        userInfo.setContacts(sp.getString("contacts", ""));
        userInfo.setTelphone(sp.getString("telphone", ""));
        userInfo.setAddress(sp.getString("address", ""));
        userInfo.setRepertory_lock(sp.getInt("repertory_lock", 0));
        userInfo.setRepertory_pw(sp.getString("repertory_pw", ""));
        userInfo.setSeller_id(sp.getString("seller_id", ""));
        MyApplication.getInstance().setSession_id(sp.getString("security_session_id", ""));
        MyApplication.getInstance().setAesKey(sp.getString("aesKey", ""));
         LogUtil.e("ME", "sp文件的session_id=" + sp.getString("security_session_id", ""));
       // LogUtil.e("ME", "照片路径=" + sp.getString("logo", ""));
        MyApplication.getInstance().setUserInfo(userInfo);
        /*try
        {
            ClientEncryptionPolicy.getInstance().setAesKey(sp.getString("aesKey",""));
        }catch (Exception e){}*/


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}
