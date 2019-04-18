package vn.huynh.whatsapp.model;

import java.util.List;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 4/15/2019.
 */

public interface FriendListInterface extends BaseModelInterface {
    void createFriendList(String name, FriendListCallBack callBack);

    void removeFriendList(String id, FriendListCallBack callBack);

    void addFriend(String userId, String friendListId, FriendListCallBack callBack);

    void addFriends(List<String> userIdList, String friendListId, FriendListCallBack callBack);

    void remove(String userId, String friendListId, FriendListCallBack callBack);

    void remove(List<String> userIdList, String friendListId, FriendListCallBack callBack);


    interface FriendListCallBack {
        void createSuccess(String friendListId);

        void createFail(String message);

        void addFriendSuccess();

        void addFriendFail(String message);

        void removeFriendSuccess();

        void removeFriendFail(String message);

    }
}
