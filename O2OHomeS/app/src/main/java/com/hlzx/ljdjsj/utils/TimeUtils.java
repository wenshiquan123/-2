package com.hlzx.ljdjsj.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alan on 2015/12/17.
 */
public class TimeUtils {

    public static String getCurTimeYMD()
    {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }
}
