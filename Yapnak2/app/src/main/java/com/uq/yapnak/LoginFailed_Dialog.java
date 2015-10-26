package com.uq.yapnak;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Joshua on 26/10/2015.
 */
public class LoginFailed_Dialog {

    public LoginFailed_Dialog(Context context) {
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
}
