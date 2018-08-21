package com.athena.services.api;

import java.util.Date;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.RoutableService;
import com.google.gson.JsonObject;

public interface ServiceContract extends Contract ,RoutableService{
	
    String getUserInfoByPid(int pid,int tid);
    
    void updatePokerUserInfoByPid(int pid,long agBuyIn, boolean autoFill, boolean autoTopOff);
    
    Long UpdateMarkChessById(int uid,int mark,int typeU);
    
    Long UpdateMarkTemp(int uid) ;
    
    public Long UpdateBotPoker9KMark(int uid, long mark);
    
    void UpdateJackpot(int mark, short source) ;
    
    
    public Long UpdateBotDummyMarkChess(int uid,int source,String name,long mark,int typeU);
    
    public Long UpdateBotDummyThaiMarkChess(int uid,int source,String name,long mark,int typeU);
    
    public Long UpdateBotMinidiceMarkChess(int uid,int source,String name,long mark);
   
    public Long UpdateBotXocdiaMarkChess(int uid,int source,String name,long mark);
    
    public Long UpdateBotStreethiloMarkChess(int uid,int source,String name,long mark);
    
    public Long UpdateBotHiloMarkChess(int uid,int source,String name,long mark);
    
     public Long UpdateBotDominoMarkChess(int uid,int source,String name,long mark);
    
    int GetJackpotWin(int markUnit, int vip, String username, int source, int userid, int avatar, int diamondType) ;
//    int GetJackpotWin(int markUnit, int vip, String username, int source) ;
    
    void getUserGameBot(int mark,short gameId, int tableId,int Diamond);
    
    void updateBotOnline(int pid, short isOnline, int gameid);
    
    Long UpdateBotMarkChessByName(int uid,int source,String name,long mark,int typeU); //type = 1 ==> Khong up date DB
    
    long UpdateBotDiamondByName(int uid,int source,String name,long mark,int typeU); //Update Diamond for Bot
    
    void RestoreUser(int pid);

    Long UpdateRouletteMutil(int uid, String username, int numberwin, int lqbuy, int totallq, String[] arr, int agwin, long ag);
    
    Long UpdateXocdiaDetailLong(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win) ;
    
    Long UpdateXocdiaDetail(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, int Win) ;

    Long UpdateXocdiaDetail(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, int Result, int Win) ;
    
    Long UpdateXocdiaDetailLongUsing(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win, int gameId);
    
    void UpdateHiloDetail(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, String Result, int Win, int GameId) ;
    
    Long PromotionByUid(int pid, boolean isAddAG);
    
    void LogTable(int iLevel, int iRevenue, java.sql.Date dtTime, int gameid, int source) ;

    void LogTable(int iLevel, long iRevenue, java.sql.Date dtTime, int gameid, int source) ;
    
    void LogPlayer(int userid,int GameId,int iLevel,int iWin,java.sql.Date dtTime, int source, int iWinMark, int deviceid) ;

    void LogPlayer(int userid,int GameId,int iLevel,int iWin,java.sql.Date dtTime, int source, long iWinMark, int deviceid) ;
    
    void LogPlayer(int userid,int GameId,int iLevel,int iWin,java.sql.Date dtTime, int source, int iWinMark, int deviceid, int diamondType) ;
    
    void LogPlayerLong(int userid,int gameid,long mark, int win, Date dtTime, int source, long markWin, int operator) ;
    
    void LogTableLong(long mark, long revenue, Date dtTime, int gameid, int source);
    
    void PlayerLeaveTable(int pid);
    
    void sendToClient(int pid,String evt,String data);
    
    void sendToClientJsonData(int pid,String evt,String data, JsonObject json);
    
    void sendSpecialAlert(int pid,String evt,String data, String name, int source);    
    
    void sendSpecialAlert_Xocdia(int pid,String evt,String data, String name, int source);    
    
    void confirmSelectRoom(int pid, int roomId, int gameId); //Phuc vu version moi
    
    void confirmSelectRoom_Only(int pid, int roomId, int tableId, int mark); //Phuc vu version moi ==> Thay doi Room trong choi ngay

    void botRejectJoinTable(int pid);
    
    void selectLevel(int pid,int levelid,String data, int gameId);
    
    void updateResultLast(int pid, int result) ; //Phuc vu reing cho Tien len
    
    void AutoJoinTable(int pid, int tableid, int gameid) ;
    
    void AutoLeftTable(int pid, int tableid, int gameid) ;
    
    void AutoInvite(int pid, int tableid, int ag, String name, int agU) ;
    
    void sendErrorMsg(int pid, String data) ;
    
    int GetCurrentPlayerInRoom(int roomId, int gameId); //Lay so nguoi hien tai trong Room.
    
    int GetCurrentPlayerInMark(int mark, int gameId); //Lay so nguoi choi theo muc cuoc
    
    int GetCurrentPlayerInMark(long mark, int gameId);
    
    boolean CheckPromotion(long ag, int vip, int source) ;

	long UpdateDiamondById(int uid, long mark, int gameid);

	Long UpdateMarkChessById(int uid, long mark, int gameid);
	
	Long UpdateMarkChessById(int uid, long mark, int gameid, int markUnit);
	
	void BotCreateTable(int gameid, int mark);
	void BotCreateTable(int gameid, int mark, int type);
	
	void BotCreateTableForRoom(int gameid, int mark, int roomID);
	
	long ChipAfterTaxforGame(long ag, int vip, int source, int gameid, int currentGold, int currentMarkUnit, int countMsg) ;
	
	int getUserMapID(int source);
	
    void updatePokerGameTypeUser(int pid,short gameType);
    
    void returnBot(Integer userid);
    
    int getLuckyPercent(int source, int userid, int vip, int gameid);
    
    int getLuckyPercent(int source, int userid, int vip, int gameid, int ag);
    
    int UpdateMarkForHighLow(int uid, int mark, int n, int gameid) ;
    
    public long UpdateBotMark(int uid, long mark, int gameid);

	public void processRoom(JsonObject json);

    Long UpdateBotMarkChess(int uid, int source, String name, long mark, int gameId);

    void updateFinishGame(int uid, int countGame, int source);//increse gamecount, gameno each game finish
}