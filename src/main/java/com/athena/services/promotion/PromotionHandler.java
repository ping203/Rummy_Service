package com.athena.services.promotion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.athena.services.handler.KeyCachedDefine;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueCommand;
import com.cachebase.queue.GameIBonusInviteFriendsCmd;
import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.log.LoggerKey;
import com.athena.services.vo.PromotionDevice;
//import com.athena.services.vo.Match;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.dst.ServerSource;
import com.vng.tfa.common.SqlService;

public class PromotionHandler {

    private static Logger logger_ = Logger.getLogger(LoggerKey.PROMOTION_HANDLER);
    private static final Lock _createLock = new ReentrantLock();
    private static PromotionHandler _instance;
    public static final int videomax = 3;
    public static final int onlinemax = 6;
    private UserController userController;
    
    public static Logger logger_debug = Logger.getLogger(LoggerKey.DEBUG_SERVICE);

    public PromotionHandler(UserController userController) {
        this.userController = userController;
    }

    public PromotionHandler() {
    }

    public static PromotionHandler getInstance() {
        if (_instance == null) {
            _createLock.lock();
            try {
                if (_instance == null) {
                    _instance = new PromotionHandler();
                }
            } finally {
                _createLock.unlock();
            }
        }
        return _instance;
    }

    /**********************************************************/
    /*
     * Get List Follow Friend from DB
     */
    public ListPromotionObj GetListPromotionDB(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        String keyPromotion = genCachePromotionKey(source, userid);
        try {
            logger_.info("==>GetListPromotionDB==>Start:" + userid + " - key: " + keyPromotion);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion == null)
                lsPromotion = new ListPromotionObj(userid, videomax, onlinemax);
            else {
                lsPromotion.getLsPromotion().clear();
                lsPromotion.setVm(videomax);
                lsPromotion.setOm(onlinemax);
            }
            CallableStatement cs = conn.prepareCall("{call GameAdminPromotion_Get(?) }");
            cs.setInt("Userid", userid);
            logger_.info("==>GetListPromotionDB==>Set vao Cache1:" + keyPromotion + "-" + lsPromotion.getLsPromotion().size());
            ResultSet rs = cs.executeQuery();
            //List<PromotionObj> lsret = new ArrayList<PromotionObj>();
            while (rs.next()) {
                PromotionObj objReturn = new PromotionObj();
                objReturn.setUserid(userid);
                logger_.info("==>GetListPromotionDB==>top:" + rs.getInt("Gold"));
                objReturn.setGold(rs.getInt("Gold"));
                objReturn.setTypep(rs.getInt("Typep"));
                objReturn.setId(rs.getLong("Id"));
                lsPromotion.getLsPromotion().add(objReturn);
            }
            rs.close();
            cs.close();
            logger_.info("==>GetListPromotionDB==>Set vao Cache:" + userid + "-" + keyPromotion);
            logger_.info("==>GetListPromotionDB==>Set vao Cache:" + userid + "-" + lsPromotion.getLastPromotion() + "-" + lsPromotion.getLsPromotion().size());
            UserController.getCacheInstance().set(keyPromotion, lsPromotion, 0);
            logger_.info("==>GetListPromotionDB==>return:");
            return lsPromotion;
        } catch (Exception e) {
            // handle exception
            logger_.error("GetListPromotionDB:" + userid + "-" + e.getMessage());
            UserController.getCacheInstance().set(keyPromotion, new ListPromotionObj(userid, videomax, onlinemax), 0);
            e.printStackTrace();
            return new ListPromotionObj(userid, videomax, onlinemax);
        } finally {
            instance.releaseDbConnection(conn);
        }

    }
    
    public static ListPromotionObj GetListPromotionDB2(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        String keyPromotion = ServerDefined.getKeyPromotion(source) + userid;
        try {
            logger_.info("==>GetListPromotionDB==>Start:" + userid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion == null)
                lsPromotion = new ListPromotionObj(userid, videomax, onlinemax);
            else {
                lsPromotion.getLsPromotion().clear();
                lsPromotion.setVm(videomax);
                lsPromotion.setOm(onlinemax);
            }
            CallableStatement cs = conn.prepareCall("{call GameAdminPromotion_Get(?) }");
            cs.setInt("Userid", userid);
            logger_.info("==>GetListPromotionDB==>Set vao Cache1:" + keyPromotion + "-" + lsPromotion.getLsPromotion().size());
            ResultSet rs = cs.executeQuery();
            //List<PromotionObj> lsret = new ArrayList<PromotionObj>();
            while (rs.next()) {
                PromotionObj objReturn = new PromotionObj();
                objReturn.setUserid(userid);
                logger_.info("==>GetListPromotionDB==>top:" + rs.getInt("Gold"));
                objReturn.setGold(rs.getInt("Gold"));
                objReturn.setTypep(rs.getInt("Typep"));
                objReturn.setId(rs.getLong("Id"));
                lsPromotion.getLsPromotion().add(objReturn);
            }
            rs.close();
            cs.close();
            logger_.info("==>GetListPromotionDB==>Set vao Cache:" + userid + "-" + keyPromotion);
            logger_.info("==>GetListPromotionDB==>Set vao Cache:" + userid + "-" + lsPromotion.getLastPromotion() + "-" + lsPromotion.getLsPromotion().size());
            UserController.getCacheInstance().set(keyPromotion, lsPromotion, 0);
            logger_.info("==>GetListPromotionDB==>return:");
            return lsPromotion;
        } catch (Exception e) {
            // handle exception
            logger_.error("GetListPromotionDB:" + userid + "-" + e.getMessage());
            UserController.getCacheInstance().set(keyPromotion, new ListPromotionObj(userid, videomax, onlinemax), 0);
            e.printStackTrace();
            return new ListPromotionObj(userid, videomax, onlinemax);
        } finally {
            instance.releaseDbConnection(conn);
        }

    }


    public void processPromotionInviteFaceB(int source, int userId) {
        try {
            if (userId > ServerDefined.userMap.get(source)) userId = userId - ServerDefined.userMap.get(source);
            if (userController != null) {
                String key = userController.genCacheUserInfoKey(source, userId);
                UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
                if (uinfo != null) {
                    Integer userReceiveId = (Integer) UserController.getCacheInstance().get(
                            KeyCachedDefine.getKeyCachedGiftCode(userId, uinfo.getDeviceId()));
                    if (userReceiveId != null) {
                        String keyUserReceive = userController.genCacheUserInfoKey(source, userReceiveId);
                        UserInfo uReceive = (UserInfo) UserController.getCacheInstance().get(keyUserReceive);

                        if (uReceive == null) return;

                        int gameCount = userController.getPlayerGameCountCache(source, userId);
                        int goldBonus = 0;
                        switch (gameCount) {
                            case 50:
                                goldBonus = 240000;
                                break;
                            case 20:
                                goldBonus = 100000;
                                break;
                            case 10:
                                goldBonus = 100000;
                                break;
                            default:
                                break;
                        }
                        if (goldBonus == 0) return;
                        String msg = ServiceImpl.actionUtils.getServiceText("str_mess_giftcode_1", uinfo.language);
                        msg = String.format(msg, uinfo.getUsername(), goldBonus);

                        QueueCommand cmd = new GameIBonusInviteFriendsCmd(
                                uinfo.getSource(), PromotionType.TYPE_INVITE_FACEBOOK, userReceiveId,
                                uReceive.getUsername(), goldBonus, msg
                        );
                        QueueManager.getInstance(UserController.queuename).put(cmd);
                    }
                }
            }
        } catch (Exception e) {
            logger_debug.error(e.getMessage(), e);
        }
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

     /*
     * Create Promotion
     */
    public long CreatePromotionDB(int source, int userid, int typep, int gold, String deviceid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameAdminPromotion_Create(?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Typep", typep);
            cs.setInt("Gold", gold);
            cs.setString("Deviceid", deviceid);
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (SQLException ex) {
            logger_.error("CreatePromotionDB:" + userid + "-" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public long CreatePromotionBotDB(int source, int userid, int typep, long gold, String deviceid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameAdminPromotion_Create_New(?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Typep", typep);
            cs.setLong("Gold", gold);
            cs.setString("Deviceid", deviceid);
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (SQLException ex) {
            logger_.error("CreatePromotionDB:" + userid + "-" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    /*
     * Remove Promotion in DB
     */
    public void RemovePromotionDB(int source, int userid, int typep, int gold) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameAdminPromotion_Delete(?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Typep", typep);
            cs.setInt("Gold", gold);
            cs.execute();
        } catch (SQLException ex) {
            logger_.error("RemovePromotionDB:" + userid + "-" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /************************************** Cache *************************************/
    private String genCachePromotionKey(int source, int userId) {
        return ServerDefined.getKeyPromotion(source) + userId;
    }

    private String genCacheDeviceIdKey(int source, String deviceid) { //Cache DeviceId
        return ServerDefined.getKeyCacheDevice(source) + deviceid;
    }

    public void ResetPromotion(int source, int userid, int cvideo, int conline) {
        try {
            String keyPromotion = genCachePromotionKey(source, userid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion != null) {
                lsPromotion.setOc(cvideo);
                lsPromotion.setVc(conline);
                lsPromotion.setCountInviteFacebook(0);
                lsPromotion.getLsPromotion().clear();
                lsPromotion.setLastPromotion(System.currentTimeMillis());
                lsPromotion.setLastPromotionVideo(System.currentTimeMillis());
                lsPromotion.getLsPromotion().clear();
            }
            UserController.getCacheInstance().set(keyPromotion, lsPromotion, 0);
        } catch (Exception e) {
            // handle exception
            logger_.error("ResetPromotion:" + userid + "-" + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * Get List FollowFriend
     */
    public ListPromotionObj GetListPromotion(int source, int userid) {
        try {
            String keyPromotion = genCachePromotionKey(source, userid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion != null) {
                return lsPromotion;
            } else {
                return new ListPromotionObj(userid, videomax, onlinemax); //GetListFollowFriendDB(source, userid) ;
            }
        } catch (Exception e) {
            // handle exception
            logger_.error("GetListPromotion:" + userid + "-" + e.getMessage());
            e.printStackTrace();
            return new ListPromotionObj(userid, videomax, onlinemax);
        }
    }

    public boolean CheckPromotion(int source, int uid) {
        try {
            logger_.info("CheckPromotion:");
            String keyPromotion = genCachePromotionKey(source, uid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion == null)
                return true;
            logger_.info("CheckPromotion:" + lsPromotion.getLsPromotion().size());
            //Khong co tien tang (tru tien online)
            if (lsPromotion.getLsPromotion().size() > 0) {
                for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                    if (lsPromotion.getLsPromotion().get(i).getTypep() != 3) return false;
                }
                return true;
            } else
                return true;
        } catch (Exception e) {
            // handle exception
            logger_.error("CheckPromotion:" + uid + "-" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public PromotionObj CreatePromotion(int source, int uid, int typep, int gold, String deviceid) { //Type - 17: DailyPromotion
        try {
            logger_.info("CreatePromotion:" + typep + "-" + gold + "-" + uid);
            PromotionObj objReturn = new PromotionObj();
            objReturn.setUserid(uid);
            objReturn.setTypep(typep);
            objReturn.setGold(gold);
            //Add to Cache
            if (typep != PromotionType.TYPE_DAILY) {//DailyPromotion nhận luôn rồi
                String keyPromotion = genCachePromotionKey(source, uid);
                ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
                if (lsPromotion == null)
                    lsPromotion = new ListPromotionObj(uid, videomax, onlinemax);
                lsPromotion.getLsPromotion().add(objReturn);
                if (typep == PromotionType.TYPE_COUNT_TIME)
                    lsPromotion.setOc(lsPromotion.getOc() + 1);
                else if (typep == PromotionType.TYPE_WATCH_VIDEO)
                    lsPromotion.setVc(lsPromotion.getVc() + 1);
                else if (typep == PromotionType.TYPE_INVITE_FACEBOOK)
                    lsPromotion.setCountInviteFacebook(lsPromotion.getCountInviteFacebook() + 1);
                UserController.getCacheInstance().set(keyPromotion, lsPromotion, 0);
            }
            UserInfoCmd cmd = new UserInfoCmd("gameCreatePromotion", source, uid, typep, gold, deviceid);
            QueueManager.getInstance(UserController.queuename).put(cmd);
            return objReturn;
        } catch (Exception e) {
            // handle exception
            logger_.error("CreatePromotion:" + uid + "-" + e.getMessage());
            e.printStackTrace();
            return new PromotionObj();
        }
    }

    /*
     * Remove FollowFriend in Cache
     */
    public String RemovePromotionInCache(int source, int uid, int typep, int gold, int vip) { //Remove Promotion
        try {
            int goldreturn = 0;
            long time = 0;
            int nextvideo = 0;
            int nextonline = 0;
            String keyPromotion = genCachePromotionKey(source, uid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion != null) {
                logger_.info("==>RemovePromotionInCache:" + keyPromotion + "-" + uid + "-" + gold + "-" + typep + "-" + lsPromotion.getLsPromotion().size());
                for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                    logger_.info("==>RemovePromotionInCache Detail:" + lsPromotion.getLsPromotion().get(i).getTypep() + "-" + lsPromotion.getLsPromotion().get(i).getGold() + "-" + lsPromotion.getOc());
                    if (lsPromotion.getLsPromotion().get(i).getTypep() == typep && lsPromotion.getLsPromotion().get(i).getGold() == gold) {
                        goldreturn = lsPromotion.getLsPromotion().get(i).getGold();
                        lsPromotion.getLsPromotion().remove(i);
                        if (typep == PromotionType.TYPE_COUNT_TIME) { //Nhan gold online ==> Dem lai gio
                            lsPromotion.setLastPromotion(System.currentTimeMillis());
                            time = CheckPromotionOnline(source, vip, lsPromotion.getOc());
                        } else if (typep == PromotionType.TYPE_WATCH_VIDEO) //Video online
                            lsPromotion.setLastPromotionVideo(System.currentTimeMillis());
                        break;
                    }
                }
                nextvideo = getChipNextVideo(lsPromotion.getVc(), lsPromotion.getVm());
                if (lsPromotion.getOc() < lsPromotion.getOm())
                    nextonline = ServerDefined.online_policy.get(source).get(vip).getChipBonus()[lsPromotion.getOc()];
                else
                    nextonline = ServerDefined.online_policy.get(source).get(vip).getChipBonus()[0];

                UserController.getCacheInstance().set(keyPromotion, lsPromotion, 0);
            }
            UserInfoCmd cmd = new UserInfoCmd("gamePromotionRemove", source, uid, typep, gold);
            QueueManager.getInstance(UserController.queuename).put(cmd);
            return goldreturn + ";" + time + ";" + nextvideo + ";" + nextonline + ";";
        } catch (Exception ex) {
            logger_.error("RemovePromotionInCache:" + uid + "-" + ex.getMessage());
            ex.printStackTrace();
            return "0;11;2000;2500;";
        }
    }

    private int getChipNextVideo(int vc, int vm) {
        try {
            if (vc < 1)
                return 3000;
            else if (vc < videomax)
                return 2000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /*
     * Get Promotion Info
     */
    public String GetPromotionInfo(int source, int uid, String deviceId, int vip, String ref) { //Remove Promotion
        try {
            String goldreturn = "";
            String keyPromotion = genCachePromotionKey(source, uid);
            logger_.info("==>key get:" + keyPromotion + "-" + uid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion == null)
                lsPromotion = new ListPromotionObj(uid, videomax, onlinemax);
            else {
                lsPromotion.setVm(videomax);
                lsPromotion.setOm(onlinemax);
            }
            goldreturn = "";
            logger_.info("==>Time online:" + lsPromotion.getLsPromotion().size() + "-" + lsPromotion.getLastPromotion());
            boolean t = false;
            for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                if (lsPromotion.getLsPromotion().get(i).getTypep() == PromotionType.TYPE_NOT_ENOUGH_GOLD) {
                    goldreturn = goldreturn + lsPromotion.getLsPromotion().get(i).getGold() + ";"; //0.Het tien
                    t = true;
                    break;
                }
            }
            if (!t)
                goldreturn = goldreturn + "0;";
            t = false;
            for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                if (lsPromotion.getLsPromotion().get(i).getTypep() == PromotionType.TYPE_FORTUITY) {
                    goldreturn = goldreturn + lsPromotion.getLsPromotion().get(i).getGold() + ";"; //1.Admin tang
                    t = true;
                    break;
                }
            }
            if (!t)
                goldreturn = goldreturn + "0;";
            t = false;
            for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                if (lsPromotion.getLsPromotion().get(i).getTypep() == PromotionType.TYPE_UP_VIP) {
                    goldreturn = goldreturn + lsPromotion.getLsPromotion().get(i).getGold() + ";"; //2.Len vip
                    t = true;
                    break;
                }
            }
            if (!t)
                goldreturn = goldreturn + "0;";
            t = false;
            for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                if (lsPromotion.getLsPromotion().get(i).getTypep() == PromotionType.TYPE_COUNT_TIME) {
                    goldreturn = goldreturn + lsPromotion.getLsPromotion().get(i).getGold() + ";"; //3.Dem thoi gian
                    t = true;
                    break;
                }
            }
            if (!t)
                goldreturn = goldreturn + "0;";
            t = false;
            for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                if (lsPromotion.getLsPromotion().get(i).getTypep() == PromotionType.TYPE_WATCH_VIDEO) {
                    goldreturn = goldreturn + lsPromotion.getLsPromotion().get(i).getGold() + ";"; //4.Xem video
                    t = true;
                    break;
                }
            }
            if (!t)
                goldreturn = goldreturn + "0;";
            t = false;
            for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                if (lsPromotion.getLsPromotion().get(i).getTypep() == PromotionType.TYPE_GIFT_CODE) {
                    goldreturn = goldreturn + lsPromotion.getLsPromotion().get(i).getGold() + ";"; //5.Code
                    t = true;
                    break;
                }
            }
            if (!t)
                goldreturn = goldreturn + "0;";
            if (deviceId.length() > 0 && deviceId.indexOf("web") == -1) {
                String key = genCacheDeviceIdKey(source, deviceId);
                PromotionDevice cpromotion = (PromotionDevice) UserController.getCacheInstance().get(key);
                if (cpromotion != null) {
                    logger_.info("==>Get in Device in Cache:" + cpromotion.getCOnline() + "-" + cpromotion.getCVideo());
                    if (cpromotion.getCOnline() > lsPromotion.getOc())
                        lsPromotion.setOc(cpromotion.getCOnline());
                    if (cpromotion.getCVideo() > lsPromotion.getVc())
                        lsPromotion.setVc(cpromotion.getCVideo());
                    if (lsPromotion.getLastPromotion() < cpromotion.getLastPromotionOnline())
                        lsPromotion.setLastPromotion(cpromotion.getLastPromotionOnline());
                    lsPromotion.setLastPromotionVideo(cpromotion.getLastPromotionVideo());
                } else {
                    cpromotion = new PromotionDevice(0, System.currentTimeMillis());
                    UserController.getCacheInstance().set(key, cpromotion, 0);
                }
                logger_.info("==>Vc and OC in Device:" + key + "-" + lsPromotion.getVc() + "-" + lsPromotion.getOc());
            }
            if (lsPromotion.getOc() >= lsPromotion.getOm()) {
                Calendar cal = Calendar.getInstance(); //current date and time
                cal.set(Calendar.HOUR_OF_DAY, 23); //set hour to last hour
                cal.set(Calendar.MINUTE, 59); //set minutes to last minute
                cal.set(Calendar.SECOND, 59); //set seconds to last second
                cal.set(Calendar.MILLISECOND, 999); //set milliseconds to last millisecond
                long currentTime = System.currentTimeMillis();
                long millis = cal.getTimeInMillis();
                long re = (millis - currentTime) / 1000;
                goldreturn = goldreturn + String.valueOf(re + 1) + ";";
                lsPromotion.setOc(lsPromotion.getOm());
            } else {
                long timeonline = (System.currentTimeMillis() - lsPromotion.getLastPromotion()) / 1000; //thoi gian con lai
                int timeWaiting = CheckPromotionOnline(source, vip, lsPromotion.getOc());
                //if (lsPromotion.getOc() < lsPromotion.getOm())
                //	timeWaiting	= ServerDefined.online_policy.get(source).get(vip).getTimeWaiting()[lsPromotion.getOc()] * 60 ;
    			/*if (vip < 2) {
    				if (lsPromotion.getOc() == 0)
    					timeWaiting = 120 ;
    				else if (lsPromotion.getOc() == 1)
    					timeWaiting = 300 ;
    			} else {
    				if (lsPromotion.getOc() == 0)
    					timeWaiting = 900 ;
    				else if (lsPromotion.getOc() == 1)
    					timeWaiting = 1800 ;
    			}*/
                logger_.info("==>Promotion Info==>TimeOnline:" + timeonline + "-" + timeWaiting);
                if (timeonline > timeWaiting)
                    timeonline = 0;
                else
                    timeonline = timeWaiting - timeonline + 1;
                goldreturn = goldreturn + String.valueOf(timeonline) + ";"; //6 time online
            }
            if (ref.indexOf("cocos_9kthai_ios_5") != -1)
                lsPromotion.setVc(lsPromotion.getVm());
            goldreturn = goldreturn + lsPromotion.getVc() + ";";
            goldreturn = goldreturn + lsPromotion.getVm() + ";";
            goldreturn = goldreturn + lsPromotion.getOc() + ";";
            goldreturn = goldreturn + lsPromotion.getOm() + ";";
            if (lsPromotion.getVm() > videomax)
                lsPromotion.setVm(videomax);
            UserController.getCacheInstance().set(keyPromotion, lsPromotion, 0);
            if (lsPromotion.getVc() <= 1 || (lsPromotion.getVc() == lsPromotion.getVm()))
                goldreturn = goldreturn + "3000;";
            else if (lsPromotion.getVc() <= 5)
                goldreturn = goldreturn + "2000;";
            else
                goldreturn = goldreturn + "1000;";
            if (lsPromotion.getOc() < lsPromotion.getOm())
                goldreturn = goldreturn + ServerDefined.online_policy.get(source).get(vip).getChipBonus()[lsPromotion.getOc()] + ";";
            else
                goldreturn = goldreturn + ServerDefined.online_policy.get(source).get(vip).getChipBonus()[0] + ";";
            logger_.info("==>Return:" + goldreturn);
            return goldreturn;
        } catch (Exception ex) {
            logger_.error("GetPromotionInfo:" + uid + "-" + ex.getMessage());
            ex.printStackTrace();
            return "0;0;0;0;0;3;0;" + videomax + ";" + videomax + ";" + onlinemax + ";" + onlinemax + ";0;0";
        }
    }

    private void UpdateCPromotionDeviceToCache(int source, String deviceId, int cvideo, int conline) { //Update Promotion by DeviceId
        try {
            if (deviceId.length() == 0) return;
            if (deviceId.indexOf("web") != -1) return;
            String key = genCacheDeviceIdKey(source, deviceId);
            PromotionDevice cpromotion = (PromotionDevice) UserController.getCacheInstance().get(key);
            if (cpromotion != null) {
                cpromotion.setCVideo(cpromotion.getCVideo() + cvideo);
                cpromotion.setCOnline(cpromotion.getCOnline() + conline);
                if (cvideo > 0)
                    cpromotion.setLastPromotionVideo(System.currentTimeMillis());
                else if (conline > 0)
                    cpromotion.setLastPromotionOnline(System.currentTimeMillis());
                UserController.getCacheInstance().set(key, cpromotion, 0);
            } else {
                cpromotion = new PromotionDevice(0, System.currentTimeMillis());
                cpromotion.setCOnline(cvideo);
                cpromotion.setCVideo(conline);
                UserController.getCacheInstance().set(key, cpromotion, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private int CheckPromotionOnline(int source, int vip, int conline) {
        logger_.info("==>Get Condition online:" + source + "-" + vip + "-" + conline);
        if (conline < 6)
            return ServerDefined.online_policy.get(source).get(vip).getTimeWaiting()[conline] * 60;
        else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.clear(Calendar.HOUR_OF_DAY);
            cal.clear(Calendar.HOUR);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            Date tomorrow = cal.getTime();
            return (int) ((tomorrow.getTime() - System.currentTimeMillis()) / 1000 - 43200);
        }
		/*if (vip < 2) {
			if (conline == 0) return 120 ;
			if (conline == 1) return 300 ;
			if (conline == 2) return 900 ;
			if (conline == 4) return 1800 ;
			if (conline == 5) return 1800 ;
		} else {
			if (conline == 0) return 900 ;
			if (conline == 1) return 1800 ;
			if (conline == 2) return 1800 ;
			if (conline == 4) return 3600 ;
			if (conline == 5) return 3600 ;
		}
		return 1000 ;*/
    }

    /*
     * Promotion Online
     */
    public String PromotionOnline(int source, int uid, String deviceid, int vip) {
        try {
            int goldreturn = 0;
            int nextgold = 0;
            String keyPromotion = genCachePromotionKey(source, uid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            logger_.info("==>promotion online start");
            if (lsPromotion != null) {
                logger_.info("==>promotion online:" + lsPromotion.getOc() + "-" + lsPromotion.getOm());
                if (lsPromotion.getOc() < lsPromotion.getOm()) {
                    boolean t = false;
                    int agonline = 0;
                    for (int i = 0; i < lsPromotion.getLsPromotion().size(); i++) {
                        if (lsPromotion.getLsPromotion().get(i).getTypep() == PromotionType.TYPE_COUNT_TIME) {
                            t = true;
                            agonline = lsPromotion.getLsPromotion().get(i).getGold();
                            break;
                        }
                    }
                    logger_.info("==>promotion online exists:" + t);
                    if (!t) {
                        long diffsecond = (System.currentTimeMillis() - lsPromotion.getLastPromotion()) / 1000;
                        int secondCondition = CheckPromotionOnline(source, vip, lsPromotion.getOc());
                        logger_.info("==>promotion online time:" + diffsecond + "-" + vip + "-" + secondCondition + "-" + uid);
                        if (diffsecond > secondCondition) {
                            if (lsPromotion.getOc() < 6)
                                goldreturn = ServerDefined.online_policy.get(source).get(vip).getChipBonus()[lsPromotion.getOc()];
                            else
                                goldreturn = 0;
							/*if (lsPromotion.getOc() < 2) {
								goldreturn = 2500 ;
							} else {
								goldreturn = 3000 ; //Lan thu 4 ==> vong quay may man.
							}
							//if (lsPromotion.getOc() < 2)
								nextgold = 2500 ;
							//else 
							//	nextgold = 0 ;*/
                            logger_.info("==>promotion online value:" + goldreturn + "-" + deviceid + "-" + uid);
                            CreatePromotion(source, uid, PromotionType.TYPE_COUNT_TIME, goldreturn, deviceid);
                            //Update vao trong cache so luong online by Device
                            if (deviceid.indexOf("web") == -1)
                                UpdateCPromotionDeviceToCache(source, deviceid, 0, 1);
//							lsPromotion.setLastPromotion(System.currentTimeMillis());
//							UserController.getCacheInstance().set(keyPromotion, lsPromotion, 0);
                        }
                    } else {
                        goldreturn = agonline;
                    }
                }
            }
            logger_.info("==>promotion online end:" + goldreturn);
            return String.valueOf(goldreturn) + ";" + String.valueOf(nextgold) + ";";
        } catch (Exception e) {
            // handle exception
            logger_.error("PromotionOnline:" + uid + "-" + e.getMessage());
            e.printStackTrace();
            return "0;0";
        }
    }

    /*
     * Promotion Online
     */
    public int PromotionVideoStart(int source, int uid, String ref) {
        try {
            logger_.info("==>PromotionVideoStart:" + uid + "-" + ref);
            if (ref.indexOf("cocos_9kthai_ios_5") != -1)
                return 0;
            if (ref.indexOf("web") != -1)
                return 0;
            String keyPromotion = genCachePromotionKey(source, uid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion != null) {
                logger_.info("==>PromotionVideoStart1:" + uid);
                long diffSecond = (System.currentTimeMillis() - lsPromotion.getLastPromotionVideo()) / 1000;
                logger_.info("==>PromotionVideoStart:" + diffSecond + "-" + lsPromotion.getVc() + "-" + lsPromotion.getVm());
                if (lsPromotion.getVc() == 0)
                    return 1;
                if (lsPromotion.getVc() == 1) {
                    if (diffSecond >= 180)
                        return 1;
                    else return 180 - (int) diffSecond;
                } else if (lsPromotion.getVc() == 2) {
                    if (diffSecond >= 300)
                        return 1;
                    else
                        return 300 - (int) diffSecond;
                } /*else if (lsPromotion.getVc() == 3) {
					if (diffSecond >= 600)
						return 1 ;
					else
						return 600 - (int)diffSecond ;
				} else if (lsPromotion.getVc() == 4) {
					if (diffSecond >= 900)
						return 1 ;
					else
						return 900 - (int)diffSecond ;
				} else if (lsPromotion.getVc() == 5) {
					if (diffSecond >= 1200)
						return 1 ;
					else
						return 1200 - (int)diffSecond ;
				} else if (lsPromotion.getVc() <= 10) {
					if (diffSecond >= 1800)
						return 1 ;
					else
						return 1800 - (int)diffSecond ;
				}*/ else
                    return 0;
            }
            logger_.info("==>PromotionVideoEnd:" + uid);
            return 0;
        } catch (Exception e) {
            // handle exception
            logger_.error("PromotionOnline:" + uid + "-" + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public String PromotionVideo(int source, int uid, String deviceid) {
        try {
            logger_.info("==>PromotionVideo:" + uid + "-" + deviceid);
            int goldreturn = 0;
            int nextgold = 0;
            String keyPromotion = genCachePromotionKey(source, uid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion != null) {
                logger_.info("==>PromotionVideo:" + lsPromotion.getVc() + "-" + lsPromotion.getVm());
                if (lsPromotion.getVc() < lsPromotion.getVm()) {
                    if (lsPromotion.getVc() <= 1) {
                        goldreturn = 3000;
                    } else if (lsPromotion.getVc() <= 3) {
                        goldreturn = 2000;
                    }
                    nextgold = getChipNextVideo(lsPromotion.getVc(), lsPromotion.getVm());
                    CreatePromotion(source, uid, PromotionType.TYPE_WATCH_VIDEO, goldreturn, deviceid);
                    //Update vao trong cache so luong online by Device
                    if (deviceid.indexOf("web") == -1)
                        UpdateCPromotionDeviceToCache(source, deviceid, 1, 0);
                }
            }
            logger_.info("==>PromotionVideo end:" + goldreturn);
            return String.valueOf(goldreturn) + ";" + String.valueOf(nextgold) + ";";
        } catch (Exception e) {
            // handle exception
            logger_.error("PromotionOnline:" + uid + "-" + e.getMessage());
            e.printStackTrace();
            return "0;0";
        }
    }

    public String PromotionInviteFriend(int source, int uid, String deviceid, int size) {
        try {
            int bonusgold = 500;
            int maxgold = 25000;
            int maxInvite = 50;
            if (source == ServerSource.INDIA_SOURCE) {
                maxgold = 50000;
                bonusgold = 1000;
            }
            logger_.info("==>PromotionInviteFriend: " + uid + " - size: " + size + " - deviceid: " + deviceid);
            int goldreturn = 0;
            int nextgold = 0;
            String keyPromotion = genCachePromotionKey(source, uid);
            ListPromotionObj lsPromotion = (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
            if (lsPromotion != null) {
                int count = lsPromotion.getCountInviteFacebook();
                logger_.info("==>PromotionInviteFriend: uid: " + uid + " - invited: " + count);
                if (count < maxInvite) {
                    if (count + size > maxInvite) size = maxInvite - count;
                    goldreturn = size * bonusgold;
                    nextgold = maxgold - (count + size) * bonusgold;
                    lsPromotion.setCountInviteFacebook(count + size);
                    CreatePromotion(source, uid, PromotionType.TYPE_INVITE_FACEBOOK, goldreturn, deviceid);
                }
            }
            logger_.info("==>PromotionInviteFriend end: goldreturn: " + goldreturn + "  - nextgold: " + nextgold);
            return String.valueOf(goldreturn) + ";" + String.valueOf(size) + ";";
        } catch (Exception e) {
            // handle exception
            logger_.error("PromotionOnline:" + uid + "-" + e.getMessage());
            e.printStackTrace();
            return "0;0";
        }
    }
}
