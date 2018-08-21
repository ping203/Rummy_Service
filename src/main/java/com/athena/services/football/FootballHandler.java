package com.athena.services.football;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

//import java.util.logging.Level;
//import java.util.logging.Logger;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.JSent;
//import com.athena.services.vo.Match;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.ServerSource;
import com.google.gson.JsonObject;
import com.vng.tfa.common.SqlService;

public class FootballHandler {

    //Top Gold Football Siam
    private List<MatchScore> lsTopWeek = new ArrayList<MatchScore>();
    private List<MatchScore> lsTopMonth = new ArrayList<MatchScore>();
    private List<MatchScore> lsTopAll = new ArrayList<MatchScore>();

    private static final Logger _logger = Logger.getLogger("FootBallHandler");

    public void Process_GetDataFootball(JsonObject je, UserInfo actionUser, int playerId, ServiceRouter serviceRouter) {
        try {
            int typeid = je.get("type").getAsInt(); // 1. getLive, 2. getHistory 3. getTopMonth 4. getTopWeek
            _logger.info("==>Process_GetFootballData: type = " + typeid);
            if (typeid == 1) { // 1. getLive
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "datafootball");
                jo.addProperty("type", typeid);
                jo.addProperty("data", ActionUtils.gson.toJson(ServiceImpl.lsMatchSiam));

                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (typeid == 2) { // 2. getHistory
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "datafootball");
                jo.addProperty("type", typeid);
                String json = ActionUtils.gson.toJson(GameGetHistoryMatch(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()).intValue()));
                jo.addProperty("data", json);
                _logger.info("Data " + json);
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (typeid == 3) {
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "datafootball");
                jo.addProperty("type", typeid);
                jo.addProperty("data", ActionUtils.gson.toJson(GameGetTopMatch(actionUser, typeid)));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (typeid == 4 || typeid == 5 || typeid == 6 || typeid == 7) {  // top score
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "datafootball");
                jo.addProperty("type", typeid);
                jo.addProperty("data", ActionUtils.gson.toJson(GameGetTopScore(actionUser, typeid)));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (typeid == 8) {  // top gold week, month, all
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "datafootball");
                jo.addProperty("type", typeid);
                jo.addProperty("data", ActionUtils.gson.toJson(lsTopWeek));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (typeid == 9) {  // top gold week, month, all
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "datafootball");
                jo.addProperty("type", typeid);
                jo.addProperty("data", ActionUtils.gson.toJson(lsTopMonth));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (typeid == 10) {  // top gold week, month, all
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "datafootball");
                jo.addProperty("type", typeid);
                jo.addProperty("data", ActionUtils.gson.toJson(lsTopAll));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else if (typeid == 11) {  // getHistoryNew
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "datafootball");
                jo.addProperty("type", typeid);
                String json = ActionUtils.gson.toJson(GameGetHistoryMatchNew(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()).intValue()));

                _logger.info("aaa");
                jo.addProperty("data", json);
                _logger.info("Data: " + json);
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            }

        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public List<MatchHistory> GameGetHistoryMatch(int source, int userid) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            _logger.info("==>User:" + userid);
            CallableStatement cs;
            cs = conn.prepareCall("{call ServicesGetMatchPlayer(?) }");
            cs.setInt("Userid", userid);

            ResultSet rs = cs.executeQuery();
            List<MatchHistory> lsret = new ArrayList<MatchHistory>();

            while (rs.next()) {
                MatchHistory um = new MatchHistory();
                um.setAG(rs.getLong("AG"));
                um.setBet(rs.getInt("Bet"));
                um.setBetValue(rs.getFloat("BetValue"));
                um.setBetWin(rs.getLong("BetWin"));
                um.setMatchName(rs.getString("MatchName"));
                um.setT(rs.getTimestamp("StopTime").getTime());
                um.setTeamA(rs.getString("TeamA"));
                um.setTeamB(rs.getString("TeamB"));
                um.setRateA1(rs.getFloat("A1"));
                um.setRateA2(rs.getFloat("A2"));
                um.setB1(rs.getInt("B1"));
                um.setB2(rs.getInt("B2"));
                um.setState(rs.getInt("StateM"));
                um.setResult(rs.getInt("Result"));
                um.setMatchID(rs.getInt("MatchID"));
                um.setRateB(rs.getFloat("Lost"));
                um.setRateA(rs.getFloat("Win"));
                um.setTime(rs.getTimestamp("CreateTime"));
                um.setMatchResult(rs.getInt("MatchResult"));

                String dataBet = "";
                if (um.getBet() == 1) {
                    dataBet = "(" + um.getTeamA() + ", " + Float.toString(um.getRateA2() - um.getRateA1()) + " @ " + Float.toString(um.getBetValue()) + ")";
                } else if (um.getBet() == 3) {
                    dataBet = "(" + um.getTeamB() + ", " + Float.toString(um.getRateA1() - um.getRateA2()) + " @ " + Float.toString(um.getBetValue()) + ")";
                }
                um.setDataBet(dataBet);
                lsret.add(um);
            }
            rs.close();
            cs.close();
            _logger.info("==>Length:" + lsret.size());
            return lsret;
        } catch (Exception ex) {
            _logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
            return new ArrayList<MatchHistory>();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<MatchHistoryNew> GameGetHistoryMatchNew(int source, int userid) {
        try{
            List<MatchHistory> ls = GameGetHistoryMatch(source, userid);
            HashMap<Integer, MatchHistoryNew> mapID = new HashMap<Integer, MatchHistoryNew>();

            Iterator<MatchHistory> iterator = ls.iterator();
            while (iterator.hasNext()){
                MatchHistory matchHistory = iterator.next();
                _logger.info(matchHistory);
                MatchHistoryNew matchHistoryNew = null;
                if(!mapID.containsKey(matchHistory.getMatchID())){
                    matchHistoryNew = new MatchHistoryNew();
                    matchHistoryNew.setTeamA(matchHistory.getTeamA());
                    matchHistoryNew.setTeamB(matchHistory.getTeamB());
                    matchHistoryNew.setRateA1(matchHistory.getRateA1());
                    matchHistoryNew.setRateA2(matchHistory.getRateA2());
                    matchHistoryNew.setTime(matchHistory.getT());
                    matchHistoryNew.setB1(matchHistory.getB1());
                    matchHistoryNew.setB2(matchHistory.getB2());
                    matchHistoryNew.setRateA(matchHistory.getRateA());
                    matchHistoryNew.setRateB(matchHistory.getRateB());

                    mapID.put(matchHistory.getMatchID(), matchHistoryNew);
                }

                matchHistoryNew = mapID.get(matchHistory.getMatchID());

                if (matchHistory.getBet() == 1) {
                    matchHistoryNew.getStrBet().add(
                            String.format(ServiceImpl.actionUtils.getConfigText("bet_football_msgbet", source, userid),
                                    ActionUtils.formatAG(matchHistory.getAG()),
                                    matchHistory.getTeamA(),
                                    ActionUtils.getTimeFootball(matchHistory.getTime()))
                    );
                    matchHistoryNew.setBetA(matchHistoryNew.getBetA() + matchHistory.getAG());
                } else if (matchHistory.getBet() == 3) {
                    matchHistoryNew.getStrBet().add(
                            String.format(ServiceImpl.actionUtils.getConfigText("bet_football_msgbet", source, userid),
                                    ActionUtils.formatAG(matchHistory.getAG()),
                                    matchHistory.getTeamB(),
                                    ActionUtils.getTimeFootball(matchHistory.getTime()))
                    );
                    matchHistoryNew.setBetB(matchHistoryNew.getBetB() + matchHistory.getAG());
                }

                if(matchHistory.getBetWin() > 0){
                    matchHistoryNew.setWin(matchHistoryNew.getWin() + matchHistory.getBetWin() );
                }else{
                    matchHistoryNew.setWin(matchHistoryNew.getWin() - matchHistory.getAG());
                }

                if (matchHistory.getMatchResult() > 0) {
                    matchHistoryNew.setResult(true);
                } else {
                    matchHistoryNew.setResult(false);
                }

                mapID.put(matchHistory.getMatchID(), matchHistoryNew);
            }

            List<MatchHistoryNew> lsret = new ArrayList<MatchHistoryNew>();
            for (Integer key : mapID.keySet()) {
                lsret.add(mapID.get(key));
            }

            Collections.sort(lsret, new Comparator<MatchHistoryNew>() {
                @Override
                public int compare(MatchHistoryNew o1, MatchHistoryNew o2) {
                    if (o1.isResult() != o2.isResult()) {
                        if (o1.isResult()) return 1;
                        return -1;
                    }
                    if (o1.isResult())
                        return o1.getTime() > o2.getTime() ? -1 : 1;
                    return o1.getTime() > o2.getTime() ? 1 : -1;
                }
            });
            return lsret;
        }catch (Exception e){
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

//    public List<MatchHistoryNew> GameGetHistoryMatchNew(int source, int userid) {
//        try {
//            List<MatchHistory> ls = GameGetHistoryMatch(source, userid);
//            HashMap<Integer, MatchHistoryNew> mapID = new HashMap<Integer, MatchHistoryNew>();
//            for (int i = 0; i < ls.size(); i++) {
//                if (!mapID.containsKey(ls.get(i).getMatchID())) {
//                    MatchHistoryNew data = new MatchHistoryNew();
//                    data.setTeamA(ls.get(i).getTeamA());
//                    data.setTeamB(ls.get(i).getTeamB());
//                    data.setRateA1(ls.get(i).getRateA1());
//                    data.setRateA2(ls.get(i).getRateA2());
//                    data.setTime(ls.get(i).getT());
//
//                    data.setB1(ls.get(i).getB1());
//                    data.setB2(ls.get(i).getB2());
//                    data.setRateA(ls.get(i).getRateA());
//                    data.setRateB(ls.get(i).getRateB());
//
////                    if(ls.get(i).getBetWin() > 0){
////                        data.setWin(ls.get(i).getBetWin() );
////                    }else{
////                        data.setWin(-ls.get(i).getAG() );
////                    }
//
//                    if (ls.get(i).getBet() == 1) {
//                        data.getStrBet().add(
//                                String.format(ServiceImpl.actionUtils.getConfigText("bet_football_msgbet", source, userid),
//                                        ActionUtils.formatAG(ls.get(i).getAG()),
//                                        ls.get(i).getTeamA(),
//                                        ActionUtils.getTimeFootball(ls.get(i).getTime()))
//                        );
//                        data.setBetA(ls.get(i).getAG());
//                    } else if (ls.get(i).getBet() == 3) {
//                        data.getStrBet().add(
//                                String.format(ServiceImpl.actionUtils.getConfigText("bet_football_msgbet", source, userid),
//                                        ActionUtils.formatAG(ls.get(i).getAG()),
//                                        ls.get(i).getTeamB(),
//                                        ActionUtils.getTimeFootball(ls.get(i).getTime()))
//                        );
//                        data.setBetB(ls.get(i).getAG());
//                    }
//                    if (ls.get(i).getMatchResult() > 0) {
//                        data.setResult(true);
//                    } else {
//                        data.setResult(false);
//                    }
//                    mapID.put(ls.get(i).getMatchID(), data);
//                    ls.remove(i);
//                    --i;
//                }
//            }
//
//            Set<Integer> keys = mapID.keySet();
//            for (Integer key : keys) {
//                for (int i = 0; i < ls.size(); i++) {
//                    if (ls.get(i).getMatchID() == key) {
//                        if (ls.get(i).getBet() == 1) {
//                            mapID.get(key).setBetA(mapID.get(key).getBetA() + ls.get(i).getAG());
//
//                            mapID.get(key).getStrBet().add(
//                                    String.format(ServiceImpl.actionUtils.getConfigText("bet_football_msgbet", source, userid),
//                                            ActionUtils.formatAG(ls.get(i).getAG()),
//                                            ls.get(i).getTeamA(),
//                                            ActionUtils.getTimeFootball(ls.get(i).getTime()))
//                            );
//
//                        } else if (ls.get(i).getBet() == 3) {
//                            mapID.get(key).setBetB(mapID.get(key).getBetB() + ls.get(i).getAG());
//
//                            mapID.get(key).getStrBet().add(
//                                    String.format(ServiceImpl.actionUtils.getConfigText("bet_football_msgbet", source, userid),
//                                            ActionUtils.formatAG(ls.get(i).getAG()),
//                                            ls.get(i).getTeamB(),
//                                            ActionUtils.getTimeFootball(ls.get(i).getTime()))
//                            );
//                        }
//
//                        if(ls.get(i).getBetWin() > 0){
//                            mapID.get(key).setWin(mapID.get(key).getWin() + ls.get(i).getBetWin() );
//                        }else{
//                            mapID.get(key).setWin(mapID.get(key).getWin() - ls.get(i).getAG());
//                        }
//
//                        ls.remove(i);
//                        --i;
//                    }
//                }
//            }
//
//            List<MatchHistoryNew> lsret = new ArrayList<MatchHistoryNew>();
//            for (Integer key : keys) {
//                lsret.add(mapID.get(key));
//            }
//
//            Collections.sort(lsret, new Comparator<MatchHistoryNew>() {
//                @Override
//                public int compare(MatchHistoryNew o1, MatchHistoryNew o2) {
//                    if (o1.isResult() != o2.isResult()) {
//                        if (o1.isResult()) return 1;
//                        return -1;
//                    }
//                    if (o1.isResult())
//                        return o1.getTime() > o2.getTime() ? -1 : 1;
//                    return o1.getTime() > o2.getTime() ? 1 : -1;
//                }
//            });
//            return lsret;
//        } catch (Exception ex) {
//            _logger.error(ex.getMessage(), ex);
//            ex.printStackTrace();
//        }
//        return new ArrayList<MatchHistoryNew>();
//    }

    public List<MatchTop> GameGetTopMatch(UserInfo actionUser, int type) {
        int source = actionUser.getSource();
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call ServicesGetMatchPlayer(?) }");
            cs.setInt("Userid", 0);
            ResultSet rs = cs.executeQuery();
            List<MatchTop> lsret = new ArrayList<MatchTop>();
            //DateFormat readFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss aa");
            while (rs.next()) {
                MatchTop um = new MatchTop();
                String uname = rs.getString("UsernameLQ");
                if (uname.equals("")) {
                    uname = rs.getString("Username");
                }
                um.setAG(rs.getLong("BetWin"));
                um.setUsername(uname);
                um.setB1(rs.getInt("B1"));
                um.setB2(rs.getInt("B2"));
                um.setTeamA(rs.getString("TeamA") + " " + um.getB1());
                um.setTeamB(um.getB2() + " " + rs.getString("TeamB"));
                um.setT(rs.getTimestamp("StopTime").getTime());
                um.setState(rs.getInt("StateM"));
                if (source == ServerSource.THAI_SOURCE || source == ServerSource.IND_SOURCE) {
                    lsret.add(um);
                } else {
                    um.setType(rs.getInt("Type"));
                    if (um.getType() == 1) {
                        lsret.add(um);
                    }
                }
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            _logger.error("==>Error==>GameGetTopMatch: " + ex.getMessage(), ex);
            ex.printStackTrace();
            return new ArrayList<MatchTop>();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<MatchScore> GameGetTopScore(UserInfo actionUser, int type) {
        SqlService instance = SqlService.getInstanceBySource(actionUser.getSource());
        Connection conn = instance.getDbConnection();
        try {
//	    		[2:36:41 PM] hohaitac: [dbo].[GameGetStarList]
//	    				 @gameInfoId int,
//	    				 @term int,
//	    				 @starNum int
//	    				[2:36:56 PM] hohaitac: Duy gọi prod này nh
//	    				[2:37:32 PM] hohaitac: @gameInfoId: 8025  -> Bóng đá
//	    				[2:38:13 PM | Đã sửa 2:38:50 PM] hohaitac: @term: kỳ báo cáo cần lấy 
//	    				- 1: Tháng này
//	    				- 2: Tháng trước
//	    				- 11: Tuần này
//	    				- 12: Tuần trước
//	    				[2:39:14 PM] hohaitac: @starNum: số lượng cao thủ cần lấy

            CallableStatement cs = conn.prepareCall("{call GameGetStarList(?,?,?) }");
            cs.setInt("gameInfo", 8027); // 
            cs.setInt("term", type);
            cs.setInt("starNum", 20);
            ResultSet rs = cs.executeQuery();
            List<MatchScore> lsret = new ArrayList<MatchScore>();
            //DateFormat readFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss aa");
            while (rs.next()) {
                MatchScore um = new MatchScore();
                String uname = rs.getString("UsernameLQ");
                if (uname.equals("")) {
                    uname = rs.getString("Username");
                }
                um.setUsername(uname);
                um.setAvatar(rs.getInt("Avatar"));
                um.setScore(rs.getInt("Score"));
                um.setAG(rs.getLong("AG"));
                um.setFBID(Long.toString(rs.getLong("FacebookID")));

                lsret.add(um);
            }
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            _logger.error("==>Error==>GameGetTopScore: " + ex.getMessage(), ex);
            ex.printStackTrace();
            return new ArrayList<MatchScore>();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<MatchSiam> GameGetListMatch(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetMatch() }");
            ResultSet rs = cs.executeQuery();
            _logger.info("==>GameGetListMatch: " + rs.getRow());
            List<MatchSiam> lsret = new ArrayList<MatchSiam>();
            //DateFormat readFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss aa");
            while (rs.next()) {
                MatchSiam um = new MatchSiam();
                um.setId(rs.getInt("ID"));
                um.setWin(rs.getFloat("Win"));
                um.setLost(rs.getFloat("Lost"));
                um.setT(rs.getTimestamp("StopTime").getTime());
                um.setStopTime(rs.getTimestamp("StopTime"));
                um.setRateA1(rs.getFloat("A1"));
                um.setRateA2(rs.getFloat("A2"));
                um.setResultB1(rs.getInt("B1"));
                um.setResultB2(rs.getInt("B2"));
                um.setTeamA(rs.getString("TeamA"));
                um.setTeamB(rs.getString("TeamB"));
                um.setMatchStatus(rs.getInt("MatchStatus"));
                um.setResult(rs.getInt("Result"));

                lsret.add(um);
            }
            _logger.info("==>GameGetListMatch: source= " + source + " - lsMatchSiam: " + lsret.size());
            rs.close();
            cs.close();
            return lsret;
        } catch (Exception ex) {
            _logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
            return new ArrayList<MatchSiam>();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public List<MatchSiam> GameGetListTop(int source) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameFootball_GetListTop() }");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                //_logger.info("==>GameGetListTop: "+rs.getRow());
                MatchScore um = new MatchScore();
                String uname = rs.getString("UsernameLQ");
                if (uname.length() == 0) {
                    uname = rs.getString("Username");
                }
                um.setUsername(uname);
                //E.UserName, E.UsernameLQ, D.Total, D.TypeTop, E.FacebookID, E.Avatar
                um.setAvatar(rs.getInt("Avatar"));
                um.setAG(rs.getLong("Total"));
                um.setFBID(Long.toString(rs.getLong("FacebookID")));
                int type = Integer.parseInt(rs.getString("TypeTop"));
                if (type == 0) {
                    lsTopWeek.add(um);
                } else if (type == 1) {
                    lsTopMonth.add(um);
                } else {
                    lsTopAll.add(um);
                }
            }
            _logger.info("==>GameGetListMatch: source= " + source + " - lsTopWeek: " + lsTopWeek.size() + " - lsTopMonth: " + lsTopMonth.size()
                    + " - lsTopAll: " + lsTopAll.size());

            rs.close();
            cs.close();
        } catch (Exception ex) {
            _logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        return new ArrayList<MatchSiam>();
    }


    public void Process_BetFootball(JsonObject je, UserInfo actionUser, int playerId, UserController userController, ServiceRouter serviceRouter) {
        try {
            if (actionUser.getUnlockPass() == 0) {
                return;
            }

            _logger.info("==>Dat cuoc bong da: ID= " + je.get("ID").getAsInt() + " - " + je.get("Bet").getAsShort() + "- AG = " + je.get("AG").getAsInt());
            for (MatchSiam mat : ServiceImpl.lsMatchSiam) {
                if (mat.getId() == je.get("ID").getAsInt()) {
//	    		            	 1 - Được đặt cược
//	    		            	 2 - Không được đặt cược    	 
//	    		            	 3 - Đã nhập kết quả
//	    		            	 4 - Đã đóng trạng thái trận
                    if ((new Date()).before(mat.getStopTime())) {
                        if (actionUser.getAG() >= je.get("AG").getAsLong() && je.get("AG").getAsLong() > 0) {
                            short bet = je.get("Bet").getAsShort();
                            float betvalue = mat.getWin();
                            if (bet == 3) {
                                betvalue = mat.getLost();
                            }

                            userController.GameIMatchPlayer(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()), mat.getId(), bet, betvalue, je.get("AG").getAsInt(), actionUser.getVIP());
                            ServiceImpl.dicUser.get(playerId).IncrementMark(0 - je.get("AG").getAsInt());

                            JsonObject jo = new JsonObject();
                            jo.addProperty("evt", "betfootball");
                            jo.addProperty("AG", ServiceImpl.dicUser.get(playerId).getAG().intValue());
                            jo.addProperty("Cmd", ServiceImpl.actionUtils.getConfigText("bet_football_successfully", actionUser.getSource(), actionUser.getUserid()));
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        } else {
                            JsonObject jo = new JsonObject();
                            jo.addProperty("evt", "betfootball");
                            jo.addProperty("AG", ServiceImpl.dicUser.get(playerId).getAG().intValue());
                            jo.addProperty("Cmd", ServiceImpl.actionUtils.getConfigText("bet_football_err1", actionUser.getSource(), actionUser.getUserid()));
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        }
                    } else {
                        mat.setMatchStatus(2);
                        JSent act = new JSent();
                        act.setEvt("10");
                        act.setCmd(ServiceImpl.actionUtils.getConfigText("bet_football_timeout", actionUser.getSource(), actionUser.getUserid()));// Đã hết thời gian đặt cược, mởi bạn đặt lại trận khác
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            _logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }

    public List<MatchScore> getLsTopWeek() {
        return lsTopWeek;
    }

    public void setLsTopWeek(List<MatchScore> lsTopWeek) {
        this.lsTopWeek = lsTopWeek;
    }

    public List<MatchScore> getLsTopMonth() {
        return lsTopMonth;
    }

    public void setLsTopMonth(List<MatchScore> lsTopMonth) {
        this.lsTopMonth = lsTopMonth;
    }

    public List<MatchScore> getLsTopAll() {
        return lsTopAll;
    }

    public void setLsTopAll(List<MatchScore> lsTopAll) {
        this.lsTopAll = lsTopAll;
    }

}
