package vn.huynh.whatsapp.services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by hieuapp on 02/03/2018.
 */

public class AppKilledBroadcast extends BroadcastReceiver {
    private static final String TAG = AppKilledBroadcast.class.getSimpleName();

    private NewMessageService newMessageService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            newMessageService = binder.getService();
            newMessageService.setShowNotification(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AppKilledBroadcast", intent.getAction());
        Intent intent2 = new Intent(context, NewMessageService.class);
        context.startService(intent2);
        context.getApplicationContext().bindService(intent2, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
