package vn.huynh.whatsapp.group.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class GroupFragment extends BaseFragment implements GroupContract.View {
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_group_list)
    RecyclerView rvChatList;
    @BindView(R.id.fab_create_group)
    FloatingActionButton fabCreateGroup;
    @BindView(R.id.ll_indicator)
    LinearLayout llIndicator;
    @BindView(R.id.loader)
    CircularDotsLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    private static final int CREATE_GROUP_INTENT = 2;
    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager chatListLayoutManager;
    private ArrayList<Chat> chatList;

    public static final String TAG = "GroupFragment";

    private GroupPresenter presenter;
    private boolean firstStart = true;
    private long totalChat = 0;
    private long chatCount = 0;
    public static Map<String, Long> unreadChatIdMap = new HashMap<>();

    public GroupFragment() {
    }

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        ButterKnife.bind(GroupFragment.this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeRecyclerView();
        setupPresenter();
        setEvents();
        resetDataBeforeReload();
        presenter.loadChatList(true, chatList);
    }

    private void initializeRecyclerView() {
        chatList = new ArrayList<>();
        rvChatList.setNestedScrollingEnabled(false);
        rvChatList.setHasFixedSize(false);
        chatListLayoutManager = new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false);
        rvChatList.setLayoutManager(chatListLayoutManager);
        chatListAdapter = new ChatListAdapter(chatList, getActivity(), new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Chat chat) {
                parentActivityListener.setReturnFromChildActivity(true);
                parentActivityListener.showMessageNotification(true);

                ChatListFragment.unreadChatIdMap.remove(chat.getId());
                GroupFragment.unreadChatIdMap.remove(chat.getId());
                if (ChatListFragment.unreadChatIdMap.isEmpty()) {
                    newNotificationCallback.removeChatNotificationDot();
                }
                if (GroupFragment.unreadChatIdMap.isEmpty()) {
                    newNotificationCallback.removeGroupNotificationDot();
                }

                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(Constant.EXTRA_CHAT_OBJECT, chat);
                startActivity(intent);
            }
        });
        rvChatList.setAdapter(chatListAdapter);
    }

    private void setupPresenter() {
        presenter = new GroupPresenter();
        presenter.attachView(this);
    }

    private void resetDataBeforeReload() {
        chatList.clear();
        chatListAdapter.notifyDataSetChanged();
        unreadChatIdMap.clear();
        totalChat = 0;
        chatCount = 0;
//        newNotificationCallback.removeGroupNotificationDot();
    }

    private void setEvents() {
        fabCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivityListener.setReturnFromChildActivity(true);
                startActivityForResult(new Intent(getActivity(), CreateGroupActivity.class), CREATE_GROUP_INTENT);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetDataBeforeReload();
                presenter.loadChatList(true, chatList);
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetDataBeforeReload();
                presenter.loadChatList(true, chatList);
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
        totalChat = count;
    }

    @Override
    public void showChatList(Chat chat, int position) {
        chatCount++;
        if (chat != null) {
            /*showHideListIndicator(llIndicator, false);
            chatList.add(position, chat);
            try {
                chatListAdapter.notifyItemInserted(position);
                chatListLayoutManager.scrollToPositionWithOffset(0, 0);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }*/
            if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0) {
                if (unreadChatIdMap.isEmpty()) {
                    newNotificationCallback.newGroupNotificationDot();
                }
                unreadChatIdMap.put(chat.getId(), 1L);
            }
            showHideListIndicator(llIndicator, false);
            chatList.add(position, chat);
            try {
                chatListAdapter.notifyItemInserted(position);
                chatListLayoutManager.scrollToPositionWithOffset(0, 0);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        if (chatCount == totalChat) {
            hideLoadingIndicator();
            if (chatList.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                showHideListEmptyIndicator(llIndicator, llEmptyData, true);
            }
        }
    }

    @Override
    public void updateChatStatus(Chat chat, boolean hasNewMessage) {
        try {
            /*if (chatList.size() > 0) {
                int i = chatList.indexOf(chatObject);
                if (i > 0) {
                    chatList.removeFriends(chatObject);
                    chatListAdapter.notifyItemRemoved(i);
                    chatList.add(0, chatObject);
                    chatListAdapter.notifyItemInserted(0);
                } else if (i == 0) {
                    chatListAdapter.notifyDataSetChanged();
                }
                chatListLayoutManager.scrollToPositionWithOffset(0, 0);
            }*/
            if (chatList.size() > 0) {
                int i = chatList.indexOf(chat);
                if (i >= 0) {
                    if (hasNewMessage) {
                        if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0) {
                            unreadChatIdMap.put(chat.getId(), 1L);
                            newNotificationCallback.newGroupNotificationDot();
                        }
                        if (i == 0) {
                            chatListAdapter.notifyItemChanged(i);
                        } else {
                            chatList.remove(chat);
                            chatListAdapter.notifyItemRemoved(i);
                            chatList.add(0, chat);
                            chatListAdapter.notifyItemInserted(0);
                            chatListLayoutManager.scrollToPositionWithOffset(0, 0);
                        }
                    } else {
                        chatListAdapter.notifyItemChanged(i);
                        if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) == 0) {
                            unreadChatIdMap.remove(chat.getId());
                            if (unreadChatIdMap.isEmpty())
                                newNotificationCallback.removeGroupNotificationDot();
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
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
        if (!firstStart && !parentActivityListener.returnFromChildActivity()) {
            resetDataBeforeReload();
            presenter.loadChatList(true, chatList);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        firstStart = false;
        if (!parentActivityListener.returnFromChildActivity()) {
            presenter.removeChatListListener();
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
        presenter.detachView();
        presenter.removeChatListListener();
    }
}
