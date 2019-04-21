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

import vn.huynh.whatsapp.utils.Utils;

/**
 * Created by duong on 4/15/2019.
 */

public class ChatRepository implements ChatInterface {
    private ChildEventListener childEventListener;
    private DatabaseReference chatDb;
    private DatabaseReference userDb;
    private DatabaseReference chatDetailDb;
    private ValueEventListener valueEventListener;
    private UserRepository userRepository;
    private Query query;
    private HashMap<DatabaseReference, ValueEventListener> mValueListenerMap = new HashMap<>();
    private HashMap<DatabaseReference, ChildEventListener> mChildEventListenerMap = new HashMap<>();

    public ChatRepository() {
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
    }

    @Override
    public void addListener() {
        if (chatDb != null && childEventListener != null) {
            chatDb.addChildEventListener(childEventListener);
        }
        if (chatDetailDb != null && valueEventListener != null) {
            chatDetailDb.addValueEventListener(valueEventListener);
        }
        if (userRepository != null) {
            userRepository.addListener();
        }
    }

    @Override
    public void getChatList(final boolean onlyGroup, final ChatListCallBack callBack) {
        removeListener();
        mValueListenerMap.clear();
        chatDb = FirebaseDatabase.getInstance().getReference().child("user").child(Utils.currentUserId()).child("chat");
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
                if (dataSnapshot.exists()) {
                    chatDb.addChildEventListener(childEventListener);
                } else {
                    callBack.loadSuccessEmptyData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void getChatDetail(final boolean onlyGroup, final Chat chat, final ChatInterface.ChatListCallBack callBack) {
        //get chat detail for chat list
        DatabaseReference chatDetailRef = FirebaseDatabase.getInstance().getReference().child("chat").child(chat.getId());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (onlyGroup && !(boolean) dataSnapshot.child("group").getValue()) {
                        callBack.loadSuccess(null);
                    } else {
                        Chat temp = dataSnapshot.getValue(Chat.class);
                        temp.setId(dataSnapshot.getKey());
                        chat.cloneChat(temp);
                        if (chat.getUsers() != null) {
                            callBack.updateChatStatus(chat);
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
    public void getChatDetail(final String chatId, final ChatDetailCallBack callBack) {
        removeListener();
        //get chat detail
        chatDetailDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    chat.setId(chatId);
                    List<String> userList = new ArrayList<>(chat.getUserIds().values());
                    long pos = 0;
                    for (String userId : userList) {
                        pos++;
                        if (pos == chat.getUserIds().size()) {
                            userRepository.getUserData(userId, chat, callBack);
                        } else {
                            userRepository.getUserData(userId, chat, null);
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
        chatDetailDb.addListenerForSingleValueEvent(valueEventListener);

    }

    @Override
    public void createChat(boolean isGroup, String name, List<User> users, final CreateChatCallBack callBack) {
        removeListener();
        userDb = FirebaseDatabase.getInstance().getReference().child("user");
        chatDb = FirebaseDatabase.getInstance().getReference().child("chat").push();
        final String chatId = chatDb.getKey();

        if (chatId == null) {
            callBack.createFail("Cannot get chat Id");
            return;
        }
        DatabaseReference userIdRef = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("userIds");
        DatabaseReference notificationIdRef = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("notificationUserIds");

        final List<String> userIds = new ArrayList<>();
        for (User user : users) {
            userIds.add(user.getId());
        }
        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setName(name);
        chat.setGroup(isGroup);
        chat.setCreatorId(Utils.currentUserId());
        chat.setLastMessage("");
        chat.setStatus(Chat.STATUS_ENABLE);
        chat.setCreateDate(ServerValue.TIMESTAMP);
        chat.setLastMessageDate(ServerValue.TIMESTAMP);

        String singleChatKey = Utils.getSingleChatId(userIds);
        if (!isGroup) {
            chat.setSingleChatId(singleChatKey);
        }
        Map<String, String> userIdsMap = new HashMap<>();
        for (String userId : userIds) {
            userIdsMap.put(userIdRef.push().getKey(), userId);
        }
        chat.setUserIds(userIdsMap);
        Map<String, String> notificationIdMap = new HashMap<>();
        for (String userId : userIds) {
            notificationIdMap.put(notificationIdRef.push().getKey(), userId);
        }
        chat.setNotificationUserIds(notificationIdMap);

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
                                    userDb.child(userId).child("chat").child(chatId).setValue(lastMessageDate);
                                }
//                                Chat chat1 = dataSnapshot.getValue(Chat.class);
//                                chat1.setId(dataSnapshot.getKey());
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
    public void checkSingleChatExist(String singleChatId, final CheckSingleChatCallBack callBack) {
        chatDb = FirebaseDatabase.getInstance().getReference().child("chat");
        query = chatDb.orderByChild("singleChatId").equalTo(singleChatId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
//                        Chat chat = childSnapshot.getValue(Chat.class);
//                        chat.setId(childSnapshot.getKey());
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
}
