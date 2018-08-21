package com.athena.services.vo;

public class DisplayRuleTran {
	public DisplayRuleTran(){
		sms = 1 ;
		card = 1 ;
		iap = 1 ;
		atm = 1 ;
		cashout = 1 ;
		payurl = "" ;
		paytypesms = 0 ;
		paytypecard = 0 ;
		listgame = new int[1] ;
		bank = 1 ;
	}
	private int sms;
	private int card;
	private int iap ;
	private int atm ;
	private int cashout ;
	private String payurl ;
	private int paytypesms ;
	private int paytypecard ;
	private int[] listgame ;
	private String payurlsms ;
	private String payurlcard ;
	private String payprefix ;
	private String payurldisplay ;
	private int bank ;
	
	
	public int getBank() {
		return bank;
	}
	public void setBank(int bank) {
		this.bank = bank;
	}
	public String getPayurldisplay() {
		return payurldisplay;
	}
	public void setPayurldisplay(String payurldisplay) {
		this.payurldisplay = payurldisplay;
	}
	public String getPayurlsms() {
		return payurlsms;
	}
	public void setPayurlsms(String payurlsms) {
		this.payurlsms = payurlsms;
	}
	public String getPayurlcard() {
		return payurlcard;
	}
	public void setPayurlcard(String payurlcard) {
		this.payurlcard = payurlcard;
	}
	public String getPayprefix() {
		return payprefix;
	}
	public void setPayprefix(String payprefix) {
		this.payprefix = payprefix;
	}
	public int[] getListgame() {
		return listgame;
	}
	public void setListgame(int[] listgame) {
		this.listgame = listgame;
	}
	public int getSms() {
		return sms;
	}
	public void setSms(int sms) {
		this.sms = sms;
	}
	public int getCard() {
		return card;
	}
	public void setCard(int card) {
		this.card = card;
	}
	public int getIap() {
		return iap;
	}
	public void setIap(int iap) {
		this.iap = iap;
	}
	public int getAtm() {
		return atm;
	}
	public void setAtm(int atm) {
		this.atm = atm;
	}
	
	public int getCashout() {
		return cashout;
	}
	public void setCashout(int cashout) {
		this.cashout = cashout;
	}
	public String getPayurl() {
		return payurl;
	}
	public void setPayurl(String payurl) {
		this.payurl = payurl;
	}
	public int getPaytypesms() {
		return paytypesms;
	}
	public void setPaytypesms(int paytypesms) {
		this.paytypesms = paytypesms;
	}
	public int getPaytypecard() {
		return paytypecard;
	}
	public void setPaytypecard(int paytypecard) {
		this.paytypecard = paytypecard;
	}
	
}
