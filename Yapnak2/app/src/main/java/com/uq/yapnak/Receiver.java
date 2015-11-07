package com.uq.yapnak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Joshua on 07/11/2015.
 */
public class Receiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("notifications", false)) {
            super.onPushReceive(context, intent);
        }
    }
}