package com.athena.services.utils;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.log.LoggerKey;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.vo.UserInfo;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;
import com.cubeia.firebase.api.action.GameDataAction;
import com.dst.LanguageDefined;
import com.dst.MessageUtil;
import com.dst.ServerSource;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import com.google.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class ActionUtils {

    public static final Gson gson = new Gson();
    public static final JsonParser Parse = new JsonParser();
    public static final Random random = new Random();

    private static JsonObject textConfigVi = new JsonObject();
    private static JsonObject textConfigTh = new JsonObject();
    private static JsonObject textConfigIndo = new JsonObject();
    private static JsonObject textConfigIndia = new JsonObject();
    private static JsonObject textConfigEn = new JsonObject();
    private static JsonObject textConfigMym = new JsonObject();
    private static JsonObject adminLogin = new JsonObject();
    
    public static Logger logger_bot_ifrs = Logger.getLogger(LoggerKey.BOT_IFRS);

    private void loadDefaultLanguage() {
        try {
            String file = "../conf/text_en.json";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);
            String inputStr = convertStreamToString(inputStream);
            JsonElement element = gson.fromJson(inputStr, JsonElement.class);

            textConfigEn = element.getAsJsonObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadConfigText(int source) {
        try {
            String file = "";
            if (source == ServerSource.IND_SOURCE) {
                file = "../conf/text_in.json";
            } else if (source == ServerSource.THAI_SOURCE) {
                file = "../conf/text_th.json";
            } else if (source == ServerSource.MYA_SOURCE) {
                file = "../conf/text_mym.json";
            } else if (source == ServerSource.INDIA_SOURCE) {
                file = "../conf/text_en.json";
            } else {
                file = "../conf/text_en.json";
            }

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);
            String inputStr = convertStreamToString(inputStream);
            JsonElement element = gson.fromJson(inputStr, JsonElement.class);

            if (source == ServerSource.IND_SOURCE) {
                textConfigIndo = element.getAsJsonObject();
            } else if (source == ServerSource.THAI_SOURCE) {
                textConfigTh = element.getAsJsonObject();
            } else if (source == ServerSource.MYA_SOURCE) {
                textConfigMym = element.getAsJsonObject();
            } else {
                textConfigEn = element.getAsJsonObject();
                textConfigIndia = element.getAsJsonObject();
            }

            loadDefaultLanguage();

            try {
                file = "../conf/admin.json";
                inputStream = getClass().getClassLoader().getResourceAsStream(file);
                inputStr = convertStreamToString(inputStream);
                element = gson.fromJson(inputStr, JsonElement.class);
                setAdminLogin(element.getAsJsonObject());

                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getConfigText(String key, int source, int uid) {
        if(uid > ServerDefined.userMap.get(source)){
            uid = uid - ServerDefined.userMap.get(source);
        }
        try {
            if (ServiceImpl.dicLanguage.containsKey(uid)) {
                String language = ServiceImpl.dicLanguage.get(uid);
                System.out.println(language);
                return getServiceText(key, language);
            }else{
                return getServiceText(key, source);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return key;
        }
    }

    public String getServiceText(String key, String language) {
        try {
            language = LanguageDefined.detectLanguage(language);

            String text = MessageUtil.getMessageServiceResourceBundle(key, language);
            if(StringUtils.isNoneEmpty(text)) return text;

            return getConfigText(key, language);
        } catch (Exception e) {
            e.printStackTrace();
            return key;
        }
    }

    public String getServiceText(String key, int source){
        try{
            String text = MessageUtil.getMessageServiceResourceBundle(key, source);

            if(StringUtils.isNoneEmpty(text)) return text;
            return getConfigText(key, source);
        }catch (Exception e){
            e.printStackTrace();
            return key;
        }
    }

    private String getConfigText(String key, String language) {
        try {
            switch (language) {
                case LanguageDefined.EN:
                    if (textConfigEn.has(key)) {
                        return textConfigEn.get(key).getAsString();
                    } else {
                        return key;
                    }
                case LanguageDefined.MYM:
                    if (textConfigMym.has(key)) {
                        return textConfigMym.get(key).getAsString();
                    } else {
                        return key;
                    }
                default:
                    if (textConfigEn.has(key)) {
                        return textConfigEn.get(key).getAsString();
                    } else {
                        return key;
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return key;
        }
    }

    public String getConfigText(String key, int source) {
        try {
            if (source == ServerSource.THAI_SOURCE) {
                if (textConfigTh.has(key)) {
                    return textConfigTh.get(key).getAsString();
                } else {
                    return key;
                }
            } else if (source == ServerSource.IND_SOURCE) {
                if (textConfigIndo.has(key)) {
                    return textConfigIndo.get(key).getAsString();
                } else {
                    return key;
                }
            } else if (source == ServerSource.INDIA_SOURCE) {
                if (textConfigIndia.has(key)) {
                    return textConfigIndia.get(key).getAsString();
                } else {
                    return key;
                }
            } else if (source == ServerSource.MYA_SOURCE) {
                if (textConfigMym.has(key)) {
                    return textConfigMym.get(key).getAsString();
                } else {
                    return key;
                }
            } else {
                if (textConfigEn.has(key)) {
                    return textConfigEn.get(key).getAsString();
                } else {
                    return key;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return key;
        }
    }

    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println("==>Error==>MD5:" + md5);
            e.printStackTrace();
        }
        return "";
    }

    public static int ConvertVipPercentToMark(int source, int vip, int percent) {
        try {
            int vipbase = ServerDefined.promotion_vip.get(source).get(vip);
            int vipup = vipbase;
            if (vip < 10) {
                vipup = ServerDefined.promotion_vip.get(source).get(vip + 1);
            } else {
                vipup = vipbase * 2;
            }
            return vipbase + (vipup - vipbase) * percent / 100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String printA(int[] a) {
        String str = "[";
        for (int i = 0; i < a.length; i++) {
            str += a[i] + ",";
        }
        str += "]";
        return str;
    }

    public static String ValidString(String str) {
        str = str.trim();
        str = str.replaceAll("\\s+", "");
        str = str.toLowerCase();
        return str;
    }

    public static GameDataAction toDataAction(int playerId, int tableId, String action) {
        //String s = toString(action);
        GameDataAction gda = new GameDataAction(playerId, tableId);
        gda.setData(ByteBuffer.wrap(action.getBytes()));
        return gda;
    }

    public static GameDataAction toDataAction(int playerId, int tableId, Object action) {
        //String s = toString(action);
        GameDataAction gda = new GameDataAction(playerId, tableId);
        gda.setData(ByteBuffer.wrap(gson.toJson(action).getBytes()));
        return gda;
    }

    public static long getDayBetween2Dates(Date dt1, Date dt2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(dt1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(dt2);
        return (cal1.getTime().getTime() - cal2.getTime().getTime()) / (24 * 3600 * 1000);
    }
    //Check Valid UsernameLQ

    public static int CheckValidUsernameLQ(String strInput) {
        try {
            char[] arrayValid = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '.', '-'};
            if (strInput.length() > 50) {
                return 1;
            }
            if (strInput.trim().length() < 6) {
                return 2;
            }
            strInput = strInput.toLowerCase();
            char[] chars = strInput.toCharArray();
            if ((chars[0] == '_') || (chars[0] == '.')) {
                return 3; //First Char not '_', '.'
            }
            if ((chars[chars.length - 1] == '_') || (chars[chars.length - 1] == '.')) {
                return 4; //Last Char not '_', '.'
            }
            for (short x = 0; x < chars.length - 1; x++) {
                boolean t = false;
                for (short i = 0; i < arrayValid.length; i++) {
                    if (arrayValid[i] == chars[x]) {
                        t = true;
                        break;
                    }
                }
                if (!t) {
                    return 5; //Char not in Valid Array
                }
                if ((chars[x] == '_') && ((chars[x + 1] == '_') || (chars[x + 1] == '.'))) {
                    return 6;
                }
                if ((chars[x] == '.') && ((chars[x + 1] == '_') || (chars[x + 1] == '.'))) {
                    return 7;
                }
            }
            boolean tlast = false;
            for (short i = 0; i < arrayValid.length; i++) {
                if (arrayValid[i] == chars[chars.length - 1]) {
                    tlast = true;
                    break;
                }
            }
            if (!tlast) {
                return 8; //Char not in Valid Array
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 9;
        }
    }

    public static boolean checkPassWord(String pass) {
        try {
            if (pass.equals("123456") || pass.equals("1234567") || pass.equals("12345678")
                    || (pass.length() < 6) || pass.equals("123456789") || pass.equals("qwerty")) {
                return false;
            }
            if (CheckValidUsernameLQ(pass) != 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String formatPositiveAG(long pag) {
        long d = pag % 1000;
        long r = pag / 1000;
        String ds = "";
        if (d < 10) {
            ds = "00" + String.valueOf(d);
        } else if (d < 100) {
            ds = "0" + String.valueOf(d);
        } else {
            ds = String.valueOf(d);
        }

        if (r >= 1) {
            return formatPositiveAG(r) + "," + ds;
        } else {
            return String.valueOf(d);
        }
    }

    public static String formatAG(long ag) {
        if (ag >= 0) {
            return formatPositiveAG(ag);
        } else {
            return "-" + formatPositiveAG(-ag);
        }
    }

    public static String getTime(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String getTimeHistoryBankDay(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        return dateFormat.format(date);
    }

    public static String getTimeHistoryBankHour(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String getTimeFootball(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return dateFormat.format(date);
    }

    //Time
    public static String getStringTime(long time, String format) {
        String _rs = "";
        Date d = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        _rs = sdf.format(d);
        return _rs;
    }

    public static long getFirstTimeOfDate() {
        return strToTime(getStringTime(System.currentTimeMillis(), "yyyy-MM-dd") + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
    }

    public static long strToTime(String date, String format) {
        long rs = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date d = sdf.parse(date);
            rs = d.getTime();
        } catch (Exception ex) {

        }
        return rs;
    }

    public String convertStreamToString(java.io.InputStream inputstream) {
        String line = "";
        StringBuilder total = new StringBuilder();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total.toString();
    }

    public static void updateAGBOT(UserInfo user, long curAG, long agDB, int gameID) {
        try {
            UserInfoCmd cmdUpdate = new UserInfoCmd( user.getSource(),user.getUserid()- ServerDefined.userMap.get((int)user.getSource()),
            		 user.getVIP(),"GameUpdateBotNew",curAG-agDB);
            QueueManager.getInstance(UserController.queuename).put(cmdUpdate);

//             UserInfoCmd cmd = new UserInfoCmd("gameCreatePromotionBot", user.getSource(), user.getUserid()- ServerDefined.userMap.get((int)user.getSource()),
//            		 gameID, curAG-agDB, user.getDeviceId());
//            QueueManager.getInstance(UserController.queuename).put(cmd);
            ActionUtils.BotLogIFRS(user.getUserid(), agDB, curAG - agDB, gameID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void BotLogIFRS(int uid, long curAG, long agTransfer, int gameID) {
        try {

            // UID # AG # GAMEID # TABLEID # MARK # AGTRANFER # TIME
            logger_bot_ifrs.info(String.valueOf(uid) + "#"
                    + curAG + "#" + gameID + "#0#0#"
                    + "#" + agTransfer + "#"
                    + (new Date()).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkReconnect(short gameid) {
        if (gameid == GAMEID.TALA || gameid == GAMEID.BINH
                || gameid == GAMEID.CHAN || gameid == GAMEID.TIENLEN
                || gameid == GAMEID.SAM || gameid == GAMEID.SLAVE //Slave
                || gameid == GAMEID.DOMINOQQ //DominoQQ
                || gameid == GAMEID.POKER_TEXAS //PokerTexas 
                || gameid == GAMEID.POKDENGNEW
                || gameid == GAMEID.DUMMY || gameid == GAMEID.DUMMY_FAST
                || gameid == GAMEID.KHENG) {
            return true;
        } else {
            return false;
        }
    }

    public static JsonObject getAdminLogin() {
        return adminLogin;
    }

    public static void setAdminLogin(JsonObject adminLogin) {
        ActionUtils.adminLogin = adminLogin;
    }

    public static JsonObject getTextConfigIndia() {
        return textConfigIndia;
    }

    public static void setTextConfigIndia(JsonObject textConfigIndia) {
        ActionUtils.textConfigIndia = textConfigIndia;
    }

    public static boolean checkReconnectIndo(short gameid) {
        return gameid == GAMEID.BINH || gameid == GAMEID.DOMINOQQ || gameid == GAMEID.REMI;
    }

    public static boolean checkReconnectIndia(short gameid) {
        return gameid == GAMEID.RUMMY || gameid == GAMEID.RUMMY_FAST
                || gameid == GAMEID.TEENPATTI
                || gameid == GAMEID.LUDO
                || gameid == GAMEID.AB;
    }


    public static boolean checkReconnectMyanmar(short gameid) {
        return (gameid == GAMEID.MYANMAR_BURMESE_POKER || gameid == GAMEID.MYANMAR_SHAN_KOE_MEE_V1 || gameid == GAMEID.MYANMAR_TOMCUACA
                || gameid == GAMEID.SAMGONG_MYANMAR || gameid == GAMEID.MYANMAR_XITO || gameid == GAMEID.MYANMAR_SHAN_KOE_MEE_V2 ||
        gameid == GAMEID.MYANMAR_POKER || gameid == GAMEID.MYANMAR_SHOWS || gameid == GAMEID.LUDO || gameid == GAMEID.DOMINOQQ || gameid == GAMEID.MYANMAR_CHECKER) ;
    }

    public static long randomBetween2Number(long least, long bound) {
        return ThreadLocalRandom.current().nextLong(least, bound);
    }

    public static int randomBetween2Number(int least, int bound) {
        return ThreadLocalRandom.current().nextInt(least, bound);
    }
}
