package com.athena.services.bot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.dst.config.myanPokdeng.PokdengConstant;
import com.dst.bean.MarkBotConfig;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import com.athena.database.ServerDefined;
import com.athena.database.UserController;
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

public class MyanmarPokdengV2BotHandler implements Bot {
    public final static Integer GAMEID = com.athena.services.utils.GAMEID.MYANMAR_SHAN_KOE_MEE_V2;

    public final int MIN_AG_RATE = 10;
    public final int MAX_AG_RATE = 200;

    private ConcurrentLinkedHashMap<Integer, UserInfo> dicBots = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    private List<UserInfo> listBots = new ArrayList<UserInfo>();
    private ServiceRouter serviceRouter;

    public final Logger _logger = Logger.getLogger(MyanmarTomCuaCaBotHandler.class);

    public MyanmarPokdengV2BotHandler(int source) {
        getListBot(source);
    }

    public void getUserGame(int mark, short gameId, int tableId, int Diamond) {

        synchronized (listBots) {
            try {
                short type = PokdengConstant.getMarkBotConfigByMark(mark).getType();

                Iterator<UserInfo> iterator = listBots.iterator();

                while (iterator.hasNext()) {
                    UserInfo u = iterator.next();

                    if (u.getGameid() == gameId &&
                            u.getUsertype() == type && u.getTableId() == 0) {
                        long addAG = 0;
                        if (u.getAG() < PokdengConstant.getMarkCreateTableByMark(mark).getAg()
                                || u.getAG() > mark * MAX_AG_RATE) {
                            addAG = getAGADD(type);
                            if (addAG > 0) {
                                int abDB = u.getAG().intValue();
                                u.setAG((long) addAG);
                                ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                            }
                        }

                        u.setIsOnline(gameId);
                        dicBots.put(u.getUserid(), u);

                        iterator.remove();

                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                        serviceRouter.dispatchToGame(gameId, action);

                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    public long getAGADD(short type) {
        return ActionUtils.randomBetween2Number(
                PokdengConstant.getMarkCreateTableByMark((int) PokdengConstant.getMarkBotConfigByType(type).getMark()).getMark() * 50
                , MAX_AG_RATE * PokdengConstant.getMarkCreateTableByMark((int) PokdengConstant.getMarkBotConfigByType(type).getMark()).getMark());
    }


    public int getVip(short type) {
        MarkBotConfig markBotConfig = PokdengConstant.getMarkBotConfigByType(type);

        return ActionUtils.randomBetween2Number(markBotConfig.getMinV(), markBotConfig.getMaxV() + 1);

    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            synchronized (listBots) {
                if (isOnline == 0) {
                    if (dicBots.containsKey(pid)) {
                        UserInfo userInfo = SerializationUtils.clone(dicBots.get(pid));
                        dicBots.remove(pid);

                        userInfo.setIsOnline(isOnline);
                        userInfo.setTableId(0);
                        if (userInfo.isDisconnect())
                            userInfo.setGameid((short) 0);

                        listBots.add(userInfo);
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
            }
        }
        return 0l;
    }

    public void BotCreateTable(int gameid, int mark) {
        try {
            synchronized (listBots) {
                short type = PokdengConstant.getMarkBotConfigByMark(mark).getType();

                Iterator<UserInfo> iterator = listBots.iterator();

                while (iterator.hasNext()) {
                    UserInfo u = iterator.next();

                    if (u.getGameid() == gameid
                            && u.getUsertype() == type && u.getTableId() == 0) {
                        if (u.getAG() < PokdengConstant.getMarkCreateTableByMark(mark).getAg()
                                || u.getAG() > MAX_AG_RATE * mark ) { // kiem tra them tien cho bot
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

                        u.setIsOnline((short) gameid);

                        dicBots.put(u.getUserid(), u);
                        iterator.remove();

                        JsonObject send = new JsonObject();
                        send.addProperty("evt", "botCreateTable");
                        send.addProperty("pid", u.getPid());
                        send.addProperty("M", mark);
                        ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                        serviceRouter.dispatchToGameActivator(gameid, request);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    public void botRejectJoinTable(int pid) {
        synchronized (listBots){
            try{
                if(dicBots.containsKey(pid)){
                    UserInfo userInfo1 = SerializationUtils.clone(dicBots.get(pid));
                    dicBots.remove(pid);

                    userInfo1.setIsOnline((short)0);
                    userInfo1.setTableId(0);
                    listBots.add(userInfo1);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
            }
        }
    }

    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        try {
            synchronized (listBots){
                short type = PokdengConstant.getMarkBotConfigByMark(mark).getType();

                Iterator<UserInfo> iterator = listBots.iterator();

                while (iterator.hasNext()){
                    UserInfo u = iterator.next();

                    if(u.getGameid() == gameid
                            && u.getUsertype() == type && u.getTableId() == 0){
                        if (u.getAG() < PokdengConstant.getMarkCreateTableByMark(mark).getAg() ||
                                u.getAG() > MAX_AG_RATE * PokdengConstant.getMarkCreateTableByMark(mark).getMark()) { // kiem tra them tien cho bot
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

                        u.setRoomId((short) roomID);
                        u.setIsOnline((short) gameid);

                        dicBots.put(u.getUserid(), u);
                        iterator.remove();

                        JsonObject send = new JsonObject();
                        send.addProperty("evt", "botCreateTable");
                        send.addProperty("pid", u.getPid());
                        send.addProperty("M", mark);
                        ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                        serviceRouter.dispatchToGameActivator(gameid, request);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }finally {
        }
    }

    public ServiceRouter getServiceRouter() {
        return serviceRouter;
    }

    public void setServiceRouter(ServiceRouter serviceR) {
        serviceRouter = serviceR;
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
                if (userTemp.getUsernameLQ().length() > 0) {
                    userTemp.setUsername(userTemp.getUsernameLQ());
                }

                count++;
                userTemp.setUsertype(PokdengConstant.getUserTypeByCountBot(sumBot, count));

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
