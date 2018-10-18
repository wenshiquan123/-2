package com.hlzx.ljdjsj.utils;

import android.content.Context;

import com.hlzx.ljdjsj.MyApplication;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;


import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2015/6/15.
 */
public class HttpUtil {

    /**
     * 使用get请求获取服务器数据
     *
     * @param url      ：请求地址
     * @param map      ：请求参数集合
     * @param callBack ：请求结果的回调
     */
    @SuppressWarnings("unused")
    public static void doGetRequest(String url, Map<String, String> map,
                                    RequestCallBack<String> callBack) {
        HttpUtils utils = MyApplication.getApplication().getHttpUtils();
        RequestParams params = null;
        if (map != null) {
            params = new RequestParams();
            Set<String> set = map.keySet();
            for (String string : set) {
                params.addQueryStringParameter(string, map.get(string));
            }
        }
        utils.send(HttpRequest.HttpMethod.GET, url, params, callBack);
    }

    /**
     * 使用Post请求获取服务器数据
     *
     * @param url      ：请求地址
     * @param map      ：请求参数集合
     * @param callBack ：请求结果的回调
     */
    public static void doPostRequest(String url, Map<String, String> map,
                                     RequestCallBack<String> callBack) {
        HttpUtils utils = MyApplication.getApplication().getHttpUtils();
        RequestParams params = null;
        if (map != null) {
            params = new RequestParams();
            Set<String> set = map.keySet();
            for (String string : set) {
                params.addBodyParameter(string, map.get(string));
            }
        }
        utils.send(HttpRequest.HttpMethod.POST, url, params, callBack);
    }

    /**
     * 上传文件到服务器
     *
     * @param url
     * @param map
     * @param updataFile
     * @param callBack
     */
    public static void uploadFile(String url, Map<String, String> map, File updataFile, RequestCallBack<String> callBack) {
        HttpUtils utils = MyApplication.getApplication().getHttpUtils();
        RequestParams params = new RequestParams();
        if (map != null) {
            Set<String> set = map.keySet();
            for (String string : set) {
                params.addBodyParameter(string, map.get(string));
            }
        }
        params.addBodyParameter("upload_file", updataFile);
        utils.send(HttpRequest.HttpMethod.POST, url, params, callBack);
    }
    /**
     * 下载文件保存到sd卡
     *
     * @param context
     * @param url
     */

    public static void addToDownloadFile(Context context, final String url,RequestCallBack<String> callBack) {
        HttpUtils utils = MyApplication.getApplication().getHttpUtils();
        LogUtil.e("ME", "音频要下载了");
        utils.download(url, url, true, false, new RequestCallBack() {
            @Override
            public void onSuccess(ResponseInfo responseInfo) {
                LogUtil.e("ME", "音频下载成功了");
            }

            @Override
            public void onFailure(HttpException e, String s) {
            }
        });
    }
}
