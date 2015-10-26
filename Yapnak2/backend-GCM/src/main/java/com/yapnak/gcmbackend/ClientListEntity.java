package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * Created by Joshua on 07/09/2015.
 */
public class ClientListEntity {
    @Id
    String status;
    String message;
    List<OfferEntity> clientList;

    public List<OfferEntity> getClientList() {
        return clientList;
    }

    public void setClientList(List<OfferEntity> clientList) {
        this.clientList = clientList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
