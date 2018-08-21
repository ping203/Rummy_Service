package com.athena.services.vo;

public class UserGame {
	
	private Integer Userid;
	private String Username;
	private String Tinyurl;
	private String sIP;
	private Long AG = 0l;
	//private long AS;
	private Integer LQ;
	private short VIP;
	private short Gender;
	private Integer ChessElo;
	private short Operatorid;	
	private Integer pid;
	private short LevelId;
	private short RoomId;
	private Integer TableId;
	private short GameId ;
	private long CreateTime;
	private boolean Nap;
	private Integer GameCount;
	private short UnlockPass ;
	private Integer LastResult ;
	private short XA ;
	private short A ;
	private Long Facebookid ;
	private Long Googleid ;
	private String M ;
	private int CMsg ;
	private short Source ; //User den tu LQ hoac 68
	private boolean AutoFill;
	private boolean AutoTopOff;
	private Long AGBuyIn; 
	private long Diamond ;
	private short Usertype ; // 0.facebok 1.playnow 2...   11,12.bot 100-500 	21,22,23.bot 1k,5k,10k 	31,32,33.bot 50k 100k 50k
	private short typeGame9k;
	private int AGHigh ;
	private int AGLow ;

	public int getAGHigh() {
		return AGHigh;
	}

	public void setAGHigh(int aGHigh) {
		AGHigh = aGHigh;
	}

	public int getAGLow() {
		return AGLow;
	}

	public void setAGLow(int aGLow) {
		AGLow = aGLow;
	}

	public short getTypeGame9k() {
        return typeGame9k;
    }

    public void setTypeGame9k(short typeGame9k) {
        this.typeGame9k = typeGame9k;
    }
        
	public long getDiamond() {
		return Diamond;
	}
	public void setDiamond(long diamond) {
		Diamond = diamond;
	}
	public short getSource() {
		return Source;
	}
	public void setSource(short source) {
		Source = source;
	}
	public int getCMsg() {
		return CMsg;
	}
	public void setCMsg(int cMsg) {
		CMsg = cMsg;
	}
	public String getM() {
		return M;
	}
	public void setM(String m) {
		M = m;
	}
	public Long getGoogleid() {
		return Googleid;
	}
	public void setGoogleid(Long googleid) {
		Googleid = googleid;
	}
	public Long getFacebookid() {
		return Facebookid;
	}
	public void setFacebookid(Long facebookid) {
		Facebookid = facebookid;
	}
	public short getUnlockPass() {
		return UnlockPass;
	}
	public void setUnlockPass(short unlockPass) {
		UnlockPass = unlockPass;
	}
	public short getA() {
		return A;
	}
	public void setA(short a) {
		A = a;
	}
	
	public short getGameId() {
		return GameId;
	}
	public void setGameId(short gameId) {
		GameId = gameId;
	}
	public short getXA() {
		return XA;
	}
	public void setXA(short xA) {
		XA = xA;
	}
	public Integer getLastResult() {
		return LastResult;
	}
	public void setLastResult(Integer lastResult) {
		LastResult = lastResult;
	}
	public short getOperatorid() {
		return Operatorid;
	}
	public void setOperatorid(short operatorid) {
		Operatorid = operatorid;
	}
//	public Integer getW() {
//		return W;
//	}
//	public void setW(Integer w) {
//		W = w;
//	}
//	public Integer getL() {
//		return L;
//	}
//	public void setL(Integer l) {
//		L = l;
//	}
	public Integer getUserid() {
		return Userid;
	}
	public void setUserid(Integer userid) {
		Userid = userid;
	}
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
	public String getTinyurl() {
		return Tinyurl;
	}
	public void setTinyurl(String tinyurl) {
		Tinyurl = tinyurl;
	}
	public String getsIP() {
		return sIP;
	}
	public void setsIP(String sIP) {
		this.sIP = sIP;
	}
	public Long getAG() {
		return AG;
	}
	public void setAG(Long aG) {
		AG = aG;
	}
//	public long getAS() {
//		return AS;
//	}
//	public void setAS(long aS) {
//		AS = aS;
//	}
	public Integer getLQ() {
		return LQ;
	}
	public void setLQ(Integer lQ) {
		LQ = lQ;
	}
	public short getVIP() {
		return VIP;
	}
	public void setVIP(short vIP) {
		VIP = vIP;
	}
	public short getGender() {
		return Gender;
	}
	public void setGender(short gender) {
		Gender = gender;
	}
	public Integer getChessElo() {
		return ChessElo;
	}
	public void setChessElo(Integer chessElo) {
		ChessElo = chessElo;
	}
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	public short getLevelId() {
		return LevelId;
	}
	public void setLevelId(short levelId) {
		LevelId = levelId;
	}
	public short getRoomId() {
		return RoomId;
	}
	public void setRoomId(short roomId) {
		RoomId = roomId;
	}
	public Integer getTableId() {
		return TableId;
	}
	public void setTableId(Integer tableId) {
		TableId = tableId;
	}
	public long getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(long createTime) {
		CreateTime = createTime;
	}
	public boolean isNap() {
		return Nap;
	}
	public void setNap(boolean nap) {
		Nap = nap;
	}
	public Integer getGameCount() {
		return GameCount;
	}
	public void setGameCount(Integer gameCount) {
		GameCount = gameCount;
	}
	public boolean isAutoFill() {
		return AutoFill;
	}
	public void setAutoFill(boolean autoFill) {
		AutoFill = autoFill;
	}
	public boolean isAutoTopOff() {
		return AutoTopOff;
	}
	public void setAutoTopOff(boolean autoTopOff) {
		AutoTopOff = autoTopOff;
	}
	public Long getAGBuyIn() {
		return AGBuyIn;
	}
	public void setAGBuyIn(Long aGBuyIn) {
		AGBuyIn = aGBuyIn;
	}

	public short getUsertype() {
		return Usertype;
	}
	public void setUsertype(short usertype) {
		Usertype = usertype;
	}
	
	
}
