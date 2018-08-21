package com.athena.services.football;

public class MatchScore {
	private String Username;
	private int Score;
	private int Avatar;
	private long AG;
	private String FacebookID;
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
	public int getScore() {
		return Score;
	}
	public void setScore(int score) {
		Score = score;
	}
	public int getAvatar() {
		return Avatar;
	}
	public void setAvatar(int avatar) {
		Avatar = avatar;
	}
	public long getAG() {
		return AG;
	}
	public void setAG(long aG) {
		AG = aG;
	}
	public String getFBID() {
		return FacebookID;
	}
	public void setFBID(String fBID) {
		FacebookID = fBID;
	}

}
