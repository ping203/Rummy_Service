package com.athena.services.bot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
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
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;

public class BotPoker9K2345 implements Bot {

    public static Logger logger = Logger.getLogger(LoggerKey.DETAILS_BOT);

    public final int BOT_MARK_10 = 10;
    public final int BOT_MARK_100 = 100;
    public final int BOT_MARK_500 = 500;
    public final int BOT_MARK_1K = 1000;
    public final int BOT_MARK_10K = 10000;
    public final int BOT_MARK_50K = 50000;
    public final int BOT_MARK_100K = 100000;
    public final int BOT_MARK_500K = 500000;

    public static final int Mark1 = 10;
    public static final int Mark2 = 100;
    public static final int Mark3 = 500;
    public static final int Mark4 = 1000;
    public static final int Mark5 = 5000;
    public static final int Mark6 = 10000;
    public static final int Mark7 = 50000;
    public static final int Mark8 = 100000;
    public static final int Mark9 = 500000;
    public static final int Mark10 = 1000000;
    public static final int Mark11 = 5000000;

//	  lsMarkCreateTable.add(new MarkCreateTable(10, 50, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(100, 500, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(500, 2500, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(1000, 5000, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(5000, 25000, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(10000, 50000, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(50000, 250000, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(100000, 500000, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(500000, 2500000, 0));
//      lsMarkCreateTable.add(new MarkCreateTable(1000000, 5000000, 0));
    //Setup Danh sach muc cuoc ban
    public final int BOT_TYPE10 = 11;
    public final int BOT_TYPE100 = 12;
    public final int BOT_TYPE500 = 13;
    public final int BOT_TYPE1K = 14;
    public final int BOT_TYPE10K = 15;
    public final int BOT_TYPE50K = 16;
    public final int BOT_TYPE100K = 17;
    public final int BOT_TYPE500K = 18;

//	 public final int BOT100000 = 25;//20;	 
    public final int MIN_AG_RATE = 10;
    public final int MAX_AG_RATE = 200;
    public final int MAX_ADD_AG = 50;
    public final int markRate = 5;

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotPoker9K = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBotPoker9K = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;

    public BotPoker9K2345(int source) {
        getListBot(source);
    }

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        //if(Diamond > 0)
        // System.out.println("==>BotHandler==>getUserGame: listBotPoker9K size:"+ listBotPoker9K.size() +" - minAG: "+minAG+" - Diamond: "+Diamond);
        //System.out.println("==>BotPoker9K2345==>getUserGame: " + listBotPoker9K.size() + " - " + minAG + " - " + Diamond);

        if (Diamond != 0) {
            return;
        }
        if (minAG > 500000) {
            return;
        }
        // System.out.println("==>getUserGame==>getBot: size: "+listBotPoker9K.size()+" - ag: "+minAG+ " - gameid: "+gameId+" -tableid: "+tableId+ " - Diamod "+Diamond);
        synchronized (listBotPoker9K) {
            try {
                short type = BOT_TYPE10;
                if (minAG >= BOT_MARK_500K) {
                    type = BOT_TYPE500K;
                } else if (minAG >= BOT_MARK_100K) {
                    type = BOT_TYPE100K;
                } else if (minAG >= BOT_MARK_50K) {
                    type = BOT_TYPE50K;
                } else if (minAG >= BOT_MARK_10K) {
                    type = BOT_TYPE10K;
                } else if (minAG >= BOT_MARK_1K) {
                    type = BOT_TYPE1K;
                } else if (minAG >= BOT_MARK_500) {
                    type = BOT_TYPE500;
                } else if (minAG >= BOT_MARK_100) {
                    type = BOT_TYPE100;
                }
                //Duyệt tìm boss sẵn sàng đủ tiền chơi
                for (int i = 0; i < listBotPoker9K.size(); i++) {
                    //System.out.println("==>BotHandler==>getUserGame: "+ listBotPoker9K.get(i).getGameid()+" - "
                    //+listBotPoker9K.get(i).getUsertype()+" - "+ type+" - isOl: "+listBotPoker9K.get(i).getIsOnline()+" - "+listBotPoker9K.get(i).getAG());
                    if (listBotPoker9K.get(i).getGameid() == gameId // client bot connected
                            && listBotPoker9K.get(i).getUsertype() == type) { // check type + ag
                        UserInfo u = listBotPoker9K.get(i);
                        int addAG = 0;
                        //System.out.println("\n ==>BotHandler==>BotCreateTable GETTTTTT : " + ActionUtils.gson.toJson(listBotPoker9K.get(i))+" \nBOTGETTTTT\n");
                        if (u.getAG() < minAG * markRate && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
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
                        if (u.getAG() < minAG * markRate) {
                            //System.out.println("==>Poker9KBot: errMoney "+u.getUserid().intValue()+" - "+u.getAG().intValue());
                            continue;
                        }
                        u.setIsOnline(gameId);
                        dicBotPoker9K.put(u.getUserid(), u);
                        //System.out.println("==>BotHandler==>getUserGame: "+ActionUtils.gson.toJson(u));
                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                        serviceRouter.dispatchToGame(gameId, action);
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

    public int getAGADD(int type) {
        int addAG = 0;
        switch (type) {
            case BOT_TYPE500K:
                addAG = randomBetween2Number(50000000, 100000000);
                break;
            case BOT_TYPE100K:
                addAG = randomBetween2Number(20000000, 100000000);
                break;
            case BOT_TYPE50K:
                addAG = randomBetween2Number(20000000, 40000000);
                break;
            case BOT_TYPE10K:
                addAG = randomBetween2Number(2000000, 8000000);
                break;
            case BOT_TYPE1K:
                addAG = randomBetween2Number(800000, 4000000);
                break;
            case BOT_TYPE500:
                addAG = randomBetween2Number(160000, 800000);
                break;
            case BOT_TYPE100:
                addAG = randomBetween2Number(80000, 200000);
                break;
            case BOT_TYPE10:
                addAG = randomBetween2Number(40000, 200000);
            default:
                break;
        }
        return addAG;
    }

    public int getVip(short type) {
        int addAG = 1;
        switch (type) {
            case BOT_TYPE100K:
            case BOT_TYPE500K:
                addAG = (new Random()).nextInt(5) + 2;
                break;
            case BOT_TYPE50K:
                addAG = (new Random()).nextInt(3) + 2;
                break;
            case BOT_TYPE10K:
            case BOT_TYPE1K:
            case BOT_TYPE500:
            case BOT_TYPE100:
            case BOT_TYPE10:
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
            cs.setInt("Gameid", GAMEID.POKER9K_2345);
            ResultSet rs = cs.executeQuery();
            short type = 0;
            listBotPoker9K.clear();
            int bot100 = 550;
            int bot500 = 450;
            int bot1k = 350;
            int bot10k = 250;
            int bot50k = 150;
            int bot100k = 80;
            int bot500k = 20;

            if (ServiceImpl.IsRunIndo && !ServiceImpl.IsRunThai) {
                bot100 = 150;
                bot500 = 120;
                bot1k = 90;
                bot10k = 60;
                bot50k = 30;
                bot100k = 10;
                bot500k = 0;
            }
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
                if (type <= bot500k) {
                    userTemp.setUsertype((short) BOT_TYPE500K);
                } else if (type <= bot100k) {
                    userTemp.setUsertype((short) BOT_TYPE100K);
                } else if (type <= bot50k) {
                    userTemp.setUsertype((short) BOT_TYPE50K);
                } else if (type <= bot10k) {
                    userTemp.setUsertype((short) BOT_TYPE10K);
                } else if (type <= bot1k) {
                    userTemp.setUsertype((short) BOT_TYPE1K);
                } else if (type <= bot500) {
                    userTemp.setUsertype((short) BOT_TYPE500);
                } else if (type <= bot100) {
                    userTemp.setUsertype((short) BOT_TYPE100);
                } else {
                    userTemp.setUsertype((short) BOT_TYPE10);
                }

                // kiem tra them tien cho bot
                int agDB = rs.getInt("AG");
                int ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG((long) ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.POKER9K_2345);
                listBotPoker9K.add(userTemp);
            }
            rs.close();
            cs.close();
            //System.out.println("==>Size9K2345:" + listBotPoker9K.size()) ;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("==>Error==>GetListBot:" + ex.getMessage());
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

    public Long UpdateBotPoker9KMark(int uid, long mark) {
        synchronized (dicBotPoker9K) {
            if (dicBotPoker9K.containsKey(uid)) {
                //System.out.println("UpdateBotMarkChessByName: dicBotPoker9K.containsKey(name) uid: " + uid+" - mark: "+
                //		 +mark+" - "+dicBotPoker9K.get(uid).getAG());
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
                //System.out.println("UpdateBotMarkChessByName: !dicBotPoker9K.containsKey(name)" + uid+" - "+mark);
            }
        }
        return 0l;
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
                    if (u.getAG() < mark * markRate && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {

                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);
                            logger.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.POKER9K_2345);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.POKER9K_2345);
                        }
                    } else if (u.getAG() < markRate * mark) {
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
                    serviceRouter.dispatchToGameActivator(gameid, request);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {
            if (je.has("act")) {
                if (je.get("act").getAsInt() == 0) {
                    int size = 0, bot50 = 0, bot200 = 0, bot1k = 0;
                    List<UserInfo> lsBot = new ArrayList<UserInfo>();
                    for (int i = 0; i < listBotPoker9K.size(); i++) {
                        if (listBotPoker9K.get(i).getGameid() == 0) {
                            size++;
                        }
//			        		if(listBotPoker9K.get(i).getUsertype() > BOT_TYPE200)
//			        			bot1k++;
//			        		else if(listBotPoker9K.get(i).getUsertype() > BOT_TYPE50)
//			        			bot200++;
//			        		else
//			        			bot50++;

                        if (je.has("T") && je.get("T").getAsInt() == listBotPoker9K.get(i).getUsertype()) {
                            lsBot.add(listBotPoker9K.get(i));
                        }
                    }
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "checkBot");
                    jo.addProperty("Disconnected", size + " / Size: " + listBotPoker9K.size());
                    jo.addProperty("bot50", bot50);
                    jo.addProperty("bot200", bot200);
                    jo.addProperty("bot1000", bot1k);

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
    public void setServiceRouter(ServiceRouter router) {
        this.serviceRouter = router;
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
    public void BotCreateTable(int gameid, int mark) {
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
            dicBotPoker9K.get(pid).setTableId(tableId);
            dicBotPoker9K.get(pid).setAutoFill(true);
            dicBotPoker9K.get(pid).setAS((long) mark);
            dicBotPoker9K.get(pid).setAutoTopOff(false);
            dicBotPoker9K.get(pid).setAGBuyIn(dicBotPoker9K.get(pid).getAG());
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
        synchronized (dicBotPoker9K) {
            if (dicBotPoker9K.containsKey(uid)) {
                //System.out.println("UpdateBotMarkChessByName: dicBotPoker9K.containsKey(name) uid: " + uid+" - mark: "+
                //		 +mark+" - "+dicBotPoker9K.get(uid).getAG());
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
                //System.out.println("UpdateBotMarkChessByName: !dicBotPoker9K.containsKey(name)" + uid+" - "+mark);
            }
        }
        return 0l;

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
