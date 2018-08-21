package com.athena.services.room;

public class RoomPlayer {
	private int 	pid;
	private String  name;
	private long 	chip;
	private short 	vip;
	private short 	avatar;
	private long 	faceid;
	private int 	tableid;
	private int 	usertype;
	
	public RoomPlayer(int pid, String name, long chip, short vip, short avatar, long faceid, int tableid){
		this.setPid(pid);
		this.setName(name);
		this.setChip(chip);
		this.setVip(vip);
		this.setAvatar(avatar);
		this.setFaceid(faceid);
		this.setTableid(tableid);
	}
	
	public RoomPlayer(int pid, String name, long chip, short vip, short avatar, long faceid, int tableid, int usertype){
		this.setPid(pid);
		this.setName(name);
		this.setChip(chip);
		this.setVip(vip);
		this.setAvatar(avatar);
		this.setFaceid(faceid);
		this.setTableid(tableid);
		this.setUsertype(usertype);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getChip() {
		return chip;
	}

	public void setChip(long chip) {
		this.chip = chip;
	}

	public short getVip() {
		return vip;
	}

	public void setVip(short vip) {
		this.vip = vip;
	}

	public short getAvatar() {
		return avatar;
	}

	public void setAvatar(short avatar) {
		this.avatar = avatar;
	}

	public long getFaceid() {
		return faceid;
	}

	public void setFaceid(long faceid) {
		this.faceid = faceid;
	}

	public int getTableid() {
		return tableid;
	}

	public void setTableid(int tableid) {
		this.tableid = tableid;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getUsertype() {
		return usertype;
	}

	public void setUsertype(int usertype) {
		this.usertype = usertype;
	}

}

