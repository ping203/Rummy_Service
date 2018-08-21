/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vng.tfa.common;

/**
 *
 * @author chiennguyen
 */
import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.commons.pool.PoolableObjectFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ClientObjectFactory
        implements PoolableObjectFactory {

    private String urlConnection;

    public ClientObjectFactory(String urlConnection) {
        this.urlConnection = urlConnection;
    }

    public void activateObject(Object arg0)
            throws Exception {
    }

    public void destroyObject(Object obj) throws Exception {
        try {
            if (obj == null) {
                return;
            }
            Connection client = (Connection) obj;
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object makeObject()
            throws Exception {
        try {
            Connection client = DriverManager.getConnection(urlConnection);
            return client;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void passivateObject(Object arg0)
            throws Exception {
    }

    public boolean validateObject(Object obj) {
        boolean result = true;
        try {
            Connection client = (Connection) obj;
            if (obj == null) {
                return result;
            }
            result = (result) && (!client.isClosed());
        } catch (Exception ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }
}
