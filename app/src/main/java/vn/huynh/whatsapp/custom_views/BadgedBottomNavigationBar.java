package vn.huynh.whatsapp.custom_views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

import vn.huynh.whatsapp.R;

/**
 * Created by duong on 5/9/2019.
 */

public class BadgedBottomNavigationBar extends BottomNavigationView {
    @LayoutRes
    int badgeLayoutResId;

    public BadgedBottomNavigationBar(Context context) {
        super(context);
    }

    public BadgedBottomNavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.badgedBottomNavigationBar);
        badgeLayoutResId = a.getResourceId(R.styleable.badgedBottomNavigationBar_badge_layout, -1);
        a.recycle();
    }

    public BadgedBottomNavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * show the badge on the menu item view.
     *
     * @param menuItemIndex
     */
    public void showBadge(int menuItemIndex) {
        android.support.design.internal.BottomNavigationMenuView bottomNavigationView =
                (android.support.design.internal.BottomNavigationMenuView) getChildAt(0);
        View view = bottomNavigationView.getChildAt(menuItemIndex);
        if (view instanceof ViewGroup) {
            //NUMBER_OF_MENU_ITEM_VIEW_CHILDREN_WITHOUT_BADGE
            if (((ViewGroup) view).getChildCount() > 2)
                return;
//            while (((ViewGroup) view).getChildCount() > 2) {
//                ((ViewGroup) view).removeViewAt(((ViewGroup) view).getChildCount() - 1);
//            }
        }
        android.support.design.internal.BottomNavigationItemView bottomNavigationItemView =
                (android.support.design.internal.BottomNavigationItemView) view;

        LayoutInflater.from(getContext()).inflate(badgeLayoutResId != -1 ? badgeLayoutResId : R.layout.notification_badge, bottomNavigationItemView,
                true);

    }

    /**
     * this method to removeFriends dot [badge view] if it's already inflated on the menu item.
     *
     * @param menuItemIndex the menu item index
     */
    public void removeBadge(int menuItemIndex) {
        android.support.design.internal.BottomNavigationMenuView bottomNavigationMenuView =
                (android.support.design.internal.BottomNavigationMenuView) getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(menuItemIndex);
        // check if the badge is already displayed on the icon.
        if (v instanceof ViewGroup) {
            int childCount = ((ViewGroup) v).getChildCount();
            /* this condition to prevent the inflating the badge more than one time on the
             menu item .. because this means that the badge is already inflated before*/
            // 3 is the NUMBER_OF_MENU_ITEM_VIEW_CHILDERN_WITH_BADGE
            if (childCount < 3) return;
        }
        android.support.design.internal.BottomNavigationItemView itemView = (android.support.design.internal.BottomNavigationItemView) v;
        // removeFriends the last child [badge view]
        itemView.removeViewAt(itemView.getChildCount() - 1);
        int chld = itemView.getChildCount();
        int i = 0;
    }

    public void changeIconSize(int size) {
        android.support.design.internal.BottomNavigationMenuView bottomNavigationView =
                (android.support.design.internal.BottomNavigationMenuView) getChildAt(0);
        for (int i = 0; i < bottomNavigationView.getChildCount(); i++) {
            final View iconView = bottomNavigationView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }
    }

    @SuppressLint("RestrictedApi")
    public void removeShiftMode() {
        android.support.design.internal.BottomNavigationMenuView bottomNavigationView =
                (android.support.design.internal.BottomNavigationMenuView) getChildAt(0);
        try {
            Field shiftingMode = bottomNavigationView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(bottomNavigationView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < bottomNavigationView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) bottomNavigationView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("ERROR NO SUCH FIELD", "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.e("ERROR ILLEGAL ALG", "Unable to change value of shift mode");
        }
    }

    @SuppressLint("RestrictedApi")
    public void removeTextAndShiftMode() {
        android.support.design.internal.BottomNavigationMenuView bottomNavigationView =
                (android.support.design.internal.BottomNavigationMenuView) getChildAt(0);
        try {
            Field shiftingMode = bottomNavigationView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(bottomNavigationView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < bottomNavigationView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) bottomNavigationView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                item.setPadding(0, 16, 0, 0);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }
}
