package com.athena.services.taixiu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaiXiuHistory implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1391915716178125061L;
	/**
	 * 
	 */
	private int uid ;
	private List<TaiXiuResult> lsH ;
	
	public TaiXiuHistory(int uid){
		this.uid = uid ;
		this.lsH = new ArrayList<TaiXiuResult>();
	}
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public void AddObj(TaiXiuResult obj) {
		this.lsH.add(obj) ;
	}
	public void RemoveObj() {
		this.lsH.remove(0) ;
	}

	public List<TaiXiuResult> getLsH() {
		return lsH;
	}

	public void setLsH(List<TaiXiuResult> lsH) {
		this.lsH = lsH;
	}
	
}
