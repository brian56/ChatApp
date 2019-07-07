package vn.huynh.whatsapp.model;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 5/18/2019.
 */

public interface FriendInterface extends BaseModelInterface {
    void getAllFriend(int friendStatus, GetFriendCallback callback);

    void createInvite(User user, String message, CreateInviteCallback callback);

    void createRequest(User user, CreateRequestCallback callback);

    void acceptRequest(Friend friend, AcceptRequestCallback callback);

    void cancelRequest(Friend friend, CancelRequestCallback callback);

    void rejectRequest(Friend friend, RejectRequestCallback callback);

    void removeFriend(Friend friend, RemoveFriendCallback callback);

    void blockRequest(Friend friend, BlockRequestCallback callback);

    void updateFriendNotification(String userId, int friendStatus, boolean showNotify);

//    void getFriendNotification(GetFriendNotificationCallback callback);

//    void listenToNewRequest(String currentUserId, FriendCallback callback);


    interface GetFriendCallback {
        void onGetAllFriendSuccess(Friend friend);

        void onGetAllFriendSuccessEmptyData();

        void onGetFriendCount(long count);

        void onFriendUpdate(Friend friend);

        void onFriendRemove(Friend friend);

        void onGetAllFriendFail(String error);
    }

    interface CreateInviteCallback {

        void onCreateInviteSuccess(Friend friend);

        void onCreateInviteFail(String error);
    }

    interface CreateRequestCallback {

        void onCreateRequestSuccess(Friend friend);

        void onCreateRequestFail(String error);
    }

    interface CancelRequestCallback {

        void onCancelSuccess(Friend friend);

        void onCancelFail(String error);
    }

    interface AcceptRequestCallback {

        void onAcceptSuccess(Friend friend);

        void onAcceptFail(String error);
    }

    interface RejectRequestCallback {

        void onRejectSuccess(Friend friend);

        void onRejectFail(String error);
    }

    interface BlockRequestCallback {

        void onBlockSuccess(Friend friend);

        void onBlockFail(String error);
    }

    interface RemoveFriendCallback {

        void onRemoveFriendSuccess(Friend friend);

        void onRemoveFail(String error);
    }

    interface GetFriendNotificationCallback {
        void onGetSuccess(int friendNotification);

        void onGetFail(String error);
    }

    interface UpdateFriendNotificationCallback {
        void onUpdateSuccess(int friendNotification);

        void onUpdateFail(String error);
    }
}
