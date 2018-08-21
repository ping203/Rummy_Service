package com.athena.services.handler;

import com.athena.database.ServerDefined;
import com.athena.services.vo.UserInfo;
import com.google.gson.Gson;
import org.apache.log4j.Logger;


public class KeyCachedDefine {
	public static String getKeyCachedStatusUser(UserInfo uinfo){
		try{
			int uid = uinfo.getUserid();
			if(uid < ServerDefined.userMap.get((int)uinfo.getSource())){
				uid += ServerDefined.userMap.get((int)uinfo.getSource());
			}

			return "status_"+uinfo.getSource()+"_"+uid;
		}catch (Exception e){
			Logger.getLogger("Debug_service").error(e.getMessage(), e);
			e.printStackTrace();
			throw e;
		}
	}

	public static String getKeyCachedBank(UserInfo uinfo) {

		return "bank_"+uinfo.getSource()+"_"+uinfo.getUserid();
	}
	
	public static String getKeyCachedGiftCode(int id,String deviceID) {

		return "giftcode_"+id+"_"+deviceID;
	}
	
	public static final String Chat_World_Log = "chatwordlog"; 
}
