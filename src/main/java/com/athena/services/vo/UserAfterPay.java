package com.athena.services.vo;

//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;

public class UserAfterPay {
	
	public UserAfterPay(){
		lqinday = 0 ;
	}
	private int userid ;
	private int ag ;
	private short vip ;
	private int lq ;
	private long dm ;
	private int lqinday ;
	
	
	public int getLqinday() {
		return lqinday;
	}
	public void setLqinday(int lqinday) {
		this.lqinday = lqinday;
	}
	public long getDm() {
		return dm;
	}
	public void setDm(long dm) {
		this.dm = dm;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public int getAg() {
		return ag;
	}
	public void setAg(int ag) {
		this.ag = ag;
	}
	public short getVip() {
		return vip;
	}
	public void setVip(short vip) {
		this.vip = vip;
	}
	public int getLq() {
		return lq;
	}
	public void setLq(int lq) {
		this.lq = lq;
	}
	
}
