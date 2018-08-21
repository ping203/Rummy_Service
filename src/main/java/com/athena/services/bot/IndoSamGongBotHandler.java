package com.athena.services.bot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.constant.SamGongConstant;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.ServerSource;
import com.dst.bean.MarkBotConfig;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;


public class IndoSamGongBotHandler implements Bot {
    public static final Integer GAMEID = com.athena.services.utils.GAMEID.INDO_SAMGONG;

    public static final int Binh_Max_Add_Gold = 100;

    public static final Logger _loggerDetails = Logger.getLogger("Indo_Binh_Bot_Details");
    public final Logger loggerBotAddGold_ = Logger.getLogger("Indo_Binh_Bot_Add_Gold");
    public static final Logger _logger = Logger.getLogger(IndoSamGongBotHandler.class);

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBots
            = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBots = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;

    public IndoSamGongBotHandler(int source) {
        getListBot(source);
    }


    public final int MAX_AG_RATE = 200;

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    public void getUserGame(int mark, short gameId, int tableId, int Diamond) {
        _logger.debug("Get UserGameBot " + mark + " " + gameId + " " + tableId + " " + Diamond);
        if (Diamond != 0) return;

        synchronized (listBots) {
            try {
                short type = SamGongConstant.getMarkBotConfigByMark(mark).getType();
                //Duyệt tìm boss sẵn sàng đủ tiền chơi
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getGameid() == gameId
                            && listBots.get(i).getUsertype() == type
                            && listBots.get(i).getTableId() == 0) { // check type + ag
                        UserInfo u = listBots.get(i);
                        long addAG = 0;
                        if (u.getAG() < SamGongConstant.getMarkCreateTableByMark(mark).getAg()
                                || u.getAG() > mark * MAX_AG_RATE) { // kiem tra them tien cho bot
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
                        dicBots.put(u.getUserid(), u);

                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                        getServiceRouter().dispatchToGame(gameId, action);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                _logger.error(e.getMessage(), e);
            }
        }
    }

    public long randomBetween2Number(long least, long bound) {
        return ActionUtils.randomBetween2Number(least, bound);
    }

    public static int MIN_MULTI_AG = 20;
    public static int MAX_MULTI_AG = 150;

    public long getAGADD(short type) {
        return randomBetween2Number(SamGongConstant.getMarkBotConfigByType(type).getMark() * MIN_MULTI_AG,
                SamGongConstant.getMarkBotConfigByType(type).getMark() * MAX_MULTI_AG);
    }

    public int getVip(short type) {
        MarkBotConfig markBotConfig = SamGongConstant.getMarkBotConfigByType(type);

        return ActionUtils.randomBetween2Number(markBotConfig.getMinV(), markBotConfig.getMaxV() + 1);
    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            synchronized (dicBots) {
                if (isOnline == 0) {
                    if (dicBots.containsKey(pid) && dicBots.get(pid).getIsOnline() > 0) {
                        dicBots.get(pid).setIsOnline(isOnline);
                        dicBots.get(pid).setTableId(0);
                        dicBots.get(pid).setRoomId((short) 0);
                        if (dicBots.get(pid).isDisconnect()) dicBots.get(pid).setGameid((short) 0);
                        UserInfo u = dicBots.get(pid);
                        listBots.add(u);
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
            synchronized (listBots) {
                short type = SamGongConstant.getMarkBotConfigByMark(mark).getType();
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getGameid() == gameid
                            && listBots.get(i).getUsertype() == type
                            ) {
                        UserInfo u = listBots.get(i);

                        if (u.getAG() < SamGongConstant.getMarkCreateTableByMark(mark).getAg()
                                || u.getAG() > mark * MAX_AG_RATE) { // kiem tra them tien cho bot
                            u.setCPromot(u.getCPromot().intValue() + 1);
                            long addAG = getAGADD(type);
                            if (addAG > 0) {
                                long abDB = u.getAG();
                                u.setAG(u.getAG() + addAG);

                                ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                                loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + addAG);
                                _loggerDetails.info(ActionUtils.gson.toJson(u) + " - " + addAG);
                            }
                        } else if (u.getAG() < mark) {
                            continue; // tim bot tiep
                        }
                        listBots.get(i).setRoomId(SamGongConstant.getRoomIDbyMark(mark));
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

    public void getListBot(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();

        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID);
            ResultSet rs = cs.executeQuery();
            int sumBot = 0;
            while (rs.next()) {
                sumBot++;
            }
            rs.close();
            cs.close();

            cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID);
            rs = cs.executeQuery();
            short count = 0;
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

                count++;
                userTemp.setUsertype(SamGongConstant.getUserTypeByCountBot(sumBot, count));

                long agDB = rs.getLong("AG");
                long ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG(ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID);
                listBots.add(userTemp);
            }
            rs.close();
            cs.close();

        } catch (SQLException ex) {
            _logger.error(ex.getMessage(), ex);
        } finally {
            instance.releaseDbConnection(conn);
        }

    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, long mark) {
        return UpdateBotMarkChessByName(uid, ServerSource.MYA_SOURCE, "", mark);
    }

    @Override
    public void BotCreateTable(int gameid, int mark, int type) {
        try {
            synchronized (listBots) {
                type = SamGongConstant.getMarkBotConfigByMark(mark).getType();
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getGameid() == gameid
                            && listBots.get(i).getUsertype() == type
                            ) {
                        UserInfo u = listBots.get(i);

                        if (u.getAG() < SamGongConstant.getMarkCreateTableByMark(mark).getAg()) { // kiem tra them tien cho bot
                            u.setCPromot(u.getCPromot().intValue() + 1);
                            long addAG = getAGADD((short) type);
                            if (addAG > 0) {
                                long abDB = u.getAG();
                                u.setAG(u.getAG() + addAG);

                                ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                                loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + addAG);
                                _loggerDetails.info(ActionUtils.gson.toJson(u) + " - " + addAG);
                            }
                        } else if (u.getAG() < 20 * mark) {
                            continue; // tim bot tiep
                        }
                        listBots.get(i).setRoomId(SamGongConstant.getRoomIDbyMark(mark));
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
            }
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void BotCreateTable(int gameid, int mark) {
        try {
            synchronized (listBots) {
                short type = SamGongConstant.getMarkBotConfigByMark(mark).getType();
                for (int i = 0; i < listBots.size(); i++) {
                    if (listBots.get(i).getGameid() == gameid
                            && listBots.get(i).getUsertype() == type
                            ) {
                        UserInfo u = listBots.get(i);

                        if (u.getAG() < SamGongConstant.getMarkCreateTableByMark(mark).getAg()
                                || u.getAG() > mark * MAX_AG_RATE) { // kiem tra them tien cho bot
                            u.setCPromot(u.getCPromot().intValue() + 1);
                            long addAG = getAGADD(type);
                            if (addAG > 0) {
                                long abDB = u.getAG();
                                u.setAG(u.getAG() + addAG);

                                ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                                loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + addAG);
                                _loggerDetails.info(ActionUtils.gson.toJson(u) + " - " + addAG);
                            }
                        } else if (u.getAG() < mark) {
                            continue; // tim bot tiep
                        }
                        listBots.get(i).setRoomId(SamGongConstant.getRoomIDbyMark(mark));
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
            for (int i = 0; i < listBots.size(); i++) {
                if (listBots.get(i).getPid() == pid) {
                    listBots.remove(i);
                    break;
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
        return UpdateBotMarkChessByName(uid, ServerSource.IND_SOURCE, name, mark);
    }

    @Override
    public Long UpdateBotMarkByUID(int uid, long mark) {
        return UpdateBotMarkChessByName(uid, ServerSource.IND_SOURCE, "", mark);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark) {
        synchronized (dicBots) {
            _loggerDetails.info("       Binh=>UpdateBotMarkChessByName: Start: " + uid + "-" + mark);
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
