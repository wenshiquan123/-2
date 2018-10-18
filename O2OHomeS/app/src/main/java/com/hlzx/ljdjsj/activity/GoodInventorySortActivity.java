package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseFragmentActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.Category;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.fragment.GoodSortFragment;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.hlzx.ljdjsj.view.OverScrollView;
import com.hlzx.ljdjsj.viewpagerindicator.TabPageIndicator;
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
 * Created by Administrator on 2015/11/12.
 */
public class GoodInventorySortActivity extends BaseFragmentActivity implements View.OnClickListener, Handler.Callback {

    //一级分类
    private ArrayList<Category> categories1 = new ArrayList<Category>();
    //二级分类
    private ArrayList<Category> categories2 = new ArrayList<Category>();

    private TextView mContentTextViews[];
    private TextView mMarkTextViews[];
    private View mViews[];

    @ViewInject(R.id.content_sv)
    private OverScrollView scrollView;

    @ViewInject(R.id.content_ll)
    private LinearLayout content_ll;

    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.snacks_ll)
    private LinearLayout snacks_ll;

    @ViewInject(R.id.mViewPager)
    private ViewPager viewPager;

    @ViewInject(R.id.indicator)
    private TabPageIndicator indicator;
    //选择的一级分类
    @ViewInject(R.id.category1_tv)
    private TextView category1_tv;

    //显示的分类编号
    private int mCategory = 0;
    ArrayList<GoodSortFragment> mFragments = new ArrayList<GoodSortFragment>();
    FragmentStatePagerAdapter adapter;

    private int index = 0;
    private int scrllViewWidth = 0, scrollViewMiddle = 0;
    private int currentItem = 0;

    //一级分类索引
    private static int mList1Index=0;

    Animation animUnfold;
    Animation animFold;

    Handler handler = new Handler(this);

    int mCategory_id1=0;
    int mCategory_id2=0;

    //更新所有分类
    private static final int UPDATE_ALL_CATEGORY = 1001;
    private static final int INIT_FRAGMENT = 1002;
    //更新子分类
    private static final int UPDATE_SUB_CATEGORY = 1003;

    //判断是否加载了一级分类
    boolean isLoadedList1=false;

    Dialog waitDialog=null;

    @Override
    public void layout() {
        setContentView(R.layout.activity_good_inventory_sort);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        initView();

    }

    public void initView() {

        title_tv.setText("商品排序");

        animUnfold = AnimationUtils.loadAnimation(this, R.anim.unfold);
        animFold = AnimationUtils.loadAnimation(this, R.anim.fold);
        snacks_ll.setOnClickListener(this);

        adapter = new MyGoodsPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(adapter);
        indicator.setViewPager(viewPager);
        //获取分类列表,0表示全部
        loadCategory(0);
    }

    /**
     * 获取分类
     */
    private void loadCategory(final int category_id) {

        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("category_id", category_id);//0表示全部
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitDialog= PublicDialog.createLoadingDialog(this,"正在加载...");
        waitDialog.show();

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

                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(serverMsg);
                        Intent intent = new Intent(GoodInventorySortActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        finish();
                        return;
                    }else if (serverMsg.equals("10012")) {    //重新握手
                        PublicUtils.handshake(GoodInventorySortActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                loadCategory(category_id);
                            }
                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        String deData = null;
                        try {
                            deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                            if (deData != null) {
                                LogUtil.e("ME", "解密数据=" + deData);
                                if (category_id == 0) {
                                    Message msg = handler.obtainMessage(UPDATE_ALL_CATEGORY);
                                    msg.obj = (String) deData;
                                    handler.sendMessage(msg);
                                    //msg.recycle();
                                } else {
                                    Message msg = handler.obtainMessage(UPDATE_SUB_CATEGORY);
                                    msg.obj = (String) deData;
                                    handler.sendMessage(msg);
                                }
                            }
                        } catch (Exception e) {
                            showToast(e.toString());
                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                        //没有分类
                        categories2.clear();
                        initFragmentShow(mList1Index);
                    }
                } catch (JSONException ex) {
                    showToast(ex.toString());
                }
                waitDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                waitDialog.dismiss();
                showToast(s);
            }
        });
    }


    /**
     * 根据条形码加载信息
     *
     * @param v
     */


    @Override
    public void onClick(View v) {
        index++;
        if (v == snacks_ll) {
            snacks(index);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == UPDATE_ALL_CATEGORY) {
            String deData = (String) msg.obj;
            LogUtil.e("ME", "handle=" + deData);
            try {
                com.alibaba.fastjson.JSONObject object = JSON.parseObject(deData);
                JSONArray array1 = object.getJSONArray("list1");
                if (array1.size() > 0) {
                    com.alibaba.fastjson.JSONObject temp1 = null;
                    Category category1 = null;
                    categories1.clear();
                    for (int i = 0; i < array1.size(); i++) {
                        category1 = new Category();
                        temp1 = array1.getJSONObject(i);
                        category1.setCategory_id(temp1.getIntValue("goods_category_id"));
                        category1.setCategory_name(temp1.getString("name"));
                        categories1.add(category1);
                        category1 = null;
                        temp1 = null;
                    }
                }
                JSONArray array2 = object.getJSONArray("list2");
                if (array2.size() > 0) {
                    com.alibaba.fastjson.JSONObject temp2 = null;
                    Category category2 = null;
                    categories2.clear();
                    category2 = new Category();
                    category2.setCategory_id(categories1.get(0).getCategory_id());
                    category2.setCategory_name("全部");
                    categories2.add(category2);
                    category2 = null;
                    for (int j = 0; j < array2.size(); j++) {
                        category2 = new Category();
                        temp2 = array2.getJSONObject(j);
                        category2.setCategory_id(temp2.getIntValue("goods_category_id"));
                        category2.setCategory_name(temp2.getString("name"));
                        //LogUtil.e("ME", "二级分类=" + temp2.getIntValue("goods_category_id") + ";" + temp2.getString("name"));
                        categories2.add(category2);
                        category2 = null;
                        temp2 = null;
                    }
                }

                //设置默认值
                mCategory_id1=categories1.get(0).getCategory_id();

                showGoodType(categories1, categories2);

            } catch (JSONException e) {
            }

        } else if (msg.what == UPDATE_SUB_CATEGORY) {
            String deData = (String) msg.obj;
            LogUtil.e("ME", "list2=" + deData);
            try {
                com.alibaba.fastjson.JSONObject object = JSON.parseObject(deData);
                JSONArray array1 = object.getJSONArray("list1");
                if (array1.size() > 0) {
                    com.alibaba.fastjson.JSONObject temp1 = null;
                    Category category1 = null;
                    categories2.clear();
                    category1 = new Category();
                    category1.setCategory_id(categories1.get(mList1Index).getCategory_id());
                    category1.setCategory_name("全部");
                    categories2.add(category1);
                    category1 = null;
                    for (int j = 0; j < array1.size(); j++) {
                        category1 = new Category();
                        temp1 = array1.getJSONObject(j);
                        category1.setCategory_id(temp1.getIntValue("goods_category_id"));
                        category1.setCategory_name(temp1.getString("name"));
                        categories2.add(category1);
                        category1= null;
                        temp1 = null;
                    }
                }
                initFragmentShow(mList1Index);
            } catch (JSONException e) {
            }

        } else if (msg.what == INIT_FRAGMENT)
        {
            category1_tv.setText(categories1.get(msg.arg1).getCategory_name());
            LogUtil.e("ME", "arg1=" + msg.arg1);
            mList1Index=msg.arg1;
            LogUtil.e("ME", "分类id1=" + categories1.get(msg.arg1).getCategory_id());
            loadCategory(categories1.get(msg.arg1).getCategory_id());
            mCategory_id1=categories1.get(msg.arg1).getCategory_id();

        }
        return false;
    }

    private void snacks(int index) {

        if (index % 2 == 0)
        {
            scrollView.setVisibility(View.INVISIBLE);
            scrollView.startAnimation(animFold);


        } else {
            scrollView.startAnimation(animUnfold);
            scrollView.setVisibility(View.VISIBLE);

        }
    }

    /**
     * 动态生成显示items中的textview
     *
     * @param list1 一级分类
     * @param list2 二级分类
     */
    private void showGoodType(ArrayList<Category> list1, ArrayList<Category> list2) {
        LogUtil.e("ME", "size=" + list1.size());
        if(isLoadedList1)//如果已经加载过一级列表，就不加载了
            return;
        LinearLayout shoptype_ll = (LinearLayout) findViewById(R.id.shoptype_ll);
        mContentTextViews = new TextView[categories1.size()];
        mViews = new View[categories1.size()];
        for (int i = 0; i < categories1.size(); i++) {
            LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.item_shoptype, null);
            view.setId(i);
            view.setOnClickListener(onClickListener);
            TextView textView = (TextView) view.findViewById(R.id.name_tv);
            textView.setText(categories1.get(i).getCategory_name());
            shoptype_ll.addView(view);
            mContentTextViews[i] = textView;
            mViews[i] = view;
        }
        mViews[0].performClick();

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            changeTextColor(v.getId());
            changeTextLocation(v.getId());

            //设置显示tab
            if(!isLoadedList1)
            {
                initFragmentShow(v.getId());
                isLoadedList1=true;
            }else
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        index++;
                        snacks(index);
                    }
                });

                Message msg = handler.obtainMessage(INIT_FRAGMENT);
                msg.arg1 = v.getId();
                handler.sendMessage(msg);
            }


        }
    };

    //改变选择的字体颜色
    private void changeTextColor(int id) {
        for (int i = 0; i < mContentTextViews.length; i++) {
            if (i != id) {
                mContentTextViews[i].setTextColor(0xff282828);
                mViews[i].setBackgroundResource(R.color.white);
            } else {
                //选中的时候
                mViews[i].setBackgroundResource(R.color.theme_bg);
                mContentTextViews[i].setTextColor(0xff282828);
            }
        }


    }

    //改变位置
    private void changeTextLocation(int clickPosition) {
        int x = (mViews[clickPosition].getTop() - getScrollViewMiddle() + (getViewheight(mViews[clickPosition]) / 2));
        scrollView.smoothScrollTo(0, x);
    }


    private void initFragmentShow(int index) {

        mFragments.clear();
        for (int i = 0; i < categories2.size(); i++)
        {
            LogUtil.e("ME", "category_name=" + categories2.get(i).getCategory_name());
            GoodSortFragment fragment = new GoodSortFragment(mCategory_id1,
                    categories1.get(mCategory_id1).getCategory_name(),
                    categories2.get(i).getCategory_id(),
                    categories2.get(i).getCategory_name()
            );
            mFragments.add(fragment);
        }

        adapter.notifyDataSetChanged();
        viewPager.setAdapter(adapter);
        indicator.setViewPager(viewPager);
        indicator.notifyDataSetChanged();
        indicator.setCurrentItem(0);
    }

    public void back(View view) {
        finish();
    }

    public void search(View view) {
        startActivity(new Intent(this, SearchGoodsManageActivity.class));
    }

    /**
     * 返回scrollview的中间位置
     *
     * @return
     */
    private int getScrollViewMiddle() {
        if (scrollViewMiddle == 0)
            scrollViewMiddle = getScrollViewheight() / 2;
        return scrollViewMiddle;
    }

    /**
     * 返回ScrollView的宽度
     *
     * @return
     */
    private int getScrollViewheight() {
        if (scrllViewWidth == 0)
            scrllViewWidth = scrollView.getBottom() - scrollView.getTop();
        return scrllViewWidth;
    }

    /**
     * 返回view的宽度
     *
     * @param view
     * @return
     */
    private int getViewheight(View view) {
        return view.getBottom() - view.getTop();
    }

    class MyGoodsPagerAdapter extends FragmentStatePagerAdapter {
        public MyGoodsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return categories2.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
           // LogUtil.e("ME","title="+categories2.get(position).getCategory_name());
            return categories2.get(position).getCategory_name();
        }
    }
}
