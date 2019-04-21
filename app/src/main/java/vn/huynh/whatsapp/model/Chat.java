package vn.huynh.whatsapp.model;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import vn.huynh.whatsapp.utils.Utils;

/**
 * Created by duong on 4/15/2019.
 */

public class Chat implements Serializable {
    private String id;
    private boolean group;
    private String name;
    private String creatorId;
    private Object createDate;
    private Object lastMessageDate;
    private int status = STATUS_ENABLE;
    private String lastMessage;
    private Map<String, String> userIds;
    private Map<String, String> notificationUserIds;

    private String singleChatId;

    private List<User> users;

    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DELETED = -1;

    public Chat() {

    }

    public Chat(String id) {
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

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Object lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    @Exclude
    public long getLastMessageDateInLong() {
        return (long) lastMessageDate;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Map<String, String> getUserIds() {
        //sort map by key
        return new TreeMap<>(userIds);
    }

    public void setUserIds(Map<String, String> userIds) {
        this.userIds = userIds;
    }

    public Map<String, String> getNotificationUserIds() {
        return notificationUserIds;
    }

    public void setNotificationUserIds(Map<String, String> notificationUserIds) {
        this.notificationUserIds = notificationUserIds;
    }

    public String getSingleChatId() {
        return singleChatId;
    }

    public void setSingleChatId(String singleChatId) {
        this.singleChatId = singleChatId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @Exclude
    public List<User> getUsers() {
        return users;
    }

    @Exclude
    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Exclude
    public void addUser(User user) {
        if (this.users == null)
            this.users = new ArrayList<>();
        for (int i = 0; i < this.users.size(); i++) {
            if (this.users.get(i).getId().equals(user.getId())) {
                this.users.remove(i);
                this.users.add(i, user);
                return;
            }
        }
        this.users.add(user);
    }

    @Exclude
    public String getChatName() {
        if (!TextUtils.isEmpty(this.name)) {
            return this.name;
        }
        String groupName = "";
        try {
            if (group) {
                if (this.users != null) {
                    for (int i = 0; i < this.users.size(); i++) {
                        Log.d("User group", this.users.get(i).getName());
                        if (i < this.users.size() - 1)
                            groupName = groupName + this.users.get(i).getName() + ", ";
                        else
                            groupName += this.users.get(i).getName();
                    }
                }
            } else {
                if (this.users != null) {
                    for (int i = 0; i < this.users.size(); i++) {
                        if (!this.users.get(i).getId().equals(Utils.currentUserId())) {
                            return this.users.get(i).getName();
                        }
                    }
                }
            }
        } catch (Exception e) {
            return groupName;
        }
//        Log.d("Chat group name", groupName);
        return groupName;
    }

    @Exclude
    public String getChatAvatar() {
        if (this.users != null && this.users.size() > 0) {
            for (int i = 0; i < this.users.size(); i++) {
                if (!this.users.get(i).getId().equals(Utils.currentUserId())) {
                    return this.users.get(i).getAvatar();
                }
            }
        }
        return "";
    }

    @Exclude
    public void cloneChat(Chat c2) {
        if (this.id == null)
            this.setId(c2.getId());
        if (this.users == null)
            this.setUsers(c2.getUsers());
        this.setUserIds(c2.getUserIds());
        this.setStatus(c2.getStatus());
        this.setNotificationUserIds(c2.getNotificationUserIds());
        this.setName(c2.getName());
        this.setCreateDate(c2.getCreateDate());
        this.setGroup(c2.isGroup());
        this.setLastMessage(c2.getLastMessage());
        this.setLastMessageDate(c2.getLastMessageDate());
        this.setSingleChatId(c2.getSingleChatId());
    }

}
