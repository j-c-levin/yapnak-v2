package com.uq.yapnak;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by Joshua on 02/11/2015.
 */
public class MainApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, getResources().getString(R.string.app_id), getResources().getString(R.string.key));
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
