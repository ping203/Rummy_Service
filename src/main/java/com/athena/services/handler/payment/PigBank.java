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
public class PigBank implements Serializable{

    private static final long serialVersionUID = 3743486613406721682L;
    private long current = 0;
    private long max = 0;

    public PigBank(long current, long max) {
        this.current = current;
        this.max = max;
    }

    public PigBank() {
    }
    
    

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }
    
    
}
