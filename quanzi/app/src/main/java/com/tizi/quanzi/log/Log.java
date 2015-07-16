package com.tizi.quanzi.log;

/**
 * Created by qixingchen on 15/7/13.
 */
public class Log {

    private static final boolean DEBUG = true;

    public static void e(String tag, String message) {
        if (DEBUG && message != null) {
            android.util.Log.e(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUG && message != null) {
            android.util.Log.i(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG && message != null) {
            android.util.Log.d(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUG && message != null) {
            android.util.Log.w(tag, message);
        }
    }

    public static void wtf(String tag, String message) {
        if (DEBUG && message != null) {
            android.util.Log.wtf(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (DEBUG && message != null) {
            android.util.Log.v(tag, message);
        }
    }
}

