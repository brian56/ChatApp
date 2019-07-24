package vn.huynh.whatsapp.model;

import android.content.Context;

import com.google.android.gms.tasks.Task;

import java.util.List;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 4/15/2019.
 */

public interface UserInterface extends BaseModelInterface {

    void isLoggedIn(CheckLoginCallback callback);

    void loadContact(Context context, List<User> contacts, LoadContactCallback callback);

    Task getUserData(String userId);

    void getContactData(User user, LoadContactCallback callback);

    void getCurrentUserData(String userId, LoadContactCallback callback);

    void checkPhoneNumberExist(String phoneNumber, CheckPhoneNumberExistCallback callback);

    void createUser(String userId, String phoneNumber, String name, CreateUserCallback callback);

    void updateUser(String userId, String phoneNumber, String name);

    void searchFriend(String phoneNumber, SearchFriendCallback callback);

    void listenerForUserFriendNotification(FriendCallback callback);

    interface LoadContactCallback {
        void loadSuccess(User user);

        void loadFail(String message);
    }

    interface CreateUserCallback {
        void createSuccess();

        void createFail(String error);
    }

    interface CheckLoginCallback {
        void alreadyLoggedIn(User user);

        void noLoggedIn();
    }

    interface CheckPhoneNumberExistCallback {
        void exist();

        void notExist();
    }

    interface SearchFriendCallback {
        void onSearchSuccess(List<User> userList);

        void onSearchFail(String error);
    }

    interface FriendCallback {
        void onFriendNotification(int showNotify);
    }
}
