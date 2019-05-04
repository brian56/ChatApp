package vn.huynh.whatsapp.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.huynh.whatsapp.utils.ChatUtils;

/**
 * Created by duong on 4/16/2019.
 */

public class MessageRepository implements MessageInterface {
    private DatabaseReference dbRef;
    private DatabaseReference messageDb;
    private ChildEventListener childEventListener;
    private List<String> mediaIdList;

    public MessageRepository() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        this.mediaIdList = new ArrayList<>();
    }

    @Override
    public void removeListener() {
        if (messageDb != null && childEventListener != null) {
            messageDb.removeEventListener(childEventListener);
        }
    }

    @Override
    public void removeMessageListener() {
        if (messageDb != null && childEventListener != null) {
            messageDb.removeEventListener(childEventListener);
        }
    }

    @Override
    public void addMessageListener() {
        if (messageDb != null && childEventListener != null) {
            messageDb.addChildEventListener(childEventListener);
        }
    }

    @Override
    public void getChatMessageData(String chatId, final GetChatMessageCallBack callBack) {
        removeListener();
        messageDb = dbRef.child("message").child(chatId);
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
//                    message.setId(dataSnapshot.getKey());
                    callBack.loadSuccess(message);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
//                    message.setId(dataSnapshot.getKey());
//                    callBack.updateMessageStatus(message);
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

            }
        };

        messageDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageDb.addChildEventListener(childEventListener);
                if (!dataSnapshot.exists()) {
                    callBack.loadSuccessEmptyData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void getNewMessageId(String chatId, SendMessageCallBack callBack) {
        messageDb = dbRef.child("message").child(chatId).push();
        callBack.getNewMessageIdSuccess(messageDb.getKey());
    }

    @Override
    public void sendMessage(final Chat chat, final String messageId, String text,
                            final List<String> uriList, final SendMessageCallBack callBack) {
        messageDb = dbRef.child("message").child(chat.getId()).child(messageId);

        final Message message = new Message();
        message.setId(messageId);
        message.setText(text);
        message.setCreator(ChatUtils.getCurrentUserId());
        message.setCreatorName(ChatUtils.getCurrentUserName());
        message.setStatus(Message.STATUS_DELIVERED);
        message.setType(Message.TYPE_TEXT);
        message.setCreateDate(ServerValue.TIMESTAMP);
        Map<String, Long> seenUsersMap = new HashMap<>();
        for (Map.Entry<String, String> entry : chat.getUserIds().entrySet()) {
            if (entry.getValue().equals(ChatUtils.getCurrentUserId()))
                seenUsersMap.put(entry.getValue(), (long) 1);
            else
                seenUsersMap.put(entry.getValue(), (long) 0);
        }
        message.setSeenUsers(seenUsersMap);

        final Map<String, String> mediaMap = new HashMap<>();

        if (uriList != null && !uriList.isEmpty()) {
            message.setType(Message.TYPE_MEDIA);
            /*ArrayList<UploadTask> tasks = new ArrayList<>();
            for (String mediaUri : uriList) {
                final String mediaId = messageDb.child("media").push().getKey();
                final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                        .child("message").child(chat.getId()).child(messageId).child(mediaId);
                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uri.isComplete());
                        Uri url = uri.getResult();
                        mediaMap.put(mediaId, url.toString());
                    }
                });
                tasks.add(uploadTask);
            }
            Tasks.whenAllSuccess(tasks).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                @Override
                public void onComplete(@NonNull Task<List<Object>> task) {
                    message.setMedia(mediaMap);
                    updateDatabaseWithNewMessage(chat, messageDb, message, callBack);
                }
            });*/
            UploadMediaAsyncTask uploadMediaAsyncTask = new UploadMediaAsyncTask(chat.getId(),
                    messageId, mediaMap, new UploadMediaCallBack() {
                @Override
                public void uploadSuccess(Map<String, String> mediaMap) {
                    message.setMedia(mediaMap);
                    updateDatabaseWithNewMessage(chat, messageDb, message, callBack);
                }

                @Override
                public void uploadFail() {
                    updateDatabaseWithNewMessage(chat, messageDb, message, callBack);
                }
            });
            uploadMediaAsyncTask.execute(uriList);
        } else {
            updateDatabaseWithNewMessage(chat, messageDb, message, callBack);
        }
    }


    private void updateDatabaseWithNewMessage(final Chat chat, final DatabaseReference newMessageDB,
                                              final Message message, final SendMessageCallBack callBack) {
        //update number unread message in chat object
        final DatabaseReference dbRefChat = dbRef.child("chat").child(chat.getId()).child("numberUnread");
        dbRefChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Long num = 0l;
                        if (!dataSnapshot1.getKey().equals(ChatUtils.getCurrentUserId())) {
                            num = (long) dataSnapshot1.getValue();
                            num++;
                        }
                        DatabaseReference df = dbRefChat.child(dataSnapshot1.getKey());
                        df.setValue(num);
                    }
                }
                //save new message
                newMessageDB.setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            mediaIdList.clear();
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long timeStamp = Long.parseLong(dataSnapshot.child("createDate").getValue().toString());
                                        message.setCreateDate(timeStamp);
                                        String id = dataSnapshot.getKey();
                                        message.setId(id);
                                        updateLastMessageToChatAndUser(message, chat, callBack);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            callBack.sendFail(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateLastMessageToChatAndUser(Message message, Chat chat, final SendMessageCallBack callBack) {
        DatabaseReference chatRef = dbRef.child("chat").child(chat.getId()).child("lastMessageDate");
        chatRef.setValue(message.getCreateDateInLong());
        for (String userId : chat.getUserIds().values()) {
            DatabaseReference userRef = dbRef.child("user")
                    .child(userId).child("chat").child(chat.getId());
            userRef.setValue(message.getCreateDateInLong());
            //update user object
            if (!userId.equals(ChatUtils.getCurrentUserId())) {
                userRef = dbRef.child("user")
                        .child(userId).child("lastChatId");
                userRef.setValue(chat.getId() + "=" + message.getCreateDateInLong() + "=" + ChatUtils.generateRandomInteger());
            }
        }
        chatRef = dbRef.child("chat").child(chat.getId()).child("lastMessageSent");
        chatRef.setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    callBack.sendSuccess();
                } else {
                    callBack.sendFail(databaseError.getMessage());
                }
            }

        });
    }

    public class UploadMediaAsyncTask extends AsyncTask<List<String>, Void, Void> {
        private static final String TAG = "UploadMediaAsyncTask";

        private final StorageReference mStorageRef;
        private String chatId, messageId;
        private UploadMediaCallBack uploadMediaCallBack;
        private Map<String, String> mediaMap;

        public UploadMediaAsyncTask(String chatId, String messageId, Map<String, String> mediaMap,
                                    UploadMediaCallBack callBack) {
            mStorageRef = FirebaseStorage.getInstance().getReference();
            this.chatId = chatId;
            this.messageId = messageId;
            this.mediaMap = mediaMap;
            this.uploadMediaCallBack = callBack;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Pre-Execute");
        }

        @Override
        protected Void doInBackground(List<String>... uri) {
            final ArrayList<UploadTask> tasks = new ArrayList<>();

            for (final String mediaUri : uri[0]) {
                final String mediaId = messageDb.child("media").push().getKey();
                if (mediaId != null) {
                    final StorageReference filePath = mStorageRef
                            .child("message").child(chatId).child(messageId).child(mediaId);
                    UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uri.isComplete()) ;
                            Uri url = uri.getResult();
                            mediaMap.put(mediaId, url.toString());
                        }
                    });
                    tasks.add(uploadTask);
                }
            }

            try {
                Log.d(TAG, "Waiting...");
                Tasks.whenAllSuccess(tasks).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Object>> task) {
                        uploadMediaCallBack.uploadSuccess(mediaMap);
                    }
                });
            } catch (Exception e) {
                uploadMediaCallBack.uploadFail();
            }

            Log.d(TAG, "End of background processing");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    interface UploadMediaCallBack {
        void uploadSuccess(Map<String, String> mediaMap);

        void uploadFail();
    }
}
