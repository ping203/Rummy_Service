package com.athena.database;

import java.util.Date;

import com.athena.services.impl.ServiceImpl;
import com.cachebase.libs.queue.QueueCommand;

public class LogCommand implements QueueCommand {

	private String cmd = "";
	private int source;
	private int uid;
	private int gameid;
	private long mark;
	private int win;
	private Date time;
	private long markWin;
	private int operator;
	private long revenue;

	public LogCommand(String cmd, int source, int uid, int gameid, long mark, int win, Date dtTime, long markWin,int operator) {
		this.cmd = cmd;
		this.source = source;
		this.uid = uid;
		this.gameid = gameid;
		this.mark = mark;
		this.win = win;
		this.time = dtTime;
		this.markWin = markWin;
		this.operator = operator;
	}

	public LogCommand(String cmd, int source, long mark, long revenue, int gameid, Date time) {
		try{
			this.cmd = cmd;
			this.source = source;
			this.mark = mark;
			this.revenue = revenue;
			this.gameid = gameid;
			this.time = time;
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute() {
		try{
			switch (this.cmd) {
				case CommandDefine.CMD_LOG_PLAYER:
					ServiceImpl.dbCommand.LogPlayer(source, uid, gameid, mark, win, time, markWin, operator);
					break;
				case CommandDefine.CMD_LOG_TABLE:
					ServiceImpl.dbCommand.LogGameRevenue(source,mark,revenue,gameid,time);
					break;
			default:
				break;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
