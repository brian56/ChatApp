package vn.huynh.whatsapp.contact_friend.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.contact_friend.contact.view.ContactFragment;
import vn.huynh.whatsapp.contact_friend.friend.view.FriendFragment;
import vn.huynh.whatsapp.utils.MyApp;

/**
 * Created by duong on 5/20/2019.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = ViewPagerAdapter.class.getSimpleName();
    private int mNumberItems;

    public ViewPagerAdapter(FragmentManager fm, int item) {
        super(fm);
        this.mNumberItems = item;
    }

    /**
     * Return fragment with respect to Position .
     */

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FriendFragment();
            case 1:
                return new ContactFragment();
            case 2:
                return new ContactFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return mNumberItems;
    }

    /**
     * This method returns the title of the tab according to the position.
     */

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                String friend = MyApp.resources.getString(R.string.label_friend);
                return friend;
            case 1:
                String contact = MyApp.resources.getString(R.string.label_contact);
                return contact;
            case 2:
                String friendGroup = MyApp.resources.getString(R.string.label_friend_group);
                return friendGroup;
        }
        return null;
    }
}
