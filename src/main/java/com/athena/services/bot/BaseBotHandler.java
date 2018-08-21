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
import com.athena.services.utils.ActionUtils;
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

public class BaseBotHandler implements Bot {

    public int MIN_AG_RATE = 10;
    public int MAX_AG_RATE = 200;
    public int MAX_ADD_AG = 50;

    public int GAMEID = 0;
    public List<ConfigMarkBot> listMarkBot = new ArrayList<ConfigMarkBot>();

    public ConcurrentLinkedHashMap<Integer, UserInfo> dicBot = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    public List<UserInfo> listBot = new ArrayList<UserInfo>();
    public ServiceRouter serviceRouter;
    public Gson gson = new Gson();
    public Logger loggerBot_ = Logger.getLogger(LoggerKey.BASE_BOT);

    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        loggerBot_.info("===>getUserGame: Start");
        if (Diamond != 0) {
            return;
        }
        synchronized (listBot) {
            try {
                int type = getTypeByMark(minAG);

                List<UserInfo> listBotWithType = new ArrayList<UserInfo>();
                for (int i = 0; i < listBot.size(); i++) {
                    if (listBot.get(i).getGameid() == gameId && listBot.get(i).getUsertype() == type && listBot.get(i).getTableId() == 0) {
                        listBotWithType.add(listBot.get(i));
                    }
                }
                if (listBotWithType.size() == 0) {
                    return;
                }

                int index = new Random().nextInt(listBotWithType.size());
                UserInfo u = listBot.get(index);

                int addAG = 0;
                if (u.getAG() < minAG * getRate(minAG) && u.getCPromot() < MAX_ADD_AG) {
                    u.setCPromot(u.getCPromot().intValue() + 1);
                    addAG = getAGADD(type);
                }

                if (addAG > 0) {
                    AddLogAgBot(addAG, u);
                    int abDB = u.getAG().intValue();
                    u.setAG(u.getAG() + addAG);
                    ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                }
                u.setIsOnline(gameId);
                dicBot.put(u.getUserid(), u);
                JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                serviceRouter.dispatchToGame(gameId, action);
            } catch (Exception e) {
                e.printStackTrace();
                loggerBot_.info("===>ERROR=>BaseBotHandler=>updateBotOnline: " + e.getMessage());
            }
        }
    }

    public int getTypeByMark(int minAG) {
        for (int i = listMarkBot.size() - 1; i >= 0; i--) {
            if (minAG >= listMarkBot.get(i).getMark()) {
                return listMarkBot.get(i).getType();
            }
        }

        return listMarkBot.get(0).getType();
    }

    public int getAGADD(int type) {
        int addAG = 0;
        for (ConfigMarkBot cmb : listMarkBot) {
            if (type == cmb.getType()) {
                addAG = randomBetween2Number(getRate(cmb.getMark()) * cmb.getMark(), MAX_AG_RATE * cmb.getMark());
            }
        }

        return addAG;
    }

    public int randomBetween2Number(int lowerBound, int upperBound) {
        return ActionUtils.random.nextInt(upperBound - lowerBound) + lowerBound;
    }

    public int getVip(short type) {
        int vip = 1;
        int type100K = 0, type50K = 0, type10K = 0;
        for (ConfigMarkBot cmb : listMarkBot) {
            if (cmb.getMark() == 100000) {
                type100K = cmb.getType();
            } else if (cmb.getMark() == 50000) {
                type50K = cmb.getType();
            } else if (cmb.getMark() == 10000) {
                type10K = cmb.getType();
            }
        }

        if (type >= type100K) {
            vip = (new Random()).nextInt(5) + 2;
        } else if (type == type50K) {
            vip = (new Random()).nextInt(4) + 2;
        } else if (type <= type10K) {
            vip = (new Random()).nextInt(3) + 1;
        }

        return vip;
    }

    public int getRate(int minAg) {
        int rateMark = 1;
        if (minAg <= 5000) {
            rateMark = 5;
        } else if (minAg == 10000 || minAg == 50000) {
            rateMark = 10;
        } else if (minAg >= 100000) {
            rateMark = 20;
        }

        return rateMark;
    }

    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            synchronized (listBot) {
                if (isOnline == 0) {
                    if (dicBot.containsKey(pid) && dicBot.get(pid).getIsOnline() > 0) {
                        dicBot.get(pid).setIsOnline(isOnline);
                        dicBot.get(pid).setTableId(0);
                        if (dicBot.get(pid).isDisconnect()) {
                            dicBot.get(pid).setGameid((short) 0);
                        }
                        UserInfo u = dicBot.get(pid);
                        listBot.add(u);
                    }
                } else {
                    dicBot.get(pid).setIsOnline(isOnline);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dicBot.get(pid);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark) {
        synchronized (dicBot) {
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
                    e.printStackTrace();
                    return 0l;
                }
            }
        }
        return 0l;
    }

    public void BotCreateTable(int gameid, int mark) {
        try {
            loggerBot_.info("===>BotCreateTable: Start");

            int type = getTypeByMark(mark);

            int countType = 0;
            for (int i = 0; i < listBot.size(); i++) {
                if (listBot.get(i).getGameid() == gameid && listBot.get(i).getUsertype() == type)
                    countType++;
            }
            loggerBot_.info("===>BotCreateTable: count type: " + type + " gameid: " + gameid + " have left " + countType);

            boolean check = true;
            for (int i = 0; i < listBot.size(); i++) {
                if (listBot.get(i).getGameid() == gameid && listBot.get(i).getUsertype() == type && listBot.get(i).getRoomId() == 0) {
                    check = false;
                    UserInfo u = listBot.get(i);
                    if (u.getAG() < 10 * mark && u.getCPromot() < MAX_ADD_AG) {
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            u.setAG(u.getAG() + addAG);
                            UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(), u.getUserid() - ServerDefined.userMap.get((int) u.getSource()), addAG);
                            QueueManager.getInstance(UserController.queuename).put(cmd);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID);
                        }
                    } else if (u.getAG() < mark) {
                        continue; // tim bot tiep
                    }
                    listBot.get(i).setRoomId((short) ((new Random()).nextInt(2) + 1));
                    u.setIsOnline((short) gameid);
                    dicBot.put(u.getUserid(), u);

                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "botCreateTable");
                    send.addProperty("pid", u.getPid());
                    send.addProperty("M", mark);
                    ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                    serviceRouter.dispatchToGameActivator(gameid, request);
                    break;
                }
            }
            if (check) {
                for (int i = 0; i < listBot.size(); i++) {
                    if (listBot.get(i).getUsertype() == type) {
                        loggerBot_.info("===>BotCreateTable: failed listBotNew size: " + listBot.size() + " - gameid: " + gameid + " - mark: " + mark);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loggerBot_.info("===>ERROR===>BotCreateTable: " + e.getMessage());
        }
    }

    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        // Auto-generated method stub
        try {
            if (mark > 0)
                return;

            int type = getTypeByMark(mark);
            for (int i = 0; i < listBot.size(); i++) {
                if (listBot.get(i).getGameid() == gameid && listBot.get(i).getUsertype() == type && listBot.get(i).getRoomId() == 0) {
                    UserInfo u = listBot.get(i);
                    if (u.getAG() < mark * getRate(mark) && u.getCPromot() < MAX_ADD_AG) { // kiem
                                                                                           // tra
                                                                                           // them
                                                                                           // tien
                                                                                           // cho
                                                                                           // bot
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID);
                        }
                    } else if (u.getAG() < mark * getRate(mark)) {
                        continue; // tim bot tiep
                    }
                    listBot.get(i).setRoomId((short) roomID);
                    u.setIsOnline((short) gameid);
                    dicBot.put(u.getUserid(), u);

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

    public void checkBot(JsonObject je, UserInfo actionUser) {
        try {
            if (je.has("act")) {
                if (je.get("act").getAsInt() == 0) {
                    int s = 0;
                    for (int i = 0; i < listBot.size(); i++) {
                        if (listBot.get(i).getGameid() == 0) {
                            s++;
                        }
                    }
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "checkBot");
                    jo.addProperty("Disconnected", s);
                    for (ConfigMarkBot cmb : listMarkBot) {
                        jo.addProperty("bot" + cmb.getMark(), getCountBotWithType(cmb.getType()));
                    }

                    ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                } else if (je.get("act").getAsInt() == 1) {
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "checkBot");
                    jo.addProperty("dic", ActionUtils.gson.toJson(dicBot));
                    jo.addProperty("list", ActionUtils.gson.toJson(listBot));

                    ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCountBotWithType(int type) {
        int count = 0;
        for (UserInfo u : listBot) {
            if (u.getUsertype() == type) {
                count++;
            }
        }

        return count;
    }

    public void reloadBot(JsonObject je, UserInfo actionUser) {
        try {
            listBot.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBot.size(); i++) {
                if (listBot.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBot.get(i).getIsOnline() > 0) {
                    t++;
                }
            }

            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBot.size());
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

            for (ConfigMarkBot cmb : listMarkBot) {
                cmb.reset();
                cmb.setCount((int) (count_list_bot * cmb.getCountPercent()));
            }

            listBot.clear();
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

                for (ConfigMarkBot cmb : listMarkBot) {
                    if (cmb.getCount() > cmb.getCountPlus()) {
                        userTemp.setUsertype((short) cmb.getType());
                        cmb.setCountPlus(cmb.getCountPlus() + 1);
                        break;
                    }
                }

                // kiem tra them tien cho bot
                int agDB = rs.getInt("AG");
                int ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG((long) ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID);
                listBot.add(userTemp);
            }
            rs.close();
            cs.close();

            loggerBot_.info("===>getListBot: ==>Size listBot:" + listBot.size());
        } catch (SQLException ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }

    }

    private void AddLogAgBot(int AG, UserInfo bot) {
        JsonObject job = new JsonObject();
        job.addProperty("uid", bot.getPid());
        job.addProperty("n", bot.getUsername());
        job.addProperty("ag", AG);
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
        synchronized (listBot) {
            for (int i = 0; i < listBot.size(); i++) {
                if (listBot.get(i).getGameid() == 0) {
                    listBot.get(i).setGameid((short) gameid);
                    return listBot.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public void processBotDisconnect(int playerId) {
        try {
            synchronized (listBot) {
                for (int i = 0; i < listBot.size(); i++) {
                    if (listBot.get(i).getPid() == playerId) {
                        listBot.get(i).setGameid((short) 0);
                        break;
                    }
                }
                if (dicBot.containsKey(playerId)) {
                    dicBot.get(playerId).setDisconnect(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
        try {
        	if(!dicBot.containsKey(pid)) return null;
            dicBot.get(pid).setRoomId((short) roomId);
            dicBot.get(pid).setTableId(tableId);
            dicBot.get(pid).setAS((long) mark);
            for (int i = 0; i < listBot.size(); i++) {
                if (listBot.get(i).getPid() == pid) {
                    listBot.remove(i);
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
            synchronized (dicBot) {
                if (dicBot.containsKey(pid)) {
                    if (tid > 0) {
                        dicBot.get(pid).setTableId(tid);
                    }
                    return ActionUtils.gson.toJson(dicBot.get(pid).getUserGame());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
        return dicBot;
    }

	@Override
	public List<UserInfo> getListBot() {
		return listBot;
	}


}
