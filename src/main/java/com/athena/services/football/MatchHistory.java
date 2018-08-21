package com.athena.services.football;

import com.google.gson.Gson;

import java.util.Date;

public class MatchHistory {
	
	private String MatchName;
	private int Bet; // cua dat 1-A 3-B
	private float BetValue; // ty le cua dat
	private long BetWin; 
	private long AG; 
	private String TeamA;
	private String TeamB;
	private long T;
	private String DataBet;
	private int Result;// result user bet
	private int State;
	private int B1; // so ban thang doi A
	private int B2; // so ban thang doi B
	private float RateA1; // ty le chap bong doi A
	private float RateA2; // ty le chap bong doi B
	private int Type; // 0.Chau Au 1.Chau A
	private int MatchID;
	
	private float RateA; // ty le an A
	private float RateB; // ty le an B
	private Date Time;
	private int MatchResult;
	
	public String getMatchName() {
		return MatchName;
	}

	public void setMatchName(String matchName) {
		MatchName = matchName;
	}

	public int getBet() {
		return Bet;
	}

	public void setBet(int bet) {
		Bet = bet;
	}

	public float getBetValue() {
		return BetValue;
	}

	public void setBetValue(float betValue) {
		BetValue = betValue;
	}

	public long getBetWin() {
		return BetWin;
	}

	public void setBetWin(long betWin) {
		BetWin = betWin;
	}

	public long getAG() {
		return AG;
	}

	public void setAG(long aG) {
		AG = aG;
	}

	public long getT() {
		return T;
	}

	public void setT(long t) {
		T = t;
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

	public int getResult() {
		return Result;
	}

	public void setResult(int result) {
		Result = result;
	}

	public int getState() {
		return State;
	}

	public void setState(int state) {
		State = state;
	}

	public int getB1() {
		return B1;
	}

	public void setB1(int b1) {
		B1 = b1;
	}

	public int getB2() {
		return B2;
	}

	public void setB2(int b2) {
		B2 = b2;
	}

	public String getDataBet() {
		return DataBet;
	}

	public void setDataBet(String dataBet) {
		DataBet = dataBet;
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

	public int getType() {
		return Type;
	}

	public void setType(int type) {
		Type = type;
	}

	public int getMatchID() {
		return MatchID;
	}

	public void setMatchID(int matchID) {
		MatchID = matchID;
	}

	public float getRateA() {
		return RateA;
	}

	public void setRateA(float rateA) {
		RateA = rateA;
	}

	public float getRateB() {
		return RateB;
	}

	public void setRateB(float rateB) {
		RateB = rateB;
	}

	public Date getTime() {
		return Time;
	}

	public void setTime(Date time) {
		Time = time;
	}

	public int getMatchResult() {
		return MatchResult;
	}

	public void setMatchResult(int matchResult) {
		MatchResult = matchResult;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
