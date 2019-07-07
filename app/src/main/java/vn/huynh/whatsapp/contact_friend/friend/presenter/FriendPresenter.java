package vn.huynh.whatsapp.contact_friend.friend.presenter;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.contact_friend.contact.ContactContract;
import vn.huynh.whatsapp.contact_friend.friend.FriendContract;
import vn.huynh.whatsapp.model.ChatInterface;
import vn.huynh.whatsapp.model.ChatRepository;
import vn.huynh.whatsapp.model.Friend;
import vn.huynh.whatsapp.model.FriendInterface;
import vn.huynh.whatsapp.model.FriendRepository;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.model.UserInterface;
import vn.huynh.whatsapp.model.UserRepository;
import vn.huynh.whatsapp.utils.ChatUtils;

/**
 * Created by duong on 5/19/2019.
 */

public class FriendPresenter implements FriendContract.Presenter {
    public static final String TAG = FriendContract.class.getSimpleName();
    private ChatInterface chatRepo;
    private UserInterface userRepo;
    private FriendInterface friendRepo;
    private FriendContract.View viewFriend;
    private FriendContract.ViewSearchFriend viewSearchFriend;
    private ContactContract.View viewContact;
    private BaseFragment.NewNotificationCallback newNotificationCallback;

    public FriendPresenter() {
        this.chatRepo = new ChatRepository();
        this.userRepo = new UserRepository();
        this.friendRepo = new FriendRepository();
    }

    @Override
    public void attachView(BaseView view) {
        if (view instanceof FriendContract.View)
            this.viewFriend = (FriendContract.View) view;
        if (view instanceof FriendContract.ViewSearchFriend)
            this.viewSearchFriend = (FriendContract.ViewSearchFriend) view;
        if (view instanceof ContactContract.View)
            this.viewContact = (ContactContract.View) view;

        if (view instanceof BaseFragment.NewNotificationCallback)
            this.newNotificationCallback = (BaseFragment.NewNotificationCallback) view;
    }

    public void attachView(Activity view) {
        if (view instanceof BaseFragment.NewNotificationCallback)
            this.newNotificationCallback = (BaseFragment.NewNotificationCallback) view;
    }

    public void removeListener() {
        if (userRepo != null) {
            userRepo.removeListener();
        }
        if (friendRepo != null) {
            friendRepo.removeListener();
        }
    }

    @Override
    public void detachView() {
        this.viewFriend = null;
        this.viewSearchFriend = null;
        this.viewContact = null;
        this.newNotificationCallback = null;
    }

    @Override
    public void detachViewFriend() {
        this.viewFriend = null;
    }

    @Override
    public void detachViewFriendSearch() {
        this.viewSearchFriend = null;
    }

    @Override
    public void listenerFriendNotification() {
        userRepo.listenerForUserFriend(new UserInterface.FriendCallback() {
            @Override
            public void onFriendNotification(int showNotify) {
                if (newNotificationCallback != null) {
                    newNotificationCallback.showHideFriendDot(showNotify);
                }
            }
        });
    }

    @Override
    public void searchFriendByPhoneNumber(String phoneNumber) {
        if (viewSearchFriend != null) {
            viewSearchFriend.showLoadingIndicator();
        }
        userRepo.searchFriend(phoneNumber, new UserInterface.SearchFriendCallback() {
            @Override
            public void onSearchSuccess(List<User> userList) {
                if (viewSearchFriend != null) {
                    viewSearchFriend.showFriendList(userList);
                }
            }

            @Override
            public void onSearchFail(String error) {
                if (viewSearchFriend != null) {
                    viewSearchFriend.showErrorIndicator();
                    viewSearchFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void addFriends(ArrayList<User> userArrayList) {
        for (User user : userArrayList) {
            createFriendRequest(user);
        }
    }

    @Override
    public void sendInvite(User user, String message) {
        if (viewContact != null) {
            viewContact.showLoadingIndicator();
        }
        friendRepo.createInvite(user, message, new FriendInterface.CreateInviteCallback() {
            @Override
            public void onCreateInviteSuccess(Friend friend) {
                if (viewContact != null) {
                    viewContact.showMessage("Invited " + friend.getName());
                }
            }

            @Override
            public void onCreateInviteFail(String error) {
                if (viewContact != null) {
                    viewContact.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void loadListFriend(int friendStatus) {
        if (viewFriend != null) {
            viewFriend.showLoadingIndicator();
        }
        friendRepo.getAllFriend(friendStatus, new FriendInterface.GetFriendCallback() {
            @Override
            public void onGetAllFriendSuccess(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.showFriendList(friend);
                    viewFriend.hideLoadingIndicator();
                }
            }

            @Override
            public void onGetAllFriendSuccessEmptyData() {
                if (viewFriend != null) {
                    viewFriend.showEmptyDataIndicator();
                    viewFriend.hideLoadingIndicator();
                }
            }

            @Override
            public void onGetFriendCount(long count) {
                if (viewFriend != null) {
                    viewFriend.setTotalFriend(count);
                }
            }

            @Override
            public void onFriendUpdate(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.updateFriendStatus(friend);
                }
            }

            @Override
            public void onFriendRemove(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.removeFriend(friend);
                }
            }

            @Override
            public void onGetAllFriendFail(String error) {
                if (viewFriend != null) {
                    viewFriend.showErrorMessage(error);
                    viewFriend.hideLoadingIndicator();
                }
            }
        });
    }

    @Override
    public void createFriendRequest(User user) {
        friendRepo.createRequest(user, new FriendInterface.CreateRequestCallback() {
            @Override
            public void onCreateRequestSuccess(Friend friend) {
                if (viewSearchFriend != null) {
                    viewSearchFriend.addFriendSuccess("Sent friend request to " + friend.getName());
                }
                if (viewContact != null) {
                    viewContact.showMessage("Sent friend request to " + friend.getName());
                }
            }

            @Override
            public void onCreateRequestFail(String error) {
                if (viewSearchFriend != null) {
                    viewSearchFriend.addFriendSuccess(error);
                }
                if (viewContact != null) {
                    viewContact.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void cancelFriendRequest(Friend friend) {
        friendRepo.cancelRequest(friend, new FriendInterface.CancelRequestCallback() {
            @Override
            public void onCancelSuccess(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.showMessage("Cancel friend request to " + friend.getName());
                }
            }

            @Override
            public void onCancelFail(String error) {
                if (viewFriend != null) {
                    viewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void acceptFriendRequest(Friend friend) {
        Log.d(TAG, "accept");
        friendRepo.acceptRequest(friend, new FriendInterface.AcceptRequestCallback() {
            @Override
            public void onAcceptSuccess(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.showMessage("You and " + friend.getName() + " has became friend");
                }
            }

            @Override
            public void onAcceptFail(String error) {
                if (viewFriend != null) {
                    viewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void rejectFriendRequest(Friend friend) {
        friendRepo.rejectRequest(friend, new FriendInterface.RejectRequestCallback() {
            @Override
            public void onRejectSuccess(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.showMessage("Reject friend request from " + friend.getName());
                }
            }

            @Override
            public void onRejectFail(String error) {
                if (viewFriend != null) {
                    viewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void blockFriendRequest(Friend friend) {
        friendRepo.blockRequest(friend, new FriendInterface.BlockRequestCallback() {
            @Override
            public void onBlockSuccess(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.showMessage("Blocked " + friend.getName());
                }
            }

            @Override
            public void onBlockFail(String error) {
                if (viewFriend != null) {
                    viewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void removeFriend(Friend friend) {
        friendRepo.removeFriend(friend, new FriendInterface.RemoveFriendCallback() {
            @Override
            public void onRemoveFriendSuccess(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.showMessage("Removed friend " + friend.getName());
                }
            }

            @Override
            public void onRemoveFail(String error) {
                if (viewFriend != null) {
                    viewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void unBlockFriendRequest(Friend friend) {
        friendRepo.removeFriend(friend, new FriendInterface.RemoveFriendCallback() {
            @Override
            public void onRemoveFriendSuccess(Friend friend) {
                if (viewFriend != null) {
                    viewFriend.showMessage("Unblock friend " + friend.getName());
                }
            }

            @Override
            public void onRemoveFail(String error) {
                if (viewFriend != null) {
                    viewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void createChat(boolean isGroup, String name, List<User> users) {
        if (viewFriend != null)
            viewFriend.showLoadingIndicator();
        chatRepo.createChat(isGroup, name, users, new ChatInterface.CreateChatCallback() {
            @Override
            public void createSuccess(String chatId) {
                if (viewFriend != null) {
                    viewFriend.openChat(chatId);
                    viewFriend.hideLoadingIndicator();
                }
            }

            @Override
            public void createFail(String message) {
                if (viewFriend != null) {
                    viewFriend.showErrorMessage(message);
                    viewFriend.hideLoadingIndicator();
                }
            }
        });
    }

    @Override
    public void checkSingleChatExist(final boolean isGroup, final String name, final List<User> users) {
        if (viewFriend != null)
            viewFriend.showLoadingIndicator();
        String singleChatId = ChatUtils.getSingleChatIdFomUsers(users);
        chatRepo.checkSingleChatExist(singleChatId, new ChatInterface.CheckSingleChatCallback() {
            @Override
            public void exist(String chatId) {
                if (viewFriend != null) {
                    viewFriend.openChat(chatId);
                    viewFriend.hideLoadingIndicator();
                }
            }

            @Override
            public void notExist() {
                createChat(isGroup, name, users);
            }
        });
    }
}
