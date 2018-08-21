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
import com.athena.services.handler.KeyCachedDefine;
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

public class FriendsHandler {
	
	private static Logger logger_ = Logger.getLogger("FriendsHandler");
	private static final Lock _createLock = new ReentrantLock();
    private static FriendsHandler _instance;
    static final String queuename = "gameUpdateDB";
    public static FriendsHandler getInstance(String urlConnection)
    {
    	if (_instance == null) {
            _createLock.lock();
            try {
                if (_instance == null) {
                    _instance = new FriendsHandler();
                }
            } finally {
                _createLock.unlock();
            }
        }
        return _instance;
    }
    
    public void Process_Friends(JsonObject je, UserInfo actionUser, int playerId, ServiceRouter serviceRouter) {
        try {
            int source = actionUser.getSource();
            logger_.info("Process_Friends:" + je.get("evt").getAsString());
            if (je.get("evt").getAsString().equals("followdetail")) { //Follow
            	FollowFriendObj ff = GetDetailFollowFriend(source, playerId- ServerDefined.userMap.get(source), je.get("Id").getAsInt()) ;
            	JsonObject jo = new JsonObject();
                jo.addProperty("evt", "followdetail");
                if (ff.getId() > 0)
                	jo.addProperty("data", ActionUtils.gson.toJson(ff));
                else
                	jo.addProperty("data", "");
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (je.get("evt").getAsString().equals("follow")) { //Follow
            	JsonObject jo = new JsonObject();
                jo.addProperty("evt", "follow");
                int idFollow = je.get("Id").getAsInt() ;
                if (idFollow > ServerDefined.userMap.get(source))
                	idFollow = idFollow - ServerDefined.userMap.get(source) ;
            	if (playerId == idFollow + ServerDefined.userMap.get(source) || idFollow <=0) {
            		jo.addProperty("data", "");
                	jo.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strFriend_Err4",actionUser.getSource(), actionUser.getUserid()));
            	} else {
                    logger_.info("Process_Friends ==> Start tao ban");
                    FollowFriendObj ff = CreateFollowFriend(source, playerId- ServerDefined.userMap.get(source), idFollow, je.get("N").getAsString(), actionUser.getVIP()) ;
                    ff.setFriendid(ff.getFriendid() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    ff.setUserid(ff.getUserid() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    if (ff.getId() > 0) {
                    	jo.addProperty("data", ActionUtils.gson.toJson(ff));
                    	jo.addProperty("msg", "");
                    } else {
                    	jo.addProperty("data", "");
                    	jo.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strFriend_Err5",actionUser.getSource(), actionUser.getUserid()));
                    }
            	}
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (je.get("evt").getAsString().equals("followbyname")) { //Follow
            	JsonObject jo = new JsonObject();
                jo.addProperty("evt", "follow");
            	String username = je.get("N").getAsString() ;
            	if (username.length() < 3) {
            		jo.addProperty("data", "");
                	jo.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strFriend_Err6",actionUser.getSource(), actionUser.getUserid()));
            	} else {
            		FollowFriendObj ff = CreateFollowFriendByName(source, playerId- ServerDefined.userMap.get(source), je.get("N").getAsString(), actionUser.getVIP()) ;
                	if (ff.getId() > 0) {
                    	jo.addProperty("data", ActionUtils.gson.toJson(ff));
                    	jo.addProperty("msg", "");
                    } else {
                    	jo.addProperty("data", "");
                    	jo.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strFriend_Err5",actionUser.getSource(), actionUser.getUserid()));
                    }
            	}
            	ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (je.get("evt").getAsString().equals("unfollow")) { //Follow
            	RemoveFollowFriendInCache(source, playerId- ServerDefined.userMap.get(source), je.get("Id").getAsInt());
            } else if (je.get("evt").getAsString().equals("followlist")) { //Follow
            	ListFollowFriendObj ls = GetListFollowFriend(source,playerId- ServerDefined.userMap.get(source)) ;
            	JsonObject jo = new JsonObject();
                jo.addProperty("evt", "followlist");
                if (ls.getLsFriend().size() > 0){
                    List<FollowFriendObj> ffos = ls.getLsFriend();
                    
                    for (FollowFriendObj ffo : ffos) {
                        ffo.setUserid(ffo.getUserid() +  ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                        ffo.setFriendid(ffo.getFriendid()+  ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    }
                    
                    jo.addProperty("data", ActionUtils.gson.toJson(ls.getLsFriend()));
                    
                    
                }else
                	jo.addProperty("data", "");
//                logger_.info("choson" + ActionUtils.gson.toJson(jo));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            }else if (je.get("evt").getAsString().equals("followfind")) { //Find  
            	Integer uid = null;
            	if(je.has("name")){
                    String name = ActionUtils.ValidString(je.get("name").getAsString());
                    String keyMap_ID_Name = ServerDefined.getKeyCacheMapIdName(source) +  ActionUtils.MD5(name);
                    uid = (Integer) UserController.getCacheInstance().get(keyMap_ID_Name);  
                    if(uid == null){
                            uid = getUIDFromDB(name,source);
                    if(uid > 0)
                            UserController.getCacheInstance().set(keyMap_ID_Name,uid,0);  
                    }               		
                    logger_.info("==>Process_Friends==>followfind: "+ ActionUtils.gson.toJson(je)
                            +" - keyMap_ID_Name: "+keyMap_ID_Name+" - uid: "+uid);   	
            	}else if(je.has("id")){
            		int findUID = je.get("id").getAsInt();
            		if(findUID > ServerDefined.userMap.get(source))
            			findUID-= ServerDefined.userMap.get(source);
            		uid = findUID;
            	}
            	JsonObject jo = new JsonObject();
 				jo.addProperty("evt", "followfind");
            	if(uid == null){
					jo.addProperty("data", "Friend do not exist, please try again!");
            		jo.addProperty("status", false);
            	}else{
            		if(uid > ServerDefined.userMap.get(source))
                		uid = uid - ServerDefined.userMap.get(source);

            		String key = ServerDefined.getKeyCache(source) + uid;
                    UserInfo uInfo = (UserInfo) UserController.getCacheInstance().get(key);              
                    
                    FriendInfo info = null;
                    if(uInfo != null){
                    	 String key_status = KeyCachedDefine.getKeyCachedStatusUser(uInfo);
	         			 String status = (String) UserController.getCacheInstance().get(key_status);
         				 if(status == null){
							 status = "...";
	         			 }                     
                    	 info = new FriendInfo(
                    	 		uInfo.getUserid(),
								 uInfo.getUsername(),
								 uInfo.getUsernameLQ(),
								 uInfo.getVIP(),
								 uInfo.getIsOnline(),
    					 			uInfo.getAG(),
								 uInfo.getAvatar(),
								 uInfo.getTableId(),
								 status);
                    }               
                   
    				if(info == null){
    					jo.addProperty("data", "Friend do not exist, please try again!");	
            			jo.addProperty("status", false);				
    				} else{
                                    info.setUid(info.getUid() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
    					jo.addProperty("data", ActionUtils.gson.toJson(info));
    					jo.addProperty("status", true);		
    				}
            	}
				ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
				serviceRouter.dispatchToPlayer(playerId, csa);               
            }           
        } catch (Exception e) {
        	logger_.error("Process_Friends:" + actionUser.getUserid() + "-" + e.getMessage());
        	e.printStackTrace();
        }
    }
    private Integer getUIDFromDB(String name, int source) {
    	SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
		try{
			CallableStatement cs = conn.prepareCall("{call RptGetUserIDForLock(?,?) }");
    		cs.setString(1, name);
    		cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            int uid = cs.getInt(2);
            cs.close();					
			return uid;	
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
            instance.releaseDbConnection(conn);
        }
		return null;
	}

	/**********************************************************/
    /*
     * Get List Follow Friend from DB
     */
    private ListFollowFriendObj GetListFollowFriendDB(int source, int userid) {
    	SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
        try{
        	logger_.info("==>GetListFriends==>Start:" + userid);
    		CallableStatement cs = conn.prepareCall("{call GameFollowFriend_Get(?) }");
    		cs.setInt("Userid", userid);
            ResultSet rs = cs.executeQuery();
            List<FollowFriendObj> lsret = new ArrayList<FollowFriendObj>();
            while (rs.next()) {
            	FollowFriendObj objReturn = new FollowFriendObj();
            	objReturn.setUserid(userid);
                objReturn.setFriendid(rs.getInt("FriendId"));
                objReturn.setVip((short)rs.getInt("Vip"));
                objReturn.setCurrentgold(rs.getInt("Gold"));
                objReturn.setAvatar(rs.getInt("Avatar"));
                objReturn.setFriendname(rs.getString("FriendName"));
                objReturn.setIsonline(rs.getInt("IsOnline"));
                objReturn.setId(rs.getLong("ID"));
                objReturn.setFid(rs.getLong("FacebookID"));
                objReturn.setTableid(0);
                logger_.info("==>GetListFriends==>Name:" +  objReturn.getFriendname()) ;
                lsret.add(objReturn);
            }
            rs.close();
            cs.close();
            ListFollowFriendObj lsFriend = new ListFollowFriendObj(userid) ;
            lsFriend.setLsFriend(lsret);
            String keyFriend = genCacheFriendKey(source, userid);
            logger_.info("==>GetListFriends==>Length:" + userid + "-" + keyFriend + "-" + lsret.size());
            UserController.getCacheInstance().set(keyFriend, lsFriend, 0);
			return lsFriend ;
		} catch (Exception e) {
			e.printStackTrace();
			logger_.error("GetListFriends:" + userid + "-" + e.getMessage());
			return new ListFollowFriendObj(userid) ;
		} finally {
            instance.releaseDbConnection(conn);
        }
	}
	
    /*
     * Create Follow Friend
     */
    private FollowFriendObj CreateFollowFriendDB(int source,int userid, int friendId, String friendName){
    	logger_.info("CreateFollowFriendDB:" + userid + "-" + friendId);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
        try{
            CallableStatement cs = conn.prepareCall("{call GameFollowFriend_Create_New(?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("FriendId", friendId);
            cs.registerOutParameter("Error", Types.BIGINT);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.registerOutParameter("Gold", Types.BIGINT);
            cs.registerOutParameter("Avatar", Types.INTEGER);
            cs.registerOutParameter("Online", Types.INTEGER);
            cs.registerOutParameter("FacebookID", Types.BIGINT);
            cs.execute();
            FollowFriendObj objReturn = new FollowFriendObj() ;
            objReturn.setId(cs.getLong("Error"));
            if (objReturn.getId() > 0) {
            	objReturn.setUserid(userid);
                objReturn.setFriendid(friendId);
                objReturn.setVip((short)cs.getInt("Vip"));
                objReturn.setCurrentgold(cs.getLong("Gold"));
                objReturn.setAvatar(cs.getInt("Avatar"));
                objReturn.setFriendname(friendName);
                objReturn.setIsonline(cs.getInt("Online"));
                objReturn.setFid(cs.getLong("FacebookID"));
                
                objReturn.setTableid(0);
            }
            return objReturn ;
        } catch (Exception ex){
        	ex.printStackTrace();
        	logger_.error("CreateFollowFriendDB:" + userid + "-" + ex.getMessage() ,ex);
            return null ;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    
    private FollowFriendObj CreateFollowFriendByNameDB(int source,int userid, String friendName){
    	logger_.info("CreateFollowFriendByNameDB:" + userid + "-" + friendName);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
        try{
            CallableStatement cs = conn.prepareCall("{call GameFollowFriend_CreateByName_New(?,?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("FriendName", friendName);
            cs.registerOutParameter("Error", Types.BIGINT);
            cs.registerOutParameter("FriendId", Types.INTEGER);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.registerOutParameter("Gold", Types.BIGINT);
            cs.registerOutParameter("Avatar", Types.INTEGER);
            cs.registerOutParameter("Online", Types.INTEGER);
            cs.registerOutParameter("FacebookID", Types.BIGINT);
            cs.execute();
            FollowFriendObj objReturn = new FollowFriendObj() ;
            objReturn.setId(cs.getLong("Error"));
            if (objReturn.getId() > 0) {
            	objReturn.setUserid(userid);
                objReturn.setFriendid(cs.getInt("FriendId"));
                objReturn.setVip((short)cs.getInt("Vip"));
                objReturn.setCurrentgold(cs.getInt("Gold"));
                objReturn.setAvatar(cs.getInt("Avatar"));
                objReturn.setFriendname(friendName);
                objReturn.setIsonline(cs.getInt("Online"));
                objReturn.setFid(cs.getLong("FacebookID"));
                objReturn.setTableid(0);
            }
            return objReturn ;
        } catch (Exception ex){
        	logger_.error("CreateFollowFriendByNameDB:" + userid + "-" + ex.getMessage(), ex);
        	ex.printStackTrace();
            return null ;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
	
	/*
	 * Remove FollowFriend in DB
	 */
    public void RemoveFollowFriendDB(int source, int userid, int friendId) {
		SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
        try{
            CallableStatement cs = conn.prepareCall("{call GameFollowFriend_Delete(?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("FriendId", friendId);
            cs.execute();
        }catch (Exception ex){
        	ex.printStackTrace();
        	logger_.error("RemoveFollowFriendDB:" + userid + "-" + ex.getMessage());
        } finally {
            instance.releaseDbConnection(conn);
        }
	}
	/************************************** Cache *************************************/
	private String genCacheFriendKey(int source, int userId) {
    	return ServerDefined.getKeyFriends(source) + userId;
    }
	private FollowFriendObj GetDetailFollowFriend(int source, int userid, int friendId) {
    	try {
    		logger_.info("GetDetailFollowFriend:" + friendId);
    		ListFollowFriendObj lsFriend = GetListFollowFriend(source, userid) ;
    		for (int i = 0; i < lsFriend.getLsFriend().size(); i++) {
    			logger_.info("GetDetailFollowFriend ID:" + lsFriend.getLsFriend().get(i).getFriendid());
				if (lsFriend.getLsFriend().get(i).getFriendid() == friendId) {
					String key = ServerDefined.getKeyCache(source) + lsFriend.getLsFriend().get(i).getFriendid();
                    UserInfo ulogin = (UserInfo) UserController.getCacheInstance().get(key);
                    lsFriend.getLsFriend().get(i).setVip(ulogin.getVIP());
                    lsFriend.getLsFriend().get(i).setFid(ulogin.getFacebookid());
					lsFriend.getLsFriend().get(i).setCurrentgold(ulogin.getAG().intValue());
					lsFriend.getLsFriend().get(i).setIsonline(ulogin.getIsOnline());
					lsFriend.getLsFriend().get(i).setLevel(0);
					lsFriend.getLsFriend().get(i).setTableid(ulogin.getTableId());
					return lsFriend.getLsFriend().get(i) ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger_.error("GetDetailFollowFriend:" + userid + "-" + e.getMessage());
		}
    	return new FollowFriendObj() ;
    }
	/*
	 * Get List FollowFriend
	 */
	private ListFollowFriendObj GetListFollowFriend(int source, int userid) {
		try {
			String keyFriend = genCacheFriendKey(source, userid);
			logger_.info("GetListFollowFriend1:" + keyFriend);
			ListFollowFriendObj lsFriend = (ListFollowFriendObj) UserController.getCacheInstance().get(keyFriend);
			logger_.info("GetListFollowFriend:" + keyFriend);
        	if (lsFriend != null) {
        		logger_.info("GetListFollowFriend Ko null1:" + lsFriend.getLsFriend().size());
        		//Update trang thai cac Friend
        		for (int i = 0; i < lsFriend.getLsFriend().size(); i++) {
        			String key = ServerDefined.getKeyCache(source) + lsFriend.getLsFriend().get(i).getFriendid();
        			logger_.info("GetListFollowFriend Key userinfo:" + key) ;
                    UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
                    if (uinfo != null) {
                    	lsFriend.getLsFriend().get(i).setVip(uinfo.getVIP());
    					lsFriend.getLsFriend().get(i).setCurrentgold(uinfo.getAG().longValue());
    					lsFriend.getLsFriend().get(i).setIsonline(uinfo.getIsOnline());
    					lsFriend.getLsFriend().get(i).setLevel(0);
    					lsFriend.getLsFriend().get(i).setTableid(uinfo.getTableId());
    					lsFriend.getLsFriend().get(i).setGameid(uinfo.getGameid());
    					String key_status = KeyCachedDefine.getKeyCachedStatusUser(uinfo);   					
	    				String status = (String) UserController.getCacheInstance().get(key_status);
	    				if(status == null){
	    					if(uinfo.getSource() == ServerSource.THAI_SOURCE)
	    						 status = "คุณกำลังคิดอะไรอยู่";
	    					 else
	    						 status = "...";
	    				}
	    				//logger_.info("==>GetListFollowFriend: "+key_status+" - status: "+status);
	    				lsFriend.getLsFriend().get(i).setStatus(status);
                    }                 
				}
        		return lsFriend ;
        	} else {
        		logger_.info("GetListFollowFriend==>null:" + keyFriend);
        		return GetListFollowFriendDB(source, userid) ;
        	}
		} catch (Exception e) {
			e.printStackTrace();
			logger_.error("GetListFollowFriend:" + userid + "-" + e.getMessage());
			return new ListFollowFriendObj(userid) ;
		}
	}
	private boolean CheckCreateFollowFriend(int source, int userid, int vip) {
		try {
			String keyFriend = genCacheFriendKey(source, userid);
			ListFollowFriendObj lsFriend = (ListFollowFriendObj) UserController.getCacheInstance().get(keyFriend);
        	if (lsFriend != null) {
        		logger_.info("CheckFollowFriend:" + userid + lsFriend.getLsFriend().size());
        		if(vip < 4)
        			if (lsFriend.getLsFriend().size() > 100)
        				return false ;
        		else if(vip < 9)
        			if (lsFriend.getLsFriend().size() > 500)
        				return false ;
        		else if (lsFriend.getLsFriend().size() > 1000)
            				return false ;
        	}
        	return true ;
		} catch (Exception e) {
			e.printStackTrace();
			logger_.error("CheckCreateFollowFriend:" + userid + "-" + e.getMessage());
			return false ;
		}
	}
	
	private FollowFriendObj CreateFollowFriendByName(int source, int uid, String friendName, int vip) {
		try {
			if (!CheckCreateFollowFriend(source, uid, vip)) return new FollowFriendObj() ;
			FollowFriendObj objReturn = CreateFollowFriendByNameDB(source, uid, friendName) ;
			if (objReturn.getId() > 0)
				UpdateFollowFriendToCache(source, uid, objReturn) ;
			return objReturn ;
		} catch (Exception e) {
			e.printStackTrace();
			logger_.error("CreateFollowFriend:" + uid + "-" + e.getMessage());
			return new FollowFriendObj() ;
		}
	}
	
	private FollowFriendObj CreateFollowFriend(int source, int uid, int friendId, String friendName, int vip) {
		try {
			logger_.info("CreateFollowFriend:" + uid + "-" + friendId);
			if (!CheckCreateFollowFriend(source, uid, vip)) return new FollowFriendObj() ;
			FollowFriendObj objReturn = CreateFollowFriendDB(source, uid, friendId, friendName) ;
			if (objReturn.getId() > 0)
				UpdateFollowFriendToCache(source, uid, objReturn) ;
			return objReturn ;
		} catch (Exception e) {
			e.printStackTrace();
			logger_.info("CreateFollowFriend:" + uid + "-" + e.getMessage(), e);
			return new FollowFriendObj() ;
		}
	}
	/*
	 * Update FollowFriend to Cache
	 */
	private int UpdateFollowFriendToCache(int source, int uid, FollowFriendObj obj) {
        try {
        	String keyFriend = genCacheFriendKey(source, uid);
        	logger_.info("UpdateFollowFriendToCache:" + uid + "-" + keyFriend);
        	ListFollowFriendObj lsFriend = (ListFollowFriendObj) UserController.getCacheInstance().get(keyFriend);
        	if (lsFriend != null) {
        		boolean t = true ;
        		for (int i = 0; i < lsFriend.getLsFriend().size(); i++) {
					if (lsFriend.getLsFriend().get(i).getFriendid() == obj.getFriendid()) {
						lsFriend.getLsFriend().get(i).setVip(obj.getVip());
						lsFriend.getLsFriend().get(i).setCurrentgold(obj.getCurrentgold());
						lsFriend.getLsFriend().get(i).setIsonline(obj.getIsonline());
						lsFriend.getLsFriend().get(i).setLevel(obj.getLevel());
						lsFriend.getLsFriend().get(i).setStatusf(obj.getStatusf());
						lsFriend.getLsFriend().get(i).setFriendname(obj.getFriendname());
						t = false ;
						break ;
					}
				}
        		logger_.info("UpdateFollowFriendToCache old cache:" + obj.getFriendname());
        		if (t)
        			lsFriend.getLsFriend().add(obj) ;
        		UserController.getCacheInstance().set(keyFriend, lsFriend, 0);
        	} else {
        		logger_.info("UpdateFollowFriendToCache new cache:" + obj.getFriendname());
        		lsFriend = new ListFollowFriendObj(uid) ;
        		lsFriend.getLsFriend().add(obj) ;
        		UserController.getCacheInstance().set(keyFriend, lsFriend, 0);
        	}
        	return 1 ;
        } catch (Exception ex) {
        	ex.printStackTrace();
        	logger_.error("UpdateFollowFriendToCache:" + uid + "-" + ex.getMessage());
            return -1;
        }
    }
	/*
	 * Remove FollowFriend in Cache
	 */
	private void RemoveFollowFriendInCache(int source, int uid, int friendId) { //Remove Follow Friend
        try {
        	logger_.info("RemoveFollowFriendInCache:" + friendId);
        	String keyFriend = genCacheFriendKey(source, uid);
        	ListFollowFriendObj lsFriend = (ListFollowFriendObj) UserController.getCacheInstance().get(keyFriend);
        	logger_.info("RemoveFollowFriendInCache:" + keyFriend + "-" + lsFriend.getUserid());
        	if (lsFriend != null) {
        		for (int i = 0; i < lsFriend.getLsFriend().size(); i++) {
        			logger_.info("RemoveFollowFriendInCache ID:" + lsFriend.getLsFriend().get(i).getFriendid() + "-" + friendId + "-" + lsFriend.getLsFriend().size());
					if (lsFriend.getLsFriend().get(i).getFriendid() == friendId) {
						lsFriend.getLsFriend().remove(i) ;
						logger_.info("RemoveFollowFriendInCache  Remove:" + i) ;
						break ;
					}
				}
        		logger_.info("RemoveFollowFriendInCache Length after:" + lsFriend.getLsFriend().size());
//        		if (lsFriend.getLsFriend().size() == 0)
//        			UserController.getCacheInstance().remove(keyFriend) ;
//        		else
        			UserController.getCacheInstance().set(keyFriend, lsFriend, 0);
        	}
        	UserInfoCmd cmd = new UserInfoCmd("gameFollowFriendRemove", source, uid, friendId);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
        	ex.printStackTrace();
        	logger_.error("RemoveFollowFriendToCache:" + uid + "-" + ex.getMessage());
        }
    }
}
