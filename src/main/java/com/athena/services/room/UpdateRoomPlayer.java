package com.athena.services.room;

import com.athena.log.LoggerKey;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.athena.services.utils.ActionUtils;

public class UpdateRoomPlayer implements Runnable {
        
    public static Logger logger = Logger.getLogger(LoggerKey.UPDATE_ROOM);

	@Override
	public void run() {
		String keyCheck = "key";
		ArrayList<RoomPlayer> listP = null;
		try{
			for(String key : RoomHandler.mapGameRoom.keySet()){
				keyCheck = key;
				synchronized (RoomHandler.mapGameRoom.get(key).getMapPlayer()) {
					listP =  new ArrayList<RoomPlayer>();
					ArrayList<RoomPlayer> listCheck =  new ArrayList<RoomPlayer>();
					for(Integer i : RoomHandler.mapGameRoom.get(key).getMapPlayer().keySet()){	
						RoomPlayer p = RoomHandler.mapGameRoom.get(key).getMapPlayer().get(i);
						if(p.getTableid() > 0){
							if(RoomHandler.mapGameRoom.get(key).getTopPlayer().size() < Room.MAX_TOP_PLAYER || p.getChip() > RoomHandler.mapGameRoom.get(key).getMinChip())
								listP.add(new RoomPlayer(p.getPid(), p.getName(), p.getChip(), p.getVip(), p.getAvatar(), 
											p.getFaceid(), p.getTableid(),p.getUsertype()));
							listCheck.add(new RoomPlayer(p.getPid(), p.getName(), p.getChip(), p.getVip(), p.getAvatar(), 
									p.getFaceid(), p.getTableid(),p.getUsertype()));
						}
					}
//					logger.info("==>UpdateRoomPlayer: "+key+" - "+listP.size()+" - "+listCheck.size()+ " - "+RoomHandler.mapGameRoom.get(key).getMinChip());
					Collections.sort(listP, new RoomPlayerCompare());
					RoomHandler.mapGameRoom.get(key).setTopPlayer(listP,key);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			try{
				System.out.println("==>UpdateRoomPlayerError: "+keyCheck+" - "+ActionUtils.gson.toJson(listP));
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
