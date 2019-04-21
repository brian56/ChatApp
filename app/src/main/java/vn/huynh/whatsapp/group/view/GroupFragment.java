package vn.huynh.whatsapp.group.view;

import android.app.Activity;
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

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.chat_list.view.ChatListAdapter;
import vn.huynh.whatsapp.group.GroupContract;
import vn.huynh.whatsapp.group.presenter.GroupPresenter;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.User;

public class GroupFragment extends BaseFragment implements GroupContract.View {
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_group_list)
    RecyclerView rvGroupList;
    @BindView(R.id.fab_create_group)
    FloatingActionButton fabCreateGroup;

    private static final int CREATE_GROUP_INTENT = 2;
    private ChatListAdapter groupListAdapter;
    private RecyclerView.LayoutManager groupListLayoutManager;
    private ArrayList<Chat> groupList;

    private GroupPresenter groupPresenter;
    private boolean refreshData = false;

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
        setupPresenter();
        setEvents();
        initializeRecyclerView();
        refreshData = false;
    }

    private void initializeRecyclerView() {
        groupList = new ArrayList<>();
        rvGroupList.setNestedScrollingEnabled(false);
        rvGroupList.setHasFixedSize(false);
        groupListLayoutManager = new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false);
        rvGroupList.setLayoutManager(groupListLayoutManager);
        groupListAdapter = new ChatListAdapter(groupList, getActivity());
        rvGroupList.setAdapter(groupListAdapter);
    }

    private void setupPresenter() {
        groupPresenter = new GroupPresenter();
        groupPresenter.attachView(this);
    }

    private void setEvents() {
        fabCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), CreateGroupActivity.class), CREATE_GROUP_INTENT);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                groupList.clear();
                groupListAdapter.notifyDataSetChanged();
                groupPresenter.loadListGroup(groupList);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CREATE_GROUP_INTENT) {
                if(data.getStringExtra("chatId") != null) {
                    String chatGroupId = data.getStringExtra("chatId");
                    openChat(chatGroupId);
                }
            }
        }
    }

    @Override
    public void showLoadingIndicator() {
        showLoadingIndicator(swipeRefreshLayout);
    }

    @Override
    public void hideLoadingIndicator() {
        hideLoadingIndicator(swipeRefreshLayout);
    }

    @Override
    public void showListGroupEmpty() {

    }

    @Override
    public void showListGroup(int position) {
        groupListAdapter.notifyItemInserted(position);
//        groupListAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateListGroupStatus(Chat chatObject) {
        if (groupList.size() > 0) {
            int i = groupList.indexOf(chatObject);
            if (i > 0) {
                groupList.remove(chatObject);
                groupListAdapter.notifyItemRemoved(i);
                groupList.add(0, chatObject);
                groupListAdapter.notifyItemInserted(0);
            } else if (i == 0) {
                groupListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void showListContact(User userObject) {

    }

    @Override
    public void showErrorMessage(String message) {

    }

    @Override
    public void openChat(String key) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("chatId", key);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        groupList.clear();
        groupListAdapter.notifyDataSetChanged();
        groupPresenter.loadListGroup(groupList);
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
    public void onStop() {
        super.onStop();
        refreshData = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        refreshData = false;
        groupPresenter.detachView();
    }

    /* protected void getUserChatList() {
        DatabaseReference userChatDB = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");
        userChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        boolean exits = false;
                        for (ChatObject chatObjectIterator : groupList) {
                            if(chatObjectIterator.getChatId().equals(childSnapshot.getKey())) {
                                exits = true;
                            }
                        }
                        if(exits || childSnapshot.getKey().toString().length() > 20)
                            continue;

                        ChatObject chat = new ChatObject(childSnapshot.getKey());
                        groupList.add(chat);
                        getChatData(chat.getChatId(), groupList, groupListAdapter);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

/*
    protected void getChatData(String chatId, final ArrayList<ChatObject> chatList, final ChatListAdapter chatListAdapter) {
        final DatabaseReference chatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("info");
        chatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String chatId = "";
                    Boolean isGroup = false;
                    String name = "";
                    if(dataSnapshot.child("id").getValue() != null) {
                        chatId = dataSnapshot.child("id").getValue().toString();
                    }
                    if(dataSnapshot.child("name").getValue() != null) {
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("group").getValue() != null) {
                        isGroup = (Boolean) dataSnapshot.child("group").getValue();
                    }
                    for (DataSnapshot userSnapshot : dataSnapshot.child("users").getChildren()) {
                        for (ChatObject chatObject : chatList) {
                            if(chatObject.getChatId().equals(chatId)) {
                                UserObject userObject = new UserObject(userSnapshot.getKey());
                                chatObject.addUserObjectArrayList(userObject);
                                chatObject.setGroup(isGroup);
                                chatObject.setName(name);
                                getUserData(userObject, chatList, chatListAdapter);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected void getUserData(UserObject user, final ArrayList<ChatObject> chatList, final ChatListAdapter chatListAdapter) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
        userDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject1 = new UserObject(dataSnapshot.getKey());
                if(dataSnapshot.child("name").getValue() != null) {
                    userObject1.setName(dataSnapshot.child("name").getValue().toString());
                }
                if(dataSnapshot.child("notificationKey").getValue() != null) {
                    userObject1.setNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());
                }
                for (ChatObject chatObject : chatList) {
                    for (UserObject userIterator : chatObject.getUserObjectArrayList()) {
                        if(userIterator.getUid().equals(userObject1.getUid())) {
                            userIterator.setName(userObject1.getName());
                            userIterator.setNotificationKey(userObject1.getNotificationKey());
                        }
                    }
                }
                chatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}
