package com.athena.services.bot;

import java.util.Random;
import org.apache.log4j.Logger;
import com.athena.services.vo.UserInfo;

public class RemiBotHandler extends BaseBotHandler {

    public RemiBotHandler(int source, int gameId) {
        GAMEID = gameId;
        MIN_AG_RATE = 200;
        MAX_AG_RATE = 500;
        MAX_ADD_AG = 100;
        loggerBot_ = Logger.getLogger("RemiBot");

        listMarkBot.add(new ConfigMarkBot(100, 11, (float) 0.2));
        listMarkBot.add(new ConfigMarkBot(500, 12, (float) 0.15));
        listMarkBot.add(new ConfigMarkBot(1000, 13, (float) 0.15));
        listMarkBot.add(new ConfigMarkBot(5000, 14, (float) 0.10));
        listMarkBot.add(new ConfigMarkBot(10000, 15, (float) 0.12));
        listMarkBot.add(new ConfigMarkBot(50000, 16, (float) 0.10));
        listMarkBot.add(new ConfigMarkBot(100000, 17, (float) 0.10));
        listMarkBot.add(new ConfigMarkBot(500000, 18, (float) 0.4));
        //listMarkBot.add(new ConfigMarkBot(1000000, 19, (float) 0.4));

        getListBot(source);
    }

    @Override
    public void getUserGame(int minAG, short gameId, int tableId, int Diamond) {
        super.getUserGame(minAG, gameId, tableId, Diamond);
    }

    @Override
    public UserInfo updateBotOnline(int pid, short isOnline) {
        return super.updateBotOnline(pid, isOnline);
    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark) {
        return super.UpdateBotMarkByName(uid, source, name, mark);
    }

    @Override
    public void BotCreateTable(int gameid, int mark) {
        super.BotCreateTable(gameid, mark);
    }

    @Override
    public void getListBot(int source) {
        super.getListBot(source);
    }

    @Override
    public int getVip(short type) {
        int vip = 1;
        int type100K = 0, type50K = 0, type10K = 0;
        for (ConfigMarkBot cmb : listMarkBot) {
            if (cmb.getMark() == 100000) {
                type100K = cmb.getType();
            } else if (cmb.getMark() == 50000) {
                type50K = cmb.getType();
            } else if (cmb.getMark() == 10000) {
                type10K = cmb.getType();
            }
        }

        if (type >= type100K) {
            vip = (new Random()).nextInt(5) + 2;
        } else if (type == type50K) {
            vip = (new Random()).nextInt(4) + 2;
        } else if (type <= type10K) {
            vip = (new Random()).nextInt(3) + 1;
        }

        return vip;
    }

    @Override
    public int getRate(int minAg) {
        return 200;
    }
}
