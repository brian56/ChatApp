package vn.huynh.whatsapp.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 3/23/2019.
 */

public class ChatUtils {
    private static final String TAG = ChatUtils.class.getSimpleName();

    public static User getUser() {
        return SharedPrefsUtil.getInstance().get(Constant.SP_USER_OBJECT, User.class);
    }

    public static void setUser(User userObject) {
        SharedPrefsUtil.getInstance().put(Constant.SP_USER_OBJECT, userObject);
    }

    public static void clearUser() {
        SharedPrefsUtil.getInstance().put(Constant.SP_USER_OBJECT, "");
    }

    public static void setCurrentChatId(String chatId) {
        SharedPrefsUtil.getInstance().put(Constant.SP_CURRENT_CHAT_ID, chatId);
    }

    public static String getCurrentChatId() {
        return SharedPrefsUtil.getInstance().get(Constant.SP_CURRENT_CHAT_ID, String.class);
    }

    private static String getPhoneFromCountryISO(Context context) {
        String iso = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (telephonyManager.getNetworkCountryIso() != null) {
                if (!telephonyManager.getNetworkCountryIso().equalsIgnoreCase("")) {
                    iso = telephonyManager.getNetworkCountryIso();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return IsoToPhone.getPhone(iso);
    }

    public static String formatPhone(String phone, Context context) {
        if (phone != null) {
            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");
            if (!String.valueOf(phone.charAt(0)).equalsIgnoreCase("+")) {
                phone = getPhoneFromCountryISO(context) + phone;
            }
        }
        return phone;
    }

    public static String getSingleChatId(List<String> list) {
        String singleChatId = "";
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1)
                singleChatId += list.get(i);
            else
                singleChatId += list.get(i) + "_";
        }
        return singleChatId;
    }

    public static String getSingleChatId(String userId1, String userId2) {
        String singleChatId = "";
        List<String> list = new ArrayList<>();
        list.add(userId1);
        list.add(userId2);
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1)
                singleChatId += list.get(i);
            else
                singleChatId += list.get(i) + "_";
        }
        return singleChatId;
    }

    public static String getSingleChatIdFomUsers(List<User> userList) {
        List<String> listId = new ArrayList<>();
        for (User user : userList) {
            listId.add(user.getId());
        }
        String singleChatId = "";
        Collections.sort(listId);
        for (int i = 0; i < listId.size(); i++) {
            if (i == listId.size() - 1)
                singleChatId += listId.get(i);
            else
                singleChatId += listId.get(i) + "_";
        }
        return singleChatId;
    }

    public static int generateRandomInteger() {
        Random r = new Random();
        int low = 0;
        int high = Integer.MAX_VALUE;
        return r.nextInt(high - low) + low;
    }
}
