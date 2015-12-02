package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.SimpleEntity;

import java.io.IOException;

/**
 * Created by Joshua on 01/12/2015.
 */
public class offerRefreshAnalytics_Async extends AsyncTask<Double, Void, Void> {

    String userId;

    public offerRefreshAnalytics_Async(String userId) {
        this.userId = userId;
    }

    @Override
    protected Void doInBackground(Double... doubles) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        SimpleEntity response = new SimpleEntity();
        try {
            response = userApi.offerRefreshAnalytics(doubles[0], doubles[1], userId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("debug", "refresh analytics: " + response.getStatus() + " : " + response.getMessage());
        return null;
    }
}
