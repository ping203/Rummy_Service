package com.athena.services.vo;

import java.io.Serializable;

public class JackpotWin implements Serializable  {
	private int userid ;
	private String username ;
	private int avatar ;
	private int markUnit ;
	private int markWin ;
	private long timeWin ;
	private int vip ;
	private int diamondType ;
	
	public int getDiamondType() {
		return diamondType;
	}
	public void setDiamondType(int diamondType) {
		this.diamondType = diamondType;
	}
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getAvatar() {
		return avatar;
	}
	public void setAvatar(int avatar) {
		this.avatar = avatar;
	}
	public int getMarkUnit() {
		return markUnit;
	}
	public void setMarkUnit(int markUnit) {
		this.markUnit = markUnit;
	}
	public int getMarkWin() {
		return markWin;
	}
	public void setMarkWin(int markWin) {
		this.markWin = markWin;
	}
	public long getTimeWin() {
		return timeWin;
	}
	public void setTimeWin(long timeWin) {
		this.timeWin = timeWin;
	}
	
	
}
