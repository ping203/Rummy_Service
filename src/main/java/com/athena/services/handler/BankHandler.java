package com.athena.services.handler;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.Date;

import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.api.service.dosprotect.DosProtector;
import com.dst.ServerSource;
import com.google.gson.JsonObject;
import com.vng.tfa.common.SqlService;

public class BankHandler {
	public void processBank(int id, JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter, ServiceContext context) {
		try{
			DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
        	if (!dos.allow("bankaction", actionUser.getUserid())) 
        		return;
        	if (actionUser.getUnlockPass() == 0) {
              return;
        	}
        	if (actionUser.getTableId() != 0) {
              return;
        	}
			if(id == EVT_ID.BANK_GET_INFO){
				processGetBankInfo(je,actionUser,serviceRouter);
			} else if(id == EVT_ID.SEND_BANK){
				processSendChipToBank(je,actionUser,serviceRouter);
			} else if(id == EVT_ID.BANK_WITHDRAW){
				processWithDraw(je,actionUser,serviceRouter);
			} else if(id == EVT_ID.BANK_SEND_OTHER_USER){
				processSendToUser(je,actionUser,serviceRouter);
			}	
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	private void processSendToUser(JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter) {
		SqlService instance = SqlService.getInstanceBySource(actionUser.getSource());
        Connection conn  = instance.getDbConnection();
		try{
			long chipsend =  je.get("chip").getAsLong();
			if(chipsend > 0){
				int sourceid = ServerDefined.userMap.get((int)actionUser.getSource());			
				int toid = je.get("toid").getAsInt();
				if(toid > sourceid)
					toid-= sourceid;
				String toname = "";
				if(toid <= 0)
					toname = je.get("toname").getAsString();
				Logger.getLogger("BANKHANDLER").info("==>processSendToUser: "+actionUser.getPid()+" - chip: "+actionUser.getAG()
				+" - chipsend: "+chipsend+" - toid: "+toid+" - toname: "+toname);
				if((toid <= 0 && toname.length() == 0)|| (chipsend >= 1000000000)){
					JsonObject act = new JsonObject();
					act.addProperty("idevt", EVT_ID.BANK_SEND_OTHER_USER);
					act.addProperty("status", false);
	            	act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBank_Fail",actionUser.getSource(), actionUser.getUserid()));
					ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
				    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
				    return;
				}
				CallableStatement cs = conn.prepareCall("{call GameTransferAG_Using(?,?,?,?,?,?,?,?) }");
	    		cs.setInt("UserId",actionUser.getPid()-sourceid);
	    		cs.setLong("AG",chipsend);
	    		cs.setString("Username", toname);
	    		cs.setInt("ToId",toid);
	    		
	            cs.registerOutParameter("Error", Types.INTEGER);
	            cs.registerOutParameter("BankSender", Types.BIGINT);
	            cs.registerOutParameter("BankReceiver", Types.BIGINT);
	            cs.registerOutParameter("UserIdReceiver", Types.BIGINT);            
	            cs.execute();
	            
	            long BankSender = cs.getLong("BankSender");
	            long BankReceiver = cs.getLong("BankReceiver");
	            int UserIdReceiver = cs.getInt("UserIdReceiver");
	            JsonObject act = new JsonObject();
				act.addProperty("idevt", EVT_ID.BANK_SEND_OTHER_USER);
				int error = cs.getInt("Error");
				cs.close();
				//@BankSender  ==> Số gold mới trong bank thằng gửi
				// @BankReceiver ==> Số gold mới trong bank thằng nhận ==> không có thì giá trị =-1
				// @UserIdReceiver ==> Id của thằng nhận.
				
	            if(error > 0){
	            	String key_bank = KeyCachedDefine.getKeyCachedBank(actionUser);
	    			UserController.getCacheInstance().set(key_bank,BankSender,0);
	    			
	    			if(BankReceiver > 0){
	    				int idrecei = UserIdReceiver+ServerDefined.userMap.get((int)actionUser.getSource());
	    				String key_bank_receiver = "bank_"+actionUser.getSource()+"_"+idrecei;
		    			UserController.getCacheInstance().set(key_bank_receiver,BankReceiver,0);
		    			String fromname = actionUser.getUsername();
		    			if(actionUser.getUsernameLQ().length() > 0)
		    				fromname = actionUser.getUsernameLQ();
		    			if(ServiceImpl.dicUser.containsKey(idrecei)){
		    				JsonObject jsonRecei  = new JsonObject();
		    				jsonRecei.addProperty("idevt", EVT_ID.BANK_YOU_GET_CHIP_FROM_ORTHER_USER);
		    				if(actionUser.getSource() == ServerSource.THAI_SOURCE){
		    					jsonRecei.addProperty("msg", "คุณได้รับ "+ActionUtils.formatAG(chipsend)+" ชิปจากบัญชี "+fromname);
		    				}else if(actionUser.getSource() == ServerSource.IND_SOURCE)
		    					jsonRecei.addProperty("msg", "Anda dapat "+ActionUtils.formatAG(chipsend)+" chips dari "+fromname);
		    				else
		    					jsonRecei.addProperty("msg", "You get "+chipsend+" chips from "+fromname);
		    				ClientServiceAction csa = new ClientServiceAction(idrecei, 1, ActionUtils.gson.toJson(jsonRecei).getBytes("UTF-8"));
		    		        serviceRouter.dispatchToPlayer(idrecei, csa); 
		    			}
	    			}    			                 
	                
	                act.addProperty("status", true);
	                if (actionUser.getSource() == ServerSource.THAI_SOURCE)
	                	act.addProperty("msg", "การโอนชิปสำเร็จแล้ว");
	                else
	                	act.addProperty("msg", "Success!");
	            	act.addProperty("chipbank", BankSender);
	            	act.addProperty("chipuser",  ServiceImpl.dicUser.get(actionUser.getPid()).getAG().longValue());
	                Logger.getLogger("BANKLOG").info(String.valueOf(actionUser.getPid()-sourceid) + "#" 
	                		+ ServiceImpl.dicUser.get(actionUser.getPid()).getAG().intValue() + "#7999#0#0#" 
	                		+ String.valueOf(0 - chipsend) + "#" + String.valueOf((new Date()).getTime()));
	            } else if (error == -1){
	            	act.addProperty("status", false);
	            	act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strFriend_Err6",actionUser.getSource(), actionUser.getUserid()));

				}else if (error == -2){
	            	act.addProperty("status", false);
	            	act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBank_Fail1",actionUser.getSource(), actionUser.getUserid()));
				} else if (error == -3){
	            	act.addProperty("status", false);
	            	act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBank_Fail3",actionUser.getSource(), actionUser.getUserid()));
				} else {	            	
	            	act.addProperty("status", false);
	            	act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBank_Fail",actionUser.getSource(), actionUser.getUserid()));
	            }
	            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
		        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
	            Logger.getLogger("BANKHANDLER").info(" ==>processSendToUser: "+actionUser.getPid()+" - BankSender: "+BankSender
	            						+" - BankReceiver: "+BankReceiver+" - UserIdReceiver: "+UserIdReceiver+" - error: "+error);	       
			}else{
				JsonObject act = new JsonObject();
				act.addProperty("idevt", EVT_ID.BANK_SEND_OTHER_USER);
				act.addProperty("status", false);
            	act.addProperty("msg", "You send "+chipsend+" chips failed! Please try again!");
            	 ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
 		        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
			}		
			 
		}catch(Exception e){
			Logger.getLogger("BANKHANDLER").error(e.getMessage(), e);
			e.printStackTrace();
		}finally{
            instance.releaseDbConnection(conn);
        }		
	}

	private void processWithDraw(JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter) {
		try{
				Logger.getLogger("BANKHANDLER").info(" processBank.BANK_WITHDRAW - "+actionUser.getPid()+" - "+ActionUtils.gson.toJson(je));
				if(actionUser.getAGHigh() > 0 || actionUser.getAGLow() > 0)			
				{
					Logger.getLogger("BANKHANDLER").info(" "+actionUser.getPid()+" - "+ActionUtils.gson.toJson(je)+" - aghigh: "+actionUser.getAGHigh()+" - aglow: "+actionUser.getAGLow());
					return;
				}
			    if (actionUser.getTableId() != 0) {
			    	Logger.getLogger("BANKHANDLER").info(" "+actionUser.getPid()+" - getTableId: "+actionUser.getTableId());
			        return;
			    }             	
			    if(je.has("chip")){
			    	long chipGet = je.get("chip").getAsLong();
			    	Long bankchip = (Long) UserController.getCacheInstance().get(KeyCachedDefine.getKeyCachedBank(actionUser));
			    	if(bankchip != null){
			    		if(bankchip < chipGet){
			    			JsonObject act = new JsonObject();
							act.addProperty("idevt", EVT_ID.BANK_WITHDRAW);  
							act.addProperty("status", false);
							act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBank_NotGold",actionUser.getSource(), actionUser.getUserid()));

							Logger.getLogger("BANKHANDLER").info(" "+actionUser.getPid()+" - chip: "+actionUser.getAG()+" - bankchip: "+bankchip);
							ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
					        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
			    		}else{
			    			JsonObject act = new JsonObject();
							act.addProperty("idevt", EVT_ID.BANK_WITHDRAW);  
			
							long chipAfterGet = processUpdateDB(0-chipGet,actionUser);
							if(chipAfterGet >= 0){
								act.addProperty("status", true);
								act.addProperty("chipbank", chipAfterGet);
								act.addProperty("chipuser", ServiceImpl.dicUser.get(actionUser.getPid()).getAG().longValue());   					
							}else{
								act.addProperty("status", false);
								act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBank_Fail",actionUser.getSource(), actionUser.getUserid()));
							}
							Logger.getLogger("BANKHANDLER").info(" "+actionUser.getPid()+" - chip: "+actionUser.getAG()+" - chipAfterGet: "+chipAfterGet);
							ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
					        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
			    		}
			    	}else{
			    		JsonObject act = new JsonObject();
						act.addProperty("idevt", EVT_ID.BANK_WITHDRAW);  
		
						long chipAfterGet = processUpdateDB(0-chipGet,actionUser);
						if(chipAfterGet >= 0){
							act.addProperty("status", true);
							act.addProperty("chipbank", chipAfterGet);
							act.addProperty("chipuser", ServiceImpl.dicUser.get(actionUser.getPid()).getAG().longValue());   					
						}else{
							act.addProperty("status", false);
							act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBank_Fail",actionUser.getSource(), actionUser.getUserid()));
						}
						Logger.getLogger("BANKHANDLER").info(" "+actionUser.getPid()+" - chip: "+actionUser.getAG()+" - chipAfterGet: "+chipAfterGet);
						ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
				        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
			    	}
			    }	
		}catch(Exception e){
			e.printStackTrace();
		}		
	}

	private void processSendChipToBank(JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter) {
		try{
			Logger.getLogger("BANKHANDLER").info(" processBank.BANK_SEND - "+actionUser.getPid()+" - chip: "+actionUser.getAG()+" - "+ActionUtils.gson.toJson(je));
			if(actionUser.getAGHigh() > 0 || actionUser.getAGLow() > 0){
				Logger.getLogger("BANKHANDLER").info(" "+actionUser.getPid()+" - "+ActionUtils.gson.toJson(je)+" - aghigh: "+actionUser.getAGHigh()+" - aglow: "+actionUser.getAGLow());
				return;
			}
            if (actionUser.getTableId() != 0) {
            	Logger.getLogger("BANKHANDLER").info(" "+actionUser.getPid()+" - getTableId: "+actionUser.getTableId());
                return;
            }             	
            			
			if(je.has("chip")){
				JsonObject act = new JsonObject();
				act.addProperty("idevt", EVT_ID.SEND_BANK);          
				long chipSend = je.get("chip").getAsLong();
				if(chipSend < 1){
					act.addProperty("status", false);
					//act.addProperty("msg", "Chips must be greater than zero!");
					act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBankErrorSendZero",actionUser.getSource(), actionUser.getUserid()));
				} else if(chipSend > 1000000000){
					act.addProperty("status", false);
					//act.addProperty("msg", "Chips must be less than 1000,000,000!");
					act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBankErrorSendOver1Billion",actionUser.getSource(), actionUser.getUserid()));
				} else if( chipSend > actionUser.getAG().longValue()){
					act.addProperty("status", false);
					//act.addProperty("msg", "Chips must be less than current balance chips!");
					act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBankErrorSendOverBalance",actionUser.getSource(), actionUser.getUserid()));
				} else{
					Logger.getLogger("BANKHANDLER").info("processSendChip: source: "+actionUser.getSource()+" - uid: "
			        		+actionUser.getPid()+" - chipSend "+chipSend+" - currentChip: "+actionUser.getAG());    		
					long chipAfterSent = processUpdateDB(chipSend,actionUser);
					if(chipAfterSent >= 0){
						act.addProperty("status", true);
						act.addProperty("chipbank", chipAfterSent);
						act.addProperty("chipuser", ServiceImpl.dicUser.get(actionUser.getPid()).getAG().longValue());
					}else{
						act.addProperty("status", false);
						act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strBank_Fail",actionUser.getSource(), actionUser.getUserid()));
					}
					Logger.getLogger("BANKHANDLER").info(" "+actionUser.getPid()+" - chip: "+actionUser.getAG()+" - chipAfterGet: "+chipAfterSent);
				}			
				ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
	            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 
			}	
		} catch(Exception e){
			e.printStackTrace();
		}	
	}

	private void processGetBankInfo(JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter) {
		try{
			String key_bank = KeyCachedDefine.getKeyCachedBank(actionUser);
			Long chip = (Long) UserController.getCacheInstance().get(key_bank);
			if(chip == null){ // get from DB
				chip = getChipFromDB(actionUser.getSource(),actionUser.getPid()-ServerDefined.userMap.get((int)actionUser.getSource()),key_bank);
			}
			Logger.getLogger("BANKHANDLER").info("==>processGetBankInfo: processGetBankInfo: "+key_bank+" - chip: "+chip);
			JsonObject act = new JsonObject();
			act.addProperty("idevt", EVT_ID.BANK_GET_INFO);
			if(chip == null){
				act.addProperty("chip", -1);
				act.addProperty("msg", "Error get information! Please try again!");
			}else{
				act.addProperty("chip", chip);
			}				
            
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa); 	
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Long getChipFromDB(int source, int uid, String key) {
		SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
		try{			
    		CallableStatement cs = conn.prepareCall("{call ServiceGetBankInfo(?,?) }");
    		cs.setInt("userid", uid);
            cs.registerOutParameter("chip", Types.BIGINT);
            cs.execute();
            long chip = cs.getLong("chip") ;
            UserController.getCacheInstance().set(key,chip,0);
            return chip;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
            instance.releaseDbConnection(conn);
        }
		return null;
	}

	private long processUpdateDB(long chipsend, UserInfo actionUser) {
		SqlService instance = SqlService.getInstanceBySource(actionUser.getSource());
        Connection conn  = instance.getDbConnection();
		try{ 
    		CallableStatement cs = conn.prepareCall("{call ServiceUpdateBank(?,?,?,?) }");
    		cs.setInt("uid", actionUser.getPid()-ServerDefined.userMap.get((int)actionUser.getSource()) );
    		cs.setLong("chipsend", chipsend);
            cs.registerOutParameter("chipbank", Types.BIGINT);
            cs.registerOutParameter("chipuser", Types.BIGINT);
            cs.execute();
            long chipbank = cs.getLong("chipbank");
            long chipuser = cs.getLong("chipuser");
            Logger.getLogger("BANKHANDLER").info(" ==>processUpdateDB: actionUser "+actionUser.getPid()+" - chipbank: "+chipbank+" - chipuser: "+chipuser+" - chipsend: "+chipsend);
            if(chipbank >= 0 && chipuser >= 0){             	
            	UserController.getCacheInstance().set(KeyCachedDefine.getKeyCachedBank(actionUser),chipbank,0);
            	if(chipsend > 0){
            		ServiceImpl.dicUser.get(actionUser.getPid()).DecrementMark(chipsend);
                	ServiceImpl.userController.UpdateAGCache(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int)actionUser.getSource()), 0-chipsend, actionUser.getVIP(), 0);    
     
            	}else{
            		ServiceImpl.dicUser.get(actionUser.getPid()).IncrementMark(0-chipsend);
                	ServiceImpl.userController.UpdateAGCache(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int)actionUser.getSource()), 0-chipsend, actionUser.getVIP(), 0);    
            	}
            	return chipbank;
            }
		}catch(Exception e){
			e.printStackTrace();
		}finally{
            instance.releaseDbConnection(conn);
        }
		return -1;
	}
}
