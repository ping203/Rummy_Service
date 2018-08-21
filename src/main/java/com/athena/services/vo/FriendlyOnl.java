package com.athena.services.vo;

public class FriendlyOnl {
	private int Id; //ID
	private String N; //Ten ban || Nguoi yeu cau
	private int V ; //Cap Vip
	private String P ; //Hien dang o game nao
	private int A ; //Avatar
	private long F ; //Facebook Id
	
	public FriendlyOnl(){
		N = "";
		V = 0 ;
		P = "" ;
		A = 0 ;
		F = 0l ;
	}
	public FriendlyOnl(String name){
		N = name;
		V = 0 ;
		P = "";
	}
	
	public int getA() {
		return A;
	}
	public void setA(int a) {
		A = a;
	}
	public long getF() {
		return F;
	}
	public void setF(long f) {
		F = f;
	}
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public String getN() {
		return N;
	}
	public void setN(String n) {
		N = n;
	}
	public int getV() {
		return V;
	}
	public void setV(int v) {
		V = v;
	}
	public String getP() {
		return P;
	}
	public void setP(String p) {
		P = p;
	}
	
}
