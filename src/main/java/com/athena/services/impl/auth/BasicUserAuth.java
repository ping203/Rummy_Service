package com.athena.services.impl.auth;

import com.google.gson.Gson;

public class BasicUserAuth {
    private String username;
    private AuthType type; //nomal NM, face: FB
    private int userId;

    public BasicUserAuth(String username, AuthType type, int userId) {
        this.username = username;
        this.type = type;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuthType getType() {
        return type;
    }

    public void setType(AuthType type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
