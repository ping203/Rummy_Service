/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cachebase.queue;

import com.athena.database.UserController;
import com.athena.services.friends.FriendsHandler;
import com.athena.services.friends.MessageHandler;
import com.athena.services.promotion.PromotionHandler;
//import com.athena.services.vo.Roulette;
import com.cachebase.libs.queue.QueueCommand;

//import java.sql.Connection;

//import org.apache.log4j.Logger;

/**
 *
 * @author thangblc
 */
public class UserInfoCmd implements QueueCommand
{
    private String method;
    private int conn;
    private int   userId;
    private long  mark;
    
    private int ag ;
    private long agupdate ;
    private int source ;
    private int agpay ;
    private int agmatch ;
    private String deviceid ;
    private String arrid ;
    
    private String uname ;
    
    private long idroulette ;
    private int lqbuyroulette ;
    
    public UserInfoCmd(String method, int conn, int userId)  //Update Daily First Time
    {
        this.conn = conn;
        this.method = method;
        this.userId = userId;
    }
    public UserInfoCmd(String method, int conn, int userId, long mark)  //Update AG
    {
        this.conn = conn;
        this.method = method;
        this.userId = userId;
        this.mark = mark;
    }
    
    public UserInfoCmd(String method, int conn, int userId, int ag,int agpay,int agmatch, String deviceid) //Insert PromotionDt, and New User
    {
        this.conn = conn;
        this.method = method;
        this.userId = userId;
        this.ag = ag;
        this.agpay = agpay ;
        this.agmatch = agmatch ;
        this.deviceid = deviceid ;
    }
    
    public UserInfoCmd(String method, int conn, int userId, long id,int lqbuy,String username) //Update Roulette
    {
        this.conn = conn;
        this.method = method;
        this.userId = userId;
        this.uname = username;
        this.idroulette = id ;
        this.lqbuyroulette = lqbuy ;
    }
    
    public UserInfoCmd(String method, int conn, int userId, long id, long agupdate,String username) //Update Roulette
    {
        this.conn = conn;
        this.method = method;
        this.userId = userId;
        this.uname = username;
        this.idroulette = id ;
        this.agupdate = agupdate ;
    }
    
    public UserInfoCmd(String method, int conn, String arrId) //Update PromotionDt
    {
        this.conn = conn;
        this.method = method;
        this.arrid = arrId ;
    }
    private int gameid ;
    private int operatorid ;
    private String ipaddress ;
    private String userip ;
    public UserInfoCmd(String method, int conn, int userId, int gameid,int operatorid,String ipaddress, String userip) //Update Isonline when connect
    {
        this.conn = conn;
        this.method = method;
        this.userId = userId ;
        this.gameid = gameid ;
        this.operatorid = operatorid ;
        this.ipaddress = ipaddress ;
        this.userip = userip ;
    }
    public UserInfoCmd(String method, int conn, int userId, int ag) //Update Daily PromotionDt
    {
        this.conn = conn;
        this.method = method;
        this.userId = userId ;
        this.ag = ag ;
    }
    private int tableid ;
    private String strNumXocdia ;
    private String strMarkXocdia ;
    private int numbuyXocdia ;
    private int markBuyXocdia ;
    private int dealerXocdia ;
    private int resultXocdia ;
    private int winXocdia ;
    private String resultHilo ;
    private int gameIdHilo ;
    
    private long markBuyXocdiaLong ;
    private long winXocdiaLong ;
    
    public UserInfoCmd(String method, int conn, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, String Result, int Win, int GameId) //Update Xocdia
    {
        this.conn = conn;
        this.method = method;
        this.userId = Userid ;
        this.tableid = TableId ;
        this.strNumXocdia = StrNum ;
        this.strMarkXocdia = StrMark ;
        this.numbuyXocdia = NumBuy ;
        this.markBuyXocdia = MarkBuy ;
        this.dealerXocdia = Dealer ;
        this.resultHilo = Result ;
        this.winXocdia = Win ;
        this.gameIdHilo = GameId ;
    }
    
    public UserInfoCmd(String method, int conn, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, int Result, int Win) //Update Xocdia
    {
        this.conn = conn;
        this.method = method;
        this.userId = Userid ;
        this.tableid = TableId ;
        this.strNumXocdia = StrNum ;
        this.strMarkXocdia = StrMark ;
        this.numbuyXocdia = NumBuy ;
        this.markBuyXocdia = MarkBuy ;
        this.dealerXocdia = Dealer ;
        this.resultXocdia = Result ;
        this.winXocdia = Win ;
    }
    
    public UserInfoCmd(String cmd, int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win) //Update Xocdia
    {
        this.conn = source;
        this.method = cmd;
        this.userId = Userid ;
        this.tableid = TableId ;
        this.strNumXocdia = StrNum ;
        this.strMarkXocdia = StrMark ;
        this.numbuyXocdia = NumBuy ;
        this.markBuyXocdiaLong = MarkBuy ;
        this.dealerXocdia = Dealer ;
        this.resultXocdia = Result ;
        this.winXocdiaLong = Win ;
    }
    
    public UserInfoCmd(String cmd, int source, int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win, int Gameid) //Update Xocdia
    {
        this.conn = source;
        this.method = cmd;
        this.userId = Userid ;
        this.tableid = TableId ;
        this.strNumXocdia = StrNum ;
        this.strMarkXocdia = StrMark ;
        this.numbuyXocdia = NumBuy ;
        this.markBuyXocdiaLong = MarkBuy ;
        this.dealerXocdia = Dealer ;
        this.resultXocdia = Result ;
        this.winXocdiaLong = Win ;
        this.gameid = Gameid ;
    }
    
    //Log
    private int logGameid ;
    private int logLevel ;
    private int logWin ;
    private int logWinMark ;
    private int logDeviceid ;
    private int logDiamondType ;
    private java.sql.Date logTime ;
    public UserInfoCmd(String method, int conn, int Userid, int gameId, int iLevel,int iWin, java.sql.Date dtTime, int iWinMark, int deviceid) //Update Xocdia
    {
        this.conn = conn;
        this.method = method;
        this.userId = Userid ;
        this.logGameid = gameId ;
        this.logLevel = iLevel ;
        this.logWin = iWin ;
        this.logWinMark = iWinMark ;
        this.logDeviceid = deviceid ;
        this.logTime = dtTime ;
    }
    public UserInfoCmd(String method, int conn, int Userid, int gameId, int iLevel,int iWin, java.sql.Date dtTime, int iWinMark, int deviceid, int diamondType) //Update Xocdia
    {
        this.conn = conn;
        this.method = method;
        this.userId = Userid ;
        this.logGameid = gameId ;
        this.logLevel = iLevel ;
        this.logWin = iWin ;
        this.logWinMark = iWinMark ;
        this.logDeviceid = deviceid ;
        this.logTime = dtTime ;
        this.logDiamondType = diamondType ;
    }
    private long logRevenue ;
    public UserInfoCmd(String method, int conn, int iLevel, long iRevenue, int gameId, java.sql.Date dtTime) //Update Xocdia
    {
        this.conn = conn;
        this.method = method;
        this.logGameid = gameId ;
        this.logLevel = iLevel ;
        this.logRevenue = iRevenue ;
        this.logTime = dtTime ;
    }
    private int matchid ;
    private int footballBet ;
    private float footballValue ;
    public UserInfoCmd(String method, int conn,int userid, int matid, int bet, float betValue, int ag) //Football
    {
        this.conn = conn;
        this.method = method;
        this.userId = userid ;
        this.matchid = matid ;
        this.footballBet = bet ;
        this.footballValue = betValue ;
        this.ag = ag ;
    }
    
    private float lotteryTyle ;
    private int lotterySoluong ;
    private int lotteryMenhgia ;
    private String lotterySo ;
    public UserInfoCmd(String method, int conn,int userid, float tyle,int soluong,int menhgia,String so) //Lottery
    {
        this.conn = conn;
        this.method = method;
        this.userId = userid ;
        this.lotteryTyle = tyle ;
        this.lotterySoluong = soluong ;
        this.lotteryMenhgia = menhgia ;
        this.lotterySo = so ;
    }
    
    private String passlock ;
    public UserInfoCmd(String method, int conn, int userId, String passlock)  //Update PassLock
    {
        this.conn = conn;
        this.method = method;
        this.userId = userId;
        this.passlock = passlock ;
    }
    
    //Log disconnect
    public UserInfoCmd(String method, int conn, int userid, int gameid, int ilevel)  //Update PassLock
    {
        this.conn = conn;
        this.method = method;
        this.userId = userid;
        this.gameid = gameid ;
        this.logLevel = ilevel ;
    }
    private int bonusVip ;

    private String bonusName ;
    public UserInfoCmd(String method, int conn, int userid, int vip, String uname, long chip)  //Update PassLock
    {
        this.conn = conn;
        this.method = method;
        this.userId = userid;
        this.bonusVip = vip ;
        this.bonusName = uname ;
        this.agupdate = chip;
    }
    
    private long messid ;
    private short statusTo ;
    private short statusFrom ;
    public UserInfoCmd(String method, int conn, int userid, long messid, short statusTo, short statusFrom)  //Update Message
    {
        this.conn = conn;
        this.method = method;
        this.userId = userid;
        this.messid = messid ;
        this.statusTo = statusTo ;
        this.statusFrom = statusFrom ;
    }
    
    private String jackpotName ;
    private int  jackpotVip ;
    private int jackpotMarkUnit ;
    private int jackpotMark ;
    public UserInfoCmd(String method, int conn, String name, int vip, int markunit, int mark)  //Update PassLock
    {
        this.conn = conn;
        this.method = method;
        this.jackpotName = name;
        this.jackpotVip = vip ;
        this.jackpotMarkUnit = markunit ;
        this.jackpotMark = mark ;
    }
    public UserInfoCmd(int conn, int uid, int vip, String method, int ag)  //Update AG,Vip
    {
        this.conn = conn;
        this.method = method;
        this.bonusVip = vip;
        this.userId = uid;
        this.ag = ag;
    }
    //For Bet Highlow
    public UserInfoCmd(int source, int uid, int vip, String method, long ag)  //Update AG,Vip
    {
        this.source = source;
        this.method = method;
        this.bonusVip = vip;
        this.userId = uid;
        this.agupdate = ag;
    }
    
    private int agHigh ;
    private int agLow ;
    private long timeBet ;
    private String strArr ;
    private long idBet ;
    private long markHighlow ;
    private int totalrefund ;
    
    public UserInfoCmd(String method, int conn, int uid, long markhighlow, int totalrefund, int agHigh, int agLow, long timeBet, String strArr, long idBet)
    {
    	this.method = method ;
    	this.conn = conn ;
    	this.userId = uid ;
    	this.markHighlow = markhighlow ;
    	this.agHigh = agHigh ;
    	this.agLow = agLow ;
    	this.timeBet = timeBet ;
    	this.strArr = strArr ;
    	this.idBet = idBet ;
    	this.totalrefund = totalrefund ;
    }
    //For Balance + Stop Highlow
    private long balanceHigh ;
    private long balanceLow ;
    private long totalHigh;
    private long totalLow ;
    private long totalPay ;
    private long timeStop ;
    
    public UserInfoCmd(String method, int conn, long balanHigh, long balanLow, long totalHigh, long totalLow, long totalPay, long timeStop, String strArr)
    {
    	this.method = method ;
    	this.conn = conn ;
    	this.balanceHigh = balanHigh ;
    	this.balanceLow = balanLow ;
    	this.totalHigh = totalHigh ;
    	this.totalLow = totalLow ;
    	this.totalPay = totalPay ;
    	this.timeStop = timeStop ;
    	this.strArr = strArr ;
    }
    
  //For Balance + Stop Highlow
    private long invitefromid ;
    private long invitetoid ;
    private String invitefromname;
    private String invitetoname ;
    private int typeinvite ;
    
    public UserInfoCmd(String method, int conn, long infromid, long intoid, String infromname, String intoname, int typein)
    {
    	this.method = method ;
    	this.conn = conn ;
    	this.invitefromid = infromid ;
    	this.invitetoid = intoid ;
    	this.invitefromname = infromname ;
    	this.invitetoname = intoname ;
    	this.typeinvite = typein ;
    }

    @Override
    public void execute()
    {
    	try {
    		if (this.method.equals("updateAG"))
            {
                UserController ucontroller = new UserController();
                ucontroller.UpdateAGDb(conn, userId, mark);
            } else if (this.method.equals("updateDiamond")) {
                UserController ucontroller = new UserController();
                ucontroller.UpdateDiamondDb(conn, userId, mark);
            }/* else if (this.method.equals("gameITempPromoteDtCmdNewUser")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameITempPromoteDtDbNewUser(conn, userId, ag, agpay, agmatch, deviceid);
            }*/ else if (this.method.equals("gameITempPromoteDtCmd")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameITempPromoteDtDb(conn, userId, ag, agpay, agmatch, deviceid);
            } else if (this.method.equals("gameUTempPromoteDtCmd")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameUTempPromotionDtDb(conn, arrid);
            } else if (this.method.equals("gameURouletteCmd")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameURouletteDb(conn, userId, idroulette, lqbuyroulette, uname);
            } else if (this.method.equals("gameUIsonlineConnectCmd")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameUIsonlineConnectDb(conn, userId, gameid, operatorid, ipaddress, userip);
            } else if (this.method.equals("updateDailyPromotion")) {
            	UserController ucontroller = new UserController();
                ucontroller.UpdateSiamDailyDb(conn, userId, ag);
            } else if (this.method.equals("updateDailyPromotionFirstTime")) {
            	UserController ucontroller = new UserController();
                ucontroller.UpdateSiamDailyFirstTimeDb(conn, userId);
            } else if (this.method.equals("updateAvatarUser")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameUpdateAvatarDb(conn, userId, ag);
            } else if (this.method.equals("gameUXocdiaCmd")) {
            	UserController ucontroller = new UserController();
                ucontroller.UpdateXocdiaDetailDb(conn, userId,  tableid, strNumXocdia, strMarkXocdia, numbuyXocdia, markBuyXocdia, dealerXocdia, resultXocdia, winXocdia);
            } else if (this.method.equals("gameUXocdiaCmdLong")) {
            	UserController ucontroller = new UserController();
                ucontroller.UpdateXocdiaDetailDbLong(conn, userId,  tableid, strNumXocdia, strMarkXocdia, numbuyXocdia, markBuyXocdiaLong, dealerXocdia, resultXocdia, winXocdiaLong);
            } else if (this.method.equals("gameUXocdiaCmd_Using")) {
            	UserController ucontroller = new UserController();
                ucontroller.UpdateXocdiaDetailDb_Using(conn, userId,  tableid, strNumXocdia, strMarkXocdia, numbuyXocdia, markBuyXocdiaLong, dealerXocdia, resultXocdia, winXocdiaLong, gameid);
            } else if (this.method.equals("gameUHiloCmd")) {
            	UserController ucontroller = new UserController();
                ucontroller.UpdateHiloDetailDb(conn, userId,  tableid, strNumXocdia, strMarkXocdia, numbuyXocdia, markBuyXocdia, dealerXocdia, resultHilo, winXocdia, gameIdHilo);
            } else if (this.method.equals("gameIUserExperienceDt")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameLogIUUserExperienceDtDb(conn, userId, logGameid, logLevel, logWin, logTime, logWinMark, logDeviceid);
            } else if (this.method.equals("gameIUserExperienceDt_New")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameLogIUUserExperienceDtDb_New(conn, userId, logGameid, logLevel, logWin, logTime, logWinMark, logDeviceid, logDiamondType);
            } else if (this.method.equals("gameIUGameRevenue")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameLogIUGameRevenueDb(conn, logLevel, logRevenue, logGameid, logTime);
            } else if (this.method.equals("updateBetFootball")) { //Football
            	UserController ucontroller = new UserController();
                ucontroller.GameIMatchPlayerDb(conn, userId, matchid, footballBet, footballValue, ag);
            } else if (this.method.equals("updateLottery")) { //Lottery
            	UserController ucontroller = new UserController();
                ucontroller.GameILotteryDb(conn, userId, lotteryTyle, lotterySoluong, lotteryMenhgia, lotterySo); 
            } else if (this.method.equals("updatePassLock")) { //Lottery
            	UserController ucontroller = new UserController();
                ucontroller.GameUPassLockDB(conn, userId, passlock); 
            } else if (this.method.equals("gameILogDisconnect")) { //Lottery
            	UserController ucontroller = new UserController();
                ucontroller.GameLogDisconnectDB(conn, userId, gameid, logLevel) ;
            } else if (this.method.equals("gameIBonusChipByTime")) {
            	UserController ucontroller = new UserController();
                ucontroller.GameIBonusChipByTimeDB(conn, userId, bonusName,agupdate) ;
            } else if (this.method.equals("gameFollowFriendRemove")) { //FollowFriend
            	FriendsHandler friend = new FriendsHandler();
            	friend.RemoveFollowFriendDB(conn, userId, ag) ;
            } else if (this.method.equals("gameMessageUpdate")) { //FollowFriend
            	MessageHandler message = new MessageHandler();
            	message.UpdateMessageDB(conn, userId, messid, statusTo, statusFrom) ;
            } else if (this.method.equals("gameCreatePromotion")) { //FollowFriend
            	PromotionHandler promotion = new PromotionHandler();
            	promotion.CreatePromotionDB(conn, userId, (int)idroulette, lqbuyroulette, uname) ;
            }else if (this.method.equals("gameCreatePromotionBot")) { //FollowFriend
            	PromotionHandler promotion = new PromotionHandler();
            	promotion.CreatePromotionBotDB(conn, userId, (int)idroulette, agupdate, uname) ;
            } else if (this.method.equals("gamePromotionRemove")) { //FollowFriend
            	PromotionHandler promotion = new PromotionHandler();
            	promotion.RemovePromotionDB(conn, userId, gameid, logLevel) ;
            } else if (this.method.equals("gameUJackpot")) { //FollowFriend
            	UserController ucontroller = new UserController();
            	ucontroller.UpdateJackpotDB(conn, userId, ag) ;
            }/* else if (this.method.equals("gameUJackpotWin")) { //FollowFriend
            	UserController ucontroller = new UserController();
            	ucontroller.UpdateJackpotWinDB(conn, jackpotName, jackpotVip, jackpotMarkUnit, jackpotMark) ;
            }*/
            else if (this.method.equals("GameUUserVIP")) { //UpdateVip
            	(new UserController()).GameUUserVIP(conn, userId,bonusVip,ag,0) ;
            } else if (this.method.equals("GameUpdateBot")) {
            	(new UserController()).GameUpdateBot(conn, userId,bonusVip,ag) ;
            } else if (this.method.equals("GameUpdateBotNew")) {
            	(new UserController()).GameUpdateBotNew(source, userId,bonusVip,agupdate) ;
            }else if (this.method.equals("updateAGTaixiu")) {
            	(new UserController()).UpdateAGTaixiuDB(conn, userId, markHighlow, totalrefund, agHigh, agLow, timeBet, strArr, idBet) ;
            } else if (this.method.equals("updateBalanceTaixiu")) {
            	(new UserController()).UpdateBalanceTaixiuDB(conn, balanceHigh, balanceLow, totalHigh, totalLow, totalPay, timeStop, strArr) ;
            } else if (this.method.equals("gameIFriendInvite")) {
            	(new UserController()).GameSiamFacebookInviteDB(conn, invitefromid, invitetoid, invitefromname, invitetoname, typeinvite) ;
            }
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
