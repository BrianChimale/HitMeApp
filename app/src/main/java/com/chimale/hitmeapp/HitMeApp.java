package com.chimale.hitmeapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by HP PAVILION on 8/11/2017.
 *
 * Application class to implement the Firebase/Picasso offline capabilities
 */

public class HitMeApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Keeps whatever Firebase database query, set with the keepSynced(true) method, offline
        //Implementation, for example, within SettingsActivity.class
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //Picasso offline implementation
        //Implementation, for example, within SettingsActivity.class

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }
}
