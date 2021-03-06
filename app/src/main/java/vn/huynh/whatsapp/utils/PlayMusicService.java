package vn.huynh.whatsapp.utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import vn.huynh.whatsapp.R;

public class PlayMusicService extends Service {
    private static final String TAG = PlayMusicService.class.getSimpleName();
    private MediaPlayer mediaPlayer;

    public PlayMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hello_vietnam);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogManagerUtils.d(TAG, "on destroy");
        mediaPlayer.release();
        Intent in = new Intent(this, RestartServiceReceiver.class);
        in.setAction("StartKilledService");
        sendBroadcast(in);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        LogManagerUtils.d(TAG, "on removeFriends");
        Intent in = new Intent(this, RestartServiceReceiver.class);
        in.setAction("StartKilledService");
        sendBroadcast(in);
    }
}
