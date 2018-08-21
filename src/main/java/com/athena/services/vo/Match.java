package com.athena.services.vo;

import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;

public class Match {
	
	public Match(){
		
	}
	
	private int Id;
	private String MatchName;
	private float Win;
	private float Draw;
	private float Lost;
	private int Result;
	private Timestamp StopTime;
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public String getMatchName() {
		return MatchName;
	}
	public void setMatchName(String matchName) {
		MatchName = matchName;
	}
	public float getWin() {
		return Win;
	}
	public void setWin(float win) {
		Win = win;
	}
	public float getDraw() {
		return Draw;
	}
	public void setDraw(float draw) {
		Draw = draw;
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
	public Timestamp getStopTime() {
		return StopTime;
	}
	public void setStopTime(Timestamp stopTime) {
		StopTime = stopTime;
	}
	
}
