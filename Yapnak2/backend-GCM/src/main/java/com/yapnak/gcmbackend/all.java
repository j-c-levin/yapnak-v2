package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 09/08/2015.
 */
@Entity
public class all {
    @Id
    int clientID;
    String email;
    String password;
    int admin;
    String clientName;
    double clientX;
    double clientY;
    String clientFoodStyle;
    String clientOffer;
    String clientPhoto;
    String salt;
    double rating;

    public int getAdmin() {
        return admin;
    }

    public void setAdmin(int admin) {
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public double getClientX() {
        return clientX;
    }

    public void setClientX(double clientX) {
        this.clientX = clientX;
    }

    public double getClientY() {
        return clientY;
    }

    public void setClientY(double clientY) {
        this.clientY = clientY;
    }

    public String getClientFoodStyle() {
        return clientFoodStyle;
    }

    public void setClientFoodStyle(String clientFoodStyle) {
        this.clientFoodStyle = clientFoodStyle;
    }

    public String getClientOffer() {
        return clientOffer;
    }

    public void setClientOffer(String clientOffer) {
        this.clientOffer = clientOffer;
    }

    public String getClientPhoto() {
        return clientPhoto;
    }

    public void setClientPhoto(String clientPhoto) {
        this.clientPhoto = clientPhoto;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

}