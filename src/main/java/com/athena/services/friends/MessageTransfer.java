/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.friends;

/**
 *
 * @author UserXP
 */
public class MessageTransfer {
    
	private int fromid ; //id nguoi gui
	private int toid ; //id nguoi nhan
	private int count;
	private String fromname ;
	private String toname ;
	private int vip ;
	private int avatar ;
	private String title ;
	private long id ;
	private long msgtime ;
	private long fid;
	
	public MessageTransfer(int fromid, int toid, String fromname,String toname, int count, int vip, int avatar,
								String title, long id, long msgtime, long fid) {
		this.fromid = fromid ;
		this.toid = toid ;
		this.fromname = fromname;
		this.toname = toname ;
		this.count = count ;
		this.vip = vip ;
		this.avatar = avatar ;
		this.title = title ;
		this.id = id ;
		this.msgtime = msgtime ;
		this.fid = fid;
	}

	
	public long getMsgtime() {
		return msgtime;
	}

	public void setMsgtime(long msgtime) {
		this.msgtime = msgtime;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
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

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getToname() {
		return toname;
	}

	public void setToname(String toname) {
		this.toname = toname;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getFromname() {
		return fromname;
	}


	public void setFromname(String fromname) {
		this.fromname = fromname;
	}


	public long getFid() {
		return fid;
	}


	public void setFid(long fid) {
		this.fid = fid;
	}
	
	
}
