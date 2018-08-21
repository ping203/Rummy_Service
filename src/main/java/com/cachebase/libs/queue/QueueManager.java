/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cachebase.libs.queue;

import java.util.Map;
import org.apache.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

/**
 *
 * @author chiennguyen
 */
public class QueueManager
{
  private static Map<String, QueueManager> instances = new NonBlockingHashMap();
  private Queue queue;
//  private static Logger loggerLogin_ = Logger.getLogger("LoginandDisconnect");
  public static QueueManager getInstance(String name)
  {
    QueueManager instance = (QueueManager)instances.get(name);
    if (instance == null) {
      synchronized (QueueManager.class)
      {
        if (instance == null)
        {
          instance = new QueueManager();
          instances.put(name, instance);
        }
      }
    }
    return instance;
  }
  
  public void init(String name,int workerNum, int maxLength)
  {
    this.queue = new QueueImpl(name,workerNum, maxLength);
  }
  
  public void process()
  {
    this.queue.process();
  }
  
  public int size()
  {
    return this.queue.size();
  }
  
  public int remaining()
  {
    return this.queue.remaining();
  }
  
  public void put(QueueCommand cmd)
  {
    
    this.queue.put(cmd);
//    loggerLogin_.info("QueueRemaining " + this.queue.getQueueName() + ":" + this.queue.remaining());
  }
}
