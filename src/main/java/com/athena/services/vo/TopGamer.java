package com.athena.services.vo;

public class TopGamer {
	private String N;
	private String NLQ;
	private int A; //Phan thuong
	private long M; //Diem thanh tich
	private int Av; //Avatar
	private long Faid ; //Facebookid
	private int Gameid ; //gameid
	private int Typeid ; //Loai top: 0 - hien tai, 1 - tuan, 2 - thang, 3 - tuan truoc, 4 - thang truoc
	private int V; //Vip
	private int Id ;
	
	public TopGamer(int id, String N, String NLQ, int A, long M, int av, long faid,int gameid, int typeid, int V){
		this.Id = id;
		this.N = N;
		this.NLQ = NLQ;
		this.A = A;
		this.M = M;
		this.Av = av;
		this.Faid = faid;
		this.Gameid = gameid;
		this.Typeid = typeid;
		this.V = V;
	}
	
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public int getV() {
		return V;
	}
	public void setV(int v) {
		V = v;
	}
	public String getNLQ() {
		return NLQ;
	}
	public void setNLQ(String nLQ) {
		NLQ = nLQ;
	}
	public String getN() {
		return N;
	}
	public void setN(String n) {
		N = n;
	}
	public int getA() {
		return A;
	}
	public void setA(int a) {
		A = a;
	}
	public long getM() {
		return M;
	}
	public void setM(long m) {
		M = m;
	}
	public int getAv() {
		return Av;
	}
	public void setAv(int av) {
		Av = av;
	}
	public long getFaid() {
		return Faid;
	}
	public void setFaid(long faid) {
		Faid = faid;
	}
	public int getGameid() {
		return Gameid;
	}
	public void setGameid(int gameid) {
		Gameid = gameid;
	}
	public int getTypeid() {
		return Typeid;
	}
	public void setTypeid(int typeid) {
		Typeid = typeid;
	}
	
}
