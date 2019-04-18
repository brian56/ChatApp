package vn.huynh.whatsapp.home;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.chat_list.view.ChatListFragment;
import vn.huynh.whatsapp.contact.view.ContactFragment;
import vn.huynh.whatsapp.group.view.GroupFragment;
import vn.huynh.whatsapp.setting.SettingFragment;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.frame_container)
    FrameLayout frameLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    final FragmentManager fm = getSupportFragmentManager();


    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        Fresco.initialize(this);

        setupOneSignal();

        getPermission();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat");

        loadFragment(new ChatListFragment());

        navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                invalidateOptionsMenu();
                switch (item.getItemId()) {
                    case R.id.navigation_chat:
                        toolbar.setTitle("Chat");
                        fragment = new ChatListFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_contact:
                        toolbar.setTitle("Contact");
                        fragment = new ContactFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_group:
                        toolbar.setTitle("Group");
                        fragment = new GroupFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_setting:
                        toolbar.setTitle("Setting");
                        fragment = new SettingFragment();
                        loadFragment(fragment);
                        return true;
                }
                return false;
            }
        };
        navigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        Log.d(HomeActivity.class.getSimpleName(), "On Create");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(HomeActivity.class.getSimpleName(), "On Start");
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        Log.d(HomeActivity.class.getSimpleName(), "On Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(HomeActivity.class.getSimpleName(), "On Destroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(HomeActivity.class.getSimpleName(), "On Restart");
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
//        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupOneSignal() {
        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("notificationKey").setValue(userId);

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
