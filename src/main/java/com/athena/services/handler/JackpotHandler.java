package com.athena.services.handler;

import org.apache.log4j.Logger;
import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.bot.BinhBotHandler;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.Admin;
import com.athena.services.vo.JackpotWinList;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.ServerSource;
import com.google.gson.JsonObject;

public class JackpotHandler {
	 private Long TotalJackpotIndo = 0l;

	 public void Process_Jackpot(ServiceRouter serviceRouter, UserController userController,JsonObject je, UserInfo actionUser) {
	        try {
	        	Logger.getLogger("BinhHandler").info("==>Process_Jackpot:" + actionUser.getPid()+" - "+ActionUtils.gson.toJson(je));
	        	if (je.get("evt").getAsString().equals("jackpot")) {
	        		long TotalJackpot = TotalJackpotIndo;
	        		if(actionUser.getSource() == ServerSource.IND_SOURCE){ 
	        			TotalJackpot = TotalJackpotIndo;
	        		}
	        		JsonObject act = new JsonObject();
	                act.addProperty("evt", "jackpot");
	                act.addProperty("G", TotalJackpot);
	                //System.out.println("==>Jackpot G: source: "+actionUser.getSource()+" - "+ TotalJackpot);
	               
	                ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
	                serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
	        	} else if (je.get("evt").getAsString().equals("jackpothistory")) {
	        		JackpotWinList ls = userController.GetListJackpotWin((int)actionUser.getSource()) ;
	            	JsonObject jo = new JsonObject();
	                jo.addProperty("evt", "jackpothistory");
	                if (ls.getLswin().size() > 0)
	                	jo.addProperty("data", ActionUtils.gson.toJson(ls));
	                else
	                	jo.addProperty("data", "");
	                ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
	                serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
	        	}
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }
	public void loadJackPot(UserController userController, int source) {
		try{
			if(source == ServerSource.IND_SOURCE){
				TotalJackpotIndo = userController.GetTotalJackpot(source) ;
				Logger.getLogger("BinhHandler").info("==>loadJackPot: TotalJackpotIndo "+TotalJackpotIndo);
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public int getJackpotWin(ServiceImpl impl,ServiceRouter serviceRouter,UserController userController,int markUnit, int vip, String username, int source, int userid, int avatar,int diamondType) {
		try{
			JsonObject jo = new JsonObject();       
			long mw = 0l;
			if( source == ServerSource.IND_SOURCE){
				synchronized (TotalJackpotIndo) {    			
	    			mw = getMaxWin(markUnit, TotalJackpotIndo.longValue());    	
	    			jo.addProperty("G", TotalJackpotIndo.longValue() - mw);
				}
				Logger.getLogger("BinhHandler").info("==>jackpot win: " + mw + "- username: " + username + "- TotalJackpotIndo: " + TotalJackpotIndo + "- Mark: " + markUnit);
			}
			if (mw > 0){
				jo.addProperty("evt", "jackpotwin");
	            jo.addProperty("N", username);          			
				jo.addProperty("D", diamondType);
				jo.addProperty("M", mw); 
                ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));                
                serviceRouter.dispatchToPlayers(impl.getArrPidByGame(0, 0, source), csa);
                
                //
                updateJackpot(userController,(int)(0 - mw), (short) source);
                //Update list user win
                userController.UpdateJackpotWinDB(source, username, vip, markUnit, (int)mw, userid - ServerDefined.userMap.get(source), avatar, diamondType) ;
			}			
            return (int)mw ;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	private long getMaxWin(int markUnit, long jackpotWin) {
		try{
			long mw = 0l;
			long mw1 = 0l;
			if (markUnit == BinhBotHandler.Binh_Mark500K) {
				mw = 500000000l ;
				mw1 = (long)(jackpotWin * 0.75);
			} else if (markUnit == BinhBotHandler.Binh_Mark100K) {
        		mw = 350000000l ;
        		mw1 = (long)(jackpotWin * 0.6) ;
			} else if (markUnit == BinhBotHandler.Binh_Mark50K) {
        		mw = 250000000l ;
        		mw1 = (long)(jackpotWin * 0.45) ;
			}else if (markUnit == BinhBotHandler.Binh_Mark10K) {
        		mw = 70000000l ;
        		mw1 = (long)(jackpotWin * 0.15) ;
			} else if (markUnit == BinhBotHandler.Binh_Mark5K) {
        		mw = 30000000l ;
        		mw1 = (long)(jackpotWin * 0.07) ;
			}
			if (mw > mw1)
				mw = mw1 ;
			return mw;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	public void updateJackpot(UserController userController, int mark, short source) {
		try{
			Logger.getLogger("BinhHandler").info("==>Cap nhat Jackpot:" + mark + "-" + source) ;
			if(source == ServerSource.IND_SOURCE){
				synchronized (TotalJackpotIndo) {	            	
	            	TotalJackpotIndo = TotalJackpotIndo.longValue() + mark ;	           
	                userController.UpdateJackpot(source, mark, 0) ;
	            }
			}    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}      
		
	}
	 
	public void processJacpotCMD(ServiceImpl impl,JsonObject je, int pid, short source) {
		try{
			if(je.get("p").getAsString().equals(Admin.PASS)){
        		if(je.get("type").getAsInt() == 0) // update jackpot history
        		{
        			JackpotWinList lsJackpotWin = (JackpotWinList) UserController.getCacheInstance().get("ListJackpotBinhWin"+je.get("source").getAsInt());
        			
        			//public void sendToClient(int pid, String evt, String data) {
        			impl.sendToClient(pid,"listwinjackpot",ActionUtils.gson.toJson(lsJackpotWin));
        		}else if(je.get("type").getAsInt() == 1){ // jackpot win
        			//GetJackpotWin(int markUnit, int vip, String username, int source, int userid, int avatar, int  diamondType) 
        			//int sour = je.get("S").getAsInt(); 
        			int gold = impl.GetJackpotWin(je.get("M").getAsInt(),je.get("V").getAsInt(),je.get("N").getAsString(),
        					source, je.get("ID").getAsInt(),je.get("A").getAsInt(),0);              			
        			impl.sendErrorMsg(pid, "OK! - GoldWin: "+ gold);
//        			if(gold == 0)
//        				sendSpecialAlert(0, "alert", "Chúc mừng cao thủ "+je.get("N").getAsString() + " đã nhận được " + ActionUtils.formatAG(gold) 
//        				+ " từ hũ mậu binh.", je.get("N").getAsString(), source);
        		}else if(je.get("type").getAsInt() == 2){
        			//String keyJackpot = "ListJackpotBinhWin";
        			JackpotWinList lsJackpotWin = (JackpotWinList) UserController.getCacheInstance().get("ListJackpotBinhWin2");
        			impl.sendErrorMsg(pid, "OK! - "+ActionUtils.gson.toJson(lsJackpotWin));
        		}else if(je.get("type").getAsInt() == 3){
        			//String keyJackpot = "ListJackpotBinhWin";
        			long lsJackpotWin =  (Long) UserController.getCacheInstance().get("TotalJackpotBinh3");
        			impl.sendErrorMsg(pid, "OK! - "+ActionUtils.gson.toJson(lsJackpotWin));
        		}else if(je.get("type").getAsInt() == 4){
        			//String keyJackpot = "ListJackpotBinhWin";
        			long lsJackpotWin =  (Long) UserController.getCacheInstance().get("TotalJackpotBinh4");
        			impl.sendErrorMsg(pid, "OK! - "+ActionUtils.gson.toJson(lsJackpotWin));
        		}else if(je.get("type").getAsInt() == 5){
        			//String keyJackpot = "ListJackpotBinhWin";
        			long lsJackpotWin =  (Long) UserController.getCacheInstance().get("TotalJackpotBinh");
        			impl.sendErrorMsg(pid, "OK! - "+ActionUtils.gson.toJson(lsJackpotWin));
        		}else if(je.get("type").getAsInt() == 6){
        			long lsJackpotWin =  (Long) UserController.getCacheInstance().get("TotalJackpotBinh");
        			 UserController.getCacheInstance().set("TotalJackpotBinh4", lsJackpotWin, 0);
        		}  else if(je.get("type").getAsInt() == 7){
        			String keyJackpot = "ListJackpotBinhWin" + 4;
    				JackpotWinList lsJackpotWin = new JackpotWinList() ;
    	    		UserController.getCacheInstance().set(keyJackpot, lsJackpotWin, 0);
        		}else if(je.get("type").getAsInt() == 8){
        			//String keyJackpot = "ListJackpotBinhWin";
        			JackpotWinList lsJackpotWin = (JackpotWinList) UserController.getCacheInstance().get("ListJackpotBinhWin3");
        			impl.sendErrorMsg(pid, "OK! - "+ActionUtils.gson.toJson(lsJackpotWin));
        		}else if(je.get("type").getAsInt() == 9){
        			//String keyJackpot = "ListJackpotBinhWin";
        			JackpotWinList lsJackpotWin = (JackpotWinList) UserController.getCacheInstance().get("ListJackpotBinhWin4");
        			impl.sendErrorMsg(pid, "OK! - "+ActionUtils.gson.toJson(lsJackpotWin));
        		}else if(je.get("type").getAsInt() == 10){
        			String keyJackpot = "ListJackpotBinhWin" + je.get("source").getAsInt();
    				JackpotWinList lsJackpotWin = new JackpotWinList() ;
    	    		UserController.getCacheInstance().set(keyJackpot, lsJackpotWin, 0);
        		}                
        	}	
			
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void processJackpot(ServiceImpl serviceImpl, JsonObject je, int playerId) {
		try{
			if(je.get("p").getAsString().equals(Admin.PASS)){
        		if(je.get("type").getAsInt() == 0) // update jackpot history
        		{
        			JackpotWinList lsJackpotWin = (JackpotWinList) UserController.getCacheInstance().get("ListJackpotBinhWin"+je.get("source").getAsInt());
        			
        			serviceImpl.sendToClient(playerId,"jackpotlistwin",ActionUtils.gson.toJson(lsJackpotWin));
        		}
        		else if(je.get("type").getAsInt() == 1)
        		{
        			int s = je.get("source").getAsInt();
        			int gold = serviceImpl.GetJackpotWin(je.get("M").getAsInt(),je.get("V").getAsInt(),je.get("N").getAsString(),
        					s, je.get("ID").getAsInt(),je.get("A").getAsInt(),0);              			
        			serviceImpl.sendErrorMsg(playerId, "OK! - GoldWin: "+ gold);
        		}else if(je.get("type").getAsInt() == 2)
        		{
        			String keyJackpot = "TotalJackpotBinh" + je.get("source").getAsInt();
    	    		UserController.getCacheInstance().set(keyJackpot, null, 0);
        		}else if(je.get("type").getAsInt() == 3){
        			long lsJackpotWin =  (Long) UserController.getCacheInstance().get("TotalJackpotBinh"+ je.get("source").getAsInt());
        			serviceImpl.sendToClient(playerId,"jackpotlistwin",String.valueOf(lsJackpotWin));
        		}        		
			}
		}catch (Exception e) {
			e.printStackTrace();
		}	
	}		
}
