package com.athena.services.handler;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.constant.UserDataDefine;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.dst.ServerSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;

public class SelectGameHandler {
	private ConcurrentLinkedHashMap<String, Boolean> mapHappyHoursUser 
			= ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, ServiceImpl.MAX_USER);
	private HashMap<String,Long> mapChipsHappyHours = new HashMap<String, Long>();
	private JsonArray rateHappyHours = new JsonArray();
	private boolean happyHoursThailand = true;
	public boolean isHappyHoursThailand() {
		return happyHoursThailand;
	}

	public void setHappyHoursThailand(boolean happyHoursThailand) {
		this.happyHoursThailand = happyHoursThailand;
	}

	public void loadConfig(){
		try{
			// Load config
			String file = "../conf/promotion/happyhours.json";
			//System.out.println("==>loadPromotionSiamOnlineConfig: " + file);
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);
		    String inputStr = ServiceImpl.actionUtils.convertStreamToString(inputStream);
		    System.out.println("loadPromotionHappyHoursConfig: " + inputStr);
		    JsonObject config = (JsonObject) ActionUtils.Parse.parse(inputStr);
		    int[] vips = ActionUtils.gson.fromJson(config.get("vip"), int[].class);
		    rateHappyHours = config.get("rate").getAsJsonArray();
		    JsonArray chips = config.get("chip").getAsJsonArray();
		    int size = chips.get(0).getAsJsonArray().size();
		    for(int i= 0; i < vips.length; i++){
		    	for(int j= 0; j< size; j++){
		    		String key = vips[i]+"_"+j;
		    		mapChipsHappyHours.put(key, chips.get(i).getAsJsonArray().get(j).getAsLong());
		    	}
		    }
		    System.out.println("loadPromotionHappyHoursConfig RATE : " + ActionUtils.gson.toJson(rateHappyHours));
		    System.out.println("loadPromotionHappyHoursConfig DONE : " + ActionUtils.gson.toJson(mapChipsHappyHours));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processHappyHour(UserInfo actionUser, UserController userController) {
		try{ 
            if (actionUser.getSource() == ServerSource.INDIA_SOURCE 
            		|| (actionUser.getSource() == ServerSource.THAI_SOURCE && happyHoursThailand)) {
            	int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            	int minutes = Calendar.getInstance().get( Calendar.MINUTE);
            	boolean checkTime = false;
            	//Time 1	11.am - 2. pm
            	//Time 2	8.pm - 11.pm
            	System.out.println("==>processHappyHour1: "+(new Date())+" - "+actionUser.getPid()
        				+" - hours: "+hours+" - minutes: "+minutes);
            	short time = 1; 
            	if(hours == 12 && minutes < 30)
            		checkTime = true;
            	else if(hours  == 21 && minutes < 30){
            		checkTime = true;
            		time = 2;
            	}           	
	            if (checkTime){
	            	int source = actionUser.getSource();
	            	String keyMap = UserDataDefine.keyHappyHours+source+actionUser.getPid()+time;
	            	synchronized (mapHappyHoursUser) {
	            		if(!mapHappyHoursUser.containsKey(keyMap)){
		            		mapHappyHoursUser.put(keyMap, true);
		            		int vip = actionUser.getVIP();
		            		int random = ActionUtils.random.nextInt(100);
		            		int indexRate = 0;
		            		JsonArray rateByVip = rateHappyHours.get(vip).getAsJsonArray();
		            		for(int i= 0; i< rateByVip.size(); i++){
		            			if(random < rateByVip.get(0).getAsInt()){
		            				indexRate = i;
		            				break;
		            			}
		            		}
		            		String keyChip = vip+"_"+indexRate;
		            		long chip = mapChipsHappyHours.get(keyChip);
		            		System.out.println("==>processHappyHour2: "+(new Date())+" - "+actionUser.getPid()+" - "+chip
		            				+" - hours: "+hours+" - minutes: "+minutes);
		            		userController.GameIBonusChipByTime(source, actionUser.getPid() - ServerDefined.userMap.get(source),
		            				actionUser.getUsername(),chip) ;	 
		            	}
					}	            		           	
            	}
            }
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
