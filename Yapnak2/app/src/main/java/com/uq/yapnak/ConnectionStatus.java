package com.uq.yapnak;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Joshua on 26/10/2015.
 */
public class ConnectionStatus {

    Context context;

    public ConnectionStatus(Context context) {
        this.context = context;
    }

    public boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifi = false;
        try {
           wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        } catch (Exception e) {
            Log.w("debug", "Wifi error: " + e);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean lte = false;
        if (prefs.getBoolean("data", false)) {
            try {
                lte = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
            } catch (Exception e) {
                Log.w("debug", "Mobile internet error: " + e);
            }
        }
        if(!(wifi||lte)) {
            new Alert_Dialog(context).internetConnectionError();
        }
        return (wifi||lte);
    }
}
