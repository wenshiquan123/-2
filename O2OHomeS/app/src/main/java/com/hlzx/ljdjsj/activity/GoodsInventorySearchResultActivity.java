package com.hlzx.ljdjsj.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class GoodsInventorySearchResultActivity extends BaseActivity implements AbsListView.OnScrollListener {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.goods_lv)
    private ListView goods_lv;

    @ViewInject(R.id.load_linear_data)
    private RelativeLayout load_data_rl;
    @ViewInject(R.id.rela_no_data)
    private RelativeLayout no_data_rl;

    //是否最后一行
    private boolean isLastRow = false;
    //是否正在加载
    private boolean isLoading = false;
    //有更多
    private boolean hasMore = false;

    //当前页码
    private int mCurPage = 1;
    //显示的条数
    private int mShowNum = 48;
    //总条数
    private int mTotalItem = 0;
    //总页数
    private int mTotalPage = 0;

    //加载更多
    private LinearLayout more_ll;
    private TextView loadMore_tv;
    private ProgressBar loadProgress_pb;

    MyGoodsAdapter adapter = null;
    ArrayList<Goods> mGoodList = new ArrayList<Goods>();
    //检索的关键词
    String keyword = "";

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_goods_inventory_search_result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        initView();
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position==mGoodList.size())
            {
                return;
            }
            Intent intent = new Intent(GoodsInventorySearchResultActivity.this, AddInventoryGoodActivity.class);
            intent.putExtra("good", mGoodList.get(position));
            startActivity(intent);
        }
    };

    @Override
    public void initView() {
        super.initView();
        title_tv.setText("搜索结果");
        Intent it = getIntent();
        keyword = it.getStringExtra("keyword");

        more_ll = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) more_ll.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) more_ll.findViewById(R.id.pb_load_progress);

        adapter = new MyGoodsAdapter(this);
        goods_lv.addFooterView(more_ll);
        goods_lv.setAdapter(adapter);
        goods_lv.setOnScrollListener(this);
        goods_lv.setOnItemClickListener(itemClickListener);
        mCurPage = 1;
        loadData(mCurPage);

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (!isLoading && hasMore) {

                loadMore_tv.setText("正在加载");
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

    public void loadData(int curPage) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("keyword", keyword);
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
        HttpUtil.doPostRequest(UrlsConstant.GOODS_DEPOT_LIST, map, new RequestCallBack<String>() {
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
                        showToast(text);
                        Intent intent = new Intent(GoodsInventorySearchResultActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    } else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(GoodsInventorySearchResultActivity.this, new ShakeHandCallback() {
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
                                    goods.setGoods_depot_id(jsonObject1.getString("goods_depot_id"));
                                    goods.setName(jsonObject1.getString("name"));
                                    goods.setFormat(jsonObject1.getString("format"));
                                    goods.setPicpath(jsonObject1.getString("picpath"));
                                    goods.setCategory_name1(jsonObject1.getString("category_name1"));
                                    goods.setCategory_name2(jsonObject1.getString("category_name2"));
                                    goods.setGoods_id(jsonObject1.getIntValue("goods_id"));
                                    goods.setInventory(jsonObject1.getIntValue("inventory"));
                                    goods.setPrice(jsonObject1.getString("price"));
                                    goods.setInitial_price(jsonObject1.getString("initial_price"));
                                    goods.setGoods_category1_id(jsonObject1.getIntValue("category_id1"));
                                    goods.setGoods_category2_id(jsonObject1.getIntValue("category_id2"));
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
                        //showToast(text);
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
                    showToast(ex.toString());
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
                more_ll.setVisibility(View.GONE);
                load_data_rl.setVisibility(View.GONE);
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
            }
        });
    }

    public void back(View view) {
        finish();
    }

    /**
     * 商品适配器
     */
    class MyGoodsAdapter extends BaseAdapter {
        Context mContext;

        public MyGoodsAdapter(Context context) {
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_goods_inventory_search_result, null);
                holder.goods_iv = (ImageView) convertView.findViewById(R.id.goods_iv);
                holder.goods_name_tv = (TextView) convertView.findViewById(R.id.goods_name_tv);
                holder.goods_price_tv = (TextView) convertView.findViewById(R.id.goods_price_tv);
                holder.goods_initial_price_tv = (TextView) convertView.findViewById(R.id.goods_initial_price_tv);
                holder.goods_format_tv = (TextView) convertView.findViewById(R.id.goods_format_tv);
                holder.goods_inventory_tv = (TextView) convertView.findViewById(R.id.goods_inventory_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (mGoodList.size() > 0) {
                Picasso.with(mContext).load(mGoodList.get(position).getPicpath())
                        .placeholder(R.mipmap.tu)
                        .error(R.mipmap.tu)
                        .into(holder.goods_iv);
                holder.goods_name_tv.setText(mGoodList.get(position).getName());
                holder.goods_format_tv.setText("规格:" + mGoodList.get(position).getFormat());

                //LogUtil.e("ME","path="+mGoodList.get(position).getPicpath());

            }
            return convertView;
        }

        class ViewHolder {
            ImageView goods_iv;
            TextView goods_name_tv;
            TextView goods_price_tv;
            TextView goods_initial_price_tv;
            TextView goods_format_tv;
            TextView goods_inventory_tv;
        }
    }
}
