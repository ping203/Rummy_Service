package com.athena.services.bot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.log.LoggerKey;
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

public class BotPoker9KHandler implements Bot {
    
    public static Logger logger = Logger.getLogger(LoggerKey.DETAILS_BOT);
    public static Logger logger_login = Logger.getLogger(LoggerKey.LOGIN_BOT);

    public final int BOT50 = 200;//60;
    public final int BOT200 = 250;//50;
    public final int BOT1000 = 100;//50;
    public final int BOT5000 = 0;//40;
    public final int BOT20000 = 0;// 30
    public final int BOT50000 = 0;//25;
//	 public final int BOT100000 = 25;//20;	 
    public final int MIN_AG_RATE = 10;
    public final int MAX_AG_RATE = 200;
    public final int MAX_ADD_AG = 50;

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotPoker9K = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBotPoker9K = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;

    public BotPoker9KHandler(int source) {
        getListBot(source);
    }

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        //if(Diamond > 0)
        //System.out.println("==>BotHandler==>getUserGame: listBotPoker9K size:"+ listBotPoker9K.size() +" - minAG: "+minAG+" - Diamond: "+Diamond);
        // System.out.println("==>BotHandler==>getUserGame: " + listBotPoker9K.size() + " - " + minAG + " - " + Diamond);

        if (Diamond != 0) {
            return;
        }
        //System.out.println("==>getUserGame==>getBot: size: "+listBotPoker9K.size()+" - ag: "+minAG+ " - gameid: "+gameId+" -tableid: "+tableId+
        //		 " - Diamod "+Diamond);
        synchronized (listBotPoker9K) {
            try {
                short type = 11; // bot bàn 50

                if (minAG >= 100000) // bàn 100k
                {
                    type = 33;
                } else if (minAG >= 50000) { // ban 50k
                    type = 16;
                } else if (minAG >= 20000) { // bàn  20k
                    type = 15;
                } else if (minAG >= 5000) { // bàn  5k
                    type = 14;
                } else if (minAG >= 1000) { // bàn  1k
                    type = 13;
                } else if (minAG >= 200) { // bàn  200
                    type = 12;
                }

//				  typeMarkHash.put(11, 50);
//				  typeMarkHash.put(21, 200);
//				  typeMarkHash.put(22, 1000);
//				  typeMarkHash.put(23, 5000);
//				  typeMarkHash.put(31, 20000);
//				  typeMarkHash.put(32, 50000);
//				  typeMarkHash.put(33, 100000);
                //  ==>BotHandler==>getUserGame: listBotPoker9K size:131 - minAG: 50 - Diamond: 0
                //		  ==>BotHandler==>getUserGame: 0 - 11 - 11 - isOl: 0
                //Duyệt tìm boss sẵn sàng đủ tiền chơi
                for (int i = 0; i < listBotPoker9K.size(); i++) {

                    //==>BotHandler==>getUserGame: 0 - 22 - 11 - isOl: 0
                    //System.out.println("==>BotHandler==>getUserGame: "+ listBotPoker9K.get(i).getGameid()+" - "
                    // +listBotPoker9K.get(i).getUsertype()+" - "+ type+" - isOl: "+listBotPoker9K.get(i).getIsOnline());
                    if (listBotPoker9K.get(i).getGameid() == gameId // client bot connected
                            && listBotPoker9K.get(i).getUsertype() == type) { // check type + ag
                        UserInfo u = listBotPoker9K.get(i);
                        int addAG = 0;
                        //System.out.println("\n ==>BotHandler==>BotCreateTable GETTTTTT : " + ActionUtils.gson.toJson(listBotPoker9K.get(i))+" \nBOTGETTTTT\n");
                        if (u.getAG() < minAG * 10 && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
//		    					Mức cược bàn		Nhóm mức cược		Số bot tiếp khách		MIN			MAX			Random Vip
//		    					50						1					60					500			10,000		0->2
//		    					200						1					50					2,000		40,000		1->3
//		    					1,000					2					50					10,000		200,000		1->3
//		    					5,000					2					40					50,000		1,000,000	2->4
//		    					20,000					3					30					200,000		4,000,000	2->4
//		    					50,000					3					25					500,000		10,000,000	2->5
//		    					100,000					3					20					1,000,000	20,000,000	3->6

                            //if(type > 23 ) u.setCPromot(9);
                            u.setCPromot(u.getCPromot().intValue() + 1);
                            addAG = getAGADD(type);

                        }
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG((long) addAG);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.POKER9K_2345);
                        }
                        if (u.getAG() < minAG * 10) {
                            //System.out.println("==>Poker9KBot: errMoney "+u.getUserid().intValue()+" - "+u.getAG().intValue());
                            continue;
                        }
                        u.setIsOnline(gameId);
                        dicBotPoker9K.put(u.getUserid(), u);
                        //System.out.println("==>BotHandler==>getUserGame: "+ActionUtils.gson.toJson(u));
                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                        getServiceRouter().dispatchToGame(gameId, action);
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

    public String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public int getAGADD(short type) {
        int addAG = 0;
        switch (type) {
            case 33:
                addAG = randomBetween2Number(1000000, 20000000);
                break;
            case 32:
                addAG = randomBetween2Number(500000, 10000000);
                break;
            case 31:
                addAG = randomBetween2Number(200000, 4000000);
                break;
            case 23:
                addAG = randomBetween2Number(50000, 1000000);
                break;
            case 22:
                addAG = randomBetween2Number(10000, 200000);
                break;
            case 21:
                addAG = randomBetween2Number(10000, 40000);
                break;
            case 13:
                addAG = randomBetween2Number(10000, 40000);
                break;
            case 12:
                addAG = randomBetween2Number(5000, 40000);
                break;
            case 11:
                addAG = randomBetween2Number(500, 10000);
                break;
            default:
                break;
        }
        return addAG;
    }

    public int getVip(short type) {
        int addAG = 0;
        switch (type) {
            // 0->2
            // 1->3
            // 1->3
            // 2->4
            // 2->4
            // 2->5
            // 3->6
            // 4->8
            case 33:
                addAG = (new Random()).nextInt(4) + 3;
                break;
            case 32:
                addAG = (new Random()).nextInt(3) + 2;
                break;
            case 31:
                addAG = (new Random()).nextInt(3) + 2;
                break;
            case 23:
                addAG = (new Random()).nextInt(3) + 2;
                break;
            case 22:
                addAG = (new Random()).nextInt(3) + 1;
                break;
            case 21:
                addAG = (new Random()).nextInt(3) + 1;
                break;
            case 13:
                addAG = (new Random()).nextInt(3) + 1;
                break;
            case 12:
                addAG = (new Random()).nextInt(3) + 1;
                break;
            case 11:
                addAG = (new Random()).nextInt(3) + 1;
                break;
            default:
                break;
        }
        return addAG;
    }

    public void getListBot(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID.POKER9K);
            ResultSet rs = cs.executeQuery();
            short type = 0;
            listBotPoker9K.clear();
            while (rs.next()) {
                UserInfo userTemp = new UserInfo();
//	            	System.out.println("==>Name:" + rs.getString("Username")) ;

                //long ag = rs.getLong("AG");
                userTemp.setUserid(rs.getInt("ID") + ServerDefined.userMap.get(source));
                userTemp.setFacebookid(Long.parseLong(rs.getString("FacebookId")));
                userTemp.setDeviceId(rs.getString("DeviceID"));
                //userTemp.setAG(ag);
                //userTemp.setVIP((short)rs.getInt("VIP"));
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

                type++;

//	            	if(type <=  BOT100000){
//	            		userTemp.setUsertype((short)33);
//	            	} 
//	            	if (type <= BOT50000){
//	            		userTemp.setUsertype((short)16);           		
//	            	}
//	            	if (type <= BOT20000){
//	            		userTemp.setUsertype((short)15);	
//	            	}
//	            	else if (type <= BOT5000){
//	            		userTemp.setUsertype((short)14);
//	            	}
                //else 
                if (type <= BOT1000) {
                    userTemp.setUsertype((short) 13);
                } else if (type <= BOT200) {
                    userTemp.setUsertype((short) 12);
                } else {
                    userTemp.setUsertype((short) 11);
                }

                // kiem tra them tien cho bot
                int agDB = rs.getInt("AG");
                int ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG((long) ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.BINH);

                listBotPoker9K.add(userTemp);

            }
            rs.close();
            cs.close();
            //System.out.println("==>Size:" + ServiceImpl.dicBot.size()) ;
        } catch (SQLException ex) {
//            System.out.println("==>Error==>GetListBot:" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            if (isOnline == 0) {
                //synchronized (listBotPoker9K) {    
                //System.out.println("==>BotHandler==>updateBotOnline:before " + ServiceImpl.listBot.size());
                if (dicBotPoker9K.containsKey(pid) && dicBotPoker9K.get(pid).getIsOnline() > 0) {
                    dicBotPoker9K.get(pid).setIsOnline(isOnline);
                    dicBotPoker9K.get(pid).setTableId(0);
                    if (dicBotPoker9K.get(pid).isDisconnect()) {
                        dicBotPoker9K.get(pid).setGameid((short) 0);
                    }
                    UserInfo u = dicBotPoker9K.get(pid);
                    listBotPoker9K.add(u);
                }
            } else {
                dicBotPoker9K.get(pid).setIsOnline(isOnline);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dicBotPoker9K.get(pid);
    }

    public long UpdateBotPoker9KMark(int uid, long mark) {
        synchronized (dicBotPoker9K) {
            if (dicBotPoker9K.containsKey(uid)) {
                // System.out.println("UpdateBotMarkChessByName: dicBotPoker9K.containsKey(name)" + uid+"-"+
                //		 	name+"-"+mark+"-"+dicBotPoker9K.get(uid).getAG());
                try {
                    long ag = dicBotPoker9K.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBotPoker9K.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", dicBotPoker9K.get(uid).getSource(), uid - ServerDefined.userMap.get((int) dicBotPoker9K.get(uid).getSource()), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBotPoker9K.get(uid).getAG();
                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("==>Error==>botHandler==>UpdateBotMarkChessByName:" + e.getMessage() + "-" + uid+"-" + name + "-" + mark);
                    return 0l;
                }
            } else {
                logger.info("UpdateBotMarkChessByName: !dicBotPoker9K.containsKey(name)" + uid + " - " + mark);
            }
        }
        return 0l;
    }

    public void BotCreateTable(int gameid, int mark) {
        //  Auto-generated method stub
        try {
            logger_login.info("==>BotHandler==>BotCreateTable " + gameid + " - " + mark);
            if (mark > 0) {
                return;
            }
            short type = 11;
            if (mark == 50000) {
                type = 31;
            } else if (mark == 100000) {
                type = 32;
            } else if (mark == 500000) {
                type = 33;
            }
            //System.out.println("==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotPoker9K));
            boolean check = true;
            for (int i = 0; i < listBotPoker9K.size(); i++) {
                //if(listBotPoker9K.get(i).getGameid() == gameid)
                //System.out.println("\n ==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotPoker9K.get(i))+" \nBOT\n");
                if (listBotPoker9K.get(i).getGameid() == gameid && listBotPoker9K.get(i).getUsertype() == type && listBotPoker9K.get(i).getRoomId() == 0) {
                    check = false;
                    UserInfo u = listBotPoker9K.get(i);
                    //System.out.println("==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotPoker9K.get(i))+"\n");
                    if (u.getAG() < mark * 10 && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            u.setAG(u.getAG() + addAG);
                            UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(),
                                    u.getUserid() - ServerDefined.userMap.get((int) u.getSource()), addAG);
                            QueueManager.getInstance(UserController.queuename).put(cmd);
                            logger.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.POKER9K);
                        }
                    } else if (u.getAG() < 10 * mark) {
                        continue; // tim bot tiep
                    }
                    listBotPoker9K.get(i).setRoomId((short) ((new Random()).nextInt(3) + 1));
                    u.setIsOnline((short) gameid);
                    //listBotPoker9K.remove(i);
                    dicBotPoker9K.put(u.getUserid(), u);
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
            if (check) {
                for (int i = 0; i < listBotPoker9K.size(); i++) {
                    if (listBotPoker9K.get(i).getUsertype() == type) {
                        logger.info("==>BotHandler==>HadBotCreateTable: failed listBotPoker9K size: " + listBotPoker9K.size()
                                + " - gameid: " + gameid + " - mark: " + mark);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        //  Auto-generated method stub
        try {
            if (mark > 0) {
                return;
            }

            short type = 11;
            if (mark >= 100000) // bàn 100k
            {
                type = 33;
            } else if (mark >= 50000) { // ban 50k
                type = 16;
            } else if (mark >= 20000) { // bàn  20k
                type = 15;
            } else if (mark >= 5000) { // bàn  5k
                type = 14;
            } else if (mark >= 1000) { // bàn  1k
                type = 13;
            } else if (mark >= 200) { // bàn  200
                type = 12;
            }
            //System.out.println("==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotPoker9K));
            //boolean check = true;
            for (int i = 0; i < listBotPoker9K.size(); i++) {
                //if(listBotPoker9K.get(i).getGameid() == gameid)
                //System.out.println("\n ==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotPoker9K.get(i))+" \nBOT\n");
                if (listBotPoker9K.get(i).getGameid() == gameid && listBotPoker9K.get(i).getUsertype() == type && listBotPoker9K.get(i).getRoomId() == 0) {
                    //check = false;	    				
                    UserInfo u = listBotPoker9K.get(i);
                    //System.out.println("==>BotHandler==>BotCreateTable : "+ActionUtils.gson.toJson(listBotPoker9K.get(i))+"\n");
                    if (u.getAG() < mark * 10 && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(),
                                    u.getUserid() - ServerDefined.userMap.get((int) u.getSource()), addAG - u.getAG().intValue());
                            QueueManager.getInstance(UserController.queuename).put(cmd);

                            u.setAG(u.getAG() + addAG);
                            logger.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.POKER9K);
                        }
                    } else if (u.getAG() < 10 * mark) {
                        continue; // tim bot tiep
                    }
                    listBotPoker9K.get(i).setRoomId((short) roomID);
                    u.setIsOnline((short) gameid);
                    //listBotPoker9K.remove(i);
                    dicBotPoker9K.put(u.getUserid(), u);
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

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {
            if (je.has("act")) {
                if (je.get("act").getAsInt() == 0) {
                    int s = 0, t11 = 0, t12 = 0, t21 = 0, t22 = 0, t23 = 0, t31 = 0, t32 = 0, t33 = 0;
                    List<UserInfo> lsBot = new ArrayList<UserInfo>();
                    for (int i = 0; i < listBotPoker9K.size(); i++) {
                        if (listBotPoker9K.get(i).getGameid() == 0) {
                            s++;
                        }
                        if (listBotPoker9K.get(i).getUsertype() > 32) {
                            t33++;
                        } else if (listBotPoker9K.get(i).getUsertype() > 31) {
                            t32++;
                        } else if (listBotPoker9K.get(i).getUsertype() > 30) {
                            t31++;
                        } else if (listBotPoker9K.get(i).getUsertype() > 22) {
                            t23++;
                        } else if (listBotPoker9K.get(i).getUsertype() > 21) {
                            t22++;
                        } else if (listBotPoker9K.get(i).getUsertype() > 20) {
                            t21++;
                        } else if (listBotPoker9K.get(i).getUsertype() > 11) {
                            t12++;
                        } else if (listBotPoker9K.get(i).getUsertype() > 10) {
                            t11++;
                        }
                        if (je.has("T") && je.get("T").getAsInt() == listBotPoker9K.get(i).getUsertype()) {
                            lsBot.add(listBotPoker9K.get(i));
                        }
                    }
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "checkBot");
                    jo.addProperty("Disconnected", s);
                    jo.addProperty("IsOnline", "Size: " + listBotPoker9K.size() + " - " + t33 + "/" + t32 + "/" + t31 + "/" + t23 + "/"
                            + t22 + "/" + t21 + "/" + t12 + "/" + t11);
                    jo.addProperty("Bot", ActionUtils.gson.toJson(lsBot));
                    ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                } else if (je.get("act").getAsInt() == 1) {
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "checkBot");
                    jo.addProperty("dic", ActionUtils.gson.toJson(dicBotPoker9K));
                    jo.addProperty("list", ActionUtils.gson.toJson(listBotPoker9K));
                    ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadBot(JsonObject je, UserInfo actionUser) {
        try {
            listBotPoker9K.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBotPoker9K.size(); i++) {
                if (listBotPoker9K.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotPoker9K.get(i).getIsOnline() > 0) {
                    t++;
                }
            }

            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBotPoker9K.size());
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    public UserInfo botLogin(int gameid) {
        synchronized (listBotPoker9K) {
            for (int i = 0; i < listBotPoker9K.size(); i++) {
                if (listBotPoker9K.get(i).getGameid() == 0) {
                    listBotPoker9K.get(i).setGameid((short) gameid);
                    return listBotPoker9K.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public void processBotDisconnect(int playerId) {
        try {
            synchronized (listBotPoker9K) {
                for (int i = 0; i < listBotPoker9K.size(); i++) {
                    if (listBotPoker9K.get(i).getPid() == playerId) {
                        listBotPoker9K.get(i).setGameid((short) 0);
                        break;
                    }
                }
                if (dicBotPoker9K.containsKey(playerId)) {
                    dicBotPoker9K.get(playerId).setDisconnect(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
        try {
            if (!dicBotPoker9K.containsKey(pid)) {
                return null;
            }
            dicBotPoker9K.get(pid).setRoomId((short) roomId);
            dicBotPoker9K.get(pid).setAS((long) mark);
            dicBotPoker9K.get(pid).setTableId(tableId);
            for (int i = 0; i < listBotPoker9K.size(); i++) {
                if (listBotPoker9K.get(i).getPid() == pid) {
                    listBotPoker9K.remove(i);
                    break;
                }
            }
            return dicBotPoker9K.get(pid);
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
        return UpdateBotPoker9KMark(uid, mark);
    }

    @Override
    public String processGetBotInfoByPid(int pid, int tid) {
        try {
            synchronized (dicBotPoker9K) {
                if (dicBotPoker9K.containsKey(pid)) {
                    if (tid > 0) {
                        dicBotPoker9K.get(pid).setTableId(tid);
                    }
                    return ActionUtils.gson.toJson(dicBotPoker9K.get(pid).getUserGame());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
        return dicBotPoker9K;
    }

    @Override
    public List<UserInfo> getListBot() {
        return listBotPoker9K;
    }
}
