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

/**
 * Created by duong on 4/15/2019.
 */

public class ChatRepository implements ChatInterface {
    private DatabaseReference dbRef;
    private ChildEventListener childEventListener;
    private DatabaseReference chatDb;

    private DatabaseReference chatDetailDb;
    private ValueEventListener valueEventListener;

    private UserRepository userRepository;
    private Query query;

    private HashMap<DatabaseReference, ValueEventListener> mValueListenerMap = new HashMap<>();
    private HashMap<DatabaseReference, ChildEventListener> mChildEventListenerMap = new HashMap<>();

    public ChatRepository() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        userRepository = new UserRepository();
    }

    @Override
    public void removeListener() {
        if (chatDb != null && childEventListener != null) {
            chatDb.removeEventListener(childEventListener);
        }
        if (chatDetailDb != null && valueEventListener != null) {
            chatDetailDb.removeEventListener(valueEventListener);
        }
        if (query != null && valueEventListener != null) {
            query.removeEventListener(valueEventListener);
        }
        if (userRepository != null) {
            userRepository.removeListener();
        }

        for (Map.Entry<DatabaseReference, ValueEventListener> entry : mValueListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        mValueListenerMap.clear();
    }

    @Override
    public void removeChatListListener() {
        if (chatDb != null && childEventListener != null) {
            chatDb.removeEventListener(childEventListener);
        }
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : mValueListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    @Override
    public void addChatListListener() {
        if (chatDb != null && childEventListener != null) {
            chatDb.addChildEventListener(childEventListener);
        }
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : mValueListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.addValueEventListener(listener);
        }
    }

    @Override
    public void removeChatDetailListener() {
        if (chatDetailDb != null && valueEventListener != null) {
            chatDetailDb.removeEventListener(valueEventListener);
        }
        if (query != null && valueEventListener != null) {
            query.removeEventListener(valueEventListener);
        }
    }

    @Override
    public void addChatDetailListener() {
        if (chatDetailDb != null && valueEventListener != null) {
            chatDetailDb.addValueEventListener(valueEventListener);
        }
    }

    @Override
    public void getChatList(final boolean onlyGroup, final ChatListCallback callBack) {
        removeListener();
        mValueListenerMap.clear();
        chatDb = dbRef.child("user").child(ChatUtils.getCurrentUserId()).child("chat");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String chatId = dataSnapshot.getKey();
                    Chat chat = new Chat(chatId);
                    getChatDetail(onlyGroup, chat, callBack);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        chatDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatDb.addChildEventListener(childEventListener);
                if (!dataSnapshot.exists()) {
                    callBack.loadSuccessEmptyData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*@Override
    public void getGroupList(final boolean onlyGroup, final ChatListCallback callBack) {
        removeListener();
        mValueListenerMap.clear();
        chatDb = dbRef.child("user").child(ChatUtils.getCurrentUserId()).child("chat");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String chatId = dataSnapshot.getKey();
                    Chat chat = new Chat(chatId);
                    getChatDetail(onlyGroup, chat, callBack);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        chatDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatDb.addChildEventListener(childEventListener);
                if (!dataSnapshot.exists()) {
                    callBack.loadSuccessEmptyData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    @Override
    public void getChatDetail(final boolean onlyGroup, final Chat chat, final ChatListCallback callBack) {
        //get chat detail for chat list
        DatabaseReference chatDetailRef = dbRef.child("chat").child(chat.getId());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (onlyGroup && !(boolean) dataSnapshot.child("group").getValue()) {
                        //get only group chat
                        callBack.loadSuccess(null);
                    } else {
                        Chat temp = dataSnapshot.getValue(Chat.class);
                        boolean newMessage = false;
                        if (temp.getLastMessageDateInLong() != chat.getLastMessageDateInLong()) {
                            newMessage = true;
                        }
                        chat.cloneChat(temp);
                        if (chat.getUsers() != null) {
                            callBack.updateChatStatus(chat, newMessage);
                        } else {
                            List<String> userList = new ArrayList<>(chat.getUserIds().values());
                            List<Task<DataSnapshot>> taskList = new ArrayList<>();
                            for (String userId : userList) {
                                chat.addUser(new User(userId));
                                taskList.add(userRepository.getUserData(userId));
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
                                        callBack.loadSuccess(chat);
                                    } catch (NullPointerException e) {
                                        callBack.loadFail(e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                } else {
                    callBack.loadSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mValueListenerMap.put(chatDetailRef, valueEventListener);
        chatDetailRef.addValueEventListener(valueEventListener);

    }

    @Override
    public void getChatDetail(final String chatId, final ChatDetailCallback callBack) {
        removeListener();
        //get chat detail
        chatDetailDb = dbRef.child("chat").child(chatId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Chat chat = dataSnapshot.getValue(Chat.class);
                    chat.setId(chatId);

                    List<String> userList = new ArrayList<>(chat.getUserIds().values());
                    List<Task<DataSnapshot>> taskList = new ArrayList<>();
                    for (String userId : userList) {
                        chat.addUser(new User(userId));
                        taskList.add(userRepository.getUserData(userId));
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
                                callBack.loadSuccess(chat);
                            } catch (NullPointerException e) {
                                callBack.loadFail(e.getMessage());
                            }
                        }
                    });
                } else {
                    callBack.loadSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        chatDetailDb.addListenerForSingleValueEvent(valueEventListener);

    }

    @Override
    public void createChat(boolean isGroup, String name, List<User> users, final CreateChatCallback callBack) {
        removeListener();
        chatDb = dbRef.child("chat").push();
        final String chatId = chatDb.getKey();

        if (chatId == null) {
            callBack.createFail("Cannot get chat Id");
            return;
        }
        DatabaseReference userIdRef = dbRef.child("chat").child(chatId).child("userIds");
        DatabaseReference notificationIdRef = dbRef.child("chat").child(chatId).child("notificationUserIds");

        final List<String> userIds = new ArrayList<>();
        for (User user : users) {
            userIds.add(user.getId());
        }
        final Chat chat = new Chat();
        chat.setId(chatId);
        chat.setName(name);
        chat.setGroup(isGroup);
        chat.setCreatorId(ChatUtils.getCurrentUserId());
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
        Map<String, String> notificationIdMap = new HashMap<>();
        for (String userId : userIds) {
            userIdsMap.put(userIdRef.push().getKey(), userId);
            notificationIdMap.put(notificationIdRef.push().getKey(), userId);
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
                                long lastMessageDate = (long) dataSnapshot.child("lastMessageDate").getValue();
                                for (String userId : userIds) {
                                    dbRef.child("user").child(userId).child("chat").child(chatId).setValue(lastMessageDate);
                                }
                                callBack.createSuccess(dataSnapshot.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    callBack.createFail(databaseError.getMessage());
                }
            }
        });
    }

    @Override
    public void checkSingleChatExist(final String singleChatId, final CheckSingleChatCallback callBack) {
        chatDb = dbRef.child("chat");
//        chatDb.keepSynced(true);
        query = chatDb.orderByChild("singleChatId").equalTo(singleChatId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        callBack.exist(childSnapshot.getKey());
                        break;
                    }
                } else {
                    callBack.notExist();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void resetNumberUnread(String chatId, final ResetUnreadMessageCallback callback) {
        final DatabaseReference dbRefChat = dbRef.child("chat").child(chatId).child("numberUnread");
        dbRefChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        if (dataSnapshot1.getKey().equals(ChatUtils.getCurrentUserId())) {
                            DatabaseReference df = dbRefChat.child(dataSnapshot1.getKey());
                            df.setValue(0, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError == null)
                                        callback.success();
                                    else
                                        callback.fail();
                                }
                            });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
