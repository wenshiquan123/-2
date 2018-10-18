/*
 * Copyright © YOLANDA. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hlzx.test.activity;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hlzx.test.R;
import com.hlzx.test.interfaces.HttpListener;
import com.hlzx.test.interfaces.HttpResponseListener;
import com.hlzx.test.util.SSLContextUtil;
import com.hlzx.wenutil.activity.WebViewActivity;
import com.hlzx.wenutil.http.NoHttp;
import com.hlzx.wenutil.http.Request;
import com.hlzx.wenutil.http.RequestMethod;
import com.hlzx.wenutil.http.RequestQueue;
import com.hlzx.wenutil.http.Response;
import com.hlzx.wenutil.ui.AdLinearLayout;
import com.hlzx.wenutil.ui.pulltorefresh.OnRefreshListener;
import com.hlzx.wenutil.ui.pulltorefresh.PullToRefreshBase;
import com.hlzx.wenutil.ui.pulltorefresh.PullToRefreshScrollView;

/**
 * Created in Nov 3, 2015 1:48:34 PM.
 *
 * @author YOLANDA;
 */
public class HttpsActivity extends Activity implements View.OnClickListener, HttpListener<String> {

    /**
     * 显示请求结果.
     */
    private TextView mTvResult;

    /**
     * 请求队列.
     */
    private RequestQueue requestQueue;

    PullToRefreshScrollView pullToRefreshScrollView;

    AdLinearLayout ad_ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("https");
        setContentView(R.layout.activity_https);

        pullToRefreshScrollView=(PullToRefreshScrollView)findViewById(R.id.pull_sv);

        ad_ll=(AdLinearLayout)findViewById(R.id.ad_ll);

        findViewById(R.id.btn_https_reqeust).setOnClickListener(this);
        mTvResult =(TextView)findViewById(R.id.tv_result);
        requestQueue=NoHttp.newRequestQueue();



        pullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshBase refreshView) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       pullToRefreshScrollView.refreshFinished();
                        com.hlzx.test.util.Toast.show("刷新完成");
                    }
                },2000);
            }
        });

    }



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_https_reqeust) {
            //Request<String> httpsRequest = NoHttp.createStringRequest("http://www.baidu.com", RequestMethod.POST);
            /*SSLContext sslContext = SSLContextUtil.getSSLContext();
            if (sslContext != null) {
                SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                httpsRequest.setSSLSocketFactory(socketFactory);
            }*/

           // requestQueue.add(0, httpsRequest, new HttpResponseListener<String>(this,httpsRequest,this,true,true));

            //ad_ll.start();
            Intent intent=new Intent(this, WebViewActivity.class);
            intent.putExtra("url","http://www.youku.com");
            startActivity(intent);
        }
    }

    @Override
    public void onSucceed(int what, Response<String> response) {
        mTvResult.setText("成功：\n" + response.get());
    }

    @Override
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
        mTvResult.setText("失败：\n" + exception.getMessage());
    }
}
