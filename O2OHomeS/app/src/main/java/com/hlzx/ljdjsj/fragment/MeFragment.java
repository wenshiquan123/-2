package com.hlzx.ljdjsj.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.activity.AboutUsActivity;
import com.hlzx.ljdjsj.activity.BuyerEvaluateActivity;
import com.hlzx.ljdjsj.activity.CheckInventoryLockActivity;
import com.hlzx.ljdjsj.activity.GetCodeForShopInventoryLockActivity;
import com.hlzx.ljdjsj.activity.GoodSuggestionActivity;
import com.hlzx.ljdjsj.activity.GoodsManageActivity;
import com.hlzx.ljdjsj.activity.LoginActivity;
import com.hlzx.ljdjsj.activity.MaketingToolsActivity;
import com.hlzx.ljdjsj.activity.NoticeSettingActivity;
import com.hlzx.ljdjsj.activity.PostageSettingActivity;
import com.hlzx.ljdjsj.activity.PublishWorkActivity;
import com.hlzx.ljdjsj.activity.RegulationWebViewActivity;
import com.hlzx.ljdjsj.activity.ShopInventoryLockActivity;
import com.hlzx.ljdjsj.activity.ShopSettingActivity;
import com.hlzx.ljdjsj.activity.SuggestionFeedbackActivity;
import com.hlzx.ljdjsj.activity.UserAnalyzeActivity;
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
import com.hlzx.ljdjsj.view.CircleImageView;
import com.hlzx.ljdjsj.view.wheel.WheelView;
import com.hlzx.ljdjsj.view.wheel.adapter.ArrayWheelAdapter;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.squareup.picasso.Picasso;
import com.umeng.message.PushAgent;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


/**
 * Created by alan on 2015/12/8.
 */
public class MeFragment extends BaseFragment implements View.OnClickListener, android.os.Handler.Callback {

    //营业时间
    private RelativeLayout mWorkTime;
    //店铺设置
    private RelativeLayout mShopSetting;
    //商品库管理
    private RelativeLayout mGoodsManage;
    //营销工具
    private RelativeLayout mMarketingTools;
    //商品建议
    private RelativeLayout mGoodsSuggestion;
    //用户分析
    private RelativeLayout mUserAnalyz;
    //买家评价
    private RelativeLayout mBuyerEvaluate;
    //用户公告
    private RelativeLayout mUserNotice;
    //邮费设置
    private RelativeLayout mPostageSetting;
    //意见反馈
    private RelativeLayout mSuggustionFeedback;
    //关于我们
    private RelativeLayout mAboutUs;
    //便利店发布规则
    private RelativeLayout mRegulation;
    //退出
    private Button mLogout;

    private TextView mOpenTime;
    private TextView mCloseTime;
    //时间段
    String mStrOpenTime[] = {
            "00:00","00:30","01:00","01:30","02:00","02:30","03:00","03:30","04:00","04:30",
            "05:00","05:30", "06:00","06:30", "07:00","07:30", "08:00","08:30", "09:00","09:30","10:00","10:30",
            "11:00","11:30", "12:00","12:30","13:00","13:30", "14:00","14:30", "15:00","15:30",
            "16:00","16:30","17:00","17:30", "18:00","18:30", "19:00","19:30", "20:00","20:30", "21:00","21:30",
            "22:00","22:30", "23:00","23:30","23:59"
    };
    ArrayWheelAdapter<String> adapter1;
    ArrayWheelAdapter<String> adapter2;
    WheelView wheelView;
    WheelView wheelView1;
    private static int UPDATE_WORK_TIME = 1001;
    private static int CLEAR_ALLIAS= 1002;//清除别名

    Handler handler = new Handler(this);

    //店铺logo
    private CircleImageView mShopIcon;
    //店铺名称
    private TextView mShopName;

    private Dialog waitDialog = null;
    private Dialog dialog = null;

    //是否已经加载了一次
    boolean isFirstLoaded = false;


    PushAgent mPushAgent=null;
    //别名
    String alias =MyApplication.getInstance().getUserInfo().getSeller_id();
    String aliasType ="kUMessageAliasTypeFacebook";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_me, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPushAgent=PushAgent.getInstance(getActivity());

        mWorkTime = (RelativeLayout) getView().findViewById(R.id.work_time_rl);
        mWorkTime.setOnClickListener(this);
        mOpenTime = (TextView) getView().findViewById(R.id.open_time_tv);
        mCloseTime = (TextView) getView().findViewById(R.id.close_time_tv);
        mShopSetting = (RelativeLayout) getView().findViewById(R.id.shop_setting_rl);
        mShopSetting.setOnClickListener(this);
        mGoodsManage = (RelativeLayout) getView().findViewById(R.id.good_manage_rl);
        mGoodsManage.setOnClickListener(this);
        mMarketingTools = (RelativeLayout) getView().findViewById(R.id.marketing_tools_rl);
        mMarketingTools.setOnClickListener(this);
        mGoodsSuggestion = (RelativeLayout) getView().findViewById(R.id.goods_suggestion_rl);
        mGoodsSuggestion.setOnClickListener(this);
        mUserAnalyz = (RelativeLayout) getView().findViewById(R.id.user_analye_rl);
        mUserAnalyz.setOnClickListener(this);
        mBuyerEvaluate= (RelativeLayout) getView().findViewById(R.id.buyer_evaluate_rl);
        mBuyerEvaluate.setOnClickListener(this);
        mUserNotice = (RelativeLayout) getView().findViewById(R.id.notice_rl);
        mUserNotice.setOnClickListener(this);
        mPostageSetting = (RelativeLayout) getView().findViewById(R.id.postage_setting_rl);
        mPostageSetting.setOnClickListener(this);
        mSuggustionFeedback = (RelativeLayout) getView().findViewById(R.id.suggest_feedback_rl);
        mSuggustionFeedback.setOnClickListener(this);
        mAboutUs = (RelativeLayout) getView().findViewById(R.id.about_us_rl);
        mAboutUs.setOnClickListener(this);
        mRegulation = (RelativeLayout) getView().findViewById(R.id.regulation_rl);
        mRegulation.setOnClickListener(this);
        mLogout = (Button) getView().findViewById(R.id.logout_bt);
        mLogout.setOnClickListener(this);

        mShopIcon = (CircleImageView) getView().findViewById(R.id.shop_icon_iv);
        mShopName = (TextView) getView().findViewById(R.id.shop_name_tv);
        adapter1 = new ArrayWheelAdapter<String>(getActivity(), mStrOpenTime);
        adapter2 = new ArrayWheelAdapter<String>(getActivity(), mStrOpenTime);
        initView();
        isFirstLoaded = true;

    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        LogUtil.e("ME","pic="+MyApplication.getInstance().getUserInfo().getLogo());
        if (MyApplication.getInstance().getUserInfo() != null) {
            if (MyApplication.getInstance().getUserInfo().getLogo() != null
                    && !MyApplication.getInstance().getUserInfo().getLogo().equals(""))
                Picasso.with(getActivity()).load(MyApplication.getInstance().getUserInfo().getLogo())
                        .resize(100,100).centerCrop()
                        .placeholder(R.mipmap.tu)
                        .error(R.mipmap.tu)
                        .into(mShopIcon);
        }
        UserInfo userInfo = MyApplication.getInstance().getUserInfo();
        if (userInfo != null && !userInfo.getToken().equals("")) {
            //设置店铺名称
            mShopName.setText(userInfo.getShop_name());
            String startTime = userInfo.getStartTime();
            String endTime = userInfo.getEndTime();
            long tempStartTime = Long.parseLong(startTime);
            long tempEndTime = Long.parseLong(endTime);
            long startHour = tempStartTime / 60;//时
            long startMinute = tempStartTime % 60 ;//分

            long endHour = tempEndTime / 60;//时
            long endMinute = tempEndTime % 60;//分

            String strStart = "";
            String strEnd = "";
            if (startHour < 10) {
                if (startMinute < 10) {
                    strStart = "0" + startHour + ":" + "0" + startMinute;
                } else {
                    strStart = "0" + startHour + ":" + startMinute;
                }
            } else {
                if (startMinute < 10) {
                    strStart = startHour + ":" + "0" + startMinute;
                } else {
                    strStart = startHour + ":" + startMinute;
                }
            }

            if (endHour < 10) {
                if (endMinute < 10) {
                    strEnd = "0" + endHour + ":" + "0" + endMinute;
                } else {
                    strEnd = "0" + endHour + ":" + endMinute;
                }
            } else {
                if (endMinute < 10) {
                    strEnd = endHour + ":" + "0" + endMinute;
                } else {
                    strEnd = endHour + ":" + endMinute;
                }
            }
            mOpenTime.setText(strStart);
            mCloseTime.setText(strEnd);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == UPDATE_WORK_TIME) {
            /*if (msg.arg1 >= msg.arg2) {
                Toast.makeText(getActivity(), "时间选择不合理", Toast.LENGTH_SHORT).show();
                return false;
            }*/
            mOpenTime.setText(mStrOpenTime[msg.arg1]);
            mCloseTime.setText(mStrOpenTime[msg.arg2]);

            updateLoadTime(mStrOpenTime[msg.arg1], mStrOpenTime[msg.arg2]);

        }else if(msg.what==CLEAR_ALLIAS)
        {
               new ClearAliasTask(alias,aliasType).execute();
        }
        return false;
    }

    long openTime = 0;
    long closeTime = 0;

    /**
     * 提交时间到服务器
     *
     * @param
     */
    private void updateLoadTime(final String startTime, final String endTime) {

        if(!NetWorkUtils.isNetworkAvailable(getContext()))
        {
           showToast("哎！网络不给力");
            return;
        }

        openTime = 0;
        closeTime = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        try {
            openTime = (dateFormat.parse(startTime).getTime() + TimeZone.getDefault().getRawOffset()) / 1000/60;
            closeTime = (dateFormat.parse(endTime).getTime() + TimeZone.getDefault().getRawOffset()) / 1000/60;
            LogUtil.e("ME", "开始营业时间=" + openTime + "分");
            LogUtil.e("ME", "打烊时间=" + closeTime + "分");
        } catch (Exception e) {
        }

        /*if (openTime == 0 || closeTime == 0) {
            return;
        }*/
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("starttime", openTime);
            json.put("endtime", closeTime);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitDialog = PublicDialog.createLoadingDialog(getActivity(), "正在提交...");
        waitDialog.dismiss();
        HttpUtil.doPostRequest(UrlsConstant.SHOP_OPENING, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "result=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");

                   //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
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
                                  updateLoadTime(startTime,endTime);
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
                            getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                    putString("starttime", openTime + "").
                                    putString("endtime",closeTime + "").
                                    commit();
                            MyApplication.getInstance().getUserInfo().setStartTime(openTime + "");
                            MyApplication.getInstance().getUserInfo().setEndTime(closeTime + "");
                        }
                        showToast(text);
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                    }
                } catch (JSONException e) {
                }
                waitDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                waitDialog.dismiss();
                showToast(s);
            }
        });


    }

    @Override
    public void onClick(View v) {
        if (v == mWorkTime) {
            ShowSelectTimeDialog();
        } else if (v == mShopSetting) {
            startActivity(new Intent(getActivity(), ShopSettingActivity.class));
        } else if (v == mGoodsManage) {

            /**
             * 进入商品管理跟营销工具的时候，如果开启库存锁，要验证密码
             */
            if (MyApplication.getInstance().getUserInfo() != null) {
                if (MyApplication.getInstance().getUserInfo().getRepertory_pw() == null ||
                        MyApplication.getInstance().getUserInfo().getRepertory_pw().equals("")) {

                    startActivity(new Intent(getActivity(), GoodsManageActivity.class));

                } else {

                    if(MyApplication.getInstance().getUserInfo().getRepertory_lock()==0)//关
                    {
                        startActivity(new Intent(getActivity(), GoodsManageActivity.class));

                    }else if(MyApplication.getInstance().getUserInfo().getRepertory_lock()==1)//开
                    {
                          //如果开启了库存锁就验证库存锁密码
                        Intent intent=new Intent(getActivity(), CheckInventoryLockActivity.class);
                        intent.putExtra("action_type",1);
                        startActivity(intent);
                    }

                }
            }


        } else if (v == mMarketingTools) {


            if (MyApplication.getInstance().getUserInfo() != null) {
                if (MyApplication.getInstance().getUserInfo().getRepertory_pw() == null ||
                        MyApplication.getInstance().getUserInfo().getRepertory_pw().equals("")) {

                    startActivity(new Intent(getActivity(), MaketingToolsActivity.class));
                    //

                } else {

                    if(MyApplication.getInstance().getUserInfo().getRepertory_lock()==0)//关
                    {
                        startActivity(new Intent(getActivity(), MaketingToolsActivity.class));

                    }else if(MyApplication.getInstance().getUserInfo().getRepertory_lock()==1)//开
                    {
                        //如果开启了库存锁就验证库存锁密码
                        Intent intent=new Intent(getActivity(), CheckInventoryLockActivity.class);
                        intent.putExtra("action_type",2);
                        startActivity(intent);
                    }

                }
            }

        } else if (v == mGoodsSuggestion) {
            startActivity(new Intent(getActivity(), GoodSuggestionActivity.class));
        } else if (v == mUserAnalyz) {
            startActivity(new Intent(getActivity(), UserAnalyzeActivity.class));
        } else if (v == mBuyerEvaluate) {
            startActivity(new Intent(getActivity(), BuyerEvaluateActivity.class));
        } else if (v == mUserNotice) {
            startActivity(new Intent(getActivity(), NoticeSettingActivity.class));
        } else if (v == mPostageSetting) {
            startActivity(new Intent(getActivity(), PostageSettingActivity.class));
        } else if (v == mSuggustionFeedback) {
            startActivity(new Intent(getActivity(), SuggestionFeedbackActivity.class));
        } else if (v == mAboutUs) {
            startActivity(new Intent(getActivity(), AboutUsActivity.class));
        } else if (v == mRegulation) {
            startActivity(new Intent(getActivity(), RegulationWebViewActivity.class));
           /* Intent intent=new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra("title","商品信息发布规则");
            intent.putExtra("url",UrlsConstant.GOODS_RELEASE_RULE);
            startActivity(intent);*/
        } else if (v == mLogout) {
            logout();
        }
    }

    /*
     * 退出登录
     */
    private void logout() {

        //清空token
        MyApplication.getInstance().getUserInfo().setToken("");
        getActivity().getSharedPreferences("userInfo",
                Context.MODE_PRIVATE).edit().
                putString("token","").commit();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();

        if(!NetWorkUtils.isNetworkAvailable(getContext()))
        {
            showToast("哎！网络不给力");
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "long_IV=" + ClientEncryptionPolicy.getInstance().getIV());
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("ME", e.toString());
        }

        LogUtil.e("ME", "session_id=" + MyApplication.getSession_id());
        LogUtil.e("ME", "提交的数据=" + map.toString());

        //dialog = PublicDialog.createLoadingDialog(getActivity(), "正在退出登录...");
        //dialog.show();

        HttpUtil.doPostRequest(UrlsConstant.LOGOUT, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo)
            {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "logout=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");

                    //判断是否在别处登录
                    /*String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent=new Intent(getActivity(),LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action",0);
                        startActivity(intent);
                        //getActivity().finish();
                        return;

                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(getActivity(), new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                  logout();
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }*/

                    if (status == HttpConstant.SUCCESS_CODE) {
                        //清空token
                       /* MyApplication.getInstance().getUserInfo().setToken("");
                        //跳转到登录页
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();

                        handler.sendEmptyMessage(CLEAR_ALLIAS);

                        //清空token
                        //MyApplication.getInstance().getUserInfo().setToken("");
                        getActivity().getSharedPreferences("userInfo",
                                Context.MODE_PRIVATE).edit().
                                putString("token","").commit();*/


                    } else if (status == HttpConstant.FAILURE_CODE)
                    {
                        //跳转到登录页
                        /*startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                        getActivity().getSharedPreferences("userInfo",
                                Context.MODE_PRIVATE).edit().
                                putString("token","").commit();
                        //showToast(text);*/
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                    //showToast(e.toString());
                }
                //if (dialog != null)
                    //dialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                //if (dialog != null)
                    //dialog.dismiss();
                //showToast(s);
            }
        });
    }

    /**
     * 选择时间dialog
     */

    public AlertDialog ShowSelectTimeDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_timeselected);
        LinearLayout cancel_ll = (LinearLayout) window.findViewById(R.id.cancel_ll);
        cancel_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        LinearLayout confirm_ll = (LinearLayout) window.findViewById(R.id.confirm_ll);
        confirm_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //Toast.makeText(getActivity(),mStrOpenTime[wheelView.getCurrentItem()],Toast.LENGTH_SHORT).show();
                Message message = handler.obtainMessage(UPDATE_WORK_TIME);
                message.arg1 = wheelView.getCurrentItem();
                message.arg2 = wheelView1.getCurrentItem();
                handler.sendMessage(message);
            }
        });

        int OIndex=0;
        for(int i=0;i<mStrOpenTime.length;i++)
        {
            if(mOpenTime.getText().toString().equals(mStrOpenTime[i]))
            {
                OIndex=i;
            }
        }
        wheelView = (WheelView) window.findViewById(R.id.open_time_wv);
        wheelView.setWheelBackground(R.color.white);
        wheelView.setViewAdapter(adapter1);
        wheelView.setCurrentItem(OIndex);

        int CIndex=0;
        for(int i=0;i<mStrOpenTime.length;i++)
        {
            if(mCloseTime.getText().toString().equals(mStrOpenTime[i]))
            {
                CIndex=i;
            }
        }
        wheelView1 = (WheelView) window.findViewById(R.id.close_time_wv);
        wheelView1.setWheelBackground(R.color.white);
        wheelView1.setViewAdapter(adapter2);
        wheelView1.setCurrentItem(CIndex);
        return dialog;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (isFirstLoaded) {
                initView();
            }

        }
    }

    class ClearAliasTask extends AsyncTask<Void, Void, Boolean> {

        String exclusiveAlias;
        String aliasType;

        public ClearAliasTask(String aliasString,String aliasTypeString) {
            // TODO Auto-generated constructor stub
            this.exclusiveAlias = aliasString;
            this.aliasType = aliasTypeString;
        }

        protected Boolean doInBackground(Void... params) {
            try {
                return mPushAgent.removeAlias(alias, aliasType);
                //return mPushAgent.addExclusiveAlias(exclusiveAlias, aliasType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (Boolean.TRUE.equals(result))
                LogUtil.e("ME", "clear alias was set successfully.");
        }
    }

}
