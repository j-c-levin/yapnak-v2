package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 15/09/2015.
 */
public class RedemptionEntity {
    @Id
    long loyaltyPoints;
    long recommended;
    String offerText;
    String status;
    String message;
    String loyaltyRedeemedLevel;

    public String getLoyaltyRedeemedLevel() {
        return loyaltyRedeemedLevel;
    }

    public void setLoyaltyRedeemedLevel(String loyaltyRedeemedLevel) {
        this.loyaltyRedeemedLevel = loyaltyRedeemedLevel;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public long getRecommended() {
        return recommended;
    }

    public void setRecommended(long recommended) {
        this.recommended = recommended;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(long loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
