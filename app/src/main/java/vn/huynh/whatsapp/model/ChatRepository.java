package vn.huynh.whatsapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.LogManagerUtils;

/**
 * Created by duong on 4/15/2019.
 */

public class ChatRepository implements ChatInterface {
    private static String TAG = ChatRepository.class.getSimpleName();

    private DatabaseReference mDbRef;

    private ChildEventListener mChatListChildEventListener;
    private Query mChatListQuery;

    private DatabaseReference mChatDetailDb;
    private ValueEventListener mChatDetailValueEventListener;

    private UserRepository mUserRepository;

    public ChatRepository() {
        mDbRef = FirebaseDatabase.getInstance().getReference();
        mUserRepository = new UserRepository();
    }

    @Override
    public void removeListener() {
        if (mChatListQuery != null && mChatListChildEventListener != null) {
            mChatListQuery.removeEventListener(mChatListChildEventListener);
        }
        if (mChatDetailDb != null && mChatDetailValueEventListener != null) {
            mChatDetailDb.removeEventListener(mChatDetailValueEventListener);
        }
        if (mUserRepository != null) {
            mUserRepository.removeListener();
        }
    }

    @Override
    public void removeChatListListener() {
        if (mChatListQuery != null && mChatListChildEventListener != null) {
            mChatListQuery.removeEventListener(mChatListChildEventListener);
        }
    }

    @Override
    public void addChatListListener() {
        if (mChatListQuery != null && mChatListChildEventListener != null) {
            mChatListQuery.addChildEventListener(mChatListChildEventListener);
        }
    }

    @Override
    public void removeChatDetailListener() {
        if (mChatDetailDb != null && mChatDetailValueEventListener != null) {
            mChatDetailDb.removeEventListener(mChatDetailValueEventListener);
        }
    }

    @Override
    public void addChatDetailListener() {
        if (mChatDetailDb != null && mChatDetailValueEventListener != null) {
            mChatDetailDb.addValueEventListener(mChatDetailValueEventListener);
        }
    }

    @Override
    public void getChatList(final boolean onlyGroup, final ChatListCallback callback) {
        removeListener();
        mChatListQuery = mDbRef.child(Constant.FB_KEY_USER).child(ChatUtils.getUser().getId()).
                child(Constant.FB_KEY_CHAT).orderByValue();
        mChatListChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String chatId = dataSnapshot.getKey();
                    Chat chat = new Chat(chatId);
                    LogManagerUtils.d(TAG, dataSnapshot.getValue() + "");
                    getChatDetail(onlyGroup, chat, callback);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                LogManagerUtils.d(TAG, "child changed");
                if (dataSnapshot.exists()) {
                    String chatId = dataSnapshot.getKey();
                    Chat chat = new Chat(chatId);
                    getChatDetailToUpdate(onlyGroup, chat, callback);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        };

        mChatListQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    if (callback != null)
                        callback.loadSuccessEmptyData();
                } else {
                    if (callback != null)
                        callback.getChatCount(dataSnapshot.getChildrenCount());
                }
                mChatListQuery.addChildEventListener(mChatListChildEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        });
    }

    //get chat detail for chat list
    @Override
    public void getChatDetail(final boolean onlyGroup, final Chat chat, final ChatListCallback callback) {
        //get chat detail for chat list
        DatabaseReference chatDetailRef = mDbRef.child(Constant.FB_KEY_CHAT).child(chat.getId());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (onlyGroup && !(boolean) dataSnapshot.child(Constant.FB_KEY_GROUP).getValue()) {
                        //get only group chat
                        if (callback != null)
                            callback.loadSuccess(null);
                    } else {
                        Chat temp = dataSnapshot.getValue(Chat.class);
//                        boolean newMessage = false;
//                        if (temp.getLastMessageDateInLong() != chat.getLastMessageDateInLong()) {
//                            newMessage = true;
//                        }
                        chat.cloneChat(temp);
//                        if (chat.getUsers() != null) {
//                            callback.updateChatStatus(chat, newMessage);
//                        } else {
                        List<String> userList = new ArrayList<>(chat.getUserIds().values());
                        List<Task<DataSnapshot>> taskList = new ArrayList<>();
                        for (String userId : userList) {
                            chat.addUser(new User(userId));
                            taskList.add(mUserRepository.getUserData(userId));
                        }
                        Tasks.whenAllSuccess(taskList).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Object>> task) {
                                try {
                                    for (Object dataSnapshot : task.getResult()) {
                                        if (dataSnapshot instanceof DataSnapshot) {
                                            User user = ((DataSnapshot) dataSnapshot).getValue(User.class);
                                            user.setId(((DataSnapshot) dataSnapshot).getKey());
                                            chat.addUser(user);
                                        }
                                    }
                                    if (callback != null)
                                        callback.loadSuccess(chat);
                                } catch (NullPointerException e) {
                                    if (callback != null)
                                        callback.loadFail(e.getMessage());
                                }
                            }
                        });
//                        }
                    }
                } else {
                    if (callback != null)
                        callback.loadSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        };
        chatDetailRef.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void getChatDetailToUpdate(final boolean onlyGroup, final Chat chat, final ChatListCallback callback) {
        //get chat detail for chat list
        DatabaseReference chatDetailRef = mDbRef.child(Constant.FB_KEY_CHAT).child(chat.getId());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (onlyGroup && !(boolean) dataSnapshot.child(Constant.FB_KEY_GROUP).getValue()) {
                        //get only group chat
                        if (callback != null)
                            callback.loadSuccess(null);
                    } else {
                        Chat temp = dataSnapshot.getValue(Chat.class);
                        chat.cloneChat(temp);
                        List<String> userList = new ArrayList<>(chat.getUserIds().values());
                        List<Task<DataSnapshot>> taskList = new ArrayList<>();
                        for (String userId : userList) {
                            chat.addUser(new User(userId));
                            taskList.add(mUserRepository.getUserData(userId));
                        }
                        Tasks.whenAllSuccess(taskList).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Object>> task) {
                                try {
                                    for (Object dataSnapshot : task.getResult()) {
                                        if (dataSnapshot instanceof DataSnapshot) {
                                            User user = ((DataSnapshot) dataSnapshot).getValue(User.class);
                                            user.setId(((DataSnapshot) dataSnapshot).getKey());
                                            chat.addUser(user);
                                        }
                                    }
                                    if (chat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0) {
                                        if (callback != null)
                                            callback.updateChatStatus(chat, true);
                                    } else {
                                        if (callback != null)
                                            callback.updateChatStatus(chat, false);
                                    }
                                } catch (NullPointerException e) {
                                    if (callback != null)
                                        callback.loadFail(e.getMessage());
                                }
                            }
                        });
                    }
                } else {
                    if (callback != null)
                        callback.updateChatStatus(null, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        };
        chatDetailRef.addListenerForSingleValueEvent(valueEventListener);

    }

    //get single chat detail
    @Override
    public void getChatDetail(final String chatId, final ChatDetailCallback callback) {
        removeChatDetailListener();
        //get chat detail
        mChatDetailDb = mDbRef.child(Constant.FB_KEY_CHAT).child(chatId);
        mChatDetailValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Chat chat = dataSnapshot.getValue(Chat.class);
                    chat.setId(chatId);

                    List<String> userList = new ArrayList<>(chat.getUserIds().values());
                    List<Task<DataSnapshot>> taskList = new ArrayList<>();
                    for (String userId : userList) {
                        chat.addUser(new User(userId));
                        taskList.add(mUserRepository.getUserData(userId));
                    }
                    Tasks.whenAllSuccess(taskList).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Object>> task) {
                            try {
                                for (Object dataSnapshot : task.getResult()) {
                                    if (dataSnapshot instanceof DataSnapshot) {
                                        User user = ((DataSnapshot) dataSnapshot).getValue(User.class);
                                        user.setId(((DataSnapshot) dataSnapshot).getKey());
                                        chat.addUser(user);
                                    }
                                }
                                if (callback != null)
                                    callback.loadSuccess(chat);
                            } catch (NullPointerException e) {
                                if (callback != null)
                                    callback.loadFail(e.getMessage());
                            }
                        }
                    });
                } else {
                    if (callback != null)
                        callback.loadSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        };
        mChatDetailDb.addListenerForSingleValueEvent(mChatDetailValueEventListener);

    }

    @Override
    public void createChat(boolean isGroup, String name, List<User> users, final CreateChatCallback callback) {
        removeListener();
        DatabaseReference chatDb = mDbRef.child(Constant.FB_KEY_CHAT).push();
        final String chatId = chatDb.getKey();

        if (chatId == null) {
            if (callback != null)
                callback.createFail("Cannot get chat Id");
            return;
        }
        DatabaseReference userIdRef = mDbRef.child(Constant.FB_KEY_CHAT).child(chatId).
                child(Constant.FB_KEY_USER_IDS);
        final List<String> userIds = new ArrayList<>();
        for (User user : users) {
            userIds.add(user.getId());
        }
        final Chat chat = new Chat();
        chat.setId(chatId);
        chat.setName(name);
        chat.setGroup(isGroup);
        chat.setCreatorId(ChatUtils.getUser().getId());
        chat.setStatus(Chat.STATUS_ENABLE);
        chat.setCreateDate(ServerValue.TIMESTAMP);
        chat.setLastMessageDate(ServerValue.TIMESTAMP);

        String singleChatKey = ChatUtils.getSingleChatId(userIds);
        if (!isGroup) {
            chat.setSingleChatId(singleChatKey);
        } else {
            chat.setSingleChatId("");
        }
        Map<String, String> userIdsMap = new HashMap<>();
        Map<String, Long> numberUnread = new HashMap<>();
        Map<String, Boolean> notificationIdMap = new HashMap<>();
        for (String userId : userIds) {
            userIdsMap.put(userIdRef.push().getKey(), userId);
            notificationIdMap.put(userId, true);
            numberUnread.put(userId, 0L);
        }
        chat.setUserIds(userIdsMap);
        chat.setNotificationUserIds(notificationIdMap);
        chat.setNumberUnread(numberUnread);

        chatDb.setValue(chat, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull final DatabaseReference databaseReference) {
                if (databaseError == null) {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                long lastMessageDate = (long) dataSnapshot.child(Constant.FB_KEY_LAST_MESSAGE_DATE).getValue();
                                for (String userId : userIds) {
                                    mDbRef.child(Constant.FB_KEY_USER).child(userId).
                                            child(Constant.FB_KEY_CHAT).child(chatId).setValue(lastMessageDate);
                                }
                                if (callback != null)
                                    callback.createSuccess(dataSnapshot.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    if (callback != null)
                        callback.createFail(databaseError.getMessage());
                }
            }
        });
    }

    @Override
    public void checkSingleChatExist(final String singleChatId, final CheckSingleChatCallback callback) {
        DatabaseReference chatDb = mDbRef.child(Constant.FB_KEY_CHAT);
        Query query = chatDb.orderByChild(Constant.FB_KEY_SINGLE_CHAT_ID).equalTo(singleChatId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (callback != null)
                            callback.exist(childSnapshot.getKey());
                        break;
                    }
                } else {
                    if (callback != null)
                        callback.notExist();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.notExist();
            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void resetNumberUnread(final String chatId, final ResetUnreadMessageCallback callback) {
        DatabaseReference dbRefChat = mDbRef.child(Constant.FB_KEY_CHAT).child(chatId).
                child(Constant.FB_KEY_NUMBER_UNREAD);
        dbRefChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        if (dataSnapshot1.getKey().equals(ChatUtils.getUser().getId())) {
                            DatabaseReference df = mDbRef.child(Constant.FB_KEY_CHAT).child(chatId).
                                    child(Constant.FB_KEY_NUMBER_UNREAD).child(dataSnapshot1.getKey());
                            df.setValue(0, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable final DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        final DatabaseReference df = mDbRef.child(Constant.FB_KEY_USER).
                                                child(ChatUtils.getUser().getId()).
                                                child(Constant.FB_KEY_CHAT).child(chatId);
                                        df.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    long time = (long) dataSnapshot.getValue();
                                                    if (System.currentTimeMillis() % 2 == 0) {
                                                        time++;
                                                    } else {
                                                        time--;
                                                    }
                                                    df.setValue(time, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                            if (callback != null)
                                                                callback.success();
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        if (callback != null)
                                            callback.fail(databaseError.getMessage());
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.fail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void setChatNotification(final boolean turnOn, final String chatId, final TurnOffNotificationCallback callback) {
        DatabaseReference dbRefChat = mDbRef.child(Constant.FB_KEY_CHAT).child(chatId).
                child(Constant.FB_KEY_NOTIFICATION_USER_IDS).child(ChatUtils.getUser().getId());
        dbRefChat.setValue(turnOn, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    final DatabaseReference df = mDbRef.child(Constant.FB_KEY_USER).child(ChatUtils.getUser().getId()).
                            child(Constant.FB_KEY_CHAT).child(chatId);
                    df.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                long time = (long) dataSnapshot.getValue();
                                if (System.currentTimeMillis() % 2 == 0) {
                                    time++;
                                } else {
                                    time--;
                                }
                                df.setValue(time, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (callback != null)
                                            callback.success(chatId, turnOn);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    if (callback != null)
                        callback.fail(databaseError.getMessage());
                }
            }
        });
    }
}
