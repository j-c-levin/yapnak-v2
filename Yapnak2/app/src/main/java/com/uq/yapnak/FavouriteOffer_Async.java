package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.FavouriteOfferEntity;

import java.io.IOException;

/**
 * Created by Joshua on 02/11/2015.
 */
public class FavouriteOffer_Async extends AsyncTask<String, Void, FavouriteOfferEntity> {

    @Override
    protected FavouriteOfferEntity doInBackground(String...values) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(),null);
        FavouriteOfferEntity response = new FavouriteOfferEntity();
        try {
            response = userApi.favouriteOffer(Integer.decode(values[0]), values[1]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(FavouriteOfferEntity response){
        if (Boolean.parseBoolean(response.getStatus())) {
            Log.d("debug", "Added favourite successfully");
        } else {
            try {
                Log.d("debug", "Favourite adding failed: " + response.toPrettyString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
