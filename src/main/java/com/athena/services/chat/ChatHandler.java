package com.athena.services.chat;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.handler.KeyCachedDefine;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.dst.ServerSource;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;

public class ChatHandler {
	private ArrayList<ChatObject> historyChat = new ArrayList<ChatObject>();
	private ConcurrentLinkedHashMap<String, String> ruleChat = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 100000);
	
	public void process(UserInfo uinfo, JsonObject json) {
		
		//kenh the gioi
//        JsonObject send = new JsonObject();
//        send.addProperty("evt", "16");
//        send.addProperty("T", 1);
//        send.addProperty("N", je.get("N").getAsString());
//        send.addProperty("D", je.get("D").getAsString());
//        send.addProperty("V", actionUser.getVIP());
//        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//        this.serviceRouter.dispatchToPlayers(getArrPidByGameSourceForChat(0, actionUser.getSource()), csa);
        try{
        	if (json.get("T").getAsInt() == ChatConstant.CHAT_WORLD) {
        		 if(uinfo.getSource() == ServerSource.THAI_SOURCE && uinfo.getVIP() < 2){
                 // Thai vip < 2 k chat world
                 }else{
                	 ChatObject obj = new ChatObject();
                 	//obj.setEvt("16");
                 	obj.setType(ChatConstant.CHAT_WORLD);
                 	if(uinfo.getUsernameLQ().length() > 0)
                 		obj.setName(uinfo.getUsernameLQ());
                 	else
                 		obj.setName(uinfo.getUsername());
                 	int uid = uinfo.getUserid().intValue();
                 	if(uid > ServerDefined.userMap.get((int)uinfo.getSource()))
                 			uid-= ServerDefined.userMap.get((int)uinfo.getSource());
                 	obj.setID(uid);
                 	obj.setData(json.get("D").getAsString());
                 	obj.setVip(uinfo.getVIP());
                 	obj.setAg(uinfo.getAG().longValue());
                 	obj.setAvatar(uinfo.getAvatar());
                 	obj.setFaceID(uinfo.getFacebookid());
                 	obj.setStatus("");
                 	
                 	if(historyChat.size() > 100)
                 		historyChat.remove(0);
                 	historyChat.add(obj);
                 	ChatLogCached log = new ChatLogCached();
                 	log.setHistoryChat(historyChat);
                 	//System.out.println("==>ChatHandler: "+ActionUtils.gson.toJson(log));
                 	UserController.getCacheInstance().set(KeyCachedDefine.Chat_World_Log, log, 0);
                 }
            }
        }catch(Exception e){
        	e.printStackTrace();
        }       
	}
	
	public String getHistoryChatWorld(){
            for (ChatObject chatObject : historyChat) {
                chatObject.setID(chatObject.getID() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
            }
		return ActionUtils.gson.toJson(historyChat);
	}
	
	public boolean checkSpamChat(int uid){
		try{
			if(!ruleChat.containsKey(String.valueOf(uid))){
				JsonObject data = new JsonObject();
				data.addProperty("time", (new Date()).getTime());
				data.addProperty("count", 0);
				ruleChat.put(String.valueOf(uid), ActionUtils.gson.toJson(data));
			}		
			else{
		
				JsonObject data = (JsonObject) ServiceImpl.parser.parse(ruleChat.get(String.valueOf(uid)));
				long time = data.get("time").getAsLong();
				long currentTime = (new Date()).getTime();
				
				//System.out.println("==>CHAT: "+ (new Date(time))+" - curTime : "+(new Date(currentTime))+" - count: "+data.get("count").getAsInt());
				
				if((currentTime - time) < 10000){ //  10s chỉ cho chat 5 lần
					if(data.get("count").getAsInt() < 4){
						data.addProperty("count", data.get("count").getAsInt()+1);
						ruleChat.put(String.valueOf(uid), ActionUtils.gson.toJson(data));
						return true;
					}else{
						return false;
					}
				}else{
					data.addProperty("time", currentTime);
					data.addProperty("count", 0);
					ruleChat.put(String.valueOf(uid), ActionUtils.gson.toJson(data));
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}

	public void getLogChat() {
		try{
			ChatLogCached log =  (ChatLogCached) UserController.getCacheInstance().get(KeyCachedDefine.Chat_World_Log);
			if(log != null)
				historyChat = log.getHistoryChat();
			if(historyChat == null)
				historyChat = new ArrayList<ChatObject>();
			Logger.getLogger("LogChat").info("==>ChatHandler==>logchat: "+ActionUtils.gson.toJson(historyChat));
		}catch(Exception e){
			e.printStackTrace();
		}	
	}

}
