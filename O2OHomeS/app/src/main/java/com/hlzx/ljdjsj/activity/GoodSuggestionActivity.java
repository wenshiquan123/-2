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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.Suggetion;
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

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class GoodSuggestionActivity extends BaseActivity implements AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.suggestion_lv)
    private ListView suggestion_lv;

    //加载数据
    @ViewInject(R.id.load_linear_data)
    RelativeLayout load_data_rl;
    //没有数据
    @ViewInject(R.id.rela_no_data)
    RelativeLayout no_data_rl;

    //下拉刷新
    @ViewInject(R.id.refresh_srl)
    private SwipeRefreshLayout refreshLayout;

    //当前页码
    private int mCurPage = 1;
    //显示的条数
    private final int mShowNum = 10;
    //条目总数
    private static int mTotal = 0;
    //是否正在加载
    private boolean isLoading = false;
    //是否正在刷新
    private boolean isRefreshing = false;

    //是否有更多数据
    private boolean hasMore = false;
    //是否是最后一行
    private boolean isLastRow = false;

    View moreView;
    TextView loadMore_tv;
    ProgressBar loadProgress_pb;
    GoodsSuggestionAdapter adapter = null;
    ArrayList<Suggetion> suggetions = new ArrayList<Suggetion>();

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_good_suggestion);
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
        title_tv.setText("商品建议");

        moreView = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) moreView.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) moreView.findViewById(R.id.pb_load_progress);

        adapter = new GoodsSuggestionAdapter(this);
        moreView.setVisibility(View.GONE);
        suggestion_lv.addFooterView(moreView);
        suggestion_lv.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(this);
        suggestion_lv.setOnScrollListener(this);

        mCurPage = 1;
        loadData(mCurPage);

    }

    @Override
    public void onRefresh() {
        if (isLoading) {
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
        mCurPage = 1;
        mTotal = 0;
        suggetions.clear();
        isRefreshing = true;
        suggestion_lv.setEnabled(false);
        loadData(mCurPage);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (!isLoading && hasMore) {
                loadMore_tv.setText("正在加载");
                loadProgress_pb.setVisibility(View.VISIBLE);
                moreView.setVisibility(View.VISIBLE);
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
     * @param curPage 加载当前页
     */
    private void loadData(int curPage) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
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
        isLoading = true;
        if(mCurPage==1)
        {
            LogUtil.e("ME","curpage="+mCurPage);
            load_data_rl.setVisibility(View.VISIBLE);
        }
        HttpUtil.doPostRequest(UrlsConstant.GOODS_SUGGESTION, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "商品建议=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String text = jsonObject.getString("text");
                    String data = jsonObject.getString("data");
                    String iv = jsonObject.getString("iv");

                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        no_data_rl.setVisibility(View.VISIBLE);
                        load_data_rl.setVisibility(View.GONE);
                        Intent intent = new Intent(GoodSuggestionActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        finish();

                        MyApplication.getInstance().getUserInfo().setToken("");
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(GoodSuggestionActivity.this, new ShakeHandCallback() {
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
                                        moreView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                        String deData = null;
                        deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                        LogUtil.e("ME", "解密数据=" + deData);
                        if (deData != null)
                        {
                            com.alibaba.fastjson.JSONObject jsonObject1 = JSON.parseObject(deData);
                            mCurPage = jsonObject1.getIntValue("currpage");
                            mTotal = jsonObject1.getIntValue("itemsum");
                            JSONArray lists = jsonObject1.getJSONArray("list");
                            if (mTotal > 0) {
                                Suggetion suggetion = null;
                                com.alibaba.fastjson.JSONObject object = null;
                                for (int i = 0; i < lists.size(); i++) {
                                    suggetion = new Suggetion();
                                    object = lists.getJSONObject(i);
                                    suggetion.setContent(object.getString("content"));
                                    suggetion.setCtime(object.getString("ctime"));
                                    suggetions.add(suggetion);
                                    suggetion = null;
                                    object = null;
                                }
                            }
                        }else
                        {
                            if (mCurPage == 1) {
                                no_data_rl.setVisibility(View.VISIBLE);
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
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                        loadMore_tv.setText("没有更多数据了");
                        loadProgress_pb.setVisibility(View.GONE);
                        moreView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception ex) {
                    //showToast(ex.toString());
                    LogUtil.e("ME","mCurPage"+mCurPage);
                    if(mCurPage == 1) {
                        no_data_rl.setVisibility(View.VISIBLE);
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

                isLoading = false;
                if (adapter.getCount() < mTotal) {
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
                if (mTotal == 0) {
                    if(mCurPage == 1) {
                        no_data_rl.setVisibility(View.VISIBLE);
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
                    no_data_rl.setVisibility(View.GONE);
                }
                load_data_rl.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                if (isRefreshing) {
                    refreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    suggestion_lv.setEnabled(true);
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                showToast(s);
                load_data_rl.setVisibility(View.GONE);
                isLoading = false;
                if (mCurPage == 1) {
                    no_data_rl.setVisibility(View.VISIBLE);
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
                    suggestion_lv.setEnabled(true);
                }
            }
        });

    }

    public void back(View view) {
        finish();
    }

    class GoodsSuggestionAdapter extends BaseAdapter {
        Context mContext;

        public GoodsSuggestionAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return suggetions.size();
        }

        @Override
        public Object getItem(int position) {
            return suggetions.get(position);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_goods_suggestion, null);
                holder.content_tv = (TextView) convertView.findViewById(R.id.content_tv);
                holder.ctime_tv = (TextView) convertView.findViewById(R.id.ctime_tv);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (suggetions.size() > 0) {
                holder.content_tv.setText(suggetions.get(position).getContent());
                holder.ctime_tv.setText(suggetions.get(position).getCtime());
            }

            return convertView;
        }

        class ViewHolder {
            TextView content_tv;
            TextView ctime_tv;
        }

    }
}
