package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 20/08/2015.
 */
@Entity
public class ClientEntity {
    @Id
    long id;
    double x;
    double y;
    String foodStyle;
    String name;
    String offer1;
    String offer2;
    String offer3;
    String photo;
    double rating;
    int showOffer1;
    int showOffer2;
    int showOffer3;
    int offer3active;
    String status;
    String message;
    int offer1Id;
    int offer2Id;
    int offer3Id;
    long isActive;
    String email;

    public int getOffer3active() {
        return offer3active;
    }

    public void setOffer3active(int offer3active) {
        this.offer3active = offer3active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getIsActive() {
        return isActive;
    }

    public void setIsActive(long isActive) {
        this.isActive = isActive;
    }

    public int getOffer2Id() {
        return offer2Id;
    }

    public void setOffer2Id(int offer2Id) {
        this.offer2Id = offer2Id;
    }

    public int getOffer1Id() {
        return offer1Id;
    }

    public void setOffer1Id(int offer1Id) {
        this.offer1Id = offer1Id;
    }

    public int getOffer3Id() {
        return offer3Id;
    }

    public void setOffer3Id(int offer3Id) {
        this.offer3Id = offer3Id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getFoodStyle() {
        return foodStyle;
    }

    public void setFoodStyle(String foodStyle) {
        this.foodStyle = foodStyle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOffer1() {
        return offer1;
    }

    public void setOffer1(String offer1) {
        this.offer1 = offer1;
    }

    public String getOffer2() {
        return offer2;
    }

    public void setOffer2(String offer2) {
        this.offer2 = offer2;
    }

    public String getOffer3() {
        return offer3;
    }

    public void setOffer3(String offer3) {
        this.offer3 = offer3;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getShowOffer1() {
        return showOffer1;
    }

    public void setShowOffer1(int showOffer1) {
        this.showOffer1 = showOffer1;
    }

    public int getShowOffer2() {
        return showOffer2;
    }

    public void setShowOffer2(int showOffer2) {
        this.showOffer2 = showOffer2;
    }

    public int getShowOffer3() {
        return showOffer3;
    }

    public void setShowOffer3(int showOffer3) {
        this.showOffer3 = showOffer3;
    }
}
