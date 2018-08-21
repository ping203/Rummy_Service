package com.athena.services.bot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.GAMEID;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;

public class DummyThaiBotHandler implements Bot {
	// So luog bot
	 //public final int BOT10    = 750;
	 public final int BOT50    = 500;
	 public final int BOT100   = 350;
	 public final int BOT200   = 250;
	 public final int BOT500   = 150;
	 public final int BOT1000  = 50;
	 public final int BOT2000  = 0;
	 public final int BOT5000  = 0;
	 public final int BOT10000 = 0;
	 public final int BOT20000 = 0;
	 public final int MAX_ADD_AG = 500;
	 
//     lsMarkCreateTable.add(new MarkCreateTable(10, 5000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(50, 13000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(100, 30000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(200, 60000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(500, 130000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(1000, 300000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(2000, 100000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(5000, 2000000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(10000, 5000000, 0)) ;
//     lsMarkCreateTable.add(new MarkCreateTable(20000, 8000000, 0)) ;
	 
	 public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotDummyThai
	 						= ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
	 public List<UserInfo> listBotDummyThai = new ArrayList<UserInfo>();
	 public ServiceRouter serviceRouter;
	 
	 public DummyThaiBotHandler(int source) {
		getListBot(source);
	}

	@Override
	public void botRejectJoinTable(int pid) {
		throw new UnsupportedOperationException();
	}
	public void getUserGame(int minAG,short gameId, int tableId, int Diamond){
		 //if(minAG > 100) return;
		 //if(Diamond > 0)
		//	 Logger.getLogger("Dummy_GetBot_Err").info("==>BotHandler==>getUserGame: listBotNew size:"+ listBotDummyThai.size()
		//	 +" - minAG: "+minAG+" - Diamond: "+Diamond);
		 //System.out.println("==>BotHandler==>getUserGame: "+ listBotNew.size()+" - "+minAG+" - "+Diamond);:
		 if(Diamond != 0) return;
		// System.out.println("==>DummyThaiBotHandler==>getUserGame==>getBot: "+listBotDummyThai.size()+" - "+minAG+" - "+ gameId +" - "+tableId);
		  synchronized (listBotDummyThai){
			  try{
				  short type = 11; // bot bàn 100
//				  if(minAG >= 500000) // bàn 500k
//					  type = 33;
//				  else if (minAG >= 100000){ // 100k
//					  type = 32;
//				  }else if (minAG >= 50000){ // bàn  50k
//					  type = 31;
//				  }else if (minAG >= 10000){ // bàn  10k
//					  type = 23;
//				  }else if (minAG >= 5000){ // bàn  5k
//					  type = 22;
//				  }else 
				  if (minAG >= 1000){ // bàn  1k
					  type = 23;
				  } else if (minAG >= 500){ // bàn  500
					  type = 22;
				  } else if (minAG >= 100){ // bàn  100
					  type = 21;
				  } else if (minAG >= 50){ // bàn  50
					  type = 12;
				  } else if (minAG == 2){ // bàn  50
					  type = 13;
				  }
				  boolean check = true;  
				  //Duyệt tìm boss sẵn sàng đủ tiền chơi
				  for(int i = 0; i < listBotDummyThai.size(); i++){	
					  //System.out.println("==>DummyThaiBotHandler==>getUserGame: "+ listBotDummyThai.get(i).getGameid()+" - "
				 // +listBotDummyThai.get(i).getUsertype()+" - "+ type+" - isOl: "+listBotDummyThai.get(i).getIsOnline());
		    			if(listBotDummyThai.get(i).getGameid() == gameId // client bot connected
		    					&& listBotDummyThai.get(i).getUsertype() == type){ // check type + ag
		    				UserInfo u = listBotDummyThai.get(i);
		    				int addAG = 0;
		    				//System.out.println("\n ==>BotHandler==>BotCreateTable GETTTTTT : "
		    				//			+ActionUtils.gson.toJson(listBotNew.get(i))+" \nBOTGETTTTT\n");
		    				if(u.getAG() < 250*minAG && u.getCPromot() < MAX_ADD_AG){ // kiem tra them tien cho bot
//		    					Mức cược bàn	Nhóm 	Số bot tiếp khách	MIN	MAX	Note	
//		    					100		1	60	5,000	20,000	thả	
//		    					500		1	50	25,000	100,000	thả	
//		    					1,000	2	50	50,000	150,000		
//		    					5,000	2	40	250,000	500,000		
//		    					10,000	2	30	500,000	2,000,000		
//		    					50,000	3	25	2,500,000	5,000,000		
//		    					100,000	3	20	5,000,000	20,000,000		
//		    					500,000	3	15	25,000,000	100,000,000	
		    					u.setCPromot(u.getCPromot().intValue()+1);
		    					addAG = getAGADD(type);
		    				}
		    				if(addAG > 0){
                                                        int abDB = u.getAG().intValue();
		    					u.setAG(u.getAG()+addAG); 				    				
		    					ActionUtils.updateAGBOT(u, u.getAG().intValue(),abDB, GAMEID.DUMMY_FAST);
			    				Logger.getLogger("Bot_Details").info(ActionUtils.gson.toJson(u)+" - "+minAG+ " - "+addAG);
		    				}	    				
		    				u.setIsOnline(gameId);	    				
							dicBotDummyThai.put(u.getUserid(), u);
							//System.out.println("==>BotHandler==>getUserGame: "+ActionUtils.gson.toJson(u));
							JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
							serviceRouter.dispatchToGame(gameId, action);
							check = false;
							break;
		    			} 
		    		}
				  if(check)
					  Logger.getLogger("Bot_Details").info("getUserGame failed: minAG:"+minAG+" - List:"+ listBotDummyThai.size());
		    	}catch (Exception e) {
		    		e.printStackTrace();
		        }
		  	}			
	    }
	 	 public  int getAGADD(short type) {
		    	int addAG = 0;
		    	switch (type) {
				case 33:
					addAG = randomBetween2Number(25000000,100000000);
					break;
				case 32:
					addAG = randomBetween2Number(5000000,20000000);
					break;
				case 31:
					addAG = randomBetween2Number(2500000,10000000);
					break;
					
				case 23:
					addAG = randomBetween2Number(500000,2000000);
					break;
				case 22:
					addAG = randomBetween2Number(250000,1000000);
					break;
					
				case 21:
					addAG = randomBetween2Number(50000,200000);
					break;
				case 12:
					addAG = randomBetween2Number(25000,100000);
					break;
				case 11:
					addAG = randomBetween2Number(5000,20000);
					break;
				case 13:
					addAG = randomBetween2Number(500,3000);
					break;
				default:
					break;
				}
			return addAG;
		}
	 	public int randomBetween2Number(int lowerBound, int upperBound){
			Random random = new Random();
			int randomNumber = random.nextInt(upperBound - lowerBound) + lowerBound;
			return randomNumber;
		}
	    public  int getVip(short type) {
	    	int addAG = 0;
	    	switch (type) {
//	    	0->2
//	    	1->3
//	    	1->3
//	    	2->4
//	    	2->4
//	    	2->5
//	    	3->6
//	    	4->8
			case 33:
				addAG = (new Random()).nextInt(5)+4; 
				break;
			case 32:
				addAG = (new Random()).nextInt(4)+3;
				break;
			case 31:
				addAG = (new Random()).nextInt(3)+2; 
				break;
				
			case 23:
				addAG = (new Random()).nextInt(3)+2;
				break;
			case 22:
				addAG = (new Random()).nextInt(3)+2;
				break;
				
			case 21:
				addAG = (new Random()).nextInt(3)+1; 
				break;
			case 12:
				addAG = (new Random()).nextInt(3)+1;
				break;
			case 11:
				addAG =  (new Random()).nextInt(3);
				break;
			case 13:
				addAG =  (new Random()).nextInt(3);
				break;
			default:
				break;
			}
		return addAG;
	}


		public UserInfo updateBotOnline(int pid, short isOnline){
	    	try{
	    		if(isOnline == 0){
	    			//synchronized (listBotNew) {    
	    				//System.out.println("==>BotHandler==>updateBotOnline:before " + ServiceImpl.listBot.size());
						if(dicBotDummyThai.containsKey(pid) && dicBotDummyThai.get(pid).getIsOnline() > 0) {												
							dicBotDummyThai.get(pid).setIsOnline(isOnline); 
							dicBotDummyThai.get(pid).setTableId(0); 
							if(dicBotDummyThai.get(pid).isDisconnect()) dicBotDummyThai.get(pid).setGameid((short)0);
							UserInfo u = dicBotDummyThai.get(pid);	
				            listBotDummyThai.add(u);
					}
	    		}else{
	    			dicBotDummyThai.get(pid).setIsOnline(isOnline);
	    		}
	    	}catch (Exception e) {
	    		e.printStackTrace();
	        }
	    	return dicBotDummyThai.get(pid);
	    }

		public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU) {
			synchronized (dicBotDummyThai) {
	             if (dicBotDummyThai.containsKey(uid)) {
	            	// System.out.println("UpdateBotMarkChessByName: dicBotNew.containsKey(name)" + uid+"-"+
	            	//		 	name+"-"+mark+"-"+dicBotNew.get(uid).getAG());
	                 try {
	                     long ag = dicBotDummyThai.get(uid).getAG() + mark; 
	                     if (ag < 0) {
	                         ag = 0;
	                     }
	                     dicBotDummyThai.get(uid).setAG(ag); 
	                     UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid- ServerDefined.userMap.get(source), mark);
	                     QueueManager.getInstance(UserController.queuename).put(cmd);
	                     return dicBotDummyThai.get(uid).getAG();
	                 } catch (Exception e) {
	                	 e.printStackTrace();
	                     //System.out.println("==>Error==>botHandler==>UpdateBotMarkChessByName:" + e.getMessage() + "-" + uid+"-" + name + "-" + mark);
	                     return 0l;
	                 }
	             }else{
	            	 Logger.getLogger("Bot_Details").info("UpdateBotMarkChessByName: !dicBotNew.containsKey(name)" + uid+"-"+name+mark);
	             }
	         }
			return 0l;
		}

		public void BotCreateTable(int gameid, int mark) {
			try{
				//System.out.println("==>BotHandler==>BotCreateTable "+ gameid+" - " + mark);
				short type = 11;
				if(mark == 50000){
					type = 31;
				} else if (mark == 100000){
					type = 32;
				} else if (mark == 500000){
					type = 33;
				}
				//System.out.println("==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotNew));
				boolean check = true;
				for(int i = 0; i < listBotDummyThai.size(); i++){	
					//if(listBotNew.get(i).getGameid() == gameid)
					//System.out.println("\n ==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotNew.get(i))+" \nBOT\n");
	    			if(listBotDummyThai.get(i).getGameid() == gameid && listBotDummyThai.get(i).getUsertype() == type && listBotDummyThai.get(i).getRoomId() == 0){
	    				check = false;
	    				
	    				UserInfo u = listBotDummyThai.get(i);
	    				
	    				//System.out.println("==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotNew.get(i))+"\n");
	    				if(u.getAG() < 10*mark && u.getCPromot() < MAX_ADD_AG){ // kiem tra them tien cho bot
	    					u.setCPromot(u.getCPromot().intValue()+1);
	    					int addAG = getAGADD(type);
	    					if(addAG > 0){
		    					u.setAG(u.getAG()+addAG); 			
		    					UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(),
				                    	u.getUserid() - ServerDefined.userMap.get((int)u.getSource()), addAG);
				                QueueManager.getInstance(UserController.queuename).put(cmd);
			    				Logger.getLogger("Bot_Details").info(ActionUtils.gson.toJson(u)+" - "+mark+ " - "+addAG);
			    				ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.DUMMY_FAST);
		    				}
	    				}else if (u.getAG() < 10*mark){
	    					continue; // tim bot tiep
	    				}
	    				listBotDummyThai.get(i).setRoomId((short)((new Random()).nextInt(3)+1));
	    				u.setIsOnline((short)gameid);	
	                    //listBotNew.remove(i);
						dicBotDummyThai.put(u.getUserid(), u);
						//System.out.println("==>BotHandler==>BotCreateTable : OK");
						
						JsonObject send = new JsonObject();
	                    send.addProperty("evt", "botCreateTable");
	                    send.addProperty("pid", u.getPid());
	                    send.addProperty("M",  mark);
	                    ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
	                    serviceRouter.dispatchToGameActivator(gameid, request);
						break;
	    			}				
				}
				if(check){
    				for(int i = 0; i < listBotDummyThai.size(); i++){	
    					if(listBotDummyThai.get(i).getUsertype() == type)
    						Logger.getLogger("Bot_Details").info("==>BotHandler==>HadBotCreateTable: failed listBotNew size: "+listBotDummyThai.size()
    						+" - gameid: " +gameid+" - mark: "+mark);
    				}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		public ServiceRouter getServiceRouter() {
			return serviceRouter;
		}

		public void setServiceRouter(ServiceRouter serviceR) {
			serviceRouter = serviceR;
		}
		
		 public void notifyBot() {
		    	try {
		    		// 1. send notify
//		    	    var options = {
//		    	        method: 'POST',
//		    	        url: 'https://onesignal.com/api/v1/notifications',
//		    	        headers: {
//		    	            'Content-Type': 'application/json',
//		    	            'Authorization': 'Basic NGMzODk0ODQtMzE0Ni00N2Y5LWE3YmMtZDU1MjVkZDQ5ZTUz'
//		    	        },
//		    	        json: {
//		    	            app_id: "0a8074bb-c0e4-48cc-8def-edd22ce17b9d",
//		    	            headings: { "en": "DST Notify" },
//		    	            included_segments: ["All"],
//		    	            contents: { "en": msg },
//		    	            ios_badgeType: "Increase",
//		    	            ios_badgeCount: 1
//		    	        }
//		    	    };
//		    		
//		    		int timeout = 15;
//		            RequestConfig config = RequestConfig.custom()
//		                    .setConnectTimeout((timeout - 10) * 1000)
//		                    .setConnectionRequestTimeout((timeout - 10) * 1000)
//		                    .setSocketTimeout(timeout * 1000).build();
//		            CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
//		            String url = "https://onesignal.com/api/v1/notifications" ;
//
//					HttpPost pos = new HttpPost(url);
//					pos.addHeader("Content-Type", "application/json");
//				    pos.addHeader("Authorization", "Basic NGMzODk0ODQtMzE0Ni00N2Y5LWE3YmMtZDU1MjVkZDQ5ZTUz");
//				    
//					JsonObject obj = new JsonObject();
//					obj.addProperty("app_id", "0a8074bb-c0e4-48cc-8def-edd22ce17b9d");
//					
//					JsonObject js = new JsonObject();					
//					js.addProperty("en","DST Notify");
//					
//					obj.addProperty("headings",ActionUtils.gson.toJson(js));
//					
//					JsonArray arr = new JsonArray();
//					arr.add(ActionUtils.gson.fromJson("All",JsonElement.class));
//					
//					obj.add("included_segments", arr);
//					obj.addProperty("included_segments",ActionUtils.gson.toJson("js"));
//					
//					JsonObject ms = new JsonObject();					
//					ms.addProperty("en","MSG");
//					obj.addProperty("contents",ActionUtils.gson.toJson(ms));
//					obj.addProperty("ios_badgeType","Increase");
//					obj.addProperty("ios_badgeCount",1);
//				   
//			        StringEntity params = new StringEntity(ActionUtils.gson.toJson(obj));
//
////	    	            "headings\":\{\"en\":\"DST Notify\"\}\,\
////	    	            "included_segments\":\[\"All\"]\,\
////	    	           "contents\":\{\"en\":\"ms\"}\,\
////	    	           "ios_badgeType\":\"Increase\",\
////	    	            "ios_badgeCount\":\1}\");
//			       System.out.println("==>Params: "+ActionUtils.gson.toJson(obj));
//			       System.out.println("==>Params: "+ActionUtils.gson.toJson(pos));
//			       System.out.println("==>Params: "+ActionUtils.gson.toJson(params));
//			        pos.setEntity(params);
//					
//					httpclient.execute(pos);
//					httpclient.close();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("==>Error==>notifyBot");
					//logger_.error(e.getMessage() + "-" + username);
				}
		    }

		public void checkBot(JsonObject je, UserInfo actionUser) {
			try{
				//BotHandler.notifyBot();
	        	//System.out.println(ActionUtils.gson.toJson(BotHandler.dicBotNew));
	        	//System.out.println(ActionUtils.gson.toJson(BotHandler.listBotNew));
	        	int s = 0, t11 = 0, t12 = 0, t21= 0, t22 = 0, t23 = 0, t31 = 0, t32 = 0, t33 = 0; 
	        	List<String> lsBot = new ArrayList<String>();
	        	for(int i= 0; i< listBotDummyThai.size(); i++){
	        		if(listBotDummyThai.get(i).getGameid() == 0)
	        			s++;          		
	        		if(listBotDummyThai.get(i).getUsertype() > 32)
	        			t33++;
	        		else if(listBotDummyThai.get(i).getUsertype() > 31)
	        			t32++;
	        		else if(listBotDummyThai.get(i).getUsertype() > 30)
	        			t31++;
	        		else if(listBotDummyThai.get(i).getUsertype() > 22)
	        			t23++;
	        		else if(listBotDummyThai.get(i).getUsertype() > 21)
	        			t22++;
	        		else if(listBotDummyThai.get(i).getUsertype() > 20)
	        			t21++;
	        		else if(listBotDummyThai.get(i).getUsertype() > 11)
	        			t12++;
	        		else if(listBotDummyThai.get(i).getUsertype() > 10)
	        			t11++;
	        		if(je.has("T") && je.get("T").getAsInt() == listBotDummyThai.get(i).getUsertype())
	        			lsBot.add(listBotDummyThai.get(i).getUsername());
	        	}
	        	JsonObject jo = new JsonObject();
	        	jo.addProperty("evt", "checkBot");
	        	jo.addProperty("Disconnected", s);
	        	jo.addProperty("IsOnline","Size: "+listBotDummyThai.size()+" - "+ t33+"/"+t32+"/"+t31+"/"+t23+"/"
	        				+t22+"/"+t21+"/"+t12+"/"+t11);
	        	jo.addProperty("Bot", ActionUtils.gson.toJson(lsBot));
	        	ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
	        	serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);       
			}catch(Exception e){
				e.printStackTrace();
			}			    		
		}

		public void reloadBot(JsonObject je, UserInfo actionUser) {
			try{
				listBotDummyThai.clear();
            	getListBot(actionUser.getSource());
            	int s = 0, t = 0;
            	for(int i= 0; i< listBotDummyThai.size(); i++){
            		if(listBotDummyThai.get(i).getGameid() == 0)
            			s++;
            		if(listBotDummyThai.get(i).getIsOnline() > 0)
            			t++;
            	}
            	
            	JsonObject jo = new JsonObject();
            	jo.addProperty("evt", "reloadBot");
            	jo.addProperty("Disconnected", s);
            	jo.addProperty("IsOnline", t+"/"+listBotDummyThai.size());
            	ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            	serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);       
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}

		public void getListBot(int source) {
	        SqlService instance = SqlService.getInstanceBySource(source);
	        Connection conn  = instance.getDbConnection();
	        
	        
	    	try{
	            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
	            cs.setInt("Gameid",GAMEID.DUMMY_FAST);
	            ResultSet rs = cs.executeQuery();     
	            short type = 0;
	            listBotDummyThai.clear();
	            while (rs.next()) {
	            	UserInfo userTemp = new UserInfo();
//	            	System.out.println("==>Name:" + rs.getString("Username")) ;
	            	
	            	//long ag = rs.getLong("AG");
	            	userTemp.setUserid(rs.getInt("ID")+ServerDefined.userMap.get(source));
	            	userTemp.setFacebookid(Long.parseLong(rs.getString("FacebookId")));
	            	userTemp.setDeviceId(rs.getString("DeviceID"));
	            	//userTemp.setAG(ag);
	            	//userTemp.setVIP((short)rs.getInt("VIP"));
	            	userTemp.setIsOnline((short)rs.getInt("IsOnline"));
	            	userTemp.setBanned(rs.getBoolean("IsBanned"));
	            	userTemp.setAvatar((short)rs.getInt("Avatar")) ;
	            	userTemp.setOnlineDaily((short)rs.getInt("OnlineDaily")) ;
	            	userTemp.setPromotionDaily(rs.getInt("PromotionDaily")) ;
	            	userTemp.setCreateTime(rs.getDate("CreateTime").getTime());
	            	userTemp.setLastLogin(rs.getDate("LastLogin").getTime());
	            	userTemp.setUsername(rs.getString("Username"));
	            	userTemp.setUsernameLQ(rs.getString("UsernameLQ"));
	            	userTemp.setIdolName(rs.getString("IdolName"));
	            	userTemp.setSource((short)source);
	            	//userTemp.setSource((short)5);
	            	if (userTemp.getUsernameLQ().length()>0)
	            		userTemp.setUsername(userTemp.getUsernameLQ());
	            	
	            	type++;
////	            		 public final int BOT50    = 500;
//		           	 public final int BOT100   = 350;
//		        	 public final int BOT200   = 250;
//		        	 public final int BOT500   = 150;
//		        	 public final int BOT1000  = 50;
//		        	 public final int BOT2000  = 0;
//		        	 public final int BOT5000  = 0;
//		        	 public final int BOT10000 = 0;
//		        	 public final int BOT20000 = 0;
	            	/*if(type <=  BOT20000){
	            		userTemp.setUsertype((short)41);
	            	}else if (type <= BOT10000){
	            		userTemp.setUsertype((short)33);           		
	            	}
	            	else if (type <= BOT5000){
	            		userTemp.setUsertype((short)32);	
	            	}
	            	else if (type <= BOT2000){
	            		userTemp.setUsertype((short)31);
	            	}
	            	else if (type <= BOT1000){
	            		userTemp.setUsertype((short)23);
	            	}
	            	else if (type <= BOT500){
	            		userTemp.setUsertype((short)22);
	            	}
	            	else if (type <= BOT200){
	            		userTemp.setUsertype((short)21);
	            	}
	            	else if (type <= BOT100){
	            		userTemp.setUsertype((short)12);
	            	} else
	            		userTemp.setUsertype((short)11);*/
	            	if (type <= 150)
	            		userTemp.setUsertype((short)21);
	            	else if (type <= 300)
	            		userTemp.setUsertype((short)12);
		            else if (type <= 400)
	            		userTemp.setUsertype((short)13);
	            	else
	            		userTemp.setUsertype((short)11);			  
	            	// kiem tra them tien cho bot
	            	int agDB = rs.getInt("AG");	            		            	
	            	int ag = getAGADD(userTemp.getUsertype());            	
					userTemp.setAG((long) ag);
	            	userTemp.setVIP((short)getVip(userTemp.getUsertype()));
	            	ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.DUMMY_FAST);
	            	listBotDummyThai.add(userTemp);
	            }
	            rs.close();
	            cs.close();
	            System.out.println("==>Size listBotDummyThai:" + listBotDummyThai.size()) ;
	        } catch (SQLException ex) {
	            System.out.println("==>Error==>GetListBot:" + ex.getMessage()) ;
	        }finally{
	            instance.releaseDbConnection(conn);
	        }
			
		}
		
		@Override
		public Long UpdateBotMarkChessByName(int uid, long mark) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void BotCreateTable(int gameid, int mark, int type) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public UserInfo botLogin(int gameid) {
			synchronized (listBotDummyThai) {
				for (int i = 0; i < listBotDummyThai.size(); i++) {
					if (listBotDummyThai.get(i).getGameid() == 0) {
						listBotDummyThai.get(i).setGameid((short) gameid);
						return listBotDummyThai.get(i);
					}
				}
			}
			return null;
		}
		@Override
		public void processBotDisconnect(int playerId) {
			try{
				for (int i = 0; i < listBotDummyThai.size(); i++) {
					if (listBotDummyThai.get(i).getPid() == playerId) {
						listBotDummyThai.get(i).setGameid((short) 0);
						break;
					}
				}
				if (dicBotDummyThai.containsKey(playerId)) {
					dicBotDummyThai.get(playerId).setDisconnect(true);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
			try{
				if(!dicBotDummyThai.containsKey(pid)) return null;
				dicBotDummyThai.get(pid).setRoomId((short) roomId);
				dicBotDummyThai.get(pid).setTableId(tableId);
				dicBotDummyThai.get(pid).setAS((long)mark);
				for (int i = 0; i < listBotDummyThai.size(); i++) {
					if (listBotDummyThai.get(i).getPid() == pid) {
						listBotDummyThai.remove(i);
						break;
					}
				}
				return dicBotDummyThai.get(pid);
			}catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		public Long UpdateBotMarkByName(int uid, int source, String name, long mark) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Long UpdateBotMarkByUID(int uid, long mark) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public String processGetBotInfoByPid(int pid, int tid) {
			try{
				synchronized (dicBotDummyThai) {
					if (dicBotDummyThai.containsKey(pid)) {
						if (tid > 0) {
							dicBotDummyThai.get(pid).setTableId(tid);
						}
						return ActionUtils.gson.toJson(dicBotDummyThai.get(pid).getUserGame());
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}
		@Override
		public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
			return dicBotDummyThai;
		}
		@Override
		public List<UserInfo> getListBot() {
			return listBotDummyThai;
		}
		    
}


