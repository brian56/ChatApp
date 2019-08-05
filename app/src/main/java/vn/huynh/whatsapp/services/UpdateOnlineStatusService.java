package vn.huynh.whatsapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Timer;
import java.util.TimerTask;

import vn.huynh.whatsapp.utils.AppUtils;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Config;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.LogManagerUtils;

/**
 * Created by duong on 8/5/2019.
 */


public class UpdateOnlineStatusService extends Service {
    public static String TAG = "UpdateOnlineStatusService";
    private final IBinder mBinder = new LocalBinder();
    Timer timer;

    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (AppUtils.isAppVisible()) {
                    mDatabase.child(Constant.FB_KEY_USER)
                            .child(ChatUtils.getUser().getId())
                            .child(Constant.FB_KEY_LAST_ONLINE).setValue(ServerValue.TIMESTAMP);
                    LogManagerUtils.d(TAG, "Push state online");
                }
            }
        }, 10, Config.UPDATE_ONLINE_STATUS_INTERVAL_IN_SECOND * 1000);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public UpdateOnlineStatusService getService() {
            // Return this instance of LocalService so clients can call public methods
            return UpdateOnlineStatusService.this;
        }
    }

    @Override
    public void onDestroy() {
        if (timer != null)
            timer.cancel();
        super.onDestroy();
    }
}

