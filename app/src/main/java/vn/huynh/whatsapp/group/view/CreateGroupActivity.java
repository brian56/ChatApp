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
    private RecyclerView.Adapter userListAdapter;
    private RecyclerView.LayoutManager userListLayoutManager;
    private ArrayList<User> userList;
    private ArrayList<User> selectedUserList;

    private GroupContract.Presenter groupPresenter;
    private ContactContract.Presenter contactPresenter;

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
        initializeRecyclerView();
        setEvents();
        userList.clear();
        contactPresenter.loadListContactForGroup(this);
    }

    private void setupPresenter() {
        groupPresenter = new GroupPresenter();
        groupPresenter.attachView(this);
        contactPresenter = new ContactPresenter();
        contactPresenter.attachView(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEvents() {
        btnCreateChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = false;
                for (User userObject : userList) {
                    if (userObject.getSelected()) {
                        selectedUserList.add(userObject);
                        isSelected = true;
                    }
                }
                if (isSelected) {
                    User currentUser = new User(ChatUtils.getUser().getId());
                    selectedUserList.add(0, currentUser);
                    groupPresenter.createGroupChat(edtGroupName.getText().toString().trim(), selectedUserList);
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Please select at least 1 user to create group", Toast.LENGTH_SHORT).show();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                userList.clear();
                userListAdapter.notifyDataSetChanged();
                contactPresenter.loadListContactForGroup(CreateGroupActivity.this);
            }
        });
        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userList.clear();
                userListAdapter.notifyDataSetChanged();
                contactPresenter.loadListContactForGroup(CreateGroupActivity.this);
            }
        });
    }

    private void initializeRecyclerView() {
        userList = new ArrayList<>();
        selectedUserList = new ArrayList<>();

        rvUserList.setNestedScrollingEnabled(false);
        rvUserList.setHasFixedSize(false);
        userListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        rvUserList.setLayoutManager(userListLayoutManager);
        userListAdapter = new ContactListAdapter(userList, false);
        rvUserList.setAdapter(userListAdapter);

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
    public void openChat(String key) {
        selectedUserList.clear();
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constant.EXTRA_CHAT_ID, key);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void showListContact(User userObject) {
        if (userObject != null && userObject.getRegisteredUser()) {
            userList.add(userObject);
            Collections.sort(userList, new Comparator<User>() {
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
            userListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        groupPresenter.detachView();
        contactPresenter.detachView();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
