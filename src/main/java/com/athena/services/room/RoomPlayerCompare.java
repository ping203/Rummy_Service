package com.athena.services.room;

import java.util.Comparator;

public class RoomPlayerCompare implements Comparator<RoomPlayer> {
	@Override
	public int compare(RoomPlayer p1, RoomPlayer p2) {
		if(p1.getChip() > p2.getChip())
			return -1;
		else if(p1.getChip() < p2.getChip())
			return 1;
		else return 0;
	}

}
