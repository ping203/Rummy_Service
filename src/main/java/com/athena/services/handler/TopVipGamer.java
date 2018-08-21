package com.athena.services.handler;

public class TopVipGamer {
	private int id;
	private String name;
	private int avatar;
	private long faceid;
	private int vip;
	private long chip;
	private String status;
	private boolean online;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAvatar() {
		return avatar;
	}
	public void setAvatar(int avatar) {
		this.avatar = avatar;
	}
	public long getFaceid() {
		return faceid;
	}
	public void setFaceid(long faceid) {
		this.faceid = faceid;
	}
	public long getChip() {
		return chip;
	}
	public void setChip(long chip) {
		this.chip = chip;
	}
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isOnline() {
		return online;
	}
	public void setOnline(boolean online) {
		this.online = online;
	}
}
