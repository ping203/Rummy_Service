/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.promotion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author UserXP
 */
public class ListPromotionObj implements Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 9175545305371530529L;
	private int userid; //id friend
	private List<PromotionObj> lsPromotion ;
	private long lastPromotion ;
	private long lastPromotionVideo ;
	private int vc ; //video curent
	private int vm ; //video max
	private int om ; //online max
	private int oc ; //online curent
	private int currentDate = 0;
	private int countInviteFacebook = 0;
	public ListPromotionObj(int userid, int vm, int om) {
		this.userid = userid ;
		lsPromotion = new ArrayList<PromotionObj>() ;
		this.vc = 0 ;
		this.vm = vm ;
		this.om = om ;
		this.oc = 0 ;
		this.lastPromotion = System.currentTimeMillis() ;
		this.lastPromotionVideo = System.currentTimeMillis() ;
	}
	
	public int getOm() {
		return om;
	}

	public void setOm(int om) {
		this.om = om;
	}

	public int getOc() {
		return oc;
	}

	public void setOc(int oc) {
		this.oc = oc;
	}

	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public List<PromotionObj> getLsPromotion() {
		return lsPromotion;
	}
	public void setLsPromotion(List<PromotionObj> lsPromotion) {
		this.lsPromotion = lsPromotion;
	}
	public long getLastPromotion() {
		return lastPromotion;
	}
	public void setLastPromotion(long lastPromotion) {
		this.lastPromotion = lastPromotion;
	}
	public int getVc() {
		return vc;
	}
	public void setVc(int vc) {
		this.vc = vc;
	}
	public int getVm() {
		return vm;
	}
	public void setVm(int vm) {
		this.vm = vm;
	}

	public long getLastPromotionVideo() {
		return lastPromotionVideo;
	}

	public void setLastPromotionVideo(long lastPromotionVideo) {
		this.lastPromotionVideo = lastPromotionVideo;
	}

	public int getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(int currentDate) {
		this.currentDate = currentDate;
	}

	public int getCountInviteFacebook() {
		return countInviteFacebook;
	}

	public void setCountInviteFacebook(int countInviteFacebook) {
		this.countInviteFacebook = countInviteFacebook;
	}
	
}
