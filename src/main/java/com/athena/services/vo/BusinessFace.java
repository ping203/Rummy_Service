/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

//import java.util.List;

/**
 *
 * @author UserXP
 */
public class BusinessFace {
    
	
	private String name ;
	private String token_for_business ;
	private BusinessIdsFace ids_for_business ;
	private String id ;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getToken_for_business() {
		return token_for_business;
	}
	public void setToken_for_business(String token_for_business) {
		this.token_for_business = token_for_business;
	}
	public BusinessIdsFace getIds_for_business() {
		return ids_for_business;
	}
	public void setIds_for_business(BusinessIdsFace ids_for_business) {
		this.ids_for_business = ids_for_business;
	}
	
}
