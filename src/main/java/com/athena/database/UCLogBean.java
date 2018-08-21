package com.athena.database;

import com.google.gson.Gson;

public class UCLogBean {
    private String function;
    long time; // minis seconds
    Object data;

    public UCLogBean(String function, long time, Object data) {
        this.function = function;
        this.time = time;
        this.data = data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
