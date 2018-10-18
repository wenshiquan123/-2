package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.hlzx.ljdjsj.interfaces.DialogOnClickListener;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.FileUtil;
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
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class EditGoodActivity extends BaseActivity implements android.os.Handler.Callback, View.OnClickListener {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    @ViewInject(R.id.tab_rl)
    private RelativeLayout tab_rl;

    //商品图
    @ViewInject(R.id.shop_iv)
    private ImageView shop_iv;
    //名称
    @ViewInject(R.id.goods_name_et)
    private EditText goods_name_et;
    //分类
    @ViewInject(R.id.category1_sr)
    private Spinner category1_sr;
    @ViewInject(R.id.category2_sr)
    private Spinner category2_sr;
    //规格
    @ViewInject(R.id.goods_format_et)
    private EditText goods_format_et;
    //进价
    @ViewInject(R.id.goods_initial_price_et)
    private EditText goods_initial_price_et;
    //售价
    @ViewInject(R.id.goods_price_et)
    private EditText goods_price_et;
    //库存
    @ViewInject(R.id.goods_inventory_et)
    private EditText goods_inventory_et;

    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    @ViewInject(R.id.goods_name_tv)
    private TextView goods_name_tv;
    @ViewInject(R.id.goods_category_tv)
    private TextView goods_category_tv;
    @ViewInject(R.id.goods_format_tv)
    private TextView goods_format_tv;

    //商品状态
    int status = -1;
    //String[] moreStr = {"上架", "下架", "删除"};
    MyPopAdapter adapterMore;
    ArrayList<String> moreItems = new ArrayList<>();

    Goods good = null;
    int action_type = 0;
    Dialog waitDialog = null;
    Handler handler = new Handler(this);

    MySpinner1Adapter adapter1 = null;
    MySpinner2Adapter adapter2 = null;
    //一级分类
    private ArrayList<Category> categories1 = new ArrayList<Category>();
    //二级分类
    private ArrayList<Category> categories2 = new ArrayList<Category>();

    //更新所有分类
    private static final int UPDATE_ALL_CATEGORY = 1001;
    //更新子分类
    private static final int UPDATE_SUB_CATEGORY = 1002;
    //更新UI
    private static final int UPDATE_UI = 1003;
    int mCategory_id = 0;
    int goods_id = 0;
    private int mPosition1 = -1;

    int category_id1 = -1;
    int category_id2 = -1;
    String category_name1 = "";
    String category_name2 = "";
    //记录初始位置
    int index1 = -1;
    int index2 = -1;

    Dialog dialog = null;
    private Bitmap goodsBmp = null;
    String imgStr = "";

    private String headFileName;//头像文件名称
    private File headFile;//头像文件
    //请求码
    private final static int CAMERE_REQUESTCODE = 1001;
    private final static int PIC_REQUESTCODE = 1002;
    private final static int CROP_PICTURE = 1003;

    //判断图片是否加载完成
    boolean isImgLoaded = false;
    //是否是自动加载子分类
    boolean isAutoLoad = false;

    //是否加载详情
    boolean isLoadedGoodsDetail = false;

    //是否判断进价和售价
    boolean isLimits = false;

    AddInventoryGoodActivity.Action ACTION = AddInventoryGoodActivity.Action.NO_DO;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_edit_good);
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
        title_tv.setText("编辑商品");
        good = (Goods) getIntent().getSerializableExtra("good");
        isLoadedGoodsDetail = getIntent().getBooleanExtra("flag", false);

        if (good != null) {
            Picasso.with(this).load(good.getPicpath())
                    .resize(100, 100).centerCrop()
                    .placeholder(R.mipmap.tu)
                    .error(R.mipmap.tu)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                            shop_iv.setImageBitmap(bitmap);
                            //获取图片
                            //BmpToString(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable drawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable drawable) {

                        }
                    });

            goods_name_et.setText(good.getName());
            goods_format_et.setText(good.getFormat());
            goods_initial_price_et.setText(good.getDealer_price());
            goods_price_et.setText(good.getPrice());
            /*if (good.getPrice() == null || good.getPrice().equals("")) {
                goods_price_et.setText("0.00");
            } else {

            }*/
            /*if (good.getDealer_price() == null || good.getDealer_price().equals("")) {
                goods_initial_price_et.setText("0.00");

            } else {
                goods_initial_price_et.setText(good.getInitial_price());
            }*/
            goods_inventory_et.setText(good.getInventory() + "");
            category_id1 = good.getGoods_category1_id();
            category_id2 = good.getGoods_category2_id();
            category_name1 = good.getCategory_name1();
            category_name2 = good.getCategory_name2();
            status = good.getStatus();
            LogUtil.e("ME", "status=" + good.getStatus());
            if (status == 0)//已下架
            {
                moreItems.add("上架");
                moreItems.add("删除");
                //adapterMore.notifyDataSetChanged();
            } else if (status == 1)//在架上
            {
                moreItems.add("下架");
                moreItems.add("删除");
                //adapterMore.notifyDataSetChanged();
            }

            goods_id = good.getGoods_id();
            if (!good.getGoods_depot_id().equals("0"))//为自定义的商品
            {
                shop_iv.setEnabled(false);
                category1_sr.setEnabled(false);
                category2_sr.setEnabled(false);
                goods_name_et.setEnabled(false);
                goods_format_et.setEnabled(false);
                goods_name_tv.setTextColor(getResources().getColor(R.color.font_grey));
                goods_name_et.setTextColor(getResources().getColor(R.color.font_grey));
                goods_category_tv.setTextColor(getResources().getColor(R.color.font_grey));
                goods_format_tv.setTextColor(getResources().getColor(R.color.font_grey));
                goods_format_et.setTextColor(getResources().getColor(R.color.font_grey));
            }
            LogUtil.e("ME", "分类ID=" + category_id1 + ";" + category_id2);
            LogUtil.e("ME", "分类name=" + category_name1 + ";" + category_name2);
        }

        shop_iv.setOnClickListener(this);

        adapterMore = new MyPopAdapter(moreItems);
        adapter1 = new MySpinner1Adapter(this);
        adapter2 = new MySpinner2Adapter(this);
        category1_sr.setAdapter(adapter1);
        category2_sr.setAdapter(adapter2);

        category1_sr.setOnItemSelectedListener(onItemSelectedListener1);
        category2_sr.setOnItemSelectedListener(onItemSelectedListener2);
        loadAllCategory(0);

    }

    @Override
    public void onClick(View v) {
        if (v == shop_iv) {
            showSheetDialog();
        }
    }

    //选择照片
    private void showSheetDialog() {
        dialog = PublicDialog.showSheet(this, sheetSelected);
        dialog.show();
    }

    PublicDialog.OnActionSheetSelected sheetSelected = new PublicDialog.OnActionSheetSelected() {
        @Override
        public void onClick(int whichButton) {
            if (whichButton == 0)//拍照
            {
                takePhoto();
            } else if (whichButton == 1)//相册获取
            {
                getPhoto();
            } else if (whichButton == 2)//取消
            {
                if (dialog != null)
                    dialog.dismiss();
            }
        }
    };

    //拍照
    private void takePhoto() {
        if (!FileUtil.isSDcardExist()) {
            showToast("SD卡不存在，不能进行拍照");
            return;
        }

        //判断是否是4.4及以上系统
        headFileName = "ljdj_" + System.currentTimeMillis() + ".png";
        headFile = FileUtil.getOrCreateFile(headFileName);

        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(headFile)),
                CAMERE_REQUESTCODE);
        LogUtil.e("ME", "拍照");

    }

    //相册获取
    private void getPhoto() {

        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, PIC_REQUESTCODE);
    }


    //用系统自带程序裁剪图片
    public void startPhotoZoom(Uri uri) {
        /*
         * 至于下面这个Intent的ACTION是怎么知道的，大家可以看下自己路径下的如下网页
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 * 直接在里面Ctrl+F搜：CROP ，之前没仔细看过，其实安卓系统早已经有自带图片裁剪功能, 是直接调本地库的
		 */
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivityForResult(intent, CROP_PICTURE);

    }

    /**
     * 保存图片
     */
    private void saveCropImg(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap tempBmp = null;
        if (extras != null) {
            tempBmp = extras.getParcelable("data");
            try {
                if (tempBmp == null) {
                    showToast("图片为空");
                    return;
                }
                /*
                 * 把图片转换成String
                 */
                shop_iv.setImageBitmap(tempBmp);
                //goodsBmp = tempBmp;
                BmpToString(tempBmp);
            } catch (Exception e) {
                e.printStackTrace();
                showToast(e.toString());
            }

        }
    }

    private void BmpToString(final Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);//质量压缩，100表示不压缩
        int options = 100;
        while ((baos.toByteArray().length / 1024) > 1024)//不能大于1M
        {
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            bitmap.compress(Bitmap.CompressFormat.PNG, options, baos);
        }

        byte[] tempB = baos.toByteArray();
        imgStr = Base64.encodeToString(tempB, 0);

    }

    AdapterView.OnItemSelectedListener onItemSelectedListener1 = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //加载二级分类
            LogUtil.e("ME", "listener1");
            mPosition1 = position;
            int category_id = categories1.get(position).getCategory_id();
            isAutoLoad = true;
            loadAllCategory(category_id);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    AdapterView.OnItemSelectedListener onItemSelectedListener2 = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.e("ME", "listener2");
            mCategory_id = categories2.get(position).getCategory_id();
            LogUtil.e("ME", "id=" + categories2.get(position).getCategory_id() +
                    ";name" + categories2.get(position).getCategory_name());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

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
                        if (category_id1 == temp1.getIntValue("goods_category_id")) {
                            index1 = i;
                        }
                        if (index1 == -1) {
                            if (temp1.getString("name").equals(category_name1)) {
                                index1 = i;
                            }
                        }
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
                    category2 = null;
                    for (int j = 0; j < array2.size(); j++) {
                        category2 = new Category();
                        temp2 = array2.getJSONObject(j);
                        category2.setCategory_id(temp2.getIntValue("goods_category_id"));
                        // LogUtil.e("ME","category_id2="+category_id2+";"+temp2.getIntValue("goods_category_id"));
                        category2.setCategory_name(temp2.getString("name"));
                        categories2.add(category2);
                        category2 = null;
                        temp2 = null;
                    }
                }

            } catch (JSONException e) {
            }
            adapter1.notifyDataSetChanged();
            adapter2.notifyDataSetChanged();
            LogUtil.e("ME", "index1=" + index1 + ";index2=" + index2);
            if (index1 != -1) {
                category1_sr.setSelection(index1);
            } else {
                category1_sr.setSelection(0);
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
                    category1 = null;
                    for (int j = 0; j < array1.size(); j++) {
                        category1 = new Category();
                        temp1 = array1.getJSONObject(j);
                        category1.setCategory_id(temp1.getIntValue("goods_category_id"));
                        if (category_id2 == temp1.getIntValue("goods_category_id")) {
                            index2 = j;
                        }
                        if (index2 == -1) {
                            if (temp1.getString("name").equals(category_name2)) {
                                index2 = j;
                            }
                        }
                        category1.setCategory_name(temp1.getString("name"));
                        categories2.add(category1);
                        category1 = null;
                        temp1 = null;
                    }
                }
            } catch (JSONException e) {
            }

            adapter2.notifyDataSetChanged();
            //category2_sr.setSelection(0);
            mCategory_id = categories2.get(0).getCategory_id();
            mCategory_id = categories2.get(0).getCategory_id();

            if (index2 != -1) {
                category2_sr.setSelection(index2);
            } else {
                category2_sr.setSelection(0);
            }


            //LogUtil.e("ME", "id=" + categories2.get(0).getCategory_id() +
            //";name" + categories2.get(0).getCategory_name());
            if (isAutoLoad) {

                if (!isLoadedGoodsDetail) {
                    if (waitDialog != null) {
                        waitDialog.dismiss();
                        waitDialog = null;
                    }
                    isAutoLoad = false;
                } else {
                    //加载商品详情
                    loadData(goods_id);
                }
            }
        } else if (msg.what == UPDATE_UI) {
            String deData = (String) msg.obj;
            com.alibaba.fastjson.JSONObject object = JSON.parseObject(deData);
            goods_initial_price_et.setText(object.getString("dealer_price"));
            goods_price_et.setText(object.getString("price"));
            goods_inventory_et.setText(object.getString("inventory"));
            if (isLoadedGoodsDetail) {
                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }
                isLoadedGoodsDetail = false;
            }
            status = object.getIntValue("status");
            moreItems.clear();
            if (status == 0)//已下架
            {
                moreItems.add("上架");
                moreItems.add("删除");
                adapterMore.notifyDataSetChanged();
            } else if (status == 1)//在架上
            {
                moreItems.add("下架");
                moreItems.add("删除");
                adapterMore.notifyDataSetChanged();
            }

        }
        return false;
    }

    public void back(View view) {
        ACTION = AddInventoryGoodActivity.Action.NO_DO;
        finish();
    }

    AdapterView.OnItemClickListener moreItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (status == 0)//已下架
            {
                if (position == 0)//上架
                {
                    action_type = 1;
                } else if (position == 1)//删除
                {
                    action_type = 3;
                }
            } else if (status == 1)//在架上
            {
                if (position == 0)//下架
                {
                    action_type = 2;
                } else if (position == 1)//删除
                {
                    action_type = 3;
                }
            }
            setStatus(action_type);
        }
    };

    //设置上下加状态
    private void setStatus(final int action_type) {
        //判断网络状态
        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }
        LogUtil.e("ME", "goods_id=" + good.getGoods_id());
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type", action_type);//操作类型：1-上架、2-下架、3-删除
            json.put("goods_ids", good.getGoods_id());
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpUtil.doPostRequest(UrlsConstant.GOODS_SET_STATUS, map, new RequestCallBack<String>() {
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
                        //showToast(text);
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
        intent.putExtra("flag", ACTION.getValue());
        setResult(RESULT_OK, intent);
        super.finish();
    }

    /**
     * 加载所有的分类
     */
    private void loadAllCategory(final int category_id) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
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
        if (waitDialog == null) {
            waitDialog = PublicDialog.createLoadingDialog(this, "正在加载...");
            waitDialog.show();
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
                        showToast(serverMsg);
                        Intent intent = new Intent(EditGoodActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        finish();
                        return;
                    } else if (serverMsg.equals("10012")) {    //重新握手
                        PublicUtils.handshake(EditGoodActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                loadData(category_id);
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
                        if (categories2.size() == 0) {
                            Category category = new Category();
                            category.setCategory_id(-1);
                            category.setCategory_name("");
                            categories2.add(category);
                        }
                        adapter2.notifyDataSetChanged();
                        category2_sr.setSelection(0);

                        if (mPosition1 != -1) {
                            mCategory_id = categories1.get(mPosition1).getCategory_id();
                            LogUtil.e("ME", "id=" + categories1.get(mPosition1).getCategory_id() +
                                    ";name" + categories1.get(mPosition1).getCategory_name());
                        }

                    }
                } catch (JSONException ex) {
                    showToast(ex.toString());
                }
                if (category_id != 0) {
                    if (waitDialog != null) {
                        waitDialog.dismiss();
                        waitDialog = null;
                    }
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

    /**
     * 加载商品详情
     *
     * @param goods_id
     */
    private void loadData(final int goods_id) {
        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("goods_id", goods_id);//0表示全部
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*if (waitDialog == null) {
            waitDialog = PublicDialog.createLoadingDialog(this, "正在加载...");
            waitDialog.show();
        }*/

        HttpUtil.doPostRequest(UrlsConstant.GOODS_INFO, map, new RequestCallBack<String>() {
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
                        Intent intent = new Intent(EditGoodActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        finish();
                        return;
                    } else if (serverMsg.equals("10012")) {    //重新握手
                        PublicUtils.handshake(EditGoodActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                loadData(goods_id);
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
                                LogUtil.e("ME", "详情=" + deData);
                                Message msg = handler.obtainMessage(UPDATE_UI);
                                msg.obj = (String) deData;
                                handler.sendMessage(msg);
                            }
                        } catch (Exception e) {
                            //showToast(e.toString());
                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                    }
                } catch (JSONException ex) {
                    showToast(ex.toString());
                }
                /*if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }*/
            }

            @Override
            public void onFailure(HttpException e, String s) {
                /*if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog = null;
                }*/
                showToast(s);
            }
        });

    }


    public void more(View view) {
        PopupWindow popupWindow = showPopAll(adapterMore, moreItemClick, tab_rl, ScreenManager.getScreenWidth(this) - 320, 0, 0);
    }

    public PopupWindow showPopAll(MyPopAdapter adapter, AbsListView.OnItemClickListener itemClickListener, View dropView, int offX, int offY, int type) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERE_REQUESTCODE) {
                if (headFile != null && headFile.exists()) {
                    LogUtil.e("ME", "拍照完成");
                    startPhotoZoom(Uri.fromFile(new File(headFile
                            .getAbsolutePath())));
                }
            } else if (requestCode == PIC_REQUESTCODE) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    startPhotoZoom(selectedImage);
                }
            } else if (requestCode == CROP_PICTURE) {
                if (data != null) {
                    //保存裁剪之后的图片
                    saveCropImg(data);
                }
            }
        }
    }

    /**
     * 保存
     *
     * @param view
     */
    public void onSave(final View view) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        if (mCategory_id == -1) {
            response_tv.setText("请选择分类");
            response_tv.setVisibility(View.VISIBLE);
            return;
        }
        String goodsName = goods_name_et.getText().toString().trim();
        String goodsFormat = goods_format_et.getText().toString().trim();
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
                    isShowTip = true;
                    //LogUtil.e("ME","整数");
                }
            } catch (Exception e) {
                try {
                    if (Float.parseFloat(goodsInitialPrice) > Float.parseFloat(goodsPrice)) {
                        //response_tv.setText("商品售价不能小于商品进价");
                        //response_tv.setVisibility(View.VISIBLE);
                        //return;
                        isShowTip = true;
                        //LogUtil.e("ME","小树");
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
                    onSave(view);
                }
            });
            return;
        }

        LogUtil.e("ME", "isShowTip=" + isShowTip);
        /*if(true)
            return;*/

        //获取图片
        if (imgStr.equals("")) {
            //response_tv.setText("商品图片不能为空");
            //response_tv.setVisibility(View.VISIBLE);
            //return;
        }


        if (waitDialog == null) {
            waitDialog = PublicDialog.createLoadingDialog(this, "正在保存...");
            waitDialog.show();
        }

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("action_type", 3);
            json.put("category_id", mCategory_id);
            json.put("name", goodsName);
            json.put("picpath", imgStr);
            json.put("goods_id", goods_id);
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
                        showToast(serverMsg);
                        Intent intent = new Intent(EditGoodActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    } else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(EditGoodActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                onSave(view);
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
                        ACTION = AddInventoryGoodActivity.Action.REFREDHING;
                        finish();
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        response_tv.setText(text);
                        response_tv.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
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
                response_tv.setText(s);
                response_tv.setVisibility(View.VISIBLE);
            }
        });

        isLimits=false;

    }

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
                convertView = LayoutInflater.from(EditGoodActivity.this).inflate(R.layout.item_pop, null);
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

    class MySpinner1Adapter extends BaseAdapter {
        Context mContext = null;

        public MySpinner1Adapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return categories1.size();
        }

        @Override
        public Object getItem(int position) {
            return categories1.get(position);
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
            if (categories1.size() > 0) {
                if (!good.getGoods_depot_id().equals("0")) {
                    holder.name_tv.setTextColor(getResources().getColor(R.color.font_grey));
                }
                holder.name_tv.setText(categories1.get(position).getCategory_name());
            }
            return convertView;
        }

        class ViewHolder {
            TextView name_tv;
        }
    }

    class MySpinner2Adapter extends BaseAdapter {
        Context mContext = null;

        public MySpinner2Adapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return categories2.size();
        }

        @Override
        public Object getItem(int position) {
            return categories2.get(position);
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
            if (categories2.size() > 0) {
                if (!good.getGoods_depot_id().equals("0")) {
                    holder.name_tv.setTextColor(getResources().getColor(R.color.font_grey));
                }
                holder.name_tv.setText(categories2.get(position).getCategory_name());
            }
            return convertView;
        }

        class ViewHolder {
            TextView name_tv;
        }
    }
}
