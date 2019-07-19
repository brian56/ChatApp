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
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.model.UserInterface;
import vn.huynh.whatsapp.model.UserRepository;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;

/**
 * Created by duong on 4/13/2019.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private static final String TAG = LoginPresenter.class.getSimpleName();

    private LoginContract.View mLoginView;
    private UserRepository mUserRepo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendingToken;
    private boolean mIsLogin = false;

    public LoginPresenter() {
        mUserRepo = new UserRepository();
    }

    public void setLogin(boolean login) {
        this.mIsLogin = login;
    }

    @Override
    public void attachView(BaseView view) {
        this.mLoginView = (LoginContract.View) view;
    }

    @Override
    public void detachView() {
        this.mLoginView = null;
        this.mUserRepo.removeListener();
    }

    @Override
    public void checkLogin() {
        mUserRepo.isLoggedIn(new UserInterface.CheckLoginCallBack() {
            @Override
            public void alreadyLoggedIn(User user) {
                if (user != null) {
                    ChatUtils.setUser(user);
                }
                if (mLoginView != null) {
                    mLoginView.loggedInMoveToHome();
                }
            }

            @Override
            public void noLoggedIn() {
                if (mLoginView != null) {
                    FirebaseAuth.getInstance().signOut();
                    OneSignal.setSubscription(false);
                    ChatUtils.clearUser();
                    mLoginView.noLoggedInMoveToLogin();
                }
            }
        });
    }

    @Override
    public void sendVerificationCode(final Activity activity, final String phoneNumber, final String name) {
        mUserRepo.checkPhoneNumberExist(phoneNumber, new UserInterface.CheckPhoneNumberExistCallBack() {
            @Override
            public void exist() {
                if (mIsLogin) {
                    //login
                    initBeforeLogin(activity, name);
                    startPhoneNumberVerification(activity, phoneNumber);
                } else {
                    //register
                    if (mLoginView != null)
                        mLoginView.userExistDoLogin();
                }
            }

            @Override
            public void notExist() {
                if (mIsLogin) {
                    //login
                    if (mLoginView != null)
                        mLoginView.userNotExistDoRegister();
                } else {
                    //register
                    initBeforeLogin(activity, name);
                    startPhoneNumberVerification(activity, phoneNumber);
                }
            }
        });
    }

    @Override
    public void resendVerificationCode(Activity activity, String phoneNumber) {
        resendVerificationCode(phoneNumber, activity);
    }

    @Override
    public void doRegister(Activity activity, String name, String phoneNumber, String input) {
        if (mLoginView != null)
            mLoginView.showLoadingIndicator();
        if (mVerificationId != null) {
            verifyPhoneNumberWithCode(activity, name, mVerificationId, input);
        } else {
            mLoginView.verifyFail("verification null");
        }
    }

    @Override
    public void doLogin(Activity activity, String phoneNumber, String input) {
        if (mLoginView != null)
            mLoginView.showLoadingIndicator();
        if (mVerificationId != null) {
            verifyPhoneNumberWithCode(activity, "", mVerificationId, input);
        } else {
            mLoginView.verifyFail("verification null");
        }
    }

    @Override
    public void startPhoneNumberVerification(Activity activity, String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                Constant.TIMEOUT_VERIFY_SMS,
                TimeUnit.SECONDS,
                activity,
                mCallbacks
        );
    }

    private void resendVerificationCode(String phoneNumber, Activity activity) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                Constant.TIMEOUT_VERIFY_SMS,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                activity,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                mResendingToken);             // ForceResendingToken from callbacks
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
                mVerificationId = null;
                if (mLoginView != null)
                    mLoginView.verifyFail(e.getMessage());
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mResendingToken = forceResendingToken;
                mVerificationId = s;
                if (mLoginView != null)
                    mLoginView.showVerifyButton();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                if (mLoginView != null)
                    mLoginView.timeOut(s);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, Activity activity, final String name) {
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (mLoginView != null) {
                    mLoginView.hideLoadingIndicator();
                }
                if (task.isSuccessful()) {
                    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        mUserRepo.getCurrentUserData(firebaseUser.getUid(), new UserInterface.LoadContactCallBack() {
                            @Override
                            public void loadSuccess(User user) {
                                if (user != null) {
                                    ChatUtils.setUser(user);
                                    checkLogin();
                                } else {
                                    mUserRepo.createUser(firebaseUser.getUid(), firebaseUser.getPhoneNumber(), name, new UserInterface.CreateUserCallBack() {
                                        @Override
                                        public void createSuccess() {
                                            checkLogin();
                                        }

                                        @Override
                                        public void createFail(String error) {
                                            if (mLoginView != null) {
                                                mLoginView.loginFail(error);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void loadFail(String message) {
                                mUserRepo.createUser(firebaseUser.getUid(), firebaseUser.getPhoneNumber(), name, new UserInterface.CreateUserCallBack() {
                                    @Override
                                    public void createSuccess() {
                                        checkLogin();
                                    }

                                    @Override
                                    public void createFail(String error) {
                                        if (mLoginView != null) {
                                            mLoginView.loginFail(error);
                                        }
                                    }
                                });
                            }
                        });
                        /*mUserRepo.createUser(user.getUid(), user.getPhoneNumber(), name, new UserInterface.CreateUserCallBack() {
                            @Override
                            public void createSuccess() {
                                checkLogin();
                            }

                            @Override
                            public void createFail(String error) {
                                if(mChatListview != null) {
                                    mChatListview.loginFail(error);
                                }
                            }
                        });*/
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                mVerificationId = null;
                if (mLoginView != null) {
                    mLoginView.hideLoadingIndicator();
                    mLoginView.invalidCode(e.getMessage());
                }
            }
        });
    }

    private void verifyPhoneNumberWithCode(Activity activity, String name, String verificationId, String input) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, input);
        signInWithPhoneAuthCredential(credential, activity, name);
    }
}
