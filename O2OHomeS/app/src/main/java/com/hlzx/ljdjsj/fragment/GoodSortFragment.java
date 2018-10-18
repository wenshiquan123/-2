package com.hlzx.ljdjsj.fragment;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.activity.AddInventoryGoodActivity;
import com.hlzx.ljdjsj.activity.EditGoodActivity;
import com.hlzx.ljdjsj.activity.LoginActivity;
import com.hlzx.ljdjsj.bean.Goods;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.fragment.order.OrderBaseFragment;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
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
 * Created by alan on 2015/11/30.
 */
public class GoodSortFragment extends OrderBaseFragment implements Handler.Callback,
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    private int category_id1 = 0;
    private String category_name1 = "";
    private int category_id2 = 0;
    private String category_name2 = "";

    private RelativeLayout no_data_rl;
    private RelativeLayout load_data_rl;
    //标志位，标志已经初始化完成
    private boolean isPrepared = false;
    //是否最后一行
    private boolean isLastRow = false;
    //是否正在加载
    private boolean isLoading = false;
    //有更多
    private boolean hasMore = false;
    //是否正在加载
    private boolean isRefreshing = false;

    //当前页码
    private int mCurPage = 1;
    //显示的条数
    private int mShowNum = 24;
    //总条数
    private int mTotalItem = 0;
    //总页数
    private int mTotalPage = 0;

    //加载更多
    private View more_ll;
    private TextView loadMore_tv;
    private ProgressBar loadProgress_pb;

    //Listview
    private ListView goods_lv;

    ArrayList<Goods> mGoodList = new ArrayList<Goods>();

    Handler handler = new Handler(this);
    GoodsAdapter adapter = null;

    private static final int UPDATE_UI = 1001;
    private static final int UPDATE_STIACK = 1002;
    Dialog waitDialog = null;

    SwipeRefreshLayout refreshLayout;

    //是否正在置顶
    boolean isStackTop = false;

    public GoodSortFragment() {
    }

    public GoodSortFragment(int category_id1, String category_name1, int category_id2, String category_name2) {
        this.category_id1 = category_id1;
        this.category_name1 = category_name1;
        this.category_id2 = category_id2;
        this.category_name2 = category_name2;
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position == mGoodList.size()) {
                return;
            }

            if (mGoodList.get(position).getGoods_id() == 0) {
                Intent intent = new Intent(getActivity(), AddInventoryGoodActivity.class);
                intent.putExtra("good", mGoodList.get(position));
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), EditGoodActivity.class);
                intent.putExtra("good", mGoodList.get(position));
                startActivity(intent);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_goodsort, container, false);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_srl);
        refreshLayout.setOnRefreshListener(this);

        goods_lv = (ListView) view.findViewById(R.id.goods_lv);
        adapter = new GoodsAdapter(getActivity());

        more_ll = LayoutInflater.from(getActivity()).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) more_ll.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) more_ll.findViewById(R.id.pb_load_progress);

        more_ll.setVisibility(View.GONE);
        goods_lv.addFooterView(more_ll);
        goods_lv.setAdapter(adapter);
        goods_lv.setOnScrollListener(this);
        //goods_lv.setOnItemClickListener(itemClickListener);


        no_data_rl = (RelativeLayout) view.findViewById(R.id.rela_no_data);
        load_data_rl = (RelativeLayout) view.findViewById(R.id.load_linear_data);
        //因为共用一个Fragment视图，所以当前这个视图已被加载到Activity中，必须先清除后再加入Activity
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }

        isPrepared = true;
        lazyLoad();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onRefresh() {
        if (isLoading) {
            refreshLayout.setRefreshing(false);
            return;
        }
        mCurPage = 1;
        mTotalItem = 0;
        mGoodList.clear();
        isRefreshing = true;
        goods_lv.setEnabled(false);

        loadData(mCurPage);

    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == UPDATE_UI) {
            String deData = (String) msg.obj;
            try {
                com.alibaba.fastjson.JSONObject object = JSON.parseObject(deData);
            } catch (JSONException e) {
            }
        } else if (msg.what == UPDATE_STIACK) {
            if (!isStackTop) {
                int position = msg.arg1;
                if (mGoodList.size() > position) {
                    int goods_id = mGoodList.get(position).getGoods_id();
                    stickToTop(goods_id);
                }
            }
        }
        return false;
    }

    /**
     * 置顶
     *
     * @param goods_id
     */
    private void stickToTop(final int goods_id) {
        //判断网络
        if (!NetWorkUtils.isNetworkAvailable(getActivity())) {
            showToast("哎！网络不给力");
            return;
        }

        LogUtil.e("ME", "goods_id=" + goods_id + ";category_id1=" + category_id1 + ";category_id2=" + category_id2);

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("goods_id", goods_id);
            json.put("category_id1", category_id1);
            if (category_id1 == category_id2) {
                json.put("category_id2", 0);
            } else {
                json.put("category_id2", category_id2);
            }
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (waitDialog == null) {
            waitDialog = PublicDialog.createLoadingDialog(getActivity(), "正在提交...");
            waitDialog.dismiss();
        }

        isStackTop = true;
        HttpUtil.doPostRequest(UrlsConstant.GOODS_TO_TOP, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

                String result = responseInfo.result.toString();

                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String text = jsonObject.getString("text");

                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        getActivity().finish();
                        return;
                    } else if (serverMsg.equals("10012")) {    //重新握手
                        PublicUtils.handshake(getActivity(), new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                stickToTop(goods_id);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        showToast(text);
                        mCurPage = 1;
                        mTotalItem = 0;
                        mGoodList.clear();
                        loadData(mCurPage);

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                    }
                } catch (Exception e) {
                }

                LogUtil.e("ME", "result=" + result);
                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }
                isStackTop = false;
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }
                showToast(s);
                isStackTop = false;
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (!isLoading && hasMore) {
                loadMore_tv.setText("加载更多数据");
                loadProgress_pb.setVisibility(View.VISIBLE);
                more_ll.setVisibility(View.VISIBLE);
                mCurPage++;
                loadData(mCurPage);
            }
            isLastRow = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (adapter == null) {
            return;
        }
        if (adapter.getCount() == 0) {
            return;
        }

        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
            isLastRow = true;
        }
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        LogUtil.e("ME", "fragment:category_id1=" + category_id1 + ";category_name1=" + category_name1);
        LogUtil.e("ME", "fragment:category_id2=" + category_id2 + ";category_name2=" + category_name2);
        mGoodList.clear();
        mCurPage = 1;
        mTotalItem = 0;
        mTotalPage = 0;
        loadData(mCurPage);
    }

    private void loadData(int curPage) {

        if (!NetWorkUtils.isNetworkAvailable(getActivity())) {
            showToast("哎!网络不给力");
            return;
        }
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("category_id", category_id2);//0表示全部
            json.put("currpage", curPage);
            json.put("shownum", mShowNum);
            json.put("status", 1);//在架上
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
        HttpUtil.doPostRequest(UrlsConstant.GOODS_LIST, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "result=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");
                    String iv = jsonObject.getString("iv");

                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(serverMsg);
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        getActivity().finish();
                        return;
                    } else if (serverMsg.equals("10012")) {    //重新握手
                        PublicUtils.handshake(getActivity(), new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                loadData(mCurPage);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        if (data == "") {
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
                            mTotalPage = object.getIntValue("pages");
                            mTotalItem = object.getIntValue("itemsum");
                            JSONArray array = object.getJSONArray("list");
                            if (array.size() > 0) {
                                com.alibaba.fastjson.JSONObject jsonObject1 = null;
                                Goods goods = null;
                                for (int i = 0; i < array.size(); i++) {
                                    jsonObject1 = array.getJSONObject(i);
                                    goods = new Goods();
                                    goods.setName(jsonObject1.getString("name"));
                                    goods.setFormat(jsonObject1.getString("format"));
                                    goods.setPicpath(jsonObject1.getString("path"));
                                    goods.setGoods_id(jsonObject1.getIntValue("goods_id"));
                                    goods.setPrice(jsonObject1.getString("price"));
                                    //goods.setGoods_category1_id(jsonObject1.get("category_id1"));
                                    //goods.setGoods_category2_id(jsonObject1.getIntValue("category_id2"));
                                    mGoodList.add(goods);
                                    goods = null;
                                    jsonObject1 = null;
                                }
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
                } catch (Exception ex) {
                    //showToast(ex.toString());
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
                LogUtil.e("ME", "total=" + mTotalItem);
                if (isRefreshing) {
                    refreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    goods_lv.setEnabled(true);
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                showToast(s);
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
                if (isRefreshing) {
                    refreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    goods_lv.setEnabled(true);
                }

            }
        });
    }

    //适配器
    class GoodsAdapter extends BaseAdapter {
        Context mContext;

        public GoodsAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mGoodList.size();
        }

        @Override
        public Object getItem(int position) {
            return mGoodList.get(position);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_goods_sort, null);
                holder.pic_iv = (ImageView) convertView.findViewById(R.id.goods_iv);
                holder.name_tv = (TextView) convertView.findViewById(R.id.goods_name_tv);
                holder.format_tv = (TextView) convertView.findViewById(R.id.goods_format_tv);
                holder.stick_bt = (Button) convertView.findViewById(R.id.stick_bt);
                holder.stick_bt.setId(position);
                holder.goods_price_tv = (TextView) convertView.findViewById(R.id.goods_price_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.stick_bt.setId(position);
            }

            if (mGoodList.size() > 0) {
                holder.name_tv.setText(mGoodList.get(position).getName());
                holder.goods_price_tv.setText("￥" + mGoodList.get(position).getPrice());
                holder.format_tv.setText("规格:" + mGoodList.get(position).getFormat());
                Picasso.with(mContext).load(mGoodList.get(position).getPicpath())
                        .resize(100, 100).centerCrop()
                        .placeholder(R.mipmap.tu)
                        .error(R.mipmap.tu)
                        .into(holder.pic_iv);

                if (position == 0) {
                    holder.stick_bt.setVisibility(View.INVISIBLE);
                } else {
                    holder.stick_bt.setVisibility(View.VISIBLE);
                }

                // LogUtil.e("ME","path="+mGoodList.get(position).getPicpath());

            }
            holder.stick_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = v.getId();
                    Message msg = handler.obtainMessage(UPDATE_STIACK);//置顶
                    msg.arg1 = position;
                    handler.sendMessage(msg);
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView pic_iv;
            TextView name_tv;
            TextView goods_price_tv;
            TextView format_tv;
            Button stick_bt;
        }

    }
}
