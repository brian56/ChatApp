package vn.huynh.whatsapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import com.google.firebase.database.Exclude;

import vn.huynh.whatsapp.custom_views.sticky_header.stickyData.StickyMainData;

/**
 * Created by duong on 5/18/2019.
 */
@Keep
public class Friend implements Parcelable, StickyMainData {
    private String userId;
    private Object createDate;
    private String name;
    private String avatar;
    private String message;
    private String phoneNumber;
    private int status = STATUS_REQUEST;
    private Boolean isRegisteredUser = false;

    public static final int STATUS_DEFAULT = 100;
    public static final int STATUS_WAS_REQUESTED = 1;
    public static final int STATUS_WAS_ACCEPTED = 2;
    public static final int STATUS_ACCEPT = 3;
    public static final int STATUS_REQUEST = 4;
    public static final int STATUS_BLOCK = 5;
    public static final int STATUS_WAS_REJECTED = 6;
    public static final int STATUS_REJECT = 7;
    public static final int STATUS_WAS_BLOCKED = 8;
    public static final int STATUS_WAS_UNFRIEND = 9;
    public static final int STATUS_CANCEL = 10;
    public static final int STATUS_WAS_CANCELED = 11;
    public static final int STATUS_INVITE = 50;

    public Friend() {

    }

    protected Friend(Parcel in) {
        userId = in.readString();
        name = in.readString();
        avatar = in.readString();
        message = in.readString();
        phoneNumber = in.readString();
        status = in.readInt();
        createDate = in.readLong();

        byte tmpUser = in.readByte();
        isRegisteredUser = tmpUser == 0 ? null : tmpUser == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(message);
        dest.writeString(phoneNumber);
        dest.writeInt(status);
        dest.writeLong((long) createDate);

        dest.writeByte((byte) (isRegisteredUser ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
    public void cloneFriend(Friend friend) {
        this.userId = friend.getUserId();
        this.phoneNumber = friend.getPhoneNumber();
        this.name = friend.getName();
        this.avatar = friend.getAvatar();
        this.message = friend.getMessage();
        this.status = friend.getStatus();
        this.createDate = friend.getCreateDateInLong();
        this.isRegisteredUser = friend.getRegisteredUser();
    }
}
