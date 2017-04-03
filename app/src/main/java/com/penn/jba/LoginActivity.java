package com.penn.jba;

import android.annotation.TargetApi;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.penn.jba.databinding.ActivityLoginBinding;

import static com.penn.jba.util.PPHelper.isPasswordValid;
import static com.penn.jba.util.PPHelper.isPhoneValid;

public class LoginActivity extends AppCompatActivity implements TextWatcher {
    private UserLoginTask mAuthTask = null;

    private ActivityLoginBinding binding;

    private boolean jobProcess = false;

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

        binding.phoneInput.addTextChangedListener(this);

        binding.passwordInput.addTextChangedListener(this);

        setOperationEnableState();
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
    protected void onDestroy() {
        if (mAuthTask != null) {
            mAuthTask.cancel(true);
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
    public void signIn() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        binding.phoneInput.setError(null);
        binding.passwordInput.setError(null);

        // Store values at the time of the login attempt.
        String phone = binding.phoneInput.getText().toString();
        String password = binding.passwordInput.getText().toString();

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
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            binding.passwordInput.setError(getString(R.string.error_invalid_password));
            focusView = (focusView == null ? binding.passwordInput : focusView);
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
            mAuthTask = new UserLoginTask(phone, password);
            mAuthTask.execute((Void) null);
        }
    }

    public void goForgetPassword() {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }

    public void goSignUp() {
        Intent intent = new Intent(this, SignUp1Activity.class);
        startActivity(intent);
    }

    //-----helper-----
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String phone;
        private final String password;

        UserLoginTask(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //finish();
            } else {
                binding.passwordInput.setError(getString(R.string.error_incorrect_password));
                binding.passwordInput.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    //pptodo TargetApi有何用
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        jobProcess = (show ? true : false);
        setOperationEnableState();
        binding.loginProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    //设置可操作控件可用状态
    private void setOperationEnableState() {
        setSignInButtonEnableState();
        setGoForgetPasswordButtonEnableState();
        setGoSignUpButtonEnableState();
    }

    private void setSignInButtonEnableState() {
        boolean enable = true;

        if (jobProcess) {
            enable = false;
        } else if (TextUtils.isEmpty(binding.phoneInput.getText())) {
            enable = false;
        } else if (TextUtils.isEmpty(binding.passwordInput.getText())) {
            enable = false;
        }

        binding.signInButton.setEnabled(enable);
    }

    private void setGoForgetPasswordButtonEnableState() {
        boolean enable = true;

        if (jobProcess) {
            enable = false;
        }

        binding.forgetPasswordButton.setEnabled(enable);
    }

    private void setGoSignUpButtonEnableState() {
        boolean enable = true;

        if (jobProcess) {
            enable = false;
        }

        binding.createNewAccountButton.setEnabled(enable);
    }
}

