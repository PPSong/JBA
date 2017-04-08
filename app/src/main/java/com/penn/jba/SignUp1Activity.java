package com.penn.jba;

import android.annotation.TargetApi;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.penn.jba.databinding.ActivityForgetPasswordBinding;
import com.penn.jba.databinding.ActivityLoginBinding;
import com.penn.jba.databinding.ActivitySignUp1Binding;
import com.penn.jba.util.PPHelper;

import static android.R.attr.cursorVisible;
import static android.R.attr.password;
import static com.penn.jba.util.PPHelper.getLastVerifyCodeRequestTime;
import static com.penn.jba.util.PPHelper.isPasswordValid;
import static com.penn.jba.util.PPHelper.isPhoneValid;
import static com.penn.jba.util.PPHelper.isVerfifyCodeValid;

public class SignUp1Activity extends AppCompatActivity implements TextWatcher {
    private RequestVerifyCodeTask requestVerifyCodeTask;

    private ActivitySignUp1Binding binding;

    private boolean jobProcess = false;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up1);
        binding.setPresenter(this);

        //设置键盘返回键的快捷方式
        binding.phoneInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.request_verify_code || id == EditorInfo.IME_NULL) {
                    requestVerifyCode();
                    return true;
                }
                return false;
            }
        });

        binding.phoneInput.addTextChangedListener(this);

        setOperationEnableState();

        ct();
    }

    @Override
    protected void onDestroy() {
        Log.v("ppLog", "onDestroy");
        if (requestVerifyCodeTask != null) {
            requestVerifyCodeTask.cancel(true);
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        setOperationEnableState();
    }

    //-----UI event handler-----
    public void requestVerifyCode() {
//        if (requestVerifyCodeTask != null) {
//            return;
//        }
//
//        // Reset errors.
//        binding.phoneInput.setError(null);
//
//        // Store values at the time of the login attempt.
//        String phone = binding.phoneInput.getText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//
//        // Check for a valid phone.
//        if (TextUtils.isEmpty(phone)) {
//            binding.phoneInput.setError(getString(R.string.error_field_required));
//            focusView = (focusView == null ? binding.phoneInput : focusView);
//            cancel = true;
//        } else if (!isPhoneValid(phone)) {
//            binding.phoneInput.setError(getString(R.string.error_invalid_phone));
//            focusView = (focusView == null ? binding.phoneInput : focusView);
//            cancel = true;
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgress(true);
//            requestVerifyCodeTask = new RequestVerifyCodeTask(phone);
//            requestVerifyCodeTask.execute((Void) null);
//        }
    }

    //-----helper-----
    public class RequestVerifyCodeTask extends AsyncTask<Void, Void, Boolean> {

        private final String phone;

        RequestVerifyCodeTask(String phone) {
            this.phone = phone;
        }

        @Override
        protected void onPreExecute() {
            PPHelper.setLastVerifyCodeRequestTime(SignUp1Activity.this);
            setOperationEnableState();
            ct();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            requestVerifyCodeTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(SignUp1Activity.this, SignUp2Activity.class);
                Log.v("ppLog", "startActivity");
                startActivity(intent);
            } else {
                //处理错误信息
            }
        }

        @Override
        protected void onCancelled() {
            Log.v("ppLog", "RequestVerifyCodeTask onCancelled");
            requestVerifyCodeTask = null;
            showProgress(false);
        }
    }

    //pptodo TargetApi有何用
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        jobProcess = (show ? true : false);
        setOperationEnableState();
        binding.jobProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    //设置可操作控件可用状态
    private void setOperationEnableState() {
        setRequestVerifyCodeButtonEnableState();
    }

    private void setRequestVerifyCodeButtonEnableState() {
        boolean enable = true;

        if (System.currentTimeMillis() / 1000 - PPHelper.getLastVerifyCodeRequestTime(this) < PPHelper.REQUEST_VERIFY_CODE_INTERVAL) {
            enable = false;
        } else if (jobProcess) {
            enable = false;
        } else if (TextUtils.isEmpty(binding.phoneInput.getText())) {
            enable = false;
        }

        binding.goSignUp2Button.setEnabled(enable);
    }

    private void ct() {
        final int leftTime = (int) (PPHelper.REQUEST_VERIFY_CODE_INTERVAL - (System.currentTimeMillis() / 1000 - PPHelper.getLastVerifyCodeRequestTime(SignUp1Activity.this)));
        if (leftTime > 0) {
            countDownTimer = new CountDownTimer(leftTime * 1000 + 100, 1000) {
                int tmpLeftTime = leftTime;
                @Override
                public void onTick(long millis) {
                    tmpLeftTime--;
                    Log.v("ppLog", "leftTime:" + tmpLeftTime);
                    String text = String.format("%02d", tmpLeftTime);
                    binding.goSignUp2Button.setText(text);
                }

                @Override
                public void onFinish() {
                    Log.v("ppLog", "onFinish");
                    setOperationEnableState();
                    binding.goSignUp2Button.setText(R.string.request_verify_code);
                }
            }.start();
        }
    }
}
