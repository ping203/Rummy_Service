package com.athena.services.impl.myanmar;


import com.athena.database.ServerDefined;
import com.athena.database.UCLogBean;
import com.athena.database.UserController;
import com.athena.services.api.ServiceContract;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.LoginLoggingModel;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceContext;
import com.dst.ServerSource;
import com.google.gson.Gson;
import com.vng.tfa.common.FaceService;
import java.util.Date;
import org.apache.log4j.Logger;

public class MyanmarFacebookLogin1Handle implements LoginHandler {
    private static Logger loggerLogin_ = Logger.getLogger("LoginandDisconnect");
    private UserController uController = new UserController();
    private FaceService faceService = new FaceService();
    private Gson gson = new Gson();
    private ServiceContext context;
    //private AuthService authServiceImpl;
    
    public MyanmarFacebookLogin1Handle(ServiceContext context) { //, AuthService authServiceImpl) {
        //this.authServiceImpl = authServiceImpl;
        this.context = context ;
    }

    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
        long startAction = System.currentTimeMillis();
        try{
            loggerLogin_.info("==>StartLoginMyanmarFace:" + (new Date()).toString() +": "+ action.getUser() + "---" + action.getOperatorid());
            UserInfo ulogin = gson.fromJson(action.getUser(), UserInfo.class);
            ulogin.setOperatorid((short)action.getOperatorid()) ;
            ulogin.setFacebookid(0l) ;
            ulogin.setGoogleid(0l) ;
            ulogin.setSource((short)ServerSource.MYA_SOURCE) ;
            ulogin.setUsertype((short)0);
            ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
            String accessToken = "" ;
            accessToken = action.getPassword() ;
            loggerLogin_.info("==>StartLoginMyanmarFace2==>Start Check AccessToken:" + action.getPassword());
            if(accessToken.length() == 0) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-6);
                lra.setErrorMessage("");
                ulogin = null ;
                loggerLogin_.info("==>StartLoginMyanmarFace2==>Login Face Finish Fail(Accesstoken):" + action.getUser()) ;
                return lra;
            }
            loggerLogin_.info("==>StartLoginMyanmarFace==>StartCallFace:" + action.getPassword()) ;
            try {
                String accessTokenTemp = "" ;
                if (accessToken.length() > 245)
                    accessTokenTemp = accessToken.substring(0, 244) ;
                else
                    accessTokenTemp = accessToken ;
                accessTokenTemp = "dt"+accessTokenTemp ;
                Long faceid = uController.GetUserFaceByAccesstoken(accessTokenTemp) ;
                if (faceid == null) {
                    String line = faceService.GetContentLoginFromFaceGraph(accessToken);
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
                    } else {
                        LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-6);
                        lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_ErrFace",ulogin.getLanguage()) + "-2");
                        ulogin = null ;
                        //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                        return lra;
                    }
                } else {
                    ulogin.setUsername("fb." + faceid) ;
                    ulogin.setFacebookid(faceid);
                    ulogin.setFacebookName("fb." + faceid) ;
                }
            } catch (Exception e) {
                // do nothing
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-12);
                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_ErrFace",ulogin.getLanguage()) + "-3");
                ulogin = null ;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginMyanmarFace==>LoginFailFaceEx:" + action.getUser() + "-" +  e.getMessage()) ;
                return lra;
            }
            /*****/
            if (ulogin.getUsername().equals("1")){
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-6);
                lra.setErrorMessage("Lỗi trong quá trình đăng nhập Facebook.");
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                ulogin = null ;
                loggerLogin_.info("==>StartLoginMyanmarFace==>LoginFailFace:" + action.getUser()) ;
                return lra ;
            }
            loggerLogin_.info("==>StartLoginMyanmarFace=>GetUserIDStart:" + action.getUser()) ;
            int userid = uController.GameGetUserid_Face(ServerSource.MYA_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId()) ;
            if (userid <= 0) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-3);
                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err6",ulogin.getLanguage()));
                ulogin = null ;
                loggerLogin_.info("==>StartLoginMyanmarFace==>LoginFailuserid:" + action.getUser() + "-" + userid) ;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                return lra;
            } else
                ulogin.setUserid(userid);
            ulogin.setTinyurl(ulogin.getUsername()) ;
            loggerLogin_.info("==>StartLoginMyanmarFace==>GetUserinfoStart:" + action.getUser() + "-" + ulogin.getUserid().intValue()) ;
            uController.GetUserInfoByUserid(ServerSource.MYA_SOURCE, ulogin, ServiceImpl.ipAddressServer);
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
            ulogin.setUserid(ServerDefined.userMap.get(ServerSource.MYA_SOURCE) + ulogin.getUserid()) ;

            loggerLogin_.info("==>StartLoginMyanmarFace==>GetUserinfoFinish:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
            if(!ulogin.isBanned()) {
                if(ulogin.getIsOnline() == 0) {
                    if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                        uController.RestoreHighLow(ServerSource.MYA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) ;
                        ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                        ulogin.setAGHigh(0);
                        ulogin.setAGLow(0);
                    }
                    return processReturnLoginOK(ulogin, action, 1);
                } else if (ulogin.getIdolName().equals(ServiceImpl.ipAddressServer)) {
                    //|| (!ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) && ulogin.getLastDeviceID().equals(ulogin.getDeviceId()))) {
                    int pid = 0;
                    pid = ServiceImpl.CheckUserOnline(ulogin.getUserid(),ulogin.getGameid());
                    if(pid>0) {
                        if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                            uController.RestoreHighLow(ServerSource.MYA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) ;
                            ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                            ulogin.setAGHigh(0);
                            ulogin.setAGLow(0);
                        }
                        return processReturnLoginOK(ulogin, action, 2);
                    } else if(pid==0) {
                		/*LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-1);
                        lra.setErrorMessage("Bạn đang ở trong bàn chơi khác.");
                        ulogin = null ;
                        return lra;*/
                        if (ActionUtils.checkReconnectMyanmar(ulogin.getGameid())){
                            //int tableid = ulogin.getTableId();
                            int tableid = ServiceImpl.GetTableIdUserOnline(ulogin.getUserid(), ulogin.getGameid()) ;
                            if (tableid > 0) {
                                if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                                    uController.RestoreHighLow(ServerSource.MYA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) ;
                                    ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                                    ulogin.setAGHigh(0);
                                    ulogin.setAGLow(0);
                                }
                                return processReturnLoginOK(ulogin, action, 3);
                            } else if (tableid < -1) {
                                LoginResponseAction lra = new LoginResponseAction(false, 0);
                                lra.setErrorCode(-5);
                                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err1",ulogin.getLanguage()));
                                ulogin = null ;
                                loggerLogin_.info("==>StartLoginMyanmarFace==>FinishLoginFailGameid:" + action.getUser()) ;
                                return lra;
                            } else {
                                LoginResponseAction lra = new LoginResponseAction(false, 0);
                                lra.setErrorCode(-1);
                                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err1",ulogin.getLanguage()));
                                ulogin = null ;
                                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                                loggerLogin_.info("==>StartLoginMyanmarFace==>FinishLoginFailnotdicUser:" + action.getUser()) ;
                                return lra;
                            }
                        } else {
                            LoginResponseAction lra = new LoginResponseAction(false, 0);
                            lra.setErrorCode(-1);
                            lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err1",ulogin.getLanguage()));
                            //Code moi
                            ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
                            impl.clientDisconnected(ulogin.getPid());
                            ulogin = null ;
                            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                            loggerLogin_.info("==>StartLoginMyanmarFace==>FinishLoginFailGameid1:" + action.getUser()) ;
                            return lra;
                        }
                    } else {//Pid == -1 ==> Khong co trong dic ==> Add vao trong dic va cho login
                        if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                            uController.RestoreHighLow(ServerSource.MYA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) ;
                            ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                            ulogin.setAGHigh(0);
                            ulogin.setAGLow(0);
                        }
                        return processReturnLoginOK(ulogin, action, 4);
                    }
                } else {
                    if (uController.GetIsOnlineFromDB(ServerSource.MYA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) == 0) {
                        uController.RemoveUserInfoByUserid(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
                    }
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-1);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err2",ulogin.getLanguage()));
                    ulogin = null ;
                    //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                    loggerLogin_.info("==>StartLoginMyanmarFace==>FinishLoginFailIPServer:" + action.getUser()) ;
                    return lra;
                }
            } else {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-4);
                lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strConnect_Err4",ulogin.getLanguage()));
                ulogin = null ;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginMyanmarFace==>FinishLoginFailBanned:" + action.getUser()) ;
                return lra ;
            }
        } catch (Exception ex){
            ex.printStackTrace();
            loggerLogin_.error("==>StartLoginMyanmarFace==>FinishLoginFailAll:" + action.getUser() + "-" + ex.getMessage()) ;
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-6);
            ret.setErrorMessage("Login failed! Please try again later!");
            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
            return ret;
        }finally {
            long endAction = System.currentTimeMillis();

            Logger.getLogger("time_execute_logging").info(
                    new UCLogBean("MyanmarFacebookLogin1Handle", (endAction - startAction), "")
            );
        }
    }

    private LoginResponseAction processReturnLoginOK(UserInfo ulogin, LoginRequestAction action, int type) {
        long startProcessReturnLoginOk = System.nanoTime();
        long ccu = 0;
        try{
//            String authCode = authServiceImpl.requestAuth(new BasicUserAuth(ulogin.getUsername(), AuthType.FB, ulogin.getUserid() - ServerDefined.userMap.get(ServerDefined.MYANMAR_SOURCE)));
            String authCode = "";
            loggerLogin_.info("==>authCode: " + authCode);

            synchronized (ServiceImpl.dicUser){
                // type = 0 ; is Register
                // type = 1 ulogin.getIsOnline() == 0
                // type = 2 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
                // type = 3 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
                // type = 4 //Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
                LoginResponseAction lra = new LoginResponseAction(true,"", ulogin.getUserid());
                ulogin.setPid(lra.getPlayerid());
                lra.setScreenname(ulogin.getUsername());
                lra.setErrorCode(0);

                if(!ServiceImpl.dicUser.containsKey(ulogin.getUserid()) && type != 0){
                    type = 1;
                }

                if(type == 0){	 // is Register
                    ServiceImpl.AddUserOnline(ulogin);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getServiceText("strRegister_Success",ulogin.getLanguage()));
                    loggerLogin_.info("==>StartLoginDT==>FinishLogin Register:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
                }else if(type == 1){ // ulogin.getIsOnline() == 0
                    ServiceImpl.AddUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                    PromotionHandler.GetListPromotionDB2(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE)) ;
                    loggerLogin_.info("==>StartLoginDT==>FinishLogin:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
                }else if (type == 2){ // ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());

                    loggerLogin_.info("==>StartLoginDT==>FinishLogin:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;

                }else if (type == 3){ //ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
                    ServiceImpl.UpdateUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());

                    loggerLogin_.info("==>StartLoginDT==>FinishLogin:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
                }else if (type == 4){ // Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
                    ServiceImpl.AddUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.MYA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());

                    loggerLogin_.info("==>StartLoginDT==>FinishLogin==>NotDic:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
                }

                ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
                String data = impl.getLoginGameData(ulogin.getGameid(), ulogin, ulogin.getUserid().intValue(),type);

                data = impl.updateAuth(data, authCode);

                loggerLogin_.info("==>Data:\n"+data);
                lra.setData(data.getBytes("UTF-8"));

                ccu = ServiceImpl.dicUser.size();

                return lra;
            }
        }catch(Exception e){
            loggerLogin_.error(e.getMessage(), e);
            e.printStackTrace();
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            ret.setErrorMessage("Login failed! Please try again later!");
            return ret;
        }finally {
            long endLoggingTime = System.nanoTime();

            LoginLoggingModel loginLoggingModel = new LoginLoggingModel(
                    "LOGIN_FACEBOOK",
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


