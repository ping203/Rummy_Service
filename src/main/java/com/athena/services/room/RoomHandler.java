package com.athena.services.room;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.athena.services.bot.BotPoker9K2345;
import com.athena.services.bot.DummyBotHandler;
import com.athena.services.bot.PokdengBotHandler;
import com.athena.services.bot.TomCuaCaBotHandler;
import com.athena.services.constant.BinhConstant;
import com.athena.services.constant.SamGongConstant;
import com.athena.services.handler.EVT_ID;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.GAMEID;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.api.service.dosprotect.DosProtector;
import com.dst.config.myanPokdeng.PokdengConstant;
import com.google.gson.JsonObject;

public class RoomHandler {
    public static Map<String, Room> mapGameRoom = new HashMap<String, Room>();
    private ScheduledExecutorService scheduler;

    public RoomHandler() {
        try {
            scheduler = Executors.newScheduledThreadPool(4, new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("RoomHandler");
                    thread.setDaemon(true);
                    return thread;
                }
            });

            scheduler.scheduleWithFixedDelay(new UpdateRoomPlayer(), 5, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processRoomIDEvt(int id, JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter,
                                 ServiceContext context) {
        try {
            DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
            if (!dos.allow("roomaction", actionUser.getUserid())) return;

            JsonObject send = new JsonObject();
            je.addProperty("pid", actionUser.getPid());
            send.add("idevtdata", je);

            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getKeyMapGameRoom(JsonObject json) {
        return json.get("gameid").getAsInt() + "_" + json.get("roomid").getAsShort();
    }

    public void processRoomAction(JsonObject json) {
        try {
            switch (json.get("idevt").getAsInt()) {
                case EVT_ID.GET_LIST_TABLE:
                    processGetListTable(json);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processGetListTable(JsonObject json) {
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("idevt", EVT_ID.GET_LIST_TABLE);
            jo.addProperty("tables", json.get("tables").getAsString());
            jo.addProperty("players", getPlayerInRoom(json));
            ClientServiceAction csa = new ClientServiceAction(json.get("pid").getAsInt(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));

            synchronized (ServiceImpl.dicUser) {
                if (ServiceImpl.dicUser.containsKey(json.get("pid").getAsInt()) && json.has("roomid")) {
                    ServiceImpl.dicUser.get(json.get("pid").getAsInt()).setRoomId(json.get("roomid").getAsShort());
                    if (json.has("type"))
                        ServiceImpl.dicUser.get(json.get("pid").getAsInt()).setGameType9k(json.get("type").getAsShort());
                }
            }
            ServiceImpl.serviceRouter.dispatchToPlayer(json.get("pid").getAsInt(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPlayerInRoom(JsonObject json) {
        try {
            String key = getKeyMapGameRoom(json);
            if (mapGameRoom.containsKey(key)) {
                if (json.get("gameid").getAsInt() == GAMEID.POKER9K_2345) {
                    return mapGameRoom.get(key).getListTopPlayersByType(json);
                } else
                    return mapGameRoom.get(key).getListTopPlayers(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void processPlayerLeave(UserInfo actionUser, int pid) {
        try {
            synchronized (mapGameRoom) {
                String key = actionUser.getGameid() + "_" + actionUser.getRoomId();
                if (mapGameRoom.containsKey(key)) {
                    synchronized (mapGameRoom.get(key).getMapPlayer()) {
                        if (mapGameRoom.get(key).getMapPlayer().containsKey(actionUser.getPid())) {
                            synchronized (mapGameRoom.get(key).getTopPlayer()) {

                                for (int i = 0; i < mapGameRoom.get(key).getTopPlayer().size(); i++) {
                                    if (mapGameRoom.get(key).getTopPlayer().get(i).getPid() == actionUser.getPid()) {
                                        mapGameRoom.get(key).getTopPlayer().remove(i);
                                        break;
                                    }
                                }
                            }
                            mapGameRoom.get(key).getMapPlayer().remove(actionUser.getPid());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            if (scheduler != null)
                scheduler.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processPlayerJoinTable(UserInfo actionUser) {
        try {
            synchronized (mapGameRoom) {
                String key = actionUser.getGameid() + "_" + actionUser.getRoomId();
                //						+" - type9k = "+ actionUser.getGameType9k());
                if (mapGameRoom.containsKey(key)) {
                    synchronized (mapGameRoom.get(key).getMapPlayer()) {
                        if (!mapGameRoom.get(key).getMapPlayer().containsKey(actionUser.getPid())) {
                            RoomPlayer player = new RoomPlayer(actionUser.getPid(),
                                    actionUser.getUsername(), actionUser.getAG(), actionUser.getVIP(), actionUser.getAvatar(),
                                    actionUser.getFacebookid(), actionUser.getTableId());
                            if (actionUser.getGameid() == GAMEID.POKER9K_2345)
                                player.setUsertype(actionUser.getGameType9k());
                            mapGameRoom.get(key).getMapPlayer().put(actionUser.getPid(), player);
                        } else {
                            mapGameRoom.get(key).getMapPlayer().get(actionUser.getPid()).setUsertype(actionUser.getGameType9k());
                        }
                    }
                } else {
                    Room room = new Room();
                    RoomPlayer player = new RoomPlayer(actionUser.getPid(),
                            actionUser.getUsername(), actionUser.getAG(), actionUser.getVIP(), actionUser.getAvatar(),
                            actionUser.getFacebookid(), actionUser.getTableId());
                    if (actionUser.getGameid() == GAMEID.POKER9K_2345)
                        player.setUsertype(actionUser.getGameType9k());
                    room.getMapPlayer().put(actionUser.getPid(), player);
                    mapGameRoom.put(key, room);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public short getRoomIDbyMark(short gameid, int mark, int roomId) {
        try {
            switch (gameid) {
                case GAMEID.DUMMY:
                    if (mark == DummyBotHandler.Mark1 || mark == DummyBotHandler.Mark2 || mark == DummyBotHandler.Mark3
                            || mark == DummyBotHandler.Mark4 || mark == DummyBotHandler.Mark5)
                        return 1;
                    else if (mark == DummyBotHandler.Mark6 || mark == DummyBotHandler.Mark7 || mark == DummyBotHandler.Mark8)
                        return 2;
                    else if (mark == DummyBotHandler.Mark9 || mark == DummyBotHandler.Mark10)
                        return 3;
                    else if (mark == DummyBotHandler.Mark11 || mark == DummyBotHandler.Mark12)
                        return 4;
                case GAMEID.POKER9K_2345:
                    if (mark == BotPoker9K2345.Mark1 || mark == BotPoker9K2345.Mark2 || mark == BotPoker9K2345.Mark3)
                        return 1;
                    else if (mark == BotPoker9K2345.Mark4 || mark == BotPoker9K2345.Mark5 || mark == BotPoker9K2345.Mark6)
                        return 2;
                    else if (mark == BotPoker9K2345.Mark7 || mark == BotPoker9K2345.Mark8)
                        return 3;
                    else if (mark == BotPoker9K2345.Mark9 || mark == BotPoker9K2345.Mark10 || mark == BotPoker9K2345.Mark11)
                        return 4;
                case GAMEID.POKDENGNEW:
                    if (mark == PokdengBotHandler.Mark1 || mark == PokdengBotHandler.Mark2 || mark == PokdengBotHandler.Mark3)
                        return 1;
                    else if (mark == PokdengBotHandler.Mark4 || mark == PokdengBotHandler.Mark5 || mark == PokdengBotHandler.Mark6)
                        return 2;
                    else if (mark == PokdengBotHandler.Mark7 || mark == PokdengBotHandler.Mark8)
                        return 3;
                    else if (mark == PokdengBotHandler.Mark9 || mark == PokdengBotHandler.Mark10)
                        return 4;
                case GAMEID.TOMCUACA:
                    if (mark == TomCuaCaBotHandler.Mark1 || mark == TomCuaCaBotHandler.Mark2)
                        return 1;
                    else if (mark == TomCuaCaBotHandler.Mark3)
                        return 2;
                    else if (mark == TomCuaCaBotHandler.Mark4)
                        return 3;
                    else if (mark == TomCuaCaBotHandler.Mark5)
                        return 4;
                case GAMEID.MYANMAR_BURMESE_POKER:
                    return (short) BinhConstant.getRoomIdByMark(mark);
                case GAMEID.MYANMAR_SHAN_KOE_MEE_V1:
                    return (short) PokdengConstant.getRoomIdByMark(mark);
                case GAMEID.MYANMAR_TOMCUACA:
                    break;
                case GAMEID.MYANMAR_XITO:
                    break;
                case GAMEID.MYANMAR_SHAN_KOE_MEE_V2:
                    return (short) PokdengConstant.getRoomIdByMark(mark);
                case GAMEID.SAMGONG_MYANMAR:
                    return (short) SamGongConstant.getRoomIDbyMark(mark);
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (short) roomId;
    }
}
