package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Id;

import org.json.simple.JSONArray;

/**
 * Created by Joshua on 15/09/2015.
 */
public class UserRedemptionEntity {
    @Id
    String status;
    String message;
    JSONArray available;

    public JSONArray getAvailable() {
        return available;
    }

    public void setAvailable(JSONArray available) {
        this.available = available;
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
