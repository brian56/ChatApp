package vn.huynh.whatsapp.chat_list.view;

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

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.chat_list.ChatListContract;
import vn.huynh.whatsapp.chat_list.presenter.ChatListPresenter;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.utils.Constant;

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
    TashieLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    public static final String TAG = "ChatListFragment";

    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager chatListLayoutManager;
    private ArrayList<Chat> chatList;
    private ChatListContract.Presenter presenter = new ChatListPresenter();

    private static final String KEY_CHAT_LIST = "KEY_CHAT_LIST";
    private static final String KEY_CURRENT_POSITION = "KEY_CURRENT_POSITION";

    private int currentItemPosition = 0;
    private boolean firstStart = true;
//    private boolean returnFromChatActivity = false;

    public ChatListFragment() {
    }

    public static ChatListFragment newInstance() {
        return new ChatListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
//        chatList.clear();
//        chatListAdapter.notifyDataSetChanged();
        presenter.loadChatList(false, chatList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if(savedInstanceState != null) {
//            chatList = savedInstanceState.getParcelableArrayList(KEY_CHAT_LIST);
//            currentItemPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
//            chatListAdapter.setChatList(chatList);
//            chatListAdapter.notifyDataSetChanged();
//            chatListLayoutManager.scrollToPositionWithOffset(currentItemPosition, 0);
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
    }

    private void setupPresenter() {
        presenter = new ChatListPresenter();
        presenter.attachView(this);
    }

    private void setEvents() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatList.clear();
                chatListAdapter.notifyDataSetChanged();
                presenter.loadChatList(false, chatList);
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatList.clear();
                chatListAdapter.notifyDataSetChanged();
                presenter.loadChatList(false, chatList);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList(KEY_CHAT_LIST, chatList);
//        outState.putInt(KEY_CURRENT_POSITION, chatListLayoutManager.findFirstCompletelyVisibleItemPosition());
        //TODO: save data
    }

    @Override
    public void onStart() {
        super.onStart();
        //TODO: attach the listener for chat list items
        if (!firstStart && !parentActivityListener.returnFromChildActivity()) {
            chatList.clear();
            chatListAdapter.notifyDataSetChanged();
            presenter.loadChatList(false, chatList);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //TODO: remove the listener for chat list items
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

    private void initializeRecyclerView() {
        chatList = new ArrayList<>();
        rvChatList.setNestedScrollingEnabled(false);
        chatListLayoutManager = new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false);
        rvChatList.setLayoutManager(chatListLayoutManager);
        chatListAdapter = new ChatListAdapter(chatList, getActivity(), new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Chat chat) {
                parentActivityListener.setReturnFromChildActivity(true);
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(Constant.EXTRA_CHAT_OBJECT, chat);
                startActivity(intent);
            }
        });
        rvChatList.setAdapter(chatListAdapter);

    }

    @Override
    public void showChatList(Chat chat, int position) {
        if (chat != null) {
            showHideListIndicator(llIndicator, false);
            chatList.add(position, chat);
            try {
                chatListAdapter.notifyItemInserted(position);
                chatListLayoutManager.scrollToPositionWithOffset(0, 0);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateChatListStatus(Chat chatObject) {
        try {
            if (chatList.size() > 0) {
                int i = chatList.indexOf(chatObject);
                if (i > 0) {
                    chatList.remove(chatObject);
                    chatListAdapter.notifyItemRemoved(i);
                    chatList.add(0, chatObject);
                    chatListAdapter.notifyItemInserted(0);
                } else if (i == 0) {
                    chatListAdapter.notifyDataSetChanged();
                }
                chatListLayoutManager.scrollToPositionWithOffset(0, 0);
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
}
