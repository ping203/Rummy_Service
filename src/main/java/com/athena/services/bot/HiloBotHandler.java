package com.athena.services.bot;

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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;

public class HiloBotHandler implements Bot{

    // So luog bot
    public final int BOT1000 = 30;
    public final int BOT10000 = 20;
    public final int BOT100000 = 10;
    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotHilo
            = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBotHilo = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;
    public Gson gson = new Gson();
    public final ConcurrentLinkedHashMap<Integer, Integer> lsMinAg = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 8);
    public final Logger logAgBot =  Logger.getLogger("AddAGBotHilo");
    public HiloBotHandler(int source) {
		getListBot(source);
	}

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

	public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
//        if (minAG > 10) {
//            return;
//        }
        if (Diamond != 0) {
            return;
        }
//        ==>HiloBotHandler==>getUserGame==>getBot 0 - 100 - 8027 - 1

//        System.out.println("==>HiloBotHandler==>getUserGame==>getBot: " + dicBotHilo.size() + " - " + minAG + " - " + gameId + " - " + tableId);
        synchronized (listBotHilo) {
            try {
                short type = getTypeByMark(minAG);

                //boolean check = true;
                //Duyá»‡t tÃ¬m boss sáºµn sÃ ng Ä‘á»§ tiá»�n chÆ¡i
                for (int i = 0; i < listBotHilo.size(); i++) {
//                    System.out.println("Bot type=" + listBotHilo.get(i).getUsertype() + "-type" + type);
                    if (listBotHilo.get(i).getGameid() == gameId // client bot connected
                            && listBotHilo.get(i).getUsertype() == type) { // check type + ag

                        UserInfo u = listBotHilo.get(i);
//                        System.out.println("listBotHilo aaa -minAg" + lsMinAg.get((int) u.getUsertype()));
                        int addAG = 0;
                        if (u.getAG() < lsMinAg.get((int) u.getUsertype()) && u.getCPromot() <getCountPromt(type)) {
                            if (type > 12) {
                                //u.setCPromot(9);
                            }
                            addAG = getAGADD(type);
                        }
//                        System.out.println("Bot Ag=" + u.getAG() +"-Agadd="+ addAG);
                        if (addAG > 0) {
                            AddLogAgBot(addAG, u);
                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);
                            ActionUtils.updateAGBOT(u,  u.getAG().intValue(),abDB, GAMEID.HILO);
                            Logger.getLogger("HiloBot_Add_Gold").info(ActionUtils.gson.toJson(u) + " - " + minAG + " - " + addAG);

                        }
//                        System.out.println("Bot detected = 11 "  );
                        u.setIsOnline(gameId);
//                        System.out.println("Bot detected id= "+ u.getUserid()  );
                        dicBotHilo.put(u.getUserid(), u);
//                      System.out.println("Bot size "+ dicBotHilo.size()  +"-uid="+ u.getUserid() );
                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
//                        System.out.println("Bot detected =  " + u.getAG());
                        serviceRouter.dispatchToGame(gameId, action);

                        //check = false;

                        break;
                    }
                }
                //if (check) {
                    //Logger.getLogger("Bot_Details").info("getUserGame failed: minAG:" + minAG + " - List:" + listBotHilo.size());
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private short getTypeByMark(int mark) {
        switch (mark) {
            case 100:
                return 11;
            case 1000:
                return 21;
            case 10000:
                return 31;
            case 100000:
                return 41;
        }
        return 11;
    }

    public int getAGADD(short type) {
        int addAG = 0;

        switch (type) {
            case 41:// 100,000
                addAG = randomBetween2Number(1500000, 5000000);
                break;
            case 31:// 10,000
                addAG = randomBetween2Number(200000, 1000000);
                break;
            case 21:// 1,000
                addAG = randomBetween2Number(30000, 100000);
                break;
            case 11://100
                addAG = randomBetween2Number(5000, 20000);
                break;
            default:
                break;
        }
        return addAG;
    }

    public int randomBetween2Number(int lowerBound, int upperBound) {
        Random random = new Random();
        int randomNumber = random.nextInt(upperBound - lowerBound) + lowerBound;
        return randomNumber;
    }

    public int getVip(short type) {
        int addAG = 0;
        switch (type) {
            case 41:
                addAG = (new Random()).nextInt(5) + 4;
                break;
            case 31:
                addAG = (new Random()).nextInt(4) + 3;
                break;
            case 21:
                addAG = (new Random()).nextInt(4) + 2;
                break;
            case 11:
                addAG = (new Random()).nextInt(3) + 1;
                break;

            default:
                break;
        }
        return addAG;
    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            synchronized (listBotHilo) { 
            if (isOnline == 0) {
   
                //System.out.println("==>BotHandler==>updateBotOnline:before " + ServiceImpl.listBot.size());
                if (dicBotHilo.containsKey(pid) && dicBotHilo.get(pid).getIsOnline() > 0) {
                    dicBotHilo.get(pid).setIsOnline(isOnline);
                    dicBotHilo.get(pid).setTableId(0);
                    if(dicBotHilo.get(pid).isDisconnect()) dicBotHilo.get(pid).setGameid((short)0);
                    dicBotHilo.get(pid).setRoomId((short)0);
                    UserInfo u = dicBotHilo.get(pid);
                    listBotHilo.add(u);
                }
            } else {
                dicBotHilo.get(pid).setIsOnline(isOnline);
            }
                            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dicBotHilo.get(pid);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark) {
        synchronized (dicBotHilo) {
//             System.out.println("UpdateBotMarkChessByName:" + uid+"-"+name+"-"+mark);
            if (dicBotHilo.containsKey(uid)) {
//                System.out.println("ag:" +dicBotHilo.get(uid).getAG());
                try {
                    long ag = dicBotHilo.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBotHilo.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid- ServerDefined.userMap.get(source), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBotHilo.get(uid).getAG();
                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("==>Error==>botHandler==>UpdateBotMarkChessByName:" + e.getMessage() + "-" + uid+"-" + name + "-" + mark);
                    return 0l;
                }
            } else {
                Logger.getLogger("Bot_Details").info("UpdateBotMarkChessByName: !dicBotNew.containsKey(name)" + uid + "-" + name + mark);
            }
        }
        return 0l;
    }

    
    public void BotCreateTable(int gameid, int mark) {
        try {
            //System.out.println("==>HiloBotHandler==>BotCreateTable "+ gameid+" - " + mark);
            short type = getTypeByMark(mark);
//            if (mark == 50000) {
//                type = 31;
//            } else if (mark == 100000) {
//                type = 32;
//            } else if (mark == 500000) {
//                type = 33;
//            }
//            System.out.println("==>HiloHandler==>BotCreateTable : " + type +"-gameid"+ gameid);
            //boolean check = true;
            for (int i = 0; i < listBotHilo.size(); i++) {
                //System.out.println("Room"+ listBotHilo.get(i).getRoomId());
                if (listBotHilo.get(i).getGameid() == gameid && listBotHilo.get(i).getUsertype() == type && listBotHilo.get(i).getRoomId() == 0) {
                    //check = false;                   
                    UserInfo u = listBotHilo.get(i);
//                    System.out.println("==>BotHandler==>BotCreateTable : " + u.getAG() +"-"+ mark *10);
                    if (u.getAG() < mark *10 && u.getCPromot()< getCountPromt(type)) { // kiem tra them tien cho bot
//                        u.setCPromot(9);
                        int addAG = getAGADD(type);
//                        System.out.println("AG ADd = " + addAG);
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(),abDB, GAMEID.HILO);
                        }
                    } else if (u.getAG() < mark) {
                        continue; // tim bot tiep
                    }
                    listBotHilo.get(i).setRoomId((short) 1);
                    u.setIsOnline((short) gameid);
                    dicBotHilo.put(u.getUserid(), u);
                    //System.out.println("==>BotHandler==>BotCreateTable : OK -mark="+ mark+ "-pid="+ u.getPid()+" - "+gameid);

                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "botCreateTable");
                    send.addProperty("pid", u.getPid());
                    send.addProperty("M", mark);
                    ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                    serviceRouter.dispatchToGameActivator(gameid, request);
                    break;
                }
            }
//            if (check) {
//                for (int i = 0; i < listBotHilo.size(); i++) {
//                    if (listBotHilo.get(i).getUsertype() == type) {
////                        Logger.getLogger("Bot_Details").info("==>BotHandler==>HadBotCreateTable: failed listBotNew size: " + listBotHilo.size()
////                                + " - gameid: " + gameid + " - mark: " + mark);
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int getCountPromt(int type){
        switch(type){
            case 11: case 12: case 21:
                return 9;
            case  22: case 23:
                return 7;
            case 31: case 32: case 33:
                return 5;
        }
        return 9;
    }
    public ServiceRouter getServiceRouter() {
        return serviceRouter;
    }
    @Override
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
//            System.out.println("==>Error==>notifyBot");
            //logger_.error(e.getMessage() + "-" + username);
        }
    }

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {
            int s = 0, t11 = 0, t12 = 0, t21 = 0, t22 = 0, t23 = 0, t31 = 0, t32 = 0, t33 = 0;
            List<String> lsBot = new ArrayList<String>();
            for (int i = 0; i < listBotHilo.size(); i++) {
                if (listBotHilo.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotHilo.get(i).getUsertype() > 32) {
                    t33++;
                } else if (listBotHilo.get(i).getUsertype() > 31) {
                    t32++;
                } else if (listBotHilo.get(i).getUsertype() > 30) {
                    t31++;
                } else if (listBotHilo.get(i).getUsertype() > 22) {
                    t23++;
                } else if (listBotHilo.get(i).getUsertype() > 21) {
                    t22++;
                } else if (listBotHilo.get(i).getUsertype() > 20) {
                    t21++;
                } else if (listBotHilo.get(i).getUsertype() > 11) {
                    t12++;
                } else if (listBotHilo.get(i).getUsertype() > 10) {
                    t11++;
                }
                if (je.has("T") && je.get("T").getAsInt() == listBotHilo.get(i).getUsertype()) {
                    lsBot.add(listBotHilo.get(i).getUsername());
                }
            }
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "checkBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", "Size: " + listBotHilo.size() + " - " + t33 + "/" + t32 + "/" + t31 + "/" + t23 + "/"
                    + t22 + "/" + t21 + "/" + t12 + "/" + t11);
            jo.addProperty("Bot", ActionUtils.gson.toJson(lsBot));
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadBot(JsonObject je, UserInfo actionUser) {
        try {
            listBotHilo.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBotHilo.size(); i++) {
                if (listBotHilo.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotHilo.get(i).getIsOnline() > 0) {
                    t++;
                }
            }

            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBotHilo.size());
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getListBot(int source) {
        lsMinAg.put(11, 100);
        lsMinAg.put(21, 21000);
        lsMinAg.put(31, 100000);
        lsMinAg.put(41, 1000000);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid",GAMEID.HILO);
            ResultSet rs = cs.executeQuery();
            short type = 0;
            listBotHilo.clear();
            while (rs.next()) {
                UserInfo userTemp = new UserInfo();
                userTemp.setUserid(rs.getInt("ID") + ServerDefined.userMap.get(source));
                userTemp.setFacebookid(Long.parseLong(rs.getString("FacebookId")));
                userTemp.setDeviceId(rs.getString("DeviceID"));
                userTemp.setIsOnline((short) rs.getInt("IsOnline"));
                userTemp.setBanned(rs.getBoolean("IsBanned"));
                userTemp.setAvatar((short) rs.getInt("Avatar"));
                userTemp.setOnlineDaily((short) rs.getInt("OnlineDaily"));
                userTemp.setPromotionDaily(rs.getInt("PromotionDaily"));
                userTemp.setCreateTime(rs.getDate("CreateTime").getTime());
                userTemp.setLastLogin(rs.getDate("LastLogin").getTime());
                userTemp.setUsername(rs.getString("Username"));
                userTemp.setUsernameLQ(rs.getString("UsernameLQ"));
                userTemp.setIdolName(rs.getString("IdolName"));
                userTemp.setSource((short) source);
                if (userTemp.getUsernameLQ().length() > 0) {
                    userTemp.setUsername(userTemp.getUsernameLQ());
                }
                userTemp.setAG(rs.getLong("AG")); 
                listBotHilo.add(userTemp);
            }
            rs.close();
            cs.close();
            int numbot100000 = listBotHilo.size()*BOT100000/100;
            int numbot10000 = numbot100000+listBotHilo.size()*BOT10000/100;
            int numbot1000 = numbot10000 + listBotHilo.size()*BOT1000/100;
            for(UserInfo userTemp : listBotHilo){
            	type++;            	
	            if (type <= numbot100000) {
	                userTemp.setUsertype((short) 41);
	            } else if (type <= numbot10000) {
	                userTemp.setUsertype((short) 31);
	            } else if (type <= numbot1000) {
	                userTemp.setUsertype((short) 21);
	            } else {
	                userTemp.setUsertype((short) 11);
	            }
	            // kiem tra them tien cho bot
	        	int agDB = userTemp.getAG().intValue();            		            	
	        	int ag = getAGADD(userTemp.getUsertype());            	
				userTemp.setAG((long) ag);
	        	userTemp.setVIP((short)getVip(userTemp.getUsertype()));
	            UserInfoCmd cmdUpdate = new UserInfoCmd( userTemp.getSource(),userTemp.getUserid()- ServerDefined.userMap.get(source),userTemp.getVIP(),"GameUpdateBot",ag-agDB);
	            QueueManager.getInstance(UserController.queuename).put(cmdUpdate);
	            ActionUtils.BotLogIFRS(userTemp.getUserid(), ag, ag-agDB, GAMEID.HILO);   
            }
            System.out.println("==>Size listBotHilo:" + listBotHilo.size()+" - "+numbot1000+" - "+numbot10000+" - "+numbot100000);
        } catch (Exception ex) {
//            System.out.println("==>Error==>GetListBot:" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
     private void AddLogAgBot(int AG,UserInfo bot){
        JsonObject job =  new JsonObject();
        job.addProperty("uid", bot.getPid());
        job.addProperty("n", bot.getUsername());
        job.addProperty("ag", AG);
        logAgBot.info(new Gson().toJson(job));
    }

	@Override
	public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long UpdateBotMarkChessByName(int uid, long mark) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void BotCreateTable(int gameid, int mark, int type) {
		//System.out.println("==>HiloBotHandler==>BotCreateTable: "+gameid+" - mark "+mark+" - type "+type);
		BotCreateTable(gameid, mark);
	}

	@Override
	public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UserInfo botLogin(int gameid) {
		synchronized (listBotHilo) {
			for (int i = 0; i < listBotHilo.size(); i++) {
				if (listBotHilo.get(i).getGameid() == 0) {
					listBotHilo.get(i).setGameid((short) gameid);
					return listBotHilo.get(i);
				}
			}
		}
		return null;
	}

	@Override
	public void processBotDisconnect(int playerId) {
		try{
			for (int i = 0; i < listBotHilo.size(); i++) {
				if (listBotHilo.get(i).getPid() == playerId) {
					listBotHilo.get(i).setGameid((short) 0);
					break;
				}
			}
			if (dicBotHilo.containsKey(playerId)) {
				dicBotHilo.get(playerId).setDisconnect(true);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
		try{
			//System.out.println("==>HiloBotHandler==>processConfirmRoom: "+pid+" - roomid: "+roomId+" - tableId: "+tableId+" - mark: "+mark+" - "+dicBotHilo.containsKey(pid));
			if(!dicBotHilo.containsKey(pid)) return null;
			dicBotHilo.get(pid).setRoomId((short) roomId);
			dicBotHilo.get(pid).setTableId(tableId);
			dicBotHilo.get(pid).setAS((long) mark);
			for (int i = 0; i < listBotHilo.size(); i++) {
				if (listBotHilo.get(i).getPid() == pid) {
					listBotHilo.remove(i);
					break;
				}
			}
			return dicBotHilo.get(pid);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Long UpdateBotMarkByName(int uid, int source, String name, long mark) {
		return UpdateBotMarkChessByName(uid, source, name, mark);
	}

	@Override
	public Long UpdateBotMarkByUID(int uid, long mark) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String processGetBotInfoByPid(int pid, int tid) {
		try{
			synchronized (dicBotHilo) {
				if (dicBotHilo.containsKey(pid)) {
					if (tid > 0) {
						dicBotHilo.get(pid).setTableId(tid);
					}
					return ActionUtils.gson.toJson(dicBotHilo.get(pid).getUserGame());
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
		return dicBotHilo;
	}

	@Override
	public List<UserInfo> getListBot() {
		return listBotHilo;
	}

}
