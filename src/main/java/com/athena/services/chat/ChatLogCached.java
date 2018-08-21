package com.athena.services.chat;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatLogCached implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1632792545031471687L;
	private ArrayList<ChatObject> historyChat;
	
	public ChatLogCached(){
		historyChat = new ArrayList<ChatObject>();
	}
	
	public ArrayList<ChatObject> getHistoryChat() {
		return historyChat;
	}

	public void setHistoryChat(ArrayList<ChatObject> historyChat) {
		this.historyChat = historyChat;
	}
}
