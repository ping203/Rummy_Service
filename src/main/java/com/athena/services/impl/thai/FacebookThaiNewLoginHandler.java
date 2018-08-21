/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.impl.thai;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.api.ServiceContract;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.utils.ActionUtils;
//import com.athena.services.vo.UserFace;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceContext;
import com.dst.ServerSource;
import com.google.gson.Gson;
import com.vng.tfa.common.FaceService;


import org.apache.log4j.Logger;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.log4j.Logger;
//

/**
 *
 * @author UserXP
 */
public class FacebookThaiNewLoginHandler implements LoginHandler {
    //public static Logger log = Logger.getLogger("Siamplaylog");
	private static Logger loggerLogin_ = Logger.getLogger("LoginandDisconnect");
    private UserController uController = new UserController();
    private PromotionHandler promotionController = new PromotionHandler();
    private FaceService faceService = new FaceService();
    private Gson gson = new Gson();
    private ServiceContext context;
//    public static StatsMonitor reqStats = new StatsMonitor();
//    public Environment env = Environment.PRODUCTION;
   
    public FacebookThaiNewLoginHandler(ServiceContext context) {
		// TODO Auto-generated constructor stub
    	this.context = context ;
	}

	@Override
    public LoginResponseAction handle(LoginRequestAction action) {
//        long startTime = System.nanoTime();
    	UserInfo ulogin = new UserInfo() ;
        try{
        	loggerLogin_.info("==>Start Login Face Thai:" + action.getUser() + "---" + action.getOperatorid());
        	ulogin = gson.fromJson(action.getUser(), UserInfo.class);
        	ulogin.setOperatorid((short)action.getOperatorid()) ;
            ulogin.setFacebookid(0l) ;
            ulogin.setGoogleid(0l) ;
            ulogin.setSource((short)ServerSource.THAI_SOURCE) ;
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
        				com.athena.services.vo.BusinessFace bussinessFace = ActionUtils.gson.fromJson(line, com.athena.services.vo.BusinessFace.class) ;
        				//Check theo token_for_business
        				String strTemp = uController.GameGetUserid_Business(ServerSource.THAI_SOURCE, bussinessFace.getToken_for_business(), Long.parseLong(bussinessFace.getId())) ;
         			   	if (strTemp.length() > 3) {
         			   		userid = Integer.parseInt(strTemp.split("-")[0]) ;
         			   		ulogin.setFacebookid(Long.parseLong(strTemp.split("-")[1]));
         			   		ulogin.setFacebookName(bussinessFace.getName()) ;
         			   		ulogin.setUsername("fb." + ulogin.getFacebookid().longValue()) ;
         			   		uController.AddUserFaceToCache(accessTokenTemp, Long.parseLong(strTemp.split("-")[1]));
         			   	} else {
         			   		//Duyet cac category
         			   		if (bussinessFace.getIds_for_business() != null) {
         			   			long facebookId = 0;
	 			   				for (int i = 0; i < bussinessFace.getIds_for_business().getData().size(); i++) {
	 			   					com.athena.services.vo.BusinessChildFace businessChild = bussinessFace.getIds_for_business().getData().get(i).getApp();
	 			   					if (businessChild.getId().equals("1040144962748243") || businessChild.getId().equals("1523918034574069")
	 			   						 || businessChild.getId().equals("274283512920189") || businessChild.getId().equals("886100001536454")
	 			   						|| businessChild.getId().equals("280000189045543") || businessChild.getId().equals("136928166722227")
	 			   						|| businessChild.getId().equals("1864332647156078") || businessChild.getId().equals("212372922596701")) {
	 			   						facebookId =  Long.parseLong(bussinessFace.getIds_for_business().getData().get(i).getId());
	 			   						ulogin.setUsername("fb." + Long.parseLong(bussinessFace.getIds_for_business().getData().get(i).getId())) ;
		 		        			   	ulogin.setFacebookName(bussinessFace.getName()) ;
		 		        			   	ulogin.setFacebookid(Long.parseLong(bussinessFace.getIds_for_business().getData().get(i).getId())) ;
	 			   						userid = uController.GameGetUserId_AppsId(ServerSource.THAI_SOURCE, Long.parseLong(bussinessFace.getIds_for_business().getData().get(i).getId()), bussinessFace.getToken_for_business()) ;
	 			   					}
	 			   					if (userid > 0)
	 			   						break ;
								}
	 			   				loggerLogin_.info("==>UserID:" + userid) ;
	 			   				if (userid == 0) { //Insert 1 user Face moi
	 			   					if(facebookId == 0)
                                    {
                                        facebookId = Long.parseLong(bussinessFace.getIds_for_business().getData().get(0).getId());
                                    }
		 			   				ulogin.setUsername("fb." + facebookId) ;
		 	        			   	ulogin.setFacebookid(facebookId);
		 	        			   	ulogin.setFacebookName(bussinessFace.getName()) ;
		 	        			    userid = uController.GameGetUserid_FaceSiam(ServerSource.THAI_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId(), bussinessFace.getToken_for_business()) ;
		 	        			    uController.AddUserFaceToCache(accessTokenTemp, facebookId);
		 	        			    try {
		 	        			    	faceService.processInvite(ServerSource.THAI_SOURCE, ulogin.getUserid(), accessToken, facebookId);
									} catch (Exception e) {
										// TODO: handle exception
										e.printStackTrace();
									}
		 	        			    
	 			   				}
	 			   			}
         			   	}
     			   	} else {
     			   		LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-6);
                        lra.setErrorMessage("เกิดข้อผิดพลาดขณะล็อกอินเฟสบุ๊ค");
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
    			   	userid = uController.GameGetUserid_Face(ServerSource.THAI_SOURCE, ulogin.getFacebookid().longValue(), ulogin.getFacebookName(), ulogin.getDeviceId()) ;
        		}
		  	} catch (Exception e) {
		  		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-12);
                lra.setErrorMessage("เกิดข้อผิดพลาดขณะล็อกอินเฟสบุ๊ค");
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                loggerLogin_.info("==>Login Face Finish Fail(Check Face):" + action.getUser() + "-" +  e.getMessage()) ;
                return lra;
		  	}
        	
            /*****/
        	if (userid == 0) {
        		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-3);
                lra.setErrorMessage("รหัสผ่านและชื่อบัญชีไม่ตรงกัน");
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                return lra;
        	} else 
        		ulogin.setUserid(userid);
        	//Lay thong tin User
        	ulogin.setTinyurl(ulogin.getUsername()) ;
        	uController.GetUserInfoByUserid(ServerSource.THAI_SOURCE,ulogin, ServiceImpl.ipAddressServer);
        	loggerLogin_.info("Get Userinfo Start:" + action.getUser() + "-" + ulogin.getUserid().intValue() + "-" + ulogin.getPromotionDaily() + "-" + ulogin.getOnlineDaily() + "-" + ulogin.getPassLQ()) ;
        	ulogin.setUserid(ServerDefined.userMap.get(ServerSource.THAI_SOURCE) + ulogin.getUserid()) ;
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
    			}
        	}
        	loggerLogin_.info("Get Userinfo Finish:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid() + "-" + ulogin.isReceiveDailyPromotion() + "-" + ulogin.getPassLQ() +"-" + ulogin.getAGHigh() + "-" + ulogin.getAGLow()) ;
    		if(!ulogin.isBanned()) {
            	if(ulogin.getIsOnline() == 0) {        		
            		if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
            			uController.RestoreHighLow(ServerSource.THAI_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE)) ;
        				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
        				ulogin.setAGHigh(0);
        				ulogin.setAGLow(0);
        			}
           			LoginResponseAction lra = new LoginResponseAction(true,"", ulogin.getUserid());
                    ulogin.setPid(lra.getPlayerid());
                    ServiceImpl.AddUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.THAI_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                    //Lay danh sach new Adminpromotion
                    if (action.getOperatorid() == 1001)
                    	promotionController.GetListPromotionDB(ServerSource.THAI_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE)) ;
                    lra.setScreenname(ulogin.getUsername());
                    lra.setErrorCode(0);
//                    reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                    ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                    loggerLogin_.info("==>Login Face Finish:" + action.getUser()) ;
                    return lra;
                } else if (ulogin.getIdolName().equals(ServiceImpl.ipAddressServer)) {
                	int pid = 0;
                	pid = ServiceImpl.CheckUserOnline(ulogin.getUserid(),ulogin.getGameid());
                	if(pid>0) { //Dang ngoai sanh              		              		
                		if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                			uController.RestoreHighLow(ServerSource.THAI_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE)) ;
            				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
            				ulogin.setAGHigh(0);
            				ulogin.setAGLow(0);
            			}
               			LoginResponseAction lra = new LoginResponseAction(true,"", pid);
                        ulogin.setPid(lra.getPlayerid());
                        lra.setScreenname(ulogin.getUsername());
                        uController.UpdateIsOnlineToCache(ServerSource.THAI_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                        lra.setErrorCode(0);
//                        reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                        ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                        loggerLogin_.info("==>Login Face Finish:" + action.getUser()) ;
                        return lra;
                	} else if(pid==0) { //Dang o trong ban choi
                	    
                        loggerLogin_.info("checkReconnect: " +  ulogin.getGameid() + ", " + ActionUtils.checkReconnect(ulogin.getGameid()));
                        if (ActionUtils.checkReconnect(ulogin.getGameid()))    
                		{
                			//int tableid = ulogin.getTableId();
                			int tableid = ServiceImpl.GetTableIdUserOnline(ulogin.getUserid(), ulogin.getGameid()) ;
                       		if (tableid > 0) {
                       			if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                        			uController.RestoreHighLow(ServerSource.THAI_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE)) ;
                    				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                    				ulogin.setAGHigh(0);
                    				ulogin.setAGLow(0);
                    			}
                       			ServiceImpl.UpdateUserOnline(ulogin);
                       			uController.UpdateIsOnlineToCache(ServerSource.THAI_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
            					//Joind vao ban
                       			LoginResponseAction lra = new LoginResponseAction(true,"", ulogin.getUserid());
                                ulogin.setPid(lra.getPlayerid());
                                lra.setScreenname(ulogin.getUsername());
                                lra.setErrorCode(0);
//                                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                                loggerLogin_.info("==>Login Face Finish:" + action.getUser()) ;
                                return lra;
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
                            //Code moi
                            ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
                            impl.clientDisconnected(ulogin.getPid());
                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                            ulogin = null ;
//                            reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                            loggerLogin_.info("FinishLogin Fail Gameid1:" + action.getUser()) ;
	                        return lra;
                   		}
                	} else {
                		uController.UserDisconnected(ServerSource.THAI_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE));
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
                	if (uController.GetIsOnlineFromDB(ServerSource.THAI_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE)) == 0) {
                		uController.RemoveUserInfoByUserid(ServerSource.THAI_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE));
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
            loggerLogin_.error("FinishLogin Fail Banned:" + action.getUser() + "-" + ex.getMessage(), ex) ;
            LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            ret.setErrorMessage("บัญชีของท่านออกได้แล้ว รอประมาณ 30 นาที");
            loggerLogin_.info("FinishLogin Fail Other-5:" + action.getUser()) ;
            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
//            reqStats.addMicro((System.nanoTime() - startTime) / 1000);
            return ret;
        }
    }
}
