package vn.huynh.whatsapp.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by duong on 4/15/2019.
 */

public class User implements Serializable {
    private String id;
    private String name;
    private String phoneNumber;
    private String email;
    private String password;
    private String notificationKey;
    private long lastOnline;
    private Object createDate;
    private String avatar;
    private int status = STATUS_OFFLINE;
    private Map<String, Object> chat;
    private Map<String, Object> friendList;

    private static final int STATUS_ONLINE = 1;
    private static final int STATUS_OFFLINE = 0;
    private static final int STATUS_BUSY = -1;
    private static final int STATUS_BANNED = -2;
    private static final int STATUS_DELETED = -3;

    private Boolean selected = false;

    public User() {

    }

    public User(String id) {
        this.id = id;
    }

    public User(String id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public Object getCreateDate() {
        return createDate;
    }

    @Exclude
    public long getCreateDateInLong() {
        return (long) createDate;
    }

    public void setCreateDate(Object createDate) {
        this.createDate = createDate;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, Object> getChat() {
        return chat;
    }

    public void setChat(Map<String, Object> chat) {
        this.chat = chat;
    }

    public Map<String, Object> getFriendList() {
        return friendList;
    }

    public void setFriendList(Map<String, Object> friendList) {
        this.friendList = friendList;
    }

    @Exclude
    public Boolean getSelected() {
        return selected;
    }

    @Exclude
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
