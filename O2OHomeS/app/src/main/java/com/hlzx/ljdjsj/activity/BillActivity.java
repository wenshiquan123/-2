package com.hlzx.ljdjsj.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
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
import com.hlzx.ljdjsj.bean.Bill;
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

import org.android.agoo.net.async.AsyncHttpClient;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 * 账单
 */
public class BillActivity extends BaseActivity implements AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    //账单列表
    @ViewInject(R.id.bill_lv)
    private ListView bill_lv;

    //是否是最后一行
    private boolean isLastRow = false;
    //是否正在刷新
    private boolean isRefreshing = false;
    //是否正在加载
    private boolean isLoading = false;
    //是否有更多数据
    private boolean hasMore = false;


    //当前页码
    private static int mCurrpage = 1;
    private static int mShownum =20;//每页显示的条数，默认是6
    private static int record_total = 0;//记录总数

    @ViewInject(R.id.bill_srl)
    SwipeRefreshLayout refreshLayout;

    BillAdapter mAdapter = null;

    ArrayList<Bill> bills = new ArrayList<Bill>();

    View moreView;
    TextView loadMore_tv;
    ProgressBar loadProgress_pb;
    @ViewInject(R.id.load_linear_data)
    private RelativeLayout load_linear_data;
    @ViewInject(R.id.rela_no_data)
    private RelativeLayout rela_no_data;


    @Override
    public void setLayout() {
        setContentView(R.layout.activity_bill);
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
        title_tv.setText("账单");

        moreView = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) moreView.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) moreView.findViewById(R.id.pb_load_progress);

        mAdapter = new BillAdapter(this);
        refreshLayout.setOnRefreshListener(this);
        moreView.setVisibility(View.GONE);
        bill_lv.addFooterView(moreView);
        bill_lv.setAdapter(mAdapter);
        bill_lv.setOnScrollListener(this);
        mCurrpage = 1;
        loadData(mCurrpage);

    }

    //下拉刷新
    @Override
    public void onRefresh() {

        isRefreshing = true;
        if (isLoading) {
            refreshLayout.setRefreshing(false);
            isRefreshing = true;
            return;
        }
        bills.clear();
        mCurrpage = 1;
        record_total=0;
        bill_lv.setEnabled(false);
        loadData(mCurrpage);
    }

    /*
         * @see 加载数据
         * @params currpage 当前页码
         * @params shownum 每页显示的条数，默认是6条
         */
    private void loadData(int currpage) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            rela_no_data.setVisibility(View.VISIBLE);
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("currpage", currpage);
            json.put("shownum", mShownum);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);

            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mCurrpage==1)
        {
            load_linear_data.setVisibility(View.VISIBLE);
        }
        isLoading=true;
        HttpUtil.doPostRequest(UrlsConstant.INCOME_BILL, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "account_record=" + result);
                //Toast.makeText(getContext(),result,Toast.LENGTH_LONG).show();
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
                        Intent intent = new Intent(BillActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        finish();
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(BillActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                loadData(mCurrpage);
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
                            if (mCurrpage == 1) {
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
                        if (data != null)
                        {
                            String deData = null;
                            deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                            if (deData != null) {
                                com.alibaba.fastjson.JSONObject jsonObject1 = JSON.parseObject(deData);
                                mCurrpage = jsonObject1.getIntValue("currpage");
                                record_total = jsonObject1.getIntValue("itemsum");
                                JSONArray array = jsonObject1.getJSONArray("list");
                                if (record_total > 0) {
                                    Bill bill;
                                    for (int i = 0; i < array.size(); i++) {
                                        bill = new Bill();
                                        com.alibaba.fastjson.JSONObject object = array.getJSONObject(i);
                                        bill.setAfter_money(object.getString("balance_after"));
                                        bill.setChange_money(object.getString("balance_change"));
                                        bill.setChange_time(object.getString("change_time"));
                                        bill.setChange_type_name_tv(object.getString("change_type_name"));
                                        bills.add(bill);
                                    }
                                }
                            } else {
                                if (mCurrpage == 1) {
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
                                hasMore = true;
                            }
                        } else {
                            if (mCurrpage == 1) {
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
                            hasMore = true;
                        }

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                        if (mCurrpage == 1) {
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
                        hasMore = true;
                    }
                } catch (Exception e)
                {
                    if (mCurrpage == 1) {
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
                    hasMore = true;
                }

                if (mAdapter.getCount() < record_total) {
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
                if (record_total == 0) {
                    if (mCurrpage == 1) {
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
                    hasMore = true;
                } else {
                    rela_no_data.setVisibility(View.GONE);
                }

                load_linear_data.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                isLoading = false;
                if (isRefreshing) {
                    refreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    bill_lv.setEnabled(true);
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                showToast(s);
                if (mCurrpage == 1) {
                    rela_no_data.setVisibility(View.VISIBLE);
                } else {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            loadMore_tv.setText("没有更多数据了");
                            loadProgress_pb.setVisibility(View.GONE);
                            moreView.setVisibility(View.GONE);
                        }
                    });
                }

                isLoading = false;
                if (isRefreshing) {
                    refreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    bill_lv.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (!isLoading && hasMore) {

                loadMore_tv.setText("正在加载");
                loadProgress_pb.setVisibility(View.VISIBLE);
                moreView.setVisibility(View.VISIBLE);
                isLoading = true;
                mCurrpage++;
                loadData(mCurrpage);
            }
            isLastRow = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0)//判断是否是最后一行
        {
            isLastRow = true;
        }

    }

    public void back(View view) {
        finish();
    }

    public class BillAdapter extends BaseAdapter {
        Context mContext;

        public BillAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return bills.size();
        }

        @Override
        public Object getItem(int position) {
            return bills.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(BillActivity.this).inflate(R.layout.item_bill, null);
                holder.time_tv = (TextView) convertView.findViewById(R.id.change_time_tv);
                holder.blance_change_money_tv = (TextView) convertView.findViewById(R.id.balance_change_tv);
                holder.balance_after_money_tv = (TextView) convertView.findViewById(R.id.balance_after_tv);
                holder.change_type_name_tv = (TextView) convertView.findViewById(R.id.change_type_name_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (bills.size() > 0) {
                holder.time_tv.setText(bills.get(position).getChange_time());
                holder.balance_after_money_tv.setText(bills.get(position).getAfter_money());
                holder.blance_change_money_tv.setText("+" + bills.get(position).getChange_money());
                holder.change_type_name_tv.setText("+" + bills.get(position).getChange_type_name_tv());
            }
            return convertView;
        }

        class ViewHolder {
            TextView blance_change_money_tv;
            TextView time_tv;
            TextView balance_after_money_tv;
            TextView change_type_name_tv;
        }
    }
}
