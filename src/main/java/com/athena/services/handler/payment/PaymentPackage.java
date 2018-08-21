/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.handler.payment;

import java.io.Serializable;

/**
 *
 * @author hoangchau
 */
public class PaymentPackage implements Serializable{

    private static final long serialVersionUID = -7256234293027364271L;
    private int payType;
    private int pkgChip;
    private int pkgXValue;
    private int subcriptionDay;
    private int subcriptionChip;
    
    private long endTime;
    private long startTime;

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public int getPkgChip() {
        return pkgChip;
    }

    public void setPkgChip(int pkgChip) {
        this.pkgChip = pkgChip;
    }

    public int getPkgXValue() {
        return pkgXValue;
    }

    public void setPkgXValue(int pkgXValue) {
        this.pkgXValue = pkgXValue;
    }

    public int getSubcriptionDay() {
        return subcriptionDay;
    }

    public void setSubcriptionDay(int subcriptionDay) {
        this.subcriptionDay = subcriptionDay;
    }

    public int getSubcriptionChip() {
        return subcriptionChip;
    }

    public void setSubcriptionChip(int subcriptionChip) {
        this.subcriptionChip = subcriptionChip;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
 
    
}
