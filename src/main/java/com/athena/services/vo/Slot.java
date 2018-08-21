package com.athena.services.vo;

import java.sql.Timestamp;
import java.util.Date;

public class Slot {
	public Slot(){
		strResult = "" ;
		strR = "" ;
		strW = "" ;
		bonus = 0 ;
		scatter = 0 ;
		CreateTime = new Timestamp((new Date()).getTime());
	}
	long id;
	int TotalRow;
	int Unit ;
	String strResult ;
	String strR ;
	String strW ;
	int bonus ;
	int scatter ;
	int TotalWin ;
	
	
	public int getTotalWin() {
		return TotalWin;
	}
	public void setTotalWin(int totalWin) {
		TotalWin = totalWin;
	}
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
	public int getTotalRow() {
		return TotalRow;
	}
	public void setTotalRow(int totalRow) {
		TotalRow = totalRow;
	}
	public int getUnit() {
		return Unit;
	}
	public void setUnit(int unit) {
		Unit = unit;
	}
	public String getStrResult() {
		return strResult;
	}
	public void setStrResult(String strResult) {
		this.strResult = strResult;
	}
	public String getStrR() {
		return strR;
	}
	public void setStrR(String strR) {
		this.strR = strR;
	}
	public String getStrW() {
		return strW;
	}
	public void setStrW(String strW) {
		this.strW = strW;
	}
	public int getBonus() {
		return bonus;
	}
	public void setBonus(int bonus) {
		this.bonus = bonus;
	}
	public int getScatter() {
		return scatter;
	}
	public void setScatter(int scatter) {
		this.scatter = scatter;
	}
	
}
