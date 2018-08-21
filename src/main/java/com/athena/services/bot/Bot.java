package com.athena.services.bot;

import java.util.List;

import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;

public interface Bot {
	public void setServiceRouter(ServiceRouter router);

	public void getUserGame(int minAG, short gameId, int tableId, int diamond);

	public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU);

	public Long UpdateBotMarkChessByName(int uid, long mark);

	public void BotCreateTable(int gameid, int mark, int type); // only TeenPatti_8040

	public void BotCreateTable(int gameid, int mark);

	public void BotCreateTableForRoom(int gameid, int mark, int roomID); // only Binh_8004, Poker9k_8025

	public UserInfo botLogin(int gameid);

	public void processBotDisconnect(int playerId);

	public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark);

	public Long UpdateBotMarkByName(int uid, int source, String name, long mark);

	public UserInfo updateBotOnline(int pid, short isOnline);

	public Long UpdateBotMarkByUID(int uid, long mark);

	public String processGetBotInfoByPid(int pid, int tid);

	public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot();

	public List<UserInfo> getListBot();

	public void botRejectJoinTable(int pid);

}
