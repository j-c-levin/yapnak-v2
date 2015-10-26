package com.uq.yapnak;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.AuthenticateEntity;

import java.io.IOException;

/**
 * Created by Joshua on 24/10/2015.
 */
public class Login_Async extends AsyncTask<String, Void, Boolean> {

    Context login;

    public Login_Async(Context login) {
        this.login = login;
    }

    @Override
    protected Boolean doInBackground(String...params) {
        //Start spinner
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(),null);
        AuthenticateEntity response = new AuthenticateEntity();
        try {
           response = userApi.authenticateUser(params[0]).setEmail(params[1]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Stop spinner
        return Boolean.parseBoolean(response.getStatus());
    }

    protected void onPostExecute(Boolean response) {
        if (response) {
            //Valid login details, navigate
            try {
                Intent intent = new Intent(login, MainList.class);
                login.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Incorrect login details, alert
            new LoginFailed_Dialog(login);
        }
    }
}
