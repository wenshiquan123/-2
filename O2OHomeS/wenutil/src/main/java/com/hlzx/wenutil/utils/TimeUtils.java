package com.hlzx.wenutil.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alan on 2016/3/24.
 */
public class TimeUtils {

    public static String getCurrentHHMM()
    {
        String curTime;
        SimpleDateFormat format=new SimpleDateFormat("HH:mm");
        curTime=format.format(new Date());
        return curTime;
    }
}
