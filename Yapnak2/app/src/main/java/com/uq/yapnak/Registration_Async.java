package com.uq.yapnak;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.parse.ParseInstallation;
import com.yapnak.gcmbackend.userEndpointApi.model.RegisterUserEntity;

import java.io.IOException;

/**
 * Created by Joshua on 13/11/2015.
 */
public class Registration_Async extends AsyncTask<String, Void, RegisterUserEntity> {

    Register register;
    Context context;

    public Registration_Async(Register register, Context context) {

        this.register = register;
        this.context = context;
    }

    @Override
    protected RegisterUserEntity doInBackground(String... strings) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        RegisterUserEntity response = new RegisterUserEntity();
        try {
            response = UserEndpoint.userEndpointApi.registerUser(strings[0]).setMobNo(strings[1]).setEmail(strings[2]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(RegisterUserEntity response) {
        if (Boolean.parseBoolean(response.getStatus())) {
            //Valid login details, navigate
            Log.d("Debug", "New userID: " + response.getUserId());
            new StripeRegister_Async(register, context).execute(response.getUserId());
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
