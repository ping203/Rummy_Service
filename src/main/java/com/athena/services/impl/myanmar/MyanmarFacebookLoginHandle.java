/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.impl.myanmar;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.api.ServiceContract;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.Operator;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceContext;
import com.dst.ServerSource;
import com.google.gson.Gson;
import com.vng.tfa.common.FaceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 *
 * @author hoangchau
 */
public class MyanmarFacebookLoginHandle implements LoginHandler {

    private static Logger loggerLogin_ = Logger.getLogger("LoginandDisconnect");
    private UserController uController = new UserController();
    private PromotionHandler promotionController = new PromotionHandler();
    private FaceService faceService = new FaceService();
    private Gson gson = new Gson();
    private ServiceContext context;
    public static List<String> listAppFace = new ArrayList<String>() {
        {
            this.add("983861404968502"); // siamplay
            this.add("1965630216827768");
            this.add("2039726489599071");
            this.add("224429551529084") ;
            this.add("1829831827108358");
            /*this.add("1829831827108358"); // th_dummythai
            this.add("274283512920189");
            this.add("886100001536454");
            this.add("136928166722227");
//            this.add("1864332647156078"); 9K card_kaoke
            this.add("212372922596701");
            this.add("1758560834404191"); // tfa_hilothai
            this.add("124293141535951");
            //this.add("225092818030944"); // Pokdeng hilo_pokdeng
            this.add("334035557006287"); // siam vertical*/
        }
    };

    public MyanmarFacebookLoginHandle(ServiceContext context) {
        this.context = context;
    }

    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
//      long startTime = System.nanoTime();
        UserInfo ulogin = new UserInfo();
        try {
            loggerLogin_.info("==>Start Login Face Thai:" + action.getUser() + "---" + action.getOperatorid());
            ulogin = gson.fromJson(action.getUser(), UserInfo.class);
            ulogin.setOperatorid((short) action.getOperatorid());
            ulogin.setFacebookid(0l);
            ulogin.setGoogleid(0l);
            ulogin.setSource((short) ServerSource.MYA_SOURCE);
            ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
            /**
             * **
             */
            loggerLogin_.info("==>Start Check AccessToken:" + action.getPassword());
            String accessToken = action.getPassword();

//        	boolean isUserFaceInsert = false ;
            if (accessToken.length() == 0) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-14); //Lỗi đăng nhập facebook.
                lra.setErrorMessage("");
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                ulogin = null;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                loggerLogin_.info("==>Login Face Finish Fail(Accesstoken):" + action.getUser());
                return lra;
            }
            loggerLogin_.info("==>Start Call Face:" + action.getPassword());
            int userid = 0;
            try {
                String accessTokenTemp = "";
                if (accessToken.length() > 150) {
                    accessTokenTemp = ulogin.getUserIdZing() + accessToken.substring(0, 148);
                } else {
                    accessTokenTemp = ulogin.getUserIdZing() + accessToken;
                }
//                if (accessToken.length() > 245) {
//                    accessTokenTemp = accessToken.substring(0, 244);
//                } else {
//                    accessTokenTemp = accessToken;
//                }
                //Get Userid From Cache
                Long faceid = uController.GetUserFaceByAccesstoken(accessTokenTemp);
                if (faceid == null) {
                    String line = faceService.GetContentLoginFromFaceGraph_New(accessToken);
                    loggerLogin_.info("==>FacebookThaiLoginHandler2: token: " + accessToken + " - line: " + line);
                    com.athena.services.vo.BusinessFace bussinessFace = ActionUtils.gson.fromJson(line, com.athena.services.vo.BusinessFace.class);
                    if (bussinessFace.getIds_for_business() != null) {
//                        String userFaceId = bussinessFace.getIds_for_business().getData().get(0).getId();
                        String userFaceId = bussinessFace.getId();
                        userid = uController.GetUserFaceFromMapping(ServerSource.MYA_SOURCE, Long.parseLong(userFaceId));
                        if (userid == 0) {
                            Map<String, String> mapUserApp = new HashMap<String, String>();
                            for (int i = 0; i < bussinessFace.getIds_for_business().getData().size(); i++) {
                                String appId = bussinessFace.getIds_for_business().getData().get(i).getApp().getId();
                                String fbuid = bussinessFace.getIds_for_business().getData().get(i).getId();
                                mapUserApp.put(fbuid, appId);
                            }

                            if (listAppFace.contains(mapUserApp.get(userFaceId))) //app gom chung BToken
                            {
                                long facebookId = 0;
                                for (String key : mapUserApp.keySet()) {
                                    facebookId = Long.parseLong(key);
                                    String strAppId = mapUserApp.get(key);
                                    if (!listAppFace.contains(strAppId)) {
                                        continue;
                                    }
                                    ulogin.setUsername("fb." + key);
                                    ulogin.setFacebookid(facebookId);
                                    userid = uController.GameGetUserId_FaceIdBusiness(ServerSource.MYA_SOURCE, Long.parseLong(userFaceId), facebookId, bussinessFace.getToken_for_business());
                                    if (userid > 0) {
                                        break;
                                    }
                                }

                            }
                            if (userid == 0) {
                                ulogin.setUsername("fb." + userFaceId);
                                ulogin.setFacebookid(Long.parseLong(userFaceId));
                                ulogin.setFacebookName(bussinessFace.getName());
                                userid = uController.GameGetUserid_FaceSiam(ServerSource.MYA_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId(), bussinessFace.getToken_for_business());
                                uController.AddUserFaceToCache(accessTokenTemp, Long.parseLong(userFaceId));
                                /*try {
                                    faceService.processInvite(ServerSource.MYA_SOURCE, ulogin.getUserid(), accessToken, Long.parseLong(userFaceId));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/
                            }
                        } else {
                            ulogin.setUsername("fb." + userFaceId);
                            ulogin.setFacebookid(Long.parseLong(userFaceId));
                        }
                    } else {
                	    line = faceService.GetContentLoginFromFaceGraph(accessToken);
                	    loggerLogin_.info("==>FacebookThaiLoginHandler3: token: " + accessToken + " - line: " + line);
                        if (line.length() > 0) {
                            com.athena.services.vo.UserFace userface = ActionUtils.gson.fromJson(line, com.athena.services.vo.UserFace.class) ;
                            if (userface.getId() == 0) {
                                LoginResponseAction lra = new LoginResponseAction(false, 0);
                                lra.setErrorCode(-6);
                                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_ErrFace",ulogin.getLanguage()) + "-1");
                                ulogin = null ;
                                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                                return lra;
                            } else {
                                //Set lai vao Cache
                                uController.AddUserFaceToCache(accessTokenTemp, Long.valueOf(userface.getId()));
                            }
                            ulogin.setUsername("fb." + userface.getId()) ;
                            ulogin.setFacebookName(userface.getName()) ;
                            ulogin.setFacebookid(userface.getId()) ;
                            userid = uController.GameGetUserid_Face(ServerSource.MYA_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId()) ;
                        } else {
                            LoginResponseAction lra = new LoginResponseAction(false, 0);
                            lra.setErrorCode(-6);
                            lra.setErrorMessage("เกิดข้อผิดพลาดขณะล็อกอินเฟสบุ๊ค");
//                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                            ulogin = null;
//                            reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                            loggerLogin_.info("==>Login Face Finish Fail(Check Face):" + action.getUser());
                            return lra;
                        }
                    } 
                } else {
                    ulogin.setUsername("fb." + faceid);
                    ulogin.setFacebookid(faceid);
                    ulogin.setFacebookName("fb." + faceid);
                    userid = uController.GameGetUserid_Face(ServerSource.MYA_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId());
                }
            } catch (Exception e) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-12);
                lra.setErrorMessage("เกิดข้อผิดพลาดขณะล็อกอินเฟสบุ๊ค");
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                ulogin = null;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                loggerLogin_.info("==>Login Face Finish Fail(Check Face):" + action.getUser() + "-" + e.getMessage());
                return lra;
            }

            /**
             * **
             */
            if (userid == 0) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-3);
                lra.setErrorMessage("รหัสผ่านและชื่อบัญชีไม่ตรงกัน");
//                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                ulogin = null;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                return lra;
            } else {
                ulogin.setUserid(userid);
            }
            //Lay thong tin User
            ulogin.setTinyurl(ulogin.getUsername());
            uController.GetUserInfoByUserid(ServerSource.MYA_SOURCE, ulogin, ServiceImpl.ipAddressServer);
            loggerLogin_.info("Get Userinfo Start:" + action.getUser() + "-" + ulogin.getUsername() + "-" + ulogin.getUserid().intValue() + "-" + ulogin.getPromotionDaily() + "-" + ulogin.getOnlineDaily() + "-" + ulogin.getPassLQ());
            if (ulogin.getAG().intValue() == -2) { //Tien am ==> Khong ton tai user
                uController.RemoveUserInfoByUserid(ServerSource.MYA_SOURCE, userid);
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-13);
                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err6",ulogin.getLanguage()));
                ulogin = null ;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginMyanmarFace==>LoginFail(AGam):" + action.getUser() + "-" +  userid) ;
                return lra;
            }
            ulogin.setUserid(ServerDefined.userMap.get(ServerSource.MYA_SOURCE) + ulogin.getUserid());
            if (!ulogin.isReceiveDailyPromotion()) { //Get List Friends
                //Lay Total Friends da cai
                try {
                    String line = faceService.GetContentFriendFromFaceGraph(accessToken);
                    if (line.length() > 0) {
                        com.athena.services.vo.FriendInstalledFace friends = ActionUtils.gson.fromJson(line, com.athena.services.vo.FriendInstalledFace.class);
                        ulogin.setCFriendsF(friends.getData().size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            loggerLogin_.info("Get Userinfo Finish:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid() + "-" + ulogin.isReceiveDailyPromotion() + "-" + ulogin.getPassLQ() + "-" + ulogin.getAGHigh() + "-" + ulogin.getAGLow());
            if (!ulogin.isBanned()) {
                if (ulogin.getIsOnline() == 0) {
                    if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid()))) {
                        uController.RestoreHighLow(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                        ulogin.setAG(ulogin.getAG() + ulogin.getAGHigh() + ulogin.getAGLow());
                        ulogin.setAGHigh(0);
                        ulogin.setAGLow(0);
                    }
                    return processReturnLoginOK(ulogin, action, 1);
                } else if (ulogin.getIdolName().equals(ServiceImpl.ipAddressServer)) {
                    int pid;
                    pid = ServiceImpl.CheckUserOnline(ulogin.getUserid(), ulogin.getGameid());
                    if (pid > 0) { //Dang ngoai sanh              		              		
                        if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid()))) {
                            uController.RestoreHighLow(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                            ulogin.setAG(ulogin.getAG() + ulogin.getAGHigh() + ulogin.getAGLow());
                            ulogin.setAGHigh(0);
                            ulogin.setAGLow(0);
                        }
                        return processReturnLoginOK(ulogin, action, 2);
                    } else if (pid == 0) { //Dang o trong ban choi

                        loggerLogin_.info("checkReconnect: " + ulogin.getGameid() + ", " + ActionUtils.checkReconnectMyanmar(ulogin.getGameid()));
                        if (ActionUtils.checkReconnectMyanmar(ulogin.getGameid())) {
                            //int tableid = ulogin.getTableId();
                            int tableid = ServiceImpl.GetTableIdUserOnline(ulogin.getUserid(), ulogin.getGameid());
                            if (tableid > 0) {
                                if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid()))) {
                                    uController.RestoreHighLow(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                                    ulogin.setAG(ulogin.getAG() + ulogin.getAGHigh() + ulogin.getAGLow());
                                    ulogin.setAGHigh(0);
                                    ulogin.setAGLow(0);
                                }
                                return processReturnLoginOK(ulogin, action, 3);
                            } else if (tableid < -1) {
                                LoginResponseAction lra = new LoginResponseAction(false, 0);
                                lra.setErrorCode(-5);
                                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1", ulogin.getSource()));
                                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                                ulogin = null;
//                                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                                loggerLogin_.info("FinishLogin Fail Gameid:" + action.getUser());
                                return lra;
                            } else {
                                LoginResponseAction lra = new LoginResponseAction(false, 0);
                                lra.setErrorCode(-1);
                                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1", ulogin.getSource()));
                                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                                ulogin = null;
//                                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                                loggerLogin_.info("FinishLogin Fail not dicUser:" + action.getUser());
                                return lra;
                            }
                        } else {
                            LoginResponseAction lra = new LoginResponseAction(false, 0);
                            lra.setErrorCode(-1);
                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1", ulogin.getSource()));
                            //Code moi
                            ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
                            impl.clientDisconnected(ulogin.getPid());
                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                            ulogin = null;
//                            reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                            loggerLogin_.info("FinishLogin Fail Gameid1:" + action.getUser());
                            return lra;
                        }
                    } else {
                        uController.UserDisconnected(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                        LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-1); //Ban dang o trong ban choi
                        lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err3", ulogin.getSource()));
                        ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                        ulogin = null;
//                        reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                        loggerLogin_.info("FinishLogin Fail not dicUser1:" + action.getUser());
                        return lra;
                    }
                } else {
                    if (uController.GetIsOnlineFromDB(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) == 0) {
                        uController.RemoveUserInfoByUserid(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    }
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-1);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err3", ulogin.getSource()));
                    ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                    ulogin = null;
//                    reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                    loggerLogin_.info("FinishLogin Fail IPServer:" + action.getUser());
                    return lra;
                }
            } else {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-4);
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err4", ulogin.getSource()));
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
                ulogin = null;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                loggerLogin_.info("FinishLogin Fail Banned:" + action.getUser());
                return lra;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            loggerLogin_.error("FinishLogin Fail Banned:" + action.getUser() + "-" + ex.getMessage(), ex);
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            ret.setErrorMessage("บัญชีของท่านออกได้แล้ว รอประมาณ 30 นาที");
            loggerLogin_.info("FinishLogin Fail Other-5:" + action.getUser());
            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing());
//            reqStats.addMicro((System.nanoTime() - startTime) / 1000);
            return ret;
        }
    }

    private LoginResponseAction processReturnLoginOK(UserInfo ulogin, LoginRequestAction action, int type) {
        try {
            // type = 0 ; is Register
            // type = 1 ulogin.getIsOnline() == 0
            // type = 2 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
            // type = 3 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
            // type = 4 //Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
        	synchronized (ServiceImpl.dicUser){
        		LoginResponseAction lra = new LoginResponseAction(true, "", ulogin.getUserid());
                ulogin.setPid(lra.getPlayerid());
                lra.setScreenname(ulogin.getUsername());
                lra.setErrorCode(0);
                
                if(!ServiceImpl.dicUser.containsKey(ulogin.getUserid()) && type != 0){
                    type = 1;
                }
                
                if (type == 0) {	 // is Register			
                    ServiceImpl.AddUserOnline(ulogin);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strRegister_Success", ulogin.getSource()));
                    loggerLogin_.info("==>FacebookThaiLoginHandler2==>FinishLogin Register:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid());
                } else if (type == 1) { // ulogin.getIsOnline() == 0
                    ServiceImpl.AddUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer, ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                    //Lay danh sach new Adminpromotion
                    promotionController.GetListPromotionDB(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    loggerLogin_.info("==>FacebookThaiLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername());
                } else if (type == 2) { // ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer, ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                    loggerLogin_.info("==>FacebookThaiLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername());
                } else if (type == 3) { //ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
                    ServiceImpl.UpdateUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer, ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                    loggerLogin_.info("==>FacebookThaiLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername());
                } else if (type == 4) { // Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
                    ServiceImpl.AddUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer, ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                    loggerLogin_.info("==>FacebookThaiLoginHandler2==>FinishLogin==>NotDic:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid());
                }

                ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
                String data = impl.getLoginGameData(ulogin.getGameid(), ulogin, ulogin.getUserid().intValue(), type);
                loggerLogin_.info("==>Data:\n" + data);
                lra.setData(data.getBytes("UTF-8"));

                return lra;
        	}
        } catch (Exception e) {
        	loggerLogin_.info("==>Error Get Date:" + ulogin.getUserid() + "-" +  ulogin.getDeviceId() + "-" + type);
            e.printStackTrace();
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            //ret.setErrorMessage("Có lỗi xảy ra, xin vui lòng thử lại!");
            ret.setErrorMessage("บัญชีของท่านกำลังออกแล้ว กรุณารอประมาณ 30 นาทีค่ะี");
            return ret;
        }
    }

}
