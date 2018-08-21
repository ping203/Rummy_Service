package com.athena.services.vo;

public class UserLucky {
	
	private int userid;
	private int vip;
	private int gameid;
	private int luckyPercent ;
	private int unluckyPercent;
	private int agmin ;
	private int agmax ;
	private long timestart ;
	private long timeend ;
	
	
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
	public int getGameid() {
		return gameid;
	}
	public void setGameid(int gameid) {
		this.gameid = gameid;
	}
	public int getLuckyPercent() {
		return luckyPercent;
	}
	public void setLuckyPercent(int luckyPercent) {
		this.luckyPercent = luckyPercent;
	}
	public int getUnluckyPercent() {
		return unluckyPercent;
	}
	public void setUnluckyPercent(int unluckyPercent) {
		this.unluckyPercent = unluckyPercent;
	}
	public int getAgmin() {
		return agmin;
	}
	public void setAgmin(int agmin) {
		this.agmin = agmin;
	}
	public int getAgmax() {
		return agmax;
	}
	public void setAgmax(int agmax) {
		this.agmax = agmax;
	}
	public long getTimestart() {
		return timestart;
	}
	public void setTimestart(long timestart) {
		this.timestart = timestart;
	}
	public long getTimeend() {
		return timeend;
	}
	public void setTimeend(long timeend) {
		this.timeend = timeend;
	}
	
	
}
