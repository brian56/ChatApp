package vn.huynh.whatsapp.contact;

import android.content.Context;

import java.util.List;

import vn.huynh.whatsapp.base.BasePresenter;
import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 4/12/2019.
 */

public interface ContactContract {
    interface View extends BaseView {
        void showListContact(User userObject);

        void showErrorMessage(String message);

        void openChat(String chatId);

    }

    interface Presenter extends BasePresenter {

        void loadListContact(Context context);

        void loadListContactForGroup(Context context);

        void createChat(boolean isGroup, String name, List<User> users);

        void checkSingleChatExist(boolean isGroup, String name, List<User> users);

    }
}
