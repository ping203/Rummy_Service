package com.athena.services.vo;

public class MarkCreateTable {
	public MarkCreateTable(){
		
	}
	public MarkCreateTable(int  mark, int ag, int con, long agPn){
		this.mark = mark ;
		this.ag = ag ;
		this.condition = con ;
		this.currplay = 0 ;
		this.agPn = agPn;
	}

	private int currplay ; //So nguoi choi hien tai
	private long agPn; //ag play now
	private int gameid ;
	private int mark ;
	private long ag ;
	private int condition ;
	public int getGameid() {
		return gameid;
	}
	public void setGameid(int gameid) {
		this.gameid = gameid;
	}

	public int getCurrplay() {
		return currplay;
	}

	public void setCurrplay(int currplay) {
		this.currplay = currplay;
	}

	public long getAgPn() {
		return agPn;
	}

	public void setAgPn(long agPn) {
		this.agPn = agPn;
	}

	public int getMark() {
		return mark;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

	public long getAg() {
		return ag;
	}

	public void setAg(long ag) {
		this.ag = ag;
	}

	public int getCondition() {
		return condition;
	}
	public void setCondition(int condition) {
		this.condition = condition;
	}
}
