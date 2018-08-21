package com.athena.services.vo;

public class DisplayRule {
	public DisplayRule(){
		bank = 1 ;
	}
	private String version ;
	private String packageid ;
	private int operatorid ;
	private String osid ;
	private String publisherid ;
	private int sms;
	private int card;
	private int iap ;
	private int atm ;
	private String listgame ;
	private int cashout ;
	private String payurl ;
	private int paytypesms ;
	private int paytypecard ;
	private String payurlcard ;
	private String payurlsms ;
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
	public String getPayurlcard() {
		return payurlcard;
	}
	public void setPayurlcard(String payurlcard) {
		this.payurlcard = payurlcard;
	}
	public String getPayurlsms() {
		return payurlsms;
	}
	public void setPayurlsms(String payurlsms) {
		this.payurlsms = payurlsms;
	}
	public String getPayprefix() {
		return payprefix;
	}
	public void setPayprefix(String payprefix) {
		this.payprefix = payprefix;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getPackageid() {
		return packageid;
	}
	public void setPackageid(String packageid) {
		this.packageid = packageid;
	}
	public int getOperatorid() {
		return operatorid;
	}
	public void setOperatorid(int operatorid) {
		this.operatorid = operatorid;
	}
	public String getOsid() {
		return osid;
	}
	public void setOsid(String osid) {
		this.osid = osid;
	}
	public String getPublisherid() {
		return publisherid;
	}
	public void setPublisherid(String publisherid) {
		this.publisherid = publisherid;
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
	public String getListgame() {
		return listgame;
	}
	public void setListgame(String listgame) {
		this.listgame = listgame;
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
