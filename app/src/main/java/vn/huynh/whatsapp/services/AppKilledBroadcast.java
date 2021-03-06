package vn.huynh.whatsapp.services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import vn.huynh.whatsapp.utils.LogManagerUtils;
import vn.huynh.whatsapp.utils.ServiceUtils;

/**
 * Created by hieuapp on 02/03/2018.
 */

public class AppKilledBroadcast extends BroadcastReceiver {
    private static final String TAG = AppKilledBroadcast.class.getSimpleName();

    private NewMessageService mNewMessageService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            mNewMessageService = binder.getService();
            mNewMessageService.setShowMessageNotification(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        LogManagerUtils.d(TAG, intent.getAction());
//        Toast.makeText(context, "app killed", Toast.LENGTH_LONG).show();
        if (!ServiceUtils.isServiceRunning(NewMessageService.class.getCanonicalName(), context)) {
//            Toast.makeText(context, "app killed: start service", Toast.LENGTH_LONG).show();
            LogManagerUtils.d(TAG, "restart service");
            Intent intent2 = new Intent(context, NewMessageService.class);
            context.startService(intent2);
            context.getApplicationContext().bindService(intent2, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
