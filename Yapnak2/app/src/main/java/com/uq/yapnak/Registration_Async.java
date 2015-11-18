package com.uq.yapnak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.parse.ParseInstallation;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.RegisterUserEntity;

import java.io.IOException;

/**
 * Created by Joshua on 13/11/2015.
 */
public class Registration_Async extends AsyncTask<String, Void, RegisterUserEntity> {

    Register register;

    public Registration_Async(Register register) {
        this.register = register;
    }

    @Override
    protected RegisterUserEntity doInBackground(String... strings) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        RegisterUserEntity response = new RegisterUserEntity();
        try {
            response = userApi.registerUser(strings[0]).setMobNo(strings[1]).setEmail(strings[2]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(RegisterUserEntity response) {
        register.spinner.hide();
        if (Boolean.parseBoolean(response.getStatus())) {
            //Valid login details, navigate
            try {
                SharedPreferences data = register.getSharedPreferences("Yapnak", 0);
                SharedPreferences.Editor editor = data.edit();
                editor.putString("userID", response.getUserId());
                // Commit the edits!
                editor.commit();
                data = PreferenceManager.getDefaultSharedPreferences(register.getApplicationContext());
                editor = data.edit();
                editor.putBoolean("remember_me", true);
                editor.commit();
                ParseInstallation.getCurrentInstallation().put("userId", response.getUserId());
                ParseInstallation.getCurrentInstallation().saveInBackground();
                Intent intent = new Intent(register, MainList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("userId", response.getUserId());
                register.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d("debug", response.getMessage());
        }
    }
}
