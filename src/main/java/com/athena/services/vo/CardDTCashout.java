package com.athena.services.vo;

//{
//"resultCode":"1",
//"orgTransId":"1482309171.4875kFGIV",
//"amount":0,
//"listCard":"0E5HPb33v9CePY2rHr3wayGNQuD1UWHYaMQDdsWj7looXaIsoHpsJkpmjpgYt/8C6evBxVVsoAQ=",
//"partnerBalance":0,
//"trippdesKey":"ofAp0Cluq620163221033252"
//}


public class CardDTCashout {
	private String resultCode;
	private String orgTransId;
	private String amount;
	private String listCard;
	private String partnerBalance ;
	private String trippdesKey;
	public String getPartnerBalance() {
		return partnerBalance;
	}
	public void setPartnerBalance(String partnerBalance) {
		this.partnerBalance = partnerBalance;
	}
	public String getTrippdesKey() {
		return trippdesKey;
	}
	public void setTrippdesKey(String trippdesKey) {
		this.trippdesKey = trippdesKey;
	}
	public String getListCard() {
		return listCard;
	}
	public void setListCard(String listCard) {
		this.listCard = listCard;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getOrgTransId() {
		return orgTransId;
	}
	public void setOrgTransId(String orgTransId) {
		this.orgTransId = orgTransId;
	}
	public String getResultCode() {
		return resultCode;
	}
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
}

