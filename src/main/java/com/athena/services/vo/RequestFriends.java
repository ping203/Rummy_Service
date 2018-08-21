package com.athena.services.vo;

public class RequestFriends {
	private int Id;
	private String F ;
//	private String T ;
	private int V ;
	private boolean S;
	
	public RequestFriends(){
		Id=0 ;
		F = "";
//		T = "";
		S = false;
		V = 0 ;
	}
	
	public boolean getS() {
		return S;
	}
	public void setS(boolean s) {
		S = s;
	}
	
	public String getF() {
		return F;
	}
	public void setF(String f) {
		F = f;
	}
	
	public int getV() {
		return V;
	}
	public void setV(int v) {
		V = v;
	}
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
}
