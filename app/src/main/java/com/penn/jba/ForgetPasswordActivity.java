package com.penn.jba;

import android.annotation.TargetApi;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.Toast;

import com.penn.jba.databinding.ActivityForgetPasswordBinding;

import static com.penn.jba.util.PPHelper.isPasswordValid;
import static com.penn.jba.util.PPHelper.isPhoneValid;
import static com.penn.jba.util.PPHelper.isVerfifyCodeValid;

public class ForgetPasswordActivity extends AppCompatActivity implements TextWatcher {
    private ResetPasswordTask resetPasswordTask;
    private RequestVerifyCodeTask requestVerifyCodeTask;
    private ActivityForgetPasswordBinding binding;

    private boolean jobProcess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password);
        binding.setPresenter(this);

        //设置键盘返回键的快捷方式
        binding.newPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.reset_password || id == EditorInfo.IME_NULL) {
                    resetPassword();
                    return true;
                }
                return false;
            }
        });

        binding.phoneInput.addTextChangedListener(this);
        binding.verifyCodeInput.addTextChangedListener(this);
        binding.newPasswordInput.addTextChangedListener(this);

        setOperationEnableState();
    }

    @Override
    protected void onDestroy() {
        if (resetPasswordTask != null) {
            resetPasswordTask.cancel(true);
        }

        if (requestVerifyCodeTask != null) {
            requestVerifyCodeTask.cancel(true);
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
        String phone;

        if (resetPasswordTask != null) {
            return;
        }

        // Reset errors.
        binding.phoneInput.setError(null);

        // Store values at the time of the login attempt.
        phone = binding.phoneInput.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid phone.
        if (TextUtils.isEmpty(phone)) {
            binding.phoneInput.setError(getString(R.string.error_field_required));
            focusView = (focusView == null ? binding.phoneInput : focusView);
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            binding.phoneInput.setError(getString(R.string.error_invalid_phone));
            focusView = (focusView == null ? binding.phoneInput : focusView);
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            requestVerifyCodeTask = new RequestVerifyCodeTask(phone);
            requestVerifyCodeTask.execute((Void) null);
        }
    }

    public void resetPassword() {
        String phone, verifyCode, newPassword;

        if (resetPasswordTask != null) {
            return;
        }

        // Reset errors.
        binding.phoneInput.setError(null);
        binding.verifyCodeInput.setError(null);
        binding.newPasswordInput.setError(null);

        // Store values at the time of the login attempt.
        phone = binding.phoneInput.getText().toString();
        verifyCode = binding.verifyCodeInput.getText().toString();
        newPassword = binding.newPasswordInput.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid phone.
        if (TextUtils.isEmpty(phone)) {
            binding.phoneInput.setError(getString(R.string.error_field_required));
            focusView = (focusView == null ? binding.phoneInput : focusView);
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            binding.phoneInput.setError(getString(R.string.error_invalid_phone));
            focusView = (focusView == null ? binding.phoneInput : focusView);
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(verifyCode) || !isVerfifyCodeValid(verifyCode)) {
            binding.verifyCodeInput.setError(getString(R.string.error_invalid_verify_code));
            focusView = (focusView == null ? binding.verifyCodeInput : focusView);
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(newPassword) || !isPasswordValid(newPassword)) {
            binding.newPasswordInput.setError(getString(R.string.error_invalid_password));
            focusView = (focusView == null ? binding.newPasswordInput : focusView);
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            resetPasswordTask = new ResetPasswordTask(phone, verifyCode, newPassword);
            resetPasswordTask.execute((Void) null);
        }
    }

    //-----helper-----
    class ResetPasswordTask extends AsyncTask<Void, Void, Boolean> {
        String phone, verifyCode, newPassword;

        ResetPasswordTask(String phone, String verifyCode, String newPassword) {
            this.phone = phone;
            this.verifyCode = verifyCode;
            this.newPassword = newPassword;
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
            resetPasswordTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(ForgetPasswordActivity.this, R.string.reset_password_ok, Toast.LENGTH_LONG).show();
                finish();
            } else {
                //deal with error message
            }
        }

        @Override
        protected void onCancelled() {
            resetPasswordTask = null;
            showProgress(false);
        }
    }

    class RequestVerifyCodeTask extends AsyncTask<Void, Void, Boolean> {
        private String phone;

        public RequestVerifyCodeTask(String phone) {
            this.phone = phone;
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
//                Toast.makeText(ForgetPasswordActivity.this, R.string.reset_password_ok, Toast.LENGTH_LONG).show();
//                finish();
            } else {
                //deal with error message
            }
        }

        @Override
        protected void onCancelled() {
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
        setResetPasswordButtonEnableState();
        setRequestVerifyCodeButtonEnableState();
    }

    private void setResetPasswordButtonEnableState() {
        boolean enable = true;

        if (jobProcess) {
            enable = false;
        } else if (TextUtils.isEmpty(binding.phoneInput.getText())) {
            enable = false;
        } else if (TextUtils.isEmpty(binding.verifyCodeInput.getText())) {
            enable = false;
        } else if (TextUtils.isEmpty(binding.newPasswordInput.getText())) {
            enable = false;
        }

        binding.resetPasswordButton.setEnabled(enable);
    }

    private void setRequestVerifyCodeButtonEnableState() {
        boolean enable = true;

        if (jobProcess) {
            enable = false;
        } else if (TextUtils.isEmpty(binding.phoneInput.getText())) {
            enable = false;
        }

        binding.requestVerifyCodeButton.setEnabled(enable);
    }
}
