/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.database;

import com.athena.log.LoggerKey;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.cachebase.libs.queue.QueueCommand;
import com.cachebase.queue.LogIUUserExperienceDt_UsingCmd;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.ina.IAP_IOS_ITEM_IN_APP;
import com.athena.services.ina.PaymentIAP_IOS;
import com.athena.services.promotion.ListPromotionObj;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.AlertPromotion;
import com.athena.services.vo.AlertSchedule;
import com.athena.services.vo.Auction;
import com.athena.services.vo.CardLucky;
import com.athena.services.vo.CashOut;
import com.athena.services.vo.ConfigUpdate;
import com.athena.services.vo.DisplayRule;
import com.athena.services.vo.GiftCode;
import com.athena.services.vo.JackpotWin;
import com.athena.services.vo.JackpotWinList;
//import com.athena.services.vo.MPromote;
import com.athena.services.vo.MarkCreateTable;
import com.athena.services.vo.Match;
import com.athena.services.vo.PolicyPromotionDetail;
//import com.athena.services.vo.PPromote;
import com.athena.services.vo.PromotionDevice;
import com.athena.services.vo.RotationLucky;
import com.athena.services.vo.Roulette;
import com.athena.services.vo.Slot;
import com.athena.services.vo.TopGamer;
import com.athena.services.vo.UserAfterPay;
import com.athena.services.vo.UserFace;
//import com.athena.services.vo.UserFace;
//import com.athena.services.vo.UserGame;
//import com.athena.services.vo.UserAuction;
import com.athena.services.vo.UserInfo;
import com.athena.services.vo.UserLucky;
import com.athena.services.vo.UserMsg;
import com.athena.services.vo.UserSetting;
import com.athena.services.vo.UserWinEvent;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.memcache.Memcache;
import com.cachebase.queue.UserInfoCmd;
import com.dst.ServerSource;
import com.vng.tfa.common.Config;
import com.vng.tfa.common.SqlService;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
/**
 * ƒ∂ƒ
 *
 * @author UserXP
 */
public class UserController implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1822207169041991756L;
    private static final Lock _createLock = new ReentrantLock();
    private static UserController _instance;
    public static final String queuename = "gameUpdateDB";//"gameGetUserInfoByID";    
    static final int queuesize = 100000;
    static final int worker = 10;
    
    public static Logger logger_friend = Logger.getLogger(LoggerKey.FRIENDS);
    public static Logger logger_login_disconnect = Logger.getLogger(LoggerKey.LOGIN_DISCONNECT);
    public static Logger logger_promotion_handler = Logger.getLogger(LoggerKey.PROMOTION_HANDLER);
    public static Logger logger_ag_update = Logger.getLogger(LoggerKey.AG_UPDATE);
    public static Logger logger_tai_xiu = Logger.getLogger(LoggerKey.TAI_XIU);
    public static Logger logger_login_execute_time = Logger.getLogger(LoggerKey.LOGIN_EXECUTE_TIME);
    public static Logger logger_debug_user_control = Logger.getLogger(LoggerKey.DEBUG_USER_CONTROL);

    static {
        QueueManager.getInstance(queuename).init(queuename, worker, queuesize);
        QueueManager.getInstance(queuename).process();
    }

    public static UserController getInstance() {
        if (_instance == null) {
            _createLock.lock();
            try {
                if (_instance == null) {
                    _instance = new UserController();
                }
            } finally {
                _createLock.unlock();
            }
        }
        return _instance;
    }

    public static Memcache getCacheInstance() {
        try {
            String host = Config.getParam("cachehost");//"192.168.10.112";// "Cache DT";
            int port = Integer.parseInt(Config.getParam("cacheport"));//11211 //replace by your port;

            return Memcache.getInstance(host, port);
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }
        return null;
    }



    public String genCacheUserInfoKey(int source, int userId) {
        return ServerDefined.getKeyCache(source) + userId;
    }

    private String genCacheIdUserInfoKey(int source, String username) {
        return ServerDefined.getKeyCacheId(source) + username;
    }

    public int GetUserIdByAccesstoken(int source, String accesstoken) {
        try {
            Integer userid = (Integer) UserController.getCacheInstance().get(accesstoken);
            if (userid == null) {
                return 0;
            } else {
                return userid.intValue();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public Long GetUserFaceByAccesstoken(String accesstoken) {
        try {
            return (Long) UserController.getCacheInstance().get(accesstoken);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String getUserIdZingFromCache(String accesstoken) {
        try {
            return (String) UserController.getCacheInstance().get(accesstoken);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void gameIBonusInviteFriends(int source, Integer type,int uid,String toUser, long ag, String mess){
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIBonusInviteFriends(?, ? , ? , ?, ?)}");
            cs.setInt("UserId", uid);
            cs.setString("Username", toUser);
            cs.setLong("Chip", ag);
            cs.setString("Msg", mess);
            cs.setInt("type", type);

            cs.execute();
        }catch (Exception e){
            e.printStackTrace();
            try {
                System.out.println((new Date()) + "==>UserController==>GetUserFaceFromMapping: source: " + source);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GetUserFaceFromMapping(int source, long userFaceId) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserId_FaceMapping(?,?) }");
            cs.setLong("FaceId", userFaceId);
            cs.registerOutParameter("Userid", Types.INTEGER);
            cs.execute();
            return cs.getInt("Userid");
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                System.out.println((new Date()) + "==>UserController==>GetUserFaceFromMapping: source: " + source + " - uid: " + userFaceId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void AddUserFaceToCache(String accesstoken, Long faceid) {
        try {
            UserController.getCacheInstance().set(accesstoken, faceid, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addUserIdZingToCache(String accesstoken, String userId) {
        try {
            UserController.getCacheInstance().set(accesstoken, userId, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void GetListLuckyUser(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        ServiceImpl.listUserLucky = new ArrayList<UserLucky>();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListLuckyUser() }");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                UserLucky obj = new UserLucky();
                obj.setUserid(rs.getInt("UserId"));
                obj.setGameid(rs.getInt("GameId"));
                obj.setVip(rs.getInt("Vip"));
                obj.setLuckyPercent(rs.getInt("LuckyPercent"));
                obj.setUnluckyPercent(rs.getInt("UnLuckyPercent"));
                obj.setAgmin(rs.getInt("AGMin"));
                obj.setAgmax(rs.getInt("AGMax"));
                obj.setTimestart(rs.getTimestamp("TimeStart").getTime());
                obj.setTimeend(rs.getTimestamp("TimeEnd").getTime());
                ServiceImpl.listUserLucky.add(obj);
            }
            System.out.println("==>List LuckyUser:" + ServiceImpl.listUserLucky.size());
            rs.close();
            cs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GetListDisplayRule(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListDisplayRule() }");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                DisplayRule obj = new DisplayRule();
                obj.setVersion(rs.getString("VersionId"));
                obj.setPackageid(rs.getString("PackageId"));
                obj.setOperatorid(rs.getInt("OperatorId"));
                obj.setOsid(rs.getString("OsId"));
                obj.setPublisherid(rs.getString("PublisherId"));
                obj.setSms(rs.getShort("SMS"));
                obj.setCard(rs.getShort("Card"));
                obj.setIap(rs.getShort("Iap"));
                obj.setAtm(rs.getShort("Atm"));
                obj.setListgame(rs.getString("Listgame"));
                obj.setCashout(rs.getShort("Cashout"));
                obj.setPayurl(rs.getString("PayUrl"));
                obj.setPaytypesms(rs.getShort("PaytypeSms"));
                obj.setPaytypecard(rs.getShort("PaytypeCard"));
                obj.setPayurlcard(rs.getString("PayUrlCard"));
                obj.setPayurlsms(rs.getString("PayUrlSms"));
                obj.setPayprefix(rs.getString("PayPrefix"));
                obj.setPayurldisplay(rs.getString("PayUrlDisplay"));
                obj.setBank(rs.getInt("Bank"));
                ServiceImpl.gameCountRule_Payment = rs.getInt("GameCountRule");
                ServiceImpl.vipRule_Payment = rs.getInt("VipRule");
                ServiceImpl.promotionRule_Payment = rs.getInt("PromotionRule");
                ServiceImpl.listDisplayRule.add(obj);
            }
            rs.close();
            cs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ******************* Get Promotion Policy *****************************
     */
    public void GetPromotionPolicy(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        if (ServerDefined.promotion_policy.containsKey(source)) {
            ServerDefined.promotion_policy.remove(source);
        }
        try {
            Map<Integer, PolicyPromotionDetail> temp = new HashMap<Integer, PolicyPromotionDetail>();
            CallableStatement cs = conn.prepareCall("{call GameGetListPolicy_Promotion() }");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                PolicyPromotionDetail obj = new PolicyPromotionDetail();
                obj.setVip(rs.getInt("Vip"));
                obj.setNumberP(rs.getInt("NumberP"));
                obj.setValueP(rs.getInt("ValueP"));
                obj.setValueTomorrow(rs.getInt("ValueTomorrow"));
                obj.setGoldHour(rs.getInt("GoldHour"));
                obj.setConditionP(rs.getInt("ConditionP"));
                temp.put(new Integer(obj.getVip()), obj);
            }
            rs.close();
            cs.close();
            ServerDefined.promotion_policy.put(new Integer(source), temp);
            System.out.println("==>Policy Promotion Vip 8:" + ServerDefined.promotion_policy.get(source).get(8).getConditionP() + "-" + ServerDefined.promotion_policy.get(source).get(8).getValueP());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //******************** Get Config Update *********************************/
    public void UpdateConfigUpdateforUser(int source, int userid, long updateTime) {
        //Update to cache
        String key = genCacheUserInfoKey(source, userid);
        UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
        if (uinfo != null) {
            uinfo.setDelPassLock(updateTime);
            UserController.getCacheInstance().set(key, uinfo, 0);
        }
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameConfigUpdate_UpdateUser(?) }");
            cs.setInt("UserId", userid);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void SetConfigUpdateDB(int source, int configvalue) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameConfigUpdate_Set(?) }");
            cs.setInt("ConfigValue", configvalue);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public ConfigUpdate GetConfigUpdate(int source) {
        try {
            String key = "configupdate_" + source;
            ConfigUpdate configUpdate = (ConfigUpdate) UserController.getCacheInstance().get(key);
            if (configUpdate == null) {
                return GetConfigUpdateDB(source);
            } else {
                return configUpdate;
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
            return new ConfigUpdate();
        }

    }

    public ConfigUpdate GetConfigUpdateDB(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameConfigUpdate_Get() }");
            ConfigUpdate objReturn = new ConfigUpdate();
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                objReturn.setConfigValue(rs.getInt("Getconfig"));
                objReturn.setTimeUpdate(rs.getTimestamp("LastUpdate").getTime());
            }
            rs.close();
            cs.close();
            String key = "configupdate_" + source;
            UserController.getCacheInstance().set(key, objReturn, 0);
            return objReturn;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ConfigUpdate();
        } finally {
            instance.releaseDbConnection(conn);
        }

    }

    /**
     * ******************* Cache By DeviceID ********************************
     */
    private String genCacheDeviceIdKey(int source, String deviceid) { //Cache DeviceId
        return ServerDefined.getKeyCacheDevice(source) + deviceid;
    }

    public PromotionDevice GetCPromotionByDeviceId(int source, String deviceId) { //Get Count Promotion By DeviceId
        try {
            String key = genCacheDeviceIdKey(source, deviceId);
            PromotionDevice cpromotion = (PromotionDevice) UserController.getCacheInstance().get(key);
            return cpromotion;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void UpdateCPromotionDeviceToCache(int source, String deviceId, int cpro) { //Update Promotion by DeviceId
        try {
            if (deviceId.length() == 0) {
                return;
            }
            if (deviceId.indexOf("web") != -1) {
                return;
            }
            String key = genCacheDeviceIdKey(source, deviceId);
            PromotionDevice cpromotion = (PromotionDevice) UserController.getCacheInstance().get(key);
            if (cpromotion != null) {
                cpromotion.setCPro(cpromotion.getCPro() + cpro);
                cpromotion.setLastLogin(System.currentTimeMillis());
                UserController.getCacheInstance().set(key, cpromotion, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void UpdateDailyPromotionDeviceToCache(int source, String deviceId) { //Update Promotion by DeviceId
        try {
            if (deviceId.length() == 0) {
                return;
            }
            if (deviceId.indexOf("web") != -1) {
                return;
            }
            String key = genCacheDeviceIdKey(source, deviceId);
            PromotionDevice cpromotion = (PromotionDevice) UserController.getCacheInstance().get(key);
            if (cpromotion != null) {
                cpromotion.setDailyPromotion(1);
                cpromotion.setLastLogin(System.currentTimeMillis());
                UserController.getCacheInstance().set(key, cpromotion, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void UpdatePromotionTomorrowToCache(int source, int uid) { //Update Promotion Tomorrow
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setUnlockPass((short) 2);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String CheckUserInCache(int source, int userId, long faceId, short gameId, String deviceId, int from, String username, String ipServer) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            String key = genCacheUserInfoKey(source, userId);
            UserInfo temp = (UserInfo) UserController.getCacheInstance().get(key);
            //System.out.println("==>CheckUserInCache: "+key+" - "+ActionUtils.gson.toJson(temp));
            if (temp == null) { //Da bi Remove khoi Cache  ==> Lay tu DB
                //Lay lai username dua vao Cache
                temp = new UserInfo();
                temp.setUserid(userId);
                temp.setFacebookid(faceId);
                temp.setGameid(gameId);
                temp.setDeviceId(deviceId);
                this.GetUserInfoAfterPayByUseridDb(source, temp, ipServer);
                if (temp.getAG().longValue() >= 0) {
                    UserController.getCacheInstance().set(key, temp, 0); //==>Set lai vao Cache
                }
                return String.valueOf(temp.getAG().longValue()) + ";" + String.valueOf(temp.getVIP()) + ";" + String.valueOf(temp.getLQ().intValue()) + ";" + String.valueOf(temp.getMarkVip()) + ";";
                /*if (temp.getAG().longValue() >= 0) {
                	try {
                		CallableStatement cs = conn.prepareCall("{call GameGetUserInfoAfterPay_New(?,?,?,?,?) }");
                		cs.setInt("Userid", userId);
                        cs.registerOutParameter("Vip", Types.INTEGER);
                        cs.registerOutParameter("Gold", Types.INTEGER);
                        cs.registerOutParameter("AG", Types.INTEGER);
                        cs.registerOutParameter("MarkVip", Types.INTEGER);
                        cs.execute();
                        //long AG = cs.getLong("AG") ;
                        int AG = cs.getInt("AG") ;
                        short vip = (short)cs.getInt("Vip") ;
                        int Gold = cs.getInt("Gold") ;
                        int Markvip = cs.getInt("MarkVip") ;
                        if (AG > 0) {
                    		temp.setAG((long) AG);
                        	temp.setVIP(vip);
                        	temp.setLQ(Gold);
                        	temp.setMarkVip(Markvip);
                            UserController.getCacheInstance().set(key, temp, 0); //==>Set lai vao Cache
                        }
                        return String.valueOf(AG) + ";" + String.valueOf(vip) + ";" + String.valueOf(Gold) + ";" + String.valueOf(Markvip) + ";" ;
            		} catch (Exception e) {
            			//handle exception
            			e.printStackTrace();
            		}
                }*/
            } else {
                return String.valueOf(temp.getAG().longValue()) + ";" + String.valueOf(temp.getVIP()) + ";" + String.valueOf(temp.getLQ().intValue()) + ";" + String.valueOf(temp.getMarkVip()) + ";";
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return "";
    }

    public void RemoveUserInfoByUserid(int source, int uid) {
        UserController.getCacheInstance().remove(genCacheUserInfoKey(source, uid));
    }

    public String UpdateAfterPaymentDB(int source, String transid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetInfoAfterPay_Indo(?,?,?,?,?,?) }");
            cs.setString("TranId", transid);
            cs.registerOutParameter("Gold", Types.INTEGER);
            cs.registerOutParameter("AG", Types.INTEGER);
            cs.registerOutParameter("UserId", Types.INTEGER);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.registerOutParameter("AGAdd", Types.INTEGER);
            cs.execute();
            int AG = cs.getInt("AG");
            int Gold = cs.getInt("Gold");
            int uid = cs.getInt("UserId");
            int vip = cs.getInt("Vip");
            int agAdd = cs.getInt("AGAdd");
            if (uid > 0) {
                String key = genCacheUserInfoKey(source, uid);
                UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
                if (uinfo != null) {
                    uinfo.setAG((long) AG);
                    uinfo.setVIP((short) vip);
                    uinfo.setLQ(Gold);
                    UserController.getCacheInstance().set(key, uinfo, 0);
                    return String.valueOf(AG) + ";" + String.valueOf(vip) + ";" + String.valueOf(Gold) + ";" + String.valueOf(agAdd) + ";" + String.valueOf(uid) + ";";
                }
            }
            return String.valueOf(AG) + ";" + String.valueOf(vip) + ";" + String.valueOf(Gold) + ";" + String.valueOf(agAdd) + ";" + String.valueOf(uid) + ";";
        } catch (Exception e) {
            e.printStackTrace();
            return "0;0;0;0;0;";
            //handle exception
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public String UpdateAGMarkVipAfterPayment(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetInfoAfterPay(?,?,?,?,?) }");
            cs.setInt("Userid", uid);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.registerOutParameter("Gold", Types.INTEGER);
            cs.registerOutParameter("AG", Types.INTEGER);
            cs.registerOutParameter("MarkVip", Types.INTEGER);
            cs.execute();
            int AG = cs.getInt("LQ");
            short vip = (short) cs.getInt("Vip");
            int Gold = cs.getInt("Gold");
            int Markvip = cs.getInt("MarkVip");
            if (AG > 0) {
                String key = genCacheUserInfoKey(source, uid);
                UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
                if (uinfo != null) {
                    uinfo.setAG((long) AG);
                    uinfo.setVIP(vip);
                    uinfo.setLQ(Gold);
                    uinfo.setMarkVip(Markvip);
                    UserController.getCacheInstance().set(key, uinfo, 0);
                    return String.valueOf(AG) + ";" + String.valueOf(vip) + ";" + String.valueOf(Gold) + ";" + String.valueOf(Markvip) + ";";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return "";
    }

    public void GetUserInfoByUserid(int source, UserInfo ulogin, String ipServer) { //Get Userinfo for Login
        try {
            UserInfo cache = this.GetUserInfoByUserid(source, ulogin.getUserid(), ulogin.getFacebookid(), ulogin.getGameid(), ulogin.getDeviceId(), ulogin.getSource(), ulogin.getUsername(), ipServer, ulogin.getOperatorid());
            ulogin.setAG(cache.getAG());
            ulogin.setVIP(cache.getVIP());
            ulogin.setMarkLevel(cache.getMarkLevel());
            ulogin.setIsOnline(cache.getIsOnline());
            ulogin.setBanned(cache.isBanned());
            ulogin.setCMsg(cache.getCMsg());
            ulogin.setMsgType((short) cache.getMsgType());
            ulogin.setCPromot(cache.getCPromot());
            ulogin.setTPromot(cache.getTPromot());
            ulogin.setMobile(cache.getMobile());
            ulogin.setRegister(cache.isRegister());
            ulogin.setAvatar((short) cache.getAvatar());
            ulogin.setRef(cache.getRef());
            ulogin.setIdolName(cache.getIdolName());
            ulogin.setUsernameOld(cache.getUsernameOld());
            ulogin.setUsername(cache.getUsername());
            ulogin.setUsernameLQ(cache.getUsernameLQ());
            ulogin.setOnlineDaily((short) cache.getOnlineDaily());
            ulogin.setPromotionDaily(cache.getPromotionDaily());
            ulogin.setLastDeviceID(cache.getDeviceId());
            ulogin.setLastLogin(cache.getLastLogin());
            ulogin.setCreateTime(cache.getCreateTime());
            ulogin.setMarkVip(cache.getMarkVip());
            ulogin.setGameCount(cache.getGameCount());
            ulogin.setReceiveDailyPromotion(cache.isReceiveDailyPromotion());
            ulogin.setPromotionValue(cache.getPromotionValue());
            //LQ
            ulogin.setLQ(cache.getLQ());
            ulogin.setDiamond(cache.getDiamond());
            ulogin.setGender(cache.getGender());
            ulogin.setMaxAmt(cache.getMaxAmt());
            ulogin.setOwnAmt(cache.getOwnAmt());
            ulogin.setPassLock(cache.getPassLock());
            ulogin.setDelPassLock(cache.getDelPassLock());
            ulogin.setNap(cache.isNap());
            ulogin.setGCountNow(cache.getGCountNow());
            ulogin.setChat(cache.getChat());
            ulogin.setSource((short) source);
            ulogin.setChessElo(cache.getChessElo());
            ulogin.setWinAccumulation(cache.getWinAccumulation()); //Moi for Tich luy theo lich su thang
            ulogin.setAGHigh(cache.getAGHigh());
            ulogin.setAGLow(cache.getAGLow());
            ulogin.setUnlockPass(cache.getUnlockPass());
            ulogin.setTableId(0);
            ulogin.setPassLQ(cache.getPassLQ()); //Daily Reward info

            // Process save map NAME - ID
            try {
                String name = "";
                if (cache.getUsernameLQ().length() > 0) {
                    name = cache.getUsernameLQ();
                } else {
                    name = cache.getUsername();
                }
                String keyMap_ID_Name = ServerDefined.getKeyCacheMapIdName(source) + ActionUtils.MD5(ActionUtils.ValidString(name));
                logger_friend.info("==>GetUserInfoByUserid==>mapid: keyMap_ID_Name: " + keyMap_ID_Name
                        + name + " - uid" + cache.getUserid());
                Integer uinfo = (Integer) UserController.getCacheInstance().get(keyMap_ID_Name);
                if (uinfo == null) {
                    UserController.getCacheInstance().set(keyMap_ID_Name, cache.getUserid(), 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String IpServerCurrent(int source, int userid) {
        try {
            String key = genCacheUserInfoKey(source, userid);
            UserInfo ulogin = (UserInfo) UserController.getCacheInstance().get(key);
            if (ulogin != null) {
                return ulogin.getIdolName();
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }
        return "";
    }

    public UserInfo GetUserInfoByUserid(int source, int userId, long faceId, short gameId, String deviceId, int from, String username, String ipServer, short operatorid) {
        UserInfo ulogin = null;
        try {
            String key = genCacheUserInfoKey(source, userId);
            int loop = 1;
            int maxLoop = 5;
            while (loop < maxLoop) {
                boolean checklock = UserController.getCacheInstance().checklock(key);
                if (!checklock) {
                    UserController.getCacheInstance().lock(key);
                    ulogin = this.GetUserInfoByUseridLoop(source, userId, faceId, gameId, deviceId, from, username, ipServer, operatorid);
                    UserController.getCacheInstance().unlock(key);
                    loop = maxLoop + 1;
                } else {
                    Thread.sleep(1000);
                    loop++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (ulogin == null) {
            ulogin = new UserInfo();
            ulogin.setUserid(userId);
            ulogin.setFacebookid(faceId);
            ulogin.setGameid(gameId);
            ulogin.setDeviceId(deviceId);
            ulogin.setSource((short) source);
            ulogin.setOperatorid(operatorid);
            this.GetUserInfoByUseridDb(source, ulogin, ipServer);
            ulogin.setIdolName(ipServer);
            if (((from != ServerSource.THAI_SOURCE) && (ulogin.getVIP() >= 2)) || (from == ServerSource.THAI_SOURCE)) {
                ulogin.setPromotionValue(this.GetPromotionValueDB(source, userId));
            }
            ulogin.setGameCount(this.GetGameCountDB(source, ulogin.getUserid())); //Lay GameCount tu DB
            //Update isonline DB
            if (ulogin != null) {
                if (ulogin.getAG() >= 0) {
                    this.updateUserInfoForCache(source, ulogin, false);
                }
            }
        }
        return ulogin;
    }

    public UserInfo GetUserInfoByUseridLoop(int source, int userId, long faceId, short gameId, String deviceId, int from, String username, String ipServer, short operatorid) {
        UserInfo ulogin = null;
        try {
            String key = genCacheUserInfoKey(source, userId);
            boolean isFromCache = false;
            ulogin = (UserInfo) UserController.getCacheInstance().get(key);
            if (ulogin == null) { //Lay tu DB
                logger_login_disconnect.info("==>LoginDB: " + userId);
                ulogin = new UserInfo();
                ulogin.setUserid(userId);
                ulogin.setFacebookid(faceId);
                ulogin.setGameid(gameId);
                ulogin.setDeviceId(deviceId);
                ulogin.setSource((short) source);
                ulogin.setOperatorid(operatorid);
                this.GetUserInfoByUseridDb(source, ulogin, ipServer);
                ulogin.setIdolName(ipServer);
                if (((from != ServerSource.THAI_SOURCE) && (ulogin.getVIP() >= 2)) || (from == ServerSource.THAI_SOURCE)) {
                    ulogin.setPromotionValue(this.GetPromotionValueDB(source, userId));
                }
                ulogin.setGameCount(this.GetGameCountDB(source, ulogin.getUserid())); //Lay GameCount tu DB
                updatePromotion(ulogin, source, deviceId, userId, ActionUtils.getFirstTimeOfDate());
            } else { //Lay tu Cache : Tinh lai OnlineDaily
                isFromCache = true;
                long firstTime = ActionUtils.getFirstTimeOfDate();
                logger_login_disconnect.info("==>LoginCache==>CPromotion: " + userId + "-" + ulogin.getCPromot() + "-" + ulogin.getUsername() + "-" + ulogin.getOperatorid() + "-" + ulogin.getAG().intValue() + "-" + ulogin.getIsOnline() + "-" + ulogin.getCreateTime().longValue() + "-" + ulogin.isPass() + "-" + firstTime);
                ulogin.setOperatorid(operatorid);
                if (ulogin.getLastLogin() != null) {
                    logger_login_disconnect.info("==>Check Promotion Daily: " + userId + "-" + ulogin.getUsername() + "-" + System.currentTimeMillis() + "-" + firstTime + "-" + ulogin.getLastLogin().longValue() + "-" + ulogin.getOnlineDaily() + "-" + ulogin.getPromotionDaily());
                    if (firstTime > ulogin.getLastLogin().longValue()) { //Login lan dau trong ngay
                        logger_login_disconnect.info("==>Check Promotion Daily==>Dau ngay:" + userId + "-" + ulogin.getUsername() + "-" + System.currentTimeMillis() + "-" + firstTime + "-" + ulogin.getLastLogin().longValue() + "-" + ulogin.getOnlineDaily() + "-" + ulogin.getPromotionDaily());
                        ulogin.setCPromot(0); //Ngay moi Reset so lan khuyen mai
                        ulogin.setReceiveDailyPromotion(false);
                        ulogin.setCFriendsF(0);
                        //ulogin.setGameCount(0); //Ngay moi so van tich luy = 0
                        if (ulogin.getOnlineDaily() > 15) //Neu da nhan qua hom truoc
                        {
                            ulogin.setOnlineDaily((short) (ulogin.getOnlineDaily() + 1 - 15));
                        }
                        if (ulogin.getOnlineDaily() > 7) {
                            ulogin.setOnlineDaily((short) 1);
                        }
                        if (from == 10) {//Hoki
                            if (firstTime - ulogin.getLastLogin().longValue() > 86400000) {
                                ulogin.setOnlineDaily((short) 1);
                            }
                        }
                        //ulogin.setGameCount(0) ;//this.GetGameCountDB(source, userId)); //Cap nhat lai Gamecount
                        if (((ulogin.getSource() != ServerSource.THAI_SOURCE) && (ulogin.getVIP() >= 2)) || (ulogin.getSource() == ServerSource.THAI_SOURCE)) //Cap nhat lai so gold tang trong ngay
                        {
                            ulogin.setPromotionValue(3000); //this.GetPromotionValueDB(source, userId));
                        }
                        updatePromotion(ulogin, source, deviceId, userId, firstTime);

                        //Cap nhat diem Vip va Vip, marklevel
                        logger_login_disconnect.info("Start Queery");
                        String strVip = GameGetMarkVipLVDB(source, ulogin.getUserid());
                        logger_login_disconnect.info("GameGetMarkVipLVDB==>" +source + " "+ ulogin.getUserid()+ " "+ strVip);
                        ulogin.setMarkVip(Integer.parseInt(strVip.split(";")[0])); //Lay diem MarkVip
                        ulogin.setVIP((short) Integer.parseInt(strVip.split(";")[1]));
                        ulogin.setMarkLevel((short) Integer.parseInt(strVip.split(";")[2]));
                    } else {
                        //Cap nhat lai CPromotion
                        if (deviceId.length() > 0) {
                            if (deviceId.indexOf("web") == -1) {
                                PromotionDevice cpro = this.GetCPromotionByDeviceId(source, deviceId);
                                //logger_promotion_handler.info("==>Check Deviceid:" + userId + "-" + cpro.getCPro() + "-" + deviceId + "-" + ulogin.getCPromot()) ;
                                if (cpro != null) {
                                    if (cpro.getDailyPromotion() == 1) {
                                        ulogin.setReceiveDailyPromotion(true);
                                    }
                                    if (cpro.getCPro() > ulogin.getCPromot().intValue()) {
                                        ulogin.setCPromot(cpro.getCPro());
                                    }
                                }
                            }
                        }
                    }
                }
                ulogin.setLastLogin(System.currentTimeMillis()); //Cap nhat lai thoi diem LastLogin
                ulogin.setLastDeviceID(deviceId);
                ulogin.setGameid(gameId); //Cap nhat lai GameID
            }
            //Update isonline DB
            if (ulogin != null) {
                if (ulogin.getAG() >= 0) {
                    this.updateUserInfoForCache(source, ulogin, isFromCache);
                    logger_login_disconnect.info("==>Login ==>ListDP1:" + userId + "-" + ulogin.getCPromot() + "-" + ulogin.getUsername() + "-" + ulogin.getOperatorid() + "-" + ulogin.getAG().intValue() + "-" + ulogin.getIsOnline() + "-" + ulogin.getCreateTime().longValue() + "-" + ulogin.isPass() + "-" + ulogin.getPassLQ());
                    UserController.getCacheInstance().set(key, ulogin, 0);
                }
            }
        } catch (Exception ex) {
            logger_login_disconnect.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }
        logger_promotion_handler.info("==>Finish Login:" + userId + "-" + ulogin.getCPromot());
        logger_login_disconnect.info("==>Finish GetUserInfoByUseridLoop " + new Gson().toJson(ulogin));
        return ulogin;
    }

    private void updatePromotion(UserInfo ulogin, int source, String deviceId, int userId, long firstTime) {
        try {
            //Set lai Promotion by Device ==> Set Receive Daily promotion (Cap nhat sau 10/3==> Chua lam)
            String keyPromotion = genCacheDeviceIdKey(source, deviceId);
            PromotionDevice cpromotion = (PromotionDevice) UserController.getCacheInstance().get(keyPromotion);
            if (cpromotion != null) {
                if (firstTime > cpromotion.getLastLogin()) {
                    cpromotion.setCPro(0);
                    cpromotion.setCOnline(0);
                    cpromotion.setCVideo(0);
                    cpromotion.setDailyPromotion(0); //Reset qua tang dau ngay
                    cpromotion.setLastLogin(System.currentTimeMillis());
                    UserController.getCacheInstance().set(keyPromotion, cpromotion, 0);
                } else {
                    if (cpromotion.getDailyPromotion() == 1) {
                        ulogin.setReceiveDailyPromotion(true);
                    }
                    if (cpromotion.getCPro() > ulogin.getCPromot().intValue()) {
                        ulogin.setCPromot(cpromotion.getCPro());
                    }
                }
            } else {
                cpromotion = new PromotionDevice(0, System.currentTimeMillis());
                UserController.getCacheInstance().set(keyPromotion, cpromotion, 0);
            }
            ServiceImpl.promotionHandler.ResetPromotion(source, userId, cpromotion.getCVideo(), cpromotion.getCOnline());
            if (source == ServerSource.IND_SOURCE || source == ServerSource.THAI_SOURCE) {
                if (cpromotion.getDailyPromotion() > 0) {
                    logger_login_disconnect.info("==>Check Promotion Daily==>Dau ngay Reset:" + userId
                            + "-" + ulogin.getUsername() + "-" + ulogin.getOnlineDaily() + "-" + firstTime + "-"
                            + ulogin.getLastLogin().longValue() + "-" + ulogin.getOnlineDaily() + "-"
                            + ulogin.getPromotionDaily());
                    if (ulogin.getOnlineDaily() < 8) {
                        logger_login_disconnect.info("==>Check Promotion Daily==>Dau ngay Reseted:" + userId
                                + "-" + ulogin.getUsername() + "-" + ulogin.getOnlineDaily() + "-" + firstTime + "-"
                                + ulogin.getLastLogin().longValue() + "-" + ulogin.getOnlineDaily() + "-"
                                + ulogin.getPromotionDaily());
                        ulogin.setOnlineDaily((short) (ulogin.getOnlineDaily() + 15));
                        ulogin.setReceiveDailyPromotion(true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private int GetPercentDailyByVip(int vip) {
//    	try {
//			if (vip == 1)
//				return 5 ;
//			else if (vip == 2)
//				return 8 ;
//			else if (vip == 3)
//				return 10 ;
//			else if (vip == 4)
//				return 15 ;
//			else if (vip == 5)
//				return 20 ;
//			else if (vip == 6)
//				return 25 ;
//			else if (vip == 7)
//				return 30 ;
//			else if (vip == 8)
//				return 35 ;
//			else if (vip == 9)
//				return 40 ;
//			else if (vip == 10)
//				return 50 ;
//			else
//				return 0 ;
//		} catch (Exception e) {
//			//handle exception
//		}
//    	return 0 ;
//    }
//    private int GetPercentDailyByDay(int day) {
//    	try {
//			if (day == 1)
//				return 5 ;
//			else if (day == 2)
//				return 7 ;
//			else if (day == 3)
//				return 10 ;
//			else if (day == 4)
//				return 13 ;
//			else if (day == 5)
//				return 17 ;
//			else if (day == 6)
//				return 21 ;
//			else if (day == 7)
//				return 25 ;
//			else
//				return 0 ;
//		} catch (Exception e) {
//			//handle exception
//		}
//    	return 0 ;
//    }
//    private int GetBasePromotionDaily(int vip) {
//    	try {
//			if (vip == 1)
//				return 7000 ;
//			else if (vip == 2)
//				return 10000 ;
//			else if (vip == 3)
//				return 15000 ;
//			else if (vip == 4)
//				return 30000 ;
//			else if (vip == 5)
//				return 50000 ;
//			else if (vip == 6)
//				return 100000 ;
//			else if (vip == 7)
//				return 150000 ;
//			else if (vip == 8)
//				return 240000 ;
//			else if (vip == 9)
//				return 400000 ;
//			else if (vip == 10)
//				return 800000 ;
//			else 
//				return 0 ;
//		} catch (Exception e) {
//			//handle exception
//			e.printStackTrace();
//		}
//    	return 3000 ;
//    }
    /*private int GetPromotionDaily_Siam(int day, int vip) {
    	try {
    		if (vip < 2) {
    			if (day == 1)
        			return 1500 ;
        		else if (day == 2)
        			return 2000 ;
        		else if (day == 3)
        			return 6000 ;
        		else if (day == 4)
        			return 4000 ;
        		else if (day == 5)
        			return 10000 ;
        		else if (day == 6)
        			return 6000 ;
        		else if (day == 7)
        			return 14000 ;
    		} else if (vip == 2) { //4000;5000;6000_6000;7000;8000_8000;9000;10000_10000;
    			if (day == 1)
        			return 4000 ;
        		else if (day == 2)
        			return 5000 ;
        		else if (day == 3)
        			return 12000 ;
        		else if (day == 4)
        			return 7000 ;
        		else if (day == 5)
        			return 16000 ;
        		else if (day == 6)
        			return 9000 ;
        		else if (day == 7)
        			return 20000 ;
    		} else if (vip == 3) { //10000;15000;20000_20000;25000;30000_30000;35000;40000_40000;
    			if (day == 1)
        			return 10000 ;
        		else if (day == 2)
        			return 15000 ;
        		else if (day == 3)
        			return 40000 ;
        		else if (day == 4)
        			return 25000 ;
        		else if (day == 5)
        			return 60000 ;
        		else if (day == 6)
        			return 35000 ;
        		else if (day == 7)
        			return 80000 ;
    		} else if (vip == 4) {//20000;30000;40000_30000;50000;60000_50000;70000;80000_100000;
    			if (day == 1)
        			return 20000 ;
        		else if (day == 2)
        			return 30000 ;
        		else if (day == 3)
        			return 70000 ;
        		else if (day == 4)
        			return 50000 ;
        		else if (day == 5)
        			return 110000 ;
        		else if (day == 6)
        			return 70000 ;
        		else if (day == 7)
        			return 180000 ;
    		} else if (vip == 5) { //30000;40000;50000_50000;60000;70000_90000;80000;90000_150000;
    			if (day == 1)
        			return 30000 ;
        		else if (day == 2)
        			return 40000 ;
        		else if (day == 3)
        			return 100000 ;
        		else if (day == 4)
        			return 60000 ;
        		else if (day == 5)
        			return 160000 ;
        		else if (day == 6)
        			return 80000 ;
        		else if (day == 7)
        			return 240000 ;
    		} else if (vip == 6) { //40000;55000;70000_80000;85000;100000_150000;115000;130000_250000;
    			if (day == 1)
        			return 40000 ;
        		else if (day == 2)
        			return 55000 ;
        		else if (day == 3)
        			return 150000 ;
        		else if (day == 4)
        			return 85000 ;
        		else if (day == 5)
        			return 250000 ;
        		else if (day == 6)
        			return 115000 ;
        		else if (day == 7)
        			return 380000 ;
    		} else if (vip == 7) { //50000;70000;90000_100000;110000;130000_2000000;150000;170000_300000;
    			if (day == 1)
        			return 50000 ;
        		else if (day == 2)
        			return 70000 ;
        		else if (day == 3)
        			return 190000 ;
        		else if (day == 4)
        			return 110000 ;
        		else if (day == 5)
        			return 330000 ;
        		else if (day == 6)
        			return 150000 ;
        		else if (day == 7)
        			return 470000 ;
    		} else if (vip == 8) { //100000;150000;200000_200000;250000;300000_350000;350000;400000_500000;
    			if (day == 1)
        			return 100000 ;
        		else if (day == 2)
        			return 150000 ;
        		else if (day == 3)
        			return 400000 ;
        		else if (day == 4)
        			return 250000 ;
        		else if (day == 5)
        			return 650000 ;
        		else if (day == 6)
        			return 350000 ;
        		else if (day == 7)
        			return 900000 ;
    		} else if (vip == 9) { //200000;300000;400000_500000;500000;600000_700000;700000;800000_1000000;
    			if (day == 1)
        			return 200000 ;
        		else if (day == 2)
        			return 300000 ;
        		else if (day == 3)
        			return 900000 ;
        		else if (day == 4)
        			return 500000 ;
        		else if (day == 5)
        			return 1300000 ;
        		else if (day == 6)
        			return 700000 ;
        		else if (day == 7)
        			return 1800000 ;
    		} else if (vip == 10) { //200000;300000;400000_1000000;500000;600000_2000000;700000;800000_3000000;
    			if (day == 1)
        			return 200000 ;
        		else if (day == 2)
        			return 300000 ;
        		else if (day == 3)
        			return 1400000 ;
        		else if (day == 4)
        			return 500000 ;
        		else if (day == 5)
        			return 2600000 ;
        		else if (day == 6)
        			return 700000 ;
        		else if (day == 7)
        			return 3800000 ;
    		}
    		
		} catch (Exception e) {
			//handle exception
			e.printStackTrace();
		}
    	return 3000 ;
    }*/
 /*private int GetPromotionDaily_India(int source, int day, int vip) {
    	try {
    		return ServerDefined.dailyreward_policy.get(source).get(vip).getBaseBonus()[day - 1] 
    				+ ServerDefined.dailyreward_policy.get(source).get(vip).getAddBonus()[day - 1] ;
    		if (vip == 0) {
    			if (day == 1)
        			return 12000 ;
        		else if (day == 2)
        			return 17000 ;
        		else if (day == 3)
        			return 23000 + 25000 ;
        		else if (day == 4)
        			return 28000 ;
        		else if (day == 5)
        			return 32000 + 30000 ;
        		else if (day == 6)
        			return 36000 ;
        		else if (day == 7)
        			return 41000 + 50000;
    		} else if (vip == 1) {
    			if (day == 1)
        			return 12000 ;
        		else if (day == 2)
        			return 17000 ;
        		else if (day == 3)
        			return 23000 + 25000 ;
        		else if (day == 4)
        			return 28000 ;
        		else if (day == 5)
        			return 32000 + 30000 ;
        		else if (day == 6)
        			return 36000 ;
        		else if (day == 7)
        			return 41000 + 50000;
    		} else if (vip == 2) { //4000;5000;6000_6000;7000;8000_8000;9000;10000_10000;
    			if (day == 1)
        			return 50000 ;
        		else if (day == 2)
        			return 70000 ;
        		else if (day == 3)
        			return 90000 + 90000;
        		else if (day == 4)
        			return 11000 ;
        		else if (day == 5)
        			return 130000 + 120000;
        		else if (day == 6)
        			return 150000 ;
        		else if (day == 7)
        			return 170000 + 200000;
    		} else if (vip == 3) { //10000;15000;20000_20000;25000;30000_30000;35000;40000_40000;
    			if (day == 1)
        			return 100000 ;
        		else if (day == 2)
        			return 150000 ;
        		else if (day == 3)
        			return 200000 + 200000 ;
        		else if (day == 4)
        			return 250000 ;
        		else if (day == 5)
        			return 300000 + 400000;
        		else if (day == 6)
        			return 350000 ;
        		else if (day == 7)
        			return 400000 + 600000 ;
    		} else if (vip == 4) {//20000;30000;40000_30000;50000;60000_50000;70000;80000_100000;
    			if (day == 1)
        			return 150000 ;
        		else if (day == 2)
        			return 250000 ;
        		else if (day == 3)
        			return 350000 + 100000;
        		else if (day == 4)
        			return 450000 ;
        		else if (day == 5)
        			return 550000 + 150000 ;
        		else if (day == 6)
        			return 650000 ;
        		else if (day == 7)
        			return 750000 + 200000 ;
    		} else if (vip == 5) { //30000;40000;50000_50000;60000;70000_90000;80000;90000_150000;
    			if (day == 1)
        			return 350000 ;
        		else if (day == 2)
        			return 470000 ;
        		else if (day == 3)
        			return 590000 +120000;
        		else if (day == 4)
        			return 187000 ;
        		else if (day == 5)
        			return 210000 + 200000;
        		else if (day == 6)
        			return 245000 ;
        		else if (day == 7)
        			return 300000 +300000;
    		} else if (vip == 6) { //40000;55000;70000_80000;85000;100000_150000;115000;130000_250000;
    			if (day == 1)
        			return 140000 ;
        		else if (day == 2)
        			return 170000 ;
        		else if (day == 3)
        			return 220000 +200000;
        		else if (day == 4)
        			return 330000 ;
        		else if (day == 5)
        			return 380000 +350000;
        		else if (day == 6)
        			return 445000 ;
        		else if (day == 7)
        			return 545000 + 500000;
    		} else if (vip == 7) { //50000;70000;90000_100000;110000;130000_2000000;150000;170000_300000;
    			if (day == 1)
        			return 266000 ;
        		else if (day == 2)
        			return 323000 ;
        		else if (day == 3)
        			return 418000 +400000;
        		else if (day == 4)
        			return 627000 ;
        		else if (day == 5)
        			return 722000 +700000;
        		else if (day == 6)
        			return 845000 ;
        		else if (day == 7)
        			return 975000 +950000;
    		} else if (vip == 8) { //100000;150000;200000_200000;250000;300000_350000;350000;400000_500000;
    			if (day == 1)
        			return 452000 ;
        		else if (day == 2)
        			return 550000 ;
        		else if (day == 3)
        			return 700000 +700000;
        		else if (day == 4)
        			return 885000 ;
        		else if (day == 5)
        			return 1050000 +1000000;
        		else if (day == 6)
        			return 1385000 ;
        		else if (day == 7)
        			return 1500000+1500000 ;
    		} else if (vip == 9) { //200000;300000;400000_500000;500000;600000_700000;700000;800000_1000000;
    			if (day == 1)
        			return 790000 ;
        		else if (day == 2)
        			return 950000 ;
        		else if (day == 3)
        			return 1200000 +1200000;
        		else if (day == 4)
        			return 1590000 ;
        		else if (day == 5)
        			return 1850000 +1600000;
        		else if (day == 6)
        			return 2050000 ;
        		else if (day == 7)
        			return 2340000 +2200000;
    		} else { //200000;300000;400000_1000000;500000;600000_2000000;700000;800000_3000000;
    			if (day == 1)
        			return 1100000 ;
        		else if (day == 2)
        			return 1500000 ;
        		else if (day == 3)
        			return 1800000 +2000000;
        		else if (day == 4)
        			return 2400000 ;
        		else if (day == 5)
        			return 2800000 +3000000;
        		else if (day == 6)
        			return 3100000 ;
        		else if (day == 7)
        			return 4000000 +4000000;
    		}
    		
		} catch (Exception e) {
			//handle exception
			e.printStackTrace();
		}
    	return 3000 ;
    }*/
 /*private int GetPromotionDaily_Hoki(int day, int vip) {
    	try {
    		if (vip < 2) {
    			if (day == 1)
        			return 3000 ;
        		else if (day == 2)
        			return 4000 ;
        		else if (day == 3)
        			return 7000 ;
        		else if (day == 4)
        			return 6000 ;
        		else if (day == 5)
        			return 10000 ;
        		else if (day == 6)
        			return 8000 ;
        		else if (day == 7)
        			return 13000 ;
    		} else if (vip == 2) { //8000;10000;12000_10000;14000;16000_20000;18000;20000_30000;
    			if (day == 1)
        			return 8000 ;
        		else if (day == 2)
        			return 10000 ;
        		else if (day == 3)
        			return 17000 ;
        		else if (day == 4)
        			return 14000 ;
        		else if (day == 5)
        			return 23000 ;
        		else if (day == 6)
        			return 18000 ;
        		else if (day == 7)
        			return 29000 ;
    		} else if (vip == 3) { //10000;15000;20000_20000;25000;30000_30000;35000;40000_40000;
    			if (day == 1)
        			return 10000 ;
        		else if (day == 2)
        			return 15000 ;
        		else if (day == 3)
        			return 30000 ;
        		else if (day == 4)
        			return 25000 ;
        		else if (day == 5)
        			return 35000 ;
        		else if (day == 6)
        			return 35000 ;
        		else if (day == 7)
        			return 60000 ;
    		} else if (vip == 4) {//20000;30000;40000_30000;50000;60000_50000;70000;80000_100000;
    			if (day == 1)
        			return 20000 ;
        		else if (day == 2)
        			return 30000 ;
        		else if (day == 3)
        			return 70000 ;
        		else if (day == 4)
        			return 50000 ;
        		else if (day == 5)
        			return 110000 ;
        		else if (day == 6)
        			return 70000 ;
        		else if (day == 7)
        			return 180000 ;
    		} else if (vip == 5) { //30000;40000;50000_50000;60000;70000_90000;80000;90000_150000;
    			if (day == 1)
        			return 30000 ;
        		else if (day == 2)
        			return 40000 ;
        		else if (day == 3)
        			return 100000 ;
        		else if (day == 4)
        			return 60000 ;
        		else if (day == 5)
        			return 160000 ;
        		else if (day == 6)
        			return 80000 ;
        		else if (day == 7)
        			return 240000 ;
    		} else if (vip == 6) { //40000;55000;70000_80000;85000;100000_150000;115000;130000_250000;
    			if (day == 1)
        			return 40000 ;
        		else if (day == 2)
        			return 55000 ;
        		else if (day == 3)
        			return 150000 ;
        		else if (day == 4)
        			return 85000 ;
        		else if (day == 5)
        			return 250000 ;
        		else if (day == 6)
        			return 115000 ;
        		else if (day == 7)
        			return 380000 ;
    		} else if (vip == 7) { //50000;70000;90000_100000;110000;130000_2000000;150000;170000_300000;
    			if (day == 1)
        			return 50000 ;
        		else if (day == 2)
        			return 70000 ;
        		else if (day == 3)
        			return 190000 ;
        		else if (day == 4)
        			return 110000 ;
        		else if (day == 5)
        			return 330000 ;
        		else if (day == 6)
        			return 150000 ;
        		else if (day == 7)
        			return 470000 ;
    		} else if (vip == 8) { //100000;150000;200000_200000;250000;300000_350000;350000;400000_500000;
    			if (day == 1)
        			return 100000 ;
        		else if (day == 2)
        			return 150000 ;
        		else if (day == 3)
        			return 400000 ;
        		else if (day == 4)
        			return 250000 ;
        		else if (day == 5)
        			return 650000 ;
        		else if (day == 6)
        			return 350000 ;
        		else if (day == 7)
        			return 900000 ;
    		} else if (vip == 9) { //200000;300000;400000_500000;500000;600000_700000;700000;800000_1000000;
    			if (day == 1)
        			return 200000 ;
        		else if (day == 2)
        			return 300000 ;
        		else if (day == 3)
        			return 900000 ;
        		else if (day == 4)
        			return 500000 ;
        		else if (day == 5)
        			return 1300000 ;
        		else if (day == 6)
        			return 700000 ;
        		else if (day == 7)
        			return 1800000 ;
    		} else if (vip == 10) { //200000;300000;400000_1000000;500000;600000_2000000;700000;800000_3000000;
    			if (day == 1)
        			return 200000 ;
        		else if (day == 2)
        			return 300000 ;
        		else if (day == 3)
        			return 1400000 ;
        		else if (day == 4)
        			return 500000 ;
        		else if (day == 5)
        			return 2600000 ;
        		else if (day == 6)
        			return 700000 ;
        		else if (day == 7)
        			return 3800000 ;
    		}
    		
		} catch (Exception e) {
			//handle exception
			e.printStackTrace();
		}
    	return 3000 ;
    }*/
    private int GetPromotionDaily(int source, int day, int vip) {
        try {
            return ServerDefined.dailyreward_policy.get(source).get(vip).GetBonusByDay(day);
        } catch (Exception e) {
            //handle exception
        }
        return 3000;
    }

    private String GetPromotionDaily_List(int source, int vip) {
        try {
            return ServerDefined.dailyreward_policy.get(source).get(vip).getStrList();
        } catch (Exception e) {
            //handle exception
        }
        return "3000;4000;5000_2000;6000;7000_3000;8000;9000_4000;";
    }

    /*private String GetPromotionDaily_Hoki_List(int vip) {
    	try {
    		if (vip <2)
    			return "3000;4000;5000_2000;6000;7000_3000;8000;9000_4000;" ;
    		else if (vip == 2)
    			return "8000;10000;12000_5000;14000;16000_7000;18000;20000_9000;" ;
    		else if (vip == 3)
    			return "10000;15000;20000_10000;25000;30000_15000;35000;40000_20000;" ;
    		else if (vip == 4)
    			return "20000;30000;40000_30000;50000;60000_50000;70000;80000_100000;" ;
    		else if (vip == 5)
    			return "30000;40000;50000_50000;60000;70000_90000;80000;90000_150000;" ;
    		else if (vip == 6)
    			return "40000;55000;70000_80000;85000;100000_150000;115000;130000_250000;" ;
    		else if (vip == 7)
    			return "50000;70000;90000_100000;110000;130000_2000000;150000;170000_300000;" ;
    		else if (vip == 8)
    			return "100000;150000;200000_200000;250000;300000_350000;350000;400000_500000;" ;
    		else if (vip == 9)
    			return "200000;300000;400000_500000;500000;600000_700000;700000;800000_1000000;" ;
    		else
    			return "200000;300000;400000_1000000;500000;600000_2000000;700000;800000_3000000;" ;
		} catch (Exception e) {
			//handle exception
			e.printStackTrace();
		}
    	return "3000;4000;5000_2000;6000;7000_3000;8000;9000_4000;" ;
    }
    private String GetPromotionDaily_Siam_List(int vip) {
    	try {
    		if (vip <2)
    			return "1500;2000;3000_3000;4000;5000_5000;6000;7000_7000;" ;
    		else if (vip == 2)
    			return "4000;5000;6000_6000;7000;8000_8000;9000;10000_10000;" ;
    		else if (vip == 3)
    			return "10000;15000;20000_20000;25000;30000_30000;35000;40000_40000;" ;
    		else if (vip == 4)
    			return "20000;30000;40000_30000;50000;60000_50000;70000;80000_100000;" ;
    		else if (vip == 5)
    			return "30000;40000;50000_50000;60000;70000_90000;80000;90000_150000;" ;
    		else if (vip == 6)
    			return "40000;55000;70000_80000;85000;100000_150000;115000;130000_250000;" ;
    		else if (vip == 7)
    			return "50000;70000;90000_100000;110000;130000_2000000;150000;170000_300000;" ;
    		else if (vip == 8)
    			return "100000;150000;200000_200000;250000;300000_350000;350000;400000_500000;" ;
    		else if (vip == 9)
    			return "200000;300000;400000_500000;500000;600000_700000;700000;800000_1000000;" ;
    		else
    			return "200000;300000;400000_1000000;500000;600000_2000000;700000;800000_3000000;" ;
		} catch (Exception e) {
			//handle exception
			e.printStackTrace();
		}
    	return "3000;4000;5000_3000;6000;7000_5000;8000;9000_7000;" ;
    }
    private String GetPromotionDaily_India_List(int source, int vip) {
    	try {
    		if (vip == 0)
    			return "12000;17000;23000_20000;28000;32000_30000;36000;41000_40000;" ;
    		else if (vip == 1)
    			return "18000;25000;35000_35000;42000;48000_40000;54000;62000_50000;" ;
    		else if (vip == 2)
    			return "23000;32000;45000_45000;54000;63000_60000;70000;80000_75000;" ;
    		else if (vip == 3)
    			return "32000;45000;63000_65000;76000;88000_80000;100000;112000_110000;" ;
    		else if (vip == 4)
    			return "46000;65000;92000_100000;110000;128000_150000;150000;175000_200000;" ;
    		else if (vip == 5)
    			return "80000;110000;150000_120000;187000;210000_200000;245000;300000_300000;" ;
    		else if (vip == 6)
    			return "140000;170000;220000_200000;330000;380000_350000;445000;545000_500000;" ;
    		else if (vip == 7)
    			return "266000;323000;418000_400000;627000;722000_700000;845000;975000_950000;" ;
    		else if (vip == 8)
    			return "452000;550000;700000_700000;885000;1050000_1000000;1385000;1500000_1500000;" ;
    		else if (vip == 9)
    			return "790000;950000;1200000_1200000;1590000;1850000_1600000;2050000;2340000_2400000;" ;
    		else
    			return "1100000;1500000;1800000_2000000;2400000;2800000_3000000;3100000;4000000_4000000;" ;
		} catch (Exception e) {
			//handle exception
			e.printStackTrace();
		}
    	return "30000;40000;50000_30000;60000;70000_50000;80000;90000_70000;" ;
    }*/
    public void updateUserInfoForCache(int source, UserInfo ulogin, boolean isFromCache) {
        try {
            ulogin.setPassLQ("");
            if (source == ServerSource.THAI_SOURCE) {
                ulogin.setPassLQ(GetPromotionDaily_List(source, (int) ulogin.getVIP()));
            } //    			ulogin.setPassLQ(GetPromotionDaily_Siam_List((int)ulogin.getVIP()));
            else if (source == ServerSource.IND_SOURCE) {
                ulogin.setPassLQ(GetPromotionDaily_List(source, (int) ulogin.getVIP()));
            } //    			ulogin.setPassLQ(GetPromotionDaily_Hoki_List((int)ulogin.getVIP()));
            else if (source == ServerSource.INDIA_SOURCE) {
                ulogin.setPassLQ(GetPromotionDaily_List(source, (int) ulogin.getVIP()));
            }else if(source == ServerSource.MYA_SOURCE){
                ulogin.setPassLQ(GetPromotionDaily_List(source, (int) ulogin.getVIP()));
            }
            logger_login_disconnect.info("==>Login ==>ListDP2:" + ulogin.getOnlineDaily() + "-" + ulogin.getUserid().intValue() + "-" + ulogin.getCPromot() + "-" + ulogin.getUsername() + "-" + ulogin.getOperatorid() + "-" + ulogin.getAG().intValue() + "-" + ulogin.getIsOnline() + "-" + ulogin.getCreateTime().longValue() + "-" + ulogin.isReceiveDailyPromotion() + "-" + ulogin.getPassLQ());
            if (ulogin.getOnlineDaily() < 8 && !ulogin.isReceiveDailyPromotion()) {
                if (source == ServerSource.THAI_SOURCE) {
                    ulogin.setPromotionDaily(GetPromotionDaily(source, ulogin.getOnlineDaily(), (int) ulogin.getVIP()));
                } //    				ulogin.setPromotionDaily(GetPromotionDaily_Siam(ulogin.getOnlineDaily(),(int)ulogin.getVIP()));
                else if (source == ServerSource.IND_SOURCE) {
                    ulogin.setPromotionDaily(GetPromotionDaily(source, ulogin.getOnlineDaily(), (int) ulogin.getVIP()));
//    				ulogin.setPromotionDaily(GetPromotionDaily_Hoki(ulogin.getOnlineDaily(),(int)ulogin.getVIP()));
                } else if (source == ServerSource.INDIA_SOURCE) {
                    ulogin.setPromotionDaily(GetPromotionDaily(source, ulogin.getOnlineDaily(), (int) ulogin.getVIP()));
                } else if(source == ServerSource.MYA_SOURCE){
                    ulogin.setPromotionDaily(GetPromotionDaily(source, ulogin.getOnlineDaily(), (int) ulogin.getVIP()));
                }
                else {
                    ulogin.setPromotionDaily(0);
                }
            } else {
                ulogin.setPromotionDaily(0);
            }
            if (!isFromCache) {
                String strVip = GameGetMarkVipLVDB(source, ulogin.getUserid());
                ulogin.setMarkVip(Integer.parseInt(strVip.split(";")[0])); //Lay diem MarkVip
                ulogin.setVIP((short) Integer.parseInt(strVip.split(";")[1]));
                ulogin.setMarkLevel(Integer.parseInt(strVip.split(";")[2])); //Lay diem MarkLv
                ulogin.setCMsg(this.GetNumberNewMailDB(source, ulogin.getUsername(), ulogin.getUsernameOld())); //Get numbew new Mail
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }
    }

    //Get UserInfo
    public void GetUserInfoByUseridDb(int source, UserInfo ulogin, String ipServer) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserInfoByIDSiam_Using(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }");
            cs.setInt("UserId", ulogin.getUserid());
            cs.setLong("Facebookid", ulogin.getFacebookid());
            cs.setString("Deviceid", ulogin.getDeviceId());
            cs.setString("IpAddress", ipServer);
            cs.setInt("GameId", ulogin.getGameid());
            cs.registerOutParameter("AG", Types.BIGINT);
            cs.registerOutParameter("LQ", Types.INTEGER);
            cs.registerOutParameter("Diamond", Types.BIGINT);
            cs.registerOutParameter("VIP", Types.INTEGER);
            cs.registerOutParameter("isOnline", Types.INTEGER);
            cs.registerOutParameter("isBanned", Types.BIT);
            cs.registerOutParameter("CPromot", Types.INTEGER);
            cs.registerOutParameter("TPromot", Types.INTEGER);
            cs.registerOutParameter("Mobile", Types.NVARCHAR);
            cs.registerOutParameter("isRegister", Types.BIT);
            cs.registerOutParameter("Avatar", Types.INTEGER);
            cs.registerOutParameter("Ref", Types.NVARCHAR);
            cs.registerOutParameter("Username", Types.NVARCHAR);
            cs.registerOutParameter("UsernameLQ", Types.NVARCHAR);
            cs.registerOutParameter("IdolName", Types.NVARCHAR);
            cs.registerOutParameter("OnlineDaily", Types.INTEGER);
            cs.registerOutParameter("LastDeviceID", Types.NVARCHAR);
            cs.registerOutParameter("LastLogin", Types.TIMESTAMP);
            cs.registerOutParameter("CreateTime", Types.TIMESTAMP);
            cs.registerOutParameter("ReceivePromotionDaily", Types.BIT);
            cs.registerOutParameter("ChessElo", Types.INTEGER);
            cs.registerOutParameter("WinAcc", Types.BIGINT);
            cs.registerOutParameter("AGHigh", Types.INTEGER);
            cs.registerOutParameter("AGLow", Types.INTEGER);
            cs.execute();
            ulogin.setAG(cs.getLong("AG"));
            ulogin.setLQ(cs.getInt("LQ"));
            ulogin.setDiamond(cs.getLong("Diamond"));
            ulogin.setVIP((short) cs.getInt("VIP"));
            ulogin.setIsOnline((short) cs.getInt("isOnline"));
            ulogin.setBanned(cs.getBoolean("isBanned"));
            ulogin.setCPromot(cs.getInt("CPromot"));
            ulogin.setTPromot(cs.getInt("TPromot"));
            ulogin.setMobile(cs.getString("Mobile"));
            ulogin.setRegister(cs.getBoolean("isRegister"));
            ulogin.setAvatar((short) cs.getInt("Avatar"));
            ulogin.setRef(cs.getString("Ref"));
            ulogin.setIdolName(cs.getString("IdolName"));
            ulogin.setUsername(cs.getString("Username"));
            ulogin.setUsernameOld(cs.getString("Username"));
            if (!cs.getString("UsernameLQ").equals("")) {
                ulogin.setUsername(cs.getString("UsernameLQ"));
                ulogin.setUsernameLQ(cs.getString("UsernameLQ"));
            }
            ulogin.setOnlineDaily((short) cs.getInt("OnlineDaily"));
            ulogin.setLastDeviceID(cs.getString("LastDeviceID"));
            ulogin.setLastLogin(cs.getTimestamp("LastLogin").getTime());
            ulogin.setCreateTime(cs.getTimestamp("CreateTime").getTime());
            ulogin.setReceiveDailyPromotion(cs.getBoolean("ReceivePromotionDaily"));
            ulogin.setChessElo(cs.getInt("ChessElo"));
            ulogin.setWinAccumulation(cs.getLong("WinAcc"));
            ulogin.setAGHigh(cs.getInt("AGHigh"));
            ulogin.setAGLow(cs.getInt("AGLow"));
            ulogin.setTableId(0); //Lay tu DB ==> tableId = 0 ;
            cs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GetUserInfoAfterPayByUseridDb(int source, UserInfo ulogin, String ipServer) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserInfoByIDSiamAfterPay_Using_New(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }");
            cs.setInt("UserId", ulogin.getUserid());
            cs.setLong("Facebookid", ulogin.getFacebookid());
            cs.setString("Deviceid", ulogin.getDeviceId());
            cs.setString("IpAddress", ipServer);
            cs.setInt("GameId", ulogin.getGameid());
            cs.registerOutParameter("AG", Types.BIGINT);
            cs.registerOutParameter("LQ", Types.INTEGER);
            cs.registerOutParameter("Diamond", Types.BIGINT);
            cs.registerOutParameter("VIP", Types.INTEGER);
            cs.registerOutParameter("isOnline", Types.INTEGER);
            cs.registerOutParameter("isBanned", Types.BIT);
            cs.registerOutParameter("CPromot", Types.INTEGER);
            cs.registerOutParameter("TPromot", Types.INTEGER);
            cs.registerOutParameter("Mobile", Types.NVARCHAR);
            cs.registerOutParameter("isRegister", Types.BIT);
            cs.registerOutParameter("Avatar", Types.INTEGER);
            cs.registerOutParameter("Ref", Types.NVARCHAR);
            cs.registerOutParameter("Username", Types.NVARCHAR);
            cs.registerOutParameter("UsernameLQ", Types.NVARCHAR);
            cs.registerOutParameter("IdolName", Types.NVARCHAR);
            cs.registerOutParameter("OnlineDaily", Types.INTEGER);
            cs.registerOutParameter("LastDeviceID", Types.NVARCHAR);
            cs.registerOutParameter("LastLogin", Types.TIMESTAMP);
            cs.registerOutParameter("CreateTime", Types.TIMESTAMP);
            cs.registerOutParameter("ReceivePromotionDaily", Types.BIT);
            cs.registerOutParameter("ChessElo", Types.INTEGER);
            cs.registerOutParameter("WinAcc", Types.BIGINT);
            cs.registerOutParameter("AGHigh", Types.INTEGER);
            cs.registerOutParameter("AGLow", Types.INTEGER);
            cs.registerOutParameter("MarkVip", Types.INTEGER);
            cs.registerOutParameter("MarkLevel", Types.INTEGER);
            cs.execute();
            ulogin.setAG(cs.getLong("AG"));
            ulogin.setLQ(cs.getInt("LQ"));
            ulogin.setDiamond(cs.getLong("Diamond"));
            ulogin.setVIP((short) cs.getInt("VIP"));
            ulogin.setIsOnline((short) cs.getInt("isOnline"));
            ulogin.setBanned(cs.getBoolean("isBanned"));
            ulogin.setCPromot(cs.getInt("CPromot"));
            ulogin.setTPromot(cs.getInt("TPromot"));
            ulogin.setMobile(cs.getString("Mobile"));
            ulogin.setRegister(cs.getBoolean("isRegister"));
            ulogin.setAvatar((short) cs.getInt("Avatar"));
            ulogin.setRef(cs.getString("Ref"));
            ulogin.setIdolName(cs.getString("IdolName"));
            ulogin.setUsername(cs.getString("Username"));
            ulogin.setUsernameOld(cs.getString("Username"));
            if (!cs.getString("UsernameLQ").equals("")) {
                ulogin.setUsername(cs.getString("UsernameLQ"));
                ulogin.setUsernameLQ(cs.getString("UsernameLQ"));
            }
            ulogin.setOnlineDaily((short) cs.getInt("OnlineDaily"));
            ulogin.setLastDeviceID(cs.getString("LastDeviceID"));
            ulogin.setLastLogin(cs.getTimestamp("LastLogin").getTime());
            ulogin.setCreateTime(cs.getTimestamp("CreateTime").getTime());
            ulogin.setReceiveDailyPromotion(cs.getBoolean("ReceivePromotionDaily"));
            ulogin.setChessElo(cs.getInt("ChessElo"));
            ulogin.setWinAccumulation(cs.getLong("WinAcc"));
            ulogin.setAGHigh(cs.getInt("AGHigh"));
            ulogin.setAGLow(cs.getInt("AGLow"));
            ulogin.setMarkVip(cs.getInt("MarkVip"));
            ulogin.setTableId(0); //Lay tu DB ==> tableId = 0 ;
            ulogin.setMarkLevel(cs.getInt("MarkLevel"));
            cs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void UpdateCPromotionToCache(int source, int uid, int cpro) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setCPromot(uinfo.getCPromot() + cpro);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateDailyPromotionToCache(int source, int uid) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            logger_login_disconnect.info("==>UpdateDailyPromotionToCache:" + key);
            if (uinfo != null) {
                uinfo.setOnlineDaily((short) (15 + uinfo.getOnlineDaily()));
                uinfo.setPromotionDaily(0);
                uinfo.setReceiveDailyPromotion(true);
                UserController.getCacheInstance().set(key, uinfo, 0);
                logger_login_disconnect.info("==>UpdateDailyPromotionToCache==>Finish:" + key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserInfo GetUserInfoFromCache(int source, int uid) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            return uinfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void RestoreHighLow(int source, int uid) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                //Update DB
                RestoreHighLowDB(source, uid, uinfo.getAGHigh(), uinfo.getAGLow());

                uinfo.setAG(uinfo.getAG().longValue() + uinfo.getAGHigh() + uinfo.getAGLow());
                uinfo.setAGHigh(0);
                uinfo.setAGLow(0);
                UserController.getCacheInstance().set(key, uinfo, 0);
            } else {
                logger_login_disconnect.info("==>RestoreHighLow: key: " + key + " - source:" + source + " - uid: " + uid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateIsOnlineToCache(int source, int uid, String ipaddress, short gameid, int operatorid, String ipuser) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setIsOnline(gameid);
                uinfo.setGameid(gameid);
                uinfo.setIdolName(ipaddress);
                uinfo.setDisconnect(false);
//            	uinfo.setPass(false); //Da login xong
//            	uinfo.setLastLogin(System.currentTimeMillis());
                UserController.getCacheInstance().set(key, uinfo, 0);
                //Update DB
                Integer userKey = new Integer(uinfo.getPid());
                if (ServiceImpl.dicKet.containsKey(userKey)) {
                    ServiceImpl.dicKet.remove(userKey);
                }
                GameUIsonlineConnectDb(source, uid, gameid, operatorid, ipaddress, ipuser);
            } else {
                logger_login_disconnect.info("==>UpdateIsOnlineToCache: key: " + key + " - source:" + source + " - uid: " + uid + " - ipaddress: " + " - gameid: " + gameid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateTableIdToCache(int source, int uid, int tableid) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setTableId(tableid);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public long InputAGToCache(int source, int uid, long ag) {
        try {
            //Update Cache
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
//        	UserInfo uinfo = ActionUtils.gson.fromJson((String)UserController.getCacheInstance().get(key) ,UserInfo.class);
            if (uinfo != null) {
                uinfo.setAG(ag);
                UserController.getCacheInstance().set(key, uinfo, 0);
//                UserController.getCacheInstance().set(key, ActionUtils.gson.toJson(uinfo).getBytes("UTF-8"), 0);
                return uinfo.getAG();
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public long UpdateVipCache(int source, int uid, short vip) {
        try {
            //Update Cache
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
//        	UserInfo uinfo = ActionUtils.gson.fromJson((String)UserController.getCacheInstance().get(key) ,UserInfo.class);
            if (uinfo != null) {
                uinfo.setVIP(vip);
                UserController.getCacheInstance().set(key, uinfo, 0);
//                UserController.getCacheInstance().set(key, ActionUtils.gson.toJson(uinfo).getBytes("UTF-8"), 0);
                return uinfo.getAG();
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public int UpdateMarkVipToCache(int source, int uid, int markvip) {
        try {
            //Update Cache
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
//        	UserInfo uinfo = ActionUtils.gson.fromJson((String)UserController.getCacheInstance().get(key) ,UserInfo.class);
            if (uinfo != null) {
                uinfo.setMarkVip(markvip);
                UserController.getCacheInstance().set(key, uinfo, 0);
//                UserController.getCacheInstance().set(key, ActionUtils.gson.toJson(uinfo).getBytes("UTF-8"), 0);
                return 1;
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public int UpdateGameCountCache(int source, int uid, int gameC, double gameA, double gameNo) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setGameCount(uinfo.getGameCount() + gameC);
                uinfo.setGameAmount(uinfo.getGameAmount() + gameA);
                uinfo.setGameNo(uinfo.getGameNo() + gameNo);
                UserController.getCacheInstance().set(key, uinfo, 0);
                return 1;
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public int getPlayerGameCountCache(int source, int uid){
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                return uinfo.getGameCount();
            } else {
                return 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public void resetGameCountToCache(int source, int uid) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setGameCount(0);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int UpdateUsernameSiamToCache(int source, int uid, String username) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setUsername(username);
                uinfo.setUsernameLQ(username);
                UserController.getCacheInstance().set(key, uinfo, 0);
                return 1;
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public long UpdateLQCache(int source, int uid, long mark, short vip) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                long lq = uinfo.getLQ().intValue() + mark; //Update Cache
                if (lq < 0) {
                    lq = 0;
                }
                uinfo.setLQ((int) lq);
                uinfo.setVIP(vip);
                UserController.getCacheInstance().set(key, uinfo, 0);
                return uinfo.getAG();
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public void UpdatePromotionDailyLQ(int source, int uid) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setPromotionDaily(0);
                uinfo.setReceiveDailyPromotion(true);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void UpdateLoanAG(int source, int uid, int mark) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                long ag = uinfo.getAG() + mark; //Update Cache
                if (ag < 0) {
                    ag = 0;
                }
                uinfo.setAG(ag);
                uinfo.setOwnAmt(uinfo.getOwnAmt() + mark);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void UpdatePayAG(int source, int uid) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                long ag = uinfo.getAG() - uinfo.getOwnAmt().intValue(); //Update Cache
                if (ag < 0) {
                    ag = 0;
                }
                uinfo.setAG(ag);
                uinfo.setOwnAmt(0);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public long UpdateAGCache(int source, int uid, long mark, short vip, long dm) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                long ag = uinfo.getAG() + mark; //Update Cache
                if (ag < 0) {
                    ag = 0;
                }
                long diamond = uinfo.getDiamond() + dm;
                if (diamond < 0) {
                    diamond = 0;
                }
                uinfo.setAG(ag);
                uinfo.setVIP(vip);
                uinfo.setDiamond(diamond);
                UserController.getCacheInstance().set(key, uinfo, 0);
                return uinfo.getAG();
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public void UpdateAGDailyPromotion(int src, int uid, int mark, int source) {
        try {
            String key = genCacheUserInfoKey(src, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                long ag = uinfo.getAG() + mark; //Update Cache
                if (ag < 0) {
                    ag = 0;
                }
                uinfo.setAG(ag);
                uinfo.setReceiveDailyPromotion(true);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
            this.UpdateAGCmd(src, uid, mark);
            this.UpdateSiamDailyCmd(src, uid, mark);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public long UpdateAG(int source, int uid, long mark, boolean isAccumulation) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                long ag = uinfo.getAG() + mark; //Update Cache
                if (ag < 0) {
                    ag = 0;
                }
                if (isAccumulation && mark > 0) {
                    uinfo.setWinAccumulation(uinfo.getWinAccumulation() + mark);
                }
                uinfo.setAG(ag);
                UserController.getCacheInstance().set(genCacheUserInfoKey(source, uid), uinfo, 0);
                this.UpdateAGCmd(source, uid, mark); //Update to Database
                return uinfo.getAG();
            } else {
                return UpdateAGDb(source, uid, mark);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public long UpdateAGDb(int source, int uid, int mark) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUMarkUser(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setLong("Mark", mark);
            cs.registerOutParameter("Temp", Types.BIGINT);
            cs.execute();
            return cs.getLong("Temp");
        } catch (Exception ex) {
            logger_ag_update.info("#" + source + "#" + uid + "#" + mark);
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ************************ Tai xiu
     *
     * @param  **************************************
     */
    public long UpdateBetTaixiu(int source, int uid, int agHigh, int agLow, long timeBet, long idBet, UserInfo userInfoDict) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                return UpdateInfoBetTaiXiu(uinfo, source, uid, agHigh, agLow, timeBet, idBet);
            }
            uinfo = new UserInfo();
            uinfo.setUserid(userInfoDict.getUserid());
            uinfo.setFacebookid(userInfoDict.getFacebookid());
            uinfo.setGameid(userInfoDict.getGameid());
            uinfo.setDeviceId(userInfoDict.getDeviceId());
            logger_tai_xiu.info("UpdateAGTaixiu getFromDB:" + uid + "-" + source + "-" + agHigh + "-" + agLow);
            this.GetUserInfoByUseridDb(source, uinfo, ServiceImpl.ipAddressServer);
            return UpdateInfoBetTaiXiu(uinfo, source, uid, agHigh, agLow, timeBet, idBet);
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public long UpdateInfoBetTaiXiu(UserInfo uinfo, int source, int uid, int agHigh, int agLow, long timeBet, long idBet) {
        try {
            uinfo.setAGHigh(uinfo.getAGHigh() + agHigh);
            uinfo.setAGLow(uinfo.getAGLow() + agLow);
            uinfo.setAGWinHighLow(0);
            uinfo.setStatusHighLow(1);
            long ag = uinfo.getAG().intValue() - agHigh - agLow;
            if (ag < 0) {
                ag = 0;
            }
            uinfo.setAG(ag);
            UserController.getCacheInstance().set(genCacheUserInfoKey(source, uid), uinfo, 0);
            this.UpdateAGTaixiuCmd(source, uid, -1l, 0, agHigh, agLow, timeBet, "", idBet); //Lich su dat
            logger_tai_xiu.info("==>UpdateInfoBetTaiXiu Bet Cache finish:" + uid + "-" + "-" + uinfo.getAGHigh() + "-" + uinfo.getAGLow());
            return uinfo.getAG().longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long UpdateAGTaixiu(int source, int uid, long markwin, long markwinreal, int totalrefund, int[] arr, int agHigh, int agLow, long timeStop, int id) { //Trao thuong tai xiu
        try {
            //Luu Cache History vao cache
            /*if (markwin != -1) { //Trao thuong
    			String keyHL = ServerDefined.getKeyCacheHighlow(source) + uid;
    			System.out.println("==>Key Add to history:" + keyHL) ;
        		TaiXiuResult obj ;
        		if (markwinreal == -3) //Refund
        			obj = new TaiXiuResult(id, agHigh + agLow, agHigh, agLow, 0, timeStop, arr) ;
        		else
        			obj = new TaiXiuResult(id, 0, agHigh, agLow, markwinreal, timeStop, arr) ;
        		TaiXiuHistory objHistory = (TaiXiuHistory) UserController.getCacheInstance().get(keyHL) ;
        		if (objHistory == null)
        			objHistory = new TaiXiuHistory(uid) ;
        		System.out.println("==>Key Add to history 1:" + keyHL + "-" + objHistory.getLsH().size()) ;
        		if (objHistory.getLsH().size()>50)
        			objHistory.RemoveObj();
        		System.out.println("==>Key Add to history 2:" + keyHL) ;
        		objHistory.AddObj(obj) ;
        		System.out.println("==>Key Add to history 3:" + keyHL + "-" + objHistory.getLsH().size()) ;
        		UserController.getCacheInstance().set(keyHL, objHistory, 0); //Set Lai cache history Highlow
        		System.out.println("==>Update taixiu to DB:" + uid + "-" + keyHL + "-" + objHistory.getLsH().size()) ;
    		}*/
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                long ag = uinfo.getAG().intValue() + markwin;// - agHigh - agLow; //Update Cache
                if (ag < 0) {
                    ag = 0;
                }
                uinfo.setAG(ag);
                uinfo.setAGHigh(0);
                uinfo.setAGLow(0);
                uinfo.setAGWinHighLow((int) markwinreal); //Gui ve Client
                uinfo.setStatusHighLow(0);
                UserController.getCacheInstance().set(genCacheUserInfoKey(source, uid), uinfo, 0);
                this.UpdateAGTaixiuCmd(source, uid, markwin, totalrefund, agHigh, agLow, timeStop, arr[0] + "-" + arr[1] + "-" + arr[2] + ";", -1); //Update to Database
                return uinfo.getAG();
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public void UpdateAGTaixiuCmd(int source, int uid, long mark, int totalrefund, int agHigh, int agLow, long timeStop, String strArr, long idBet) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updateAGTaixiu", source, uid, mark, totalrefund, agHigh, agLow, timeStop, strArr, idBet);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public long UpdateAGTaixiuDB(int source, int uid, long markwin, int agrefund, int agHigh, int agLow, long timeBet, String strArr, long idBet) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUMarkUserTaixiu_New(?,?,?,?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setLong("Mark", markwin);
            cs.setInt("AGHigh", agHigh);
            cs.setInt("AGLow", agLow);
            cs.setInt("AGRefund", agrefund);
            cs.setLong("TimeBet", timeBet);
            cs.setString("ArrResult", strArr);
            cs.setLong("IdBet", idBet);
            cs.registerOutParameter("Error", Types.BIGINT);
            cs.execute();
            return cs.getLong("Error");
        } catch (Exception ex) {
            logger_ag_update.info("#" + source + "#" + uid + "#" + markwin);
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void UpdateBalanceTaixiu(int source, long balanceHigh, long balanceLow, long totalHigh, long totalLow, long totalPay, long timeStop, String strResult) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updateBalanceTaixiu", source, balanceHigh, balanceLow, totalHigh, totalLow, totalPay, timeStop, strResult);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int UpdateBalanceTaixiuDB(int source, long balanceHigh, long balanceLow, long totalHigh, long totalLow, long totalPay, long timeStop, String strResult) {
        System.out.println("==>UpdateBalanceTaixiuDB DB:" + balanceHigh + "-" + balanceLow + "-" + timeStop);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIBalanceTaixiu(?,?,?,?,?,?,?,?) }");
            cs.setLong("BalanceHigh", balanceHigh);
            cs.setLong("BalanceLow", balanceLow);
            cs.setLong("TimeStop", timeStop);
            cs.setString("StrResult", strResult);
            cs.setLong("TotalHigh", totalHigh);
            cs.setLong("TotalLow", totalLow);
            cs.setLong("TotalPay", totalPay);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ************************ List Userid online ***************************
     */
    public List<Integer> GetListUseridOnline(int source, String ipServer) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListOnline(?) }");
            cs.setString("IpServer", ipServer);
            ResultSet rs = cs.executeQuery();
            List<Integer> lsret = new ArrayList<Integer>();
            //DateFormat readFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss aa");
            while (rs.next()) {
                lsret.add(rs.getInt("ID"));
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ************************ Diamond ***************************************
     */
    public void UpdateDiamondCmd(int source, int uid, long mark) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updateDiamond", source, uid, mark);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public long UpdateDiamond(int source, int uid, long mark) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                long diamond = uinfo.getDiamond() + mark;
                if (diamond < 0) {
                    diamond = 0;
                }
                uinfo.setDiamond(diamond);
                UserController.getCacheInstance().set(genCacheUserInfoKey(source, uid), uinfo, 0);
                this.UpdateDiamondCmd(source, uid, mark); //Update to Database
                return uinfo.getDiamond();
            } else {
                return UpdateDiamondDb(source, uid, mark);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public long UpdateDiamondDb(int source, int uid, long mark) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUDiamondUser(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setLong("Mark", mark);
            cs.registerOutParameter("Temp", Types.BIGINT);
            cs.execute();
            return cs.getLong("Temp");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GetIsOnlineFromDB(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetIsOnline(?,?) }");
            cs.setInt("UserId", uid);
            cs.registerOutParameter("Temp", Types.INTEGER);
            cs.execute();
            return cs.getInt("Temp");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public long UpdateAGDb(int source, int uid, long mark) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUMarkUser(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setLong("Mark", mark);
            cs.registerOutParameter("Temp", Types.BIGINT);
            cs.execute();
            return cs.getLong("Temp");
        } catch (Exception ex) {
            logger_ag_update.info("#" + source + "#" + uid + "#" + mark);
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void UpdateAGCmd(int source, int uid, long mark) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updateAG", source, uid, mark);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //Get Userid

    public int GameGetUserid(int source, String username, String pass) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserIdTemp(?,?,?) }");
            cs.setString("Username", username);
            cs.setString("Password", pass);
            cs.registerOutParameter("Userid", Types.INTEGER);
            cs.execute();
            return cs.getInt("Userid");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameGetUseridSiamFromCache(int source, String username) {
        try {
            String keyId = genCacheIdUserInfoKey(source, username);
            Integer uid = (Integer) UserController.getCacheInstance().get(keyId);
            if (uid != null) {
                return uid.intValue();
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //handle exception
            return 0;
        }
    }

    public int GameGetUseridSiam(int source, String username, String deviceId) {
        int returnValue = GameGetUseridSiamFromCache(source, "z." + username);
        if (returnValue > 0) {
            return returnValue;
        } else {
            SqlService instance = SqlService.getInstanceBySource(source);
            Connection conn = instance.getDbConnection();
            try {
                CallableStatement cs = conn.prepareCall("{call GameGetUserId_SDK_VNG(?,?,?) }");
                cs.setString("UserIdZing", username);
                cs.setString("DeviceId", deviceId);
                cs.registerOutParameter("Userid", Types.INTEGER);
                cs.execute();
                returnValue = cs.getInt("Userid");
                //Set vao cache
                String keyId = genCacheIdUserInfoKey(source, "z." + username);
                UserController.getCacheInstance().set(keyId, new Integer(returnValue), 0);
                return returnValue;
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            } finally {
                instance.releaseDbConnection(conn);
            }
        }
    }

    public String GameGetUserid_Business(int source, String bussinessToken, long facebookId) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserId_BusinessNew(?,?,?,?) }");
            cs.setString("BusinessToken", bussinessToken);
            cs.setLong("FacebookID", facebookId);
            cs.registerOutParameter("Userid", Types.INTEGER);
            cs.registerOutParameter("FaceId", Types.BIGINT);
            cs.execute();
            return cs.getInt("Userid") + "-" + cs.getLong("FaceId");
        } catch (Exception ex) {
            ex.printStackTrace();
            return "0-0";
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public com.athena.services.vo.UserFace GetFacebookInfoByDeviceId(int source, String deviceId) {
        com.athena.services.vo.UserFace objReturn = new UserFace();
        objReturn.setId(0l);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GetFacebookInfoByDeviceId(?) }");
            cs.setString("DeviceId", deviceId);
            cs.registerOutParameter("FacebookId", Types.BIGINT);
            cs.registerOutParameter("FacebookName", Types.NVARCHAR);
            cs.execute();
            objReturn.setId(cs.getLong("FacebookId"));
            objReturn.setName(cs.getString("FacebookName"));
            return objReturn;
        } catch (Exception ex) {
            ex.printStackTrace();
            return objReturn;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameGetUserid_Face(int source, long facebookId, String facebookName, String deviceId) {
        int returnValue = GameGetUseridSiamFromCache(source, "fb." + facebookId);
        if (returnValue > 0) {
            return returnValue;
        } else {
            SqlService instance = SqlService.getInstanceBySource(source);
            Connection conn = instance.getDbConnection();
            try {
                CallableStatement cs = conn.prepareCall("{call GameGetUserId_Face(?,?,?,?) }");
                cs.setLong("FacebookId", facebookId);
                cs.setString("FacebookName", facebookName);
                cs.setString("DeviceId", deviceId);
                cs.registerOutParameter("Userid", Types.INTEGER);
                cs.execute();
                returnValue = cs.getInt("Userid");
                //Set vao cache
                String keyId = genCacheIdUserInfoKey(source, "fb." + facebookId);
                UserController.getCacheInstance().set(keyId, new Integer(returnValue), 0);
                return returnValue;
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            } finally {
                instance.releaseDbConnection(conn);
            }
        }
    }

    public int GameGetUserid_FaceSiam(int source, long facebookId, String facebookName, String deviceId, String businessToken) {
        int returnValue = GameGetUseridSiamFromCache(source, "fb." + facebookId);
        if (returnValue > 0) {
            return returnValue;
        } else {
            SqlService instance = SqlService.getInstanceBySource(source);
            Connection conn = instance.getDbConnection();
            try {
                CallableStatement cs = conn.prepareCall("{call GameGetUserId_FaceSiam(?,?,?,?,?) }");
                cs.setLong("FacebookId", facebookId);
                cs.setString("FacebookName", facebookName);
                cs.setString("DeviceId", deviceId);
                cs.setString("BusinessToken", businessToken);
                cs.registerOutParameter("Userid", Types.INTEGER);
                cs.execute();
                returnValue = cs.getInt("Userid");
                //Set vao cache
                String keyId = genCacheIdUserInfoKey(source, "fb." + facebookId);
                UserController.getCacheInstance().set(keyId, new Integer(returnValue), 0);
                return returnValue;
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    System.out.println((new Date()) + "==>UserController==>GameGetUserid_FaceSiam: source: " + source
                            + " - facebookId: " + facebookId + " - facebookName: " + facebookName + " - deviceId: " + deviceId + " - businessToken: " + businessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            } finally {
                instance.releaseDbConnection(conn);
            }
        }
    }

    public int GameGetUserId_AppsId(int source, long facebookId, String businessToken) {
        int returnValue = GameGetUseridSiamFromCache(source, "fb." + facebookId);
        if (returnValue > 0) {
            return returnValue;
        } else {
            SqlService instance = SqlService.getInstanceBySource(source);
            Connection conn = instance.getDbConnection();
            try {
                CallableStatement cs = conn.prepareCall("{call GameGetUserId_AppsId(?,?,?) }");
                cs.setLong("FacebookId", facebookId);
                cs.setString("BusinessToken", businessToken);
                cs.registerOutParameter("Userid", Types.INTEGER);
                cs.execute();
                returnValue = cs.getInt("Userid");
                //Set vao cache
                String keyId = genCacheIdUserInfoKey(source, "fb." + facebookId);
                UserController.getCacheInstance().set(keyId, new Integer(returnValue), 0);
                return returnValue;
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            } finally {
                instance.releaseDbConnection(conn);
            }
        }
    }

    public int GameGetUserId_FaceIdBusiness(int source, long facebookId, long faceIdbusiness, String businessToken) {
        int returnValue = GameGetUseridSiamFromCache(source, "fb." + facebookId);
        if (returnValue > 0) {
            return returnValue;
        } else {
            SqlService instance = SqlService.getInstanceBySource(source);
            Connection conn = instance.getDbConnection();
            try {
                CallableStatement cs = conn.prepareCall("{call GameGetUserId_FaceIdBusiness(?,?,?,?) }");
                cs.setLong("FacebookId", facebookId);
                cs.setLong("faceIdbusiness", faceIdbusiness);
                cs.setString("BusinessToken", businessToken);
                cs.registerOutParameter("Userid", Types.INTEGER);
                cs.execute();
                returnValue = cs.getInt("Userid");
                //Set vao cache
                String keyId = genCacheIdUserInfoKey(source, "fb." + facebookId);
                UserController.getCacheInstance().set(keyId, new Integer(returnValue), 0);
                return returnValue;
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    System.out.println((new Date()) + "==>UserController==>GameGetUserId_FaceIdBusiness: source: " + source
                            + " - facebookId: " + facebookId + " - faceIdbusiness: " + faceIdbusiness + " - businessToken: " + businessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            } finally {
                instance.releaseDbConnection(conn);
            }
        }
    }

    public int GameGetUserId_AppsId_New(int source, long facebookId, String businessToken, String businessChildId) {
        int returnValue = GameGetUseridSiamFromCache(source, "fb." + facebookId);
        if (returnValue > 0) {
            return returnValue;
        } else {
            SqlService instance = SqlService.getInstanceBySource(source);
            Connection conn = instance.getDbConnection();
            try {
                CallableStatement cs = conn.prepareCall("{call GameGetUserId_AppsId_New(?,?,?,?) }");
                cs.setLong("FacebookId", facebookId);
                cs.setString("BusinessToken", businessToken);
                cs.setString("BusinessChildId", businessChildId);
                cs.registerOutParameter("Userid", Types.INTEGER);
                cs.execute();
                returnValue = cs.getInt("Userid");
                //Set vao cache
                String keyId = genCacheIdUserInfoKey(source, "fb." + facebookId);
                UserController.getCacheInstance().set(keyId, new Integer(returnValue), 0);
                return returnValue;
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            } finally {
                instance.releaseDbConnection(conn);
            }
        }
    }

    public int GameGetUserid_Device(int source, String deviceId, String username, String password) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserId_Device(?,?,?,?) }");
            cs.setString("DeviceId", deviceId);
            cs.setString("Username", username);
            cs.setString("Password", password);
            cs.registerOutParameter("Userid", Types.INTEGER);
            cs.execute();
            return cs.getInt("Userid");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameGetUseridVietnam(int source, String username, String pass, String deviceId) {
        String keyIdOld = genCacheIdUserInfoKey(source, ActionUtils.ValidString(username) + "___" + String.valueOf(source) + "___" + ActionUtils.ValidString(pass));//ActionUtils.ValidString(username) + "_" + ActionUtils.ValidString(oldpass) + "_" + ActionUtils.ValidString(deviceId));
        logger_login_disconnect.info("==>Login==>GetKey:" + username + "-" + keyIdOld);
        Integer uid = (Integer) UserController.getCacheInstance().get(keyIdOld);
        int returnValue = 0;
        if (uid != null) {
            returnValue = uid.intValue();
        }
        //int returnValue = GameGetUseridSiamFromCache(source, ActionUtils.ValidString(username) + "___" + String.valueOf(source) + "___" + ActionUtils.ValidString(pass)) ; // + "_" + ActionUtils.ValidString(deviceId)) ;
        if (returnValue > 0) {
            logger_login_disconnect.info("==>Login==>GetKey==>FromCache:" + username + "-" + source);
            return returnValue;
        } else {
            SqlService instance = SqlService.getInstanceBySource(source);
            Connection conn = instance.getDbConnection();
            try {
                CallableStatement cs = conn.prepareCall("{call GameGetUserId(?,?,?,?) }");
                cs.setString("Username", username);
                cs.setString("Password", pass);
                cs.setString("DeviceId", deviceId);
                cs.registerOutParameter("Userid", Types.INTEGER);
                cs.execute();
                returnValue = cs.getInt("Userid");
                logger_login_disconnect.info("==>Login==>GetKey==>FromDB:" + username + "-" + returnValue);
                //Set vao cache
                if (returnValue > 0) {
                    String keyId = genCacheIdUserInfoKey(source, ActionUtils.ValidString(username) + "___" + String.valueOf(source) + "___" + ActionUtils.ValidString(pass)); //ActionUtils.ValidString(username) + "_" + ActionUtils.ValidString(pass) + "_" + ActionUtils.ValidString(deviceId));
                    UserController.getCacheInstance().set(keyId, new Integer(returnValue), 0);
                }
                return returnValue;
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            } finally {
                instance.releaseDbConnection(conn);
            }
        }

    }

    /**
     * ********************* Process for Log To DB Log ***********
     */
    public void GameLogIUUserExperienceDt(int source, int uid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, int iWinMark, int deviceid) {
        try {
//            UserInfoCmd cmd = new UserInfoCmd("gameIUserExperienceDt", source, uid, GameId, iLevel, iWin, dtTime, iWinMark, deviceid);
            QueueCommand cmd = new LogIUUserExperienceDt_UsingCmd(source, uid, GameId, iLevel, iWin, dtTime, iWinMark, deviceid);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void GameLogIUUserExperienceDt(int source, int uid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, long iWinMark, int deviceid) {
        try {
            QueueCommand cmd = new LogIUUserExperienceDt_UsingCmd(source, uid, GameId, iLevel, iWin, dtTime, iWinMark, deviceid);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameLogIUUserExperienceDtDb(int source, int uid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, int iWinMark, int deviceid) {
        SqlService instance = SqlService.getInstanceLogBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call LogIUUserExperienceDt(?,?,?,?,?,?,?,?) }");
            cs.setInt("iUserId", uid);
            cs.setDate("dtDate", dtTime);
            cs.setInt("iWinMark", iWinMark);
            cs.setInt("iWin", iWin);
            cs.setInt("iGameID", GameId);
            cs.setInt("iLevel", iLevel);
            cs.setInt("iDev", deviceid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            int returnValue = cs.getInt("Error");
            cs.close();
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameLogIUUserExperienceDtDb(int source, int uid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, long iWinMark, int deviceid) {
        SqlService instance = SqlService.getInstanceLogBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call LogIUUserExperienceDt_Using(?,?,?,?,?,?,?,?) }");
            cs.setInt("iUserId", uid);
            cs.setDate("dtDate", dtTime);
            cs.setLong("iWinMark", iWinMark);
            cs.setInt("iWin", iWin);
            cs.setInt("iGameID", GameId);
            cs.setInt("iLevel", iLevel);
            cs.setInt("iDev", deviceid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            int returnValue = cs.getInt("Error");
            cs.close();
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameLogIUUserExperienceDt_New(int source, int uid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, int iWinMark, int deviceid, int diamondType) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameIUserExperienceDt_New", source, uid, GameId, iLevel, iWin, dtTime, iWinMark, deviceid, diamondType);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameLogIUUserExperienceDtDb_New(int source, int uid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, int iWinMark, int deviceid, int diamondType) {
        SqlService instance = SqlService.getInstanceLogBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call LogIUUserExperienceDt_New(?,?,?,?,?,?,?,?,?) }");
            cs.setInt("iUserId", uid);
            cs.setDate("dtDate", dtTime);
            cs.setInt("iWinMark", iWinMark);
            cs.setInt("iWin", iWin);
            cs.setInt("iGameID", GameId);
            cs.setInt("iLevel", iLevel);
            cs.setInt("iDev", deviceid);
            cs.setInt("iDiamond", diamondType);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            int returnValue = cs.getInt("Error");
            cs.close();
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameLogIUGameRevenue(int source, int iLevel, long iRevenue, int gameid, java.sql.Date dtTime) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameIUGameRevenue", source, iLevel, iRevenue, gameid, dtTime);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameLogIUGameRevenueDb(int source, int iLevel, long iRevenue, int gameid, java.sql.Date dtTime) {
        SqlService instance = SqlService.getInstanceLogBySource(source);
        Connection conn = instance.getDbConnection();

        try {
            //System.out.println("GameLogIUGameRevenueDb: "+source+" - level "+iLevel+" - rev "+iRevenue+" - gameid "+gameid+" - time "+dtTime);
            CallableStatement cs = conn.prepareCall("{call LogIUGameRevenue(?,?,?,?,?) }");
            cs.setLong("Revenue", iRevenue);
            cs.setInt("iLevel", iLevel);
            cs.setDate("dtDate", dtTime);
            cs.setInt("GameId", gameid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            int returnValue = cs.getInt("Error");
            cs.close();
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameLogDisconnect(int source, int userid, int gameid, int ilevel) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameILogDisconnect", source, userid, gameid, ilevel);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void GameLogDisconnectDB(int source, int userid, int gameid, int ilevel) {
        SqlService instance = SqlService.getInstanceLogBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call LogIDisconnect(?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Level", ilevel);
            cs.setInt("GameId", gameid);
            cs.execute();
            cs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ********************* Update Isonline To DB ***************
     */
    public void GameUIsonlineConnect(int source, int uid, int gameid, int operatorid, String ipaddress, String ipuser) {
        try {

            GameUIsonlineConnectDb(source, uid, gameid, operatorid, ipaddress, ipuser);
//        	UserInfoCmd cmd = new UserInfoCmd("gameUIsonlineConnectCmd", source, uid, gameid, operatorid, ipaddress, ipuser);
//            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameUIsonlineConnectDb(int source, int uid, int gameid, int operatorid, String ipaddress, String ipuser) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUserConnectedSiam(?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("GameId", gameid);
            cs.setInt("OnlineIn", operatorid);
            cs.setString("IPAddress", ipaddress);
            cs.setString("IPUser", ipuser);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            int returnValue = cs.getInt("Error");
            cs.close();
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                System.out.println((new Date()) + "==>UserController==>GameUIsonlineConnectDb: source: " + source + " - uid: " + uid + "  - gameid: " + gameid
                        + " - operatorid: " + operatorid + " - ipaddress: " + ipaddress + " ipuser: " + ipuser);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int RestoreHighLowDB(int source, int uid, int aghigh, int aglow) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameRestoreHighLow(?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("AGHigh", aghigh);
            cs.setInt("AGLow", aglow);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            int returnValue = cs.getInt("Error");
            cs.close();
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ********************* Update and Get Jackpot ***************
     */
    public long GetTotalJackpot(int source) {
        try {
            String keyJackpot = "TotalJackpotBinh" + source;
            Long objValue = (Long) UserController.getCacheInstance().get(keyJackpot);
            if (objValue == null) {
                long returnValue = 0;
                SqlService instance = SqlService.getInstanceBySource(source);
                Connection conn = instance.getDbConnection();
                try {
                    CallableStatement cs = conn.prepareCall("{call GameGetTotalJackpot(?) }");
                    cs.registerOutParameter("TotalJP", Types.BIGINT);
                    cs.execute();
                    returnValue = cs.getLong("TotalJP");
                    UserController.getCacheInstance().set(keyJackpot, Long.parseLong(String.valueOf(returnValue)), 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    instance.releaseDbConnection(conn);
                }
                return returnValue;
            } else {
                return objValue.longValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    public long GetTotalJackpotDiamond(int source) {
        try {
            String keyJackpot = "TotalJackpotBinhDiamond" + source;
            Long objValue = (Long) UserController.getCacheInstance().get(keyJackpot);
            if (objValue == null) {
                long returnValue = 0;
                SqlService instance = SqlService.getInstanceBySource(source);
                Connection conn = instance.getDbConnection();
                try {
                    CallableStatement cs = conn.prepareCall("{call GameGetTotalJackpot_Diamond(?) }");
                    cs.registerOutParameter("TotalJP", Types.BIGINT);
                    cs.execute();
                    returnValue = cs.getLong("TotalJP");
                    UserController.getCacheInstance().set(keyJackpot, Long.parseLong(String.valueOf(returnValue)), 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    instance.releaseDbConnection(conn);
                }
                return returnValue;
            } else {
                return objValue.longValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    public void UpdateJackpot(int source, int mark, int diamondType) {
        try {
            String keyJackpot = "";
            if (diamondType == 0) {
                keyJackpot = "TotalJackpotBinh" + source;
            } else {
                keyJackpot = "TotalJackpotBinhDiamond" + source;
            }
            Long objValue = (Long) UserController.getCacheInstance().get(keyJackpot);
            if (objValue != null) {
                objValue = objValue.longValue() + mark;
                UserController.getCacheInstance().set(keyJackpot, Long.parseLong(String.valueOf(objValue)), 0);
            }
            UserInfoCmd cmd = new UserInfoCmd("gameUJackpot", source, mark, diamondType);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void UpdateJackpotWinDB(int source, String username, int vip, int markunit, int mark, int userid, int avatar, int diamondType) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIJackpotWin_New(?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Markunit", markunit);
            cs.setInt("Mark", mark);
            cs.setInt("DiamondType", diamondType);
            cs.execute();
            JackpotWin obj = new JackpotWin();
            obj.setVip(vip);
            obj.setUserid(userid);
            obj.setUsername(username);
            obj.setMarkUnit(markunit);
            obj.setMarkWin(mark);
            obj.setAvatar(avatar);
            obj.setTimeWin(System.currentTimeMillis());
            obj.setDiamondType(diamondType);
            String keyJackpot = "ListJackpotBinhWin" + source;
            if (mark > 0) {
                JackpotWinList lsJackpotWin = (JackpotWinList) UserController.getCacheInstance().get(keyJackpot);
                if (lsJackpotWin != null) {
                    lsJackpotWin.getLswin().add(obj);
                } else {
                    lsJackpotWin = new JackpotWinList();
                    lsJackpotWin.getLswin().add(obj);
                }
                UserController.getCacheInstance().set(keyJackpot, lsJackpotWin, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void UpdateListJackpotWinCached(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call RptGetJackpotWin(?,?,?) }");
            long time = new java.util.Date().getTime();
            cs.setDate("sdate", new java.sql.Date(time - 2592000000l));
            cs.setDate("edate", new java.sql.Date(time));
            cs.setInt("type", 1);
            ResultSet rs = cs.executeQuery();
            String keyJackpot = "ListJackpotBinhWin" + source;
            JackpotWinList lsJackpotWin = new JackpotWinList();
            while (rs.next()) {
                JackpotWin obj = new JackpotWin();
                //"Id":"2117","Markunit":"1000","Mark":"0","CreateTime":"2017-04-11 03:24:07","Userid":"68236","DiamondType":"0"
                //,"UserName":"tung123","UsernameLQ":"","VIP":"3","Avatar":"16"}],"id":null}
                obj.setVip(rs.getInt("Vip"));
                obj.setUserid(rs.getInt("Userid"));
                String uname = rs.getString("UsernameLQ");
                if (uname.length() == 0) {
                    obj.setUsername(rs.getString("UserName"));
                } else {
                    obj.setUsername(uname);
                }
                obj.setMarkUnit(rs.getInt("Markunit"));
                obj.setMarkWin(rs.getInt("Mark"));
                obj.setAvatar(Integer.parseInt(rs.getString("Avatar")));
                obj.setTimeWin(rs.getDate("CreateTime").getTime());
                obj.setDiamondType(0);
                lsJackpotWin.getLswin().add(obj);
            }
            UserController.getCacheInstance().set(keyJackpot, lsJackpotWin, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public JackpotWinList GetListJackpotWin(int source) {
        try {
            String keyJackpot = "ListJackpotBinhWin" + source;
            JackpotWinList lsJackpotwin = (JackpotWinList) UserController.getCacheInstance().get(keyJackpot);
            if (lsJackpotwin == null) {
                return new JackpotWinList();
            } else {
                return lsJackpotwin;
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
            return new JackpotWinList();
        }
    }

    public void UpdateJackpotDB(int source, int mark, int diamondType) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateJackpot_New(?,?) }");
            cs.setInt("Mark", mark);
            cs.setInt("DiamondType", diamondType);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * *********************Promotion User************************
     */
    public void GameIBonusChipByTime(int source, int uid, String username, long chips) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameIBonusChipByTime", source, uid, 0, username, chips);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void GameIBonusChipByTimeDB(int source, int uid, String username, long gold) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIBonusChipByTime(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Username", username);
            cs.setLong("Chip", gold);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /*public void GameITempPromoteDtNewUser(int source, int uid, int mark,int agpay,int agmatch, String deviceid) {
        try {
        	UserInfoCmd cmd = new UserInfoCmd("gameITempPromoteDtCmdNewUser", source, uid, mark, agpay, agmatch, deviceid);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
        }
    }
    
    public int GameITempPromoteDtDbNewUser(int source,int uid,int ag,int agpay,int agmatch, String deviceid){
        try{
        	CallableStatement cs = conn.prepareCall("{call GameITempPromoteDtNewUser(?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Deviceid", deviceid) ;
            cs.setLong("AG", ag);
            cs.setLong("AGPay", agpay);
            cs.setLong("AGMatch", agmatch);
            cs.registerOutParameter("ERROR", Types.BIGINT);
            cs.execute();
            return cs.getInt("ERROR");
        } catch (Exception ex) {
            return -1;
        }
    }*/
    public int GameITempPromoteDt(int source, int uid, int mark, int agpay, int agmatch, String deviceid) {
        try {
            this.UpdateAG(source, uid, mark, false);
            this.GameITempPromoteDtCmd(source, uid, mark, agpay, agmatch, deviceid);
            return 1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public void GameITempPromoteDtCmd(int source, int uid, int mark, int agpay, int agmatch, String deviceid) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameITempPromoteDtCmd", source, uid, mark, agpay, agmatch, deviceid);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameITempPromoteTomorrowDb(int source, int uid, int ag, String deviceid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameITempPromoteTomorrow(?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Deviceid", deviceid);
            cs.setLong("AG", ag);
            cs.registerOutParameter("ERROR", Types.BIGINT);
            cs.execute();
            return cs.getInt("ERROR");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GamePromotionByGameNo(int source, int uid, int ag, String username, int gameno, int vip) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameITempPromoteByGameNo(?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Username", username);
            cs.setInt("GameNo", gameno);
            cs.setInt("Vip", vip);
            cs.setInt("AG", ag);
            cs.registerOutParameter("ERROR", Types.BIGINT);
            cs.execute();
            return cs.getInt("ERROR");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameITempPromoteDtDb(int source, int uid, int ag, int agpay, int agmatch, String deviceid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameITempPromoteDt(?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Deviceid", deviceid);
            cs.setLong("AG", ag);
            cs.setLong("AGPay", agpay);
            cs.setLong("AGMatch", agmatch);
            cs.registerOutParameter("ERROR", Types.BIGINT);
            cs.execute();
            return cs.getInt("ERROR");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameUTempPromotionDt(int source, String arrid) {
        try {
            this.GameUTempPromotionDtCmd(source, arrid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void GameUTempPromotionDtCmd(int source, String arrid) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameUTempPromoteDtCmd", source, arrid);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void GameUTempPromotionDtDb(int source, String arrid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUTempPromotionDt(?) }");
            cs.setString("String", arrid);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ********* Process for Roulette ********************
     */
    public long GameIRouletteDb(int source, int uid, String uname, Roulette rl) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIRoulette(?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", uid);
            cs.setString("Username", uname);
            cs.setString("ListNumber", ActionUtils.gson.toJson(rl.getArr()));
            cs.setInt("NumberWin", rl.getNumberWin());
            cs.setBoolean("isBuy", rl.isBuy());
            cs.setInt("LQBuy", rl.getLQBuy());
            cs.setInt("TotalLQ", rl.getLQTotal());
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    public int GameURoulette(int source, int uid, long id, int lqbuy, String username) {
        try {
            if (lqbuy > 0) {
                this.UpdateAG(source, uid, lqbuy * 33, false); //Call Update AG Cache + DB
                this.GameURouletteCmd(source, uid, id, lqbuy, username);
                return lqbuy * 33;
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public void GameURouletteCmd(int source, int uid, long id, int lqbuy, String username) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameURouletteCmd", source, uid, id, lqbuy, username);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameURouletteDb(int source, int userid, long id, int lqbuy, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameURoulette(?,?,?,?,?) }");
            cs.setLong("Id", id);
            cs.setInt("Userid", userid);
            cs.setString("Username", username);
            cs.setInt("LQBuy", lqbuy);
            cs.registerOutParameter("AGWin", Types.INTEGER);
            cs.execute();
            return cs.getInt("AGWin");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public long UpdateRouletteMultiDb(int source, int userid, String uname, Roulette rl) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIRouletteMulti(?,?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("Username", uname);
            cs.setString("ListNumber", ActionUtils.gson.toJson(rl.getArr()));
            cs.setInt("NumberWin", rl.getNumberWin());
            cs.setInt("AGWin", rl.getAgWin());
            cs.setBoolean("isBuy", rl.isBuy());
            cs.setInt("LQBuy", rl.getLQBuy());
            cs.setInt("TotalLQ", rl.getLQTotal());
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    /**
     * *************** Promotion ***************************
     */
    private String genCachePromotionKey(int source, int userId) {
        return ServerDefined.getKeyPromotion(source) + userId;
    }

    public void SetPromotionToCache(int source, int uid, ListPromotionObj lsPromotion) {
        try {
            String keyPromotion = genCachePromotionKey(source, uid);
//            logger_.info("==>GetListPromotionDB==>Length:" + uid + "-" + lsret.size() + "-" + keyPromotion);
            UserController.getCacheInstance().set(keyPromotion, lsPromotion, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ListPromotionObj GetPromotionToCache(int source, int uid) {
        try {
            String keyPromotion = genCachePromotionKey(source, uid);
            return (ListPromotionObj) UserController.getCacheInstance().get(keyPromotion);
        } catch (Exception e) {
            //handle exception
            return null;
        }
    }

    /**
     * *************** Process for Xocdia   ******************************
     */
    public long UpdateXocdiaDetail(int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, int Result, int Win) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameUXocdiaCmd", source, Userid, TableId, StrNum, StrMark, NumBuy, MarkBuy, Dealer, Result, Win);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0l;
    }

    public long UpdateXocdiaDetailLongValue(int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameUXocdiaCmdLong", source, Userid, TableId, StrNum, StrMark, NumBuy, MarkBuy, Dealer, Result, Win);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0l;
    }

    public long UpdateXocdiaDetailLong_Using(int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win, int GameId) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameUXocdiaCmd_Using", source, Userid, TableId, StrNum, StrMark, NumBuy, MarkBuy, Dealer, Result, Win, GameId);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0l;
    }

    public long UpdateXocdiaDetailDb(int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, int Result, int Win) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIXocdiaDetail(?,?,?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", Userid);
            cs.setInt("TableId", TableId);
            cs.setString("StrNum", StrNum);
            cs.setString("StrMark", StrMark);
            cs.setInt("NumBuy", NumBuy);
            cs.setInt("Dealer", Dealer);
            cs.setInt("MarkBuy", MarkBuy);
            cs.setInt("Result", Result);
            cs.setInt("Win", Win);
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    public long UpdateXocdiaDetailDbLong(int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIXocdiaDetail(?,?,?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", Userid);
            cs.setInt("TableId", TableId);
            cs.setString("StrNum", StrNum);
            cs.setString("StrMark", StrMark);
            cs.setInt("NumBuy", NumBuy);
            cs.setInt("Dealer", Dealer);
            cs.setLong("MarkBuy", MarkBuy);
            cs.setInt("Result", Result);
            cs.setLong("Win", Win);
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    public long UpdateXocdiaDetailDb_Using(int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win, int GameId) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIXocdiaDetail_Using(?,?,?,?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", Userid);
            cs.setInt("TableId", TableId);
            cs.setString("StrNum", StrNum);
            cs.setString("StrMark", StrMark);
            cs.setInt("NumBuy", NumBuy);
            cs.setInt("Dealer", Dealer);
            cs.setLong("MarkBuy", MarkBuy);
            cs.setInt("Result", Result);
            cs.setLong("Win", Win);
            cs.setInt("GameId", GameId);
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    public long UpdateHiloDetail(int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, String Result, int Win, int GameId) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("gameUHiloCmd", source, Userid, TableId, StrNum, StrMark, NumBuy, MarkBuy, Dealer, Result, Win, GameId);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0l;
    }

    public long UpdateHiloDetailDb(int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, String Result, int Win, int GameId) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIHiloDetail_New(?,?,?,?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", Userid);
            cs.setInt("TableId", TableId);
            cs.setString("StrNum", StrNum);
            cs.setString("StrMark", StrMark);
            cs.setInt("NumBuy", NumBuy);
            cs.setInt("Dealer", Dealer);
            cs.setInt("MarkBuy", MarkBuy);
            cs.setString("Result", Result);
            cs.setInt("Win", Win);
            cs.setInt("GameId", GameId);
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    /**
     * *************** Process for Transfer AG ***************************
     */
    public int GameTransferAGDb(int source, int uid, int ag, int item, String name, int diamondType) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameTransferAG_New(?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("AG", ag);
            cs.setInt("item", item);
            cs.setInt("DiamondType", diamondType);
            cs.setString("Username", name);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameTransferAGFinishDb(int source, int id, String uname, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameTransferAGFinish_Using(?,?,?,?) }");
            cs.setInt("ID", id);
            cs.setString("ToUser", uname);
            cs.setInt("Userid", userid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ************** User Disconnect ************************
     */
    public void UpdateDisconnectToCache(int source, int uid) {
        try {
            //Update Cache
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setDisconnect(true);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int UserDisconnected(int source, int uid) {
        try {
            //Update Cache
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setIsOnline((short) 0);
                uinfo.setIdolName("");
                uinfo.setTableId(0);
                UserController.getCacheInstance().set(key, uinfo, 0);
                UserDisconnectedDB(source, uid);
                uinfo = (UserInfo) UserController.getCacheInstance().get(key);
                if (uinfo.getIsOnline() != 0) {
                    System.out.println("==>UpdateUserOnlineFail: " + (new Date()).toString() + ":" + key + ":\t" + uinfo.getIsOnline());
                }
                return 0;
            } else {
                System.out.println("==>UserDisconnect Not Cache:" + source + "-" + uid + "-" + key);
                UserDisconnectedDB(source, uid);
            }
            //Update DB
            return -1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public int UserDisconnectedDB(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUserDisconnect(?,?) }");
            cs.setInt("UserId", uid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                System.out.println((new Date()) + "==>UserController==>UserDisconnectedDB: source: " + source + " - uid: " + uid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * *************Get Vip, MarkVip *********************
     */
    public String GameGetMarkVipDB(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetMarkVip_New(?,?,?) }");
            cs.setInt("Userid", uid);
            cs.registerOutParameter("MarkVip", Types.INTEGER);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.execute();
            return String.valueOf(cs.getInt("MarkVip")) + ";" + String.valueOf(cs.getInt("Vip")) + ";";
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                System.out.println((new Date()) + "==>UserController==>GameGetMarkVipDB: source: " + source + " - uid: " + uid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "0;0;";
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * *************Get Vip, MarkVip, MarkLevel *********************
     */
    public String GameGetMarkVipLVDB(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();

        long startProcess = System.currentTimeMillis();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetMarkVip_Using(?,?,?,?) }");
            cs.setInt("Userid", uid);
            cs.registerOutParameter("MarkVip", Types.INTEGER);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.registerOutParameter("MarkLevel", Types.INTEGER);
            cs.execute();

            String result  = String.valueOf(cs.getInt("MarkVip")) + ";"
                    + String.valueOf(cs.getInt("Vip")) + ";"
                    + String.valueOf(cs.getInt("MarkLevel")) + ";";


            return result;
        } catch (Exception ex) {
            ex.printStackTrace();

            return "0;0;0;";
        } finally {
            long endProcess = System.currentTimeMillis();

            logger_login_execute_time.info(new UCLogBean("GameGetMarkVipLVDB", (endProcess - startProcess),
                    "source: " + source + " - uid: " + uid));

            instance.releaseDbConnection(conn);
        }
    }

    /**
     * *********** User Setting ********************
     */
    private String genCacheUserSettingKey(int source, int userId) {
        return ServerDefined.getKeyCacheUserSetting(source) + userId;
    }

    public UserSetting GetUserSettingByUserid(int source, int userId) {
        UserSetting usetting = null;
        try {
            String key = genCacheUserSettingKey(source, userId);
            usetting = (UserSetting) UserController.getCacheInstance().get(key);
            if (usetting == null) {
                usetting = new UserSetting();
                usetting.setUserid(userId);
                this.GetUserSettingDb(source, usetting);
            }
            UserController.getCacheInstance().set(key, usetting, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return usetting;
    }

    public void GetUserSettingDb(int source, UserSetting usetting) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserSetting(?) }");
            cs.setInt("UserId", usetting.getUserid());
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                usetting.setId(rs.getInt("ID"));
                usetting.setBm(rs.getBoolean("BackgroundMusic"));
                usetting.setS(rs.getBoolean("Sound"));
                usetting.setI(rs.getBoolean("Invite"));
                usetting.setXA((short) rs.getInt("XitoA"));
            }
            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GetUserSettingDb(int source, UserInfo user) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            UserSetting um = new UserSetting();
            um.setUserid(user.getUserid());
            user.setUserSetting(um);
            CallableStatement cs = conn.prepareCall("{call GameGetUserSetting(?) }");
            if (user.getSource() == 1) {
                cs.setInt("UserId", user.getUserid());
            } else if (user.getSource() == 2) {
                cs.setInt("UserId", user.getUserid() - 1000000000);
            } else if (user.getSource() == 3) {
                cs.setInt("UserId", user.getUserid() - 1100000000);
            } else if (user.getSource() == 4) {
                cs.setInt("UserId", user.getUserid() - 1200000000);
            } else if (user.getSource() == 5) {
                cs.setInt("UserId", user.getUserid() - 1300000000);
            } else if (user.getSource() == ServerSource.THAI_SOURCE) {
                cs.setInt("UserId", user.getUserid() - 500000000);
            }
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                user.getUserSetting().setId(rs.getInt("ID"));
                user.getUserSetting().setBm(rs.getBoolean("BackgroundMusic"));
                user.getUserSetting().setS(rs.getBoolean("Sound"));
                user.getUserSetting().setI(rs.getBoolean("Invite"));
                user.getUserSetting().setXA((short) rs.getInt("XitoA"));
            }
            rs.close();
            return 1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameUpdateUserSettingToCache(int source, Integer userId, boolean bm, boolean s, boolean i, int id) {
        try {
            UserSetting usetting = null;
            try {
                String key = genCacheUserSettingKey(source, userId);
                usetting = (UserSetting) UserController.getCacheInstance().get(key);
                if (usetting == null) {
                    usetting = new UserSetting();
                    usetting.setUserid(userId);
                    usetting.setId(id);
                }
                usetting.setBm(bm);
                usetting.setS(s);
                usetting.setI(i);
                UserController.getCacheInstance().set(key, usetting, 0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameIUserSettingDb(int source, Integer userid, boolean bm, boolean s, boolean i) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIUserSetting(?,?,?,?,?) }");
            cs.setInt("UserId", userid);
            cs.setBoolean("Bm", bm);
            cs.setBoolean("S", s);
            cs.setBoolean("I", i);
            cs.registerOutParameter("Id", Types.INTEGER);
            cs.execute();
            return cs.getInt("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * **************** Gift Code ***********************
     */
    public String GameCheckGiftCode(int source, String code, int userid) {
        long startTime = System.currentTimeMillis();

        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameCheckGiftCode_Using(?,?,?,?,?) }");
            cs.setInt("UserId", userid);
            cs.setString("Code", code);
            cs.registerOutParameter("Id", Types.INTEGER);
            cs.registerOutParameter("AgCode", Types.INTEGER);
            cs.registerOutParameter("StrAlert", Types.NVARCHAR);
            cs.execute();
            return cs.getInt("Id") + "#" + cs.getInt("AgCode") + "#" + cs.getString("StrAlert") + "#";
        } catch (Exception ex) {
            logger_debug_user_control.error(ex.getMessage(), ex);
            return "0#0#a#";
        } finally {
            long endTime = System.currentTimeMillis();
            logger_login_execute_time.info(new UCLogBean("GameCheckGiftCode",(endTime - startTime),
                    source + " "+ code+" "+ userid   ));
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * **************** Share Facebook ***********************
     */
    public int GameCheckShareFacebook(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameCheckShareFacebook(?,?) }");
            cs.setInt("UserId", userid);
            cs.registerOutParameter("Ag", Types.INTEGER);
            cs.execute();
            return cs.getInt("Ag");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ************************************************
     */
    public int GetPromotionValueDB(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetPromotionValue(?,?) }");
            cs.setInt("Userid", userid);
            cs.registerOutParameter("PromotionValue", Types.DOUBLE);
            cs.execute();
            return (int) cs.getDouble("PromotionValue");
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public int GetGameCountDB(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetGameCount(?,?) }");
            cs.setInt("Userid", userid);
            cs.registerOutParameter("GameCount", Types.INTEGER);
            cs.execute();
            return cs.getInt("GameCount");
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
            try {
                System.out.println((new Date()) + "==>UserController==>GetGameCountDB: source: " + source + " - uid: " + userid);
            } catch (Exception ex) {
                e.printStackTrace();
            }
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public int GetNumberNewMailDB(int source, String usernamelq, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetNumberNewMail(?,?,?) }");
            cs.setString("Username", username);
            cs.setString("UsernameLQ", usernamelq);
            cs.registerOutParameter("Newmail", Types.INTEGER);
            cs.execute();
            return cs.getInt("Newmail");
        } catch (Exception e) {
            //handle exception
            try {
                System.out.println((new Date()) + "==>UserController==>GetNumberNewMailDB: source: " + source + " - usernamelq: " + usernamelq + " - username: " + username);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public void AddLogOnlineDB(int source, int numberOnline, int gameid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameAddLogOnline(?,?) }");
            cs.setInt("NumberO", numberOnline);
            cs.setInt("Gameid", gameid);
            cs.execute();
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ******************* Daily Promotion **************************
     */
    public void UpdateSiamDailyFirstTimeCmd(int source, int uid) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updateDailyPromotionFirstTime", source, uid);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
        }
    }

    public void UpdateSiamDailyFirstTimeDb(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUDailyPromotionFirstTime(?,?) }");
            cs.setInt("UserId", uid);
            cs.execute();
            cs.close();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void UpdateSiamDailyCmd(int source, int uid, int ag) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updateDailyPromotion", source, uid, ag);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
        }
    }

    public void UpdateSiamDailyDb(int source, int uid, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUDailyPromotion(?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("AG", ag);
            cs.execute();
            cs.close();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * *************** Update Avatar ***************************
     */
    public void GameUpdateAvatar(int source, int uid, int avatar) {
        try {
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setAvatar((short) avatar);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
            UserInfoCmd cmd = new UserInfoCmd("updateAvatarUser", source, uid, avatar);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameUpdateAvatarDb(int source, int uid, int avatar) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateAvatar(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("Avatar", avatar);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ********************* Football ********************************
     */
    public void GameIMatchPlayer(int source, int userid, int matid, int bet, float betValue, int ag, short vip) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updateBetFootball", source, userid, matid, bet, betValue, ag);
            QueueManager.getInstance(queuename).put(cmd);
            UpdateAGCache(source, userid, 0 - ag, vip, 0);
        } catch (Exception ex) {
        }
    }

    public int GameIMatchPlayerDb(int source, int userid, int matid, int bet, float betValue, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIMatchPlayer(?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("MatchID", matid);
            cs.setInt("Bet", bet);
            cs.setFloat("BetValue", betValue);
            cs.setInt("AG", ag);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ******************** Lotery  ************************************
     */
    public void GameILottery(int source, int uid, float tyle, int soluong, int menhgia, String so, short vip) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updateLottery", source, uid, tyle, soluong, menhgia, so);
            QueueManager.getInstance(queuename).put(cmd);
            UpdateAGCache(source, uid, 0 - soluong * menhgia, vip, 0);
        } catch (Exception ex) {
        }
    }

    public int GameILotteryDb(int source, int uid, float tyle, int soluong, int menhgia, String so) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceTheMayManAddXoSo(?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setFloat("Tyle", tyle);
            cs.setInt("Soluong", soluong);
            cs.setInt("Menhgia", menhgia);
            cs.setString("So", so);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * **************** Pass Lock *********************************
     */
    public void GameUPassLockDB(int source, int uid, String pass) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUPassLock(?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Pass", pass);
            cs.execute();
            cs.close();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameUPassLock(int source, int uid, String pass) {
        try {
            UserInfoCmd cmd = new UserInfoCmd("updatePassLock", source, uid, pass);
            QueueManager.getInstance(queuename).put(cmd);
            String key = genCacheUserInfoKey(source, uid);
            UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
            if (uinfo != null) {
                uinfo.setPassLock(pass);
                UserController.getCacheInstance().set(key, uinfo, 0);
            }
        } catch (Exception ex) {
        }
    }

    public int GetUserIDByUsername(int source, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserIDByUserName(?,?) }");
            cs.setString("Username", username);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //////////////////////////////////////
    public int Service68PlayLogin(int source, String username, String password, String deviceid, int operatorid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call Service68PlayLogin(?,?,?,?,?) }");
            cs.setString("Username", username);
            cs.setString("Password", password);
            cs.setString("DeviceId", deviceid);
            cs.setInt("OperatorId", operatorid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void UserLoginCard(int source, UserInfo ulogin) {

    }

    public List<String> GameIAuctionPlayer(int source, int userid, long aucid, String uname, int vip, int type, int price, int step) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIAuctionPlayer(?,?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setLong("AuctionID", aucid);
            cs.setString("Username", uname);
            cs.setInt("Price", price);
            cs.setInt("VIP", vip);
            cs.setInt("Type", type);
            cs.setInt("PriceStep", step);
            cs.registerOutParameter("TotalAuc", Types.INTEGER);
            //cs.registerOutParameter("Position", Types.INTEGER);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            List<String> lsret = new ArrayList<String>();
            if (cs.getInt("Error") < 0) {
                return null;
            }
            lsret.add(String.valueOf(cs.getInt("TotalAuc")));
            if (cs.getInt("Error") == 1) {
                lsret.add("true");
            }
            if (cs.getInt("Error") == 2) {
                lsret.add("false");
            }
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<TopGamer> GameGetListTopRich(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListTopRich() }");
            ResultSet rs = cs.executeQuery();
            List<TopGamer> lsret = new ArrayList<TopGamer>();
            //DateFormat readFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss aa");
            while (rs.next()) {
                TopGamer um = new TopGamer(rs.getInt("ID"), rs.getString("Username"), rs.getString("UsernameLQ"), rs.getInt("Diamond"),
                        rs.getLong("AG"), rs.getInt("Avatar"), rs.getLong("Faid"), rs.getInt("IsOnline"), 0, rs.getInt("Vip"));
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<String> GameGetListDefaultName(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetDefaultName() }");
            ResultSet rs = cs.executeQuery();
            List<String> lsret = new ArrayList<String>();
            while (rs.next()) {
                lsret.add(rs.getString("Username"));
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<Match> GameGetListMatch(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetMatch() }");
            ResultSet rs = cs.executeQuery();
            List<Match> lsret = new ArrayList<Match>();
            //DateFormat readFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss aa");
            while (rs.next()) {
                Match um = new Match();
                um.setId(rs.getInt("ID"));
                um.setMatchName(rs.getString("MatchName"));
                um.setWin(rs.getFloat("Win"));
                um.setDraw(rs.getFloat("Draw"));
                um.setLost(rs.getFloat("Lost"));
                um.setStopTime(rs.getTimestamp("StopTime"));
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<Auction> GameGetAuction(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetAuction() }");
            ResultSet rs = cs.executeQuery();
            List<Auction> lsret = new ArrayList<Auction>();
            //DateFormat readFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss aa");
            while (rs.next()) {
                Auction um = new Auction();
                um.setAuctionId(rs.getLong("AuctionID"));
                um.setAuctionType(rs.getInt("AuctionType"));
                um.setPriceStep(rs.getInt("PriceStep"));
                um.setStartTime(rs.getTimestamp("StartTime"));
                um.setFinishTime(rs.getTimestamp("FinishTime"));
                um.setConditionVIP(rs.getInt("ConditionVIP"));
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<AlertPromotion> GameGetAlertPromotion(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetAlertPromotion() }");
            ResultSet rs = cs.executeQuery();
            List<AlertPromotion> lsret = new ArrayList<AlertPromotion>();
            while (rs.next()) {
                AlertPromotion um = new AlertPromotion();
                um.setDescription(rs.getString("Description"));
                um.setStarttime(rs.getTimestamp("StartTime"));
                um.setEndtime(rs.getTimestamp("EndTime"));
                um.setId(rs.getInt("Id"));
                um.setOperator(rs.getInt("OperatorId"));
                um.setP(rs.getInt("KMValue"));
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<UserWinEvent> GameGetUserWinEvent(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserWinEvent() }");
            ResultSet rs = cs.executeQuery();
            List<UserWinEvent> lsret = new ArrayList<UserWinEvent>();
            while (rs.next()) {
                UserWinEvent um = new UserWinEvent();
                um.setUsername(rs.getString("Username"));
                um.setAgwin(rs.getInt("AGWin"));
                um.setId(rs.getInt("Id"));
                um.setMsg(rs.getString("MsgInGame"));
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<AlertSchedule> GameGetAlertSchedule(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceGetListAlertSchedule() }");
            ResultSet rs = cs.executeQuery();
            List<AlertSchedule> lsret = new ArrayList<AlertSchedule>();
            while (rs.next()) {
                AlertSchedule um = new AlertSchedule();
                um.setContentm(rs.getString("ContentMsg"));
                um.setImgm(rs.getString("ImgMsg"));
                um.setLinkm(rs.getString("LinkMsg"));
                um.setTypem(rs.getInt("TypeMsg"));
                um.setTimem(rs.getTimestamp("TimeMsg"));
                um.setId(rs.getInt("Id"));
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<MarkCreateTable> GameGetMarkCreateTable(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetMarkCreateTable() }");
            ResultSet rs = cs.executeQuery();
            List<MarkCreateTable> lsret = new ArrayList<MarkCreateTable>();
            while (rs.next()) {
                MarkCreateTable um = new MarkCreateTable();
                um.setAg(rs.getInt("AG"));
                um.setGameid(rs.getInt("GameId"));
                um.setMark(rs.getInt("Mark"));
                um.setCondition(rs.getInt("Condition"));
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<TopGamer> GameGetListTopGamer(int source, int gameid, int typeid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            List<TopGamer> lsret = new ArrayList<TopGamer>();
            CallableStatement cs = conn.prepareCall("{call GameGetListTopGamer(?,?) }");
            cs.setInt("Gameid", gameid);
            cs.setInt("Typeid", typeid);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                TopGamer um = new TopGamer(0, rs.getString("N"), rs.getString("NLQ"), rs.getInt("A"), rs.getLong("M"),
                        rs.getInt("Av"), rs.getLong("Faid"), rs.getInt("Gid"), rs.getInt("Typeid"), rs.getInt("V"));
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<TopGamer> GameGetListTopHighlow(int source, int typeid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            List<TopGamer> lsret = new ArrayList<TopGamer>();
            CallableStatement cs = conn.prepareCall("{call GameGetListTop_HighLow(?) }");
            cs.setInt("TypeTop", typeid);
            ResultSet rs = cs.executeQuery();
            // public TopGamer(int id, String N, String NLQ, int A, long M, int av, long faid,int gameid, int typeid, int V){
            while (rs.next()) {
                TopGamer um = new TopGamer(0, rs.getString("Username"), rs.getString("UsernameLQ"), 0, rs.getInt("AGWin"), rs.getInt("Av"),
                        rs.getLong("FacebookID"), 8100, 0, rs.getInt("Vip"));

                um.setTypeid(typeid);
                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameGetTypeCardLuckyTournament(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetTypeCardLuckyTournament(?) }");
            cs.registerOutParameter("TourType", Types.INTEGER);
            cs.execute();
            return cs.getInt("TourType");
        } catch (Exception ex) {
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public UserAfterPay GameGetUserInfoAfterPay(int source, int uid) {
        //Lay Lai du lieu
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            UserAfterPay userreturn = new UserAfterPay();
            CallableStatement cs = conn.prepareCall("{call GameGetUserInfoAfterPay(?) }");
            cs.setInt("Userid", uid);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                userreturn.setUserid(rs.getInt("id"));
                userreturn.setAg(rs.getInt("AG"));
                userreturn.setLq(rs.getInt("Gold"));
                userreturn.setVip((short) rs.getInt("Vip"));
                userreturn.setDm(rs.getLong("Diamond"));
                userreturn.setLqinday(rs.getInt("ChessElo"));
            }
            rs.close();
            cs.close();
            return userreturn;
        } catch (Exception ex) {
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameUpdateRef(int source, int uid, String ref, String strMail, String strVersion) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateRefTemp(?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            if (ref.length() > 200) {
                ref = ref.substring(0, 195);
            }
            cs.setString("Ref", ref);
            cs.setString("StrMail", strMail);
            cs.setString("StrVersion", strVersion);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameUpdateUsername(int source, int uid, String username, String pass) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateUsername(?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Username", username.toLowerCase());
            cs.setString("Password", pass);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameUpdatePassword(int source, int uid, String oldpass, String newpass, String username, String deviceId) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdatePassword(?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("OldPass", oldpass);
            cs.setString("NewPass", newpass);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            int returnValue = cs.getInt("Error");
            if (returnValue == 1) {
                //Remove Key Pass khoi cache
                String keyId1 = genCacheIdUserInfoKey(source, ActionUtils.ValidString(username) + "___" + String.valueOf(source) + "___" + ActionUtils.ValidString(oldpass));//ActionUtils.ValidString(username) + "_" + ActionUtils.ValidString(oldpass) + "_" + ActionUtils.ValidString(deviceId));
                boolean resultOldpass = UserController.getCacheInstance().remove(keyId1);
                //Add Key moi
                String keyId2 = genCacheIdUserInfoKey(source, ActionUtils.ValidString(username) + "___" + String.valueOf(source) + "___" + ActionUtils.ValidString(newpass)); //ActionUtils.ValidString(username) + "_" + ActionUtils.ValidString(newpass) + "_" + ActionUtils.ValidString(deviceId));
                boolean resultNewpass = UserController.getCacheInstance().set(keyId2, new Integer(uid), 0);
                logger_login_disconnect.info("==>GameUpdatePassword: keyId1: " + keyId1 + " - resultOldpass: " + resultOldpass + " - keyId2: " + keyId2 + " - resultNewpass: " + resultNewpass);
            }
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameUUserVIP(int source, int uid, int vip, int ag, int lq) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call prc_game_updateuservip(?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("VIP", vip);
            cs.setInt("AG", ag);
            cs.setInt("LQ", lq);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameUpdateBot(int source, int uid, int vip, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call Bot_UpdateVipAG(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("VIP", vip);
            cs.setInt("AG", ag);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameUpdateBotNew(int source, int uid, int vip, long ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call Bot_UpdateVipAG_New(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("VIP", vip);
            cs.setLong("AG", ag);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameIVideoGold(int source, int uid, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIVideoGold(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("AG", ag);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public void GameGetGiftCode(int source, HashMap<String, GiftCode> dic, List<GiftCode> lsGiftCode) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            dic.clear();
            lsGiftCode.clear();
            CallableStatement cs = conn.prepareCall("{call GameGetGiftCode() }");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                GiftCode gc = new GiftCode();
                gc.setId(rs.getInt("id"));
                gc.setVip0ag(rs.getInt("vip0ag"));
                gc.setVip1ag(rs.getInt("vip1ag"));
                gc.setVip2ag(rs.getInt("vip2ag"));
                gc.setVip3ag(rs.getInt("vip3ag"));
                gc.setVip4ag(rs.getInt("vip4ag"));
                gc.setVip5ag(rs.getInt("vip5ag"));
                gc.setVip6ag(rs.getInt("vip6ag"));
                gc.setVip7ag(rs.getInt("vip7ag"));
                gc.setVip8ag(rs.getInt("vip8ag"));
                gc.setVip9ag(rs.getInt("vip9ag"));
                gc.setVip10ag(rs.getInt("vip10ag"));
                gc.setSdate(rs.getTimestamp("sdate"));
                gc.setEdate(rs.getTimestamp("edate"));
                gc.setSkey(rs.getString("skey"));
                lsGiftCode.add(gc);
                dic.put(gc.getSkey(), gc);
            }
            rs.close();
            cs.close();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameIGiftCode(int source, int uid, int giftid, String uname, String skey, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIGiftCode(?,?,?,?,?,?) }");
            cs.setInt("Userid", uid);
            cs.setInt("Giftid", giftid);
            cs.setString("Username", uname);
            cs.setString("skey", skey);
            cs.setInt("AG", ag);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return -2;
    }

    /*public void GameGetListCardLucky(int source,UserInfo user){
    	try {
    		CallableStatement cs = conn.prepareCall("{call GameGetListCardLucky(?) }");
            cs.setString("Username",user.getUsername());
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
            	CardLucky um = new CardLucky();
                um.setId(rs.getLong("Id"));
                um.setType(rs.getInt("Type"));
                um.setArr(ActionUtils.gson.fromJson(rs.getString("Arr"), int[].class));
                um.setStatus(rs.getBoolean("Status"));
                um.setAgwin(rs.getInt("AGWin"));
                um.setLqBuy(rs.getInt("LQ"));
                um.setCreateTime(rs.getTimestamp("CreateTime"));
                user.getArrCardLucky().add(um);
            }
            rs.close();
            cs.close();
        } catch (Exception ex) {
        }
    }*/
    public void GameGetListRotationLucky(int source, UserInfo user) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListRotationLucky(?) }");
//    		if (user.getSource() == 5)
//    			cs.setInt("Userid",user.getUserid() - 1300000000);
//    		else if (user.getSource() == 3)
//    			cs.setInt("Userid",user.getUserid() - 1100000000);
            cs.setInt("Userid", user.getUserid());
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                RotationLucky um = new RotationLucky();
                um.setId(rs.getLong("Id"));
                um.setType(rs.getInt("Type"));
                um.setAgwin(rs.getInt("AGWin"));
                um.setCreateTime(rs.getTimestamp("CreateTime"));
                user.getArrRotationLucky().add(um);
            }
            rs.close();
            cs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameGetIdMaxHighlow(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetIdMaxHighlow(?) }");
            cs.registerOutParameter("IdMax", Types.INTEGER);
            cs.execute();
            return cs.getInt("IdMax");
        } catch (Exception ex) {
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameGetEventCardLucky(int source, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceCardLuckyEvent(?,?) }");
            cs.setString("Username", username);
            cs.registerOutParameter("Mark", Types.INTEGER);
            cs.execute();
            return cs.getInt("Mark");
        } catch (Exception ex) {
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public short GameGetRotationLuckyMark(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceGetRotationLuckyMark(?,?) }");
            cs.setInt("UserId", userid);
            cs.registerOutParameter("Mark", Types.INTEGER);
            cs.execute();
            return (short) cs.getInt("Mark");
        } catch (Exception ex) {
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameITransferAGToVip(int source, int userid, String uname, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameITransferAGToVip(?,?,?,?) }");
            cs.setInt("UserID", userid);
            cs.setString("Username", uname);
            cs.setInt("AG", ag);
            cs.registerOutParameter("Mark", Types.INTEGER);
            cs.execute();
            return cs.getInt("Mark");
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public long GameISlot(int source, int userid, String uname, Slot sl, int isScatter) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameISlot(?,?,?,?,?,?,?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("Username", uname);
            cs.setString("StrR", sl.getStrR());
            cs.setString("StrResult", sl.getStrResult());
            cs.setString("StrW", sl.getStrW());
            cs.setInt("TotalR", sl.getTotalRow());
            cs.setInt("Unit", sl.getUnit());
            cs.setInt("Bonus", sl.getBonus());
            cs.setInt("Scatter", sl.getScatter());
            cs.setInt("TotalW", sl.getTotalWin());
            cs.setInt("IsScatter", isScatter);
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    /**
     * **********************************
     */
    public long GameICardLucky(int source, int userid, String uname, CardLucky cl, int lq) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameICardLucky(?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("Username", uname);
            cs.setInt("Type", cl.getType());
            cs.setString("Arr", ActionUtils.gson.toJson(cl.getArr()));
            cs.setInt("LQ", lq);
            //cs.setBoolean("isBuy",cl.isBuy());
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    public long GameIRotationLucky(int source, int userid, String uname, RotationLucky cl) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIRotationLucky(?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Type", cl.getType());
            cs.setInt("NumRan", cl.getNumran());
            cs.setInt("LQbuy", cl.getLqbuy());
            cs.registerOutParameter("Id", Types.BIGINT);
            cs.execute();
            return cs.getLong("Id");
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0l;
    }

    public int GameUCardLucky(int source, int userid, long id, int tourid, int type, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUCardLucky(?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setLong("Id", id);
            cs.setString("Username", username);
            cs.setInt("TourID", tourid);
            cs.setInt("Type", type);
            cs.registerOutParameter("AGWin", Types.INTEGER);
            cs.execute();
            return cs.getInt("AGWin");
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public void GameDPassLock(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameDPassLock(?) }");
            cs.setInt("UserId", uid);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameDDPassLock(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameDDPassLock(?) }");
            cs.setInt("UserId", uid);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameHPassLock(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameHPassLock(?) }");
            cs.setInt("UserId", uid);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameUIdolName(int source, int uid, String json) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUIdolName(?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("IdolName", json);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameAGTra(int source, int uid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameAGTra(?) }");
            cs.setInt("UserId", uid);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameAGVay(int source, int uid, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameAGVay(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("AGVay", ag);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void UpdateUserInfo(int source, String ipaddress) {
        System.out.println("source: " + source);
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            //Remove tat ca user tren server ipaddress khoi cache
            CallableStatement promote = conn.prepareCall("{call GameGetUserIDByIPAddress(?) }");
            promote.setString("Ipserver", ipaddress);
            ResultSet rs = promote.executeQuery();
            while (rs.next()) {
                int uid = rs.getInt("Id");
                String key = genCacheUserInfoKey(source, uid);
                try {
                    UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(key);
                    if (uinfo != null) {
                        uinfo.setIsOnline((short) 0);
                        uinfo.setIdolName("");
                        uinfo.setTableId(0);
                        UserController.getCacheInstance().set(key, uinfo, 0);
                    }
                } catch (Exception e) {
                    //handle exception
                    e.printStackTrace();
                    this.RemoveUserInfoByUserid(source, uid);
                }
            }
            rs.close();
            promote.close();
            Statement st = conn.createStatement();
            st.executeUpdate("Update UserInfo set isOnline = 0, IdolName='' Where isOnline > 1 and IdolName='" + ipaddress + "'");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /*public int UpdateTableid(int source,int uid, int tableid){
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateTableId(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("TableId", tableid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return -1;
        }finally{
            instance.releaseDbConnection(conn);
        }
    }*/
 /*public int UserConnected(int source,UserInfo user, String ipaddress){
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUserConnected(?,?,?,?,?) }");
            if (user.getSource() == 1)
            	cs.setInt("UserId", user.getUserid());
            else if (user.getSource() == 2)
            	cs.setInt("UserId", user.getUserid() - 1000000000);
            else if (user.getSource() == 3)
            	cs.setInt("UserId", user.getUserid() - 1100000000);
            else if (user.getSource() == 4)
            	cs.setInt("UserId", user.getUserid() - 1200000000);
            else if (user.getSource() == 5)
            	cs.setInt("UserId", user.getUserid() - 1300000000);
            else if (user.getSource() == 9)
            	cs.setInt("UserId", user.getUserid() - 500000000);
            cs.setInt("GameId", user.getGameid());
            cs.setInt("OnlineIn", user.getOperatorid()) ;
            cs.setString("IPAddress", ipaddress) ;
            cs.registerOutParameter("Error", Types.INTEGER);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
            	UserAuction um = new UserAuction(rs.getLong("AuctionID"));
                um.setCount(rs.getInt("C"));
                user.getStepAution().put(um.getAuctionId(),um);
            }
            rs.close();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return -1;
        }finally{
            instance.releaseDbConnection(conn);
        }
    }*/
 /*public void GameLogMsgNew(int source,int t, String username, String ip, String strContent){
    	try{
            CallableStatement cs = conn.prepareCall("{call GameLogMsgNew(?,?,?,?) }");
            cs.setInt("T", t);
            cs.setString("Username", username);
            cs.setString("StrIP", ip);
            cs.setString("StrContent", strContent);
            cs.execute();
        } catch (Exception ex) {
        }
    }
    public void GameLogMsgNewTemp(int source,int t, String username, String ip, String strContent, int playerid, int userid){
    	try{
            CallableStatement cs = conn.prepareCall("{call GameLogMsgNewTemp(?,?,?,?,?,?) }");
            cs.setInt("T", t);
            cs.setString("Username", username);
            cs.setString("StrIP", ip);
            cs.setString("StrContent", strContent);
            cs.setInt("Playerid", playerid);
            cs.setInt("Userid", userid);
            cs.execute();
        } catch (Exception ex) {
        }
    }*/
    public void GameReadMsgNew(int source, int id) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameReadMsgNew(?) }");
            cs.setInt("id", id);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameDelMsgNew(int source, int id) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameDelMsgNew(?) }");
            cs.setInt("id", id);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameIGoldDreamcity(int source, int uid, int gold) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIGoldDreamcity(?,?,?) }");
            cs.setInt("Userid", uid);
            cs.setInt("Gold", gold);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
        return 0;
    }

    public int GameConvertDiamond2AG(int source, int uid, int dm) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameConvertDiamond2AG(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("Diamond", dm);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameConvertAG2Diamond(int source, int uid, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameConvertAG2Diamond(?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("AG", ag);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int ServiceChangeLQ2AG(int source, int uid, int lq, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceChangeLQ2AG(?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("LQ", lq);
            cs.setInt("AG", ag);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int ServiceChangeLQ(int source, int uid, int lq, String username, String content, String name) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceChangeLQ(?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("LQ", lq);
            cs.setString("Username", username);
            cs.setString("ContentChat", content);
            cs.setString("NameChat", name);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameIUserMobile(int source, int uid, String username, String mobile, String email) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIUserMobile(?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Username", username);
            cs.setString("Mobile", mobile);
            cs.setString("Email", email);
            cs.registerOutParameter("Id", Types.INTEGER);
            cs.execute();
            return cs.getInt("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameIMsgNew(int source, String from, String to, String msg) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIMsgNew(?,?,?,?) }");
            cs.setString("From", from);
            cs.setString("To", to);
            cs.setString("MSG", msg);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<UserMsg> GameGetListUserMsgNew(int source, String uname, String unamelq, int type) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        List<UserMsg> lsret = new ArrayList<UserMsg>();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetListUserMsg_Using(?,?,?) }");
            cs.setString("Username", uname);
            cs.setString("UsernameLQ", unamelq);
            cs.setInt("Type", type);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                UserMsg um = new UserMsg();
                um.setId(rs.getInt("ID"));
                um.setFrom(rs.getString("FromUser").toLowerCase());
                um.setTo(rs.getString("ToUser").toLowerCase());
                um.setAG(rs.getInt("AG"));
                um.setI((short) rs.getInt("ItemID"));
                um.setVip((short) rs.getInt("Vip"));
                um.setMsg(rs.getString("Msg") == null ? "" : rs.getString("Msg"));
                um.setDT(rs.getInt("DiamondType"));
                um.setT((short) rs.getInt("Type"));
                um.setTime(rs.getDate("SentTime").getTime());
                um.setS(rs.getBoolean("Status"));
                um.setD(false);
                lsret.add(um);
            }
            rs.close();
            cs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return lsret;
    }

    /**
     * *********************Profile User************************
     */
    public void GameIUserProfile(int source, int uid, String tencmt, String socmt, String sodthoai, String nghenghiep, String diachi, String email, String bday, int g, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIUserProfile(?,?,?,?,?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Username", username);
            cs.setString("Tencmt", tencmt);
            cs.setString("Socmt", socmt);
            cs.setString("Sodthoai", sodthoai);
            cs.setString("Nghenghiep", nghenghiep);
            cs.setString("Diachi", diachi);
            cs.setString("Email", email);
            @SuppressWarnings("deprecation")
            java.sql.Date dt = new java.sql.Date(java.util.Date.parse(bday));
            cs.setDate("Ngaysinh", dt);
            cs.setInt("Gender", g);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameUUserProfile(int source, int uid, String tencmt, String socmt, String sodthoai, String nghenghiep, String diachi, String email) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUUserProfile(?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setString("Tencmt", tencmt);
            cs.setString("Socmt", socmt);
            cs.setString("Sodthoai", sodthoai);
            cs.setString("Nghenghiep", nghenghiep);
            cs.setString("Diachi", diachi);
            cs.setString("Email", email);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    //Xoso

    public int ServiceTheMayManAddXoSo(int source, int uid, float tyle, int soluong, int menhgia, String so) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceTheMayManAddXoSo(?,?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setFloat("Tyle", tyle);
            cs.setInt("Soluong", soluong);
            cs.setInt("Menhgia", menhgia);
            cs.setString("So", so);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int ServiceTheMayMan_UpdateXoSoTour(int source, int uid, int id, String so) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceTheMayMan_UpdateXoSoTour(?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setFloat("Id", id);
            cs.setString("So", so);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /*public String UpdateAGElo(int source,int uid,long mark, int chessElo){
        try{
            CallableStatement cs = conn.prepareCall("{call GameUMarkEloUser(?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setLong("Mark", mark);
            cs.setInt("Elo", chessElo);
            cs.registerOutParameter("Temp", Types.BIGINT);
            cs.registerOutParameter("EloOut", Types.INTEGER);
            cs.execute();
            return String.valueOf(cs.getLong("Temp")) + "/" + String.valueOf(cs.getInt("EloOut"));
        }catch (Exception ex){
            return "";
        }
    }*/
    public void IncrementMark(int source, int uid, int value, int promo, int count, java.sql.Date dt) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call IncrementMarkPhom(?,?,?,?,?) }");
            cs.setInt("UserId", uid);
            cs.setInt("MarkCong", value);
            cs.setInt("PayPromotion", promo);
            cs.setInt("CountPromotion", count);
            cs.setDate("LastPromotion", dt);
            cs.execute();
        } catch (Exception ex) {
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /**
     * ************************************ Friends ****************************
     */
    public void GameSiamFacebookInvite(int source, long fromid, long toid, String fromuser, String touser, int typeIn) {
        try {
            System.out.println("==>Que Invite Face:" + fromid + "-" + toid + "-" + fromuser + "-" + touser + "-" + typeIn);
            UserInfoCmd cmd = new UserInfoCmd("gameIFriendInvite", source, fromid, toid, fromuser, touser, typeIn);
            QueueManager.getInstance(queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GameSiamFacebookInviteDB(int source, long fromid, long toid, String fromuser, String touser, int typeIn) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
//        	System.out.println("==>Que Invite FaceDB:" + fromid + "-" + toid + "-" + fromuser + "-" + touser + "-" + typeIn) ;
            CallableStatement cs = conn.prepareCall("{call FacebookInvite(?,?,?,?,?,?) }");
            cs.setLong("Fromid", fromid);
            cs.setLong("Toid", toid);
            cs.setString("Fromusername", fromuser);
            cs.setString("Tousername", touser);
            cs.setInt("TypeInvite", typeIn);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Userid ==> Userid thang duoc moi
    //FaceId ==> Userid thang moi
    public int CreateFriendFacebookRequest(int source, long facebookid, int userid, String facebookName) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIFriendFaceRequest(?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setLong("FaceId", facebookid);
            cs.setString("FacebookName", facebookName);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 2;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public long GameGetGoldByDeviceId(int source, String deviceid, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetGoldByDeviceId(?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("strDeviceid", deviceid);
            cs.registerOutParameter("Gold", Types.INTEGER);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.execute();
            return cs.getInt("Gold") * 10 + cs.getInt("Vip");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameIUserinfoFacebook(int source, Integer userid, String username, String password, String deviceid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIUserinfoFacebook(?,?,?,?,?) }");
            cs.setInt("UserId", userid);
            cs.setString("Username", username);
            cs.setString("strPassword", password);
            cs.setString("strDeviceid", deviceid);
            cs.registerOutParameter("Id", Types.INTEGER);
            cs.execute();
            return cs.getInt("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameIUserinfo(int source, Integer userid, String username, String usernameLQ, int operator) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameIUserinfo(?,?,?,?,?) }");
            cs.setInt("UserId", userid);
            cs.setString("Username", username);
            cs.setString("UsernameLQ", usernameLQ);
            cs.setInt("Operatorid", operator);
            cs.registerOutParameter("Id", Types.INTEGER);
            cs.execute();
            return cs.getInt("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameUUsernameLQ(int source, Integer userid, String usernameLQOld, String usernameLQNew, int operator) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUUsernameLQ(?,?,?,?,?) }");
            cs.setInt("UserId", userid);
            cs.setString("Username", usernameLQOld);
            cs.setString("UsernameLQ", usernameLQNew);
            cs.setInt("Operatorid", operator);
            cs.registerOutParameter("Id", Types.INTEGER);
            cs.execute();
            return cs.getInt("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    /*public int GameUXitoAvatar(int source, Integer userid, int xa){
    	try {
    		CallableStatement cs = conn.prepareCall("{call GameUXitoAvatar(?,?,?) }");
            cs.setInt("UserId", userid);
            cs.setInt("Xa", xa);            
            cs.registerOutParameter("Id", Types.INTEGER);
            cs.execute();            
    		return cs.getInt("Id") ;
    	} catch (Exception ex){
            return -1 ;
        }
    }*/
    public int GameCheckUserLQ(int source, String usernameLQ) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameCheckUserLQ(?,?) }");
            cs.setString("UsernameLQ", usernameLQ);
            cs.registerOutParameter("Id", Types.INTEGER);
            cs.execute();
            return cs.getInt("Id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int ServiceRegisterCheckLogin(int source, String username, String password, String deviceid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceRegisterCheckLogin(?,?,?,?) }");
            cs.setString("Username", username);
            cs.setString("Password", password);
            cs.setString("DeviceId", deviceid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public long ServiceFaceCheckLogin(int source, String password, String deviceid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceFaceCheckLogin(?,?,?) }");
            cs.setString("Accesstoken", password);
            cs.setString("DeviceId", deviceid);
            cs.registerOutParameter("Error", Types.BIGINT);
            cs.execute();
            return cs.getLong("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1l;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void ServiceIUserFace(int source, String accesstoken, String deviceid, long facebookid, String username, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceIUserFace(?,?,?,?,?) }");
            cs.setString("Accesstoken", accesstoken);
            cs.setString("DeviceId", deviceid);
            cs.setLong("Facebookid", facebookid);
            cs.setString("Username", username);
            cs.setInt("Userid", userid);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int ServiceBotCheckLogin(int source, String username, String password) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceBotLogin(?,?,?) }");
            cs.setString("Username", username);
            cs.setString("Password", password);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int ServiceCheckLogin_Web(int source, String username, String password) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceCheckLogin_Web(?,?,?) }");
            cs.setString("Username", username);
            cs.setString("Password", password);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    //Cash Out

    public CashOut GameCashOut(int source, int userid, int cashvalue, String cashTelco, int diamondType) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CashOut objReturn = new CashOut();
            objReturn.setError(1);
            CallableStatement cs = conn.prepareCall("{call GameCashOut_New(?,?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("CashValue", cashvalue);
            cs.setString("CashTelco", cashTelco);
            cs.setInt("DiamondType", diamondType);
            cs.registerOutParameter("CardID", Types.NVARCHAR);
            cs.registerOutParameter("Serial", Types.NVARCHAR);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            objReturn.setC(cs.getString("CardID"));
            objReturn.setS(cs.getString("Serial"));
            objReturn.setError(cs.getInt("Error"));
            cs.close();
            return objReturn;
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public CashOut GameCashOutNew(int source, int userid, int cashvalue, String cashTelco, int diamondType) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CashOut objReturn = new CashOut();
            objReturn.setError(1);
            CallableStatement cs = conn.prepareCall("{call GameCashOutNew_New(?,?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("CashValue", cashvalue);
            cs.setString("CashTelco", cashTelco);
            cs.setInt("DiamondType", diamondType);
            cs.registerOutParameter("CardID", Types.NVARCHAR);
            cs.registerOutParameter("Serial", Types.NVARCHAR);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            objReturn.setC(cs.getString("CardID"));
            objReturn.setS(cs.getString("Serial"));
            objReturn.setError(cs.getInt("Error"));
            cs.close();
            return objReturn;
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameCashOutInsertCardDB(int source, int userid, int cashvalue, String cashTelco, String cardID, String serial) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameCashOutInsertCard(?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("CashValue", cashvalue);
            cs.setString("CashTelco", cashTelco);
            cs.setString("CardID", cardID);
            cs.setString("Serial", serial);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

//  GameCashOutInsertCard_New]
//	 -- Add the parameters for the stored procedure here
//	 @Userid int,
//	 @CashValue int, -- 4-Accept, 5-Rejected
//	 @CashTelco nvarchar(50),
//	 @CardId nvarchar(50), --Ma the tra ve
//	 @Serial nvarchar(50), --Serial tra ve
//	 @TypeCard int , --Nguon the 0:1pay, 3: widepay 
//	 @Error int output
//    GameCashOutInsertCard_New]
//    		 -- Add the parameters for the stored procedure here
//    		 @Userid int,
//    		 @CashValue int, -- 4-Accept, 5-Rejected
//    		 @CashTelco nvarchar(50),
//    		 @CardId nvarchar(50), --Ma the tra ve
//    		 @Serial nvarchar(50), --Serial tra ve
//    		 @TypeCard int , --Nguon the 0:1pay, 3: widepay 
//    		 @Error int output
    public int GameCashOutInsertCardDB_New(int source, int userid, int cashvalue, String cashTelco, String cardID, String serial, int typeCard) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameCashOutInsertCard_New(?,?,?,?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("CashValue", cashvalue);
            cs.setString("CashTelco", cashTelco);
            cs.setString("CardId", cardID);
            cs.setString("Serial", serial);
            cs.setInt("TypeCard", typeCard);

            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception e) {
            //handle exception
            //System.out.println("GameCashOutInsertCardDB_New: "+source+" - "+);
            e.printStackTrace();
            return -1;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Update App Id
    public void UpdateAppId(int source, int userid, String appid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateAppId(?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("AppId", appid);
            cs.execute();
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Update Chat
    public void UpdateChat(int source, int userid, int chat) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateChat(?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Chat", chat);
            cs.execute();
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Update Free Card Lucky
    public void UpdateFreeLuckyCard(int source, int userid, int down) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateFreeLuckyCard(?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Down", down);
            cs.execute();
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    //Get Free Card Lucky

    public void GameGetFreeLuckyCard(int source, UserInfo ulogin) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetFreeLuckyCard(?,?,?) }");
            if (ulogin.getSource() == 1) {
                cs.setInt("Userid", ulogin.getUserid());
            } else if (ulogin.getSource() == 3) {
                cs.setInt("Userid", ulogin.getUserid() - 1100000000);
            }
            cs.registerOutParameter("FreeCard", Types.INTEGER);
            cs.registerOutParameter("FreeRotation", Types.INTEGER);
            cs.execute();
            ulogin.setFreeLuckyCard((short) cs.getInt("FreeCard"));
            ulogin.setFreeLuckyRotation((short) cs.getInt("FreeRotation"));
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    //Update Free Rotation Lucky

    public void UpdateFreeRotationCard(int source, int userid, int down) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateFreeRotation(?,?) }");
            cs.setInt("Userid", userid);
            cs.setInt("Down", down);
            cs.execute();
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //
    //Chuyen User, Lay AG tu Lang quat
    public Long GetAGByUsername(int source, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetAGByUserName(?,?) }");
            cs.setString("Username", username);
            cs.registerOutParameter("AG", Types.BIGINT);
            cs.execute();
            return cs.getLong("AG");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0l;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    //Chuyen User, Update lai AG, Ref sang 52fun

    public void UpdateAGByUsername(int source, int userid, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateAGByUserName(?,?) }");
            cs.setInt("Userid", userid);
            cs.setLong("AG", ag);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    //Update Gold InApp Purchase

    public int UpdateAGIAP(int source, String username, String productid, String orderId, String sigdata, String signature, String pakname) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateAGByIAP(?,?,?,?,?,?) }");
            cs.setString("Username", username);
            cs.setString("ProductId", productid);
            cs.setString("OrderId", orderId);
            cs.setString("Sigdata", sigdata);
            cs.setString("Signature", signature);
            //cs.setString("Packname", pakname);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Update thong tin Security cho Langquat
    public int RegisterLQSecurity(int source, String pass, String mobile, int userid, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameRegisterSecurityLQ(?,?,?,?,?) }");
            cs.setString("Username", username);
            cs.setInt("Userid", userid);
            cs.setString("StrPass", pass);
            cs.setString("StrMobile", mobile);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int UpdateLQSecurity(int source, String pass, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateSecurityLQ(?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("StrPass", pass);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int UpdateMobileSecurity(int source, String pass, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateMobileSecurity(?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("StrMobile", pass);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    //Update thong tin Security cho Langquat

    public int GameGetAwardByCode(int source, String code, int userid, String username) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetAwardByCode(?,?,?,?) }");
            cs.setInt("Userid", userid);
            cs.setString("StrCode", code);
            cs.setString("Username", username);
            cs.registerOutParameter("AG", Types.INTEGER);
            cs.execute();
            return cs.getInt("AG");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Lay thong tin mat khau
    /*public void GetUserPassLQ(int source,UserInfo user){
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn  = instance.getDbConnection();
    	try {
    		CallableStatement cs = conn.prepareCall("{call GameGetPassLQ(?,?,?) }");
            cs.setInt("UserId", user.getUserid());
            cs.registerOutParameter("M", Types.NVARCHAR);
            cs.registerOutParameter("P", Types.NVARCHAR);
            cs.execute();
            user.setPassLQ(cs.getString("P")) ;
            user.setMobile(cs.getString("M")) ;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally{
            instance.releaseDbConnection(conn);
        }
    }*/
    //Nap tien qua IOS
    public int AddMoneyIOS(int source, int userid, PaymentIAP_IOS obj, int sanbox) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call AddMoneyIOS(?,?,?,?,?,?,?,?,?,?,?,?,?) }");
            cs.setString("product_id", obj.getProduct_id());
            cs.setString("transaction_id", obj.getTransaction_id());
            cs.setString("original_transaction_id", obj.getOriginal_transaction_id());
            cs.setString("unique_vendor_identifier", obj.getUnique_vendor_identifier());
            cs.setString("unique_identifier", obj.getUnique_identifier());
            cs.setString("bvrs", obj.getBvrs());
            cs.setString("item_id", obj.getItem_id());
            cs.setString("bid", obj.getBid());
            cs.setInt("Sanbox", sanbox);
            long tempo1 = Long.parseLong(obj.getOriginal_purchase_date_ms());
            cs.setDate("original_purchase_date", new java.sql.Date(tempo1));
            tempo1 = Long.parseLong(obj.getPurchase_date_ms());
            cs.setDate("purchase_date", new java.sql.Date(tempo1));
            cs.setInt("UserId", userid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Nap tien qua IOS
    public int AddMoneyIOS(int source, int userid, IAP_IOS_ITEM_IN_APP obj, int sanbox) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call AddMoneyIOS(?,?,?,?,?,?,?,?,?,?,?,?,?) }");
            cs.setString("product_id", obj.getProduct_id());
            cs.setString("transaction_id", obj.getTransaction_id());
            cs.setString("original_transaction_id", obj.getOriginal_transaction_id());
            cs.setString("unique_vendor_identifier", obj.getPurchase_date());
            cs.setString("unique_identifier", obj.getPurchase_date_ms());
            cs.setString("bvrs", obj.getPurchase_date_pst());
            cs.setString("item_id", obj.getIs_trial_period());
            cs.setString("bid", obj.getProduct_id());
            cs.setInt("Sanbox", sanbox);
            long tempo1 = Long.parseLong(obj.getOriginal_purchase_date_ms());
            cs.setDate("original_purchase_date", new java.sql.Date(tempo1));
            tempo1 = Long.parseLong(obj.getPurchase_date_ms());
            cs.setDate("purchase_date", new java.sql.Date(tempo1));
            cs.setInt("UserId", userid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Nap tien qua IOS
    public int AddMoneyCam(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call AddMoneyCam(?,?,?) }");
            cs.setInt("UserId", userid);
            cs.setInt("Gold", userid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Dang ky username
    public int GameRegisterUsername(int source, String username, String pass, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameRegisterUsername(?,?,?) }");
            cs.setString("username", username);
            cs.setString("password", pass);
            cs.setInt("userid", userid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
    //Dang ky username

    public int GameRegisterUsernameBySocket(int source, String username, String pass, String deviceId, int operator, String ipAddress) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServiceRegister(?,?,?,?,?,?) }");
            cs.setString("Username", username);
            cs.setString("Password", pass);
            cs.setString("DeviceId", deviceId);
            cs.setInt("OperatorId", operator);
            cs.setString("IpAddress", ipAddress);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    //Dang ky username
    public int GameConvertLQToDT(int source, String username, String code, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameConvertLQToDT(?,?,?,?) }");
            cs.setString("Username", username);
            cs.setString("Code", code);
            cs.setInt("Userid", userid);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public int GameConvertLQToDT_LockLQ(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameConvertLQToDT_Lock(?,?) }");
            cs.setInt("Userid", userid);
            cs.registerOutParameter("AG", Types.INTEGER);
            cs.execute();
            return cs.getInt("AG");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void GameConvertLQToDT_UpDateAG(int source, String username, String code, int ag) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameConvertLQToDT_UpdateAG(?,?,?) }");
            cs.setString("Username", username);
            cs.setString("Code", code);
            cs.setInt("AG", ag);
            cs.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
}
