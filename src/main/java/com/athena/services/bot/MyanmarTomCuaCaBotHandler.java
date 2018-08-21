/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.bot;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.ServerSource;
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

/**
 *
 * @author hoangchau
 */
public class MyanmarTomCuaCaBotHandler implements Bot {
    private static final int GAMEID = com.athena.services.utils.GAMEID.MYANMAR_TOMCUACA;
    
    public final int BOT_MARK_100 = 100;
    public final int BOT_MARK_1K = 1000;
    public final int BOT_MARK_10K = 10000;
    public final int BOT_MARK_100K = 100000;
    public final int BOT_MARK_1M = 1000000;

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

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBot = 
    		ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBots = new ArrayList<>();
    public ServiceRouter serviceRouter;
    public Gson gson = new Gson();
    
    public final Logger loggerBotAddGold_ = Logger.getLogger("MyanmarBot_Add_Gold");
    public final Logger loggerBot_ = Logger.getLogger("MyanmarTomCuaCaBot");
    public final Logger loggerError_ = Logger.getLogger("MyanmarTomCuaCaError");
    public final static Logger _logger = Logger.getLogger(MyanmarTomCuaCaBotHandler.class);

    private final int source;

    public MyanmarTomCuaCaBotHandler(int source) {
        this.source = source;

        getListBot();
    }

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setServiceRouter(ServiceRouter router) {
        this.serviceRouter = router;
    }

    @Override
    public void getUserGame(int mark, short gameId, int tableId, int diamond) {
        if (diamond != 0) {
            return;
        }

        synchronized (listBots) {
            try {
                short type = getTypeByMark(mark);

                List<UserInfo> listBotWithType = new ArrayList<>();
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getGameid() == gameId && listBots.get(i).getUsertype() == type && listBots.get(i).getTableId() == 0) {
                        listBotWithType.add(listBots.get(i));
                    }
                }
                if (listBotWithType.isEmpty()) {
                    return;
                }

                int index = new Random().nextInt(listBotWithType.size());
                UserInfo u = listBots.get(index);

                int addAG = 0;
                if (u.getAG() < (mark * getRate(mark)) || u.getAG() > mark * MAX_AG_RATE) {
                    u.setCPromot(u.getCPromot() + 1);
                    addAG = getAGADD(type);
                    if (addAG > 0) {
                        AddLogAgBot(addAG, u);
                        long abDB = u.getAG();
                        u.setAG((long) addAG);
                        ActionUtils.updateAGBOT(u, u.getAG(), abDB, GAMEID);
                        loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                        loggerBot_.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                    }
                }
                u.setIsOnline(gameId);
                dicBot.put(u.getUserid(), u);
                JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                serviceRouter.dispatchToGame(gameId, action);
                
            } catch (Exception e) {
                loggerBot_.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU) {
        return UpdateBotMarkChessByName(uid,ServerSource.MYA_SOURCE,name ,mark);
    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, long mark) {
        return UpdateBotMarkChessByName(uid,ServerSource.MYA_SOURCE,"" ,mark);
    }

    @Override
    public void BotCreateTable(int gameid, int mark, int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void BotCreateTable(int gameid, int mark) {
        try {
            short type = getTypeByMark(mark);

            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == gameid && listBots.get(i).getUsertype() == type && listBots.get(i).getTableId() == 0) {
                    UserInfo u = listBots.get(i);

                    if (u.getAG() < mark * getRate(mark) || u.getAG() > mark * MAX_AG_RATE/* && u.getCPromot()< MAX_ADD_AG*/) { // kiem tra them tien cho bot
                        long addAG = getAGADD(type);
                        if (addAG > 0) {
                            long abDB = u.getAG();
                            u.setAG((long) addAG);
                            ActionUtils.updateAGBOT(u, u.getAG(), abDB, GAMEID);
                        }
                    } else if (u.getAG() < mark) {
                        continue; // tim bot tiep
                    }
                    listBots.get(i).setRoomId((short) ((new Random()).nextInt(2) + 1));
                    u.setIsOnline((short) gameid);
                    dicBot.put(u.getUserid(), u);

                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "botCreateTable");
                    send.addProperty("pid", u.getPid());
                    send.addProperty("M", mark);
                    ActivatorAction<String> request = new ActivatorAction<>(ActionUtils.gson.toJson(send));
                    serviceRouter.dispatchToGameActivator(gameid, request);
                    break;
                }
            }
        } catch (Exception e) {
            loggerBot_.error(e.getMessage(), e);
        }
    }

    @Override
    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        try {
            if (mark > 0)
                return;

            short type = getTypeByMark(mark);
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == gameid && listBots.get(i).getUsertype() == type && listBots.get(i).getRoomId() == 0 && listBots.get(i).getTableId() == 0) {
                    UserInfo u = listBots.get(i);
                    if (u.getAG() < mark * getRate(mark)) { // kiem tra them tien cho bot
                        u.setCPromot(u.getCPromot() + 1);
                        long addAG = getAGADD(type);
                        if (addAG > 0) {
                            long abDB = u.getAG();
                            u.setAG(addAG);
                            loggerBot_.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG(), addAG, GAMEID);
                            ActionUtils.updateAGBOT(u, u.getAG(), abDB, GAMEID);
                        }
                    } else if (u.getAG() < mark * getRate(mark)) {
                        continue; // tim bot tiep
                    }
                    listBots.get(i).setRoomId((short) roomID);
                    u.setIsOnline((short) gameid);
                    dicBot.put(u.getUserid(), u);

                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "botCreateTable");
                    send.addProperty("pid", u.getPid());
                    send.addProperty("M", mark);
                    ActivatorAction<String> request = new ActivatorAction<>(ActionUtils.gson.toJson(send));
                    serviceRouter.dispatchToGameActivator(gameid, request);
                    break;
                }
            }
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
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
            synchronized (listBots) {
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getPid() == playerId) {
                        listBots.get(i).setGameid((short) 0);
                        break;
                    }
                }
                if (dicBot.containsKey(playerId)) {
                    dicBot.get(playerId).setDisconnect(true);
                }
            }
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
    }

    @Override
    public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
        try {
            if (!dicBot.containsKey(pid)) {
                return null;
            }
            dicBot.get(pid).setRoomId((short) roomId);
            dicBot.get(pid).setTableId(tableId);
            dicBot.get(pid).setAS((long) mark);
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getPid() == pid) {
                    listBots.remove(i);
                    break;
                }
            }
            return dicBot.get(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long UpdateBotMarkByName(int uid, int source, String name, long mark) {
        return UpdateBotMarkChessByName(uid, source, name, mark);
    }
    
    public Long UpdateBotMarkChessByName(int uid, int source, String names, long mark) {
        synchronized (dicBot) {
            loggerBot_.info(" BotHandler=>UpdateBotMarkChessByName: Start: " + uid+"-"+mark);
            if (dicBot.containsKey(uid)) {
                try {
                    long ag = dicBot.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBot.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid - ServerDefined.userMap.get(source), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBot.get(uid).getAG();
                } catch (Exception e) {
                    loggerBot_.error(e.getMessage(), e);
                    return 0l;
                }
            } else {
                loggerBot_.info("==>PokdengBotHandler=>UpdateBotMarkChessByName: !dicBots.containsKey(name)" + uid + "-" + mark);
            }
        }
        return 0l;
    }

    @Override
    public UserInfo updateBotOnline(int pid, short isOnline) {
        try{
            synchronized (dicBot){
                if (isOnline == 0) {
                    if (dicBot.containsKey(pid) && dicBot.get(pid).getIsOnline() > 0) {
                        dicBot.get(pid).setIsOnline(isOnline);
                        dicBot.get(pid).setTableId(0);
                        if (dicBot.get(pid).isDisconnect())
                            dicBot.get(pid).setGameid((short) 0);
                        UserInfo u = dicBot.get(pid);
                        listBots.add(u);
                    }
                } else {
                    dicBot.get(pid).setIsOnline(isOnline);
                }
            }
        }catch (Exception e){
            _logger.error(e.getMessage(), e);
        }
        return dicBot.get(pid);
    }

    @Override
    public Long UpdateBotMarkByUID(int uid, long mark) {
        return UpdateBotMarkChessByName(uid,ServerSource.MYA_SOURCE,"",mark);
    }

    @Override
    public String processGetBotInfoByPid(int pid, int tid) {
        try{
            synchronized (dicBot) {
                if (dicBot.containsKey(pid)) {
                    if (tid > 0) {
                        dicBot.get(pid).setTableId(tid);
                    }
                    return ActionUtils.gson.toJson(dicBot.get(pid).getUserGame());
                }
            }
        }catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
        return "";
    }

    @Override
    public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
        return dicBot;
    }

    @Override
    public List<UserInfo> getListBot() {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID);
            ResultSet rs = cs.executeQuery();
            int count_list_bot = 0;
            while (rs.next()) {
                count_list_bot++;
            }
            rs.close();
            cs.close();

            cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID);
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

            listBots.clear();
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
                long agDB = rs.getLong("AG");
                long ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG((long) ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID);
                listBots.add(userTemp);
            }
            rs.close();
            cs.close();

        } catch (SQLException ex) {
            loggerBot_.error(ex.getMessage(), ex);
        } finally {
            instance.releaseDbConnection(conn);
        }

        return null;
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
    
    private void AddLogAgBot(int AG, UserInfo bot) {
        JsonObject job = new JsonObject();
        job.addProperty("uid", bot.getPid());
        job.addProperty("n", bot.getUsername());
        job.addProperty("ag", AG);
        loggerBotAddGold_.info(new Gson().toJson(job));
        loggerBot_.info(new Gson().toJson(job));
    }
}
