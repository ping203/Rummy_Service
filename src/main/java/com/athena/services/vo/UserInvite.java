package com.athena.services.vo;

//import java.util.Date;

public class UserInvite {
	private int Id;
	//Type
	private String N;
	private long AG;
	//ItemId
	private int V;
	private short Avatar;
	private String NLQ = "";
	public String getNLQ() {
		return NLQ;
	}
	public void setNLQ(String nLQ) {
		NLQ = nLQ;
	}
	public int getId() {
		return Id;
	}
	public void setId(int Id) {
		this.Id = Id;
	}
	
	public String getN() {
		return N;
	}
	public void setN(String name) {
		N = name;
	}
	public long getAG() {
		return AG;
	}
	public void setAG(long aG) {
		AG = aG;
	}
	public int getV() {
		return V;
	}
	public void setV(int v) {
		V = v;
	}
	public short getAvatar() {
		return Avatar;
	}
	public void setAvatar(short avatar) {
		Avatar = avatar;
	}
}
