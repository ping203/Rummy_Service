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
import com.athena.services.constant.BinhConstant;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.GameUtil;
import com.dst.ServerSource;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;


public class MyanmarBinhBotHandler implements Bot {
    public static final Integer GAMEID = com.athena.services.utils.GAMEID.MYANMAR_BURMESE_POKER;

    public static final int Binh_Mark200 = 200;
    public static final int Binh_Mark500 = 500;
    public static final int Binh_Mark1K = 1000;
    public static final int Binh_Mark5K = 5000;
    public static final int Binh_Mark10K = 10000;
    public static final int Binh_Mark50K = 50000;
    public static final int Binh_Mark100K = 100000;
    public static final int Binh_Mark500K = 500000;
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

    public static  Logger _loggerDetails = Logger.getLogger(LoggerKey.DETAILS_BOT);
    public static Logger loggerBotAddGold_ = Logger.getLogger(LoggerKey.ADD_GOLD_BOT);
    public static  Logger _logger = Logger.getLogger(MyanmarBinhBotHandler.class);

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBots
            = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBots = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    public MyanmarBinhBotHandler(int source) {
        getListBot(source);
    }

    private boolean isBotReady(UserInfo u, int gameId, int type) {
//        if (BinhConstant.DEBUG) {
//            if (u.getUsertype() < 10 && u.getGameid() == gameId && u.getRoomId() == 0
//                    && u.getIsOnline() == 0) return true;
//        } else {
            if (u.getUsertype() < type) return false;
            if (u.getGameid() == gameId && u.getRoomId() == 0
                    && u.getIsOnline() == 0) return true;
//        }

        return false;
    }

    public void getUserGame(int mark, short gameId, int tableId, int Diamond) {
        if (Diamond != 0) return;
        if (mark > Binh_Mark500K) return;
        synchronized (listBots) {
            try {
                short type = BOT_100;
                if (mark >= Binh_Mark500K)
                    type = BOT_500K;
                else if (mark >= Binh_Mark100K) {
                    type = BOT_100K;
                } else if (mark >= Binh_Mark50K) {
                    type = BOT_50K;
                } else if (mark >= Binh_Mark10K) {
                    type = BOT_10K;
                } else if (mark >= Binh_Mark5K) {
                    type = BOT_5K;
                } else if (mark >= Binh_Mark1K) {
                    type = BOT_1K;
                } else if (mark >= Binh_Mark500) {
                    type = BOT_500;
                } else if (mark >= 200) {
                    type = BOT_200;
                }
                //Duyệt tìm boss sẵn sàng đủ tiền chơi
                List<UserInfo> lstRealy = new ArrayList<>();
                for (int i = 0; i < listBots.size(); i++) {
                    if (isBotReady(listBots.get(i), gameId, type)) {
                    	lstRealy.add(listBots.get(i));
                    }
                }
                
                if(lstRealy.size() > 0){
	                int index = GameUtil.random.nextInt(lstRealy.size());
	                UserInfo u = lstRealy.get(index);
	                int addAG = 0;
	                if (u.getAG() < BinhConstant.getMarkCreateTableByMark(mark).getAg() && u.getCPromot().intValue() < Binh_Max_Add_Gold) { // kiem tra them tien cho bot
	                    addAG = getAGADD(type);
	                } 
	                if (addAG > 0) {
	                    int abDB = u.getAG().intValue();
	                    u.setAG(u.getAG() + addAG);
	                    ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
	
	                    loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
	                    _loggerDetails.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
	                }
	
	                u.setIsOnline(gameId);
	                synchronized (dicBots) {
	                    dicBots.put(u.getUserid(), u);
	                }
	                JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
	                getServiceRouter().dispatchToGame(gameId, action);
                }
            } catch (Exception e) {
                _logger.error(e.getMessage(), e);
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
                addAG = randomBetween2Number(25000000, 75000000);
                break;
            case BOT_100K:
                addAG = randomBetween2Number(5000000, 15000000);
                break;
            case BOT_50K:
                addAG = randomBetween2Number(2500000, 7500000);
                break;
            case BOT_10K:
                addAG = randomBetween2Number(200000, 1000000);
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
            synchronized (dicBots) {
                if (isOnline == 0) {
                    if (dicBots.containsKey(pid) && dicBots.get(pid).getIsOnline() > 0) {
                    	System.out.println("==> updateBotOnline: "+dicBots.get(pid).getUsername()+dicBots.get(pid).getIsOnline()
                    			+"#"+dicBots.get(pid).getTableId()+"#"+dicBots.get(pid).getRoomId());
                        dicBots.get(pid).setIsOnline(isOnline);
                        
                        dicBots.get(pid).setTableId(0);
                        dicBots.get(pid).setRoomId((short) 0);
                        
                        if (dicBots.get(pid).isDisconnect()) 
                        	dicBots.get(pid).setGameid((short) 0);
                        UserInfo u = dicBots.get(pid);
                        listBots.add(u);
                        for(int i = 0; i < listBots.size(); i++){
                        	if(listBots.get(i).getIsOnline() > 0 || listBots.get(i).getRoomId() > 0){
                        		System.out.println("name: "+ listBots.get(i).getUsername() +"#"+listBots.get(i).getTableId());
                        	}
                        }
                    }
                } else {
                    dicBots.get(pid).setIsOnline(isOnline);
                }
            }

        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
        return dicBots.get(pid);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU) {
        synchronized (dicBots) {
            if (dicBots.containsKey(uid)) {
                try {
                    long ag = dicBots.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBots.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid - ServerDefined.userMap.get(source), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBots.get(uid).getAG();
                } catch (Exception e) {
                    _logger.error(e.getMessage(), e);
                    return 0l;
                }
            } else {
                _loggerDetails.info("UpdateBotMarkChessByName: !dicBots.containsKey(name)" + uid + "-" + name + mark);
            }
        }
        return 0l;
    }

    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        try {
            short type = BOT_100; // bot bàn 100
            if (mark >= Binh_Mark500K) // bàn 500k
                type = BOT_500K;
            else if (mark >= Binh_Mark100K) { // 100k
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
            } else if (mark >= Binh_Mark200) { // bàn  200
                type = BOT_200;
            }
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == gameid && listBots.get(i).getUsertype() == type && listBots.get(i).getRoomId() == 0
                        && listBots.get(i).getIsOnline() == 0) {
                    UserInfo u = listBots.get(i);

                    if (u.getAG() < BinhConstant.getMarkCreateTableByMark(mark).getAg() && u.getCPromot() < Binh_Max_Add_Gold) { // kiem tra them tien cho bot
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);

                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                            loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + addAG);
                            _loggerDetails.info(ActionUtils.gson.toJson(u) + " - " + addAG);
                        }
                    } else if (u.getAG() < 20 * mark) {
                        continue; // tim bot tiep
                    }
                    listBots.get(i).setRoomId((short) roomID);
                    u.setIsOnline((short) gameid);
                    dicBots.put(u.getUserid(), u);

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
            _logger.error(e.getMessage(), e);
        }
    }


    public ServiceRouter getServiceRouter() {
        return serviceRouter;
    }

    public void setServiceRouter(ServiceRouter serviceRouter) {
        this.serviceRouter = serviceRouter;
    }

    public void reloadBot(JsonObject je, UserInfo actionUser, UserController userController) {
        try {
            listBots.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == 0)
                    s++;
                if (listBots.get(i).getIsOnline() > 0)
                    t++;
            }

            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBots.size());
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }

    }

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {
            int s = 0, bot100 = 0, bot200 = 0, bot500 = 0, bot1k = 0, bot5k = 0, bot10k = 0, bot50k = 0, bot100k = 0, bot500k = 0;
            List<String> lsBot = new ArrayList<String>();
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == 0)
                    s++;
                if (listBots.get(i).getUsertype() > BOT_100K)
                    bot500k++;
                else if (listBots.get(i).getUsertype() > BOT_50K)
                    bot100k++;
                else if (listBots.get(i).getUsertype() > BOT_10K)
                    bot50k++;
                else if (listBots.get(i).getUsertype() > BOT_5K)
                    bot10k++;
                else if (listBots.get(i).getUsertype() > BOT_1K)
                    bot5k++;
                else if (listBots.get(i).getUsertype() > BOT_500)
                    bot1k++;
                else if (listBots.get(i).getUsertype() > BOT_200)
                    bot500++;
                else if (listBots.get(i).getUsertype() > BOT_100)
                    bot200++;
                else
                    bot100++;
                if (je.has("T") && je.get("T").getAsInt() == listBots.get(i).getUsertype())
                    lsBot.add(listBots.get(i).getUsername() + listBots.get(i).getAG().intValue());
            }
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "checkBot");
            jo.addProperty("Disconnected", s + " / Size: " + listBots.size());
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
            _logger.error(e.getMessage(), e);
        }
    }

    public void getListBot(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            int sumBot = 0;

            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                sumBot++;
            }
            rs.close();
            cs.close();

            System.out.println("SumBot: " + sumBot);

            int p100 = 20;
            int p500 = 15;
            int p1k = 15;
            int p5k = 14;
            int p10k = 12;
            int p50k = 10;
            int p100k = 10;
            int p500k = 4;

            // So luog bot
            int BOT500 = p500 * sumBot / 100;
            int BOT1000 = p1k * sumBot / 100;
            int BOT5000 = p5k * sumBot / 100;
            int BOT10000 = p10k * sumBot / 100;
            int BOT50000 = p50k * sumBot / 100;
            int BOT100000 = p100k * sumBot / 100;
            int BOT500000 = p500k * sumBot / 100;


            cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID);
            rs = cs.executeQuery();
            short type = 0;
            listBots.clear();
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
                if (userTemp.getUsernameLQ().length() > 0)
                    userTemp.setUsername(userTemp.getUsernameLQ());

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
                } else
                    userTemp.setUsertype((short) BOT_100);

//                if (BinhConstant.DEBUG) {
//                    userTemp.setUsertype(new Random().nextInt(2) == 1 ? (short) 1 : userTemp.getUsertype());
//                }

                long agDB = rs.getLong("AG");
                long ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG(ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID);
                listBots.add(userTemp);
            }
            rs.close();
            cs.close();
            System.out.println("==>Size listBotBinh:" + listBots.size());
        } catch (SQLException ex) {
            _logger.error(ex.getMessage(), ex);
        } finally {
            instance.releaseDbConnection(conn);
        }

    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, long mark) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void BotCreateTable(int gameid, int mark, int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void BotCreateTable(int gameid, int mark) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserInfo botLogin(int gameid) {
        synchronized (listBots) {
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == 0) {
                    listBots.get(i).setGameid((short) gameid);
                    return listBots.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public void processBotDisconnect(int playerId) {
        try {
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getPid() == playerId) {
                    listBots.get(i).setGameid((short) 0);
                    break;
                }
            }
            // Check bot dag choi giai phong sau khi van choi ket thuc
            if (dicBots.containsKey(playerId)) {
                dicBots.get(playerId).setDisconnect(true);
            }
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
    }

    @Override
    public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
        try {
            if (!dicBots.containsKey(pid)) return null;
            dicBots.get(pid).setRoomId((short) roomId);
            dicBots.get(pid).setTableId(tableId);
            dicBots.get(pid).setAS((long) mark);
            synchronized (listBots) {
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getPid() == pid) {
                        listBots.remove(i);
                        break;
                    }
                }
            }
            return dicBots.get(pid);
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Long UpdateBotMarkByName(int uid, int source, String name, long mark) {
        return UpdateBotMarkChessByName(uid, ServerSource.MYA_SOURCE, name, mark);
    }

    @Override
    public Long UpdateBotMarkByUID(int uid, long mark) {
        return UpdateBotMarkChessByName(uid, ServerSource.MYA_SOURCE, "", mark);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark) {
        synchronized (dicBots) {
            _loggerDetails.info("Binh=>UpdateBotMarkChessByName: Start: " + uid + "-" + mark);
            if (dicBots.containsKey(uid)) {
                try {
                    long ag = dicBots.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBots.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid - ServerDefined.userMap.get(source), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBots.get(uid).getAG();
                } catch (Exception e) {
                    _loggerDetails.info("===>ERROR=>UpdateBotMarkChessByName: " + e.getMessage(), e);
                    _logger.error(e.getMessage(), e);
                    return 0l;
                }
            } else {
                _loggerDetails.info("==>Binh=>UpdateBotMarkChessByName: !dicBots.containsKey(name)" + uid + "-" + mark);
            }
        }
        return 0l;
    }

    @Override
    public String processGetBotInfoByPid(int pid, int tid) {
        try {
            synchronized (dicBots) {
                if (dicBots.containsKey(pid)) {
                    if (tid > 0) {
                        dicBots.get(pid).setTableId(tid);
                    }
                    return ActionUtils.gson.toJson(dicBots.get(pid).getUserGame());
                }
            }
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
        return "";
    }

    @Override
    public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
        return dicBots;
    }

    @Override
    public List<UserInfo> getListBot() {
        return listBots;
    }
}
