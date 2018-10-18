package com.hlzx.ljdjsj.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
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
import com.hlzx.ljdjsj.bean.Evaluate;
import com.hlzx.ljdjsj.bean.Suggetion;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.hlzx.ljdjsj.view.CircleImageView;
import com.hlzx.ljdjsj.view.ScoreView;
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
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class BuyerEvaluateActivity extends BaseActivity implements android.os.Handler.Callback,
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.evaluate_lv)
    private ListView evaluate_lv;

    //加载数据
    @ViewInject(R.id.load_linear_data)
    RelativeLayout load_data_rl;
    //没有数据
    @ViewInject(R.id.rela_no_data)
    RelativeLayout no_data_rl;

    @ViewInject(R.id.swipe_srl)
    SwipeRefreshLayout swipe_srl;

    //评价条数
    @ViewInject(R.id.items_tv)
    private TextView items_tv;


    //每页显示的条数
    private static final int mShowNum = 10;
    //当前页
    private static int mCurPage = 0;
    private static int mTotal = 0;//记录总数

    //是否是最后一行
    private boolean isLastRow = false;
    //是否正在刷新
    private boolean isRefreshing = false;
    //是否正在加载
    private boolean isLoading = false;
    //是否有更多数据
    private boolean hasMore = false;

    View moreView;
    TextView loadMore_tv;
    ProgressBar loadProgress_pb;
    List<Evaluate> evaluates = new ArrayList<Evaluate>();
    MyEvaluateAdapter adapter = null;

    Handler handler = new Handler(this);

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_buyer_evaluate);
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
        title_tv.setText("买家评价");

        moreView = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null);
        loadMore_tv = (TextView) moreView.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) moreView.findViewById(R.id.pb_load_progress);
        swipe_srl.setOnRefreshListener(this);
        adapter = new MyEvaluateAdapter(this);

        moreView.setVisibility(View.GONE);
        evaluate_lv.addFooterView(moreView);
        evaluate_lv.setAdapter(adapter);
        evaluate_lv.setOnScrollListener(this);

        mCurPage = 1;
        loadData(mCurPage);
    }

    @Override
    public boolean handleMessage(Message msg) {

        return false;
    }

    @Override
    public void onRefresh() {
        if (isLoading) {
            swipe_srl.setRefreshing(false);
            isRefreshing = false;
            return;
        }
        if (isRefreshing) {
            return;
        }
        mCurPage = 1;
        mTotal = 0;
        evaluates.clear();
        isRefreshing = true;
        evaluate_lv.setEnabled(false);
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

    private void loadData(int curPage) {

        //判断网络
        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        String edata1 = "";
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("currpage", curPage);
            json.put("shownum", mShowNum);
            edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("ME", e.toString());
        }

        isLoading = true;
        if (mCurPage == 1) {
            load_data_rl.setVisibility(View.VISIBLE);
        }
        HttpUtil.doPostRequest(UrlsConstant.ORDER_COMMENT_LIST, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

                String result = responseInfo.result.toString();
                LogUtil.e("ME", "order=" + result);

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
                        Intent intent = new Intent(BuyerEvaluateActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        finish();
                        return;
                    } else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(BuyerEvaluateActivity.this, new ShakeHandCallback() {
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
                        if (deData != null) {
                            LogUtil.e("ME", "解密=" + deData);
                            com.alibaba.fastjson.JSONObject jsonObject1 = JSON.parseObject(deData);
                            mCurPage = jsonObject1.getIntValue("currpage");
                            mTotal = jsonObject1.getIntValue("itemsum");
                            JSONArray lists = jsonObject1.getJSONArray("list");
                            if (mTotal > 0) {
                                Evaluate evaluate = null;
                                com.alibaba.fastjson.JSONObject object = null;
                                for (int i = 0; i < lists.size(); i++) {
                                    evaluate = new Evaluate();
                                    object = lists.getJSONObject(i);
                                    evaluate.setDate(object.getString("ctime"));
                                    evaluate.setServe_quality(object.getIntValue("quality"));
                                    evaluate.setExpress_v(object.getIntValue("speed"));
                                    evaluate.setServe_attiude(object.getIntValue("service"));
                                    evaluate.setContent(object.getString("content"));
                                    evaluate.setPicpath(object.getString("head_img"));
                                    evaluate.setUsername(object.getString("username"));
                                    evaluates.add(evaluate);
                                    evaluate = null;
                                    object = null;
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
                                            moreView.setVisibility(View.VISIBLE);
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
                                        moreView.setVisibility(View.VISIBLE);
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
                                    moreView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        hasMore = false;
                    }
                } catch (Exception ex) {
                    hasMore = false;
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
                } else {
                    no_data_rl.setVisibility(View.GONE);
                }
                load_data_rl.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        items_tv.setText("买家评价(" + mTotal + ")");
                    }
                });

                isLoading = false;
                if (isRefreshing) {
                    swipe_srl.setRefreshing(false);
                    isRefreshing = false;
                    evaluate_lv.setEnabled(true);
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
                            moreView.setVisibility(View.GONE);
                        }
                    });
                }

                isLoading = false;

                if (isRefreshing) {
                    swipe_srl.setRefreshing(false);
                    isRefreshing = false;
                    evaluate_lv.setEnabled(true);
                }
            }
        });

    }

    public void back(View view) {
        finish();
    }


    class MyEvaluateAdapter extends BaseAdapter {
        Context mContext;

        public MyEvaluateAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return evaluates.size();
        }

        @Override
        public Object getItem(int position) {
            return evaluates.get(position);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_evaluate, null);
                holder.date_tv = (TextView) convertView.findViewById(R.id.evaluate_date_tv);
                holder.quality_sv = (ScoreView) convertView.findViewById(R.id.quality_sv);
                holder.attitude_sv = (ScoreView) convertView.findViewById(R.id.attitude_sv);
                holder.speed_sv = (ScoreView) convertView.findViewById(R.id.speed_sv);
                holder.content_tv = (TextView) convertView.findViewById(R.id.content_tv);
                holder.buyer_iv = (CircleImageView) convertView.findViewById(R.id.buyer_iv);
                holder.content_ll = (LinearLayout) convertView.findViewById(R.id.content_ll);
                holder.buyer_name_tv = (TextView) convertView.findViewById(R.id.buyer_name_tv);
               // holder.buyer_phone_tv = (TextView) convertView.findViewById(R.id.buyer_phone_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (evaluates.size() > position) {
                holder.date_tv.setText(evaluates.get(position).getDate());
                Picasso.with(mContext).load(UrlsConstant.BASE_PIC_URL + evaluates.get(position).getPicpath())
                        .resize(100, 100).centerCrop()
                        .placeholder(R.mipmap.tu)
                        .error(R.mipmap.tu)
                        .into(holder.buyer_iv);

                if (evaluates.get(position).getContent() == null ||
                        evaluates.get(position).getContent().equals("")) {
                    holder.content_ll.setVisibility(View.GONE);
                } else {
                    holder.content_ll.setVisibility(View.VISIBLE);
                    holder.content_tv.setText(evaluates.get(position).getContent());
                }
                holder.quality_sv.setScore(evaluates.get(position).getServe_quality(), R.mipmap.star_dark, R.mipmap.star_bright);
                holder.speed_sv.setScore(evaluates.get(position).getExpress_v(), R.mipmap.star_dark, R.mipmap.star_bright);
                holder.attitude_sv.setScore(evaluates.get(position).getServe_attiude(), R.mipmap.star_dark, R.mipmap.star_bright);
                holder.buyer_name_tv.setText(evaluates.get(position).getUsername());
                //holder.buyer_phone_tv.setText(evaluates.get(position).getUser_phone());
            }
            return convertView;
        }

        class ViewHolder {
            TextView content_tv;
            TextView date_tv;
            ScoreView quality_sv;
            ScoreView speed_sv;
            ScoreView attitude_sv;
            CircleImageView buyer_iv;
            LinearLayout content_ll;
            TextView buyer_name_tv;
            TextView buyer_phone_tv;
        }

    }

}
