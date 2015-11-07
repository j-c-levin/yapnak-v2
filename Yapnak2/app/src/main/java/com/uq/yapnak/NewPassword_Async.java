package com.uq.yapnak;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.SimpleEntity;

import java.io.IOException;

/**
 * Created by Joshua on 07/11/2015.
 */
public class NewPassword_Async extends AsyncTask<String, Void, SimpleEntity> {

    NewPassword newPassword;

    public NewPassword_Async (NewPassword newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    protected SimpleEntity doInBackground(String... strings) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(),null);
        SimpleEntity response = new SimpleEntity();
        try {
            //current, password, userId
            response = userApi.inAppPasswordReset(strings[0], strings[1], strings[2]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute (SimpleEntity response) {
        if (newPassword.spinner != null) {
            newPassword.spinner.hide();
        }
        if (Boolean.parseBoolean(response.getStatus())) {
            newPassword.passwordResetSuccess();
        } else {
            newPassword.passwordResetFail();
        }
    }
}
