package vn.huynh.whatsapp.contact_friend.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseFragment;

/**
 * Created by duong on 5/20/2019.
 */

public class ContactAndFriendFragment extends BaseFragment {
    public static final String TAG = "ContactAndFriendFragment";

    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    public static final int NUMBER_ITEM = 3;


    public ContactAndFriendFragment() {

    }

    public static ContactAndFriendFragment newInstance() {

        Bundle args = new Bundle();

        ContactAndFriendFragment fragment = new ContactAndFriendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact_and_friend, container, false);
        ButterKnife.bind(this, rootView);
        setupViewPager();
        return rootView;
    }

    private void setupViewPager() {
        viewPager.setOffscreenPageLimit(NUMBER_ITEM);
        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), NUMBER_ITEM));
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });
    }
}
