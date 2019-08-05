package vn.huynh.whatsapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by duong on 4/15/2019.
 */
@Keep
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
    private int friendNotification;
    private int status = STATUS_ONLINE;
    private Map<String, Long> chat;
    private Map<String, Integer> friend;
    private Map<String, Long> friendGroup;
    private Boolean selected = false;
    private Boolean isRegisteredUser = true;
    private int friendStatus = Friend.STATUS_DEFAULT;

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_BUSY = 2;
    public static final int STATUS_INVISIBLE = 3;
    public static final int STATUS_DELETED = -1;
    public static final int STATUS_INACTIVE = -2;
    public static final int STATUS_BANNED = -10;

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        password = in.readString();
        avatar = in.readString();
        lastChatId = in.readString();
        friendNotification = in.readInt();
        notificationKey = in.readString();
        lastOnline = in.readLong();
        status = in.readInt();
        friendStatus = in.readInt();
        createDate = in.readLong();

        byte tmpSelected = in.readByte();
        selected = tmpSelected == 0 ? null : tmpSelected == 1;

        byte tmpUser = in.readByte();
        isRegisteredUser = tmpUser == 0 ? null : tmpUser == 1;

        chat = new HashMap<>();
        in.readMap(chat, Long.class.getClassLoader());

        friend = new HashMap<>();
        in.readMap(chat, Integer.class.getClassLoader());

        friendGroup = new HashMap<>();
        in.readMap(friendGroup, Long.class.getClassLoader());

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
        dest.writeInt(friendNotification);
        dest.writeString(notificationKey);
        dest.writeLong((long) lastOnline);
        dest.writeInt(status);
        dest.writeInt(friendStatus);
        dest.writeLong((long) createDate);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (isRegisteredUser ? 1 : 0));

        if (chat != null) {
            dest.writeMap(chat);
        } else {
            chat = new HashMap<>();
            dest.writeMap(chat);
        }

        if (friend != null) {
            dest.writeMap(friend);
        } else {
            friend = new HashMap<>();
            dest.writeMap(friend);
        }

        if (friendGroup != null) {
            dest.writeMap(friendGroup);
        } else {
            friendGroup = new HashMap<>();
            dest.writeMap(friendGroup);
        }
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

    public int getFriendNotification() {
        return friendNotification;
    }

    public void setFriendNotification(int friendNotification) {
        this.friendNotification = friendNotification;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Exclude
    public int getFriendStatus() {
        return friendStatus;
    }

    @Exclude
    public void setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
    }

    public Map<String, Long> getChat() {
        return chat;
    }

    public void setChat(Map<String, Long> chat) {
        this.chat = chat;
    }

    public Map<String, Integer> getFriend() {
        return friend;
    }

    public void setFriend(Map<String, Integer> friend) {
        this.friend = friend;
    }

    public Map<String, Long> getFriendGroup() {
        return friendGroup;
    }

    public void setFriendGroup(Map<String, Long> friendGroup) {
        this.friendGroup = friendGroup;
    }

    @Exclude
    public Boolean getSelected() {
        return selected;
    }

    @Exclude
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    @Exclude
    public Boolean getRegisteredUser() {
        return isRegisteredUser;
    }

    @Exclude
    public void setRegisteredUser(Boolean registeredUser) {
        isRegisteredUser = registeredUser;
    }

    @Exclude
    public void cloneUser(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.createDate = user.getCreateDateInLong();
        this.avatar = user.getAvatar();
        this.notificationKey = user.getNotificationKey();
        this.lastOnline = user.getLastOnline();
        this.lastChatId = user.getLastChatId();
        this.friendNotification = user.getFriendNotification();
        this.status = user.getStatus();
        this.friendStatus = user.getFriendStatus();
        this.chat = user.getChat();
        this.friend = user.getFriend();
        this.friendGroup = user.getFriendGroup();
        this.selected = user.getSelected();
        this.isRegisteredUser = user.getRegisteredUser();
    }
}
