package com.athena.services.handler;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.room.MarkTableInfo;
import com.athena.services.room.RoomHandler;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.JSent;
import com.athena.services.vo.TopGamer;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.api.service.dosprotect.DosProtector;
import com.cubeia.firebase.api.service.dosprotect.FrequencyRule;
import com.dst.ServerSource;
import com.google.gson.JsonObject;
import com.vng.tfa.common.SqlService;

public class ServiceActionHandler {
	
	private BankHandler bank = new BankHandler();
	private RoomHandler room = new RoomHandler();
	private MarkTableInfo markTableInfo = new MarkTableInfo();
	private SelectGameHandler selectGHandler = new SelectGameHandler();

	private List<TopVipGamer> lsTopVip = new ArrayList<TopVipGamer>();
	
	public void configDOS(ServiceContext context){
		try{
				DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);         
				dos.config("bankaction", new FrequencyRule(1, 1000));   
				dos.config("topvip", new FrequencyRule(1, 1000));
				dos.config("gettransferhistory", new FrequencyRule(1, 1000));	
				dos.config("registername", new FrequencyRule(1, 1000));
				dos.config("roomaction", new FrequencyRule(1, 1000));
				dos.config("gameaction", new FrequencyRule(1, 200));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void process(JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter, ServiceContext context) {
		try{
			int id = je.get("idevt").getAsInt();
			if (actionUser.getUnlockPass() == 0) return;
			
			if (id > 699) processService(id,je,actionUser,serviceRouter,context);
			else if (id > 599) room.processRoomIDEvt(id,je,actionUser,serviceRouter,context);
			else if (id > 499) processGetTransferHistory(id,je,actionUser,serviceRouter,context);
			else if(id > 399) processGetTop(id,je,actionUser,serviceRouter,context);
			else if(id > 299) bank.processBank(id,je,actionUser,serviceRouter,context);
			else if(id > 199) processUpdateUserInfo(id,je,actionUser, serviceRouter,context);
			else if (id > 99) processPromotionEvt(id,je,actionUser,serviceRouter,context); 
			else processGameAction(id, je, actionUser, serviceRouter,context);
		}catch(Exception e){
			e.printStackTrace();
		}	
	}

	private void processService(int id, JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter,
			ServiceContext context) {
		try{
			if(id == EVT_ID.CHANGE_HAPPY_HOURS){
				selectGHandler.setHappyHoursThailand(!selectGHandler.isHappyHoursThailand());
				JsonObject jo = new JsonObject();
	            jo.addProperty("idevt", EVT_ID.GET_TRANSFER_HISTORY);                  
	            jo.addProperty("data", selectGHandler.isHappyHoursThailand());
	            Logger.getLogger("LoginandDisconnect").info("==>processService: "+actionUser.getUserid()+" - "+ActionUtils.gson.toJson(jo));
	            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
	            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
			}		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processGameAction(int id, JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter, ServiceContext context) {
		try{
			DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);  
			if (!dos.allow("joinSpamRule", actionUser.getPid())){
				Logger.getLogger("LoginandDisconnect").info("processGameAction: "+ActionUtils.gson.toJson(je)+" - "+actionUser.getPid() );
				return;
			}
				
			if(id == EVT_ID.CREATE_TABLE){
	            JsonObject send = new JsonObject();
	            send.addProperty("evt", "createTable");
	            send.addProperty("pid", actionUser.getPid());
	            send.addProperty("M", je.get("M").getAsInt());
				if(je.has("noLimited")){
					send.addProperty("noLimited", je.get("noLimited").getAsBoolean());
				}else{
					send.addProperty("noLimited", true);
				}

	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			}else if(id == EVT_ID.PLAYNOW_CHANGE_TABLE){
				JsonObject send = new JsonObject();
	            send.addProperty("evt", "playnow2");
	            send.addProperty("idtable",je.get("idtable").getAsInt());
	            send.addProperty("M", je.get("M").getAsInt());
	            send.addProperty("pid", actionUser.getPid());
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			}else if(id == EVT_ID.PLAYNOW_BY_MARK_TYPE){
				JsonObject send = new JsonObject();
	            send.addProperty("evt", "playbymark");
	            send.addProperty("Type",je.get("Type").getAsInt());
	            send.addProperty("M", je.get("M").getAsInt());
	            send.addProperty("pid", actionUser.getPid());
	            Logger.getLogger("ServiceActionHandler").info("==>EVT_ID.PLAYNOW_BY_MARK: pid: "+actionUser.getPid()+" - M: "+je.get("M").getAsInt()+" - Type: "+je.get("Type").getAsInt()+" - gameid: "+actionUser.getGameid());
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			}else if(id == EVT_ID.CREATE_TABLE_TYPE){
	            JsonObject send = new JsonObject();
	            send.addProperty("evt", "createTableType");
	            send.addProperty("pid", actionUser.getPid());
	            send.addProperty("M", je.get("M").getAsInt());
	            send.addProperty("T", je.get("Type").getAsInt());
	            send.addProperty("P", je.has("P") ? je.get("P").getAsInt() : 0);
	            send.addProperty("Seat", je.has("Seat") ? je.get("Seat").getAsInt() : 5);
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			}else if(id == EVT_ID.PLAYNOW_CHANGE_TABLE_BY_TYPE){ //9k
				JsonObject send = new JsonObject();
	            send.addProperty("evt", "playnow5");
	            send.addProperty("idtable",je.get("idtable").getAsInt());
	            send.addProperty("M", je.get("M").getAsInt());
	            send.addProperty("pid", actionUser.getPid());
	            send.addProperty("T", je.get("Type").getAsInt());
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			}else if(id == EVT_ID.CREATE_TABLE_WITH_NUM_SEAT){
				JsonObject send = new JsonObject();
	            send.addProperty("evt", "createTableWithSeat");
	            send.addProperty("pid", actionUser.getPid());
	            send.addProperty("M", je.get("M").getAsInt());
	            send.addProperty("Seat", je.get("Seat").getAsInt());
	            send.addProperty("P", je.has("P") ? je.get("P").getAsInt() : 0);
	            send.addProperty("MarkFinish", je.has("MF") ? je.get("MF").getAsInt() : 0);
	            send.addProperty("Type", je.has("Type") ? je.get("Type").getAsInt() : 0);
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			}else if(id == EVT_ID.PLAYNOW_POKER_TEXAS){
	            JsonObject send = new JsonObject();
	            send.addProperty("evt", "playnowTexas");
	            send.addProperty("pid", actionUser.getPid());
	            send.addProperty("M", je.get("M").getAsInt());
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			} else if(id == EVT_ID.JOIN_TABLE_WITH_ID){
				//DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);  
				//if (dos.allow("joinSpamRule", actionUser.getPid())) 
		        //{
					int gameid = je.get("gameid").getAsInt();
		            int source = actionUser.getSource();   
		            Logger.getLogger("LoginandDisconnect").info("==>JOIN_TABLE_WITH_ID: " + actionUser.getUserid() + "- tableid: " 
		            		+ actionUser.getUsername() + "-" + actionUser.getGameid()
		            		+actionUser.getTableId()+" - isonline: "+actionUser.getIsOnline());
		        	 if (actionUser.getTableId() != 0) {	             		
		             		//JoinRequestAction action = new JoinRequestAction(actionUser.getPid(), actionUser.getTableId(), -1, "");
		                    //serviceRouter.dispatchToGame((int)actionUser.getGameid(), action);          
		             }else {
		            	  if (actionUser.getIsOnline() != gameid) {
		                  	ServiceImpl.userController.UpdateIsOnlineToCache(source, actionUser.getPid() - ServerDefined.userMap.get(source),
		                  			ServiceImpl.ipAddressServer,(short) gameid, actionUser.getOperatorid(), actionUser.getsIP());
		                  	ServiceImpl.dicUser.get(actionUser.getPid()).setGameid((short) gameid);
		            	  }
		            	  JoinRequestAction action = new JoinRequestAction(actionUser.getPid(), je.get("tableid").getAsInt(), -1, "");
				          serviceRouter.dispatchToGame(gameid, action);
		             }	
		       // }            
			}else if(id == EVT_ID.CREATE_TABLE_WITH_NUM_SEAT_9K){
	            JsonObject send = new JsonObject();
	            send.addProperty("evt", "createTableWithSeatType");
	            send.addProperty("pid", actionUser.getPid());
	            send.addProperty("M", je.get("M").getAsInt());
	            send.addProperty("T", je.get("Type").getAsInt());
	            send.addProperty("Seat", je.get("Seat").getAsInt());
	            Logger.getLogger("Poker9KHandler").info("==>EVT_ID.CREATE_TABLE_WITH_NUM_SEAT_9K: pid: "+actionUser.getPid()+" - M: "+je.get("M").getAsInt()+" - type: "+actionUser.getGameType9k());
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			}else if(id == EVT_ID.PLAYNOW_BY_MARK_TYPE){
				JsonObject send = new JsonObject();
	            send.addProperty("evt", "playbymark");
	            send.addProperty("Type",je.get("Type").getAsInt());
	            send.addProperty("M", je.get("M").getAsInt());
	            send.addProperty("pid", actionUser.getPid());
	            Logger.getLogger("ServiceActionHandler").info("==>EVT_ID.PLAYNOW_BY_MARK: pid: "+actionUser.getPid()+" - M: "+je.get("M").getAsInt()+" - Type: "+je.get("Type").getAsInt()+" - gameid: "+actionUser.getGameid());
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
			}else if(id == EVT_ID.PLAYNOW_WITH_GAMEID){
				JsonObject send = new JsonObject();
	            int gameid = je.get("gameid").getAsInt();
	            send.addProperty("evt", "playnowwithgameid");
	            send.addProperty("pid", actionUser.getPid());
	            send.addProperty("gameid", gameid);
	            send.addProperty("tableid", je.get("tableid").getAsInt());
	            send.addProperty("T", je.has("T") ? je.get("T").getAsInt() : 0);
	            
	            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	            serviceRouter.dispatchToGameActivator(gameid, request);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void processGetTransferHistory(int idevt, JsonObject je, UserInfo user, ServiceRouter router,
			ServiceContext context) {
		//"EXEC LQLog.dbo.RptgetListTransferInfo @timebegin = ?, @timeend = ?, @fromuser= N?, @touser = N?");
		try{
			if(idevt == EVT_ID.GET_TRANSFER_HISTORY){
				DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
	        	if (dos.allow("gettransferhistory", user.getUserid())) {
	        		SqlService instance = SqlService.getInstanceBySource(user.getSource());
	        		Connection conn  = instance.getDbConnection();
	        		List<BankHistory> ls = new ArrayList<BankHistory>();
	        	    try{
	        	        CallableStatement cs = conn.prepareCall("{call GameGetListBank_History(?)}");
	        	        cs.setInt("userid", user.getUserid() - ServerDefined.userMap.get((int)user.getSource()));
	        	        ResultSet data = cs.executeQuery();
	        	       
	        	        while (data.next()) {       	        
//	        	        	1. 20-08-17@13:41:01| +1,000 Chip từ tài khoản ABC chuyển đến Số dư Bank 14,000 Chip.
//	        	        	> 20-08-17@13.41:01| +1,000 ชิป มาจากบัญชี ABC ยอดที่เหลือ 14,000 ชิป
//	        	        	2. 20-08-17@13.41:02| - 2,000 Chip chuyển đến tài khoản XYZ. Số dư Bank 12,000 Chip.
//	        	        	> 20-08-17@13.41:02| - 2,000 ชิป ไปยังบัญชี XYZ ยอดที่เหลือ 12,000 ชิป
//	        	        	3. 20-08-17@13.41:03| +1,000 Chip Cất tiền vào két sắt. Số dư Bank 13,000 Chip.
//	        	        	> 20-08-17@13.41:03| +1,000 ชิป ฝากชิปเข้าตู้เซฟ ยอดที่เหลือ 13,000 ชิป
//	        	        	4. 20-08-17@13.41:04| -1,000 Chip Rút tiền khỏi két sắt. Số dư Bank 13,000 Chip.
//	        	        	> 20-08-17@13.41:05| -1,000 ชิป ถอนเงินออกจากตู้เซฟ ยอดที่เหลือ 13,000 ชิป
	        	        	//GameGetListBank_History] 
//	        	        	 id, userid, chipbank, chipchange, fromuserid, chip, ToName, FromName

	        	        	int fromid = data.getInt("fromuserid");
	        	        	int userid = data.getInt("userid");
	        	        	String fromname = data.getString("FromName");	
	        	        	String toname = data.getString("ToName");
	        	        	BankHistory obj = new BankHistory();
	        	        	String history = "";
	        	        	long chipchange = data.getLong("chipchange");
	        	        	long chipbank = data.getLong("chipbank");
	        	        	Date date = new Date(data.getTimestamp("createtime").getTime());
	        	        	int source = user.getSource();
	        	        	if(userid == fromid){
	        	        		if(chipchange >= 0){
	        	        			history = ServiceImpl.actionUtils.getConfigText("strSendBank",source, user.getUserid());
	        	        			obj.setChipchange(chipchange);
	        	        		}else{
	        	        			history = ServiceImpl.actionUtils.getConfigText("strWithdrawBank",source, user.getUserid());
	        	        			obj.setChipchange(chipchange);
	        	        		}	        	        		
	        	        	} else if(userid == (user.getUserid() - ServerDefined.userMap.get(source))){
	        	        		if(chipchange > 0){
		        	        		history = ServiceImpl.actionUtils.getConfigText("strGetToOtherBank",source, user.getUserid())+fromname;
		        	        		obj.setChipchange(chipchange);
	        	        		}else
	        	        		{
	        	        			history = ServiceImpl.actionUtils.getConfigText("strSendToOtherBank",source, user.getUserid())+fromname;
		        	        		obj.setChipchange(chipchange);
	        	        		}
	        	        	}
	        	        		
	        	        	obj.setMsg(history);
	        	        	obj.setFromname(fromname);
	        	        	obj.setToname(toname);
	        	        	if(userid == fromid){
	        	        		String uname = user.getUsernameLQ();
	        	        		if(uname.length() == 0)
	        	        			uname = user.getUsername();
	        	        		obj.setFromname(uname);
		        	        	obj.setToname(uname);
	        	        	}
	        	        	obj.setChip(chipbank);
	        	        	obj.setTimeday(ActionUtils.getTimeHistoryBankDay(date));
	        	        	obj.setTimehour(ActionUtils.getTimeHistoryBankHour(date));
	        	        	
	        	        	ls.add(obj);
	        	        }
	        	        JsonObject jo = new JsonObject();
	                    jo.addProperty("idevt", EVT_ID.GET_TRANSFER_HISTORY);                  
	                    jo.addProperty("data", ActionUtils.gson.toJson(ls));
	                    Logger.getLogger("BANKHANDLER").info("==>processGetTransferHistory: "+user.getUserid()+" - "+ActionUtils.gson.toJson(jo));
	                    ClientServiceAction csa = new ClientServiceAction(user.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
	                    router.dispatchToPlayer(user.getPid(), csa);
	        	    } catch (Exception ex){
	        	    	ex.printStackTrace();
	        	    }
	        	    finally{
	        	        instance.releaseDbConnection(conn);
	        	    }
	        	}
			}			
		}catch(Exception e){
			e.printStackTrace();
		}	
	}

	private void processGetTop(int id, JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter,
			ServiceContext context) {
		try{
			if(id == EVT_ID.GET_TOP_VIP){
				DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
            	if (dos.allow("topvip", actionUser.getUserid())) {
            		Logger.getLogger("ServiceActionHandler").info("==>ServiceActionHandler==>processGetTopVip size:" + lsTopVip.size());
            		 int source = actionUser.getSource();
            		JsonObject jo = new JsonObject();
                    jo.addProperty("idevt", EVT_ID.GET_TOP_VIP);    
                    List<TopVipGamer> lsTopVipReturn = new ArrayList<TopVipGamer>();
                    for(TopVipGamer top : lsTopVip){
                    	TopVipGamer gamer = new TopVipGamer();  					
    					
    					//return "status_"+uinfo.getSource()+"_"+uinfo.getUserid();
    					int uid = top.getId()+ ServerDefined.userMap.get(source);
    					String key_status = "status_"+source+"_"+uid;
	    				String status = (String) UserController.getCacheInstance().get(key_status);
	    				if(status == null){
	    					 if(source == ServerSource.THAI_SOURCE)
         						 status = "";
         					 else
         						 status = "...";
	    				}
	    				String key = ServerDefined.getKeyCache(source) + top.getId();              
	    				UserInfo ulogin = (UserInfo) UserController.getCacheInstance().get(key);
	    				if(ulogin != null && ulogin.getIsOnline() > 0)
	    					gamer.setOnline(true);
	    				else
	    					gamer.setOnline(false);
	    				gamer.setId(top.getId());
			        	gamer.setVip(top.getVip());
			        	gamer.setChip(top.getChip());
			        	gamer.setFaceid(top.getFaceid());
			        	gamer.setAvatar(top.getAvatar());			        	
			        	gamer.setName(top.getName());
	    				gamer.setStatus(status);
	    				lsTopVipReturn.add(gamer);
                    }
                    jo.addProperty("data", ActionUtils.gson.toJson(lsTopVipReturn));
                    ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
            	}			
			}else if(id == EVT_ID.GET_TOP_RICH){
				DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
            	if (dos.allow("toprich", actionUser.getUserid())){
            		JsonObject jo = new JsonObject();
    	            jo.addProperty("idevt", EVT_ID.GET_TOP_RICH);
    	            int source = actionUser.getSource();
    	            List<TopVipGamer> lsRet = new ArrayList<TopVipGamer>();
    	            for(TopGamer top : ServiceImpl.lsTopRich){  
    	            	TopVipGamer gamer = new TopVipGamer();  					
    					
    					//return "status_"+uinfo.getSource()+"_"+uinfo.getUserid();
    					int uid = top.getId()+ ServerDefined.userMap.get(source);
    					String key_status = "status_"+source+"_"+uid;
	    				String status = (String) UserController.getCacheInstance().get(key_status);
	    				if(status == null){
	    					 if(source == ServerSource.THAI_SOURCE)
         						 status = "คุณกำลังคิดอะไรอยู่";
         					 else
         						 status = "...";
	    				}
	    				String key = ServerDefined.getKeyCache(source) + top.getId();              
	    				UserInfo ulogin = (UserInfo) UserController.getCacheInstance().get(key);
	    				if(ulogin != null && ulogin.getIsOnline() > 0)
	    					gamer.setOnline(true);
	    				else
	    					gamer.setOnline(false);
	    				gamer.setId(top.getId());
			        	gamer.setVip(top.getV());
			        	gamer.setChip(top.getM());
			        	gamer.setFaceid(top.getFaid());
			        	gamer.setAvatar(top.getAv());
			        	if(top.getNLQ().length() > 0)
			        		gamer.setName(top.getNLQ());
			        	else
			        		gamer.setName(top.getN());
	    				gamer.setStatus(status);
	    				
	    				lsRet.add(gamer);
    	            }
    	            jo.addProperty("data", ActionUtils.gson.toJson(lsRet));
    	            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
    	            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
            	}				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void processUpdateUserInfo(int id, JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter, ServiceContext context) {
		try{
			if(id == EVT_ID.GET_STATUS){
				String key_status = KeyCachedDefine.getKeyCachedStatusUser(actionUser);
				String status = (String) UserController.getCacheInstance().get(key_status);
				if(status == null){
					 if(actionUser.getSource() == ServerSource.THAI_SOURCE)
 						 status = "คุณกำลังคิดอะไรอยู่";
 					 else
 						 status = "...";
				}
				
				JsonObject act = new JsonObject();
				act.addProperty("idevt", EVT_ID.GET_STATUS);
				act.addProperty("status", status);
	            
	            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
			} else if(id == EVT_ID.UPDATE_STATUS){
				String key_status = KeyCachedDefine.getKeyCachedStatusUser(actionUser);
				String status = "";
				if(je.has("status")) status = je.get("status").getAsString();
				if(status.length() > 500) status = status.substring(0,500);
				boolean result = UserController.getCacheInstance().set(key_status,status,0);
				
				JsonObject act = new JsonObject();
				act.addProperty("idevt", EVT_ID.UPDATE_STATUS);
				act.addProperty("status", status);
				act.addProperty("result", result);
				if(!result)
					act.addProperty("msg", "Update status failed! Please try again later!");
				
	            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
			} else if(id == EVT_ID.REGISTER_ACC){
				DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);  
				Logger.getLogger("LoginandDisconnect").info("==>REGISTER_ACC: "+ActionUtils.gson.toJson(je)+" - pid "+actionUser.getPid());
				if (dos.allow("registername", actionUser.getPid())) 
		        {
					String name = je.get("name").getAsString();
					String pass = je.get("pass").getAsString();
					String oldpass = je.get("oldpass").getAsString();
					int checkname = ActionUtils.CheckValidUsernameLQ(name) ;
					String result = ServiceImpl.actionUtils.getConfigText("strRegister_Err5",actionUser.getSource(), actionUser.getUserid());
					boolean status = false;
					int source = actionUser.getSource();
					
					boolean checkpass = ActionUtils.checkPassWord(pass);
					Logger.getLogger("LoginandDisconnect").info("==>REGISTER_ACC: "+actionUser.getUsername()+" - checkname "+checkname
							+" - checkpass "+ checkpass+" - idnex : "+actionUser.getUsername().indexOf("te.")+" - "+actionUser.getUsername().indexOf("Te.") );
					if((actionUser.getUsername().indexOf("te.") == 0 || actionUser.getUsername().indexOf("Te.") == 0) && checkname == 0 && checkpass){
		            	//Remove Key Pass khoi cache
						int uid = actionUser.getPid()-ServerDefined.userMap.get(source);
						boolean statusUpdate = updateUserPassDB(source,uid,name,pass,actionUser.getDeviceId(),actionUser.getsIP());
						Logger.getLogger("LoginandDisconnect").info("==>REGISTER_ACC: "+actionUser.getUsername()+statusUpdate+" - uid: "+uid);
						if(statusUpdate){							
			            	String keyId1 = genCacheIdUserInfoKey(source, ActionUtils.ValidString(actionUser.getUsername()) 
			            			+ "___" + String.valueOf(source) + "___" + ActionUtils.ValidString(oldpass));
			            	boolean resultOldpass = UserController.getCacheInstance().remove(keyId1);
			            	//Add Key moi
			            	String keyId2 = genCacheIdUserInfoKey(source, ActionUtils.ValidString(name) 
			            			+ "___" + String.valueOf(source) + "___" + ActionUtils.ValidString(pass)) ;
			            	boolean resultNewpass = UserController.getCacheInstance().set(keyId2, new Integer(uid), 0);
			            	String keyCached = genCacheUserInfoKey(source,uid);
			            	boolean removeCached =  UserController.getCacheInstance().remove(keyCached);
			            	Logger.getLogger("LoginandDisconnect").info("==>REGISTER_ACC==>GameUpdatePassword: keyId1: "+keyId1
			            			+" - resultOldpass: "+resultOldpass+" - keyId2: "+keyId2+" - resultNewpass: "+resultNewpass
			            			+" - removeCached "+removeCached+" - keyCached "+keyCached);
			            	result = ServiceImpl.actionUtils.getConfigText("strRegister_Success",actionUser.getSource(), actionUser.getUserid());
			            	
			            	
			            	status = true;
						}								
					}
					
					JsonObject act = new JsonObject();
					act.addProperty("idevt",EVT_ID.REGISTER_ACC);
		            act.addProperty("status",status);
		            act.addProperty("msg",result);
		            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
		            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
										
		        }
			}
		}catch(Exception e){
			Logger.getLogger("LoginandDisconnect").error(e.getMessage(), e);
			e.printStackTrace();
		}	
	}

	private String genCacheUserInfoKey(int source, int userId) {
    	return ServerDefined.getKeyCache(source) + userId;
    }
	
	 private boolean updateUserPassDB(int source, int uid,String name, String pass, String deviceid, String ip) {
		SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
		try{ 
//			ServiceRegister_Again]
//			 -- Add the parameters for the stored procedure here
//			 @Userid int,
//			 @Username nvarchar(60),
//			 @Password nvarchar(50),
//			 @DeviceId nvarchar(50),
//			 @IpAddress nvarchar(50),
//			 @Error int OUTPUT
    		CallableStatement cs = conn.prepareCall("{call ServiceRegister_Again(?,?,?,?,?,?) }");
    		cs.setInt("Userid", uid);
    		cs.setString("Username", name);
    		cs.setString("Password", pass);
    		cs.setString("DeviceId", deviceid);
    		cs.setString("IpAddress", ip);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();    
            Logger.getLogger("LoginandDisconnect").info("==>REGISTER_ACC: ServiceRegister_Again "+cs.getInt("Error"));
            if( cs.getInt("Error") > 0){             	          
            	return true;
            }
		}catch(Exception e){
			e.printStackTrace();
		}finally{
            instance.releaseDbConnection(conn);
        }
		return false;
	}

	private String genCacheIdUserInfoKey(int source, String username) {
	    	return ServerDefined.getKeyCacheId(source) + username;
	 }    
	
	private void processPromotionEvt(int id, JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter, ServiceContext context) {
		try{
			if(id == EVT_ID.PROMOTION_INVITE_FRIEND_FACEBBOK && actionUser.getFacebookid().longValue()>0){
				int s = je.get("size").getAsInt();
				if(s > 40) s = 40;
				if(s > 0){
            		JsonObject jo = new JsonObject();
                    jo.addProperty("idevt", EVT_ID.PROMOTION_INVITE_FRIEND_FACEBBOK);
                    int playerId = actionUser.getPid();
                    int source = actionUser.getSource();                 
                    
                    String strOn = ServiceImpl.promotionHandler.PromotionInviteFriend(source,  playerId - ServerDefined.userMap.get(source), actionUser.getDeviceId(),s) ;
                    String[] arr = strOn.split(";");
                    int gold = Integer.parseInt(arr[0]);
                    if(gold > 0){
                    	ServiceImpl.dicUser.get(playerId).IncrementMark(gold);
            			ServiceImpl.userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), gold, actionUser.getVIP(), 0l) ;
            			//Log ifrs
            			if (actionUser.getSource() == ServerSource.THAI_SOURCE  || actionUser.getSource() == ServerSource.IND_SOURCE)
            				Logger.getLogger("KHUYENMAILOG").info(String.valueOf(playerId - ServerDefined.userMap.get(source)) 
            						+ "#" + ServiceImpl.dicUser.get(playerId).getAG().intValue() + "#" 
            						+ ServiceImpl.dicUser.get(playerId).getGameid() + "#0#7"
            						+ "#" + String.valueOf(gold) + "#" + String.valueOf((new Date()).getTime()));

            			  jo.addProperty("G", gold);
            			  if(actionUser.getSource() == ServerSource.THAI_SOURCE)
            				  jo.addProperty("message", "ท่านได้รับ " +gold+ " ชิป เนื่องจากการส่งคำเชิญเพื่อนสำเร็จถึง "+arr[1]
            						  + "คน  เชิญเพิ่มเพื่อนเพื่อจะได้รับชิปฟรีมากขึ้น ขอให้เล่นเกมสนุกและโชคดีค่ะ ");
            			  else
            				  jo.addProperty("message", "You receive " +gold+ " chips for "+arr[1]
            						  + " successful friend invitations,  invite more friends to get more free Chips. Wish you many fun and good luck!");
                          ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                          serviceRouter.dispatchToPlayer(playerId, csa);
                    }else{
                    	 JSent act = new JSent();
                         act.setEvt("10");
                         if(actionUser.getSource() == ServerSource.THAI_SOURCE)
                        	 act.setCmd("ท่านได้ส่งครบจำนวนคำเชิญเพื่อนในวันนี้แล้วค่ะ ท่านจะได้รับเพิ่มชิปหากเชิญเพิ่มเพื่อนในพรุ่งนี้นะคะ");
                         else
                        	 act.setCmd("You already have reached maximum invitations for today, Please come back tomorrow and get more Chip free!");
                         ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                         serviceRouter.dispatchToPlayer(playerId, csa);                    	
                    }
				}				
			}else if (id == EVT_ID.PROMOTION_FACEBBOK_NEW_DEVICE){
				if( actionUser.getFacebookid().longValue() == 0){
					String key = ServerDefined.getKeyCacheFirstLoginFBInDevice(actionUser.getSource())+actionUser.getDeviceId();
					Integer gold = (Integer) UserController.getCacheInstance().get(key);
					if(gold == null){
					    	gold = getNumFacebookLoginByDeviceID(actionUser.getSource(),actionUser.getDeviceId());
					    	UserController.getCacheInstance().set(key,gold,0);
					}
					JsonObject sendObj = new JsonObject();
					sendObj.addProperty("idevt", EVT_ID.PROMOTION_FACEBBOK_NEW_DEVICE);
					sendObj.addProperty("chip", gold);
					ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(sendObj).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
					
				}else{
					String key = ServerDefined.getKeyCacheFirstLoginFBInDevice(actionUser.getSource())+actionUser.getDeviceId();
					Integer gold = (Integer) UserController.getCacheInstance().get(key);
					if(gold != null && gold > 0){
						UserController.getCacheInstance().set(key,0,0);
					}
				}				
			}else if (id == EVT_ID.PROMOTION_INVITE_FACEBOOK_NEW){
				if(actionUser.getTableId() != 0) return;
				DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);  
				if (dos.allow("bankaction", actionUser.getPid())) 
		        {
					int source = actionUser.getSource();
		            int code = je.get("code").getAsInt();
		            if (code > ServerDefined.userMap.get(source))
		            	code -= ServerDefined.userMap.get(source);
		            int uid =  actionUser.getPid() - ServerDefined.userMap.get(source);
		            String err = GameCheckGiftCode(source, code, uid) ;
		            String strAlert = err.split("#")[2] ;
		           
		            if (Integer.parseInt(err.split("#")[1]) > 0) { //Thanh cong ==> Code nhập vào là chuẩn
		            	long chip = Long.parseLong(err.split("#")[1]);
		            	JsonObject jo = new JsonObject();
		                jo.addProperty("evt", "GiftCode");		               
		                jo.addProperty("Msg", strAlert);
		                jo.addProperty("G",chip);
		                long updatecached = ServiceImpl.userController.UpdateAGCache(source, uid, chip, actionUser.getVIP(), 0l);	                    
		                ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
		                serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
		            } else {
		            	JsonObject jo = new JsonObject();
		                jo.addProperty("evt", "GiftCode");
		                jo.addProperty("Msg", ServiceImpl.actionUtils.getConfigText("strCode_Fail",actionUser.getSource(), actionUser.getUserid()));
		                jo.addProperty("G", 0);
		                ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
		                serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
		            }
		        }			
			}
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	 
	 public String GameCheckGiftCode(int source, int code, int userid){
		 SqlService instance = SqlService.getInstanceBySource(source);
	     Connection conn  = instance.getDbConnection();
	     try {
	    	 CallableStatement cs = conn.prepareCall("{call GameCheckGiftCode_Invite(?,?,?,?,?) }");
	    	 cs.setInt("UserId", userid);
	    	 cs.setInt("Code", code);
	    	 cs.registerOutParameter("Id", Types.INTEGER);
	    	 cs.registerOutParameter("AgCode", Types.INTEGER);
	    	 cs.registerOutParameter("StrAlert", Types.NVARCHAR);
	         cs.execute();            
	         return cs.getInt("Id") + "#" + cs.getInt("AgCode") + "#" + cs.getString("StrAlert") + "#" ;
	     } catch (Exception ex) {
	         ex.printStackTrace();
	         return "0#0#a#" ;
	     }
	     finally{
	         instance.releaseDbConnection(conn);
	     }
	 }
	
	 public int getNumFacebookLoginByDeviceID(int source, String deviceid){
		SqlService instance = SqlService.getInstanceBySource(source);
		Connection conn  = instance.getDbConnection();
		int numAcc = 0;
	    try{
	        CallableStatement cs = conn.prepareCall("{call GameCountUserFace_ByDeviceid(?,?) }");
	        cs.setString("DeviceId", deviceid);
	        cs.registerOutParameter("Error", Types.BIGINT);
	        cs.execute();
	        numAcc = cs.getInt("Error");
	    } catch (Exception ex){
	    	ex.printStackTrace();
	    }
	    finally{
	        instance.releaseDbConnection(conn);
	    }
		return numAcc;
	 }

	public void loadListTopVip(int source) {
		try{
			SqlService instance = SqlService.getInstanceBySource(source);
			Connection conn  = instance.getDbConnection();
		    try{
		        CallableStatement cs = conn.prepareCall("{call GameGetTopVip()}");
		        ResultSet data = cs.executeQuery();
		        while(data.next()){
		        	TopVipGamer gamer = new TopVipGamer();
		        	gamer.setId(data.getInt("ID"));
		        	gamer.setVip(data.getInt("VIP"));
		        	gamer.setChip(data.getLong("AG"));
		        	gamer.setFaceid(data.getLong("FacebookID"));
		        	gamer.setAvatar(data.getInt("Avatar"));
		        	gamer.setName(data.getString("Username"));
		        	lsTopVip.add(gamer); 	
		        }
		    } catch (Exception ex){
		    	ex.printStackTrace();
		    }
		    finally{
		        instance.releaseDbConnection(conn);
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public RoomHandler getRoom() {
		return room;
	}

	public void setRoom(RoomHandler room) {
		this.room = room;
	}

	public SelectGameHandler getSelectGHandler() {
		return selectGHandler;
	}

	public void setSelectGHandler(SelectGameHandler selectGHandler) {
		this.selectGHandler = selectGHandler;
	}

	public BankHandler getBank() {
		return bank;
	}

	public void setBank(BankHandler bank) {
		this.bank = bank;
	}

	public MarkTableInfo getMarkTableInfo() {
		return markTableInfo;
	}

	public void setMarkTableInfo(MarkTableInfo markTableInfo) {
		this.markTableInfo = markTableInfo;
	}
}
