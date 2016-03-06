package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Entity;

/**
 * Created by Joshua on 06/03/2016.
 */
@Entity
public class ClientTokenEntity extends VoidEntity{

    String clientToken;

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

}
