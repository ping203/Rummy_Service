/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

/**
 *
 * @author UserXP
 */
public class UserTrans2 {
	private Integer Userid;
    private String Username;
    private String Tinyurl;
    private Long AG;
	private Integer LQ;
    private short VIP;
    private int MVip ; //Chi tiet diem Vip
	private int markLevel;

    private int PD ; //Promotion Daily
    private short OD ; //Online Daily
    private short A ;
    private int NM ;//Number Mail
    private String ListDP ;
    private int NewAccFBInDevice = 0;
    private long chipbank;
    private int gameid = 0;
    private int NumFriendMail;
    private int gameNo;
    private boolean isShowAds;

	public int getGameNo() {
		return gameNo;
	}

	public void setGameNo(int gameNo) {
		this.gameNo = gameNo;
	}

	public boolean isShowAds() {
		return isShowAds;
	}

	public void setShowAds(boolean showAds) {
		isShowAds = showAds;
	}

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
	
    public short getA() {
		return A;
	}

	public void setA(short a) {
		A = a;
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

	public short getVIP() {
		return VIP;
	}

	public void setVIP(short vIP) {
		VIP = vIP;
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

	public int getGameid() {
		return gameid;
	}

	public void setGameid(int gameid) {
		this.gameid = gameid;
	}

	public int getNumFriendMail() {
		return NumFriendMail;
	}

	public void setNumFriendMail(int numFriendMail) {
		NumFriendMail = numFriendMail;
	}
}
