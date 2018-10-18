package com.hlzx.ljdjsj.activity;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.DepositRecord;
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
public class DepositRecordActivity extends BaseActivity implements AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.record_lv)
    private ListView record_lv;

    @ViewInject(R.id.load_linear_data)
    View load_data_view;

    @ViewInject(R.id.rela_no_data)
    View no_data_view;

    @ViewInject(R.id.refresh_srl)
    private SwipeRefreshLayout refresh_srl;

    View moreView;
    private TextView load_more_tv;
    private ProgressBar load_more_pb;

    //判断是否是最后一行
    boolean isLastRow = false;
    //是否正在加载
    boolean isLoading = false;
    //有更多数据
    boolean hasMore = false;
    //是否正在刷新
    boolean isRefreshing = false;

    //每页显示的条数
    private int mShowNum = 0;
    //当前页
    private int mCurrPage = 1;
    //总条数
    private int mTotal = 0;

    DepositRecordAdapter mAdapter;
    ArrayList<DepositRecord> records = new ArrayList<DepositRecord>();


    @Override
    public void setLayout() {
        setContentView(R.layout.activity_deposit_record);
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
        title_tv.setText("提现记录");

        moreView = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        load_more_tv = (TextView) moreView.findViewById(R.id.tv_load_more);
        load_more_pb = (ProgressBar) moreView.findViewById(R.id.pb_load_progress);

        record_lv.setFocusable(false);
        record_lv.setFocusableInTouchMode(false);
        mAdapter = new DepositRecordAdapter(this);

        moreView.setVisibility(View.GONE);
        record_lv.addFooterView(moreView);
        record_lv.setAdapter(mAdapter);
        record_lv.setOnScrollListener(this);
        refresh_srl.setOnRefreshListener(this);

        loadData(mCurrPage);

    }

    @Override
    public void onRefresh() {
        if (isLoading) {
            refresh_srl.setRefreshing(false);
            return;
        }

        mCurrPage = 1;
        mTotal = 0;
        records.clear();
        isRefreshing = true;
        record_lv.setEnabled(false);
        loadData(mCurrPage);
    }

    private void loadData(int currpage) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            no_data_view.setVisibility(View.VISIBLE);
            return;
        }
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("currpage", currpage);
            json.put("shownum", mShowNum);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);

            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        isLoading = true;
        if (mCurrPage == 1) {
            load_data_view.setVisibility(View.VISIBLE);
        }

        HttpUtil.doPostRequest(UrlsConstant.INCOME_WITH_DRAW_LIST, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "deposit=" + result);
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
                        Intent intent = new Intent(DepositRecordActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        finish();
                        MyApplication.getInstance().getUserInfo().setToken("");
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(DepositRecordActivity.this, new ShakeHandCallback() {
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
                                no_data_view.setVisibility(View.VISIBLE);
                            } else {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        load_more_tv.setText("没有更多数据了");
                                        load_more_pb.setVisibility(View.GONE);
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
                            mTotal = jsonObject1.getIntValue("itemsum");
                            JSONArray array = jsonObject1.getJSONArray("list");
                            if (array.size() > 0) {
                                com.alibaba.fastjson.JSONObject object = null;
                                DepositRecord record = null;
                                for (int i = 0; i < array.size(); i++) {
                                    record = new DepositRecord();
                                    object = array.getJSONObject(i);
                                    record.setChange_time(object.getString("change_time"));
                                    record.setBalance_change(object.getString("balance_change"));
                                    record.setChange_status(object.getIntValue("change_status"));
                                    records.add(record);
                                    record = null;
                                    object = null;
                                }
                            } else {
                                if (mCurrPage == 1) {
                                    no_data_view.setVisibility(View.VISIBLE);
                                } else {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            load_more_tv.setText("没有更多数据了");
                                            load_more_pb.setVisibility(View.GONE);
                                            moreView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                                hasMore = false;
                            }
                        } else {
                            if (mCurrPage == 1) {
                                no_data_view.setVisibility(View.VISIBLE);
                            } else {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        load_more_tv.setText("没有更多数据了");
                                        load_more_pb.setVisibility(View.GONE);
                                        moreView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                            hasMore = false;
                        }

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        no_data_view.setVisibility(View.VISIBLE);
                        hasMore = false;
                    }

                } catch (Exception ex) {
                    //showToast(ex.toString());
                    if (mCurrPage == 1) {
                        no_data_view.setVisibility(View.VISIBLE);
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                load_more_tv.setText("没有更多数据了");
                                load_more_pb.setVisibility(View.GONE);
                                moreView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    hasMore = false;
                }
                if (mAdapter.getCount() < mTotal) {
                    hasMore = true;
                } else {
                    hasMore = false;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            load_more_tv.setText("没有更多数据了");
                            load_more_pb.setVisibility(View.GONE);
                        }
                    });
                }
                LogUtil.e("ME", "total=" + mTotal + ";page=" + mCurrPage);
                if (mTotal == 0) {
                    if (mCurrPage == 1) {
                        no_data_view.setVisibility(View.VISIBLE);
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                load_more_tv.setText("没有更多数据了");
                                load_more_pb.setVisibility(View.GONE);
                                moreView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    hasMore = false;
                } else {
                    no_data_view.setVisibility(View.GONE);
                }
                isLoading = false;
                load_data_view.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();

                if (isRefreshing) {
                    refresh_srl.setRefreshing(false);
                    isRefreshing = false;
                    record_lv.setEnabled(true);
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {

                isLoading = false;
                if (mCurrPage == 1) {
                    no_data_view.setVisibility(View.VISIBLE);
                } else {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            //load_more_tv.setText("没有更多数据了");
                            //load_more_pb.setVisibility(View.GONE);
                            moreView.setVisibility(View.GONE);
                        }
                    });
                }
                hasMore = false;
                if (isRefreshing) {
                    refresh_srl.setRefreshing(false);
                    isRefreshing = false;
                    record_lv.setEnabled(true);
                }
            }
        });

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (!isLoading && hasMore) {
                load_more_tv.setText("加载更多数据");
                load_more_pb.setVisibility(View.VISIBLE);
                moreView.setVisibility(View.VISIBLE);
                isLoading = true;
                mCurrPage++;
                loadData(mCurrPage);
            }
            isLastRow = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //判断是否是最后一行
        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
            isLastRow = true;
        }

    }

    public void back(View view) {
        finish();
    }

    public class DepositRecordAdapter extends BaseAdapter {
        Context mContext;

        public DepositRecordAdapter(Context context) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(DepositRecordActivity.this).inflate(R.layout.item_deposit_record, null);
                holder.date_tv = (TextView) convertView.findViewById(R.id.date_tv);
                holder.money_tv = (TextView) convertView.findViewById(R.id.money_tv);
                holder.status_tv = (TextView) convertView.findViewById(R.id.status_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (records.size() > 0) {
                holder.date_tv.setText(records.get(position).getChange_time());
                holder.money_tv.setText(records.get(position).getBalance_change());
                if (records.get(position).getChange_status() == 1) {
                    holder.status_tv.setText("处理中");
                } else if (records.get(position).getChange_status() == 2) {
                    holder.status_tv.setText("已完成");
                }
            }

            return convertView;
        }

        class ViewHolder {
            TextView date_tv;
            TextView money_tv;
            TextView status_tv;
        }
    }

}
