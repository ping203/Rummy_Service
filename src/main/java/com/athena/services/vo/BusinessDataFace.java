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
public class BusinessDataFace {
    
	private String id ;
	private BusinessChildFace app ;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public BusinessChildFace getApp() {
		return app;
	}
	public void setApp(BusinessChildFace app) {
		this.app = app;
	}
}
