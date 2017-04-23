package com.penn.jba;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.penn.jba.databinding.ActivityCreateMomentBinding;
import com.penn.jba.databinding.ActivityLoginBinding;
import com.penn.jba.model.Geo;
import com.penn.jba.realm.model.CurrentUser;
import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.realm.model.Pic;
import com.penn.jba.util.MomentImageAdapter;
import com.penn.jba.util.MomentImagePreviewAdapter;
import com.penn.jba.util.PPHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;

import static com.penn.jba.util.PPRetrofit.authBody;
import static com.penn.jba.util.PPRetrofit.getInstance;

public class CreateMomentActivity extends AppCompatActivity {
    private Context activityContext;

    private ActivityCreateMomentBinding binding;

    private ArrayList<Disposable> disposableList = new ArrayList<Disposable>();

    private FootprintMine footprintMine;

    private Geo geo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityContext = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_moment);
        binding.setPresenter(this);

        setup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Disposable d : disposableList) {
            if (!d.isDisposed()) {
                d.dispose();
            }
        }
    }

    //-----helper-----
    private void setup() {
        geo = PPHelper.getLatestGeo();

        //内容输入监控
        Observable<String> momentContentInputObservable = RxTextView.textChanges(binding.contentInput)
                .skip(1)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return PPHelper.isMomentContentValid(activityContext, charSequence.toString());
                    }
                }).doOnNext(
                        new Consumer<String>() {
                            @Override
                            public void accept(String error) throws Exception {
                                binding.contentInput.setError(TextUtils.isEmpty(error) ? null : error);
                            }
                        }
                );

        //地址输入监控
        Observable<String> placeInputObservable = RxTextView.textChanges(binding.placeInput)
                .skip(1)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return PPHelper.isPlaceValid(activityContext, charSequence.toString());
                    }
                }).doOnNext(
                        new Consumer<String>() {
                            @Override
                            public void accept(String error) throws Exception {
                                binding.placeInput.setError(TextUtils.isEmpty(error) ? null : error);
                            }
                        }
                );

        //发布按钮是否可用
        disposableList.add(Observable.combineLatest(
                momentContentInputObservable,
                placeInputObservable,
                new BiFunction<String, String, Boolean>() {
                    @Override
                    public Boolean apply(String s, String s2) throws Exception {
                        return TextUtils.isEmpty(s) && TextUtils.isEmpty(s2);
                    }
                })
                .subscribeOn(Schedulers.io())
                .distinctUntilChanged()
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Log.v("pplog23", "test");
                        binding.publishBt.setEnabled(aBoolean);
                    }
                })
        );

        //登录按钮监控
        Observable<Object> publishButtonObservable = RxView.clicks(binding.publishBt)
                .debounce(200, TimeUnit.MILLISECONDS);

        disposableList.add(publishButtonObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(
                        new Consumer<Object>() {
                            public void accept(Object o) {
                                publishMoment();
                            }
                        }
                )
        );

        footprintMine = new FootprintMine();
        long timeStamp = System.currentTimeMillis();
        String tmpHash = PPHelper.currentUserId + "_" + timeStamp;
        footprintMine.setHash(tmpHash);
        footprintMine.setCreateTime(timeStamp);
        footprintMine.setStatus("local");
        footprintMine.setType(3);
        footprintMine.setPics(new RealmList<Pic>());

        for (int i = 0; i < 2; i++) {

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            Log.v("pplog25", "" + byteArray.length);

            Pic pic = new Pic();
            pic.setPath(tmpHash + "_" + i);
            pic.setData(byteArray);

            footprintMine.getPics().add(pic);
        }

//        private String hash;
//        private long createTime;
//        private String id;
//        private String status; //local, net, failed
//        private int type;
//        private String body;
//        private RealmList<Pic> pics;

        int widthDp = 64;
        final float scale = activityContext.getResources().getDisplayMetrics().density;
        int width = activityContext.getResources().getDisplayMetrics().widthPixels;
        int pixels = (int) (widthDp * scale + 0.5f);
        int cols = width / pixels;
        binding.imagePreviewGv.setNumColumns(cols);
        MomentImagePreviewAdapter momentImagePreviewAdapter = new MomentImagePreviewAdapter(activityContext, footprintMine.getPics(), pixels);
        binding.imagePreviewGv.setAdapter(momentImagePreviewAdapter);
    }

    private void publishMoment() {
        String content = binding.contentInput.getText().toString();
        String place = binding.placeInput.getText().toString();

        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(place)) {
            PPHelper.showPPToast(activityContext, activityContext.getResources().getString(R.string.moment_content_place_required), Toast.LENGTH_LONG);

            return;
        }

        try {
            JSONArray arr = new JSONArray();
            arr.put(geo.lon);
            arr.put(geo.lat);

            JSONObject body = new JSONObject()
                    .put("detail", new JSONObject()
                            .put("content", content)
                            .put("location", new JSONObject()
                                    .put("geo", arr)
                                    .put("detail", place)
                            )
                    );

            footprintMine.setBody(body.toString());

            Log.v("pplog25", body.toString());

            try (Realm realm = Realm.getDefaultInstance()) {
                realm.beginTransaction();
                final FootprintMine ft = realm.copyToRealm(footprintMine);
                realm.commitTransaction();
            }

            finish();

        } catch (JSONException e) {
            Log.v("ppLog", "api data error:" + e);
            PPHelper.showPPToast(activityContext, e.toString(), Toast.LENGTH_LONG);

            return;
        }


    }
}
