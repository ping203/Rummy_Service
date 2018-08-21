/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.friends;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author UserXP
 */
public class ListMessageObj implements Serializable  {
    
	private int userid; //id friend
	private List<MessageObj> lsMessage ;
	private int newMsg ;
	public ListMessageObj(int userid) {
		this.userid = userid ;
		lsMessage = new ArrayList<MessageObj>() ;
		newMsg = 0;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public List<MessageObj> getLsMessage() {
		return lsMessage;
	}
	public void setLsMessage(List<MessageObj> lsFriend) {
		this.lsMessage = lsFriend;
	}
	public int getNewMsg() {
		return newMsg;
	}
	public void setNewMsg(int newMsg) {
		this.newMsg = newMsg;
	}
}
