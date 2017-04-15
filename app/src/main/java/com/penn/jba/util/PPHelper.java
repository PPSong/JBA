package com.penn.jba.util;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.penn.jba.R;
import com.penn.jba.realm.model.CurrentUser;
import com.penn.jba.realm.model.CurrentUserSetting;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by penn on 02/04/2017.
 */

public class PPHelper {
    //秒
    public static final int REQUEST_VERIFY_CODE_INTERVAL = 5;

    //pptodo remove testing block
    public static BehaviorSubject<Boolean> testingInit = BehaviorSubject.<Boolean>create();

    public static void ppTestInit(Context context, String phone, String pwd, boolean clearData) {
        if (pwd != null) {
            ppTestSignIn(context, phone, pwd, clearData);
        }
        else {
            initRealm(context, phone, false);
            try (Realm realm = Realm.getDefaultInstance()) {

                CurrentUser currentUser = realm.where(CurrentUser.class)
                        .findFirst();

                //设置PPRetrofit authBody
                try {
                    String authBody = new JSONObject()
                            .put("userid", currentUser.getUserId())
                            .put("token", currentUser.getToken())
                            .put("tokentimestamp", currentUser.getTokenTimestamp())
                            .toString();
                    PPRetrofit.authBody = authBody;
                } catch (JSONException e) {
                    Log.v("ppLog", "api data error:" + e);
                }
                testingInit.onNext(true);
            }
        }
    }

    private static void ppTestSignIn(final Context activityContext, String phone, String pwd, final boolean clearData) {
        PPJSONObject jBody = new PPJSONObject();
        jBody
                .put("phone", phone)
                .put("pwd", pwd);

        final Observable<String> apiResult = PPRetrofit.getInstance().api("user.login", jBody.getJSONObject());
        apiResult
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<String>() {
                            public void accept(String s) {
                                Log.v("ppLog", "get result:" + s);

                                PPWarn ppWarn = PPHelper.ppWarning(s);
                                if (ppWarn != null) {
                                    Toast.makeText(activityContext, ppWarn.msg, Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String phone = PPHelper.ppFromString(s, "data.extra.0").getAsString();
                                initRealm(activityContext, phone, clearData);
                                try (Realm realm = Realm.getDefaultInstance()) {
                                    realm.beginTransaction();

                                    CurrentUser currentUser = realm.where(CurrentUser.class)
                                            .findFirst();

                                    CurrentUserSetting currentUserSetting;

                                    if (currentUser == null) {
                                        //新注册用户或者首次在本手机使用
                                        currentUser = realm.createObject(CurrentUser.class);
                                        //默认在足迹页面不是显示我的moment
                                        currentUserSetting = realm.createObject(CurrentUserSetting.class);
                                        currentUserSetting.setFootprintMine(false);
                                    }

                                    currentUser.setUserId(PPHelper.ppFromString(s, "data.userid").getAsString());
                                    currentUser.setToken(PPHelper.ppFromString(s, "data.token").getAsString());
                                    currentUser.setTokenTimestamp(PPHelper.ppFromString(s, "data.tokentimestamp").getAsLong());
                                    currentUser.setPhone(phone);
                                    currentUser.setNickname(PPHelper.ppFromString(s, "data.extra.1").getAsString());
                                    currentUser.setGender(PPHelper.ppFromString(s, "data.extra.2").getAsInt());
                                    currentUser.setBirthday(PPHelper.ppFromString(s, "data.extra.5").getAsLong());

                                    realm.commitTransaction();

                                    //设置PPRetrofit authBody
                                    try {
                                        String authBody = new JSONObject()
                                                .put("userid", currentUser.getUserId())
                                                .put("token", currentUser.getToken())
                                                .put("tokentimestamp", currentUser.getTokenTimestamp())
                                                .toString();
                                        PPRetrofit.authBody = authBody;
                                    } catch (JSONException e) {
                                        Log.v("ppLog", "api data error:" + e);
                                    }
                                    testingInit.onNext(true);
                                }
                            }
                        },
                        new Consumer<Throwable>() {
                            public void accept(Throwable t1) {

                                Toast.makeText(activityContext, t1.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.v("ppLog", "error:" + t1.toString());
                                t1.printStackTrace();
                            }
                        }
                );
    }
    //pptodo end testing block

    public static void initRealm(Context context, String phone, boolean clearData) {
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(phone + ".realm")
                .build();
        //清除当前用户的数据文件, 测试用
        if (clearData) {
            Realm.deleteRealm(config);
        }

        Realm.setDefaultConfiguration(config);

        Stetho.initialize(
                Stetho.newInitializerBuilder(context)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(context).build())
                        .build());
    }

    public static String isPhoneValid(Context context, String phone) {
        String error = "";
        if (TextUtils.isEmpty(phone)) {
            error = context.getString(R.string.error_field_required);
        } else if (!Pattern.matches("\\d{11}", phone)) {
            error = context.getString(R.string.error_invalid_phone);
        }

        return error;
    }

    public static String isPasswordValid(Context context, String password) {
        String error = "";
        if (TextUtils.isEmpty(password)) {
            error = context.getString(R.string.error_field_required);
        } else if (!Pattern.matches("\\w{6,12}", password.toString())) {
            error = context.getString(R.string.error_invalid_password);
        }

        return error;
    }

    public static String isNicknameValid(Context context, String nickname) {
        String error = "";
        if (TextUtils.isEmpty(nickname)) {
            error = context.getString(R.string.error_field_required);
        } else if (!Pattern.matches("\\w{3,12}", nickname.toString())) {
            error = context.getString(R.string.error_invalid_nickname);
        }

        return error;
    }

    public static String isVerifyCodeValid(Context context, String verifyCode) {
        String error = "";
        if (TextUtils.isEmpty(verifyCode)) {
            error = context.getString(R.string.error_field_required);
        } else if (!Pattern.matches("\\d{6}", verifyCode)) {
            error = context.getString(R.string.error_invalid_verify_code);
        }

        return error;
    }

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static String isBirthdayValid(Context context, String birthday) {
        String error = "";
        if (TextUtils.isEmpty(birthday)) {
            error = context.getString(R.string.error_field_required);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);
            try {
                dateFormat.parse(birthday.trim());
            } catch (ParseException pe) {
                error = context.getString(R.string.error_invalid_birthday);
            }
        }

        return error;
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
                //Log.v("ppLog", "解析整个json String");
                return item;
            }
            String[] seg = path.split("\\.");
            for (int i = 0; i < seg.length; i++) {
                if (i > 0) {
                    //Log.v("ppLog", "解析完毕:" + seg[i - 1]);
                    //Log.v("ppLog", "-------");
                }
                //Log.v("ppLog", "准备解析:" + seg[i]);
                if (seg[i].length() == 0) {
                    //""情况
                    //Log.v("ppLog", "解析空字符串的path片段, 停止继续解析");
                    return null;
                }
                if (item != null) {
                    //当前path片段item不为null
                    //Log.v("ppLog", "当前path片段item不为null");
                    if (item.isJsonArray()) {
                        //当前path片段item为数组
                        //Log.v("ppLog", "当前path片段item为数组");
                        String regex = "\\d+";
                        if (Pattern.matches("\\d+", seg[i])) {
                            //当前path片段描述为数组格式
                            //Log.v("ppLog", "当前path片段描述为数组格式");
                            item = item.getAsJsonArray().get(Integer.parseInt(seg[i]));
                        } else {
                            //当前path片段描述不为数组格式
                            //Log.v("ppLog", "当前path片段描述不为数组格式");
                            //Log.v("ppLog", "path中间片段描述错误:" + seg[i] + ", 停止继续解析");
                            return null;
                        }
                    } else if (item.isJsonObject()) {
                        //当前path片段item为JsonObject
                        //Log.v("ppLog", "当前path片段item为JsonObject");
                        item = item.getAsJsonObject().get(seg[i]);
                    } else {
                        //当前path片段item为JsonPrimitive
                        //Log.v("ppLog", "当前path片段item为JsonPrimitive");
                        //Log.v("ppLog", "path中间片段取值为JsonPrimitive, 停止继续解析");
                        return null;
                    }
                } else {
                    //当前path片段item为null
                    //Log.v("ppLog", "当前path片段item为null");
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
