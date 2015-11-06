package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 04/11/2015.
 */
@Entity
public class FavouritesEntity {
    @Id
    String status;
    String message;
    long offerId;
    long clientId;
    double latitude;
    double longitude;
    String clientName;
    String offerText;
    String distance;
    String clientOfferPhoto;
    double clientRating;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getOfferId() {
        return offerId;
    }

    public void setOfferId(long offerId) {
        this.offerId = offerId;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getClientOfferPhoto() {
        return clientOfferPhoto;
    }

    public void setClientOfferPhoto(String clientOfferPhoto) {
        this.clientOfferPhoto = clientOfferPhoto;
    }

    public double getClientRating() {
        return clientRating;
    }

    public void setClientRating(double clientRating) {
        this.clientRating = clientRating;
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
