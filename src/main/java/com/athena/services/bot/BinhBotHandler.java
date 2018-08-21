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
import com.athena.log.LoggerKey;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.GAMEID;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.ServerSource;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;

public class BinhBotHandler implements Bot {

    public static Logger logger = Logger.getLogger(LoggerKey.DETAILS_BOT);

    public static final int Binh_Mark100 = 100;
    public static final int Binh_Mark500 = 500;

    public static final int Binh_Mark1K = 1000;
    public static final int Binh_Mark5K = 5000;
    public static final int Binh_Mark10K = 10000;

    public static final int Binh_Mark50K = 50000;
    public static final int Binh_Mark100K = 100000;
    public static final int Binh_Mark500K = 500000;
    public static final int Binh_Mark1M = 1000000;
    public static final int Binh_Mark5M = 5000000;
    public static final int Binh_Max_Add_Gold = 100;

    public final int BOT_100 = 11;
    public final int BOT_200 = 12;
    public final int BOT_500 = 13;

    public final int BOT_1K = 20;
    public final int BOT_5K = 21;
    public final int BOT_10K = 22;

    public final int BOT_50K = 30;
    public final int BOT_100K = 31;
    public final int BOT_500K = 32;

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotNew
            = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBotNew = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;

    public BinhBotHandler(int source) {
        getListBot(source);
    }

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        if (Diamond != 0) {
            return;
        }
        if (minAG > Binh_Mark50K && !ServiceImpl.IsRunIndo) {
            return;
        }
        //System.out.println("==>getUserGame==>getBot: "+listBotNew.size());
        synchronized (listBotNew) {
            try {
                short type = BOT_100;
                if (minAG >= Binh_Mark500K) {
                    type = BOT_500K;
                } else if (minAG >= Binh_Mark100K) {
                    type = BOT_100K;
                } else if (minAG >= Binh_Mark50K) {
                    type = BOT_50K;
                } else if (minAG >= Binh_Mark10K) {
                    type = BOT_10K;
                } else if (minAG >= Binh_Mark5K) {
                    type = BOT_5K;
                } else if (minAG >= Binh_Mark1K) {
                    type = BOT_1K;
                } else if (minAG >= Binh_Mark500) {
                    type = BOT_500;
                } else if (minAG >= 200) {
                    type = BOT_200;
                }
                //boolean check = true;  
                //Duyệt tìm boss sẵn sàng đủ tiền chơi
                for (int i = 0; i < listBotNew.size(); i++) {
                    //System.out.println("==>BotHandler==>getUserGame: Gameid(): "+ listBotNew.get(i).getGameid()+" - "
                    //+listBotNew.get(i).getUsertype()+" - "+ type+" - isOl: "+listBotNew.get(i).getIsOnline()+" - "+listBotNew.get(i).getUsername());
                    if (listBotNew.get(i).getGameid() == gameId && listBotNew.get(i).getTableId() == 0
                            && listBotNew.get(i).getUsertype() == type && listBotNew.get(i).getIsOnline() == 0) { // check type + ag
                        UserInfo u = listBotNew.get(i);
                        int addAG = 0;
                        //System.out.println("\n ==>BotHandler==>BotCreateTable GETTTTTT : "
                        //			+ActionUtils.gson.toJson(listBotNew.get(i))+" \nBOTGETTTTT\n");
                        if (u.getAG() < minAG * 10 && u.getCPromot().intValue() < Binh_Max_Add_Gold) { // kiem tra them tien cho bot
//		    					Mức cược bàn	Nhóm 	Số bot tiếp khách	MIN	MAX	Note	
//		    					100		1	60	5,000	20,000	thả	
//		    					500		1	50	25,000	100,000	thả	
//		    					1,000	2	50	50,000	150,000		
//		    					5,000	2	40	250,000	500,000		
//		    					10,000	2	30	500,000	2,000,000		
//		    					50,000	3	25	2,500,000	5,000,000		
//		    					100,000	3	20	5,000,000	20,000,000		
//		    					500,000	3	15	25,000,000	100,000,000	
                            //if(type > 23 )u.setCPromot(9);
                            addAG = getAGADD(type);
                        } else if (u.getAG() < minAG * 10) {
                            continue;
                        }
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.BINH);
                        }

                        u.setIsOnline(gameId);
                        synchronized (dicBotNew) {
                            dicBotNew.put(u.getUserid(), u);
                        }
                        //System.out.println("==>BotHandler==>getUserGame: "+ActionUtils.gson.toJson(u));
                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                        getServiceRouter().dispatchToGame(gameId, action);
                        //check = false;
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int randomBetween2Number(int lowerBound, int upperBound) {
        Random random = new Random();
        int randomNumber = random.nextInt(upperBound - lowerBound) + lowerBound;
        return randomNumber;
    }

    public int getAGADD(short type) {
        int addAG = 0;
        switch (type) {
            case BOT_500K:
                addAG = randomBetween2Number(5000000, 30000000);
                break;
            case BOT_100K:
                addAG = randomBetween2Number(2000000, 10000000);
                break;
            case BOT_50K:
                addAG = randomBetween2Number(700000, 3000000);
                break;

            case BOT_10K:
                addAG = randomBetween2Number(300000, 1000000);
                break;
            case BOT_5K:
                addAG = randomBetween2Number(200000, 500000);
                break;
            case BOT_1K:
                addAG = randomBetween2Number(30000, 100000);
                break;

            case BOT_500:
                addAG = randomBetween2Number(20000, 50000);
                break;
            case BOT_200:
                addAG = randomBetween2Number(20000, 100000);
                break;
            case BOT_100:
                addAG = randomBetween2Number(5000, 10000);
                break;
            default:
                break;
        }
        return addAG;
    }

    public int getVip(short type) {
        int addVIP = 0;
        switch (type) {
            case BOT_500K:
                addVIP = (new Random()).nextInt(5) + 4;
                break;
            case BOT_100K:
                addVIP = (new Random()).nextInt(4) + 3;
                break;
            case BOT_50K:
                addVIP = (new Random()).nextInt(3) + 2;
                break;

            case BOT_10K:
                addVIP = (new Random()).nextInt(3) + 2;
                break;
            case BOT_5K:
                addVIP = (new Random()).nextInt(3) + 2;
                break;
            case BOT_1K:
                addVIP = (new Random()).nextInt(3) + 1;
                break;

            case BOT_500:
                addVIP = (new Random()).nextInt(3) + 1;
                break;
            case BOT_200:
                addVIP = (new Random()).nextInt(3) + 1;
                break;
            case BOT_100:
                addVIP = (new Random()).nextInt(2) + 1;
                break;
            default:
                break;
        }
        return addVIP;
    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            //System.out.println("==>BinhBotHandler==>updateBotOnline: "+pid+" - Online: "+isOnline);
            synchronized (dicBotNew) {
                if (isOnline == 0) {
                    //synchronized (listBotNew) {    
                    //System.out.println("==>BotHandler==>updateBotOnline:before " + ServiceImpl.listBot.size());
                    if (dicBotNew.containsKey(pid) && dicBotNew.get(pid).getIsOnline() > 0) {
                        //System.out.println("==>BotHandler==>updateBotOnline: "+dicBotNew.get(pid).getIsOnline());
                        dicBotNew.get(pid).setIsOnline(isOnline);
                        dicBotNew.get(pid).setTableId(0);
                        dicBotNew.get(pid).setRoomId((short) 0);
                        if (dicBotNew.get(pid).isDisconnect()) {
                            dicBotNew.get(pid).setGameid((short) 0);
                        }
                        UserInfo u = dicBotNew.get(pid);
                        listBotNew.add(u);
                    }
                } else {
                    dicBotNew.get(pid).setIsOnline(isOnline);
                }
            }
            //System.out.println("==>BinhBotHandler==>updateBotOnline: "+pid+" - Online: "+isOnline);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dicBotNew.get(pid);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU) {
        synchronized (dicBotNew) {
            if (dicBotNew.containsKey(uid)) {
                // System.out.println("UpdateBotMarkChessByName: dicBotNew.containsKey(name)" + uid+"-"+
                //		 	name+"-"+mark+"-"+dicBotNew.get(uid).getAG());
                try {
                    long ag = dicBotNew.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBotNew.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid - ServerDefined.userMap.get(source), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBotNew.get(uid).getAG();
                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("==>Error==>botHandler==>UpdateBotMarkChessByName:" + e.getMessage() + "-" + uid+"-" + name + "-" + mark);
                    return 0l;
                }
            } else {
                logger.info("UpdateBotMarkChessByName: !dicBotNew.containsKey(name)" + uid + "-" + name + mark);
            }
        }
        return 0l;
    }

    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        //Auto-generated method stub
        try {
            //System.out.println((new Date())+"==>BotHandler==>BotCreateTable: gameid: "+ gameid+" - mark: " + mark+" - roomID: "+roomID);
            // if(mark >= 50000 && !ServiceImpl.IsRunIndo) return;
            short type = BOT_100; // bot bàn 100
            if (mark >= Binh_Mark500K) // bàn 500k
            {
                type = BOT_500K;
            } else if (mark >= Binh_Mark100K) { // 100k
                type = BOT_100K;
            } else if (mark >= Binh_Mark50K) { // bàn  50k
                type = BOT_50K;
            } else if (mark >= Binh_Mark10K) { // bàn  10k
                type = BOT_10K;
            } else if (mark >= Binh_Mark5K) { // bàn  5k
                type = BOT_5K;
            } else if (mark >= Binh_Mark1K) { // bàn  1k
                type = BOT_1K;
            } else if (mark >= Binh_Mark500) { // bàn  500
                type = BOT_500;
            }
            //System.out.println("==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotNew));
            //boolean check = true;
            for (int i = 0; i < listBotNew.size(); i++) {
                //if(i < 10)
                //System.out.println("\n roomid - "+listBotNew.get(i).getRoomId()+" - gameid: "+listBotNew.get(i).getGameid()+" - type: "+listBotNew.get(i).getUsertype());
                //if(listBotNew.get(i).getGameid() == gameid)
                if (listBotNew.get(i).getGameid() == gameid && listBotNew.get(i).getUsertype() == type && listBotNew.get(i).getRoomId() == 0
                        && listBotNew.get(i).getIsOnline() == 0 && listBotNew.get(i).getTableId() == 0) {
                    //System.out.println("==>BotHandler==>BotCreateTable : listBotNew.get(i).getRoomId(): "+listBotNew.get(i).getRoomId());
                    //check = false;

                    UserInfo u = listBotNew.get(i);

                    //System.out.println("==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotNew.get(i))+"\n");
                    if (u.getAG() < 10 * mark && u.getCPromot() < Binh_Max_Add_Gold) { // kiem tra them tien cho bot
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.BINH);
                        }
                    } else if (u.getAG() < 20 * mark) {
                        continue; // tim bot tiep
                    }
                    listBotNew.get(i).setRoomId((short) roomID);
                    u.setIsOnline((short) gameid);
                    //listBotNew.remove(i);
                    dicBotNew.put(u.getUserid(), u);
                    //System.out.println("==>BotHandler==>BotCreateTable : OK");

                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "botCreateTable");
                    send.addProperty("pid", u.getPid());
                    send.addProperty("M", mark);
                    ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                    getServiceRouter().dispatchToGameActivator(gameid, request);
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

    public void setServiceRouter(ServiceRouter serviceRouter) {
        this.serviceRouter = serviceRouter;
    }

    public void notifyBot() {
//		    	try {
//		    		
//		    		// 1. send notify
////		    	    var options = {
////		    	        method: 'POST',
////		    	        url: 'https://onesignal.com/api/v1/notifications',
////		    	        headers: {
////		    	            'Content-Type': 'application/json',
////		    	            'Authorization': 'Basic NGMzODk0ODQtMzE0Ni00N2Y5LWE3YmMtZDU1MjVkZDQ5ZTUz'
////		    	        },
////		    	        json: {
////		    	            app_id: "0a8074bb-c0e4-48cc-8def-edd22ce17b9d",
////		    	            headings: { "en": "DST Notify" },
////		    	            included_segments: ["All"],
////		    	            contents: { "en": msg },
////		    	            ios_badgeType: "Increase",
////		    	            ios_badgeCount: 1
////		    	        }
////		    	    };
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
//				} catch (Exception e) {
//					e.printStackTrace();
//					// handle exception
//					System.out.println("==>Error==>notifyBot");
//					//logger_.error(e.getMessage() + "-" + username);
//				}
    }

    public void reloadBot(JsonObject je, UserInfo actionUser, UserController userController) {
        try {
            listBotNew.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBotNew.size(); i++) {
                if (listBotNew.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotNew.get(i).getIsOnline() > 0) {
                    t++;
                }
            }

            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBotNew.size());
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {
            //BotHandler.notifyBot();
            //System.out.println(ActionUtils.gson.toJson(BotHandler.dicBotNew));
            //System.out.println(ActionUtils.gson.toJson(BotHandler.listBotNew));
            int s = 0, bot100 = 0, bot200 = 0, bot500 = 0, bot1k = 0, bot5k = 0, bot10k = 0, bot50k = 0, bot100k = 0, bot500k = 0;
            List<String> lsBot = new ArrayList<String>();
            for (int i = 0; i < listBotNew.size(); i++) {
                if (listBotNew.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotNew.get(i).getUsertype() > BOT_100K) {
                    bot500k++;
                } else if (listBotNew.get(i).getUsertype() > BOT_50K) {
                    bot100k++;
                } else if (listBotNew.get(i).getUsertype() > BOT_10K) {
                    bot50k++;
                } else if (listBotNew.get(i).getUsertype() > BOT_5K) {
                    bot10k++;
                } else if (listBotNew.get(i).getUsertype() > BOT_1K) {
                    bot5k++;
                } else if (listBotNew.get(i).getUsertype() > BOT_500) {
                    bot1k++;
                } else if (listBotNew.get(i).getUsertype() > BOT_200) {
                    bot500++;
                } else if (listBotNew.get(i).getUsertype() > BOT_100) {
                    bot200++;
                } else {
                    bot100++;
                }
                if (je.has("T") && je.get("T").getAsInt() == listBotNew.get(i).getUsertype()) {
                    lsBot.add(listBotNew.get(i).getUsername() + listBotNew.get(i).getAG().intValue());
                }
            }
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "checkBot");
            jo.addProperty("Disconnected", s + " / Size: " + listBotNew.size());
            jo.addProperty("Available", bot100 + bot200 + bot500 + bot1k + bot5k + bot10k + bot50k + bot100k + bot500k);
            jo.addProperty("bot100", bot100);
            jo.addProperty("bot200", bot200);
            jo.addProperty("bot500", bot500);

            jo.addProperty("bot1k", bot1k);
            jo.addProperty("bot5k", bot5k);
            jo.addProperty("bot10k", bot10k);

            jo.addProperty("bot50k", bot50k);
            jo.addProperty("bot100k", bot100k);
            jo.addProperty("bot500k", bot500k);

            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getListBot(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();

        // So luog bot
        int BOT200 = 550;
        int BOT500 = 450;
        int BOT1000 = 350;
        int BOT5000 = 250;
        int BOT10000 = 150;
        int BOT50000 = 80;
        int BOT100000 = 30;
        int BOT500000 = 0;

        if (source == ServerSource.IND_SOURCE) { //438
            BOT200 = 290;
            BOT500 = 230;
            BOT1000 = 180;
            BOT5000 = 130;
            BOT10000 = 90;
            BOT50000 = 60;
            BOT100000 = 35;
            BOT500000 = 15;
        }
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID.BINH);
            ResultSet rs = cs.executeQuery();
            short type = 0;
            listBotNew.clear();
            while (rs.next()) {
                UserInfo userTemp = new UserInfo();
//	            	System.out.println("==>Name:" + rs.getString("Username")) ;

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
                //userTemp.setSource((short)5);
                if (userTemp.getUsernameLQ().length() > 0) {
                    userTemp.setUsername(userTemp.getUsernameLQ());
                }

                type++;

                if (type <= BOT500000) {
                    userTemp.setUsertype((short) BOT_500K);
                } else if (type <= BOT100000) {
                    userTemp.setUsertype((short) BOT_100K);
                } else if (type <= BOT50000) {
                    userTemp.setUsertype((short) BOT_50K);
                } else if (type <= BOT10000) {
                    userTemp.setUsertype((short) BOT_10K);
                } else if (type <= BOT5000) {
                    userTemp.setUsertype((short) BOT_5K);
                } else if (type <= BOT1000) {
                    userTemp.setUsertype((short) BOT_1K);
                } else if (type <= BOT500) {
                    userTemp.setUsertype((short) BOT_500);
                } else if (type <= BOT200 && ServiceImpl.IsRunIndo) {
                    userTemp.setUsertype((short) BOT_200);
                } else {
                    userTemp.setUsertype((short) BOT_100);
                }

                long agDB = rs.getLong("AG");
                long ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG(ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.BINH);
                listBotNew.add(userTemp);
            }
            rs.close();
            cs.close();
            System.out.println("==>Size listBotBinh:" + listBotNew.size());
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }

    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, long mark) {
        // TODO Auto-generated method stub
        return 0l;
    }

    @Override
    public void BotCreateTable(int gameid, int mark, int type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void BotCreateTable(int gameid, int mark) {
        // TODO Auto-generated method stub

    }

    @Override
    public UserInfo botLogin(int gameid) {
        synchronized (listBotNew) {
            for (int i = 0; i < listBotNew.size(); i++) {
                if (listBotNew.get(i).getGameid() == 0) {
                    listBotNew.get(i).setGameid((short) gameid);
                    return listBotNew.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public void processBotDisconnect(int playerId) {
        try {
            for (int i = 0; i < listBotNew.size(); i++) {
                if (listBotNew.get(i).getPid() == playerId) {
                    listBotNew.get(i).setGameid((short) 0);
                    break;
                }
            }
            // Check bot dag choi giai phong sau khi van choi ket thuc
            if (dicBotNew.containsKey(playerId)) {
                dicBotNew.get(playerId).setDisconnect(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
        try {
            if (!dicBotNew.containsKey(pid)) {
                return null;
            }
            dicBotNew.get(pid).setRoomId((short) roomId);
            dicBotNew.get(pid).setTableId(tableId);
            dicBotNew.get(pid).setAS((long) mark);
            synchronized (listBotNew) {
                for (int i = 0; i < listBotNew.size(); i++) {
                    if (listBotNew.get(i).getPid() == pid) {
                        listBotNew.remove(i);
                        break;
                    }
                }
            }
            return dicBotNew.get(pid);
        } catch (Exception e) {
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
        try {
            synchronized (dicBotNew) {
                if (dicBotNew.containsKey(pid)) {
                    if (tid > 0) {
                        dicBotNew.get(pid).setTableId(tid);
                    }
                    return ActionUtils.gson.toJson(dicBotNew.get(pid).getUserGame());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
        return dicBotNew;
    }

    @Override
    public List<UserInfo> getListBot() {
        return listBotNew;
    }
}
