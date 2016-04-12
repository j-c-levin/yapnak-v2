package com.uq.yapnak;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.parse.ParseInstallation;
import com.yapnak.gcmbackend.userEndpointApi.model.RegisterUserEntityv2;

import java.io.IOException;

/**
 * Created by Joshua on 13/11/2015.
 */
public class Registration_Async extends AsyncTask<String, Void, RegisterUserEntityv2> {

    Register register;
    Context context;

    public Registration_Async(Register register, Context context) {

        this.register = register;
        this.context = context;
    }

    @Override
    protected RegisterUserEntityv2 doInBackground(String... strings) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        RegisterUserEntityv2 response = new RegisterUserEntityv2();
        try {
            response = UserEndpoint.userEndpointApi.registerUserV2(strings[0]).setMobNo(strings[1]).setEmail(strings[2]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(RegisterUserEntityv2 response) {
        if (Boolean.parseBoolean(response.getStatus())) {
            //Valid login details, navigate
            Log.d("Debug", "New userID: " + response.getUserId() + " and tokens: " + response.getAccessToken() + " / " + response.getRefreshToken());
            try {
                SharedPreferences data = register.getSharedPreferences("Yapnak", 0);
                SharedPreferences.Editor editor = data.edit();
                editor.putString("userID", response.getUserId());
                editor.putString("refreshToken", response.getRefreshToken());
                editor.putString("accessToken", response.getAccessToken());
                // Commit the edits!
                editor.commit();
                data = PreferenceManager.getDefaultSharedPreferences(register.getApplicationContext());
                editor = data.edit();
                editor.putBoolean("remember_me", true);
                editor.commit();
                ParseInstallation.getCurrentInstallation().put("userId", response.getUserId());
                ParseInstallation.getCurrentInstallation().saveInBackground();
                new StripeRegister_Async(register, context).execute(response.getUserId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            register.spinner.hide();
            new Alert_Dialog(context).registrationFailed(response.getMessage());
            Log.d("debug", response.getMessage());
        }
    }
}
