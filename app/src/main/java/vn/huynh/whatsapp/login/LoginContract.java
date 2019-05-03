package vn.huynh.whatsapp.login;

import android.app.Activity;

import vn.huynh.whatsapp.base.BasePresenter;
import vn.huynh.whatsapp.base.BaseView;

/**
 * Created by duong on 4/13/2019.
 */

public interface LoginContract {
    interface View extends BaseView {

        void loggedInMoveToHome();

        void noLoggedInMoveToLogin();

        void userExistDoLogin();

        void userNotExistDoRegister();

        void loginFail(String message);

        void verifyFail(String message);

        void invalidCode(String message);

        void timeOut(String message);

        void showVerifyButton();
    }

    interface Presenter extends BasePresenter {

        void checkLogin();

        void initBeforeLogin(Activity activity, String name);

        void startPhoneNumberVerification(Activity activity, String input);

        void doLogin(Activity activity, String phoneNumber, String input);

        void doRegister(Activity activity, String name, String phoneNumber, String input);

        void sendVerificationCode(Activity activity, String phoneNumber, String name);

        void resendVerificationCode(Activity activity, String phoneNumber);

    }
}
