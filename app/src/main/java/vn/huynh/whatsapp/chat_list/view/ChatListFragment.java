package vn.huynh.whatsapp.chat_list.view;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
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
    private ChatListContract.Presenter mChatListPresenter = new ChatListPresenter();
    private SearchView mSearchView;

    private static final int CREATE_GROUP_INTENT = 100;
    private static final String KEY_CHAT_LIST = "KEY_CHAT_LIST";
    private static final String KEY_CURRENT_POSITION = "KEY_CURRENT_POSITION";

    private long mTotalChat = 0;
    private long mChatCount = 0;
    private boolean mFirstStart = true;
    public static Map<String, Long> sUnreadChatIdMap = new HashMap<>();

    private ItemTouchHelper.SimpleCallback mTouchCallback;
    private ItemTouchHelper mItemTouchHelper;
//    private boolean returnFromChatActivity = false;

    public ChatListFragment() {
    }

    public static ChatListFragment newInstance() {
        return new ChatListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        initializeRecyclerView();
        setupPresenter();
        setEvents();
        resetDataBeforeReload();
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

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        mSearchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
//        mSearchView.setIconifiedByDefault(false);
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

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
            //TODO: show popup add friend or group
            View menuItemView = getActivity().findViewById(R.id.action_add_friend_or_group);
            PopupMenu popup = new PopupMenu(getContext(), menuItemView);
            popup.inflate(R.menu.menu_popup_add_friend_or_group);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_add_friend:
                            //TODO: show add friend dialog
                            DialogSearchFriend dialogSearchFriend = new DialogSearchFriend(getContext());
                            dialogSearchFriend.show(new DialogSearchFriend.SearchFriendListener() {
                                @Override
                                public void onAddedFriendListener(ArrayList<User> selectedUsers) {
                                    showErrorMessage(MyApp.resources.getString(R.string.notification_your_friend_request_sent, selectedUsers.get(0).getName()));
                                }
                            });
                            return true;
                        case R.id.menu_add_group:
                            //TODO: show create group activity
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

    private void initializeRecyclerView() {
        mChatList = new ArrayList<>();
        rvChatList.setNestedScrollingEnabled(false);
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
        mTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                try {
                    final int position = viewHolder.getAdapterPosition();
                    final Chat chatItem = mChatList.get(position);
                    final String chatName = chatItem.getChatName();
                    mChatList.remove(position);
                    mChatListAdapter.notifyItemRemoved(position);
                    Snackbar snackbar = Snackbar.make(viewHolder.itemView, "Chat: " + chatName + (direction == ItemTouchHelper.RIGHT ? " was deleted" : " was muted"), Snackbar.LENGTH_LONG);
                    snackbar.setAction(android.R.string.cancel, new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            try {
                                mChatList.add(position, chatItem);
                                mChatListAdapter.notifyItemInserted(position);
                                mChatListLayoutManager.scrollToPositionWithOffset(position, 0);
                            } catch (Exception e) {
                                LogManagerUtils.e(TAG, e.getMessage());
                            }
                        }
                    });
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.addCallback(new Snackbar.Callback() {

                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            //see Snackbar.Callback docs for event details
                            if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE) {
                                //TODO:
                            }
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {

                        }
                    });
                    snackbar.show();
                } catch (Exception e) {
                    LogManagerUtils.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(getContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                        .addSwipeLeftActionIcon(R.drawable.ic_notifications_off_white_24dp)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                        .addSwipeRightActionIcon(R.drawable.ic_delete_white_24dp)
                        .addSwipeRightLabel(getString(R.string.action_delete))
                        .setSwipeRightLabelColor(Color.WHITE)
                        .addSwipeLeftLabel(getString(R.string.action_mute))
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        mItemTouchHelper = new ItemTouchHelper(mTouchCallback);
        mItemTouchHelper.attachToRecyclerView(rvChatList);
    }

    private void resetDataBeforeReload() {
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
    }

    private void setEvents() {
        swipeRefreshLayout.setDistanceToTriggerSync(250);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetDataBeforeReload();
                mChatListPresenter.loadChatList(false, mChatList);
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetDataBeforeReload();
                mChatListPresenter.loadChatList(false, mChatList);
            }
        });
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
        //TODO: attach the listener for chat list items
        if (!mFirstStart && !parentActivityListener.returnFromChildActivity()) {
            resetDataBeforeReload();
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
        //TODO: removeFriends the listener for chat list items
        mFirstStart = false;
        if (parentActivityListener != null && !parentActivityListener.returnFromChildActivity()) {
            mChatListPresenter.removeChatListListener();
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
        mChatListPresenter.detachView();
        mChatListPresenter.removeChatListListener();
    }

    @Override
    public void setChatCount(long count) {
        mTotalChat = count;
    }

    @Override
    public void showChatList(Chat chat, int position) {
        if (chat != null) {
            mChatCount++;
            if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0) {
                if (sUnreadChatIdMap.isEmpty()) {
                    newNotificationCallback.newChatNotificationDot();
                }
                sUnreadChatIdMap.put(chat.getId(), 1L);
                if (chat.isGroup()) {
                    if (GroupFragment.sUnreadChatIdMap.isEmpty()) {
                        newNotificationCallback.newGroupNotificationDot();
                    }
                    GroupFragment.sUnreadChatIdMap.put(chat.getId(), 1L);
                }
            }
            showHideListIndicator(llIndicator, false);
            mChatList.add(position, chat);
            mChatListAdapter.notifyItemInserted(position);
            mChatListLayoutManager.scrollToPositionWithOffset(0, 0);
            if (mChatCount == mTotalChat) {
                hideLoadingIndicator();
//                mChatListAdapter.notifyDataSetChanged();
                mChatListLayoutManager.scrollToPositionWithOffset(0, 0);
                if (mChatList.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                    showHideListEmptyIndicator(llIndicator, llEmptyData, true);
                }
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
                            newNotificationCallback.newChatNotificationDot();
                            if (chat.isGroup()) {
                                GroupFragment.sUnreadChatIdMap.put(chat.getId(), 1L);
                                newNotificationCallback.newGroupNotificationDot();
                            }
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
                                newNotificationCallback.removeChatNotificationDot();
                            if (chat.isGroup()) {
                                GroupFragment.sUnreadChatIdMap.remove(chat.getId());
                                if (GroupFragment.sUnreadChatIdMap.isEmpty())
                                    newNotificationCallback.removeGroupNotificationDot();
                            }
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
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
