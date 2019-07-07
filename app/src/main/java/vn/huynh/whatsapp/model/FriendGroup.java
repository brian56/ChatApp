package vn.huynh.whatsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by duong on 4/15/2019.
 */

public class FriendGroup implements Parcelable {
    private String id;
    private String name;
    private Object createDate;
    private Map<String, Object> member = new HashMap<>();

    protected FriendGroup(Parcel in) {
        id = in.readString();
        name = in.readString();
        createDate = in.readLong();

        member = new HashMap<>();
        in.readMap(member, Long.class.getClassLoader());
    }

    public static final Creator<FriendGroup> CREATOR = new Creator<FriendGroup>() {
        @Override
        public FriendGroup createFromParcel(Parcel in) {
            return new FriendGroup(in);
        }

        @Override
        public FriendGroup[] newArray(int size) {
            return new FriendGroup[size];
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
        dest.writeLong((long) createDate);

        if (member != null) {
            dest.writeMap(member);
        } else {
            member = new HashMap<>();
            dest.writeMap(member);
        }
    }

    public FriendGroup() {

    }

    public FriendGroup(String id) {
        this.id = id;
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

    public Map<String, Object> getMember() {
        return member;
    }

    public void setMember(Map<String, Object> member) {
        this.member = member;
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
}
