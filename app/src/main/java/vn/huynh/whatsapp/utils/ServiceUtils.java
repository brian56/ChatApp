package vn.huynh.whatsapp.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Created by duong on 4/3/2019.
 */

public class ServiceUtils {
    private static final String TAG = ServiceUtils.class.getSimpleName();
    public static boolean isServiceRunning(String serviceName, Context context) {
        if (context == null)
            return false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName.equals(serviceInfo.service.getClassName())) {
                    return true;
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public static void startServiceMainOrBackgroundThread(Context context, Intent service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startForegroundService(service);
            } catch (Exception e) {
                try {
                    context.startService(service);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } else {
            try {
                context.startService(service);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
