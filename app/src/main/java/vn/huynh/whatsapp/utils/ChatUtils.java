package vn.huynh.whatsapp.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 3/23/2019.
 */

public class ChatUtils {
    public static User user;
    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User userObject) {
        user = new User();
        user.setId(userObject.getId());
        user.setAvatar(userObject.getAvatar());
        user.setChat(userObject.getChat());
        user.setCreateDate(userObject.getCreateDateInLong());
        user.setName(userObject.getName());
        user.setEmail(userObject.getEmail());
        user.setPassword(userObject.getPassword());
        user.setPhoneNumber(userObject.getPhoneNumber());
        user.setFriendList(userObject.getFriendList());
        user.setLastOnline(userObject.getLastOnline());
        user.setNotificationKey(userObject.getNotificationKey());
        user.setStatus(userObject.getStatus());
        user.setSelected(userObject.getSelected());
    }

    public static void clearUser() {
        user = new User();
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
        for (int i = 0; i < list.size(); i++) {
            if(i == list.size() -1)
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
        for (int i = 0; i < listId.size(); i++) {
            if(i == listId.size() -1)
                singleChatId += listId.get(i);
            else
                singleChatId += listId.get(i) + "_";
        }
        return singleChatId;
    }

}
