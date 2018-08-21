package com.athena.services.chat;

import java.io.Serializable;

public class ChatObject implements Serializable {


//	{"evt":"16","G":8008,"T":2,"D":"chiu","N":"quangqn#05"}
//	{"evt":"16","T":4,"D":"di dau v","N":"chian1998#02","NN":"kim_trong"}
//	{"T":4,"D":"[aye]","N":"dinh1233444#01","evt":"16","NN":"[loanbong91]"}
//	{"G":8013,"T":2,"D":"game","evt":"16","N":"longhack68#03"}
//	{"NN":"qaeqweq","D":"wa choi m","evt":"16","N":"hieu_ac#04","T":4}
//	{"D":"choi dui the thi khong doi duoc la sao. chuyen thi cung khong duoc la sao","T":2,"N":"dldcanh#03","G":8013,"evt":"16"}
	//{"D":"n","evt":"16","N":"jjjjhhd#02","T":1}
	//{"evt":"16","T":1,"N":"ducn92#02","D":"    "}
	//{"G":8010,"evt":"16","T":2,"N":"thangdang1983#01","D":"c"}
	//{"G":8007,"evt":"16","T":2,"N":"ahieu3565.gm#01","D":"e Có s"}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4409169076541647857L;
	private int GameID;
	private String Evt;
	private int Type; 
	private String Name;
	private String Data;
	private int Vip;
	private String NextName;
	private long Ag;
	private short Avatar;
	private int ID;// userid
	private String Status;
	private long FaceID;
	
	public int getGameID() {
		return GameID;
	}
	public void setGameID(int gameID) {
		GameID = gameID;
	}
	public String getEvt() {
		return Evt;
	}
	public void setEvt(String evt) {
		Evt = evt;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public int getType() {
		return Type;
	}
	public void setType(int type) {
		Type = type;
	}
	public String getData() {
		return Data;
	}
	public void setData(String data) {
		Data = data;
	}
	public int getVip() {
		return Vip;
	}
	public void setVip(int vip) {
		Vip = vip;
	}
	public String getNextName() {
		return NextName;
	}
	public void setNextName(String nextName) {
		NextName = nextName;
	}
	public long getAg() {
		return Ag;
	}
	public void setAg(long ag) {
		Ag = ag;
	}
	public short getAvatar() {
		return Avatar;
	}
	public void setAvatar(short avatar) {
		Avatar = avatar;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}
	public long getFaceID() {
		return FaceID;
	}
	public void setFaceID(long faceID) {
		FaceID = faceID;
	}


}
