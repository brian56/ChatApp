package vn.huynh.whatsapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CheckOnlineStatusService extends Service {
    public CheckOnlineStatusService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
