package com.athena.services.room;

import java.util.HashMap;

public class MarkTableInfo {
	private HashMap<String, Integer> mapMark = new HashMap<String, Integer>();
	
	public void updatePlayerMark(int gameid, int mark, int value){
		try{
			synchronized (mapMark) {
				String key = gameid+"_"+mark;
				if(mapMark.containsKey(key))
					mapMark.put(key,mapMark.get(key)+value);
				else
					mapMark.put(key,value);
				if(mapMark.get(key) < 0)
					mapMark.put(key,0);
			}		
		}catch (Exception e) {
//			System.out.println("==>updatePlayerMark: "+gameid+" - "+mark+" - "+value);
			e.printStackTrace();
		}
	}

	public int getCurrentPlayer(int mark, int gameId) {
		try{
			String key = gameId+"_"+mark;
			int count = 0;
			if(mapMark.containsKey(key))
				count = mapMark.get(key);
			/*else
				System.out.println("==>getCurrentPlayer: "+key);*/
			if(count < 0) count = 0;
			return count;
		}catch (Exception e) {
//			System.out.println("==>getCurrentPlayer: "+gameId+" - "+mark);
			e.printStackTrace();
		}
		return 0;
	}
	
	public int getCurrentPlayer(long mark, int gameId) {
		try{
			String key = gameId+"_"+mark;
			int count = 0;
			if(mapMark.containsKey(key))
				count = mapMark.get(key);
			/*else
				System.out.println("==>getCurrentPlayer: "+key);*/
			if(count < 0) count = 0;
			return count;
		}catch (Exception e) {
//			System.out.println("==>getCurrentPlayer: "+gameId+" - "+mark);
			e.printStackTrace();
		}
		return 0;
	}
	
}
