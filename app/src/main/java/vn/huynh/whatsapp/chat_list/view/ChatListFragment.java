package vn.huynh.whatsapp.chat_list.view;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.loopeer.itemtouchhelperextension.ItemTouchHelperExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.chat.ChatContract;
import vn.huynh.whatsapp.chat.presenter.ChatPresenter;
import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.chat_list.ChatListContract;
import vn.huynh.whatsapp.chat_list.presenter.ChatListPresenter;
import vn.huynh.whatsapp.contact_friend.friend.view.DialogSearchFriend;
import vn.huynh.whatsapp.group.view.CreateGroupActivity;
import vn.huynh.whatsapp.group.view.GroupFragment;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.DisplayUtils;
import vn.huynh.whatsapp.utils.LogManagerUtils;
import vn.huynh.whatsapp.utils.MyApp;

/**
 * Created by duong on 4/2/2019.
 */

public class ChatListFragment extends BaseFragment implements ChatListContract.View {
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_chat_list)
    RecyclerView rvChatList;
    @BindView(R.id.ll_indicator)
    LinearLayout llIndicator;
    @BindView(R.id.loader)
    CircularDotsLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    public static final String TAG = "ChatListFragment";

    private ChatListAdapter mChatListAdapter;
    private LinearLayoutManager mChatListLayoutManager;
    private ArrayList<Chat> mChatList;
    private ChatListContract.Presenter mChatListPresenter;
    private ChatContract.Presenter mChatPresenter;
    private SearchView mSearchView;
    private SearchManager searchManager;

    private static final int CREATE_GROUP_INTENT = 100;
    private static final String KEY_CHAT_LIST = "KEY_CHAT_LIST";
    private static final String KEY_CURRENT_POSITION = "KEY_CURRENT_POSITION";

    private long mTotalChat = 0;
    private long mChatCount = 0;
    private boolean mFirstStart = true;
    public static Map<String, Long> sUnreadChatIdMap = new HashMap<>();

    private ItemTouchHelperExtension.Callback mTouchCallback;
    private ItemTouchHelperExtension mItemTouchHelper;

    public ChatListFragment() {
    }

    public static ChatListFragment newInstance() {
        return new ChatListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LogManagerUtils.d(TAG, "On create");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO: restore data if have
        initData();
        setupPresenter();
        setEvents();
        resetData();
        mChatListPresenter.loadChatList(false, mChatList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if(savedInstanceState != null) {
//            mChatList = savedInstanceState.getParcelableArrayList(KEY_CHAT_LIST);
//            currentItemPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
//            mChatListAdapter.setChatList(mChatList);
//            mChatListAdapter.notifyDataSetChanged();
//            mChatListLayoutManager.scrollToPositionWithOffset(currentItemPosition, 0);
//        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat_list, menu);

        searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        mSearchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setFocusable(false);

        mSearchView.setMaxWidth(DisplayUtils.dip2px(getContext(), 200));
        mSearchView.setQueryHint(getString(R.string.menu_search_hint));
        TextView searchText = (TextView)
                mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(-8, 0, -12, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.weight = 1;
        searchText.setLayoutParams(params);
        mSearchView.clearFocus();

        // listening to search query text change
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler mChatListview when query submitted
                try {
                    mChatListAdapter.getFilter().filter(query);
                    ((LinearLayoutManager) rvChatList.getLayoutManager()).scrollToPositionWithOffset(0, 0);
                    LogManagerUtils.d(TAG, "itemCountSubmit = " + mChatListAdapter.getItemCount());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler mChatListview when text is changed
                try {
                    mChatListAdapter.getFilter().filter(query);
                    ((LinearLayoutManager) rvChatList.getLayoutManager()).scrollToPositionWithOffset(0, 0);
                    LogManagerUtils.d(TAG, "Query =" + query + ", itemCount = " + mChatListAdapter.getItemCount());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_friend_or_group) {
            View menuItemView = getActivity().findViewById(R.id.action_add_friend_or_group);
            PopupMenu popup = new PopupMenu(getContext(), menuItemView);
            popup.inflate(R.menu.menu_popup_add_friend_or_group);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_add_friend:
                            DialogSearchFriend dialogSearchFriend = new DialogSearchFriend(getContext());
                            dialogSearchFriend.show(new DialogSearchFriend.SearchFriendListener() {
                                @Override
                                public void onAddedFriendListener(ArrayList<User> selectedUsers) {
                                    Toast.makeText(getContext(), MyApp.resources.getString(R.string.notification_your_friend_request_sent, selectedUsers.get(0).getName()), Toast.LENGTH_LONG).show();
                                }
                            });
                            return true;
                        case R.id.menu_add_group:
                            parentActivityListener.setReturnFromChildActivity(true);
                            startActivityForResult(new Intent(getActivity(), CreateGroupActivity.class), CREATE_GROUP_INTENT);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popup.show();
            return true;
        }
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList(KEY_CHAT_LIST, mChatList);
//        outState.putInt(KEY_CURRENT_POSITION, mChatListLayoutManager.findFirstCompletelyVisibleItemPosition());
        //TODO: save data
    }

    @Override
    public void onStart() {
        super.onStart();
        LogManagerUtils.d(TAG, "On Start");
        if (!mFirstStart && !parentActivityListener.returnFromChildActivity()) {
            resetData();
            mChatListPresenter.loadChatList(false, mChatList);
        }
        if (!sUnreadChatIdMap.isEmpty()) {
            newNotificationCallback.newChatNotificationDot();
        } else {
            newNotificationCallback.removeChatNotificationDot();
        }
        if (!GroupFragment.sUnreadChatIdMap.isEmpty()) {
            newNotificationCallback.newGroupNotificationDot();
        } else {
            newNotificationCallback.removeGroupNotificationDot();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        LogManagerUtils.d(TAG, "On stop");
        mFirstStart = false;
        if (parentActivityListener != null && !parentActivityListener.returnFromChildActivity()) {
            mChatListPresenter.removeChatListListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogManagerUtils.d(TAG, "On resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        LogManagerUtils.d(TAG, "On pause");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogManagerUtils.d(TAG, "On destroy view");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogManagerUtils.d(TAG, "On destroy");
        mChatListPresenter.detachView();
        mChatListPresenter.removeChatListListener();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CREATE_GROUP_INTENT) {
                if (data.getStringExtra(Constant.EXTRA_CHAT_ID) != null) {
                    String chatGroupId = data.getStringExtra(Constant.EXTRA_CHAT_ID);

                    parentActivityListener.setReturnFromChildActivity(true);
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra(Constant.EXTRA_CHAT_ID, chatGroupId);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void initData() {
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        mChatList = new ArrayList<>();
        rvChatList.setNestedScrollingEnabled(false);
        mChatListLayoutManager = new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false);
        rvChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(mChatList, getActivity(), new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, Chat chat) {
                chat.getNumberUnread().put(ChatUtils.getUser().getId(), 0L);
                mChatListAdapter.notifyItemChanged(chat.getId());
                parentActivityListener.setReturnFromChildActivity(true);
                parentActivityListener.showMessageNotification(true);
                ChatListFragment.sUnreadChatIdMap.remove(chat.getId());
                GroupFragment.sUnreadChatIdMap.remove(chat.getId());
                if (ChatListFragment.sUnreadChatIdMap.isEmpty()) {
                    newNotificationCallback.removeChatNotificationDot();
                }
                if (GroupFragment.sUnreadChatIdMap.isEmpty()) {
                    newNotificationCallback.removeGroupNotificationDot();
                }
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(Constant.EXTRA_CHAT_OBJECT, chat);
                startActivity(intent);

            }
        }, new ChatListAdapter.ChatAdapterFilterListener() {
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
        mTouchCallback = new ItemTouchHelperCallback();
        mItemTouchHelper = new ItemTouchHelperExtension(mTouchCallback);
        mItemTouchHelper.attachToRecyclerView(rvChatList);
        mChatListAdapter.setItemTouchHelperExtension(mItemTouchHelper);
        mChatListAdapter.setOnActionItemClickListener(new ChatListAdapter.OnActionItemClickListener() {
            @Override
            public void onMarkAsRead(final ProgressBar pbProcessing, final TextView tvUnread,
                                     final ImageView ivNotification, final int position, final Chat chatItem) {
                mItemTouchHelper.closeOpened();
                chatItem.setProcessing(true);
                final int tvUnreadVisible = tvUnread.getVisibility();
                final int ivNotificationVisible = ivNotification.getVisibility();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pbProcessing.setVisibility(View.VISIBLE);
                        tvUnread.setVisibility(View.GONE);
                        ivNotification.setVisibility(View.GONE);
                    }
                }, 500);

                Snackbar snackbar = Snackbar.make(getView(),
                        "Conversation: " + chatItem.getChatName() + " was mark as read",
                        Snackbar.LENGTH_LONG);
                snackbar.setAction(android.R.string.cancel, new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        //undo the function
                        chatItem.setProcessing(false);
                        tvUnread.setVisibility(tvUnreadVisible);
                        ivNotification.setVisibility(ivNotificationVisible);
                        pbProcessing.setVisibility(View.GONE);
                    }
                });
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.addCallback(new Snackbar.Callback() {

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        //see Snackbar.Callback docs for event details
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE
                                || event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                            //mark as read
                            mChatPresenter.resetNumberUnread(chatItem.getId(), false);
                        }
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {

                    }
                });
                snackbar.show();
            }

            @Override
            public void onMute(final ProgressBar pbProcessing, final TextView tvUnread,
                               final ImageView ivNotification, final int position, Chat chatItem) {
                mItemTouchHelper.closeOpened();
                chatItem.setProcessing(true);
                final String chatId = chatItem.getId();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pbProcessing.setVisibility(View.VISIBLE);
                        tvUnread.setVisibility(View.GONE);
                        ivNotification.setVisibility(View.GONE);
                        mChatPresenter.setChatNotification(false, chatId);
                    }
                }, 500);
            }

            @Override
            public void onUnmute(final ProgressBar pbProcessing, final TextView tvUnread,
                                 final ImageView ivNotification, final int position, Chat chatItem) {
                mItemTouchHelper.closeOpened();
                chatItem.setProcessing(true);
                final String chatId = chatItem.getId();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pbProcessing.setVisibility(View.VISIBLE);
                        tvUnread.setVisibility(View.GONE);
                        ivNotification.setVisibility(View.GONE);
                        mChatPresenter.setChatNotification(true, chatId);
                    }
                }, 500);
            }

            @Override
            public void onBack() {
                mItemTouchHelper.closeOpened();
            }
        });
    }

    @Override
    public void resetData() {
        mChatList.clear();
        mChatListAdapter.notifyDataSetChanged();
        sUnreadChatIdMap.clear();
        GroupFragment.sUnreadChatIdMap.clear();
//        newNotificationCallback.removeChatNotificationDot();
//        newNotificationCallback.removeGroupNotificationDot();
        mTotalChat = 0;
        mChatCount = 0;
    }

    private void setupPresenter() {
        mChatListPresenter = new ChatListPresenter();
        mChatListPresenter.attachView(this);

        mChatPresenter = new ChatPresenter();
        mChatPresenter.attachView(this);
    }

    @Override
    public void setEvents() {
        swipeRefreshLayout.setDistanceToTriggerSync(250);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetData();
                mChatListPresenter.loadChatList(false, mChatList);
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
                mChatListPresenter.loadChatList(false, mChatList);
            }
        });
    }

    @Override
    public void setChatCount(long count) {
        mTotalChat = count;
    }

    public void showHideNotificationDot(String chatId, long numberUnreadMessage, boolean isGroup) {
        if (numberUnreadMessage > 0) {
            sUnreadChatIdMap.put(chatId, numberUnreadMessage);
            if (isGroup)
                GroupFragment.sUnreadChatIdMap.put(chatId, numberUnreadMessage);
        } else {
            sUnreadChatIdMap.remove(chatId);
            if (isGroup)
                GroupFragment.sUnreadChatIdMap.remove(chatId);
        }

        if (sUnreadChatIdMap.isEmpty()) {
            newNotificationCallback.removeChatNotificationDot();
        } else {
            newNotificationCallback.newChatNotificationDot();
        }
        if (isGroup) {
            if (GroupFragment.sUnreadChatIdMap.isEmpty())
                newNotificationCallback.removeGroupNotificationDot();
            else
                newNotificationCallback.newGroupNotificationDot();
        }
    }

    @Override
    public void showChatList(Chat chat, int position) {
        mChatCount++;
        if (chat != null) {
            if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0 && chat.getNotificationUserIds().get(ChatUtils.getUser().getId())) {
                showHideNotificationDot(chat.getId(), chat.getNumberUnread().get(ChatUtils.getUser().getId()), chat.isGroup());
            }
            showHideListIndicator(llIndicator, false);
            mChatList.add(position, chat);
            mChatListAdapter.notifyItemInserted(position);

            mChatListLayoutManager.scrollToPositionWithOffset(0, 0);
        }
        if (mChatCount == mTotalChat) {
            hideLoadingIndicator();
            mChatListLayoutManager.scrollToPositionWithOffset(0, 0);
            if (mChatList.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                showHideListEmptyIndicator(llIndicator, llEmptyData, true);
            }
        }
    }

    @Override
    public void updateChatStatus(Chat chat, boolean hasNewMessage) {
        try {
            if (mChatList.size() > 0) {
                int index = -1;
                for (int i = 0; i < mChatList.size(); i++) {
                    if (chat.getId().equals(mChatList.get(i).getId())) {
                        index = i;
                        if (chat.getLastMessageSent() == null) {
                            hasNewMessage = false;
                        } else if (mChatList.get(i).getLastMessageSent() == null) {
                            hasNewMessage = true;
                        } else if (!chat.getLastMessageSent().getId().equals(mChatList.get(i).getLastMessageSent().getId())) {
                            hasNewMessage = true;
                        } else {
                            hasNewMessage = false;
                        }
                        mChatList.get(i).cloneChat(chat);
                        break;
                    }
                }

                if (index >= 0) {
                    if (mChatList.get(index).getNotificationUserIds().get(ChatUtils.getUser().getId())) {
                        showHideNotificationDot(mChatList.get(index).getId(),
                                mChatList.get(index).getNumberUnread().get(ChatUtils.getUser().getId()),
                                mChatList.get(index).isGroup());
                    } else {
                        showHideNotificationDot(mChatList.get(index).getId(),
                                0,
                                mChatList.get(index).isGroup());
                    }
                    if (hasNewMessage) {
                        if (index == 0) {
                            mChatListAdapter.notifyItemChanged(chat.getId());
                        } else {
                            int moveToPosition = index;
                            for (int i = 0; i < index; i++) {
                                if (mChatList.get(index).getLastMessageDateInLong() >
                                        mChatList.get(i).getLastMessageDateInLong()) {
                                    moveToPosition = i;
                                    break;
                                }
                            }
                            if (TextUtils.isEmpty(mSearchView.getQuery().toString().trim())) {
                                //nothing in search view, notify item regular
                                mChatListAdapter.notifyItemChanged(index);

                                Chat temp = mChatList.remove(index);
                                mChatList.add(moveToPosition, temp);
                                mChatListAdapter.notifyItemMoved(index, moveToPosition);
                            } else {
                                //searching, use filter adapter notify function
                                mChatListAdapter.notifyItemChanged(mChatList.get(index).getId());

                                Chat temp = mChatList.remove(index);
                                mChatList.add(moveToPosition, temp);
                                mChatListAdapter.notifyItemMoved(temp.getId(), temp.getLastMessageDateInLong());
                            }
                        }
                    } else {
                        mChatListAdapter.notifyItemChanged(mChatList.get(index).getId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateChatNotification(String chatId, boolean turnOn) {
        if (mChatList.size() > 0) {
            int index = -1;
            for (int i = 0; i < mChatList.size(); i++) {
                if (chatId.equals(mChatList.get(i).getId())) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                if (turnOn) {
                    Toast.makeText(getContext(), MyApp.resources.getString(R.string.toast_turn_on_notification_for,
                            mChatList.get(index).getChatName()), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), MyApp.resources.getString(R.string.toast_turn_off_notification_for,
                            mChatList.get(index).getChatName()), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void updateNumberUnreadMessage(String chatId) {
        if (mChatList.size() > 0) {
            int index = -1;
            for (int i = 0; i < mChatList.size(); i++) {
                if (chatId.equals(mChatList.get(i).getId())) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                Toast.makeText(getContext(), MyApp.resources.getString(R.string.toast_conversation_was_mark_as_read,
                        mChatList.get(index).getChatName()), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void hideLoadingIndicator() {
        hideLoadingSwipeLayout(swipeRefreshLayout);
    }

    @Override
    public void showLoadingIndicator() {
        showHideListEmptyIndicator(llIndicator, llEmptyData, false);
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListErrorIndicator(llIndicator, llError, false);
        showLoadingSwipeLayout(swipeRefreshLayout);
    }

    @Override
    public void showEmptyDataIndicator() {
        newNotificationCallback.removeChatNotificationDot();
        newNotificationCallback.removeGroupNotificationDot();
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
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}
