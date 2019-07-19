package vn.huynh.whatsapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by duong on 4/3/2019.
 */

public class RestartServiceReceiver extends BroadcastReceiver {
    private static final String TAG = RestartServiceReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase("StartKilledService")) {
            //start Service
//            if (!ServiceUtils.isServiceRunning("vn.huynh.whatsapp.utils.PlayMusicService", context)) {
//                Intent i = new Intent(context, PlayMusicService.class);
//                ServiceUtils.startServiceMainOrBackgroundThread(context, i);
//            }
            LogManagerUtils.d(TAG, "restart service");
            Intent i = new Intent(context, PlayMusicService.class);
            context.startService(i);
        }
    }
}
