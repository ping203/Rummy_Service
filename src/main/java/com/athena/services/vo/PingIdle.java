package com.athena.services.vo;

import java.io.Serializable;
import java.util.Date;

public class PingIdle implements Serializable{
	
	public PingIdle(){
		TimeIdle = (new Date()).getTime();
		isPing = false;
	}
	
	private Long TimeIdle;
	private boolean isPing;
	
	public Long getTimeIdle() {
		return TimeIdle;
	}
	public void setTimeIdle(Long timeIdle) {
		TimeIdle = timeIdle;
	}
	public boolean isPing() {
		return isPing;
	}
	public void setPing(boolean isPing) {
		this.isPing = isPing;
	}
	
	
}
