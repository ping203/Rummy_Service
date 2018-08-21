/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.friends;

import java.io.Serializable;

/**
 *
 * @author UserXP
 */
public class MessageObj  implements Serializable  {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 4747324061440813251L;
	private int id; //id Message
	private int fromid ; //id nguoi gui
	private int toid ; //id nguoi nhan
	private short typemsg;
	private int gold;
	private String msg ;
	private long timemsg ;
	private short statusmsg ;
	private String fromname ;
	private String toname ;
	private int itemid ;
	private int vip ;
	private int avatar ;
	private long fid;
	
	public MessageObj() {
		id = 0 ;
		fromid = 0 ;
		toid = 0 ;
	}
	
	
	public int getVip() {
		return vip;
	}


	public void setVip(int vip) {
		this.vip = vip;
	}


	public int getAvatar() {
		return avatar;
	}


	public void setAvatar(int avatar) {
		this.avatar = avatar;
	}


	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getFromid() {
		return fromid;
	}

	public void setFromid(int fromid) {
		this.fromid = fromid;
	}

	public int getToid() {
		return toid;
	}

	public void setToid(int toid) {
		this.toid = toid;
	}

	public short getTypemsg() {
		return typemsg;
	}

	public void setTypemsg(short typemsg) {
		this.typemsg = typemsg;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public long getTimemsg() {
		return timemsg;
	}

	public void setTimemsg(long timemsg) {
		this.timemsg = timemsg;
	}

	public short getStatusmsg() {
		return statusmsg;
	}

	public void setStatusmsg(short statusmsg) {
		this.statusmsg = statusmsg;
	}

	public String getFromname() {
		return fromname;
	}

	public void setFromname(String fromname) {
		this.fromname = fromname;
	}

	public String getToname() {
		return toname;
	}

	public void setToname(String toname) {
		this.toname = toname;
	}

	public int getItemid() {
		return itemid;
	}

	public void setItemid(int itemid) {
		this.itemid = itemid;
	}


	public long getFid() {
		return fid;
	}


	public void setFid(long fid) {
		this.fid = fid;
	}

	
}
