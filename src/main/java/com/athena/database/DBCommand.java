package com.athena.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.Date;

import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.vng.tfa.common.SqlService;

public class DBCommand {

	public void GameLogExperience(int source, int uid, int gameid, long mark, int win, Date dtTime, long markWin,
			int operator) {
		try {
			LogCommand cmd = new LogCommand(CommandDefine.CMD_LOG_PLAYER, source, uid, gameid, mark, win, dtTime, markWin, operator);
            QueueManager.getInstance(UserController.queuename).put(cmd);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}

	public void LogPlayer(int source, int uid, int gameid,long mark,int win, Date dtTime, long markWin, int operator){
        SqlService instance = SqlService.getInstanceLogBySource(source);
        Connection conn  = instance.getDbConnection();
    	try {
    		CallableStatement cs = conn.prepareCall("{call LogPlayer(?,?,?,?,?,?,?) }");
    		cs.setInt("iUserId", uid);
    		cs.setDate("dtDate", new java.sql.Date(dtTime.getTime()));
    		cs.setLong("iWinMark", markWin);
            cs.setInt("iWin", win);
            cs.setInt("iGameID", gameid);
            cs.setLong("iLevel", mark);
            cs.setInt("iDev", operator);
            cs.execute(); 
            cs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally{
            instance.releaseDbConnection(conn);
        }
    }

	public void GameLogRevenue(int source, long mark, long revenue, int gameid, Date time) {
		try {
			LogCommand cmd = new LogCommand(CommandDefine.CMD_LOG_TABLE, source, mark, revenue, gameid, time);
            QueueManager.getInstance(UserController.queuename).put(cmd);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	public void LogGameRevenue(int source, long mark, long revenue, int gameid, Date dtTime){
        SqlService instance = SqlService.getInstanceLogBySource(source);
        Connection conn  = instance.getDbConnection();
    	try {
    		CallableStatement cs = conn.prepareCall("{call LogTable(?,?,?,?) }");
    		cs.setLong("Revenue", revenue);
            cs.setLong("iLevel", mark);
    		cs.setDate("dtDate", new java.sql.Date(dtTime.getTime()));
            cs.setInt("GameId", gameid);
            cs.execute();
            cs.close();
        } catch (Exception ex) {
        	ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
    }
}
