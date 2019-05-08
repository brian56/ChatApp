package vn.huynh.whatsapp.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.home.HomeActivity;

/**
 * Created by duong on 4/25/2019.
 */

public class AppUtils {
    public static boolean isAppVisible() {
        if (HomeActivity.checkVisible() || ChatActivity.checkVisible()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * open soft keyboard.
     *
     * @param context
     * @param view
     */
    public static void showKeyBoard(Context context, View view) {
        try {
            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(view, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * close soft keyboard.
     *
     * @param context
     * @param view
     */
    public static void hideKeyBoard(Context context, View view) {
        try {
            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
