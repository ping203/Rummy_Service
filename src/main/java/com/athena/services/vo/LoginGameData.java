package com.athena.services.vo;


public class LoginGameData {
	private String evt = "";
	private String data;
	private long time;
	private String auth="";

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getEvt() {
		return evt;
	}

	public void setEvt(String evt) {
		this.evt = evt;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
