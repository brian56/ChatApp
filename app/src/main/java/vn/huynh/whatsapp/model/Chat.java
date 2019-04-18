package vn.huynh.whatsapp.model;

import android.text.TextUtils;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.huynh.whatsapp.utils.Utils;

/**
 * Created by duong on 4/15/2019.
 */

public class Chat implements Serializable {
    private String id;
    private boolean group;
    private String name;
    private Object createDate;
    private Object lastMessageDate;
    private int status = STATUS_ENABLE;
    private String lastMessage;
    private Map<String, String> userIds;
    private Map<String, String> notificationUserIds;

    private String singleChatId;

    private List<User> users = new ArrayList<>();

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
        return userIds;
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
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.remove(i);
                users.add(i, user);
                return;
            }
        }
        this.users.add(user);
    }

    @Exclude
    public String getChatName() {
        if(!TextUtils.isEmpty(name)) {
            return name;
        }
        String groupName = "";
        if(group) {
            if(users != null) {
                for (int i = 0; i < users.size(); i++) {
                    if (i < users.size() -1)
                        groupName = groupName + users.get(i).getName() + ", ";
                    else
                        groupName += users.get(i).getName();
                }
            }
        } else {
            if(users != null) {
                for (int i = 0; i < users.size(); i++) {
                    if (!users.get(i).getId().equals(Utils.currentUserId()))
                        return users.get(i).getName();
                }
            }
        }
        return groupName;
    }

    @Exclude
    public String getChatAvatar() {
        if(users != null && users.size() > 0) {
            for (int i = 0; i < users.size(); i++) {
                if(!users.get(i).getId().equals(Utils.currentUserId())) {
                    return users.get(i).getAvatar();
                }
            }
        }
        return "";
    }
}
