/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cachebase.memcache;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

/**
 *
 * @author chiennguyen
 */
public class Memcache {

    private MemcachedClient _mc = null;
    private String keyInstance = "";
    private String keyLock = "_lock";
    public static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Memcache.class);
    private static final Lock _createLock = new ReentrantLock();
    private static Map<String, Memcache> _instance = new NonBlockingHashMap<String, Memcache>();

    public static Memcache getInstance(String host, int port) {
        String key = host + "_" + port;
        _createLock.lock();
        try {
            if (!_instance.containsKey(key)) {
                _instance.put(key, new Memcache(host, port));
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
            log.error(ex.getMessage(), ex);
        } finally {
            _createLock.unlock();
        }
        return _instance.get(key);
    }

    public void checkInstance() {
        if (this._mc.isShutdown()) {
            String key = this.keyInstance;

            String[] arr = key.split("_");
            if (arr.length == 2) {
                try {
                    String host = arr[0];
                    int port = Integer.valueOf(arr[1]);
                    MemcachedClientBuilder builder = new XMemcachedClientBuilder(
                            AddrUtil.getAddresses(host + ":" + port));
                    this._mc = builder.build();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    public Memcache(String host, int port) {
        try {
            MemcachedClientBuilder builder = new XMemcachedClientBuilder(
                    AddrUtil.getAddresses(host + ":" + port));
            this._mc = builder.build();
            this.keyInstance = host + "_" + port;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }
    public boolean set(String key, Object value, int expire) {
        this.checkInstance();
        boolean rs = false;
        try {
            byte[] byteValue = TfaSerialize.serialize(value);
            rs = this._mc.set(key, expire, byteValue);
        } catch (Exception ex) {
        	ex.printStackTrace();
//            log.error(ex.getMessage(), ex);
        }
        return rs;
    }

    public Object get(String key) {
        this.checkInstance();
        Object rs = null;
        try {
            byte[] byteValue = this._mc.get(key);
            if (byteValue != null) {
                rs = TfaSerialize.deSerialize(byteValue);
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
            log.error(ex.getMessage(), ex);
        }
        return rs;
    }

    public boolean remove(String key) {
        this.checkInstance();
        boolean rs = false;
        try {
            rs = this._mc.delete(key);
        }catch(Exception e){
        	e.printStackTrace();
        }
        return rs;
    }

    public Map<SocketAddress, Map<String, String>> stats() {
        Map<SocketAddress, Map<String, String>> rs = null;
        try {
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return rs;
    }
    public void lock(String key)
    {
        this.set(key + keyLock, "1", 300);
    }
    public void unlock(String key)
    {
        this.remove(key + keyLock);
    }
    public boolean checklock(String key)
    {
        boolean isLock = false;
        Object get = this.get(key + keyLock);
        if(get != null)
        {
            isLock = true;
        }
        return isLock;
    }

}
