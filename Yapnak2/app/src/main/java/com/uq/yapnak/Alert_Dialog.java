package com.uq.yapnak;

import android.content.Context;
import android.content.DialogInterface;
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
}
