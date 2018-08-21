/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.friends;

import java.io.Serializable;

/**
 *
 * @author UserXP
 */
public class FollowFriendObj implements Serializable  {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -5623735057903173568L;
	private long id; //id friend
	private int userid ;
	private int friendid ;
	private int isonline ;
	private short vip ;
	private long currentgold ;
	private int avatar ;
	private int level ;
	private int statusf ;
	private String friendname ;
	private int tableid ;
	private long fid;
	private String status;
	private short gameid;
	
	public FollowFriendObj() {
		id = 0l ;
		userid = 0 ;
		friendid = 0 ;
		isonline = 0 ;
		vip = 0 ;
		currentgold = 0 ;
		avatar = 0 ;
		level = 0 ;
		statusf = 0 ;
		friendname = "" ;
		tableid = 0 ;
		fid = 0;
		setStatus("...");
	}
	
	
	
	public int getTableid() {
		return tableid;
	}



	public void setTableid(int tableid) {
		this.tableid = tableid;
	}



	public String getFriendname() {
		return friendname;
	}


	public void setFriendname(String friendname) {
		this.friendname = friendname;
	}


	public int getStatusf() {
		return statusf;
	}

	public void setStatusf(int statusf) {
		this.statusf = statusf;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public int getFriendid() {
		return friendid;
	}
	public void setFriendid(int friendid) {
		this.friendid = friendid;
	}
	public int getIsonline() {
		return isonline;
	}
	public void setIsonline(int isonline) {
		this.isonline = isonline;
	}
	public short getVip() {
		return vip;
	}
	public void setVip(short vip) {
		this.vip = vip;
	}
	public long getCurrentgold() {
		return currentgold;
	}
	public void setCurrentgold(long currentgold) {
		this.currentgold = currentgold;
	}
	public int getAvatar() {
		return avatar;
	}
	public void setAvatar(int avatar) {
		this.avatar = avatar;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public long getFid() {
		return fid;
	}



	public void setFid(long fid) {
		this.fid = fid;
	}



	public short getGameid() {
		return gameid;
	}



	public void setGameid(short gameid) {
		this.gameid = gameid;
	}
	
}
