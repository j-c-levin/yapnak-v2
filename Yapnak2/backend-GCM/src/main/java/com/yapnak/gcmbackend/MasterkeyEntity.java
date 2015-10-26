package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 26/09/2015.
 */
public class MasterkeyEntity {
    @Id
    String status;
    String message;
    String masterkey;

    public String getMasterkey() {
        return masterkey;
    }

    public void setMasterkey(String masterkey) {
        this.masterkey = masterkey;
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
