package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.yapnak.gcmbackend.userEndpointApi.model.FavouriteOfferEntity;

import java.io.IOException;

/**
 * Created by Joshua on 06/11/2015.
 */
public class RemoveFavouriteOffer_Async extends AsyncTask<String, Void, FavouriteOfferEntity> {

    @Override
    protected FavouriteOfferEntity doInBackground(String... values) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(),null);
        FavouriteOfferEntity response = new FavouriteOfferEntity();
        try {
            response = UserEndpoint.userEndpointApi.removeFavouriteOffer(Integer.decode(values[0]), values[1]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(FavouriteOfferEntity response){
        if (Boolean.parseBoolean(response.getStatus())) {
            Log.d("debug", "Removed favourite successfully");
        } else {
            try {
                Log.d("debug", "Favourite remove failed: " + response.toPrettyString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
