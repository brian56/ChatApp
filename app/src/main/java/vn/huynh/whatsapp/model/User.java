package vn.huynh.whatsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by duong on 4/15/2019.
 */

public class User implements Parcelable {
    private String id;
    private String name;
    private String phoneNumber;
    private String email;
    private String password;
    private String notificationKey;
    private Object lastOnline;
    private Object createDate;
    private String avatar;
    private String lastChatId;
    private int status = STATUS_ONLINE;
    private Map<String, Long> chat;
    private Map<String, Long> friendList;
    private Boolean selected = false;

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_BUSY = 2;
    public static final int STATUS_DELETED = -1;
    public static final int STATUS_BANNED = -2;

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        password = in.readString();
        avatar = in.readString();
        lastChatId = in.readString();
        notificationKey = in.readString();
        lastOnline = in.readLong();
        status = in.readInt();
        createDate = in.readLong();

        byte tmpSelected = in.readByte();
        selected = tmpSelected == 0 ? null : tmpSelected == 1;

        chat = new HashMap<>();
        in.readMap(chat, Long.class.getClassLoader());

        friendList = new HashMap<>();
        in.readMap(friendList, Long.class.getClassLoader());

        /*int chatSize = in.readInt();
        this.chat = new HashMap<>(chatSize);
        for (int i = 0; i < chatSize; i++) {
            String key = in.readString();
            Long value = in.readLong();
            this.chat.put(key, value);
        }

        int friendListSize = in.readInt();
        this.friendList = new HashMap<>(friendListSize);
        for (int i = 0; i < friendListSize; i++) {
            String key = in.readString();
            Long value = in.readLong();
            this.friendList.put(key, value);
        }*/
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(avatar);
        dest.writeString(lastChatId);
        dest.writeString(notificationKey);
        dest.writeLong((long) lastOnline);
        dest.writeInt(status);
        dest.writeLong((long) createDate);
        dest.writeByte((byte) (selected ? 1 : 0));

        if (chat != null) {
            dest.writeMap(chat);
        } else {
            chat = new HashMap<>();
            dest.writeMap(chat);
        }
        if (friendList != null) {
            dest.writeMap(friendList);
        } else {
            friendList = new HashMap<>();
            dest.writeMap(friendList);
        }
        /*if(this.friendList != null) {
            dest.writeInt(this.chat.size());
            for (Map.Entry<String, Long> entry : this.chat.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeLong(entry.getValue());
            }
        }

        if(this.friendList != null) {
            dest.writeInt(this.friendList.size());
            for (Map.Entry<String, Long> entry : this.friendList.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeLong(entry.getValue());
            }
        }*/

    }

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

    public String getId() {
        return id;
    }

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

    public Object getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Object lastOnline) {
        this.lastOnline = lastOnline;
    }

    @Exclude
    public long getLastOnlineInLong() {
        return (long) lastOnline;
    }

    public Object getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Object createDate) {
        this.createDate = createDate;
    }

    @Exclude
    public long getCreateDateInLong() {
        return (long) createDate;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLastChatId() {
        return lastChatId;
    }

    public void setLastChatId(String lastChatId) {
        this.lastChatId = lastChatId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, Long> getChat() {
        return chat;
    }

    public void setChat(Map<String, Long> chat) {
        this.chat = chat;
    }

    public Map<String, Long> getFriendList() {
        return friendList;
    }

    public void setFriendList(Map<String, Long> friendList) {
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
