package com.alading.dream.utils;

import android.util.Log;

public class MyLog {
    private static final String TAG = "MyLog";
    private static boolean isShow = true;

    public static void logTime(String args) {
        if (isShow) {
            Log.d("logTime", args);
        }
    }

    public static void logE(String args) {
        if (isShow) {
            Log.e("errorMsg", args);
        }
    }

    public static void logD(String args) {
        if (isShow) {
            Log.d("logMsg", args);
        }
    }
    public static void logDebug(String args){
        if (isShow) {
            Log.d("logDebug", args);
        }
    }
    public static void logTemple(String args) {
            Log.d("logMsg", args);
    }
}
