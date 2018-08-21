package com.athena.services.ina;

public class IAP_IOS_NEW {
	private int status ;
	private String environment ;
	
	private IAP_IOS_ITEM receipt;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public IAP_IOS_ITEM getReceipt() {
		return receipt;
	}

	public void setReceipt(IAP_IOS_ITEM receipt) {
		this.receipt = receipt;
	}

}
