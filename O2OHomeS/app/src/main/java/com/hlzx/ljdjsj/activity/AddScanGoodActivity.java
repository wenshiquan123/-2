package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.PublicDialog;
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

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 * 添加扫描商品
 */
public class AddScanGoodActivity extends BaseActivity implements View.OnClickListener, Handler.Callback {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.search_et)
    private EditText search_et;

    @ViewInject(R.id.load_linear_data)
    private RelativeLayout load_data_rl;

    @ViewInject(R.id.rela_no_data)
    private RelativeLayout no_data_rl;

    //商品图片
    @ViewInject(R.id.shop_iv)
    private ImageView shop_iv;
    //商品ID
   String goods_id="";
    //商品名称
    @ViewInject(R.id.goods_name_tv)
    private TextView goods_name_tv;
    //商品规格
    @ViewInject(R.id.goods_format_tv)
    private TextView goods_format_tv;
    //商品一级分类
    @ViewInject(R.id.category1_tv)
    private TextView catecgory1_tv;
    //商品二级分类
    @ViewInject(R.id.category2_tv)
    private TextView catecgory2_tv;

    @ViewInject(R.id.response_tv)
    private TextView response_tv;
    //商品进价
    @ViewInject(R.id.goods_initial_price_et)
    private EditText goods_initial_price_et;
    //商品售价
    @ViewInject(R.id.goods_price_et)
    private EditText goods_price_et;
    //商品库存
    @ViewInject(R.id.goods_inventory_et)
    private EditText goods_inventory_et;

    //条形码
    private String strUpc = "";

    Handler handler = new Handler(this);
    private static final int UPDATE_UI = 1001;

    Dialog waitDialog = null;



    @Override
    public void setLayout() {
        setContentView(R.layout.activity_add_scan_good);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        initView();
    }


    @Override
    public void onClick(View v)
    {

    }

    @Override
    public void initView() {
        super.initView();
        title_tv.setText("商品详情");

        search_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // 先隐藏键盘
                    ((InputMethodManager) search_et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(
                                    AddScanGoodActivity.this.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    search();
                    return true;
                }
                return false;
            }
        });

        Intent it = getIntent();
        if (it.getStringExtra("result") != null && !it.getStringExtra("result").equals("")) {
            strUpc = it.getStringExtra("result");
        }

        loadData(strUpc);
    }

    public void search()
    {
        String searchStr = search_et.getText().toString().trim();
        if (TextUtils.isEmpty(searchStr)) {
            showToast("搜索不能为空！");
            return;
        }
        Intent intent = new Intent(this, GoodsManageSearchResultActivity.class);
        intent.putExtra("keyword", searchStr);
        startActivity(intent);
    }

    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == UPDATE_UI) {
            try {
                com.alibaba.fastjson.JSONObject object = (com.alibaba.fastjson.JSONObject) msg.obj;
                String picpath = object.getString("picpath");
                Picasso.with(this).load(picpath).placeholder(R.mipmap.tu).error(R.mipmap.tu).into(shop_iv);
                goods_id=object.getString("goods_depot_id");
                goods_name_tv.setText(object.getString("name"));
                goods_format_tv.setText(object.getString("format"));
                catecgory1_tv.setText(object.getString("category_name1"));
                catecgory2_tv.setText(object.getString("category_name2"));
            } catch (JSONException e) {
            }
        }
        return false;
    }

    private void loadData(final String upc) {
        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            no_data_rl.setVisibility(View.VISIBLE);
            return;
        }

        if (upc.equals("")) {
            showToast("条形码为空");
            return;
        }
        LogUtil.e("ME", "条形码=" + upc);
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type", 1);//1表示条形码,2表示商品ID
            json.put("upc", upc);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        load_data_rl.setVisibility(View.VISIBLE);
        HttpUtil.doPostRequest(UrlsConstant.GOODS_DEPOT_INFO, map, new RequestCallBack<String>() {
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
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
                        Intent intent=new Intent(AddScanGoodActivity.this,LoginActivity.class);
                        intent.putExtra("action",0);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(AddScanGoodActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                loadData(upc);
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
                                com.alibaba.fastjson.JSONObject object = JSON.parseObject(deData);
                                Message message = handler.obtainMessage(UPDATE_UI);
                                message.obj = object;
                                handler.sendMessage(message);
                                LogUtil.e("ME", "解密数据=" + deData);
                            } else {
                                no_data_rl.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            showToast(e.toString());
                            no_data_rl.setVisibility(View.VISIBLE);
                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                        no_data_rl.setVisibility(View.VISIBLE);
                        title_tv.setVisibility(View.GONE);
                        search_et.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    showToast(e.toString());
                    no_data_rl.setVisibility(View.VISIBLE);
                }

                load_data_rl.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                showToast(s);
                load_data_rl.setVisibility(View.GONE);
                no_data_rl.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onCommit(final View view)
    {
        if(!NetWorkUtils.isNetworkAvailable(this))
        {
            showToast("哎！网络不给力");
            return;
        }

        String goodsName = goods_name_tv.getText().toString().trim();
        String goodsFormat = goods_format_tv.getText().toString().trim();
        String goodsInitialPrice = goods_initial_price_et.getText().toString().trim();
        String goodsPrice = goods_price_et.getText().toString().trim();
        String goodsInventory = goods_inventory_et.getText().toString().trim();

        if (TextUtils.isEmpty(goodsName)) {
            response_tv.setText("商品名称不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }

        if (TextUtils.isEmpty(goodsFormat)) {
            response_tv.setText("商品规格不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(goodsInitialPrice)) {
            response_tv.setText("商品进价不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(goodsPrice)) {
            response_tv.setText("商品售价不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(goodsInventory)) {
            response_tv.setText("商品存库不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }

        //获取图片
        shop_iv.setDrawingCacheEnabled(true);//设置可以从缓存获取
        Bitmap goodsBmp=Bitmap.createBitmap(shop_iv.getDrawingCache());
        shop_iv.setDrawingCacheEnabled(false);//清空缓存，以便下次获取，否则获取的事上一次的图片

        if (goodsBmp == null) {
            response_tv.setText("商品图片不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }
        //把bitmap做压缩处理转换成字符串
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        goodsBmp.compress(Bitmap.CompressFormat.PNG, 100, baos);//质量压缩，100表示不压缩
        int options = 100;
        while ((baos.toByteArray().length / 1024) > 1024)//不能大于1M
        {
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            goodsBmp.compress(Bitmap.CompressFormat.PNG, options, baos);
        }

        byte[] tempB = baos.toByteArray();
        String strGoodsImage = Base64.encodeToString(tempB, 0);

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type", 1);
            json.put("goods_depot_id",goods_id);
            json.put("name", goodsName);
            json.put("picpath", strGoodsImage);
            json.put("format", goodsFormat);
            json.put("initial_price", goodsInitialPrice);
            json.put("price", goodsPrice);
            json.put("inventory", goodsInventory);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitDialog = PublicDialog.createLoadingDialog(this, "正在保存...");
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.GOODS_SAVE, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "save=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")||serverMsg.equals("20004")) {
                        Intent intent=new Intent(AddScanGoodActivity.this,LoginActivity.class);
                        intent.putExtra("action",0);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        //finish();
                        return;
                    }else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(AddScanGoodActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                onCommit(view);
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE)
                    {
                        showToast(text);
                        finish();
                    }else if(status == HttpConstant.FAILURE_CODE)
                    {
                        response_tv.setText(text);
                        response_tv.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                }
                waitDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                waitDialog.dismiss();
                response_tv.setText(s);
                response_tv.setVisibility(View.VISIBLE);
            }
        });

    }
    public void back(View view) {
        finish();
    }
}
