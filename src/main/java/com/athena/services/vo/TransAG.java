/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

/**
 *
 * @author UserXP
 */
public class TransAG {

    private String FromUser;
    private int AG;
    private String WaitTime;

    /**
     * Get the value of WaitTime
     *
     * @return the value of WaitTime
     */
    public String getWaitTime() {
        return WaitTime;
    }

    /**
     * Set the value of WaitTime
     *
     * @param WaitTime new value of WaitTime
     */
    public void setWaitTime(String WaitTime) {
        this.WaitTime = WaitTime;
    }

    /**
     * Get the value of AG
     *
     * @return the value of AG
     */
    public int getAG() {
        return AG;
    }

    /**
     * Set the value of AG
     *
     * @param AG new value of AG
     */
    public void setAG(int AG) {
        this.AG = AG;
    }

    /**
     * Get the value of FromUser
     *
     * @return the value of FromUser
     */
    public String getFromUser() {
        return FromUser;
    }

    /**
     * Set the value of FromUser
     *
     * @param FromUser new value of FromUser
     */
    public void setFromUser(String FromUser) {
        this.FromUser = FromUser;
    }

}
