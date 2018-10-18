package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.Goods;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.interfaces.DialogOnClickListener;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 * 从商品库添加商品
 */
public class AddInventoryGoodActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    //商品图片
    @ViewInject(R.id.shop_iv)
    private ImageView shop_iv;
    //商品ID
    String goods_id = "";
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

    Dialog waitDialog = null;
    //商品
    Goods goods = null;

    //分类
    @ViewInject(R.id.category1_sr)
    private Spinner category1_sr;
    @ViewInject(R.id.category2_sr)
    private Spinner category2_sr;

    ArrayList<String> cateyegoryStr1 = new ArrayList<>();
    ArrayList<String> cateyegoryStr2 = new ArrayList<>();
    MySpinnerAdapter1 adapter1 = null;
    MySpinnerAdapter2 adapter2 = null;

    //是否判断进价和售价
    boolean isLimits = false;

    Action ACTION = Action.NO_DO;

    public static enum Action {
        REFREDHING(0x01),
        NO_DO(0x02);
        private int mIntValue;

        Action(int intValue) {
            mIntValue = intValue;
        }

        public int getValue() {
            return this.mIntValue;
        }
    }

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_add_inventory_good);
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

        Intent it = this.getIntent();
        goods = (Goods) it.getSerializableExtra("good");
        if (goods != null) {
            LogUtil.e("ME", goods.getCategory_name1() + ";" + goods.getCategory_name2());
            Picasso.with(this).load(goods.getPicpath()).into(shop_iv);
            goods_name_tv.setText(goods.getName());
            catecgory1_tv.setText(goods.getCategory_name1());
            catecgory2_tv.setText(goods.getCategory_name2());
            goods_format_tv.setText(goods.getFormat());
            goods_id = goods.getGoods_depot_id();
        }
        cateyegoryStr1.add(goods.getCategory_name1());
        adapter1 = new MySpinnerAdapter1(this);
        category1_sr.setAdapter(adapter1);

        cateyegoryStr2.add(goods.getCategory_name2());
        adapter2 = new MySpinnerAdapter2(this);
        category2_sr.setAdapter(adapter2);
        category1_sr.setEnabled(false);
        category2_sr.setEnabled(false);

    }

    /**
     * 提交服务器
     *
     * @param view
     */
    public void onCommit(final View view) {
        if (!NetWorkUtils.isNetworkAvailable(this)) {
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

        if (TextUtils.isEmpty(goodsInitialPrice)) {
            response_tv.setText("商品进价不能为空");
            response_tv.setVisibility(View.VISIBLE);
            return;
            //goodsInitialPrice="";
        }

        boolean isShowTip = false;
        if (!isLimits) {
            try {
                if (Integer.parseInt(goodsInitialPrice) > Integer.parseInt(goodsPrice)) {
                    //response_tv.setText("商品售价不能小于商品进价");
                    //response_tv.setVisibility(View.VISIBLE);
                    //return;
                    isShowTip=true;
                }
            } catch (Exception e) {
                try {
                    if (Float.parseFloat(goodsInitialPrice) > Float.parseFloat(goodsPrice)) {
                        //response_tv.setText("商品售价不能小于商品进价");
                       // response_tv.setVisibility(View.VISIBLE);
                        //return;

                        isShowTip=true;
                    }
                } catch (Exception e1) {
                    response_tv.setText(e1.toString());
                    response_tv.setVisibility(View.VISIBLE);
                    return;
                }
            }
        }

        if (isShowTip) {
            Dialog dialog = PublicDialog.warningDialog(this, "进价大于售价，确定要提交吗?", new DialogOnClickListener() {
                @Override
                public void onCancel() {
                    return;
                }

                @Override
                public void onConfirm() {
                    isLimits = true;
                    onCommit(view);
                }
            });
            return;
        }

        //获取图片
        shop_iv.setDrawingCacheEnabled(true);//设置可以从缓存获取
        Bitmap goodsBmp = Bitmap.createBitmap(shop_iv.getDrawingCache());
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
            json.put("goods_depot_id", goods_id);
            json.put("name", goodsName);
            json.put("picpath", strGoodsImage);
            json.put("format", goodsFormat);
            json.put("dealer_price", goodsInitialPrice);
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
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(AddInventoryGoodActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    } else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(AddInventoryGoodActivity.this, new ShakeHandCallback() {
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

                    if (status == HttpConstant.SUCCESS_CODE) {
                        showToast(text);
                        ACTION = Action.REFREDHING;
                        finish();
                    } else if (status == HttpConstant.FAILURE_CODE) {
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

        isLimits=false;
    }


    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("flag", ACTION.getValue());
        setResult(RESULT_OK, intent);
        super.finish();
    }


    public void back(View view) {
        ACTION = Action.NO_DO;
        finish();
    }

    class MySpinnerAdapter1 extends BaseAdapter {
        Context mContext = null;

        public MySpinnerAdapter1(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return cateyegoryStr1.size();
        }

        @Override
        public Object getItem(int position) {
            return cateyegoryStr1.get(position);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_spinner, null);
                holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name_tv.setTextColor(mContext.getResources().getColor(R.color.font_grey));
            holder.name_tv.setText(cateyegoryStr1.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView name_tv;
        }
    }

    class MySpinnerAdapter2 extends BaseAdapter {
        Context mContext = null;

        public MySpinnerAdapter2(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return cateyegoryStr2.size();
        }

        @Override
        public Object getItem(int position) {
            return cateyegoryStr2.get(position);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_spinner, null);
                holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name_tv.setTextColor(mContext.getResources().getColor(R.color.font_grey));
            holder.name_tv.setText(cateyegoryStr2.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView name_tv;
        }
    }
}
