package com.uq.yapnak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.yapnak.gcmbackend.userEndpointApi.model.VoidEntity;

import java.io.IOException;

/**
 * Created by Joshua on 25/03/2016.
 */
public class StripeRegister_Async extends AsyncTask<String, Void, VoidEntity> {

    Register register;
    Context context;

    public StripeRegister_Async(Register register, Context context) {

        this.register = register;
        this.context = context;
    }

    @Override
    protected VoidEntity doInBackground(String... strings) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        VoidEntity response = new VoidEntity();
        try {
            SharedPreferences data = register.getSharedPreferences("Yapnak", 0);
            response = UserEndpoint.userEndpointApi.stripeRegisterCard(register.token, data.getString("accessToken", null), strings[0]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(VoidEntity response) {
        register.spinner.hide();
        Log.d("debug", "Stripe Registered: " + response.toString());
        if (Boolean.parseBoolean(response.getStatus())) {
            SharedPreferences data = register.getSharedPreferences("Yapnak", 0);
            Intent intent = new Intent(register, MainList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("userId", data.getString("userID", "none"));
            register.startActivity(intent);
        } else {
            try {
                new Alert_Dialog(context).cardSaveFailed(this, StripeRegister_Async.class.getMethod("failedButLoggedIn"));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public void failedButLoggedIn() {
        SharedPreferences data = register.getSharedPreferences("Yapnak", 0);
        Intent intent = new Intent(register, MainList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("userId", data.getString("userID", "none"));
        register.startActivity(intent);
    }

}
