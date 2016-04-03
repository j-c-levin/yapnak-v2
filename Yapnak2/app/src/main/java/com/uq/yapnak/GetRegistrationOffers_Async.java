package com.uq.yapnak;

import android.os.AsyncTask;

import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

import java.io.IOException;

/**
 * Created by Joshua on 13/11/2015.
 */
public class GetRegistrationOffers_Async extends AsyncTask<Double, Void, OfferListEntity> {

    private Register register;
    private String userId;

    public GetRegistrationOffers_Async(Register register, String userId) {
        this.register = register;
        this.userId = userId;
    }

    @Override
    protected OfferListEntity doInBackground(Double... doubles) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        OfferListEntity response = new OfferListEntity();
        try {
            response = UserEndpoint.userEndpointApi.getOffers(doubles[0], doubles[1], userId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(OfferListEntity response) {
        if (Boolean.parseBoolean(response.getStatus()) && response.getFoundOffers()) {
            register.loadOffers(response);
        }
    }
}
