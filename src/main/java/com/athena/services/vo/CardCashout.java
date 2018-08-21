package com.athena.services.vo;

public class CardCashout {
	private String pin;
	private String serial;
	private String telco;
	private String msg;
	private int status ;
	private String source = "";
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public String getTelco() {
		return telco;
	}
	public void setTelco(String telco) {
		this.telco = telco;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
