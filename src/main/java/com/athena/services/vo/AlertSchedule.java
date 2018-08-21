package com.athena.services.vo;

import java.sql.Timestamp;
//import java.util.Calendar;
//import java.util.Date;


public class AlertSchedule {
	public AlertSchedule(){
		id=0 ;
		contentm = "" ;
		typem = 1 ;
		linkm = "" ;
		imgm="" ;
	}
	private int id ;
	private String contentm ;
	private Timestamp timem ;
	private int typem ;
	private String linkm ;
	private String imgm ;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContentm() {
		return contentm;
	}
	public void setContentm(String contentm) {
		this.contentm = contentm;
	}
	public Timestamp getTimem() {
		return timem;
	}
	public void setTimem(Timestamp timem) {
		this.timem = timem;
	}
	public int getTypem() {
		return typem;
	}
	public void setTypem(int typem) {
		this.typem = typem;
	}
	public String getLinkm() {
		return linkm;
	}
	public void setLinkm(String linkm) {
		this.linkm = linkm;
	}
	public String getImgm() {
		return imgm;
	}
	public void setImgm(String imgm) {
		this.imgm = imgm;
	}
	
	
}
