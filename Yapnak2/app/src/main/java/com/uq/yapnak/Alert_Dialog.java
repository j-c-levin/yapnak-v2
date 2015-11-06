package com.uq.yapnak;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;

/**
 * Created by Joshua on 26/10/2015.
 */
public class Alert_Dialog {

    Context context;

    public Alert_Dialog(Context context) {
        this.context = context;
    }

    public void incorrectLogin() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.login_error_title)
                .setMessage(R.string.login_error)
                .setPositiveButton(R.string.login_error_try_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .show();
    }

    public void connectionError(ConnectionResult r) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.connection_error_title)
                .setMessage(R.string.connection_error + " " + r.toString())
                .setPositiveButton(R.string.connection_error_try_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .show();
    }

    public void gpsError() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.gps_error_title)
                .setMessage(R.string.gps_error)
                .setPositiveButton(R.string.gps_error_try_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        if (intent.resolveActivity((context.getPackageManager())) != null) {
                            context.startActivity(intent);
                        }
                    }
                })
                .show();
    }

    public void internetConnectionError() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.internet_error_title)
                .setMessage(R.string.internet_error)
                .setPositiveButton(R.string.internet_error_try_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        if (intent.resolveActivity((context.getPackageManager())) != null) {
                            context.startActivity(intent);
                        }
                    }
                })
                .setNegativeButton(R.string.internet_error_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

}
