package com.uq.yapnak;

import android.os.AsyncTask;

import com.yapnak.gcmbackend.userEndpointApi.model.UserDetailsEntity;

import java.io.IOException;

/**
 * Created by Joshua on 03/11/2015.
 */
public class UserDetails_Async extends AsyncTask<String, Void, UserDetailsEntity> implements UserDetailsListener {

    UserDetailsListener listener;

    public UserDetails_Async(UserDetailsListener listener) {
        this.listener = listener;
    }

    @Override
    protected UserDetailsEntity doInBackground(String... strings) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(),null);
        UserDetailsEntity response = new UserDetailsEntity();
        try {
            response =  UserEndpoint.userEndpointApi.getUserDetails().setUserId(strings[0]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(UserDetailsEntity response) {
        if (Boolean.parseBoolean(response.getStatus())) {
            listener.onTaskComplete(response);
        }
    }

    @Override
    public void onTaskComplete(UserDetailsEntity user) {

    }
}
