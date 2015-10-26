package com.yapnak.gcmbackend;

import java.util.List;

/**
 * Created by Joshua on 25/06/2015.
 */
public class SQLList {

    private List<SQLEntity> ObjectsList;

    public List<SQLEntity> getList() {
        return ObjectsList;
    }

    public void setList(List<SQLEntity> objectsList) {
        ObjectsList = objectsList;
    }


}