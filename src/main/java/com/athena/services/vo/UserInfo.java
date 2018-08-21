/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

import com.athena.log.LoggerKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
//import java.util.HashMap;
import java.util.List;

import com.athena.services.config.LoadConfigProperties;
import com.athena.services.impl.ServiceImpl;
import com.dst.LanguageDefined;
import com.dst.ServerSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author UserXP
 */
public class UserInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6853158610421207486L;
    public static Logger logger_debug = Logger.getLogger(LoggerKey.DEBUG_SERVICE);

    public UserInfo() {
        Username = "";
        LevelId = 0;
        RoomId = 0;
        TableId = 0;
        Pass = false;
        AS = 0l;
        LQ = 0;
        GroupID = 0;
        ChessElo = 0;
        isNap = false;
        isDisconnect = false;
        Operatorid = 105;
        OnlineDaily = 1;
        PromotionDaily = 0;
        MsgType = 0;
        deviceId = "";
        UsernameLQ = "";
        Tinyurl = "";
        Mobile = "";
        MaxAmt = 0;
        OwnAmt = 0;
        PassLock = "";
        DelPassLock = 0l;
        CreateTime = 0l;
        Source = 11; //User den tu Lang quat.
        PPromot = new ArrayList<PPromote>();
        MPromot = new ArrayList<MPromote>();
        ListMsg = new ArrayList<UserMsg>();
        ListMsgAG = new ArrayList<UserMsg>();
        ListMsgPlayer = new ArrayList<UserMsg>();
        IdolName = "";
        Idle = new PingIdle();
        ArrRoulette = new ArrayList<Roulette>();
        ArrRotationLucky = new ArrayList<RotationLucky>();
        LastResult = 0;
        UnlockPass = 1;
        Facebookid = 0l;
        FacebookName = "";
        Googleid = 0l;
        isAutoInvite = true;
        chat = 0;
        event = 0;
        Banned = false;
        Ref = "";
        FreeLuckyCard = 0;
        FreeLuckyRotation = 0;
        LuckyTournament = -1;
        RotationLuckyTournament = -1;
        PassLQ = "";
        LastDisconnect = new Date();
        Reg = 0;
        userSetting = new UserSetting();
        Gender = (short) 3;
        GameCount = 0;
        GCountNow = 0;
        MarkVip = 0;
        markLevel = 0;
        CPromot = 0;
        ReceiveDailyPromotion = false;
        CFriendsF = 0;
        NewGamer = 0;
        GameNo = 0;
        GameAmount = 0;
        Goldnap = 0;
        Usertype = 0;
        DefaultName = "";
        UserIdZing = "";
        AutoFill = true;
        AutoTopOff = false;
        AGBuyIn = 0l;
        Diamond = 0l;
        AGHigh = 0;
        AGLow = 0;
        statusHighLow = 0;
    }
    private boolean isPendingProcessRequest = false;

    public boolean isPendingProcessRequest() {
        return isPendingProcessRequest;
    }

    public void setPendingProcessRequest(boolean pendingProcessRequest) {
        isPendingProcessRequest = pendingProcessRequest;
    }

    public UserGame getUserGame() {
        UserGame ret = new UserGame();
        ret.setPid(this.Pid);
        ret.setUserid(this.Userid);
        ret.setUsername(this.Username);
        ret.setTinyurl(this.Tinyurl);
        ret.setsIP(this.sIP);
        ret.setAG(this.AG);
        ret.setLQ(this.LQ);
        ret.setVIP(this.VIP);
        ret.setDiamond(this.Diamond);
        ret.setGender(this.Gender);
        ret.setChessElo(this.ChessElo);
        ret.setCreateTime(this.CreateTime);
        ret.setNap(this.isNap);
        ret.setGameCount(this.GameCount);
        ret.setLevelId(this.LevelId);
        ret.setRoomId(this.RoomId);
        ret.setTableId(this.TableId);
        ret.setLastResult(this.LastResult);
        ret.setXA((short) 0);
        ret.setGameId(this.gameid);
        ret.setOperatorid(this.Operatorid);
        ret.setA(this.Avatar);
        ret.setUnlockPass(this.UnlockPass);
        ret.setFacebookid(this.Facebookid);
        ret.setGoogleid(this.Googleid);
        ret.setM(this.Mobile);
        ret.setSource(this.Source);
        ret.setCMsg(this.CMsg);
        ret.setAGBuyIn(this.AGBuyIn);
        ret.setAutoFill(this.AutoFill);
        ret.setAutoTopOff(this.AutoTopOff);
        ret.setUsertype(this.Usertype);
        ret.setTypeGame9k(this.gameType9k);
        ret.setAGHigh(this.AGHigh);
        ret.setAGLow(this.AGLow);
        return ret;
    }

    public UserTrans getUserTrans() {
        UserTrans ret = new UserTrans();
        ret.setUserid(this.Userid);
        ret.setUsername(this.Username);
        ret.setTinyurl(this.Tinyurl);
        ret.setG(this.Gender);
        ret.setCE(this.ChessElo);
        ret.setAG(AG);
        ret.setLQ(LQ);
        ret.setVIP(VIP);
        ret.setD(this.Diamond);
        //ret.setCMsg(CMsg);
        ret.setTPromot(TPromot);
        ret.setAGVay(this.OwnAmt);
        ret.setO(this.Operatorid);
        ret.setR(this.isRegister());
        if ((this.UsernameLQ.length() == 0) && (this.Source == ServerSource.THAI_SOURCE)) {
            ret.setUsernameLQ("Siam");
        } else {
            ret.setUsernameLQ(this.UsernameLQ);
        }
        ret.setDN(this.DefaultName);
        if (this.Source == 10 || this.Source == ServerSource.THAI_SOURCE) {
            ret.setPD(this.PromotionDaily);
            ret.setOD(this.OnlineDaily);
        } else {
            ret.setPD(0);
            ret.setOD((short) 0);
        }
        ret.setA(this.Avatar);
        if (this.getCMsg() >= 100) {
            ret.setMT((short) 1);
        } else {
            ret.setMT((short) 0);
        }
        ret.setNM(this.CMsg);
        ret.setCMsg(this.CMsg % 100);
        ret.setM(this.Mobile);
        ret.setE(this.event);
        ret.setLR(this.FreeLuckyRotation);
        ret.setC(this.chat);
        ret.setMVip(this.MarkVip);
        ret.setListDP(this.PassLQ);
        if (this.PassLQ.equals("")) {
            ret.setPL((short) 0);
        } else {
            ret.setPL((short) 1);
        }
        if (this.GameCount >= 10 && !isNap && !ServiceImpl.isX2) {
            ret.setX((short) 1);
        } else {
            ret.setX((short) 0);
        }
        if (CreateTime.compareTo(DelPassLock) != 0) {
            ret.setPass((short) Integer.parseInt(String.valueOf((DelPassLock - (new Date()).getTime()) / (24 * 3600 * 1000))));
        } else {
            if (PassLock.equals("")) {
                ret.setPass((short) 0);
            } else {
                ret.setPass((short) 8);
            }
        }
        ret.setGC(0);
        ret.setWA(this.WinAccumulation);
        ret.setMarkLevel(this.markLevel);

        return ret;
    }

    public UserTrans2 getUserTrans2() {
        UserTrans2 ret = new UserTrans2();
        ret.setUserid(this.Userid);
        ret.setUsername(this.Username);
        ret.setTinyurl(this.Tinyurl);
        ret.setAG(AG);
        ret.setLQ(LQ);
        ret.setVIP(VIP);
        ret.setMarkLevel(this.markLevel);

        if ((this.UsernameLQ.length() == 0) && (this.Source == ServerSource.THAI_SOURCE)) {
            ret.setUsernameLQ("Siam");
        } else {
            ret.setUsernameLQ(this.UsernameLQ);
        }
        if (this.Source == 10 || this.Source == ServerSource.THAI_SOURCE) {
            ret.setPD(this.PromotionDaily);
            ret.setOD(this.OnlineDaily);
        } else {
            ret.setPD(0);
            ret.setOD((short) 0);
        }
        ret.setA(this.Avatar);

        ret.setNM(this.CMsg);

        ret.setMVip(this.MarkVip);
        ret.setListDP(this.PassLQ);
        ret.setShowAds(this.isShowAds);
        ret.setGameNo(this.GameCount);

        return ret;
    }

    public void IncrementMark(long ag) {
        AG += ag;
    }

    public void DecrementMark(long ag) {
        AG -= ag;
    }

    public void IncrementLQ(int lq) {
        LQ += lq;
    }

    public void DecrementLQ(int lq) {
        LQ -= lq;
    }

    public void DecrementDiamond(long diamond) {
        Diamond -= diamond;
    }

    public void IncrementDiamond(long diamond) {
        Diamond += diamond;
    }

    public String language;
    private short Source; //1- Lang quat, 2- 3C, 3- Dautruong, 4- 52, 5 - Cam, 9- Thai
    private String UsernameOld; //Luu tru username old
    private String Username;
    private String UsernameLQ;
    private String Tinyurl;
    private String Signid;
    private Integer Userid;
    private Long AG;
    private Long AS; //Dung luu tam muc cuoc choi hien tai
    private Integer LQ;
    private boolean Banned;
    private short VIP;
    private int MarkVip;
    private int markLevel;
    private short Gender;
    private Integer ChessElo;
    private Integer MaxAmt;
    private Integer OwnAmt;
    private Integer GroupID;
    private String IdolName;
    private String PassLock;
    private Long DelPassLock; //Luu thoi gian lay config gan nhat
    private String DefaultName;
    private boolean AutoFill;
    private boolean AutoTopOff;
    private Long AGBuyIn = 0l;

    private int CFriendsF;
//    private boolean FirstLoginInDay ;
    private boolean ReceiveDailyPromotion;
    private int CMsg;
    private Integer CPromot;
    private List<PPromote> PPromot;
    private List<MPromote> MPromot;
    private Integer TPromot;
    private Long CreateTime;
    private Long LastLogin;
    private short isOnline;
    private List<UserMsg> ListMsg;
    private List<UserMsg> ListMsgPlayer;
    private List<UserMsg> ListMsgAG;

    private int Pid;
    private int TableId;
    private short LevelId;
    private short RoomId;
    private short gameid;
    private String From;
    private boolean Pass;
    private String sIP;
    private boolean isDisconnect;
    private PingIdle Idle;
    private boolean isNap; //Dung tam cho taixiu
    private int GameCount;
    private boolean isShowAds = false;
    private int GCountNow;
    private short Operatorid;
    private boolean Register;
    private int LastResult;
    private UserSetting userSetting;
    private String deviceId;
    private short OnlineDaily; //So ngay online lien tiep
    private int PromotionDaily; //So tien duoc tang ngay hom do
    private short MsgType; //Loai MSg 1 - Co thu tu he thong, 2 - Co thu nhan AG
    private short UnlockPass; //1 - Da mo pass, 0 - Chua mo - Dung de check xem da thong bao tang tien hom sau chua
    private short Avatar;
    private String Mobile;//Dang ky mobile chua
    private boolean isAutoInvite; //Xem co duoc auto Invite khong
    private short chat; //1-Cam chat, 0- chat binh thuong
    private Long Facebookid;
    private String FacebookName; //Ten tren face book
    private Long Googleid;
    private short event; //Thong bao event dang dien ra.
    private String Ref; //Den tu nguon nao
    private short FreeLuckyCard; //So lan duoc boc bai may man con duoc free _ Dau truong
    private short FreeLuckyRotation; //So lan duoc quay vong quay _ Dau truong
    private short LuckyTournament; //Diem trong cuoc dua bai
    private short RotationLuckyTournament; //Diem trong cuoc dua vong quay
    private String PassLQ; //Mật khẩu trên Làng quạt
    private Date LastDisconnect;
    private short Reg;//Cho biet co la user dang ky khong
    private String LastDeviceID; //Lan login gan nhat la deviceid nao.
    private String UserIdZing; //UserIdZing
    private int NewGamer; //De check NewGamer
    private int AGNewGamer; //Luu Gold tang tan thu
    private double GameNo; //So van choi
    private double GameAmount; //Tong tien giao dich
    private int Goldnap;
    private short Usertype; // 0.facebok 1.playnow   11.bot 100-500 12.bot 1k,5k,10k 13.bot 50k 100k 50k
    private int PromotionValue; //Tong so tien duoc khuyen mai trong ngay
    private long Diamond;

    //Tai suu
    private int AGHigh; //Tien dat cua High
    private int AGLow; //Tien dat cua Low
    private int statusHighLow; //trang thai HighLow
    private int AGWinHighLow; //AG win lan cuoi
//    private int loginCount ; //so lan gui login tu duoi client len
    private long WinAccumulation; //Tien thang tich luy

    //private String UserData = "";// Json Data
    public int getAGWinHighLow() {
        return AGWinHighLow;
    }

    public void setAGWinHighLow(int aGWinHighLow) {
        AGWinHighLow = aGWinHighLow;
    }

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

    public int getStatusHighLow() {
        return statusHighLow;
    }

    public void setStatusHighLow(int statusHighLow) {
        this.statusHighLow = statusHighLow;
    }
    private short gameType9k;

    public short getGameType9k() {
        return gameType9k;
    }

    public void setGameType9k(short gameType9k) {
        this.gameType9k = gameType9k;
    }

    public String getLanguage() {
        if (StringUtils.isEmpty(this.language)) {
            return LanguageDefined.EN;
        }
        return LanguageDefined.detectLanguage(language);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getWinAccumulation() {
        return WinAccumulation;
    }

    public void setWinAccumulation(long winAccumulation) {
        WinAccumulation = winAccumulation;
    }

    public long getDiamond() {
        return Diamond;
    }

    public void setDiamond(long diamond) {
        Diamond = diamond;
    }

    public int getPromotionValue() {
        return PromotionValue;
    }

    public void setPromotionValue(int promotionValue) {
        PromotionValue = promotionValue;
    }

    public String getDefaultName() {
        return DefaultName;
    }

    public void setDefaultName(String defaultName) {
        DefaultName = defaultName;
    }

    public short getUsertype() {
        return Usertype;
    }

    public void setUsertype(short usertype) {
        Usertype = usertype;
    }

    public int getGoldnap() {
        return Goldnap;
    }

    public void setGoldnap(int goldnap) {
        Goldnap = goldnap;
    }

    public double getGameNo() {
        return GameNo;
    }

    public void setGameNo(double gameNo) {
        GameNo = gameNo;
    }

    public double getGameAmount() {
        return GameAmount;
    }

    public void setGameAmount(double gameAmount) {
        GameAmount = gameAmount;
    }

    public int getAGNewGamer() {
        return AGNewGamer;
    }

    public void setAGNewGamer(int aGNewGamer) {
        AGNewGamer = aGNewGamer;
    }

    public int getNewGamer() {
        return NewGamer;
    }

    public void setNewGamer(int newGamer) {
        NewGamer = newGamer;
    }

    public String getUserIdZing() {
        return UserIdZing;
    }

    public void setUserIdZing(String userIdZing) {
        UserIdZing = userIdZing;
    }

    public boolean isReceiveDailyPromotion() {
        return ReceiveDailyPromotion;
    }

    public void setReceiveDailyPromotion(boolean receiveDailyPromotion) {
        ReceiveDailyPromotion = receiveDailyPromotion;
    }

    public int getCFriendsF() {
        return CFriendsF;
    }

    public void setCFriendsF(int cFriendsF) {
        CFriendsF = cFriendsF;
    }
//	public boolean isFirstLoginInDay() {
//		return FirstLoginInDay;
//	}
//	public void setFirstLoginInDay(boolean firstLoginInDay) {
//		FirstLoginInDay = firstLoginInDay;
//	}

    public int getMarkVip() {
        return MarkVip;
    }

    public void setMarkVip(int markVip) {
        MarkVip = markVip;
    }

    public int getMarkLevel() {
        return markLevel;
    }

    public void setMarkLevel(int markLevel) {
        this.markLevel = markLevel;
    }

    public String getLastDeviceID() {
        return LastDeviceID;
    }

    public void setLastDeviceID(String lastDeviceID) {
        LastDeviceID = lastDeviceID;
    }

    public short getReg() {
        return Reg;
    }

    public void setReg(short reg) {
        Reg = reg;
    }

    public Date getLastDisconnect() {
        return LastDisconnect;
    }

    public void setLastDisconnect(Date lastDisconnect) {
        LastDisconnect = lastDisconnect;
    }

    public String getPassLQ() {
        return PassLQ;
    }

    public void setPassLQ(String passLQ) {
        PassLQ = passLQ;
    }

    public short getRotationLuckyTournament() {
        return RotationLuckyTournament;
    }

    public void setRotationLuckyTournament(short rotationLuckyTournament) {
        RotationLuckyTournament = rotationLuckyTournament;
    }

    public int getLuckyTournament() {
        return LuckyTournament;
    }

    public void setLuckyTournament(short luckyTournament) {
        LuckyTournament = luckyTournament;
    }

    public short getFreeLuckyRotation() {
        return FreeLuckyRotation;
    }

    public void setFreeLuckyRotation(short freeLuckyRotation) {
        FreeLuckyRotation = freeLuckyRotation;
    }

    public short getFreeLuckyCard() {
        return FreeLuckyCard;
    }

    public void setFreeLuckyCard(short freeLuckyCard) {
        FreeLuckyCard = freeLuckyCard;
    }

    public String getRef() {
        return Ref;
    }

    public void setRef(String ref) {
        Ref = ref;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(short event) {
        this.event = event;
    }
    //1- Lang quat, 2- 3C, 3- Dautruong, 4- 52, 5 - Cam, 9- Thai

    public short getSource() {
        return Source;
    }

    public void setSource(short source) {
        try {
            this.Source = source;
        } catch (Exception e) {
            logger_debug.error(e.getMessage(), e);
            throw e;
        }

    }

    public Long getFacebookid() {
        return Facebookid;
    }

    public void setFacebookid(Long facebookid) {
        Facebookid = facebookid;
    }

    public String getFacebookName() {
        return FacebookName;
    }

    public void setFacebookName(String facebookName) {
        FacebookName = facebookName;
    }

    public Long getGoogleid() {
        return Googleid;
    }

    public void setGoogleid(Long googleid) {
        Googleid = googleid;
    }

    public short getChat() {
        return chat;
    }

    public void setChat(short chat) {
        this.chat = chat;
    }

    public boolean isAutoInvite() {
        return isAutoInvite;
    }

    public void setAutoInvite(boolean isAutoInvite) {
        this.isAutoInvite = isAutoInvite;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public short getAvatar() {
        return Avatar;
    }

    public void setAvatar(short avatar) {
        Avatar = avatar;
    }

    public short getUnlockPass() {
        return UnlockPass;
    }

    public void setUnlockPass(short unlockPass) {
        UnlockPass = unlockPass;
    }

    public int getMsgType() {
        return MsgType;
    }

    public void setMsgType(short msgType) {
        MsgType = msgType;
    }

    public short getOnlineDaily() {
        return OnlineDaily;
    }

    public void setOnlineDaily(short onlineDaily) {
        OnlineDaily = onlineDaily;
    }

    public int getPromotionDaily() {
        return PromotionDaily;
    }

    public void setPromotionDaily(int promotionDaily) {
        PromotionDaily = promotionDaily;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public UserSetting getUserSetting() {
        return userSetting;
    }

    public void setUserSetting(UserSetting userSetting) {
        this.userSetting = userSetting;
    }

    public int getLastResult() {
        return LastResult;
    }

    public void setLastResult(int lastResult) {
        LastResult = lastResult;
    }

    public boolean isRegister() {
        return Register;
    }

    public void setRegister(boolean register) {
        Register = register;
    }

    public String getUsernameLQ() {
        return UsernameLQ;
    }

    public void setUsernameLQ(String usernameLQ) {
        UsernameLQ = usernameLQ;
    }

    public short getOperatorid() {
        return Operatorid;
    }

    public void setOperatorid(short operatorid) {
        Operatorid = operatorid;
    }
//	private HashMap<Long, UserAuction> StepAution;
//    private List<CardLucky> ArrCardLucky;
    private List<Roulette> ArrRoulette;
    private int TotalScatter;
    private List<RotationLucky> ArrRotationLucky;

    public int getTotalScatter() {
        return TotalScatter;
    }

    public void setTotalScatter(int totalScatter) {
        TotalScatter = totalScatter;
    }

    public List<RotationLucky> getArrRotationLucky() {
        return ArrRotationLucky;
    }

    public void setArrRotationLucky(List<RotationLucky> arrRotationLucky) {
        ArrRotationLucky = arrRotationLucky;
    }
//	public HashMap<Long, UserAuction> getStepAution() {
//		return StepAution;
//	}
//	public void setStepAution(HashMap<Long, UserAuction> stepAution) {
//		StepAution = stepAution;
//	}
//	public List<CardLucky> getArrCardLucky() {
//		return ArrCardLucky;
//	}
//	public void setArrCardLucky(List<CardLucky> arrCardLucky) {
//		ArrCardLucky = arrCardLucky;
//	}

    public List<Roulette> getArrRoulette() {
        return ArrRoulette;
    }

    public void setArrRoulette(List<Roulette> arrRoulette) {
        ArrRoulette = arrRoulette;
    }

    public int getGCountNow() {
        return GCountNow;
    }

    public void setGCountNow(int gCountNow) {
        GCountNow = gCountNow;
    }

    public boolean isNap() {
        return isNap;
    }

    public void setNap(boolean isNap) {
        this.isNap = isNap;
    }

    public int getGameCount() {
        return GameCount;
    }

    public void setGameCount(int gameCount) {
        GameCount = gameCount;
    }

    public PingIdle getIdle() {
        return Idle;
    }

    public void setIdle(PingIdle idle) {
        Idle = idle;
    }

    public boolean isDisconnect() {
        return isDisconnect;
    }

    public void setDisconnect(boolean isDisconnect) {
        this.isDisconnect = isDisconnect;
    }

    public String getsIP() {
        return sIP;
    }

    public void setsIP(String sIP) {
        this.sIP = sIP;
    }
//	public Integer getL() {
//		return L;
//	}
//	public void setL(Integer l) {
//		L = l;
//	}
//	public Integer getW() {
//		return W;
//	}
//	public void setW(Integer w) {
//		W = w;
//	}

    public List<UserMsg> getListMsgPlayer() {
        return ListMsgPlayer;
    }

    public void setListMsgPlayer(List<UserMsg> listMsgPlayer) {
        ListMsgPlayer = listMsgPlayer;
    }

    public List<UserMsg> getListMsgAG() {
        return ListMsgAG;
    }

    public void setListMsgAG(List<UserMsg> listMsgAG) {
        ListMsgAG = listMsgAG;
    }

    public boolean isPass() {
        return Pass;
    }

    public void setPass(boolean pass) {
        Pass = pass;
    }

    public Integer getMaxAmt() {
        return MaxAmt;
    }

    public void setMaxAmt(Integer maxAmt) {
        MaxAmt = maxAmt;
    }

    public Integer getOwnAmt() {
        return OwnAmt;
    }

    public void setOwnAmt(Integer ownAmt) {
        OwnAmt = ownAmt;
    }

    public Integer getGroupID() {
        return GroupID;
    }

    public void setGroupID(Integer groupID) {
        GroupID = groupID;
    }

    public String getIdolName() {
        return IdolName;
    }

    public void setIdolName(String idolName) {
        IdolName = idolName;
    }

//	public List<FriendlyOnl> getFriendsName() {
//		return FriendsName;
//	}
//
//	public void setFriendsName(List<FriendlyOnl> friendsName) {
//		FriendsName = friendsName;
//	}
    public String getPassLock() {
        return PassLock;
    }

    public void setPassLock(String passLock) {
        PassLock = passLock;
    }

    public Long getDelPassLock() {
        return DelPassLock;
    }

    public void setDelPassLock(Long delPassLock) {
        DelPassLock = delPassLock;
    }

    public short getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(short isOnline) {
        this.isOnline = isOnline;
    }

    public List<UserMsg> getListMsg() {
        return ListMsg;
    }

    public void setListMsg(List<UserMsg> listMsg) {
        ListMsg = listMsg;
    }

    public Long getAG() {
        return AG;
    }

    public void setAG(Long aG) {
        AG = aG;
        try {
            if (aG < 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            logger_debug.error(e.getMessage(), e);
        }
    }

    public Long getAS() {
        return AS;
    }

    public void setAS(Long aS) {
        AS = aS;
    }

    public Integer getLQ() {
        return LQ;
    }

    public void setLQ(Integer lQ) {
        LQ = lQ;
    }

    public boolean isBanned() {
        return Banned;
    }

    public void setBanned(boolean banned) {
        Banned = banned;
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

    public int getCMsg() {
        return CMsg;
    }

    public void setCMsg(int cMsg) {
        CMsg = cMsg;
    }

    public Integer getCPromot() {
        return CPromot;
    }

    public void setCPromot(Integer cPromot) {
        CPromot = cPromot;
    }

    public List<PPromote> getPPromot() {
        return PPromot;
    }

    public void setPPromot(List<PPromote> pPromot) {
        PPromot = pPromot;
    }

    public List<MPromote> getMPromot() {
        return MPromot;
    }

    public void setMPromot(List<MPromote> mPromot) {
        MPromot = mPromot;
    }

    public Integer getTPromot() {
        return TPromot;
    }

    public void setTPromot(Integer tPromot) {
        TPromot = tPromot;
    }

    public Long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(Long createTime) {
        CreateTime = createTime;
    }

    public Long getLastLogin() {
        return LastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        LastLogin = lastLogin;
    }

    /**
     * Get the value of From
     *
     * @return the value of From
     */
    public String getFrom() {
        return From;
    }

    /**
     * Set the value of From
     *
     * @param From new value of From
     */
    public void setFrom(String From) {
        this.From = From;
    }

    /**
     * Get the value of gameid
     *
     * @return the value of gameid
     */
    public short getGameid() {
        return gameid;
    }

    /**
     * Set the value of gameid
     *
     * @param signid new value of gameid
     */
    public void setSignid(String signid) {
        this.Signid = signid;
    }

    /**
     * Get the value of gameid
     *
     * @return the value of gameid
     */
    public String getSignid() {
        return Signid;
    }

    /**
     * Set the value of gameid
     *
     * @param gameid new value of gameid
     */
    public void setGameid(short gameid) {
        this.gameid = gameid;
    }

    /**
     * Get the value of RoomId
     *
     * @return the value of RoomId
     */
    public short getRoomId() {
        return RoomId;
    }

    /**
     * Set the value of RoomId
     *
     * @param RoomId new value of RoomId
     */
    public void setRoomId(short RoomId) {
        this.RoomId = RoomId;
    }

    /**
     * Get the value of LevelId
     *
     * @return the value of LevelId
     */
    public short getLevelId() {
        return LevelId;
    }

    /**
     * Set the value of LevelId
     *
     * @param LevelId new value of LevelId
     */
    public void setLevelId(short LevelId) {
        this.LevelId = LevelId;
    }

    /**
     * Get the value of TableId
     *
     * @return the value of TableId
     */
    public int getTableId() {
        return TableId;
    }

    /**
     * Set the value of TableId
     *
     * @param TableId new value of TableId
     */
    public void setTableId(int TableId) {
        this.TableId = TableId;
    }

    /**
     * Get the value of Pid
     *
     * @return the value of Pid
     */
    public int getPid() {
        return Pid;
    }

    /**
     * Set the value of Pid
     *
     * @param Pid new value of Pid
     */
    public void setPid(int Pid) {
        this.Pid = Pid;
    }

    /**
     * Get the value of userid
     *
     * @return the value of userid
     */
    public Integer getUserid() {
        return Userid;
    }

    /**
     * Set the value of userid
     *
     * @param userid new value of userid
     */
    public void setUserid(Integer userid) {
        this.Userid = userid;
    }

    /**
     * Get the value of tinyurl
     *
     * @return the value of tinyurl
     */
    public String getTinyurl() {
        return Tinyurl;
    }

    /**
     * Set the value of tinyurl
     *
     * @param tinyurl new value of tinyurl
     */
    public void setTinyurl(String tinyurl) {
        this.Tinyurl = tinyurl;
    }

    /**
     * Get the value of username
     *
     * @return the value of username
     */
    public String getUsername() {
        return Username;
    }

    /**
     * Set the value of username
     *
     * @param username new value of username
     */
    public void setUsername(String username) {
        this.Username = username;
    }

    public String getUsernameOld() {
        return UsernameOld;
    }

    public void setUsernameOld(String usernameOld) {
        UsernameOld = usernameOld;
    }

    public boolean isAutoTopOff() {
        return AutoTopOff;
    }

    public void setAutoTopOff(boolean autoTopOff) {
        AutoTopOff = autoTopOff;
    }

    public boolean isAutoFill() {
        return AutoFill;
    }

    public void setAutoFill(boolean autoFill) {
        AutoFill = autoFill;
    }

    public Long getAGBuyIn() {
        return AGBuyIn;
    }

    public void setAGBuyIn(Long aGBuyIn) {
        AGBuyIn = aGBuyIn;
    }

    public boolean isOverComeMinVip() {
        return this.VIP >= Integer.parseInt(LoadConfigProperties.getConfig("min_vip_hotnew"));
    }

}
