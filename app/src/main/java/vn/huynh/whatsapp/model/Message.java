package vn.huynh.whatsapp.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by duong on 4/15/2019.
 */

public class Message implements Serializable {
    private String id;
    private String creator;
    private String text;
    private int status = STATUS_SENDING;
    private Object createDate;
    private Map<String, Object> seenUsers;
    private Map<String, String> media;

    public static final int STATUS_SENDING = 1;
    public static final int STATUS_DELETED = -1;
    public static final int STATUS_DELIVERED = 2;

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

    public Map<String, Object> getSeenUsers() {
        return seenUsers;
    }

    public void setSeenUsers(Map<String, Object> seenUsers) {
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
