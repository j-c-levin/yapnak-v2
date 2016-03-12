package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.VoidEntity;

import java.io.IOException;

/**
 * Created by Joshua on 13/11/2015.
 */
public class StripeCharge_Async extends AsyncTask<String, Void, VoidEntity> {


    public StripeCharge_Async() {

    }

    @Override
    protected VoidEntity doInBackground(String... strings) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        VoidEntity response = new VoidEntity();
        try {
            response = userApi.stripeCharge(strings[0]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(VoidEntity response) {
      Log.d("debug", "Done: " + response.toString());
    }
}
