package com.hlzx.ljdjsj.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.utils.LogUtil;


public class ProgressWebView extends WebView {

    private ProgressBar progressbar;

    @SuppressLint("SetJavaScriptEnabled")
	public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        progressbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressbar.setVisibility(GONE);
        progressbar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_style));
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 5, 0, 0));
        addView(progressbar);
        
        //getSettings().setSupportZoom(true);
        //getSettings().setBuiltInZoomControls(true);
        getSettings().setJavaScriptEnabled(true);
        //getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        getSettings().setRenderPriority(RenderPriority.HIGH);
        //getSettings().setBlockNetworkImage(true); 
        
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setSaveFormData(false);
        getSettings().setLoadsImagesAutomatically(true);
        // http�����ʱ��ģ��Ϊ�����UA�����ʵʱ�����Ǳߵ�ҳ��������⣬����ģ��iPhone��ua������������
        //String user_agent ="Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/124 (KHTML, like Gecko) Safari/125.1";
        //getSettings().setUserAgentString(user_agent);

        setWebChromeClient(new WebChromeClient());
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
            } else {
                if (progressbar.getVisibility() == GONE)
                    progressbar.setVisibility(VISIBLE);
                progressbar.setProgress(newProgress);
            }

            LogUtil.e("ME","progress="+newProgress);
            super.onProgressChanged(view, newProgress);
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }
    

}
