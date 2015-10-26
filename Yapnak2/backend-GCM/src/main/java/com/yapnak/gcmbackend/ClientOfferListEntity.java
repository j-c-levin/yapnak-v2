package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * Created by Joshua on 08/09/2015.
 */
@Entity
public class ClientOfferListEntity {
    @Id
    String status;
    String message;
    List<ClientOfferEntity> offerList;

    public List<ClientOfferEntity> getOfferList() {
        return offerList;
    }

    public void setOfferList(List<ClientOfferEntity> offerList) {
        this.offerList = offerList;
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
