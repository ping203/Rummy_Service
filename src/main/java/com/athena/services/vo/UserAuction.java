package com.athena.services.vo;

public class UserAuction {
	
	public UserAuction(long aucid){
		this.AuctionId = aucid;
		this.Count = 0;
	}
	
	long AuctionId;
	int Count;
	public void increment(){
		this.Count++;
	}
	public long getAuctionId() {
		return AuctionId;
	}
	public void setAuctionId(long auctionId) {
		AuctionId = auctionId;
	}
	public int getCount() {
		return Count;
	}
	public void setCount(int count) {
		Count = count;
	}
}
