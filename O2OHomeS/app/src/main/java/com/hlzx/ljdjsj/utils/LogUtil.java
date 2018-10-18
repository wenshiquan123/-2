package com.hlzx.ljdjsj.utils;

import android.util.Log;

public class LogUtil {
    public static boolean islog = true;

    /**
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        if (islog)
        {
            message = message == null ? "为空" : message;
            Log.d(tag, message);
        }
    }


    /**
     * @param tag
     * @param message
     */
    public static void e(String tag, String message) {
        if (islog) {
            message = message == null ? "为空" : message;
            Log.e(tag, message);
        }
    }

    /**
     * @param message
     */
    public static void e(String message) {
        if (islog) {
            message = message == null ? "为空" : message;
            Log.e("ME", message);
        }
    }

    /**
     * @param tag
     * @param message
     */
    public static void i(String tag, String message) {
        if (islog) {
            message = message == null ? "为空" : message;
            Log.e(tag, message);
        }
    }

    /**
     * @param message
     */
    public static void i(String message) {
        if (islog) {
            message = message == null ? "为空" : message;
            Log.e("ME", message);
        }
    }
}
