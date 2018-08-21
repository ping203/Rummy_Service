/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

//import java.io.Serializable;

/**
 *
 * @author UserXP
 */
public class ObjectLevel {

    private int Id;
    private String Name;
    private int MinMark;
    private int MaxMark;

    /**
     * Get the value of MaxMark
     *
     * @return the value of MaxMark
     */
    public int getMaxMark() {
        return MaxMark;
    }

    /**
     * Set the value of MaxMark
     *
     * @param MaxMark new value of MaxMark
     */
    public void setMaxMark(int MaxMark) {
        this.MaxMark = MaxMark;
    }

    /**
     * Get the value of MinMark
     *
     * @return the value of MinMark
     */
    public int getMinMark() {
        return MinMark;
    }

    /**
     * Set the value of MinMark
     *
     * @param MinMark new value of MinMark
     */
    public void setMinMark(int MinMark) {
        this.MinMark = MinMark;
    }

    /**
     * Get the value of Name
     *
     * @return the value of Name
     */
    public String getName() {
        return Name;
    }

    /**
     * Set the value of Name
     *
     * @param Name new value of Name
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     * Get the value of Id
     *
     * @return the value of Id
     */
    public int getId() {
        return Id;
    }

    /**
     * Set the value of Id
     *
     * @param Id new value of Id
     */
    public void setId(int Id) {
        this.Id = Id;
    }

}
