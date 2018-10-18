package com.hlzx.ljdjsj.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.hlzx.ljdjsj.BaseFragmentActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.NetWorkUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.hlzx.ljdjsj.view.ProgressWebView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RegulationWebViewActivity extends BaseFragmentActivity implements android.os.Handler.Callback{

	@ViewInject(R.id.title_tv)
	private TextView title_tv;

	Handler handler=new Handler(this);

	private static final int LOAD_URL=1001;

	@Override
	public void layout() {
       setContentView(R.layout.mywebview);
	}

	ProgressWebView webView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initView();
	}

	private void initView()
	{
		title_tv.setText("商品信息发布规则");
		webView = (ProgressWebView)findViewById(R.id.mywebview);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				//view.loadUrl(url);
				return true;
			}
		});
		loadData();
	}
	@Override
	public boolean handleMessage(Message msg) {
		if(msg.what==LOAD_URL)
		{
			String deData=(String)msg.obj;
			com.alibaba.fastjson.JSONObject object=JSON.parseObject(deData);
			String url=object.getString("rule_url");
			//String url="http://www.youku.com";

			LogUtil.e("ME","url="+url);
			webView.loadUrl(url);
		}
		return false;
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if(webView.canGoBack())
            {
                webView.goBack();
                return true;
            }
            else
            {
            	
            }
        }
        return super.onKeyDown(keyCode, event);
    }

	private void loadData() {

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
			String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
			map.put("security_session_id", MyApplication.getSession_id());
			map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
			map.put("data", edata1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		HttpUtil.doPostRequest(UrlsConstant.GOODS_RELEASE_RULE, map, new RequestCallBack<String>() {
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
						showToast(text);
						Intent intent = new Intent(RegulationWebViewActivity.this, LoginActivity.class);
						intent.putExtra("action",0);
						//intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						startActivity(intent);
						finish();
						return;
					}

					if (status == HttpConstant.SUCCESS_CODE) {
						String deData = null;
						deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
						if (deData != null) {
							LogUtil.e("ME", "解密数据=" + deData);
							Message msg = handler.obtainMessage(LOAD_URL);
							msg.obj = deData;
							handler.sendMessage(msg);
						}
					} else if (status == HttpConstant.FAILURE_CODE) {
						showToast(text);
					}
				} catch (Exception e) {
					showToast(e.toString());
				}

			}

			@Override
			public void onFailure(HttpException e, String s) {
				showToast(s);
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		webView.destroy();
		super.onDestroy();
	}

	public void back(View view)
	{
		finish();
	}
	
	

}
