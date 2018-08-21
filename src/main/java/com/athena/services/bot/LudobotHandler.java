package com.athena.services.bot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.vng.tfa.common.SqlService;

public class LudobotHandler extends BaseBotHandler{

    public static final int Mark1  = 10;
    public static final int Mark2  = 100;
    public static final int Mark3  = 500;
    public static final int Mark4  = 1000;
    public static final int Mark5  = 5000;
    public static final int Mark6  = 10000;
    public static final int Mark7  = 50000;
    
    private int Source = 0;
    public LudobotHandler(int source, int gameId){
        GAMEID = gameId;
        MIN_AG_RATE = 10;
        MAX_AG_RATE = 200;
        MAX_ADD_AG = 50;

        Source = source;

        listMarkBot.add(new ConfigMarkBot(Mark1, 11, (float) 0.15));
        listMarkBot.add(new ConfigMarkBot(Mark2, 12, (float) 0.15));
        listMarkBot.add(new ConfigMarkBot(Mark3, 13, (float) 0.15));
        listMarkBot.add(new ConfigMarkBot(Mark4, 14, (float) 0.1));
        listMarkBot.add(new ConfigMarkBot(Mark5, 15, (float) 0.1));
        listMarkBot.add(new ConfigMarkBot(Mark6, 16, (float) 0.15));
        listMarkBot.add(new ConfigMarkBot(Mark7, 17, (float) 0.2));

        getListBot(source);
    }

    @Override
    public void getListBot(int source) {

        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try{
            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID);
            ResultSet rs = cs.executeQuery();
            int count_list_bot = 0;
            listBot.clear();
            while (rs.next()) {
                count_list_bot++;
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
                userTemp.setPid(userTemp.getUserid());
                if (userTemp.getUsernameLQ().length() > 0) {
                    userTemp.setUsername(userTemp.getUsernameLQ());
                }
                userTemp.setAG(rs.getLong("AG"));
                listBot.add(userTemp);
            }
            for (ConfigMarkBot cmb : listMarkBot) {
                cmb.reset();
                cmb.setCount((int) (count_list_bot * cmb.getCountPercent()));
            }

            for(UserInfo userTemp : listBot){
                Long agDB = userTemp.getAG();
                for (ConfigMarkBot cmb : listMarkBot) {

                    if (cmb.getCount() > cmb.getCountPlus()) {
                        userTemp.setUsertype((short) cmb.getType());
                        cmb.setCountPlus(cmb.getCountPlus() + 1);
                        break;
                    }
                }
                int ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG((long) ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
//                userTemp.setGameid((short)GAMEID);
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID);
            }
            rs.close();
            cs.close();

        } catch (Exception ex){
//            System.out.println("==>Error==>GetListBot:" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    @Override
    public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        loggerBot_.info("===>get User Game");
        loggerBot_.info("===>getUserGame: Start");
        loggerBot_.info("Base BOT HANDLER===> getUserGame: Start");
        if (Diamond != 0) {
            return;
        }
        synchronized (listBot) {
            try {
                int type = getTypeByMark(minAG);
                loggerBot_.info("==> type "+type + " GAMEID "+gameId);
                List<UserInfo> listBotWithType = new ArrayList<UserInfo>();
                for (int i = 0; i < listBot.size(); i++) {
                    if (listBot.get(i).getGameid() == gameId && listBot.get(i).getUsertype() == type && listBot.get(i).getTableId() == 0) {
                        listBotWithType.add(listBot.get(i));
                    }
                }
                loggerBot_.info("==> listBot with type size: "+listBotWithType.size());
                if (listBotWithType.size() == 0) {
                    return;
                }

                int index = new Random().nextInt(listBotWithType.size());
                UserInfo u = listBotWithType.get(index);
                loggerBot_.info("user BOT infor: "+ u.getUsername()+" AG "+ u.getAG()+" pId "+ u.getPid() + " userId "+u.getUserid() + " gameID: "+ u.getGameid());
                int addAG = 0;
                loggerBot_.info("min AG: "+minAG * getRate(minAG));
                loggerBot_.info("CPromot: "+u.getCPromot() + " max ADD AG "+MAX_ADD_AG);
                if (u.getAG() < minAG * getRate(minAG) && u.getCPromot() < MAX_ADD_AG) {
                    u.setCPromot(u.getCPromot().intValue() + 1);
                    addAG = getAGADD(type);
                }
                loggerBot_.info("add ag" + addAG);
                if (addAG > 0) {
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

        return -1;
    }

    @Override
    public UserInfo botLogin(int gameid) {
        // TODO Auto-generated method stub
        return super.botLogin(gameid);
    }

    @Override
    public UserInfo updateBotOnline(int pid, short isOnline) {
        try {
            loggerBot_.info("==> update bot online: "+ pid+"-"+isOnline);
            synchronized (listBot) {
                if (isOnline == 0) {
                    if (dicBot.containsKey(pid) && dicBot.get(pid).getIsOnline() > 0) {
                        dicBot.get(pid).setIsOnline(isOnline);
                        dicBot.get(pid).setTableId(0);
                        dicBot.get(pid).setRoomId((short) 0);
                        loggerBot_.info("uinfor: "+ dicBot.get(pid).getUsername() +", gameID: "+ dicBot.get(pid).getGameid() +", isDisconnect: ");
                        if (dicBot.get(pid).isDisconnect()) {
                            dicBot.get(pid).setGameid((short) 0);
                        }
                        UserInfo u = dicBot.get(pid);
                        loggerBot_.info("uInfor: "+ u.getUsername() + ", gameID:" + u.getGameid() + " roomID: "+ u.getRoomId());
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

    @Override
    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        // TODO Auto-generated method stub
        super.BotCreateTableForRoom(gameid, mark, roomID);
    }

    @Override
    public Long UpdateBotMarkByUID(int uid, long mark) {
        synchronized (dicBot) {
            if (dicBot.containsKey(uid)) {
                try {
                    long ag = dicBot.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBot.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", Source, uid - ServerDefined.userMap.get(Source), mark);
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

    @Override
    public int getRate(int minAg) {
        int rateMark = 1;
        if (minAg <= 5000) {
            rateMark = 50;
        } else if (minAg == 10000 || minAg == 50000) {
            rateMark = 100;
        } else if (minAg >= 100000) {
            rateMark = 200;
        }

        return rateMark;
    }

    @Override
    public int getVip(short type) {
        int vip = 1;
        int VIPtype = 0;
        for (ConfigMarkBot cmb : listMarkBot) {
            switch (cmb.getMark()) {
                case 100000:
                    VIPtype = cmb.getType();
                    break;
                case 50000:
                    VIPtype = cmb.getType();
                    break;
                case 10000:
                    VIPtype = cmb.getType();
                    break;
                case 500:
                    VIPtype = cmb.getType();
                    break;
                case 100:
                    VIPtype = cmb.getType();
                    break;
                default:
                    break;
            }
        }

        if (type >= VIPtype) {
            vip = (new Random()).nextInt(5) + 2;
        } else if (type == VIPtype) {
            vip = (new Random()).nextInt(4) + 2;
        } else if (type <= VIPtype) {
            vip = (new Random()).nextInt(3) + 1;
        } else if (type == VIPtype){
            vip = (new Random()).nextInt(3);
        } else if (type == VIPtype){
            vip = (new Random()).nextInt(2);
        }
        return vip;
    }

    @Override
    public void BotCreateTable(int gameid, int mark) {
        // TODO Auto-generated method stub
        super.BotCreateTable(gameid, mark);
    }

}
