package vn.huynh.whatsapp.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;
import com.google.firebase.FirebaseApp;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.home.HomeActivity;
import vn.huynh.whatsapp.login.LoginContract;
import vn.huynh.whatsapp.login.presenter.LoginPresenter;

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
    TashieLoader tashieLoader;
    @BindView(R.id.ll_loading)
    LinearLayout llLoading;

    private String name = "";
    private String phoneNumber = "";
    private String code = "";

    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initialize();
        setEvents();
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
    public void loggedInMoveToHome() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }

    @Override
    public void loginFail(String message) {
        Toast.makeText(getApplicationContext(), "Login failed: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void verifyFail(String message) {
        Toast.makeText(getApplicationContext(), "Verify failed: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showVerifyButton() {
        edtCode.setEnabled(true);
        edtCode.requestFocus();
        btnVerify.setText("Verify");
        tashieLoader.setVisibility(View.GONE);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput(true)) {
                    btnVerify.setText("");
                    tashieLoader.setVisibility(View.VISIBLE);
                    presenter.doLogin(LoginActivity.this, name, phoneNumber, code);
                }
            }
        });
    }

    private void initialize() {
        FirebaseApp.initializeApp(this);
        presenter = new LoginPresenter();
        presenter.attachView(this);
        presenter.checkLogin();
    }

    @Override
    public void noLoggedInMoveToLogin() {
        llLoading.setVisibility(View.GONE);
    }

    private void setEvents() {
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput(false)) {
                    btnVerify.setText("");
                    tashieLoader.setVisibility(View.VISIBLE);
                    presenter.sendVerificationCode(LoginActivity.this, phoneNumber, name);
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide phone number and name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkInput(boolean checkCode) {
        name = edtName.getText().toString().trim();
        phoneNumber = edtPhoneNumber.getText().toString().trim();
        code = edtCode.getText().toString().trim();
        if (checkCode)
            return !(edtPhoneNumber.getText().toString().trim().isEmpty() || edtName.getText().toString().trim().isEmpty()
                    || edtCode.getText().toString().trim().isEmpty());
        else
            return !(edtPhoneNumber.getText().toString().trim().isEmpty() || edtName.getText().toString().trim().isEmpty());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
