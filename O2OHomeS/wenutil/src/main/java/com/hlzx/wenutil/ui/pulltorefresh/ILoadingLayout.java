package com.hlzx.wenutil.ui.pulltorefresh;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * Created by alan on 2016/3/17.
 */
public interface ILoadingLayout {

    void setLastUpdateLabel(CharSequence lastUpdateLabel);

    void setLoadingDrawable(Drawable loadingDrawable);

    void setPullLabel(CharSequence pullLabel);

    void setRefreshingLabel(CharSequence refreshingLabel);

    void setReleaseLabel(CharSequence releaseLabel);

    void setTextTypeface(Typeface tf);

}
