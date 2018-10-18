package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.UnconfirmOrder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 * 今日订单数
 */
public class TodayOrderActivity extends BaseActivity implements AbsListView.OnScrollListener,
        Handler.Callback,View.OnClickListener,SwipeRefreshLayout.OnRefreshListener{
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.refresh_srl)
    SwipeRefreshLayout refresh_srl;

    @ViewInject(R.id.unconfirm_order_lv)
    private ListView unconfirm_order_lv;

    ArrayList<UnconfirmOrder> orders=new ArrayList<>();
    MyUnconfirmOrderAdapter adapter=null;

    //当前页码
    private int mCurPage = 1;
    //显示的条数
    private int mShowNum = 10;
    //总条数
    private int mTotalItem = 0;
    //总页数
    private int mTotalPage = 0;
    //加载更多
    private View more_ll;
    private TextView loadMore_tv;
    private ProgressBar loadProgress_pb;

    @ViewInject(R.id.load_linear_data)
    private RelativeLayout load_data_rl;

    @ViewInject(R.id.rela_no_data)
    private RelativeLayout no_data_rl;

    Dialog waitDialog=null;
    //日期
    String date="";


    //是否最后一行
    private boolean isLastRow = false;
    //是否正在加载
    private boolean isLoading = false;
    //有更多
    private boolean hasMore = false;
    //是否正在刷新
    private boolean isRefreshing=false;


    @Override
    public void setLayout() {
        setContentView(R.layout.activity_today_order);
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
        title_tv.setText("今日订单");
        date=getIntent().getStringExtra("date");
        more_ll = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) more_ll.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) more_ll.findViewById(R.id.pb_load_progress);

        adapter=new MyUnconfirmOrderAdapter(this);
        more_ll.setVisibility(View.GONE);
        unconfirm_order_lv.addFooterView(more_ll);
        unconfirm_order_lv.setAdapter(adapter);
        refresh_srl.setOnRefreshListener(this);
        unconfirm_order_lv.setOnScrollListener(this);

        loadData(mCurPage);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    public void onRefresh() {
        if(isLoading)
        {
            refresh_srl.setRefreshing(false);
            return;
        }
        mCurPage=1;
        mTotalItem=0;
        mTotalPage=0;
        orders.clear();
        isRefreshing=true;
        unconfirm_order_lv.setEnabled(false);
        loadData(mCurPage);

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (!isLoading && hasMore) {

                loadMore_tv.setText("加载更多数据");
                loadProgress_pb.setVisibility(View.VISIBLE);
                more_ll.setVisibility(View.VISIBLE);

                //unconfirm_order_lv.removeFooterView(more_ll);
                //unconfirm_order_lv.addFooterView(more_ll);
                //unconfirm_order_lv.setAdapter(adapter);

                mCurPage++;
                loadData(mCurPage);
            }
            isLastRow = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
            isLastRow = true;
        }
    }

    /**
     * 加载数据
     *
     * @param curPage
     */
    private void loadData(int curPage) {

        //判断网络
        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            no_data_rl.setVisibility(View.VISIBLE);
            return;
        }
        LogUtil.e("ME","date="+date);

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type",1);
            json.put("date",date);
            json.put("currpage", curPage);
            json.put("shownum", mShowNum);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mCurPage == 1) {
            load_data_rl.setVisibility(View.VISIBLE);
        }
        isLoading = true;
        HttpUtil.doPostRequest(UrlsConstant.INCOME_ORDER_LIST, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "goods_list=" + result);

                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");
                    String iv = jsonObject.getString("iv");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent=new Intent(TodayOrderActivity.this,LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action",0);
                        startActivity(intent);
                        //finish();
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        if (data.equals("")) {
                            if (mCurPage == 1) {
                                no_data_rl.setVisibility(View.VISIBLE);
                            } else {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadMore_tv.setText("没有更多数据了");
                                        loadProgress_pb.setVisibility(View.GONE);
                                        more_ll.setVisibility(View.VISIBLE);

                                    }
                                });
                            }
                        }
                        String deData = null;
                        deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                        if (deData != null) {
                            LogUtil.e("ME", "解析数据=" + deData);

                            com.alibaba.fastjson.JSONObject object = JSON.parseObject(deData);
                            mCurPage = object.getIntValue("currpage");
                            mTotalItem = object.getIntValue("itemsum");
                            JSONArray array = object.getJSONArray("list");
                            if (array.size() > 0) {
                                com.alibaba.fastjson.JSONObject jsonObject1 = null;
                                UnconfirmOrder order=null;
                                for(int i=0;i<array.size();i++)
                                {
                                    jsonObject1=array.getJSONObject(i);
                                    order=new UnconfirmOrder();
                                    order.setCtime(jsonObject1.getString("ctime"));
                                    order.setOrder_code(jsonObject1.getString("order_code"));
                                    order.setOrder_status_str(jsonObject1.getString("order_status_str"));
                                    order.setTotal_money(jsonObject1.getString("total_money"));
                                    order.setUpdate_time(jsonObject1.getString("update_time"));
                                    order.setIs_account(jsonObject1.getIntValue("is_account"));
                                    orders.add(order);
                                    jsonObject1=null;
                                    order=null;
                                }

                            } else {
                                if (mCurPage == 1) {
                                    no_data_rl.setVisibility(View.VISIBLE);
                                } else {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadMore_tv.setText("没有更多数据了");
                                            loadProgress_pb.setVisibility(View.GONE);
                                            more_ll.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                                hasMore = false;
                            }

                        } else {
                            if (mCurPage == 1) {
                                no_data_rl.setVisibility(View.VISIBLE);
                            } else {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadMore_tv.setText("没有更多数据了");
                                        loadProgress_pb.setVisibility(View.GONE);
                                        more_ll.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                            hasMore = false;
                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                        if (mCurPage == 1) {
                            no_data_rl.setVisibility(View.VISIBLE);
                        } else {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    loadMore_tv.setText("没有更多数据了");
                                    loadProgress_pb.setVisibility(View.GONE);
                                    more_ll.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        hasMore = false;
                    }

                } catch (Exception e) {
                    if (mCurPage == 1) {
                        no_data_rl.setVisibility(View.VISIBLE);
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                loadMore_tv.setText("没有更多数据了");
                                loadProgress_pb.setVisibility(View.GONE);
                                more_ll.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }

                if (adapter.getCount() < mTotalItem) {
                    hasMore = true;
                } else {
                    hasMore = false;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            loadMore_tv.setText("没有更多数据了");
                            loadProgress_pb.setVisibility(View.GONE);
                            more_ll.setVisibility(View.VISIBLE);
                        }
                    });
                }
                if (mTotalItem == 0) {
                    if (mCurPage == 1) {
                        no_data_rl.setVisibility(View.VISIBLE);
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                loadMore_tv.setText("没有更多数据了");
                                loadProgress_pb.setVisibility(View.GONE);
                                more_ll.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else {
                    no_data_rl.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                isLoading = false;
                load_data_rl.setVisibility(View.GONE);
                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }
                if(isRefreshing)
                {
                    refresh_srl.setRefreshing(false);
                    isRefreshing=false;
                    unconfirm_order_lv.setEnabled(true);
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                showToast(s);
                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }

                load_data_rl.setVisibility(View.GONE);
                if (mCurPage == 1) {
                    no_data_rl.setVisibility(View.VISIBLE);
                } else {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            loadMore_tv.setText("没有更多数据了");
                            loadProgress_pb.setVisibility(View.GONE);
                            more_ll.setVisibility(View.VISIBLE);
                        }
                    });
                }
                isLoading = false;

                if(isRefreshing)
                {
                    refresh_srl.setRefreshing(false);
                    isRefreshing=false;
                    unconfirm_order_lv.setEnabled(true);
                }
            }
        });
    }

    public void back(View view) {
        finish();
    }


    /**
     * 商品适配器
     */
    class MyUnconfirmOrderAdapter extends BaseAdapter {
        Context mContext;

        public MyUnconfirmOrderAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return orders.size();
        }

        @Override
        public Object getItem(int position) {
            return orders.get(position);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_today_order, null);
                holder.order_code_tv=(TextView)convertView.findViewById(R.id.order_code_tv);
                holder.total_money_tv=(TextView)convertView.findViewById(R.id.total_money_tv);
                holder.status_tv=(TextView)convertView.findViewById(R.id.status_tv);
                holder.update_time_tv=(TextView)convertView.findViewById(R.id.update_time_tv);
                holder.ctime_tv=(TextView)convertView.findViewById(R.id.ctime_tv);
                holder.is_account_tv=(TextView)convertView.findViewById(R.id.is_account_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if(orders.size()>0)
            {
                holder.order_code_tv.setText(orders.get(position).getOrder_code());
                holder.total_money_tv.setText(orders.get(position).getTotal_money());
                if(orders.get(position).getUpdate_time()==null||orders.get(position).getUpdate_time().equals(""))
                {
                    holder.update_time_tv.setVisibility(View.INVISIBLE);
                }else {
                    holder.update_time_tv.setVisibility(View.VISIBLE);
                    holder.update_time_tv.setText(orders.get(position).getUpdate_time());
                }
                holder.status_tv.setText(orders.get(position).getOrder_status_str());
                holder.ctime_tv.setText(orders.get(position).getCtime());

                if(orders.get(position).getIs_account()==0)
                {
                    holder.is_account_tv.setText("未到账");
                }else if(orders.get(position).getIs_account()==1)
                {
                    holder.is_account_tv.setText("已到账");
                }

            }

            return convertView;
        }

        class ViewHolder {
            TextView order_code_tv;
            TextView total_money_tv;
            TextView status_tv;
            TextView update_time_tv;
            TextView ctime_tv;
            TextView is_account_tv;
        }
    }
}
