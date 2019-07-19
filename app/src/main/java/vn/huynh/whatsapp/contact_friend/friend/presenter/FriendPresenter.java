package vn.huynh.whatsapp.contact_friend.friend.presenter;

import android.app.Activity;

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
import vn.huynh.whatsapp.utils.LogManagerUtils;

/**
 * Created by duong on 5/19/2019.
 */

public class FriendPresenter implements FriendContract.Presenter {
    public static final String TAG = FriendContract.class.getSimpleName();
    private ChatInterface mChatRepo;
    private UserInterface mUserRepo;
    private FriendInterface mFriendRepo;
    private FriendContract.View mViewFriend;
    private FriendContract.ViewSearchFriend mViewSearchFriend;
    private ContactContract.View mViewContact;
    private BaseFragment.NewNotificationCallback mNewNotificationCallback;

    public FriendPresenter() {
        this.mChatRepo = new ChatRepository();
        this.mUserRepo = new UserRepository();
        this.mFriendRepo = new FriendRepository();
    }

    @Override
    public void attachView(BaseView view) {
        if (view instanceof FriendContract.View)
            this.mViewFriend = (FriendContract.View) view;
        if (view instanceof FriendContract.ViewSearchFriend)
            this.mViewSearchFriend = (FriendContract.ViewSearchFriend) view;
        if (view instanceof ContactContract.View)
            this.mViewContact = (ContactContract.View) view;

        if (view instanceof BaseFragment.NewNotificationCallback)
            this.mNewNotificationCallback = (BaseFragment.NewNotificationCallback) view;
    }

    public void attachView(Activity view) {
        if (view instanceof BaseFragment.NewNotificationCallback)
            this.mNewNotificationCallback = (BaseFragment.NewNotificationCallback) view;
    }

    public void removeListener() {
        if (mUserRepo != null) {
            mUserRepo.removeListener();
        }
        if (mFriendRepo != null) {
            mFriendRepo.removeListener();
        }
    }

    @Override
    public void detachView() {
        this.mViewFriend = null;
        this.mViewSearchFriend = null;
        this.mViewContact = null;
        this.mNewNotificationCallback = null;
    }

    @Override
    public void detachViewFriend() {
        this.mViewFriend = null;
    }

    @Override
    public void detachViewFriendSearch() {
        this.mViewSearchFriend = null;
    }

    @Override
    public void listenerFriendNotification() {
        mUserRepo.listenerForUserFriend(new UserInterface.FriendCallback() {
            @Override
            public void onFriendNotification(int showNotify) {
                if (mNewNotificationCallback != null) {
                    mNewNotificationCallback.showHideFriendDot(showNotify);
                }
            }
        });
    }

    @Override
    public void searchFriendByPhoneNumber(String phoneNumber) {
        if (mViewSearchFriend != null) {
            mViewSearchFriend.showLoadingIndicator();
        }
        mUserRepo.searchFriend(phoneNumber, new UserInterface.SearchFriendCallback() {
            @Override
            public void onSearchSuccess(List<User> userList) {
                if (mViewSearchFriend != null) {
                    mViewSearchFriend.showFriendList(userList);
                }
            }

            @Override
            public void onSearchFail(String error) {
                if (mViewSearchFriend != null) {
                    mViewSearchFriend.showErrorIndicator();
                    mViewSearchFriend.showErrorMessage(error);
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
        if (mViewContact != null) {
            mViewContact.showLoadingIndicator();
        }
        mFriendRepo.createInvite(user, message, new FriendInterface.CreateInviteCallback() {
            @Override
            public void onCreateInviteSuccess(Friend friend) {
                if (mViewContact != null) {
                    mViewContact.showMessage("Invited " + friend.getName());
                }
            }

            @Override
            public void onCreateInviteFail(String error) {
                if (mViewContact != null) {
                    mViewContact.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void loadListFriend(int friendStatus) {
        if (mViewFriend != null) {
            mViewFriend.showLoadingIndicator();
        }
        mFriendRepo.getAllFriend(friendStatus, new FriendInterface.GetFriendCallback() {
            @Override
            public void onGetAllFriendSuccess(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.showFriendList(friend);
                    mViewFriend.hideLoadingIndicator();
                }
            }

            @Override
            public void onGetAllFriendSuccessEmptyData() {
                if (mViewFriend != null) {
                    mViewFriend.showEmptyDataIndicator();
                    mViewFriend.hideLoadingIndicator();
                }
            }

            @Override
            public void onGetFriendCount(long count) {
                if (mViewFriend != null) {
                    mViewFriend.setTotalFriend(count);
                }
            }

            @Override
            public void onFriendUpdate(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.updateFriendStatus(friend);
                }
            }

            @Override
            public void onFriendRemove(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.removeFriend(friend);
                }
            }

            @Override
            public void onGetAllFriendFail(String error) {
                if (mViewFriend != null) {
                    mViewFriend.showErrorMessage(error);
                    mViewFriend.hideLoadingIndicator();
                }
            }
        });
    }

    @Override
    public void createFriendRequest(User user) {
        mFriendRepo.createRequest(user, new FriendInterface.CreateRequestCallback() {
            @Override
            public void onCreateRequestSuccess(Friend friend) {
                if (mViewSearchFriend != null) {
                    mViewSearchFriend.addFriendSuccess("Sent friend request to " + friend.getName());
                }
                if (mViewContact != null) {
                    mViewContact.showMessage("Sent friend request to " + friend.getName());
                }
            }

            @Override
            public void onCreateRequestFail(String error) {
                if (mViewSearchFriend != null) {
                    mViewSearchFriend.addFriendSuccess(error);
                }
                if (mViewContact != null) {
                    mViewContact.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void cancelFriendRequest(Friend friend) {
        mFriendRepo.cancelRequest(friend, new FriendInterface.CancelRequestCallback() {
            @Override
            public void onCancelSuccess(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.showMessage("Cancel friend request to " + friend.getName());
                }
            }

            @Override
            public void onCancelFail(String error) {
                if (mViewFriend != null) {
                    mViewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void acceptFriendRequest(Friend friend) {
        LogManagerUtils.d(TAG, "accept");
        mFriendRepo.acceptRequest(friend, new FriendInterface.AcceptRequestCallback() {
            @Override
            public void onAcceptSuccess(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.showMessage("You and " + friend.getName() + " has became friend");
                }
            }

            @Override
            public void onAcceptFail(String error) {
                if (mViewFriend != null) {
                    mViewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void rejectFriendRequest(Friend friend) {
        mFriendRepo.rejectRequest(friend, new FriendInterface.RejectRequestCallback() {
            @Override
            public void onRejectSuccess(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.showMessage("Reject friend request from " + friend.getName());
                }
            }

            @Override
            public void onRejectFail(String error) {
                if (mViewFriend != null) {
                    mViewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void blockFriendRequest(Friend friend) {
        mFriendRepo.blockRequest(friend, new FriendInterface.BlockRequestCallback() {
            @Override
            public void onBlockSuccess(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.showMessage("Blocked " + friend.getName());
                }
            }

            @Override
            public void onBlockFail(String error) {
                if (mViewFriend != null) {
                    mViewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void removeFriend(Friend friend) {
        mFriendRepo.removeFriend(friend, new FriendInterface.RemoveFriendCallback() {
            @Override
            public void onRemoveFriendSuccess(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.showMessage("Removed friend " + friend.getName());
                }
            }

            @Override
            public void onRemoveFail(String error) {
                if (mViewFriend != null) {
                    mViewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void unBlockFriendRequest(Friend friend) {
        mFriendRepo.removeFriend(friend, new FriendInterface.RemoveFriendCallback() {
            @Override
            public void onRemoveFriendSuccess(Friend friend) {
                if (mViewFriend != null) {
                    mViewFriend.showMessage("Unblock friend " + friend.getName());
                }
            }

            @Override
            public void onRemoveFail(String error) {
                if (mViewFriend != null) {
                    mViewFriend.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void createChat(boolean isGroup, String name, List<User> users) {
        if (mViewFriend != null)
            mViewFriend.showLoadingIndicator();
        mChatRepo.createChat(isGroup, name, users, new ChatInterface.CreateChatCallback() {
            @Override
            public void createSuccess(String chatId) {
                if (mViewFriend != null) {
                    mViewFriend.openChat(chatId);
                    mViewFriend.hideLoadingIndicator();
                }
            }

            @Override
            public void createFail(String message) {
                if (mViewFriend != null) {
                    mViewFriend.showErrorMessage(message);
                    mViewFriend.hideLoadingIndicator();
                }
            }
        });
    }

    @Override
    public void checkSingleChatExist(final boolean isGroup, final String name, final List<User> users) {
        if (mViewFriend != null)
            mViewFriend.showLoadingIndicator();
        String singleChatId = ChatUtils.getSingleChatIdFomUsers(users);
        mChatRepo.checkSingleChatExist(singleChatId, new ChatInterface.CheckSingleChatCallback() {
            @Override
            public void exist(String chatId) {
                if (mViewFriend != null) {
                    mViewFriend.openChat(chatId);
                    mViewFriend.hideLoadingIndicator();
                }
            }

            @Override
            public void notExist() {
                createChat(isGroup, name, users);
            }
        });
    }
}
