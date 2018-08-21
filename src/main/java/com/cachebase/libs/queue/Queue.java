/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cachebase.libs.queue;

/**
 *
 * @author chiennguyen
 */
public abstract interface Queue
{
  public abstract QueueCommand take();
  
  public abstract boolean put(QueueCommand paramQueueCommand);
  
  public abstract void process();
  
  public abstract int size();
  
  public abstract int remaining();
  
  public abstract int getWorkerNum();
  
  public abstract int getMaxLength();
  
  public abstract String getQueueName();
}
