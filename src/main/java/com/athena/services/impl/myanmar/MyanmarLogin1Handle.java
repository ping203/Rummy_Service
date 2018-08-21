package com.athena.services.impl.myanmar;

//new type login

import com.athena.database.ServerDefined;
import com.athena.database.UCLogBean;
import com.athena.database.UserController;
import com.athena.services.api.ServiceContract;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.utils.ActionUtils;
//import com.athena.services.utils.CoupleKey;
import com.athena.services.utils.LoginLoggingModel;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceContext;
import com.dst.LanguageDefined;
import com.dst.ServerSource;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author UserXP
 */
public class MyanmarLogin1Handle implements LoginHandler {
    private static Logger loggerLogin_ = Logger.getLogger("LoginandDisconnect");
    private UserController uController = new UserController();
    private Gson gson = new Gson();
    private ServiceContext context;
    //private AuthService authServiceImpl;

    public MyanmarLogin1Handle(ServiceContext context) { //, AuthService authServiceImpl) {
        //this.authServiceImpl = authServiceImpl;
        this.context = context;
    }

    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
        long startAction = System.currentTimeMillis();
        try {
            loggerLogin_.info("==>StartLoginMyanma:" + action.getUser() + "---" + action.getOperatorid());
            UserInfo ulogin = gson.fromJson(action.getUser(), UserInfo.class);

            ulogin.setOperatorid((short) action.getOperatorid());
            ulogin.setFacebookid(0l);
            ulogin.setGoogleid(0l);
            ulogin.setSource((short) ServerSource.MYA_SOURCE);
            ulogin.setUsertype((short) 1);

            //add language
            if (StringUtils.isEmpty(ulogin.getLanguage())) {
                ulogin.setLanguage(LanguageDefined.EN);
            }

            if (ulogin.getReg() == 1) { //Dang ky nick
                String strUsername = ulogin.getUsername();
                String strPassword = action.getPassword();
                loggerLogin_.info("==>StartLoginMyanma==>StartRegister:" + action.getUser() + "-" + strUsername + "-" + strPassword);
                int error = ActionUtils.CheckValidUsernameLQ(strUsername);
                if (error == 1) {
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-1);
//                    lra.setErrorMessage(MessageUtil.getMessageServiceResourceBundle(,ulogin.getSource()));
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Err1", ulogin.getLanguage()));
                    ulogin = null;
                    loggerLogin_.info("==>StartLoginMyanma==>RegisterFail:" + action.getUser() + "-" + strUsername + "-" + strPassword + "-" + error);
                    return lra;
                } else if (error == 2) {
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-2);
//                    lra.setErrorMessage(MessageUtil.getMessageServiceResourceBundle("strRegister_Err2",ulogin.getSource()));
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Err2", ulogin.getLanguage()));
                    ulogin = null;
                    loggerLogin_.info("==>StartLoginMyanma==>RegisterFail:" + action.getUser() + "-" + strUsername + "-" + strPassword + "-" + error);
                    return lra;
                } else if (strPassword.equals("123456") || (strPassword.length() < 6) || strPassword.equals("123456789")
                        || strPassword.equals("qwerty") || strPassword.equals("12345678")) {
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-3);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Err3", ulogin.getLanguage()));
                    ulogin = null;
                    //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                    loggerLogin_.info("==>StartLoginMyanma==>RegisterFail:" + action.getUser() + "-" + strUsername + "-" + strPassword + "-" + error);
                    return lra;
                } else if (strUsername.indexOf("admin") > 0) {
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-4);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Err4", ulogin.getLanguage()));
                    ulogin = null;
                    //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                    loggerLogin_.info("==>StartLoginMyanma==>RegisterFail:" + action.getUser() + "-" + strUsername + "-" + strPassword + "-" + error);
                    return lra;
                } else if (error > 0) {
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-5);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Err5", ulogin.getLanguage()));
                    ulogin = null;
                    //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                    loggerLogin_.info("==>StartLoginMyanma==>RegisterFail:" + action.getUser() + "-" + strUsername + "-" + strPassword + "-" + error);
                    return lra;
                } else {
                    int idUser = uController.GameRegisterUsernameBySocket(ServerSource.MYA_SOURCE, strUsername, strPassword, ulogin.getDeviceId(), 500, action.getRemoteAddress().getAddress().getHostAddress());
                    loggerLogin_.info("==>StartLoginMyanma==>Register:" + action.getUser() + "-" + idUser);
                    if (idUser == -2) {
                        LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-6);
                        lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Err6", ulogin.getLanguage()));
                        ulogin = null;
                        //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                        loggerLogin_.info("==>StartLoginMyanma==>RegisterFailInsert:" + action.getUser() + "-" + strUsername + "-" + strPassword + "-" + idUser);
                        return lra;
                    } else if (idUser == -1) {
                        LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-7);
                        lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Err7", ulogin.getLanguage()));
                        ulogin = null;
                        loggerLogin_.info("==>StartLoginMyanma==>RegisterFailInsert:" + action.getUser() + "-" + strUsername + "-" + strPassword + "-" + idUser);
                        return lra;
                    } else if (idUser > 0) {
                        ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
                        ulogin.setUserid(idUser);
                        uController.GetUserInfoByUserid(ServerSource.MYA_SOURCE, ulogin, ServiceImpl.ipAddressServer);
                        ulogin.setTinyurl(ulogin.getUsername());
                        ulogin.setIdolName(ServiceImpl.ipAddressServer);
                        ulogin.setUserid(ServerDefined.userMap.get(ServerSource.MYA_SOURCE) + ulogin.getUserid());
                        loggerLogin_.info("processReturnLoginOK 0 " + ulogin.getUserid() + " " + ulogin.getUsername());
                        return processReturnLoginOK(ulogin, action, 0);
                    }
                }
            }
            loggerLogin_.info("==>StartLoginMyanma==>Check pass:" + action.getUser() + "-" + action.getPassword());
            // Login binh thuong ==> Lay Userid ==> Lay Thong tin User
            ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
            if (action.getPassword().length() == 0 || ulogin.getFrom().length() == 0 || ulogin.getUsername().length() < 3) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-2);
                lra.setErrorMessage("");
                loggerLogin_.info("==>StartLoginMyanma==>LoginFailpass:" + action.getUser() + "-" + action.getPassword());
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                return lra;
            }
            loggerLogin_.info("==>StartLoginMyanmaFace=>GetUserIDStart:" + action.getUser());
            int userid = uController.GameGetUseridVietnam(ServerSource.MYA_SOURCE, ulogin.getUsername(), action.getPassword(), ulogin.getDeviceId());
            if (userid <= 0) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-3);
                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err5", ulogin.getLanguage()));
                ulogin = null;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginMyanma==>LoginFailuserid:" + action.getUser() + "-" + userid);
                return lra;
            } else
                ulogin.setUserid(userid); //UserId của DT
            loggerLogin_.info("==>StartLoginMyanma==>GetUserinfoStart:" + action.getUser() + "-" + ulogin.getUserid().intValue());
            ulogin.setIdolName(ServiceImpl.ipAddressServer);
            uController.GetUserInfoByUserid(ServerSource.MYA_SOURCE, ulogin, ServiceImpl.ipAddressServer);
            if (ulogin.getAG().intValue() == -2) { //Tien am ==> Khong ton tai user
                uController.RemoveUserInfoByUserid(ServerSource.MYA_SOURCE, userid);
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-13);
                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err6", ulogin.getLanguage()));
                ulogin = null;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginMyanma==>LoginFail(AGam):" + action.getUser() + "-" + userid);
                return lra;
            }
            ulogin.setTinyurl(ulogin.getUsername());
            ulogin.setUserid(ServerDefined.userMap.get(ServerSource.MYA_SOURCE) + ulogin.getUserid());
            ulogin.setUsername(ulogin.getUsername().toLowerCase());
            loggerLogin_.info("==>StartLoginMyanma==>GetUserinfoFinish:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid());
            if (!ulogin.isBanned()) {
                if (ulogin.getIsOnline() == 0) {
                    if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                        uController.RestoreHighLow(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                        ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                        ulogin.setAGHigh(0);
                        ulogin.setAGLow(0);
                    }
                    loggerLogin_.info("processReturnLoginOK 1 " + ulogin.getUserid() + " " + ulogin.getUsername());
                    return processReturnLoginOK(ulogin, action, 1);
                } else if (ulogin.getIdolName().equals(ServiceImpl.ipAddressServer)) {
                    int pid = 0;

                    pid = ServiceImpl.CheckUserOnline(ulogin.getUserid(), ulogin.getGameid());
                    if (pid > 0) {
                        if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                            uController.RestoreHighLow(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                            ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                            ulogin.setAGHigh(0);
                            ulogin.setAGLow(0);
                        }
                        loggerLogin_.info("processReturnLoginOK 2 " + ulogin.getUserid() + " " + ulogin.getUsername());
                        return processReturnLoginOK(ulogin, action, 2);
                    } else if (pid == 0) {
                        if (ActionUtils.checkReconnectMyanmar(ulogin.getGameid())) {
                            //update type reconnect
                            short gameIdOnline = ulogin.getGameid();
                            if (ulogin.getIsOnline() > 0) {
                                gameIdOnline = ulogin.getIsOnline();
                            }
                            int tableid = ServiceImpl.GetTableIdUserOnline(ulogin.getUserid(), gameIdOnline);
                            if (tableid > 0) {
                                if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                                    uController.RestoreHighLow(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                                    ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                                    ulogin.setAGHigh(0);
                                    ulogin.setAGLow(0);
                                }
                                loggerLogin_.info("processReturnLoginOK 3 " + ulogin.getUserid() + " " + ulogin.getUsername());
                                return processReturnLoginOK(ulogin, action, 3);
                            } else if (tableid < -1) {
                                LoginResponseAction lra = new LoginResponseAction(false, 0);
                                lra.setErrorCode(-5);
                                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err1", ulogin.getLanguage()));
                                ulogin = null;
                                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                                loggerLogin_.info("==>StartLoginMyanma==>FinishLoginFailGameid:" + action.getUser());
                                return lra;
                            } else {
                                LoginResponseAction lra = new LoginResponseAction(false, 0);
                                lra.setErrorCode(-1);
                                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err1", ulogin.getLanguage()));
                                ulogin = null;
                                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                                loggerLogin_.info("==>StartLoginMyanma==>FinishLoginFailnotdicUser:" + action.getUser());
                                return lra;
                            }
                        } else {
                            LoginResponseAction lra = new LoginResponseAction(false, 0);
                            lra.setErrorCode(-1);
                            lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err1", ulogin.getLanguage()));
                            //Code moi
                            ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
                            impl.clientDisconnected(ulogin.getPid());
                            ulogin = null;
                            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                            loggerLogin_.info("==>StartLoginMyanma==>FinishLoginFailGameid1:" + action.getUser());
                            return lra;
                        }
                    } else { //Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
                        if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                            uController.RestoreHighLow(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                            ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                            ulogin.setAGHigh(0);
                            ulogin.setAGLow(0);
                        }
                        loggerLogin_.info("processReturnLoginOK 4 " + ulogin.getUserid() + " " + ulogin.getUsername());
                        return processReturnLoginOK(ulogin, action, 4);
                		/*uController.UserDisconnected(ServerDefined.MYANMAR_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerDefined.MYANMAR_SOURCE));
                		LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-1); //Ban dang o trong ban choi
                        lra.setErrorMessage(MessageUtil.getMessageServiceResourceBundle("strConnect_Err3",ulogin.getSource()));
                        ulogin = null ;
                        //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                        loggerLogin_.info("==>StartLoginMyanma==>FinishLoginFailnotdicUser1:" + action.getUser()) ;
                        return lra;*/
                    }
                } else {//Modified: 01/07/2016
                    if (uController.GetIsOnlineFromDB(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) == 0) {
                        uController.RemoveUserInfoByUserid(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    }
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-1);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err2", ulogin.getLanguage()));
                    ulogin = null;
                    //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                    loggerLogin_.info("==>StartLoginMyanma==>FinishLoginFailIPServer:" + action.getUser());
                    return lra;
                }
            } else {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-4);
                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err4", ulogin.getLanguage()));
                ulogin = null;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginMyanma==>FinishLoginFailBanned:" + action.getUser());
                return lra;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            loggerLogin_.error("==>StartLoginMyanma==>FinishLoginFailAll:" + action.getUser() + "-" + ex.getMessage());
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            ret.setErrorMessage("Login failed! Please try again later!");
            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
            return ret;
        }finally {
            long endAction = System.currentTimeMillis();

            Logger.getLogger("time_execute_logging").info(
                    new UCLogBean("MyanmarLogin1Handle", (endAction - startAction), "")
            );
        }
    }

    private LoginResponseAction processReturnLoginOK(UserInfo ulogin, LoginRequestAction action, int type) {
        long startProcessReturnLoginOk = System.nanoTime();
        long ccu = 0;
        try {
//            String authCode = authServiceImpl.requestAuth(new BasicUserAuth(ulogin.getUsername(), AuthType.NM, ulogin.getUserid() - ServerDefined.userMap.get(ServerDefined.MYANMAR_SOURCE)));
            String authCode = "";
            loggerLogin_.info("==>authCode: " + authCode);

            synchronized (ServiceImpl.dicUser) {
                // type = 0 ; is Register
                // type = 1 ulogin.getIsOnline() == 0
                // type = 2 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
                // type = 3 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
                // type = 4 //Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
                if(!ServiceImpl.dicUser.containsKey(ulogin.getUserid()) && type != 0){
                    type = 1;
                }
                LoginResponseAction lra = new LoginResponseAction(true, "", ulogin.getUserid());
                ulogin.setPid(lra.getPlayerid());
                lra.setScreenname(ulogin.getUsername());
                lra.setErrorCode(0);
                if (type == 0) {     // is Register
                    ServiceImpl.AddUserOnline(ulogin);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Success", ulogin.getLanguage()));
                    loggerLogin_.info("==>StartLoginMyanma==>FinishLogin Register:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid());
                } else if (type == 1) { // ulogin.getIsOnline() == 0
                    ServiceImpl.AddUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer, ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                    PromotionHandler.GetListPromotionDB2(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    loggerLogin_.info("==>StartLoginMyanma==>FinishLogin 1:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid());
                } else if (type == 2) { // ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer, ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                    loggerLogin_.info("==>StartLoginMyanma==>FinishLogin 2:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid());
                } else if (type == 3) { //ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0

                    ServiceImpl.UpdateUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer, ulogin.getIsOnline(), ulogin.getOperatorid(), ulogin.getsIP());

                    loggerLogin_.info("==>StartLoginMyanma==>FinishLogin 3:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid());

                } else if (type == 4) { // Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
                    ServiceImpl.AddUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer, ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());

                    loggerLogin_.info("==>StartLoginMyanma==>FinishLogin==>NotDic:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid());
                }
                ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
                String data = impl.getLoginGameData(ulogin.getGameid(), ulogin, ulogin.getUserid().intValue(), type);
//                data = impl.updateAuth(data, authCode);
                loggerLogin_.info("==>Data:\n" + data);
                lra.setData(data.getBytes("UTF-8"));

                ccu = ServiceImpl.dicUser.size();

                return lra;
            }
        } catch (Exception e) {
            loggerLogin_.error(e.getMessage(), e);
            e.printStackTrace();
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            ret.setErrorMessage(ServiceImpl.actionUtils.getConfigText("login_strFail", ulogin.getSource(), ulogin.getUserid()));
            return ret;
        } finally {
            long endLoggingTime = System.nanoTime();

            LoginLoggingModel loginLoggingModel = new LoginLoggingModel(
                    "LOGIN",
                    ulogin.getUserid(),
                    ulogin.getUsername(),
                    ulogin.getSource(),
                    startProcessReturnLoginOk,
                    endLoggingTime,
                    ccu
            );

            Logger.getLogger("Monitor_loggingandDisconnect").info(loginLoggingModel);
        }
    }
}

