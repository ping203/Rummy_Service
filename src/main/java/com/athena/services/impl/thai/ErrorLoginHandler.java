/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.impl.thai;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
//import com.athena.services.utils.CoupleKey;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.google.gson.Gson;
import com.vng.tfa.common.SqlService;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
//import java.net.URLDecoder;
import java.util.Date;
//import java.util.HashMap;

import org.apache.log4j.Logger;

//import zme.api.core.Environment;
//import zme.api.exception.ZingMeApiException;
//import zme.api.graph.ZME_GraphAPI;
//import zme.api.oauth.ZME_Authentication;

/**
 *
 * @author UserXP
 */
public class ErrorLoginHandler implements LoginHandler {
    
    private final String messageKey;
    private final int source;

    public ErrorLoginHandler(String messageKey, int source) {
        this.messageKey = messageKey;
        this.source = source;
    }
    
    
    
    //private UserController uController = new UserController();
    //private Gson gson = new Gson();
    //public Environment env = Environment.PRODUCTION;
    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
//    	return null ;
    	LoginResponseAction ret = new LoginResponseAction(false, 0);
        ret.setErrorCode(-1000); //Gap van de khi connect server
        ret.setErrorMessage(ServiceImpl.actionUtils.getConfigText(messageKey,source));//"strConnect_Err3"
        return ret;
    }
    
    
}
