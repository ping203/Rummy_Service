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
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.GAMEID;
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

public class PokdengBotHandler implements Bot{

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
    
    public static final int Mark1  = 100;
    public static final int Mark2  = 500;
    public static final int Mark3  = 1000;
    public static final int Mark4  = 5000;
    public static final int Mark5  = 10000;
    public static final int Mark6  = 50000;
    public static final int Mark7  = 100000;
    public static final int Mark8  = 500000;
    public static final int Mark9  = 1000000;
    public static final int Mark10 = 5000000;

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
    public final int BOT_100_COUNT = 100;
    public final int BOT_500_COUNT = 75;
    public final int BOT_1K_COUNT = 75;
    public final int BOT_5K_COUNT = 50;
    public final int BOT_10K_COUNT = 60;
    public final int BOT_50K_COUNT = 50;
    public final int BOT_100K_COUNT = 50;
    public final int BOT_500K_COUNT = 20;
    public final int BOT_1M_COUNT = 20;
    public final int bot_100_count_percent = 20;
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
//    public final int MAX_ADD_AG = 50;

    private ConcurrentLinkedHashMap<Integer, UserInfo> dicBotPokdeng = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
    private List<UserInfo> listBotPokdeng = new ArrayList<UserInfo>();
    private ServiceRouter serviceRouter;
//    public final ConcurrentLinkedHashMap<Integer, Integer> lsMinAg = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 8);
    public final Logger loggerBotAddGold_ = Logger.getLogger("PokdengBot_Add_Gold");
    public final Logger loggerBot_ = Logger.getLogger("PokdengBot");

    public PokdengBotHandler(int source) {
		getListBot(source);
	}

	public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        loggerBot_.info("       PokdengBotHandler=>getUserGame: Start");
        if (Diamond != 0) {
            return;
        }
//        if (minAG > BOT_MARK_500K) {
//            return;
//        }
        synchronized (listBotPokdeng) {
            try {
                short type = getTypeByMark(minAG);
                loggerBot_.info("===>getUserGame: type: " + type);

                List<UserInfo> listBotWithType = new ArrayList<UserInfo>();
                for (int i = 0; i < listBotPokdeng.size(); i++) {
                    if (listBotPokdeng.get(i).getGameid() == gameId
                            && listBotPokdeng.get(i).getUsertype() == type
                            && listBotPokdeng.get(i).getTableId() == 0) {
                        listBotWithType.add(listBotPokdeng.get(i));
                    }
                }
                if (listBotWithType.size() == 0) {
                    return;
                }
                                
                int index = new Random().nextInt(listBotWithType.size());
                UserInfo u = listBotPokdeng.get(index);
                
                int addAG = 0;                
                if (u.getAG() < minAG * getRate(minAG) || u.getAG() > minAG * MAX_AG_RATE/* && u.getCPromot() < MAX_ADD_AG */) {
                    u.setCPromot(u.getCPromot().intValue() + 1);
                    addAG = getAGADD(type);
                    if (addAG > 0) {
                        AddLogAgBot(addAG, u);
                        int abDB = u.getAG().intValue();
                        u.setAG((long) addAG);
                        ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.POKDENGNEW);
                        loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + minAG + " - " + addAG);
                        loggerBot_.info(ActionUtils.gson.toJson(u) + " - " + minAG + " - " + addAG);
                    }
                }
                u.setIsOnline(gameId);
                dicBotPokdeng.put(u.getUserid(), u);
                loggerBot_.info("===>getUserGame: Bot size "+ dicBotPokdeng.size()  +"-uid="+ u.getUserid() );
                loggerBot_.info("===>getUserGame: name: " + u.getUsername());
                JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
                serviceRouter.dispatchToGame(gameId, action);

//                for (int i = 0; i < listBotPokdeng.size(); i++) {
//                    if (listBotPokdeng.get(i).getGameid() == gameId && listBotPokdeng.get(i).getUsertype() == type) {
//                        loggerBot_.info("===>getUserGame: name: " + listBotPokdeng.get(i).getUsername());
//                        UserInfo u = listBotPokdeng.get(i);
//                        int addAG = 0;
//                        if (u.getTableId() != 0)
//                            continue;
//                        
//                        if (u.getAG() < minAG * getRate(minAG) && u.getCPromot() < MAX_ADD_AG) {
//                            u.setCPromot(u.getCPromot().intValue() + 1);
//                            addAG = getAGADD(type);
//                        }
//                        loggerBot_.info("===>getUserGame: check addAG: " + u.getAG());
//
//                        if (addAG > 0) {
//                            AddLogAgBot(addAG, u);
//                            int abDB = u.getAG().intValue();
//                            u.setAG(u.getAG() + addAG);
//                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.POKDENGNEW);
//                            loggerBotAddGold_.info(ActionUtils.gson.toJson(u) + " - " + minAG + " - " + addAG);
//                            loggerBot_.info(ActionUtils.gson.toJson(u) + " - " + minAG + " - " + addAG);
//                        }
//                        u.setIsOnline(gameId);
//                        dicBotPokdeng.put(u.getUserid(), u);
//                        loggerBot_.info("===>getUserGame: Bot size "+ dicBotPokdeng.size()  +"-uid="+ u.getUserid() );
//                        loggerBot_.info("===>getUserGame: name: " + u.getUsername());
//                        JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
//                        serviceRouter.dispatchToGame(gameId, action);
//                        break;
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
                loggerBot_.info("===>ERROR=>PokdengBotHandler=>updateBotOnline: " + e.getMessage());
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

    public int getAGADD(short type) {
//      Mức cược bàn        Nhóm        Số bot tiếp khách       MIN         MAX             Note    
//      100                 1           60                      5,000       20,000          
//      500                 1           50                      25,000      100,000         
//      1,000               2           50                      50,000      150,000     
//      5,000               2           40                      250,000     500,000     
//      10,000              2           30                      500,000     2,000,000       
//      50,000              3           25                      2,500,000   5,000,000       
//      100,000             3           20                      5,000,000   20,000,000      
//      500,000             3           15                      25,000,000  100,000,000   
//      1000,000            3           10                      50,000,000  200,000,000 

        int addAG = 0;
        switch (type) {
            case BOT_TYPE1M:
                addAG = randomBetween2Number(getRate(BOT_MARK_1M) * BOT_MARK_1M, MAX_AG_RATE * BOT_MARK_1M);
                break;
            case BOT_TYPE500K:
                addAG = randomBetween2Number(getRate(BOT_MARK_500K) * BOT_MARK_500K, MAX_AG_RATE * BOT_MARK_500K);
                break;
            case BOT_TYPE100K:
                addAG = randomBetween2Number(getRate(BOT_MARK_100K) * BOT_MARK_100K, MAX_AG_RATE * BOT_MARK_100K);
                break;
            case BOT_TYPE50K:
                addAG = randomBetween2Number(getRate(BOT_MARK_50K) * BOT_MARK_50K, MAX_AG_RATE * BOT_MARK_50K);
                break;
            case BOT_TYPE10K:
                addAG = randomBetween2Number(getRate(BOT_MARK_10K) * BOT_MARK_10K, MAX_AG_RATE * BOT_MARK_10K);
                break;
            case BOT_TYPE5K:
                addAG = randomBetween2Number(getRate(BOT_MARK_5K) * BOT_MARK_5K, MAX_AG_RATE * BOT_MARK_5K);
                break;
            case BOT_TYPE1K:
                addAG = randomBetween2Number(getRate(BOT_MARK_1K) * BOT_MARK_1K, MAX_AG_RATE * BOT_MARK_1K);
                break;
            case BOT_TYPE500:
                addAG = randomBetween2Number(getRate(BOT_MARK_500) * BOT_MARK_500, MAX_AG_RATE * BOT_MARK_500);
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
    
    public int getRate(int minAg) {
        int rateMark = 1;
        switch (minAg) {
            case BOT_MARK_100:
            case BOT_MARK_500:
            case BOT_MARK_1K:
            case BOT_MARK_5K:
                rateMark = 5;
                break;
            case BOT_MARK_10K:
            case BOT_MARK_50K:
                rateMark = 10;
                break;
            case BOT_MARK_100K:
            case BOT_MARK_500K:
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
            synchronized (listBotPokdeng) {
                loggerBot_.info("       PokdengBotHandler=>updateBotOnline: Start");
                if (isOnline == 0) {

                    // System.out.println("==>BotHandler==>updateBotOnline:before " +
                    // ServiceImpl.listBot.size());
                    if (dicBotPokdeng.containsKey(pid) && dicBotPokdeng.get(pid).getIsOnline() > 0) {
                        dicBotPokdeng.get(pid).setIsOnline(isOnline);
                        dicBotPokdeng.get(pid).setTableId(0);
                        if (dicBotPokdeng.get(pid).isDisconnect())
                            dicBotPokdeng.get(pid).setGameid((short) 0);
                        UserInfo u = dicBotPokdeng.get(pid);
                        listBotPokdeng.add(u);
//                        dicBotPokdeng.get(pid).setRoomId((short) 0);
                    }
                } else {
                    dicBotPokdeng.get(pid).setIsOnline(isOnline);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loggerBot_.info("===>ERROR=>PokdengBotHandler=>updateBotOnline: " + e.getMessage());
        }
        return dicBotPokdeng.get(pid);
    }

    public Long UpdateBotMarkChessByName(int uid, int source, String names, long mark) {
        synchronized (dicBotPokdeng) {
            loggerBot_.info("       PokdengBotHandler=>UpdateBotMarkChessByName: Start: " + uid+"-"+mark);
//             System.out.println("UpdateBotMarkChessByName:" + uid+"-"+name+"-"+mark);
            if (dicBotPokdeng.containsKey(uid)) {
//                System.out.println("ag:" +dicBotPokdeng.get(uid).getAG());
                try {
                    long ag = dicBotPokdeng.get(uid).getAG() + mark;
                    if (ag < 0) {
                        ag = 0;
                    }
                    dicBotPokdeng.get(uid).setAG(ag);
                    UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid - ServerDefined.userMap.get(source), mark);
                    QueueManager.getInstance(UserController.queuename).put(cmd);
                    return dicBotPokdeng.get(uid).getAG();
                } catch (Exception e) {
                    e.printStackTrace();
                    // System.out.println("==>Error==>botHandler==>UpdateBotMarkChessByName:" +
                    // e.getMessage() + "-" + uid+"-" + name + "-" + mark);
                    loggerBot_.info("===>ERROR=>UpdateBotMarkChessByName: " + e.getMessage());
                    return 0l;
                }
            } else {
                loggerBot_.info("==>PokdengBotHandler=>UpdateBotMarkChessByName: !dicBotNew.containsKey(name)" + uid + "-" + mark);
            }
        }
        return 0l;
    }


    public void BotCreateTable(int gameid, int mark) {
        try {
            loggerBot_.info("       PokdengBotHandler=>BotCreateTable: Start");

            short type = getTypeByMark(mark);
            int countType = 0;
            for (int i = 0; i < listBotPokdeng.size(); i++) {
                if (listBotPokdeng.get(i).getGameid() == gameid && listBotPokdeng.get(i).getUsertype() == type)
                    countType++;
            }
            loggerBot_.info("Pokdeng count type: " + type + " gameid: " + gameid + " have left " + countType);

            boolean check = true;
            for (int i = 0; i < listBotPokdeng.size(); i++) {
                if (listBotPokdeng.get(i).getGameid() == gameid && listBotPokdeng.get(i).getUsertype() == type/* && listBotPokdeng.get(i).getRoomId() == 0*/) {
                    check = false;
                    UserInfo u = listBotPokdeng.get(i);
                    if (u.getAG() < mark * getRate(mark) || u.getAG() > mark * MAX_AG_RATE/* && u.getCPromot() < MAX_ADD_AG*/) {
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            u.setAG((long)addAG);
                            UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(), u.getUserid() - ServerDefined.userMap.get((int) u.getSource()), addAG);
                            QueueManager.getInstance(UserController.queuename).put(cmd);
                            loggerBot_.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.POKDENGNEW);
                        }
                    } else if (u.getAG() < mark) {
                        continue; // tim bot tiep
                    }
                    listBotPokdeng.get(i).setRoomId((short) ((new Random()).nextInt(2) + 1));
                    u.setIsOnline((short) gameid);
                    dicBotPokdeng.put(u.getUserid(), u);

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
                for (int i = 0; i < listBotPokdeng.size(); i++) {
                    if (listBotPokdeng.get(i).getUsertype() == type) {
                        loggerBot_.info("==>BotCreateTable: failed listBotNew size: " + listBotPokdeng.size() + " - gameid: " + gameid + " - mark: " + mark);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loggerBot_.info("===>ERROR=>PokdengBotHandler=>BotCreateTable: " + e.getMessage());
        }
    }
    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        // Auto-generated method stub
        try {
            if (mark > 0)
                return;

            short type = getTypeByMark(mark);
            for (int i = 0; i < listBotPokdeng.size(); i++) {
                if (listBotPokdeng.get(i).getGameid() == gameid && listBotPokdeng.get(i).getUsertype() == type && listBotPokdeng.get(i).getRoomId() == 0) {
                    UserInfo u = listBotPokdeng.get(i);
                    if (u.getAG() < mark * getRate(mark)/* && u.getCPromot() < MAX_ADD_AG*/) { // kiem tra them tien cho bot
                        u.setCPromot(u.getCPromot().intValue() + 1);
                        int addAG = getAGADD(type);
                        if (addAG > 0) {
                            int abDB = u.getAG().intValue();
                            u.setAG(u.getAG() + addAG);
                            Logger.getLogger("Bot_Details").info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
                            ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.POKDENGNEW);
                            ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.POKDENGNEW);
                        }
                    } else if (u.getAG() < mark * getRate(mark)) {
                        continue; // tim bot tiep
                    }
                    listBotPokdeng.get(i).setRoomId((short) roomID);
                    u.setIsOnline((short) gameid);
                    dicBotPokdeng.put(u.getUserid(), u);

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
            loggerBot_.info("       PokdengBotHandler=>checkBot: Start");

            if (je.has("act")) {
                if (je.get("act").getAsInt() == 0) {
                    int s = 0, count_100 = 0, count_500 = 0, count_1K = 0, count_5K = 0, count_10K = 0, count_50K = 0, count_100K = 0, count_500K = 0, count_1M = 0;
//                    List<String> lsBot = new ArrayList<String>();
                    for (int i = 0; i < listBotPokdeng.size(); i++) {
                        if (listBotPokdeng.get(i).getGameid() == 0) {
                            s++;
                        }
                        if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE1M) {
                            count_1M++;
                        } else if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE500K) {
                            count_500K++;
                        } else if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE100K) {
                            count_100K++;
                        } else if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE50K) {
                            count_50K++;
                        } else if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE10K) {
                            count_10K++;
                        } else if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE5K) {
                            count_5K++;
                        } else if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE1K) {
                            count_1K++;
                        } else if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE500) {
                            count_500++;
                        } else if (listBotPokdeng.get(i).getUsertype() == BOT_TYPE100) {
                            count_100++;
                        }
//                        if (je.has("T") && je.get("T").getAsInt() == listBotPokdeng.get(i).getUsertype()) {
//                            lsBot.add(listBotPokdeng.get(i).getUsername());
//                        }
                    }
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "checkBot");
                    jo.addProperty("Disconnected", s);
//                    jo.addProperty("IsOnline", "Size: " + listBotPokdeng.size() + " - " + count_1M + "/" + count_500K + "/" + count_100K + "/" + count_50K + "/" + count_10K + "/" + count_5K + "/" + count_1K + "/" + count_500 + "/" + count_100);
//                    jo.addProperty("Bot", ActionUtils.gson.toJson(lsBot));
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
                    jo.addProperty("dic", ActionUtils.gson.toJson(dicBotPokdeng));
                    jo.addProperty("list", ActionUtils.gson.toJson(listBotPokdeng));
                    
                    ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loggerBot_.info("===>ERROR=>PokdengBotHandler=>checkBot: " + e.getMessage());
        }
    }

    public void reloadBot(JsonObject je, UserInfo actionUser) {
        try {
            loggerBot_.info("       PokdengBotHandler=>reloadBot: Start");
            listBotPokdeng.clear();
            getListBot(actionUser.getSource());
            int s = 0, t = 0;
            for (int i = 0; i < listBotPokdeng.size(); i++) {
                if (listBotPokdeng.get(i).getGameid() == 0) {
                    s++;
                }
                if (listBotPokdeng.get(i).getIsOnline() > 0) {
                    t++;
                }
            }

            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "reloadBot");
            jo.addProperty("Disconnected", s);
            jo.addProperty("IsOnline", t + "/" + listBotPokdeng.size());
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
            loggerBot_.info("===>ERROR=>PokdengBotHandler=>reloadBot: " + e.getMessage());
        }

    }

    public void getListBot(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID.POKDENGNEW);
            ResultSet rs = cs.executeQuery();
            int count_list_bot = 0;
            while (rs.next()) {
                count_list_bot++;
            }
            rs.close();
            cs.close();
            
            cs = conn.prepareCall("{call GameGetListBot_New(?) }");
            cs.setInt("Gameid", GAMEID.POKDENGNEW);
            rs = cs.executeQuery();

            int bot_100_count = (count_list_bot * bot_100_count_percent) / 100;
            int bot_500_count = (count_list_bot * bot_500_count_percent) / 100;
            int bot_1K_count = (count_list_bot * bot_1K_count_percent) / 100;
            int bot_5K_count = (count_list_bot * bot_5K_count_percent) / 100;
            int bot_10K_count = (count_list_bot * bot_10K_count_percent) / 100;
            int bot_50K_count = (count_list_bot * bot_50K_count_percent) / 100;
            int bot_100K_count = (count_list_bot * bot_100K_count_percent) / 100;
            int bot_500K_count = (count_list_bot * bot_500K_count_percent) / 100;
            int bot_1M_count = (count_list_bot * bot_1M_count_percent) / 100;
            
            short type = 0;
            listBotPokdeng.clear();
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
                int ag = getAGADD(userTemp.getUsertype());
                userTemp.setAG((long) ag);
                userTemp.setVIP((short) getVip(userTemp.getUsertype()));
                ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.POKDENGNEW);
                Logger.getLogger("PokdengTest").info(": " + userTemp.getUsertype() + "-" + userTemp.getVIP() + "-" + userTemp.getAG());
                listBotPokdeng.add(userTemp);
            }
            rs.close();
            cs.close();

            loggerBot_.info("===>PokdengBotHandler=>getListBot: ==>Size listBotPokdeng:" + listBotPokdeng.size());
        } catch (SQLException ex) {
            loggerBot_.info("==>Error==>PokdengBotHandler=>GetListBot:" + ex.getMessage());
        } finally {
            instance.releaseDbConnection(conn);
        }

    }

    private void AddLogAgBot(int AG, UserInfo bot) {
        loggerBot_.info("       PokdengBotHandler=>AddLogAgBot: Start");
        JsonObject job = new JsonObject();
        job.addProperty("uid", bot.getPid());
        job.addProperty("n", bot.getUsername());
        job.addProperty("ag", AG);
        loggerBotAddGold_.info(new Gson().toJson(job));
        loggerBot_.info(new Gson().toJson(job));
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
		synchronized (listBotPokdeng) {
            for (int i = 0; i < listBotPokdeng.size(); i++) {
                if (listBotPokdeng.get(i).getGameid() == 0) {
                	listBotPokdeng.get(i).setGameid((short) gameid);
                    return listBotPokdeng.get(i);
                }
            }
        }
		return null;
	}

	@Override
	public void processBotDisconnect(int playerId) {
		try{
			synchronized (listBotPokdeng) {
                for (int i = 0; i < listBotPokdeng.size(); i++) {
                    if (listBotPokdeng.get(i).getPid() == playerId) {
                        listBotPokdeng.get(i).setGameid((short) 0);
                        break;
                    }
                }
                if (dicBotPokdeng.containsKey(playerId)) {
                    dicBotPokdeng.get(playerId).setDisconnect(true);
                }
            }
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
		try{
			if(!dicBotPokdeng.containsKey(pid)) return null;
			dicBotPokdeng.get(pid).setRoomId((short) roomId);
            dicBotPokdeng.get(pid).setTableId(tableId);
            dicBotPokdeng.get(pid).setAS((long) mark);
            for (int i = 0; i < listBotPokdeng.size(); i++) {
                if (listBotPokdeng.get(i).getPid() == pid) {
                    listBotPokdeng.remove(i);
                    break;
                }
            }
            return dicBotPokdeng.get(pid);
		}catch (Exception e) {
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
	// public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark) {
	public Long UpdateBotMarkByUID(int uid, long mark) {
		return UpdateBotMarkChessByName(uid,ServerSource.THAI_SOURCE,"",mark);
	}

	@Override
	public String processGetBotInfoByPid(int pid, int tid) {
		try{
			synchronized (dicBotPokdeng) {
                if (dicBotPokdeng.containsKey(pid)) {
                    if (tid > 0) {
                        dicBotPokdeng.get(pid).setTableId(tid);
                    }
                    return ActionUtils.gson.toJson(dicBotPokdeng.get(pid).getUserGame());
                }
            }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
		return dicBotPokdeng;
	}

	@Override
	public List<UserInfo> getListBot() {
		return listBotPokdeng;
	}

}
