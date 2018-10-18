package com.hlzx.ljdjsj.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.activity.ArriveRecordActivity;
import com.hlzx.ljdjsj.activity.BusinessRankingActivity;
import com.hlzx.ljdjsj.activity.DepositRecordActivity;
import com.hlzx.ljdjsj.activity.DepositRuleActivity;
import com.hlzx.ljdjsj.activity.DepositRuleWebViewActivity;
import com.hlzx.ljdjsj.activity.LoginActivity;
import com.hlzx.ljdjsj.activity.PayoffRecordActivity;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.TimeUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/8.
 */
public class IncomeFragment extends BaseFragment implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, android.os.Handler.Callback {


    //营业排行
    private RelativeLayout mBusinessRanking;
    //提现规则
    private RelativeLayout mDepositRule;
    //提现记录
    private RelativeLayout mDepositRecord;
    //到账记录
    private RelativeLayout mArriveRecord;
    //盈利记录
    private RelativeLayout mPayoffRecord;

    //加载数据
    private RelativeLayout linear_load_data;
    //没有数据
    private RelativeLayout rela_no_data;

    //是否加载数据
    private boolean isLoad = false;
    //是否是下拉刷新
    //private boolean isRefreshing = false;
   //SwipeRefreshLayout refreshLayout;

    //今日到账
    private TextView today_account_tv;
    //今日营业额
    private TextView today_sales_tv;
    //今日订单数
    private TextView today_order_tv;
    //本周销售额
    private TextView weekday_sales_tv;
    //账户总收入
    private TextView gross_tv;
    //账户余额
    private TextView remaining_tv;
    //最后更新时间
    private TextView lastUpdateTime;
    //判断是否第一次已加载
    private boolean isFirstLoaded=false;
    //提现
    private RelativeLayout mDeposit_rl;
    //排名
    private TextView ranking_tv;

    //城市
    private String city="";
    //所在城市排名
    private int city_ranking=0;
    //击败全国排名百分比
    private String country_ranking="0%";



    Handler handler = new Handler(this);
    private static int UPDATE = 1001;
    Dialog waitDialog=null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_income, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBusinessRanking = (RelativeLayout) getView().findViewById(R.id.business_ranking_list_rl);
        mBusinessRanking.setOnClickListener(this);
        mDepositRule = (RelativeLayout) getView().findViewById(R.id.deposit_rule_rl);
        mDepositRule.setOnClickListener(this);
        mDepositRecord = (RelativeLayout) getView().findViewById(R.id.deposit_record_rl);
        mDepositRecord.setOnClickListener(this);
        mArriveRecord = (RelativeLayout) getView().findViewById(R.id.arrive_record_rl);
        mArriveRecord.setOnClickListener(this);
        mPayoffRecord = (RelativeLayout) getView().findViewById(R.id.payoff_record_rl);
        mPayoffRecord.setOnClickListener(this);
        mDeposit_rl=(RelativeLayout)getView().findViewById(R.id.deposit_rl);
        mDeposit_rl.setOnClickListener(this);


        //refreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.income_srl);
        //refreshLayout.setOnRefreshListener(this);

        linear_load_data = (RelativeLayout) getView().findViewById(R.id.load_linear_data);
        rela_no_data = (RelativeLayout) getView().findViewById(R.id.rela_no_data);

        today_account_tv = (TextView) getView().findViewById(R.id.today_account_tv);
        today_sales_tv = (TextView) getView().findViewById(R.id.today_sales_tv);
        today_order_tv = (TextView) getView().findViewById(R.id.today_order_count_tv);
        weekday_sales_tv = (TextView) getView().findViewById(R.id.weekday_sales_tv);
        gross_tv = (TextView) getView().findViewById(R.id.gross_tv);
        remaining_tv = (TextView) getView().findViewById(R.id.remaining_tv);

        lastUpdateTime=(TextView)getView().findViewById(R.id.update_time);
        ranking_tv=(TextView)getView().findViewById(R.id.ranking_tv);

        if (isLoad) {
            loadData();
            isFirstLoaded=true;
        }

    }

    @Override
      public void onRefresh() {
        //isRefreshing = true;
        //loadData();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (UPDATE == msg.what) {
            String deData = (String) msg.obj;
            try {
                com.alibaba.fastjson.JSONObject object = JSON.parseObject(deData);
                today_account_tv.setText(object.getString("today_account"));
                today_sales_tv.setText(object.getString("today_sales")+"");
                today_order_tv.setText(object.getString("today_order_count"));
                weekday_sales_tv.setText(object.getString("tswk_sales"));
                gross_tv.setText(object.getString("gross"));
                remaining_tv.setText(object.getString("remaining"));
                //城市
                city=object.getString("city");
                //所在城市排名
                city_ranking=object.getIntValue("city_ranking");
                //击败全国排名百分比
                country_ranking=object.getString("country_ranking")+"%";

                String strRanking=getActivity().getResources().getString(R.string.ranking);
                String tempRanking=String.format(strRanking,city,city_ranking,country_ranking);
                ranking_tv.setText(tempRanking);
                lastUpdateTime.setText("最后更新时间"+ object.getString("update_time"));

            } catch (JSONException e) {

            }

            //最后更新时间
            //lastUpdateTime.setText("最后更新时间"+ TimeUtils.getCurTimeYMD());
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == mBusinessRanking) {
            Intent intent=new Intent(getActivity(), BusinessRankingActivity.class);
            intent.putExtra("city",city);
            intent.putExtra("city_ranking",city_ranking);
            intent.putExtra("country_ranking",country_ranking);
            intent.putExtra("update_time",lastUpdateTime.getText().toString());
            intent.putExtra("week_sale",weekday_sales_tv.getText().toString());
            startActivity(intent);
        } else if (v == mDepositRule) {
            startActivity(new Intent(getActivity(),DepositRuleWebViewActivity.class));
        } else if (v == mDepositRecord) {
            startActivity(new Intent(getActivity(), DepositRecordActivity.class));
        } else if (v == mArriveRecord) {
            startActivity(new Intent(getActivity(), ArriveRecordActivity.class));
        } else if (v == mPayoffRecord) {
            startActivity(new Intent(getActivity(), PayoffRecordActivity.class));
        }else if(v==mDeposit_rl)
        {
            deposit();
        }
    }
    /*
     *提现申请
     */
    private void deposit()
    {
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitDialog= PublicDialog.createLoadingDialog(getActivity(),"正在提交...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.INCOME_DEPOSIT, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "提现=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");
                    if (status == HttpConstant.SUCCESS_CODE) {

                        //刷新
                        loadData();
                        //showToast(text);
                    }else if(status==HttpConstant.FAILURE_CODE)
                    {
                        showToast(text);
                    }
                }catch (JSONException e){}
                waitDialog.dismiss();
            }
            @Override
            public void onFailure(HttpException e, String s) {
                showToast(s);
                waitDialog.dismiss();
            }
        });
    }


    private void loadData()
    {

        if(!NetWorkUtils.isNetworkAvailable(getContext()))
        {
            showToast( "哎！网络不给力");
            //rela_no_data.setVisibility(View.VISIBLE);
            return;
        }

        //if (!isRefreshing)
        //rela_no_data.setVisibility(View.GONE);
        //linear_load_data.setVisibility(View.VISIBLE);
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpUtil.doPostRequest(UrlsConstant.INCOME, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "income=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String text = jsonObject.getString("text");
                    String data = jsonObject.getString("data");
                    String iv = jsonObject.getString("iv");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {

                        //rela_no_data.setVisibility(View.VISIBLE);
                        //linear_load_data.setVisibility(View.GONE);

                        showToast(text);
                        Intent intent=new Intent(getActivity(),LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action",0);
                        startActivity(intent);
                        //getActivity().finish();

                        //清空token
                        MyApplication.getInstance().getUserInfo().setToken("");
                        return;

                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(getActivity(), new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                 loadData();
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        if (data != null && !data.equals("")) {
                            String deData = null;
                            try {
                                deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                                LogUtil.e("ME", "解密数据=" + deData);
                                Message msg = handler.obtainMessage(UPDATE);
                                msg.obj = deData;
                                handler.sendMessage(msg);
                            } catch (Exception e) {
                                LogUtil.e("ME", "解密数据异常" + deData);
                                //linear_load_data.setVisibility(View.GONE);
                                //rela_no_data.setVisibility(View.VISIBLE);
                            }

                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        LogUtil.e("ME", "解密数据异常");
                        //linear_load_data.setVisibility(View.GONE);
                        //rela_no_data.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    //linear_load_data.setVisibility(View.GONE);
                    //rela_no_data.setVisibility(View.VISIBLE);
                }
                //refreshLayout.setRefreshing(false);
                //isRefreshing=false;
                //linear_load_data.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                //refreshLayout.setRefreshing(false);
                //isRefreshing=false;
                //linear_load_data.setVisibility(View.GONE);
                //rela_no_data.setVisibility(View.VISIBLE);
                LogUtil.e("ME", "请求失败=" + s);
            }

        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //刷新
        loadData();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {

            isLoad = true;
            LogUtil.e("ME", "income可见");
            if(isFirstLoaded)
            {
                loadData();
            }
        } else {
            isLoad = false;
            LogUtil.e("ME", "income不可见");
        }
    }
}
