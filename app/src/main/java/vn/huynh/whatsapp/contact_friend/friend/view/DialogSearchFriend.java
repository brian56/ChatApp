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
    private static final String TAG = DialogSearchFriend.class.getSimpleName();
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

    private Context mContext;
    Dialog mDialog;
    private String mPhoneNumber;
    private SearchFriendListener mSearchFriendListener;
    private ContactListAdapter mUserListAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    private ArrayList<User> mUserArrayList;
    private ArrayList<User> mSelectedUsers;

    private FriendPresenter mFriendPresenter;

    public DialogSearchFriend(Context context) {
        this.mContext = context;
    }

    public interface SearchFriendListener {
        void onAddedFriendListener(ArrayList<User> selectedUsers);
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public void show(SearchFriendListener searchFriendListener) {
        this.mSearchFriendListener = searchFriendListener;

        mDialog = new Dialog(mContext, R.style.Dialog);
        View view = View.inflate(mContext, R.layout.dialog_search, null);
        mDialog.setTitle(mContext.getResources().getString(R.string.title_search_friend));
        mDialog.setContentView(view);
        ButterKnife.bind(this, view);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initData();

        mDialog.show();
        initPresenter();
        setEvents();
    }

    private void initPresenter() {
        mFriendPresenter = new FriendPresenter();
        mFriendPresenter.attachView(this);
    }

    @Override
    public void initData() {
        mPhoneNumber = "";
        mSelectedUsers = new ArrayList<>();
        mUserArrayList = new ArrayList<>();

        rvSearchResult.setNestedScrollingEnabled(false);
        rvSearchResult.setHasFixedSize(false);
        mLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayout.VERTICAL, false);
        rvSearchResult.setLayoutManager(mLinearLayoutManager);

        mUserListAdapter = new ContactListAdapter(mContext, mUserArrayList, false, true, true);
        rvSearchResult.setAdapter(mUserListAdapter);
        hideLoadingIndicator();

        edtPhoneNumber.requestFocus();
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void setEvents() {
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mSelectedUsers.clear();
                    for (User user : mUserArrayList) {
                        if (user.getSelected()) {
                            mSelectedUsers.add(user);
                        }
                    }
                    if (!mSelectedUsers.isEmpty()) {
                        mFriendPresenter.addFriends(mSelectedUsers);
                        btnAddFriend.setVisibility(View.INVISIBLE);
                        addFriendLoader.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(mContext, MyApp.resources.getString(R.string.error_please_select_one_user), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhoneNumber = edtPhoneNumber.getText().toString().trim();
                if (TextUtils.isEmpty(mPhoneNumber)) {
                    Toast.makeText(mContext, MyApp.resources.getString(R.string.error_invalid_phone_number), Toast.LENGTH_LONG).show();
                } else {
                    mPhoneNumber = formatPhoneNumber(mPhoneNumber);
                    mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    mPhoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() + mPhoneNumber;
                    mUserArrayList.clear();
                    mUserListAdapter.notifyDataSetChanged();
                    mFriendPresenter.searchFriendByPhoneNumber(mPhoneNumber);
                }
            }
        });
    }

    @Override
    public void resetData() {

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
            mUserArrayList.clear();
            mUserArrayList.addAll(users);
            mUserListAdapter.notifyDataSetChanged();
        } else {
            mUserArrayList.clear();
            mUserListAdapter.notifyDataSetChanged();
            rvSearchResult.setVisibility(View.GONE);
            showEmptyDataIndicator();
        }
    }

    @Override
    public void addFriendSuccess(String message) {
        mDialog.dismiss();
        if (mSearchFriendListener != null) {
            mSearchFriendListener.onAddedFriendListener(mSelectedUsers);
        }
    }

    @Override
    public void addFriendFail(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        btnAddFriend.setVisibility(View.VISIBLE);
        addFriendLoader.setVisibility(View.GONE);
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.charAt(0) == '0') {
            phoneNumber = phoneNumber.substring(1);
        }
        return phoneNumber;
    }
}

