package com.uq.yapnak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.parse.ParseInstallation;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.AuthenticateEntity;

import java.io.IOException;

/**
 * Created by Joshua on 24/10/2015.
 */
public class Login_Async extends AsyncTask<String, Void, AuthenticateEntity> {

    LoginActivity login;

    public Login_Async(LoginActivity login) {
        this.login = login;
    }

    @Override
    protected AuthenticateEntity doInBackground(String...params) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(),null);
        AuthenticateEntity response = new AuthenticateEntity();
        try {
           response =  userApi.authenticateUser(params[0]).setEmail(params[1]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Stop spinner
        return response;
    }

    protected void onPostExecute(AuthenticateEntity response) {
        login.spinner.hide();
        if (Boolean.parseBoolean(response.getStatus())) {
            //Valid login details, navigate
            try {
                SharedPreferences data = login.getSharedPreferences("Yapnak", 0);
                SharedPreferences.Editor editor = data.edit();
                editor.putString("userID", response.getUserId());
                // Commit the edits!
                editor.commit();
                data = PreferenceManager.getDefaultSharedPreferences(login.getApplicationContext());
                editor = data.edit();
                editor.putBoolean("remember_me", true);
                editor.commit();
                ParseInstallation.getCurrentInstallation().put("userId",  response.getUserId());
                ParseInstallation.getCurrentInstallation().saveInBackground();
                Intent intent = new Intent(login, MainList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("userId", response.getUserId());
                login.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Incorrect login details, alert
            new Alert_Dialog(login).incorrectLogin();
        }
    }
}
