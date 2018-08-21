package com.athena.services.vo;

import java.util.Date;

public class AuctionPlayer {
	private String Username;
	private int Price;
	private Date TimeAuction;
	
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
	public int getPrice() {
		return Price;
	}
	public void setPrice(int price) {
		Price = price;
	}
	public Date getTimeAuction() {
		return TimeAuction;
	}
	public void setTimeAuction(Date timeAuction) {
		TimeAuction = timeAuction;
	}
}
