package com.athena.services.vo;

import java.io.Serializable;

public class PolicyPromotionDaily implements Serializable {
	
	public PolicyPromotionDaily(){
		
	}
	public PolicyPromotionDaily(int[] bonusArr, int[] addArr){
		this.baseBonus = bonusArr ;
		this.addBonus = addArr ;
		if (bonusArr.length == 7 && addArr.length == 7)
			this.strList = bonusArr[0] + ";" + bonusArr[1] + ";" + bonusArr[2] + "_" + addArr[2] + ";" + bonusArr[3] + ";" 
						+ bonusArr[4] + "_" + addArr[4] + ";" + bonusArr[5] + ";" + bonusArr[6] + "_" + addArr[6] + ";" ;
		else
			this.strList = "" ;
	}
	
	private String strList;
    private int[] baseBonus ;
    private int[] addBonus ;
	public String getStrList() {
		return strList;
	}
	public void setStrList(String strList) {
		this.strList = strList;
	}
	public int[] getBaseBonus() {
		return baseBonus;
	}
	public void setBaseBonus(int[] baseBonus) {
		this.baseBonus = baseBonus;
	}
	public int[] getAddBonus() {
		return addBonus;
	}
	public void setAddBonus(int[] addBonus) {
		this.addBonus = addBonus;
	}
	public int GetBonusByDay(int day) {
		return baseBonus[day - 1] + addBonus[day - 1] ;
	}
}
