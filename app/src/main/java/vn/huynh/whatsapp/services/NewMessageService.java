package vn.huynh.whatsapp.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.home.HomeActivity;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.Friend;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.AppUtils;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.ImageUtils;
import vn.huynh.whatsapp.utils.LogManagerUtils;
import vn.huynh.whatsapp.utils.MyApp;
import vn.huynh.whatsapp.utils.SharedPrefsUtil;

/**
 * Created by duong on 4/26/2019.
 */

public class NewMessageService extends Service {
    public static String TAG = NewMessageService.class.getSimpleName();
    private NotificationCompat.Builder mNotification;
    public static int ID_NOTIFICATION = 9;
    private boolean mIsShowMessageNotification = true;
    private boolean mIsShowFriendNotification = true;
    private static final String NOTIFICATION_CHANNEL = "WHATSAPP_CHANNEL_ID";

    private NotificationManager mNotificationManager;
    private RemoteViews mSimpleContentView;

    private final IBinder mBinder = new LocalBinder();
    private DatabaseReference mDF = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mDatabaseMessage;
    private ValueEventListener mValueEventListenerMessage;
    private DatabaseReference mDatabaseFriend;
    private ChildEventListener mChildEventListenerFriend;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (ChatUtils.getUser() == null) {
            return START_STICKY;
        }
        String userId = ChatUtils.getUser().getId();
        mDatabaseMessage = mDF.child("user").child(userId).child("lastChatId");
        mValueEventListenerMessage = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    LogManagerUtils.d(TAG, "onStartCommand: " + dataSnapshot.getKey());
                    final String chatId = dataSnapshot.getValue().toString().split("=")[0];
                    DatabaseReference dbRef = mDF.child("chat").child(chatId);
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                final Chat chat = dataSnapshot.getValue(Chat.class);
                                if (chat.getLastMessageSent() != null && ChatUtils.getUser() != null) {
                                    if (chat.getLastMessageSent().getCreator().equals(ChatUtils.getUser().getId())) {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                                loadUserData(chat);
                                updateMsgStatus(chatId, chat.getLastMessageSent().getId());
                                DatabaseReference dbRef = mDF.child("user").child(chat.getLastMessageSent().getCreator());
                                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            User sender = dataSnapshot.getValue(User.class);
                                            if (chatId.equals(ChatUtils.getCurrentChatId()) && AppUtils.isAppVisible()) {
                                                return;
                                            } else {
                                                if (mIsShowMessageNotification)
                                                    createNotification(sender, chat, null);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseMessage.addValueEventListener(mValueEventListenerMessage);

        //friend mNotification
        mDatabaseFriend = mDF.child("friend").child(userId);
        mChildEventListenerFriend = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    if (mIsShowFriendNotification)
                        createNotification(null, null, friend);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                if (mIsShowFriendNotification)
                    createNotification(null, null, friend);
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
        mDatabaseFriend.addChildEventListener(mChildEventListenerFriend);
        return START_STICKY;
    }

    @SuppressLint("NewApi")
    public void createNotification(User sender, Chat chat, Friend friend) {
        if (chat != null) {
            String lastMessageId = chat.getLastMessageSent().getId();
            if (!lastMessageId.equals(SharedPrefsUtil.getInstance().get(Constant.SP_LAST_NOTIFICATION_MESSAGE_ID, String.class))) {
                SharedPrefsUtil.getInstance().put(Constant.SP_LAST_NOTIFICATION_MESSAGE_ID, chat.getLastMessageSent().getId());
            } else {
                return;
            }

            Intent intent = new Intent(NewMessageService.this, ChatActivity.class);
            intent.putExtra(Constant.EXTRA_CHAT_ID, chat.getId());
            intent.putExtra(Constant.EXTRA_CHAT_NAME, chat.getChatName());
            LogManagerUtils.d(TAG, "show mNotification: " + chat.getId());

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent contentIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            String title = "", message = "";
            if (chat.isGroup()) {
                title = chat.getChatName();
            } else {
                title = sender.getName();
            }
            mNotification = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, "Name", importance);
                mNotificationManager.createNotificationChannel(notificationChannel);
                mNotification = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
            } else {
                mNotification = new NotificationCompat.Builder(getApplicationContext());
            }

            message = chat.getLastMessageSent().getText();
            if (TextUtils.isEmpty(message)) {
                if (chat.getLastMessageSent().getMedia() != null && !chat.getLastMessageSent().getMedia().isEmpty()) {
                    message = MyApp.resources.getString(R.string.message_sent_media);
                }
            }
            mNotification = mNotification
                    .setSmallIcon(R.drawable.ic_notification_new)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                    .setLargeIcon(BitmapFactory.decodeResource(MyApp.resources,
                            R.mipmap.ic_launcher_round))
                    .setContentIntent(contentIntent)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setAutoCancel(true);

            mNotificationManager.notify(ID_NOTIFICATION, mNotification.build());
        }
        if (friend != null) {
            String lastFriendId = friend.getUserId();
            int lastFriendStatus = friend.getStatus();
            if ((!lastFriendId.equals(SharedPrefsUtil.getInstance().get(Constant.SP_LAST_NOTIFICATION_FRIEND_ID, String.class))
                    && lastFriendStatus != SharedPrefsUtil.getInstance().get(Constant.SP_LAST_NOTIFICATION_FRIEND_STATUS, Integer.class))
                    || (lastFriendId.equals(SharedPrefsUtil.getInstance().get(Constant.SP_LAST_NOTIFICATION_FRIEND_ID, String.class))
                    && lastFriendStatus != SharedPrefsUtil.getInstance().get(Constant.SP_LAST_NOTIFICATION_FRIEND_STATUS, Integer.class))) {
                SharedPrefsUtil.getInstance().put(Constant.SP_LAST_NOTIFICATION_FRIEND_ID, lastFriendId);
                SharedPrefsUtil.getInstance().put(Constant.SP_LAST_NOTIFICATION_FRIEND_STATUS, lastFriendStatus);
            } else {
                return;
            }

            Intent intent = new Intent(NewMessageService.this, HomeActivity.class);
            intent.putExtra(Constant.EXTRA_FRIEND_ID, friend.getUserId());
            intent.putExtra(Constant.EXTRA_FRIEND_STATUS, friend.getStatus());
            LogManagerUtils.d(TAG, "show mNotification friend: " + friend.getUserId());

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);
            // Get the PendingIntent containing the entire back stack
//            PendingIntent contentIntent =
//                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);

            String title = "", message = "";

            switch (friend.getStatus()) {
                case Friend.STATUS_WAS_REQUESTED:
                    title = MyApp.resources.getString(R.string.notification_new_friend_request_from, friend.getName());
                    break;
                case Friend.STATUS_WAS_ACCEPTED:
                    title = MyApp.resources.getString(R.string.notification_accept_friend_request, friend.getName());
                    message = MyApp.resources.getString(R.string.notification_you_two_are_friend, friend.getName());
                    break;
                case Friend.STATUS_WAS_REJECTED:
                    title = MyApp.resources.getString(R.string.notification_rejected_your_friend_request, friend.getName());
                    break;
                case Friend.STATUS_WAS_BLOCKED:
                    title = MyApp.resources.getString(R.string.notification_block, friend.getName());
                    break;
                default:
                    return;
            }
            mNotification = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, "Name", importance);
                mNotificationManager.createNotificationChannel(notificationChannel);
                mNotification = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
            } else {
                mNotification = new NotificationCompat.Builder(getApplicationContext());
            }

            mNotification = mNotification
                    .setSmallIcon(R.drawable.ic_notification_new)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                    .setLargeIcon(BitmapFactory.decodeResource(MyApp.resources,
                            R.mipmap.ic_launcher_round))
                    .setContentIntent(contentIntent)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setAutoCancel(true);

            mNotificationManager.notify(ID_NOTIFICATION, mNotification.build());
        }
    }

    private void updateMsgStatus(final String chatId, final String messageId) {
        DatabaseReference df = mDF.child("message").child(chatId).
                child(messageId).child("seenUsers").child(ChatUtils.getUser().getId());
        df.setValue(1);
    }

    public void removeListener() {
        if (mDatabaseMessage != null) {
            mDatabaseMessage.removeEventListener(mValueEventListenerMessage);
        }
        if (mDatabaseFriend != null) {
            mDatabaseFriend.removeEventListener(mChildEventListenerFriend);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        LogManagerUtils.d(TAG, "onTaskRemove");
        removeListener();
        stopSelf();
        Intent intent = new Intent(this, AppKilledBroadcast.class);
        intent.setAction(Constant.BROADCAST_APP_KILL_ACTION);
        sendBroadcast(intent);
    }

    public void setShowMessageNotification(boolean isShowNoti) {
        LogManagerUtils.d(TAG, " mIsShowMessageNotification " + this.mIsShowMessageNotification + "=> " + isShowNoti);
        this.mIsShowMessageNotification = isShowNoti;
    }

    public void setmIsShowFriendNotification(boolean isShowNoti) {
        LogManagerUtils.d(TAG, " mIsShowMessageNotification " + this.mIsShowFriendNotification + "=> " + isShowNoti);
        this.mIsShowFriendNotification = isShowNoti;
    }

    class GetBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream in;
            try {

                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                return BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                bitmap = ImageUtils.getRoundedCornerBitmap(bitmap, 9);
                mSimpleContentView.setImageViewBitmap(R.id.img_avatar, bitmap);
                mNotificationManager.notify(ID_NOTIFICATION, mNotification.build());

            } else {
                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher);

                mSimpleContentView.setImageViewBitmap(R.id.img_avatar, icon);
                mNotificationManager.notify(ID_NOTIFICATION, mNotification.build());

            }
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public NewMessageService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NewMessageService.this;
        }
    }

    private void loadUserData(final Chat chat) {
        if (chat != null && chat.getUserIds() != null) {
            for (Map.Entry<String, String> entry : chat.getUserIds().entrySet()) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("user").child(entry.getValue());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            chat.addUser(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
