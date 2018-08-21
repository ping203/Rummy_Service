/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

/**
 *
 * @author UserXP
 */
public class UserTrans {
    
	private Integer Userid;
    private String Username;
    private String Tinyurl;
    private Long AG;
    //private Long AS;
	private Integer LQ;
    private short VIP;
    private int MVip ; //Chi tiet diem Vip
	private short G;
	private Integer CE; //ChessElo
    private int CMsg;
    private Integer TPromot;
    private Integer AGVay;
    private String sIP;
    private short Pass;
    private short X;
    private short O; //Operatorid
    private boolean R; //Register
    private int PD ; //Promotion Daily
    private short OD ; //Online Daily
    private short MT ; //Message Type
    private short A ;
    private String M ; //Mobile
    private short E ; //Event
    private short LR; //LuckyRotation
    private short PL ; //Pass Lang quat
    private short C ; //Duoc phep doi the khong - 3 la duoc phep
    private int NM ;//Number Mail
    private String DN ; //Default Name
    private long D ; //Diamond
    private int GC ; //GetConfig
    private long WA ; //Win Accumulation
    private String ListDP ;
    private int NewAccFBInDevice = 0;
    private long chipbank;
    private int vippoint;
    private int markLevel;

	public int getMarkLevel() {
		return markLevel;
	}

	public void setMarkLevel(int markLevel) {
		this.markLevel = markLevel;
	}

	public String getListDP() {
		return ListDP;
	}

	public void setListDP(String listDP) {
		ListDP = listDP;
	}

	public long getWA() {
		return WA;
	}

	public void setWA(long wA) {
		WA = wA;
	}

	public int getGC() {
		return GC;
	}

	public void setGC(int gC) {
		GC = gC;
	}

	public long getD() {
		return D;
	}

	public void setD(long d) {
		D = d;
	}

	public String getDN() {
		return DN;
	}

	public void setDN(String dN) {
		DN = dN;
	}

	public int getNM() {
		return NM;
	}

	public void setNM(int nM) {
		NM = nM;
	}

	public int getMVip() {
		return MVip;
	}

	public void setMVip(int mVip) {
		MVip = mVip;
	}

	public short getC() {
		return C;
	}

	public void setC(short c) {
		C = c;
	}

	public short getPL() {
		return PL;
	}

	public void setPL(short pL) {
		PL = pL;
	}

	public short getLR() {
		return LR;
	}

	public void setLR(short lR) {
		LR = lR;
	}

	public short getE() {
		return E;
	}

	public void setE(short e) {
		E = e;
	}

	public String getM() {
		return M;
	}

	public void setM(String m) {
		M = m;
	}
	
    public short getA() {
		return A;
	}

	public void setA(short a) {
		A = a;
	}
    public short getMT() {
		return MT;
	}

	public void setMT(short mT) {
		MT = mT;
	}

	public int getPD() {
		return PD;
	}

	public void setPD(int pD) {
		PD = pD;
	}

	public short getOD() {
		return OD;
	}

	public void setOD(short oD) {
		OD = oD;
	}

	private String UsernameLQ ;
    
    public String getUsernameLQ() {
		return UsernameLQ;
	}

	public void setUsernameLQ(String usernameLQ) {
		UsernameLQ = usernameLQ;
	}

	public boolean getR() {
		return R;
	}

	public void setR(boolean r) {
		R = r;
	}

	public short getO() {
		return O;
	}

	public void setO(short o) {
		O = o;
	}

	public Integer getCE() {
		return CE;
	}

	public void setCE(Integer ce) {
		CE = ce;
	}
    
	public short getX() {
		return X;
	}

	public void setX(short x) {
		X = x;
	}

	public short getPass() {
		return Pass;
	}

	public void setPass(short pass) {
		Pass = pass;
	}

	public String getsIP() {
		return sIP;
	}

	public void setsIP(String sIP) {
		this.sIP = sIP;
	}

	public Integer getAGVay() {
		return AGVay;
	}

	public void setAGVay(Integer aGVay) {
		AGVay = aGVay;
	}

	public short getVIP() {
		return VIP;
	}

	public void setVIP(short vIP) {
		VIP = vIP;
	}
	
	public short getG() {
		return G;
	}

	public void setG(short g) {
		G = g;
	}

	public int getCMsg() {
		return CMsg;
	}

	public void setCMsg(int cMsg) {
		CMsg = cMsg;
	}

	public Integer getTPromot() {
		return TPromot;
	}

	public void setTPromot(Integer tPromot) {
		TPromot = tPromot;
	}

	public Long getAG() {
		return AG;
	}

	public void setAG(Long aG) {
		AG = aG;
	}
	
//	public Long getAS() {
//		return AS;
//	}
//
//	public void setAS(Long aS) {
//		AS = aS;
//	}

	public Integer getLQ() {
		return LQ;
	}

	public void setLQ(Integer lQ) {
		LQ = lQ;
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

	public int getNewAccFBInDevice() {
		return NewAccFBInDevice;
	}

	public void setNewAccFBInDevice(int newAccFBInDevice) {
		NewAccFBInDevice = newAccFBInDevice;
	}

	public long getChipbank() {
		return chipbank;
	}

	public void setChipbank(long chipbank) {
		this.chipbank = chipbank;
	}

	public int getVippoint() {
		return vippoint;
	}

	public void setVippoint(int vippoint) {
		this.vippoint = vippoint;
	}

}
