package com.athena.services.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.PolicyPromotionOnline;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PromotionOnlineConfig {
	public static  HashMap<Integer, PolicyPromotionOnline> configSiam =  new HashMap<Integer, PolicyPromotionOnline>();
	public void loadPromotionSiamOnlineConfig(){
		try{
			String file = "../conf/promotion/online.json";
			//System.out.println("==>loadPromotionSiamOnlineConfig: " + file);
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);
		    String inputStr = convertStreamToString(inputStream);
		    System.out.println("loadPromotionSiamOnlineConfig: " + inputStr);
		    JsonObject config = (JsonObject) ActionUtils.Parse.parse(inputStr);
		    
		    JsonArray vips = config.get("vip").getAsJsonArray();
		    int[] time = ActionUtils.gson.fromJson(config.get("time"), int[].class);
		    for(int i= 0; i< vips.size(); i++){
				   int[] chips = ActionUtils.gson.fromJson(config.get("chip").getAsJsonArray().get(i), int[].class);
				   PolicyPromotionOnline policyChip = new PolicyPromotionOnline(6,time,chips);
				   configSiam.put(vips.get(i).getAsInt(), policyChip);
		    }
		    System.out.println("==>loadPromotionSiamOnlineConfig DONE: "+ActionUtils.gson.toJson(configSiam));
		}catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public String convertStreamToString(InputStream inputstream) {
    	String line = "";
		StringBuilder total = new StringBuilder();			
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(inputstream,"UTF-8"));
			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return total.toString();
	}
}
