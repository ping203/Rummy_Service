package com.athena.services.impl.india;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.api.ServiceContract;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.utils.ActionUtils;
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

public class FacebookIndiaLogin implements LoginHandler {
	private static Logger loggerLogin_ = Logger.getLogger("LoginandDisconnect");
    private UserController uController = new UserController();
    private FaceService faceService = new FaceService();
    private Gson gson = new Gson();
    private ServiceContext context;
    //public static StatsMonitor reqStats = new StatsMonitor();
//    public Environment env = Environment.PRODUCTION;
   
    public FacebookIndiaLogin(ServiceContext context) {
    	this.context = context ;
	}

	@Override
    public LoginResponseAction handle(LoginRequestAction action) {
        try{
        	loggerLogin_.info("==>StartLoginIndiaFace:" + (new Date()).toString() +": "+ action.getUser() + "---" + action.getOperatorid());
        	UserInfo ulogin = gson.fromJson(action.getUser(), UserInfo.class);
            ulogin.setOperatorid((short)action.getOperatorid()) ;
            ulogin.setFacebookid(0l) ;
            ulogin.setGoogleid(0l) ;
            ulogin.setSource((short)ServerSource.INDIA_SOURCE) ;
            ulogin.setUsertype((short)0);
            ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());

            String accessToken = "" ;
        	accessToken = action.getPassword() ;
        	loggerLogin_.info("==>StartLoginIndiaFace2==>Start Check AccessToken:" + action.getPassword());
        	if(accessToken.length() == 0) {
                LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-6);
                lra.setErrorMessage("");
                ulogin = null ;
                loggerLogin_.info("==>StartLoginIndiaFace2==>Login Face Finish Fail(Accesstoken):" + action.getUser()) ;
                return lra;
            }
        	loggerLogin_.info("==>StartLoginIndiaFace==>StartCallFace:" + action.getPassword()) ;
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
		                    lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_ErrFace",ulogin.getSource()) + "-1");
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
	                    lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_ErrFace",ulogin.getSource()) + "-2");
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
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_ErrFace",ulogin.getSource()) + "-3");
                ulogin = null ;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginIndiaFace==>LoginFailFaceEx:" + action.getUser() + "-" +  e.getMessage()) ;
                return lra;
		  	}
            /*****/  
        	if (ulogin.getUsername().equals("1")){
        		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-6);
                lra.setErrorMessage("Lỗi trong quá trình đăng nhập Facebook.");
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                ulogin = null ;
                loggerLogin_.info("==>StartLoginIndiaFace==>LoginFailFace:" + action.getUser()) ;
                return lra ;
        	}
        	loggerLogin_.info("==>StartLoginIndiaFace=>GetUserIDStart:" + action.getUser()) ;
        	int userid = uController.GameGetUserid_Face(ServerSource.INDIA_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId()) ;
        	if (userid <= 0) {
        		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-3);
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err6",ulogin.getSource()));
                ulogin = null ;
                loggerLogin_.info("==>StartLoginIndiaFace==>LoginFailuserid:" + action.getUser() + "-" + userid) ;
            	//ServiceImpl.dicCurrent.remove(action.getUser()) ;
                return lra;
        	} else 
        		ulogin.setUserid(userid);
        	ulogin.setTinyurl(ulogin.getUsername()) ;
        	loggerLogin_.info("==>StartLoginIndiaFace==>GetUserinfoStart:" + action.getUser() + "-" + ulogin.getUserid().intValue()) ;
        	uController.GetUserInfoByUserid(ServerSource.INDIA_SOURCE, ulogin, ServiceImpl.ipAddressServer);
        	if (ulogin.getAG().intValue() == -2) { //Tien am ==> Khong ton tai user
        		uController.RemoveUserInfoByUserid(ServerSource.INDIA_SOURCE, userid);
        		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-13);
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err6",ulogin.getSource()));
                ulogin = null ;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginIndiaFace==>LoginFail(AGam):" + action.getUser() + "-" +  userid) ;
                return lra;
        	}
        	ulogin.setUserid(ServerDefined.userMap.get(ServerSource.INDIA_SOURCE) + ulogin.getUserid()) ;
        	/*if (!ulogin.isReceiveDailyPromotion()) { //Get List Friends
        		//Lay Total Friends da cai
            	try {
            		String line = faceService.GetContentFriendFromFaceGraph(accessToken);
            		if (line.length() > 0) {
     			   		com.athena.services.vo.FriendInstalledFace friends = ActionUtils.gson.fromJson(line, com.athena.services.vo.FriendInstalledFace.class) ;
     			   		ulogin.setCFriendsF(friends.getData().size());
     			    }
    			} catch (Exception e) {
    				//  handle exception
    				loggerLogin_.error("===>Error==>FacebookDT==>GetFriends:" + e.getMessage()) ;
    			}
        	}*/
			//ulogin.setUsername(ulogin.getUsername()+"_s1") ;
        	loggerLogin_.info("==>StartLoginIndiaFace==>GetUserinfoFinish:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
    		if(!ulogin.isBanned()) {
            	if(ulogin.getIsOnline() == 0) {
            		if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
            			uController.RestoreHighLow(ServerSource.INDIA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE)) ;
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
                			uController.RestoreHighLow(ServerSource.INDIA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE)) ;
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
                		if (ActionUtils.checkReconnectIndia(ulogin.getGameid())){
                			//int tableid = ulogin.getTableId();
                			int tableid = ServiceImpl.GetTableIdUserOnline(ulogin.getUserid(), ulogin.getGameid()) ;
                       		if (tableid > 0) {
                       			if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                        			uController.RestoreHighLow(ServerSource.INDIA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE)) ;
                    				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                    				ulogin.setAGHigh(0);
                    				ulogin.setAGLow(0);
                    			}
                       			return processReturnLoginOK(ulogin, action, 3);
                       		} else if (tableid < -1) {
   	                    		LoginResponseAction lra = new LoginResponseAction(false, 0);
   	                            lra.setErrorCode(-5);
   	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
   	                            ulogin = null ;
   	                            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
   	                            loggerLogin_.info("==>StartLoginIndiaFace==>FinishLoginFailGameid:" + action.getUser()) ;
	                         	return lra;
                       		} else {
                       			LoginResponseAction lra = new LoginResponseAction(false, 0);
   	                            lra.setErrorCode(-1);
   	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
   	                            ulogin = null ;
   	                            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
   	                            loggerLogin_.info("==>StartLoginIndiaFace==>FinishLoginFailnotdicUser:" + action.getUser()) ;
	                         	return lra;
                       		}
                   		} else {
	                    		LoginResponseAction lra = new LoginResponseAction(false, 0);
	                            lra.setErrorCode(-1);
	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
	                          //Code moi
	                            ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
	                            impl.clientDisconnected(ulogin.getPid());
	                            ulogin = null ;
	                            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
	                            loggerLogin_.info("==>StartLoginIndiaFace==>FinishLoginFailGameid1:" + action.getUser()) ;
		                        return lra;
                   		}
                	} else {//Pid == -1 ==> Khong co trong dic ==> Add vao trong dic va cho login
                		if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                			uController.RestoreHighLow(ServerSource.INDIA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE)) ;
            				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
            				ulogin.setAGHigh(0);
            				ulogin.setAGLow(0);
            			}
                		return processReturnLoginOK(ulogin, action, 4);
                	}
                } else {
                	if (uController.GetIsOnlineFromDB(ServerSource.INDIA_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE)) == 0) {
	              		uController.RemoveUserInfoByUserid(ServerSource.INDIA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE));
	          		  }
                	LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-1);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err2",ulogin.getSource()));
                    ulogin = null ;
                    //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                    loggerLogin_.info("==>StartLoginIndiaFace==>FinishLoginFailIPServer:" + action.getUser()) ;
                    return lra;
                }
            } else {
            	LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-4);
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err4",ulogin.getSource()));
                ulogin = null ;
                //ServiceImpl.dicCurrent.remove(action.getUser()) ;
                loggerLogin_.info("==>StartLoginIndiaFace==>FinishLoginFailBanned:" + action.getUser()) ;
                return lra ;
            }
        } catch (Exception ex){
        	ex.printStackTrace();
            loggerLogin_.error("==>StartLoginIndiaFace==>FinishLoginFailAll:" + action.getUser() + "-" + ex.getMessage()) ;
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-6);
            ret.setErrorMessage("Login failed! Please try again later!");
            //ServiceImpl.dicCurrent.remove(action.getUser()) ;
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
	            loggerLogin_.info("==>StartLoginDT==>FinishLogin Register:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;	    
			}else if(type == 1){ // ulogin.getIsOnline() == 0
				ServiceImpl.AddUserOnline(ulogin);
	            uController.UpdateIsOnlineToCache(ServerSource.INDIA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());        
	            PromotionHandler.GetListPromotionDB2(ServerSource.INDIA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE)) ;            								
	            loggerLogin_.info("==>StartLoginDT==>FinishLogin:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
			}else if (type == 2){ // ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
	            uController.UpdateIsOnlineToCache(ServerSource.INDIA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
	    
	            loggerLogin_.info("==>StartLoginDT==>FinishLogin:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;

			}else if (type == 3){ //ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
				ServiceImpl.UpdateUserOnline(ulogin);
       			uController.UpdateIsOnlineToCache(ServerSource.INDIA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
	
		        loggerLogin_.info("==>StartLoginDT==>FinishLogin:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;
			}else if (type == 4){ // Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
	            ServiceImpl.AddUserOnline(ulogin);
	            uController.UpdateIsOnlineToCache(ServerSource.INDIA_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.INDIA_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
	
	            loggerLogin_.info("==>StartLoginDT==>FinishLogin==>NotDic:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;	  
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
            ret.setErrorMessage("Login failed! Please try again later!");
           	return ret;
		}
	}
}

