package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Joshua on 11/11/2015.
 */
public class Search_Async extends AsyncTask<String, Void, OfferListEntity> {

    MainList mainList;
    String userId;

    public Search_Async(MainList mainList, String userId) {
        this.mainList = mainList;
        this.userId = userId;
    }

    @Override
    protected OfferListEntity doInBackground(String... strings) {
        //Search for location
        URLConnection connection = null;
        double longitude = 0.0;
        double latitude = 0.0;
        try {
            connection = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(strings[0], "UTF-8")).openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            InputStream response = connection.getInputStream();
            JSONObject json = new JSONObject(IOUtils.toString(response));
            longitude = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            latitude = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            Log.d("debug", "at: " + longitude + " " + latitude);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        OfferListEntity response = new OfferListEntity();
        try {
            response = userApi.getOffers(latitude, longitude, userId).execute();
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
            mainList.noOffers();
        }
    }
}

