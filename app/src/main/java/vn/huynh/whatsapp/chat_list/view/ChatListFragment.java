package vn.huynh.whatsapp.chat_list.view;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.chat_list.ChatListContract;
import vn.huynh.whatsapp.chat_list.presenter.ChatListPresenter;
import vn.huynh.whatsapp.model.Chat;

/**
 * Created by duong on 4/2/2019.
 */

public class ChatListFragment extends BaseFragment implements ChatListContract.View {

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_chat_list)
    RecyclerView rvChatList;
    private boolean refreshData = false;

    private ChatListAdapter chatListAdapter;
    private RecyclerView.LayoutManager chatListLayoutManager;
    private ArrayList<Chat> chatList;
    private ChatListContract.Presenter presenter;

    public ChatListFragment() {
    }

    public static ChatListFragment newInstance() {
        ChatListFragment chatFragment = new ChatListFragment();
        return chatFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeRecyclerView();
        setupPresenter();
        setEvents();
        refreshData = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.loadChatList(chatList);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshData) {
            presenter.loadChatList(chatList);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        refreshData = true;
        presenter.removeListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        refreshData = false;
        presenter.detachView();
    }

    private void setupPresenter() {
        presenter = new ChatListPresenter();
        presenter.attachView(this);
    }

    private void setEvents() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadChatList(chatList);
            }
        });
    }

    private void initializeRecyclerView() {
        chatList = new ArrayList<>();
        rvChatList.setNestedScrollingEnabled(false);
        rvChatList.setHasFixedSize(false);
        chatListLayoutManager = new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false);
        rvChatList.setLayoutManager(chatListLayoutManager);
        chatListAdapter = new ChatListAdapter(chatList, getActivity());
        rvChatList.setAdapter(chatListAdapter);

    }

    @Override
    public void showChatListEmpty() {

    }

    @Override
    public void showChatList(List<Chat> chatObjects) {
        chatListAdapter.notifyDataSetChanged();
    }

    @Override
    public void hideLoadingIndicator() {
        hideLoadingIndicator(swipeRefreshLayout);
    }

    @Override
    public void showLoadingIndicator() {
        showLoadingIndicator(swipeRefreshLayout);
    }

    @Override
    public void showErrorMessage(String message) {

    }
}
