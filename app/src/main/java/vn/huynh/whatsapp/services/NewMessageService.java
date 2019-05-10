package vn.huynh.whatsapp.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.util.Log;
import android.widget.RemoteViews;

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
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.AppUtils;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.ImageUtils;
import vn.huynh.whatsapp.utils.MyApp;

/**
 * Created by duong on 4/26/2019.
 */

public class NewMessageService extends Service {
    public static String TAG = NewMessageService.class.getSimpleName();
    private NotificationCompat.Builder notification;
    public static int ID_NOTIFICATION = 9;
    boolean showNotification = true;
    private static String NOTIFICATION_CHANNEL = "WHATSAPP_CHANNEL_ID";

    NotificationManager notificationManager;
    RemoteViews simpleContentView;

    private final IBinder mBinder = new LocalBinder();
    private DatabaseReference mDF = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mDatabase;
    private ValueEventListener valueEventListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String userId = ChatUtils.getCurrentUserId();
        mDatabase = mDF.child("user").child(userId).child("lastChatId");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onStartCommand: " + dataSnapshot.getKey());
                    final String chatId = dataSnapshot.getValue().toString().split("=")[0];
                    DatabaseReference dbRef = mDF.child("chat").child(chatId);
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                final Chat chat = dataSnapshot.getValue(Chat.class);
                                if (chat.getLastMessageSent() != null) {
                                    if (chat.getLastMessageSent().getCreator().equals(ChatUtils.getCurrentUserId())) {
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
                                                if (showNotification)
                                                    createNotification(sender, chat);
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
        mDatabase.addValueEventListener(valueEventListener);
        return START_REDELIVER_INTENT;
    }

    @SuppressLint("NewApi")
    public void createNotification(User sender, Chat chat) {
//        User friend = chat.getFriend();

        simpleContentView = new RemoteViews(getApplicationContext().getPackageName(),
                R.layout.notification_new_message);
        Intent intent = new Intent(NewMessageService.this, ChatActivity.class);
        intent.putExtra(Constant.EXTRA_CHAT_ID, chat.getId());
        intent.putExtra(Constant.EXTRA_CHAT_NAME, chat.getChatName());
        Log.d(TAG, "show notification: " + chat.getId());
        String title = "", message = "";
        if (chat.isGroup()) {
            title = chat.getChatName();
        } else {
            title = sender.getName();
        }
        PendingIntent contentIntent = PendingIntent.getActivity(NewMessageService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        /*notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher_new)
                .setLargeIcon(BitmapFactory.decodeResource(MyApp.resources,
                        R.mipmap.ic_launcher_new))
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setDefaults(Notification.DEFAULT_ALL)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);
*/
        notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, "Name", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            notification = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
        } else {
            notification = new NotificationCompat.Builder(getApplicationContext());
        }

        message = chat.getLastMessageSent().getText();
        if (TextUtils.isEmpty(message)) {
            if (chat.getLastMessageSent().getMedia() != null && !chat.getLastMessageSent().getMedia().isEmpty()) {
                message = MyApp.resources.getString(R.string.message_sent_media);
            }
        }
        notification = notification
                .setSmallIcon(R.drawable.ic_notification_new)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setLargeIcon(BitmapFactory.decodeResource(MyApp.resources,
                        R.mipmap.ic_launcher_new))
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);

        /*notification.setContent(simpleContentView);

        new GetBitmapFromUrl().execute(sender.getAvatar());
        simpleContentView.setTextViewText(R.id.txt_chat_name, title);
        message = chat.getLastMessageSent().getText();
        if (TextUtils.isEmpty(message)) {
            if (chat.getLastMessageSent().getMedia() != null && !chat.getLastMessageSent().getMedia().isEmpty()) {
                message = MyApp.resources.getString(R.string.message_sent_media);
            }
        }
        simpleContentView.setTextViewText(R.id.txt_message, message);
        String time = DateUtils.formatTimeWithMarker(chat.getLastMessageSent().getCreateDateInLong());
        simpleContentView.setTextViewText(R.id.tv_time, time);
        if (chat.getLastMessageSent().getType() == Message.TYPE_TEXT) {
            //sent text
            simpleContentView.setTextViewCompoundDrawables(R.id.txt_message, 0, 0, 0, 0);
        } else {
            //sent media
            simpleContentView.setTextViewCompoundDrawables(R.id.txt_message, 0, 0, R.drawable.ic_photo_size_select_actual_black_24dp, 0);
        }
        if (chat.isGroup()) {
            simpleContentView.setTextViewText(R.id.tv_creator, chat.getLastMessageSent().getCreatorName() + ": ");
        } else {
            simpleContentView.setTextViewText(R.id.tv_creator, "");
        }*/

        notificationManager.notify(ID_NOTIFICATION, notification.build());

    }

    private void updateMsgStatus(final String chatId, final String messageId) {
        DatabaseReference df = mDF.child("message").child(chatId).
                child(messageId).child("seenUsers").child(ChatUtils.getCurrentUserId());
        df.setValue(1);
    }

    public void removeListener() {
        if (mDatabase != null) {
            mDatabase.removeEventListener(valueEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListener();
        Intent intent = new Intent();
        intent.setAction(Constant.BROADCAST_APP_KILL_ACTION);
        sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        removeListener();
        Intent intent = new Intent();
        intent.setAction(Constant.BROADCAST_APP_KILL_ACTION);
        sendBroadcast(intent);
    }

    public void setShowNotification(boolean isShowNoti) {
        Log.d("Noti_NOTIFICATION", " showNotification " + this.showNotification + "=> " + isShowNoti);
        this.showNotification = isShowNoti;
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
                simpleContentView.setImageViewBitmap(R.id.img_avatar, bitmap);
                notificationManager.notify(ID_NOTIFICATION, notification.build());

            } else {
                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher_new);

                simpleContentView.setImageViewBitmap(R.id.img_avatar, icon);
                notificationManager.notify(ID_NOTIFICATION, notification.build());

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
