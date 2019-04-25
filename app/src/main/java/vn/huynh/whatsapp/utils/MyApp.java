package vn.huynh.whatsapp.utils;

import android.app.Application;
import android.content.res.Resources;

/**
 * Created by duong on 4/22/2019.
 */

public class MyApp extends Application {
    public static Resources resources;
    @Override
    public void onCreate() {
        super.onCreate();
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        resources = getResources();
    }
}
