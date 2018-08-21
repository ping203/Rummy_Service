/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.impl.thai;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.utils.ActionUtils;
//import com.athena.services.utils.CoupleKey;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.dst.ServerSource;
import com.google.gson.Gson;



//import java.net.URLDecoder;
import java.util.Date;
//import java.util.HashMap;

//import zme.api.core.Environment;
//import zme.api.exception.ZingMeApiException;
//import zme.api.graph.ZME_GraphAPI;
//import zme.api.oauth.ZME_Authentication;

/**
 *
 * @author UserXP
 */
public class ThaiRegisterHandler implements LoginHandler {
    
    private UserController uController = new UserController();
    private PromotionHandler promotionController = new PromotionHandler();
    private Gson gson = new Gson();
//    public Environment env = Environment.PRODUCTION;
    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
    	try{
    		UserInfo ulogin = gson.fromJson(action.getUser(), UserInfo.class);
            ulogin.setOperatorid((short)action.getOperatorid()) ;
            ulogin.setFacebookid(0l) ;
            ulogin.setGoogleid(0l) ;
            ulogin.setSource((short)ServerSource.THAI_SOURCE) ;
            if (action.getPassword().length() < 9) {
            	LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-9);
                lra.setErrorMessage("ชื่อผู้ใช้ไม่ถูกต้อง");
                //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
                return lra;
            }
            ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
            ulogin.setUsername(action.getPassword());
            int userid = uController.GameGetUserid_Device(ServerSource.THAI_SOURCE, ulogin.getDeviceId(), ulogin.getUsername(), action.getPassword()) ;
            if (userid == 0) {
        		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-3);
                lra.setErrorMessage("รหัสผ่านและชื่อบัญชีไม่ตรงกัน");
                //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
                return lra;
        	} else 
        		ulogin.setUserid(userid);
        	ulogin.setTinyurl(ulogin.getUsername()) ;
        	uController.GetUserInfoByUserid(ServerSource.THAI_SOURCE,ulogin, ServiceImpl.ipAddressServer);
        	ulogin.setUserid(500000000 + ulogin.getUserid()) ;
        	if(!ulogin.isBanned()) {
            	if(ulogin.getIsOnline() == 0) {
            		LoginResponseAction lra = new LoginResponseAction(true,"", ulogin.getUserid());
                    ulogin.setPid(lra.getPlayerid());
                    ServiceImpl.AddUserOnline(ulogin);
                    uController.UpdateIsOnlineToCache(ServerSource.THAI_SOURCE, ulogin.getUserid() - 500000000, ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                  //Lay danh sach new Adminpromotion
                    if (action.getOperatorid() == 1001)
                    	promotionController.GetListPromotionDB(ServerSource.THAI_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE)) ;
                    lra.setScreenname(ulogin.getUsername());
                    lra.setErrorCode(0);
                    //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                    return lra;
                } else if (ulogin.getIdolName().equals(ServiceImpl.ipAddressServer)) {
                	int pid = 0;
                	pid = ServiceImpl.CheckUserOnline(ulogin.getUserid(),ulogin.getGameid());
                	if(pid>0) {
                		LoginResponseAction lra = new LoginResponseAction(true,"", pid);
                        ulogin.setPid(lra.getPlayerid());
                        lra.setScreenname(ulogin.getUsername());
                        lra.setErrorCode(0);
                        //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                        return lra;
                	} else if(pid==0) {
                		/*LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-1);
                        lra.setErrorMessage("Bạn đang ở trong bàn chơi khác.");
                        ulogin = null ;
                        return lra;*/
                        if (ActionUtils.checkReconnect(ulogin.getGameid())) {
                			//int tableid = ulogin.getTableId();
                			int tableid = ServiceImpl.GetTableIdUserOnline(ulogin.getUserid(), ulogin.getGameid()) ;
                       		if (tableid > 0) {
                      			ServiceImpl.UpdateUserOnline(ulogin);
                      			uController.UpdateIsOnlineToCache(ServerSource.THAI_SOURCE, ulogin.getUserid() - 500000000, ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
            					//Joind vao ban
                      			LoginResponseAction lra = new LoginResponseAction(true,"", ulogin.getUserid());
                                  ulogin.setPid(lra.getPlayerid());
                                  lra.setScreenname(ulogin.getUsername());
                                  lra.setErrorCode(0);
                                  //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                                  return lra;
                      		} else if (tableid < -1) {
  	                    		LoginResponseAction lra = new LoginResponseAction(false, 0);
  	                            lra.setErrorCode(-5);
  	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
  	                            //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
  	                            ulogin = null ;
  	                            return lra;
                      		} else {
                      			LoginResponseAction lra = new LoginResponseAction(false, 0);
  	                            lra.setErrorCode(-1);
  	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
  	                            //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
  	                            ulogin = null ;
  	                            return lra;
                      		}
                  		} else {
                    		LoginResponseAction lra = new LoginResponseAction(false, 0);
                            lra.setErrorCode(-1);
                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
                            //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                            ulogin = null ;
                            return lra;
                  		}
                	} else {
                		uController.UserDisconnected(ServerSource.THAI_SOURCE, ulogin.getUserid() - 500000000);
                		LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-1); //Ban dang o trong ban choi
                        lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err3",ulogin.getSource()));
                        //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                        ulogin = null ;
                        return lra;
//                		return handle(action);
                	}
                } else {
                	LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-1);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err3",ulogin.getSource()));
                    //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                    ulogin = null ;
                    return lra;
                }
            } else {
            	LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-4);
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err4",ulogin.getSource()));
                //ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
                return lra ;
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        LoginResponseAction ret = new LoginResponseAction(false, 0);
        ret.setErrorCode(-5);
        ret.setErrorMessage("บัญชีของท่านออกได้แล้ว รอประมาณ 30 นาที");
        return ret;
    }
}
