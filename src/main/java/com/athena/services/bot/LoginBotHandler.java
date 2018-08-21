package com.athena.services.bot;

import org.apache.log4j.Logger;
import com.athena.database.UserController;
import com.athena.log.LoggerKey;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.GAMEID;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LoginBotHandler implements LoginHandler {

    public UserController uController = new UserController();
    public JsonParser parser = new JsonParser();

    public static Logger logger_login = Logger.getLogger(LoggerKey.LOGIN_BOT);

    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
        try {
            JsonObject obj = (JsonObject) parser.parse(action.getUser());

            if (!obj.get("user").getAsString().equals("Administrator")
                    || !action.getPassword().equals("Administrator123Aa@#$")) {
                LoginResponseAction ret = new LoginResponseAction(false, 0);
                ret.setErrorCode(-5);
                ret.setErrorMessage("Failed! You are not SystemLogin ^^!");
                logger_login.info("Failed! You are not SystemLogin ^^!" + action.getUser() + "-" + action.getOperatorid());
                return ret;
            }
            int gameid = obj.get("gameid").getAsInt();
            UserInfo ulogin = null;
            if (ServiceImpl.mapBot.containsKey(gameid)) {
                ulogin = ServiceImpl.mapBot.get(gameid).botLogin(gameid);
            } else {
                System.out.println("BotLogin: " + gameid);
            }
            if (ulogin == null) {
                LoginResponseAction ret = new LoginResponseAction(false, 0);
                ret.setErrorCode(-5);
                ret.setErrorMessage("Get User System Fail " + gameid);
                System.out.println("BotLogin:==> Get User System Fail" + gameid);
                return ret;
            }
            ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
            ulogin.setUsername(ulogin.getUsername().toLowerCase());
            LoginResponseAction lra = new LoginResponseAction(true, "", ulogin.getUserid());
            ulogin.setPid(ulogin.getUserid());
            ulogin.setRoomId((short) 0);
            lra.setScreenname(ulogin.getUsername());
            lra.setErrorCode(0);
            System.out.println(
                    "BOT LOGIN OK " + ulogin.getUsername() + " " + ulogin.getPid() + " " + ulogin.getGameid());
            logger_login.info("==>StartLogin Bot: OKKKKK " + ulogin.getUsername() + " " + ulogin.getPid() + " " + ulogin.getGameid());
            return lra;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger_login.error("FinishLogin Fail:" + action.getUser() + "-" + ex.getMessage());
        }
        LoginResponseAction ret = new LoginResponseAction(false, 0);
        ret.setErrorCode(-5);
        ret.setErrorMessage("FinishLogin Fail Other");
        logger_login.info("FinishLogin Fail Other-5:");
        return ret;
    }
}
