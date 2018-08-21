package com.athena.services.taixiu;

public class TaiXiuBet {
	private int N;
	private int M;
	private int uid ;
	
	public TaiXiuBet(int N, int M, int uid){
		this.N = N;
		this.M = M;
		this.uid = uid ;
	}
	public int getN() {
		return N;
	}
	public void setN(int n) {
		N = n;
	}
	public int getM() {
		return M;
	}
	public void setM(int m) {
		M = m;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	
}
