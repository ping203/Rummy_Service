package com.athena.services.vo;

import java.sql.Timestamp;
//import java.util.Date;

public class RotationLucky {
	public RotationLucky(){
		lqbuy = 0 ;
	}
	long id;
	int type;
	Timestamp CreateTime;
	int agwin ;
	int numran ;
	int lqbuy ;
	
	
	
	public int getLqbuy() {
		return lqbuy;
	}
	public void setLqbuy(int lqbuy) {
		this.lqbuy = lqbuy;
	}
	public int getNumran() {
		return numran;
	}
	public void setNumran(int numran) {
		this.numran = numran;
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
	public int getAgwin() {
		return agwin;
	}
	public void setAgwin(int agwin) {
		this.agwin = agwin;
	}
	public Timestamp getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}
	
}
