package com.penn.jba;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.penn.jba.databinding.ActivityLoginBinding;
import com.penn.jba.realm.model.CurrentUser;
import com.penn.jba.realm.model.CurrentUserSetting;
import com.penn.jba.realm.model.Pic;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPJSONObject;
import com.penn.jba.util.PPRetrofit;
import com.penn.jba.util.PPWarn;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.realm.Realm;
import io.realm.RealmList;

import static com.penn.jba.util.PPRetrofit.authBody;

public class LoginActivity extends AppCompatActivity {
    private Context activityContext;

    private ActivityLoginBinding binding;

    private ArrayList<Disposable> disposableList = new ArrayList<Disposable>();

    private BehaviorSubject<Boolean> jobProcessing = BehaviorSubject.<Boolean>create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityContext = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setPresenter(this);

        //设置键盘返回键的快捷方式
        binding.passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.sign_in_ime || id == EditorInfo.IME_NULL) {
                    signIn();
                    return true;
                }
                return false;
            }
        });

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

    @Override
    protected void onNewIntent(Intent intent) {
        String signInResult = intent.getStringExtra("signInResult");
        if (signInResult != null) {
            try {
                startUpOk(signInResult);
            } catch (Exception e) {
                Log.v("pplog", "login onNewIntent error:" + e);
                PPHelper.showPPToast(this, "signInOk error:" + e, Toast.LENGTH_LONG);
            }
        } else {
        }
    }

    //-----helper-----
    private void setup() {
        //先发送个初始事件,便于判断按钮是否可用
        jobProcessing.onNext(false);

        //手机号码输入监控
        Observable<String> phoneInputObservable = RxTextView.textChanges(binding.phoneInput)
                .skip(1)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return PPHelper.isPhoneValid(activityContext, charSequence.toString());
                    }
                }).doOnNext(
                        new Consumer<String>() {
                            @Override
                            public void accept(String error) throws Exception {
                                binding.phoneInputLayout.setError(TextUtils.isEmpty(error) ? null : error);
                            }
                        }
                );

        //密码输入监控
        Observable<String> passwordInputObservable = RxTextView.textChanges(binding.passwordInput)
                .skip(1)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return PPHelper.isPasswordValid(activityContext, charSequence.toString());
                    }
                }).doOnNext(
                        new Consumer<String>() {
                            @Override
                            public void accept(String error) throws Exception {
                                binding.passwordInputLayout.setError(TextUtils.isEmpty(error) ? null : error);
                            }
                        }
                );

        //登录按钮是否可用
        disposableList.add(Observable.combineLatest(
                phoneInputObservable,
                passwordInputObservable,
                jobProcessing,
                new Function3<String, String, Boolean, Boolean>() {
                    @Override
                    public Boolean apply(String s, String s2, Boolean aBoolean) throws Exception {
                        return TextUtils.isEmpty(s) && TextUtils.isEmpty(s2) && !aBoolean;
                    }
                })
                .subscribeOn(Schedulers.io())
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        binding.signInButton.setEnabled(aBoolean);
                    }
                })
        );

        //登录按钮监控
        Observable<Object> signInButtonObservable = RxView.clicks(binding.signInButton)
                .debounce(200, TimeUnit.MILLISECONDS);

        disposableList.add(signInButtonObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(
                        new Consumer<Object>() {
                            public void accept(Object o) {
                                signIn();
                            }
                        }
                )
        );

        //忘记密码按钮监控
        Observable<Object> forgetPasswordButtonObservable = RxView.clicks(binding.forgetPasswordButton)
                .debounce(200, TimeUnit.MILLISECONDS);

        disposableList.add(forgetPasswordButtonObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(
                        new Consumer<Object>() {
                            public void accept(Object o) {
                                goForgetPassword();
                            }
                        }
                )
        );

        //注册按钮监控
        Observable<Object> createNewAccountButtonObservable = RxView.clicks(binding.createNewAccountButton)
                .debounce(200, TimeUnit.MILLISECONDS);

        disposableList.add(createNewAccountButtonObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(
                        new Consumer<Object>() {
                            public void accept(Object o) {
                                goSignUp();
                            }
                        }
                )
        );

        //忘记密码,注册新账号是否可用,进度条是否可见
        disposableList.add(jobProcessing
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                binding.forgetPasswordButton.setEnabled(!aBoolean);
                                binding.createNewAccountButton.setEnabled(!aBoolean);
                                binding.loginProgress.setVisibility(aBoolean ? View.VISIBLE : View.INVISIBLE);
                            }
                        }
                )
        );
    }

    private void signIn() {
        jobProcessing.onNext(true);
        PPJSONObject jBody = new PPJSONObject();
        jBody
                .put("phone", binding.phoneInput.getText().toString())
                .put("pwd", binding.passwordInput.getText().toString());

        final Observable<String> apiResult1 = PPRetrofit.getInstance().api("user.login", jBody.getJSONObject());

        apiResult1
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        PPWarn ppWarn = PPHelper.ppWarning(s);
                        if (ppWarn != null) {
                            throw new Exception(ppWarn.msg);
                        }

                        signInOk(s);

                        return PPRetrofit.getInstance().api("user.startup", null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<String>() {
                            public void accept(String s) {
                                Log.v("ppLog", "get result:" + s);
                                jobProcessing.onNext(false);

                                PPWarn ppWarn = PPHelper.ppWarning(s);
                                if (ppWarn != null) {
                                    PPHelper.showPPToast(activityContext, ppWarn.msg, Toast.LENGTH_SHORT);

                                    return;
                                }

                                startUpOk(s);
                            }
                        },
                        new Consumer<Throwable>() {
                            public void accept(Throwable t1) {
                                jobProcessing.onNext(false);

                                PPHelper.showPPToast(activityContext, t1.getMessage(), Toast.LENGTH_SHORT);
                                t1.printStackTrace();
                            }
                        }
                );
    }


    private void signInOk(String s) throws Exception {
       PPHelper.signInOk(this, s, false);
    }

    private void startUpOk(String s) {
        PPHelper.startUpOk(this, s);

        Intent intent1 = new Intent(this, TabsActivity.class);
        startActivity(intent1);
    }

    private void goForgetPassword() {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }

    private void goSignUp() {
        Intent intent = new Intent(this, SignUp1Activity.class);
        startActivity(intent);
    }
}

