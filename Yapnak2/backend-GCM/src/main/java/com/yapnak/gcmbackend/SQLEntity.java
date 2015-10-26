package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 17/05/2015.
 */
@Entity
public class SQLEntity {

    @Id
    long id;
    double x;
    double y;
    String foodStyle;
    String name;
    String offer;
    String photo;
    double rating;
    int points;
    int showOffer;

    public int getShowOffer() {
        return showOffer;
    }

    public void setShowOffer(int showOffer) {
        this.showOffer = showOffer;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}