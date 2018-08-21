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
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.google.gson.JsonObject;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.SqlService;

public class RummyIndiaBot implements Bot{
	public final int BOT_MARK_10 = 10;
	public final int BOT_MARK_100 = 100;
	public final int BOT_MARK_500 = 500;
	public final int BOT_MARK_1K = 1000;
	public final int BOT_MARK_5K = 5000;
	public final int BOT_MARK_10K = 10000;
	public final int BOT_MARK_20K = 20000;
	public final int BOT_MARK_50K = 50000;
	public final int BOT_MARK_100K = 100000;
	public final int BOT_MARK_500K = 500000;

	// lsMarkCreateTable.add(new MarkCreateTable(10, 50, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(100, 500, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(500, 2500, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(1000, 5000, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(5000, 25000, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(10000, 50000, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(50000, 250000, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(100000, 500000, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(500000, 2500000, 0));
	// lsMarkCreateTable.add(new MarkCreateTable(1000000, 5000000, 0));

	// Setup Danh sach muc cuoc ban
	public final int BOT_TYPE10 = 11;
	public final int BOT_TYPE100 = 12;
	public final int BOT_TYPE500 = 13;
	public final int BOT_TYPE1K = 14;
	public final int BOT_TYPE5K = 15;
	public final int BOT_TYPE10K = 16;
	public final int BOT_TYPE20K = 17;
	public final int BOT_TYPE50K = 18;
	public final int BOT_TYPE100K = 19;
	public final int BOT_TYPE500K = 20;

	// public final int BOT100000 = 25;//20;
	public final int MIN_AG_RATE = 10;
	public final int MAX_AG_RATE = 200;
	public final int MAX_ADD_AG = 50;
	public final int markRate = 80;

	public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotRummyIndia = ConcurrentLinkedHashMap
			.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
	public List<UserInfo> listBotRummyIndia = new ArrayList<UserInfo>();
	public ServiceRouter serviceRouter;
	private Logger logger_ = Logger.getLogger("RummyIndia");

	public RummyIndiaBot(int source) {
		getListBot(source);
	}
	@Override
	public void botRejectJoinTable(int pid) {
		throw new UnsupportedOperationException();
	}


	public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
		// if(Diamond > 0)
		logger_.info("==>BotHandler==>getUserGame: listBotRummyIndia size:" + listBotRummyIndia.size() + " - minAG: "
				+ minAG + " - Diamond: " + Diamond);
		// System.out.println("==>BotPoker9K2345==>getUserGame: " +
		// listBotTeenpatti.size() + " - " + minAG + " - " + Diamond);

		if (Diamond != 0)
			return;
		// if (minAG > 500000)
		// return;
		// System.out.println("==>getUserGame==>getBot: size:
		// "+listBotTeenpatti.size()+" - ag: "+minAG+ " - gameid: "+gameId+" -tableid:
		// "+tableId+ " - Diamod "+Diamond);
		synchronized (listBotRummyIndia) {
			try {
				short type = BOT_TYPE10;
				if (minAG >= BOT_MARK_500K) {
					type = BOT_TYPE500K;
				} else if (minAG >= BOT_MARK_100K) {
					type = BOT_TYPE100K;
				} else if (minAG >= BOT_MARK_50K) {
					type = BOT_TYPE50K;
				} else if (minAG >= BOT_MARK_20K) {
					type = BOT_TYPE20K;
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
				for (int i = 0; i < listBotRummyIndia.size(); i++) {
					if (listBotRummyIndia.get(i).getGameid() == gameId
							&& listBotRummyIndia.get(i).getUsertype() == type)
						countType++;
				}
				logger_.info("Rummy india count type " + type + " game id " + gameId + " have left " + countType);
				int count = 0;
				// Duyệt tìm boss sẵn sàng đủ tiền chơi
				for (int i = 0; i < listBotRummyIndia.size(); i++) {
					// System.out.println("==>BotHandler==>getUserGame: "+
					// listBotTeenpatti.get(i).getGameid()+" - "
					// +listBotTeenpatti.get(i).getUsertype()+" - "+ type+" - isOl:
					// "+listBotTeenpatti.get(i).getIsOnline());
					if (listBotRummyIndia.get(i).getGameid() == gameId // client bot connected
							&& listBotRummyIndia.get(i).getUsertype() == type) { // check type + ag
						UserInfo u = listBotRummyIndia.get(i);
						int addAG = 0;
						if (u.getTableId() != 0) {
							count++;
							if (count < 5) {
								logger_.info("Players in table id " + u.getTableId());
							}
							continue;
						}

						// System.out.println("\n ==>BotHandler==>BotCreateTable GETTTTTT : " +
						// ActionUtils.gson.toJson(listBotTeenpatti.get(i))+" \nBOTGETTTTT\n");
						if (u.getAG() < minAG * markRate && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
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
							ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.RUMMY_FAST);
						}
						if (u.getAG() < minAG * markRate) {
							// System.out.println("==>Poker9KBot: errMoney "+u.getUserid().intValue()+" -
							// "+u.getAG().intValue());
							continue;
						}
						logger_.info("Rummy India Bot found bot join table 1 " + u.getPid() + " name " + u.getUsername()
								+ " type " + u.getUsertype() + " ag " + u.getAG() + " online " + u.getIsOnline()
								+ " table id " + u.getTableId() + " game id " + u.getGameid());
						u.setIsOnline(gameId);
						dicBotRummyIndia.put(u.getUserid(), u);
						// System.out.println("==>BotHandler==>getUserGame:
						// "+ActionUtils.gson.toJson(u));
						JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
						serviceRouter.dispatchToGame(gameId, action);
						logger_.info("Rummy India Bot found bot join table 2 " + u.getPid() + " name " + u.getUsername()
								+ " type " + u.getUsertype() + " ag " + u.getAG() + " online " + u.getIsOnline()
								+ " table id " + u.getTableId() + " game id " + u.getGameid());
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
		case BOT_TYPE500K:
			addAG = randomBetween2Number(50000000, 100000000);
			break;
		case BOT_TYPE100K:
			addAG = randomBetween2Number(20000000, 100000000);
			break;
		case BOT_TYPE50K:
			addAG = randomBetween2Number(20000000, 40000000);
			break;
		case BOT_TYPE20K:
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
		case BOT_TYPE10:
			addAG = randomBetween2Number(40000, 200000);
		default:
			break;
		}
		return addAG;
	}

	public int getVip(short type) {
		int addAG = 1;
		switch (type) {
		case BOT_TYPE100K:
		case BOT_TYPE500K:
			addAG = (new Random()).nextInt(5) + 2;
			break;
		case BOT_TYPE50K:
			addAG = (new Random()).nextInt(4) + 2;
			break;
		case BOT_TYPE20K:
		case BOT_TYPE10K:
		case BOT_TYPE5K:
		case BOT_TYPE1K:
		case BOT_TYPE500:
		case BOT_TYPE100:
		case BOT_TYPE10:
			addAG = (new Random()).nextInt(3) + 1;
			break;
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
			cs.setInt("Gameid", GAMEID.RUMMY_FAST);
			ResultSet rs = cs.executeQuery();
			short type = 0;
			listBotRummyIndia.clear();

			int bot100 = 50;
			int bot500 = 150;
			int bot1k = 250;
			int bot5k = 350;
			int bot10k = 450;
			int bot20k = 500;
			int bot50k = 550;
			// int bot50k = 150;
			// int bot100k = 100;
			// int bot500k = 50;
			while (rs.next()) {
				UserInfo userTemp = new UserInfo();
				// System.out.println("==>Name:" + rs.getString("Username")) ;

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
				// if (type <= bot500k) {
				// userTemp.setUsertype((short) BOT_TYPE500K);
				// } else if (type <= bot100k) {
				// userTemp.setUsertype((short) BOT_TYPE100K);
				// } else if (type <= bot50k) {
				// userTemp.setUsertype((short) BOT_TYPE50K);
				// } else
				if (type <= bot50k && type > bot20k) {
					userTemp.setUsertype((short) BOT_TYPE50K);
				} else if (type <= bot20k && type > bot10k) {
					userTemp.setUsertype((short) BOT_TYPE20K);
				} else if (type <= bot10k && type > bot5k) {
					userTemp.setUsertype((short) BOT_TYPE10K);
				} else if (type <= bot5k && type > bot1k) {
					userTemp.setUsertype((short) BOT_TYPE5K);
				} else if (type <= bot1k && type > bot500) {
					userTemp.setUsertype((short) BOT_TYPE1K);
				} else if (type <= bot500 && type > bot100) {
					userTemp.setUsertype((short) BOT_TYPE500);
				} else if (type <= bot100) {
					userTemp.setUsertype((short) BOT_TYPE100);
				} else
					userTemp.setUsertype((short) BOT_TYPE10);

				// kiem tra them tien cho bot
				int agDB = rs.getInt("AG");
				int ag = getAGADD(userTemp.getUsertype());
				userTemp.setAG((long) ag);
				userTemp.setVIP((short) getVip(userTemp.getUsertype()));
				ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.RUMMY_FAST);
				listBotRummyIndia.add(userTemp);
			}
			rs.close();
			cs.close();
			// System.out.println("==>Size9K2345:" + listBotTeenpatti.size()) ;
		} catch (SQLException ex) {
//			System.out.println("==>Error==>GetListBot:" + ex.getMessage());
			ex.printStackTrace();
		} finally {
			instance.releaseDbConnection(conn);
		}
	}

	public UserInfo updateBotOnline(int pid, short isOnline) {
		try {
			synchronized (listBotRummyIndia) {
				if (isOnline == 0) {
					logger_.info("Update bot online contain " + pid + " " + dicBotRummyIndia.containsKey(pid));
					// System.out.println("==>BotHandler==>updateBotOnline:before " +
					// ServiceImpl.listBot.size());
					if (dicBotRummyIndia.containsKey(pid)) {
						dicBotRummyIndia.get(pid).setIsOnline(isOnline);
						dicBotRummyIndia.get(pid).setTableId(0);
						if (dicBotRummyIndia.get(pid).isDisconnect())
							dicBotRummyIndia.get(pid).setGameid((short) 0);
						UserInfo u = dicBotRummyIndia.get(pid);
						listBotRummyIndia.add(u);
						logger_.info("Update bot online " + pid + " name " + dicBotRummyIndia.get(pid).getUsername()
								+ " online " + dicBotRummyIndia.get(pid).getIsOnline() + " table id "
								+ dicBotRummyIndia.get(pid).getTableId() + " game id "
								+ dicBotRummyIndia.get(pid).getGameid());
					}
				} else {
					logger_.info("Update bot online isOnline != 0 contain " + pid + dicBotRummyIndia.containsKey(pid));
					dicBotRummyIndia.get(pid).setIsOnline(isOnline);
					logger_.info("Update bot online isOnline != 0 " + isOnline + " pid "
							+ dicBotRummyIndia.get(pid).getPid() + " name " + dicBotRummyIndia.get(pid).getUsername()
							+ " table id " + dicBotRummyIndia.get(pid).getTableId() + " game id "
							+ dicBotRummyIndia.get(pid).getGameid());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dicBotRummyIndia.get(pid);
	}

	public Long UpdateBotRummyMark(int uid, long mark) {
		synchronized (dicBotRummyIndia) {
			System.out.println("dicBotRummyIndia.containsKey(uid) " + dicBotRummyIndia.containsKey(uid));
			if (dicBotRummyIndia.containsKey(uid)) {
				// System.out.println("UpdateBotMarkChessByName:
				// dicBotTeenpatti.containsKey(name) uid: " + uid+" - mark: "+
				// +mark+" - "+dicBotTeenpatti.get(uid).getAG());
				try {
					System.out.println(
							"dicBotRummyIndia.get(uid).getAG() " + dicBotRummyIndia.get(uid).getAG() + " mark " + mark);
					long ag = dicBotRummyIndia.get(uid).getAG() + mark;
					if (ag < 0) {
						ag = 0;
					}
					System.out.println("ag " + ag);
					dicBotRummyIndia.get(uid).setAG(ag);
					UserInfoCmd cmd = new UserInfoCmd("updateAG", dicBotRummyIndia.get(uid).getSource(),
							uid - ServerDefined.userMap.get((int) dicBotRummyIndia.get(uid).getSource()), mark);
					QueueManager.getInstance(UserController.queuename).put(cmd);
					System.out.println("Update bot ag rummy: " + dicBotRummyIndia.get(uid).getAG());
					return dicBotRummyIndia.get(uid).getAG();
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

	public void BotCreateTable(int gameid, int mark) {
		// Auto-generated method stub
		try {
			// System.out.println("==>BotHandler==>BotCreateTable "+ gameid+" - " + mark);
			short type = 11;
			// if(mark == 50000){
			// type = 31;
			// } else if (mark == 100000){
			// type = 32;
			// } else if (mark == 500000){
			// type = 33;
			// }
			// System.out.println("==>BotHandler==>BotCreateTable :
			// "+ActionUtils.gson.toJson(listBotNew));
			boolean check = true;
			for (int i = 0; i < listBotRummyIndia.size(); i++) {
				// if(listBotNew.get(i).getGameid() == gameid)
				// System.out.println("\n ==>BotHandler==>BotCreateTable :
				// "+ActionUtils.gson.toJson(listBotNew.get(i))+" \nBOT\n");
				if (listBotRummyIndia.get(i).getGameid() == gameid && listBotRummyIndia.get(i).getUsertype() == type
						&& listBotRummyIndia.get(i).getRoomId() == 0) {
					check = false;
					UserInfo u = listBotRummyIndia.get(i);
					// System.out.println("==>BotHandler==>BotCreateTable :
					// "+ActionUtils.gson.toJson(listBotNew.get(i))+"\n");
					if (u.getAG() < 80 * mark && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
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
							ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.RUMMY_FAST);
						}
					} else if (u.getAG() < 80 * mark) {
						continue; // tim bot tiep
					}
					listBotRummyIndia.get(i).setRoomId((short) ((new Random()).nextInt(3) + 1));
					u.setIsOnline((short) gameid);
					// listBotNew.remove(i);
					dicBotRummyIndia.put(u.getUserid(), u);
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
			if (check) {
				for (int i = 0; i < listBotRummyIndia.size(); i++) {
					if (listBotRummyIndia.get(i).getUsertype() == type)
						Logger.getLogger("Bot_Details")
								.info("==>BotHandler==>HadBotCreateTable: failed listBotRummyIndia size: "
										+ listBotRummyIndia.size() + " - gameid: " + gameid + " - mark: " + mark);
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
			if (mark >= 100000) // bàn 100k
			{
				type = 33;
			} else if (mark >= 50000) { // ban 50k
				type = 16;
			} else if (mark >= 20000) { // bàn 20k
				type = 15;
			} else if (mark >= 5000) { // bàn 5k
				type = 14;
			} else if (mark >= 1000) { // bàn 1k
				type = 13;
			} else if (mark >= 200) { // bàn 200
				type = 12;
			}
			// System.out.println("==>BotHandler==>BotCreateTable :
			// "+ActionUtils.gson.toJson(listBotTeenpatti));
			// boolean check = true;
			for (int i = 0; i < listBotRummyIndia.size(); i++) {
				// if(listBotTeenpatti.get(i).getGameid() == gameid)
				// System.out.println("\n ==>BotHandler==>BotCreateTable :
				// "+ActionUtils.gson.toJson(listBotTeenpatti.get(i))+" \nBOT\n");
				if (listBotRummyIndia.get(i).getGameid() == gameid && listBotRummyIndia.get(i).getUsertype() == type
						&& listBotRummyIndia.get(i).getRoomId() == 0) {
					// check = false;
					UserInfo u = listBotRummyIndia.get(i);
					// System.out.println("==>BotHandler==>BotCreateTable :
					// "+ActionUtils.gson.toJson(listBotTeenpatti.get(i))+"\n");
					if (u.getAG() < mark * markRate && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
						u.setCPromot(u.getCPromot().intValue() + 1);
						int addAG = getAGADD(type);
						if (addAG > 0) {

							int abDB = u.getAG().intValue();
							u.setAG(u.getAG() + addAG);
							Logger.getLogger("Bot_Details")
									.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
							ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.RUMMY_FAST);
							ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.RUMMY_FAST);
						}
					} else if (u.getAG() < markRate * mark) {
						continue; // tim bot tiep
					}
					listBotRummyIndia.get(i).setRoomId((short) roomID);
					u.setIsOnline((short) gameid);
					// listBotTeenpatti.remove(i);
					dicBotRummyIndia.put(u.getUserid(), u);
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
			// if(check){
			// for(int i = 0; i < listBotTeenpatti.size(); i++){
			// if(listBotTeenpatti.get(i).getUsertype() == type)
			// Logger.getLogger("Bot_Details").info("==>BotHandler==>HadBotCreateTable:
			// failed listBotTeenpatti size: "+listBotTeenpatti.size()
			// +" - gameid: " +gameid+" - mark: "+mark);
			// }
			// }
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
					for (int i = 0; i < listBotRummyIndia.size(); i++) {
						if (listBotRummyIndia.get(i).getGameid() == 0)
							size++;
						// if(listBotTeenpatti.get(i).getUsertype() > BOT_TYPE200)
						// bot1k++;
						// else if(listBotTeenpatti.get(i).getUsertype() > BOT_TYPE50)
						// bot200++;
						// else
						// bot50++;

						if (je.has("T") && je.get("T").getAsInt() == listBotRummyIndia.get(i).getUsertype())
							lsBot.add(listBotRummyIndia.get(i));
					}
					JsonObject jo = new JsonObject();
					jo.addProperty("evt", "checkBot");
					jo.addProperty("Disconnected", size + " / Size: " + listBotRummyIndia.size());
					jo.addProperty("bot50", bot50);
					jo.addProperty("bot200", bot200);
					jo.addProperty("bot1000", bot1k);

					ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1,
							ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
					serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
				} else if (je.get("act").getAsInt() == 1) {
					JsonObject jo = new JsonObject();
					jo.addProperty("evt", "checkBot");
					jo.addProperty("dic", ActionUtils.gson.toJson(dicBotRummyIndia));
					jo.addProperty("list", ActionUtils.gson.toJson(listBotRummyIndia));
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
			listBotRummyIndia.clear();
			getListBot(actionUser.getSource());
			int s = 0, t = 0;
			for (int i = 0; i < listBotRummyIndia.size(); i++) {
				if (listBotRummyIndia.get(i).getGameid() == 0)
					s++;
				if (listBotRummyIndia.get(i).getIsOnline() > 0)
					t++;
			}

			JsonObject jo = new JsonObject();
			jo.addProperty("evt", "reloadBot");
			jo.addProperty("Disconnected", s);
			jo.addProperty("IsOnline", t + "/" + listBotRummyIndia.size());
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
		synchronized (listBotRummyIndia) {
//			Logger.getLogger("Bot_Login").info("LISTBOTRUMMY: " + RummyBot.listBotRummy.size());
//			System.out.println("LISTBOTRUMMY: " + RummyBot.listBotRummy.size());
			for (int i = 0; i < listBotRummyIndia.size(); i++) {
//				if(RummyBot.listBotRummy.get(i).getGameid() != 0)
//					System.out.println("LISTBOTRUMMY getGameid: " + RummyBot.listBotRummy.get(i).getUsername() + " "  + RummyBot.listBotRummy.get(i).getGameid());
				if (listBotRummyIndia.get(i).getGameid() == 0) {
					listBotRummyIndia.get(i).setGameid((short) gameid);
					return listBotRummyIndia.get(i);
				}
			}
		}
		return null;
	}

	@Override
	public void processBotDisconnect(int playerId) {
		try{
			synchronized (listBotRummyIndia) {
				for (int i = 0; i < listBotRummyIndia.size(); i++) {
					if (listBotRummyIndia.get(i).getPid() == playerId) {
						listBotRummyIndia.get(i).setGameid((short) 0);
						break;
					}
				}
				if (dicBotRummyIndia.containsKey(playerId)) {
					dicBotRummyIndia.get(playerId).setDisconnect(true);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
		try{
			if(!dicBotRummyIndia.containsKey(pid)) return null;
			dicBotRummyIndia.get(pid).setRoomId((short) roomId);
			dicBotRummyIndia.get(pid).setTableId(tableId);
			dicBotRummyIndia.get(pid).setAS((long) mark);
			for (int i = 0; i < dicBotRummyIndia.size(); i++) {
				if (listBotRummyIndia.get(i).getPid() == pid) {
					listBotRummyIndia.remove(i);
					break;
				}
			}
			return dicBotRummyIndia.get(pid);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Long UpdateBotMarkByName(int uid, int source, String name, long mark) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long UpdateBotMarkByUID(int uid, long mark) {
		synchronized (dicBotRummyIndia) {
			System.out.println("dicBotRummyIndia.containsKey(uid) " + dicBotRummyIndia.containsKey(uid));
			if (dicBotRummyIndia.containsKey(uid)) {
				// System.out.println("UpdateBotMarkChessByName:
				// dicBotTeenpatti.containsKey(name) uid: " + uid+" - mark: "+
				// +mark+" - "+dicBotTeenpatti.get(uid).getAG());
				try {
					System.out.println(
							"dicBotRummyIndia.get(uid).getAG() " + dicBotRummyIndia.get(uid).getAG() + " mark " + mark);
					long ag = dicBotRummyIndia.get(uid).getAG() + mark;
					if (ag < 0) {
						ag = 0;
					}
					System.out.println("ag " + ag);
					dicBotRummyIndia.get(uid).setAG(ag);
					UserInfoCmd cmd = new UserInfoCmd("updateAG", dicBotRummyIndia.get(uid).getSource(),
							uid - ServerDefined.userMap.get((int) dicBotRummyIndia.get(uid).getSource()), mark);
					QueueManager.getInstance(UserController.queuename).put(cmd);
					System.out.println("Update bot ag rummy: " + dicBotRummyIndia.get(uid).getAG());
					return dicBotRummyIndia.get(uid).getAG();
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
			synchronized (dicBotRummyIndia) {
				if (dicBotRummyIndia.containsKey(pid)) {
					if (tid > 0) {
						dicBotRummyIndia.get(pid).setTableId(tid);
					}
					return ActionUtils.gson.toJson(dicBotRummyIndia.get(pid).getUserGame());
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
		return dicBotRummyIndia;
	}

	@Override
	public List<UserInfo> getListBot() {
		return listBotRummyIndia;
	}
}
