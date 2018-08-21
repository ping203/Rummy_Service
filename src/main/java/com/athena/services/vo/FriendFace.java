/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

/**
 *
 * @author UserXP
 */
public class FriendFace {
    
	private long id;
	private boolean installed ;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public boolean isInstalled() {
		return installed;
	}
	public void setInstalled(boolean installed) {
		this.installed = installed;
	}
}
