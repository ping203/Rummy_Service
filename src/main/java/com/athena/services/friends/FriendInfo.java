package com.athena.services.friends;

public class FriendInfo {
	//tên, avatar, online hay offline, vip, tiền, iduser :D
	private String name;
	private String namelq;
	private short avatar;
	private short online;
	private short vip;
	private long ag;
	private int uid;
	private int idtable;
	private String status;
	
	public FriendInfo(int uid, String name, String namelq, short vip, short online, long ag, short avatar, int idtable, String status){
		setUid(uid);
		setName(name);
		setVip(vip);
		setOnline(online);
		setAg(ag);
		setAvatar(avatar);
		setNamelq(namelq);
		setIdtable(idtable);
		setStatus(status);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public short getAvatar() {
		return avatar;
	}

	public void setAvatar(short avatar) {
		this.avatar = avatar;
	}

	public short getOnline() {
		return online;
	}

	public void setOnline(short online) {
		this.online = online;
	}

	public short getVip() {
		return vip;
	}

	public void setVip(short vip) {
		this.vip = vip;
	}

	public long getAg() {
		return ag;
	}

	public void setAg(long ag) {
		this.ag = ag;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getNamelq() {
		return namelq;
	}

	public void setNamelq(String namelq) {
		this.namelq = namelq;
	}

	public int getIdtable() {
		return idtable;
	}

	public void setIdtable(int idtable) {
		this.idtable = idtable;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
