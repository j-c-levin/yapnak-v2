package com.uq.yapnak;

import android.os.AsyncTask;

import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

import java.io.IOException;

/**
 * Created by Joshua on 06/11/2015.
 */
public class GetFavourites_Async extends AsyncTask<Double, Void, OfferListEntity> {

    private MainList mainList;
    private String userId;

    public GetFavourites_Async(MainList mainList, String userId) {
        this.mainList = mainList;
        this.userId = userId;
    }

    @Override
    protected OfferListEntity doInBackground(Double... doubles) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        OfferListEntity response = new OfferListEntity();
        try {
            response = UserEndpoint.userEndpointApi.getFavourites(doubles[0], doubles[1], userId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(OfferListEntity response) {
        mainList.swipeRefreshLayout.setRefreshing(false);
        if (Boolean.parseBoolean(response.getStatus()) && response.getFoundOffers()) {
            mainList.loadOffers(response);
        } else {
            mainList.noFavourites();
        }
    }
}
