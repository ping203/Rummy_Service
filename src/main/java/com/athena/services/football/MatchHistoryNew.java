package com.athena.services.football;

import com.google.gson.Gson;

import java.util.ArrayList;

public class MatchHistoryNew {

	private String TeamA;
	private String TeamB;
	private float RateA1; 
	private float RateA2;
	private long Time;
	private long BetA;
	private long BetB;
	private long Win;
	private int B1;
	private int B2;
	
	private float RateA; // ty le an A
	private float RateB; // ty le an B
	private boolean Result;
	private ArrayList<String> StrBet = new ArrayList<String>();// You bet xxx Gold on TeamA
	
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

	public long getTime() {
		return Time;
	}

	public void setTime(long time) {
		Time = time;
	}

	public long getBetA() {
		return BetA;
	}

	public void setBetA(long betA) {
		BetA = betA;
	}

	public long getBetB() {
		return BetB;
	}

	public void setBetB(long betB) {
		BetB = betB;
	}

	public long getWin() {
		return Win;
	}

	public void setWin(long win) {
		Win = win;
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

	public ArrayList<String> getStrBet() {
		return StrBet;
	}

	public void setStrBet(ArrayList<String> strBet) {
		StrBet = strBet;
	}

	public boolean isResult() {
		return Result;
	}

	public void setResult(boolean result) {
		Result = result;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
