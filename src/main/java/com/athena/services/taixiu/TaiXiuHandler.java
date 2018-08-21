package com.athena.services.taixiu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.dst.ServerSource;
import com.google.gson.JsonObject;

public class TaiXiuHandler {

    public long AgTai;
    public long AgXiu;
    public int userTai = 0;
    public int userXiu = 0;
    public int NTai;
    public int NXiu;
    public long AgBalanceTai;
    public long AgBalanceXiu;
    public long startTime;
    public long stopTime;
    public long AgBalanceTaiSave;
    public long AgBalanceXiuSave;
    public long AgTaiSave;
    public long AgXiuSave;

    public static int cauTai = 0;
    public static int cauXiu = 0;
    public int countBalance = 0;

    public int iRandomBalance;
    public int diffRandom;
    public boolean statusTaixiu;
    public static int timePlay = 40;
    public static int timeWait = 10;
    public ServiceRouter serviceRouter;
    public List<Integer> listIDOpen = new ArrayList<Integer>();
    public List<TaiXiuResult> listID = new ArrayList<TaiXiuResult>();
    public List<TaiXiuResult> listBet = new ArrayList<TaiXiuResult>();
    public static String strHistoryTaixiu = "";
    public static int idHighlow = 0;

    public ServiceRouter getServiceRouter() {
        return serviceRouter;
    }

    public void setServiceRouter(ServiceRouter serviceR) {
        serviceRouter = serviceR;
    }

    public TaiXiuHandler() {
        Thread thread1 = new Thread(new ThreadTaiXiuHandler(), "threadtaixiu");
        thread1.setDaemon(true);
        thread1.start();
    }

    public void SendActionTaixiu(String evt, int seconds, String strKetqua) {
        try {
            JsonObject send = new JsonObject();
            send.addProperty("evt", evt);
            send.addProperty("S", seconds);
            send.addProperty("R", strKetqua);
            Logger.getLogger("TaiXiuLog").info("==>Start Taixiu Alert:" + ActionUtils.gson.toJson(send) + "-" + ServiceImpl.getArrPidByTaixiu(0).length);
            ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
            int[] arrP = getArrPidByTaixiu();
            if (arrP.length > 0) {
                serviceRouter.dispatchToPlayers(arrP, csa);
            }
//            ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//            serviceRouter.dispatchToPlayers(ServiceImpl.getArrPidByTaixiu(0), csa);
            Logger.getLogger("TaiXiuLog").info("==>Gui ve client:" + ActionUtils.gson.toJson(send));
        } catch (Exception e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void SendActionTaixiu_New(String evt, int seconds, String strKetqua) {
        try {
            JsonObject send = new JsonObject();
            send.addProperty("evt", evt);
            send.addProperty("S", seconds);
            send.addProperty("R", strKetqua);

            send.addProperty("H", AgTai);
            send.addProperty("L", AgXiu);
            send.addProperty("NH", NTai);
            send.addProperty("NL", NXiu);
            send.addProperty("UH", userTai);
            send.addProperty("UL", userXiu);

            Logger.getLogger("TaiXiuLog").info("==>Start Taixiu Alert:" + ActionUtils.gson.toJson(send) + "-" + ServiceImpl.getArrPidByTaixiu(0).length);
            ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
            int[] arrP = getArrPidByTaixiu();
            if (arrP.length > 0) {
                serviceRouter.dispatchToPlayers(arrP, csa);
            }
//            ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//            serviceRouter.dispatchToPlayers(ServiceImpl.getArrPidByTaixiu(0), csa);
            Logger.getLogger("TaiXiuLog").info("==>Gui ve client:" + ActionUtils.gson.toJson(send));
        } catch (Exception e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class ThreadTaiXiuHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    //Tinh ket qua va cap nhat
                    startTime = System.currentTimeMillis();
                    //Date date = new Date(startTime);
//                	Logger.getLogger("TaiXiuLog").info("==>Start Bet:" + startTime + "-" + dicBet.size() + "-" + AgTai + "-" + AgXiu + "-" + date + "-" + idHighlow) ;
//                	dicBet.clear();
//                	listID.clear();
                    listBet.clear();
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.SECOND, timePlay + 5);
                    Timer finishTimer = new Timer();
                    finishTimer.schedule(new AlarmStopTaixiu(), c.getTime());

                    Calendar cBalanceFinish = Calendar.getInstance();
                    cBalanceFinish.add(Calendar.SECOND, timePlay + 1);
                    Timer balanceTimerFinish = new Timer();
                    balanceTimerFinish.schedule(new AlarmBalanceFinishTaixiu(), cBalanceFinish.getTime());

                    statusTaixiu = true;
                    AgTai = 0l;
                    AgXiu = 0l;
                    NTai = 0;
                    NXiu = 0;
                    AgBalanceTai = 0l;
                    AgBalanceXiu = 0l;
                    countBalance = 0;
                    iRandomBalance = 50;
                    if (cauTai > 3) {
                        iRandomBalance = 50 + (cauTai - 3) * 5;
                    } else if (cauXiu > 3) {
                        iRandomBalance = 50 - (cauXiu - 3) * 5;
                    }
                    if (iRandomBalance > 85) {
                        iRandomBalance = 85;
                    } else if (iRandomBalance < 15) {
                        iRandomBalance = 15;
                    }
                    diffRandom = ActionUtils.random.nextInt(100);
                    //Random cho viec can cua
                    int numberRan = 3;//ActionUtils.random.nextInt(6) ; //Toi da thuc hien 8 lan can cua
                    int jumpBalance = (int) timePlay / (numberRan + 5);
                    for (int i = 0; i < numberRan + 5; i++) {
                        int secondBalance = (i + 1) * jumpBalance - 1;// + ActionUtils.random.nextInt(jumpBalance - 2) ; //So giay
                        Timer balanceTimer = new Timer();
                        Calendar cBalance = Calendar.getInstance();
                        cBalance.add(Calendar.SECOND, secondBalance);
                        balanceTimer.schedule(new AlarmBalanceTaixiu(), cBalance.getTime());
                    }
                    //Send to All Client Start
                    SendActionTaixiu("highlowstart1", timePlay, "");
                    idHighlow++;
//                    Thread.sleep(50000l); //Delay 1 phut 30s
                    Thread.sleep((timePlay + timeWait + 6) * 1000); //Delay 1 phut 30s
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String GetHistory(int source, int userid) {
        try {
            Logger.getLogger("TaiXiuLog").info("==>Start Get Hisroty:" + userid + "-" + source);
            String keyHL = ServerDefined.getKeyCacheHighlow(source) + String.valueOf(userid - ServerDefined.userMap.get(source));
            Logger.getLogger("TaiXiuLog").info("==>Start Get Hisroty Key:" + keyHL);
            TaiXiuHistory objHistory = (TaiXiuHistory) UserController.getCacheInstance().get(keyHL);
            if (objHistory != null) {
                JsonObject send = new JsonObject();
                send.addProperty("evt", "highlowhistory1");
                send.addProperty("H", ActionUtils.gson.toJson(objHistory.getLsH()));
                Logger.getLogger("TaiXiuLog").info("==>Gui ve Client History:" + ActionUtils.gson.toJson(send));
                ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(userid, csa);
            } else {
                JsonObject send = new JsonObject();
                send.addProperty("evt", "highlowhistory1");
                send.addProperty("H", "");
                Logger.getLogger("TaiXiuLog").info("==>Gui ve Client History:" + ActionUtils.gson.toJson(send));
                ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(userid, csa);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void HighlowOpen(Integer userid) {
        Logger.getLogger("TaiXiuLog").info("==>HighlowOpen:" + userid);
        boolean t = false;
        for (int i = 0; i < listIDOpen.size(); i++) {
            if (listIDOpen.get(i).intValue() == userid.intValue()) {
                t = true;
                break;
            }
        }
        if (!t) {
            listIDOpen.add(userid);
        }
    }

    public void HighlowClose(int userid) {
        Logger.getLogger("TaiXiuLog").info("==>HighlowClose:" + userid);
        for (int i = 0; i < listIDOpen.size(); i++) {
            if (listIDOpen.get(i).intValue() == userid) {
                listIDOpen.remove(i);
                break;
            }
        }
    }

    public boolean CheckHighlow(int uid) {
        for (int i = 0; i < listBet.size(); i++) {
            if (listBet.get(i).getUserid() == uid) {
                return true;
            }
        }
        return false;
    }

    public void bet(UserInfo userInfo, int N, int M, int gameid, int source) {
        try {

            idHighlow++;
            boolean result = false;
            if (N == 1) {
                result = ServiceImpl.UpdateBetTaiXiu(userInfo.getUserid().intValue(), 0, M, System.currentTimeMillis(), idHighlow);
                if (result) {
                    listBet.add(new TaiXiuResult(idHighlow, 0, 0, M, 0, System.currentTimeMillis(), new int[3], userInfo.getUserid().intValue(), source, startTime + 40000));
                }
            } else {
                result = ServiceImpl.UpdateBetTaiXiu(userInfo.getUserid().intValue(), M, 0, System.currentTimeMillis(), idHighlow);
                if (result) {
                    listBet.add(new TaiXiuResult(idHighlow, 0, M, 0, 0, System.currentTimeMillis(), new int[3], userInfo.getUserid().intValue(), source, startTime + 40000));
                }
            }
            if (!result) {
                return;
            }

            //int userTai = 0 ;
            //int userXiu = 0 ;
            if (N == 1) {
                AgXiu = AgXiu + (long) M;
                NXiu++;
            } else {
                AgTai = AgTai + (long) M;
                NTai++;
            }

            userTai = 0;
            userXiu = 0;
            for (int i = 0; i < listBet.size(); i++) {
                if (listBet.get(i).getUserid() == userInfo.getUserid().intValue()) {
                    userTai = userTai + listBet.get(i).getH();
                    userXiu = userXiu + listBet.get(i).getL();
                }
            }
            JsonObject send = new JsonObject();
            send.addProperty("evt", "bethighlow1");
            send.addProperty("H", AgTai);
            send.addProperty("L", AgXiu);
            send.addProperty("NH", NTai);
            send.addProperty("NL", NXiu);
            send.addProperty("UH", userTai);
            send.addProperty("UL", userXiu);
            send.addProperty("AG", ServiceImpl.dicUser.get(userInfo.getUserid()).getAG().intValue());
            if (userInfo.getTableId() != 0) {
                send.addProperty("MB", ServiceImpl.GetMaxBetHighlow((int) userInfo.getGameid(), userInfo.getAS().intValue(), userInfo.getAG().intValue()));
            } else {
                send.addProperty("MB", userInfo.getAG().intValue());
            }
            Logger.getLogger("TaiXiuLog").info("==>Gui ve Client:" + ActionUtils.gson.toJson(send));
            ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(userInfo.getUserid().intValue(), csa);
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
    }

    private long GetGoldWinReal(long agwin) {
        return agwin + agwin * 95 / 100;
    }

    class AlarmStopTaixiu extends TimerTask {

        public void run() {
            try {
                Logger.getLogger("TaiXiuLog").info("==>Stop Taixiu:" + listBet.size() + "-" + AgTai + "-" + AgXiu + "-" + AgBalanceTai + "-" + AgBalanceXiu);
                stopTime = System.currentTimeMillis();
                long totalPay = 0l;
                long totalGold = 0l;
                long diffMs = stopTime - startTime;
                long diffSec = diffMs / 1000;
                long min = diffSec / 60;
                long sec = diffSec % 60;
                //Send to All Client Stop
                int dice1 = (new Random()).nextInt(6) + 1;
                int dice2 = (new Random()).nextInt(6) + 1;
                int dice3 = (new Random()).nextInt(6) + 1;
                int ketqua = dice1 + dice2 + dice3;
                int[] Arr = new int[3];
                Arr[0] = dice1;
                Arr[1] = dice2;
                Arr[2] = dice3;
                String strKetqua = String.valueOf(dice1) + "-" + String.valueOf(dice2) + "-" + String.valueOf(dice3) + ";";

//    			Logger.getLogger("TaiXiuLog").info("==>Stop Taixiu:" + dicBet.size() + "-" + ketqua + "-" + listID.size()) ;
                //Check xem co phai random lai
                for (int i = 0; i < listBet.size(); i++) {
                    TaiXiuResult obj = listBet.get(i);
                    if (obj.getUserid() == 0) {
                        continue; //Get gia
                    }
                    totalGold = totalGold + obj.getL() + obj.getH() - obj.getR();
                    if ((ketqua < 11 && (obj.getL() - obj.getR() > 0))
                            || (ketqua >= 11 && (obj.getH() - obj.getR() > 0))) { //Server phai tra tien
                        if (ketqua < 11) {
                            totalPay = totalPay + GetGoldWinReal(obj.getL() - obj.getR());// (obj.getL() - obj.getR()) + ((obj.getL() - obj.getR())/100) * 97 ;
                        } else {
                            totalPay = totalPay + GetGoldWinReal(obj.getH() - obj.getR());// (obj.getH() - obj.getR()) + ((obj.getH() - obj.getR())/100) * 97 ;
                        }
                    }
                }
                int randomreturn = (new Random()).nextInt(4);
                if ((totalPay - totalGold > 10000000) || (randomreturn == 3 && totalPay - totalGold > 2000000)) { //Server thua qua 10M ==> Random lai
                    dice1 = (new Random()).nextInt(6) + 1;
                    dice2 = (new Random()).nextInt(6) + 1;
                    dice3 = (new Random()).nextInt(6) + 1;
                    ketqua = dice1 + dice2 + dice3;
                    strKetqua = String.valueOf(dice1) + "-" + String.valueOf(dice2) + "-" + String.valueOf(dice3) + ";";

                    Arr[0] = dice1;
                    Arr[1] = dice2;
                    Arr[2] = dice3;
                }
                //SendActionTaixiu("highlowstop1", timeWait, strKetqua);
                SendActionTaixiu_New("highlowstop1", timeWait, strKetqua);
                totalPay = 0l;
                if (ketqua >= 11) {
                    cauTai++;
                    cauXiu = 0;
                } else {
                    cauTai = 0;
                    cauXiu++;
                }
                //Tinh ket qua va trao thuong
                for (int i = 0; i < listBet.size(); i++) {
                    TaiXiuResult obj = listBet.get(i);
                    if (obj.getUserid() == 0) {
                        continue; //Bet gia
                    }
                    long totalWin = 0;
                    if ((ketqua < 11 && (obj.getL() - obj.getR() > 0))
                            || (ketqua >= 11 && (obj.getH() - obj.getR() > 0))) {
                        if (ketqua < 11) {
                            totalPay = totalPay + GetGoldWinReal(obj.getL() - obj.getR()); //(obj.getL() - obj.getR()); // + ((obj.getL() - obj.getR())/100) * 97 ;
                            totalWin = GetGoldWinReal(obj.getL() - obj.getR()); //(obj.getL() - obj.getR()) + ((obj.getL() - obj.getR())/100) * 97 ;
                        } else {
                            totalPay = totalPay + GetGoldWinReal(obj.getH() - obj.getR()); //(obj.getH() - obj.getR()) + ((obj.getH() - obj.getR())/100) * 97 ;
                            totalWin = GetGoldWinReal(obj.getH() - obj.getR()); //(obj.getH() - obj.getR()) + ((obj.getH() - obj.getR())/100) * 97 ;
                        }
                    }
                    totalPay = totalPay + obj.getR();
                    listBet.get(i).setW(totalWin);
                    if (totalWin == 0) {
                        listBet.get(i).setW(obj.getR() - obj.getH() - obj.getL());
                    }
                    listBet.get(i).setArr(Arr);
                }
                for (int i = 0; i < listBet.size(); i++) {
                    if (listBet.get(i).getUserid() == 0) {
                        continue; //Bet gia
                    }
                    if (listBet.get(i).getSB() == 0) {
                        String keyHL = ServerDefined.getKeyCacheHighlow(listBet.get(i).getS()) + String.valueOf(listBet.get(i).getUserid() - ServerDefined.userMap.get(listBet.get(i).getS()));
                        Logger.getLogger("TaiXiuLog").info("==>Set Lai history:" + keyHL);
                        TaiXiuHistory objHistory = (TaiXiuHistory) UserController.getCacheInstance().get(keyHL);
                        if (objHistory == null) {
                            objHistory = new TaiXiuHistory(listBet.get(i).getUserid() - ServerDefined.userMap.get(listBet.get(i).getS()));
                        }
                        if (objHistory.getLsH().size() > 20) {
                            objHistory.RemoveObj();
                        }
                        objHistory.AddObj(listBet.get(i));
                        listBet.get(i).setSB(1);
                        long totalWin = 0l;
                        long totalWinReal = 0l;
                        int totalRefund = listBet.get(i).getR();
                        if (listBet.get(i).getW() >= 0) {
                            totalWin = listBet.get(i).getW() + listBet.get(i).getR();
                            totalWinReal = listBet.get(i).getW() + listBet.get(i).getR() - listBet.get(i).getH() - listBet.get(i).getL();
                        } else {
                            totalWin = totalWin + listBet.get(i).getR();
                            totalWinReal = listBet.get(i).getW();
                        }
                        if (i < listBet.size() - 1) {
                            for (int j = i + 1; j < listBet.size(); j++) {
                                if (listBet.get(j).getUserid() == listBet.get(i).getUserid()) {
                                    listBet.get(j).setSB(1);
                                    if (listBet.get(j).getW() >= 0) {
                                        totalWin = totalWin + listBet.get(j).getW() + listBet.get(j).getR();
                                        totalWinReal = totalWinReal + listBet.get(j).getW() + listBet.get(j).getR() - listBet.get(j).getH() - listBet.get(j).getL();
                                    } else {
                                        totalWin = totalWin + listBet.get(j).getR();
                                        totalWinReal = totalWinReal + listBet.get(j).getW();
                                    }
                                    totalRefund = totalRefund + listBet.get(j).getR();
                                    objHistory.AddObj(listBet.get(j));
                                }
                            }
                        }
                        Logger.getLogger("TaiXiuLog").info("==>Taixiu ketqua:" + listBet.get(i).getUserid() + "-" + ketqua + "-" + totalWin);
                        UserController.getCacheInstance().set(keyHL, objHistory, 0); //Set Lai cache history Highlow
                        //Update Diem Taixiu tren cache + va trong dic
                        ServiceImpl.UpdateKetquaTaiXiu(listBet.get(i).getUserid(), totalWin, totalWinReal, totalRefund, Arr, startTime, strKetqua, serviceRouter);
                    }
                }
                Logger.getLogger("TaiXiuLog").info("==>Stop Taixiu Insert into Balance:" + listBet.size() + "-" + AgTai + "-" + AgXiu + "-" + AgBalanceTai + "-" + AgBalanceXiu);
                if (ServiceImpl.IsRunServerTaiXiu) {
                    Logger.getLogger("TaiXiuLog").info("==>Insert into Balance:" + listBet.size() + "-" + AgTai + "-" + AgXiu + "-" + AgBalanceTai + "-" + AgBalanceXiu);

                    if (ServiceImpl.IsRunThai) {
                        (new UserController()).UpdateBalanceTaixiu(ServerSource.THAI_SOURCE, AgBalanceTaiSave, AgBalanceXiuSave, AgTaiSave, AgXiuSave, totalPay, startTime, strKetqua);
                    }
                }
                strHistoryTaixiu = strHistoryTaixiu + strKetqua;
                if (strHistoryTaixiu.length() > 120) {
                    strHistoryTaixiu = strHistoryTaixiu.substring(6);
                }
                Logger.getLogger("TaiXiuLog").info("The difference is " + min + " minutes and " + sec + " seconds.");
            } catch (Exception e) {
                // Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public int[] getArrPidByTaixiu() { //source - Game
        int[] ret = new int[listIDOpen.size()];
        for (int i = 0; i < listIDOpen.size(); i++) {
            ret[i] = listIDOpen.get(i).intValue();
        }
        Logger.getLogger("TaiXiuLog").info("==>Size realtime:" + listIDOpen.size());
        return ret;
    }

    private int getBalanceMin(int basicR) {
        return (int) (basicR / 4);
    }

    class AlarmBalanceTaixiu extends TimerTask {

        public void run() {
            try {
                Logger.getLogger("TaiXiuLog").info("==>Start Balance:" + System.currentTimeMillis() + "-" + AgTai + "-" + AgXiu + "-" + diffRandom);
                countBalance++;
                int agBasic = 1000;
                int csMin = 5;
                int csMax = 10;
                //Random so thang danh cua Tai va cua Xiu
                int numberDanh = ActionUtils.random.nextInt(50 - countBalance * 6) + 2;
                for (int i = 0; i < numberDanh; i++) {
                    int iRan = ActionUtils.random.nextInt(100);
                    if (iRandomBalance == 50) {
                        if (diffRandom < 15 && iRan > iRandomBalance) //Them vao cua Tai
                        {
                            iRan = ActionUtils.random.nextInt(100);
                        }
                        if (diffRandom > 14 && diffRandom < 30 && iRan <= iRandomBalance) //Them vao cua Tai
                        {
                            iRan = ActionUtils.random.nextInt(100);
                        }
                    }
                    int basicrandom = 50;
                    csMin = getBalanceMin(basicrandom);
                    int agBalance = csMin + ActionUtils.random.nextInt(basicrandom);
                    if (iRan > iRandomBalance) { //Them vao cua Tai
                        AgBalanceTai = AgBalanceTai + agBalance * agBasic;
                        AgTai = AgTai + agBalance * agBasic;
                        NTai = NTai + 1;
                        //Ad gia 1 cai Bet
                        listBet.add(new TaiXiuResult(-2, 0, agBalance * agBasic, 0, 0, System.currentTimeMillis(), new int[3], 0, 0, startTime + 40000));
                    } else { //Them vao cua Xiu
                        AgBalanceXiu = AgBalanceXiu + agBalance * agBasic;
                        AgXiu = AgXiu + agBalance * agBasic;
                        NXiu = NXiu + 1;
                        //Ad gia 1 cai Bet
                        listBet.add(new TaiXiuResult(-2, 0, 0, (agBalance + csMin) * agBasic, 0, System.currentTimeMillis(), new int[3], 0, 0, startTime + 40000));
                    }
                }
                long seconds = (System.currentTimeMillis() - startTime) / 1000;
                JsonObject sendTX = new JsonObject();
                sendTX.addProperty("evt", "highlowrealtime1");
                sendTX.addProperty("H", AgTai);
                sendTX.addProperty("L", AgXiu);
                sendTX.addProperty("NH", NTai);
                sendTX.addProperty("NL", NXiu);
                if (timePlay - 2 - seconds > 5) {
                    sendTX.addProperty("NT", 5);
                } else {
                    sendTX.addProperty("NT", timePlay - 1 - seconds);
                }
                sendTX.addProperty("T", timePlay - 1 - seconds);
                sendTX.addProperty("strH", strHistoryTaixiu);
                Logger.getLogger("TaiXiuLog").info("==>Finish Balance:" + ActionUtils.gson.toJson(sendTX) + "-" + System.currentTimeMillis() + "-" + listBet.size());
                if (getArrPidByTaixiu().length != 0) {
                    ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendTX).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayers(getArrPidByTaixiu(), csa);
                }
            } catch (Exception e) {
                // Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class AlarmBalanceFinishTaixiu extends TimerTask {

        public void run() {
            try {
                Logger.getLogger("TaiXiuLog").info("==>Start BalanceFinish:" + System.currentTimeMillis() + "-" + AgTai + "-" + AgXiu);
                statusTaixiu = false;
                //Chot chi so ghi DB
                AgTaiSave = AgTai;
                AgXiuSave = AgXiu;
                AgBalanceTaiSave = AgBalanceTai;
                AgBalanceXiuSave = AgBalanceXiu;
                for (int i = listBet.size() - 1; i > -1; i--) {
                    if (listBet.get(i).getH() > 0 && AgTai > AgXiu) {
                        if (AgTai - AgXiu > listBet.get(i).getH()) { //Refund lai toan bo
                            listBet.get(i).setR(listBet.get(i).getH());
                            AgTai = AgTai - listBet.get(i).getH();
                            NTai = NTai - 1;
                            if (listBet.get(i).getUserid() == 0) {
                                AgBalanceTai = AgBalanceTai - listBet.get(i).getR();
                            }
                        } else {
                            listBet.get(i).setR((int) (AgTai - AgXiu));
                            if (listBet.get(i).getUserid() == 0) {
                                AgBalanceTai = AgBalanceTai - listBet.get(i).getR();
                            }
                            AgTai = AgXiu;
                        }
                        if (listBet.get(i).getUserid() != 0) //Khong phai luot bet gia
                        {
                            ServiceImpl.RefundTaiXiu(listBet.get(i).getUserid(), listBet.get(i).getR(), 2, serviceRouter);
                        }
                    } else if (listBet.get(i).getL() > 0 && AgTai < AgXiu) {
                        if (AgXiu - AgTai > listBet.get(i).getL()) { //Refund lai toan bo
                            listBet.get(i).setR(listBet.get(i).getL());
                            AgXiu = AgXiu - listBet.get(i).getL();
                            if (listBet.get(i).getUserid() == 0) {
                                AgBalanceXiu = AgBalanceXiu - listBet.get(i).getR();
                            }
                            NXiu = NXiu - 1;
                        } else {
                            listBet.get(i).setR((int) (AgXiu - AgTai));
                            if (listBet.get(i).getUserid() == 0) {
                                AgBalanceXiu = AgBalanceXiu - listBet.get(i).getR();
                            }
                            AgXiu = AgTai;
                        }
                        if (listBet.get(i).getUserid() != 0) //Khong phai luot bet gia
                        {
                            ServiceImpl.RefundTaiXiu(listBet.get(i).getUserid(), listBet.get(i).getR(), 1, serviceRouter);
                        }
                    }
                    if (AgTai == AgXiu) {
                        break;
                    }
                }
                long seconds = (System.currentTimeMillis() - startTime) / 1000;
                JsonObject sendTX = new JsonObject();
                sendTX.addProperty("evt", "highlowrealtime1");
                sendTX.addProperty("H", AgTai);
                sendTX.addProperty("L", AgXiu);
                sendTX.addProperty("NH", NTai);
                sendTX.addProperty("NL", NXiu);
                sendTX.addProperty("NT", 0);
                sendTX.addProperty("T", timePlay - 1 - seconds);
                sendTX.addProperty("strH", strHistoryTaixiu);
                Logger.getLogger("TaiXiuLog").info("==>Finish Balance Finished:" + ActionUtils.gson.toJson(sendTX) + "-" + System.currentTimeMillis());
                if (getArrPidByTaixiu().length != 0) {
                    ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendTX).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayers(getArrPidByTaixiu(), csa);
                }
//                serviceRouter.dispatchToPlayers(ServiceImpl.getArrPidByTaixiu(0), csa);
            } catch (Exception e) {
                // Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
