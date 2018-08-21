package com.athena.services.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.athena.services.friends.MessageObj;

public class JackpotWinList implements Serializable  {
	private List<JackpotWin> lswin ;
	public JackpotWinList() {
		lswin = new ArrayList<JackpotWin>() ;
	}
	public List<JackpotWin> getLswin() {
		return lswin;
	}

	public void setLswin(List<JackpotWin> lswin) {
		this.lswin = lswin;
	}
	
	
}
