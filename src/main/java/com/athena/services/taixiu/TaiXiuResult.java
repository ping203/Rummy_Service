package com.athena.services.taixiu;

import java.io.Serializable;

public class TaiXiuResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7109143777902569462L;
	private int L; //Low
	private int H; //High
	private int R ; //Refund
	private long W; //Win
	private long T;
	private int[] Arr ;
	private long IdBet ;
	private int userid ;
	private int SB; //StatusBet
	private int S; //Source Userid
	private long TStop; //Thoi gian Stop
	
	public TaiXiuResult(long id, int r, int h, int l, long w, long t, int[] arr, int userid, int s, long tstop){
		this.H = h;
		this.L = l;
		this.R = r ;
		this.W = w ;
		this.T = t ;
		this.Arr = arr ;
		this.IdBet = id ;
		this.userid = userid ;
		this.SB = 0 ;
		this.S = s ;
		this.TStop = tstop ;
	}
	
	
	public long getTStop() {
		return TStop;
	}

	public void setTStop(long tStop) {
		TStop = tStop;
	}

	public int getS() {
		return S;
	}

	public void setS(int s) {
		S = s;
	}

	public int getSB() {
		return SB;
	}

	public void setSB(int sB) {
		SB = sB;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public long getIdBet() {
		return IdBet;
	}

	public void setIdBet(long idBet) {
		IdBet = idBet;
	}

	public int getR() {
		return R;
	}

	public void setR(int r) {
		R = r;
	}

	public int getL() {
		return L;
	}

	public void setL(int l) {
		L = l;
	}

	public int getH() {
		return H;
	}

	public void setH(int h) {
		H = h;
	}

	public long getW() {
		return W;
	}

	public void setW(long w) {
		W = w;
	}

	public long getT() {
		return T;
	}

	public void setT(long t) {
		T = t;
	}

	public int[] getArr() {
		return Arr;
	}

	public void setArr(int[] arr) {
		Arr = arr;
	}
	
}
