package vn.huynh.whatsapp.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import vn.huynh.whatsapp.utils.SendNotification;
import vn.huynh.whatsapp.utils.Utils;

/**
 * Created by duong on 4/16/2019.
 */

public class MessageRepository implements MessageInterface {
    private DatabaseReference messageDb;
    private ChildEventListener childEventListener;
    private int totalUploadedMedia;
    private List<String> mediaIdList;

    public MessageRepository() {
        this.mediaIdList = new ArrayList<>();
    }

    @Override
    public void removeListener() {
        if (messageDb != null && childEventListener != null) {
            messageDb.removeEventListener(childEventListener);
        }
    }

    @Override
    public void getChatMessageData(String chatId, final GetChatMessageCallBack callBack) {
        messageDb = FirebaseDatabase.getInstance().getReference().child("message").child(chatId);
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
//                if(dataSnapshot.exists()) {
//                    Message message = dataSnapshot.getValue(Message.class);
//                    message.setId(dataSnapshot.getKey());
//                    callBack.updateMessageStatus(message);
//                }
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
        messageDb.addChildEventListener(childEventListener);
    }

    @Override
    public void getNewMessageId(String chatId, SendMessageCallBack callBack) {
        messageDb = FirebaseDatabase.getInstance().getReference().child("message").child(chatId).push();
        callBack.getNewMessageIdSuccess(messageDb.getKey());
    }

    @Override
    public void sendMessage(final Chat chat, final String messageId, String text, final List<String> uriList, final SendMessageCallBack callBack) {
        totalUploadedMedia = 0;
        messageDb = FirebaseDatabase.getInstance().getReference().child("message").child(chat.getId()).child(messageId);

        final Message message = new Message();
        message.setId(messageId);
        message.setText(text);
        message.setCreator(Utils.currentUserId());
        message.setStatus(Message.STATUS_DELIVERED);
        message.setCreateDate(ServerValue.TIMESTAMP);
        Map<String, Object> seenUsersMap = new HashMap<>();
        seenUsersMap.put(Utils.currentUserId(), true);
        message.setSeenUsers(seenUsersMap);

        final Map<String, String> mediaMap = new HashMap<>();

        if (!uriList.isEmpty()) {
            for (final String mediaUri : uriList) {
                String mediaId = messageDb.child("media").push().getKey();
                mediaIdList.add(mediaId);
                final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                        .child("message").child(chat.getId()).child(messageId).child(mediaId);
                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mediaMap.put(mediaIdList.get(totalUploadedMedia), uri.toString());
                                totalUploadedMedia++;
                                if (totalUploadedMedia == uriList.size()) {
                                    message.setMedia(mediaMap);
                                    updateDatabaseWithNewMessage(chat, messageDb, message, callBack);
                                }

                            }
                        });
                    }

                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callBack.sendFail(e.getMessage());
                    }
                });
            }
        } else {
            updateDatabaseWithNewMessage(chat, messageDb, message, callBack);
        }
    }

    /*@Override
    public void sendMessage(final Chat chat, String text, final List<String> uriList, final SendMessageCallBack callBack) {
        totalUploadedMedia = 0;
        messageDb = FirebaseDatabase.getInstance().getReference().child("message").child(chat.getId()).push();
        String messageId = messageDb.getKey();
        messageDb = FirebaseDatabase.getInstance().getReference().child("message").child(chat.getId()).child(messageId);

        final Message message = new Message();
        message.setId(messageId);
        message.setText(text);
        message.setCreator(Utils.currentUserId());
        message.setStatus(Message.STATUS_SENDING);
        message.setCreateDate(ServerValue.TIMESTAMP);
        Map<String, Object> seenUsersMap = new HashMap<>();
        seenUsersMap.put(Utils.currentUserId(), true);

        final Map<String, String> mediaMap = new HashMap<>();

        if (!uriList.isEmpty()) {
            for (final String mediaUri : uriList) {
                String mediaId = messageDb.child("media").push().getKey();
                mediaIdList.add(mediaId);
                final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                        .child("message").child(chat.getId()).child(messageId).child(mediaId);
                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mediaMap.put(mediaIdList.get(totalUploadedMedia), uri.toString());
                                totalUploadedMedia++;
                                if (totalUploadedMedia == uriList.size()) {
                                    message.setMedia(mediaMap);
                                    updateDatabaseWithNewMessage(chat, messageDb, message, callBack);
                                }

                            }
                        });
                    }

                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callBack.sendFail(e.getMessage());
                    }
                });
            }
        } else {
            updateDatabaseWithNewMessage(chat, messageDb, message, callBack);
        }
    }*/

    private void updateDatabaseWithNewMessage(final Chat chat, DatabaseReference newMessageDB, final Message message, final SendMessageCallBack callBack) {
        newMessageDB.setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    totalUploadedMedia = 0;
                    mediaIdList.clear();

                    String text = "";
                    if (message.getText() != null) {
                        text = message.getText();
                    } else {
                        text = "Sent media";
                    }
                    for (User userObject : chat.getUsers()) {
                        if (!userObject.getId().equals(Utils.currentUserId())) {
                            new SendNotification(text, "New message", userObject.getNotificationKey());
                        }
                    }
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                long timeStamp = Long.parseLong(dataSnapshot.child("createDate").getValue().toString());
                                updateLastMessageTime(chat.getId(), timeStamp, message.getText(), callBack);
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

    private void updateLastMessageTime(String chatId, long lastMessageTime, String lastMessageText, final SendMessageCallBack callBack) {
        DatabaseReference chatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId);

        HashMap newChatMap = new HashMap();
        newChatMap.put("lastMessageDate", lastMessageTime);
        newChatMap.put("lastMessage", lastMessageText.isEmpty() ? "Sent photo" : lastMessageText);
        chatDb.updateChildren(newChatMap, new DatabaseReference.CompletionListener() {
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
}
