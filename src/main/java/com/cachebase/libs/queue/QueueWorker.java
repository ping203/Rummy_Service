/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cachebase.libs.queue;

import org.apache.log4j.Logger;

/**
 *
 * @author chiennguyen
 */
public class QueueWorker
  implements Runnable
{
  private static Logger logger = Logger.getLogger(QueueWorker.class);
  private Queue queue;
  private long _msleep_idle = 1000L;
  
  public QueueWorker(Queue queue)
  {
    this.queue = queue;
  }
  
  public void run()
  {
    try
    {
      for (;;)
      {
        QueueCommand command = this.queue.take();
        if (command != null) {
          command.execute();
        } else if (this._msleep_idle > 0L) {
          Thread.sleep(this._msleep_idle);
        }
      }
    }
    catch (Exception ex)
    {
      logger.error("Error in exec QueueWorker", ex);
    }
  }
}

