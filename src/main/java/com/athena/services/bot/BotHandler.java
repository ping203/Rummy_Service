package com.athena.services.bot;

import java.util.Set;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;

public class BotHandler {

	public void processBotAction(ServiceImpl serviceImpl, JsonObject je, int playerId) {
		try {
			JsonObject jo = new JsonObject();
			jo.addProperty("evt", "checkbot");
			jo.addProperty("gameid", je.get("gameid").getAsInt());
//			if (je.get("gameid").getAsInt() == GAMEID.BINH) {
//				jo.addProperty("list", ActionUtils.gson.toJson(BinhBotHandler.listBotNew));
//				jo.addProperty("dict", ActionUtils.gson.toJson(BinhBotHandler.dicBotNew));
//			} else if (je.get("gameid").getAsInt() == GAMEID.POKER9K_2345) {
//				jo.addProperty("list", ActionUtils.gson.toJson(BotPoker9K2345.listBotPoker9K));
//				jo.addProperty("dict", ActionUtils.gson.toJson(BotPoker9K2345.dicBotPoker9K));
//			} else if (je.get("gameid").getAsInt() == GAMEID.TEENPATTI) {
//				jo.addProperty("list", ActionUtils.gson.toJson(TeenpattiBot.listBotTeenpatti));
//				jo.addProperty("dict", ActionUtils.gson.toJson(TeenpattiBot.dicBotTeenpatti));
//			} else if (je.get("gameid").getAsInt() == GAMEID.RUMMY) {
//				jo.addProperty("list", ActionUtils.gson.toJson(RummyBot.listBotRummy));
//				jo.addProperty("dict", ActionUtils.gson.toJson(RummyBot.dicBotRummy));
//			} else if (je.get("gameid").getAsInt() == GAMEID.RUMMY_FAST) {
//				jo.addProperty("list", ActionUtils.gson.toJson(RummyIndiaBot.listBotRummyIndia));
//				jo.addProperty("dict", ActionUtils.gson.toJson(RummyIndiaBot.dicBotRummyIndia));
//            } else if (je.get("gameid").getAsInt() == GAMEID.POKDENGNEW) {
//                jo.addProperty("list", ActionUtils.gson.toJson(PokdengBotHandler.listBotPokdeng));
//                jo.addProperty("dict", ActionUtils.gson.toJson(PokdengBotHandler.dicBotPokdeng));
//            } else if (je.get("gameid").getAsInt() == GAMEID.TOMCUACA) {
//                jo.addProperty("list", ActionUtils.gson.toJson(TomCuaCaBotHandler.listBotTomCuaCa));
//                jo.addProperty("dict", ActionUtils.gson.toJson(TomCuaCaBotHandler.dicBotTomCuaCa));
//			}
			ClientServiceAction csa = new ClientServiceAction(playerId, 1,
					ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
			serviceImpl.getServiceRouter().dispatchToPlayer(playerId, csa);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processBotDisconnect(int playerId) {
		try {
			for(Integer key : ServiceImpl.mapRunBot.keySet()){
				ServiceImpl.mapBot.get(key).processBotDisconnect(playerId);				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
		try {		
			for(Integer key : ServiceImpl.mapRunBot.keySet()){
				UserInfo uInfo = ServiceImpl.mapBot.get(key).processConfirmRoom(pid,roomId,tableId,mark);
				if(uInfo != null)
					return uInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	public String processGetBotInfoByPid(int pid, int tid) {
		try {
			for(Integer key : ServiceImpl.mapRunBot.keySet()){
				String uInfo = ServiceImpl.mapBot.get(key).processGetBotInfoByPid(pid,tid);
				if(uInfo != null && uInfo.length() > 0)
					return uInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public UserInfo updateAvailablebot(int pid) {
		try {
			for(Integer key : ServiceImpl.mapRunBot.keySet()){
				UserInfo uInfo = ServiceImpl.mapBot.get(key).updateBotOnline(pid,(short) 0);
				if(uInfo != null)
					return uInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateBotInfoBuyIn(int pid, long agBuyIn, boolean autoFill, boolean autoTopOff) {
		try {
			boolean checkUser = false;
			synchronized (ServiceImpl.dicUser) {
				if (ServiceImpl.dicUser.containsKey(pid)) {
					ServiceImpl.dicUser.get(pid).setAutoFill(autoFill);
					ServiceImpl.dicUser.get(pid).setAutoTopOff(autoTopOff);
					ServiceImpl.dicUser.get(pid).setAGBuyIn(agBuyIn);
				}else
					checkUser = true;
			}
			if(checkUser){
				for(Integer key : ServiceImpl.mapRunBot.keySet()){
					if(ServiceImpl.mapBot.get(key).getDicBot().containsKey(pid)){
						synchronized (ServiceImpl.mapBot.get(key).getDicBot().get(pid)) {
							ServiceImpl.mapBot.get(key).getDicBot().get(pid).setAutoFill(autoFill);
							ServiceImpl.mapBot.get(key).getDicBot().get(pid).setAutoTopOff(autoTopOff);
							ServiceImpl.mapBot.get(key).getDicBot().get(pid).setAGBuyIn(agBuyIn);
						}
					};
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int processGetCurrentBotInMark(int mark, int gameId) {
		try {
			if(ServiceImpl.mapBot.get(gameId) == null) return 0;
			int countbot = 0;
			// System.out.println(" GetCurrentPlayerInMark " + mark + "-"+ gameId);
			Set<Integer> keysBot = ServiceImpl.mapBot.get(gameId).getDicBot().keySet();
			ConcurrentLinkedHashMap<Integer, UserInfo> dicBots = ServiceImpl.mapBot.get(gameId).getDicBot();
			
			if (keysBot != null && !dicBots.isEmpty()) {
				for (Integer key : keysBot) {
					if (dicBots.containsKey(key)) {
						if ((dicBots.get(key).getGameid() == gameId) && (dicBots.get(key).getAS() == mark)) {
							countbot++;
						}
					}
				}
			}
			return countbot;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getCurrentBotInRoom(int roomId, int gameId) {
		try {
			if(ServiceImpl.mapBot.get(gameId) == null) return 0;
			int countbot = 0;
			Set<Integer> keysBot = ServiceImpl.mapBot.get(gameId).getDicBot().keySet();
			ConcurrentLinkedHashMap<Integer, UserInfo> dicBots = ServiceImpl.mapBot.get(gameId).getDicBot();
					
			if (keysBot != null && !dicBots.isEmpty()) {
				for (Integer key : keysBot) {
					if (dicBots.containsKey(key)) {
						if ((dicBots.get(key).getGameid() == gameId) && (dicBots.get(key).getRoomId() == roomId)
								&& dicBots.get(key).getTableId() != 0) {
							countbot++;
						}
					}
				}
			}
			return countbot;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void setRouter(ServiceRouter router) {
		try {
			for(Integer key : ServiceImpl.mapBot.keySet()){
				ServiceImpl.mapBot.get(key).setServiceRouter(router);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
