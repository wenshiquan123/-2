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
public class ShopMessageActivity extends BaseActivity {

    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    //联系人
    @ViewInject(R.id.name_et)
    private EditText name_et;
    //联系电话
    @ViewInject(R.id.phone_num_et)
    private EditText phone_num_et;
    //详细地址
    @ViewInject(R.id.address_et)
    private EditText address_et;

    //提示
    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    private Dialog waitDialog = null;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_shop_message);
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
        title_tv.setText("店铺信息");

        if (MyApplication.getInstance().getUserInfo() != null) {
            name_et.setText(MyApplication.getInstance().getUserInfo().getContacts());
            phone_num_et.setText(MyApplication.getInstance().getUserInfo().getTelphone());
            address_et.setText(MyApplication.getInstance().getUserInfo().getAddress());
        }
    }

    public void onSave(final View view) {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            return;
        }

        String contacts = name_et.getText().toString();
        String telphone = phone_num_et.getText().toString();
        String address = address_et.getText().toString();
        if (contacts.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("联系人不能为空");
            return;
        }
        if (telphone.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("联系电话不能为空");
            return;
        }
        if (address.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("联系地址不能为空");
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("contacts", contacts);
            json.put("telphone", telphone);
            json.put("address", address);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitDialog = PublicDialog.createLoadingDialog(this, "正在保存...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.SHOP_MESSAGE, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "店铺信息设置=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String text = jsonObject.getString("text");
                    String data = jsonObject.getString("data");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")) {
                        Intent intent=new Intent(ShopMessageActivity.this,LoginActivity.class);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        return;
                    }else if (serverMsg.equals("10012")) {    //重新握手
                        PublicUtils.handshake(ShopMessageActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                onSave(view);
                            }
                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        if(MyApplication.getInstance().getUserInfo()!=null)
                        {
                            MyApplication.getInstance().getUserInfo().setContacts(name_et.getText().toString());
                            MyApplication.getInstance().getUserInfo().setTelphone(phone_num_et.getText().toString());
                            MyApplication.getInstance().getUserInfo().setAddress(address_et.getText().toString());

                            ShopMessageActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                    putString("contacts",name_et.getText().toString()).
                                    putString("telphone",phone_num_et.getText().toString()).
                                    putString("address",address_et.getText().toString()).
                                    commit();

                        }
                        showToast(text);
                        finish();
                    } else if (status == HttpConstant.FAILURE_CODE)
                    {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);
                    }
                } catch (JSONException e) {
                    response_tv.setVisibility(View.VISIBLE);
                    response_tv.setText(e.toString());
                }
                waitDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText(s);
                waitDialog.dismiss();
            }
        });

    }


    public void back(View view) {

        finish();
    }
}
