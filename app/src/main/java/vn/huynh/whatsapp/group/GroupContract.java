package vn.huynh.whatsapp.group;

import java.util.List;

import vn.huynh.whatsapp.chat_list.ChatListContract;
import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 4/7/2019.
 */

public interface GroupContract {
    interface View extends ChatListContract.View {
        void showListContact(User userObject);

        void openChat(String key);
    }

    interface Presenter extends ChatListContract.Presenter {
        void createGroupChat(String groupName, List<User> list);
    }
}
