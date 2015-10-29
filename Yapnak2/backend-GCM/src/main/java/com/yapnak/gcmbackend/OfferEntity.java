package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 06/09/2015.
 */
public class OfferEntity {
    @Id
    long offerId;
    long clientId;
    String status;
    String message;
    double latitude;
    double longitude;
    String foodStyle;
    String clientName;
    String offerText;
    String clientPhoto;
    String distance;
    String clientOfferPhoto;

    public String getClientOfferPhoto() {
        return clientOfferPhoto;
    }

    public void setClientOfferPhoto(String clientOfferPhoto) {
        this.clientOfferPhoto = clientOfferPhoto;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhoto() {
        return clientPhoto;
    }

    public void setClientPhoto(String clientPhoto) {
        this.clientPhoto = clientPhoto;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFoodStyle() {
        return foodStyle;
    }

    public void setFoodStyle(String foodStyle) {
        this.foodStyle = foodStyle;
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
