package vn.huynh.whatsapp.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.google.firebase.FirebaseApp;
import com.rilixtech.CountryCodePicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.home.HomeActivity;
import vn.huynh.whatsapp.login.LoginContract;
import vn.huynh.whatsapp.login.presenter.LoginPresenter;
import vn.huynh.whatsapp.utils.AppUtils;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.LogManagerUtils;
import vn.huynh.whatsapp.utils.ViewUtils;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    @BindView(R.id.edt_phone_number)
    EditText edtPhoneNumber;
    @BindView(R.id.edt_code)
    EditText edtCode;
    @BindView(R.id.edt_name)
    EditText edtName;
    @BindView(R.id.btn_verify)
    Button btnVerify;
    @BindView(R.id.loader_sending_code)
    CircularDotsLoader circularDotsLoader;
    @BindView(R.id.ll_loading)
    LinearLayout llLoading;
    @BindView(R.id.ccp)
    CountryCodePicker countryCodePicker;
    @BindView(R.id.tv_resend)
    TextView tvResendCode;
    @BindView(R.id.tv_login_register)
    TextView tvLoginOrRegister;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_test_account)
    ImageView ivTestAccount;
    @BindView(R.id.til_name)
    TextInputLayout textInputLayoutName;

    private static final String TAG = LoginActivity.class.getSimpleName();

    private String mUserName = "";
    private String mPhoneNumber = "";
    private String mVerifyCode = "";
    private boolean mIsLogin = true;

    private LoginPresenter mLoginPresenter;
    private CountDownTimer mCountDownTimer;
    private boolean mUpdateVerifyButton = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initData();
        setEvents();
    }

    @Override
    public void initData() {
        FirebaseApp.initializeApp(this);
        mLoginPresenter = new LoginPresenter();
        mLoginPresenter.attachView(this);
        mLoginPresenter.setLogin(mIsLogin);
        mLoginPresenter.checkLogin();
        AppUtils.hideKeyBoard(getApplicationContext(), edtPhoneNumber);
        mCountDownTimer = new CountDownTimer(Constant.TIMEOUT_VERIFY_SMS * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (mUpdateVerifyButton) {
                    long second = millisUntilFinished / 1000;
                    btnVerify.setText(getResources().getString(R.string.label_verify, second));
                }
            }

            public void onFinish() {
                timeOut("");
            }
        };
    }

    private void setEventButtonVerify() {
        btnVerify.setEnabled(true);
        btnVerify.setText(getResources().getString(R.string.label_send_code));
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtPhoneNumber.setError(null);
                if (mIsLogin) {
                    //login
                    if (checkInput(false, mIsLogin)) {
                        btnVerify.setEnabled(false);
                        toggleInputViews(false);
                        btnVerify.setText("");
                        circularDotsLoader.setVisibility(View.VISIBLE);
                        mLoginPresenter.sendVerificationCode(LoginActivity.this, mPhoneNumber, "");
                    } else {
                        Toast.makeText(getApplicationContext(), "Please provide phone number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //register
                    if (checkInput(false, mIsLogin)) {
                        btnVerify.setEnabled(false);
                        toggleInputViews(false);
                        btnVerify.setText("");
                        circularDotsLoader.setVisibility(View.VISIBLE);
                        mLoginPresenter.sendVerificationCode(LoginActivity.this, mPhoneNumber, mUserName);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please provide phone number and mUserName", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void setEvents() {
        setEventButtonVerify();
        tvLoginOrRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                }
                ViewUtils.collapseWidth(edtCode);
                setEventButtonVerify();
                if (mIsLogin) {
                    //switch to register
                    mIsLogin = false;
                    mLoginPresenter.setLogin(mIsLogin);
                    tvTitle.setText(getResources().getString(R.string.label_register_account));
                    tvLoginOrRegister.setText(getResources().getString(R.string.label_tap_to_login));
                    ViewUtils.expand(textInputLayoutName);
                } else {
                    //switch to login
                    mIsLogin = true;
                    mLoginPresenter.setLogin(mIsLogin);
                    tvTitle.setText(getResources().getString(R.string.label_log_in));
                    tvLoginOrRegister.setText(getResources().getString(R.string.label_tap_to_register));
                    ViewUtils.collapse(textInputLayoutName);
                }
            }
        });
        ivTestAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: show test account dialog
            }
        });
    }

    @Override
    public void resetData() {

    }

    @Override
    public void showLoadingIndicator() {

    }

    @Override
    public void hideLoadingIndicator() {

    }

    @Override
    public void showEmptyDataIndicator() {

    }

    @Override
    public void showErrorIndicator() {

    }

    @Override
    public void showErrorMessage(String message) {

    }

    @Override
    public void showVerifyButton() {
        btnVerify.setEnabled(true);
        btnVerify.setText(getResources().getString(R.string.label_verify));
        if (mCountDownTimer != null)
            mCountDownTimer.start();

        circularDotsLoader.setVisibility(View.GONE);
        ViewUtils.expandWidth(edtCode);
        edtCode.setEnabled(true);
        edtCode.requestFocus();
        AppUtils.showKeyBoard(getApplicationContext(), edtCode);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput(true, mIsLogin)) {
                    btnVerify.setEnabled(false);
                    toggleInputViews(false);
                    mUpdateVerifyButton = false;
                    btnVerify.setText("");
                    btnVerify.setEnabled(false);
                    circularDotsLoader.setVisibility(View.VISIBLE);
                    if (mIsLogin) {
                        mLoginPresenter.doLogin(LoginActivity.this, mPhoneNumber, mVerifyCode);
                    } else {
                        mLoginPresenter.doRegister(LoginActivity.this, mUserName, mPhoneNumber, mVerifyCode);
                    }
                } else {
                    edtCode.setEnabled(true);
                    edtCode.requestFocus();
                    Toast.makeText(getApplicationContext(), "Please provide verify mVerifyCode", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void userExistDoLogin() {
        toggleInputViews(true);
        tvLoginOrRegister.performClick();
        edtPhoneNumber.setError(getResources().getString(R.string.error_phone_number_exist_please_login));
        circularDotsLoader.setVisibility(View.GONE);
        btnVerify.setEnabled(true);
        btnVerify.setText(getResources().getString(R.string.label_send_code));
    }

    @Override
    public void userNotExistDoRegister() {
        toggleInputViews(true);
        tvLoginOrRegister.performClick();
        edtPhoneNumber.setError(getResources().getString(R.string.error_phone_number_not_exist_please_register));
        circularDotsLoader.setVisibility(View.GONE);
        btnVerify.setEnabled(true);
        btnVerify.setText(getResources().getString(R.string.label_send_code));
    }

    @Override
    public void noLoggedInMoveToLogin() {
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        mIsLogin = false;
        mLoginPresenter.setLogin(mIsLogin);
        llLoading.setVisibility(View.GONE);
    }

    @Override
    public void loggedInMoveToHome() {
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        if (mIsLogin) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_login_successful), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_register_successful), Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void timeOut(String message) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_timeout_please_try_again), Toast.LENGTH_LONG).show();
        LogManagerUtils.d(TAG, "timeOut: " + message);
        toggleInputViews(true);
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();

        edtCode.setText("");
        setEvents();
        btnVerify.setEnabled(true);
        btnVerify.setText(getResources().getString(R.string.label_time_out_resend_code));
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPhone = mPhoneNumber;
                if (checkInput(false, mIsLogin)) {
                    if (oldPhone.equals(mPhoneNumber)) {
                        //same phone number
                        mLoginPresenter.resendVerificationCode(LoginActivity.this, mPhoneNumber);
                    } else {
                        //new phone number
                        mLoginPresenter.sendVerificationCode(LoginActivity.this, mPhoneNumber, mUserName);
                    }
                } else {
                    if (mIsLogin) {
                        Toast.makeText(getApplicationContext(), "Please provide phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please provide phone number and mUserName", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        circularDotsLoader.setVisibility(View.GONE);
        edtPhoneNumber.requestFocus();
        ViewUtils.collapseWidth(edtCode);
    }

    @Override
    public void loginFail(String message) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_login_failed_please_try_again), Toast.LENGTH_LONG).show();
        LogManagerUtils.d(TAG, "loginFail: " + message);
        toggleInputViews(true);
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();

        setEvents();
        btnVerify.setEnabled(true);
        btnVerify.setText(getResources().getString(R.string.label_send_code));
        circularDotsLoader.setVisibility(View.GONE);
        edtPhoneNumber.requestFocus();
        ViewUtils.collapseWidth(edtCode);
    }

    @Override
    public void invalidCode(String message) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_invalid_verify_code_please_try_again), Toast.LENGTH_LONG).show();
        LogManagerUtils.d(TAG, "invalidCode :" + message);
        mUpdateVerifyButton = true;
        edtCode.setEnabled(true);

        btnVerify.setEnabled(true);
        circularDotsLoader.setVisibility(View.GONE);
        edtCode.requestFocus();
    }

    @Override
    public void verifyFail(String message) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_verify_failed_please_try_again), Toast.LENGTH_LONG).show();
        LogManagerUtils.d(TAG, "verifyFail :" + message);
        toggleInputViews(true);

        if (mCountDownTimer != null)
            mCountDownTimer.cancel();

        btnVerify.setEnabled(true);
        btnVerify.setText(getResources().getString(R.string.label_send_code));
        circularDotsLoader.setVisibility(View.GONE);
        edtPhoneNumber.requestFocus();
        ViewUtils.collapseWidth(edtCode);
    }

    private boolean checkInput(boolean checkCode, boolean isLogin) {
        mUserName = edtName.getText().toString().trim();
        mPhoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() + edtPhoneNumber.getText().toString().trim();
        mPhoneNumber = ChatUtils.formatPhone(mPhoneNumber, getApplicationContext());
        mVerifyCode = edtCode.getText().toString().trim();

        if (checkCode)
            return !(edtPhoneNumber.getText().toString().trim().isEmpty() || (!isLogin && edtName.getText().toString().trim().isEmpty())
                    || edtCode.getText().toString().trim().isEmpty());
        else
            return !(edtPhoneNumber.getText().toString().trim().isEmpty() || (!isLogin && edtName.getText().toString().trim().isEmpty()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        mLoginPresenter.detachView();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void toggleInputViews(boolean enable) {
        if (enable) {
            tvLoginOrRegister.setClickable(true);
            tvLoginOrRegister.setAlpha(1f);
            edtCode.setEnabled(true);
            edtPhoneNumber.setEnabled(true);
            edtName.setEnabled(true);
            btnVerify.setEnabled(true);
            countryCodePicker.setClickable(true);
            countryCodePicker.setAlpha(1f);
        } else {
            tvLoginOrRegister.setClickable(false);
            tvLoginOrRegister.setAlpha(0.4f);
            edtCode.setEnabled(false);
            edtPhoneNumber.setEnabled(false);
            edtName.setEnabled(false);
            countryCodePicker.setClickable(false);
            countryCodePicker.setAlpha(0.4f);
        }
    }
}
