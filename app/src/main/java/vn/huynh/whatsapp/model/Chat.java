package vn.huynh.whatsapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import vn.huynh.whatsapp.utils.ChatUtils;

/**
 * Created by duong on 4/15/2019.
 */

public class Chat implements Parcelable {
    private String id;
    private boolean group;
    private String name;
    private String creatorId;
    private Object createDate;
    private Object lastMessageDate;
    private int status = STATUS_ENABLE;
    private String lastMessage;
    private String lastMessageId;
    private String singleChatId;
    private Map<String, String> userIds;
    private Map<String, String> notificationUserIds;
    private List<User> users;

    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DELETED = -1;

    protected Chat(Parcel in) {
        id = in.readString();
        group = in.readByte() != 0;
        name = in.readString();
        creatorId = in.readString();
        createDate = in.readLong();
        lastMessageDate = in.readLong();
        status = in.readInt();
        lastMessage = in.readString();
        lastMessageId = in.readString();
        singleChatId = in.readString();
        users = in.createTypedArrayList(User.CREATOR);

        userIds = new HashMap<>();
        in.readMap(userIds, String.class.getClassLoader());

        notificationUserIds = new HashMap<>();
        in.readMap(notificationUserIds, String.class.getClassLoader());

        /*int userIdsSize = in.readInt();
        this.userIds = new HashMap<>(userIdsSize);
        for (int i = 0; i < userIdsSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.userIds.put(key, value);
        }

        int notificationUserIdsSize = in.readInt();
        this.notificationUserIds = new HashMap<>(notificationUserIdsSize);
        for (int i = 0; i < notificationUserIdsSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.notificationUserIds.put(key, value);
        }*/
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (group ? 1 : 0));
        dest.writeString(name);
        dest.writeString(creatorId);
        dest.writeLong((long) createDate);
        dest.writeLong((long) lastMessageDate);
        dest.writeInt(status);
        dest.writeString(lastMessage);
        dest.writeString(lastMessageId);
        dest.writeString(singleChatId);
        dest.writeTypedList(users);

        if (userIds != null) {
            dest.writeMap(userIds);
        } else {
            userIds = new HashMap<>();
            dest.writeMap(userIds);
        }
        if (notificationUserIds != null) {
            dest.writeMap(notificationUserIds);
        } else {
            notificationUserIds = new HashMap<>();
            dest.writeMap(notificationUserIds);
        }
        /*if(this.userIds != null) {
            dest.writeInt(this.userIds.size());
            for (Map.Entry<String, String> entry : this.userIds.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }

        if(this.notificationUserIds != null) {
            dest.writeInt(this.notificationUserIds.size());
            for (Map.Entry<String, String> entry : this.notificationUserIds.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }*/
    }

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

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
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
                        if (!this.users.get(i).getId().equals(ChatUtils.currentUserId())) {
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
    public String getSingleChatAvatar() {
        if (this.users != null && this.users.size() > 0) {
            for (int i = 0; i < this.users.size(); i++) {
                if (!this.users.get(i).getId().equals(ChatUtils.currentUserId())) {
                    return this.users.get(i).getAvatar();
                }
            }
        }
        return "";
    }

    @Exclude
    public List<String> getGroupChatAvatar() {
        List<String> avatars = new ArrayList<>();
        if (this.users != null && this.users.size() > 0) {
            for (int i = 0; i < this.users.size(); i++) {
                avatars.add(this.users.get(i).getAvatar());
            }
        }
        return avatars;
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
        this.setCreatorId(c2.getCreatorId());
        this.setCreateDate(c2.getCreateDate());
        this.setGroup(c2.isGroup());
        this.setLastMessage(c2.getLastMessage());
        this.setLastMessageId(c2.getLastMessageId());
        this.setLastMessageDate(c2.getLastMessageDate());
        this.setSingleChatId(c2.getSingleChatId());
    }

}
