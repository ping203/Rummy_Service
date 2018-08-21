/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

import java.io.Serializable;

/**
 *
 * @author UserXP
 */
public class UserLoginThai implements Serializable  {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -3514241283985813789L;
	private int returnCode;
	private String message ;
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
