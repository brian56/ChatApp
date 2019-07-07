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
import android.util.Log;
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

    final FragmentManager fm = getSupportFragmentManager();
    private Fragment chatListFragment = new ChatListFragment();
    private Fragment contactAndFriendFragment = new ContactAndFriendFragment();
    private Fragment groupFragment = new GroupFragment();
    private Fragment settingFragment = new SettingFragment();

    private static final int CHAT_LIST_FRAGMENT_INDEX = 0;
    private static final int CONTACT_AND_FRIEND_FRAGMENT_INDEX = 1;
    private static final int GROUP_FRAGMENT_INDEX = 2;
    private static final int SETTING_FRAGMENT_INDEX = 3;

    private String currentFragmentTAG = ChatListFragment.TAG;
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener;
    private boolean returnFromChildActivity = false;
    private static boolean isVisible = false;
    private NewMessageService newMessageService;
    private boolean isBound = false;
    private Intent intentNewMessageService;

    private FriendPresenter friendPresenter;

    public static int NUMBER_NOTIFICATION_ALL = 0;
    public static int NUMBER_NOTIFICATION_GROUP = 0;
    public static int NUMBER_NOTIFICATION_CONTACT = 0;
    public static int NUMBER_NOTIFICATION_SETTING = 0;

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
        Log.d(HomeActivity.class.getSimpleName(), "On Create");
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
        if (chatListFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, ChatListFragment.TAG, chatListFragment);
        if (contactAndFriendFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, ContactAndFriendFragment.TAG, contactAndFriendFragment);
        if (groupFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, GroupFragment.TAG, groupFragment);
        if (settingFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, SettingFragment.TAG, settingFragment);
        outState.putString(KEY_CURRENT_FRAGMENT_TAG, currentFragmentTAG);
    }

    private void initFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            chatListFragment = new ChatListFragment();
            contactAndFriendFragment = new ContactAndFriendFragment();
            groupFragment = new GroupFragment();
            settingFragment = new SettingFragment();

            loadFragment(chatListFragment, ChatListFragment.TAG);
            currentFragmentTAG = ChatListFragment.TAG;
        } else {
            if (getSupportFragmentManager().getFragment(savedInstanceState, ChatListFragment.TAG) != null) {
                chatListFragment = getSupportFragmentManager().getFragment(savedInstanceState, ChatListFragment.TAG);
            }
            if (getSupportFragmentManager().getFragment(savedInstanceState, ContactAndFriendFragment.TAG) != null) {
                contactAndFriendFragment = getSupportFragmentManager().getFragment(savedInstanceState, ContactAndFriendFragment.TAG);
            }
            if (getSupportFragmentManager().getFragment(savedInstanceState, GroupFragment.TAG) != null) {
                groupFragment = getSupportFragmentManager().getFragment(savedInstanceState, GroupFragment.TAG);
            }
            if (getSupportFragmentManager().getFragment(savedInstanceState, SettingFragment.TAG) != null) {
                settingFragment = getSupportFragmentManager().getFragment(savedInstanceState, SettingFragment.TAG);
            }

            currentFragmentTAG = savedInstanceState.getString(KEY_CURRENT_FRAGMENT_TAG);
            if (currentFragmentTAG == null) {
                currentFragmentTAG = ChatListFragment.TAG;
            }
            switch (currentFragmentTAG) {
                case ChatListFragment.TAG:
                    loadFragment(chatListFragment, ChatListFragment.TAG);
                    break;
                case ContactAndFriendFragment.TAG:
                    loadFragment(contactAndFriendFragment, ContactAndFriendFragment.TAG);
                    break;
                case GroupFragment.TAG:
                    loadFragment(groupFragment, GroupFragment.TAG);
                    break;
                case SettingFragment.TAG:
                    loadFragment(settingFragment, SettingFragment.TAG);
                    break;
                default:
                    loadFragment(chatListFragment, ChatListFragment.TAG);
                    break;
            }
        }
    }

    private void setEvent() {
        navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                invalidateOptionsMenu();
                switch (item.getItemId()) {
                    case R.id.navigation_chat:
                        toolbar.setTitle(getResources().getString(R.string.menu_chat));
//                        ChatListFragment.unreadChatIdMap.clear();
//                        removeBadgeNumber(CHAT_LIST_FRAGMENT_INDEX);
                        currentFragmentTAG = ChatListFragment.TAG;
                        loadFragment(chatListFragment, ChatListFragment.TAG);
                        return true;
                    case R.id.navigation_contact:
                        toolbar.setTitle(getResources().getString(R.string.menu_contact_and_friend));
//                        removeBadgeNumber(CONTACT_AND_FRIEND_FRAGMENT_INDEX);
                        currentFragmentTAG = ContactAndFriendFragment.TAG;
                        loadFragment(contactAndFriendFragment, ContactAndFriendFragment.TAG);
                        return true;
                    case R.id.navigation_group:
                        toolbar.setTitle(getResources().getString(R.string.menu_group));
//                        removeBadgeNumber(GROUP_FRAGMENT_INDEX);
                        currentFragmentTAG = GroupFragment.TAG;
                        loadFragment(groupFragment, GroupFragment.TAG);
                        return true;
                    case R.id.navigation_setting:
                        toolbar.setTitle(getResources().getString(R.string.menu_setting));
//                        removeBadgeNumber(SETTING_FRAGMENT_INDEX);
                        currentFragmentTAG = SettingFragment.TAG;
                        loadFragment(settingFragment, SettingFragment.TAG);
                        return true;
                }
                return false;
            }
        };
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    private void loadFragment(Fragment fragment, String TAG) {
        // load fragment
        FragmentTransaction transaction = fm.beginTransaction();
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.frame_container, fragment, TAG);
        }
        hideOtherFragments(transaction, TAG);
    }

    private void hideOtherFragments(FragmentTransaction transaction, String TAG) {
        if (!ChatListFragment.TAG.equals(TAG)) {
            transaction.hide(chatListFragment);
        } else {
            transaction.show(chatListFragment);
        }
        if (!ContactAndFriendFragment.TAG.equals(TAG)) {
            transaction.hide(contactAndFriendFragment);
        } else {
            transaction.show(contactAndFriendFragment);
        }
        if (!GroupFragment.TAG.equals(TAG)) {
            transaction.hide(groupFragment);
        } else {
            transaction.show(groupFragment);
        }
        if (!SettingFragment.TAG.equals(TAG)) {
            transaction.hide(settingFragment);
        } else {
            transaction.show(settingFragment);
        }
        transaction.commit();
    }

    private void setupBottomNavigation(BadgedBottomNavigationBar badgedBottomNavigationBar) {
        badgedBottomNavigationBar.removeTextAndShiftMode();//disable BottomNavigationView shift mode
        badgedBottomNavigationBar.changeIconSize(24);
    }

    private void initPresenter() {
        friendPresenter = new FriendPresenter();
        friendPresenter.attachView(this);
        friendPresenter.listenerFriendNotification();
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
        return returnFromChildActivity;
    }

    @Override
    public void setReturnFromChildActivity(boolean returnFromChildActivity) {
        this.returnFromChildActivity = returnFromChildActivity;
    }

    @Override
    public void showMessageNotification(boolean show) {
        if (newMessageService != null) {
            newMessageService.setShowMessageNotification(show);
        }
    }

    @Override
    public void showFriendNotification(boolean show) {
        if (newMessageService != null) {
            newMessageService.setShowFriendNotification(show);
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
        return isVisible;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            newMessageService = binder.getService();
            Log.d("Noti_MainActivity", "setShowMessageNotification()");
            newMessageService.setShowMessageNotification(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(HomeActivity.class.getSimpleName(), "On Start");

        if (!isBound) {
            intentNewMessageService = new Intent(this, NewMessageService.class);
            if (!ServiceUtils.isServiceRunning(NewMessageService.class.getCanonicalName(), this)) {
                startService(intentNewMessageService);
            }
            bindService(intentNewMessageService, serviceConnection, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
        if (newMessageService != null) {
            Log.d("Home onStop()", "setShowMessageNotification()");
            newMessageService.setShowMessageNotification(false);
        }
        isVisible = true;
        initPresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (returnFromChildActivity()) {
            setReturnFromChildActivity(false);
        }
        Log.d(HomeActivity.class.getSimpleName(), "On Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(HomeActivity.class.getSimpleName(), "On Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
        if (newMessageService != null) {
            Log.d("Home onStop()", "setShowMessageNotification()");
            newMessageService.setShowMessageNotification(true);
        }
        Log.d(HomeActivity.class.getSimpleName(), "On Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            if (newMessageService != null)
                newMessageService.setShowMessageNotification(true);
            unbindService(serviceConnection);
            isBound = false;
            Log.d(HomeActivity.class.getSimpleName(), "unbind service");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(HomeActivity.class.getSimpleName(), "On Restart");
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
