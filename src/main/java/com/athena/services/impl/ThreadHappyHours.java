package com.athena.services.impl;

import java.util.ArrayList;
import java.util.Calendar;

import com.athena.services.vo.UserInfo;

public class ThreadHappyHours implements Runnable{
    @Override
    public void run() {
    	try{
    		while(true){
    			Thread.sleep(60000);
    			int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    			//int minutes = Calendar.getInstance().get( Calendar.MINUTE);
    			if(hours  == 12 || hours == 21){
    				ArrayList<UserInfo> ls = new ArrayList<UserInfo>();
    				synchronized (ServiceImpl.dicUser) {
    					for(Integer key : ServiceImpl.dicUser.keySet()){
        					ls.add(ServiceImpl.dicUser.get(key));
        				}
					}  				
    				for(UserInfo u: ls){
    					ServiceImpl.actionHandler.getSelectGHandler().processHappyHour(u, ServiceImpl.userController);
    					Thread.sleep(200);
    				}    				
    				Thread.sleep(8*60*60*1000);
    			}
        	}
    	}catch (Exception e) {
			e.printStackTrace();
		}   	
    }
}
