package com.athena.services.handler;

public class BankHistory {
	private String fromname;
	private String toname;
	private String msg;
	private String timeday;
	private String timehour;
	private long chip;
	private long chipchange;

	public String getFromname() {
		return fromname;
	}
	public void setFromname(String fromname) {
		this.fromname = fromname;
	}
	public String getToname() {
		return toname;
	}
	public void setToname(String toname) {
		this.toname = toname;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public long getChip() {
		return chip;
	}
	public void setChip(long chip) {
		this.chip = chip;
	}
	public long getChipchange() {
		return chipchange;
	}
	public void setChipchange(long chipchange) {
		this.chipchange = chipchange;
	}
	public String getTimeday() {
		return timeday;
	}
	public void setTimeday(String timeday) {
		this.timeday = timeday;
	}
	public String getTimehour() {
		return timehour;
	}
	public void setTimehour(String timehour) {
		this.timehour = timehour;
	}
}
