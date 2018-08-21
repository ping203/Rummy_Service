package com.athena.services.football;

import java.sql.Timestamp;

public class MatchSiam {
	private int Id; 

	private float Win; // Ty le chu 
	private float Lost;// Ty le khach
	private int Result; //
	private long T;// Time
	private float RateA1; // ty le chu nha chap bong
	private float RateA2; // Ty le khach chap bong
	private int ResultB1; //  ban thang chu nha
	private int ResultB2; // ban thang khach
	private String TeamA;
	private String TeamB;
	private int MatchStatus;
	private Timestamp StopTime; //thoi gian ket thuc dat
	private int Type; // 0.Keo Chau Au 1.Keo Chau A
	
	public Timestamp getStopTime() {
		return StopTime;
	}
	public void setStopTime(Timestamp stopTime) {
		StopTime = stopTime;
	}
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public float getWin() {
		return Win;
	}
	public void setWin(float win) {
		Win = win;
	}
	public float getLost() {
		return Lost;
	}
	public void setLost(float lost) {
		Lost = lost;
	}
	public int getResult() {
		return Result;
	}
	public void setResult(int result) {
		Result = result;
	}
	public long getT() {
		return T;
	}
	public void setT(long t) {
		T = t;
	}
	public float getRateA1() {
		return RateA1;
	}
	public void setRateA1(float rateA1) {
		RateA1 = rateA1;
	}
	public float getRateA2() {
		return RateA2;
	}
	public void setRateA2(float rateA2) {
		RateA2 = rateA2;
	}
	public int getResultB1() {
		return ResultB1;
	}
	public void setResultB1(int resultB1) {
		ResultB1 = resultB1;
	}
	public int getResultB2() {
		return ResultB2;
	}
	public void setResultB2(int resultB2) {
		ResultB2 = resultB2;
	}
	public String getTeamA() {
		return TeamA;
	}
	public void setTeamA(String teamA) {
		TeamA = teamA;
	}
	public String getTeamB() {
		return TeamB;
	}
	public void setTeamB(String teamB) {
		TeamB = teamB;
	}
	public int getMatchStatus() {
		return MatchStatus;
	}
	public void setMatchStatus(int matchStatus) {
		MatchStatus = matchStatus;
	}
	public int getType() {
		return Type;
	}
	public void setType(int type) {
		Type = type;
	}
	
}
