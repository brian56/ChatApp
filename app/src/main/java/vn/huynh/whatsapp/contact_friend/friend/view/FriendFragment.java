package vn.huynh.whatsapp.contact_friend.friend.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.contact_friend.friend.FriendContract;
import vn.huynh.whatsapp.contact_friend.friend.presenter.FriendPresenter;
import vn.huynh.whatsapp.custom_views.sticky_header.stickyView.StickHeaderItemDecoration;
import vn.huynh.whatsapp.model.Friend;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.SharedPrefsUtil;

/**
 * Created by duong on 5/20/2019.
 */

public class FriendFragment extends BaseFragment implements FriendContract.View {
    public static String TAG = "FriendFragment";
    @BindView(R.id.rv_friend_list)
    RecyclerView rvFriendList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.ll_indicator)
    LinearLayout llIndicator;
    @BindView(R.id.loader)
    CircularDotsLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    @BindView(R.id.floating_action_button)
    FloatingActionButton fabAddFriend;

    DialogSearchFriend mDialogSearchFriend;

    private FriendListAdapter mFriendListAdapter;
    private RecyclerView.LayoutManager mFriendListLayoutManager;

    private FriendListAdapter.HeaderDataImpl mHeaderWasRequested;
    private FriendListAdapter.HeaderDataImpl mHeaderAccept;
    private FriendListAdapter.HeaderDataImpl mHeaderRequest;
    private FriendListAdapter.HeaderDataImpl mHeaderBlock;
    private FriendListAdapter.HeaderDataImpl mHeaderWasRejected;

    private boolean firstStart = true;
    private FriendContract.Presenter mFriendPresenter;
    private long mTotalFriend = 0;
    /**
     * because the update friend event fired twice, so we using this flag to know when to update the
     * friend object correctly
     */
    private static boolean sDoUpdate = false;


    public FriendFragment() {

    }

    public static FriendFragment newInstance() {

        Bundle args = new Bundle();

        FriendFragment fragment = new FriendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        setEvents();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (parentActivityListener == null) {
            if (context instanceof ParentActivityListener) {
                parentActivityListener = (ParentActivityListener) context;
            }
        }
        if (newNotificationCallback == null) {
            if (context instanceof NewNotificationCallback) {
                newNotificationCallback = (NewNotificationCallback) context;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        sDoUpdate = false;
        SharedPrefsUtil.getInstance().put(Constant.SP_LAST_NOTIFICATION_FRIEND_ID, "");
        SharedPrefsUtil.getInstance().put(Constant.SP_LAST_NOTIFICATION_FRIEND_STATUS, 0);
        resetData();
        setupPresenter();
        mFriendPresenter.loadListFriend(-1);
//        mFriendPresenter.listenerFriendNotification();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFriendPresenter.detachViewFriend();
    }

    private void setupPresenter() {
        mFriendPresenter = new FriendPresenter();
        mFriendPresenter.attachView(this);
    }

    @Override
    public void setEvents() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetData();
                mFriendListAdapter.notifyDataSetChanged();
                mFriendPresenter.loadListFriend(-1);
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
                mFriendListAdapter.notifyDataSetChanged();
                mFriendPresenter.loadListFriend(-1);
            }
        });
        fabAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogSearchFriend = new DialogSearchFriend(getContext());
                mDialogSearchFriend.show(new DialogSearchFriend.SearchFriendListener() {
                    @Override
                    public void onAddedFriendListener(ArrayList<User> selectedUsers) {
                        //TODO: add friend request to list
//                        for (User user : selectedUsers) {
//                            mFriendPresenter.createFriendRequest(user);
//                        }
                    }
                });
            }
        });
    }

    @Override
    public void initData() {
        initializeRecyclerView();
    }

    private void initHeadersAndData() {
        mHeaderAccept = new FriendListAdapter.HeaderDataImpl(Friend.STATUS_ACCEPT);
        mHeaderWasRequested = new FriendListAdapter.HeaderDataImpl(Friend.STATUS_WAS_REQUESTED);
        mHeaderRequest = new FriendListAdapter.HeaderDataImpl(Friend.STATUS_REQUEST);
        mHeaderBlock = new FriendListAdapter.HeaderDataImpl(Friend.STATUS_BLOCK);
        mHeaderWasRejected = new FriendListAdapter.HeaderDataImpl(Friend.STATUS_WAS_REJECTED);
    }

    private void initializeRecyclerView() {
        rvFriendList.setNestedScrollingEnabled(false);
        rvFriendList.setHasFixedSize(false);
        mFriendListLayoutManager = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false);
        rvFriendList.setLayoutManager(mFriendListLayoutManager);
        rvFriendList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fabAddFriend.getVisibility() == View.VISIBLE) {
                    fabAddFriend.hide();
                } else if (dy < 0 && fabAddFriend.getVisibility() != View.VISIBLE) {
                    fabAddFriend.show();
                }
            }
        });
        mFriendListAdapter = new FriendListAdapter(getContext(),
                new FriendListAdapter.ItemFriendMenuClickListener() {
                    @Override
                    public void onAccept(Friend friend) {
                        mFriendPresenter.acceptFriendRequest(friend);
                    }

                    @Override
                    public void onReject(Friend friend) {
                        mFriendPresenter.rejectFriendRequest(friend);
                    }

                    @Override
                    public void onBlock(Friend friend) {
                        mFriendPresenter.blockFriendRequest(friend);
                    }

                    @Override
                    public void onCancel(Friend friend) {
                        mFriendPresenter.cancelFriendRequest(friend);
                    }

                    @Override
                    public void onUnfriend(Friend friend) {
                        mFriendPresenter.removeFriend(friend);
                    }

                    @Override
                    public void onUnblock(Friend friend) {
                        mFriendPresenter.unBlockFriendRequest(friend);
                    }
                });
        rvFriendList.setAdapter(mFriendListAdapter);
        rvFriendList.addItemDecoration(new StickHeaderItemDecoration(mFriendListAdapter));
        initHeadersAndData();
    }

    @Override
    public void resetData() {
//        friendRequestMap = new HashMap<>();
        mFriendListAdapter.clearData();
        initHeadersAndData();
    }

    //=========================

    @Override
    public void showLoadingIndicator() {
        showHideListEmptyIndicator(llIndicator, llEmptyData, false);
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListErrorIndicator(llIndicator, llError, false);
        showLoadingSwipeLayout(swipeRefreshLayout);
    }

    @Override
    public void hideLoadingIndicator() {
        hideLoadingSwipeLayout(swipeRefreshLayout);
    }

    @Override
    public void showEmptyDataIndicator() {
//        newNotificationCallback.removeContactNotificationDot();
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListEmptyIndicator(llIndicator, llEmptyData, true);
    }

    @Override
    public void showErrorIndicator() {
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListErrorIndicator(llIndicator, llError, true);
    }

    @Override
    public void showErrorMessage(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    //====================


    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showHideFriendNotification(int friendNotification) {
//        if(friendNotification == 0) {
//            newNotificationCallback.removeContactNotificationDot();
//        } else {
//            newNotificationCallback.newContactNotificationDot();
//        }
    }

    @Override
    public void setTotalFriend(long totalFriend) {
        this.mTotalFriend = totalFriend;
    }

    @Override
    public void showFriendList(Friend friend) {
        if (friend != null) {
            showHideListEmptyIndicator(llIndicator, llEmptyData, false);
            showHideListLoadingIndicator(llIndicator, loader, false);
            showHideListErrorIndicator(llIndicator, llError, false);
            switch (friend.getStatus()) {
                case Friend.STATUS_WAS_REQUESTED:
//                    friendRequestMap.put(friend.getUserId(), friend.getStatus());
                    mFriendListAdapter.setHeaderAndData(friend, mHeaderWasRequested);
                    break;
                case Friend.STATUS_ACCEPT:
                case Friend.STATUS_WAS_ACCEPTED:
                    mFriendListAdapter.setHeaderAndData(friend, mHeaderAccept);
                    break;
                case Friend.STATUS_REQUEST:
                    mFriendListAdapter.setHeaderAndData(friend, mHeaderRequest);
                    break;
                case Friend.STATUS_BLOCK:
                    mFriendListAdapter.setHeaderAndData(friend, mHeaderBlock);
                    break;
                case Friend.STATUS_WAS_REJECTED:
                    mFriendListAdapter.setHeaderAndData(friend, mHeaderWasRejected);
                    break;
                /*case Friend.STATUS_REJECT:
//                    rejectList.add(friend);
                    break;
                case Friend.STATUS_WAS_BLOCKED:
//                    wasBlockedList.add(friend);
                    break;
                case Friend.STATUS_INVITE:
//                    inviteList.add(friend);
                    break;*/
            }
            if ((mFriendListAdapter.getItemCount() - mFriendListAdapter.getNumberHeader()) == mTotalFriend) {
                //load done
//                mFriendListAdapter.notifyDataSetChanged();
                hideLoadingIndicator();
            }
            if (mFriendListAdapter.countItemByStatus(Friend.STATUS_WAS_REQUESTED) == 0) {
                if (newNotificationCallback != null)
                    newNotificationCallback.removeContactNotificationDot();
            } else {
                if (newNotificationCallback != null)
                    newNotificationCallback.newContactNotificationDot();
            }
        }
    }

    @Override
    public void updateFriendStatus(Friend friend) {
        if (sDoUpdate) {
            if (friend != null) {
                mFriendListAdapter.removeData(friend);
                switch (friend.getStatus()) {
                    case Friend.STATUS_WAS_REQUESTED:
//                        friendRequestMap.put(friend.getUserId(), friend.getStatus());
                        mFriendListAdapter.setHeaderAndData(friend, mHeaderWasRequested);
                        break;
                    case Friend.STATUS_ACCEPT:
//                        friendRequestMap.remove(friend.getUserId());
                        mFriendListAdapter.setHeaderAndData(friend, mHeaderAccept);
                        break;
                    case Friend.STATUS_REQUEST:
                        mFriendListAdapter.setHeaderAndData(friend, mHeaderRequest);
                        break;
                    case Friend.STATUS_BLOCK:
                        mFriendListAdapter.setHeaderAndData(friend, mHeaderBlock);
                        break;
                    case Friend.STATUS_WAS_REJECTED:
                        mFriendListAdapter.setHeaderAndData(friend, mHeaderWasRejected);
                        break;
                }
            }
            sDoUpdate = false;
            if (mFriendListAdapter.countItemByStatus(Friend.STATUS_WAS_REQUESTED) == 0) {
                if (newNotificationCallback != null)
                    newNotificationCallback.removeContactNotificationDot();
            } else {
                if (newNotificationCallback != null)
                    newNotificationCallback.newContactNotificationDot();
            }
        } else {
            sDoUpdate = true;
        }
    }

    @Override
    public void removeFriend(Friend friend) {
        if (friend != null) {
            mFriendListAdapter.removeData(friend);
            if (mFriendListAdapter.countItemByStatus(Friend.STATUS_WAS_REQUESTED) == 0) {
                if (newNotificationCallback != null)
                    newNotificationCallback.removeContactNotificationDot();
            }
            if (mFriendListAdapter.getItemCount() == 0) {
                showHideListEmptyIndicator(llIndicator, llEmptyData, true);
            }
        }
    }

    @Override
    public void openChat(String chatId) {

    }
}
