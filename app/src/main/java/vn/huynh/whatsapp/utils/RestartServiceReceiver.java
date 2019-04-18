package vn.huynh.whatsapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by duong on 4/3/2019.
 */

public class RestartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase("StartKilledService")) {
            //start Service
//            if (!ServiceUtils.isServiceRunning("vn.huynh.whatsapp.utils.PlayMusicService", context)) {
//                Intent i = new Intent(context, PlayMusicService.class);
//                ServiceUtils.startServiceMainOrBackgroundThread(context, i);
//            }
            Log.d(RestartServiceReceiver.class.getSimpleName(), "restart service");
            Intent i = new Intent(context, PlayMusicService.class);
            context.startService(i);
        }
    }
}
