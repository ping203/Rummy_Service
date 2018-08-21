/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.database;

/**
 *
 * @author hoangchau
 */
public class DataCacheNotFoundException extends Exception{

    private static final long serialVersionUID = 2425107702663634067L;

    public DataCacheNotFoundException() {
    }

    public DataCacheNotFoundException(String message) {
        super(message);
    }
    
}
