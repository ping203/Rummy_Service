/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.promotion;

import java.io.Serializable;

/**
 *
 * @author UserXP
 */
public class PromotionObj implements Serializable{
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1780602070916412465L;
	private long id; //id Message
	private int userid;
	private int gold ;
	private int typep ;//0 - Tang het tien, 1-tang dot bien, 2- tang len vip, 3-Tang Dem thoi gian, 4 - Tang vong quay, 5-Tang xem video, 6-Giftcode
	private long createtime ;
	
	public PromotionObj() {
		id = 0 ;
		gold = 0 ;
		typep = 0 ;
		createtime = System.currentTimeMillis() ;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getTypep() {
		return typep;
	}

	public void setTypep(int typep) {
		this.typep = typep;
	}

	public long getCreatetime() {
		return createtime;
	}

	public void setCreatetime(long createtime) {
		this.createtime = createtime;
	}	
	
}
