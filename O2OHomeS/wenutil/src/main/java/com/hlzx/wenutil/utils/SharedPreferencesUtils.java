package com.hlzx.wenutil.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by alan on 2016/3/24.
 */
public class SharedPreferencesUtils {

    static SharedPreferences sp=null;
    static SharedPreferences.Editor editor=null;

    static SharedPreferencesUtils spUtil=null;
    private static  Context mContext;

    SharedPreferencesUtils()
    {
        if(null==sp)
        {
            sp=mContext.getSharedPreferences("wen",Context.MODE_PRIVATE);
        }
    }

    public static final SharedPreferencesUtils getInstance(Context context)
    {
        mContext=context;
        if(spUtil==null)
        {
           spUtil=new SharedPreferencesUtils();
        }
        return spUtil;
    }

    public static final void put(String key,String value)
    {
        if(sp==null)
        {
            new NullPointerException("sp==null,To Invoke getInstance,firstly");
        }
        editor=sp.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static final String get(String key)
    {
        if(sp==null)
        {
            new NullPointerException("sp==null,To Invoke getInstance,firstly");
        }
        return sp.getString(key,"");
    }

}
