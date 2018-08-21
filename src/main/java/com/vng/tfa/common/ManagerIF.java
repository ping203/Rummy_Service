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

public abstract interface ManagerIF
{
  public abstract Connection borrowClient();
  
  public abstract void returnClient(Connection paramConnection);
  
  public abstract void invalidClient(Connection paramConnection);
}
