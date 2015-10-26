package com.yapnak.gcmbackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * Created by Joshua on 23/08/2015.
 */
@Entity
public class SearchUserEntity {

    @Id
    String status;
    String message;
    List<String> isUser;

    public List<String> getIsUser() {
        return isUser;
    }

    public void setIsUser(List<String> isUser) {
        this.isUser = isUser;
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
}
