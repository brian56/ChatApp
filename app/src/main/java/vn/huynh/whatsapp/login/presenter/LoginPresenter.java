package vn.huynh.whatsapp.login.presenter;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.onesignal.OneSignal;

import java.util.concurrent.TimeUnit;

import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.login.LoginContract;
import vn.huynh.whatsapp.model.UserInterface;
import vn.huynh.whatsapp.model.UserRepository;

/**
 * Created by duong on 4/13/2019.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private LoginContract.View view;
    private UserInterface userInterface;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationId;

    public LoginPresenter() {
        userInterface = new UserRepository();
    }

    @Override
    public void attachView(BaseView view) {
        this.view = (LoginContract.View) view;
    }

    @Override
    public void detachView() {
        this.view = null;
        this.userInterface.removeListener();
    }

    @Override
    public void checkLogin() {
        userInterface.isLoggedIn(new UserInterface.CheckLoginCallBack() {
            @Override
            public void alreadyLoggedIn() {
                if(view != null) {
                    view.loggedInMoveToHome();
                }
            }

            @Override
            public void noLoggedIn() {
                if(view != null) {
                    FirebaseAuth.getInstance().signOut();
                    OneSignal.setSubscription(false);
                    view.noLoggedInMoveToLogin();
                }
            }
        });
    }

    @Override
    public void sendVerificationCode(Activity activity, String phoneNumber, String name) {
        initBeforeLogin(activity, name);
        startPhoneNumberVerification(activity, phoneNumber);
    }

    @Override
    public void doLogin(Activity activity, String name, String phoneNumber, String input) {
        if(view != null)
            view.showLoadingIndicator();
        if (verificationId != null) {
            verifyPhoneNumberWithCode(activity, name, verificationId, input);
        }
    }

    @Override
    public void startPhoneNumberVerification(Activity activity, String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                activity,
                mCallbacks
        );
    }

    @Override
    public void initBeforeLogin(final Activity activity, final String name) {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential, activity, name);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                verificationId = null;
                if(view != null)
                    view.verifyFail(e.getMessage());
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId = s;
                if(view != null)
                    view.showVerifyButton();
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, Activity activity, final String name) {
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (view != null) {
                    view.hideLoadingIndicator();
                }
                if (task.isSuccessful()) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        userInterface.createUser(user.getUid(), user.getPhoneNumber(), name, new UserInterface.CreateUserCallBack() {
                            @Override
                            public void createSuccess() {
                                checkLogin();
                            }

                            @Override
                            public void createFail(String error) {
                                if(view != null) {
                                    view.loginFail(error);
                                }
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                verificationId = null;
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.loginFail(e.getMessage());
                }
            }
        });
    }

    private void verifyPhoneNumberWithCode(Activity activity, String name, String verificationId, String input) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, input);
        signInWithPhoneAuthCredential(credential, activity, name);
    }
}
