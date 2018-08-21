package com.athena.services.vo;

import java.sql.Timestamp;
import java.util.Date;

public class Roulette {
	public Roulette(){
		isBuy = false;
		status = false;	
		LQBuy = 0 ;
		LQTotal = 0 ;
		CreateTime = new Timestamp((new Date()).getTime());
	}
	long id;
	int LQBuy;
	int LQTotal ;
	boolean isBuy;
	String[] Arr ;	
	int AGWin;
	int NumberWin;
	boolean status;
	
	Timestamp CreateTime;
	
	public Timestamp getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getLQBuy() {
		return LQBuy;
	}
	public void setLQBuy(int lqbuy) {
		this.LQBuy = lqbuy;
	}
	public int getLQTotal() {
		return LQTotal;
	}
	public void setLQTotal(int lqtotal) {
		this.LQTotal = lqtotal;
	}
	public String[] getArr() {
		return Arr;
	}
	public void setArr(String[] arr) {
		Arr = arr;
	}	
	public int getAgWin() {
		return AGWin;
	}
	public void setAgWin(int agwin) {
		this.AGWin = agwin;
	}
	public int getNumberWin() {
		return NumberWin;
	}
	public void setNumberWin(int numberWin) {
		this.NumberWin = numberWin;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public boolean isBuy() {
		return isBuy;
	}
	public void setBuy(boolean isBuy) {
		this.isBuy = isBuy;
	}
}
