package vn.huynh.whatsapp.group.view;

import android.app.Activity;
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

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.chat_list.view.ChatListAdapter;
import vn.huynh.whatsapp.chat_list.view.ChatListFragment;
import vn.huynh.whatsapp.group.GroupContract;
import vn.huynh.whatsapp.group.presenter.GroupPresenter;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;

public class GroupFragmentOld extends BaseFragment implements GroupContract.View {
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_group_list)
    RecyclerView rvChatList;
    //    @BindView(R.id.fab_create_group)
//    FloatingActionButton fabCreateGroup;
    @BindView(R.id.ll_indicator)
    LinearLayout llIndicator;
    @BindView(R.id.loader)
    CircularDotsLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    private static final int CREATE_GROUP_INTENT = 2;
    private ChatListAdapter mChatListAdapter;
    private LinearLayoutManager mChatListLayoutManager;
    private ArrayList<Chat> mChatList;

    public static final String TAG = "GroupFragment";

    private GroupPresenter mGroupPresenter;
    private boolean mFirstStart = true;
    private long mTotalChat = 0;
    private long mChatCount = 0;
    public static Map<String, Long> sUnreadChatIdMap = new HashMap<>();

    public GroupFragmentOld() {
    }

    public static GroupFragmentOld newInstance() {
        return new GroupFragmentOld();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        ButterKnife.bind(GroupFragmentOld.this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        setupPresenter();
        setEvents();
        resetData();
        mGroupPresenter.loadChatList(true, mChatList);
    }

    @Override
    public void initData() {
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        mChatList = new ArrayList<>();
        rvChatList.setNestedScrollingEnabled(false);
        rvChatList.setHasFixedSize(false);
        mChatListLayoutManager = new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false);
        rvChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(mChatList, getActivity(), new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, Chat chat) {
                chat.getNumberUnread().put(ChatUtils.getUser().getId(), 0L);
                mChatListAdapter.notifyItemChanged(position);
                parentActivityListener.setReturnFromChildActivity(true);
                parentActivityListener.showMessageNotification(true);

                ChatListFragment.sUnreadChatIdMap.remove(chat.getId());
                GroupFragmentOld.sUnreadChatIdMap.remove(chat.getId());
                if (ChatListFragment.sUnreadChatIdMap.isEmpty()) {
                    newNotificationCallback.removeChatNotificationDot();
                }
                if (GroupFragmentOld.sUnreadChatIdMap.isEmpty()) {
                    newNotificationCallback.removeGroupNotificationDot();
                }

                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(Constant.EXTRA_CHAT_OBJECT, chat);
                startActivity(intent);
            }
        }, new ChatListAdapter.ChatAdapterListener() {
            @Override
            public void onFilter(boolean isEmptyResult) {
                if (isEmptyResult) {
                    rvChatList.setVisibility(View.GONE);
                    showHideListEmptyIndicator(llIndicator, llEmptyData, true);
                } else {
                    rvChatList.setVisibility(View.VISIBLE);
                    showHideListEmptyIndicator(llIndicator, llEmptyData, false);
                }
            }
        });
        rvChatList.setAdapter(mChatListAdapter);
    }

    private void setupPresenter() {
        mGroupPresenter = new GroupPresenter();
        mGroupPresenter.attachView(this);
    }

    @Override
    public void resetData() {
        mChatList.clear();
        mChatListAdapter.notifyDataSetChanged();
        sUnreadChatIdMap.clear();
        mTotalChat = 0;
        mChatCount = 0;
//        newNotificationCallback.removeGroupNotificationDot();
    }

    @Override
    public void setEvents() {
//        fabCreateGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                parentActivityListener.setReturnFromChildActivity(true);
//                startActivityForResult(new Intent(getActivity(), CreateGroupActivity.class), CREATE_GROUP_INTENT);
//            }
//        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetData();
                mGroupPresenter.loadChatList(true, mChatList);
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
                mGroupPresenter.loadChatList(true, mChatList);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CREATE_GROUP_INTENT) {
                if (data.getStringExtra(Constant.EXTRA_CHAT_ID) != null) {
                    String chatGroupId = data.getStringExtra(Constant.EXTRA_CHAT_ID);
                    openChat(chatGroupId);
                }
            }
        }
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
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListEmptyIndicator(llIndicator, llEmptyData, true);
    }

    @Override
    public void showErrorIndicator() {
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListErrorIndicator(llIndicator, llError, true);
    }

    @Override
    public void showListContact(User userObject) {

    }

    @Override
    public void setChatCount(long count) {
        mTotalChat = count;
    }

    @Override
    public void showChatList(Chat chat, int position) {
        mChatCount++;
        if (chat != null) {
            /*showHideListIndicator(llIndicator, false);
            mChatList.add(position, chat);
            try {
                mChatListAdapter.notifyItemInserted(position);
                mChatListLayoutManager.scrollToPositionWithOffset(0, 0);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }*/
            if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0) {
                if (sUnreadChatIdMap.isEmpty()) {
                    newNotificationCallback.newGroupNotificationDot();
                }
                sUnreadChatIdMap.put(chat.getId(), 1L);
            }
            showHideListIndicator(llIndicator, false);
            mChatList.add(position, chat);
            mChatListAdapter.notifyItemInserted(position);
            mChatListLayoutManager.scrollToPositionWithOffset(0, 0);
        }
        if (mChatCount == mTotalChat) {
            hideLoadingIndicator();
//            mChatListAdapter.notifyDataSetChanged();
            mChatListLayoutManager.scrollToPositionWithOffset(0, 0);
            if (mChatList.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                showHideListEmptyIndicator(llIndicator, llEmptyData, true);
            }
        }
    }

    @Override
    public void updateChatStatus(Chat chat, boolean hasNewMessage) {
        try {
            int index = -1;
            for (int i = 0; i < mChatList.size(); i++) {
                if (chat.getId().equals(mChatList.get(i).getId())) {
                    index = i;
                    if (chat.getLastMessageDateInLong() - mChatList.get(i).getLastMessageDateInLong() > 50)
                        hasNewMessage = true;
                    mChatList.get(i).cloneChat(chat);
                    break;
                }
            }

            if (index >= 0) {
                if (hasNewMessage) {
                    if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0) {
                        sUnreadChatIdMap.put(chat.getId(), 1L);
                        newNotificationCallback.newGroupNotificationDot();

                        ChatListFragment.sUnreadChatIdMap.put(chat.getId(), 1L);
                        newNotificationCallback.newChatNotificationDot();
                    }
                    if (index == 0) {
                        mChatListAdapter.notifyItemChanged(index);
                    } else {
                        mChatListAdapter.notifyItemChanged(index);
                        Chat temp = mChatList.get(index);
                        mChatList.remove(index);
                        mChatList.add(0, temp);
                        mChatListAdapter.notifyItemMoved(index, 0);
                        mChatListLayoutManager.scrollToPositionWithOffset(0, 0);
                    }
                } else {
                    mChatListAdapter.notifyItemChanged(index);
                    if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) == 0) {
                        sUnreadChatIdMap.remove(chat.getId());
                        if (sUnreadChatIdMap.isEmpty())
                            newNotificationCallback.removeGroupNotificationDot();

                        ChatListFragment.sUnreadChatIdMap.remove(chat.getId());
                        if (ChatListFragment.sUnreadChatIdMap.isEmpty())
                            newNotificationCallback.removeChatNotificationDot();
                    }
                }
            }
        } catch (
                IndexOutOfBoundsException e)

        {
            e.printStackTrace();
        }

    }

    @Override
    public void updateChatNotification(String chatId, boolean turnOn) {

    }

    @Override
    public void updateNumberUnreadMessage(String chatId) {

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
        if (!mFirstStart && !parentActivityListener.returnFromChildActivity()) {
            resetData();
            mGroupPresenter.loadChatList(true, mChatList);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirstStart = false;
        if (!parentActivityListener.returnFromChildActivity()) {
            mGroupPresenter.removeChatListListener();
        }
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGroupPresenter.detachView();
        mGroupPresenter.removeChatListListener();
    }
}
