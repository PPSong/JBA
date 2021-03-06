package com.penn.jba.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static android.R.attr.data;

/**
 * Created by penn on 07/04/2017.
 */

public class PPRetrofit

{
    private static final String BASE_URL = "http://jbapp.magicfish.cn/";
    //private static final String BASE_URL = "http://192.168.1.9:3000";
    private static PPRetrofit ppRetrofit = new PPRetrofit();
    public static String authBody;
    private PPJBService ppJBService;

    public PPRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(PPMsgPackConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(getRequestHeader())
                .build();

        ppJBService = retrofit.create(PPJBService.class);
    }

    public static PPRetrofit getInstance() {
        return ppRetrofit;
    }

    public Observable<String> api(String apiName, JSONObject jBody) {
        String request = "";
        try {
            request = new JSONObject()
                    .put("method", apiName)
                    .put("data", jBody == null ? "" : jBody)
                    .put("auth", authBody)
                    .toString();
        } catch (JSONException e) {
            Log.v("ppLog", "api data error:" + e);
        }
        return ppJBService.api(request);
    }

    private OkHttpClient getRequestHeader() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        return httpClient;
    }
}
