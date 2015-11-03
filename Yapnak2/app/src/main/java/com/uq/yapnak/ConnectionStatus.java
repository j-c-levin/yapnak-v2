package com.uq.yapnak;

import android.content.Context;
import android.net.ConnectivityManager;
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
        boolean lte = false;
        try {
            lte = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        } catch (Exception e) {
            Log.w("debug", "Mobile internet error: " + e);
        }
        if(!(wifi||lte)) {
            new InternetConnectionError_Dialog(context);
        }
        Log.d("debug", String.valueOf((wifi||lte)));
        return (wifi||lte);
    }
}
