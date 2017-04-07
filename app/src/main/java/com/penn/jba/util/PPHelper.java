package com.penn.jba.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
        context.getSharedPreferences("JBA", Context.MODE_PRIVATE).edit().putLong("LastVerifyCodeRequestTime", System.currentTimeMillis() / 1000).apply();
    }

    public static long getLastVerifyCodeRequestTime(Context context) {
        return context.getSharedPreferences("JBA", Context.MODE_PRIVATE).getLong("LastVerifyCodeRequestTime", 0);
    }

    public static PPWarn ppWarning(String jServerResponse) {
        int code = ppFromString(jServerResponse, "code").getAsInt();
        if (code != 1) {
            return new PPWarn(jServerResponse);
        } else {
            return null;
        }
    }

    public static JsonElement ppFromString(String json, String path) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement item = parser.parse(json);
            if (path == null || path.length() == 0 || Pattern.matches("\\.+", path)) {
                Log.v("ppLog", "解析整个json String");
                return item;
            }
            String[] seg = path.split("\\.");
            for (int i = 0; i < seg.length; i++) {
                if (i > 0) {
                    Log.v("ppLog", "解析完毕:" + seg[i - 1]);
                    Log.v("ppLog", "-------");
                }
                Log.v("ppLog", "准备解析:" + seg[i]);
                if (seg[i].length() == 0) {
                    //""情况
                    Log.v("ppLog", "解析空字符串的path片段, 停止继续解析");
                    return null;
                }
                if (item != null) {
                    //当前path片段item不为null
                    Log.v("ppLog", "当前path片段item不为null");
                    if (item.isJsonArray()) {
                        //当前path片段item为数组
                        Log.v("ppLog", "当前path片段item为数组");
                        String regex = "\\d+";
                        if (Pattern.matches("\\d+", seg[i])) {
                            //当前path片段描述为数组格式
                            Log.v("ppLog", "当前path片段描述为数组格式");
                            item = item.getAsJsonArray().get(Integer.parseInt(seg[i]));
                        } else {
                            //当前path片段描述不为数组格式
                            Log.v("ppLog", "当前path片段描述不为数组格式");
                            Log.v("ppLog", "path中间片段描述错误:" + seg[i] + ", 停止继续解析");
                            return null;
                        }
                    } else if (item.isJsonObject()) {
                        //当前path片段item为JsonObject
                        Log.v("ppLog", "当前path片段item为JsonObject");
                        item = item.getAsJsonObject().get(seg[i]);
                    } else {
                        //当前path片段item为JsonPrimitive
                        Log.v("ppLog", "当前path片段item为JsonPrimitive");
                        Log.v("ppLog", "path中间片段取值为JsonPrimitive, 停止继续解析");
                        return null;
                    }
                } else {
                    //当前path片段item为null
                    Log.v("ppLog", "当前path片段item为null");
                    Log.v("ppLog", "path中间片段取值为null, 停止继续解析");
                    return null;
                }
            }
            return item;
        } catch (Exception e) {
            Log.v("ppLog", "Json解析错误" + e);
            return null;
        }
    }
}
