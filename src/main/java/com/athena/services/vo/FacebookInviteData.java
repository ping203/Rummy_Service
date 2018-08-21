package com.athena.services.vo;

public class FacebookInviteData {
	
	private FacebookApp application;
	private String created_time;
	private FacebookInviteUser from;	
	
	private String message;	
	private FacebookInviteUser to;	
	private String id;
	public FacebookApp getApplication() {
		return application;
	}
	public void setApplication(FacebookApp application) {
		this.application = application;
	}
	public String getCreated_time() {
		return created_time;
	}
	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}
	public FacebookInviteUser getFrom() {
		return from;
	}
	public void setFrom(FacebookInviteUser from) {
		this.from = from;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public FacebookInviteUser getTo() {
		return to;
	}
	public void setTo(FacebookInviteUser to) {
		this.to = to;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
