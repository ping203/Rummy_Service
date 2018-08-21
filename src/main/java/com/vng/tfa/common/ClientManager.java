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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

public class ClientManager
  implements ManagerIF
{
  private ClientPoolByHost commentClientPoolByHost;
  private static final Lock createLock_ = new ReentrantLock();
  private static Map<String, ClientManager> INSTANCES = new HashMap();
  private static Logger logger_ = Logger.getLogger(ClientManager.class);
  
  public static ManagerIF getInstance(String urlConnection)
  {
    String key = urlConnection;
    if (!INSTANCES.containsKey(key))
    {
      createLock_.lock();
      try
      {
        if (!INSTANCES.containsKey(key)) {
          INSTANCES.put(key, new ClientManager(urlConnection));
        }
      }
      finally
      {
        createLock_.unlock();
      }
    }
    return (ManagerIF)INSTANCES.get(key);
  }
  
  public ClientManager(String urlConnection)
  {
    this.commentClientPoolByHost = new ClientPoolByHost(urlConnection);
  }
  
  public Connection borrowClient()
  {
    Connection client = this.commentClientPoolByHost.borrowClient();
    
    return client;
  }
  
  public void returnClient(Connection client)
  {
    this.commentClientPoolByHost.returnObject(client);
  }
  
  public void invalidClient(Connection client)
  {
    this.commentClientPoolByHost.invalidClient(client);
  }
}
