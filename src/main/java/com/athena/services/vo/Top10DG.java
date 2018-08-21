package com.athena.services.vo;

public class Top10DG {
	private String N;
	private Long AG;
	private int uid;
	
	
	public Top10DG(UserInfo ui){
		setN(ui.getUsername());
		setAG(ui.getAG());
		setUid(ui.getUserid());
	}
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getN() {
		return N;
	}
	public void setN(String n) {
		N = n;
	}
	public Long getAG() {
		return AG;
	}
	public void setAG(Long aG) {
		AG = aG;
	}
}
