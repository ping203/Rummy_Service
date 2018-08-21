package com.athena.services.cashout;

public class CashoutObject {
	private int ID = 0;
	private String Telco = "";
	private int Value = 0;
	private String TimeCash = "";
	private String TimePay = "";
	private String Status = "";
	private String Serial = "";
	private String CardID = "";
	
	public int getId() {
		return ID;
	}
	public void setId(int id) {
		this.ID = id;
	}
	public String getTelco() {
		return Telco;
	}
	public void setTelco(String telco) {
		this.Telco = telco;
	}
	public int getValue() {
		return Value;
	}
	public void setValue(int value) {
		this.Value = value;
	}
	public String getTimeCash() {
		return TimeCash;
	}
	public void setTimeCash(String timeCash) {
		this.TimeCash = timeCash;
	}
	public String getTimePay() {
		return TimePay;
	}
	public void setTimePay(String timePay) {
		this.TimePay = timePay;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}
	public String getSerial() {
		return Serial;
	}
	public void setSerial(String serial) {
		Serial = serial;
	}
	public String getCardID() {
		return CardID;
	}
	public void setCardID(String cardID) {
		CardID = cardID;
	}
}
