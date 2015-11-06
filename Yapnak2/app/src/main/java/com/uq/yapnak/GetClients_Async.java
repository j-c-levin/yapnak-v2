package com.uq.yapnak;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

import java.io.IOException;

/**
 * Created by Joshua on 27/10/2015.
 */
public class GetClients_Async extends AsyncTask<Double, Void, OfferListEntity> {

    private MainList mainList;

    public GetClients_Async(MainList mainList) {
        this.mainList = mainList;
    }

    @Override
    protected OfferListEntity doInBackground(Double... doubles) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        OfferListEntity response = new OfferListEntity();
        try {
            response = userApi.getClients(doubles[0], doubles[1]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(OfferListEntity response) {
        if (mainList.spinner != null) {
            mainList.spinner.hide();
        }
        mainList.swipeRefreshLayout.setRefreshing(false);
        if (Boolean.parseBoolean(response.getStatus())) {
            mainList.loadOffers(response);
        }
    }
}
