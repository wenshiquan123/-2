package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
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
public class RefuseOderActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.reason1_rb)
    private RadioButton reason1_rb;
    @ViewInject(R.id.reason2_rb)
    private RadioButton reason2_rb;
    @ViewInject(R.id.reason3_rb)
    private RadioButton reason3_rb;
    @ViewInject(R.id.reason4_rb)
    private RadioButton reason4_rb;
    @ViewInject(R.id.refuse_reason_et)
    private EditText refuse_reason_et;
    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    //拒绝理由类型
    private static int refuse_type=0;
    private static int order_id;//订单id
    private static int order_status;//订单状态
    //拒绝理由
    String[] strReason = {
            "非常抱歉，您的订单超出了我们的服务范围，我们将无法为你配送，请到首页切换到您所在小区在家点点小店下单，祝您购物愉快！",
            "非常抱歉，您的订单暂时缺货无法进行配送，给您带来的不便深感抱歉!",
            "非常抱歉，您的订单因金额过少无法进行配送，您可以增加订单商品金额重新下单。给你带来的不便请谅解！",
            "请输入您的拒单原因"
    };

    Dialog dialog;
    @Override
    public void setLayout() {
        setContentView(R.layout.activity_refuse_order);
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
        title_tv.setText("拒绝订单");

        order_id=getIntent().getIntExtra("order_id",0);
        order_status=getIntent().getIntExtra("order_status",0);
        LogUtil.e("ME","拒绝订单:oder_id="+order_id+";order_status="+order_status);


        reason1_rb.setOnCheckedChangeListener(changeListener);
        reason2_rb.setOnCheckedChangeListener(changeListener);
        reason3_rb.setOnCheckedChangeListener(changeListener);
        reason4_rb.setOnCheckedChangeListener(changeListener);
    }

    CompoundButton.OnCheckedChangeListener changeListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView instanceof RadioButton)
            {
                if(isChecked) {
                    if (buttonView.getId() == reason1_rb.getId()) {
                        refuse_type = 1;
                        refuse_reason_et.setEnabled(false);
                        refuse_reason_et.setText(strReason[0]);
                        reason2_rb.setChecked(false);
                        reason3_rb.setChecked(false);
                        reason4_rb.setChecked(false);
                        //reason1_rb.setChecked(true);

                    } else if (buttonView.getId() == reason2_rb.getId()) {
                        refuse_type = 2;
                        refuse_reason_et.setEnabled(false);
                        refuse_reason_et.setText(strReason[1]);

                        reason1_rb.setChecked(false);
                        reason3_rb.setChecked(false);
                        reason4_rb.setChecked(false);
                        //reason2_rb.setChecked(true);

                    } else if (buttonView.getId() == reason3_rb.getId()) {
                        refuse_type = 3;
                        refuse_reason_et.setEnabled(false);
                        refuse_reason_et.setText(strReason[2]);

                        reason2_rb.setChecked(false);
                        reason1_rb.setChecked(false);
                        reason4_rb.setChecked(false);
                        //reason3_rb.setChecked(true);
                    } else if (buttonView.getId() == reason4_rb.getId()) {
                        refuse_type = 4;
                        refuse_reason_et.setEnabled(true);
                        refuse_reason_et.requestFocus();
                        refuse_reason_et.setText("");
                        refuse_reason_et.setHint(strReason[3]);

                        reason2_rb.setChecked(false);
                        reason3_rb.setChecked(false);
                        reason1_rb.setChecked(false);
                        //reason4_rb.setChecked(true);
                    }
                }
            }
        }
    };

    //提交
    public void commit(final View view)
    {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            return;
        }

        String reason=refuse_reason_et.getText().toString();
        if(reason.trim().equals(""))
        {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("拒绝理由不能为空!");
            return;
        }
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("order_id",order_id);
            json.put("action_type",3);//处理理由类型：1.开始配送、2.我已送达、3.拒绝订单
            json.put("sefusing_type", refuse_type==0 ? 4:refuse_type);
            json.put("sefusing_str", reason);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog= PublicDialog.createLoadingDialog(this,"正在提交...");
        dialog.show();
        HttpUtil.doPostRequest(UrlsConstant.ORDER_ACTION, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "登录返回的数据:=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject= JSON.parseObject(result);
                    int status=jsonObject.getIntValue("status");
                    //String data=jsonObject.getString("data");
                    String text=jsonObject.getString("text");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent=new Intent(RefuseOderActivity.this,LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action",0);
                        startActivity(intent);
                        //getActivity().finish();

                        //清空token
                        MyApplication.getInstance().getUserInfo().setToken("");
                        return;

                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(RefuseOderActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str)
                            {
                                 commit(view);
                            }
                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if(status== HttpConstant.SUCCESS_CODE)
                    {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText("提交成功");
                        dialog.dismiss();
                        finish();
                    }else if(status== HttpConstant.FAILURE_CODE)
                    {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);
                       dialog.dismiss();
                    }
                }catch (JSONException e)
                {
                    response_tv.setVisibility(View.VISIBLE);
                    response_tv.setText("解析数据异常");
                    dialog.dismiss();
                }
            }
            @Override
            public void onFailure(HttpException e, String s) {
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText(s);
                dialog.dismiss();
            }
        });

    }
    public void back(View view) {
        finish();
    }

}
