package vn.huynh.whatsapp.contact_friend.friend;

import java.util.ArrayList;
import java.util.List;

import vn.huynh.whatsapp.base.BasePresenter;
import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.model.Friend;
import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 5/19/2019.
 */

public interface FriendContract {
    interface View extends BaseView {
        void showFriendList(Friend friend);

        void updateFriendStatus(Friend friend);

        void removeFriend(Friend friend);

        void showMessage(String message);

        void setTotalFriend(long totalFriend);

        void openChat(String chatId);

        void showHideFriendNotification(int friendNotification);

    }

    interface ViewSearchFriend extends BaseView {
        void showFriendList(List<User> userArrayList);

        void addFriendSuccess(String message);

        void addFriendFail(String message);
    }

    interface Presenter extends BasePresenter {
        void removeListener();

        void detachViewFriend();

        void detachViewFriendSearch();

        void listenerFriendNotification();

//        void updateFriendNotification(int friendNotification);

        void loadListFriend(int friendStatus);

        void sendInvite(User user, String message);

        void createFriendRequest(User user);

        void acceptFriendRequest(Friend friend);

        void cancelFriendRequest(Friend friend);

        void rejectFriendRequest(Friend friend);

        void blockFriendRequest(Friend friend);

        void unBlockFriendRequest(Friend friend);

        void removeFriend(Friend friend);

        void createChat(boolean isGroup, String name, List<User> users);

        void checkSingleChatExist(boolean isGroup, String name, List<User> users);

        void searchFriendByPhoneNumber(String phoneNumber);

        void addFriends(ArrayList<User> userArrayList);
    }
}
