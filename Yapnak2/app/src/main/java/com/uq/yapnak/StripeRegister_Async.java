package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.VoidEntity;

import java.io.IOException;

/**
 * Created by Joshua on 25/03/2016.
 */
public class StripeRegister_Async extends AsyncTask<String, Void, VoidEntity> {

    Register register;

    public StripeRegister_Async(Register register) {
        this.register = register;
    }

    @Override
    protected VoidEntity doInBackground(String... strings) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        VoidEntity response = new VoidEntity();
        try {
            response = userApi.stripeRegisterCard(strings[0], strings[1]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(VoidEntity response) {
        Log.d("debug", "Registered: " + response.toString());
    }

}
