package vn.huynh.whatsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by duong on 4/15/2019.
 */

public class Message implements Parcelable {
    private String id;
    private String creator;
    private String text;
    private int status = STATUS_SENDING;
    private Object createDate;
    private Map<String, Long> seenUsers;
    private Map<String, String> media;

    public static final int STATUS_SENDING = 1;
    public static final int STATUS_DELETED = -1;
    public static final int STATUS_DELIVERED = 2;

    protected Message(Parcel in) {
        id = in.readString();
        creator = in.readString();
        text = in.readString();
        status = in.readInt();
        createDate = in.readLong();

        seenUsers = new HashMap<>();
        in.readMap(seenUsers, Long.class.getClassLoader());

        media = new HashMap<>();
        in.readMap(media, String.class.getClassLoader());

        /*int seenUsersSize = in.readInt();
        this.seenUsers = new HashMap<>(seenUsersSize);
        for (int i = 0; i < seenUsersSize; i++) {
            String key = in.readString();
            Long value = in.readLong();
            this.seenUsers.put(key, value);
        }

        int mediaSize = in.readInt();
        this.media = new HashMap<>(mediaSize);
        for (int i = 0; i < mediaSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.media.put(key, value);
        }*/

    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(creator);
        dest.writeString(text);
        dest.writeInt(status);
        dest.writeLong((long) createDate);

        if (seenUsers != null) {
            dest.writeMap(seenUsers);
        } else {
            seenUsers = new HashMap<>();
            dest.writeMap(seenUsers);
        }
        if (media != null) {
            dest.writeMap(media);
        } else {
            media = new HashMap<>();
            dest.writeMap(media);
        }

        /*if(this.seenUsers != null) {
            dest.writeInt(this.seenUsers.size());
            for (Map.Entry<String, Long> entry : this.seenUsers.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeLong(entry.getValue());
            }
        }

        if(this.media != null) {
            dest.writeInt(this.media.size());
            for (Map.Entry<String, String> entry : this.media.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }*/
    }

    public Message() {

    }

    public Message(String id) {
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public Map<String, Long> getSeenUsers() {
        return seenUsers;
    }

    public void setSeenUsers(Map<String, Long> seenUsers) {
        this.seenUsers = seenUsers;
    }

    public Map<String, String> getMedia() {
        return media;
    }

    public void setMedia(Map<String, String> media) {
        this.media = media;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Exclude
    public static void copyMessageObject(Message m1, Message m2) {
        m1.setId(m2.getId());
        m1.setSeenUsers(m2.getSeenUsers());
        m1.setCreator(m2.getCreator());
        m1.setText(m2.getText());
        m1.setStatus(m2.getStatus());
        m1.setCreateDate(m2.getCreateDate());
        m1.setMedia(m2.getMedia());
    }
}
