package vn.huynh.whatsapp.group.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseActivity;
import vn.huynh.whatsapp.contact_friend.contact.ContactContract;
import vn.huynh.whatsapp.contact_friend.contact.presenter.ContactPresenter;
import vn.huynh.whatsapp.contact_friend.contact.view.ContactListAdapter;
import vn.huynh.whatsapp.group.GroupContract;
import vn.huynh.whatsapp.group.presenter.GroupPresenter;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;

public class CreateGroupActivity extends BaseActivity implements GroupContract.View {

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_user_list)
    RecyclerView rvUserList;
    @BindView(R.id.btn_create_chat_room)
    Button btnCreateChatRoom;
    @BindView(R.id.edt_group_name)
    EditText edtGroupName;
    @BindView(R.id.ll_indicator)
    LinearLayout llIndicator;
    @BindView(R.id.loader)
    CircularDotsLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    private static final String TAG = CreateGroupActivity.class.getSimpleName();
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;
    private ArrayList<User> mUserList;
    private ArrayList<User> mSelectedUserList;

    private GroupContract.Presenter mGroupPresenter;
    private ContactContract.Presenter mContactPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create group");

        setupPresenter();
        initData();
        setEvents();
        mUserList.clear();
        mContactPresenter.loadListContactForGroup(this);
    }

    private void setupPresenter() {
        mGroupPresenter = new GroupPresenter();
        mGroupPresenter.attachView(this);
        mContactPresenter = new ContactPresenter();
        mContactPresenter.attachView(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setEvents() {
        btnCreateChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = false;
                for (User userObject : mUserList) {
                    if (userObject.getSelected()) {
                        mSelectedUserList.add(userObject);
                        isSelected = true;
                    }
                }
                if (isSelected) {
                    User currentUser = new User(ChatUtils.getUser().getId());
                    mSelectedUserList.add(0, currentUser);
                    mGroupPresenter.createGroupChat(edtGroupName.getText().toString().trim(), mSelectedUserList);
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Please select at least 1 user to create group", Toast.LENGTH_SHORT).show();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mUserList.clear();
                mUserListAdapter.notifyDataSetChanged();
                mContactPresenter.loadListContactForGroup(CreateGroupActivity.this);
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserList.clear();
                mUserListAdapter.notifyDataSetChanged();
                mContactPresenter.loadListContactForGroup(CreateGroupActivity.this);
            }
        });
    }

    @Override
    public void initData() {
        initializeRecyclerView();
    }

    @Override
    public void resetData() {

    }

    private void initializeRecyclerView() {
        mUserList = new ArrayList<>();
        mSelectedUserList = new ArrayList<>();

        rvUserList.setNestedScrollingEnabled(false);
        rvUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        rvUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new ContactListAdapter(mUserList, false, true, false);
        rvUserList.setAdapter(mUserListAdapter);

    }

    @Override
    public void showLoadingIndicator() {
        showHideListEmptyIndicator(llIndicator, llEmptyData, false);
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListErrorIndicator(llIndicator, llError, false);
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideLoadingIndicator() {
        swipeRefreshLayout.setRefreshing(false);
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
    public void setChatCount(long count) {

    }

    @Override
    public void showChatList(Chat chat, int position) {

    }

    @Override
    public void showErrorMessage(String message) {

    }

    @Override
    public void updateChatStatus(Chat chatObject, boolean hasNewMessage) {

    }

    @Override
    public void updateChatNotification(String chatId, boolean turnOn) {

    }

    @Override
    public void updateNumberUnreadMessage(String chatId) {

    }

    @Override
    public void openChat(String key) {
        mSelectedUserList.clear();
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constant.EXTRA_CHAT_ID, key);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void showListContact(User userObject) {
        if (userObject != null && userObject.getRegisteredUser()) {
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
            hideLoadingIndicator();
            mUserListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGroupPresenter.detachView();
        mContactPresenter.detachView();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
