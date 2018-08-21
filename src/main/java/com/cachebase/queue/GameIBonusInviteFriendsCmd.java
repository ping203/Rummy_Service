package com.cachebase.queue;

import com.athena.database.UserController;
import com.cachebase.libs.queue.QueueCommand;

public class GameIBonusInviteFriendsCmd implements QueueCommand {
    private int type;
    private int pid;
    private String toUser;
    private int ag;
    private String msg;
    private int source;

    public GameIBonusInviteFriendsCmd(int source, int type, int pid, String toUser, int ag, String msg) {
        this.source = source;
        this.type = type;
        this.pid = pid;
        this.toUser = toUser;
        this.ag = ag;
        this.msg = msg;
    }

    @Override
    public void execute() {
        try {
            UserController userController = new UserController();
            userController.gameIBonusInviteFriends(source, type, pid, toUser, ag, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
