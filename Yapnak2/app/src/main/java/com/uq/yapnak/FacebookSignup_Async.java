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
 * Created by Joshua on 10/11/2015.
 */
public class FacebookSignup_Async extends AsyncTask<String,Void,RegisterUserEntity> {

    Landing landing;

    public FacebookSignup_Async(Landing landing) {
        this.landing = landing;
    }

    @Override
    protected RegisterUserEntity doInBackground(String... strings) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(),null);
        RegisterUserEntity response = new RegisterUserEntity();
        try {
            response = userApi.registerUser(strings[0]).setEmail(strings[1]).setFirstName(strings[2].split(" ")[0]).setLastName(strings[2].split(" ")[1]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(RegisterUserEntity result) {
        landing.spinner.hide();
        boolean isGood = false;
        if (Boolean.parseBoolean(result.getStatus())) {
            Log.d("debug", "successfully registered: " + result.getUserId());
            isGood = true;
        } else if (result.getMessage().equals("User already registered")) {
            Log.d("debug", "user already registered: "  + result.getUserId());
            isGood = true;
        } else {
            Log.d("debug", "Facebook registration failed: " + result.getMessage());
        }
        if (isGood) {
            try {
                SharedPreferences data = landing.getSharedPreferences("Yapnak", 0);
                SharedPreferences.Editor editor = data.edit();
                editor.putString("userID", result.getUserId());
                // Commit the edits!
                editor.commit();
                data = PreferenceManager.getDefaultSharedPreferences(landing.getApplicationContext());
                editor = data.edit();
                editor.putBoolean("remember_me", true);
                editor.commit();
                ParseInstallation.getCurrentInstallation().put("userId", result.getUserId());
                ParseInstallation.getCurrentInstallation().saveInBackground();
                Intent intent = new Intent(landing, MainList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("userId", result.getUserId());
                landing.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
