package com.cachebase.queue;

import com.athena.database.UserController;
import com.cachebase.libs.queue.QueueCommand;

import java.sql.Date;

public class LogIUUserExperienceDt_UsingCmd implements QueueCommand {
    private int conn;
    private int userId;
    private int logGameid;
    private int logLevel;
    private int logWin;
    private long logWinMark;
    private int logDeviceid;
    private Date logTime;

    public LogIUUserExperienceDt_UsingCmd(int conn, int Userid, int gameId, int iLevel, int iWin, java.sql.Date dtTime, long iWinMark, int deviceid){
        this.conn = conn;
        this.userId = Userid ;
        this.logGameid = gameId ;
        this.logLevel = iLevel ;
        this.logWin = iWin ;
        this.logWinMark = iWinMark ;
        this.logDeviceid = deviceid ;
        this.logTime = dtTime ;
    }

    @Override
    public void execute() {
        UserController ucontroller = new UserController();
        ucontroller.GameLogIUUserExperienceDtDb(conn, userId, logGameid, logLevel, logWin, logTime, logWinMark, logDeviceid);
    }
}
