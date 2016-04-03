package com.uq.yapnak;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.yapnak.gcmbackend.userEndpointApi.UserEndpointApi;

/**
 * Created by Joshua on 03/04/2016.
 */
public class UserEndpoint {

    public static UserEndpointApi userEndpointApi;

    public UserEndpoint() {
        UserEndpointApi.Builder builder = new UserEndpointApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null).setApplicationName("yapnak-app");
        userEndpointApi = builder.build();
    }
}
