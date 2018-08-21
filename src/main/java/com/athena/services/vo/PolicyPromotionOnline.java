package com.athena.services.vo;

import java.io.Serializable;

public class PolicyPromotionOnline implements Serializable {
	
	public PolicyPromotionOnline(){
		
	}
	public PolicyPromotionOnline(int np, int[] timeArr, int[] chipArr){
		this.numberP = np ;
		this.timeWaiting = timeArr ;
		this.chipBonus = chipArr ;
	}
	
	private int numberP;
    private int[] timeWaiting ;
    private int[] chipBonus ;
	public int getNumberP() {
		return numberP;
	}
	public void setNumberP(int numberP) {
		this.numberP = numberP;
	}
	public int[] getTimeWaiting() {
		return timeWaiting;
	}
	public void setTimeWaiting(int[] timeWaiting) {
		this.timeWaiting = timeWaiting;
	}
	public int[] getChipBonus() {
		return chipBonus;
	}
	public void setChipBonus(int[] chipBonus) {
		this.chipBonus = chipBonus;
	}
    
}
