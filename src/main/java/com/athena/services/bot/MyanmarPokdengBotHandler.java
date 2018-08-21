package com.athena.services.bot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dst.config.myanPokdeng.PokdengConstant;
import org.apache.log4j.Logger;
import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.log.LoggerKey;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.ServerSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;

public class MyanmarPokdengBotHandler implements Bot {
    public final static Integer GAMEID = com.athena.services.utils.GAMEID.MYANMAR_SHAN_KOE_MEE_V1;
    // config lists mark
    public final int BOT_MARK_100 = 100;
    public final int BOT_MARK_500 = 500;
    public final int BOT_MARK_1K = 1000;
    public final int BOT_MARK_5K = 5000;
    public final int BOT_MARK_10K = 10000;
    public final int BOT_MARK_50K = 50000;
    public final int BOT_MARK_100K = 100000;
    public final int BOT_MARK_500K = 500000;
    public final int BOT_MARK_1M = 1000000;

    // config lists bot type
    public final int BOT_TYPE100 = 11;
    public final int BOT_TYPE500 = 12;
    public final int BOT_TYPE1K = 13;
    public final int BOT_TYPE5K = 14;
    public final int BOT_TYPE10K = 15;
    public final int BOT_TYPE50K = 30;
    public final int BOT_TYPE100K = 31;
    public final int BOT_TYPE500K = 32;
    public final int BOT_TYPE1M = 33;

    // so luong bot tung muc cuoc ban
    public final int bot_500_count_percent = 15;
    public final int bot_1K_count_percent = 15;
    public final int bot_5K_count_percent = 10;
    public final int bot_10K_count_percent = 12;
    public final int bot_50K_count_percent = 10;
    public final int bot_100K_count_percent = 10;
    public final int bot_500K_count_percent = 4;
    public final int bot_1M_count_percent = 4;

    public final int MIN_AG_RATE = 10;
    public final int MAX_AG_RATE = 200;

    private ConcurrentLinkedHashMap<Integer, UserInfo> dicBots = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    private List<UserInfo> listBots = new ArrayList<UserInfo>();
    private ServiceRouter serviceRouter;

    public final Logger _logger = Logger.getLogger(MyanmarTomCuaCaBotHandler.class);
    public final Logger loggerBotAddGold_ = Logger.getLogger(LoggerKey.ADD_GOLD_BOT);

    public MyanmarPokdengBotHandler(int source) {
        getListBot(source);
    }

    public void getUserGame(int mark, short gameId, int tableId, int Diamond) {
        if (Diamond != 0) {
            return;
        }

        synchronized (listBots) {
            try {
                short type = getTypeByMark(mark);

                List<UserInfo> listBotWithType = new ArrayList<UserInfo>();
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getGameid() == gameId && listBots.get(i).getUsertype() == type && listBots.get(i).getTableId() == 0) {
                        listBotWithType.add(listBots.get(i));
                    }
                }
                if (listBotWithType.size() == 0) {
                    return;
                }

                int index = new Random().nextInt(listBotWithType.size());
                UserInfo u = listBotWithType.get(index);

                long addAG = 0;
                if (u.getAG() < PokdengConstant.getMarkCreateTableByMark(mark).getAg() || u.getAG() > mark * MAX_AG_RATE) {
                    u.setCPromot(u.getCPromot().intValue() + 1);
                    addAG = getAGADD(type);
                    if (addAG > 0) {
                        AddLogAgBot(addAG, u);
                        int abDB = u.getAG().intValue();
                        u.setAG((long) addAG);
                        ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                        loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                    }
                }
                u.setIsOnline(gameId);
                dicBots.put(u.getUserid(), u);
                JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                serviceRouter.dispatchToGame(gameId, action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private short getTypeByMark(int minAG) {
        short type = BOT_TYPE100;
        if (minAG == BOT_MARK_1M) {
            type = BOT_TYPE1M;
        } else if (minAG == BOT_MARK_500K) {
            type = BOT_TYPE500K;
        } else if (minAG == BOT_MARK_100K) {
            type = BOT_TYPE100K;
        } else if (minAG == BOT_MARK_50K) {
            type = BOT_TYPE50K;
        } else if (minAG == BOT_MARK_10K) {
            type = BOT_TYPE10K;
        } else if (minAG == BOT_MARK_5K) {
            type = BOT_TYPE5K;
        } else if (minAG == BOT_MARK_1K) {
            type = BOT_TYPE1K;
        } else if (minAG == BOT_MARK_500) {
            type = BOT_TYPE500;
        }

        return type;
    }

    public long getAGADD(short type) {
        long addAG = 0;
        switch (type) {
            case BOT_TYPE1M:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_1M).getAg(), MAX_AG_RATE * BOT_MARK_1M);
                break;
            case BOT_TYPE500K:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_500K).getAg(), MAX_AG_RATE * BOT_MARK_500K);
                break;
            case BOT_TYPE100K:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_100K).getAg(), MAX_AG_RATE * BOT_MARK_100K);
                break;
            case BOT_TYPE50K:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_50K).getAg(), MAX_AG_RATE * BOT_MARK_50K);
                break;
            case BOT_TYPE10K:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_10K).getAg(), MAX_AG_RATE * BOT_MARK_10K);
                break;
            case BOT_TYPE5K:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_5K).getAg(), MAX_AG_RATE * BOT_MARK_5K);
                break;
            case BOT_TYPE1K:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_1K).getAg(), MAX_AG_RATE * BOT_MARK_1K);
                break;
            case BOT_TYPE500:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_500).getAg(), MAX_AG_RATE * BOT_MARK_500);
                break;
            case BOT_TYPE100:
                addAG = randomBetween2Number(PokdengConstant.getMarkCreateTableByMark(BOT_MARK_100).getAg(), MAX_AG_RATE * BOT_MARK_100);
                break;
            default:
                break;
        }
        return addAG;
    }

    public long randomBetween2Number(long lowerBound, long upperBound) {
        return ActionUtils.randomBetween2Number(lowerBound, upperBound);
    }

    public int getVip(short type) {
        int addAG = 1;
        switch (type) {
            case BOT_TYPE1M:
            case BOT_TYPE500K:
            case BOT_TYPE100K:
                addAG = (new Random()).nextInt(5) + 2;
                break;
            case BOT_TYPE50K:
                addAG = (new Random()).nextInt(4) + 2;
                break;
            case BOT_TYPE10K:
            case BOT_TYPE5K:
            case BOT_TYPE1K:
            case BOT_TYPE500:
            case BOT_TYPE100:
                addAG = (new Random()).nextInt(3) + 1;
                break;
            default:
                break;
        }
        return addAG;
    }

//    public int getRate(int minAg) {
//        int rateMark = 1;
//        switch (minAg) {
//            case BOT_MARK_100:
//            case BOT_MARK_500:
//            case BOT_MARK_1K:
//            case BOT_MARK_5K:
//                rateMark = 5;
//                break;
//            case BOT_MARK_10K:
//            case BOT_MARK_50K:
//                rateMark = 10;
//                break;
//            case BOT_MARK_100K:
//            case BOT_MARK_500K:
//            case BOT_MARK_1M:
//                rateMark = 20;
//                break;
//            default:
//                break;
//        }
//        return rateMark;
//    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            synchronized (listBots) {
                if (isOnline == 0) {
                    if (dicBots.containsKey(pid) && dicBots.get(pid).getIsOnline() > 0) {
                        dicBots.get(pid).setIsOnline(isOnline);
                        dicBots.get(pid).setTableId(0);
                        if (dicBots.get(pid).isDisconnect())
                            dicBots.get(pid).setGameid((short) 0);
                        UserInfo u = dicBots.get(pid);
                        listBots.add(u);
                    }
                } else {
                    dicBots.get(pid).setIsOnline(isOnline);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dicBots.get(pid);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String names, long mark) {
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
                    e.printStackTrace();
                    return 0l;
                }
            } else {
            }
        }
        return 0l;
    }

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }
    public void BotCreateTable(int gameid, int mark) {
        try {

            short type = getTypeByMark(mark);

            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == gameid && listBots.get(i).getUsertype() == type && listBots.get(i).getTableId() == 0) {
                    UserInfo u = listBots.get(i);
                    if (u.getAG() < PokdengConstant.getMarkCreateTableByMark(mark).getAg() || u.getAG() > mark * MAX_AG_RATE/* && u.getCPromot() < MAX_ADD_AG*/) {
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        long addAG = getAGADD(type);
                        if (addAG > 0) {
                            u.setAG((long) addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID);
                        }
                    } else if (u.getAG() < mark) {
                        continue; // tim bot tiep
                    }
                    listBots.get(i).setRoomId((short) PokdengConstant.getRoomIdByMark(mark));
                    u.setIsOnline((short) gameid);
                    dicBots.put(u.getUserid(), u);

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


    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        try {
            if (mark > 0)
                return;

            short type = getTypeByMark(mark);
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == gameid && listBots.get(i).getUsertype() == type && listBots.get(i).getRoomId() == 0 && listBots.get(i).getTableId() == 0) {
                    UserInfo u = listBots.get(i);
                    if (u.getAG() < PokdengConstant.getMarkCreateTableByMark(mark).getAg()) { // kiem tra them tien cho bot
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        long addAG = getAGADD(type);
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);

                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                        }
                    } else if (u.getAG() < PokdengConstant.getMarkCreateTableByMark(mark).getAg()) {
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
                    serviceRouter.dispatchToGameActivator(gameid, request);
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

    public void setServiceRouter(ServiceRouter serviceR) {
        serviceRouter = serviceR;
    }

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {

            if (je.has("act")) {
                if (je.get("act").getAsInt() == 0) {
                    int s = 0, count_100 = 0, count_500 = 0, count_1K = 0, count_5K = 0, count_10K = 0, count_50K = 0, count_100K = 0, count_500K = 0, count_1M = 0;
                    for (int i = 0; i < listBots.size(); i++) {
                        if (listBots.get(i).getGameid() == 0) {
                            s++;
                        }
                        if (listBots.get(i).getUsertype() == BOT_TYPE1M) {
                            count_1M++;
                        } else if (listBots.get(i).getUsertype() == BOT_TYPE500K) {
                            count_500K++;
                        } else if (listBots.get(i).getUsertype() == BOT_TYPE100K) {
                            count_100K++;
                        } else if (listBots.get(i).getUsertype() == BOT_TYPE50K) {
                            count_50K++;
                        } else if (listBots.get(i).getUsertype() == BOT_TYPE10K) {
                            count_10K++;
                        } else if (listBots.get(i).getUsertype() == BOT_TYPE5K) {
                            count_5K++;
                        } else if (listBots.get(i).getUsertype() == BOT_TYPE1K) {
                            count_1K++;
                        } else if (listBots.get(i).getUsertype() == BOT_TYPE500) {
                            count_500++;
                        } else if (listBots.get(i).getUsertype() == BOT_TYPE100) {
                            count_100++;
                        }
                    }
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "checkBot");
                    jo.addProperty("Disconnected", s);
                    jo.addProperty("bot100", count_100);
                    jo.addProperty("bot500", count_500);
                    jo.addProperty("bot1K", count_1K);
                    jo.addProperty("bot5K", count_5K);
                    jo.addProperty("bot10K", count_10K);
                    jo.addProperty("bot50K", count_50K);
                    jo.addProperty("bot100K", count_100K);
                    jo.addProperty("bot500K", count_500K);
                    jo.addProperty("bot1M", count_1M);

                    ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                } else if (je.get("act").getAsInt() == 1) {
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "checkBot");
                    jo.addProperty("dic", ActionUtils.gson.toJson(dicBots));
                    jo.addProperty("list", ActionUtils.gson.toJson(listBots));

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
            listBots.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBots.get(i).getIsOnline() > 0) {
                    t++;
                }
            }

            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBots.size());
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

            int bot_500_count = (count_list_bot * bot_500_count_percent) / 100;
            int bot_1K_count = (count_list_bot * bot_1K_count_percent) / 100;
            int bot_5K_count = (count_list_bot * bot_5K_count_percent) / 100;
            int bot_10K_count = (count_list_bot * bot_10K_count_percent) / 100;
            int bot_50K_count = (count_list_bot * bot_50K_count_percent) / 100;
            int bot_100K_count = (count_list_bot * bot_100K_count_percent) / 100;
            int bot_500K_count = (count_list_bot * bot_500K_count_percent) / 100;
            int bot_1M_count = (count_list_bot * bot_1M_count_percent) / 100;
            _logger.debug(bot_500_count +"#"+bot_1K_count+"#"+bot_5K_count+"#"+bot_10K_count+"#"+bot_50K_count+"#"+bot_100K_count+"#"+bot_500K_count+"#"+bot_1M_count);
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
                if (userTemp.getUsernameLQ().length() > 0) {
                    userTemp.setUsername(userTemp.getUsernameLQ());
                }

                type++;
                if (type <= bot_1M_count) {
                    userTemp.setUsertype((short) BOT_TYPE1M);
                } else if (type <= bot_500K_count + bot_1M_count) {
                    userTemp.setUsertype((short) BOT_TYPE500K);
                } else if (type <= bot_100K_count + bot_500K_count + bot_1M_count) {
                    userTemp.setUsertype((short) BOT_TYPE100K);
                } else if (type <= (bot_50K_count + bot_100K_count + bot_500K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE50K);
                } else if (type <= (bot_10K_count + bot_50K_count + bot_100K_count + bot_500K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE10K);
                } else if (type <= (bot_5K_count + bot_10K_count + bot_50K_count + bot_100K_count + bot_500K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE5K);
                } else if (type <= (bot_1K_count + bot_5K_count + bot_10K_count + bot_50K_count + bot_100K_count + bot_500K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE1K);
                } else if (type <= (bot_500_count + bot_1K_count + bot_5K_count + bot_10K_count + bot_50K_count + bot_100K_count + bot_500K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE500);
                } else {// if (type <= (bot_100_count + bot_500_count + bot_1K_count + bot_5K_count + bot_10K_count + bot_50K_count + bot_100K_count + bot_500K_count + bot_1M_count)) {
                    userTemp.setUsertype((short) BOT_TYPE100);
                }
                // kiem tra them tien cho bot
                int agDB = rs.getInt("AG");
                long ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG((long) ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID);
                listBots.add(userTemp);
            }
            rs.close();
            cs.close();

        } catch (SQLException ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }

    }

    private void AddLogAgBot(long AG, UserInfo bot) {
        JsonObject job = new JsonObject();
        job.addProperty("uid", bot.getPid());
        job.addProperty("n", bot.getUsername());
        job.addProperty("ag", AG);
        loggerBotAddGold_.info(new Gson().toJson(job));
    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU) {
        return UpdateBotMarkChessByName(uid, ServerSource.MYA_SOURCE, name, mark);
    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, long mark) {
        return UpdateBotMarkChessByName(uid, ServerSource.MYA_SOURCE, "", mark);
    }

    @Override
    public void BotCreateTable(int gameid, int mark, int type) {
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
            synchronized (listBots) {
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getPid() == playerId) {
                        listBots.get(i).setGameid((short) 0);
                        break;
                    }
                }
                if (dicBots.containsKey(playerId)) {
                    dicBots.get(playerId).setDisconnect(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
        try {
            if (!dicBots.containsKey(pid)) return null;
            dicBots.get(pid).setRoomId((short) roomId);
            dicBots.get(pid).setTableId(tableId);
            dicBots.get(pid).setAS((long) mark);
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getPid() == pid) {
                    listBots.remove(i);
                    break;
                }
            }
            return dicBots.get(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long UpdateBotMarkByName(int uid, int source, String name, long mark) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long UpdateBotMarkByUID(int uid, long mark) {
        return UpdateBotMarkChessByName(uid, ServerSource.MYA_SOURCE, "", mark);
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
            e.printStackTrace();
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
