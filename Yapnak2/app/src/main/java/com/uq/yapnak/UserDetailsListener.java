package com.uq.yapnak;

import com.yapnak.gcmbackend.userEndpointApi.model.UserDetailsEntity;

/**
 * Created by Joshua on 03/11/2015.
 */
public interface UserDetailsListener {
    void onTaskComplete(UserDetailsEntity user);
}
