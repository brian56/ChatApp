package vn.huynh.whatsapp.model;

import android.content.Context;

import com.google.android.gms.tasks.Task;

import java.util.List;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 4/15/2019.
 */

public interface UserInterface extends BaseModelInterface {

    void isLoggedIn(CheckLoginCallBack callBack);

    void loadContact(Context context, List<User> contacts, LoadContactCallBack callBack);

    Task getUserData(String userId);

    void getContactData(User user, LoadContactCallBack callBack);

    void getCurrentUserData(String userId, LoadContactCallBack callBack);

    void checkPhoneNumberExist(String phoneNumber, CheckPhoneNumberExistCallBack callBack);

    void createUser(String userId, String phoneNumber, String name, CreateUserCallBack callBack);

    void updateUser(String userId, String phoneNumber, String name);

    interface LoadContactCallBack {
        void loadSuccess(User user);

        void loadFail(String message);
    }

    interface CreateUserCallBack {
        void createSuccess();

        void createFail(String error);
    }

    interface CheckLoginCallBack {
        void alreadyLoggedIn();

        void noLoggedIn();
    }

    interface CheckPhoneNumberExistCallBack {
        void exist();

        void notExist();
    }
}
