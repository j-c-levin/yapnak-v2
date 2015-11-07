package com.uq.yapnak;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setTitle(R.string.internet_error_title)
                .setMessage(R.string.internet_error)
                .setPositiveButton(R.string.internet_error_try_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        if (intent.resolveActivity((context.getPackageManager())) != null) {
                            context.startActivity(intent);
                        }
                    }
                });
        if (prefs.getBoolean("data", false)) {
            alert.setNegativeButton(R.string.internet_error_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        } else {
            alert.setNegativeButton(R.string.internet_error_toggle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(context, AppSettings.class);
                    context.startActivity(intent);
                }
            });
        }
        alert.show();
    }

    public void passwordResetSuccess() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.password_reset_success_title)
                .setMessage(R.string.password_reset_success)
                .setPositiveButton(R.string.password_reset_success_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void passwordResetFail() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.password_reset_fail_title)
                .setMessage(R.string.password_reset_fail)
                .setPositiveButton(R.string.password_reset_fail_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void passwordMismatch() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.password_reset_mismatch_title)
                .setMessage(R.string.password_reset_mismatch)
                .setPositiveButton(R.string.password_reset_mismatch_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void passwordDetailsMissing() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.password_reset_missing_title)
                .setMessage(R.string.password_reset_missing)
                .setPositiveButton(R.string.password_reset_missing_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
