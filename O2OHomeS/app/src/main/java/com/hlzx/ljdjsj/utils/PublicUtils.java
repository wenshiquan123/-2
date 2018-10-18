package com.hlzx.ljdjsj.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.activity.LoginActivity;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.hlzx.ljdjsj.utils.http.CryptLib;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Administrator on 2015/6/2.
 */
public class PublicUtils {
    // 动态隐藏软键盘
    public static void hideSoftInput(Activity activity) {
        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 动态隐藏软键盘
    public static void hideSoftInput(Context context, EditText edit) {
        edit.clearFocus();
        InputMethodManager inputmanger = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmanger.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    /**
     * 强制显示
     *
     * @param context
     * @param etitText
     */
    public static void showSoftKeyboard(Context context, EditText etitText) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etitText, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 给输入框以焦点和弹出软键盘
     *
     * @param edit
     */
    public static void FreshSoftKeyboard(EditText edit) {
        edit.requestFocus();
        InputMethodManager imm = (InputMethodManager) edit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    public static void popSoftkeyboard(Context ctx, View view, boolean wantPop) {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (wantPop) {
            view.requestFocus();
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 防止按钮连续点击
    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static String dayToNow(long time) {
        Calendar now = Calendar.getInstance();

        long minute = (now.getTimeInMillis() - time) / 60000;
        if (minute < 60) {
            if (minute == 0) {
                return "刚刚";
            } else {
                return minute + "分钟前";
            }
        }

        long hour = minute / 60;
        if (hour < 24) {
            return hour + "小时前";
        }

        long day = hour / 24;
        if (day < 30) {
            return day + "天前";
        }

        long month = day / 30;
        if (month < 12) {
            return month + "个月前";
        }

        long year = month / 12;
        return year + "年前";
    }


    /**
     * 验证手机格式是否正确
     */
    public static boolean checkPhone(String str) {
        /*Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");

        Matcher m = p.matcher(str);
        return m.matches();*/
        String regExp = "^[1]([3][0-9]{1}|59|58|88|81|89)[0-9]{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * UUID
     */
    private static UUID uuid = null;

    /**
     * Returns unique UUID of the device
     *
     * @param context Context
     * @return UUID
     */
    public static UUID getDeviceUUID(Context context) {
        if (context == null) {
            return null;
        }

        if (uuid == null) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String id = tm.getDeviceId();
            if (id != null) {
                uuid = UUID.nameUUIDFromBytes(id.getBytes());
            }
        }

        return uuid;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * 拆分集合
     *
     * @param <T>
     * @param resList 要拆分的集合
     * @param count   每个集合的元素个数
     * @return 返回拆分后的各个集合
     */
    public static <T> List<List<T>> split(List<T> resList, int count) {

        if (resList == null || count < 1)
            return null;
        List<List<T>> ret = new ArrayList<List<T>>();
        int size = resList.size();
        if (size <= count) { //数据量不足count指定的大小
            ret.add(resList);
        } else {
            int pre = size / count;
            int last = size % count;
            //前面pre个集合，每个大小都是count个元素
            for (int i = 0; i < pre; i++) {
                List<T> itemList = new ArrayList<T>();
                for (int j = 0; j < count; j++) {
                    itemList.add(resList.get(i * count + j));
                }
                ret.add(itemList);
            }
            //last的进行处理
            if (last > 0) {
                List<T> itemList = new ArrayList<T>();
                for (int i = 0; i < last; i++) {
                    itemList.add(resList.get(pre * count + i));
                }
                ret.add(itemList);
            }
        }
        return ret;
    }

    //使用String的split 方法
    public static String[] convertStrToArray(String str) {
        String[] strArray = null;
        strArray = str.split(","); //拆分字符为"," ,然后把结果交给数组strArray
        return strArray;
    }


    /**
     * 握手操作
     */
    static Dialog dialog=null;
    public static void handshake(final Context context,final ShakeHandCallback callback) {

        dialog=PublicDialog.createLoadingDialog(context,"正在连接...");
        dialog.show();

        Map<String, String> map = null;
        try {
            String[] data = ClientEncryptionPolicy.getInstance().
                    encryptShakeHandsData(PublicUtils.getDeviceInfo(MyApplication.getInstance().
                            getApplicationContext()).toString());
            map = new HashMap();
            map.put("sha1", data[0]);
            map.put("data", URLEncoder.encode(data[1], "utf-8"));
            LogUtil.e("ME", "sha1=" + data[0] + "|data=" + URLEncoder.encode(data[1], "utf-8"));

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("ME", "异常=" + e.toString());
        }
        LogUtil.e("ME", "url=" + UrlsConstant.SHAKE_HAND);
        HttpUtil.doPostRequest(UrlsConstant.SHAKE_HAND, map,new RequestCallBack() {
                    @Override
                    public void onSuccess(ResponseInfo responseInfo) {
                        String result = responseInfo.result.toString();
                        LogUtil.e("握手返回信息=" + result);
                        try {
                            JSONObject object= new JSONObject(result);
                            int status=object.getInt("status");
                            String data=object.getString("data");
                            String text=object.getString("text");

                            if (status== HttpConstant.SUCCESS_CODE) {
                                com.alibaba.fastjson.JSONObject object1=JSON.parseObject(data);
                                //保存session_id
                                MyApplication.getInstance().setSession_id(object1.getString("security_session_id"));
                                context.getSharedPreferences("userInfo",Context.MODE_PRIVATE).edit().
                                        putString("security_session_id", object1.getString("security_session_id")).
                                        putString("aesKey",ClientEncryptionPolicy.getAesKey()).
                                        commit();
                               // LogUtil.e("ME", "保存的session_id=" + object1.getString("security_session_id"));

                                callback.onSuccessed(text);

                            } else if (status== HttpConstant.FAILURE_CODE)
                            {
                                //Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
                                callback.onFalied(text);
                            }
                        } catch (Exception e) {
                            //Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                            callback.onFalied(e.toString());
                        }

                        if(dialog!=null) {
                            dialog.dismiss();
                            dialog=null;
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        LogUtil.e("ME", "握手失败=" + s);
                        callback.onFalied(s);
                        if(dialog!=null) {
                            dialog.dismiss();
                            dialog=null;
                        }
                        //Toast.makeText(context, "连接服务器失败，请检查网络重新连接", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * 收集设备参数信息
     */
    public static String getDeviceInfo(Context context) {
        String result = getImei(context) + ":" + getImis(context) + ":" +getMac(context).toLowerCase()+ ":" + getMobileBrand() + ":" + getMobileVersion() + ":" + getHardVersion() + ":" + getDeviceUUID(context).toString();
        LogUtil.e("ME", "加密前" + result);
        try {
            result = CryptLib.SHA256(result, 32);
            LogUtil.e("ME", "加密后" + result);
        } catch (Exception e) {

        }
        return result;
    }

    // 得到手机卡imsi
    public static String getImis(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    // 得到手机卡imei
    public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    // 得到手机mac地址
    public static String getMac(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    // 手机型号
    public static String getMobileBrand() {
        return android.os.Build.MODEL;
    }

    // 手机系统版本
    public static String getMobileVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    // 手机硬件版本号
    public static String getHardVersion() {
        return android.os.Build.BRAND;
    }


    /**
     * @param uploadFile
     * @param actionUrl
     * @param fileName
     * @see上传图片、文件
     */
    public static void uploadFile(File uploadFile, String actionUrl, String fileName) {
        String end = "/r/n";
        String hyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(actionUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //设置属性
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(true);

            connection.setRequestMethod("POST");
            /**
             * 设置上传文件头
             */
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            /**
             * 设置请求头
             */
            DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
            ds.writeBytes(hyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data;name=\"file1\";filename=\"" + fileName + "\"" + end);
            ds.writeBytes(end);

            /* 取得文件的FileInputStream */
            FileInputStream fStream = new FileInputStream(uploadFile);
           /* 设定每次写入1024bytes */
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            /* 从文件读取数据到缓冲区 */
            while ((length = fStream.read(buffer)) != -1) {
             /* 将数据写入DataOutputStream中 */
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(hyphens + boundary + hyphens + end);//分界符
            fStream.close();
            ds.flush();
            /* 取得Response内容 */
            InputStream is = connection.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            System.out.println("上传成功");
            ds.close();

        } catch (Exception e) {
            System.out.println("上传失败");
        }


    }


}
