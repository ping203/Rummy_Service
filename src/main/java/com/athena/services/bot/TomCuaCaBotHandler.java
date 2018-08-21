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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;

public class TomCuaCaBotHandler implements Bot {

    // config lists mark
    public final int BOT_MARK_100 = 100;
    public final int BOT_MARK_1K = 1000;
    public final int BOT_MARK_10K = 10000;
    public final int BOT_MARK_100K = 100000;
    public final int BOT_MARK_1M = 1000000;

    public static final int Mark1 = 100;
    public static final int Mark2 = 1000;
    public static final int Mark3 = 10000;
    public static final int Mark4 = 100000;
    public static final int Mark5 = 1000000;

    // config lists bot type
    public final int BOT_TYPE100 = 11;
    public final int BOT_TYPE1K = 12;
    public final int BOT_TYPE10K = 13;
    public final int BOT_TYPE100K = 14;
    public final int BOT_TYPE1M = 15;

    // so luong bot tung muc cuoc ban
    public final int bot_100_count_percent = 30;
    public final int bot_1K_count_percent = 25;
    public final int bot_10K_count_percent = 20;
    public final int bot_100K_count_percent = 15;
    public final int bot_1M_count_percent = 10;

    public final int MIN_AG_RATE = 10;
    public final int MAX_AG_RATE = 200;
//    public final int MAX_ADD_AG = 50;

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotTomCuaCa = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBotTomCuaCa = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;
    public Gson gson = new Gson();
//    public final ConcurrentLinkedHashMap<Integer, Integer> lsMinAg = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 8);
    public final Logger loggerBot_ = Logger.getLogger("TomCuaCaBot");
    public final Logger loggerError_ = Logger.getLogger("TomCuaCaError");

    public TomCuaCaBotHandler(int source) {
        getListBot(source);
    }

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        loggerBot_.info("===>getUserGame: Start");
//        if (minAG > 10) {
//            return;
//        }
        if (Diamond != 0) {
            return;
        }
        synchronized (listBotTomCuaCa) {
            try {
                short type = getTypeByMark(minAG);

                List<UserInfo> listBotWithType = new ArrayList<UserInfo>();
                for (int i = 0; i < listBotTomCuaCa.size(); i++) {
                    if (listBotTomCuaCa.get(i).getGameid() == gameId && listBotTomCuaCa.get(i).getUsertype() == type) {
                        listBotWithType.add(listBotTomCuaCa.get(i));
                    }
                }
                loggerBot_.info("===>getUserGame: gameId: " + gameId + ", type: " + type + ", count: " + listBotWithType.size());
                if (listBotWithType.size() == 0) {
                    return;
                }

                int index = new Random().nextInt(listBotWithType.size());
                UserInfo u = listBotTomCuaCa.get(index);

                if (u.getTableId() != 0) {
                    return;
                }

                int addAG = 0;
                if (u.getAG() < minAG * getRate(minAG) || u.getAG() > minAG * MAX_AG_RATE/* && u.getCPromot() < MAX_ADD_AG*/) {
                    u.setCPromot(u.getCPromot().intValue() + 1);
                    addAG = getAGADD(type);
                }
                loggerBot_.info("===>getUserGame: check addAG: " + u.getAG());

                if (addAG > 0) {
                    AddLogAgBot(addAG, u);
                    int abDB = u.getAG().intValue();
                    u.setAG((long) addAG);
                    ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.TOMCUACA);
                }
                loggerBot_.info(u.getUserGame() + " - " + minAG + " - " + gameId);
                u.setIsOnline(gameId);
                dicBotTomCuaCa.put(u.getUserid(), u);
                JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                serviceRouter.dispatchToGame(gameId, action);

//                for (int i = 0; i < listBotTomCuaCa.size(); i++) {
//                    if ((listBotTomCuaCa.get(i).getGameid() == gameId || listBotTomCuaCa.get(i).getGameid() == 8051)// client bot connected
//                            && listBotTomCuaCa.get(i).getUsertype() == type) { // check type + ag
//
//                        UserInfo u = listBotTomCuaCa.get(i);
//                        int addAG = 0;
//                        if (u.getAG() < lsMinAg.get((int) u.getUsertype()) && u.getCPromot() <getCountPromt(type)) {
//                            addAG = getAGADD(type);
//                        }
//                        if (addAG > 0) {
//                            AddLogAgBot(addAG, u);
//                            int abDB = u.getAG().intValue();
//                            u.setAG(u.getAG() + addAG);
//                            ActionUtils.updateAGBOT(u, u.getAG().intValue(),abDB, GAMEID.TOMCUACA);
//                            Logger.getLogger("TomCuaCaBot_Add_Gold").info(ActionUtils.gson.toJson(u) + " - " + minAG + " - " + addAG);
//                        }
//                        u.setIsOnline(gameId);
//                        dicBotTomCuaCa.put(u.getUserid(), u);
//                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
//                        serviceRouter.dispatchToGame(gameId, action);
//                        break;
//                    }
//                }
            } catch (Exception e) {
                loggerError_.error("===>getUserGame: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private short getTypeByMark(int minAG) {
        short type = BOT_TYPE100;
        if (minAG >= BOT_MARK_1M) {
            type = BOT_TYPE1M;
        } else if (minAG >= BOT_MARK_100K) {
            type = BOT_TYPE100K;
        } else if (minAG >= BOT_MARK_10K) {
            type = BOT_TYPE10K;
        } else if (minAG >= BOT_MARK_1K) {
            type = BOT_TYPE1K;
        }

        return type;
    }

    public int getAGADD(short type) {
        int addAG = 0;
        switch (type) {
            case BOT_TYPE1M:
                addAG = randomBetween2Number(getRate(BOT_MARK_1M) * BOT_MARK_1M, MAX_AG_RATE * BOT_MARK_1M);
                break;
            case BOT_TYPE100K:
                addAG = randomBetween2Number(getRate(BOT_MARK_100K) * BOT_MARK_100K, MAX_AG_RATE * BOT_MARK_100K);
                break;
            case BOT_TYPE10K:
                addAG = randomBetween2Number(getRate(BOT_MARK_10K) * BOT_MARK_10K, MAX_AG_RATE * BOT_MARK_10K);
                break;
            case BOT_TYPE1K:
                addAG = randomBetween2Number(getRate(BOT_MARK_1K) * BOT_MARK_1K, MAX_AG_RATE * BOT_MARK_1K);
                break;
            case BOT_TYPE100:
                addAG = randomBetween2Number(getRate(BOT_MARK_100) * BOT_MARK_100, MAX_AG_RATE * BOT_MARK_100);
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
            case BOT_TYPE1M:
                addAG = (new Random()).nextInt(5) + 4;
                break;
            case BOT_TYPE100K:
                addAG = (new Random()).nextInt(4) + 3;
                break;
            case BOT_TYPE10K:
                addAG = (new Random()).nextInt(4) + 2;
                break;
            case BOT_TYPE1K:
            case BOT_TYPE100:
                addAG = (new Random()).nextInt(3) + 1;
                break;
            default:
                break;
        }
        return addAG;
    }

    public int getRate(int minAg) {
        int rateMark = 1;
        switch (minAg) {
            case BOT_MARK_100:
            case BOT_MARK_1K:
                rateMark = 5;
                break;
            case BOT_MARK_10K:
                rateMark = 10;
                break;
            case BOT_MARK_100K:
            case BOT_MARK_1M:
                rateMark = 20;
                break;
            default:
                break;
        }
        return rateMark;
    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            synchronized (listBotTomCuaCa) {
                if (isOnline == 0) {
                    if (dicBotTomCuaCa.containsKey(pid) && dicBotTomCuaCa.get(pid).getIsOnline() > 0) {
                        dicBotTomCuaCa.get(pid).setIsOnline(isOnline);
                        dicBotTomCuaCa.get(pid).setTableId(0);
                        if (dicBotTomCuaCa.get(pid).isDisconnect()) {
                            dicBotTomCuaCa.get(pid).setGameid((short) 0);
                        }
                        UserInfo u = dicBotTomCuaCa.get(pid);
                        listBotTomCuaCa.add(u);
                        dicBotTomCuaCa.get(pid).setRoomId((short) 0);
                    }
                } else {
                    dicBotTomCuaCa.get(pid).setIsOnline(isOnline);
                }
            }
        } catch (Exception e) {
            loggerBot_.error("===>updateBotOnline: " + e.getMessage());
            e.printStackTrace();
        }
        return dicBotTomCuaCa.get(pid);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark) {
        synchronized (dicBotTomCuaCa) {
            loggerBot_.info("===>UpdateBotMarkChessByName: Start mark: " + mark);
            if (dicBotTomCuaCa.containsKey(uid)) {
                try {
                    long ag = dicBotTomCuaCa.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBotTomCuaCa.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid - ServerDefined.userMap.get(source), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBotTomCuaCa.get(uid).getAG();
                } catch (Exception e) {
                    loggerBot_.error(e.getMessage() , e);
                    return 0l;
                }
            } else {
                loggerBot_.info("UpdateBotMarkChessByName: !dicBotNew.containsKey(name)" + uid + "-" + name + mark);
            }
        }
        return 0l;
    }

    @Override
    public void BotCreateTable(int gameid, int mark) {
        try {
            short type = getTypeByMark(mark);

            for (int i = 0; i < listBotTomCuaCa.size(); i++) {
                if (listBotTomCuaCa.get(i).getGameid() == gameid && listBotTomCuaCa.get(i).getUsertype() == type && listBotTomCuaCa.get(i).getRoomId() == 0) {
                    UserInfo u = listBotTomCuaCa.get(i);

                    if (u.getAG() < mark * getRate(mark) || u.getAG() > mark * MAX_AG_RATE/* && u.getCPromot()< MAX_ADD_AG*/) { // kiem tra them tien cho bot
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG((long) addAG);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.TOMCUACA);
                        }
                    } else if (u.getAG() < mark) {
                        continue; // tim bot tiep
                    }
                    listBotTomCuaCa.get(i).setRoomId((short) ((new Random()).nextInt(2) + 1));
                    u.setIsOnline((short) gameid);
                    dicBotTomCuaCa.put(u.getUserid(), u);

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
            loggerBot_.error("===>BotCreateTable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ServiceRouter getServiceRouter() {
        return serviceRouter;
    }

    public void setServiceRouter(ServiceRouter serviceR) {
        serviceRouter = serviceR;
    }

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {
            int s = 0, count_100 = 0, count_1K = 0, count_10K = 0, count_100K = 0, count_1M = 0;
            List<String> lsBot = new ArrayList<String>();
            for (int i = 0; i < listBotTomCuaCa.size(); i++) {
                if (listBotTomCuaCa.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotTomCuaCa.get(i).getUsertype() == BOT_TYPE1M) {
                    count_1M++;
                } else if (listBotTomCuaCa.get(i).getUsertype() == BOT_TYPE100K) {
                    count_100K++;
                } else if (listBotTomCuaCa.get(i).getUsertype() == BOT_TYPE10K) {
                    count_10K++;
                } else if (listBotTomCuaCa.get(i).getUsertype() == BOT_TYPE1K) {
                    count_1K++;
                } else if (listBotTomCuaCa.get(i).getUsertype() == BOT_TYPE100) {
                    count_100++;
                }
                if (je.has("T") && je.get("T").getAsInt() == listBotTomCuaCa.get(i).getUsertype()) {
                    lsBot.add(listBotTomCuaCa.get(i).getUsername());
                }
            }
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "checkBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", "Size: " + listBotTomCuaCa.size() + " - " + count_1M + "/" + count_100K + "/" + count_10K + "/" + count_1K + "/" + count_100);
            jo.addProperty("Bot", ActionUtils.gson.toJson(lsBot));
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadBot(JsonObject je, UserInfo actionUser) {
        try {
            listBotTomCuaCa.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBotTomCuaCa.size(); i++) {
                if (listBotTomCuaCa.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotTomCuaCa.get(i).getIsOnline() > 0) {
                    t++;
                }
            }

            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBotTomCuaCa.size());
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
            cs.setInt("Gameid", GAMEID.TOMCUACA);
            ResultSet rs = cs.executeQuery();
            int count_list_bot = 0;
            while (rs.next()) {
                count_list_bot++;
            }
            rs.close();
            cs.close();

            cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID.TOMCUACA);
            rs = cs.executeQuery();

            int bot_100_count = (count_list_bot * bot_100_count_percent) / 100;
            int bot_1K_count = (count_list_bot * bot_1K_count_percent) / 100;
            int bot_10K_count = (count_list_bot * bot_10K_count_percent) / 100;
            int bot_100K_count = (count_list_bot * bot_100K_count_percent) / 100;
            int bot_1M_count = (count_list_bot * bot_1M_count_percent) / 100;

            loggerBot_.info("==>Size bot_100_count: " + bot_100_count);
            loggerBot_.info("==>Size bot_1K_count: " + bot_1K_count);
            loggerBot_.info("==>Size bot_10K_count: " + bot_10K_count);
            loggerBot_.info("==>Size bot_100K_count: " + bot_100K_count);
            loggerBot_.info("==>Size bot_1M_count: " + bot_1M_count);

            listBotTomCuaCa.clear();
            short type = 0;
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
//                userTemp.setAG(rs.getLong("AG"));

                type++;

                if (type <= bot_1M_count) {
                    userTemp.setUsertype((short) BOT_TYPE1M);
                } else if (type <= (bot_100K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE100K);
                } else if (type <= (bot_10K_count + bot_100K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE10K);
                } else if (type <= (bot_1K_count + bot_10K_count + bot_100K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE1K);
                } else {
                    userTemp.setUsertype((short) BOT_TYPE100);
                }

                // kiem tra them tien cho bot
                int agDB = rs.getInt("AG");
                int ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG((long) ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.TOMCUACA);
                listBotTomCuaCa.add(userTemp);
            }
            rs.close();
            cs.close();

            loggerBot_.info("==>Size listBotTomCuaCa:" + listBotTomCuaCa.size());
        } catch (SQLException ex) {
            loggerBot_.info("==>Error==>GetListBot:" + ex.getMessage());
        } finally {
            instance.releaseDbConnection(conn);
        }

    }

    private void AddLogAgBot(int AG, UserInfo bot) {
        JsonObject job = new JsonObject();
        job.addProperty("uid", bot.getPid());
        job.addProperty("n", bot.getUsername());
        job.addProperty("ag", AG);
//        loggerBot_.info(new Gson().toJson(job));
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
        synchronized (listBotTomCuaCa) {
            for (int i = 0; i < listBotTomCuaCa.size(); i++) {
                if (listBotTomCuaCa.get(i).getGameid() == 0) {
                    listBotTomCuaCa.get(i).setGameid((short) gameid);
                    return listBotTomCuaCa.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public void processBotDisconnect(int playerId) {
        try {
            synchronized (listBotTomCuaCa) {
                for (int i = 0; i < listBotTomCuaCa.size(); i++) {
                    if (listBotTomCuaCa.get(i).getPid() == playerId) {
                        listBotTomCuaCa.get(i).setGameid((short) 0);
                        break;
                    }
                }
                if (dicBotTomCuaCa.containsKey(playerId)) {
                    dicBotTomCuaCa.get(playerId).setDisconnect(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
        try {
            if (!dicBotTomCuaCa.containsKey(pid)) {
                return null;
            }
            dicBotTomCuaCa.get(pid).setRoomId((short) roomId);
            dicBotTomCuaCa.get(pid).setTableId(tableId);
            dicBotTomCuaCa.get(pid).setAS((long) mark);
            for (int i = 0; i < listBotTomCuaCa.size(); i++) {
                if (listBotTomCuaCa.get(i).getPid() == pid) {
                    listBotTomCuaCa.remove(i);
                    break;
                }
            }
            return dicBotTomCuaCa.get(pid);
        } catch (Exception e) {
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
        try {
            synchronized (dicBotTomCuaCa) {
                if (dicBotTomCuaCa.containsKey(pid)) {
                    if (tid > 0) {
                        dicBotTomCuaCa.get(pid).setTableId(tid);
                    }
                    return ActionUtils.gson.toJson(dicBotTomCuaCa.get(pid).getUserGame());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
        return dicBotTomCuaCa;
    }

    @Override
    public List<UserInfo> getListBot() {
        return listBotTomCuaCa;
    }

}
