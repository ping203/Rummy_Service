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
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientPoolByHost {

    private static  Logger log = LoggerFactory.getLogger(ClientPoolByHost.class);
    public static final int DEFAULT_MAX_ACTIVE = 512;
    public static final long DEFAULT_MAX_WAITTIME_WHEN_EXHAUSTED = -1L;
    public static final int DEFAULT_MAX_IDLE = 5;
    private ClientObjectFactory clientFactory;
    private int maxActive;
    private int maxIdle;
    private long maxWaitTimeWhenExhausted;
    private GenericObjectPool pool;
    

    public ClientPoolByHost(String urlConnection) {
        this.clientFactory = new ClientObjectFactory(urlConnection);
        this.maxActive = DEFAULT_MAX_ACTIVE;
        this.maxIdle = DEFAULT_MAX_IDLE;
        this.maxWaitTimeWhenExhausted = -1L;
        this.pool = createPool();

        this.pool.setMinEvictableIdleTimeMillis(50000L);
        this.pool.setTimeBetweenEvictionRunsMillis(55000L);
    }

    public void close() {
        try {
            this.pool.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private GenericObjectPool createPool() {
        GenericObjectPoolFactory poolFactory = new GenericObjectPoolFactory(this.clientFactory, this.maxActive, (byte) 1, this.maxWaitTimeWhenExhausted, this.maxIdle);

        GenericObjectPool p = (GenericObjectPool) poolFactory.createPool();
        p.setTestOnBorrow(true);
        p.setTestWhileIdle(true);
        p.setMaxIdle(-1);
        return p;
    }

    public Connection borrowClient() {
        Connection client = null;
        try {
            if(this.pool.getNumActive() >= DEFAULT_MAX_ACTIVE)
            {
            	return null;
            }/*else
            	log.info("==>getNumActiveClient: "+ this.pool.getNumActive());*/
            client = (Connection) this.pool.borrowObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return client;
    }	

    public void returnObject(Connection client) {
        try {
            this.pool.returnObject(client);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void invalidClient(Connection client) {
        try {
            this.pool.invalidateObject(client);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
