package com.athena.services.vo;

import java.io.Serializable;

public class PromotionDevice implements Serializable {
	
	public PromotionDevice(int c, long ltime){
		CPro = c;
		LastLogin = ltime;
		this.CVideo = 0 ;
		this.COnline = 0 ;
		this.LastPromotionOnline = System.currentTimeMillis() ;
		this.LastPromotionVideo = System.currentTimeMillis() ;
		this.DailyPromotion = 0 ;
	}
	private int CPro;
    private long LastLogin;
    private int CVideo ;
    private int COnline ;
    private long LastPromotionOnline ;
    private long LastPromotionVideo ;
    private int DailyPromotion ;
    
    public int getDailyPromotion() {
		return DailyPromotion;
	}
	public void setDailyPromotion(int dailyPromotion) {
		DailyPromotion = dailyPromotion;
	}
	public int getCPro() {
		return CPro;
	}
	public void setCPro(int cPro) {
		CPro = cPro;
	}
	public long getLastLogin() {
		return LastLogin;
	}
	public void setLastLogin(long lastLogin) {
		LastLogin = lastLogin;
	}
	public int getCVideo() {
		return CVideo;
	}
	public void setCVideo(int cVideo) {
		CVideo = cVideo;
	}
	public int getCOnline() {
		return COnline;
	}
	public void setCOnline(int cOnline) {
		COnline = cOnline;
	}
	public long getLastPromotionOnline() {
		return LastPromotionOnline;
	}
	public void setLastPromotionOnline(long lastPromotionOnline) {
		LastPromotionOnline = lastPromotionOnline;
	}
	public long getLastPromotionVideo() {
		return LastPromotionVideo;
	}
	public void setLastPromotionVideo(long lastPromotionVideo) {
		LastPromotionVideo = lastPromotionVideo;
	}
}
