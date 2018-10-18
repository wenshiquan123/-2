package com.hlzx.ljdjsj.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.activity.BuyerEvaluateActivity;
import com.hlzx.ljdjsj.activity.LoginActivity;
import com.hlzx.ljdjsj.activity.OrderDetailActivity;
import com.hlzx.ljdjsj.bean.OrderInfo;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.common.ScreenManager;
import com.hlzx.ljdjsj.interfaces.DialogOnClickListener;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.interfaces.ViewPageListener;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/11.
 * 自定义订单列表
 */
public class OrderListView extends LinearLayout implements AdapterView.OnItemClickListener,
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener, android.os.Handler.Callback {

    public OrderListView(Context context) {
        super(context);
        initLayout(context);
    }

    public OrderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    /*private final static String statusStr[] = {
             "待发货", "配送中", "已配送", "已完成", "已拒绝"
     };*/
    private ArrayList<OrderInfo> orderInfos = null;
    ListView mListView;
    SwipeRefreshLayout refreshLayout;
    View load_linear_data, rela_no_data;//加载数据，没有数据
    private int page_show_num = 10;//每页显示的订单数
    ViewPageListener mListener;

    boolean isLoading = false;//是否正在加载
    private boolean isLastRow = false;//判断是否最后一行
    private boolean hasMore = false;//是否有更多数据
    boolean isRefreshing = false;//是否正在刷新

    private int status;//当单状态 1 2 3 4 5

    private int imageWidth = 85;
    OrderAdapter mAdapter;
    AlertDialog phoneDialog;
    View moreView;
    private TextView loadMore_tv;
    private ProgressBar loadProgress_pb;

    private static int currpage = 0;//当前页
    private static int pageNum = 0;//总页数
    private static int orderTotal = 0;//当单总数

    Handler handler = new Handler(this);
    //我已送达
    private static int MSG_1 = 1001;
    //开始配送
    private static int MSG_2 = 1002;

    Dialog loadDialog;
    Context mContext;

    private void initLayout(Context context) {
        mContext = context;

        if(null==orderInfos) {
            orderInfos = new ArrayList<OrderInfo>();
        }else
        {
            orderInfos.clear();
        }

        LayoutInflater.from(context).inflate(R.layout.activity_listview, this, true);
        load_linear_data = findViewById(R.id.load_linear_data);
        rela_no_data = findViewById(R.id.rela_no_data);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_srl);
        refreshLayout.setOnRefreshListener(this);

        mListView = (ListView) findViewById(R.id.pull_list);
        mAdapter = new OrderAdapter(context);

        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);

        moreView = LayoutInflater.from(context).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) moreView.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) moreView.findViewById(R.id.pb_load_progress);

        moreView.setVisibility(View.GONE);
        mListView.addFooterView(moreView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);

        imageWidth *= metric.density;
    }

    public void select(int status, ViewPageListener listener) {

        this.mListener = listener;
        this.status = status;

        if (isLoading) {
            return;
        }

        //重置数据
        currpage = 1;
        pageNum = 0;
        orderTotal = 0;
        //orderInfos.clear();
        loadData(status, currpage);



    }

    /*加载数据
     *@param
     * status,订单状态
     * load_currpage 当前页码，默认为1
     *
     */
    public void loadData(final int status, final int load_currpage) {

        //判断网络
        if (!NetWorkUtils.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), "哎！网络不给力", Toast.LENGTH_SHORT).show();
            rela_no_data.setVisibility(View.VISIBLE);
            return;
        }
        LogUtil.e("ME", "load_status=" + status);

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("orderstatus", status);//订单状态 0代表
            json.put("currpage", load_currpage);
            json.put("shownum", page_show_num);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);

            LogUtil.e("ME", "security_session_id=" + MyApplication.getSession_id());
            LogUtil.e("ME", "iv=" + MyApplication.getAesKey());
            LogUtil.e("ME", "token=" + MyApplication.getInstance().getUserInfo().getToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        isLoading = true;
        if (currpage == 1) {
            load_linear_data.setVisibility(View.VISIBLE);
        }
        HttpUtil.doPostRequest(UrlsConstant.GET_ORDER, map, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        String result = responseInfo.result.toString();
                        LogUtil.e("ME", "order=" + result);
                        //Toast.makeText(getContext(),result,Toast.LENGTH_LONG).show();
                        try {
                            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                            final int status = jsonObject.getIntValue("status");
                            String text = jsonObject.getString("text");
                            String data = jsonObject.getString("data");
                            String iv = jsonObject.getString("iv");

                            //判断是否在别处登录
                            String serverMsg = jsonObject.getString("msg");
                            if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                                rela_no_data.setVisibility(View.VISIBLE);
                                load_linear_data.setVisibility(View.GONE);
                                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                intent.putExtra("action", 0);
                                getContext().startActivity(intent);

                                if (currpage == 1) {
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

                                //清空token
                                MyApplication.getInstance().getUserInfo().setToken("");
                                return;
                            } else if (serverMsg.equals("10012")) {
                                PublicUtils.handshake(mContext, new ShakeHandCallback() {
                                    @Override
                                    public void onSuccessed(String str) {
                                        loadData(status, load_currpage);
                                    }

                                    @Override
                                    public void onFalied(String str) {
                                        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }

                            if (status == HttpConstant.SUCCESS_CODE) {
                                if (data.equals("")) {
                                    if (currpage == 1) {
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

                                if (data != null) {
                                    String deData = null;
                                    deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                                    LogUtil.e("ME", "解密数据=" + deData);
                                    if (deData != null) {
                                        com.alibaba.fastjson.JSONObject jsonObject1 = JSON.parseObject(deData);
                                        currpage = jsonObject1.getIntValue("currpage");
                                        pageNum = jsonObject1.getIntValue("pages");
                                        orderTotal = jsonObject1.getIntValue("itemsum");
                                        JSONArray orderList = jsonObject1.getJSONArray("list");
                                        if (orderTotal > 0) {
                                            OrderInfo orderInfo;
                                            for (int i = 0; i < orderList.size(); i++) {
                                                orderInfo = new OrderInfo();
                                                com.alibaba.fastjson.JSONObject jsonObject2 = orderList.getJSONObject(i);
                                                orderInfo.setOrder_id(jsonObject2.getIntValue("order_id"));
                                                orderInfo.setOrder_status(jsonObject2.getIntValue("order_status"));
                                                orderInfo.setOrder_code(jsonObject2.getString("prder_code"));
                                                orderInfo.setLink_man(jsonObject2.getString("link_man"));
                                                orderInfo.setLink_phone(jsonObject2.getString("link_phone"));
                                                orderInfo.setAddress(jsonObject2.getString("address"));
                                                orderInfo.setTotal_money(jsonObject2.getString("total_money"));
                                                orderInfo.setCome_time(jsonObject2.getString("come_time"));
                                                orderInfo.setOrder_detail(jsonObject2.getJSONArray("order_detail"));
                                                orderInfos.add(orderInfo);
                                            }
                                        } else {
                                            if (currpage == 1) {
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
                                        if (currpage == 1) {
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
                                }
                            } else if (status == HttpConstant.FAILURE_CODE) {
                                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                                if (currpage == 1) {
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

                        } catch (Exception ex) {
                            if (currpage == 1) {
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
                        if (mAdapter.getCount() < orderTotal) {
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
                        LogUtil.e("ME", "total=" + orderTotal);
                        if (orderTotal == 0) {
                            if (currpage == 1) {
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
                        load_linear_data.setVisibility(View.GONE);
                        isLoading = false;
                        if (isRefreshing) {
                            refreshLayout.setRefreshing(false);
                            isRefreshing = false;
                            mListView.setEnabled(true);
                        }

                        mAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        load_linear_data.setVisibility(View.GONE);
                        isLoading = false;
                        hasMore = false;
                        if (currpage == 1) {
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

                        if (isRefreshing) {
                            refreshLayout.setRefreshing(false);
                            isRefreshing = false;
                            mListView.setEnabled(true);
                        }
                    }
                }

        );
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        if (position == orderInfos.size()) {
            return;
        }

        LogUtil.e("ME", "position" + position + ";size=" + orderInfos.size());

        Intent intent = new Intent(mContext, OrderDetailActivity.class);
        intent.putExtra("order_id", orderInfos.get(position).getOrder_id());
        //intent.putExtra("order_status", orderInfos.get(position).getOrder_status());
        intent.putExtra("order_status", status);
        mContext.startActivity(intent);
    }

    /**
     * scrollState有三种状态，分别是SCROLL_STATE_IDLE、SCROLL_STATE_TOUCH_SCROLL、SCROLL_STATE_FLING
     * SCROLL_STATE_IDLE是当屏幕停止滚动时
     * SCROLL_STATE_TOUCH_SCROLL是当用户在以触屏方式滚动屏幕并且手指仍然还在屏幕上时
     * SCROLL_STATE_FLING是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        LogUtil.e("ME", "onScrollStateChanged=" + isLastRow);
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // 如果是自动加载,可以在这里放置异步加载数据的代码
            if (!isLoading && hasMore) {
                loadMore_tv.setText("正在加载");
                loadProgress_pb.setVisibility(View.VISIBLE);
                moreView.setVisibility(View.VISIBLE);
                currpage++;
                loadData(status, currpage);
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

    //下拉刷新得回调方法
    @Override
    public void onRefresh() {

        if (isLoading) {
            refreshLayout.setRefreshing(false);
            return;
        }

        isRefreshing = true;
        orderInfos.clear();
        orderTotal = 0;
        mListView.setEnabled(false);
        loadData(status, 1);
    }


    @Override
    public boolean handleMessage(Message msg) {
        int order_id = orderInfos.get(msg.arg1).getOrder_id();
        if (msg.what == MSG_1) {
            LogUtil.e("ME", "我已送达");
            doCommit(2, order_id);

        } else if (msg.what == MSG_2) {
            doCommit(1, order_id);
            LogUtil.e("ME", "开始配送");
        }
        return false;
    }

    /*
     * 提交处理类型到服务器
     * @params action_type 为处理类型
     * @params order_id 订单id
     * @return void
     */
    private void doCommit(final int action_type, int order_id) {
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("order_id", order_id);
            json.put("action_type", action_type);//处理理由类型：1.开始配送、2.我已送达、3.拒绝订单
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        loadDialog = PublicDialog.createLoadingDialog(mContext, "正在提交..");
        loadDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.ORDER_ACTION, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "订单处理=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    //String data=jsonObject.getString("data");
                    String text = jsonObject.getString("text");
                    if (status == HttpConstant.SUCCESS_CODE) {
                        loadDialog.dismiss();
                        //Toast.makeText(mContext, "提交成功", Toast.LENGTH_SHORT).show();
                        //提交成功之后跳到相应的界面，接口实现
                        mListener.OnClick(action_type);


                    } else if (status == HttpConstant.FAILURE_CODE) {
                        loadDialog.dismiss();
                        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    loadDialog.dismiss();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                loadDialog.dismiss();
                Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //订单适配器
    private class OrderAdapter extends BaseAdapter {

        Context mContext;

        public OrderAdapter(Context context) {
            mContext = context;
            //LogUtil.e("ME","订单适配器");
        }

        @Override
        public int getCount() {
            return orderInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return orderInfos.get(position);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_order, null);
                holder.call_bt = (Button) convertView.findViewById(R.id.call_buyer_bt);
                holder.address_tv = (TextView) convertView.findViewById(R.id.address_tv);
                holder.total_money_tv = (TextView) convertView.findViewById(R.id.total_money_tv);
                holder.come_time_tv = (TextView) convertView.findViewById(R.id.come_time_tv);

                holder.right_bt = (Button) convertView.findViewById(R.id.right_bt);
                holder.right_bt.setId(position);
                holder.icon_content_ll = (LinearLayout) convertView.findViewById(R.id.icon_content_ll);
                holder.icon_content_ll.setId(position);
                holder.evaluate_bt=(Button)convertView.findViewById(R.id.buyer_evaluate_bt);
                holder.evaluate_bt.setId(position);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.right_bt.setId(position);
                holder.icon_content_ll.setId(position);
                holder.evaluate_bt.setId(position);
            }

            final String linkPhone = orderInfos.get(position).getLink_phone();
            String linkMan = orderInfos.get(position).getLink_man();
            holder.call_bt.setTag(linkPhone + "," + linkMan);
            holder.call_bt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (phoneDialog == null) {
                        phoneDialog = PublicDialog.phoneDialog(mContext, (String) v.getTag(), new DialogOnClickListener() {
                            @Override
                            public void onCancel() {
                                phoneDialog.dismiss();
                                phoneDialog = null;
                            }

                            @Override
                            public void onConfirm() {
                                phoneDialog.dismiss();
                                phoneDialog = null;
                                Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + linkPhone));
                                mContext.startActivity(call);

                            }
                        });
                    }

                }
            });

            if (orderInfos.size() > 0) {
                holder.address_tv.setText(orderInfos.get(position).getAddress());
                holder.total_money_tv.setText(orderInfos.get(position).getTotal_money() + "");
                if (orderInfos.get(position).getCome_time() == null || orderInfos.get(position).getCome_time().equals("")) {
                    holder.come_time_tv.setText("尽快配送");
                } else {
                    holder.come_time_tv.setText(orderInfos.get(position).getCome_time());
                }
            }

            //判断订单状态
            if (status == 1)//待发货
            {
                holder.right_bt.setVisibility(View.VISIBLE);
                holder.right_bt.setText("开始配送");
                holder.evaluate_bt.setVisibility(View.GONE);

            } else if (status == 2)//配送中
            {
                holder.right_bt.setVisibility(View.VISIBLE);
                holder.right_bt.setText("我已送达");
                holder.evaluate_bt.setVisibility(View.GONE);

            }else if(status==4)//已完成
            {
                holder.evaluate_bt.setVisibility(View.GONE);
                holder.right_bt.setVisibility(View.GONE);
            }else {
                holder.right_bt.setVisibility(View.GONE);
                holder.evaluate_bt.setVisibility(View.GONE);
            }


            holder.right_bt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = v.getId();
                    if (((Button) v).getText().equals("我已送达")) {
                        Message msg = handler.obtainMessage(MSG_1);
                        msg.arg1 = position;
                        handler.sendMessage(msg);

                    } else if (((Button) v).getText().equals("开始配送")) {
                        Message msg = handler.obtainMessage(MSG_2);
                        msg.arg1 = position;
                        handler.sendMessage(msg);
                    }
                }
            });

            holder.evaluate_bt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=v.getId();
                    Intent intent=new Intent(mContext,BuyerEvaluateActivity.class);
                    mContext.startActivity(intent);

                }
            });


            //添加图片，最多4个
            holder.icon_content_ll.removeAllViews();
            JSONArray array = orderInfos.get(position).getOrder_detail();
            if (array != null && array.size() > 0) {
                for (int i = 0; i < array.size(); i++) {
                    ImageView view = new ImageView(mContext);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            ScreenManager.dp2px(mContext, 80), ScreenManager.dp2px(mContext, 80));
                    lp.leftMargin = 10;
                    view.setLayoutParams(lp);
                    com.alibaba.fastjson.JSONObject object = array.getJSONObject(i);
                    Picasso.with(mContext).load(UrlsConstant.BASE_PIC_URL + object.getString("goods_path"))
                            .resize(100,100).centerCrop()
                            .placeholder(R.mipmap.tu)
                            .error(R.mipmap.tu)
                            .into(view);
                    // view.setImageResource(R.mipmap.img_start);
                    holder.icon_content_ll.addView(view);
                }

            }
            LogUtil.e("ME", "getView");
            return convertView;
        }

        class ViewHolder {
            Button call_bt;
            TextView address_tv;
            TextView total_money_tv;
            Button right_bt;
            Button evaluate_bt;
            TextView come_time_tv;
            LinearLayout icon_content_ll;
        }
    }
}
