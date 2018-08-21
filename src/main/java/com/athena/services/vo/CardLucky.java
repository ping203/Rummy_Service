package com.athena.services.vo;

import java.sql.Timestamp;
import java.util.Date;

public class CardLucky {
	public CardLucky(){
		lqBuy = 0 ;
		status = false;
		type = 0;
		CreateTime = new Timestamp((new Date()).getTime());
		timeCheck = 0 ;
	}
	long id;
	int type;
	int[] Arr;
	int agwin;
	boolean status;
	int lqBuy;
	
	
	public int getLqBuy() {
		return lqBuy;
	}
	public void setLqBuy(int lqBuy) {
		this.lqBuy = lqBuy;
	}
	Timestamp CreateTime;
	long timeCheck ;
	
	
	public long getTimeCheck() {
		return timeCheck;
	}
	public void setTimeCheck(long timeCheck) {
		this.timeCheck = timeCheck;
	}
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int[] getArr() {
		return Arr;
	}
	public void setArr(int[] arr) {
		Arr = arr;
	}
	public int getAgwin() {
		return agwin;
	}
	public void setAgwin(int agwin) {
		this.agwin = agwin;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
}
