/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

/**
 *
 * @author UserXP
 */
public class ActionToPlayer {

    private int pid;
    private String data;
    private String evt;

    /**
     * Get the value of evt
     *
     * @return the value of evt
     */
    public String getEvt() {
        return evt;
    }

    /**
     * Set the value of evt
     *
     * @param evt new value of evt
     */
    public void setEvt(String evt) {
        this.evt = evt;
    }

    /**
     * Get the value of data
     *
     * @return the value of data
     */
    public String getData() {
        return data;
    }

    /**
     * Set the value of data
     *
     * @param data new value of data
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Get the value of pid
     *
     * @return the value of pid
     */
    public int getPid() {
        return pid;
    }

    /**
     * Set the value of pid
     *
     * @param pid new value of pid
     */
    public void setPid(int pid) {
        this.pid = pid;
    }

}
