package com.hlzx.wenutil.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.hlzx.wenutil.R;
import com.hlzx.wenutil.ui.ProgressWebView;


public class WebViewActivity extends Activity {


	private TextView title_tv;

	ProgressWebView webView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_webview);
		Intent it = getIntent();
		String url = it.getStringExtra("url");
		
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
