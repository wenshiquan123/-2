package com.hlzx.ljdjsj;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.view.ProgressWebView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class WebViewActivity extends BaseFragmentActivity {

	@ViewInject(R.id.title_tv)
	private TextView title_tv;
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

		Intent it = getIntent();
		String title = it.getStringExtra("title");
		title_tv.setText(title);
		String url = it.getStringExtra("url");
		LogUtil.e("ME","url="+url);
		
		webView = (ProgressWebView)findViewById(R.id.mywebview);
		webView.setWebViewClient(new WebViewClient(){
        	@Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            // TODO Auto-generated method stub
	           // view.loadUrl(url);
	            return true;
	        }
        });
		webView.loadUrl(url);
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
            {}
        }
        return super.onKeyDown(keyCode, event);
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
