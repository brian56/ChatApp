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
import vn.huynh.whatsapp.utils.SendNotification;

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
                    message.setId(dataSnapshot.getKey());
                    callBack.loadSuccess(message);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    message.setId(dataSnapshot.getKey());
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
        message.setCreator(ChatUtils.currentUserId());
        message.setStatus(Message.STATUS_DELIVERED);
        message.setCreateDate(ServerValue.TIMESTAMP);
        Map<String, Long> seenUsersMap = new HashMap<>();
        seenUsersMap.put(ChatUtils.currentUserId(), (long) 1);
        message.setSeenUsers(seenUsersMap);

        final Map<String, String> mediaMap = new HashMap<>();

        if (uriList != null && !uriList.isEmpty()) {
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


    private void updateDatabaseWithNewMessage(final Chat chat, DatabaseReference newMessageDB,
                                              final Message message, final SendMessageCallBack callBack) {
        newMessageDB.setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mediaIdList.clear();

                    String text;
                    if (message.getText() != null) {
                        text = message.getText();
                    } else {
                        text = "Sent media";
                    }
                    for (User userObject : chat.getUsers()) {
                        if (!userObject.getId().equals(ChatUtils.currentUserId())) {
                            new SendNotification(text, "New message", userObject.getNotificationKey());
                        }
                    }
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                long timeStamp = Long.parseLong(dataSnapshot.child("createDate").getValue().toString());
                                updateLastMessageToChat(message.getId(), chat, timeStamp, message.getText(), callBack);
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

    private void updateLastMessageToChat(String messageId, Chat chat, long lastMessageTime, String lastMessageText,
                                         final SendMessageCallBack callBack) {
        DatabaseReference chatRef = dbRef.child("chat").child(chat.getId());

        HashMap<String, Object> newChatMap = new HashMap<>();
        newChatMap.put("lastMessageDate", lastMessageTime);
        newChatMap.put("lastMessageId", messageId);
        newChatMap.put("lastMessage", lastMessageText.isEmpty() ? "Sent photo" : lastMessageText);
        chatRef.updateChildren(newChatMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    callBack.sendSuccess();
                } else {
                    callBack.sendFail(databaseError.getMessage());
                }
            }

        });
        for (String userId : chat.getUserIds().values()) {
            DatabaseReference userRef = dbRef.child("user")
                    .child(userId).child("chat").child(chat.getId());
            userRef.setValue(lastMessageTime);
        }
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
