package vn.huynh.whatsapp.home;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;
import vn.huynh.whatsapp.chat_list.view.ChatListFragment;
import vn.huynh.whatsapp.contact_friend.friend.presenter.FriendPresenter;
import vn.huynh.whatsapp.contact_friend.view.ContactAndFriendFragment;
import vn.huynh.whatsapp.custom_views.BadgedBottomNavigationBar;
import vn.huynh.whatsapp.group.view.GroupFragment;
import vn.huynh.whatsapp.services.NewMessageService;
import vn.huynh.whatsapp.setting.SettingFragment;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.LogManagerUtils;
import vn.huynh.whatsapp.utils.ServiceUtils;

public class HomeActivity extends AppCompatActivity implements BaseFragment.ParentActivityListener,
        BaseFragment.NewNotificationCallback {

    @BindView(R.id.frame_container)
    FrameLayout frameLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation)
    BadgedBottomNavigationBar bottomNavigationView;

    private static final String TAG = HomeActivity.class.getSimpleName();

    final FragmentManager mFragmentManager = getSupportFragmentManager();
    private Fragment mChatListFragment = new ChatListFragment();
    private Fragment mContactAndFriendFragment = new ContactAndFriendFragment();
    private Fragment mGroupFragment = new GroupFragment();
    private Fragment mSettingFragment = new SettingFragment();

    private static final int CHAT_LIST_FRAGMENT_INDEX = 0;
    private static final int CONTACT_AND_FRIEND_FRAGMENT_INDEX = 1;
    private static final int GROUP_FRAGMENT_INDEX = 2;
    private static final int SETTING_FRAGMENT_INDEX = 3;

    private String mCurrentFragmentTAG = ChatListFragment.TAG;
    private BottomNavigationView.OnNavigationItemSelectedListener mNavigationItemSelectedListener;
    private boolean mIsReturnFromChildActivity = false;
    private static boolean sIsVisible = false;
    private NewMessageService mNewMessageService;
    private boolean mIsBoundService = false;
    private Intent mIntentNewMessageService;

    private FriendPresenter mFriendPresenter;

    private static final String KEY_CURRENT_FRAGMENT_TAG = "KEY_CURRENT_FRAGMENT_TAG";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        Fresco.initialize(this);

        setupOneSignal();

        getPermission();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.menu_chat));
        setupBottomNavigation(bottomNavigationView);
        initFragments(savedInstanceState);
        setEvent();

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(Constant.EXTRA_FRIEND_ID) != null) {
                goToContactAndFriendList();
            }

        }
        LogManagerUtils.d(TAG, "On Create");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        setIntent(intent);
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.getString(Constant.EXTRA_FRIEND_ID) != null) {
                    goToContactAndFriendList();
                }
            }
        }
    }

    private void goToContactAndFriendList() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_contact);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mChatListFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, ChatListFragment.TAG, mChatListFragment);
        if (mContactAndFriendFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, ContactAndFriendFragment.TAG, mContactAndFriendFragment);
        if (mGroupFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, GroupFragment.TAG, mGroupFragment);
        if (mSettingFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, SettingFragment.TAG, mSettingFragment);
        outState.putString(KEY_CURRENT_FRAGMENT_TAG, mCurrentFragmentTAG);
    }

    private void initFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mChatListFragment = new ChatListFragment();
            mContactAndFriendFragment = new ContactAndFriendFragment();
            mGroupFragment = new GroupFragment();
            mSettingFragment = new SettingFragment();

            loadFragment(mChatListFragment, ChatListFragment.TAG);
            mCurrentFragmentTAG = ChatListFragment.TAG;
        } else {
            if (getSupportFragmentManager().getFragment(savedInstanceState, ChatListFragment.TAG) != null) {
                mChatListFragment = getSupportFragmentManager().getFragment(savedInstanceState, ChatListFragment.TAG);
            }
            if (getSupportFragmentManager().getFragment(savedInstanceState, ContactAndFriendFragment.TAG) != null) {
                mContactAndFriendFragment = getSupportFragmentManager().getFragment(savedInstanceState, ContactAndFriendFragment.TAG);
            }
            if (getSupportFragmentManager().getFragment(savedInstanceState, GroupFragment.TAG) != null) {
                mGroupFragment = getSupportFragmentManager().getFragment(savedInstanceState, GroupFragment.TAG);
            }
            if (getSupportFragmentManager().getFragment(savedInstanceState, SettingFragment.TAG) != null) {
                mSettingFragment = getSupportFragmentManager().getFragment(savedInstanceState, SettingFragment.TAG);
            }

            mCurrentFragmentTAG = savedInstanceState.getString(KEY_CURRENT_FRAGMENT_TAG);
            if (mCurrentFragmentTAG == null) {
                mCurrentFragmentTAG = ChatListFragment.TAG;
            }
            switch (mCurrentFragmentTAG) {
                case ChatListFragment.TAG:
                    loadFragment(mChatListFragment, ChatListFragment.TAG);
                    break;
                case ContactAndFriendFragment.TAG:
                    loadFragment(mContactAndFriendFragment, ContactAndFriendFragment.TAG);
                    break;
                case GroupFragment.TAG:
                    loadFragment(mGroupFragment, GroupFragment.TAG);
                    break;
                case SettingFragment.TAG:
                    loadFragment(mSettingFragment, SettingFragment.TAG);
                    break;
                default:
                    loadFragment(mChatListFragment, ChatListFragment.TAG);
                    break;
            }
        }
    }

    private void setEvent() {
        mNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                invalidateOptionsMenu();
                switch (item.getItemId()) {
                    case R.id.navigation_chat:
                        toolbar.setTitle(getResources().getString(R.string.menu_chat));
//                        ChatListFragment.sUnreadChatIdMap.clear();
//                        removeBadgeNumber(CHAT_LIST_FRAGMENT_INDEX);
                        mCurrentFragmentTAG = ChatListFragment.TAG;
                        loadFragment(mChatListFragment, ChatListFragment.TAG);
                        return true;
                    case R.id.navigation_contact:
                        toolbar.setTitle(getResources().getString(R.string.menu_contact_and_friend));
//                        removeBadgeNumber(CONTACT_AND_FRIEND_FRAGMENT_INDEX);
                        mCurrentFragmentTAG = ContactAndFriendFragment.TAG;
                        loadFragment(mContactAndFriendFragment, ContactAndFriendFragment.TAG);
                        return true;
                    case R.id.navigation_group:
                        toolbar.setTitle(getResources().getString(R.string.menu_group));
//                        removeBadgeNumber(GROUP_FRAGMENT_INDEX);
                        mCurrentFragmentTAG = GroupFragment.TAG;
                        loadFragment(mGroupFragment, GroupFragment.TAG);
                        return true;
                    case R.id.navigation_setting:
                        toolbar.setTitle(getResources().getString(R.string.menu_setting));
//                        removeBadgeNumber(SETTING_FRAGMENT_INDEX);
                        mCurrentFragmentTAG = SettingFragment.TAG;
                        loadFragment(mSettingFragment, SettingFragment.TAG);
                        return true;
                }
                return false;
            }
        };
        bottomNavigationView.setOnNavigationItemSelectedListener(mNavigationItemSelectedListener);
    }

    private void loadFragment(Fragment fragment, String TAG) {
        // load fragment
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.frame_container, fragment, TAG);
        }
        hideOtherFragments(transaction, TAG);
    }

    private void hideOtherFragments(FragmentTransaction transaction, String TAG) {
        if (!ChatListFragment.TAG.equals(TAG)) {
            transaction.hide(mChatListFragment);
        } else {
            transaction.show(mChatListFragment);
        }
        if (!ContactAndFriendFragment.TAG.equals(TAG)) {
            transaction.hide(mContactAndFriendFragment);
        } else {
            transaction.show(mContactAndFriendFragment);
        }
        if (!GroupFragment.TAG.equals(TAG)) {
            transaction.hide(mGroupFragment);
        } else {
            transaction.show(mGroupFragment);
        }
        if (!SettingFragment.TAG.equals(TAG)) {
            transaction.hide(mSettingFragment);
        } else {
            transaction.show(mSettingFragment);
        }
        transaction.commit();
    }

    private void setupBottomNavigation(BadgedBottomNavigationBar badgedBottomNavigationBar) {
//        badgedBottomNavigationBar.removeTextAndShiftMode();//disable BottomNavigationView shift mode
        badgedBottomNavigationBar.changeIconSize(24);
    }

    private void initPresenter() {
        mFriendPresenter = new FriendPresenter();
        mFriendPresenter.attachView(this);
        mFriendPresenter.listenerFriendNotification();
    }

    @Override
    public void newChatNotificationDot() {
        bottomNavigationView.showBadge(CHAT_LIST_FRAGMENT_INDEX);
    }

    @Override
    public void removeChatNotificationDot() {
        bottomNavigationView.removeBadge(CHAT_LIST_FRAGMENT_INDEX);
    }

    @Override
    public void newGroupNotificationDot() {
        bottomNavigationView.showBadge(GROUP_FRAGMENT_INDEX);
    }

    @Override
    public void removeGroupNotificationDot() {
        bottomNavigationView.removeBadge(GROUP_FRAGMENT_INDEX);
    }

    @Override
    public void newContactNotificationDot() {
        bottomNavigationView.showBadge(CONTACT_AND_FRIEND_FRAGMENT_INDEX);
    }

    @Override
    public void removeContactNotificationDot() {
        bottomNavigationView.removeBadge(CONTACT_AND_FRIEND_FRAGMENT_INDEX);
    }

    @Override
    public void newSettingNotificationDot() {
        bottomNavigationView.showBadge(SETTING_FRAGMENT_INDEX);
    }

    @Override
    public void removeSettingNotification() {
        bottomNavigationView.removeBadge(SETTING_FRAGMENT_INDEX);
    }

    @Override
    public boolean returnFromChildActivity() {
        return mIsReturnFromChildActivity;
    }

    @Override
    public void setReturnFromChildActivity(boolean returnFromChildActivity) {
        this.mIsReturnFromChildActivity = returnFromChildActivity;
    }

    @Override
    public void showMessageNotification(boolean show) {
        if (mNewMessageService != null) {
            mNewMessageService.setShowMessageNotification(show);
        }
    }

    @Override
    public void showFriendNotification(boolean show) {
        if (mNewMessageService != null) {
            mNewMessageService.setmIsShowFriendNotification(show);
        }
    }

    @Override
    public void showHideFriendDot(int showNotify) {
        if (showNotify == 0) {
            removeContactNotificationDot();
        } else {
            newContactNotificationDot();
        }
    }

    public static boolean checkVisible() {
        return sIsVisible;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            mNewMessageService = binder.getService();
            LogManagerUtils.d(TAG, "setShowMessageNotification()");
            mNewMessageService.setShowMessageNotification(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LogManagerUtils.d(TAG, "On Start");

        if (!mIsBoundService) {
            mIntentNewMessageService = new Intent(this, NewMessageService.class);
            if (!ServiceUtils.isServiceRunning(NewMessageService.class.getCanonicalName(), this)) {
                startService(mIntentNewMessageService);
            }
            bindService(mIntentNewMessageService, serviceConnection, Context.BIND_AUTO_CREATE);
            mIsBoundService = true;
        }
        if (mNewMessageService != null) {
            LogManagerUtils.d(TAG, "onStop setShowMessageNotification()");
            mNewMessageService.setShowMessageNotification(false);
        }
        sIsVisible = true;
        initPresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (returnFromChildActivity()) {
            setReturnFromChildActivity(false);
        }
        LogManagerUtils.d(TAG, "On Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogManagerUtils.d(TAG, "On Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        sIsVisible = false;
        if (mNewMessageService != null) {
            LogManagerUtils.d(TAG, "onStop() " + "setShowMessageNotification()");
            mNewMessageService.setShowMessageNotification(true);
        }
        LogManagerUtils.d(TAG, "On Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogManagerUtils.d(TAG, "On Destroy");
        if (mIsBoundService) {
            if (mNewMessageService != null)
                mNewMessageService.setShowMessageNotification(true);
            unbindService(serviceConnection);
            mIsBoundService = false;
            LogManagerUtils.d(TAG, "unbind service");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogManagerUtils.d(TAG, "On Restart");
    }

    private void setupOneSignal() {
        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(ChatUtils.getUser().getId()).child("notificationKey").setValue(userId);

            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
    }

    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
        }
    }
}
