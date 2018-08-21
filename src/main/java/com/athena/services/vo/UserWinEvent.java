package com.athena.services.vo;


public class UserWinEvent {
	public UserWinEvent(){
		id = 0 ;
		agwin = 0 ;
		username = "" ;
	}
	private int id ;
	private String username ;
	private int agwin ;
	private String msg ;
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getAgwin() {
		return agwin;
	}
	public void setAgwin(int agwin) {
		this.agwin = agwin;
	}
	
	
}
