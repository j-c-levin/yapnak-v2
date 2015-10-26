package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Id;

/**
 * Created by Joshua on 19/09/2015.
 */
public class PhotoEntity {
    @Id
    String status;
    String message;
    String uploadUrl;

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
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
