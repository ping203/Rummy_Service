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
public class ListFollowFriendObj implements Serializable  {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 3668051007020192744L;
	private int userid; //id friend
	private List<FollowFriendObj> lsFriend ;
	public ListFollowFriendObj(int userid) {
		this.userid = userid ;
		lsFriend = new ArrayList<FollowFriendObj>() ;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public List<FollowFriendObj> getLsFriend() {
		return lsFriend;
	}
	public void setLsFriend(List<FollowFriendObj> lsFriend) {
		this.lsFriend = lsFriend;
	}
}
