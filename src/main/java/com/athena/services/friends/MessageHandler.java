package com.athena.services.friends;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
//import com.athena.services.vo.Match;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.ServerSource;
import com.google.gson.JsonObject;
import com.vng.tfa.common.SqlService;

public class MessageHandler {

    private static Logger logger_ = Logger.getLogger("MessageHandler");
    private static final Lock _createLock = new ReentrantLock();
    private static MessageHandler _instance;
    static final String queuename = "gameUpdateDB";

    public static MessageHandler getInstance(String urlConnection) {
        if (_instance == null) {
            _createLock.lock();
            try {
                if (_instance == null) {
                    _instance = new MessageHandler();
                }
            } finally {
                _createLock.unlock();
            }
        }
        return _instance;
    }

    public void Process_Message(JsonObject je, UserInfo actionUser, int playerId, ServiceRouter serviceRouter) {
        try {
            int source = actionUser.getSource();
            if (je.get("evt").getAsString().equals("messagedetail")) { //Follow
            	Integer uid = 0;
            	if(je.has("Id")){
            		 uid = je.get("Id").getAsInt();
            		 if (uid > ServerDefined.userMap.get(source)) {
                     	uid = uid - ServerDefined.userMap.get(source);
                     }
            	} else if(je.has("name")){
                 	String keyMap_ID_Name = ServerDefined.getKeyCacheMapIdName(source) +  ActionUtils.MD5(ActionUtils.ValidString(je.get("name").getAsString()));
                 	uid = (Integer) UserController.getCacheInstance().get(keyMap_ID_Name);         	
                 	logger_.info("==>Process_Message==>messagedetail: "+playerId+" - "+ ActionUtils.gson.toJson(je)
                       		+" - keyMap_ID_Name: "+keyMap_ID_Name+" - uid: "+uid);   	
             	}
            	ListMessageObj ls = null;
            	if(uid > 0){
                    ls = GetListMessageDetail(source, playerId - ServerDefined.userMap.get(source), uid, actionUser.getUsername());
                    ls.setUserid(ls.getUserid() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    for (MessageObj messageObj : ls.getLsMessage()) {
                        messageObj.setFid(messageObj.getFid() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                        messageObj.setFromid(messageObj.getFromid() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                        messageObj.setToid(messageObj.getToid()+ ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    }
                }
            		
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "messagedetail");
                if (ls != null && ls.getLsMessage().size() > 0) {
                    jo.addProperty("data", ActionUtils.gson.toJson(ls));
                } else {
                    jo.addProperty("data", "");
                }
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (je.get("evt").getAsString().equals("messagelist")) { //Follow
                List<MessageTransfer> ls = GetListMessage(source, playerId - ServerDefined.userMap.get(source), actionUser.getUsername());
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "messagelist");
                if (ls.size() > 0) {
                    jo.addProperty("data", ActionUtils.gson.toJson(ls));
                } else {
                    jo.addProperty("data", "");
                }
                logger_.info("==>messagelist==>finish:" + ActionUtils.gson.toJson(jo));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (je.get("evt").getAsString().equals("message")) { //Follow        
            	ProcessMessage(je,actionUser,playerId,source,serviceRouter,false);
            } else if (je.get("evt").getAsString().equals("message2")) { //Follow        
            	ProcessMessage(je,actionUser,playerId,source,serviceRouter,true);
            } else if (je.get("evt").getAsString().equals("messageread")) { //read message
                UpdateMessageToCache(source, playerId - ServerDefined.userMap.get(source), je.get("Id").getAsInt());
            } else if (je.get("evt").getAsString().equals("messagedelete")) { //delete message
                RemoveMessageInCache(source, playerId - ServerDefined.userMap.get(source), je.get("Id").getAsInt());
            } else if (je.get("evt").getAsString().equals("messagenew")) { //Lay tong so mail moi
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "messagenew");
                jo.addProperty("N", GetNewMail(source, playerId - ServerDefined.userMap.get(source)));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (je.get("evt").getAsString().equals("messagedeleteall")) { //delete message by userid
                logger_.info("==>messagedeleteall:" + je.get("Id").getAsInt());
                RemoveMessageByUserid(source, playerId - ServerDefined.userMap.get(source), je.get("Id").getAsInt());
            }
        } catch (Exception e) {
        	//e.printStackTrace();
            logger_.error("Process_Message:" + actionUser.getUserid() + "-" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ProcessMessage(JsonObject je, UserInfo actionUser, int playerId, int source, ServiceRouter serviceRouter, boolean isNew) {
		try{
			logger_.info("CreateMessageDB Start:" + playerId+" - "+ActionUtils.gson.toJson(je));
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "message");
            String strContent = je.get("Msg").getAsString().trim();
            if (strContent.length() > 200) {
                strContent = strContent.substring(0, 199);
            }
            Integer id = 0;
            String toname = "";
        	if(je.has("Id")){
        		id = je.get("Id").getAsInt();
        		
        		toname = je.get("N").getAsString();
        	} else if(je.has("name")){
        		toname = je.get("name").getAsString();
             	String keyMap_ID_Name = ServerDefined.getKeyCacheMapIdName(source) +  ActionUtils.MD5(ActionUtils.ValidString(toname));
             	id = (Integer) UserController.getCacheInstance().get(keyMap_ID_Name);         	
             	logger_.info("==>Process_Message==>messagedetail: "+playerId+" - "+ ActionUtils.gson.toJson(je)
                   		+" - keyMap_ID_Name: "+keyMap_ID_Name+" - uid: "+id);
             	if(id == null) id = 0;
         	}
        	if (id > ServerDefined.userMap.get(source)) {
    			id = id - ServerDefined.userMap.get(source);
            }
            if (id <= 0 || strContent.length() == 0) {
                jo.addProperty("data", "");
            } else {                                     
        		String uname = actionUser.getUsername();
                if(actionUser.getUsernameLQ().length() > 0)
                	uname = actionUser.getUsernameLQ();
                MessageObj ff = CreateMessage(source, playerId - ServerDefined.userMap.get(source), id, je.get("Msg").getAsString(), 
                		//je.get("AG").getAsInt(), je.get("I").getAsInt(), uname, je.get("N").getAsString(), 
                		0, 0, uname, toname, 
                		(int) actionUser.getVIP(), (int) actionUser.getAvatar(),isNew);
                if (ff.getId() > 0) {
                    ff.setFromid(ff.getFromid() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    ff.setToid(ff.getToid()+ ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    jo.addProperty("data", ActionUtils.gson.toJson(ff));
                    if (ServiceImpl.dicUser.containsKey(id + ServerDefined.userMap.get(source))) {
                        logger_.info("==>CreateMessageDB End ==> Send to client1:" + String.valueOf(id + ServerDefined.userMap.get(source)));
                        ClientServiceAction csa1 = new ClientServiceAction(id + ServerDefined.userMap.get(source), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(id + ServerDefined.userMap.get(source), csa1);
                    }
                } else {
                    jo.addProperty("data", "");
                }                                  
            }
            logger_.info("==>CreateMessageDB End ==> Send to client2:" + playerId);
            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(playerId, csa);
            //Check xem thang user gui co cung
		}catch (Exception e) {
            logger_.info("==>CreateMessageDB End ==> err " + e.getMessage(), e);
			e.printStackTrace();
		}
	}

	/**
     * *******************************************************
     */
    /*
     * Get List Message from DB
     */
    private List<MessageTransfer> GetListMessageDB(int source, int userid, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            logger_.info("==>GetListMessageDB==>Start:" + userid);
            CallableStatement cs = conn.prepareCall("{call GameMessage_Get(?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("Username", username);
            int newMsg = 0;
            ResultSet rs = cs.executeQuery();
            List<MessageObj> lsret = new ArrayList<MessageObj>();
            while (rs.next()) {
                MessageObj objReturn = new MessageObj();
                objReturn.setId(rs.getInt("Id"));
                objReturn.setFromid(rs.getInt("Fromid"));
                objReturn.setToid(rs.getInt("ToId"));
                objReturn.setFromname(rs.getString("Fromname"));
                objReturn.setToname(rs.getString("Toname"));
                objReturn.setGold(rs.getInt("Gold"));
                objReturn.setItemid(rs.getInt("Itemid"));
                objReturn.setMsg(rs.getString("Msg"));
                objReturn.setStatusmsg(rs.getShort("StatusM"));
                objReturn.setTimemsg(rs.getTimestamp("Timemsg").getTime());
                objReturn.setVip(rs.getInt("Vip"));
                objReturn.setAvatar(rs.getInt("Avatar"));
                objReturn.setFid(rs.getLong("FacebookID"));
                if (objReturn.getStatusmsg() == 0 && objReturn.getToid() == userid) {
                    newMsg++;
                }
                lsret.add(objReturn);
            }
            rs.close();
            cs.close();
            logger_.info("==>GetListMessageDB==>Length:" + userid + "-" + lsret.size());
            ListMessageObj lsMessage = new ListMessageObj(userid);
            lsMessage.setLsMessage(lsret);
            lsMessage.setNewMsg(newMsg);
            String keyFriend = genCacheMessageKey(source, userid);
            UserController.getCacheInstance().set(keyFriend, lsMessage, 0);
            
            //return luon de ko goi lai neu cache chet
            List<MessageTransfer> lsReturn = new ArrayList<MessageTransfer>();
            for (int i = lsMessage.getLsMessage().size() - 1; i >= 0; i--) {
                    boolean t = true;
                    int toidCheck = lsMessage.getLsMessage().get(i).getFromid();
                    int vip = lsMessage.getLsMessage().get(i).getVip();
                    int avatar = lsMessage.getLsMessage().get(i).getAvatar();
                    long fid = lsMessage.getLsMessage().get(i).getFid();
                    long id = lsMessage.getLsMessage().get(i).getId();
                    long msgtime = lsMessage.getLsMessage().get(i).getTimemsg();
                    String title = lsMessage.getLsMessage().get(i).getMsg();
                    String toname = lsMessage.getLsMessage().get(i).getFromname();
                    String fromname = lsMessage.getLsMessage().get(i).getToname();
                    if (toidCheck == userid) {
                        toidCheck = lsMessage.getLsMessage().get(i).getToid();
                        toname = lsMessage.getLsMessage().get(i).getToname();
                        fromname = lsMessage.getLsMessage().get(i).getFromname();
                    }
                    for (int j = 0; j < lsReturn.size(); j++) {
                        if (lsReturn.get(j).getToid() == toidCheck) {
                            t = false;
                            break;
                        }
                    }
                    if (t && toidCheck != userid) {
                        logger_.info("GetListMessage Name:" + toname);
                        int count = 0;
                        for (int j = 0; j < lsMessage.getLsMessage().size(); j++) {
                            if ((lsMessage.getLsMessage().get(j).getToid() == userid) && (lsMessage.getLsMessage().get(j).getStatusmsg() == 0)) {
                                logger_.info("GetListMessage Count:" + lsMessage.getLsMessage().get(j).getToid() + "-" + userid + "-" + lsMessage.getLsMessage().get(j).getStatusmsg());
                                count++;
                            }
                        }
                        lsReturn.add(new MessageTransfer(userid, toidCheck, fromname,toname, count, vip, avatar, title, id, msgtime,fid));
                    }
                }
            
            //end
            return lsReturn;
        } catch (Exception e) {
            e.printStackTrace();
            logger_.error("GetListMessageDB:" + userid + "-" + e.getMessage());
            return new ArrayList<MessageTransfer>();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /*
     * Create Follow Friend
     */
    private MessageObj CreateMessageDB(int source, int fromid, int toid, String msg, int gold, int itemid, String fromname, String toname, int vip, int avatar) {
        logger_.info("CreateMessageDB Start:" + fromname + "-" + toname);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameMessage_Create(?,?,?,?,?,?,?) }");
            cs.setInt("Fromid", fromid);
            cs.setInt("Toid", toid);
            cs.setInt("Gold", gold);
            cs.setInt("Itemid", itemid);
            cs.setString("Msg", msg);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.registerOutParameter("Timemsg", Types.TIMESTAMP);
            cs.execute();
            MessageObj objReturn = new MessageObj();
            objReturn.setId(cs.getInt("Error"));
            if (objReturn.getId() > 0) {
                objReturn.setFromid(fromid);
                objReturn.setToid(toid);
                objReturn.setFromname(fromname);
                objReturn.setToname(toname);
                objReturn.setGold(gold);
                objReturn.setItemid(itemid);
                objReturn.setMsg(msg);
                objReturn.setVip(vip);
                objReturn.setAvatar(avatar);
                objReturn.setTimemsg(cs.getTimestamp("Timemsg").getTime());
            }
            return objReturn;
        } catch (Exception ex) {
        	ex.printStackTrace();
            logger_.error("CreateMessageDB:" + fromname + "-" + toname + "-" + ex.getMessage());
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    private MessageObj CreateMessageDB2(int source, int fromid, int toid, String msg, int gold, int itemid, String fromname, String toname, int vip, int avatar) {
        logger_.info("CreateMessageDB Start:" + fromname + "-" + toname);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameMessage_Create_Using(?,?,?,?,?,?,?,?) }");
            cs.setInt("Fromid", fromid);
            cs.setInt("Toid", toid);
            cs.setInt("Gold", gold);
            cs.setInt("Itemid", itemid);
            cs.setString("Msg", msg);
            cs.registerOutParameter("Fid", Types.BIGINT);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.registerOutParameter("Timemsg", Types.TIMESTAMP);
            cs.execute();
            MessageObj objReturn = new MessageObj();
            objReturn.setId(cs.getInt("Error"));
            if (objReturn.getId() > 0) {
                objReturn.setFromid(fromid);
                objReturn.setToid(toid);
                objReturn.setFromname(fromname);
                objReturn.setToname(toname);
                objReturn.setGold(gold);
                objReturn.setItemid(itemid);
                objReturn.setMsg(msg);
                objReturn.setVip(vip);
                objReturn.setAvatar(avatar);
                objReturn.setFid(cs.getLong("Fid"));
                objReturn.setTimemsg(cs.getTimestamp("Timemsg").getTime());
            }
            return objReturn;
        } catch (Exception ex) {
        	ex.printStackTrace();
            logger_.error("CreateMessageDB:" + fromname + "-" + toname + "-" + ex.getMessage());
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    /*
     * Remove FollowFriend in DB
     */
    public void UpdateMessageDB(int source, int userid, long id, short statusm, short statusfrom) {
        logger_.info("UpdateMessageDB:" + id + "-" + statusm + "-" + statusfrom);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameMessage_Update(?,?,?) }");
            cs.setLong("id", id);
            cs.setShort("status", statusm);
            cs.setShort("statusFrom", statusfrom);
            cs.execute();
        } catch (Exception ex) {
        	ex.printStackTrace();
            logger_.error("UpdateMessageDB:" + userid + "-" + ex.getMessage());
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ************************************ Cache ************************************
     */
    private String genCacheMessageKey(int source, int userId) {
        return ServerDefined.getKeyMessage(source) + userId;
    }
    /*
     * Get List Message
     */

    private List<MessageTransfer> GetListMessage(int source, int userid, String username) {
        try {
            logger_.info("GetListMessage Start:" + userid);
            String keyMessage = genCacheMessageKey(source, userid);
            ListMessageObj lsMessage = (ListMessageObj) UserController.getCacheInstance().get(keyMessage);
            List<MessageTransfer> lsReturn = new ArrayList<>();
            if (lsMessage != null) {
//                logger_.info("GetListMessage Length:" + lsMessage.getLsMessage().size() + "-" + keyMessage + "-" + username);
                for (int i = lsMessage.getLsMessage().size() - 1; i >= 0; i--) {
                    boolean t = true;
                    int toidCheck = lsMessage.getLsMessage().get(i).getFromid();
                    long id = lsMessage.getLsMessage().get(i).getId();

                    long fid = lsMessage.getLsMessage().get(i).getFid();
                    int avatar = lsMessage.getLsMessage().get(i).getAvatar();
                   
                    long msgtime = lsMessage.getLsMessage().get(i).getTimemsg();
                    String title = lsMessage.getLsMessage().get(i).getMsg();
                    String toname = lsMessage.getLsMessage().get(i).getFromname();
                    String fromname = lsMessage.getLsMessage().get(i).getToname();
                    if (toidCheck == userid) {
                        toidCheck = lsMessage.getLsMessage().get(i).getToid();
                        toname = lsMessage.getLsMessage().get(i).getToname();
                        fromname = lsMessage.getLsMessage().get(i).getFromname();
                    }
                    String key = ServerDefined.getKeyCache(source) + toidCheck;
                    //logger_.info("Key Userinfo:" + key) ;
                    UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
                    int vip = lsMessage.getLsMessage().get(i).getVip();
                    if(uinfo != null){
                    	vip = uinfo.getVIP();
                    	fid = uinfo.getFacebookid();
                    }
                    	
                    for (int j = 0; j < lsReturn.size(); j++) {
                        if (lsReturn.get(j).getToid() == toidCheck) {
                            t = false;
                            break;
                        }
                    }
                    if (t && toidCheck != userid) {
//                        logger_.info("GetListMessage Name:" + toname);
                        int count = 0;
                        for (int j = 0; j < lsMessage.getLsMessage().size(); j++) {
                            if ((lsMessage.getLsMessage().get(j).getToid() == userid) && (lsMessage.getLsMessage().get(j).getStatusmsg() == 0)
                            		&&  lsMessage.getLsMessage().get(j).getFromid() == toidCheck) {
//                                logger_.info("GetListMessage Count:" + lsMessage.getLsMessage().get(j).getToid() 
//                                		+ "-" + userid + "-" + lsMessage.getLsMessage().get(j).getStatusmsg());
                                count++;
                            }
                        }
                        lsReturn.add(new MessageTransfer(userid, toidCheck, fromname,toname, count, vip, avatar, title, id, msgtime,fid));
                    }
                }
                
                
                logger_.info("GetListMessage Length Return:" + lsReturn.size());
                //Update trang thai cac Friend
//                return lsReturn;
            } else {
                logger_.info("GetListMessage lai");
                //GetListMessageDB(source, userid, username);
                lsReturn =  GetListMessageDB(source, userid, username);
            }
            
            for (MessageTransfer messageTransfer : lsReturn) {
                messageTransfer.setFromid(messageTransfer.getFromid() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                messageTransfer.setToid(messageTransfer.getToid()+ ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
            }
            
            return lsReturn;
        } catch (Exception e) {
            logger_.error("GetListMessage:" + userid + "-" + e.getMessage());
            return new ArrayList<MessageTransfer>();
        }
    }

    private ListMessageObj GetListMessageDetail(int source, int userid, int toid, String username) {
        try {
            logger_.info("GetListMessageDetail Start:" + userid + "-" + toid);
            String keyMessage = genCacheMessageKey(source, userid);
            ListMessageObj lsMessage = (ListMessageObj) UserController.getCacheInstance().get(keyMessage);
            if (lsMessage != null) {
                ListMessageObj lsMessagereturn = new ListMessageObj(userid);
                for (int i = 0; i < lsMessage.getLsMessage().size(); i++) {
                    if (lsMessage.getLsMessage().get(i).getToid() == toid || lsMessage.getLsMessage().get(i).getFromid() == toid) {
                        lsMessagereturn.getLsMessage().add(lsMessage.getLsMessage().get(i));
//                        logger_.info("GetListMessageDetail ==>" + lsMessage.getLsMessage().get(i).getFromid() + "-" + lsMessage.getLsMessage().get(i).getStatusmsg());
                        if (lsMessage.getLsMessage().get(i).getFromid() == toid) {
                            if ((int) lsMessage.getLsMessage().get(i).getStatusmsg() == 0) {
                                lsMessage.getLsMessage().get(i).setStatusmsg((short) 1);
                                lsMessage.setNewMsg(lsMessage.getNewMsg() - 1);
//                                logger_.info("GetListMessageDetail ==>Update DB:" + lsMessage.getLsMessage().get(i).getFromid() + "-" + lsMessage.getLsMessage().get(i).getStatusmsg());
                                UserInfoCmd cmd = new UserInfoCmd("gameMessageUpdate", source, userid, (int) lsMessage.getLsMessage().get(i).getId(), (short) 1, (short) 0); //Read Message
                                QueueManager.getInstance(queuename).put(cmd);
                            }
                        }
                    }
                }
                UserController.getCacheInstance().set(keyMessage, lsMessage, 0);
                return lsMessagereturn;
            } else {
            	GetListMessageDB(source, userid, username);
            	return  (ListMessageObj) UserController.getCacheInstance().get(keyMessage);
                //return GetListMessageDetail(source, userid, toid, username);
            }
        } catch (Exception e) {
        	e.printStackTrace();
//            logger_.error("GetListMessage:" + userid + "-" + e.getMessage());
            return new ListMessageObj(userid);
        }
    }

    private MessageObj CreateMessage(int source, int uid, int toid, String msg, int gold, int itemid, String fromname,
    		String toname, int vip, int avatar, boolean isNew) {
        try {
            MessageObj objReturn = null;
            if(isNew )
            	objReturn = CreateMessageDB(source, uid, toid, msg, gold, itemid, fromname, toname, vip, avatar);
            else
            	objReturn = CreateMessageDB2(source, uid, toid, msg, gold, itemid, fromname, toname,vip,avatar);
            if (objReturn.getId() > 0) {
                AddMessageToCache(source, uid, objReturn, 0);
                //Search ListMsg cua Touser de Add
                AddMessageToCache(source, objReturn.getToid(), objReturn, 1);
            }
            return objReturn;
        } catch (Exception e) {
        	e.printStackTrace();
            logger_.error("CreateMessage:" + uid + "-" + e.getMessage());
            return new MessageObj();
        }
    }
    /*
     * Add Message to Cache
     */

	private int AddMessageToCache(int source, int uid, MessageObj obj, int newMsg) {
        try {
            String keyMessage = genCacheMessageKey(source, uid);
            ListMessageObj lsMessage = (ListMessageObj) UserController.getCacheInstance().get(keyMessage);
            if (lsMessage != null) {
                lsMessage.getLsMessage().add(obj);
                lsMessage.setNewMsg(lsMessage.getNewMsg() + newMsg);
                UserController.getCacheInstance().set(keyMessage, lsMessage, 0);
            } else {
                lsMessage = new ListMessageObj(uid);
                lsMessage.getLsMessage().add(obj);
                lsMessage.setNewMsg(newMsg);
                UserController.getCacheInstance().set(keyMessage, lsMessage, 0);
            }
            return 1;
        } catch (Exception ex) {
            logger_.error("AddMessageToCache:" + uid + "-" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }
    /*
     * Remove Message in Cache
     */

    private void RemoveMessageInCache(int source, int uid, int id) { //Remove Message
        try {
            String keyMessage = genCacheMessageKey(source, uid);
            ListMessageObj lsMessage = (ListMessageObj) UserController.getCacheInstance().get(keyMessage);
            if (lsMessage != null) {
                for (int i = 0; i < lsMessage.getLsMessage().size(); i++) {
                    if (lsMessage.getLsMessage().get(i).getId() == id) {
                        if (lsMessage.getLsMessage().get(i).getFromid() == uid) {//Xoa thu minh gui
                            UserInfoCmd cmd = new UserInfoCmd("gameMessageUpdate", source, uid, id, (short) -2, (short) 1); //Delete Message From user
                            QueueManager.getInstance(queuename).put(cmd);
                        } else {//Xoa thu minh nhan
                            UserInfoCmd cmd = new UserInfoCmd("gameMessageUpdate", source, uid, id, (short) 2, (short) 0); //Delete Message To user
                            QueueManager.getInstance(queuename).put(cmd);
                        }
                        lsMessage.getLsMessage().remove(i);
                        break;
                    }
                }
                UserController.getCacheInstance().set(keyMessage, lsMessage, 0);
            }
        } catch (Exception ex) {
            logger_.error("RemoveMessageInCache:" + uid + "-" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void RemoveMessageByUserid(int source, int uid, int toid) {
        try {
            logger_.info("RemoveMessageByUserid:" + uid + "-" + toid);
            String keyMessage = genCacheMessageKey(source, uid);
            ListMessageObj lsMessage = (ListMessageObj) UserController.getCacheInstance().get(keyMessage);
            if (lsMessage != null) {
                logger_.info("RemoveMessageByUserid ==> start:" + uid + "-" + toid + "-" + lsMessage.getLsMessage().size());
                boolean t = true;
                while (t) {
                    t = false;
                    for (int i = 0; i < lsMessage.getLsMessage().size(); i++) {
                        logger_.info("RemoveMessageByUserid ==>i:" + lsMessage.getLsMessage().get(i).getFromid() + "-" + lsMessage.getLsMessage().get(i).getToid() + "-" + toid);
                        if (lsMessage.getLsMessage().get(i).getFromid() == toid - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) { //Xoa thu minh nhan
                            UserInfoCmd cmd = new UserInfoCmd("gameMessageUpdate", source, uid, lsMessage.getLsMessage().get(i).getId(), (short) 2, (short) 0); //Delete Message From user
                            QueueManager.getInstance(queuename).put(cmd);
                            lsMessage.getLsMessage().remove(i);
                            t = true;
                        } else if (lsMessage.getLsMessage().get(i).getToid() == toid - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) { //Xoa thu minh gui
                            UserInfoCmd cmd = new UserInfoCmd("gameMessageUpdate", source, uid, lsMessage.getLsMessage().get(i).getId(), (short) -2, (short) 1); //Delete Message To user
                            QueueManager.getInstance(queuename).put(cmd);
                            lsMessage.getLsMessage().remove(i);
                            t = true;
                        }
                    }
                }
                UserController.getCacheInstance().set(keyMessage, lsMessage, 0);
            }
        } catch (Exception e) {           
           logger_.error("RemoveMessageByUserid:" + uid + "-" + e.getMessage());
           e.printStackTrace();
        }
    }
    
    
    /*
     * Update Status Message in Cache
     */

    private void UpdateMessageToCache(int source, int uid, int id) { //Update Message
        try {
            String keyMessage = genCacheMessageKey(source, uid);
            ListMessageObj lsMessage = (ListMessageObj) UserController.getCacheInstance().get(keyMessage);
            if (lsMessage != null) {
                for (int i = 0; i < lsMessage.getLsMessage().size(); i++) {
                    if (lsMessage.getLsMessage().get(i).getId() == id) {
                        lsMessage.getLsMessage().get(i).setStatusmsg((short) 1);
                        break;
                    }
                }
                UserController.getCacheInstance().set(keyMessage, lsMessage, 0);
            }
            UserInfoCmd cmd = new UserInfoCmd("gameMessageUpdate", source, uid, id, (short) 1, (short) 0); //Read Message
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            logger_.error("UpdateMessageToCache:" + uid + "-" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public int GetNewMail(int source, int uid) {
        try {
            String keyMessage = genCacheMessageKey(source, uid);
            ListMessageObj lsMessage = (ListMessageObj) UserController.getCacheInstance().get(keyMessage);
            if (lsMessage != null) {
                return lsMessage.getNewMsg();
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger_.error("GetNewMail:" + uid + "-" + e.getMessage());
            return 0;
        }
    }
}
