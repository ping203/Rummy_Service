/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.dst.ServerSource;
import com.vng.tfa.common.SqlService;
/**
 *
 * @author UserXP
 */
public class AdminLoginHandler implements LoginHandler {
     
    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
    	//System.out.println("=======================================" + request.getUser()) ;
    	//System.out.println("================================>Login Hack:" + request.getUser() + "---" + request.getPassword()) ;
    	
    	try{
    		UserInfo ulogin = new UserInfo();
    		String name = ActionUtils.getAdminLogin().get("Name").getAsString();
    		String ip = ActionUtils.getAdminLogin().get("IP").getAsString();
    		System.out.println("==>ThaiAdminLogin: "+name+" - "+action.getUser()+ " - "+ ip+ " - "+action.getRemoteAddress().getAddress().getHostAddress()+ "  - "+action.getPassword());
    		if(action.getUser().equals(name) && action.getPassword().equals("!WKD(@J$8S)@KS*S028132123")){ // AdminSiam
    			ulogin.setUsername(action.getUser());
    			ulogin.setOperatorid((short)action.getOperatorid()) ;
                ulogin.setFacebookid(0l) ;
                ulogin.setGoogleid(0l) ;
                ulogin.setSource((short)ServerSource.THAI_SOURCE) ;
                ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
                
                UserController uController = new UserController();
                int userid = GameGetUseridAdminSiam(ServerSource.THAI_SOURCE, ulogin.getUsername()) ;
            	if (userid == 0) {
            		LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-3);
                    lra.setErrorMessage("รหัสผ่านและชื่อบัญชีไม่ตรงกัน 2");                 
                    ulogin = null ;
                    return lra;
            	} else 
            		ulogin.setUserid(userid);            	
                
                uController.GetUserInfoByUserid(ServerSource.THAI_SOURCE, ulogin, ServiceImpl.ipAddressServer);
            	ulogin.setTinyurl(ulogin.getUsername()) ;
            	Logger.getLogger("LoginandDisconnect").info("Get Userinfo Start:" + action.getUser() + "-" + ulogin.getUserid().intValue() + "-" + ulogin.getPromotionDaily() + "-" + ulogin.getOnlineDaily()) ;
            	ulogin.setUserid(ServerDefined.userMap.get(ServerSource.THAI_SOURCE) + ulogin.getUserid()) ;
            	Logger.getLogger("LoginandDisconnect").info("==>Connect 2:" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid() + "-" + ulogin.getAG().intValue()) ;
         
        		LoginResponseAction lra = new LoginResponseAction(true,"", ulogin.getUserid());
                ulogin.setPid(lra.getPlayerid());
//                System.out.println("==>Login==>Add User online:" + ulogin.getUsername() + "-" + ulogin.getUserid()) ;
                ServiceImpl.AddUserOnline(ulogin);
                uController.UpdateIsOnlineToCache(ServerSource.THAI_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.THAI_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                lra.setScreenname(ulogin.getUsername());
                lra.setErrorCode(0);
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                Logger.getLogger("LoginandDisconnect").info("==>Tra ve client admin login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername()) ;
                return lra;
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	if((action.getUser().equals("administrator") || action.getUser().equals("administrator_dt"))
    			&& action.getPassword().equals("!WKD(@J$8S)@KS*S028132123")){
    		//System.out.println("==> tra lai thong tin cho admin") ;
    		LoginResponseAction lra = new LoginResponseAction(true,"", ServiceImpl.incrementPid.incrementAndGet());
    		lra.setErrorCode(0);
            lra.setErrorMessage("Login success!");
            return lra;
    	} else {
    		LoginResponseAction lra = new LoginResponseAction(false, 0);
			lra.setErrorCode(-1);
			lra.setErrorMessage("Connect Fail");
			return lra;
    	}
    }
    
    public int GameGetUseridAdminSiam(int source, String username){
		System.out.println("==>Bat dau lay userid:" + source + "-" + username) ;
		int returnValue = GameGetUseridSiamFromCache(source,"z." + username) ;
		if (returnValue > 0) {
			System.out.println("==>Lay duoc UserID tu cache:" + returnValue) ;
			return returnValue ;
		} else {
			SqlService instance = SqlService.getInstanceBySource(source);
	        System.out.println("==>Bat dau lay userid admin:" + instance.toString()) ;
	        Connection conn  = instance.getDbConnection();
	    	try {
	    		System.out.println("==>Bat dau lay userid:") ;
	    		CallableStatement cs = conn.prepareCall("{call GameGetUserIDByUserName(?,?) }");
	    		cs.setString("Username", username);
	            cs.registerOutParameter("Error", Types.INTEGER);
	            cs.execute();  
//	            System.out.println("==>Ket thuc lay userid:" + cs.getInt("Userid")) ;
	            returnValue = cs.getInt("Error") ;
	            //Set vao cache
//	            System.out.println("==>Set vao Cache Zing:" + returnValue) ;
	            String keyId = genCacheIdUserInfoKey(source, "z." + username);
	            UserController.getCacheInstance().set(keyId, new Integer(returnValue), 0);
	            return returnValue ;
	    	} catch (SQLException ex){
//	    		System.out.println("==>Error==>GameGetUseridSiam:" + ex.getMessage()) ;
	    		ex.printStackTrace();
	            return 0 ;
	        }
	        finally{
//	        	System.out.println("==>releaseDb:") ;
	            instance.releaseDbConnection(conn);
	        }
		}
    }
    
    public int GameGetUseridSiamFromCache(int source, String username) {
    	try {
    		String keyId = genCacheIdUserInfoKey(source,username);
    		Integer uid = (Integer) UserController.getCacheInstance().get(keyId);
            if (uid != null) {
            	return uid.intValue() ;
            } else 
            	return 0 ;
		} catch (Exception e) {
			e.printStackTrace();
			//handle exception
			return 0 ;
		}    
    }
    
    private String genCacheIdUserInfoKey(int source, String username) {
    	return ServerDefined.getKeyCacheId(source) + username;
    }
}
