package com.hlzx.wenutil.http;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Created by alan on 2016/3/12.
 */
public class UserAgent {

    /**
     * Get User-Agent of System.
     *
     * @param context {@link Context}.
     * @return UA.
     */
    public static String getUserAgent(Context context) {
        String webUserAgent = null;
        if(context!=null)
        {
            try {

                Class<?> sysResCls=Class.forName("com.android.internal.R$string");
                Field webUserAgentField =sysResCls.getDeclaredField("web_user_agent");
                Integer resId=(Integer)webUserAgentField.get(null);
                webUserAgent=context.getString(resId);

            }catch (Exception e)
            {
                // we have nothing to do
            }
        }
        if(TextUtils.isEmpty(webUserAgent))
        {
            webUserAgent="Mozilla/5.0 (Linux; U; Android %s) AppleWebKit/533.1 (KHTML, like Gecko) Version/5.0 %sSafari/533.1";
        }

        Locale locale=Locale.getDefault();
        StringBuffer buffer=new StringBuffer();
        //Add version
        final String version= Build.VERSION.RELEASE;
        if(version.length()>0)
        {
            buffer.append(version);
        }else
        {
            buffer.append("1.0");
        }

        buffer.append("; ");
        final String language=locale.getLanguage();
        if(language!=null)
        {
            buffer.append(language.toLowerCase(locale));
            final String country=locale.getCountry();
            if(!TextUtils.isEmpty(country))
            {
                buffer.append("-");
                buffer.append(country.toLowerCase(locale));
            }
        }else
        {
            //default to "en"
            buffer.append("en");
        }
        //add the model for the release build
        if("REL".equals(Build.VERSION.CODENAME))
        {
            final String model=Build.MODEL;
            if(model.length()>0)
            {
                buffer.append("; ");
                buffer.append(model);
            }
        }

        final String id=Build.ID;
        if(id.length()>0)
        {
            buffer.append(" Build/");
            buffer.append(id);
        }
        return String.format(webUserAgent,buffer,"Mobile ");
    }
}
