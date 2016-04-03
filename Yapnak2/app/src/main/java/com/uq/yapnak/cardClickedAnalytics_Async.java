package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.yapnak.gcmbackend.userEndpointApi.model.SimpleEntity;

import java.io.IOException;

/**
 * Created by Joshua on 01/12/2015.
 */
public class cardClickedAnalytics_Async extends AsyncTask<Integer, Void, Void> {

    String userId;

    public cardClickedAnalytics_Async(String userId) {
        this.userId = userId;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        SimpleEntity response = new SimpleEntity();
        try {
            response = UserEndpoint.userEndpointApi.cardClickedAnalytics(integers[0], integers[1], userId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("debug", "card clicked analytics: " + response.getStatus() + " : " + response.getMessage());
        return null;
    }
}
