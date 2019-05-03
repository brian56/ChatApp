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
import vn.huynh.whatsapp.contact.view.ContactFragment;
import vn.huynh.whatsapp.group.view.GroupFragment;
import vn.huynh.whatsapp.services.NewMessageService;
import vn.huynh.whatsapp.setting.SettingFragment;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.ServiceUtils;

public class HomeActivity extends AppCompatActivity implements BaseFragment.ParentActivityListener {

    @BindView(R.id.frame_container)
    FrameLayout frameLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation)
    BottomNavigationView navigation;
    private static final String TAG = HomeActivity.class.getSimpleName();

    final FragmentManager fm = getSupportFragmentManager();
    private Fragment chatListFragment = new ChatListFragment();
    private Fragment contactFragment = new ContactFragment();
    private Fragment groupFragment = new GroupFragment();
    private Fragment settingFragment = new SettingFragment();

    private String currentFragmentTAG = ChatListFragment.TAG;
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener;
    private boolean returnFromChildActivity = false;
    private static boolean isVisible = false;
    private NewMessageService newMessageService;
    private boolean isBound = false;

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

        initFragments(savedInstanceState);
        setEvent();
        Log.d(HomeActivity.class.getSimpleName(), "On Create");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (chatListFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, ChatListFragment.TAG, chatListFragment);
        if (contactFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, ContactFragment.TAG, contactFragment);
        if (groupFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, GroupFragment.TAG, groupFragment);
        if (settingFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, SettingFragment.TAG, settingFragment);
        outState.putString(KEY_CURRENT_FRAGMENT_TAG, currentFragmentTAG);
    }

    private void initFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            chatListFragment = new ChatListFragment();
            contactFragment = new ContactFragment();
            groupFragment = new GroupFragment();
            settingFragment = new SettingFragment();

            loadFragment(chatListFragment, ChatListFragment.TAG);
            currentFragmentTAG = ChatListFragment.TAG;
        } else {
            if (getSupportFragmentManager().getFragment(savedInstanceState, ChatListFragment.TAG) != null) {
                chatListFragment = getSupportFragmentManager().getFragment(savedInstanceState, ChatListFragment.TAG);
            }
            if (getSupportFragmentManager().getFragment(savedInstanceState, ContactFragment.TAG) != null) {
                contactFragment = getSupportFragmentManager().getFragment(savedInstanceState, ContactFragment.TAG);
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
                case ContactFragment.TAG:
                    loadFragment(contactFragment, ContactFragment.TAG);
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
                        currentFragmentTAG = ChatListFragment.TAG;
                        loadFragment(chatListFragment, ChatListFragment.TAG);
                        return true;
                    case R.id.navigation_contact:
                        toolbar.setTitle(getResources().getString(R.string.menu_contact));
                        currentFragmentTAG = ContactFragment.TAG;
                        loadFragment(contactFragment, ContactFragment.TAG);
                        return true;
                    case R.id.navigation_group:
                        toolbar.setTitle(getResources().getString(R.string.menu_group));
                        currentFragmentTAG = GroupFragment.TAG;
                        loadFragment(groupFragment, GroupFragment.TAG);
                        return true;
                    case R.id.navigation_setting:
                        toolbar.setTitle(getResources().getString(R.string.menu_setting));
                        currentFragmentTAG = SettingFragment.TAG;
                        loadFragment(settingFragment, SettingFragment.TAG);
                        return true;
                }
                return false;
            }
        };
        navigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
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
        if (!ContactFragment.TAG.equals(TAG)) {
            transaction.hide(contactFragment);
        } else {
            transaction.show(contactFragment);
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

    @Override
    public boolean returnFromChildActivity() {
        return returnFromChildActivity;
    }

    @Override
    public void setReturnFromChildActivity(boolean returnFromChildActivity) {
        this.returnFromChildActivity = returnFromChildActivity;
    }

    public static boolean checkVisible() {
        return isVisible;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            newMessageService = binder.getService();
            Log.d("Noti_MainActivity", "setShowNotification()");
            newMessageService.setShowNotification(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
//        Intent intent = new Intent(HomeActivity.this, PushStateOnlineService.class);
//        startService(intent);
        if (newMessageService != null) {
            Log.d("Home onStop()", "setShowNotification()");
            newMessageService.setShowNotification(false);
        }
        if (!ServiceUtils.isServiceRunning(NewMessageService.class.getCanonicalName(), getApplicationContext())) {
            Intent intent2 = new Intent(this, NewMessageService.class);
            startService(intent2);
            bindService(intent2, serviceConnection, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
        isVisible = true;
        Log.d(HomeActivity.class.getSimpleName(), "On Start");
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
            Log.d("Home onStop()", "setShowNotification()");
            newMessageService.setShowNotification(true);
        }
        Log.d(HomeActivity.class.getSimpleName(), "On Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            if (newMessageService != null)
                newMessageService.removeListener();
            unbindService(serviceConnection);
            isBound = false;
        }
        Log.d(HomeActivity.class.getSimpleName(), "On Destroy");
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
                FirebaseDatabase.getInstance().getReference().child("user").child(ChatUtils.getCurrentUserId()).child("notificationKey").setValue(userId);

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
