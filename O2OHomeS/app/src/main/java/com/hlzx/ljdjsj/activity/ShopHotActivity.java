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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.Goods;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.hlzx.ljdjsj.view.SwipeListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class ShopHotActivity extends BaseActivity implements View.OnClickListener, AbsListView.OnScrollListener,
        SwipeRefreshLayout.OnRefreshListener, android.os.Handler.Callback {

    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.recommend_good_lv)
    private SwipeListView recommend_good_lv;


    MyHotAdapter hotAdapter = null;
    ArrayList<Goods> goodses = new ArrayList<Goods>();

    //状态：0-全部，1-在架上，2-已下架，3-已售罄
    private int status = 0;
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

    //分类ID
    private int category_id = 0;

    //是否最后一行
    private boolean isLastRow = false;
    //是否正在加载
    private boolean isLoading = false;
    //有更多
    private boolean hasMore = false;
    //是否正在刷新
    private boolean isRefreshing = false;

    Dialog waitDialog = null;

    private static final int REQUEST_ADD = 101;

    @ViewInject(R.id.refresh_srl)
    private SwipeRefreshLayout refresh_srl;

    private static final int DELETE = 11;
    Handler handler = new Handler(this);

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_shopkeeper_recommend);
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
        title_tv.setText("店铺热卖");

        more_ll = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) more_ll.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) more_ll.findViewById(R.id.pb_load_progress);

        hotAdapter = new MyHotAdapter(this);
        more_ll.setVisibility(View.GONE);
        recommend_good_lv.addFooterView(more_ll);
        recommend_good_lv.setAdapter(hotAdapter);
        recommend_good_lv.setOnItemClickListener(itemClickListener);
        recommend_good_lv.setOnScrollListener(this);
        refresh_srl.setOnRefreshListener(this);


        mCurPage = 1;
        loadData(mCurPage);


    }

    AbsListView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position==goodses.size())
            {
                return;
            }
            Intent intent = new Intent(ShopHotActivity.this, EditGoodActivity.class);
            intent.putExtra("good", goodses.get(position));
            startActivityForResult(intent,REQUEST_ADD);
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == DELETE) {
            int position = msg.arg1;
            int goods_id = goodses.get(position).getGoods_id();
            delete(goods_id);
        }
        return false;
    }

    private void delete(int goods_id) {
        //判断网络状态
        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }
        LogUtil.e("ME", "goods_id=" + goods_id);
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type", 12);
            json.put("goods_ids", goods_id);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        waitDialog = PublicDialog.createLoadingDialog(this, "正在提交...");
        waitDialog.show();

        HttpUtil.doPostRequest(UrlsConstant.GOODS_MARKETING_TOOLS, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "result=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");
                    if (status == HttpConstant.SUCCESS_CODE) {
                        //刷新
                        mCurPage = 1;
                        mTotalItem = 0;
                        goodses.clear();
                        more_ll.setVisibility(View.GONE);
                        recommend_good_lv.removeFooterView(more_ll);
                        recommend_good_lv.addFooterView(more_ll);
                        recommend_good_lv.setAdapter(hotAdapter);
                        loadData(mCurPage);

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                    }
                } catch (Exception e) {
                    showToast(e.toString());
                }

                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }
                showToast(s);
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {
        if (isLoading) {
            refresh_srl.setRefreshing(false);
            return;
        }

        mCurPage = 1;
        mTotalItem = 0;
        mTotalPage = 0;
        goodses.clear();

        isRefreshing = true;
        recommend_good_lv.setEnabled(false);
        loadData(mCurPage);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            LogUtil.e("ME", "哈哈哈");
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
        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎!网络不给力");
            return;
        }

        LogUtil.e("ME", "status=" + status + ";category_id=" + category_id);
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("category_id", category_id);//0表示全部
            json.put("currpage", curPage);
            json.put("shownum", mShowNum);
            json.put("is_hot", 1);//只显示热卖
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
        HttpUtil.doPostRequest(UrlsConstant.GOODS_LIST, map, new RequestCallBack<String>()

                {
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
                                no_data_rl.setVisibility(View.VISIBLE);
                                load_data_rl.setVisibility(View.GONE);
                                Intent intent = new Intent(ShopHotActivity.this, LoginActivity.class);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                intent.putExtra("action", 0);
                                startActivity(intent);
                                finish();
                                return;
                            }else if (serverMsg.equals("10012")) {    //重新握手
                                PublicUtils.handshake(ShopHotActivity.this, new ShakeHandCallback() {
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
                                    mTotalPage = object.getIntValue("pages");
                                    mTotalItem = object.getIntValue("itemsum");
                                    JSONArray array = object.getJSONArray("list");
                                    if (array.size() > 0) {
                                        com.alibaba.fastjson.JSONObject jsonObject1 = null;
                                        Goods goods = null;
                                        for (int i = 0; i < array.size(); i++) {
                                            jsonObject1 = array.getJSONObject(i);
                                            goods = new Goods();
                                            goods.setGoods_id(jsonObject1.getIntValue("goods_id"));
                                            goods.setGoods_depot_id(jsonObject1.getString("goods_depot_id"));
                                            goods.setName(jsonObject1.getString("name"));
                                            goods.setFormat(jsonObject1.getString("format"));
                                            goods.setPicpath(jsonObject1.getString("path"));
                                            goods.setCategory_name1(jsonObject1.getString("category_name1"));
                                            goods.setCategory_name2(jsonObject1.getString("category_name1"));
                                            goods.setHot(jsonObject1.getIntValue("hot"));
                                            goods.setRecommend(jsonObject1.getIntValue("recommend"));
                                            goods.setInventory(jsonObject1.getIntValue("inventory"));
                                            goods.setPrice(jsonObject1.getString("price"));
                                            goods.setStatus(jsonObject1.getIntValue("status"));
                                            goods.setInitial_price(jsonObject1.getString("initial_price"));
                                            goods.setDealer_price(jsonObject1.getString("dealer_price"));
                                            goods.setGoods_category1_id(jsonObject1.getIntValue("category_id1"));
                                            goods.setGoods_category2_id(jsonObject1.getIntValue("category_id2"));
                                            goodses.add(goods);
                                            goods = null;
                                            jsonObject1 = null;
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

                        if (hotAdapter.getCount() < mTotalItem) {
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

                        hotAdapter.notifyDataSetChanged();
                        isLoading = false;
                        load_data_rl.setVisibility(View.GONE);

                        if (isRefreshing) {
                            refresh_srl.setRefreshing(false);
                            isRefreshing = false;
                            recommend_good_lv.setEnabled(true);
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
                                    more_ll.setVisibility(View.GONE);
                                }
                            });
                        }
                        isLoading = false;

                        if (isRefreshing) {
                            refresh_srl.setRefreshing(false);
                            isRefreshing = false;
                            recommend_good_lv.setEnabled(true);
                        }
                    }
                }

        );
    }

    public void back(View view) {
        finish();
    }

    public void add(View view) {
        Intent intent = new Intent(this, AddHotGoodsActivity.class);
        startActivityForResult(intent, REQUEST_ADD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD) {

                mCurPage = 1;
                goodses.clear();
                mTotalItem = 0;
                loadData(mCurPage);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyHotAdapter extends BaseAdapter {
        Context mContext;

        public MyHotAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return goodses.size();
        }

        @Override
        public Object getItem(int position) {
            return goodses.get(position);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_goods_list, null);
                holder.goods_iv = (ImageView) convertView.findViewById(R.id.goods_iv);
                holder.goods_name_tv = (TextView) convertView.findViewById(R.id.goods_name_tv);
                holder.goods_price_tv = (TextView) convertView.findViewById(R.id.goods_price_tv);
                holder.goods_initial_price_tv = (TextView) convertView.findViewById(R.id.goods_initial_price_tv);
                holder.goods_format_tv = (TextView) convertView.findViewById(R.id.goods_format_tv);
                holder.goods_inventory_tv = (TextView) convertView.findViewById(R.id.goods_inventory_tv);
                holder.right_rl = (RelativeLayout) convertView.findViewById(R.id.right_rl);
                holder.left_rl = (RelativeLayout) convertView.findViewById(R.id.left_rl);
                holder.right_rl.setId(position);

                holder.goods_status_tv=(TextView)convertView.findViewById(R.id.goods_status_tv);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.right_rl.setId(position);
            }
            if (goodses.size() > 0) {
                Picasso.with(mContext).load(goodses.get(position).getPicpath())
                        .resize(100,100).centerCrop()
                        .placeholder(R.mipmap.tu)
                        .error(R.mipmap.tu)
                        .into(holder.goods_iv);
                holder.goods_name_tv.setText(goodses.get(position).getName());
                holder.goods_price_tv.setText("￥" + goodses.get(position).getPrice());
                holder.goods_initial_price_tv.setText("￥" + goodses.get(position).getDealer_price());
                holder.goods_format_tv.setText("规格:" + goodses.get(position).getFormat());
                holder.goods_inventory_tv.setText("库存:" + goodses.get(position).getInventory());

                holder.goods_status_tv.setVisibility(View.VISIBLE);
                if(goodses.get(position).getStatus()==1)
                {
                    holder.goods_status_tv.setText("在架上");
                }else if(goodses.get(position).getStatus()==0)
                {
                    holder.goods_status_tv.setText("已下架");

                }else if(goodses.get(position).getStatus()==-1)
                {
                    holder.goods_status_tv.setText("已删除");

                }else if(goodses.get(position).getInventory()==0)
                {
                    holder.goods_status_tv.setText("已售罄");
                }
            }

            //LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            //LinearLayout.LayoutParams.MATCH_PARENT);
            //holder.left_rl.setLayoutParams(lp1);
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(recommend_good_lv.getRightViewWidth(), LinearLayout.LayoutParams.MATCH_PARENT);
            holder.right_rl.setLayoutParams(lp2);

            holder.right_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = v.getId();
                    Message msg = handler.obtainMessage(DELETE);
                    msg.arg1 = position;
                    handler.sendMessage(msg);
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView goods_iv;
            TextView goods_name_tv;
            TextView goods_price_tv;
            TextView goods_initial_price_tv;
            TextView goods_format_tv;
            TextView goods_inventory_tv;
            TextView goods_status_tv;

            RelativeLayout right_rl;
            RelativeLayout left_rl;
        }
    }


}
