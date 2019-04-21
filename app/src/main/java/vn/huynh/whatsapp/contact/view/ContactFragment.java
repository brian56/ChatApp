package vn.huynh.whatsapp.contact.view;

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

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.contact.ContactContract;
import vn.huynh.whatsapp.contact.presenter.ContactPresenter;
import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 3/23/2019.
 */

public class ContactFragment extends BaseFragment implements ContactContract.View {

    @BindView(R.id.rv_contact_list)
    RecyclerView rvUserList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView.Adapter userListAdapter;
    private RecyclerView.LayoutManager userListLayoutManager;
    private ArrayList<User> userList;

    private boolean refreshData = false;
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
        refreshData = false;
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
                presenter.loadListContact(getActivity());
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
            userList.add(userObject);
            userListAdapter.notifyDataSetChanged();
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
        userList.clear();
        userListAdapter.notifyDataSetChanged();
        presenter.loadListContact(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshData) {
            userList.clear();
            presenter.loadListContact(getContext());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        refreshData = false;
        presenter.detachView();
    }

    @Override
    public void onStop() {
        super.onStop();
        refreshData = true;
    }

    /* private void getContactList() {
        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex((ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
            String phone = phones.getString(phones.getColumnIndex((ContactsContract.CommonDataKinds.Phone.NUMBER)));
            phone = Utils.formatPhone(phone, getActivity().getApplicationContext());
            UserObject contact = new UserObject("", name, phone);
            contactList.add(contact);
            getUserDetail(contact);
        }
    }

    private void getUserDetail(UserObject contact) {
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = userDB.orderByChild("phone").equalTo(contact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "";
                    String name = "";
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.child("phone").getValue() != null) {
                            phone = childSnapshot.child("phone").getValue().toString();
                        }
                        if (childSnapshot.child("name").getValue() != null) {
                            name = childSnapshot.child("name").getValue().toString();
                        }

                        UserObject user = new UserObject(childSnapshot.getKey(), name, phone);
                        if (name.equalsIgnoreCase(phone)) {
                            for (UserObject contact : contactList) {
                                if (Utils.formatPhone(contact.getPhone(), getActivity().getApplicationContext()).equalsIgnoreCase(user.getPhone())) {
                                    user.setName(contact.getName());
                                }
                            }
                        }
                        userList.add(user);
                        userListAdapter.notifyDataSetChanged();
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}
