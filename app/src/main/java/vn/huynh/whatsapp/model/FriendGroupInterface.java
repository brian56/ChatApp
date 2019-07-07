package vn.huynh.whatsapp.model;

import java.util.List;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 4/15/2019.
 */

public interface FriendGroupInterface extends BaseModelInterface {
    void createFriendGroup(String name, List<User> userList, CreateFriendGroupCallback callback);

    void removeFriendGroup(String id, RemoveFriendGroupCallback callBack);

    void addFriends(List<String> userIdList, String friendGroupId, AddFriendsCallback callback);

    void removeFriends(List<String> userIdList, String friendGroupId, RemoveFriendsCallback callback);

    interface CreateFriendGroupCallback {
        void createSuccess(String friendListId);

        void createFail(String error);
    }

    interface RemoveFriendGroupCallback {
        void removeSuccess(FriendGroup friendGroup);

        void removeFail(String error);
    }

    interface AddFriendsCallback {
        void addSuccess(List<FriendGroup> friendGroupList);

        void addFail(String error);
    }

    interface RemoveFriendsCallback {
        void removeSuccess(List<FriendGroup> friendGroupList);

        void removeFail(String error);
    }
}
