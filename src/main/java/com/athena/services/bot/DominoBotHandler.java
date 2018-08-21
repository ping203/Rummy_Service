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

public class DominoBotHandler implements Bot{
    // So luog bot
//  lsMarkCreateTable.add(new MarkCreateTable(50, 500, 0));
//  lsMarkCreateTable.add(new MarkCreateTable(100, 1000, 0));
//  lsMarkCreateTable.add(new MarkCreateTable(500, 5000, 0));
//  lsMarkCreateTable.add(new MarkCreateTable(2000, 20000, 0));
//  lsMarkCreateTable.add(new MarkCreateTable(10000, 100000, 0));
//  lsMarkCreateTable.add(new MarkCreateTable(50000, 500000, 0));
	
//    public final int BOT_NUM_100 	= 500;
//    public final int BOT_NUM_500 	= 400;
//    public final int BOT_NUM_2000 	= 250;
//    public final int BOT_NUM_10000 	= 150;
//    public final int BOT_NUM_50000 	= 50;
    
    public final int BOT_NUM_100 	= 0;
    public final int BOT_NUM_500 	= 0;
    public final int BOT_NUM_2000 	= 0;
    public final int BOT_NUM_10000 	= 0;
    public final int BOT_NUM_50000 	= 0;
    
    public final int BOT_TYPE_50 	= 11;
    public final int BOT_TYPE_100 	= 12;
    public final int BOT_TYPE_500 	= 13;
    public final int BOT_TYPE_2000 	= 14;
    public final int BOT_TYPE_10000 	= 15;
    public final int BOT_TYPE_50000 	= 16;

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotDomino
            = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBotDomino = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;
    public Gson gson = new Gson();

    public final Logger logAgBot =  Logger.getLogger("AddAGBotDomino");
    public DominoBotHandler(int source) {
		getListBot(source);
	}

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

	public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        if (Diamond != 0)  
        	return;
       // System.out.println("==>DominoBotHandler==>getUserGame==>getBot: " + dicBotDomino.size() + " - " + minAG + " - " + gameId + " - " + tableId);
        synchronized (listBotDomino) {
            try {
                short type = getTypeByMark(minAG);
                //System.out.println("getUserGameBot:"+ listBotDomino.size()+"-tableid:"+ tableId +"-minag:"+minAG+"-type= "+type );
                for (int i = 0; i < listBotDomino.size(); i++) {
                    //System.out.println("Bot:"+listBotDomino.get(i).getPid()+"-name=" + listBotDomino.get(i).getUsername()+ "-roomid" + listBotDomino.get(i).getRoomId());
                    if (listBotDomino.get(i).getGameid() == gameId // client bot connected
                            && listBotDomino.get(i).getUsertype() == type) { // check type + ag

                        UserInfo u = listBotDomino.get(i);
//                        System.out.println("listBotDomino aaa -minAg" + lsMinAg.get((int) u.getUsertype()));
                        int addAG = 0;
                        if (u.getAG() < minAG*10 && u.getCPromot() < 5) {
                            addAG = getAGADD(type);
                            if(type > 13)
                            	u.setCPromot(u.getCPromot()+1);
                        }
//                        System.out.println("Bot Ag=" + u.getAG() +"-Agadd="+ addAG);
                        if (addAG > 0) {
                            AddLogAgBot(addAG, u);
                            u.setAG(u.getAG() + addAG);
                            UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(),
			                    	u.getUserid() - ServerDefined.userMap.get((int)u.getSource()), addAG);
                            QueueManager.getInstance(UserController.queuename).put(cmd);
                            Logger.getLogger("DominoBot_Add_Gold").info(ActionUtils.gson.toJson(u) + " - " + minAG + " - " + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.DOMINOQQ);
                        }
//                        System.out.println("Bot detected = 11 "  );
                        u.setIsOnline(gameId);
//                        System.out.println("Bot detected id= "+ u.getUserid()  );
                        if (!dicBotDomino.containsKey(u.getUserid())) dicBotDomino.put(u.getUserid(), u);
//                      System.out.println("Bot size "+ dicBotDomino.size()  +"-uid="+ u.getUserid() );
                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                        System.out.println("Bot detected =  " + u.getUserid());
                        serviceRouter.dispatchToGame(gameId, action);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private short getTypeByMark(int mark) {
        switch (mark) {
            case 50:
                return BOT_TYPE_50;
            case 100:
                return BOT_TYPE_100;
            case 500:
                return BOT_TYPE_500;
            case 2000:
                return BOT_TYPE_2000;
            case 10000:
                return BOT_TYPE_10000;
            case 50000:
                return BOT_TYPE_50000;
        }
        return BOT_TYPE_50;
    }
    public int getAGADD(short type) {
        int addAG = 0;
//        2,000	        20,000       100
//        10,000	100,000      500
//        20,000	500,000      1,000
//        100,000	1,000,000    5,000 
//        200,000	2,000,000    10,000
//        1,000,000	8,000,000    50,000
//        2,000,000	15,000,000   100,000
//        10,000,000	20,000,000   500,000
        switch (type) {
            case BOT_TYPE_50000:// 50,000
                addAG = randomBetween2Number(1000000, 8000000);
                break;

            case BOT_TYPE_10000:// 10,000
                addAG = randomBetween2Number(200000, 2000000);
                break;
            case BOT_TYPE_2000:// 2,000
                addAG = randomBetween2Number(50000, 200000);
                break;
            case BOT_TYPE_500:// 500
                addAG = randomBetween2Number(10000, 50000);
                break;

            case BOT_TYPE_100:// 100
                addAG = randomBetween2Number(2000, 20000);
                break;
            case BOT_TYPE_50://50
                addAG = randomBetween2Number(1000, 10000);
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
        int vip  = 4;
        switch (type) {
            case BOT_TYPE_50000:
            	vip = (new Random()).nextInt(4) + 2;
                break;

            case BOT_TYPE_10000:
            	vip = (new Random()).nextInt(3) + 2;
                break;
            case BOT_TYPE_2000:
            	vip = (new Random()).nextInt(3) + 2;
                break;
            case BOT_TYPE_500:
            	vip = (new Random()).nextInt(3) + 1;
                break;

            case BOT_TYPE_100:
            	vip = (new Random()).nextInt(3) + 1;
                break;
            case BOT_TYPE_50:
            	vip = (new Random()).nextInt(3);
                break;
            default:
                break;
        }
        return vip;
    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            synchronized (listBotDomino) { 
            if (isOnline == 0) {
   
                //System.out.println("==>BotHandler==>updateBotOnline:before " + ServiceImpl.listBot.size());
                if (dicBotDomino.containsKey(pid) && dicBotDomino.get(pid).getIsOnline() > 0) {
                    dicBotDomino.get(pid).setIsOnline(isOnline);
                    dicBotDomino.get(pid).setTableId(0);
                    if(dicBotDomino.get(pid).isDisconnect()) dicBotDomino.get(pid).setGameid((short)0);
                    dicBotDomino.get(pid).setRoomId((short)0);
                    UserInfo u = dicBotDomino.get(pid);
                    listBotDomino.add(u);
                }
            } else {
                dicBotDomino.get(pid).setIsOnline(isOnline);
            }
                            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dicBotDomino.get(pid);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark) {
        synchronized (dicBotDomino) {
//             System.out.println("UpdateBotMarkChessByName:" + uid+"-"+name+"-"+mark);
            if (dicBotDomino.containsKey(uid)) {
//                System.out.println("ag:" +dicBotDomino.get(uid).getAG());
                try {
                    long ag = dicBotDomino.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBotDomino.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid- ServerDefined.userMap.get(source), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBotDomino.get(uid).getAG();
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
            //System.out.println("==>BotHandler==>BotCreateTable "+ gameid+" - " + mark);
            short type = getTypeByMark(mark);
            for (int i = 0; i < listBotDomino.size(); i++) {
//              System.out.println("Room="+ listBotDomino.get(i).getRoomId() +"-type="+listBotDomino.get(i).getUsertype());
                if (listBotDomino.get(i).getGameid() == gameid && listBotDomino.get(i).getUsertype() == type && listBotDomino.get(i).getRoomId() == 0) {                
                    UserInfo u = listBotDomino.get(i);
                   // System.out.println("==>BotHandler==>BotCreateTable : " + u.getAG() +"-"+ mark *10);
                    if (u.getAG() < mark *10 && u.getCPromot()< 5) { // kiem tra them tien cho bot
//                        u.setCPromot(9);
                        int addAG = getAGADD(type);
                        if(type > 13)
                        	u.setCPromot(u.getCPromot()+1);
//                        System.out.println("AG ADd = " + addAG);
                        if (addAG > 0) {
                            u.setAG(u.getAG() + addAG);
                            UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(),
			                    	u.getUserid() - ServerDefined.userMap.get((int)u.getSource()), addAG);
                            QueueManager.getInstance(UserController.queuename).put(cmd);
                            Logger.getLogger("Bot_Details").info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.DOMINOQQ);
                        }
                    } else if (u.getAG() < mark) {
                        continue; // tim bot tiep
                    }
                    listBotDomino.get(i).setRoomId((short) ((new Random()).nextInt(2) + 1));
                    u.setIsOnline((short) gameid);
                    dicBotDomino.put(u.getUserid(), u);
                    //System.out.println("==>BotHandler==>BotCreateTable : OK -mark="+ mark+ "-pid="+ u.getPid());
                    System.out.println("Bot createtable "+ u.getPid());
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "botCreateTable");
                    send.addProperty("pid", u.getPid());
                    send.addProperty("M", mark);
                    ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                    serviceRouter.dispatchToGameActivator(gameid, request);
                    break;
                }
            }
        } catch (Exception e) {
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
        }
    }

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {
            int s = 0, t11 = 0, t12 = 0, t21 = 0, t22 = 0, t23 = 0, t31 = 0, t32 = 0, t33 = 0;
            List<String> lsBot = new ArrayList<String>();
            for (int i = 0; i < listBotDomino.size(); i++) {
                if (listBotDomino.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotDomino.get(i).getUsertype() > 32) {
                    t33++;
                } else if (listBotDomino.get(i).getUsertype() > 31) {
                    t32++;
                } else if (listBotDomino.get(i).getUsertype() > 30) {
                    t31++;
                } else if (listBotDomino.get(i).getUsertype() > 22) {
                    t23++;
                } else if (listBotDomino.get(i).getUsertype() > 21) {
                    t22++;
                } else if (listBotDomino.get(i).getUsertype() > 20) {
                    t21++;
                } else if (listBotDomino.get(i).getUsertype() > 11) {
                    t12++;
                } else if (listBotDomino.get(i).getUsertype() > 10) {
                    t11++;
                }
                if (je.has("T") && je.get("T").getAsInt() == listBotDomino.get(i).getUsertype()) {
                    lsBot.add(listBotDomino.get(i).getUsername());
                }
            }
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "checkBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", "Size: " + listBotDomino.size() + " - " + t33 + "/" + t32 + "/" + t31 + "/" + t23 + "/"
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
            listBotDomino.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBotDomino.size(); i++) {
                if (listBotDomino.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotDomino.get(i).getIsOnline() > 0) {
                    t++;
                }
            }
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBotDomino.size());
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getListBot(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid",GAMEID.DOMINOQQ);
            ResultSet rs = cs.executeQuery();
            short type = 0;
            listBotDomino.clear();
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
                type++;
	            	
                if (type <= BOT_NUM_50000 ) {
                    userTemp.setUsertype((short) BOT_TYPE_50000);
                } else if (type <= BOT_NUM_10000) {
                    userTemp.setUsertype((short) BOT_TYPE_10000);
                } else if (type <= BOT_NUM_2000) {
                    userTemp.setUsertype((short) BOT_TYPE_2000);
                } else if (type <= BOT_NUM_500 ) {
                    userTemp.setUsertype((short) BOT_TYPE_500);
                } else if (type <= BOT_NUM_100) {
                    userTemp.setUsertype((short) BOT_TYPE_100);
                } else {
                    userTemp.setUsertype((short) BOT_TYPE_50);
                }
                // kiem tra them tien cho bot
            	int agDB = rs.getInt("AG");	            		            	
            	int ag = getAGADD(userTemp.getUsertype());            	
				userTemp.setAG((long) ag);
            	userTemp.setVIP((short)getVip(userTemp.getUsertype()));
                UserInfoCmd cmdUpdate = new UserInfoCmd( userTemp.getSource(),userTemp.getUserid()- ServerDefined.userMap.get(source),userTemp.getVIP(),"GameUpdateBot",ag-agDB);
                QueueManager.getInstance(UserController.queuename).put(cmdUpdate);
                ActionUtils.BotLogIFRS(userTemp.getUserid(), ag, ag-agDB, GAMEID.DOMINOQQ);    
                listBotDomino.add(userTemp);
            }
            rs.close();
            cs.close();

            System.out.println("==>Size listBotDomino:" +listBotDomino.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }

    }
     private void AddLogAgBot(int AG,UserInfo bot){
        //System.out.println("AddAG Bot:"+ bot.getPid()+"-AG:"+ AG);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UserInfo botLogin(int gameid) {
		synchronized (listBotDomino) {
			for (int i = 0; i < listBotDomino.size(); i++) {
				if (listBotDomino.get(i).getGameid() == 0) {
					listBotDomino.get(i).setGameid((short) gameid);
					return listBotDomino.get(i);
				}
			}
		}
		return null;
	}

	@Override
	public void processBotDisconnect(int playerId) {
		try{
			for (int i = 0; i < listBotDomino.size(); i++) {
				if (listBotDomino.get(i).getPid() == playerId) {
					listBotDomino.get(i).setGameid((short) 0);
					break;
				}
			}
			if (dicBotDomino.containsKey(playerId)) {
				dicBotDomino.get(playerId).setDisconnect(true);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
		try{
			if(!dicBotDomino.containsKey(pid)) return null;
			dicBotDomino.get(pid).setRoomId((short) roomId);
			dicBotDomino.get(pid).setTableId(tableId);
			dicBotDomino.get(pid).setAS((long) mark);
			for (int i = 0; i < listBotDomino.size(); i++) {
				if (listBotDomino.get(i).getPid() == pid) {
					listBotDomino.remove(i);
					break;
				}
			}
			return dicBotDomino.get(pid);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Long UpdateBotMarkByName(int uid, int source, String name, long mark) {
		return UpdateBotMarkChessByName(uid,source,name,mark);
	}

	@Override
	public Long UpdateBotMarkByUID(int uid, long mark) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String processGetBotInfoByPid(int pid, int tid) {
		try{
			synchronized (dicBotDomino) {
				if (dicBotDomino.containsKey(pid)) {
					if (tid > 0) {
						dicBotDomino.get(pid).setTableId(tid);
					}
					return ActionUtils.gson.toJson(dicBotDomino.get(pid).getUserGame());
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
		return dicBotDomino;
	}

	@Override
	public List<UserInfo> getListBot() {
		return listBotDomino;
	}

}
