package com.athena.services.bot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.GAMEID;
import com.athena.services.vo.UserInfo;
import com.athena.services.bot.constants.myanBohn.*;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;

public class BohnBotHandler implements Bot{
	//public final int BOT_MARK_10 = 10;
	public final int BOT_MARK_100 = 100;
	public final int BOT_MARK_500 = 500;
	public final int BOT_MARK_1K = 1000;
	public final int BOT_MARK_5K = 5000;
	public final int BOT_MARK_10K = 10000;
	public final int BOT_MARK_50K = 50000;
	public final int BOT_MARK_100K = 100000;
	public final int BOT_MARK_500K = 500000;
	public final int BOT_MARK_1M = 1000000;
	public final int BOT_MARK_5M = 5000000;
	public final int BOT_MARK_10M = 10000000;
	public final int BOT_MARK_50M = 50000000;

	// Setup Danh sach muc cuoc ban
	public final int BOT_TYPE100 = 11;
	public final int BOT_TYPE500 = 12;
	public final int BOT_TYPE1K = 13;
	public final int BOT_TYPE5K = 14;
	public final int BOT_TYPE10K = 15;
	public final int BOT_TYPE50K = 16;
	public final int BOT_TYPE100K = 17;
	public final int BOT_TYPE500K = 18;
	public final int BOT_TYPE1M = 19;
	public final int BOT_TYPE5M = 20;
	public final int BOT_TYPE10M = 21;
	public final int BOT_TYPE50M = 22;

	// public final int BOT100000 = 25;//20;
	public final int MIN_AG_RATE = 10;
	public final int MAX_AG_RATE = 200;
	public final int MAX_ADD_AG = 50;

	public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotBohn = ConcurrentLinkedHashMap
			.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
	public List<UserInfo> listBotBohn = new ArrayList<UserInfo>();
	public ServiceRouter serviceRouter;
	public List<Integer> listPid = new ArrayList<Integer>();

	public BohnBotHandler(int source) {
		getListBot(source);
	}
	
    @Override
    public void botRejectJoinTable(int pid) {
        throw new UnsupportedOperationException();
    }

	public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
		if (Diamond != 0)
			return;
		synchronized (listBotBohn) {
			try {
				short type = BOT_TYPE100;
				if (minAG >= BOT_MARK_50M) {
					type = BOT_TYPE50M;
				} else if(minAG >= BOT_MARK_10M) {
					type = BOT_TYPE10M;
				} else if(minAG >= BOT_MARK_5M) {
					type = BOT_TYPE5M;
				} else if(minAG >= BOT_MARK_1M) {
					type = BOT_TYPE1M;
				} else if(minAG >= BOT_MARK_500K) {
					type = BOT_TYPE500K;
				} else if (minAG >= BOT_MARK_100K) {
					type = BOT_TYPE100K;
				} else if (minAG >= BOT_MARK_50K) {
					type = BOT_TYPE50K;
				} else if (minAG >= BOT_MARK_10K) {
					type = BOT_TYPE10K;
				} else if (minAG >= BOT_MARK_5K) {
					type = BOT_TYPE5K;
				} else if (minAG >= BOT_MARK_1K) {
					type = BOT_TYPE1K;
				} else if (minAG >= BOT_MARK_500) {
					type = BOT_TYPE500;
				} else if (minAG >= BOT_MARK_100) {
					type = BOT_TYPE100;
				}

				int countType = 0;
				for (int i = 0; i < listBotBohn.size(); i++) {
					if (listBotBohn.get(i).getGameid() == gameId && listBotBohn.get(i).getUsertype() == type)
						countType++;
				}
				//System.out.println("Bohn count type " + type + " game id " + gameId + " have left " + countType);
				Logger.getLogger("bohn_debug").info("Get bot for mark: " + minAG + ";AG required:" + BohnConstant.getBoundGold(minAG) + ";Available:" + countType);
				for (int i = 0; i < listBotBohn.size(); i++) {
					//Logger.getLogger("bohn_debug").info(listBotBohn.get(i).getPid() + ";" + listBotBohn.get(i).getGameid() + ";" + listBotBohn.get(i).getTableId() + ";" + listBotBohn.get(i).getUsertype() + ";" + listBotBohn.get(i).getAG());
					if (listBotBohn.get(i).getGameid() == gameId // client bot connected
							&& listBotBohn.get(i).getUsertype() == type) { // check type + ag
						UserInfo u = listBotBohn.get(i);
						int addAG = 0;
						if (u.getTableId() != 0)
							continue;

						if (u.getAG() < BohnConstant.getBoundGold(minAG) && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
							// Mức cược bàn Nhóm mức cược Số bot tiếp khách MIN MAX Random Vip
							// 50 1 60 500 10,000 0->2
							// 200 1 50 2,000 40,000 1->3
							// 1,000 2 50 10,000 200,000 1->3
							// 5,000 2 40 50,000 1,000,000 2->4
							// 20,000 3 30 200,000 4,000,000 2->4
							// 50,000 3 25 500,000 10,000,000 2->5
							// 100,000 3 20 1,000,000 20,000,000 3->6

							// if(type > 23 ) u.setCPromot(9);
							u.setCPromot(u.getCPromot().intValue() + 1);
							addAG = getAGADD(type);
						}

						if (addAG > 0) {
							int abDB = u.getAG().intValue();
							u.setAG((long) addAG);
							ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.MYANMAR_BOHN);
						}
						if (u.getAG() < BohnConstant.getBoundGold(minAG)) {
							continue;
						}
						u.setIsOnline(gameId);
						dicBotBohn.put(u.getUserid(), u);
						JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
						serviceRouter.dispatchToGame(gameId, action);
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int randomBetween2Number(int lowerBound, int upperBound) {
		Random random = new Random();
		int randomNumber = random.nextInt(upperBound - lowerBound) + lowerBound;
		return randomNumber;
	}

	public String getTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}

	public int getAGADD(int type) {
		int addAG = 0;
		switch (type) {
		case BOT_TYPE50M:
		case BOT_TYPE10M:
		case BOT_TYPE5M:
		case BOT_TYPE1M:
			addAG = randomBetween2Number(1200000000, 2000000000);
			break;
		case BOT_TYPE500K:
			addAG = randomBetween2Number(50000000, 100000000);
			break;
		case BOT_TYPE100K:
			addAG = randomBetween2Number(20000000, 100000000);
			break;
		case BOT_TYPE50K:
			addAG = randomBetween2Number(20000000, 40000000);
			break;
		case BOT_TYPE10K:
			addAG = randomBetween2Number(2000000, 8000000);
			break;
		case BOT_TYPE5K:
		case BOT_TYPE1K:
			addAG = randomBetween2Number(800000, 4000000);
			break;
		case BOT_TYPE500:
			addAG = randomBetween2Number(160000, 800000);
			break;
		case BOT_TYPE100:
			addAG = randomBetween2Number(80000, 200000);
			break;
		default:
			break;
		}
		return addAG;
	}

	public int getVip(short type) {
		int addAG = 1;
		switch (type) {
		case BOT_TYPE50M:
		case BOT_TYPE10M:
			addAG = (new Random()).nextInt(7) + 2;
			break;
		case BOT_TYPE5M:
		case BOT_TYPE1M:
			addAG = (new Random()).nextInt(6) + 2;
		case BOT_TYPE500K:
		case BOT_TYPE100K:
			addAG = (new Random()).nextInt(5) + 2;
			break;
		case BOT_TYPE50K:
			addAG = (new Random()).nextInt(4) + 2;
			break;
		case BOT_TYPE10K:
		case BOT_TYPE5K:
		case BOT_TYPE1K:
		case BOT_TYPE500:
		//case BOT_TYPE100:
		default:
			break;
		}
		return addAG;
	}

	public void getListBot(int source) {
		SqlService instance = SqlService.getInstanceBySource(source);
		Connection conn = instance.getDbConnection();
		try {
			CallableStatement cs = conn.prepareCall("{call GameGetListBot_New(?) }");
			cs.setInt("Gameid", GAMEID.MYANMAR_BOHN);
			ResultSet rs = cs.executeQuery();
			short type = 0;
			listBotBohn.clear();
			listPid.clear();
			int bot100 = 50;
			int bot500 = 90;
			int bot1k = 130;
			int bot5k = 165;
			int bot10k = 200;
			int bot50k = 225;
			int bot100k = 245;
			int bot500k = 260;
			int bot1m = 275;
			int bot5m = 285;
			int bot10m = 295;
			int bot50m = 300;
			//Logger.getLogger("log_bohn_bot").info("Testing Getting ListBot Bohn, source: " + source);
			
			while (rs.next()) {
				UserInfo userTemp = new UserInfo();
				//Logger.getLogger("log_bohn_bot").info("==>Name:" + rs.getString("Username")) ;

				// long ag = rs.getLong("AG");
				userTemp.setUserid(rs.getInt("ID") + ServerDefined.userMap.get(source));
				userTemp.setFacebookid(Long.parseLong(rs.getString("FacebookId")));
				userTemp.setDeviceId(rs.getString("DeviceID"));
				// userTemp.setAG(ag);
				// userTemp.setVIP((short)rs.getInt("VIP"));
				userTemp.setIsOnline((short) rs.getInt("IsOnline"));
				userTemp.setBanned(rs.getBoolean("IsBanned"));
				userTemp.setAvatar((short) rs.getInt("Avatar"));
				userTemp.setOnlineDaily((short) rs.getInt("OnlineDaily"));
				userTemp.setPromotionDaily(rs.getInt("PromotionDaily"));
				userTemp.setCreateTime(rs.getDate("CreateTime").getTime());
				userTemp.setLastLogin(rs.getDate("LastLogin").getTime());
				userTemp.setUsername(rs.getString("Username"));
				userTemp.setUsernameLQ(rs.getString("UsernameLQ"));
				userTemp.setIdolName(rs.getString("IdolName"));
				userTemp.setSource((short) source);
				if (userTemp.getUsernameLQ().length() > 0)
					userTemp.setUsername(userTemp.getUsernameLQ());

				type++;
				if (type > bot10m) {
					userTemp.setUsertype((short) BOT_TYPE50M);
				} else if(type <= bot10m && type > bot5m) {
					userTemp.setUsertype((short) BOT_TYPE10M);
				} else if(type <= bot5m && type > bot1m) {
					userTemp.setUsertype((short) BOT_TYPE5M);
				} else if(type <= bot1m && type > bot500k) {
					userTemp.setUsertype((short) BOT_TYPE1M);
				} else if(type <= bot500k && type > bot100k) {
					userTemp.setUsertype((short) BOT_TYPE500K);
				} else if(type <= bot100k && type > bot50k) {
					userTemp.setUsertype((short) BOT_TYPE100K);
				} else if (type <= bot50k && type > bot10k) {
					userTemp.setUsertype((short) BOT_TYPE50K);
				} else if (type <= bot10k && type > bot5k) {
					userTemp.setUsertype((short) BOT_TYPE10K);
				} else if (type <= bot5k && type > bot1k) {
					userTemp.setUsertype((short) BOT_TYPE5K);
				} else if (type <= bot1k && type > bot500) {
					userTemp.setUsertype((short) BOT_TYPE1K);
				} else if (type <= bot500 && type > bot100) {
					userTemp.setUsertype((short) BOT_TYPE500);
				} else {
					userTemp.setUsertype((short) BOT_TYPE100);
				} 
				// kiem tra them tien cho bot
				int agDB = rs.getInt("AG");
				int ag = getAGADD(userTemp.getUsertype());
				userTemp.setAG((long) ag);
				userTemp.setVIP((short) getVip(userTemp.getUsertype()));
				ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.MYANMAR_BOHN);
				listBotBohn.add(userTemp);
				listPid.add(userTemp.getPid());
			}
			rs.close();
			cs.close();
			Logger.getLogger("log_bohn_bot").info("==>SizeBohnBot:" + listBotBohn.size()) ;
		} catch (SQLException ex) {
			Logger.getLogger("log_bohn_bot").info("==>Error==>GetListBot:" + ex.getMessage());
		} finally {
			instance.releaseDbConnection(conn);
		}
	}

	public UserInfo updateBotOnline(int pid, short isOnline) {
		try {
			synchronized (listBotBohn) {
				if (isOnline == 0) {

			/*		Logger.getLogger("log_bohn_bot").info("==>BotHandler==>updateBotOnline:before " +
				 ServiceImpl.listBot.size());*/
					if (dicBotBohn.containsKey(pid)) {
						dicBotBohn.get(pid).setIsOnline(isOnline);
						dicBotBohn.get(pid).setTableId(0);
						if (dicBotBohn.get(pid).isDisconnect())
							dicBotBohn.get(pid).setGameid((short) 0);
							dicBotBohn.get(pid).setRoomId((short) 0);
						UserInfo u = dicBotBohn.get(pid);
						if(!listPid.contains(u.getPid())) {
							listBotBohn.add(u);
							listPid.add(u.getPid());
						}
					}
				} else {
					dicBotBohn.get(pid).setIsOnline(isOnline);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dicBotBohn.get(pid);
	}

	public Long UpdateBotBohnMark(int uid, long mark) {
		synchronized (dicBotBohn) {
			System.out.println("dicBotBohn.containsKey(uid) " + dicBotBohn.containsKey(uid));
			if (dicBotBohn.containsKey(uid)) {
				try {
					System.out
							.println("dicBotBohn.get(uid).getAG() " + dicBotBohn.get(uid).getAG() + " mark " + mark);
					long ag = dicBotBohn.get(uid).getAG() + mark;
					if (ag < 0) {
						ag = 0;
					}
					System.out.println("ag " + ag);
					dicBotBohn.get(uid).setAG(ag);
					UserInfoCmd cmd = new UserInfoCmd("updateAG", dicBotBohn.get(uid).getSource(),
							uid - ServerDefined.userMap.get((int) dicBotBohn.get(uid).getSource()), mark);
					QueueManager.getInstance(UserController.queuename).put(cmd);
					System.out.println("Update bot ag bohn: " + dicBotBohn.get(uid).getAG());
					return dicBotBohn.get(uid).getAG();
				} catch (Exception e) {
					e.printStackTrace();
					return 0l;
				}
			} else {
			}
		}
		return 0l;
	}

	public void BotCreateTable(int gameid, int mark) {
		// Auto-generated method stub
		try {
			// System.out.println("==>BotHandler==>BotCreateTable "+ gameid+" - " + mark);
			short type = 11;
			int roomID = 1;
			if (mark >= 50000000) // bàn 50m
			{
				type = 22; roomID = 4;
			} else if(mark >= 10000000) { // ban 10m
				type = 21; roomID = 4;
			} else if(mark >= 5000000) { // ban 5m
				type = 20; roomID = 4;
			} else if(mark >= 1000000) { // ban 1m
				type = 19; roomID = 3;
			} else if(mark >= 500000) {	// ban 500k
				type = 18; roomID = 3;
			} else if(mark >= 100000) {	// ban 100k
				type = 17; roomID = 2;
			} else if (mark >= 50000) { // ban 50k
				type = 16; roomID = 2;
			} else if (mark >= 10000) { // bàn 10k
				type = 15; roomID = 2;
			} else if (mark >= 5000) { // bàn 5k
				type = 14; roomID = 1;
			} else if (mark >= 1000) { // bàn 1k
				type = 13; roomID = 1;
			} else if (mark >= 500) { // bàn 200
				type = 12; roomID = 1;
			}			
			//Logger.getLogger("bohn_debug").info("==>BotHandler==>BotCreateTable");
			// "+ActionUtils.gson.toJson(listBotNew));
			boolean check = true;
			//Logger.getLogger("bohn_debug").info("==>BotHandler==>BotCreateTable Size of listbot available:" + listBotBohn.size());
			for (int i = 0; i < listBotBohn.size(); i++) {
				//Logger.getLogger("bohn_debug").info("==>BotHandler==>Type:" + type + ";Room:" + listBotBohn.get(i).getRoomId() + ";Table:" + listBotBohn.get(i).getTableId());
				if (listBotBohn.get(i).getGameid() == gameid && listBotBohn.get(i).getUsertype() == type
						&& listBotBohn.get(i).getRoomId() == 0) {
					check = false;
					UserInfo u = listBotBohn.get(i);
					// System.out.println("==>BotHandler==>BotCreateTable :
					// "+ActionUtils.gson.toJson(listBotNew.get(i))+"\n");
					if (u.getAG() < BohnConstant.getBoundGold(mark) && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
						// u.setCPromot(9);
						u.setCPromot(u.getCPromot().intValue() + 1);
						int addAG = getAGADD(type);
						if (addAG > 0) {
							u.setAG(u.getAG() + addAG);
							UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(),
									u.getUserid() - ServerDefined.userMap.get((int) u.getSource()), addAG);
							QueueManager.getInstance(UserController.queuename).put(cmd);
							Logger.getLogger("Bot_Details")
									.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
							ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.MYANMAR_BOHN);
						}
					} else if (u.getAG() < BohnConstant.getBoundGold(mark)) {
						Logger.getLogger("bohn_debug").info("==>BotHandler==>Not enough AG");
						continue; // tim bot tiep
					}
					listBotBohn.get(i).setRoomId((short)roomID);
					u.setIsOnline((short) gameid);
					// listBotNew.remove(i);
					dicBotBohn.put(u.getUserid(), u);
					Logger.getLogger("bohn_debug").info("==>BotHandler==>BotCreateTable : OK");

					JsonObject send = new JsonObject();
					send.addProperty("evt", "botCreateTable");
					send.addProperty("pid", u.getPid());
					send.addProperty("M", mark);
					ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
					serviceRouter.dispatchToGameActivator(gameid, request);
					break;
				}
			}
			if (check) {
				for (int i = 0; i < listBotBohn.size(); i++) {
					if (listBotBohn.get(i).getUsertype() == type)
						Logger.getLogger("Bot_Details")
								.info("==>BotHandler==>HadBotCreateTable: failed listBotBohn size: "
										+ listBotBohn.size() + " - gameid: " + gameid + " - mark: " + mark);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
		// Auto-generated method stub
		try {
			// Logger.getLogger("Bot_Login").info("==>BotHandler==>BotCreateTable "+
			// gameid+" - " + mark);
			if (mark > 0)
				return;

			short type = 11;
			if (mark >= 50000000) // bàn 50m
			{
				type = 22;
			} else if(mark >= 10000000) { // ban 10m
				type = 21;
			} else if(mark >= 5000000) { // ban 5m
				type = 20;
			} else if(mark >= 1000000) { // ban 1m
				type = 19;
			} else if(mark >= 500000) {	// ban 500k
				type = 18;
			} else if(mark >= 100000) {	// ban 100k
				type = 17;
			} else if (mark >= 50000) { // ban 50k
				type = 16;
			} else if (mark >= 10000) { // bàn 10k
				type = 15;
			} else if (mark >= 5000) { // bàn 5k
				type = 14;
			} else if (mark >= 1000) { // bàn 1k
				type = 13;
			} else if (mark >= 500) { // bàn 200
				type = 12;
			}
			// System.out.println("==>BotHandler==>BotCreateTable :
			// boolean check = true;
			for (int i = 0; i < listBotBohn.size(); i++) {
				if (listBotBohn.get(i).getGameid() == gameid && listBotBohn.get(i).getUsertype() == type
						) {
					// check = false;
					UserInfo u = listBotBohn.get(i);
					if (u.getAG() < BohnConstant.getBoundGold(mark) && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
						u.setCPromot(u.getCPromot().intValue() + 1);
						int addAG = getAGADD(type);
						if (addAG > 0) {

							int abDB = u.getAG().intValue();
							u.setAG(u.getAG() + addAG);
							Logger.getLogger("Bot_Details")
									.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
							ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.MYANMAR_BOHN);
							ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.MYANMAR_BOHN);
						}
					} else if (u.getAG() < BohnConstant.getBoundGold(mark)) {
						continue; // tim bot tiep
					}
					listBotBohn.get(i).setRoomId((short) roomID);
					u.setIsOnline((short) gameid);
					// listBotTeenpatti.remove(i);
					dicBotBohn.put(u.getUserid(), u);
					// System.out.println("==>BotHandler==>BotCreateTable : OK");

					JsonObject send = new JsonObject();
					send.addProperty("evt", "botCreateTable");
					send.addProperty("pid", u.getPid());
					send.addProperty("M", mark);
					ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
					serviceRouter.dispatchToGameActivator(gameid, request);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void checkBot(JsonObject je, UserInfo actionUser) {
		try {
			if (je.has("act")) {
				if (je.get("act").getAsInt() == 0) {
					int size = 0, bot50 = 0, bot200 = 0, bot1k = 0;
					List<UserInfo> lsBot = new ArrayList<UserInfo>();
					for (int i = 0; i < listBotBohn.size(); i++) {
						if (listBotBohn.get(i).getGameid() == 0)
							size++;

						if (je.has("T") && je.get("T").getAsInt() == listBotBohn.get(i).getUsertype())
							lsBot.add(listBotBohn.get(i));
					}
					JsonObject jo = new JsonObject();
					jo.addProperty("evt", "checkBot");
					jo.addProperty("Disconnected", size + " / Size: " + listBotBohn.size());
					jo.addProperty("bot50", bot50);
					jo.addProperty("bot200", bot200);
					jo.addProperty("bot1000", bot1k);

					ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1,
							ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
					serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
				} else if (je.get("act").getAsInt() == 1) {
					JsonObject jo = new JsonObject();
					jo.addProperty("evt", "checkBot");
					jo.addProperty("dic", ActionUtils.gson.toJson(dicBotBohn));
					jo.addProperty("list", ActionUtils.gson.toJson(listBotBohn));
					ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1,
							ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
					serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reloadBot(JsonObject je, UserInfo actionUser) {
		try {
			getListBot(actionUser.getSource());
			int s = 0, t = 0;
			for (int i = 0; i < listBotBohn.size(); i++) {
				if (listBotBohn.get(i).getGameid() == 0)
					s++;
				if (listBotBohn.get(i).getIsOnline() > 0)
					t++;
			}

			JsonObject jo = new JsonObject();
			jo.addProperty("evt", "reloadBot");
			jo.addProperty("Disconnected", s);
			jo.addProperty("IsOnline", t + "/" + listBotBohn.size());
			ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1,
					ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
			serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setServiceRouter(ServiceRouter router) {
		this.serviceRouter = router;
	}

	@Override
	public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long UpdateBotMarkChessByName(int uid, long mark) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void BotCreateTable(int gameid, int mark, int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UserInfo botLogin(int gameid) {
		synchronized (listBotBohn) {
			for (int i = 0; i < listBotBohn.size(); i++) {
				if (listBotBohn.get(i).getGameid() == 0) {
					listBotBohn.get(i).setGameid((short) gameid);				
					return listBotBohn.get(i);
				}
			}
		}
		return null;
	}

	@Override
	public void processBotDisconnect(int playerId) {
		try{
			synchronized (listBotBohn) {
				for (int i = 0; i < listBotBohn.size(); i++) {
					if (listBotBohn.get(i).getPid() == playerId) {
						listBotBohn.get(i).setGameid((short) 0);
						break;
					}
				}
				if (dicBotBohn.containsKey(playerId)) {
					dicBotBohn.get(playerId).setDisconnect(true);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
		try{
			if(!dicBotBohn.containsKey(pid)) {
				return null;
			}
			synchronized (dicBotBohn) {
				dicBotBohn.get(pid).setRoomId((short) roomId);
				dicBotBohn.get(pid).setTableId(tableId);
				dicBotBohn.get(pid).setAS((long) mark);
				for (int i = 0; i < listBotBohn.size(); i++) {
					if (listBotBohn.get(i).getPid() == pid) {
						listBotBohn.remove(i);
						for(int j=0; j<listPid.size(); j++) {
							if(listPid.get(j) == pid) {
								listPid.remove(j);
								break;
							}
						}
						break;
					}
				}
				
				int countContain = 0;
				for (int i = 0; i < listBotBohn.size(); i++) {
					if (listBotBohn.get(i).getPid() == pid) {
						countContain++;
					}
				}
				
				System.out.println("Bohn BotHandler listBotBohn still contain " + pid + " count " + countContain);
				return dicBotBohn.get(pid);
			}		
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Long UpdateBotMarkByName(int uid, int source, String name, long mark) {
		// TODO Auto-generated method stub
		//Logger.getLogger("log_bohn_bot").info("Update bot AG");
		return UpdateBotBohnMark(uid, mark);

	}

	@Override
	public Long UpdateBotMarkByUID(int uid, long mark) {
		synchronized (dicBotBohn) {
			System.out.println("dicBotBohn.containsKey(uid) " + dicBotBohn.containsKey(uid));
			if (dicBotBohn.containsKey(uid)) {
				// System.out.println("UpdateBotMarkChessByName:
				// dicBotTeenpatti.containsKey(name) uid: " + uid+" - mark: "+
				// +mark+" - "+dicBotTeenpatti.get(uid).getAG());
				try {
					System.out
							.println("dicBotBohn.get(uid).getAG() " + dicBotBohn.get(uid).getAG() + " mark " + mark);
					long ag = dicBotBohn.get(uid).getAG() + mark;
					if (ag < 0) {
						ag = 0;
					}
					System.out.println("ag " + ag);
					dicBotBohn.get(uid).setAG(ag);
					UserInfoCmd cmd = new UserInfoCmd("updateAG", dicBotBohn.get(uid).getSource(),
							uid - ServerDefined.userMap.get((int) dicBotBohn.get(uid).getSource()), mark);
					QueueManager.getInstance(UserController.queuename).put(cmd);
					System.out.println("Update bot ag Bohn: " + dicBotBohn.get(uid).getAG());
					return dicBotBohn.get(uid).getAG();
				} catch (Exception e) {
					e.printStackTrace();
					// System.out.println("==>Error==>botHandler==>UpdateBotMarkChessByName:" +
					// e.getMessage() + "-" + uid+"-" + name + "-" + mark);
					return 0l;
				}
			} else {
				// System.out.println("UpdateBotMarkChessByName:
				// !dicBotTeenpatti.containsKey(name)" + uid+" - "+mark);
			}
		}
		return 0l;
	}

	@Override
	public String processGetBotInfoByPid(int pid, int tid) {
		try{
			synchronized (dicBotBohn) {
				if (dicBotBohn.containsKey(pid)) {
					if (tid > 0) {
						dicBotBohn.get(pid).setTableId(tid);
					}
					return ActionUtils.gson.toJson(dicBotBohn.get(pid).getUserGame());
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
		return dicBotBohn;
	}

	@Override
	public List<UserInfo> getListBot() {
		return listBotBohn;
	}
}
