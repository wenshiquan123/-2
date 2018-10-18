package com.hlzx.ljdjsj.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.ArriveRecord;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.hlzx.ljdjsj.view.NoScrollForListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class ArriveRecordActivity extends BaseActivity implements
        android.os.Handler.Callback,
        AbsListView.OnScrollListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {


    @ViewInject(R.id.arrive_record_lv)
    private ListView arrive_record_lv;

    @ViewInject(R.id.load_linear_data)
    private RelativeLayout load_linear_data;
    @ViewInject(R.id.rela_no_data)
    private RelativeLayout rela_no_data;
    //未确认订单
    @ViewInject(R.id.umconfirm_order_ll)
    private LinearLayout umconfirm_order_ll;

    //未确认订单数/金额
    @ViewInject(R.id.umconfirm_order_money_tv)
    private TextView umconfirm_order_money_tv;

    ArrayList<ArriveRecord> records = new ArrayList<ArriveRecord>();
    Handler handler = new Handler(this);
    private static final int UPDATE = 1001;
    //每页显示条数
    private static final int shownum = 10;
    //当前页
    private static int mCurrPage = 1;
    //总条数
    private int mItemTotal = 0;

    boolean isLoading = false;//是否正在加载
    private boolean isLastRow = false;//判断是否最后一行
    private boolean hasMore = false;//是否有更多数据
    boolean isRefreshing = false;//是否正在刷新

    private TextView loadMore_tv;
    private ProgressBar loadProgress_pb;
    View moreView;
    ArriveRecordAdapter mAdapter;

    @ViewInject(R.id.refresh_srl)
    private SwipeRefreshLayout refresh_srl;

    //是否展开第一个
    boolean isExspand=false;


    @Override
    public void setLayout() {
        setContentView(R.layout.activity_arrive_record);
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

        moreView = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) moreView.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) moreView.findViewById(R.id.pb_load_progress);

        mAdapter = new ArriveRecordAdapter(this);
        arrive_record_lv.setFocusable(false);
        arrive_record_lv.setFocusableInTouchMode(false);
        arrive_record_lv.setOnScrollListener(this);

        moreView.setVisibility(View.GONE);
        arrive_record_lv.addFooterView(moreView);
        arrive_record_lv.setAdapter(mAdapter);

        umconfirm_order_ll.setOnClickListener(this);
        refresh_srl.setOnRefreshListener(this);
        loadData(1);
        isExspand=true;
    }

    @Override
    public void onClick(View v) {
        if (v == umconfirm_order_ll) {
            startActivity(new Intent(this, UnconfirmOrderActivity.class));
        }
    }

    @Override
    public void onRefresh() {

        if (isLoading) {
            refresh_srl.setRefreshing(false);
            return;
        }

        mCurrPage = 1;
        mItemTotal = 0;
        records.clear();
        isRefreshing = true;
        arrive_record_lv.setEnabled(false);
        loadData(mCurrPage);
        isExspand=true;

    }

    private void loadData(int currpage) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎!网络不给力");
            rela_no_data.setVisibility(View.VISIBLE);
            return;
        }

        //封装session
        final Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("currpage", currpage);
            json.put("shownum", shownum);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mCurrPage == 1) {
            load_linear_data.setVisibility(View.VISIBLE);
        }
        isLoading = true;

        HttpUtil.doPostRequest(UrlsConstant.INCOME_ACCOUNT, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "到账记录" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String text = jsonObject.getString("text");
                    String data = jsonObject.getString("data");
                    String iv = jsonObject.getString("iv");
                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(ArriveRecordActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        finish();

                        MyApplication.getInstance().getUserInfo().setToken("");
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(ArriveRecordActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                loadData(mCurrPage);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        if (data.equals("")) {
                            if (mCurrPage == 1) {
                                rela_no_data.setVisibility(View.VISIBLE);
                            } else {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadMore_tv.setText("没有更多数据了");
                                        loadProgress_pb.setVisibility(View.GONE);
                                        moreView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                            hasMore = false;
                        }
                        if (data != null) {
                            String deData = null;

                            deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                            LogUtil.e("ME", "解密数据=" + deData);
                            com.alibaba.fastjson.JSONObject jsonObject1 = JSON.parseObject(deData);
                            mCurrPage = jsonObject1.getIntValue("currpage");
                            mItemTotal = jsonObject1.getIntValue("itemsum");

                            String str = jsonObject1.getIntValue("unconfirmed") + "/" + jsonObject1.getString("unconfirmed_money");
                            Message msg = handler.obtainMessage(UPDATE);
                            msg.obj = str;
                            handler.sendMessage(msg);
                            JSONArray array = jsonObject1.getJSONArray("list");
                            if (array.size() > 0)
                            {
                                ArriveRecord record;
                                com.alibaba.fastjson.JSONObject object;
                                for (int i = 0; i < array.size(); i++) {
                                    object = array.getJSONObject(i);
                                    record = new ArriveRecord();
                                    record.setAccount(object.getString("account"));//今日确认到账金额
                                    record.setAccount_count(object.getString("account_count"));//今日确认到账订单
                                    record.setSales(object.getString("sales"));//今日营业额
                                    record.setOrder_count(object.getString("order_count"));//今日订单
                                    record.setDate(object.getString("date_str"));//到账日期
                                    record.setReward(object.getString("reward"));//订单奖励
                                    records.add(record);
                                    record = null;
                                    object = null;
                                }
                            } else {
                                if (mCurrPage == 1) {
                                    rela_no_data.setVisibility(View.VISIBLE);
                                } else {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadMore_tv.setText("没有更多数据了");
                                            loadProgress_pb.setVisibility(View.GONE);
                                            moreView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                                hasMore = false;
                            }
                        } else {
                            if (mCurrPage == 1) {
                                rela_no_data.setVisibility(View.VISIBLE);
                            } else {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadMore_tv.setText("没有更多数据了");
                                        loadProgress_pb.setVisibility(View.GONE);
                                        moreView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                            hasMore = false;
                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                        if (mCurrPage == 1) {
                            rela_no_data.setVisibility(View.VISIBLE);
                        } else {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    loadMore_tv.setText("没有更多数据了");
                                    loadProgress_pb.setVisibility(View.GONE);
                                    moreView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        hasMore = false;
                    }
                } catch (Exception e) {
                    if (mCurrPage == 1) {
                        rela_no_data.setVisibility(View.VISIBLE);
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                loadMore_tv.setText("没有更多数据了");
                                loadProgress_pb.setVisibility(View.GONE);
                                moreView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                LogUtil.e("ME", "total=" + mItemTotal);

                if (mAdapter.getCount() < mItemTotal) {
                    hasMore = true;
                } else {
                    hasMore = false;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            loadMore_tv.setText("没有更多数据了");
                            loadProgress_pb.setVisibility(View.GONE);
                            moreView.setVisibility(View.VISIBLE);
                        }
                    });
                }

                if (mItemTotal == 0) {
                    if (mCurrPage == 1) {
                        rela_no_data.setVisibility(View.VISIBLE);
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                loadMore_tv.setText("没有更多数据了");
                                loadProgress_pb.setVisibility(View.GONE);
                                moreView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else {
                    rela_no_data.setVisibility(View.GONE);
                }

                isLoading = false;
                load_linear_data.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                moreView.setVisibility(View.GONE);

                if (isRefreshing) {
                    refresh_srl.setRefreshing(false);
                    isRefreshing = false;
                    arrive_record_lv.setEnabled(true);
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                load_linear_data.setVisibility(View.GONE);
                showToast(s);

                if (mCurrPage == 1) {
                    rela_no_data.setVisibility(View.VISIBLE);
                } else {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            loadMore_tv.setText("没有更多数据了");
                            loadProgress_pb.setVisibility(View.GONE);
                            moreView.setVisibility(View.VISIBLE);
                        }
                    });
                }
                isLoading = false;

                if (isRefreshing) {
                    refresh_srl.setRefreshing(false);
                    isRefreshing = false;
                    arrive_record_lv.setEnabled(true);
                }

            }

        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)//判断是否滚动到底部
        {
            if (!isLoading && hasMore) {
                loadMore_tv.setText("加载更多数据");
                loadProgress_pb.setVisibility(View.VISIBLE);
                moreView.setVisibility(View.VISIBLE);

                isLoading = true;
                mCurrPage++;
                loadData(mCurrPage);
            }
            isLastRow = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (mAdapter == null) {
            return;
        }
        if (mAdapter.getCount() == 0) {
            return;
        }

        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0)//判断是否是最后一行
        {
            isLastRow = true;
        }

    }

    @Override
    public boolean handleMessage(Message msg)
    {
        if (msg.what == UPDATE) {
            umconfirm_order_money_tv.setText((String) msg.obj);
        }
        return false;
    }

    public void back(View view) {
        finish();
    }
    //奖励规定
    public void aword(View view) {
        startActivity(new Intent(this, AwordRuleActivity.class));
    }

    public class ArriveRecordAdapter extends BaseAdapter {
        Context mContext;

        public ArriveRecordAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return records.size();
        }

        @Override
        public Object getItem(int position) {
            return records.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(ArriveRecordActivity.this).inflate(R.layout.item_arrive_record, null);
                holder.expand_iv = (ImageView) convertView.findViewById(R.id.expand_iv);
                holder.subView_ll = (LinearLayout) convertView.findViewById(R.id.sub_view_ll);
                holder.expand_iv.setId(position);
                holder.relative1_rl = (RelativeLayout) convertView.findViewById(R.id.relative1_rl);
                holder.relative2_rl = (RelativeLayout) convertView.findViewById(R.id.relative2_rl);
                holder.relative1_rl.setId(position);
                holder.relative2_rl.setId(position);

                holder.order_arrived_tv = (TextView) convertView.findViewById(R.id.order_arrived_tv);
                holder.order_aword_tv = (TextView) convertView.findViewById(R.id.order_aword_tv);
                holder.today_order_and_sales_tv = (TextView) convertView.findViewById(R.id.today_order_and_sales_tv);
                holder.today_comfirm_order_and_sales_tv = (TextView) convertView.findViewById(R.id.today_comfirm_order_and_sales_tv);
                holder.date_tv = (TextView) convertView.findViewById(R.id.date_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.expand_iv.setId(position);
                holder.relative1_rl.setId(position);
                holder.relative2_rl.setId(position);
            }

            if (position == 0) {
                if(isExspand) {
                    records.get(position).setIsExpand(true);
                    isExspand=false;
                }
            }

            if (records.get(position).isExpand()) {
                holder.subView_ll.setVisibility(View.VISIBLE);
                holder.expand_iv.setImageResource(R.drawable.sq);
            } else {
                holder.subView_ll.setVisibility(View.GONE);
                holder.expand_iv.setImageResource(R.drawable.zk);
            }
            //LogUtil.e("ME", "vid=" + position);

            holder.expand_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    LogUtil.e("ME", "position=" + id);
                    if (records.get(id).isExpand()) {
                        records.get(id).setIsExpand(false);
                    } else {
                        records.get(id).setIsExpand(true);
                    }

                //LogUtil.e("ME","125464"+records.get(position).isExpand());
                notifyDataSetChanged();
            }
        }
        );

        holder.relative1_rl.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
            int position = v.getId();
            Intent intent = new Intent(ArriveRecordActivity.this, TodayOrderActivity.class);
            intent.putExtra("date", records.get(position).getDate());
            startActivity(intent);
        }
        }

        );

        holder.relative2_rl.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
            int position = v.getId();
            Intent intent = new Intent(ArriveRecordActivity.this, TodayConfirmOrderActivity.class);
            intent.putExtra("date", records.get(position).getDate());
            startActivity(intent);
        }
        }

        );

        if(records.size()>0)

        {
            holder.today_order_and_sales_tv.setText(records.get(position).getOrder_count() + "/" + records.get(position).getSales());
            holder.today_comfirm_order_and_sales_tv.setText(records.get(position).getAccount_count() + "/" + records.get(position).getAccount());
            holder.date_tv.setText(records.get(position).getDate());
            holder.order_arrived_tv.setText(records.get(position).getAccount());
            holder.order_aword_tv.setText(records.get(position).getReward());
        }

        return convertView;
    }

    class ViewHolder {
        LinearLayout subView_ll;
        ImageView expand_iv;

        RelativeLayout relative1_rl;
        RelativeLayout relative2_rl;

        TextView today_order_and_sales_tv;//今日确定订单数和金额
        TextView today_comfirm_order_and_sales_tv;//今日为确定订单数和金额
        TextView order_arrived_tv;//订单到账
        TextView order_aword_tv;//订单奖励
        TextView date_tv;//订单日期
    }
}
}
