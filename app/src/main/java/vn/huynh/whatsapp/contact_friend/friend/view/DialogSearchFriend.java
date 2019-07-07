package vn.huynh.whatsapp.contact_friend.friend.view;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.rilixtech.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.contact_friend.contact.view.ContactListAdapter;
import vn.huynh.whatsapp.contact_friend.friend.FriendContract;
import vn.huynh.whatsapp.contact_friend.friend.presenter.FriendPresenter;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.MyApp;

/**
 * Created by duong on 7/5/2019.
 */

public class DialogSearchFriend implements FriendContract.ViewSearchFriend {

    @BindView(R.id.ll_indicator)
    LinearLayout llIndicator;
    @BindView(R.id.loader)
    CircularDotsLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    @BindView(R.id.edt_phone_number)
    EditText edtPhoneNumber;
    @BindView(R.id.rv_search_result)
    RecyclerView rvSearchResult;
    @BindView(R.id.btn_add_friend)
    Button btnAddFriend;
    @BindView(R.id.ccp)
    CountryCodePicker countryCodePicker;
    @BindView(R.id.iv_search)
    ImageView ivSearch;
    @BindView(R.id.loader_add_friend)
    CircularDotsLoader addFriendLoader;

    private Context context;
    Dialog dialog;
    private String phoneNumber;
    private SearchFriendListener searchFriendListener;
    private ContactListAdapter userListAdapter;
    private LinearLayoutManager linearLayoutManager;

    private ArrayList<User> userArrayList;
    private ArrayList<User> selectedUsers;
    private User user;

    private FriendPresenter friendPresenter;

    public DialogSearchFriend(Context context) {
        this.context = context;
    }

    public interface SearchFriendListener {
        void onAddedFriendListener(ArrayList<User> selectedUsers);
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public void show(SearchFriendListener searchFriendListener) {
        this.searchFriendListener = searchFriendListener;

        dialog = new Dialog(context, R.style.Dialog);
        View view = View.inflate(context, R.layout.dialog_search, null);
        dialog.setTitle(context.getResources().getString(R.string.title_search_friend));
        dialog.setContentView(view);
        ButterKnife.bind(this, view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initData();

        dialog.show();
        initPresenter();
        setupEvent();
    }

    private void initPresenter() {
        friendPresenter = new FriendPresenter();
        friendPresenter.attachView(this);
    }

    private void initData() {
        phoneNumber = "";
        selectedUsers = new ArrayList<>();
        userArrayList = new ArrayList<>();

        rvSearchResult.setNestedScrollingEnabled(false);
        rvSearchResult.setHasFixedSize(false);
        linearLayoutManager = new LinearLayoutManager(context, LinearLayout.VERTICAL, false);
        rvSearchResult.setLayoutManager(linearLayoutManager);

        userListAdapter = new ContactListAdapter(userArrayList, false);
        rvSearchResult.setAdapter(userListAdapter);
        hideLoadingIndicator();

        edtPhoneNumber.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setupEvent() {
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    selectedUsers.clear();
                    for (User user : userArrayList) {
                        if (user.getSelected()) {
                            selectedUsers.add(user);
                        }
                    }
                    if (!selectedUsers.isEmpty()) {
                        friendPresenter.addFriends(selectedUsers);
                        btnAddFriend.setVisibility(View.INVISIBLE);
                        addFriendLoader.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(context, MyApp.resources.getString(R.string.error_please_select_one_user), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = edtPhoneNumber.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(context, MyApp.resources.getString(R.string.error_invalid_phone_number), Toast.LENGTH_LONG).show();
                } else {
                    formatPhoneNumber(phoneNumber);
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    phoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() + phoneNumber;
                    userArrayList.clear();
                    userListAdapter.notifyDataSetChanged();
                    friendPresenter.searchFriendByPhoneNumber(phoneNumber);
                }
            }
        });
    }

    @Override
    public void showLoadingIndicator() {
        llIndicator.setVisibility(View.VISIBLE);
        loader.setVisibility(View.VISIBLE);
        llEmptyData.setVisibility(View.GONE);
        llError.setVisibility(View.GONE);
    }

    @Override
    public void hideLoadingIndicator() {
        llIndicator.setVisibility(View.GONE);
        loader.setVisibility(View.GONE);
        llEmptyData.setVisibility(View.GONE);
        llError.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyDataIndicator() {
        llIndicator.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        llEmptyData.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);
    }

    @Override
    public void showErrorIndicator() {
        llIndicator.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        llEmptyData.setVisibility(View.GONE);
        llError.setVisibility(View.VISIBLE);
    }

    @Override
    public void showErrorMessage(String error) {

    }

    @Override
    public void showFriendList(List<User> users) {
        if (users != null && !users.isEmpty()) {
            hideLoadingIndicator();
            rvSearchResult.setVisibility(View.VISIBLE);
            userArrayList.addAll(users);
            userListAdapter.notifyDataSetChanged();
        } else {
            rvSearchResult.setVisibility(View.GONE);
            showEmptyDataIndicator();
        }
    }

    @Override
    public void addFriendSuccess(String message) {
        dialog.dismiss();
        if (searchFriendListener != null) {
            searchFriendListener.onAddedFriendListener(selectedUsers);
        }
    }

    @Override
    public void addFriendFail(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        btnAddFriend.setVisibility(View.VISIBLE);
        addFriendLoader.setVisibility(View.GONE);
    }

    private void formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.charAt(0) == '0') {
            phoneNumber = phoneNumber.substring(1);
        }
    }
}

