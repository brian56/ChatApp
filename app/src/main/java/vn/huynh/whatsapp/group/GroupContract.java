package vn.huynh.whatsapp.group;

import java.util.List;

import vn.huynh.whatsapp.base.BasePresenter;
import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 4/7/2019.
 */

public interface GroupContract {
    interface View extends BaseView {
        void showListGroup(int position);

        void updateListGroupStatus(Chat chatObject);

        void showListGroupEmpty();

        void showListContact(User userObject);

        void showErrorMessage(String message);

        void openChat(String key);

    }

    interface Presenter extends BasePresenter {
        void loadListGroup(List<Chat> list);

        void createGroupChat(String groupName, List<User> list);

    }
}
