package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.UserInfo;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.FileUtil;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.hlzx.ljdjsj.view.CircleImageView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 */
public class ShopSettingActivity extends BaseActivity implements View.OnClickListener {


    @ViewInject(R.id.shop_message_rl)
    private RelativeLayout shop_message_rl;
    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    @ViewInject(R.id.update_password_rl)
    private RelativeLayout update_password_rl;
    @ViewInject(R.id.stop_work_rl)
    private RelativeLayout stop_work_rl;
    @ViewInject(R.id.shop_inventory_lock_rl)
    private RelativeLayout shop_inventory_lock_rl;

    //头像设置
    @ViewInject(R.id.head_setting_rl)
    private RelativeLayout head_setting_rl;


    //头像
    @ViewInject(R.id.head_icon_iv)
    private CircleImageView head_icon_iv;
    @ViewInject(R.id.shop_lock_tv)
    private TextView shop_lock_tv;

    Dialog dialog = null;
    Dialog waitDialog = null;

    private String headFileName;//头像文件名称
    private File headFile;//头像文件
    //请求码
    private final static int CAMERE_REQUESTCODE = 1001;
    private final static int PIC_REQUESTCODE = 1002;
    private final static int CROP_PICTURE = 1003;

    UserInfo userInfo = null;
    SharedPreferences sp = null;

    //版本比较：是否是4.4及以上版本
    final boolean mIsKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_shop_setting);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        initUserInfo(sp);
        initView();


    }

    @Override
    public void initView() {
        title_tv.setText("店铺设置");
        shop_message_rl.setOnClickListener(this);
        update_password_rl.setOnClickListener(this);
        stop_work_rl.setOnClickListener(this);
        shop_inventory_lock_rl.setOnClickListener(this);
        head_setting_rl.setOnClickListener(this);
        userInfo = MyApplication.getInstance().getUserInfo();
        LogUtil.e("ME", "头像=" + userInfo.getLogo());
        if (userInfo != null && !userInfo.getToken().equals("")) {
            Picasso.with(this).load(userInfo.getLogo())
                    .resize(100,100).centerCrop()
                    .placeholder(R.mipmap.tu)
                    .error(R.mipmap.tu)
                    .into(head_icon_iv);
            if (userInfo.getRepertory_lock() == 1) {
                shop_lock_tv.setText("已开启");
            } else {
                shop_lock_tv.setText("已关闭");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserInfo userInfo = MyApplication.getInstance().getUserInfo();
        if (userInfo.getRepertory_lock() == 1) {
            shop_lock_tv.setText("已开启");
        } else {
            shop_lock_tv.setText("已关闭");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == shop_message_rl) {
            startActivity(new Intent(this, ShopMessageActivity.class));
        } else if (v == update_password_rl) {
            startActivity(new Intent(this, UpdatePasswordActivity.class));
        } else if (v == stop_work_rl) {
            startActivity(new Intent(this, StopWorkActivity.class));
        } else if (v == shop_inventory_lock_rl) {
            //判断库存锁密码是否为空，如果为空就需要验证码登录
            // LogUtil.e("ME","库存锁密码="+MyApplication.getInstance().getUserInfo().getRepertory_pw());
            if (MyApplication.getInstance().getUserInfo() != null) {
                if (MyApplication.getInstance().getUserInfo().getRepertory_pw() == null ||
                        MyApplication.getInstance().getUserInfo().getRepertory_pw().equals("")) {
                    //获取验证码
                    startActivity(new Intent(this, GetCodeForShopInventoryLockActivity.class));

                } else {
                    startActivity(new Intent(this, ShopInventoryLockActivity.class));
                }
            }

        } else if (v == head_setting_rl) {
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
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
        //把“return-data”设置为了true然后在onActivityResult中通过
        // data.getParcelableExtra("data")来获取数据，不过这样的话dp这个变量的值就不能太大了，
        // 不然你的程序就挂了。这里也就是我遇到问题的地方了，在大多数高配手机上这样用是没有问题的，
        // 不过很多低配手机就有点hold不住了，直接就异常了，包括我们的国产神机米3也没能hold住，
        // 所以我建议大家不要通过return data 大数据，
        //小数据还是没有问题的，说以我们在剪切图片的时候就尽量使用Uri这个东东来帮助我们。
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);//设置为不返回数据
        intent.putExtra("return-data", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivityForResult(intent, CROP_PICTURE);
    }


    public void back(View view) {
        finish();
    }


    /**
     * 保存图片
     */
    private void saveCropImg(Uri uri) {
        /*Bundle extras = data.getExtras();
        Bitmap tempBmp = null;
        if (extras != null) {
            tempBmp = extras.getParcelable("data");
            try {
               if (tempBmp == null) {
                    showToast("图片为空");
                    return;
                }

                updateLoad(tempBmp);

            } catch (Exception e) {
                e.printStackTrace();
                showToast(e.toString());
            }
        }*/

        Bitmap tempBmp = null;
        try {
            // 读取uri所在的图片
            tempBmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            if (tempBmp == null)
            {
                showToast("图片为空");
                return;
            }
            //上传服务器
            updateLoad(tempBmp);

        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    //上传头像

    Bitmap headIcon = null;

    private void updateLoad(Bitmap bmp) {

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToast("哎！网络不给力");
            return;
        }

        if (bmp == null) {
            showToast("上传的图片不能为空");
            return;
        }
        headIcon = bmp;

        //把bitmap做压缩处理转换成字符串
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);//质量压缩，100表示不压缩
        int options = 100;
        while ((baos.toByteArray().length / 1024) > 1024)//不能大于1M
        {
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            bmp.compress(Bitmap.CompressFormat.PNG, options, baos);
        }

        byte[] tempB = baos.toByteArray();
        String strLogo = Base64.encodeToString(tempB, 0);

        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("logo", strLogo);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        waitDialog = PublicDialog.createLoadingDialog(this, "正在上传...");
        waitDialog.setCancelable(true);
        waitDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.SHOP_UPDATE_LOGO, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "上传图像=" + result);

                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String data = jsonObject.getString("data");
                    String text = jsonObject.getString("text");
                    String iv = jsonObject.getString("iv");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002")) {
                        Intent intent = new Intent(ShopSettingActivity.this, LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("action", 0);
                        startActivity(intent);
                        //finish();
                        return;
                    } else if (serverMsg.equals("10012")) {
                        PublicUtils.handshake(ShopSettingActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                updateLoad(headIcon);
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
                                String logo_url = object.getString("logo_url");
                                ShopSettingActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().
                                        putString("logo",UrlsConstant.BASE_PIC_URL + logo_url).
                                        commit();

                                if (headIcon != null)
                                    head_icon_iv.setImageBitmap(headIcon);
                                if (MyApplication.getInstance().getUserInfo() != null) {
                                    MyApplication.getInstance().getUserInfo().setLogo(UrlsConstant.BASE_PIC_URL + logo_url);
                                }
                            }
                            LogUtil.e("ME", "解密数据=" + deData);
                        } catch (Exception e) {
                            showToast("解密异常");
                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        showToast(text);
                    }
                } catch (JSONException e) {
                    showToast(e.toString());
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


    Uri uri = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERE_REQUESTCODE) {
                if (headFile != null && headFile.exists()) {
                    LogUtil.e("ME", "拍照完成");
                    uri = Uri.fromFile(new File(headFile.getAbsolutePath()));
                    startPhotoZoom(uri);
                }
            } else if (requestCode == PIC_REQUESTCODE) {
                if (data != null) {
                    uri = data.getData();
                    startPhotoZoom(uri);
                }
            } else if (requestCode == CROP_PICTURE) {
                if (uri != null) {
                    //保存裁剪之后的图片
                    saveCropImg(uri);
                }
            }
        }
    }
}
