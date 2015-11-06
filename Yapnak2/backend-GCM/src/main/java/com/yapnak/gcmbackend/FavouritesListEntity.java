package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * Created by Joshua on 05/11/2015.
 */
@Entity
public class FavouritesListEntity {
    @Id
    String status;
    String message;
    List<FavouritesEntity> favourites;

    public List<FavouritesEntity> getFavourites() {
        return favourites;
    }

    public void setFavourites(List<FavouritesEntity> favourites) {
        this.favourites = favourites;
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
