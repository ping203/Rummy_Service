package com.athena.services.utils;

import com.google.gson.Gson;

public class LoginLoggingModel {
    String action;
    int pid;
    long timeProcess;
    long ccu;
    long startTime;
    long endTime;
    String name;
    int source;

    public LoginLoggingModel(String action, int pid, long ccu, long startTime, long endTime) {
        this.action = action;
        this.pid = pid;
        this.ccu = ccu;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeProcess = (endTime - startTime) / 1000;
    }

    public LoginLoggingModel(String action, int pid, String name, int source, long startTime, long endTime, long ccu) {
        this.ccu = ccu;
        this.action = action;
        this.pid = pid;
        this.name = name;
        this.source = source;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeProcess = (endTime - startTime) / 1000;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
