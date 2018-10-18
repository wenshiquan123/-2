package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

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
public class PostageSettingActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.response_tv)
    private TextView response_tv;
    @ViewInject(R.id.postage_tb)
    private ToggleButton postage_tb;

    @ViewInject(R.id.fare_setting_ll)
    private LinearLayout fare_setting_ll;
    //满元免运
    @ViewInject(R.id.fare_point_et)
    private EditText fare_point_et;
    //运费
    @ViewInject(R.id.fare_price_et)
    private EditText fare_price_et;

    //运费开关 2是关闭，1是开启
    private int mFlag = 0;

    Dialog waitDialog=null;
    SharedPreferences sp=null;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_postage_setting);
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
        title_tv.setText("邮费设置");
        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        if (MyApplication.getInstance().getUserInfo() != null) {
            if (MyApplication.getInstance().getUserInfo().getFare_off().equals("2")) {
                mFlag = 2;
                postage_tb.setChecked(false);
                fare_setting_ll.setVisibility(View.INVISIBLE);
            } else if (MyApplication.getInstance().getUserInfo().getFare_off().equals("1")) {
                mFlag = 1;
                postage_tb.setChecked(true);
                fare_setting_ll.setVisibility(View.VISIBLE);
            }

            fare_price_et.setText(MyApplication.getInstance().getUserInfo().getFare_price()+"");
            fare_point_et.setText(MyApplication.getInstance().getUserInfo().getFare_point()+"");
        }

        postage_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFlag = 1;
                    fare_setting_ll.setVisibility(View.VISIBLE);
                } else {
                    mFlag = 2;
                    fare_setting_ll.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    String fare_price;
    String fare_point;
    public void commit(View view) {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            return;
        }

        fare_price=fare_price_et.getText().toString();
        fare_point=fare_point_et.getText().toString();
        if(fare_price.trim().equals(""))
        {
            fare_price="0";
        }
        if(fare_point.trim().equals(""))
        {
            fare_point="0";
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type", mFlag);
            json.put("fare_point",fare_point);
            json.put("fare_price",fare_price);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        waitDialog= PublicDialog.createLoadingDialog(this,"正在提交...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.SHOP_POSTAGE, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "邮费=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")) {
                        Intent intent=new Intent(PostageSettingActivity.this,LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action",1);
                        startActivity(intent);
                        //finish();
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        PostageSettingActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                putString("fare_off", mFlag + "").
                                putString("fare_point", fare_point).
                                putString("fare_price",fare_price).
                                commit();
                        MyApplication.getInstance().getUserInfo().setFare_off(mFlag+"");
                        MyApplication.getInstance().getUserInfo().setFare_point(fare_point);
                        MyApplication.getInstance().getUserInfo().setFare_price(fare_price);
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
