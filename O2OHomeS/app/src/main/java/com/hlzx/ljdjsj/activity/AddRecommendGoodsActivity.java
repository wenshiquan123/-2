package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.Category;
import com.hlzx.ljdjsj.bean.Goods;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.common.ScreenManager;
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
public class AddRecommendGoodsActivity extends BaseActivity implements View.OnClickListener,
        android.os.Handler.Callback, AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.status_tv)
    private TextView status_tv;
    @ViewInject(R.id.category_tv)
    private TextView category_tv;

    @ViewInject(R.id.status_ll)
    private LinearLayout status_ll;
    @ViewInject(R.id.category_ll)
    private LinearLayout category_ll;

    @ViewInject(R.id.goods_lv)
    private ListView goods_lv;
    @ViewInject(R.id.selected_img_ll)
    private LinearLayout selected_img_ll;

    @ViewInject(R.id.mark1_iv)
    private ImageView mark1_iv;
    @ViewInject(R.id.mark2_iv)
    private ImageView mark2_iv;

    @ViewInject(R.id.confirm_bt)
    private Button confirm_bt;

    Dialog waitDialog = null;

    String[] allStr = {
            "全部", "在架上", "已下架", "售罄"
    };

    ArrayList<String> allStatus = new ArrayList<String>();
    ArrayList<Category> allCategoryList = new ArrayList<Category>();

    MyPopAdapter adapterStatus;
    MyPopAdapter2 adapterAllCategory;

    Handler handler = new Handler(this);

    private static final int UPDATE_CATEGORY = 1001;
    //添加商品
    private static final int ADD_RECOMMEND = 1002;

    private static final int UPDATE_UI_PIC = 1003;
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
    //是否正刷新
    private boolean isRefreshing = false;

    //是否是加载分类
    private boolean isLoadCategory = false;

    MyGoodsListAdapter goodsListAdapter = null;
    ArrayList<Goods> goodses = new ArrayList<Goods>();

    //提交goods_id
    ArrayList<Integer> goods_ids = new ArrayList<>();

    //索引
    int mIndex = 0;
    FrameLayout.LayoutParams lp = null;
    View imgView;
    ImageView goods_iv;
    ImageView detele_iv;
    //已添加的
    ArrayList<View> views = new ArrayList<>();

    @ViewInject(R.id.refresh_srl)
    private SwipeRefreshLayout refresh_srl;

    @ViewInject(R.id.selected_img_hsv)
    private HorizontalScrollView selected_img_hsv;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_add_marketing_goods);
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
        title_tv.setText("添加商品");
        lp = new FrameLayout.LayoutParams(
                ScreenManager.dp2px(AddRecommendGoodsActivity.this, 100),
                ScreenManager.dp2px(AddRecommendGoodsActivity.this, 100));
        lp.setMargins(10, 0, 0, 0);

        String format = getResources().getString(R.string.recommend);
        String tempStr = String.format(format, 0);
        confirm_bt.setText(tempStr);

        more_ll = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) more_ll.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) more_ll.findViewById(R.id.pb_load_progress);

        status_ll.setOnClickListener(this);
        category_ll.setOnClickListener(this);
        confirm_bt.setOnClickListener(this);
        refresh_srl.setOnRefreshListener(this);


        //请求分类
        loadCategory();

        for (String str : allStr) {
            allStatus.add(str);
        }

        adapterStatus = new MyPopAdapter(allStatus);
        adapterAllCategory = new MyPopAdapter2(this);
        goodsListAdapter = new MyGoodsListAdapter(this);

        more_ll.setVisibility(View.GONE);
        goods_lv.addFooterView(more_ll);
        goods_lv.setAdapter(goodsListAdapter);
        goods_lv.setOnItemClickListener(goodsItemClickListener);
        goods_lv.setOnScrollListener(this);
    }

    AdapterView.OnItemClickListener goodsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(position==goodses.size())
            {
                return;
            }

            int goods_id = goodses.get(position).getGoods_id();
            Message msg = handler.obtainMessage(UPDATE_UI_PIC);
            msg.arg1 = goods_id;
            msg.obj = goodses.get(position).getPicpath();
            handler.sendMessage(msg);
        }
    };


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
        goods_lv.setEnabled(false);
        loadData(mCurPage);
    }

    private void loadCategory() {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            no_data_rl.setVisibility(View.VISIBLE);
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("category_id", 0);//0表示全部
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpUtil.doPostRequest(UrlsConstant.GOODS_CATEGORY_LIST, map, new RequestCallBack<String>() {
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

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(AddRecommendGoodsActivity.this, LoginActivity.class);
                        intent.putExtra("action", 0);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                        MyApplication.getInstance().getUserInfo().setToken("");
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        String deData = null;
                        try {
                            deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                            if (deData != null) {
                                LogUtil.e("ME", "解密数据=" + deData);

                                Message msg = handler.obtainMessage(UPDATE_CATEGORY);
                                msg.obj = (String) deData;
                                handler.sendMessage(msg);

                            }
                        } catch (Exception e) {
                            showToast(e.toString());
                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                        no_data_rl.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException ex) {
                    showToast(ex.toString());
                    no_data_rl.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {

                showToast(s);
                no_data_rl.setVisibility(View.VISIBLE);
            }
        });
    }


    /**
     * 加载数据
     *
     * @param curPage
     */
    private void loadData(int curPage) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            no_data_rl.setVisibility(View.VISIBLE);
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
            json.put("is_recommend", 2);//显示非推荐
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isLoadCategory) {
            if (waitDialog == null) {
                waitDialog = PublicDialog.createLoadingDialog(this, "正在加载...");
                waitDialog.show();
            }
            isLoadCategory = false;
        }
        if (mCurPage == 1) {
            load_data_rl.setVisibility(View.VISIBLE);
        }
        isLoading = true;
        HttpUtil.doPostRequest(UrlsConstant.GOODS_LIST, map, new RequestCallBack<String>() {
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
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(AddRecommendGoodsActivity.this, LoginActivity.class);
                        intent.putExtra("action", 0);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                        MyApplication.getInstance().getUserInfo().setToken("");
                        return;
                    }else if (serverMsg.equals("10012")) {    //重新握手
                        PublicUtils.handshake(AddRecommendGoodsActivity.this, new ShakeHandCallback() {
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

                if (goodsListAdapter.getCount() < mTotalItem) {
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

                goodsListAdapter.notifyDataSetChanged();
                isLoading = false;
                load_data_rl.setVisibility(View.GONE);
                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }

                if (isRefreshing) {
                    refresh_srl.setRefreshing(false);
                    isRefreshing = false;
                    goods_lv.setEnabled(true);
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
                            more_ll.setVisibility(View.GONE);
                        }
                    });
                }
                isLoading = false;

                if (isRefreshing) {
                    refresh_srl.setRefreshing(false);
                    isRefreshing = false;
                    goods_lv.setEnabled(true);
                }
            }
        });
    }

    /**
     * 添加推荐
     *
     * @param goods_ids
     */
    private void AddRecommend(ArrayList<Integer> goods_ids) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < goods_ids.size(); i++) {
            if (i == (goods_ids.size() - 1)) {
                sb.append(goods_ids.get(i) + "");
            } else {
                sb.append(goods_ids.get(i) + ",");
            }
        }
        LogUtil.e("ME", "recommend=" + sb.toString());


        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type", 1);
            json.put("goods_ids",sb.toString());//0表示全部
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            //LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (waitDialog == null) {
            waitDialog = PublicDialog.createLoadingDialog(this, "正在提交...");
            waitDialog.show();
        }

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
                        finish();
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
    public void finish() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            LogUtil.e("ME", "哈哈哈");
            if (!isLoading && hasMore) {
                loadMore_tv.setText("加载更多数据");
                loadProgress_pb.setVisibility(View.VISIBLE);
                more_ll.setVisibility(View.VISIBLE);

                //goods_lv.removeFooterView(more_ll);
                //goods_lv.addFooterView(more_ll);
                //goods_lv.setAdapter(goodsListAdapter);
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

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == UPDATE_CATEGORY) {
            String deData = (String) msg.obj;
            LogUtil.e("ME", "handle=" + deData);
            try {
                com.alibaba.fastjson.JSONObject object = JSON.parseObject(deData);
                JSONArray array1 = object.getJSONArray("list1");
                if (array1.size() > 0) {
                    com.alibaba.fastjson.JSONObject temp1 = null;
                    Category category1 = null;
                    allCategoryList.clear();
                    Category temp = new Category();
                    temp.setCategory_id(0);
                    temp.setCategory_name("全部分类");
                    allCategoryList.add(temp);
                    for (int i = 0; i < array1.size(); i++) {
                        category1 = new Category();
                        temp1 = array1.getJSONObject(i);
                        category1.setCategory_id(temp1.getIntValue("goods_category_id"));
                        category1.setCategory_name(temp1.getString("name"));
                        allCategoryList.add(category1);
                        category1 = null;
                        temp1 = null;
                    }
                }
            } catch (Exception e) {
            }
            adapterAllCategory.notifyDataSetChanged();
            mCurPage = 1;
            isLoadCategory = true;
            loadData(mCurPage);
        } else if (msg.what == UPDATE_UI_PIC) {
            imgView = LayoutInflater.from(this).inflate(R.layout.item_selected_recommend, null);
            goods_iv = (ImageView) imgView.findViewById(R.id.goods_iv);
            goods_iv.setLayoutParams(lp);
            detele_iv = (ImageView) imgView.findViewById(R.id.delete_iv);
            detele_iv.setId(msg.arg1);
            detele_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeView(views, v.getId());
                    LogUtil.e("ME", "id=" + v.getId());
                }
            });
            Picasso.with(this).load((String) msg.obj)
            .resize(100,100).centerCrop()
            .placeholder(R.mipmap.tu).error(R.mipmap.tu).into(goods_iv);
            if (goods_ids.contains(msg.arg1)) {
                showToast("已经选择了");
                return false;
            }
            imgView.setId(msg.arg1);
            selected_img_ll.addView(imgView);
            views.add(imgView);
            goods_ids.add(msg.arg1);
            String format = getResources().getString(R.string.recommend);
            String str = String.format(format, goods_ids.size());
            confirm_bt.setText(str);

            int screenWidth=ScreenManager.getScreenWidth(this);
            int contentWidth=goods_ids.size()*(ScreenManager.dp2px(this,100)+10);
            if(contentWidth>=screenWidth)
            {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        selected_img_hsv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                },100);
            }
            LogUtil.e("ME","scroll="+contentWidth+";screenWidth="+screenWidth);

        }
        return false;
    }

    private void removeView(ArrayList<View> views, int id) {
        for (int i = 0; i < views.size(); i++) {
            if (views.get(i).getId() == id) {
                views.remove(i);
                goods_ids.remove((Integer) id);
                String format = getResources().getString(R.string.recommend);
                String str = String.format(format, goods_ids.size());
                confirm_bt.setText(str);
            }
        }
        updateView(views);
    }

    private void updateView(ArrayList<View> views) {
        selected_img_ll.removeAllViews();
        for (int i = 0; i < views.size(); i++) {
            selected_img_ll.addView(views.get(i));
        }


    }

    AdapterView.OnItemClickListener statusItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //0-全部，1-在架上，2-已下架，3-已售罄
            if (position == 0) {
                status = 0;
            } else if (position == 1) {
                status = 1;
            } else if (position == 2) {
                status = 2;
            } else if (position == 3) {
                status = 3;
            }
            status_tv.setText(allStr[status]);
            goodses.clear();
            mCurPage = 1;
            mTotalPage = 0;
            mTotalItem = 0;
            loadData(mCurPage);

            if(popupWindow1!=null)
            {
                popupWindow1.dismiss();
            }
            if(popupWindow2!=null)
            {
                popupWindow2.dismiss();
            }

        }
    };

    AdapterView.OnItemClickListener categoryItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            category_tv.setText(allCategoryList.get(position).getCategory_name());
            category_id = allCategoryList.get(position).getCategory_id();
            goodses.clear();
            mCurPage = 1;
            mTotalPage = 0;
            mTotalItem = 0;
            loadData(mCurPage);

            if(popupWindow1!=null)
            {
                popupWindow1.dismiss();
            }
            if(popupWindow2!=null)
            {
                popupWindow2.dismiss();
            }

        }
    };

    PopupWindow popupWindow1=null;
    PopupWindow popupWindow2=null;
    @Override
    public void onClick(View v) {
        if (v == status_ll) {
            mark1_iv.setImageResource(R.drawable.zk);
            popupWindow1 = showPopAll(adapterStatus, statusItemClick, status_ll, 10, 0, 2);
            popupWindow1.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mark1_iv.setImageResource(R.drawable.sq);
                }
            });

        } else if (v == category_ll) {
            mark2_iv.setImageResource(R.drawable.zk);
            popupWindow2 = showPopAll(adapterAllCategory, categoryItemClick, category_ll, 20, 0, 1);
            popupWindow2.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mark2_iv.setImageResource(R.drawable.sq);
                }
            });
        } else if (v == confirm_bt) {
            AddRecommend(goods_ids);
        }
    }

    public PopupWindow showPopAll(BaseAdapter adapter, AbsListView.OnItemClickListener itemClickListener, View dropView, int offX, int offY, int type) {
        View view = LayoutInflater.from(this).inflate(R.layout.pop_all, null);
        ListView listView = (ListView) view.findViewById(R.id.pop_lv);
        listView.setAdapter(adapter);
        if (itemClickListener != null)
            listView.setOnItemClickListener(itemClickListener);
        PopupWindow mPopAllCategory;
        if (type == 1) {
            mPopAllCategory = new PopupWindow(view, 300, ScreenManager.dp2px(this, 200), true);
        } else {
            mPopAllCategory = new PopupWindow(view, 300, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        }
        mPopAllCategory.setBackgroundDrawable(new BitmapDrawable());
        mPopAllCategory.setFocusable(true);
        mPopAllCategory.setOutsideTouchable(true);
        mPopAllCategory.showAsDropDown(dropView, offX, offY);
        mPopAllCategory.update();
        return mPopAllCategory;
    }

    public void back(View view) {
        finish();
    }

    /***
     * pop列表适配器
     */
    public class MyPopAdapter extends BaseAdapter {
        ArrayList<String> mData;

        public MyPopAdapter(ArrayList<String> data) {
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
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
                convertView = LayoutInflater.from(AddRecommendGoodsActivity.this).inflate(R.layout.item_pop, null);
                holder.cagetory_tv = (TextView) convertView.findViewById(R.id.category_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.cagetory_tv.setText(mData.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView cagetory_tv;
        }
    }

    public class MyPopAdapter2 extends BaseAdapter {

        Context mContext;

        public MyPopAdapter2(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return allCategoryList.size();
        }

        @Override
        public Object getItem(int position) {
            return allCategoryList.get(position);
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
                convertView = LayoutInflater.from(AddRecommendGoodsActivity.this).inflate(R.layout.item_pop, null);
                holder.cagetory_tv = (TextView) convertView.findViewById(R.id.category_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (allCategoryList.size() > 0) {
                holder.cagetory_tv.setText(allCategoryList.get(position).getCategory_name());
            }
            return convertView;
        }
        class ViewHolder {
            TextView cagetory_tv;
        }
    }

    /**
     * 商品适配器
     */
    class MyGoodsListAdapter extends BaseAdapter {
        Context mContext;

        public MyGoodsListAdapter(Context context) {
            this.mContext = context;
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
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
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
