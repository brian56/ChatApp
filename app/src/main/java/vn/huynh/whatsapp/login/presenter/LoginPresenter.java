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

    private LoginContract.View view;
    private UserRepository userRepo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private boolean isLogin = false;

    public LoginPresenter() {
        userRepo = new UserRepository();
    }

    public void setLogin(boolean login) {
        this.isLogin = login;
    }

    @Override
    public void attachView(BaseView view) {
        this.view = (LoginContract.View) view;
    }

    @Override
    public void detachView() {
        this.view = null;
        this.userRepo.removeListener();
    }

    @Override
    public void checkLogin() {
        userRepo.isLoggedIn(new UserInterface.CheckLoginCallBack() {
            @Override
            public void alreadyLoggedIn(User user) {
                if (user != null) {
                    ChatUtils.setUser(user);
                }
                if (view != null) {
                    view.loggedInMoveToHome();
                }
            }

            @Override
            public void noLoggedIn() {
                if (view != null) {
                    FirebaseAuth.getInstance().signOut();
                    OneSignal.setSubscription(false);
                    ChatUtils.clearUser();
                    view.noLoggedInMoveToLogin();
                }
            }
        });
    }

    @Override
    public void sendVerificationCode(final Activity activity, final String phoneNumber, final String name) {
        userRepo.checkPhoneNumberExist(phoneNumber, new UserInterface.CheckPhoneNumberExistCallBack() {
            @Override
            public void exist() {
                if (isLogin) {
                    //login
                    initBeforeLogin(activity, name);
                    startPhoneNumberVerification(activity, phoneNumber);
                } else {
                    //register
                    if (view != null)
                        view.userExistDoLogin();
                }
            }

            @Override
            public void notExist() {
                if (isLogin) {
                    //login
                    if (view != null)
                        view.userNotExistDoRegister();
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
        if (view != null)
            view.showLoadingIndicator();
        if (verificationId != null) {
            verifyPhoneNumberWithCode(activity, name, verificationId, input);
        } else {
            view.verifyFail("verification null");
        }
    }

    @Override
    public void doLogin(Activity activity, String phoneNumber, String input) {
        if (view != null)
            view.showLoadingIndicator();
        if (verificationId != null) {
            verifyPhoneNumberWithCode(activity, "", verificationId, input);
        } else {
            view.verifyFail("verification null");
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
                resendingToken);             // ForceResendingToken from callbacks
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
                if (view != null)
                    view.verifyFail(e.getMessage());
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                resendingToken = forceResendingToken;
                verificationId = s;
                if (view != null)
                    view.showVerifyButton();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                if (view != null)
                    view.timeOut(s);
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
                    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        userRepo.getCurrentUserData(firebaseUser.getUid(), new UserInterface.LoadContactCallBack() {
                            @Override
                            public void loadSuccess(User user) {
                                if (user != null) {
                                    ChatUtils.setUser(user);
                                    checkLogin();
                                } else {
                                    userRepo.createUser(firebaseUser.getUid(), firebaseUser.getPhoneNumber(), name, new UserInterface.CreateUserCallBack() {
                                        @Override
                                        public void createSuccess() {
                                            checkLogin();
                                        }

                                        @Override
                                        public void createFail(String error) {
                                            if (view != null) {
                                                view.loginFail(error);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void loadFail(String message) {
                                userRepo.createUser(firebaseUser.getUid(), firebaseUser.getPhoneNumber(), name, new UserInterface.CreateUserCallBack() {
                                    @Override
                                    public void createSuccess() {
                                        checkLogin();
                                    }

                                    @Override
                                    public void createFail(String error) {
                                        if (view != null) {
                                            view.loginFail(error);
                                        }
                                    }
                                });
                            }
                        });
                        /*userRepo.createUser(user.getUid(), user.getPhoneNumber(), name, new UserInterface.CreateUserCallBack() {
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
                        });*/
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                verificationId = null;
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.invalidCode(e.getMessage());
                }
            }
        });
    }

    private void verifyPhoneNumberWithCode(Activity activity, String name, String verificationId, String input) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, input);
        signInWithPhoneAuthCredential(credential, activity, name);
    }
}
