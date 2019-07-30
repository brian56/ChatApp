package vn.huynh.whatsapp.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Config;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.LogManagerUtils;

/**
 * Created by duong on 4/16/2019.
 */

public class MessageRepository implements MessageInterface {
    private DatabaseReference mDbRef;

    private List<String> mMediaIdList;
    private List<Message> mMessageList;
    private String mLastMessagePaginationId;
    private String mNewestMessageId;
    private long mTotalMessageCurrentPage = 0;
    private boolean mIsLoadingMore = false;

    private Query mNewMessageQuery;
    private ChildEventListener mNewMessageChildEventListener;

    private Query mQueryFirstPage;
    private ChildEventListener mFirstPageChildEventListener;

    private UploadMediaAsyncTask mUploadMediaAsyncTask;

    public MessageRepository() {
        mDbRef = FirebaseDatabase.getInstance().getReference();
        this.mMediaIdList = new ArrayList<>();
        this.mMessageList = new ArrayList<>();
        this.mLastMessagePaginationId = "";
        this.mNewestMessageId = "";
        this.mTotalMessageCurrentPage = 0;
        this.mIsLoadingMore = false;
    }

    @Override
    public void removeListener() {
    }

    @Override
    public void removeMessageListener() {
        if (mQueryFirstPage != null && mFirstPageChildEventListener != null) {
            mQueryFirstPage.removeEventListener(mFirstPageChildEventListener);
        }
        if (mNewMessageQuery != null && mNewMessageChildEventListener != null) {
            mNewMessageQuery.removeEventListener(mNewMessageChildEventListener);
        }
    }

    @Override
    public void addMessageListener() {
        if (mNewMessageQuery != null && mNewMessageChildEventListener != null) {
            mNewMessageQuery.addChildEventListener(mNewMessageChildEventListener);
        }
    }

    @Override
    public void getChatMessageFirstPage(String chatId, final GetChatMessageFirstPageCallback callback) {
        mMessageList.clear();
        mLastMessagePaginationId = "";
        mNewestMessageId = "";
        mTotalMessageCurrentPage = 0;
        mQueryFirstPage = mDbRef.child(Constant.FB_KEY_MESSAGE).child(chatId).orderByKey().limitToLast(Config.NUMBER_PAGINATION_MESSAGE);
        mFirstPageChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    mMessageList.add(message);
                    if (mMessageList.size() == Config.NUMBER_PAGINATION_MESSAGE) {
                        mNewestMessageId = mMessageList.get(mMessageList.size() - 1).getId();
                        mLastMessagePaginationId = mMessageList.get(0).getId();
//                        mMessageList.remove(0);
                        if (callback != null)
                            callback.loadSuccess(mMessageList, mNewestMessageId);
                    } else {
                        if (mMessageList.size() == mTotalMessageCurrentPage) {
                            mNewestMessageId = mMessageList.get(mMessageList.size() - 1).getId();
                            mLastMessagePaginationId = mMessageList.get(0).getId();
//                        mMessageList.remove(0);
                            if (callback != null)
                                callback.loadSuccessDone(mMessageList, mNewestMessageId);
                        }
                    }
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
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        };

        DatabaseReference loadMessageDb = mDbRef.child(Constant.FB_KEY_MESSAGE).child(chatId);
        loadMessageDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    if (callback != null)
                        callback.loadSuccessEmptyData();
                } else {
                    mQueryFirstPage.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mTotalMessageCurrentPage = dataSnapshot.getChildrenCount();
                                mQueryFirstPage.addChildEventListener(mFirstPageChildEventListener);
                            } else {
                                if (callback != null)
                                    callback.loadSuccessEmptyData();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            if (callback != null)
                                callback.loadFail(databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void getChatMessageLoadMore(String chatId, final GetChatMessageLoadMoreCallback callback) {
        if (!mIsLoadingMore) {
            mIsLoadingMore = true;
            mMessageList.clear();
            final Query loadMoreMessageQuery = mDbRef.child(Constant.FB_KEY_MESSAGE).child(chatId).
                    orderByKey().endAt(mLastMessagePaginationId).limitToLast(Config.NUMBER_PAGINATION_MESSAGE);
            final ChildEventListener loadMoreMessageChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists()) {
                        Message message = dataSnapshot.getValue(Message.class);
                        mMessageList.add(message);
                        if (mMessageList.size() == Config.NUMBER_PAGINATION_MESSAGE) {
                            mLastMessagePaginationId = mMessageList.get(0).getId();
                            mMessageList.remove(mMessageList.size() - 1);
                            mIsLoadingMore = false;
                            if (callback != null)
                                callback.loadSuccess(mMessageList);
                        } else {
                            if (mMessageList.size() == mTotalMessageCurrentPage) {
                                mMessageList.remove(mMessageList.size() - 1);
                                mLastMessagePaginationId = mMessageList.get(0).getId();
                                mIsLoadingMore = false;
                                if (callback != null)
                                    callback.loadSuccessDone(mMessageList);
                            }
                        }
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
                    if (callback != null)
                        callback.loadFail(databaseError.getMessage());
                }
            };

            loadMoreMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mTotalMessageCurrentPage = dataSnapshot.getChildrenCount();
                        loadMoreMessageQuery.addChildEventListener(loadMoreMessageChildEventListener);
                    } else {
                        mIsLoadingMore = false;
                        if (callback != null)
                            callback.loadSuccessEmptyData();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (callback != null)
                        callback.loadFail(databaseError.getMessage());
                }
            });
        }
    }

    @Override
    public void getNewMessage(String chatId, final GetNewMessageCallback callback) {
        //remove the listener in the first page to avoid duplicate when a new message was created
        removeMessageListener();
        if (mNewestMessageId.isEmpty()) {
            //chat doesn't have any message
            mNewMessageQuery = mDbRef.child(Constant.FB_KEY_MESSAGE).child(chatId);
        } else {
            //start listening for new message from newest message
            mNewMessageQuery = mDbRef.child(Constant.FB_KEY_MESSAGE).child(chatId).
                    orderByKey().startAt(mNewestMessageId);
        }
        mNewMessageChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message.getId().equals(mNewestMessageId))
                        return;
                    mNewestMessageId = message.getId();
                    if (callback != null)
                        callback.getSuccess(message);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
//                    Message message = dataSnapshot.getValue(Message.class);
//                    message.setId(dataSnapshot.getKey());
//                    callback.updateMessageStatus(message);
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
                    callback.getFail(databaseError.getMessage());
            }
        };
        mNewMessageQuery.addChildEventListener(mNewMessageChildEventListener);
    }

    @Override
    public void cancelUpload() {
        if (mUploadMediaAsyncTask != null && mUploadMediaAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mUploadMediaAsyncTask.cancel(true);
        }
    }

    @Override
    public void getNewMessageId(String chatId, SendMessageCallBack callback) {
        DatabaseReference loadMessageDb = mDbRef.child(Constant.FB_KEY_MESSAGE).child(chatId).push();
        if (callback != null)
            callback.getNewMessageIdSuccess(loadMessageDb.getKey());
    }

    @Override
    public void sendMessage(final Chat chat, final String messageId, String text,
                            final List<String> uriList, final SendMessageCallBack callback) {
        final DatabaseReference sendMessageDb = mDbRef.child(Constant.FB_KEY_MESSAGE).
                child(chat.getId()).child(messageId);

        final Message message = new Message();
        message.setId(messageId);
        message.setText(text);
        message.setCreator(ChatUtils.getUser().getId());
        message.setCreatorName(ChatUtils.getUser().getName());
        message.setStatus(Message.STATUS_DELIVERED);
        message.setType(Message.TYPE_TEXT);
        message.setCreateDate(ServerValue.TIMESTAMP);
        Map<String, Long> seenUsersMap = new HashMap<>();
        for (Map.Entry<String, String> entry : chat.getUserIds().entrySet()) {
            if (entry.getValue().equals(ChatUtils.getUser().getId()))
                seenUsersMap.put(entry.getValue(), (long) 1);
            else
                seenUsersMap.put(entry.getValue(), (long) 0);
        }
        message.setSeenUsers(seenUsersMap);

        final Map<String, String> mediaMap = new HashMap<>();

        if (uriList != null && !uriList.isEmpty()) {
            message.setType(Message.TYPE_MEDIA);
            if (mUploadMediaAsyncTask != null && mUploadMediaAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                mUploadMediaAsyncTask.cancel(true);
            }
            mUploadMediaAsyncTask = new UploadMediaAsyncTask(chat.getId(),
                    messageId, mediaMap, sendMessageDb, new UploadMediaCallBack() {
                @Override
                public void uploadSuccess(Map<String, String> mediaMap) {
                    message.setMedia(mediaMap);
                    updateDatabaseWithNewMessage(chat, sendMessageDb, message, callback);
                }

                @Override
                public void uploadFail() {
                    updateDatabaseWithNewMessage(chat, sendMessageDb, message, callback);
                }
            });
            mUploadMediaAsyncTask.execute(uriList);
        } else {
            updateDatabaseWithNewMessage(chat, sendMessageDb, message, callback);
        }
    }


    private void updateDatabaseWithNewMessage(final Chat chat, final DatabaseReference newMessageDB,
                                              final Message message, final SendMessageCallBack callback) {
        //update number unread message in chat object
        final DatabaseReference dbRefChat = mDbRef.child(Constant.FB_KEY_CHAT).child(chat.getId()).
                child(Constant.FB_KEY_NUMBER_UNREAD);
        dbRefChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long num;
                    DatabaseReference df;
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        num = 0L;
                        if (!dataSnapshot1.getKey().equals(ChatUtils.getUser().getId())) {
                            num = (long) dataSnapshot1.getValue();
                            num++;
                        }
                        df = dbRefChat.child(dataSnapshot1.getKey());
                        df.setValue(num);
                    }
                }
                //save new message
                newMessageDB.setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            mMediaIdList.clear();
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long timeStamp = Long.parseLong(dataSnapshot.child(Constant.FB_KEY_CREATE_DATE).
                                                getValue().toString());
                                        message.setCreateDate(timeStamp);
                                        String id = dataSnapshot.getKey();
                                        message.setId(id);
                                        updateLastMessageToChatAndUser(message, chat, callback);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            if (callback != null)
                                callback.sendFail(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.sendFail(databaseError.getMessage());
            }
        });

    }

    private void updateLastMessageToChatAndUser(Message message, Chat chat, final SendMessageCallBack callback) {
        DatabaseReference chatRef = mDbRef.child(Constant.FB_KEY_CHAT).child(chat.getId()).
                child(Constant.FB_KEY_LAST_MESSAGE_DATE);
        chatRef.setValue(message.getCreateDateInLong());
        for (String userId : chat.getUserIds().values()) {
            DatabaseReference userRef = mDbRef.child(Constant.FB_KEY_USER)
                    .child(userId).child(Constant.FB_KEY_CHAT).child(chat.getId());
            userRef.setValue(message.getCreateDateInLong());
            //update user object
            if (!userId.equals(ChatUtils.getUser().getId())) {
                userRef = mDbRef.child(Constant.FB_KEY_USER).child(userId).child(Constant.FB_KEY_LAST_CHAT_ID);
                userRef.setValue(chat.getId() + "=" + message.getCreateDateInLong() + "=" +
                        ChatUtils.generateRandomInteger());
            }
        }
        chatRef = mDbRef.child(Constant.FB_KEY_CHAT).child(chat.getId()).child(Constant.FB_KEY_LAST_MESSAGE_SENT);
        chatRef.setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    if (callback != null)
                        callback.sendSuccess();
                } else {
                    if (callback != null)
                        callback.sendFail(databaseError.getMessage());
                }
            }

        });
    }

    public class UploadMediaAsyncTask extends AsyncTask<List<String>, Void, Void> {
        private static final String TAG = "UploadMediaAsyncTask";

        private final StorageReference mStorageRef;
        private final DatabaseReference mNewMessageDb;
        private String mChatId, mMessageId;
        private UploadMediaCallBack mUploadMediaCallBack;
        private Map<String, String> mMediaMap;

        public UploadMediaAsyncTask(String chatId, String messageId, Map<String, String> mediaMap,
                                    DatabaseReference newMessageDb,
                                    UploadMediaCallBack callback) {
            this.mStorageRef = FirebaseStorage.getInstance().getReference();
            this.mNewMessageDb = newMessageDb;
            this.mChatId = chatId;
            this.mMessageId = messageId;
            this.mMediaMap = mediaMap;
            this.mUploadMediaCallBack = callback;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LogManagerUtils.d(TAG, "Pre-Execute");
        }

        @Override
        protected Void doInBackground(List<String>... uri) {
            final ArrayList<UploadTask> tasks = new ArrayList<>();
            for (final String mediaUri : uri[0]) {
                final String mediaId = mNewMessageDb.child(Constant.FB_KEY_MEDIA).push().getKey();
                if (mediaId != null) {
                    final StorageReference filePath = mStorageRef
                            .child(Constant.FB_KEY_MESSAGE).child(mChatId).child(mMessageId).child(mediaId);
                    UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uri.isComplete()) ;
                            Uri url = uri.getResult();
                            mMediaMap.put(mediaId, url.toString());
                        }
                    });
                    tasks.add(uploadTask);
                }
            }

            try {
                LogManagerUtils.d(TAG, "Waiting...");
                Tasks.whenAllSuccess(tasks).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Object>> task) {
                        mUploadMediaCallBack.uploadSuccess(mMediaMap);
                    }
                });
            } catch (Exception e) {
                mUploadMediaCallBack.uploadFail();
            }

            LogManagerUtils.d(TAG, "End of background processing");
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
