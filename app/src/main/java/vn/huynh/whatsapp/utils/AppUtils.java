package vn.huynh.whatsapp.utils;

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
}
