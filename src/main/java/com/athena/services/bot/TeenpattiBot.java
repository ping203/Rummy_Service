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
import com.athena.services.impl.ServiceImpl;
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

public class TeenpattiBot implements Bot {

	public final int BOT_MARK_10 = 10;
	public final int BOT_MARK_50 = 50;
	public final int BOT_MARK_100 = 100;
	public final int BOT_MARK_500 = 500;
	public final int BOT_MARK_1K = 1000;
	public final int BOT_MARK_2K = 2000;
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
	public final int BOT_TYPE50 = 12;
	public final int BOT_TYPE100 = 13;
	public final int BOT_TYPE500 = 14;
	public final int BOT_TYPE1K = 15;
	public final int BOT_TYPE2K = 16;
	public final int BOT_TYPE5K = 17;
	public final int BOT_TYPE10K = 18;
	public final int BOT_TYPE20K = 19;
	public final int BOT_TYPE50K = 20;
	public final int BOT_TYPE100K = 21;
	public final int BOT_TYPE500K = 22;

	// public final int BOT100000 = 25;//20;
	public final int MIN_AG_RATE = 10;
	public final int MAX_AG_RATE = 200;
	public final int MAX_ADD_AG = 50;
	public final int markRate = 500;

	@Override
	public void botRejectJoinTable(int pid) {
		throw new UnsupportedOperationException();
	}

	public ConcurrentLinkedHashMap<Integer, UserInfo> dicBotTeenpatti = ConcurrentLinkedHashMap
			.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 1000);
	public List<UserInfo> listBotTeenpatti = new ArrayList<UserInfo>();
	public ServiceRouter serviceRouter;
	public List<Integer> listPid = new ArrayList<Integer>();

	public TeenpattiBot(int source) {
		getListBot(source);
	}

	public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
		// if(Diamond > 0)
		System.out.println("==>BotHandler==>getUserGame: listBotTeenpatti size:" + listBotTeenpatti.size()
				+ " - minAG: " + minAG + " - Diamond: " + Diamond);
		// System.out.println("==>BotPoker9K2345==>getUserGame: " +
		// listBotTeenpatti.size() + " - " + minAG + " - " + Diamond);

		if (Diamond != 0)
			return;
		if (minAG > 10000)
			return;
		// System.out.println("==>getUserGame==>getBot: size:
		// "+listBotTeenpatti.size()+" - ag: "+minAG+ " - gameid: "+gameId+" -tableid:
		// "+tableId+ " - Diamod "+Diamond);
		synchronized (listBotTeenpatti) {
			try {
				long rateMin = 1000;
				long rateMax = 8000;
				short type = BOT_TYPE10;
				if (minAG >= BOT_MARK_500K) {
					type = BOT_TYPE500K;
				} else if (minAG >= BOT_MARK_100K) {
					type = BOT_TYPE100K;
				} else if (minAG >= BOT_MARK_50K) {
					type = BOT_TYPE50K;
				} else if (minAG >= BOT_MARK_20K) {
					type = BOT_TYPE2K;
					rateMin = 750;
					rateMax = 5000;
				} else if (minAG >= BOT_MARK_10K) {
					type = BOT_TYPE10K;
					rateMin = 800;
					rateMax = 5000;
				} else if (minAG >= BOT_MARK_5K) {
					type = BOT_TYPE5K;
					rateMin = 800;
					rateMax = 5000;
				} else if (minAG >= BOT_MARK_2K) {
					type = BOT_TYPE2K;
					rateMin = 1100;
					rateMax = 5000;
				} else if (minAG >= BOT_MARK_1K) {
					type = BOT_TYPE1K;
					rateMin = 1000;
					rateMax = 5000;
				} else if (minAG >= BOT_MARK_500) {
					type = BOT_TYPE500;
					rateMin = 600;
					rateMax = 5000;
				} else if (minAG >= BOT_MARK_100) {
					type = BOT_TYPE100;
					rateMin = 600;
					rateMax = 6000;
				} else if (minAG >= BOT_MARK_50) {
					type = BOT_TYPE50;
					rateMin = 500;
					rateMax = 4000;
				} else if (minAG >= BOT_MARK_10) {
					type = BOT_TYPE10;
				}
				System.out.println("==>BotHandler==>getUserGame: listBotTeenpatti type bot: " + type + " tableid "
						+ tableId + " gameid: " + gameId);
				// System.out.println("==>BotHandler==>getUserGame: listBotTeenpatti"+" -
				// "+listBotTeenpatti.get(listBotTeenpatti.size()-1).getUsername()+" - "
				// +listBotTeenpatti.get(listBotTeenpatti.size()-1).getIsOnline()+" - "
				// +listBotTeenpatti.get(listBotTeenpatti.size()-2).getUsername()
				// +" - "+listBotTeenpatti.get(listBotTeenpatti.size()-2).getIsOnline());
				// Duyệt tìm boss sẵn sàng đủ tiền chơi
				int countType = 0;
				for (int i = 0; i < listBotTeenpatti.size(); i++) {
					if (listBotTeenpatti.get(i).getGameid() == gameId && listBotTeenpatti.get(i).getUsertype() == type)
						countType++;
				}
				System.out.println("Teenpatti count type " + type + " game id " + gameId + " have left " + countType);

				for (int i = 0; i < listBotTeenpatti.size(); i++) {
					// System.out.println("==>BotHandler==>getUserGame: " +
					// listBotTeenpatti.get(i).getUsertype()
					// + listBotTeenpatti.get(i).getGameid() + " - " + " - " + type + " - "
					// + +listBotTeenpatti.get(i).getIsOnline());
					if (listBotTeenpatti.get(i).getGameid() == gameId // client bot connected
							&& listBotTeenpatti.get(i).getUsertype() == type) { // check type + ag
						UserInfo u = listBotTeenpatti.get(i);
						System.out.println("Bot name: " + " pid " + u.getPid() + " name " + u.getUsername() + " ag: "
								+ u.getAG() + " online " + u.getIsOnline() + " seated " + u.getTableId());
						if (u.getTableId() != 0)
							continue;
						int addAG = 0;
						// System.out.println("\n ==>BotHandler==>BotCreateTable GETTTTTT : " +
						// ActionUtils.gson.toJson(listBotTeenpatti.get(i))+" \nBOTGETTTTT\n");
						if (u.getAG() < minAG * rateMin && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
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
							ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.TEENPATTI);
						}
						if (u.getAG() < minAG * rateMin) {
							// System.out.println("==>Poker9KBot: errMoney "+u.getUserid().intValue()+" -
							// "+u.getAG().intValue());
							continue;
						}
						u.setIsOnline(gameId);
						dicBotTeenpatti.put(u.getUserid(), u);
						// System.out.println("==>BotHandler==>getUserGame:
						// "+ActionUtils.gson.toJson(u));
						System.out.println("Teenpatti found bot join table: " + " pid " + u.getPid() + " name "
								+ u.getUsername() + " ag: " + u.getAG() + " mark " + minAG + " tableId " + tableId
								+ " online " + u.getIsOnline() + " seated " + u.getTableId());
						JoinRequestAction action = new JoinRequestAction(u.getPid(), tableId, -1, "");
						serviceRouter.dispatchToGame(gameId, action);
						break;
					}
				}
				// boolean check = true;
				// for (int i = 0; i < listBotTeenpatti.size(); i++) {
				//// System.out.println("==>BotHandler==>getUserGame: " +
				// listBotTeenpatti.get(i).getUsertype()
				//// + listBotTeenpatti.get(i).getGameid() + " - " + " - " + type + " - "
				//// + +listBotTeenpatti.get(i).getIsOnline());
				// if (listBotTeenpatti.get(i).getIsOnline()> 0){
				// check= false;
				// System.out.println("==>BotHandler==>getUserGame: listBotTeenpatti-
				// "+listBotTeenpatti.get(i).getUsername());
				// }
				// }
				// if(check)
				// System.out.println("==>BotHandler==>getUserGame: listBotTeenpatti- " +check);
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
			addAG = randomBetween2Number(15000000, 100000000);
			break;
		case BOT_TYPE10K:
			addAG = randomBetween2Number(8000000, 50000000);
			break;
		case BOT_TYPE5K:
			addAG = randomBetween2Number(4000000, 25000000);
			break;
		case BOT_TYPE2K:
			addAG = randomBetween2Number(2200000, 10000000);
			break;
		case BOT_TYPE1K:
			addAG = randomBetween2Number(1000000, 5000000);
			break;
		case BOT_TYPE500:
			addAG = randomBetween2Number(300000, 2500000);
			break;
		case BOT_TYPE100:
			addAG = randomBetween2Number(60000, 600000);
			break;
		case BOT_TYPE50:
			addAG = randomBetween2Number(25000, 200000);
			break;
		case BOT_TYPE10:
			addAG = randomBetween2Number(10000, 80000);
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
			addAG = (new Random()).nextInt(3) + 2;
			break;
		case BOT_TYPE20K:
		case BOT_TYPE10K:
		case BOT_TYPE2K:
		case BOT_TYPE1K:
		case BOT_TYPE500:
		case BOT_TYPE100:
		case BOT_TYPE50:
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
			cs.setInt("Gameid", GAMEID.TEENPATTI);
			ResultSet rs = cs.executeQuery();
			short type = 0;
			listBotTeenpatti.clear();
			listPid.clear();
			// int bot100 = 550;
			// int bot500 = 450;
			// int bot1k = 350;
			// int bot10k = 250;
			// int bot50k = 150;
			// int bot100k = 80;
			// int bot500k = 20;

			int bot50 = 100;
			int bot100 = 200;
			int bot500 = 250;
			int bot1k = 300;
			int bot2k = 350;
			int bot5k = 400;
			int bot10k = 450;
			// int bot20k = 200;
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
				// }else if (type <= bot20k) {
				// userTemp.setUsertype((short) BOT_TYPE20K);
				// } else
				if (type <= bot10k && type > bot5k) {
					userTemp.setUsertype((short) BOT_TYPE10K);
				} else if (type <= bot5k && type > bot2k) {
					userTemp.setUsertype((short) BOT_TYPE5K);
				} else if (type <= bot2k && type > bot1k) {
					userTemp.setUsertype((short) BOT_TYPE2K);
				} else if (type <= bot1k && type > bot500) {
					userTemp.setUsertype((short) BOT_TYPE1K);
				} else if (type <= bot500 && type > bot100) {
					userTemp.setUsertype((short) BOT_TYPE500);
				} else if (type <= bot100 && type > bot50) {
					userTemp.setUsertype((short) BOT_TYPE100);
				} else if (type <= bot50) {
					userTemp.setUsertype((short) BOT_TYPE50);
				} else
					userTemp.setUsertype((short) BOT_TYPE10);

				// kiem tra them tien cho bot
				int agDB = rs.getInt("AG");
				int ag = getAGADD(userTemp.getUsertype());
				userTemp.setAG((long) ag);
				userTemp.setVIP((short) getVip(userTemp.getUsertype()));
				ActionUtils.updateAGBOT(userTemp, ag, agDB, GAMEID.TEENPATTI);
				// if (!listPid.contains(userTemp.getPid())) {
				listBotTeenpatti.add(userTemp);
				listPid.add(userTemp.getPid());
				// }
			}
			rs.close();
			cs.close();
			// System.out.println("==>Size9K2345:" + listBotTeenpatti.size()) ;
		} catch (SQLException ex) {
			System.out.println("==>Error==>GetListBot:" + ex.getMessage());
		} finally {
			instance.releaseDbConnection(conn);
		}
	}

	public UserInfo updateBotOnline(int pid, short isOnline) {
		try {
			synchronized (listBotTeenpatti) {
				if (isOnline == 0) {

					// System.out.println("==>BotHandler==>updateBotOnline:before " +
					// ServiceImpl.listBot.size());
					if (dicBotTeenpatti.containsKey(pid)) {
						dicBotTeenpatti.get(pid).setIsOnline(isOnline);
						dicBotTeenpatti.get(pid).setTableId(0);
						if (dicBotTeenpatti.get(pid).isDisconnect())
							dicBotTeenpatti.get(pid).setGameid((short) 0);
						UserInfo u = dicBotTeenpatti.get(pid);
						System.out.println("TeenpattiBot listBotTeenpatti add user " + u.getUsername() + " game id "
								+ u.getGameid() + " table id " + u.getTableId() + " pid " + u.getPid() + " ag "
								+ u.getAG() + " type " + u.getUsertype());
						int countContain = 0;
						for (int i = 0; i < listBotTeenpatti.size(); i++) {
							if (listBotTeenpatti.get(i).getPid() == pid) {
								countContain++;
							}
						}

						System.out.println("TeenpattiBot listBotTeenpatti contain " + pid + " count " + countContain);

						if (!listPid.contains(u.getPid())) {
							listBotTeenpatti.add(u);
							listPid.add(u.getPid());
							System.out.println("TeenpattiBot listBotTeenpatti add " + u.getPid());
						}

						System.out.println("TeenpattiBot listBotTeenpatti size " + listBotTeenpatti.size());
						return dicBotTeenpatti.remove(pid);
					}
				} else {
					dicBotTeenpatti.get(pid).setIsOnline(isOnline);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dicBotTeenpatti.get(pid);
	}

	public Long UpdateBotTeenpattiMark(int uid, long mark) {
		synchronized (dicBotTeenpatti) {
			if (dicBotTeenpatti.containsKey(uid)) {
				// System.out.println("UpdateBotMarkChessByName:
				// dicBotTeenpatti.containsKey(name) uid: " + uid+" - mark: "+
				// +mark+" - "+dicBotTeenpatti.get(uid).getAG());
				try {
					long ag = dicBotTeenpatti.get(uid).getAG() + mark;
					if (ag < 0) {
						ag = 0;
					}
					dicBotTeenpatti.get(uid).setAG(ag);
					UserInfoCmd cmd = new UserInfoCmd("updateAG", dicBotTeenpatti.get(uid).getSource(),
							uid - ServerDefined.userMap.get((int) dicBotTeenpatti.get(uid).getSource()), mark);
					QueueManager.getInstance(UserController.queuename).put(cmd);
					return dicBotTeenpatti.get(uid).getAG();
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

	public void BotCreateTable(int gameid, int mark, int type) {
		// Auto-generated method stub
		try {
			Logger.getLogger("Bot_Login").info("==>BotHandler==>BotCreateTable " + gameid + " - " + mark);
			if (mark > 0)
				return;
			short typeUser = 11;
			// if(mark == 50000){
			// type = 31;
			// } else if (mark == 100000){
			// type = 32;
			// } else if (mark == 500000){
			// type = 33;
			// }
			// System.out.println("==>BotHandler==>BotCreateTable :
			// "+ActionUtils.gson.toJson(listBotPoker9K));
			long rateMin = 1000;
			long rateMax = 8000;
			// if (mark >= BOT_MARK_20K) {
			// type = BOT_TYPE2K;
			// rateMin = 750;
			// rateMax = 5000;
			// } else if (mark >= BOT_MARK_10K) {
			// type = BOT_TYPE10K;
			// rateMin = 800;
			// rateMax = 5000;
			// } else
			// if (mark >= BOT_MARK_5K) {
			// type = BOT_TYPE5K;
			// rateMin = 800;
			// rateMax = 5000;
			// } else if (mark >= BOT_MARK_2K) {
			// type = BOT_TYPE2K;
			// rateMin = 1100;
			// rateMax = 5000;
			// } else
			if (mark >= BOT_MARK_1K) {
				type = BOT_TYPE1K;
				rateMin = 1000;
				rateMax = 5000;
			} else if (mark >= BOT_MARK_500) {
				type = BOT_TYPE500;
				rateMin = 600;
				rateMax = 5000;
			} else if (mark >= BOT_MARK_100) {
				type = BOT_TYPE100;
				rateMin = 600;
				rateMax = 6000;
			} else if (mark >= BOT_MARK_50) {
				type = BOT_TYPE50;
				rateMin = 500;
				rateMax = 4000;
			} else if (mark >= BOT_MARK_10) {
				type = BOT_TYPE10;
			}
			boolean check = true;
			for (int i = 0; i < listBotTeenpatti.size(); i++) {
				// if(listBotPoker9K.get(i).getGameid() == gameid)
				// System.out.println("\n ==>BotHandler==>BotCreateTable :
				// "+ActionUtils.gson.toJson(listBotPoker9K.get(i))+" \nBOT\n");
				if (listBotTeenpatti.get(i).getGameid() == gameid && listBotTeenpatti.get(i).getUsertype() == typeUser
						&& listBotTeenpatti.get(i).getRoomId() == 0) {
					check = false;
					UserInfo u = listBotTeenpatti.get(i);
					// System.out.println("==>BotHandler==>BotCreateTable :
					// "+ActionUtils.gson.toJson(listBotPoker9K.get(i))+"\n");
					if (u.getAG() < mark * rateMin && u.getCPromot() < MAX_ADD_AG) { // kiem tra them tien cho bot
						u.setCPromot(u.getCPromot().intValue() + 1);
						int addAG = getAGADD(typeUser);
						if (addAG > 0) {
							u.setAG(u.getAG() + addAG);
							UserInfoCmd cmd = new UserInfoCmd("updateAG", u.getSource(),
									u.getUserid() - ServerDefined.userMap.get((int) u.getSource()), addAG);
							QueueManager.getInstance(UserController.queuename).put(cmd);
							Logger.getLogger("Bot_Details")
									.info(ActionUtils.gson.toJson(u) + " - " + mark + " - " + addAG);
							ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.TEENPATTI);
						}
					} else if (u.getAG() < rateMin * mark) {
						continue; // tim bot tiep
					}
					listBotTeenpatti.get(i).setRoomId((short) ((new Random()).nextInt(3) + 1));
					u.setIsOnline((short) gameid);
					// listBotPoker9K.remove(i);
					dicBotTeenpatti.put(u.getUserid(), u);
					// System.out.println("==>BotHandler==>BotCreateTable : OK");

					JsonObject send = new JsonObject();
					send.addProperty("evt", "botCreateTable");
					send.addProperty("pid", u.getPid());
					send.addProperty("M", mark);
					send.addProperty("T", type);
					ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
					serviceRouter.dispatchToGameActivator(gameid, request);
					break;
				}
			}
			if (check) {
				for (int i = 0; i < listBotTeenpatti.size(); i++) {
					if (listBotTeenpatti.get(i).getUsertype() == typeUser)
						Logger.getLogger("Bot_Details")
								.info("==>BotHandler==>HadBotCreateTable: failed listBotTeenpatti size: "
										+ listBotTeenpatti.size() + " - gameid: " + gameid + " - mark: " + mark);
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
			for (int i = 0; i < listBotTeenpatti.size(); i++) {
				// if(listBotTeenpatti.get(i).getGameid() == gameid)
				// System.out.println("\n ==>BotHandler==>BotCreateTable :
				// "+ActionUtils.gson.toJson(listBotTeenpatti.get(i))+" \nBOT\n");
				if (listBotTeenpatti.get(i).getGameid() == gameid && listBotTeenpatti.get(i).getUsertype() == type
						&& listBotTeenpatti.get(i).getRoomId() == 0) {
					// check = false;
					UserInfo u = listBotTeenpatti.get(i);
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
							ActionUtils.BotLogIFRS(u.getUserid(), u.getAG().intValue(), addAG, GAMEID.TEENPATTI);
							ActionUtils.updateAGBOT(u, u.getAG().intValue(), abDB, GAMEID.TEENPATTI);
						}
					} else if (u.getAG() < markRate * mark) {
						continue; // tim bot tiep
					}
					listBotTeenpatti.get(i).setRoomId((short) roomID);
					u.setIsOnline((short) gameid);
					// listBotTeenpatti.remove(i);
					dicBotTeenpatti.put(u.getUserid(), u);
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

	public ServiceRouter getServiceRouter() {
		return serviceRouter;
	}

	public void checkBot(JsonObject je, UserInfo actionUser) {
		try {
			if (je.has("act")) {
				if (je.get("act").getAsInt() == 0) {
					int size = 0, bot50 = 0, bot200 = 0, bot1k = 0;
					List<UserInfo> lsBot = new ArrayList<UserInfo>();
					for (int i = 0; i < listBotTeenpatti.size(); i++) {
						if (listBotTeenpatti.get(i).getGameid() == 0)
							size++;
						// if(listBotTeenpatti.get(i).getUsertype() > BOT_TYPE200)
						// bot1k++;
						// else if(listBotTeenpatti.get(i).getUsertype() > BOT_TYPE50)
						// bot200++;
						// else
						// bot50++;

						if (je.has("T") && je.get("T").getAsInt() == listBotTeenpatti.get(i).getUsertype())
							lsBot.add(listBotTeenpatti.get(i));
					}
					JsonObject jo = new JsonObject();
					jo.addProperty("evt", "checkBot");
					jo.addProperty("Disconnected", size + " / Size: " + listBotTeenpatti.size());
					jo.addProperty("bot50", bot50);
					jo.addProperty("bot200", bot200);
					jo.addProperty("bot1000", bot1k);

					ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1,
							ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
					serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
				} else if (je.get("act").getAsInt() == 1) {
					JsonObject jo = new JsonObject();
					jo.addProperty("evt", "checkBot");
					jo.addProperty("dic", ActionUtils.gson.toJson(dicBotTeenpatti));
					jo.addProperty("list", ActionUtils.gson.toJson(listBotTeenpatti));
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
			for (int i = 0; i < listBotTeenpatti.size(); i++) {
				if (listBotTeenpatti.get(i).getGameid() == 0)
					s++;
				if (listBotTeenpatti.get(i).getIsOnline() > 0)
					t++;
			}

			JsonObject jo = new JsonObject();
			jo.addProperty("evt", "reloadBot");
			jo.addProperty("Disconnected", s);
			jo.addProperty("IsOnline", t + "/" + listBotTeenpatti.size());
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
	public void BotCreateTable(int gameid, int mark) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserInfo botLogin(int gameid) {
		synchronized (listBotTeenpatti) {
			// Logger.getLogger("Bot_Login").info("LISTBOTTEENPATTI: " +
			// TeenpattiBot.listBotTeenpatti.size());
			// System.out.println("LISTBOTTEENPATTI: " +
			// TeenpattiBot.listBotTeenpatti.size());
			for (int i = 0; i < listBotTeenpatti.size(); i++) {
				// if(TeenpattiBot.listBotTeenpatti.get(i).getGameid() != 0)
				// System.out.println("LISTBOTTEENPATTI getGameid: " +
				// TeenpattiBot.listBotTeenpatti.get(i).getUsername() + " " +
				// TeenpattiBot.listBotTeenpatti.get(i).getGameid());
				if (listBotTeenpatti.get(i).getGameid() == 0) {
					listBotTeenpatti.get(i).setGameid((short) gameid);
					return listBotTeenpatti.get(i);
				}
			}
		}
		return null;
	}

	@Override
	public void processBotDisconnect(int playerId) {
		try {
			synchronized (listBotTeenpatti) {
				for (int i = 0; i < listBotTeenpatti.size(); i++) {
					if (listBotTeenpatti.get(i).getPid() == playerId) {
						listBotTeenpatti.get(i).setGameid((short) 0);
						break;
					}
				}
				if (dicBotTeenpatti.containsKey(playerId)) {
					dicBotTeenpatti.get(playerId).setDisconnect(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public UserInfo processConfirmRoom(int pid, int roomId, int tableId, int mark) {
		try {
			if (dicBotTeenpatti.containsKey(pid)) {
				System.out.println("Teenpatti BotHandler IsRunBotTeenpatti " + " contain " + pid + " "
						+ dicBotTeenpatti.containsKey(pid));
				dicBotTeenpatti.get(pid).setRoomId((short) roomId);
				dicBotTeenpatti.get(pid).setTableId(tableId);
				dicBotTeenpatti.get(pid).setAS((long) mark);
				System.out.println("Teenpatti BotHandler dicBotTeenpatti name " + dicBotTeenpatti.get(pid).getUsername()
						+ " gameid " + dicBotTeenpatti.get(pid).getGameid() + " tableid "
						+ dicBotTeenpatti.get(pid).getTableId());
				for (int i = 0; i < listBotTeenpatti.size(); i++) {
					if (listBotTeenpatti.get(i).getPid() == pid) {
						System.out.println("Teenpatti BotHandler listBotTeenpatti remove " + pid + " "
								+ listBotTeenpatti.get(i).getUsername());
						listBotTeenpatti.remove(i);
						for (int j = 0; j < listPid.size(); j++) {
							if (listPid.get(j) == pid) {
								listPid.remove(j);
								break;
							}
						}
						System.out.println("Teenpatti BotHandler listBotTeenpatti size " + listBotTeenpatti.size());
						break;
					}
				}

				int countContain = 0;
				for (int i = 0; i < listBotTeenpatti.size(); i++) {
					if (listBotTeenpatti.get(i).getPid() == pid) {
						countContain++;
					}
				}

				System.out.println(
						"Teenpatti BotHandler listBotTeenpatti still contain " + pid + " count " + countContain);
				return dicBotTeenpatti.get(pid);
			}
		} catch (Exception e) {
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
		synchronized (dicBotTeenpatti) {
			if (dicBotTeenpatti.containsKey(uid)) {
				// System.out.println("UpdateBotMarkChessByName:
				// dicBotTeenpatti.containsKey(name) uid: " + uid+" - mark: "+
				// +mark+" - "+dicBotTeenpatti.get(uid).getAG());
				try {
					long ag = dicBotTeenpatti.get(uid).getAG() + mark;
					if (ag < 0) {
						ag = 0;
					}
					dicBotTeenpatti.get(uid).setAG(ag);
					UserInfoCmd cmd = new UserInfoCmd("updateAG", dicBotTeenpatti.get(uid).getSource(),
							uid - ServerDefined.userMap.get((int) dicBotTeenpatti.get(uid).getSource()), mark);
					QueueManager.getInstance(UserController.queuename).put(cmd);
					return dicBotTeenpatti.get(uid).getAG();
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
		try {
			synchronized (dicBotTeenpatti) {
				if (dicBotTeenpatti.containsKey(pid)) {
					if (tid > 0) {
						dicBotTeenpatti.get(pid).setTableId(tid);
					}
					return ActionUtils.gson.toJson(dicBotTeenpatti.get(pid).getUserGame());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public ConcurrentLinkedHashMap<Integer, UserInfo> getDicBot() {
		return dicBotTeenpatti;
	}

	@Override
	public List<UserInfo> getListBot() {
		return listBotTeenpatti;
	}
}
