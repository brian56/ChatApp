package vn.huynh.whatsapp.contact_friend.contact.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.contact_friend.contact.ContactContract;
import vn.huynh.whatsapp.contact_friend.contact.presenter.ContactPresenter;
import vn.huynh.whatsapp.contact_friend.friend.FriendContract;
import vn.huynh.whatsapp.contact_friend.friend.presenter.FriendPresenter;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;

/**
 * Created by duong on 3/23/2019.
 */

public class ContactFragment extends BaseFragment implements ContactContract.View {

    @BindView(R.id.rv_contact_list)
    RecyclerView rvUserList;
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

    public static final String TAG = "ContactFragment";

    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;
    private ArrayList<User> mUserList;

    private boolean mIsFirstStart = true;
    private ContactContract.Presenter mContactPresenter;
    private FriendContract.Presenter mFriendPresenter;
    private InviteDialog mInviteDialog;

    public ContactFragment() {
    }

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPresenter();
        mInviteDialog = new InviteDialog(getContext());
        setEvents();
        initData();
        mContactPresenter.loadListContact(getContext());
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

    private void setupPresenter() {
        mContactPresenter = new ContactPresenter();
        mContactPresenter.attachView(this);

        mFriendPresenter = new FriendPresenter();
        mFriendPresenter.attachView(this);
    }

    @Override
    public void setEvents() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mUserList.clear();
                mUserListAdapter.notifyDataSetChanged();
                mContactPresenter.loadListContact(getContext());
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserList.clear();
                mUserListAdapter.notifyDataSetChanged();
                mContactPresenter.loadListContact(getContext());
            }
        });
    }

    @Override
    public void initData() {
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        mUserList = new ArrayList<>();
        rvUserList.setNestedScrollingEnabled(false);
        rvUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false);
        rvUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new ContactListAdapter(mUserList, true, false, true,
                new ContactListAdapter.OnItemClickListener() {
                    @Override
                    public void onInvite(User user) {
                        showInviteDialog(user);
                    }

                    @Override
                    public void onAddFriend(User user) {
                        mFriendPresenter.createFriendRequest(user);
                    }

                    @Override
                    public void onChat(User user) {
                        List<User> list = new ArrayList<>();
                        list.add(new User(ChatUtils.getUser().getId()));
                        list.add(user);
                        mContactPresenter.checkSingleChatExist(false, "", list);
                    }
                });
        rvUserList.setAdapter(mUserListAdapter);

    }

    @Override
    public void resetData() {

    }

    private void showInviteDialog(final User user) {
        if (mInviteDialog != null && mInviteDialog.isShowing()) {
            return;
        }
        mInviteDialog.show(user, new InviteDialog.InviteListener() {
            @Override
            public void onInviteCompleteListener(User friend, String message) {
                mFriendPresenter.sendInvite(friend, message);
            }
        });
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showListContact(User userObject) {
        if (userObject != null) {
            mUserList.add(userObject);
            Collections.sort(mUserList, new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    if (o1.getRegisteredUser() && !o2.getRegisteredUser())
                        return -1;
                    if (!o1.getRegisteredUser() && o2.getRegisteredUser())
                        return 1;
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            mUserListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showSearchResult(List<User> userList) {
        //TODO: show search result
    }

    @Override
    public void showLoadingIndicator() {
        showHideListEmptyIndicator(llIndicator, llEmptyData, false);
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListErrorIndicator(llIndicator, llError, false);
        showLoadingSwipeLayout(swipeRefreshLayout);
    }

    @Override
    public void hideLoadingIndicator() {
        showHideListIndicator(llIndicator, false);
        hideLoadingSwipeLayout(swipeRefreshLayout);
    }

    @Override
    public void showEmptyDataIndicator() {
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListEmptyIndicator(llIndicator, llEmptyData, true);
    }

    @Override
    public void showErrorIndicator() {
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListErrorIndicator(llIndicator, llError, true);
    }

    @Override
    public void showErrorMessage(String message) {

    }

    @Override
    public void openChat(String key) {
        parentActivityListener.setReturnFromChildActivity(true);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constant.EXTRA_CHAT_ID, key);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mIsFirstStart && !parentActivityListener.returnFromChildActivity()) {
            mUserList.clear();
            mUserListAdapter.notifyDataSetChanged();
            mContactPresenter.loadListContact(getContext());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mIsFirstStart = false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactPresenter.detachView();
    }
}
