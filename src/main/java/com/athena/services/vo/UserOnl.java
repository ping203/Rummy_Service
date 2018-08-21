package com.athena.services.vo;

public class UserOnl {
	private String N;
	private int pid;
	private short gameid;
	private short vip ;
	private short operatorid ;
	private short source ;
	private String ref ;
	
	public UserOnl(String n,Integer id,short g, short vip, short operatorid, short source, String ref){
		this.N = n;
		this.pid = id;
		this.gameid = g;
		this.vip = vip ;
		this.operatorid = operatorid ;
		this.source = source ;
		this.ref = ref ;
	}
	
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public short getSource() {
		return source;
	}

	public void setSource(short source) {
		this.source = source;
	}
	
	public short getOperatorid() {
		return operatorid;
	}

	public void setOperatorid(short operatorid) {
		this.operatorid = operatorid;
	}

	public short getVip() {
		return vip;
	}

	public void setVip(short vip) {
		this.vip = vip;
	}

	public short getGameid() {
		return gameid;
	}

	public void setGameid(short gameid) {
		this.gameid = gameid;
	}

	public String getN() {
		return N;
	}
	public void setN(String n) {
		N = n;
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}	
}
