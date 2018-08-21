package com.athena.services.football;

public class MatchTop {

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

	public String getUsername() {
		return Username;
	}

	public void setUsername(String username) {
		Username = username;
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

	public int getState() {
		return State;
	}

	public void setState(int state) {
		State = state;
	}

	public int getType() {
		return Type;
	}

	public void setType(int type) {
		Type = type;
	}

	private String Username; //UserName
	private long AG;
	private long T; // Time
	private String TeamA;
	private String TeamB;
	private int B1;
	private int B2;
	private int State;
	private int Type;// 0.Chau Au 1.Chau A
	
}
