package com.penn.jba.util;

import android.content.Context;

import java.util.regex.Pattern;

/**
 * Created by penn on 02/04/2017.
 */

public class PPHelper {
    //秒
    public static final int REQUEST_VERIFY_CODE_INTERVAL = 5;

    public static boolean isPhoneValid(String phone) {
        return Pattern.matches("\\d{11}", phone);
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }


    public static boolean isVerfifyCodeValid(String password) {
        return password.length() == 4;
    }

    public static void setLastVerifyCodeRequestTime(Context context) {
        context.getSharedPreferences("JBA", Context.MODE_PRIVATE).edit().putLong("LastVerifyCodeRequestTime",  System.currentTimeMillis()/1000).apply();
    }

    public static long getLastVerifyCodeRequestTime(Context context) {
        return context.getSharedPreferences("JBA", Context.MODE_PRIVATE).getLong("LastVerifyCodeRequestTime", 0);
    }
}
