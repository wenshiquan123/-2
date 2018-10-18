package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class StopWorkActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    //关闭营业
    @ViewInject(R.id.close_work_tb)
    private ToggleButton close_work_tb;
    //开业时间跟停业原因
    @ViewInject(R.id.stop_des_ll)
    private LinearLayout stop_des_ll;

    //开业时间
    @ViewInject(R.id.open_time_rl)
    private RelativeLayout open_time_rl;
    //暂停营业原因
    @ViewInject(R.id.stop_work_cause_rl)
    private RelativeLayout stop_work_cause_rl;

    //设置的开业时间
    @ViewInject(R.id.start_work_tv)
    private TextView start_work_tv;
    //设置的原因
    @ViewInject(R.id.stop_work_cause_et)
    private EditText stop_work_cause_et;

    //登录服务器返回的提示
    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    @ViewInject(R.id.line_v)
    private View line_v;

    //操作类型：1-暂停，2-关闭
    private int mflag = 0;
    Dialog dialog = null;
    Dialog myDialog = null;

    //已选时间
    private int mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute;


    @Override
    public void setLayout() {
        setContentView(R.layout.activity_stop_work);
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
        title_tv.setText("停止营业");

        if (MyApplication.getInstance().getUserInfo() != null) {
            LogUtil.e("ME", "status=" + MyApplication.getInstance().getUserInfo().getStatus());
            if (MyApplication.getInstance().getUserInfo().getStatus().equals("2")) {//暂停
                close_work_tb.setChecked(true);
                //显示原因时间
                start_work_tv.setText(MyApplication.getInstance().getUserInfo().getAudit_time());
                stop_work_cause_et.setText(MyApplication.getInstance().getUserInfo().getAudit_summary());
                stop_des_ll.setVisibility(View.VISIBLE);
                line_v.setVisibility(View.VISIBLE);
                mflag = 1;

            } else if (MyApplication.getInstance().getUserInfo().getStatus().equals("1")) {//关闭
                close_work_tb.setChecked(false);
                start_work_tv.setText(MyApplication.getInstance().getUserInfo().getAudit_time());
                stop_work_cause_et.setText(MyApplication.getInstance().getUserInfo().getAudit_summary());
                stop_des_ll.setVisibility(View.INVISIBLE);
                line_v.setVisibility(View.INVISIBLE);
                mflag = 2;
            }
        }

        close_work_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mflag = 1;//暂停
                    stop_des_ll.setVisibility(View.VISIBLE);
                } else {
                    mflag = 2;//关闭
                    stop_des_ll.setVisibility(View.INVISIBLE);
                }
            }
        });
        open_time_rl.setOnClickListener(this);
        stop_des_ll.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonthOfYear = calendar.get(Calendar.MONTH);
        mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
    }

    @Override
    public void onClick(View v) {
        if (v == open_time_rl) {
            showTimePicker();
        } else if (v == stop_work_cause_rl) {
            startActivityForResult(new Intent(this, StopWorkCauseActivity.class), 1001);
        }
    }

    private void showTimePicker() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null);
        DatePicker datePicker = (DatePicker) v.findViewById(R.id.dpPicker);
        TimePicker timePicker = (TimePicker) v.findViewById(R.id.tpPicker);
        LinearLayout cancel_ll = (LinearLayout) v.findViewById(R.id.cancel_ll);
        cancel_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myDialog != null)
                    myDialog.dismiss();
            }
        });
        LinearLayout comfire_ll = (LinearLayout) v.findViewById(R.id.confirm_ll);
        comfire_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myDialog != null) {
                    myDialog.dismiss();
                }

                String mStrMonth = "";
                if (mMonthOfYear < 10) {
                    mStrMonth = "0" + (mMonthOfYear + 1);
                } else {
                    mStrMonth = "" + (mMonthOfYear + 1);
                }
                String mStrDay = "";
                if (mDayOfMonth < 10) {
                    mStrDay = "0" + mDayOfMonth;
                } else {
                    mStrDay = "" + mDayOfMonth;
                }
                String mStrHour = "";
                if (mHourOfDay < 10) {
                    mStrHour = "0" + mHourOfDay;
                } else {
                    mStrHour = "" + mHourOfDay;
                }
                String mStrMinute = "";
                if (mMinute < 10) {
                    mStrMinute = "0" + mMinute;
                } else {
                    mStrMinute = "" + mMinute;
                }

                String strTime = mYear + "-" + mStrMonth + "-" + mStrDay + " " + mStrHour + ":" + mStrMinute + ":01";
                start_work_tv.setText(strTime);
            }
        });

        datePicker.init(mYear, mMonthOfYear, mDayOfMonth, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonthOfYear = monthOfYear;
                mDayOfMonth = dayOfMonth;
            }
        });
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(mHourOfDay);
        timePicker.setCurrentMinute(mMinute);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mHourOfDay = hourOfDay;
                mMinute = minute;
            }
        });

        myDialog = new Dialog(this, R.style.loading_dialog);
        myDialog.setContentView(v);
        myDialog.show();

    }

    //提交数据
    public void commit(final View view) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        if (mflag == 0) {
            return;
        }
        LogUtil.e("ME", "flag=" + mflag);
        String open_time = start_work_tv.getText().toString();
        String stop_cause = stop_work_cause_et.getText().toString();
        if (open_time.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("开业时间不能为空");
            return;
        }
        if (stop_cause.trim().equals("")) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("停业原因不能为空");
            return;
        }
        //把时间转换成秒
        long second = 0;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            second = dateFormat.parse(open_time).getTime() / 1000;
            LogUtil.e("ME", "秒=" + second);
        } catch (ParseException e) {
            second = 0;
        }
        if (second == 0) {
            response_tv.setVisibility(View.VISIBLE);
            response_tv.setText("时间转换失败");
            return;
        }

        if (mflag == 1) {
            long currentTime = System.currentTimeMillis();
            if (second * 1000 <= currentTime) {
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText("开业时间不能比现在早");
                return;
            }
        }
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("audit_type", mflag);
            json.put("audit_time", second);
            json.put("audit_summary", stop_cause);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog = PublicDialog.createLoadingDialog(this, "正在提交...");
        dialog.show();
        HttpUtil.doPostRequest(UrlsConstant.SHOP_STOP_WORK, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

                String result = responseInfo.result.toString();
                LogUtil.e("ME", "暂停营业=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")) {
                        showToast(text);
                        Intent intent = new Intent(StopWorkActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(StopWorkActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                commit(view);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {

                        if (MyApplication.getInstance().getUserInfo() != null) {

                            int shop_status = 0;
                            if (mflag == 1) {
                                shop_status = 2;
                            } else if (mflag == 2) {
                                shop_status = 1;
                            }
                            MyApplication.getInstance().getUserInfo().setStatus(shop_status + "");
                            MyApplication.getInstance().getUserInfo().setAudit_time(start_work_tv.getText().toString());
                            MyApplication.getInstance().getUserInfo().setAudit_summary(stop_work_cause_et.
                                    getText().toString());

                            StopWorkActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                    putString("status", shop_status + "").
                                    putString("audit_time", start_work_tv.getText().toString()).
                                    putString("audit_summary", stop_work_cause_et.getText().toString()).
                                    commit();


                            finish();
                        }

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        response_tv.setVisibility(View.VISIBLE);
                        response_tv.setText(text);
                    }

                } catch (JSONException e) {
                    response_tv.setVisibility(View.VISIBLE);
                    response_tv.setText(e.toString());
                }
                dialog.dismiss();

            }

            @Override
            public void onFailure(HttpException e, String s) {
                dialog.dismiss();
                response_tv.setVisibility(View.VISIBLE);
                response_tv.setText(s);
            }
        });

    }

    public void back(View view) {
        finish();
    }
}
