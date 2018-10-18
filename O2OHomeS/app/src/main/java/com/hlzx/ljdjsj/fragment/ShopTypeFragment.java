package com.hlzx.ljdjsj.fragment;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
 *
 */
public class ShopTypeFragment extends OrderBaseFragment implements android.os.Handler.Callback,
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    private int category_id = 0;
    private String category_name = "";

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
    //是否正在刷新
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
    private LinearLayout more_ll;
    private TextView loadMore_tv;
    private ProgressBar loadProgress_pb;

    //gridview
    private GridView goods_gv;
    //请求码
    private static final int REQUEST_CODE_EDIT = 1001;

    ArrayList<Goods> mGoodList = new ArrayList<Goods>();

    Handler handler = new Handler(this);
    GoodsAdapter adapter = null;

    private static final int UPDATE_UI = 1001;

    //下拉
    SwipeRefreshLayout refreshLayout = null;

    public ShopTypeFragment(){
    }

    public ShopTypeFragment(int category_id, String category_name) {
        this.category_id = category_id;
        this.category_name = category_name;
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mGoodList.get(position).getGoods_id() == 0) {
                Intent intent = new Intent(getActivity(), AddInventoryGoodActivity.class);
                intent.putExtra("good", mGoodList.get(position));
                LogUtil.e("ME", "分类1=" + mGoodList.get(position).getCategory_name1() + ";分类2="
                        + mGoodList.get(position).getCategory_name2());
                startActivityForResult(intent, REQUEST_CODE_EDIT);
            } else {
                Intent intent = new Intent(getActivity(), EditGoodActivity.class);
                intent.putExtra("good", mGoodList.get(position));
                LogUtil.e("ME", "分类1=" + mGoodList.get(position).getCategory_name1() + ";分类2="
                        + mGoodList.get(position).getCategory_name2());
                intent.putExtra("flag", true);//是否加载详情
                startActivityForResult(intent, REQUEST_CODE_EDIT);
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_shoptype,null);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_srl);
        refreshLayout.setOnRefreshListener(this);

        goods_gv = (GridView) view.findViewById(R.id.goods_gv);
        adapter = new GoodsAdapter(getActivity());
        goods_gv.setAdapter(adapter);
        goods_gv.setOnScrollListener(this);
        goods_gv.setOnItemClickListener(itemClickListener);

        more_ll = (LinearLayout) view.findViewById(R.id.more_ll);
        loadMore_tv = (TextView) view.findViewById(R.id.tv_load_more);
        loadProgress_pb = (ProgressBar) view.findViewById(R.id.pb_load_progress);
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

        mGoodList.clear();
        mCurPage = 1;
        mTotalItem = 0;
        mTotalPage = 0;
        isRefreshing = true;
        goods_gv.setEnabled(false);
        loadData(mCurPage);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == UPDATE_UI) {
            adapter.notifyDataSetChanged();
        }
        return false;
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
        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
            isLastRow = true;
        }
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        LogUtil.e("ME", "fragment:category_id=" + category_id + ";category_name=" + category_name);
        mGoodList.clear();
        mCurPage = 1;
        mTotalItem = 0;
        mTotalPage = 0;
        loadData(mCurPage);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int flag = data.getIntExtra("flag", 0);
        LogUtil.e("ME", "flag=" + flag);
        if (requestCode == REQUEST_CODE_EDIT) {
            if (resultCode == getActivity().RESULT_OK) {
                if (flag == AddInventoryGoodActivity.Action.REFREDHING.getValue()) {
                    mGoodList.clear();
                    mCurPage = 1;
                    mTotalItem = 0;
                    mTotalPage = 0;
                    loadData(mCurPage);
                }
            }
        }
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
            json.put("category_id", category_id);//0表示全部
            json.put("currpage", curPage);
            json.put("shownum", mShowNum);
            json.put("is_recommend", 1);

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
                                    goods.setGoods_depot_id(jsonObject1.getString("goods_depot_id"));
                                    goods.setName(jsonObject1.getString("name"));
                                    goods.setFormat(jsonObject1.getString("format"));
                                    goods.setPicpath(jsonObject1.getString("picpath"));
                                    goods.setCategory_name1(jsonObject1.getString("category_name1"));
                                    goods.setCategory_name2(jsonObject1.getString("category_name2"));
                                    goods.setGoods_id(jsonObject1.getIntValue("goods_id"));
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

                Message msg=handler.obtainMessage(UPDATE_UI);
                msg.sendToTarget();
                //msg.recycle();
                //adapter.notifyDataSetChanged();

                isLoading = false;
                more_ll.setVisibility(View.GONE);
                load_data_rl.setVisibility(View.GONE);

                if (isRefreshing) {
                    refreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    goods_gv.setEnabled(true);
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
                    goods_gv.setEnabled(true);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_goods, null);
                holder.pic_iv = (ImageView) convertView.findViewById(R.id.pic_iv);
                holder.name_tv = (TextView) convertView.findViewById(R.id.goods_name_tv);
                holder.format_tv = (TextView) convertView.findViewById(R.id.format_tv);
                holder.mark_iv = (ImageView) convertView.findViewById(R.id.mark_iv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (mGoodList.size() > 0) {
                if (mGoodList.get(position).getName() != null && !mGoodList.get(position).getName().equals(""))
                    holder.name_tv.setText(mGoodList.get(position).getName());
                if (mGoodList.get(position).getFormat() != null && !mGoodList.get(position).getFormat().equals(""))
                    holder.format_tv.setText(mGoodList.get(position).getFormat());
                if (mGoodList.get(position).getPicpath() != null && !mGoodList.get(position).getPicpath().equals(""))
                {
                    Picasso.with(mContext).load(mGoodList.get(position).getPicpath())
                            .resize(100,100).centerCrop()
                            .placeholder(R.mipmap.tu)
                            .error(R.mipmap.tu)
                            .into(holder.pic_iv);
                }
                if (mGoodList.get(position).getGoods_id() == 0)//未添加
                {
                    holder.mark_iv.setImageResource(R.color.transparent);
                } else {
                    holder.mark_iv.setImageResource(R.mipmap.added);//已添加
                }
            }
            LogUtil.e("ME", "getView" + position);
            return convertView;
        }

        class ViewHolder {
            ImageView pic_iv;
            TextView name_tv;
            TextView format_tv;
            ImageView mark_iv;
        }

    }
}
