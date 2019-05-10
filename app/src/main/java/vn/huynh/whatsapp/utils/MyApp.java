package vn.huynh.whatsapp.utils;

import android.app.Application;
import android.content.res.Resources;

import com.google.gson.Gson;

/**
 * Created by duong on 4/22/2019.
 */

public class MyApp extends Application {
    public static Resources resources;
    private static MyApp mSelf;
    private Gson mGSon;

    public static MyApp self() {
        return mSelf;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSelf = this;
        mGSon = new Gson();
        resources = getResources();

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public Gson getGSon() {
        return mGSon;
    }
}
