package com.athena.services.vo;

import java.sql.Timestamp;
//import java.util.Calendar;
//import java.util.Date;


public class AlertPromotion {
	public AlertPromotion(){
		id = 0 ;
		description = "" ;
	}
	private int id ;
	private String description ;
	private Timestamp starttime ;
	private Timestamp endtime ;
	private int operator ;
	private int P ;
	
	public int getP() {
		return P;
	}
	public void setP(int p) {
		P = p;
	}
	public int getOperator() {
		return operator;
	}
	public void setOperator(int operator) {
		this.operator = operator;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Timestamp getStarttime() {
		return starttime;
	}
	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}
	public Timestamp getEndtime() {
		return endtime;
	}
	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}
}
