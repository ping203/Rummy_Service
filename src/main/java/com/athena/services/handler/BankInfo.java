package com.athena.services.handler;

import java.util.List;

public class BankInfo {
	private long chip;
	private List<BankHistory> history;
	public  List<BankHistory> getHistory() {
		return history;
	}
	public void setHistory(List<BankHistory> history) {
		this.history = history;
	}
	public long getChip() {
		return chip;
	}
	public void setChip(long chip) {
		this.chip = chip;
	}
}
