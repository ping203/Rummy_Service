package com.athena.services.room;

import java.util.ArrayList;
import java.util.HashMap;
import com.athena.services.utils.ActionUtils;
import com.google.gson.JsonObject;

public class Room {
	public static final int MAX_TOP_PLAYER = 24;
	private HashMap<Integer,RoomPlayer> mapPlayer = new HashMap<Integer,RoomPlayer>();
	private ArrayList<RoomPlayer> topPlayer = new ArrayList<RoomPlayer>();
	private long minChip = 0 ;
	
	public HashMap<Integer,RoomPlayer> getMapPlayer() {
		return mapPlayer;
	}

	public void setMapPlayer(HashMap<Integer,RoomPlayer> mapPlayer) {
		this.mapPlayer = mapPlayer;
	}

	public String getListTopPlayers(JsonObject json) {
		try{
			synchronized (topPlayer) {
				return ActionUtils.gson.toJson(topPlayer);
			}		
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public ArrayList<RoomPlayer> getTopPlayer() {
		return topPlayer;
	}

	public void setTopPlayer(ArrayList<RoomPlayer> topPlayer, String key) {
		synchronized (this.topPlayer){
			this.topPlayer.clear();
			short idx= 0;
			for(int i= 0; i< topPlayer.size(); i++){
				idx++;
				if(idx > MAX_TOP_PLAYER) break;
				this.topPlayer.add(topPlayer.get(i));
			}
			if(this.topPlayer.size() > 0)
				minChip = this.topPlayer.get(this.topPlayer.size()-1).getChip();
		}
	}

	public String getListTopPlayersByType(JsonObject json) {
		try{
			synchronized (topPlayer) {
				int type = json.get("type").getAsInt();
				ArrayList<RoomPlayer> ret = new ArrayList<RoomPlayer>();
				for(RoomPlayer player : topPlayer)
					if(player.getUsertype() == type)
						ret.add(player);
				return ActionUtils.gson.toJson(ret);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public long getMinChip() {
		return minChip;
	}

	public void setMinChip(long minChip) {
		this.minChip = minChip;
	}
}
