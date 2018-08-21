/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.impl.indo;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.api.ServiceContract;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.Operator;
import com.athena.services.vo.BusinessChildFace;
import com.athena.services.vo.BusinessFace;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceContext;
import com.dst.ServerSource;
import com.google.gson.Gson;
import com.vng.tfa.common.FaceService;

import org.apache.log4j.Logger;

/**
 *
 * @author UserXP
 */
public class FacebookIndoLoginHandler2 implements LoginHandler {
    //public static Logger log = Logger.getLogger("Siamplaylog");
	private static Logger loggerLogin_ = Logger.getLogger("LoginandDisconnect");
    private UserController uController = new UserController();
    private PromotionHandler promotionController = new PromotionHandler();
    private FaceService faceService = new FaceService();
    private Gson gson = new Gson();
    private ServiceContext context;
//    public static StatsMonitor reqStats = new StatsMonitor();
//    public Environment env = Environment.PRODUCTION;
    public FacebookIndoLoginHandler2(ServiceContext context) {
        this.context = context;
    }
    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
//        long startTime = System.nanoTime();
    	UserInfo ulogin = new UserInfo() ;
        try{
        	loggerLogin_.info("==>Start Login Face Indo:" + action.getUser() + "---" + action.getOperatorid());
        	ulogin = gson.fromJson(action.getUser(), UserInfo.class);
        	ulogin.setOperatorid((short)action.getOperatorid()) ;
            ulogin.setFacebookid(0l) ;
            ulogin.setGoogleid(0l) ;
            ulogin.setSource((short)10) ;
            ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
            /*****/
            loggerLogin_.info("==>Start Check AccessToken:" + action.getPassword());
        	String accessToken = "" ;
        	accessToken = action.getPassword() ;
        	
//        	boolean isUserFaceInsert = false ;
        	if(accessToken.length() == 0) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-14); //Lỗi đăng nhập facebook.
                lra.setErrorMessage("");
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                loggerLogin_.info("==>Login Face Finish Fail(Accesstoken):" + action.getUser()) ;
                return lra;
            }
        	loggerLogin_.info("==>Start Call Face:" + action.getPassword()) ;
        	int userid = 0 ;
        	try {
        		String accessTokenTemp = "" ;
        		if (accessToken.length() > 245)
        			accessTokenTemp = accessToken.substring(0, 244) ;
        		else
        			accessTokenTemp = accessToken ;
        		//Get Userid From Cache
        		Long faceid = uController.GetUserFaceByAccesstoken(accessTokenTemp) ;
        		if (faceid == null) {
        			String line = faceService.GetContentLoginFromFaceGraph_New(accessToken);
        			if (line.length() > 0) {
        				BusinessFace bussinessFace = ActionUtils.gson.fromJson(line, BusinessFace.class) ;
         			   	//Duyet cac category
     			   		if (bussinessFace.getId() != null) {
     			   			long facebookId = Long.parseLong(bussinessFace.getId());
                            ulogin.setUsername("fb." +facebookId) ;
                            ulogin.setFacebookid(facebookId);
                            ulogin.setFacebookName(bussinessFace.getName()) ;
                            userid = uController.GameGetUserid_FaceSiam(ServerSource.IND_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId(), bussinessFace.getToken_for_business()) ;
                            uController.AddUserFaceToCache(accessTokenTemp, facebookId);
                            try{
                            	faceService.processInvite(ServerSource.IND_SOURCE,ulogin.getUserid(), accessToken,facebookId);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
     			   	} else {
     			   		LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-6);
                        lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_ErrFace",ulogin.getSource()) + "-1");
                        ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                        ulogin = null ;
//                        reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                        loggerLogin_.info("==>Login Face Finish Fail(Check Face):" + action.getUser()) ;
                        return lra;
     			   	}
        		} else {
        			ulogin.setUsername("fb." + faceid) ;
    			   	ulogin.setFacebookid(faceid);
    			   	ulogin.setFacebookName("fb." + faceid) ;
    			   	userid = uController.GameGetUserid_Face(ServerSource.IND_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId()) ;
        		}
		  	} catch (Exception e) {
		  		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-12);
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_ErrFace",ulogin.getSource()) + "-3");
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                loggerLogin_.info("==>Login Face Finish Fail(Check Face):" + action.getUser() + "-" +  e.getMessage()) ;
                return lra;
		  	}
        	//faceService.processInvite(ServerDefined.IND_SOURCE, ulogin.getUserid(), accessToken,ulogin.getFacebookid());
            /*****/
        	if (userid == 0) {
        		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-3);
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_ErrFace",ulogin.getSource()));
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                return lra;
        	} else 
        		ulogin.setUserid(userid);
        	//Lay thong tin User
        	ulogin.setTinyurl(ulogin.getUsername()) ;
        	loggerLogin_.info("Get Userinfo Start:" + action.getUser() + "-" + ulogin.getUserid().intValue()) ;
        	uController.GetUserInfoByUserid(ServerSource.IND_SOURCE,ulogin, ServiceImpl.ipAddressServer);
        	ulogin.setUserid(ulogin.getUserid()) ;
        	if (!ulogin.isReceiveDailyPromotion()) { //Get List Friends
        		//Lay Total Friends da cai
            	try {
            		String line = faceService.GetContentFriendFromFaceGraph(accessToken);
            		if (line.length() > 0) {
     			   		com.athena.services.vo.FriendInstalledFace friends = ActionUtils.gson.fromJson(line, com.athena.services.vo.FriendInstalledFace.class) ;
     			   		ulogin.setCFriendsF(friends.getData().size());
     			    }
    			} catch (Exception e) {
    				e.printStackTrace();
    				//loggerLogin_.error("===>Error==>FacebookThaiNewLoginHandler==>GetFriends:" + e.getMessage()) ;
    			}
        	}
        	loggerLogin_.info("Get Userinfo Finish:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
    		if(!ulogin.isBanned()) {
            	if(ulogin.getIsOnline() == 0) {
            		if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
            			uController.RestoreHighLow(ServerSource.IND_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE)) ;
        				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
        				ulogin.setAGHigh(0);
        				ulogin.setAGLow(0);
        			}
            		return processReturnLoginOK(ulogin, action, 1);
                } else if (ulogin.getIdolName().equals(ServiceImpl.ipAddressServer)) {
                	int pid = 0;
                	pid = ServiceImpl.CheckUserOnline(ulogin.getUserid(),ulogin.getGameid());
                	if(pid>0) { //Dang ngoai sanh
                		if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                			uController.RestoreHighLow(ServerSource.IND_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE)) ;
            				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
            				ulogin.setAGHigh(0);
            				ulogin.setAGLow(0);
            			}
                		return processReturnLoginOK(ulogin, action, 2);
                	} else if(pid==0) { //Dang o trong ban choi
                		if (ActionUtils.checkReconnectIndo(ulogin.getGameid()))
                		{
//          				 || (ulogin.getGameid() == 8012) || (ulogin.getGameid() == 8010)
//          				 || (ulogin.getGameid() == 8004) || (ulogin.getGameid() == 8007)) { //Ap dung Reconnect voi game poker
                			//int tableid = ulogin.getTableId();
                			int tableid = ServiceImpl.GetTableIdUserOnline(ulogin.getUserid(), ulogin.getGameid()) ;
                       		if (tableid > 0) {
                       			if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                        			uController.RestoreHighLow(ServerSource.IND_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE)) ;
                    				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                    				ulogin.setAGHigh(0);
                    				ulogin.setAGLow(0);
                    			}
                       			return processReturnLoginOK(ulogin, action, 3);
                       		} else if (tableid < -1) {
   	                    		LoginResponseAction lra = new LoginResponseAction(false, 0);
   	                            lra.setErrorCode(-5);
   	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
   	                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
   	                            ulogin = null ;
//                                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
   	                            loggerLogin_.info("FinishLogin Fail Gameid:" + action.getUser()) ;
	                         	return lra;
                       		} else {
                       			LoginResponseAction lra = new LoginResponseAction(false, 0);
   	                            lra.setErrorCode(-1);
   	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
   	                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
   	                            ulogin = null ;
//                                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
   	                            loggerLogin_.info("FinishLogin Fail not dicUser:" + action.getUser()) ;
	                         	return lra;
                       		}
                   		} else {
                    		LoginResponseAction lra = new LoginResponseAction(false, 0);
                            lra.setErrorCode(-1);
                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                            ulogin = null ;
//                            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
//                            reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                            loggerLogin_.info("FinishLogin Fail Gameid1:" + action.getUser()) ;
	                        return lra;
                   		}
                	} else {
                		uController.UserDisconnected(ServerSource.IND_SOURCE, ulogin.getUserid());
                		LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-1); //Ban dang o trong ban choi
                        lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err3",ulogin.getSource()));
                        ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                        ulogin = null ;
//                        reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                        loggerLogin_.info("FinishLogin Fail not dicUser1:" + action.getUser()) ;
                        return lra;
                	}
                } else {
                	if (uController.GetIsOnlineFromDB(10,ulogin.getUserid()) == 0) {
                		uController.RemoveUserInfoByUserid(ServerSource.IND_SOURCE, ulogin.getUserid());
                	}
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-1);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err3",ulogin.getSource()));
                    ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                    ulogin = null ;
//                    reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                    loggerLogin_.info("FinishLogin Fail IPServer:" + action.getUser()) ;
                    return lra;
                }
            } else {
            	LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-4);
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err4",ulogin.getSource()));
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                loggerLogin_.info("FinishLogin Fail Banned:" + action.getUser()) ;
                return lra ;
            }
        } catch (Exception ex){
        	ex.printStackTrace();
            loggerLogin_.error("FinishLogin Fail Banned:" + action.getUser() + "-" + ex.getMessage()) ;
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            ret.setErrorMessage("Anda telah keluar akun. Tunggulah 30 menit.");
            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
//          reqStats.addMicro((System.nanoTime() - startTime) / 1000);
            loggerLogin_.info("FinishLogin Fail Other-5:" + action.getUser()) ;
            return ret;
        }
    }
    private LoginResponseAction processReturnLoginOK(UserInfo ulogin, LoginRequestAction action, int type) {
		try{
			// type = 0 ; is Register
			// type = 1 ulogin.getIsOnline() == 0
			// type = 2 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
			// type = 3 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
			// type = 4 //Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
			
			LoginResponseAction lra = new LoginResponseAction(true,"", ulogin.getUserid());
            ulogin.setPid(lra.getPlayerid());
	        lra.setScreenname(ulogin.getUsername());
	        lra.setErrorCode(0);
			if(type == 0){	 // is Register			
	            ServiceImpl.AddUserOnline(ulogin);           
	            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strRegister_Success",ulogin.getSource()));
	            loggerLogin_.info("==>FacebookIndoLoginHandler2==>FinishLogin Register:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;	    
			}else if(type == 1){ // ulogin.getIsOnline() == 0
                ServiceImpl.AddUserOnline(ulogin);
                uController.UpdateIsOnlineToCache(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
               //Lay danh sach new Adminpromotion
                if (action.getOperatorid() == Operator.OPERATOR_INDO1)
                	promotionController.GetListPromotionDB(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE)) ;
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                loggerLogin_.info("==>FacebookIndoLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername()) ;							
	        }else if (type == 2){ // ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
                uController.UpdateIsOnlineToCache(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                loggerLogin_.info("==>FacebookIndoLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername()) ;	        	
	        }else if (type == 3){ //ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
		        ServiceImpl.UpdateUserOnline(ulogin);
       			uController.UpdateIsOnlineToCache(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
       			ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
       			loggerLogin_.info("==>FacebookIndoLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername()) ;
	        }else if (type == 4){ // Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
	            ServiceImpl.AddUserOnline(ulogin);
	            uController.UpdateIsOnlineToCache(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
	
	            loggerLogin_.info("==>FacebookIndoLoginHandler2==>FinishLogin==>NotDic:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;	  
			}  
			
			ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
			String data = impl.getLoginGameData(ulogin.getGameid(), ulogin, ulogin.getUserid().intValue(),type);
			loggerLogin_.info("==>Data:\n"+data);
			lra.setData(data.getBytes("UTF-8"));
			
            return lra;
		}catch(Exception e){
			e.printStackTrace();
			LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            //ret.setErrorMessage("Có lỗi xảy ra, xin vui lòng thử lại!");
            ret.setErrorMessage("บัญชีของท่านกำลังออกแล้ว กรุณารอประมาณ 30 นาทีค่ะี");
           	return ret;
		}
	}
}
