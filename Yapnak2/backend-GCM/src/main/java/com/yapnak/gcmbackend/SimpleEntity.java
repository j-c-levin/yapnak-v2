package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 08/09/2015.
 */
public class SimpleEntity {
    @Id
    String status;
    String message;

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
