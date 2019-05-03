package vn.huynh.whatsapp.contact.view;

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
import vn.huynh.whatsapp.contact.ContactContract;
import vn.huynh.whatsapp.contact.presenter.ContactPresenter;
import vn.huynh.whatsapp.model.User;
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
    TashieLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    public static final String TAG = "ContactFragment";

    private RecyclerView.Adapter userListAdapter;
    private RecyclerView.LayoutManager userListLayoutManager;
    private ArrayList<User> userList;

    private boolean firstStart = true;
    private ContactContract.Presenter presenter;

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
        setEvents();
        initializeRecyclerView();
        presenter.loadListContact(getContext());
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
        presenter = new ContactPresenter();
        presenter.attachView(this);
    }

    private void setEvents() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                userList.clear();
                userListAdapter.notifyDataSetChanged();
                presenter.loadListContact(getContext());
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userList.clear();
                userListAdapter.notifyDataSetChanged();
                presenter.loadListContact(getContext());
            }
        });
    }

    private void initializeRecyclerView() {
        userList = new ArrayList<>();
        rvUserList.setNestedScrollingEnabled(false);
        rvUserList.setHasFixedSize(false);
        userListLayoutManager = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false);
        rvUserList.setLayoutManager(userListLayoutManager);
        userListAdapter = new ContactListAdapter(userList, true, presenter);
        rvUserList.setAdapter(userListAdapter);

    }

    @Override
    public void showListContact(User userObject) {
        if(userObject != null) {
            showHideListIndicator(llIndicator, false);
            userList.add(userObject);
            userListAdapter.notifyDataSetChanged();
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
            userList.clear();
            userListAdapter.notifyDataSetChanged();
            presenter.loadListContact(getContext());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        firstStart = false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
