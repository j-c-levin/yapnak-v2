package com.uq.yapnak;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;
import com.yapnak.gcmbackend.userEndpointApi.model.AboutUsEntity;

import java.io.IOException;

/**
 * Created by Joshua on 07/11/2015.
 */
public class About_Async extends AsyncTask<Void, Void, AboutUsEntity> {

    Context context;

    public About_Async(Context context) {
        this.context = context;
    }

    @Override
    protected AboutUsEntity doInBackground(Void... voids) {
        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        AboutUsEntity response = new AboutUsEntity();
        try {
            response = userApi.aboutUs().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(AboutUsEntity response) {
        if (Boolean.parseBoolean(response.getStatus())) {
            SharedPreferences data = context.getSharedPreferences("Yapnak", 0);
            SharedPreferences.Editor editor = data.edit();
            editor.putString("about", response.getAboutUs());
            editor.commit();
        }
    }
}
