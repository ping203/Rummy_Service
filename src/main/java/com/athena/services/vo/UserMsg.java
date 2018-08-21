package com.athena.services.vo;

//import java.util.Date;

public class UserMsg {
	private int Id;
	//Type
	private short T;
	private short Vip ;
	private String From;
	private String To;
	private int AG;
	//ItemId
	private short I;
	private String Msg;
	private Long Time;
	private int DT ;//Diamond Type
	//isread
	private boolean S;
	private boolean D;
	
	
	public int getDT() {
		return DT;
	}
	public void setDT(int dT) {
		DT = dT;
	}
	public boolean isD() {
		return D;
	}
	public void setD(boolean d) {
		D = d;
	}
	public boolean isS() {
		return S;
	}
	public void setS(boolean s) {
		S = s;
	}
	public int getId() {
		return Id;
	}
	public void setId(int Id) {
		this.Id = Id;
	}
	public short getVip() {
		return Vip;
	}
	public void setVip(short vip) {
		this.Vip = vip;
	}
	public short getT() {
		return T;
	}
	public void setT(short t) {
		T = t;
	}
	public String getFrom() {
		return From;
	}
	public void setFrom(String from) {
		From = from;
	}
	public String getTo() {
		return To;
	}
	public void setTo(String to) {
		To = to;
	}
	public int getAG() {
		return AG;
	}
	public void setAG(int aG) {
		AG = aG;
	}
	public short getI() {
		return I;
	}
	public void setI(short i) {
		I = i;
	}
	public String getMsg() {
		return Msg;
	}
	public void setMsg(String msg) {
		Msg = msg;
	}
	public Long getTime() {
		return Time;
	}
	public void setTime(Long time) {
		Time = time;
	}
	
	
}
