package com.penn.jba;

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

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.penn.jba.databinding.ActivityLoginBinding;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPJSONObject;
import com.penn.jba.util.PPRetrofit;
import com.penn.jba.util.PPWarn;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    private boolean jobProcess = false;

    private ArrayList<Disposable> disposableList = new ArrayList<Disposable>();

    private BehaviorSubject<Boolean> jobProcessing = BehaviorSubject.<Boolean>create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("needAutoLogin", false)) {
            Intent intent1 = new Intent(this, TabsActivity.class);
            startActivity(intent1);
        } else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        jobProcessing.onNext(false);

        Observable<String> phoneInputObervable = RxTextView.textChanges(binding.phoneInput)
                .skip(1)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        String error = "";
                        if (TextUtils.isEmpty(charSequence)) {
                            error = getString(R.string.error_field_required);
                        } else if (!Pattern.matches("\\d{11}", charSequence.toString())) {
                            error = getString(R.string.error_invalid_phone);
                        }
                        return error;
                    }
                }).doOnNext(
                        new Consumer<String>() {
                            @Override
                            public void accept(String error) throws Exception {
                                binding.phoneInputLayout.setError(TextUtils.isEmpty(error) ? null : error);
                            }
                        }
                );

        Observable<String> passwordInputObervable = RxTextView.textChanges(binding.passwordInput)
                .skip(1)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        String error = "";
                        if (TextUtils.isEmpty(charSequence)) {
                            error = getString(R.string.error_field_required);
                        } else if (!Pattern.matches("\\w{6,12}", charSequence.toString())) {
                            error = getString(R.string.error_invalid_password);
                        }
                        return error;
                    }
                }).doOnNext(
                        new Consumer<String>() {
                            @Override
                            public void accept(String error) throws Exception {
                                binding.passwordInputLayout.setError(TextUtils.isEmpty(error) ? null : error);
                            }
                        }
                );
        ;

        Observable<Object> signInButtonObervable = RxView.clicks(binding.signInButton)
                .debounce(200, TimeUnit.MILLISECONDS);

        disposableList.add(Observable.combineLatest(
                phoneInputObervable,
                passwordInputObervable,
                jobProcessing,
                new Function3<String, String, Boolean, Boolean>() {
                    @Override
                    public Boolean apply(String s, String s2, Boolean aBoolean) throws Exception {
                        Log.v("ppLog", "test:" + (TextUtils.isEmpty(s) && TextUtils.isEmpty(s2) && !aBoolean));
                        return TextUtils.isEmpty(s) && TextUtils.isEmpty(s2) && !aBoolean;
                    }
                }
                )
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                binding.signInButton.setEnabled(aBoolean);
                            }
                        })
        );

        disposableList.add(signInButtonObervable
                .subscribe(
                        new Consumer<Object>() {
                            public void accept(Object o) {
                                signIn();
                            }
                        }
                )
        );

        disposableList.add(jobProcessing
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

        final Observable<String> loginResult = PPRetrofit.getInstance().api("user.login", jBody.getJSONObject());
        loginResult.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<String>() {
                            public void accept(String s) {
                                Log.v("ppLog", "get result:" + s);
                                jobProcessing.onNext(false);

                                PPWarn ppWarn = PPHelper.ppWarning(s);
                                if (ppWarn != null) {
                                    Toast.makeText(LoginActivity.this, ppWarn.msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Consumer<Throwable>() {
                            public void accept(Throwable t1) {
                                jobProcessing.onNext(false);

                                Toast.makeText(LoginActivity.this, t1.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.v("ppLog", "error:" + t1.toString());
                                t1.printStackTrace();
                            }
                        }
                );
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Disposable d : disposableList) {
            if (!d.isDisposed()) {
                d.dispose();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //-----UI event handler-----
    public void goForgetPassword() {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }

    public void goSignUp() {
        Intent intent = new Intent(this, SignUp1Activity.class);
        startActivity(intent);
    }

    //-----helper-----
}

