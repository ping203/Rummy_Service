/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

/**
 *
 * @author UserXP
 */
public class UserNotSession {
    
	private int uid ;
	private int source ;
	private long timecheck ;
	public UserNotSession(int iUid, int iSource, long iTime){
		uid = iUid;
		source = iSource ;
		timecheck = iTime ;
	} 
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public long getTimecheck() {
		return timecheck;
	}
	public void setTimecheck(long timecheck) {
		this.timecheck = timecheck;
	}
}
