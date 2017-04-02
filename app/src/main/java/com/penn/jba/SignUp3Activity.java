package com.penn.jba;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class SignUp3Activity extends AppCompatActivity {
    private UserSignUpTask mAuthTask = null;

    private EditText mPassword;
    private EditText mNickname;

    private View mProgressView;
    private Button mSignUpButton;

    private TextView mSexError;

    private static EditText mBirthday;

    private CheckBox mAgreeCheck;

    private String sex = "none";
    private Boolean agree = false;

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date - 18years as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR) - 18;
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog dialogDatePicker = new DatePickerDialog(getActivity(), R.style.CustomDatePickerDialogTheme, this, year, month, day);
            return dialogDatePicker;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.MONTH, month);
            String dateString = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            mBirthday.setText(dateString);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up3);

        mPassword = (EditText) findViewById(R.id.password);

        mNickname = (EditText) findViewById(R.id.nickname);

        mBirthday = (EditText) findViewById(R.id.birthday);

        mBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBirthday.setError(null);
                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                } else {
                }
            }
        });

        mAgreeCheck = (CheckBox) findViewById(R.id.agree_check);

        mProgressView = findViewById(R.id.sign_up_progress);
        mSignUpButton = (Button) findViewById(R.id.sign_up_button);

        mSexError = (TextView) findViewById(R.id.sex_error);
    }

    public void chooseMale(View v) {
        mSexError.setError(null);
        sex = "male";
    }

    public void chooseFemale(View v) {
        mSexError.setError(null);
        sex = "female";
    }

    public void signUp(View v) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPassword.setError(null);
        mNickname.setError(null);
        mBirthday.setError(null);
        mSexError.setError(null);
        mAgreeCheck.setError(null);

        // Store values at the time of the sign up attempt.
        String phone = "";
        String verifyCode = "";

        String password = mPassword.getText().toString();
        String nickname = mNickname.getText().toString();
        String birthday = mBirthday.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        // Check for a valid nickname.
        if (TextUtils.isEmpty(nickname)) {
            mNickname.setError(getString(R.string.error_field_required));
            focusView = mNickname;
            cancel = true;
        }

        if (TextUtils.isEmpty(birthday)) {
            mBirthday.setError(getString(R.string.error_field_required));
            focusView = mBirthday;
            cancel = true;
        } else if (!isValidDate(birthday)){
            mBirthday.setError(getString(R.string.error_date_format));
            focusView = mBirthday;
            cancel = true;
        }

        if (sex == "none") {
            mSexError.setError(getString(R.string.error_field_required));
            focusView = mSexError;
            cancel = true;
        }

        if (!agree) {
            mAgreeCheck.setError(getString(R.string.please_agree));
            focusView = mAgreeCheck;
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
            mAuthTask = new UserSignUpTask(phone, verifyCode, password, nickname, birthday, sex);
            mAuthTask.execute((Void) null);
        }
    }

    public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {

        private final String phone;
        private final String verifyCode;
        private final String password;
        private final String nickname;
        private final String birthday;
        private final String sex;

        UserSignUpTask(String phone, String verifyCode, String password, String nickname, String birthday, String sex) {
            this.phone = phone;
            this.verifyCode = verifyCode;
            this.password = password;
            this.nickname = nickname;
            this.birthday = birthday;
            this.sex = sex;
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
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //finish();
            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mSignUpButton.setEnabled(show ? false : true);
    }

    public void clickAgree(View v) {
        mAgreeCheck.setError(null);
        agree = mAgreeCheck.isChecked();
        Log.v("ppLog", "" + agree);
    }

    private boolean isPasswordValid(String password) {
        Log.v("ppLog", password + "," + password.length());
        return password.length() >= 6;
    }

    private boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }
}
