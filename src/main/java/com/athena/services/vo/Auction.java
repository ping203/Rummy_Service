package com.athena.services.vo;

import java.sql.Timestamp;
import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
import java.util.List;

public class Auction {
	
	public Auction(){
		ArrPlayer = new ArrayList<AuctionPlayer>();
	}
	
	private long AuctionId;
	private int AuctionType;
	private String ProductName;
	private String ProductImage;
	private int ProductCost;
	private int ProductPrice;
	private String Description;
	private int PriceStep;
	private int MinPerson;
	private int ConditionVIP;
	private int ConditionLQ;
	private int ConditionAG;
	private Timestamp StartTime;
	private Timestamp FinishTime;
	private List<AuctionPlayer> ArrPlayer;
	
	public List<AuctionPlayer> getArrPlayer() {
		return ArrPlayer;
	}
	public void setArrPlayer(List<AuctionPlayer> arrPlayer) {
		ArrPlayer = arrPlayer;
	}
	public long getAuctionId() {
		return AuctionId;
	}
	public void setAuctionId(long auctionId) {
		AuctionId = auctionId;
	}
	public int getAuctionType() {
		return AuctionType;
	}
	public void setAuctionType(int auctionType) {
		AuctionType = auctionType;
	}
	public String getProductName() {
		return ProductName;
	}
	public void setProductName(String productName) {
		ProductName = productName;
	}
	public String getProductImage() {
		return ProductImage;
	}
	public void setProductImage(String productImage) {
		ProductImage = productImage;
	}
	public int getProductCost() {
		return ProductCost;
	}
	public void setProductCost(int productCost) {
		ProductCost = productCost;
	}
	public int getProductPrice() {
		return ProductPrice;
	}
	public void setProductPrice(int productPrice) {
		ProductPrice = productPrice;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public int getPriceStep() {
		return PriceStep;
	}
	public void setPriceStep(int priceStep) {
		PriceStep = priceStep;
	}
	public int getMinPerson() {
		return MinPerson;
	}
	public void setMinPerson(int minPerson) {
		MinPerson = minPerson;
	}
	public int getConditionVIP() {
		return ConditionVIP;
	}
	public void setConditionVIP(int conditionVIP) {
		ConditionVIP = conditionVIP;
	}
	public int getConditionLQ() {
		return ConditionLQ;
	}
	public void setConditionLQ(int conditionLQ) {
		ConditionLQ = conditionLQ;
	}
	public int getConditionAG() {
		return ConditionAG;
	}
	public void setConditionAG(int conditionAG) {
		ConditionAG = conditionAG;
	}
	public Timestamp getStartTime() {
		return StartTime;
	}
	public void setStartTime(Timestamp startTime) {
		StartTime = startTime;
	}
	public Timestamp getFinishTime() {
		return FinishTime;
	}
	public void setFinishTime(Timestamp finishTime) {
		FinishTime = finishTime;
	}
}
