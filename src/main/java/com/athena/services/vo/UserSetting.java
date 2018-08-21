package com.athena.services.vo;

import java.io.Serializable;

public class UserSetting implements Serializable{
	
	public UserSetting(){
		this.id = 0 ;
		this.userid = 0 ;
		this.bm = true ;
		this.s = true ;
		this.i = true ;
		this.XA = 1 ;
	}
	
	private int id ;
	private int userid ;
	private boolean bm; //ackgroundMusic ;
	private boolean s ; //sound
	private boolean i ; //invite
	private short XA ; //Xito Avatar
	
	public short getXA() {
		return XA;
	}
	public void setXA(short xA) {
		XA = xA;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public boolean isBm() {
		return bm;
	}
	public void setBm(boolean bm) {
		this.bm = bm;
	}
	public boolean isS() {
		return s;
	}
	public void setS(boolean s) {
		this.s = s;
	}
	public boolean isI() {
		return i;
	}
	public void setI(boolean i) {
		this.i = i;
	}
}
