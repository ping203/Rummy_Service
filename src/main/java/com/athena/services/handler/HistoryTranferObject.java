package com.athena.services.handler;

public class HistoryTranferObject {
	private String from;
	private String to;
	private String senttime;
	private String receivetime;
	private long chip;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public long getChip() {
		return chip;
	}
	public void setChip(long chip) {
		this.chip = chip;
	}
	public String getReceivetime() {
		return receivetime;
	}
	public void setReceivetime(String receivetime) {
		this.receivetime = receivetime;
	}
	public String getSenttime() {
		return senttime;
	}
	public void setSenttime(String senttime) {
		this.senttime = senttime;
	}
}
