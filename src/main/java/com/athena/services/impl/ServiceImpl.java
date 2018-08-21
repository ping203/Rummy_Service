package com.athena.services.impl;

import com.athena.database.DBCommand;
import com.athena.database.IAPCommand;
import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.log.LoggerKey;
import com.athena.services.api.ServiceContract;
import com.athena.services.bot.*;
import com.athena.services.chat.ChatConstant;
import com.athena.services.chat.ChatHandler;
import com.athena.services.config.PromotionOnlineConfig;
import com.athena.services.constant.EvtDefine;
import com.athena.services.football.FootballHandler;
import com.athena.services.football.MatchSiam;
import com.athena.services.friends.FriendsHandler;
import com.athena.services.friends.MessageHandler;
import com.athena.services.handler.payment.PaymentPackageHandle;
import com.athena.services.handler.JackpotHandler;
import com.athena.services.handler.KeyCachedDefine;
import com.athena.services.handler.ServiceActionHandler;
import com.athena.services.handler.payment.PaymentPackage;
import com.athena.services.handler.payment.PigBank;
import com.athena.services.handler.payment.PigBankHandle;
import com.athena.services.impl.auth.AuthService;
import com.athena.services.impl.auth.AuthServiceImpl;
import com.athena.services.impl.india.FacebookIndiaLogin;
import com.athena.services.impl.india.IndiaLogin;
import com.athena.services.impl.indo.FacebookIndoLoginHandler;
import com.athena.services.impl.indo.FacebookIndoLoginHandler2;
import com.athena.services.impl.indo.IndoLoginHandler;
import com.athena.services.impl.indo.IndoLoginHandler2;
import com.athena.services.impl.indo.IndoTempLoginHandler;
import com.athena.services.impl.myanmar.MyanmarFacebookLogin1Handle;
import com.athena.services.impl.myanmar.MyanmarFacebookLoginHandle;
import com.athena.services.impl.myanmar.MyanmarLogin1Handle;
import com.athena.services.impl.thai.FacebookThaiLoginHandler2;
import com.athena.services.impl.thai.FacebookThaiNew1LoginHandler;
import com.athena.services.impl.thai.FacebookThaiPokdengLogin;
import com.athena.services.impl.thai.ThaiLoginHandler;
import com.athena.services.impl.thai.ThaiLoginHandler2;
import com.athena.services.impl.thai.ErrorLoginHandler;
import com.athena.services.impl.thai.VietTempLoginHandler;
import com.athena.services.ina.IAP_IOS_ITEM_IN_APP;
import com.athena.services.ina.PaymentIAP;
import com.athena.services.ina.PaymentIAP_IOS;
import com.athena.services.ina.PaymentIAP_IOSTemp;
import com.athena.services.ina.Security;
import com.athena.services.ina.VerifyIAPApple;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.promotion.PromotionType;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.GAMEID;
import com.athena.services.utils.LoginLoggingModel;
import com.athena.services.utils.Operator;
import com.athena.services.vo.*;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.login.LoginLocator;
import com.cubeia.firebase.api.login.PostLoginProcessor;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.dosprotect.DosProtector;
import com.cubeia.firebase.api.service.dosprotect.FrequencyRule;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;
import com.vng.tfa.common.Config;

//import com.vng.tfa.common.WebServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.athena.services.slot.SlotLogicGame;
import com.athena.services.slot.SlotMap;
import com.athena.services.taixiu.TaiXiuHandler;
import com.dst.GameUtil;
import com.dst.ServerSource;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.vng.tfa.common.ConfigKey;

import net.spy.memcached.MemcachedClient;

public class ServiceImpl implements Service, ServiceContract, LoginLocator, PostLoginProcessor {

    public static ActionUtils actionUtils = new ActionUtils();
    private ChatHandler chatHandler = new ChatHandler();
    public static ServiceActionHandler actionHandler = new ServiceActionHandler();
    public static Logger log = Logger.getLogger(ServiceImpl.class);
    public static Logger loggerLogin_ = Logger.getLogger(LoggerKey.LOGIN_DISCONNECT);
    public static Logger log_debug = Logger.getLogger(LoggerKey.DEBUG_SERVICE);
    private static Logger loggerPayment_ = Logger.getLogger("PaymentHandler");

    public static final int MAX_USER = 100000;
    public static ConcurrentLinkedHashMap<Integer, UserInfo> dicUser = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, MAX_USER);
    public static ConcurrentLinkedHashMap<Integer, UserNotSession> dicKet = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, MAX_USER);
    public static ConcurrentLinkedHashMap<String, String> dicCurrent = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, MAX_USER);

    public static ConcurrentLinkedHashMap<Integer, String> dicLanguage = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, MAX_USER);

    public static UserController userController = new UserController();
    public static List<DisplayRule> listDisplayRule = new ArrayList<DisplayRule>();
    public static List<UserLucky> listUserLucky = new ArrayList<UserLucky>();
    public static HashMap<Integer, Bot> mapBot = new HashMap<Integer, Bot>();
    public static HashMap<Integer, Boolean> mapRunBot = new HashMap<Integer, Boolean>();
    //Display Rule
    public static int gameCountRule_Payment = 10;
    public static int vipRule_Payment = 1;
    public static int promotionRule_Payment = 1;

    public static boolean IsRunThai = false;
    public static boolean IsRunMYM = false;
    public static boolean IsRunIndo = false;
    public static boolean IsRunIndia = false;
    public static boolean IsRunServerTaiXiu = false;

    private JackpotHandler jackpotHandler = new JackpotHandler();
    public static AtomicInteger incrementPid = new AtomicInteger(0);
    public static boolean isX2 = false;
    public static String arrTyleHilo = "1;2;3;64;32;18;12;8;6;6;6;6;8;12;18;32;64;30;180;1;1";
    public static int slotRandom[] = {5, 6, 10, 12, 14, 20, 30, 30, 40, 40, 60, 70, 70, 90, 100, 130, 130, 270, 270, 400, 680, 1500, 1500, 2800, 2800, 6200};
    public static int slotRate[] = {3, 4, 6, 8, 10, 15, 20, 20, 30, 30, 40, 50, 50, 70, 80, 100, 100, 200, 200, 300, 500, 1000, 1000, 2000, 2000, 5000};
    public static int slotType[] = {4, 3, 9, 8, 7, 6, 5, 9, 4, 8, 7, 3, 6, 5, 9, 4, 8, 3, 7, 6, 5, 0, 4, 0, 3, 0};
    public static int slotNum[] = {2, 2, 3, 3, 3, 3, 3, 4, 3, 4, 4, 3, 4, 4, 5, 4, 5, 4, 5, 5, 5, 3, 5, 4, 5, 5};
    public static boolean IsThaiTest = false;
    public static boolean IsAmazone = false;

    public static int agDailyFriendFace[] = {100, 500, 800, 1200, 2000};
    public static int conDailyFriendFace[] = {1, 10, 20, 50, 100};
    public static int agDailyPromotion[] = {100, 500, 800, 1200, 2000};
    public static int agDailyRotation[] = {100, 500, 1000, 2000, 5000, 10000};

    public static List<UserOnl> lsUserOnl = new ArrayList<UserOnl>();

    public static final JsonParser parser = new JsonParser();
    public static List<String> lsAlert2Player = new ArrayList<String>();
    public static List<String> lsAlert2Player68 = new ArrayList<String>();
    public static List<String> lsAlert2PlayerDautruong = new ArrayList<String>();
    public static List<String> lsAlert2PlayerFun52 = new ArrayList<String>();
    public static List<String> lsAlert2PlayerCam = new ArrayList<String>();
    public static List<String> lsAlert2PlayerThai = new ArrayList<String>();
    public static List<Integer> lsUserRemove = new ArrayList<Integer>();
    //private static List<String[]> lsUserOnline = new ArrayList<String[]>();
//    public static String nameCardLucky = "" ;
    public static String nameRoulette = "";
    //    public static boolean isLucky = false ;
    public static boolean isRoulette = false;
    //    public static String nameSuperLucky = "" ;
//    private static HashMap<Integer, String> dicNongTraiShop = new HashMap<Integer, String>();
    private static MemcachedClient connectionCache;
    public static ServiceRouter serviceRouter;
    private ServiceRegistry seviceRegistry;
    public static DBCommand dbCommand = new DBCommand();
    private IAPCommand iapCmd = new IAPCommand();
    //    public static int NumberWaiting = 0 ;
    //private SystemController sysController = new SystemController();
//Lang quat
//    private ZingLoginHandler handler = new ZingLoginHandler();
    private AdminLoginHandler adminHandler = new AdminLoginHandler();
    public static List<AlertPromotion> lsAlertPromotionThai = new ArrayList<AlertPromotion>();
    public static List<AlertPromotion> lsAlertPromotionHoki = new ArrayList<AlertPromotion>();

    //public static List<Auction> lsAuction = new ArrayList<Auction>();
    public static List<Match> lsMatch = new ArrayList<Match>();
    public static FootballHandler footballHandler = new FootballHandler();
    public static FriendsHandler friendHandler = new FriendsHandler();

    public static MessageHandler messageHandler = new MessageHandler();
    public static PromotionHandler promotionHandler = new PromotionHandler(userController);
    private BotHandler botHandler = new BotHandler();

    public static TaiXiuHandler taixiuHandler;
    public static int countGetList = 0;

    public static List<MatchSiam> lsMatchSiam = new ArrayList<MatchSiam>();
    public static List<MatchSiam> lsMatchHoki = new ArrayList<MatchSiam>();
    public static List<AlertPromotion> lsAlertPromotion = new ArrayList<AlertPromotion>();
    //    public static List<GiftCode> lsGiftCode = new ArrayList<GiftCode>() ;
    //public static List<AlertSchedule> lsAlertSchedule = new ArrayList<AlertSchedule>() ;
    public static List<TopGamer> lsTopGamer = new ArrayList<TopGamer>();
    public static List<TopGamer> lsTopHighLow = new ArrayList<TopGamer>();
    public static List<TopGamer> lsTopRich = new ArrayList<TopGamer>();
    public static List<String> lsDefaultGame = new ArrayList<String>();

    //Thai
//    private ThaiLoginHandler thaiHandler = new ThaiLoginHandler();
    private VietTempLoginHandler vietTempHandler = new VietTempLoginHandler();
    //    private FacebookThaiNewLoginHandler thaiFacebookHandler = new FacebookThaiNewLoginHandler();
    //Indo
    private IndoLoginHandler indoHandler = new IndoLoginHandler();
    private IndoTempLoginHandler indoTempHandler = new IndoTempLoginHandler();
    private FacebookIndoLoginHandler indoFacebookHandler = new FacebookIndoLoginHandler();
//3C
//    private BlueLoginHandler blueHandler = new BlueLoginHandler();
//    private BlueRegisterHandler blueRegisterHandler = new BlueRegisterHandler();
//    private FacebookLoginHandler facebookHandler = new FacebookLoginHandler();

    public static String ipAddressServer = "";
    //    public static List<Auction> lsAuction68 = new ArrayList<Auction>();
    public static List<Match> lsMatch68 = new ArrayList<Match>();
    public static List<MatchSiam> lsMatch68New = new ArrayList<MatchSiam>();
    public static List<AlertPromotion> lsAlertPromotion68 = new ArrayList<AlertPromotion>();
//Dau truong
//    private DautruongLoginHandler dautruongHandler = new DautruongLoginHandler();
//    private DautruongRegisterHandler dautruongeRegisterHandler = new DautruongRegisterHandler();
//    private FacebookDTLoginHandler facebookDTHandler = new FacebookDTLoginHandler();
//    private WebLoginHandler webDTHandler = new WebLoginHandler();

    //    public static List<Auction> lsAuctionDautruong = new ArrayList<Auction>();
    public static List<Match> lsMatchDautruong = new ArrayList<Match>();
    public static List<MatchSiam> lsMatchDTNew = new ArrayList<MatchSiam>();
    public static List<AlertPromotion> lsAlertPromotionDautruong = new ArrayList<AlertPromotion>();
    //52Fun
    //private Fun52LoginHandler fun52Handler = new Fun52LoginHandler();
//    private Fun52RegisterHandler fun52RegisterHandler = new Fun52RegisterHandler();
//    private FacebookFun52LoginHandler facebookFun52Handler = new FacebookFun52LoginHandler();
//    public static List<Auction> lsAuctionFun52 = new ArrayList<Auction>();
    public static List<Match> lsMatchFun52 = new ArrayList<Match>();
    public static List<MatchSiam> lsMatch52New = new ArrayList<MatchSiam>();
    public static List<AlertPromotion> lsAlertPromotionFun52 = new ArrayList<AlertPromotion>();
//Cam
//    private CamLoginHandler camHandler = new CamLoginHandler();
//    private CamRegisterHandler camRegisterHandler = new CamRegisterHandler();
//    private FacebookCamLoginHandler facebookCamHandler = new FacebookCamLoginHandler();

    private LoginBotHandler botLoginHandler = new LoginBotHandler();
    //private static Connection connectionCam;
    //private static Connection connectionCamLog;
//    public static List<Auction> lsAuctionCam = new ArrayList<Auction>();
    public static List<Match> lsMatchCam = new ArrayList<Match>();
    public static List<AlertPromotion> lsAlertPromotionCam = new ArrayList<AlertPromotion>();

    private static final Lock _createLock = new ReentrantLock();
    private static ServiceImpl _instance = null;
//    public static StatsMonitor reqStats = new StatsMonitor();

    public static ServiceContext context;

    public static ServiceImpl getInstance() {
        if (_instance == null) {
            _createLock.lock();
            try {
                _instance = new ServiceImpl();
            } catch (Exception ex) {
//                Logger.getLogger(ServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                _createLock.unlock();
            }
        }
        return _instance;
    }

    // connection cache
    public static MemcachedClient getConnectionCache() throws IOException {
        try {
            if (connectionCache == null) {
                connectionCache = new MemcachedClient(new InetSocketAddress("127.0.0.1", 11211));
            }
            return connectionCache;
        } catch (Exception e) {
//          System.out.println("Memcache:" + e.getMessage());
            log.error(e.getMessage(), e);
            connectionCache = null;
            return null;
        }
    }

    public Properties getClusterProperties(ServiceRegistry reg) {
        ClusterConfigProviderContract serv = context.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);

        // ClusterConfigProviderContract serv = reg.getServiceInstance(ClusterConfigProviderContract.class);
        Properties p = new Properties();
        for (ConfigProperty prop : serv.getAllProperties()) {
            PropertyKey key = prop.getKey();
            String name = key.getNamespace() + "." + key.getProperty();
            if (name.startsWith(".")) {
                name = key.getProperty();
            }
            p.put(name, prop.getValue());
        }
        return p;
    }

    @Override
    public void init(ServiceContext con) throws SystemException {
        try {
            //LogUtil.init("siamplay");
            context = con;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress()
                            && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        ipAddressServer = ia.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
            System.out.println("==>Error==>Get IPAddress:" + e.getMessage());
            ipAddressServer = "";
            e.printStackTrace();
        }
//    	String dataSource = getClusterProperties(this.context.getParentRegistry()).getProperty("client.gateway.max-number-of- sessions");
//    	loggerLogin_.info("Datasource:" + dataSource);

        try {
            System.out.println("TimeZone: " + TimeZone.getDefault());
            (new Config()).loadConfig();

            if (Config.getParam("Thai_Run").equals("1")) {
                IsRunThai = true;
            }

            if (Config.getParam(ConfigKey.MYANMAR_GAME_ENABLE).equals("1")) {
                IsRunMYM = true;
            }

            if (Config.getParam("Indo_Run").equals("1")) {
                IsRunIndo = true;
            }

            if (Config.getParam("India_Run").equals("1")) {
                IsRunIndia = true;
            }
            if (Config.getParam("Taixiu_Run").equals("1")) {
                IsRunServerTaiXiu = true;
            }
            countGetList = 0;
            //Bot

            if (Config.getParam("BotBinh_Run").equals("1")) {
                mapRunBot.put(GAMEID.BINH, true);
            }
            if (Config.getParam("BotPoker9K_Run").equals("1")) {
                mapRunBot.put(GAMEID.POKER9K, true);
            }
            if (Config.getParam("BotPoker9K2345_Run").equals("1")) {
                mapRunBot.put(GAMEID.POKER9K_2345, true);
            }
            if (Config.getParam("BotDummy_Run").equals("1")) {
                mapRunBot.put(GAMEID.DUMMY, true);
            }
            if (Config.getParam("BotDummyThai_Run").equals("1")) {
                mapRunBot.put(GAMEID.DUMMY_FAST, true);
            }
            if (Config.getParam("BotMinidice_Run").equals("1")) {
                mapRunBot.put(GAMEID.MINIDICE, true);
            }
            if (Config.getParam("BotXocdia_Run").equals("1")) {
                mapRunBot.put(GAMEID.XOCDIA, true);
            }
            if (Config.getParam("BotStreethilo_Run").equals("1")) {
                mapRunBot.put(GAMEID.STREETHILO, true);
            }
            if (Config.getParam("BotHilo_Run").equals("1")) {
                mapRunBot.put(GAMEID.HILO, true);
            }
            if (Config.getParam("BotDomino_Run").equals("1")) {
                mapRunBot.put(GAMEID.DOMINOQQ, true);
            }
            if (Config.getParam("BotTeenpatti_Run").equals("1")) {
                mapRunBot.put(GAMEID.TEENPATTI, true);
            }
            if (Config.getParam("BotRummy_Run").equals("1")) {
                mapRunBot.put(GAMEID.RUMMY, true);
                mapRunBot.put(GAMEID.RUMMY_FAST, true);
            }
            if (Config.getParam("BotPokdeng_Run").equals("1")) {
                mapRunBot.put(GAMEID.POKDENGNEW, true);
            }
            if (Config.getParam("BotTomCuaCa_Run").equals("1")) {
                mapRunBot.put(GAMEID.TOMCUACA, true);
            }
            if (Config.getParam("BotRemi_Run").equals("1")) {
                mapRunBot.put(GAMEID.REMI, true);
            }

            if (Config.getParam(ConfigKey.MYANMAR_BOT_BURMESE_POKER_ENABLE).equals("1")) {
                mapRunBot.put(GAMEID.MYANMAR_BURMESE_POKER, true);
            }

            if (Config.getParam(ConfigKey.MYANMAR_BOT_SHAN_KOE_MEE_ENABLE).equals("1")) {
                mapRunBot.put(GAMEID.MYANMAR_SHAN_KOE_MEE_V1, true);
            }

            if (Config.getParam(ConfigKey.MYANMAR_BOT_TOM_CUA_CA_ENABLE).equals("1")) {
                mapRunBot.put(GAMEID.MYANMAR_TOMCUACA, true);
            }

            if (Config.getParam(ConfigKey.MYANMAR_BOT_SHAN_KOE_MEE_V2_ENABLE).equals("1")) {
                mapRunBot.put(GAMEID.MYANMAR_SHAN_KOE_MEE_V2, true);
            }

            if (Config.getParam(ConfigKey.INDO_BOT_SAMGONG_ENABLE).equals("1")) {
                mapRunBot.put(GAMEID.INDO_SAMGONG, true);
            }

            if (Config.getParam(ConfigKey.MYANMAR_BOT_SHOWS_ENABLE).equals("1")) {
                mapRunBot.put(GAMEID.MYANMAR_SHOWS, true);
            }

            if (Config.getParam(ConfigKey.MYMANMAR_BOT_LUDO).equals("1")) {
                mapRunBot.put(GAMEID.MYANMAR_LUDO, true);
            }

            if (Config.getParam(ConfigKey.MYMANMAR_BOT_Domino).equals("1")) {
                mapRunBot.put(GAMEID.DOMINOQQ, true);
            }

            if (Config.getParam(ConfigKey.MYMANMAR_BOT_CHECKER).equals("1")) {
                mapRunBot.put(GAMEID.MYANMAR_CHECKER, true);
            }

            if (Config.getParam(ConfigKey.MYANMAR_BOT_BOHN).equals("1")) {
                mapRunBot.put(GAMEID.MYANMAR_BOHN, true);
            }
            //
            if (Config.getParam(ConfigKey.MYANMAR_BOT_RUMMY).equals("1")) {
                System.out.println("ConfigKey: " + ConfigKey.MYANMAR_BOT_RUMMY);
                mapRunBot.put(GAMEID.MYANMAR_RUMMY, true);
            }

            //IAP IOS
            if (Config.getParam("IAP_Sandbox").equals("1")) {
                VerifyIAPApple.isSandboxIAP = true;
            }

            System.out.println("==>mapRunBot: " + ActionUtils.gson.toJson(mapRunBot));
            //Chong Dos Login
            DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
            FrequencyRule rule = new FrequencyRule(1, 2000);
            dos.config("loginSpamRule", rule);
            FrequencyRule rule1 = new FrequencyRule(1, 1000);
            dos.config("joinSpamRule", rule1);
            FrequencyRule rule2 = new FrequencyRule(1, 1000);
            dos.config("leftSpamRule", rule2);
            FrequencyRule rule3 = new FrequencyRule(1, 10000); //10s
            dos.config("cashout", rule3);
            FrequencyRule rule4 = new FrequencyRule(1, 1000);
            dos.config("toprich", rule4);
            FrequencyRule rule5 = new FrequencyRule(1, 1000);
            dos.config("topgamer", rule5);

            dos.config("createT", new FrequencyRule(1, 1000));
            dos.config("promotion", new FrequencyRule(1, 1000));
            dos.config("shareFacebook", new FrequencyRule(1, 1000));
            dos.config("playnow", new FrequencyRule(1, 1000));
            actionHandler.configDOS(con);
        } catch (Exception e) {
            e.printStackTrace();
        }

        chatHandler.getLogChat();

        int source = ServerSource.THAI_SOURCE;

        //try { //Server ThaiLand
        if (IsRunThai) {
            userController.UpdateUserInfo(source, ipAddressServer);
            userController.GetListDisplayRule(source); //Get rule display Function
            actionUtils.loadConfigText(source);
            (new PromotionOnlineConfig()).loadPromotionSiamOnlineConfig();
            lsMatchSiam = footballHandler.GameGetListMatch(source);
            footballHandler.GameGetListTop(source);
            lsAlertPromotionThai = userController.GameGetAlertPromotion(source);
            lsDefaultGame = userController.GameGetListDefaultName(source);
            lsTopRich = userController.GameGetListTopRich(source);
            userController.GetPromotionPolicy(source);
            userController.GetListLuckyUser(source);
            actionHandler.loadListTopVip(source);
            actionHandler.getSelectGHandler().loadConfig();
//        	configUpdate_Siam = userController.GetConfigUpdate(source) ;

            if (mapRunBot.containsKey(GAMEID.DUMMY) && mapRunBot.get(GAMEID.DUMMY)) {
                mapBot.put(GAMEID.DUMMY, new DummyBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.DUMMY_FAST) && mapRunBot.get(GAMEID.DUMMY_FAST)) {
                mapBot.put(GAMEID.DUMMY_FAST, new DummyThaiBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.POKER9K) && mapRunBot.get(GAMEID.POKER9K)) {
                mapBot.put(GAMEID.POKER9K, new BotPoker9KHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.POKER9K_2345) && mapRunBot.get(GAMEID.POKER9K_2345)) {
                mapBot.put(GAMEID.POKER9K_2345, new BotPoker9K2345(source));
            }
            if (mapRunBot.containsKey(GAMEID.MINIDICE) && mapRunBot.get(GAMEID.MINIDICE)) {
                mapBot.put(GAMEID.MINIDICE, new MinidiceBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.XOCDIA) && mapRunBot.get(GAMEID.XOCDIA)) {
                mapBot.put(GAMEID.XOCDIA, new XocdiaBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.STREETHILO) && mapRunBot.get(GAMEID.STREETHILO)) {
                mapBot.put(GAMEID.STREETHILO, new StreetHiloBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.HILO) && mapRunBot.get(GAMEID.HILO)) {
                mapBot.put(GAMEID.HILO, new HiloBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.POKDENGNEW) && mapRunBot.get(GAMEID.POKDENGNEW)) {
                mapBot.put(GAMEID.POKDENGNEW, new PokdengBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.TOMCUACA) && mapRunBot.get(GAMEID.TOMCUACA)) {
                mapBot.put(GAMEID.TOMCUACA, new TomCuaCaBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.LUDO) && mapRunBot.get(GAMEID.LUDO)) {
                mapBot.put(GAMEID.LUDO, new LudobotHandler(source, GAMEID.LUDO));
            }
        }

        if (IsRunIndo) {
            source = ServerSource.IND_SOURCE;

            actionUtils.loadConfigText(source);
            userController.UpdateUserInfo(source, ipAddressServer);
            userController.GetPromotionPolicy(source);
            userController.GetListLuckyUser(source);
            actionHandler.loadListTopVip(source);
//        	configUpdate_Hoki = userController.GetConfigUpdate(source) ;

            lsMatchHoki = footballHandler.GameGetListMatch(source);
            lsAlertPromotionHoki = userController.GameGetAlertPromotion(source);
            lsDefaultGame = userController.GameGetListDefaultName(source);
            lsTopRich = userController.GameGetListTopRich(source);
            jackpotHandler.loadJackPot(userController, source);

            if (mapRunBot.containsKey(GAMEID.BINH) && mapRunBot.get(GAMEID.BINH)) {
                mapBot.put(GAMEID.BINH, new BinhBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.MINIDICE) && mapRunBot.get(GAMEID.MINIDICE)) {
                mapBot.put(GAMEID.MINIDICE, new MinidiceBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.DOMINOQQ) && mapRunBot.get(GAMEID.DOMINOQQ)) {
                mapBot.put(GAMEID.DOMINOQQ, new DominoBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.POKER9K_2345) && mapRunBot.get(GAMEID.POKER9K_2345)) {
                mapBot.put(GAMEID.POKER9K_2345, new BotPoker9K2345(source));
            }
            if (mapRunBot.containsKey(GAMEID.HILO) && mapRunBot.get(GAMEID.HILO)) {
                mapBot.put(GAMEID.HILO, new HiloBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.TOMCUACA) && mapRunBot.get(GAMEID.TOMCUACA)) {
                mapBot.put(GAMEID.TOMCUACA, new TomCuaCaBotHandler(source));
            }
            if (mapRunBot.containsKey(GAMEID.REMI) && mapRunBot.get(GAMEID.REMI)) {
                mapBot.put(GAMEID.REMI, new RemiBotHandler(source, GAMEID.REMI));
            }

            //update indo samgong
            if (mapRunBot.containsKey(GAMEID.INDO_SAMGONG) && mapRunBot.get(GAMEID.INDO_SAMGONG)) {
                System.out.println("PUT INDO SAMGONG");
                mapBot.put(GAMEID.INDO_SAMGONG, new IndoSamGongBotHandler(source));
            }
        }

        if (IsRunIndia) {
            source = ServerSource.INDIA_SOURCE;
            actionUtils.loadConfigText(source);
            userController.UpdateUserInfo(source, ipAddressServer);
            //lsAuction = userController.GameGetAuction(getConnection());
            lsMatch = userController.GameGetListMatch(source);
            footballHandler.GameGetListTop(source);
            lsAlertPromotion = userController.GameGetAlertPromotion(source);
            lsTopRich = userController.GameGetListTopRich(source);
            userController.GetPromotionPolicy(source);
            actionHandler.getSelectGHandler().loadConfig();

            if (mapRunBot.containsKey(GAMEID.TEENPATTI) && mapRunBot.get(GAMEID.TEENPATTI)) {
                mapBot.put(GAMEID.TEENPATTI, new TeenpattiBot(source));
            }

            if (mapRunBot.containsKey(GAMEID.RUMMY) && mapRunBot.get(GAMEID.RUMMY)) {
                mapBot.put(GAMEID.RUMMY, new RummyBot(source));
                mapBot.put(GAMEID.RUMMY_FAST, new RummyIndiaBot(source));
            }
        }

        if (IsRunMYM) {
            source = ServerSource.MYA_SOURCE;

            userController.UpdateUserInfo(source, ipAddressServer);
            userController.GetListDisplayRule(source); //Get rule display Function
            actionUtils.loadConfigText(source);

            lsMatchSiam = footballHandler.GameGetListMatch(source);
            footballHandler.GameGetListTop(source);
            (new PromotionOnlineConfig()).loadPromotionSiamOnlineConfig();
            lsAlertPromotionThai = userController.GameGetAlertPromotion(source);
            lsDefaultGame = userController.GameGetListDefaultName(source);
            lsTopRich = userController.GameGetListTopRich(source);

            userController.GetPromotionPolicy(source);
            userController.GetListLuckyUser(source);
            actionHandler.loadListTopVip(source);
            actionHandler.getSelectGHandler().loadConfig();

            if (mapRunBot.containsKey(GAMEID.MYANMAR_TOMCUACA) && mapRunBot.get(GAMEID.MYANMAR_TOMCUACA)) {
                mapBot.put(GAMEID.MYANMAR_TOMCUACA, new MyanmarTomCuaCaBotHandler(source));
            }

            if (mapRunBot.containsKey(GAMEID.MYANMAR_BURMESE_POKER) && mapRunBot.get(GAMEID.MYANMAR_BURMESE_POKER)) {
                mapBot.put(GAMEID.MYANMAR_BURMESE_POKER, new MyanmarBinhBotHandler(source));
            }

            if (mapRunBot.containsKey(GAMEID.MYANMAR_SHAN_KOE_MEE_V1) && mapRunBot.get(GAMEID.MYANMAR_SHAN_KOE_MEE_V1)) {
                mapBot.put(GAMEID.MYANMAR_SHAN_KOE_MEE_V1, new MyanmarPokdengBotHandler(source));
            }

            if (mapRunBot.containsKey(GAMEID.MYANMAR_SHAN_KOE_MEE_V2) && mapRunBot.get(GAMEID.MYANMAR_SHAN_KOE_MEE_V2)) {
                mapBot.put(GAMEID.MYANMAR_SHAN_KOE_MEE_V2, new MyanmarPokdengV2BotHandler(source));
            }

            if (mapRunBot.containsKey(GAMEID.MYANMAR_CHECKER) && mapRunBot.get(GAMEID.MYANMAR_CHECKER)) {
                mapBot.put(GAMEID.MYANMAR_CHECKER, new MyanmarCheckerBotHandler(source, GAMEID.MYANMAR_CHECKER));
            }

            if (mapRunBot.containsKey(GAMEID.MYANMAR_SHOWS) && mapRunBot.get(GAMEID.MYANMAR_SHOWS)) {
                mapBot.put(GAMEID.MYANMAR_SHOWS, new MyanmarShowsBotHandler(source));
            }

            if (mapRunBot.containsKey(GAMEID.LUDO) && mapRunBot.get(GAMEID.LUDO)) {
                mapBot.put(GAMEID.LUDO, new LudobotHandler(source, GAMEID.LUDO));
            }
            if (mapRunBot.containsKey(GAMEID.DOMINOQQ) && mapRunBot.get(GAMEID.DOMINOQQ)) {
                mapBot.put(GAMEID.DOMINOQQ, new DominoQQV2BotHandler(source));
            }

            if (mapRunBot.containsKey(GAMEID.MYANMAR_BOHN) && mapRunBot.get(GAMEID.MYANMAR_BOHN)) {
                mapBot.put(GAMEID.MYANMAR_BOHN, new BohnBotHandler(source));
            }
            //
            if (mapRunBot.containsKey(GAMEID.MYANMAR_RUMMY) && mapRunBot.get(GAMEID.MYANMAR_RUMMY)) {
                System.out.println("check mapBOT");
                mapBot.put(GAMEID.MYANMAR_RUMMY, new MyanmarRummyBotHandler(source));
            }

        }

        nameRoulette = "";
        isRoulette = false;

        Thread threadHappyHours = new Thread(new ThreadHappyHours(), "threadHappyHours");
        threadHappyHours.setDaemon(true);
        threadHappyHours.start();
    }

    @Override
    public void init(ServiceRegistry sr) {
        System.out.println("init:ServiceImpl");
        setSeviceRegistry(sr);
    }

    @Override
    public void start() {
        System.out.println("ServiceImpl starting......");
        //TimeIdle.schedule(new ToDoTask(), 20000l);
    }

    @Override
    public void stop() {
        System.out.println("ServiceImpl stoping......");
    }

    @Override
    public void destroy() {
        System.out.println("ServiceImpl destroy......");
        actionHandler.getRoom().destroy();
    }

    @Override
    public void setRouter(ServiceRouter router) {
        ServiceImpl.serviceRouter = router;
        botHandler.setRouter(router);
        if (IsRunServerTaiXiu) {
            taixiuHandler.setServiceRouter(router);
        }
    }
    //endregion

    @Override
    public void getUserGameBot(int mark, short gameId, int tableId, int Diamond) {
        try {
            System.out.println("check getUserGameBot in service: " + mapBot.size());
            Integer gid = (int) gameId;
            if (mapBot.containsKey(gid)) {
                System.out.println("ton tai gid: " +gid);
                mapBot.get(gid).getUserGame(mark, gameId, tableId, Diamond);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateBotOnline(int pid, short isOnline, int gameid) {
        try {
            if (mapBot.get(gameid) != null) //System.out.println("==>ServiceImpl==>updateBotOnline:"+pid+"-isOnline: "+isOnline);
            {
                mapBot.get(gameid).updateBotOnline(pid, isOnline);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long UpdateBotMarkChessByName(int uid, int source, String name, long mark, int typeU) { //type = 1 ==> Khong up date DB
        try {
            //return botBinhHandler.UpdateBotMarkChessByName(uid,source,name,mark,typeU);
            return mapBot.get(GAMEID.BINH).UpdateBotMarkChessByName(uid, source, name, mark, typeU);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Long UpdateBotDummyMarkChess(int uid, int source, String name, long mark, int typeU) { //type = 1 ==> Khong up date DB
        try {
            //return DummyBotHandler.UpdateBotMarkChessByName(uid,source,name,mark,typeU);
            return mapBot.get(GAMEID.DUMMY).UpdateBotMarkChessByName(uid, source, name, mark, typeU);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Long UpdateBotDummyThaiMarkChess(int uid, int source, String name, long mark, int typeU) { //type = 1 ==> Khong up date DB
        try {
            return mapBot.get(GAMEID.DUMMY_FAST).UpdateBotMarkChessByName(uid, source, name, mark, typeU);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Long UpdateBotPoker9KMark(int uid, long mark) {
        try {
            //return botPoker9KHandler.UpdateBotPoker9KMark(uid,mark);
            return mapBot.get(GAMEID.POKER9K).UpdateBotMarkChessByName(uid, mark);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public long UpdateBotMark(int uid, long mark, int gameid) {
        try {
            //System.out.println("==>UpdateBotMark: uid: "+uid+" - mark: "+mark+" - gameid"+gameid);
            if (mapBot.get(gameid) != null) {
                return mapBot.get(gameid).UpdateBotMarkByUID(uid, mark);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0l;
    }

    //Xu ly Chuyen AG
    private void Process_TransferAG(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                int isD = 0;
                if (je.has("D")) {
                    isD = je.get("D").getAsInt();
                }
                if (actionUser.getSource() == 1) {
                    sendErrorMsg(playerId, "Chức năng đang được nâng cấp.");
                    return;
                }
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                if (actionUser.getTableId() != 0) {
                    return;
                }
                int source = actionUser.getSource();
                int so_ag = je.get("AG").getAsInt();
                if (actionUser.getVIP() < 2 && source != ServerSource.THAI_SOURCE) {
                    sendErrorMsg(playerId, actionUtils.getConfigText("strBank_UnderVip", source, actionUser.getUserid()));
                } else if (actionUser.getTableId() != 0) {
                    sendErrorMsg(playerId, actionUtils.getConfigText("strBank_UnderGold", source, actionUser.getUserid()));
                } else if (actionUser.getOwnAmt() > 0) {
                    sendErrorMsg(playerId, actionUtils.getConfigText("strBank_OwnerGold", source, actionUser.getUserid()));
                } else if ((((actionUser.getAG() < so_ag) || (so_ag <= 10000)) && isD == 0)
                        || (((actionUser.getDiamond() < so_ag) || (so_ag <= 10000)) && isD == 1)) {
                    sendErrorMsg(playerId, actionUtils.getConfigText("strBank_NotGold", source, actionUser.getUserid()));
                } else {
                    int Error = -1;
                    Logger.getLogger("BankHandler").info("==>Process_TransferAG: - " + actionUser.getUsername()
                            + " - ag: " + actionUser.getAG().intValue() + " - je.get(Name): " + je.get("Name").getAsString() + " - ag chuyen: " + so_ag);
                    Error = userController.GameTransferAGDb(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()), so_ag, 0, je.get("Name").getAsString(), isD);
                    Logger.getLogger("BankHandler").info("==>Process_TransferAG: error = " + Error);
                    if (Error == 0) {
                        if (isD == 1) {
                            dicUser.get(playerId).DecrementDiamond(so_ag);
                        } else {
                            dicUser.get(playerId).DecrementMark(so_ag);
                        }
                        if (actionUser.getUserGame().equals(je.get("Name").getAsString())) {
                            dicUser.get(playerId).setCMsg(dicUser.get(playerId).getCMsg() + 100);//Cap nhat lai CMsg
                        }
                        if (isD == 1) {
                            userController.UpdateAGCache(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()), 0, actionUser.getVIP(), 0 - so_ag);
                        } else {
                            userController.UpdateAGCache(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()), 0 - so_ag, actionUser.getVIP(), 0);
                        }
                        if (actionUser.getSource() == ServerSource.THAI_SOURCE) {
                            Logger.getLogger("BANKLOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get((int) actionUser.getSource())) + "#" + dicUser.get(playerId).getAG().intValue() + "#7999#0#" + isD + "#" + String.valueOf(0 - so_ag) + "#" + String.valueOf((new Date()).getTime()));
                        }
                        //Send Alert for Success
                        JSent act = new JSent();
                        act.setEvt("13");
                        act.setCmd(actionUtils.getConfigText("strBank_Success", actionUser.getSource(), actionUser.getUserid()));
                        act.setAg(dicUser.get(playerId).getAG().longValue());
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                        //Send Alert if ToUser is online
                        int pidRec = isUserOnl(je.get("Name").getAsString());
                        if (pidRec > 0) {
                            loadMailTransferAGNew(pidRec, 1);
                            JsonObject send = new JsonObject();
                            send.addProperty("evt", "15");
                            send.addProperty("T", 11);
                            ClientServiceAction csaS = new ClientServiceAction(pidRec, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(pidRec, csaS);
                        }
                    } else if (Error == -1) {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strBank_Fail", actionUser.getSource(), actionUser.getUserid()));
                    } else {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strBank_Fail1", actionUser.getSource(), actionUser.getUserid()));
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
    }

    public void PaymentSendToClient(int userid) {
        try {

            userController.RemoveUserInfoByUserid(ServerSource.THAI_SOURCE, userid);

        } catch (Exception e) {
            e.printStackTrace();
            // handle exception
        }
    }

    public void PaymentIndo(String transid, int source) {
        try {
            loggerPayment_.info("==>Service Start:" + transid + "-" + source);
            String strReturn = userController.UpdateAfterPaymentDB(9, transid);
            loggerPayment_.info("==>Service FinishDB:" + transid + "-" + strReturn);
            int playerId = Integer.parseInt(strReturn.split(";")[4]);
            int userId = playerId;
            if (playerId > 0) {
                int ag = Integer.parseInt(strReturn.split(";")[0]);
                int vip = Integer.parseInt(strReturn.split(";")[1]);
                int gold = Integer.parseInt(strReturn.split(";")[2]);
                int agAdd = Integer.parseInt(strReturn.split(";")[3]);
                int sourceRead = 0;

                if (ServerDefined.userMap.containsKey(source)) {
                    sourceRead = ServerDefined.userMap.get(source);
                }
                loggerPayment_.info("==>Service Source Read:" + transid + "-" + sourceRead);
                playerId = playerId + sourceRead;
                if (dicUser.containsKey(playerId)) {
                    dicUser.get(playerId).setAG((long) ag);
                    dicUser.get(playerId).setVIP((short) vip);
                    dicUser.get(playerId).setLQ(gold);
                    //Add nguoc lai Cache
                    UserInfo ulogin = new UserInfo();
                    ulogin.setUserid(userId);
                    ulogin.setFacebookid(dicUser.get(playerId).getFacebookid());
                    ulogin.setGameid(dicUser.get(playerId).getGameid());
                    ulogin.setDeviceId(dicUser.get(playerId).getDeviceId());
                    ulogin.setSource((short) source);
                    ulogin.setOperatorid(dicUser.get(playerId).getOperatorid());
                    userController.GetUserInfoByUserid(source, ulogin, ServiceImpl.ipAddressServer);

                    loggerPayment_.info("==>Service Source SendTo Client:" + transid + "-" + playerId);
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "paymentafter");
                    jo.addProperty("AG", ag);
                    jo.addProperty("AGAdd", agAdd);
                    jo.addProperty("Vip", vip);
                    jo.addProperty("Gold", gold);
                    loggerPayment_.info("==>Service Source Read:" + transid + "-" + ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                }
            }
        } catch (Exception e) {
            loggerPayment_.error("==>Error==>PaymentIndo:" + transid + "-" + e.getMessage());
            // handle exception
        }
    }

    //Xu ly Mat khau cap 2
    /*private void Process_Lock(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (je.get("T").getAsInt() == 1) {
                    if (actionUser.getPassLock().length() == 0 && je.get("P").getAsString().length() == 6) {
                        if (actionUser.getSource() == 1) {
                            userController.GameUPassLock(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()), je.get("P").getAsString());
                        }
                        dicUser.get(playerId).setPassLock(je.get("P").getAsString());
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "12");
                        jo.addProperty("T", "1");
                        if (actionUser.getSource() == 5) {
                            jo.addProperty("data", "<vi>Bạn đặt mật khẩu thất bại.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ທ່ານ​ຕັ້ງ​ລະ​ຫັດ​ລົ້ມຫຼຽ​ວ</la>");
                        } else {
                            jo.addProperty("data", "Đặt mật khẩu thành công.");
                        }
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        if (actionUser.getSource() == 5) {
                            sendErrorMsg(playerId, "<vi>Bạn đặt mật khẩu thất bại.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ທ່ານ​ຕັ້ງ​ລະ​ຫັດ​ລົ້ມຫຼຽ​ວ</la>");
                        } else {
                            sendErrorMsg(playerId, "Bạn đặt mật khẩu thất bại.");
                        }
                    }
                } else if (je.get("T").getAsInt() == 2) {
                    if (actionUser.getPassLock().length() > 0) {
                        if (actionUser.getCreateTime().compareTo(actionUser.getDelPassLock()) == 0) {
                            userController.GameDPassLock(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()));
                            dicUser.get(playerId).setDelPassLock((new Date()).getTime() + 24 * 3600 * 1000 * 7);
                            JsonObject jo = new JsonObject();
                            jo.addProperty("evt", "12");
                            jo.addProperty("T", "2");
                            if (actionUser.getSource() == 5) {
                                jo.addProperty("data", "<vi>Mật khẩu sẽ mở sau 7 ngày.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ລະ​ຫັດ​ຈະ​ເປີ​ດຫຼັງ 7 ມື້</la>");
                            } else {
                                jo.addProperty("data", "Mật khẩu sẽ mở sau 7 ngày.");
                            }
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        }
                    }
                } else if (je.get("T").getAsInt() == 3) {
                    if (actionUser.getCreateTime().compareTo(actionUser.getDelPassLock()) != 0) {
                        int source = actionUser.getSource();
                        userController.GameHPassLock(source, actionUser.getUserid() - ServerDefined.userMap.get(source));

                        if (actionUser.getSource() == 5) {
                            sendErrorMsg(playerId, "<vi>Hủy mật khẩu thành công.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ຍົກ​ເລີກ​ລ​ະ​ຫັດ​ສຳ​ເລັດ</la>");
                        } else {
                            sendErrorMsg(playerId, "Hủy mật khẩu thành công.");
                        }
                    }
                } else if (je.get("T").getAsInt() == 4) {
                    if (actionUser.getPassLock().equals(je.get("P").getAsString())
                            && je.get("NP").getAsString().length() == 6) {
                        userController.GameUPassLock(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()), je.get("NP").getAsString());
                        dicUser.get(playerId).setPassLock(je.get("NP").getAsString());
                        if (actionUser.getSource() == 5) {
                            sendErrorMsg(playerId, "<vi>Bạn đổi mật khẩu thành công.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ທ່ານ​ປ່ຽນ​ລະ​ຫັດ​ສຳ​ເລັດ</la>");
                        } else {
                            sendErrorMsg(playerId, "Bạn đổi mật khẩu thành công.");
                        }
                    } else {
                        if (actionUser.getSource() == 5) {
                            sendErrorMsg(playerId, "<vi>Bạn đổi mật khẩu thất bại.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ທ່ານ​ປ່ຽນ​ລະ​ຫັດ​ສຳ​ເລັດ</la>");
                        } else {
                            sendErrorMsg(playerId, "Bạn đổi mật khẩu thất bại.");
                        }
                    }
                } else if (je.get("T").getAsInt() == 5) {
                    if (actionUser.getPassLock().equals(je.get("P").getAsString())) {
                        dicUser.get(playerId).setPass(true);
                        dicUser.get(playerId).setUnlockPass((short) 1);
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "12");
                        jo.addProperty("T", "5");
                        if (actionUser.getSource() == 5) {
                            jo.addProperty("data", "<vi>Mở mật khẩu thành công.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ເປີດ​ລະ​ຫັດ​ສຳ​ເລັດ</la>");
                        } else {
                            jo.addProperty("data", "Mở mật khẩu thành công.");
                        }
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                        if (actionUser.getCreateTime().compareTo(actionUser.getDelPassLock()) != 0) {
                            int source = actionUser.getSource();
                            userController.GameHPassLock(source, actionUser.getUserid() - ServerDefined.userMap.get(source));
                            if (actionUser.getSource() == 5) {
                                sendErrorMsg(playerId, "<vi>Hủy mở mật khẩu thành công.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ຍົກ​ເລີກ​ລ​ະ​ຫັດ​ສຳ​ເລັດ</la>");
                            } else {
                                sendErrorMsg(playerId, "Hủy mở mật khẩu thành công.");
                            }
                        }
                    } else {
                        if (actionUser.getSource() == 5) {
                            sendErrorMsg(playerId, "<vi>Mật khẩu không chính xác.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ລະ​ຫັດ​ບໍ່​ຖືກ​ຕ້ອງ</la>");
                        } else {
                            sendErrorMsg(playerId, "Mật khẩu không chính xác.");
                        }
                    }
                } else if (je.get("T").getAsInt() == 6) {
                    dicUser.get(playerId).setPass(false);
                    if (actionUser.getSource() == 5) {
                        sendErrorMsg(playerId, "<vi>Hủy mật khẩu thành công.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ຍົກ​ເລີກ​ລ​ະ​ຫັດ​ສຳ​ເລັດ</la>");
                    } else {
                        sendErrorMsg(playerId, "Hủy mật khẩu thành công.");
                    }
                } else if (je.get("T").getAsInt() == 7) {
                    if (actionUser.getPassLock().equals(je.get("P").getAsString())) {
                        int source = actionUser.getSource();
                        userController.GameDDPassLock(source, actionUser.getUserid() - ServerDefined.userMap.get(source));
                        dicUser.get(playerId).setPassLock("");
                        dicUser.get(playerId).setPass(false);
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "12");
                        jo.addProperty("T", "7");
                        if (actionUser.getSource() == 5) {
                            jo.addProperty("data", "<vi>Hủy mật khẩu thành công.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ຍົກ​ເລີກ​ລ​ະ​ຫັດ​ສຳ​ເລັດ</la>");
                        } else {
                            jo.addProperty("data", "Hủy mật khẩu thành công.");
                        }
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        if (actionUser.getSource() == 5) {
                            sendErrorMsg(playerId, "<vi>Mật khẩu không chính xác.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ລະ​ຫັດ​ບໍ່​ຖືກ​ຕ້ອງ</la>");
                        } else {
                            sendErrorMsg(playerId, "Mật khẩu không chính xác.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
        }
    }*/
//    //Xu ly Ngan hang
//    private void Process_Bank(JsonObject je, UserInfo actionUser, int playerId) {
//        try {
//            synchronized (dicUser) {
//                if (actionUser.getUnlockPass() == 0) {
//                    return;
//                }
//                if (je.get("T").getAsInt() == 1) {
//                    if ((je.get("AG").getAsInt() > 0) && (je.get("AG").getAsInt() + actionUser.getOwnAmt()) <= actionUser.getMaxAmt()) {
//                        int Error = -1;
//                        int source = actionUser.getSource();
//                        Error = userController.GameAGVay(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("AG").getAsInt());
//                        if (Error >= 0) {
//                            userController.UpdateLoanAG(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("AG").getAsInt());
//                            dicUser.get(playerId).IncrementMark(je.get("AG").getAsInt());
//                            dicUser.get(playerId).setOwnAmt(actionUser.getOwnAmt() + je.get("AG").getAsInt());
//                            JsonObject jo = new JsonObject();
//                            jo.addProperty("evt", "14");
//                            jo.addProperty("T", "1");
//                            jo.addProperty("data", je.get("AG").getAsInt());
//                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
//                            serviceRouter.dispatchToPlayer(playerId, csa);
//                        } else {
////                            if (actionUser.getSource() == 9) {
////                                sendErrorMsg(playerId, actionUtils.strBank_LoanErr1_TH);
////                            } else {
//                                sendErrorMsg(playerId, actionUtils.getConfigText("strBank_LoanErr1",actionUser.getSource()));
//                            //}
//                        }
//                    } else {
////                        if (actionUser.getSource() == 9) {
////                            sendErrorMsg(playerId, actionUtils.strBank_LoanErr2_TH);
////                        } else {
//                            sendErrorMsg(playerId, actionUtils.getConfigText("strBank_LoanErr2",actionUser.getSource()));
//                        //}
//                    }
//                } else if (je.get("T").getAsInt() == 2) {
//                    if (actionUser.getOwnAmt() <= actionUser.getAG()) {
//                        int source = actionUser.getSource();
//                        userController.GameAGTra(source, actionUser.getUserid() - ServerDefined.userMap.get(source));
//                        if (source == 1) {
//                            userController.UpdatePayAG(source, actionUser.getUserid());
//                        }
//                        dicUser.get(playerId).DecrementMark(actionUser.getOwnAmt());
//                        dicUser.get(playerId).setOwnAmt(0);
//                        JsonObject jo = new JsonObject();
//                        jo.addProperty("evt", "14");
//                        jo.addProperty("T", "2");
//                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
//                        serviceRouter.dispatchToPlayer(playerId, csa);
//                    } else {
////                        if (actionUser.getSource() == 9) {
////                            sendErrorMsg(playerId, actionUtils.strBank_PayErr_TH);
////                        } else {
//                            sendErrorMsg(playerId, actionUtils.getConfigText("strBank_PayErr",actionUser.getSource()));
//                        //}
//                    }
//                }
//            }
//        } catch (Exception e) {
//            // handle exception
//            System.out.println("==>Error==>Process_Bank:" + e.getMessage());
//        }
//    }
    //Xu ly Mail
    private void Process_Mail(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                if (je.get("T").getAsInt() == 1) { //Doc thu
                    for (int i = 0; i < actionUser.getListMsg().size(); i++) {
                        if (actionUser.getListMsg().get(i).getId() == je.get("ID").getAsInt()) {
                            dicUser.get(playerId).getListMsg().get(i).setS(true);
                            dicUser.get(playerId).setCMsg(dicUser.get(playerId).getCMsg() - 1);//Cap nhat lai CMsg
                            userController.GameReadMsgNew(actionUser.getSource(), je.get("ID").getAsInt());
                            break;
                        }
                    }
                    for (int i = 0; i < actionUser.getListMsgAG().size(); i++) {
                        if (actionUser.getListMsgAG().get(i).getId() == je.get("ID").getAsInt()) {
                            dicUser.get(playerId).getListMsgAG().get(i).setS(true);
                            dicUser.get(playerId).setCMsg(dicUser.get(playerId).getCMsg() - 100); //Cap nhat lai CMsg
                            userController.GameReadMsgNew(actionUser.getSource(), je.get("ID").getAsInt());
                            break;
                        }
                    }
                    for (int i = 0; i < actionUser.getListMsgPlayer().size(); i++) {
                        if (actionUser.getListMsgPlayer().get(i).getId() == je.get("ID").getAsInt()) {
                            dicUser.get(playerId).getListMsgPlayer().get(i).setS(true);
                            userController.GameReadMsgNew(actionUser.getSource(), je.get("ID").getAsInt());
                            break;
                        }
                    }
                } else if (je.get("T").getAsInt() == 2) { //Nhan thu co AG
                    int[] lsArr = ActionUtils.gson.fromJson(je.get("Arr").getAsJsonArray(), int[].class);
                    for (int k = 0; k < lsArr.length; k++) {
                        for (int i = 0; i < actionUser.getListMsg().size(); i++) {
                            if (actionUser.getListMsg().get(i).getId() == lsArr[k]) {
                                if (actionUser.getListMsg().get(i).getAG() > 0 && !actionUser.getListMsg().get(i).isD()) {
                                    int Error = -1;
                                    Error = userController.GameTransferAGFinishDb(actionUser.getSource(), lsArr[k], actionUser.getUsername(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()));
                                    if (Error >= 0) {
                                        if (Error > 100000000) {
                                            dicUser.get(playerId).IncrementDiamond(Error - 100000000);
                                        } else {
                                            dicUser.get(playerId).IncrementMark(actionUser.getListMsg().get(i).getAG());
                                        }
                                        dicUser.get(playerId).setCMsg(dicUser.get(playerId).getCMsg() - 100);//Cap nhat lai CMsg
                                        if (Error > 100000000) {
                                            userController.UpdateAGCache(actionUser.getSource(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()), 0, actionUser.getVIP(), Error - 100000000);
                                        } else {
                                            userController.UpdateAGCache(actionUser.getSource(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()), actionUser.getListMsg().get(i).getAG(), actionUser.getVIP(), 0l);
                                        }
                                        if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                                            if (actionUser.getListMsg().get(i).getT() == 1) {
                                                Logger.getLogger("BANKLOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get((int) actionUser.getSource())) + "#" + dicUser.get(playerId).getAG().intValue() + "#7999#" + String.valueOf(actionUser.getListMsg().get(i).getT()) + "#0#" + String.valueOf(actionUser.getListMsg().get(i).getAG()) + "#" + String.valueOf((new Date()).getTime()));
                                            } else {
                                                Logger.getLogger("KHUYENMAILOG").info(String.valueOf(actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource())) + "#" + dicUser.get(playerId).getAG().intValue() + "#7999#" + String.valueOf(actionUser.getListMsg().get(i).getT()) + "#0#" + String.valueOf(actionUser.getListMsg().get(i).getAG()) + "#" + String.valueOf((new Date()).getTime()));
                                            }
                                        }
                                        JsonObject send = new JsonObject();
                                        send.addProperty("evt", "31");
                                        send.addProperty("AG", actionUser.getListMsg().get(i).getAG());
                                        send.addProperty("I", actionUser.getListMsg().get(i).getI());
                                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                                        serviceRouter.dispatchToPlayer(playerId, csa);
                                        dicUser.get(playerId).getListMsg().get(i).setD(true);
                                        dicUser.get(playerId).getListMsg().remove(i);
                                    } else {
                                        sendErrorMsg(playerId, actionUtils.getConfigText("strMail_Receive", actionUser.getSource(), actionUser.getUserid()));
                                    }
                                } else {
                                    sendErrorMsg(playerId, actionUtils.getConfigText("strMail_Receive", actionUser.getSource(), actionUser.getUserid()));
                                }
                                break;
                            }
                        }
                        for (int i = 0; i < actionUser.getListMsgAG().size(); i++) {
                            if (actionUser.getListMsgAG().get(i).getId() == lsArr[k]) {
                                if (actionUser.getListMsgAG().get(i).getAG() > 0 && !actionUser.getListMsgAG().get(i).isD()) {
                                    int Error = -1;
                                    Error = userController.GameTransferAGFinishDb(actionUser.getSource(), lsArr[k], actionUser.getUsername(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()));
                                    if (Error >= 0) {
                                        if (Error > 100000000) {
                                            dicUser.get(playerId).IncrementDiamond(Error - 100000000);
                                        } else {
                                            dicUser.get(playerId).IncrementMark(actionUser.getListMsgAG().get(i).getAG());
                                        }
                                        dicUser.get(playerId).setCMsg(dicUser.get(playerId).getCMsg() - 100);//Cap nhat lai CMsg
                                        if (Error > 100000000) {
                                            userController.UpdateAGCache(actionUser.getSource(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()), 0, actionUser.getVIP(), Error - 100000000);
                                        } else {
                                            userController.UpdateAGCache(actionUser.getSource(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()), actionUser.getListMsgAG().get(i).getAG(), actionUser.getVIP(), 0l);
                                        }
                                        if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) { //Siam
                                            if (actionUser.getListMsgAG().get(i).getT() == 1) {
                                                Logger.getLogger("BANKLOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get((int) actionUser.getSource())) + "#" + dicUser.get(playerId).getAG().intValue() + "#7999#" + String.valueOf(actionUser.getListMsgAG().get(i).getT()) + "#0#" + String.valueOf(actionUser.getListMsgAG().get(i).getAG()) + "#" + String.valueOf((new Date()).getTime()));
                                            } else {
                                                Logger.getLogger("KHUYENMAILOG").info(String.valueOf(actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource())) + "#" + dicUser.get(playerId).getAG().intValue() + "#7999#" + String.valueOf(actionUser.getListMsgAG().get(i).getT()) + "#0#" + String.valueOf(actionUser.getListMsgAG().get(i).getAG()) + "#" + String.valueOf((new Date()).getTime()));
                                            }
                                        }
                                        JsonObject send = new JsonObject();
                                        send.addProperty("evt", "31");
                                        send.addProperty("AG", actionUser.getListMsgAG().get(i).getAG());
                                        send.addProperty("I", actionUser.getListMsgAG().get(i).getI());
                                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                                        serviceRouter.dispatchToPlayer(playerId, csa);
                                        dicUser.get(playerId).getListMsgAG().get(i).setD(true);
                                        dicUser.get(playerId).getListMsgAG().remove(i);
                                    } else {
                                        sendErrorMsg(playerId, actionUtils.getConfigText("strMail_Receive", actionUser.getSource(), actionUser.getUserid()));
                                    }
                                } else {
                                    sendErrorMsg(playerId, actionUtils.getConfigText("strMail_Receive", actionUser.getSource(), actionUser.getUserid()));
                                }
                                break;
                            }
                        }
                        for (int i = 0; i < actionUser.getListMsgPlayer().size(); i++) {
                            if (actionUser.getListMsgPlayer().get(i).getId() == lsArr[k]) {
                                if (actionUser.getListMsgPlayer().get(i).getAG() > 0 && !actionUser.getListMsgPlayer().get(i).isD()) {
                                    int Error = -1;
                                    Error = userController.GameTransferAGFinishDb(actionUser.getSource(), lsArr[k], actionUser.getUsername(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()));
                                    if (Error >= 0) {
                                        if (Error > 100000000) {
                                            dicUser.get(playerId).IncrementDiamond(Error - 100000000);
                                        } else {
                                            dicUser.get(playerId).IncrementMark(actionUser.getListMsgPlayer().get(i).getAG());
                                        }
                                        dicUser.get(playerId).setCMsg(dicUser.get(playerId).getCMsg() - 100);//Cap nhat lai CMsg
                                        if (Error > 100000000) {
                                            userController.UpdateAGCache(actionUser.getSource(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()), 0, actionUser.getVIP(), Error - 100000000);
                                        } else {
                                            userController.UpdateAGCache(actionUser.getSource(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()), actionUser.getListMsgPlayer().get(i).getAG(), actionUser.getVIP(), 0l);
                                        }
                                        if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                                            if (actionUser.getListMsgPlayer().get(i).getT() == 1) {
                                                Logger.getLogger("BANKLOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get((int) actionUser.getSource())) + "#" + dicUser.get(playerId).getAG().intValue() + "#7999#" + String.valueOf(actionUser.getListMsgPlayer().get(i).getT()) + "#0#" + String.valueOf(actionUser.getListMsgPlayer().get(i).getAG()) + "#" + String.valueOf((new Date()).getTime()));
                                            } else {
                                                Logger.getLogger("KHUYENMAILOG").info(String.valueOf(actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource())) + "#" + dicUser.get(playerId).getAG().intValue() + "#7999#" + String.valueOf(actionUser.getListMsgPlayer().get(i).getT()) + "#0#" + String.valueOf(actionUser.getListMsgPlayer().get(i).getAG()) + "#" + String.valueOf((new Date()).getTime()));
                                            }
                                        }
                                        JsonObject send = new JsonObject();
                                        send.addProperty("evt", "31");
                                        send.addProperty("AG", actionUser.getListMsgPlayer().get(i).getAG());
                                        send.addProperty("I", actionUser.getListMsgPlayer().get(i).getI());
                                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                                        serviceRouter.dispatchToPlayer(playerId, csa);
                                        dicUser.get(playerId).getListMsgPlayer().get(i).setD(true);
                                        dicUser.get(playerId).getListMsgPlayer().remove(i);
                                    } else {
                                        sendErrorMsg(playerId, actionUtils.getConfigText("strMail_Receive", actionUser.getSource(), actionUser.getUserid()));
                                    }
                                } else {
                                    sendErrorMsg(playerId, actionUtils.getConfigText("strMail_Receive", actionUser.getSource(), actionUser.getUserid()));
                                }
                                break;
                            }
                        }
                    }
                } else if (je.get("T").getAsInt() == 3) { //Xoa thu
                    int[] lsArr = ActionUtils.gson.fromJson(je.get("Arr").getAsJsonArray(), int[].class);
                    for (int k = 0; k < lsArr.length; k++) {
                        for (int i = 0; i < actionUser.getListMsg().size(); i++) {
                            if (actionUser.getListMsg().get(i).getId() == lsArr[k]) {
                                dicUser.get(playerId).getListMsg().remove(i);
                                userController.GameDelMsgNew(actionUser.getSource(), lsArr[k]);
                                break;
                            }
                        }
                        for (int i = 0; i < actionUser.getListMsgAG().size(); i++) {
                            if (actionUser.getListMsgAG().get(i).getId() == lsArr[k]) {
                                dicUser.get(playerId).getListMsgAG().remove(i);
                                userController.GameDelMsgNew(actionUser.getSource(), lsArr[k]);
                                break;
                            }
                        }
                        for (int i = 0; i < actionUser.getListMsgPlayer().size(); i++) {
                            if (actionUser.getListMsgPlayer().get(i).getId() == lsArr[k]) {
                                dicUser.get(playerId).getListMsgPlayer().remove(i);
                                userController.GameDelMsgNew(actionUser.getSource(), lsArr[k]);
                                break;
                            }
                        }

                    }
                } else if (je.get("T").getAsInt() == 10) { //Nhan Mail tu he thong - Nhung Thu khong co Ag
                    if (dicUser.get(playerId).getListMsg().size() < 10) {
                        loadMailSystemNew(playerId, 3);
                    }
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "20");
                    jo.addProperty("P", 1);
                    jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsg().subList(0, dicUser.get(playerId).getListMsg().size())));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("T").getAsInt() == 11) { //Thu tu nguoi choi
                    if (dicUser.get(playerId).getListMsgPlayer().size() < 10) {
                        loadMailPlayerNew(playerId, 2);
                    }
                    //if(dicUser.get(playerId).getListMsgPlayer().size()<10){
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "21");
                    jo.addProperty("P", 1);
                    jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgPlayer().subList(0, dicUser.get(playerId).getListMsgPlayer().size())));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                    /*} else {
                       JsonObject jo = new JsonObject();
                       jo.addProperty("evt", "21");
                       jo.addProperty("P", dicUser.get(playerId).getListMsgPlayer().size() % 10 == 0 ? dicUser.get(playerId).getListMsgPlayer().size()/10 : dicUser.get(playerId).getListMsgPlayer().size()/10+1);
                       if((je.get("P").getAsInt()-1)*10+10 <= dicUser.get(playerId).getListMsgPlayer().size()){
                       jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgPlayer().subList((je.get("P").getAsInt()-1)*10, (je.get("P").getAsInt()-1)*10+10)));
                       } else {
                       jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgPlayer().subList((je.get("P").getAsInt()-1)*10, dicUser.get(playerId).getListMsgPlayer().size())));
                       }
                       ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                       serviceRouter.dispatchToPlayer(playerId, csa);
                       }*/
                } else if (je.get("T").getAsInt() == 12) { //Thu chuyen AG va thu he thong co AG
                    if (dicUser.get(playerId).getListMsgAG().size() < 10) {
                        loadMailTransferAGNew(playerId, 4);
                    }
                    //if(dicUser.get(action.getPlayerId()).getListMsgAG().size()<10){
                    //System.out.println("===> nhan di:" + dicUser.get(action.getPlayerId()).getListMsgAG().size());
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "22");
                    jo.addProperty("P", 1);
                    jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgAG().subList(0, dicUser.get(playerId).getListMsgAG().size())));
                    //jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(action.getPlayerId()).getListMsgAG()));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                    /*} else {
                       JsonObject jo = new JsonObject();
                       jo.addProperty("evt", "22");
                       jo.addProperty("P", dicUser.get(action.getPlayerId()).getListMsgAG().size() % 10 == 0 ? dicUser.get(action.getPlayerId()).getListMsgAG().size()/10 : dicUser.get(action.getPlayerId()).getListMsgAG().size()/10+1);
                       if((je.get("P").getAsInt()-1)*10+10 <= actionUser.getListMsgAG().size()){
                       jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(action.getPlayerId()).getListMsgAG().subList((je.get("P").getAsInt()-1)*10, (je.get("P").getAsInt()-1)*10+10)));
                       }else{
                       jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(action.getPlayerId()).getListMsgAG().subList((je.get("P").getAsInt()-1)*10, dicUser.get(action.getPlayerId()).getListMsgAG().size())));
                       }
                          
                       ClientServiceAction csa = new ClientServiceAction(action.getPlayerId(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                       serviceRouter.dispatchToPlayer(action.getPlayerId(), csa);
                       }*/
                } else if (je.get("T").getAsInt() == 15) { //Lay thu cua 3C
                    if (dicUser.get(playerId).getListMsgAG().size() < 10) {
                        loadMailTransferAGNew(playerId, 5);
                    }
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "22");
                    jo.addProperty("P", 1);
                    jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgAG().subList(0, dicUser.get(playerId).getListMsgAG().size())));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("T").getAsInt() == 4) { //Nhan Mail tu he thong 
                    if (dicUser.get(playerId).getListMsg().size() < 10) {
                        loadMailSystemNew(playerId, 0);
                    }
                    if (dicUser.get(playerId).getListMsg().size() < 10) {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "20");
                        jo.addProperty("P", 1);
                        jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsg().subList(0, dicUser.get(playerId).getListMsg().size())));
                        //jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(action.getPlayerId()).getListMsg()));
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "20");
                        jo.addProperty("P", dicUser.get(playerId).getListMsg().size() % 10 == 0 ? dicUser.get(playerId).getListMsg().size() / 10 : dicUser.get(playerId).getListMsg().size() / 10 + 1);
                        if ((je.get("P").getAsInt() - 1) * 10 + 10 <= actionUser.getListMsg().size()) {
                            jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsg().subList((je.get("P").getAsInt() - 1) * 10, (je.get("P").getAsInt() - 1) * 10 + 10)));
                        } else {
                            jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsg().subList((je.get("P").getAsInt() - 1) * 10, dicUser.get(playerId).getListMsg().size())));
                        }
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                } else if (je.get("T").getAsInt() == 5) { //Thu tu nguoi choi
                    if (dicUser.get(playerId).getListMsgPlayer().size() < 10) {
                        loadMailPlayerNew(playerId, 2);
                    }
                    if (dicUser.get(playerId).getListMsgPlayer().size() < 10) {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "21");
                        jo.addProperty("P", 1);
                        jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgPlayer().subList(0, dicUser.get(playerId).getListMsgPlayer().size())));
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "21");
                        //jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(action.getPlayerId()).getListMsgPlayer()));
                        jo.addProperty("P", dicUser.get(playerId).getListMsgPlayer().size() % 10 == 0 ? dicUser.get(playerId).getListMsgPlayer().size() / 10 : dicUser.get(playerId).getListMsgPlayer().size() / 10 + 1);
                        if ((je.get("P").getAsInt() - 1) * 10 + 10 <= dicUser.get(playerId).getListMsgPlayer().size()) {
                            jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgPlayer().subList((je.get("P").getAsInt() - 1) * 10, (je.get("P").getAsInt() - 1) * 10 + 10)));
                        } else {
                            jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgPlayer().subList((je.get("P").getAsInt() - 1) * 10, dicUser.get(playerId).getListMsgPlayer().size())));
                        }
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                } else if (je.get("T").getAsInt() == 6) { //Thu chuyen AG
                    if (dicUser.get(playerId).getListMsgAG().size() < 10) {
                        loadMailTransferAGNew(playerId, 1);
                    }
                    if (dicUser.get(playerId).getListMsgAG().size() < 10) {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "22");
                        jo.addProperty("P", 1);
                        //jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(action.getPlayerId()).getListMsgAG().subList(0, dicUser.get(action.getPlayerId()).getListMsgAG().size())));
                        jo.addProperty("data", ActionUtils.gson.toJson(dicUser.get(playerId).getListMsgAG()));
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "22");
                        jo.addProperty("P",
                                dicUser.get(playerId).getListMsgAG().size() % 10 == 0
                                ? dicUser.get(playerId).getListMsgAG().size() / 10 : dicUser.get(playerId).getListMsgAG().size() / 10 + 1);

                        int pagging = je.get("P").getAsInt() - 1;
                        if (pagging < 0) {
                            pagging = 0;
                        }

                        if (pagging * 10 + 10 <= actionUser.getListMsgAG().size()) {
                            jo.addProperty("data",
                                    ActionUtils.gson.toJson(
                                            dicUser.get(playerId).getListMsgAG()
                                                    .subList(pagging * 10, pagging * 10 + 10)));
                        } else {
                            jo.addProperty("data",
                                    ActionUtils.gson.toJson(
                                            dicUser.get(playerId).getListMsgAG()
                                                    .subList(pagging * 10, dicUser.get(playerId).getListMsgAG().size())));
                        }

                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                } else if (je.get("T").getAsInt() == 7) { //Gui thu
//  					if (je.get("NN").getAsString().trim().equals("") || je.get("D").getAsString().equals("")) {
                    if (je.get("NN").getAsString().length() == 0 || je.get("D").getAsString().length() == 0) {
//                          if (actionUser.getSource() == 9) {
//                              sendErrorMsg(playerId, actionUtils.strMail_SendFail_TH);
//                          } else {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strMail_SendFail", actionUser.getSource(), actionUser.getUserid()));
                        //}
                    } else {
                        int error = -1;
                        //System.out.println("==>Gui thu:" + je.get("NN").getAsString() + "-" + actionUser.getSource()) ;
                        error = userController.GameIMsgNew(actionUser.getSource(), actionUser.getUsername(), je.get("NN").getAsString(), je.get("D").getAsString());
                        if (error >= 0 && je.get("D").toString().length() > 0) {
//                              if (actionUser.getSource() == 9) {
//                                  sendErrorMsg(playerId, actionUtils.strMail_SendSuccess_TH);
//                              } else {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strMail_SendSuccess", actionUser.getSource(), actionUser.getUserid()));
                            //}
                            int pidRec = isUserOnl(je.get("NN").getAsString());
                            if (pidRec > 0) {
                                JsonObject send = new JsonObject();
                                send.addProperty("evt", "15");
                                send.addProperty("T", 10);
                                ClientServiceAction csaS = new ClientServiceAction(pidRec, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(pidRec, csaS);
                            }
                        } else if (error == -2) {
//                              if (actionUser.getSource() == 9) {
//                                  sendErrorMsg(playerId, actionUtils.strMail_Over_TH);
//                              } else {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strMail_Over", actionUser.getSource(), actionUser.getUserid()));
                            // }
                        } else {
//                              if (actionUser.getSource() == 9) {
//                                  sendErrorMsg(playerId, actionUtils.strMail_SendFail_TH);
//                              } else {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strMail_SendFail", actionUser.getSource(), actionUser.getUserid()));
                            //}
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Xu ly Chat
    private void Process_Chat(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                Logger.getLogger("GAME_CHAT_LOG").info(playerId + " - " + ActionUtils.gson.toJson(je));
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }

                String rName = je.get("N").getAsString();
                rName = rName.substring(0, rName.length() - 3);
                if (!rName.equals(actionUser.getUsername()) && !rName.equals(actionUser.getUsernameLQ())) {
                    return;
                }
                if (je.get("D").getAsString().length() > 200) {
                    return;
                }

                if (actionUser.getChat() == 1) {
                    return; //Bi cam chat
                }
                chatHandler.process(actionUser, je);
                if (je.get("T").getAsInt() == ChatConstant.CHAT_WORLD) { //Chat kenh the gioi
                    if (actionUser.getSource() == 1) { //Lang quat
                        if (((actionUser.getVIP() == 10) && (actionUser.getLQ() < 500))
                                || ((actionUser.getVIP() == 9) && (actionUser.getLQ() < 700))
                                || ((actionUser.getVIP() == 8) && (actionUser.getLQ() < 800))
                                || ((actionUser.getVIP() == 7) && (actionUser.getLQ() < 1000))) {
                            JsonObject send = new JsonObject();
                            send.addProperty("evt", "16");
                            send.addProperty("T", 4);
                            send.addProperty("N", "Hệ thống");
                            send.addProperty("D", "Bạn chưa đủ LQ để chat kênh thế giới.");
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        } else {
                            userController.ServiceChangeLQ(actionUser.getSource(), playerId, 1, actionUser.getUsername(), je.get("D").getAsString(), je.get("N").getAsString());

                            //kenh the gioi
                            JsonObject send = new JsonObject();
                            send.addProperty("evt", "16");
                            send.addProperty("T", 1);
                            send.addProperty("N", je.get("N").getAsString());
                            send.addProperty("D", je.get("D").getAsString());
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayers(getArrPidBySourceGame(0, actionUser.getSource()), csa);
                        }
                    } else {
                        if (chatHandler.checkSpamChat(actionUser.getUserid().intValue())) {
                            JsonObject send = new JsonObject();
                            send.addProperty("evt", "16");
                            send.addProperty("T", 1);
                            send.addProperty("N", je.get("N").getAsString());
                            send.addProperty("D", je.get("D").getAsString());
                            send.addProperty("V", actionUser.getVIP());
                            send.addProperty("Avatar", actionUser.getAvatar());
                            send.addProperty("Ag", actionUser.getAG().longValue());
                            send.addProperty("ID", actionUser.getUserid().intValue());
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayers(getArrPidBySourceGame(0, actionUser.getSource()), csa);
                        }
                    }
                } else if (je.get("T").getAsInt() == ChatConstant.CHAT_GAME) {
                    //kenh game
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "16");
                    send.addProperty("T", 2);
                    send.addProperty("N", je.get("N").getAsString());
                    send.addProperty("D", je.get("D").getAsString());
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayers(getArrPidBySourceGame(je.get("G").getAsInt(), actionUser.getSource()), csa);
                    userController.ServiceChangeLQ(actionUser.getSource(), playerId, 0, actionUser.getUsername(), je.get("D").getAsString(), je.get("N").getAsString());
                } else if (je.get("T").getAsInt() == 3) {
                    //kenh bang
                } else if (je.get("T").getAsInt() == 4) {
                    if ((je.get("D").getAsString().indexOf("mrs_gatay") >= 0) || (je.get("N").getAsString().indexOf("mrs_gatay") >= 0)) {
                        userController.ServiceChangeLQ(actionUser.getSource(), playerId, 0, actionUser.getUsername(), je.get("D").getAsString(), je.get("N").getAsString());
                    }
                    // rieng
                    int pid = isUserOnl(je.get("NN").getAsString());
                    if (pid == actionUser.getPid()) {
                        return;
                    }
                    if (pid != 0) {
                        if (actionUser.getSource() != iSourceUserOnl(je.get("NN").getAsString())) {
                            JsonObject send = new JsonObject();
                            send.addProperty("evt", "16");
                            send.addProperty("T", 4);

                            send.addProperty("N", actionUtils.getConfigText("strSystem", actionUser.getSource(), actionUser.getUserid()));
                            send.addProperty("D", actionUtils.getConfigText("strChat_Server", actionUser.getSource(), actionUser.getUserid()));

                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        } else {
                            JsonObject send = new JsonObject();
                            send.addProperty("evt", "16");
                            send.addProperty("T", 4);
                            send.addProperty("N", je.get("N").getAsString());
                            send.addProperty("D", je.get("D").getAsString());
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(pid, csa);
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        }
                    } else {
                        JsonObject send = new JsonObject();
                        send.addProperty("evt", "16");
                        send.addProperty("T", 4);

                        send.addProperty("N", actionUtils.getConfigText("strSystem", actionUser.getSource(), actionUser.getUserid()));
                        send.addProperty("D", actionUtils.getConfigText("strChat_Off", actionUser.getSource(), actionUser.getUserid()));

                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                }

            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
    }

    //Xu ly Chuyen LQ==>AG
    /*private void Process_ConvertLQtoAG(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                if (actionUser.getTableId() != 0) {
                    return;
                }
                if (je.get("LQ").getAsInt() <= 0) {
                    return;
                }
                if (actionUser.getLQ() >= je.get("LQ").getAsInt()) {
                    if (userController.ServiceChangeLQ2AG(1, actionUser.getUserid(), je.get("LQ").getAsInt(), je.get("LQ").getAsInt()) == 0) {
                        dicUser.get(playerId).setLQ(dicUser.get(playerId).getLQ() - je.get("LQ").getAsInt());
                        dicUser.get(playerId).setAG(dicUser.get(playerId).getAG() + je.get("LQ").getAsInt());
                        userController.UpdateAGCache(1, playerId, je.get("LQ").getAsInt(), actionUser.getVIP(), 0l);
                        userController.UpdateLQCache(1, playerId, 0 - je.get("LQ").getAsInt(), actionUser.getVIP());
                        JsonObject send = new JsonObject();
                        send.addProperty("evt", "cv");
                        send.addProperty("LQ", dicUser.get(playerId).getLQ());
                        send.addProperty("AG", dicUser.get(playerId).getAG());
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        sendErrorMsg(playerId, "Bạn chuyển LQ sang AG thất bại.");
                    }
                } else {
                    sendErrorMsg(playerId, "Bạn chuyển LQ sang AG thất bại.");
                }
            }
        } catch (Exception e) {
            // handle exception
            System.out.println("==>Error==>Process_ConvertLQtoAG:" + e.getMessage());
        }
    }*/
    private void Process_ConvertDiamondToAG(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getTableId() != 0) {
                    return;
                }
                if (je.get("DM").getAsInt() <= 0) {
                    return;
                }
                loggerLogin_.info("==>Convert:" + je.get("DM").getAsInt());
                if (actionUser.getDiamond() >= je.get("DM").getAsInt()) {
                    int source = (int) actionUser.getSource();
                    int error = userController.GameConvertDiamond2AG(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("DM").getAsInt());
                    loggerLogin_.info("==>Convert 1:" + error + "-" + (actionUser.getUserid() - ServerDefined.userMap.get(source)));
                    if (error > 0) {
                        dicUser.get(playerId).setDiamond(dicUser.get(playerId).getDiamond() - je.get("DM").getAsInt());
                        dicUser.get(playerId).setAG(dicUser.get(playerId).getAG() + error);
                        userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), error, actionUser.getVIP(), 0 - je.get("DM").getAsInt());
                        loggerLogin_.info("==>Convert==>UpdateCache:" + dicUser.get(playerId).getDiamond() + "-" + dicUser.get(playerId).getAG());
                        JsonObject send = new JsonObject();
                        send.addProperty("evt", "cv");
                        send.addProperty("DM", dicUser.get(playerId).getDiamond());
                        send.addProperty("AG", dicUser.get(playerId).getAG());
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        sendErrorMsg(playerId, "Bạn chuyển Diamond sang Chip thất bại.");
                    }
                } else {
                    sendErrorMsg(playerId, "Bạn chuyển Diamond sang Chip thất bại.");
                }
            }
        } catch (Exception e) {
            // handle exception
            System.out.println("==>Error==>Process_ConvertDiamondToAG:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_ConvertAGToDiamond(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getTableId() != 0) {
                    return;
                }
                if (je.get("DM").getAsInt() <= 0) {
                    return;
                }
                if (actionUser.getAG().longValue() >= je.get("DM").getAsInt()) {
                    int source = (int) actionUser.getSource();
                    int error = userController.GameConvertAG2Diamond(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("DM").getAsInt());
                    if (error > 0) {
                        dicUser.get(playerId).setDiamond(dicUser.get(playerId).getDiamond() + error);
                        dicUser.get(playerId).setAG(dicUser.get(playerId).getAG() - je.get("DM").getAsInt());
                        userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), 0 - je.get("DM").getAsInt(), actionUser.getVIP(), error);
                        JsonObject send = new JsonObject();
                        send.addProperty("evt", "cv");
                        send.addProperty("DM", dicUser.get(playerId).getDiamond());
                        send.addProperty("AG", dicUser.get(playerId).getAG());
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        sendErrorMsg(playerId, "Bạn chuyển Diamond sang Chip thất bại.");
                    }
                } else {
                    sendErrorMsg(playerId, "Bạn chuyển Diamond sang Chip thất bại.");
                }
            }
        } catch (Exception e) {
            // handle exception
            System.out.println("==>Error==>Process_ConvertDiamondToAG:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Xu ly Cap nhat thong tin Mobile
    private void Process_UpdateMobile(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (je.get("M").getAsString().length() == 0 || je.get("E").getAsString().length() == 0) {
                    JSent act = new JSent();
                    act.setEvt("usermobile");
//                    if (actionUser.getSource() == 9) {
//                        act.setCmd(actionUtils.strProfile_Fail_TH);
//                    } else {
                    act.setCmd(actionUtils.getConfigText("strProfile_Fail", actionUser.getSource(), actionUser.getUserid()));
                    // }
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else {
                    int Error = -1;
                    int source = actionUser.getSource();
                    Error = userController.GameIUserMobile(source, actionUser.getUserid() - ServerDefined.userMap.get(source), actionUser.getUsername(), je.get("M").getAsString(), je.get("E").getAsString());
                    if (Error > 0) {
                        JSent act = new JSent();
                        act.setEvt("usermobile");
//                        if (actionUser.getSource() == 5) {
//                            act.setCmd(actionUtils.strProfile_Success_TH);
//                        } else {
                        act.setCmd(actionUtils.getConfigText("strProfile_Success", actionUser.getSource(), actionUser.getUserid()));
                        //}
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        JSent act = new JSent();
                        act.setEvt("usermobile");
//                        if (actionUser.getSource() == 9) {
//                            act.setCmd(actionUtils.strProfile_Fail_TH);
//                        } else {
                        act.setCmd(actionUtils.getConfigText("strProfile_Fail", actionUser.getSource(), actionUser.getUserid()));
                        //}
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
            System.out.println("==>Error==>Process_UpdateMobile:" + e.getMessage());
        }
    }

    //Xuly cap nhat Profile
    private void Process_UpdateProfile(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                dicUser.get(playerId).setGender(je.get("G").getAsShort());
                userController.GameIUserProfile(actionUser.getSource(), dicUser.get(playerId).getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()), je.get("N").getAsString(), je.get("Id").getAsString(), je.get("M").getAsString(), je.get("J").getAsString(), je.get("A").getAsString(), je.get("E").getAsString(), je.get("D").getAsString(), je.get("G").getAsInt(), dicUser.get(playerId).getUsername());
                JSent act = new JSent();
                act.setEvt("pf");
                act.setCmd("");
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            }
        } catch (Exception e) {
//             handle exception
//            System.out.println("==>Error==>Process_UpdateProfile:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Xu ly Doi AG thanh diem Vip
    private void Process_ConvertAGtoVip(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                if (actionUser.getVIP() < 10) {
                    sendErrorMsg(playerId, "Bạn không đủ cấp Vip để đổi AG sang điểm Vip.");
                    return;
                }
                if (actionUser.getAG() < je.get("AG").getAsInt()) {
                    sendErrorMsg(playerId, "Bạn không đủ AG để đổi sang điểm Vip.");
                    return;
                }
                //Insert to DB
                int markVip = 0;
                markVip = userController.GameITransferAGToVip(1, playerId, actionUser.getUsername(), je.get("AG").getAsInt());
                userController.UpdateAGCache(1, playerId, 0 - je.get("AG").getAsInt(), actionUser.getVIP(), 0l);
                dicUser.get(playerId).DecrementMark(je.get("AG").getAsInt());
                //Sent to Client
                JsonObject act = new JsonObject();
                act.addProperty("evt", "agtv");
                act.addProperty("M", markVip);
                act.addProperty("AG", dicUser.get(playerId).getAG());
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_ConvertAGtoVip:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Xu ly Roulette
    private void Process_Roulette(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                if (actionUser.getSource() == 3) {
                    sendErrorMsg(playerId, "Chức năng sẽ mở lại trong thời gian tới.");
                    return;
                }
                /*if (actionUser.getVIP() < 1) {
                 sendErrorMsg(playerId, "Bạn không đủ cấp Vip để tham gia trò chơi.") ;
                 return ;
                 }*/
                if (actionUser.getTableId() != 0) {
                    return;
                }
                if (je.get("evt").getAsString().equals("rl") || je.get("evt").getAsString().equals("rlm")) {
                    String strLQ = je.get("LQ").getAsString();
                    String strNumber = je.get("Num").getAsString();
                    if (strLQ.indexOf("-") >= 0) {
                        System.out.println("==>Roulette Am roi hack gi nua:" + strLQ + "-" + strNumber + "-" + actionUser.getUsername() + "-" + actionUser.getAG().intValue());
                        return;
                    }
                    if (strLQ.length() < 1) {
                        return;
                    }
                    if (strNumber.length() < 1) {
                        return;
                    }
                    String[] arrLQ = strLQ.split(";");
                    String[] arrNumber = strNumber.split(";");
                    if (arrLQ.length != arrNumber.length) {
                        return; //LQ dat va So dat khong khop nhau
                    }
                    int numberWin = ActionUtils.random.nextInt(37);
                    boolean isWin = false;
                    boolean am = false;
                    for (int i = 0; i < arrNumber.length; i++) {
                        if (Integer.parseInt(arrNumber[i]) == numberWin) {
                            isWin = true;
                        }
                        if (Integer.parseInt(arrLQ[i]) <= 0) {
                            am = true;
                        }
                    }
//                	System.out.println("==>ASDASDA:" + am) ;
                    if (am) {
//                		System.out.println("==>Roulette Am:" + strLQ + "-" + strNumber + "-" + actionUser.getUsername() + "-" + actionUser.getAG().intValue()) ;
                        return;//Dat cuoc am
                    }
                    if ((actionUser.getUsername().equals(nameRoulette)) && isRoulette && isWin) //giam ty le win doi voi 1 user
                    {
                        numberWin = ActionUtils.random.nextInt(37);
                    }
                    long TotalLQ = 0;
                    long BuyLQ = 0;
                    for (int i = 0; i < arrLQ.length; i++) {
                        if (je.get("evt").getAsString().equals("rl")) {
                            TotalLQ = TotalLQ + Integer.parseInt(arrLQ[i]) * 1000;
                            if (Integer.parseInt(arrNumber[i]) == numberWin) {
                                BuyLQ = Integer.parseInt(arrLQ[i]) * 1000;
                            }
                        } else {
                            TotalLQ = TotalLQ + Integer.parseInt(arrLQ[i]);
                            if (Integer.parseInt(arrNumber[i]) == numberWin) {
                                BuyLQ = Integer.parseInt(arrLQ[i]);
                            }
                        }
                    }
                    if (dicUser.get(playerId).getAG() < TotalLQ) {
//                        if (actionUser.getSource() == 9) {
//                            sendErrorMsg(playerId, actionUtils.strRoulette_Err1_TH);
//                        } else {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strRoulette_Err1", actionUser.getSource(), actionUser.getUserid()));
                        //}
                        return;
                    }
                    //System.out.println("=>RL:" + numberWin + "-" + TotalLQ + "-" + BuyLQ) ;
                    Roulette rl = new Roulette();
                    rl.setNumberWin(numberWin);
                    rl.setLQBuy((int) BuyLQ);
                    rl.setLQTotal((int) TotalLQ);
                    rl.setArr(arrNumber);
                    rl.setAgWin(0);
                    rl.setBuy(true);
                    //Insert to DB
                    long id = 0l;
                    int source = actionUser.getSource();
                    id = userController.GameIRouletteDb(source, dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source), actionUser.getUsername(), rl);
                    if (id > 0) {
                        userController.UpdateAGCache(source, dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source), 0 - TotalLQ, actionUser.getVIP(), 0l);
                        if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                            //Ghi Log Roulette
                            Logger.getLogger("ROULETTELOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#8000#0#-1#" + String.valueOf(0 - TotalLQ) + "#" + String.valueOf((new Date()).getTime()));
                        }
                        //System.out.println("==>Roulette tru tien:" + actionUser.getUsername() + "-" + actionUser.getAG().intValue() + "-" + TotalLQ) ;
                        dicUser.get(playerId).DecrementMark((int) TotalLQ);
                        rl.setId(id);
                        dicUser.get(playerId).getArrRoulette().add(rl);
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "rl");
                        act.addProperty("N", numberWin);
                        act.addProperty("id", id);
                        act.addProperty("LQ", dicUser.get(playerId).getAG());
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
//                        if (actionUser.getSource() == 9) {
//                            sendErrorMsg(playerId, actionUtils.strRoulette_Err2_TH);
//                        } else {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strRoulette_Err2", actionUser.getSource(), actionUser.getUserid()));
                        // }
                    }
                    arrLQ = null;
                    arrNumber = null;
                } else if (je.get("evt").getAsString().equals("frl")) {
                    //System.out.println("=>FRL:" + je.get("id").getAsLong()) ;
                    for (int k = 0; k < actionUser.getArrRoulette().size(); k++) {
                        if (actionUser.getArrRoulette().get(k).getId() == je.get("id").getAsLong()) {
                            if (!actionUser.getArrRoulette().get(k).isStatus()) {
                                int ag = 0;
                                actionUser.getArrRoulette().get(k).setStatus(true);
                                int source = actionUser.getSource();
                                ag = userController.GameURouletteDb(source, dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source), je.get("id").getAsLong(), actionUser.getArrRoulette().get(k).getLQBuy(), actionUser.getUsername());
                                userController.UpdateAGCache(source, dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source), ag, actionUser.getVIP(), 0l);

                                if ((ag > 100000) && (source == ServerSource.THAI_SOURCE || source == 10)) {
                                    sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + actionUtils.getConfigText("strRoulette_Alert_Pre", actionUser.getSource(), actionUser.getUserid())
                                            + ActionUtils.formatAG(ag) + actionUtils.getConfigText("strRoulette_Alert_Sur", actionUser.getSource(), actionUser.getUserid()), actionUser.getUsername(), actionUser.getSource());
                                }
                                if ((ag > 10000000) && (source == 1)) {
                                    sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + actionUtils.getConfigText("strRoulette_Alert_Pre", actionUser.getSource(), actionUser.getUserid())
                                            + ActionUtils.formatAG(ag) + actionUtils.getConfigText("strRoulette_Alert_Sur", actionUser.getSource(), actionUser.getUserid()), actionUser.getUsername(), actionUser.getSource());
                                }
                                if ((ag > 50000) && (source == 3)) {
                                    sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + actionUtils.getConfigText("strRoulette_Alert_Pre", actionUser.getSource(), actionUser.getUserid())
                                            + ActionUtils.formatAG(ag) + actionUtils.getConfigText("strRoulette_Alert_Sur", actionUser.getSource(), actionUser.getUserid()), actionUser.getUsername(), actionUser.getSource());
                                }
                                if ((ag > 50000) && (source == 2)) {
                                    sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + actionUtils.getConfigText("strRoulette_Alert_Pre", actionUser.getSource(), actionUser.getUserid())
                                            + ActionUtils.formatAG(ag) + actionUtils.getConfigText("strRoulette_Alert_Sur", actionUser.getSource(), actionUser.getUserid()), actionUser.getUsername(), actionUser.getSource());
                                }
                                if ((ag > 50000) && (source == 4)) {
                                    sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + actionUtils.getConfigText("strRoulette_Alert_Pre", actionUser.getSource(), actionUser.getUserid())
                                            + ActionUtils.formatAG(ag) + actionUtils.getConfigText("strRoulette_Alert_Sur", actionUser.getSource(), actionUser.getUserid()), actionUser.getUsername(), actionUser.getSource());
                                }
                                dicUser.get(playerId).getArrRoulette().get(k).setStatus(true);
                                dicUser.get(playerId).getArrRoulette().get(k).setAgWin(ag);
                                dicUser.get(playerId).IncrementMark(ag);
                                if (source == ServerSource.THAI_SOURCE || source == 10) //Ghi Log Khuyen mai
                                {
                                    Logger.getLogger("ROULETTELOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#8000#0#0#" + String.valueOf(ag) + "#" + String.valueOf((new Date()).getTime()));
                                }
                                JsonObject act = new JsonObject();
                                act.addProperty("evt", "frl");
                                act.addProperty("AG", ag);
                                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa);
                                //Tang tien
                                if (dicUser.get(playerId).getAG().intValue() < 2000) { //Tang tien
                                    int promotion = PromotionByUid(playerId, false).intValue();
                                    if (promotion > 0) {
                                        JsonObject act1 = new JsonObject();
                                        act1.addProperty("evt", "am");
                                        act1.addProperty("M", promotion);
                                        ClientServiceAction csa1 = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act1).getBytes("UTF-8"));
                                        serviceRouter.dispatchToPlayer(playerId, csa1);
                                    }
                                }
                            }
                            dicUser.get(playerId).getArrRoulette().remove(0); //Bo bot trong bo nho
                        }
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_Roulette:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //0 - Bonus
    //1 - Scatter
    //2 - Wild
//    private int GetTypeForSlot(int typemain) {
//        int typeS = (new Random()).nextInt(95);
//        int valueReturn = -1;
//        if (typeS < 10) {
//            valueReturn = 0;
//        } else if (typeS < 20) {
//            valueReturn = 2;
//        } else if (typeS < 30) {
//            valueReturn = 3;
//        } else if (typeS < 40) {
//            valueReturn = 4;
//        } else if (typeS < 50) {
//            valueReturn = 5;
//        } else if (typeS < 60) {
//            valueReturn = 6;
//        } else if (typeS < 70) {
//            valueReturn = 7;
//        } else if (typeS < 80) {
//            valueReturn = 8;
//        } else if (typeS < 90) {
//            valueReturn = 9;
//        } else {
//            valueReturn = 1;
//        }
//        if ((valueReturn == typemain) || (valueReturn == 2)) {
//            valueReturn = 3;
//            if (typemain == 3) {
//                valueReturn = 4;
//            }
//        }
//        return valueReturn;
//    }
    private void NewProcessSlot(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }

                int totalRow = je.get("R").getAsInt();
                int unit = je.get("U").getAsInt();

                System.out.println("==>Slot:" + totalRow + "-" + unit + "by : " + actionUser.getDefaultName());

                if (totalRow <= 0 || unit <= 0 || totalRow > 9) {
                    sendErrorMsg(playerId, "Lượt quay không hợp lệ.");
                    return;
                }

                if (unit * totalRow > actionUser.getAG()) {
                    sendErrorMsg(playerId, "Bạn không đủ Gold để đặt cược.");
                    return;
                }

                SlotMap slotMap = new SlotMap();
                SlotLogicGame.getInstance().resetValue();
                SlotLogicGame.getInstance().setmUnit(unit);

                SlotLogicGame.getInstance().checkSlotMap(totalRow, unit, slotMap);
                Vector<Integer> payLineWin = SlotLogicGame.getInstance().getLineWin();
                Vector<Integer> numIconWinPerLine = SlotLogicGame.getInstance().getListNumIconWinPerLine();
                Vector<Integer> listBonusLine = SlotLogicGame.getInstance().getBonusLine();
                Vector<Float> GoldWinPerLine = SlotLogicGame.getInstance().getListGoldWinPerLine();
                Integer totalWin = SlotLogicGame.getInstance().getmTotalWin();

                JsonObject act = new JsonObject();
                act.addProperty("evt", "slot");
                act.addProperty("Map", (new Gson()).toJson(slotMap.listIconType));
                act.addProperty("PLWin", (new Gson()).toJson(payLineWin));
                act.addProperty("GWPLine", (new Gson()).toJson(GoldWinPerLine));
                act.addProperty("NumIconWinPerLine", (new Gson()).toJson(numIconWinPerLine));
                act.addProperty("BonusLine", (new Gson()).toJson(listBonusLine));
                act.addProperty("TotalWin", totalWin);

                ClientServiceAction csa = new ClientServiceAction(playerId, 1,
                        ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println("==>Error==>Process_Slot:" + e.getMessage());
        }
    }

    //    //Xu ly Slot
//    private void Process_Slot(JsonObject je, UserInfo actionUser, int playerId) {
//        try {
//            synchronized (dicUser) {
//                if (actionUser.getUnlockPass() == 0) {
//                    return;
//                }
//                int TotalRow = je.get("R").getAsInt();
//                int Unit = je.get("U").getAsInt();
//                //System.out.println("==>Slot:" + TotalRow + "-" + Unit) ;
//                if (TotalRow <= 0 || Unit <= 0) {
//                    sendErrorMsg(playerId, "Lượt quay không hợp lệ.");
//                    return;
//                }
//                if (Unit * TotalRow > actionUser.getAG()) {
//                    sendErrorMsg(playerId, "Bạn không đủ Gold để đặt cược.");
//                    return;
//                }
//                //Khoi tao cac gia tri
//                Slot sl = new Slot();
//                sl.setTotalRow(TotalRow);
//                sl.setUnit(Unit);
//                String strResult = "";
//                int[][] Arr = new int[3][5];
//                for (int i = 0; i < 3; i++) {
//                    for (int j = 0; j < 5; j++) {
//                        Arr[i][j] = -1;
//                    }
//                }
//                int numR = -1;
//                int typeS = -1;
//                int win1 = -1;
//                int win2 = -1;
////                int win3 = -1;
//                //Random cho dòng đầu tiên ==> Duong so 2
//                for (int i = 0; i < slotRandom.length; i++) {
//                    numR = (new Random()).nextInt(slotRandom[i]);
//                    if (numR == slotRandom[i] - 2) { //Win
//                        for (int j = 0; j < slotNum[i]; j++) { //Tao cac quan bai win
//                            typeS = (new Random()).nextInt(10);
//                            if (typeS > 7) {
//                                Arr[0][j] = 2;
//                            } else {
//                                Arr[0][j] = slotType[i];
//                            }
//                        }
//                        if (slotNum[i] < 5) //O phai co gia tri khac o win
//                        {
//                            Arr[0][slotNum[i] - 1] = GetTypeForSlot(slotType[i]);
//                        }
//                        win1 = slotType[i];
//                        break;
//                    }
//                }
//                //Dong dau khong win ==> Random cho 2 cot dau tien khac nhau va khac wild (Neu Type <5)
//                if (win1 == -1) {
//                    Arr[0][0] = GetTypeForSlot(1);
//                    Arr[0][1] = GetTypeForSlot(Arr[0][0]);
//                }
//                //Random cho dòng thứ 2 ==> Duong so 1
//                for (int i = 0; i < slotRandom.length; i++) {
//                    if (slotType[i] != win1) {
//                        numR = (new Random()).nextInt(slotRandom[i]);
//                        if (numR == slotRandom[i] - 2) { //Win
//                            for (int j = 0; j < slotNum[i]; j++) { //Tao cac quan bai win
//                                typeS = (new Random()).nextInt(10);
//                                if (typeS > 7) {
//                                    Arr[1][j] = 2;
//                                } else {
//                                    Arr[1][j] = slotType[i];
//                                }
//                            }
//                            if (slotNum[i] < 5) //O phai co gia tri khac o win
//                            {
//                                Arr[1][slotNum[i] - 1] = GetTypeForSlot(slotType[i]);
//                            }
//                            win2 = slotType[i];
//                            break;
//                        }
//                    }
//                }
//                //Dong thu 2 khong win ==> Random cho 2 cot dau tien khac nhau va khac wild (Neu Type <5)
//                if (win2 == -1) {
//                    Arr[1][0] = GetTypeForSlot(1);
//                    Arr[1][1] = GetTypeForSlot(Arr[1][0]);
//                }
//                //Random cho dòng thứ 3 ==> duong so 3
//                for (int i = 0; i < slotRandom.length; i++) {
//                    if ((slotType[i] != win1) && (slotType[i] != win2)) {
//                        numR = (new Random()).nextInt(slotRandom[i]);
//                        if (numR == slotRandom[i] - 2) { //Win
//                            for (int j = 0; j < slotNum[i]; j++) { //Tao cac quan bai win
//                                typeS = (new Random()).nextInt(10);
//                                if (typeS > 7) {
//                                    Arr[2][j] = 2;
//                                } else {
//                                    Arr[2][j] = slotType[i];
//                                }
//                            }
//                            if (slotNum[i] < 5) //O phai co gia tri khac o win
//                            {
//                                Arr[2][slotNum[i] - 1] = GetTypeForSlot(slotType[i]);
//                            }
////                            win3 = slotType[i];
//                            break;
//                        }
//                    }
//                }
//                //Chinh Random lai cho dong thu 4
//                int valueWin = LuckyFunction.CheckSlotRow(Arr, 0, 0, 1, 1, 2, 2, 1, 3, 0, 4);
//                if (valueWin != 0) { //Neu Win ==> Random lai lan nua xem win that khong
//                    //Kiem tra xem la an may
//                    int numberO = LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 1, 1, 2, 2, 1, 3, 0, 4);
//                    for (int i = 0; i < slotRandom.length; i++) {
//                        if ((slotNum[i] == numberO) && ((slotType[i] == Arr[0][0]) || (slotType[i] == Arr[1][1]) || (slotType[i] == Arr[2][2]))) {
//                            numR = (new Random()).nextInt(slotRandom[i]);
//                            if (numR != slotRandom[i] - 2) { //Ko Win ==> Chinh lai Arr[1][1]
//                                Arr[1][1] = GetTypeForSlot(Arr[0][0]);
//                            }
//                            break;
//                        }
//                    }
//                }
//                //Chinh Random lai cho cong thu 5
//                valueWin = LuckyFunction.CheckSlotRow(Arr, 2, 0, 1, 1, 0, 2, 1, 3, 2, 4);
//                if (valueWin != 0) { //Neu Win ==> Random lai lan nua xem win that khong
//                    //Kiem tra xem la an may
//                    int numberO = LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 1, 1, 0, 2, 1, 3, 2, 4);
//                    for (int i = 0; i < slotRandom.length; i++) {
//                        if ((slotNum[i] == numberO) && ((slotType[i] == Arr[2][0]) || (slotType[i] == Arr[1][1]) || (slotType[i] == Arr[0][2]))) {
//                            numR = (new Random()).nextInt(slotRandom[i]);
//                            if (numR != slotRandom[i] - 2) { //Ko Win ==> Chinh lai Arr[1][1]
//                                Arr[1][1] = GetTypeForSlot(Arr[2][0]);
//                            }
//                            break;
//                        }
//                    }
//                }
//                //Ran dom cho nhung o con lai
//                for (int i = 0; i < 3; i++) {
//                    for (int j = 0; j < 5; j++) {
//                        if (Arr[i][j] == -1) {
//                            Arr[i][j] = GetTypeForSlot(-1);
//                        }
//                    }
//                }
//                Arr[0][1] = 0;
//                Arr[0][2] = 0;
//                Arr[0][3] = 0;
//                int totalW = 0;
//                String strR = "";
//                String strW = "";
//                String strNW = "";
//                for (int i = 0; i < 3; i++) {
//                    for (int j = 0; j < 5; j++) {
//                        strResult = strResult + Arr[i][j] + ";";
//                    }
//                }
//                sl.setStrResult(strResult);
//                //Row 1
//                int returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 1, 1, 1, 2, 1, 3, 1, 4);
//                if (returnRow > 0) {
//                    strR = strR + "1;";
//                    strW = strW + returnRow + ";";
//                    strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 1, 1, 1, 2, 1, 3, 1, 4) + ";";
//                    totalW = totalW + returnRow;
//                }
//                //Row 2
//                if (TotalRow > 1) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 0, 1, 0, 2, 0, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "2;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 0, 1, 0, 2, 0, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 3
//                if (TotalRow > 2) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 2, 1, 2, 2, 2, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "3;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 2, 1, 2, 2, 2, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 4
//                if (TotalRow > 3) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 1, 1, 2, 2, 1, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "4;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 1, 1, 2, 2, 1, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 5
//                if (TotalRow > 4) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 1, 1, 0, 2, 1, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "5;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 1, 1, 0, 2, 1, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 6
//                if (TotalRow > 5) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 0, 1, 0, 2, 0, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "6;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 0, 1, 0, 2, 0, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 7
//                if (TotalRow > 6) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 2, 1, 2, 2, 2, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "7;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 2, 1, 2, 2, 2, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 8
//                if (TotalRow > 7) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 0, 1, 1, 2, 2, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "8;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 0, 1, 1, 2, 2, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 9
//                if (TotalRow > 8) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 2, 1, 1, 2, 0, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "9;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 2, 1, 1, 2, 0, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 10
//                if (TotalRow > 9) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 2, 1, 1, 2, 0, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "10;";
//                        strW = strW + returnRow + ";";
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 2, 1, 1, 2, 0, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 11
//                if (TotalRow > 10) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 0, 1, 1, 2, 2, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "11;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 0, 1, 1, 2, 2, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 12
//                if (TotalRow > 11) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 1, 1, 1, 2, 1, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "12;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 1, 1, 1, 2, 1, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 13
//                if (TotalRow > 12) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 1, 1, 1, 2, 1, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "13;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 1, 1, 1, 2, 1, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 14
//                if (TotalRow > 13) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 1, 1, 0, 2, 1, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "14;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 1, 1, 0, 2, 1, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 15
//                if (TotalRow > 14) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 1, 1, 2, 2, 1, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "15;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 1, 1, 2, 2, 1, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 16
//                if (TotalRow > 15) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 1, 1, 0, 2, 1, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "16;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 1, 1, 0, 2, 1, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 17
//                if (TotalRow > 16) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 1, 1, 2, 2, 1, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "17;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 1, 1, 2, 2, 1, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 18
//                if (TotalRow > 17) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 0, 1, 2, 2, 0, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "18;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 0, 1, 2, 2, 0, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 19
//                if (TotalRow > 18) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 2, 1, 0, 2, 2, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "19;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 2, 1, 0, 2, 2, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 20
//                if (TotalRow > 19) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 2, 1, 2, 2, 2, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "20;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 2, 1, 2, 2, 2, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 21
//                if (TotalRow > 20) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 0, 1, 0, 2, 0, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "21;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 0, 1, 0, 2, 0, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 22
//                if (TotalRow > 21) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 2, 1, 0, 2, 2, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "22;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 2, 1, 0, 2, 2, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 23
//                if (TotalRow > 22) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 1, 0, 0, 1, 2, 2, 0, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "23;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 1, 0, 0, 1, 2, 2, 0, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 24
//                if (TotalRow > 23) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 2, 1, 0, 2, 2, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "24;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 2, 1, 0, 2, 2, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 25
//                if (TotalRow > 24) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 0, 1, 2, 2, 0, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "25;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 0, 1, 2, 2, 0, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 26
//                if (TotalRow > 25) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 0, 1, 1, 2, 2, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "26;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 0, 1, 1, 2, 2, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 27
//                if (TotalRow > 26) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 2, 1, 1, 2, 0, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "27;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 2, 1, 1, 2, 0, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 28
//                if (TotalRow > 27) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 0, 0, 2, 1, 1, 2, 2, 3, 0, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "28;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 0, 0, 2, 1, 1, 2, 2, 3, 0, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 29
//                if (TotalRow > 28) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 0, 1, 1, 2, 0, 3, 2, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "29;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 0, 1, 1, 2, 0, 3, 2, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                //Row 30
//                if (TotalRow > 29) {
//                    returnRow = LuckyFunction.CheckSlotRow(Arr, 2, 0, 1, 1, 0, 2, 0, 3, 1, 4);
//                    if (returnRow > 0) {
//                        strR = strR + "30;";
//                        strW = strW + returnRow;
//                        strNW = strNW + LuckyFunction.GetNumberWinPerRow(Arr, 2, 0, 1, 1, 0, 2, 0, 3, 1, 4) + ";";
//                        totalW = totalW + returnRow;
//                    }
//                }
//                sl.setTotalRow(TotalRow);
//                sl.setStrW(strW);
//                sl.setStrR(strR);
//                sl.setTotalWin(totalW);
//                sl.setScatter(LuckyFunction.CheckScatter(Arr));
//                long id = 0l;
//                int source = actionUser.getSource();
//                id = userController.GameISlot(source, dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source), actionUser.getUsername(), sl, actionUser.getTotalScatter());
//                if (id > 0) {
//                    sl.setId(id);
//                    if (actionUser.getTotalScatter() > 0) {
//                        dicUser.get(playerId).DecrementMark(0 - totalW * Unit);
//                        dicUser.get(playerId).setTotalScatter(dicUser.get(playerId).getTotalScatter() - 1);
//                    } else {
//                        dicUser.get(playerId).DecrementMark((TotalRow - totalW) * Unit);
//                    }
//                    if (sl.getScatter() == 3) {
//                        dicUser.get(playerId).setTotalScatter(dicUser.get(playerId).getTotalScatter() + 5);
//                    } else if (sl.getScatter() == 4) {
//                        dicUser.get(playerId).setTotalScatter(dicUser.get(playerId).getTotalScatter() + 15);
//                    } else if (sl.getScatter() == 5) {
//                        dicUser.get(playerId).setTotalScatter(dicUser.get(playerId).getTotalScatter() + 20);
//                    }
//                    JsonObject act = new JsonObject();
//                    act.addProperty("evt", "slot");
//                    act.addProperty("R", strResult);
//                    act.addProperty("RW", strR);
//                    act.addProperty("AGW", strW);
//                    act.addProperty("NPR", strNW);
//                    act.addProperty("Total", totalW);
//                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
//                    serviceRouter.dispatchToPlayer(playerId, csa);
//                    //Check xem duoc bonus khong => Neu bonus ==> Vao Game con.
//                    int bonus = LuckyFunction.CheckBonus(Arr, 1, 0, 1, 1, 1, 2, 1, 3, 1, 4); //Row1
//                    if (bonus == 0) {
//                        bonus = LuckyFunction.CheckBonus(Arr, 0, 0, 0, 1, 0, 2, 0, 3, 0, 4); //Row 2
//                    }
//                    if (bonus == 0) {
//                        bonus = LuckyFunction.CheckBonus(Arr, 2, 0, 2, 1, 2, 2, 2, 3, 2, 4); //Row 3
//                    }
//                    if (bonus == 0) {
//                        bonus = LuckyFunction.CheckBonus(Arr, 0, 0, 1, 1, 2, 2, 1, 3, 0, 4); //Row 4
//                    }
//                    if (bonus == 0) {
//                        bonus = LuckyFunction.CheckBonus(Arr, 2, 0, 1, 1, 0, 2, 1, 3, 2, 4); //Row 5
//                    }
//                    if (bonus == 0) {
//                        bonus = LuckyFunction.CheckBonus(Arr, 1, 0, 0, 1, 0, 2, 0, 3, 1, 4); //Row 6
//                    }
//                    if (bonus == 0) {
//                        bonus = LuckyFunction.CheckBonus(Arr, 1, 0, 2, 1, 2, 2, 2, 3, 1, 4); //Row 7
//                    }
//                    if (bonus == 0) {
//                        bonus = LuckyFunction.CheckBonus(Arr, 0, 0, 0, 1, 1, 2, 2, 3, 2, 4);//Row 8
//                    }
//                    if (bonus == 0) {
//                        bonus = LuckyFunction.CheckBonus(Arr, 2, 0, 2, 1, 1, 2, 0, 3, 0, 4); //Row 9
//                    }
//                    if (bonus > 0) {
//                        int totalWMini = 0;
//                        int numwin = (new Random()).nextInt(3) * Unit;
//                        String strWMini = Integer.toString(numwin);
//                        totalWMini = numwin;
//                        numwin = (new Random()).nextInt(6) * Unit;
//                        strWMini = strWMini + ";" + Integer.toString(numwin);
//                        totalWMini = totalWMini + numwin;
//                        numwin = (new Random()).nextInt(10) * Unit;
//                        strWMini = strWMini + ";" + Integer.toString(numwin);
//                        totalWMini = totalWMini + numwin;
//                        JsonObject actmini = new JsonObject();
//                        actmini.addProperty("evt", "slotmini");
//                        actmini.addProperty("W", strResult);
//                        actmini.addProperty("TW", totalWMini);
//                        dicUser.get(playerId).setGroupID(totalWMini); //Lưu tạm Bonus.
//                        ClientServiceAction csamini = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(actmini).getBytes("UTF-8"));
//                        serviceRouter.dispatchToPlayer(playerId, csamini);
//                    }
//                } else {
//                    sendErrorMsg(playerId, "Lượt quay của bạn chưa hợp lệ!");
//                }
//            }
//        } catch (Exception e) {
//            // handle exception
//            System.out.println("==>Error==>Process_Slot:" + e.getMessage());
//        }
//    }
    //Slot Game mini
    private void Process_SlotMini(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                System.out.println("==>Process_SlotMini:" + dicUser.get(playerId).getGroupID());
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                int source = actionUser.getSource();
                UpdateMarkChessById(playerId - ServerDefined.userMap.get(source), dicUser.get(playerId).getGroupID(), 0);
                dicUser.get(playerId).DecrementMark(dicUser.get(playerId).getGroupID());
                dicUser.get(playerId).setGroupID(0);
                JsonObject act = new JsonObject();
                act.addProperty("evt", "slotminifinish");
                act.addProperty("AG", dicUser.get(playerId).getAG());
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            }
        } catch (Exception e) {
            // handle exception
            System.out.println("==>Error==>Process_Slot:" + e.getMessage());
        }
    }

    //Xu ly Bong da
    private void Process_Football(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                if (actionUser.getVIP() < 1) {
                    sendErrorMsg(playerId, "Bạn phải là Vip 1 trở lên mới được chơi!");
                    return;
                }
                int source = actionUser.getSource();
                if (actionUser.getSource() == 1) { //LQ
                    for (Match mat : lsMatch) {
                        if (mat.getId() == je.get("ID").getAsInt()) {
                            if ((new Date()).before(mat.getStopTime())) {
                                if (actionUser.getAG() > je.get("AG").getAsInt() && je.get("AG").getAsInt() > 0) {
                                    short bet = je.get("Bet").getAsShort();
                                    float betvalue = mat.getWin();
                                    if (bet == 2) {
                                        betvalue = mat.getDraw();
                                    } else if (bet == 3) {
                                        betvalue = mat.getLost();
                                    }
                                    userController.GameIMatchPlayer(source, actionUser.getUserid(), mat.getId(), bet, betvalue, je.get("AG").getAsInt(), actionUser.getVIP());
                                    dicUser.get(playerId).IncrementMark(0 - je.get("AG").getAsInt());
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 0);
                                    act.addProperty("AG", dicUser.get(playerId).getAG());
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                } else {
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 1);
                                    act.addProperty("AG", 0);
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                }
                            }
                        }
                    }
                } else if (actionUser.getSource() == 2) { //3C
//                	System.out.println("==>Dat bong da 3C:" + je.get("Bet").getAsShort() + "-" + je.get("AG").getAsInt());
                    for (Match mat : lsMatch68) {
                        if (mat.getId() == je.get("ID").getAsInt()) {
                            if ((new Date()).before(mat.getStopTime())) {
                                if (actionUser.getAG() > je.get("AG").getAsInt() && je.get("AG").getAsInt() > 0) {
                                    short bet = je.get("Bet").getAsShort();
                                    float betvalue = mat.getWin();
                                    if (bet == 2) {
                                        betvalue = mat.getDraw();
                                    } else if (bet == 3) {
                                        betvalue = mat.getLost();
                                    }
                                    userController.GameIMatchPlayer(source, actionUser.getUserid() - ServerDefined.userMap.get(source), mat.getId(), bet, betvalue, je.get("AG").getAsInt(), actionUser.getVIP());
                                    dicUser.get(playerId).IncrementMark(0 - je.get("AG").getAsInt());
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 0);
                                    act.addProperty("AG", dicUser.get(playerId).getAG());
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                } else {
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 1);
                                    act.addProperty("AG", 0);
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                }
                            }
                        }
                    }
                } else if (actionUser.getSource() == 3) { //DT
                    for (Match mat : lsMatchDautruong) {
                        if (mat.getId() == je.get("ID").getAsInt()) {
                            if ((new Date()).before(mat.getStopTime())) {
                                if (actionUser.getAG() > je.get("AG").getAsInt() && je.get("AG").getAsInt() > 0) {
                                    short bet = je.get("Bet").getAsShort();
                                    float betvalue = mat.getWin();
                                    if (bet == 2) {
                                        betvalue = mat.getDraw();
                                    } else if (bet == 3) {
                                        betvalue = mat.getLost();
                                    }
                                    userController.GameIMatchPlayer(source, actionUser.getUserid() - ServerDefined.userMap.get(source), mat.getId(), bet, betvalue, je.get("AG").getAsInt(), actionUser.getVIP());
                                    dicUser.get(playerId).IncrementMark(0 - je.get("AG").getAsInt());
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 0);
                                    act.addProperty("AG", dicUser.get(playerId).getAG());
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                } else {
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 1);
                                    act.addProperty("AG", 0);
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                }
                            }
                        }
                    }
                } else if (actionUser.getSource() == 4) { //52
                    for (Match mat : lsMatchFun52) {
                        if (mat.getId() == je.get("ID").getAsInt()) {
                            if ((new Date()).before(mat.getStopTime())) {
                                if (actionUser.getAG() > je.get("AG").getAsInt() && je.get("AG").getAsInt() > 0) {
                                    short bet = je.get("Bet").getAsShort();
                                    float betvalue = mat.getWin();
                                    if (bet == 2) {
                                        betvalue = mat.getDraw();
                                    } else if (bet == 3) {
                                        betvalue = mat.getLost();
                                    }
                                    userController.GameIMatchPlayer(source, actionUser.getUserid() - ServerDefined.userMap.get(source), mat.getId(), bet, betvalue, je.get("AG").getAsInt(), actionUser.getVIP());
                                    dicUser.get(playerId).IncrementMark(0 - je.get("AG").getAsInt());
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 0);
                                    act.addProperty("AG", dicUser.get(playerId).getAG());
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                } else {
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 1);
                                    act.addProperty("AG", 0);
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                }
                            }
                        }
                    }
                } else if (actionUser.getSource() == 5) { //Cam
                    for (Match mat : lsMatchCam) {
                        if (mat.getId() == je.get("ID").getAsInt()) {
                            if ((new Date()).before(mat.getStopTime())) {
                                if (actionUser.getAG() > je.get("AG").getAsInt() && je.get("AG").getAsInt() > 0) {
                                    short bet = je.get("Bet").getAsShort();
                                    float betvalue = mat.getWin();
                                    if (bet == 2) {
                                        betvalue = mat.getDraw();
                                    } else if (bet == 3) {
                                        betvalue = mat.getLost();
                                    }
                                    int error = userController.GameIMatchPlayerDb(source, actionUser.getUserid() - ServerDefined.userMap.get(source), mat.getId(), bet, betvalue, je.get("AG").getAsInt());
                                    if (error > 0) {
                                        dicUser.get(playerId).IncrementMark(0 - je.get("AG").getAsInt());
                                        JsonObject act = new JsonObject();
                                        act.addProperty("evt", "fb");
                                        act.addProperty("error", 0);
                                        act.addProperty("AG", dicUser.get(playerId).getAG());
                                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                        serviceRouter.dispatchToPlayer(playerId, csa);
                                    }
                                } else {
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "fb");
                                    act.addProperty("error", 1);
                                    act.addProperty("AG", 0);
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                }
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_Roulette:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_SiamDailyPromotion(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
//                System.out.println("==>Receive:" + actionUser.getUsername() + "-" + actionUser.isReceiveDailyPromotion());
                if (actionUser.isReceiveDailyPromotion()) {
                    return; //Da nhan
                }
                if ((actionUser.getSource() != 10) && (actionUser.getSource() != ServerSource.THAI_SOURCE) && (actionUser.getSource() != 3) && (actionUser.getSource() != 4) && (actionUser.getSource() != 2)) {
                    return;
                }
                int markag = 0;
                int xsiam = 0;
                if (actionUser.getVIP() < 4) {
                    xsiam = 1;
                } else if (actionUser.getVIP() < 6) {
                    xsiam = 2;
                } else if (actionUser.getVIP() < 8) {
                    xsiam = 3;
                } else if (actionUser.getVIP() == 8) {
                    xsiam = 5;
                } else if (actionUser.getVIP() == 9) {
                    xsiam = 7;
                } else if (actionUser.getVIP() == 10) {
                    xsiam = 10;
                }
                int agFriends = 0;
                for (int i = 0; i < conDailyFriendFace.length; i++) {
                    if (actionUser.getCFriendsF() >= conDailyFriendFace[i]) {
                        agFriends = agDailyFriendFace[i];
                    }
                }
                int agDaily = 0;
                if (actionUser.getOnlineDaily() > 5) {
                    agDaily = agDailyPromotion[4];
                } else {
                    agDaily = agDailyPromotion[actionUser.getOnlineDaily() - 1];
                }
                int agRotation = 0;
                if (actionUser.getVIP() < 2) {
                    agRotation = agDailyRotation[ActionUtils.random.nextInt(4)];
                } else if (actionUser.getVIP() == 2) {
                    agRotation = agDailyRotation[ActionUtils.random.nextInt(5)];
                } else {
                    agRotation = agDailyRotation[ActionUtils.random.nextInt(6)];
                }
                markag = (agFriends + agDaily + agRotation) * xsiam;
                dicUser.get(playerId).IncrementMark(markag);
                dicUser.get(playerId).setReceiveDailyPromotion(true);
                int source = actionUser.getSource();
                userController.UpdateAGDailyPromotion(source, actionUser.getUserid() - ServerDefined.userMap.get(source), markag, source);
                //Sent to Client
                JsonObject act = new JsonObject();
                act.addProperty("evt", "siamdaily");
                act.addProperty("M", markag);
                act.addProperty("X", xsiam);
                act.addProperty("F", agFriends);
                act.addProperty("D", agDaily);
                act.addProperty("R", agRotation);
                //System.out.println("==>OK Siam Daily:" + ActionUtils.gson.toJson(act).getBytes("UTF-8")) ;
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
                //Ghi Log
                if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                    Logger.getLogger("KHUYENMAILOG").info(String.valueOf(actionUser.getUserid() - actionUser.getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid() + "#0#4#" + String.valueOf(markag) + "#" + String.valueOf((new Date()).getTime()));
                }
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_ConvertAGtoVip:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_TopGamer(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            int gameid = je.get("Gameid").getAsInt(); //Top Game
            int typeid = je.get("Typeid").getAsInt(); //Loai Top
//			System.out.println("==>Start Top:" + gameid + "-" + typeid) ;
            List<TopGamer> lsRet = new ArrayList<TopGamer>();

            for (TopGamer top : lsTopGamer) {
                if (top.getGameid() == gameid && top.getTypeid() == typeid) {
                    TopGamer gamer = new TopGamer(top.getId(), top.getN(), top.getNLQ(), top.getA(),
                            top.getM(), top.getAv(), top.getFaid(), top.getGameid(), top.getTypeid(), top.getV());
                    if (checkOldSiamOper(actionUser)) {
                        if (gamer.getM() > 2000000000) {
                            gamer.setM(2000000000);
                        }
                    }
                    lsRet.add(gamer);
                }

            }
            if (lsRet.size() == 0) {
                List<TopGamer> ls = userController.GameGetListTopGamer(actionUser.getSource(), gameid, typeid);
                for (int i = 0; i < ls.size(); i++) {
                    lsTopGamer.add(ls.get(i));
                    TopGamer top = ls.get(i);
                    if (top.getGameid() == gameid && top.getTypeid() == typeid) {
                        TopGamer gamer = new TopGamer(top.getId(), top.getN(), top.getNLQ(), top.getA(),
                                top.getM(), top.getAv(), top.getFaid(), top.getGameid(), top.getTypeid(), top.getV());
                        if (checkOldSiamOper(actionUser)) {
                            if (gamer.getM() > 2000000000) {
                                gamer.setM(2000000000);
                            }
                        }
                        lsRet.add(gamer);
                    }
                }
            }
//			System.out.println("==>Size Top:" + lsTopGamer.size()) ;
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "topgamer");
            jo.addProperty("data", ActionUtils.gson.toJson(lsRet));
            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(playerId, csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkOldSiamOper(UserInfo actionUser) {
        if (actionUser.getOperatorid() == Operator.OPERATOR_THAI || actionUser.getOperatorid() == Operator.OPERATOR_THAI1
                || actionUser.getOperatorid() == Operator.OPERATOR_THAI2) {
            return true;
        }
        return false;
    }

    private void Process_TopHighLow(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            int typeid = je.get("Typeid").getAsInt(); //Loai Top
            System.out.println("==>Start Top Highlow:" + typeid);
            List<TopGamer> lsReturn = new ArrayList<TopGamer>();
            if (lsTopHighLow != null) {
                for (int i = 0; i < lsTopHighLow.size(); i++) {
                    if (lsTopHighLow.get(i).getTypeid() == typeid) {
                        lsReturn.add(lsTopHighLow.get(i));
                    }
                }
            }
            if (lsReturn.size() == 0) {
                lsReturn = userController.GameGetListTopHighlow((int) actionUser.getSource(), typeid);
                for (int i = 0; i < lsReturn.size(); i++) {
                    lsTopHighLow.add(lsReturn.get(i));
                }
            }
            System.out.println("==>Size Top Highlow: lsReturn: " + lsReturn.size() + " - lsTopHighLow: " + lsTopHighLow.size());
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "tophighlow");
            jo.addProperty("data", ActionUtils.gson.toJson(lsReturn));
            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(playerId, csa);
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_TopHighLow:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_TopRich(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            loggerLogin_.info("==>Process_TopRich:" + lsTopRich.size());
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "toprich");
            List<TopGamer> lsRet = new ArrayList<TopGamer>();
            for (TopGamer top : lsTopRich) {
                TopGamer gamer = new TopGamer(top.getId(), top.getN(), top.getNLQ(), top.getA(),
                        top.getM(), top.getAv(), top.getFaid(), top.getGameid(), top.getTypeid(), top.getV());
                if (checkOldSiamOper(actionUser)) {
                    if (gamer.getM() > 2000000000) {
                        gamer.setM(2000000000);
                    }
                }
                lsRet.add(gamer);
            }
            for (TopGamer topGamer : lsRet) {
                topGamer.setId(topGamer.getId() + ServerDefined.userMap.get(ServerSource.MYA_SOURCE));
            }
            jo.addProperty("data", ActionUtils.gson.toJson(lsRet));
            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(playerId, csa);
        } catch (Exception e) {
            //  handle exception
            e.printStackTrace();
//            System.out.println("==>Error==>Process_TopRich:" + e.getMessage());
        }
    }

    private void Process_LQConvertDT(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            if (actionUser.getSource() != 3) {
                return;
            }
            System.out.println("==>Start Convert:" + je.get("N").getAsString() + "-" + je.get("C").getAsString() + "-" + actionUser.getSource());
            String username = je.get("N").getAsString().trim();
            String code = je.get("C").getAsString().trim();
            if ((username.length() == 0) || (code.length() == 0)) {
                return;
            }
            int err = userController.GameConvertLQToDT(actionUser.getSource(), username, code, playerId - 1100000000);
            System.out.println("==>Error Convert:" + err + "-");
            if (err > 0) { //Thanh cong ==> Khoa nick ben LQ
                PlayerDisconnected(err, (int) actionUser.getSource()); //Cho Disconnect.
                userController.RemoveUserInfoByUserid(1, err);
                int AG = userController.GameConvertLQToDT_LockLQ(1, err);
                if (AG > 0) {
                    userController.GameConvertLQToDT_UpDateAG(3, username, code, AG);
                }
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "LQConvertDT");
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            } else {
                sendErrorMsg(playerId, "Bạn liên hệ với Admin Đấu trường hoặc Làng quạt để được hỗ trợ.");
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_LQConvertDT:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_GiftCode(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            String code = je.get("C").getAsString().trim();
            if (actionUser.getTableId() != 0) {
                return;
            }
            if (code.length() == 0) {
                return;
            }

            String err = "0#0#a#";
            int uid = actionUser.getPid();

            int defaultUID = ServerDefined.userMap.get((int) actionUser.getSource());
            if (uid > defaultUID) {
                uid -= defaultUID;
            }
            int codeUID = 0;

            try {
                if (NumberUtils.isCreatable(code)) {
                    codeUID = Integer.parseInt(code);
                }
            } catch (Exception e) {
                //none
            }

            if (codeUID > defaultUID) {
                codeUID -= defaultUID;
            }

            boolean checkInviteUID = false;
            if (uid != codeUID) {
                if (codeUID == 0) {
                    err = userController.GameCheckGiftCode(actionUser.getSource(), code, playerId - defaultUID);
                } else if (codeUID > 0) {
                    checkInviteUID = true;
                    err = userController.GameCheckGiftCode(actionUser.getSource(), code, playerId - defaultUID);
                }
            }
            loggerLogin_.info("==>Process_GiftCode:ErrCode: " + playerId + " - " + err);
            String strAlert = err.split("#")[2];

            if (Integer.parseInt(err.split("#")[1]) > 0) { //Thanh cong ==> Code nhập vào là chuẩn
                if (checkInviteUID) {
                    try {
                        String keyUInfo = userController.genCacheUserInfoKey(actionUser.getSource(), uid);
                        UserInfo uinfo = (UserInfo) UserController.getCacheInstance().get(keyUInfo);
                        Integer data = (Integer) UserController.getCacheInstance().get(
                                KeyCachedDefine.getKeyCachedGiftCode(playerId - defaultUID, uinfo.getDeviceId()));
                        if (data == null) {
                            String key = KeyCachedDefine.getKeyCachedGiftCode(playerId - defaultUID, uinfo.getDeviceId());
                            UserController.getCacheInstance().set(key, codeUID, 0);

                            //resetgamecount
                            synchronized (dicUser) {
                                try {
                                    if (dicUser.containsKey(playerId)) {
                                        dicUser.get(playerId).setGameCount(0);
                                    }
                                } catch (Exception e) {
                                    loggerLogin_.info("==> err " + e.getMessage(), e);
                                }
                            }

                            userController.resetGameCountToCache(actionUser.getSource(), playerId - defaultUID);
                            loggerLogin_.info("==>Process_GiftCode:invite: " + playerId + " - " + codeUID + " - " + keyUInfo + " - " + uid);
//                            Logger.getLogger("Debug_service").error("==>Process_GiftCode:invite: " + playerId + " - " + codeUID + " - " + keyUInfo + " - " + uid);
                        } else {
                            loggerLogin_.info("==>Process_GiftCode:ErrCode: " + playerId + " - " + codeUID + " - " + keyUInfo);
//                            Logger.getLogger("Debug_service").error("==>Process_GiftCode:ErrCode: " + playerId + " - " + codeUID + " - " + keyUInfo);
                        }
                    } catch (Exception e) {
                        loggerLogin_.error(e.getMessage(), e);
                        e.printStackTrace();
                    }
                }
                promotionHandler.CreatePromotion(actionUser.getSource(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()),
                        PromotionType.TYPE_GIFT_CODE, Integer.parseInt(err.split("#")[1]), actionUser.getDeviceId());
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "GiftCode");
                if (strAlert.length() < 5) {
                    jo.addProperty("Msg", actionUtils.getConfigText("strCode_Success_Pre", actionUser.getSource(), actionUser.getUserid()) + err.split("#")[1]
                            + actionUtils.getConfigText("strCode_Success_Sur", actionUser.getSource(), actionUser.getUserid()) + code);
                } else {
                    jo.addProperty("Msg", strAlert);
                }
                jo.addProperty("G", Integer.parseInt(err.split("#")[1]));
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
                loggerLogin_.info("Process_GiftCode==>giftcode fail " + actionUtils.getConfigText("strCode_Success_Pre", actionUser.getSource(), actionUser.getUserid()).toString());
            } else {
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "GiftCode");
                jo.addProperty("Msg", actionUtils.getConfigText("strCode_Fail", actionUser.getSource(), actionUser.getUserid()));
                jo.addProperty("G", 0);
                loggerLogin_.info("Process_GiftCode==>giftcode fail " + actionUtils.getConfigText("strBankErrorSendZero", actionUser.getSource(), actionUser.getUserid()).toString());
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
            }
        } catch (Exception e) {
            loggerLogin_.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void Process_ShareImageFb(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
            if (dos.allow("shareFacebook", actionUser.getUserid())) {
                System.out.println("==>Start ShareImageFb:" + actionUser.getSource());
//              System.out.println("==>Call DB:" + ServerDefined.userMap.get((int)actionUser.getSource())) ;
                int err = userController.GameCheckShareFacebook(actionUser.getSource(), playerId - ServerDefined.userMap.get((int) actionUser.getSource()));
                System.out.println("==>ShareImageFb:" + err);
                if (err > 0) { //Thanh cong ==> Share lan dau trong ngay
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "shareImageFb");
                    jo.addProperty("Msg", actionUtils.getConfigText("strShareFace_Success_Pre", actionUser.getSource(), actionUser.getUserid()) + String.valueOf(err)
                            + actionUtils.getConfigText("strShareFace_Success_Sur", actionUser.getSource(), actionUser.getUserid()));
                    jo.addProperty("AG", err);
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else {
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "shareImageFb");
                    jo.addProperty("Msg", actionUtils.getConfigText("strShareFace_Fail", actionUser.getSource(), actionUser.getUserid()));
                    jo.addProperty("AG", 0);
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                }
            } else {
                System.out.println("==>Cam Ddos share facebook");
            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
//            System.out.println("==>Error==>Process_ShareImageFb:" + e.getMessage());
        }
    }
    //Xu ly Dau gia

    /*private void Process_Auction(JsonObject je, UserInfo actionUser, int playerId) {
     try {
     synchronized (dicUser) {
     if (actionUser.getUnlockPass() == 0) return ;
     for(Auction auc : lsAuction){
     if(auc.getAuctionId() == je.get("ID").getAsInt()){
     if((new Date()).after(auc.getStartTime()) && ((new Date()).before(auc.getFinishTime()))){
     int heso = auc.getPriceStep() - dicUser.get(playerId).getVIP()*200;
     if (auc.getAuctionType() == 0) 
     heso = auc.getPriceStep() - dicUser.get(playerId).getVIP()*200;
     if ((je.get("AG").getAsInt() > 100000) || (je.get("AG").getAsInt()<0)) {
     JSent act = new JSent();
     act.setEvt("10");
     act.setCmd("Mức giá đấu không hợp lệ.");
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     } else {
     if((dicUser.get(playerId).getAG() >= je.get("AG").getAsInt()*heso && auc.getAuctionType() == 0 && je.get("AG").getAsInt() > 0) 
     || (dicUser.get(playerId).getLQ() >= je.get("AG").getAsInt()*heso && auc.getAuctionType() == 1 && je.get("AG").getAsInt() > 0)){
     int step = je.get("AG").getAsInt()*heso;
     dicUser.get(playerId).IncrementAuction(auc.getAuctionId());
     int freeCount = 0;//getFreeCountAuction(dicUser.get(action.getPlayerId()).getVIP()) ;
     //(dicUser.get(action.getPlayerId()).getVIP()==auc.getConditionVIP() ? 1:(dicUser.get(action.getPlayerId()).getVIP()-auc.getConditionVIP())*2)
     if(dicUser.get(playerId).getStepAution().get(auc.getAuctionId()).getCount() < freeCount) step = 0;
     //System.out.println("=>Step:" + step) ;
     //dicUser.get(action.getPlayerId()).getVIP()
     List<String> arrwin = userController.GameIAuctionPlayer(getConnection(), dicUser.get(playerId).getUserid(), auc.getAuctionId(), dicUser.get(playerId).getUsername(),freeCount,auc.getAuctionType(), je.get("AG").getAsInt(), step);
     if(arrwin != null){
     //System.out.println("=>Size:" + arrwin.size() + arrwin.get(arrwin.size()-1) + "*");
     if(arrwin.get(arrwin.size()-1).equals("false")) {
     sendErrorMsg(playerId, "Phiên đấu giá đã kết thúc.") ;
     break;
     }
     if(arrwin.get(arrwin.size()-1).equals("true")) {
     if(auc.getAuctionType() == 0) dicUser.get(playerId).DecrementMark(step);
     else if(auc.getAuctionType() == 1) dicUser.get(playerId).DecrementLQ(step);
     arrwin.remove(arrwin.size()-1);
     }
     JsonObject act = new JsonObject();
     act.addProperty("evt", "dg");
     act.addProperty("AG", dicUser.get(playerId).getAG());
     act.addProperty("LQ", dicUser.get(playerId).getLQ());
     act.addProperty("T", arrwin.get(0));
     //act.addProperty("P", 0);
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     } else {
     sendErrorMsg(playerId, "Gửi đấu giá thất bại.") ;
     }
     } else {
     sendErrorMsg(playerId, "Bạn không đủ AG/LQ để tham gia. Hãy nhanh chân nạp AG/LQ để đạt được phần quà.") ;
     }
     }                   				
     } else {
     sendErrorMsg(playerId, "Phiên đấu giá đã kết thúc.") ;
     }
     break;
     }
     }
     }
     } catch (Exception e) {
     // handle exception
     System.out.println("==>Error==>Process_Auction:" + e.getMessage()) ;
     }
     }*/
    //Xu ly Xo so
    private void Process_Lottery(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                if (actionUser.getTableId() != 0) {
                    return;
                }
                if (actionUser.getVIP() < 2) {
                    if (actionUser.getSource() == 5) {
                        sendErrorMsg(playerId, "<vi>Bạn phải là vip 2 trở lên mới được chơi!</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ທ່ານ​ຕ້ອງ​ແມ່​ນ vip 1 ຂື້ນ​ໄປ​ຈຶ່ງ​ໄດ້ຫຼິ້ນ</la>");
                    } else {
                        sendErrorMsg(playerId, "Bạn phải là vip 2 trở lên mới được chơi!");
                    }
                    return;
                }
                if ((je.get("T").getAsInt() != 35 && je.get("T").getAsInt() != 75) || je.get("AG").getAsInt() <= 0) {
                    if (actionUser.getSource() == 5) {
                        sendErrorMsg(playerId, "<vi>Bạn đặt số không hợp lệ.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ທ່າ​ນ​ຕັ້ງ​ເລກ​ບໍ່​ຖຶກ​ຕ້ອງ</la>");
                    } else {
                        sendErrorMsg(playerId, "Bạn đặt số không hợp lệ.");
                    }
                } else {
                    Calendar c = Calendar.getInstance();
                    //if((new Date()).getHours() >= 18){
                    if (c.get(Calendar.HOUR_OF_DAY) >= 18) {
                        if (actionUser.getSource() == 5) {
                            sendErrorMsg(playerId, "<vi>Bạn chỉ được mua thẻ may mắn từ 0h đến 18h hàng ngày !</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>ທ່ານ​ສາ​ມາດ​ຊື້​ບັດ​ນຳ​ໂຊກ​ແຕ່ 0hຫາ18h ຂອງ​ທຸກໆ​ມື້</la>");
                        } else {
                            sendErrorMsg(playerId, "Bạn chỉ được mua thẻ may mắn từ 0h đến 18h hàng ngày !");
                        }
                        return;
                    }
                    if (dicUser.get(playerId).getAG() >= je.get("AG").getAsInt()) {
                        float tyle = 3.5f;
                        if (je.get("T").getAsInt() == 75) {
                            tyle = 75;
                        }
                        int source = actionUser.getSource();
                        userController.GameILottery(source, dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source), tyle, 1, je.get("AG").getAsInt(), je.get("N").getAsString(), actionUser.getVIP());
                        dicUser.get(playerId).setAG(dicUser.get(playerId).getAG() - je.get("AG").getAsInt());
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "xs");
                        act.addProperty("AG", dicUser.get(playerId).getAG());
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        if (actionUser.getSource() == 5) {
                            sendErrorMsg(playerId, "<vi>Bạn không đủ Gold hãy nạp thẻ ngay để thêm may mắn.</vi><en>Transfer Gold fail.</en><kh>Unknow</kh><la>​ທ່ານ​ບໍ່​ມີ Gold ພຽງ​ພໍ ຄ​ວນ​ເຕີມ​ບັດ​ດ່​ວນ​ເພື່ອ​ເພີ່​ມ​ຄວາມ​ໂຊກ​ດີ</la>");
                        } else if (actionUser.getSource() == 1) {
                            sendErrorMsg(playerId, "Bạn không đủ AG hãy nạp thẻ ngay để thêm may mắn.");
                        } else {
                            sendErrorMsg(playerId, "Bạn không đủ Gold hãy nạp thẻ ngay để thêm may mắn.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_Lottery:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Xu ly Cuoc dua Xo so
    private void Process_Lottery_Tour(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                int error = userController.ServiceTheMayMan_UpdateXoSoTour(actionUser.getSource(), dicUser.get(playerId).getUserid(), je.get("Id").getAsInt(), je.get("N").getAsString());
                if (error < 0) {
                    if (error == -1) {
                        sendErrorMsg(playerId, "Bạn không đủ AG để đổi thẻ !");
                    } else if (error == -2) {
                        sendErrorMsg(playerId, "Đã quá thời gian để đổi thẻ, Bạn chỉ được phép đổi thẻ ngày hôm nay trước 18h !");
                    } else if (error == -3) {
                        sendErrorMsg(playerId, "Không tồn tại thẻ !");
                    } else {
                        sendErrorMsg(playerId, "Đổi thẻ thất bại, mời bạn quay lại sau !");
                    }
                } else {
                    dicUser.get(playerId).setAG(dicUser.get(playerId).getAG() - error);
                    userController.UpdateAGCache(actionUser.getSource(), playerId, 0 - error, actionUser.getVIP(), 0l);
                    JsonObject act = new JsonObject();
                    act.addProperty("evt", "xst");
                    act.addProperty("AG", dicUser.get(playerId).getAG());
                    act.addProperty("N", je.get("N").getAsString());
                    act.addProperty("Id", je.get("Id").getAsInt());
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                }
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_Lottery_Tour:" + e.getMessage());
            e.printStackTrace();
        }
    }
    //Xu ly Vòng quay may mắn

    /*private void Process_Rotation_Lucky(JsonObject je, UserInfo actionUser, int playerId) {
     try {
     synchronized (dicUser) {
     if (je.get("evt").getAsString().equals("srlucky")) {
     if(actionUser.getSource() == 3) {
     if(actionUser.getArrRotationLucky().size() < 1) {
     userController.GameGetListRotationLucky(source,dicUser.get(playerId));
     }
     JsonObject act = new JsonObject();
     act.addProperty("evt", "srlucky");
     int numberfree = 0 ;
     if (actionUser.getArrRotationLucky().size() > cFreeRotationLucky_Dautruong(actionUser.getVIP()))
     numberfree = actionUser.getFreeLuckyRotation() ;
     else
     numberfree = actionUser.getFreeLuckyRotation() + cFreeRotationLucky_Dautruong(actionUser.getVIP()) - actionUser.getArrRotationLucky().size() ;
     act.addProperty("M", numberfree);
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     } else if(actionUser.getSource() == 1) {
     if(actionUser.getArrRotationLucky().size() < 1) {
     //    						System.out.println("==>ID:" + playerId) ;
     userController.GameGetListRotationLucky(getConnection(),dicUser.get(playerId));
     //    						userController.GameGetListRotationLucky(getConnectionCam(),dicUser.get(playerId));
     }
     if (dicUser.get(playerId).getRotationLuckyTournament() == -1)
     dicUser.get(playerId).setRotationLuckyTournament(userController.GameGetRotationLuckyMark(getConnection(),actionUser.getUserid()));
     JsonObject act = new JsonObject();
     act.addProperty("evt", "srlucky");
     int numberfree = 0 ;
     //            			System.out.println("==>Free:" + cFreeRotationLucky(actionUser.getVIP())) ;
     if (actionUser.getArrRotationLucky().size() >= cFreeRotationLucky(actionUser.getVIP()))
     numberfree = 0 ;
     else
     numberfree = cFreeRotationLucky(actionUser.getVIP()) - actionUser.getArrRotationLucky().size() ;
     //            			System.out.println("==>Free:" + actionUser.getArrRotationLucky().size()) ;
     act.addProperty("M", numberfree);
     act.addProperty("LQ", actionUser.getArrRotationLucky().size() - cFreeRotationLucky(actionUser.getVIP())<0 ? 0 : (actionUser.getArrRotationLucky().size() - cFreeRotationLucky(actionUser.getVIP())+1) * 1000) ;
     act.addProperty("MT", dicUser.get(playerId).getRotationLuckyTournament());
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     }
     return ;
     }
     if (actionUser.getSource() == 3) { //Dau truong
     //    				System.out.println("==>Vong quay thuong:1");
     if(actionUser.getArrRotationLucky().size() < 1) {
     userController.GameGetListRotationLucky(source,dicUser.get(playerId));
     }
     //        			System.out.println("==>Vong quay thuong:" + actionUser.getArrRotationLucky().size());
     int numberquay = 0 ;
     if (actionUser.getArrRotationLucky().size() > cFreeRotationLucky_Dautruong(actionUser.getVIP()))
     numberquay = actionUser.getFreeLuckyRotation() ;
     else
     numberquay = actionUser.getFreeLuckyRotation() + cFreeRotationLucky_Dautruong(actionUser.getVIP()) - actionUser.getArrRotationLucky().size() ;
     if(numberquay > 0) {
     if (actionUser.getArrRotationLucky().size() >= cFreeRotationLucky_Dautruong(actionUser.getVIP())) {
     dicUser.get(playerId).setFreeLuckyRotation((short)(dicUser.get(playerId).getFreeLuckyRotation() - 1)) ;
     userController.UpdateFreeRotationCard(source, actionUser.getUserid() - ServerDefined.userMap.get(source), 1) ;
     }
     int iResult = (new Random()).nextInt(30000);
     RotationLucky cl = new RotationLucky() ;
     cl.setType(0) ;
     cl.setId(0);
     cl.setAgwin(0) ;
     cl.setLqbuy(0);
     int ag = 0;
     if (iResult < 15771) {
     ag = 1200 ;
     cl.setType(1) ;
     } else if (iResult < 25771) {
     ag = 2500 ;
     cl.setType(2) ;
     } else if (iResult < 28771) {
     ag = 5000 ;
     cl.setType(3) ;
     } else if (iResult < 29771) {
     ag = 10000 ;
     cl.setType(4) ;
     } else if (iResult < 29921) {
     ag = 50000 ;
     cl.setType(5) ;
     } else if (iResult < 29981) {
     ag = 100000 ;
     cl.setType(6) ;
     } else if (iResult < 29996) {
     ag = 200000 ;
     cl.setType(7) ;
     } else if (iResult < 29999) {
     ag = 500000 ;
     cl.setType(8) ;
     } else if (iResult < 30000) {
     ag = 1000000 ;
     cl.setType(9) ;
     }
     //                		System.out.println("==>Vong quay thuong:" + ag);
     //if (c.get(Calendar.HOUR_OF_DAY) == 11 && (c.get(Calendar.DAY_OF_MONTH) == 8))
     //	ag=ag*2 ;
     cl.setAgwin(ag) ;
     long id = 0 ;
     userController.GameIRotationLucky(source, actionUser.getUserid() - ServerDefined.userMap.get(source), actionUser.getUsername(), cl);
     dicUser.get(playerId).IncrementMark(ag);
     cl.setId(id);
     dicUser.get(playerId).getArrRotationLucky().add(cl);
     JsonObject act = new JsonObject();
     act.addProperty("evt", "rlucky");
     act.addProperty("Cmd", ActionUtils.gson.toJson(cl));
     act.addProperty("AG", dicUser.get(playerId).getAG());
     act.addProperty("M", numberquay - 1);
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     if (cl.getType() == 4) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 10,000 Gold ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 5) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 50,000 Gold ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 6) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 100,000 Gold ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 7) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 200,000 Gold ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 8) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 500,000 Gold ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 9) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 1,000,000 Gold ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     }
     } else {
     sendErrorMsg(playerId, "Bạn đã hết lượt quay vòng quay may mắn trong ngày !") ;
     }
     } else if (actionUser.getSource() == 1) {
     if (je.get("evt").getAsString().equals("rlucky")) {
     //    					System.out.println("==>Vong quay thuong:1");
     if(actionUser.getArrRotationLucky().size() < 1) {
     userController.GameGetListRotationLucky(getConnection(),dicUser.get(playerId));
     //            				userController.GameGetListRotationLucky(getConnectionCam(),dicUser.get(playerId));
     }
     //            			System.out.println("==>Vong quay thuong:" + actionUser.getArrRotationLucky().size());
     int lq = 0 ;
     //int numberquay = 0 ;
     if (actionUser.getArrRotationLucky().size() >= cFreeRotationLucky(actionUser.getVIP())) {
     lq = (actionUser.getArrRotationLucky().size() - cFreeRotationLucky(actionUser.getVIP()) + 1) * 1000 ;
     //            				numberquay = 0 ;
     }
     //            			System.out.println("==>ABC:" + actionUser.getArrRotationLucky().size() + "-" + cFreeRotationLucky(actionUser.getVIP()) + "-" + lq) ;
     if(actionUser.getLQ() >= lq) {
     if (actionUser.getArrRotationLucky().size() >= cFreeRotationLucky(actionUser.getVIP())) {
     dicUser.get(playerId).setFreeLuckyRotation((short)(dicUser.get(playerId).getFreeLuckyRotation() - 1)) ;
     userController.UpdateFreeRotationCard(getConnection(), actionUser.getUserid(), 1) ;
     //                				userController.UpdateFreeRotationCard(getConnectionCam(), actionUser.getUserid(), 1) ;
     }
     int iResult = (new Random()).nextInt(3000000);
     RotationLucky cl = new RotationLucky() ;
     cl.setType(0) ;
     cl.setId(0);
     cl.setAgwin(0) ;
     cl.setLqbuy(lq);
     if ((actionUser.getVIP() <= 4 && iResult>=2996166) || (actionUser.getVIP() <= 5 && iResult>=2999916)
     || (actionUser.getVIP() <= 6 && iResult>=2999976)) {
     iResult = (new Random()).nextInt(3000000);
     }
     int ag = 0;
     if (iResult < 1506166) {
     ag = 0 ;
     cl.setType(1) ;
     } else if (iResult < 2606166) {
     ag = 10000 ;
     cl.setType(2) ;	 //Cong 1 diem vao cuoc dua
     } else if (iResult < 2906166) {
     ag = 20000 ;
     cl.setType(3) ; //Cong 2 diem vao cuoc dua
     } else if (iResult < 2981166) {
     ag = 50000 ;
     cl.setType(4) ; //Cong 3 diem vao cuoc dua
     } else if (iResult < 2996166) {
     ag = 100000 ;
     cl.setType(5) ; //Cong 4 diem vao cuoc dua
     } else if (iResult < 2998666) {
     ag = 200000 ;
     cl.setType(6) ; //Cong 5 diem vao cuoc dua
     } else if (iResult < 2999666) {
     ag = 500000 ;
     cl.setType(7) ; //Cong 6 diem vao cuoc dua
     } else if (iResult < 2999816) {
     ag = 1000000 ;
     cl.setType(8) ; //Cong 8 diem vao cuoc dua
     } else if (iResult < 2999955) {
     ag = 2000000 ;
     cl.setType(9) ; //Cong 9 diem vao cuoc dua
     } else if (iResult < 2999985) {
     ag = 5000000 ;
     cl.setType(10) ; //Cong 10 diem vao cuoc dua
     } else if (iResult < 2999995) {
     ag = 10000000 ;
     cl.setType(11) ; //Cong 12 diem vao cuoc dua
     } else if (iResult < 3000000) {
     ag = 50000000 ;
     cl.setType(12) ; //Cong 15 diem vao cuoc dua
     }	
     //                    		System.out.println("==>Vong quay thuong:" + ag);
     cl.setAgwin(ag) ;
     long id = 0 ;
     id = userController.GameIRotationLucky(getConnection(), actionUser.getUserid(), actionUser.getUsername(), cl);
     //                    		id = userController.GameIRotationLucky(getConnectionCam(), actionUser.getUserid() - 1300000000, actionUser.getUsername(), cl);
     dicUser.get(playerId).IncrementMark(ag);
     dicUser.get(playerId).DecrementLQ(cl.getLqbuy());
     cl.setId(id);
     dicUser.get(playerId).getArrRotationLucky().add(cl);
     JsonObject act = new JsonObject();
     act.addProperty("evt", "rlucky");
     act.addProperty("Cmd", ActionUtils.gson.toJson(cl));
     act.addProperty("AG", dicUser.get(playerId).getAG());
     act.addProperty("LQ", dicUser.get(playerId).getLQ());
     act.addProperty("M", actionUser.getArrRotationLucky().size() > cFreeRotationLucky(actionUser.getVIP()) ? 0 : cFreeRotationLucky(actionUser.getVIP()) - actionUser.getArrRotationLucky().size() );
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     //                            System.out.println("==>Length:" + dicUser.get(playerId).getArrRotationLucky().size());
     } else {
     sendErrorMsg(playerId, "Bạn đã hết lượt quay vòng quay may mắn trong ngày !") ;
     }
     } if (je.get("evt").getAsString().equals("frlucky")) {
     //    					System.out.println("==>frlucky:" + dicUser.get(playerId).getArrRotationLucky().size() + "-" + cFreeRotationLucky(actionUser.getVIP())) ;
     RotationLucky cl = dicUser.get(playerId).getArrRotationLucky().get(dicUser.get(playerId).getArrRotationLucky().size() -1);
     if (cl.getType() > 0) {
     short mark = 0;
     if (cl.getType() == 2)
     mark = 1 ;
     else if (cl.getType() == 3)
     mark = 2 ;
     else if (cl.getType() == 4)
     mark = 3 ;
     else if (cl.getType() == 5)
     mark = 4 ;
     else if (cl.getType() == 6)
     mark = 5 ;
     else if (cl.getType() == 7)
     mark = 6 ;
     else if (cl.getType() == 8)
     mark = 8 ;
     else if (cl.getType() == 9)
     mark = 9 ;
     else if (cl.getType() == 10)
     mark = 10 ;
     else if (cl.getType() == 11)
     mark = 12 ;
     else if (cl.getType() == 12)
     mark = 15 ;
     dicUser.get(playerId).setRotationLuckyTournament((short)(dicUser.get(playerId).getRotationLuckyTournament() + mark));
     }
     JsonObject act = new JsonObject();
     act.addProperty("evt", "frlucky");
     act.addProperty("M", dicUser.get(playerId).getRotationLuckyTournament());
     act.addProperty("LQ", cFreeCardLucky(actionUser.getVIP()) - dicUser.get(playerId).getArrRotationLucky().size() > 0 
     ? 0 : (dicUser.get(playerId).getArrRotationLucky().size() - cFreeRotationLucky(actionUser.getVIP()) + 1) * 1000);
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     //                        System.out.println("==>"+ActionUtils.gson.toJson(act).getBytes("UTF-8")) ;
     if (cl.getType() == 4) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 50,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 5) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 100,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 6) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 200,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 7) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 500,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 8) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 1,000,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 9) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 2,000,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 10) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 5,000,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 11) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 10,000,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 12) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " quay được giải thưởng 50,000,000 AG ở Vòng quay may mắn!", actionUser.getUsername(), actionUser.getSource());
     }
     }
     }
    			
     }
     } catch (Exception e) {
     // handle exception
     System.out.println("==>Error==>Process_Rotation_Lucky:" + e.getMessage()) ;
     }
     }*/
    //Xu ly Bai may man
    /*private void Process_LuckyCard(JsonObject je, UserInfo actionUser, int playerId) {
     try {
     synchronized (dicUser) {
     if (actionUser.getUnlockPass() == 0) return ;
     if(je.get("evt").getAsString().equals("tlucky")){
     if (actionUser.getSource() == 1) { //Lang quat
     //    					System.out.println("==>tlucky:" + actionUser.getUsername()) ;
     Calendar c = Calendar.getInstance();                    	
     if(dicUser.get(playerId).getArrCardLucky().size() < 1) 
     userController.GameGetListCardLucky(getConnection(),dicUser.get(playerId));   
     //                    		userController.GameGetListCardLucky(getConnectionCam(),dicUser.get(playerId));   
     //                    	System.out.println("==>0");
     if (actionUser.getLuckyTournament() == -1)
     dicUser.get(playerId).setLuckyTournament(userController.GameGetEventCardLucky(getConnection(),actionUser.getUsername()));
     //                    		actionUser.setLuckyTournament(userController.GameGetEventCardLucky(getConnectionCam(),actionUser.getUsername()));
     //                    	System.out.println("==>1");
     dicUser.get(playerId).setFreeLuckyCard(sFreeCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)));
     DateFormat readFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
     JsonObject act = new JsonObject();
     act.addProperty("evt", "time");
     act.addProperty("Cmd", readFormat.format(new Date()));
     act.addProperty("P", cFreeCardLucky(dicUser.get(playerId).getVIP())-sFreeCardLucky(dicUser.get(playerId).getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)));
     //            			System.out.println("==>Count:" + sFreeCardLucky(dicUser.get(playerId).getArrCardLucky(), c.get(Calendar.HOUR_OF_DAY)) + "-" + actionUser.getArrCardLucky().size()) ;
     act.addProperty("LQ", cFreeCardLucky(dicUser.get(playerId).getVIP())-sFreeCardLucky(dicUser.get(playerId).getArrCardLucky(), c.get(Calendar.HOUR_OF_DAY)) > 0 
     ? 0 : getLQBuyCardLucky(sBuyCardLucky(dicUser.get(playerId).getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY))));
     //            			System.out.println("==>2");
     act.addProperty("M", actionUser.getLuckyTournament()) ;
     //            			System.out.println("==>tlucky ==> Send Client:" + ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     } else if (actionUser.getSource() == 3) { //Dautruong
     Calendar c = Calendar.getInstance();                    	
     if(dicUser.get(playerId).getArrCardLucky().size() < 1) 
     userController.GameGetListCardLucky(source,dicUser.get(playerId));                    	
     DateFormat readFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
     JsonObject act = new JsonObject();
     act.addProperty("evt", "time");
     act.addProperty("Cmd", readFormat.format(new Date()));
     int numberFree = 0 ;
     numberFree = cFreeCardLucky_Dautruong(dicUser.get(playerId).getVIP()) - sFreeCardLucky(dicUser.get(playerId).getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) ;
     act.addProperty("P", numberFree);
     act.addProperty("LQ", numberFree > 0 ? 0 : 10000000);
     act.addProperty("M", 0);//userController.GameGetEventCardLucky(getConnection(),actionUser.getUsername())) ;
     //System.out.println("==>tlucky ==> Send Client:");
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     }
     } else if(je.get("evt").getAsString().equals("slucky")){
     if (actionUser.getSource() == 1) { //Lang quat
     //                		System.out.println("==>Bat dau boc bai") ;
     Calendar c = Calendar.getInstance();
     if ((c.get(Calendar.HOUR_OF_DAY) == 11 && c.get(Calendar.MINUTE) < 15)
     || (c.get(Calendar.HOUR_OF_DAY) == 15 && c.get(Calendar.MINUTE) < 15)
     || (c.get(Calendar.HOUR_OF_DAY) == 20 && c.get(Calendar.MINUTE) < 15)){
     if(actionUser.getVIP() < 4){
     sendErrorMsg(playerId, "Bạn cần đạt vip 4 để được tham gia bốc bài may mắn !") ;
     return;
     }
                    		
     //                    		int totalUsername = 0 ;
     //                    		//System.out.println("==>IP:" + actionUser.getsIP()) ;
     //                    		if (dicCardLucky.containsKey(actionUser.getsIP())) {
     //                    			String lsname = dicCardLucky.get(actionUser.getsIP()).getLsName() ;
     //                    			//System.out.println("==>DS:" + lsname) ;
     //                    			if (lsname.equals("")) {
     //                    				dicCardLucky.get(actionUser.getsIP()).setLsName(actionUser.getUsername() + ";") ;
     //                    			} else {
     //                    				totalUsername = lsname.split(";").length ;
     //                    				if (lsname.indexOf(actionUser.getUsername() + ";") >0)
     //                    					totalUsername = 0 ;
     //                    				else if (totalUsername < 11){
     //                    					if (lsname.indexOf(actionUser.getUsername() + ";") == -1) {
     //                    						dicCardLucky.get(actionUser.getsIP()).setLsName(lsname + actionUser.getUsername() + ";") ;
     //                    					}
     //                    				}	
     //                    			}
     //                    		} else {
     //                    			dicCardLucky.put(actionUser.getsIP(), new CardLuckyIP(actionUser.getUsername() + ";")) ;
     //                    		}
     //                    		if (totalUsername > 10) {
     //                    			sendErrorMsg(playerId, "Số lượng Username được phép bốc trên dải IP này đã hết!") ;
     //                    			return ;
     //                    		}
     //if(actionUser.getArrCardLucky().size() < 1) 
     //	userController.GameGetListCardLucky(getConnection(),dicUser.get(playerId));                        	
     //                        	if(sFreeCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) < cFreeCardLucky(actionUser.getVIP())
     //                        			|| sBuyCardLucky(dicUser.get(playerId).getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) < 1000){
     //                    		if (actionUser.getFreeLuckyCard() < cFreeCardLucky(actionUser.getVIP())) {
     //                        			|| sBuyCardLucky(dicUser.get(playerId).getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) < 1000){
     int buycl = 0;
     //                        		if(cFreeCardLucky(actionUser.getVIP())-sFreeCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) <= 0){
     //                        			buycl = getLQBuyCardLucky(sBuyCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)));
     //                        		}
     if(cFreeCardLucky(actionUser.getVIP()) <= actionUser.getFreeLuckyCard()){
     buycl = getLQBuyCardLucky(sBuyCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)));
     } else
     actionUser.setFreeLuckyCard(actionUser.getFreeLuckyCard() + 1);
     if(actionUser.getLQ() >= buycl){
     List<Card> cards = getRandomCard();
     if (!nameSuperLucky.equals("")) {
     if (nameSuperLucky.equals(actionUser.getUsername())) {
     nameSuperLucky = "" ;
     cards = new ArrayList<Card>();
     cards.add(new Card(1,3,2)) ;
     cards.add(new Card(1,4,3)) ;
     cards.add(new Card(1,5,4)) ;
     cards.add(new Card(1,6,5)) ;
     cards.add(new Card(1,7,6)) ;
     }
     }
     CardLucky cl = new CardLucky();
     cl.setType(LuckyFunction.Check_5Quan(cards));
     cl.setAgwin(0);
     cl.setArr(getCardTrans(cards));
     cl.setTimeCheck(c.getTimeInMillis()) ;
     //                            		if ((actionUser.getVIP() < 4) && (cl.getType() > 0)) {
     //                            			cards = getRandomCard() ;
     //                            			cl.setType(LuckyFunction.Check_5Quan(cards)) ;
     //                            			cl.setArr(getCardTrans(cards)) ;
     //                            		}
     if ((cl.getType() == 0) && (actionUser.getUsername().equals(nameCardLucky))
     && isLucky) {
     cards = getRandomCard() ;
     cl.setType(LuckyFunction.Check_5Quan(cards)) ;
     cl.setArr(getCardTrans(cards)) ;
     }
     if(buycl>0) cl.setLqBuy(buycl);
     cl.setStatus(false) ;
     int ag = 0;
     if (cl.getType() == 1)
     ag = 10000 ;
     else if (cl.getType() == 5)
     ag = 20000 ;
     else if (cl.getType() == 20)
     ag = 100000 ;
     else if (cl.getType() == 50)
     ag = 300000 ;
     else if (cl.getType() == 300)
     ag = 500000 ;
     else if (cl.getType() == 1000)
     ag = 1000000 ;
     else if (cl.getType() == 3000)
     ag = 5000000 ;
     else if (cl.getType() == 10000)
     ag = 50000000 ;
     //if (c.get(Calendar.HOUR_OF_DAY) == 11 && (c.get(Calendar.DAY_OF_MONTH) == 8))
     //	ag=ag*2 ;
     //long id = userController.GameICardLucky(getConnection(), actionUser.getUserid(), actionUser.getUsername(), cl,buycl);
     //                            		dicUser.get(playerId).DecrementLQ(buycl);
     //                            		dicUser.get(playerId).IncrementMark(ag);
     cl.setId(0);
     cl.setAgwin(ag);
     dicUser.get(playerId).getArrCardLucky().add(cl);
     JsonObject act = new JsonObject();
     act.addProperty("evt", "slucky");
     act.addProperty("Cmd", ActionUtils.gson.toJson(cl));
     act.addProperty("LQ", dicUser.get(playerId).getLQ());
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     } else {
     sendErrorMsg(playerId, "Bạn không đủ LQ để tham gia !") ;
     }
     //                        	} else{
     //                        		sendErrorMsg(playerId, "Bạn đã hết lần lật bài may mắn !") ;
     //                        	}
     } else {
     sendErrorMsg(playerId, "Thời gian chơi Bài may mắn đã hết, hẹn gặp lai bạn trong phiên tiếp theo!") ;
     }
     } else if (actionUser.getSource() == 3) { //Dautruong
     //                		System.out.println("==>Bai may man Dau truong") ;
     Calendar c = Calendar.getInstance();
     if ((c.get(Calendar.HOUR_OF_DAY) == 12 && c.get(Calendar.MINUTE) < 50 && c.get(Calendar.MINUTE) > 29)
     || (c.get(Calendar.HOUR_OF_DAY) == 22 && c.get(Calendar.MINUTE) < 20)){
     if(actionUser.getVIP() < 2){
     sendErrorMsg(playerId, "Bạn cần đạt vip 2 để được tham gia bốc bài may mắn !") ;
     return;
     }
     //                    		System.out.println("==>Total:" + sFreeCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) + "-" + cFreeCardLucky_Dautruong(actionUser.getVIP()) + "-" + actionUser.getFreeLuckyCard()) ;
     if(actionUser.getArrCardLucky().size() < 1) 
     userController.GameGetListCardLucky(source,dicUser.get(playerId));
     //                        	if(sFreeCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) < cFreeCardLucky_Dautruong(actionUser.getVIP()) || actionUser.getFreeLuckyCard() > 0){
     //                    			if (cFreeCardLucky_Dautruong(actionUser.getVIP()) < sFreeCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY))) {
     //                    				dicUser.get(playerId).setFreeLuckyCard(dicUser.get(playerId).getFreeLuckyCard() - 1) ;
     //                    				userController.UpdateFreeLuckyCard(source, actionUser.getUserid() - ServerDefined.userMap.get(source), 1) ;
     //                    			}
     if(sFreeCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) < cFreeCardLucky_Dautruong(actionUser.getVIP())){
     List<Card> cards = getRandomCard();
     CardLucky cl = new CardLucky();
     cl.setType(LuckyFunction.Check_5Quan(cards));
     cl.setAgwin(0);
     cl.setArr(getCardTrans(cards));
     cl.setTimeCheck(c.getTimeInMillis()) ;
     //                        		if ((actionUser.getVIP() < 5) && (cl.getType() > 0)) {
     //                        			cards = getRandomCard() ;
     //                        			cl.setType(LuckyFunction.Check_5Quan(cards)) ;
     //                        			cl.setArr(getCardTrans(cards)) ;
     //                        		}
     if ((cl.getType() == 0) && (actionUser.getUsername().equals(nameCardLucky))
     && isLucky) {
     cards = getRandomCard() ;
     cl.setType(LuckyFunction.Check_5Quan(cards)) ;
     cl.setArr(getCardTrans(cards)) ;
     }
     cl.setStatus(false) ;
     int ag = 0;
     if (cl.getType() == 1)
     ag = 2000 ;
     else if (cl.getType() == 5)
     ag = 4000 ;
     else if (cl.getType() == 20)
     ag = 12000 ;
     else if (cl.getType() == 50)
     ag = 30000 ;
     else if (cl.getType() == 300)
     ag = 50000 ;
     else if (cl.getType() == 1000)
     ag = 100000 ;
     else if (cl.getType() == 3000)
     ag = 300000 ;
     else if (cl.getType() == 10000)
     ag = 1000000 ;
     //                        		System.out.println("==>Phan thuong:" + ag);
     //if (c.get(Calendar.HOUR_OF_DAY) == 11 && (c.get(Calendar.DAY_OF_MONTH) == 8))
     //	ag=ag*2 ;
     //long id = userController.GameICardLucky(source, actionUser.getUserid() - ServerDefined.userMap.get(source), actionUser.getUsername(), cl,0);
     //dicUser.get(playerId).IncrementMark(ag);
     cl.setId(0);
     cl.setAgwin(ag);
     dicUser.get(playerId).getArrCardLucky().add(cl);
     JsonObject act = new JsonObject();
     act.addProperty("evt", "slucky");
     act.addProperty("Cmd", ActionUtils.gson.toJson(cl));
     act.addProperty("LQ", dicUser.get(playerId).getLQ());
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     if (cl.getType() == 20) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 12,000 Gold (Xám) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 50) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 30,000 Gold (Sảnh) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 300) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 50,000 Gold (Thùng) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 1000) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 100,000 Gold (Cù lũ) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 3000) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 300,000 Gold (Tứ quý) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (cl.getType() == 10000) {
     sendSpecialAlert(actionUser.getPid(), "alertV", "Chúc mừng đấu thủ " + actionUser.getUsername() + " đã nhận được giải thưởng 1,000,000 Gold (Thùng phá sảnh) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     }
     System.out.println("==>Ket thuc:" + cl.getType());
     } else{
     sendErrorMsg(playerId, "Bạn đã hết lần lật bài may mắn !") ;
     }
     } else {
     sendErrorMsg(playerId, "Thời gian chơi Bài may mắn đã hết, hẹn gặp lai bạn trong phiên tiếp theo!") ;
     }
     }
     } else if(je.get("evt").getAsString().equals("flucky")){
     Calendar c = Calendar.getInstance();
     if ((c.get(Calendar.HOUR_OF_DAY) == 11 && c.get(Calendar.MINUTE) < 15)
     || (c.get(Calendar.HOUR_OF_DAY) == 15 && c.get(Calendar.MINUTE) < 15)
     || (c.get(Calendar.HOUR_OF_DAY) == 20 && c.get(Calendar.MINUTE) < 15)){
     if(actionUser.getArrCardLucky().size()>0){
     int k = actionUser.getArrCardLucky().size() - 1 ;
     //if(actionUser.getArrCardLucky().get(k).getId() == je.get("id").getAsLong()){
     if(!actionUser.getArrCardLucky().get(k).isStatus()){
     //System.out.println("==>Time:" + (c.getTimeInMillis() - actionUser.getArrCardLucky().get(k).getTimeCheck()));
     //            					boolean t = false ;
     //            					if (k == 0)
     //            						t = true ;
     //            					else if (c.getTimeInMillis() - actionUser.getArrCardLucky().get(k-1).getTimeCheck() > 5053)
     //            						t = true ;
     //            					if (t) {
     if (c.getTimeInMillis() - actionUser.getArrCardLucky().get(k).getTimeCheck() > 3553) {
     CardLucky cl = actionUser.getArrCardLucky().get(k) ;
     long id = 0 ;
     if (actionUser.getSource() == 3)
     id = userController.GameICardLucky(source, actionUser.getUserid() - ServerDefined.userMap.get(source), actionUser.getUsername(), cl,0);
     else
     id = userController.GameICardLucky(getConnection(), actionUser.getUserid(), actionUser.getUsername(), cl, cl.getLqBuy());
     //            							id = userController.GameICardLucky(getConnectionCam(), actionUser.getUserid(), actionUser.getUsername(), cl, cl.getLqBuy());
     //int ag = userController.GameUCardLucky(getConnection(), actionUser.getUserid(), je.get("id").getAsLong(), je.get("tour").getAsInt(), actionUser.getArrCardLucky().get(k).getType(), actionUser.getUsername());
     //Cong diem cuoc dua bai
     if (je.get("tour").getAsInt() > 0) {
     int mark = 0;
     if (cl.getType() == 1)
     mark = 1 ;
     else if (cl.getType() == 5)
     mark = 2 ;
     else if (cl.getType() == 20)
     mark = 5 ;
     else if (cl.getType() == 50)
     mark = 10 ;
     else if (cl.getType() == 300)
     mark = 20 ;
     else if (cl.getType() == 1000)
     mark = 30 ;
     else if (cl.getType() == 3000)
     mark = 40 ;
     else if (cl.getType() == 10000)
     mark = 50 ;
     dicUser.get(playerId).setLuckyTournament(dicUser.get(playerId).getLuckyTournament() + mark);
     }
     dicUser.get(playerId).DecrementLQ(cl.getLqBuy());
     dicUser.get(playerId).IncrementMark(cl.getAgwin());
     dicUser.get(playerId).getArrCardLucky().get(k).setStatus(true);
     JsonObject act = new JsonObject();
     act.addProperty("evt", "flucky");
     act.addProperty("AG", actionUser.getArrCardLucky().get(k).getAgwin());
     act.addProperty("LQ", cFreeCardLucky(actionUser.getVIP())-sFreeCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY)) > 0 
     ? 0 : getLQBuyCardLucky(sBuyCardLucky(actionUser.getArrCardLucky(),c.get(Calendar.HOUR_OF_DAY))));
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     //Add thong bao dac biet chay
     if (dicUser.get(playerId).getArrCardLucky().get(k).getType() == 1000) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " nhận được giải thưởng 1,000,000 AG (Cù lũ) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (dicUser.get(playerId).getArrCardLucky().get(k).getType() == 3000) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " nhận được giải thưởng 5,000,000 AG (Tứ quý) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     } else if (dicUser.get(playerId).getArrCardLucky().get(k).getType() == 10000) {
     sendSpecialAlert(actionUser.getPid(), "alertV", actionUser.getUsername() + " nhận được giải thưởng 50,000,000 AG (Thùng phá sảnh) ở Bài may mắn!", actionUser.getUsername(), actionUser.getSource());
     }
     } else {
     sendErrorMsg(playerId, "Lượt bốc này của bạn không được tính điểm vào cuộc đua !") ;
     }
     } else {
     sendErrorMsg(playerId, "Bạn đã nhận thưởng rồi. Mời bạn chơi tiếp !") ;
     }
     //}
     } else {
     sendErrorMsg(playerId, "Bạn hãy bắt đầu lượt lật bài may mắn mới !") ;
     }
     } else {
     sendErrorMsg(playerId, "Thời gian chơi Bài may mắn đã hết, hẹn gặp lai bạn trong phiên tiếp theo !") ;
     return;
     }
     }
     }
     } catch (Exception e) {
     // handle exception
     System.out.println("==>Error==>Process_LuckyCard:" + e.getMessage()) ;
     }
     }*/
    //Xu ly Daily Promotion
    private void Process_DailyPromotion(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (je.get("evt").getAsString().equals("dp")) { //Daily Promotion
                    loggerLogin_.info("Dailypromotion:" + actionUser.getUsername() + "-" + actionUser.getPromotionDaily() + "-" + actionUser.getOnlineDaily() + "-" + je.get("day").getAsInt() + "-" + actionUser.isReceiveDailyPromotion());
                    int source = actionUser.getSource();
                    if ((source == ServerSource.THAI_SOURCE) || (source == ServerSource.IND_SOURCE) || source == ServerSource.INDIA_SOURCE
                            || source == ServerSource.MYA_SOURCE) {
                        if (actionUser.getPromotionDaily() > 0 && !actionUser.isReceiveDailyPromotion()) {
                            userController.UpdateAG(source, actionUser.getUserid() - ServerDefined.userMap.get(source), actionUser.getPromotionDaily(), false);
                            userController.UpdateDailyPromotionToCache(source, dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source));
                            userController.UpdateDailyPromotionDeviceToCache(source, dicUser.get(playerId).getDeviceId()); //Update Daily promotion theo Device
                            dicUser.get(playerId).IncrementMark(actionUser.getPromotionDaily());
                            Logger.getLogger("KHUYENMAILOG").info(String.valueOf(playerId - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid() + "#0#17#" + String.valueOf(actionUser.getPromotionDaily()) + "#" + String.valueOf((new Date()).getTime()));
                            promotionHandler.CreatePromotion(source, playerId - ServerDefined.userMap.get(source), PromotionType.TYPE_DAILY, actionUser.getPromotionDaily(), actionUser.getDeviceId()); //ghi vao log DB
                            JsonObject act = new JsonObject();
                            act.addProperty("evt", "dp");
                            act.addProperty("AG", actionUser.getPromotionDaily());
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                            dicUser.get(playerId).setOnlineDaily((short) (15 + dicUser.get(playerId).getOnlineDaily()));
                            dicUser.get(playerId).setPromotionDaily(0);
                            dicUser.get(playerId).setReceiveDailyPromotion(true);
                            loggerLogin_.info("==>Da nhan Dailypromotion:" + actionUser.getUsername() + "-" + actionUser.getUserid().intValue() + "-" + dicUser.get(playerId).getPromotionDaily() + "-" + dicUser.get(playerId).getOnlineDaily());
                        } else {
                            if (source == ServerSource.THAI_SOURCE) {
                                sendErrorMsg(playerId, "คุณได้รับรางวัลแล้ว");
                            } else if (source == 10) {
                                sendErrorMsg(playerId, "Anda telah dapat.");
                            } else {
                                sendErrorMsg(playerId, ServiceImpl.actionUtils.getConfigText("strDailyBonus_err1", source, actionUser.getUserid()));
                            }
                        }
                        return;
                    }

                    if (actionUser.getOnlineDaily() == je.get("day").getAsInt() && actionUser.getPromotionDaily() > 0) {
                        userController.UpdateAGDb(source, actionUser.getUserid() - ServerDefined.userMap.get(source), actionUser.getPromotionDaily());
                        userController.UpdateAGCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), actionUser.getPromotionDaily(), actionUser.getVIP(), 0l);
                        if (source == 1) {
                            userController.UpdatePromotionDailyLQ(source, actionUser.getUserid());
                        }
                        //userController.UpdateAG(getConnection(), actionUser.getUserid(), actionUser.getPromotionDaily()) ;
                        int agLucky = actionUser.getPromotionDaily();
                        dicUser.get(playerId).IncrementMark(actionUser.getPromotionDaily());
                        dicUser.get(playerId).setPromotionDaily(0);
                        //Tang tien cho nguoi choi
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "dp");
                        act.addProperty("AG", agLucky);
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                } else if (je.get("evt").getAsString().equals("dpl")) { //Daily Promotion ==> Lucky Promotion
                    if (actionUser.getOnlineDaily() == je.get("day").getAsInt()
                            && actionUser.getPromotionDaily() > 0
                            && actionUser.getOnlineDaily() % 5 == 0) {
                        int iLucky = ActionUtils.random.nextInt(100);
                        int agLucky = 0;
                        if (iLucky == 51) {
                            agLucky = 1000000;
                        } else if (iLucky % 30 == 0) {
                            agLucky = 500000;
                        } else if (iLucky % 20 == 0) {
                            agLucky = 200000;
                        } else if (iLucky % 10 == 0) {
                            agLucky = 100000;
                        } else if (iLucky % 5 == 0) {
                            agLucky = 10000;
                        } else if (iLucky % 2 == 0) {
                            agLucky = 3000;
                        } else if (iLucky % 2 == 1) {
                            agLucky = 2000;
                        }
                        int source = actionUser.getSource();
                        userController.UpdateAG(source, actionUser.getUserid() - ServerDefined.userMap.get(source), agLucky, false);
                        dicUser.get(playerId).IncrementMark(agLucky);
                        dicUser.get(playerId).setPromotionDaily(0);
                        //Tang tien cho nguoi choi
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "dp");
                        act.addProperty("AG", agLucky);
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
//            System.out.println("==>Error==>Process_DailyPromotion:" + e.getMessage());
        }
    }

    /*private void UpdateUVip(UserInfo actionUser, int playerId) {
    	 try {
             synchronized (dicUser) {
            	 int ag = 0;
//               System.out.println("==>UpVip:" + actionUser.getUsername() + "-" + actionUser.getVIP() +"-" + actionUser.getRoomId());
               if (actionUser.getVIP() == 0 && actionUser.getRoomId() != 6) {
                   dicUser.get(playerId).setVIP((short) 1);
                   ag = 2000;
                   if (actionUser.getSource() == 9) {
                   	ag = 4000;
              
                   }
                   dicUser.get(playerId).IncrementMark(ag);
//                   System.out.println("==>uvip Cong tien:" + actionUser.getUsername() + "-" + dicUser.get(playerId).getAG().intValue()) ;
                   int source = actionUser.getSource();
                   userController.GameUUserVIP(source, playerId - ServerDefined.userMap.get(source), 1, ag, 0);
                   if (source != 1) {
                       userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), ag, (short) 1);
                   }
                   if (source == ServerDefined.THAI_SOURCE) {
                       Logger.getLogger("KHUYENMAILOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid() + "#0#11#" + String.valueOf(ag) + "#" + String.valueOf((new Date()).getTime()));
                   }
                   if (dicUser.get(playerId).getTableId() > 0) {
                       GameDataAction gda = new GameDataAction(playerId, dicUser.get(playerId).getTableId());
                       JsonObject jegame = new JsonObject();
                       jegame.addProperty("evt", "uvip");
                       jegame.addProperty("ag", ag);
                       gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jegame).getBytes("UTF-8")));
                       serviceRouter.dispatchToGame(dicUser.get(playerId).getGameid(), gda);
                   }
                   JsonObject act = new JsonObject();
                   act.addProperty("evt", "uvip");
                   act.addProperty("AG", ag);
                   act.addProperty("LQ", 0);
                   ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                   serviceRouter.dispatchToPlayer(playerId, csa);
               }
               //System.out.println("===>UpVip nao:" + actionUser.getTableId() + "--" + actionUser.getUsername());
               if ((actionUser.getTableId() != 0) && (ag > 0)) {
                   GameDataAction gda = new GameDataAction(playerId, actionUser.getTableId());
                   JsonObject jo = new JsonObject();
                   jo.addProperty("evt", "amuvip");
                   jo.addProperty("ag", ag);
                   gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jo).getBytes("UTF-8")));
                   serviceRouter.dispatchToGame(actionUser.getGameid(), gda);
               }
             }
    	} catch (Exception e) {
	        // handle exception
	        System.out.println("==>Error==>Process_UpdateUser:" + e.getMessage());
	    }
    }*/
    //Xu ly cap nhat Ten, Pass + Avatar
    private void Process_UpdateUser(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }

                if (je.get("evt").getAsString().equals("rename")) { //Doi ten cho username
                    if (actionUser.getUsername().indexOf("fb.") != 0 && actionUser.getUsername().indexOf(".gm") != actionUser.getUsername().length() - 3) {
                        sendErrorMsg(playerId, "Bạn không được phép đổi tên!");
                        return;
                    }
                    //System.out.println("==>Rename:" + je.get("N").getAsString() + "-" + je.get("P").getAsString()) ;
                    int error = 0;
                    if (CheckValidUsernameLQ(je.get("N").getAsString()) != 0) {
                        error = -1; //Ten khong dung kieu cach
                    }
                    if (je.get("P").getAsString().length() < 6) {
                        error = -2; //Pass khong du do dai
                    }
                    if (error == 0) {
                        error = userController.GameUpdateUsername(actionUser.getSource(), actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource()), je.get("N").getAsString(), je.get("P").getAsString());
                    }
                    //System.out.println("==>Error:" + error);
                    if (error > 0) {
                        dicUser.get(playerId).setUsername(je.get("N").getAsString().toLowerCase());
                    }
                    JsonObject act = new JsonObject();
                    act.addProperty("evt", "rename");
                    act.addProperty("error", error);
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("evt").getAsString().equals("changepass")) { //Doi pass cho username
                    int error = 0;
                    if (je.get("NP").getAsString().length() < 6) {
                        error = -2; //Pass khong du do dai
                    }
                    int source = actionUser.getSource();
                    error = userController.GameUpdatePassword(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("OP").getAsString(), je.get("NP").getAsString(), actionUser.getUsername(), actionUser.getDeviceId());
                    JsonObject act = new JsonObject();
                    act.addProperty("evt", "changepass");
                    act.addProperty("error", error);
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("evt").getAsString().equals("changea")) { //Doi Avatar cho User
                    int error = 0;
                    if (je.get("A").getAsInt() == actionUser.getAvatar()) {
                        error = -2; //Avatar khong doi
                    } else {
                        int source = actionUser.getSource();
                        userController.GameUpdateAvatar(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("A").getAsInt());
                        dicUser.get(playerId).setAvatar(je.get("A").getAsShort());
                    }
                    JsonObject act = new JsonObject();
                    act.addProperty("evt", "changea");
                    act.addProperty("error", error);
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("evt").getAsString().equals("uag")) {
                    if (actionUser.getTableId() == 0) {
                        int source = actionUser.getSource();
                        String strReturn = "";
                        strReturn = userController.CheckUserInCache(source, playerId - ServerDefined.userMap.get(source), actionUser.getFacebookid(), actionUser.getGameid(), actionUser.getDeviceId(), actionUser.getSource(), actionUser.getUsername(), ipAddressServer);
                        if (strReturn.length() > 1) {
                            String[] arrNumber = strReturn.split(";");
                            dicUser.get(playerId).setAG(Long.parseLong(arrNumber[0]));
                            dicUser.get(playerId).setVIP(Short.parseShort(arrNumber[1]));
                            dicUser.get(playerId).setLQ(Integer.parseInt(arrNumber[2]));
                            dicUser.get(playerId).setMarkVip(Integer.parseInt(arrNumber[3]));
                            JsonObject act = new JsonObject();
                            act.addProperty("evt", "uag");
                            if (actionUser.getOperatorid() > Operator.OPERATOR_THAI2) {
                                act.addProperty("ag", dicUser.get(playerId).getAG().longValue());
                            } else {
                                if (dicUser.get(playerId).getAG().longValue() > 2000000000) {
                                    act.addProperty("ag", 2000000000);
                                } else {
                                    act.addProperty("ag", dicUser.get(playerId).getAG().intValue());
                                }
                            }

                            act.addProperty("lq", dicUser.get(playerId).getLQ().intValue());
                            act.addProperty("vip", dicUser.get(playerId).getVIP());
                            act.addProperty("dm", 0);
                            act.addProperty("mvip", dicUser.get(playerId).getMarkVip());
                            act.addProperty("lqago", dicUser.get(playerId).getChessElo().intValue());
                            act.addProperty("vippoint", ActionUtils.ConvertVipPercentToMark(dicUser.get(playerId).getSource(),
                                    dicUser.get(playerId).getVIP(), dicUser.get(playerId).getMarkVip()));

                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        }/* else {
                        	UserAfterPay userTemp = null;
                            //Lay Vip va AG
                            int markvip = 0;
                            userTemp = userController.GameGetUserInfoAfterPay(source, actionUser.getUserid() - ServerDefined.userMap.get(source));
                            String strVip = userController.GameGetMarkVipDB(source, actionUser.getUserid() - ServerDefined.userMap.get(source));
                    		markvip = Integer.parseInt(strVip.split(";")[0]);
                            userController.UpdateMarkVipToCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), markvip);
                            if (userTemp != null) {
                                userController.UpdateAGCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), userTemp.getAg() - dicUser.get(playerId).getAG().longValue(), userTemp.getVip(), 0l);
                                dicUser.get(playerId).setVIP(userTemp.getVip());
                                dicUser.get(playerId).setAG(Long.valueOf(userTemp.getAg()));
                                dicUser.get(playerId).setLQ(userTemp.getLq());
                                dicUser.get(playerId).setMarkVip(markvip);;
                                JsonObject act = new JsonObject();
                                act.addProperty("evt", "uag");
                                act.addProperty("ag", userTemp.getAg());
                                act.addProperty("lq", userTemp.getLq());
                                act.addProperty("vip", userTemp.getVip());
                                act.addProperty("dm", userTemp.getDm());
                                act.addProperty("mvip", markvip);
                                act.addProperty("lqago", userTemp.getLqinday());
                                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa);
                            }
                        }*/
                    }
                } else if (je.get("evt").getAsString().equals("uvip")) { // Gift VIP
                    int ag = 0;
//                    System.out.println("==>UpVip:" + actionUser.getUsername() + "-" + actionUser.getVIP() +"-" + actionUser.getRoomId());
                    if (actionUser.getVIP() == 0 && je.get("vip").getAsInt() == 1 && actionUser.getRoomId() != 6) {
                        int source = actionUser.getSource();
                        dicUser.get(playerId).setVIP((short) 1);
                        ag = 2000;
                        if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == ServerSource.IND_SOURCE) {
                            ag = 3000;
                            /*if (actionUser.getGameid() == 8021)
                        		ag = 6000;
                        	else
                        		ag = 5000;*/
                        } else if (actionUser.getSource() == ServerSource.INDIA_SOURCE) {
                            ag = 10000;
                        }
                        if (actionUser.getOperatorid() == Operator.OPERATOR_3C1 || actionUser.getOperatorid() == Operator.OPERATOR_THAI1
                                || actionUser.getOperatorid() == Operator.OPERATOR_THAI2 || actionUser.getOperatorid() == Operator.OPERATOR_THAI3
                                || actionUser.getOperatorid() == Operator.OPERATOR_THAI4
                                || actionUser.getOperatorid() == Operator.OPERATOR_INDO || actionUser.getOperatorid() == Operator.OPERATOR_INDO1) { //Tặng tiền lên Vip theo kiểu mới
                            promotionHandler.CreatePromotion(source, playerId - ServerDefined.userMap.get(source),
                                    PromotionType.TYPE_UP_VIP, ag, dicUser.get(playerId).getDeviceId()); //Insert 1 dong trong AdminPromotion
                            userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), 0, (short) 1, 0l); //Set Vip To Cache
                            userController.GameUUserVIP(source, playerId - ServerDefined.userMap.get(source), 1, 0, 0);
                            JsonObject act = new JsonObject();
                            act.addProperty("evt", "uvip");
                            act.addProperty("AG", 0);
                            act.addProperty("LQ", 0);
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        } else {
                            dicUser.get(playerId).IncrementMark(ag);
                            userController.GameUUserVIP(source, playerId - ServerDefined.userMap.get(source), 1, ag, 0);
                            if (source != 1) {
                                userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), ag, (short) 1, 0l);
                            }
                            if (source == ServerSource.THAI_SOURCE || source == ServerSource.IND_SOURCE) {
                                Logger.getLogger("KHUYENMAILOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid() + "#0#11#" + String.valueOf(ag) + "#" + String.valueOf((new Date()).getTime()));
                            }
                            if (dicUser.get(playerId).getTableId() > 0) {
                                GameDataAction gda = new GameDataAction(playerId, dicUser.get(playerId).getTableId());
                                JsonObject jegame = new JsonObject();
                                jegame.addProperty("evt", "uvip");
                                jegame.addProperty("ag", ag);
                                gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jegame).getBytes("UTF-8")));
                                serviceRouter.dispatchToGame(dicUser.get(playerId).getGameid(), gda);
                            }
                            JsonObject act = new JsonObject();
                            act.addProperty("evt", "uvip");
                            act.addProperty("AG", ag);
                            act.addProperty("LQ", 0);
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                        }
                    }
                    //System.out.println("===>UpVip nao:" + actionUser.getTableId() + "--" + actionUser.getUsername());
                    if ((actionUser.getTableId() != 0) && (ag > 0)) {
                        GameDataAction gda = new GameDataAction(playerId, actionUser.getTableId());
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "amuvip");
                        jo.addProperty("ag", ag);
                        gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jo).getBytes("UTF-8")));
                        serviceRouter.dispatchToGame(actionUser.getGameid(), gda);
                    }
                } else if (je.get("evt").getAsString().equals("US")) { //Cai dat lai Setting User
                    dicUser.get(playerId).getUserSetting().setBm(je.get("bm").getAsBoolean());
                    dicUser.get(playerId).getUserSetting().setBm(je.get("s").getAsBoolean());
                    dicUser.get(playerId).getUserSetting().setBm(je.get("i").getAsBoolean());
                    int id = 0;
                    int source = actionUser.getSource();
                    id = userController.GameIUserSettingDb(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("bm").getAsBoolean(), je.get("s").getAsBoolean(), je.get("i").getAsBoolean());
                    userController.GameUpdateUserSettingToCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("bm").getAsBoolean(), je.get("s").getAsBoolean(), je.get("i").getAsBoolean(), id);
                    dicUser.get(playerId).getUserSetting().setId(id);
                } else if (je.get("evt").getAsString().equals("appid")) {
                    int source = actionUser.getSource();
                    userController.UpdateAppId(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("data").getAsString());
                } else if (je.get("evt").getAsString().equals("agvideo")) {
                    int ag = 1000;
                    if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                        ag = 1000;
                    }
                    int Error = 0;
                    int source = actionUser.getSource();
                    Error = userController.GameIVideoGold(source, playerId - ServerDefined.userMap.get(source), ag);
                    if (Error > 0) {
                        dicUser.get(playerId).IncrementMark(Error);
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "agvideo");
                        act.addProperty("AG", Error);
                        act.addProperty("LQ", 0);
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_UpdateUser:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /*private void Process_UpdateUsernameLQ(JsonObject je, UserInfo actionUser, int playerId) {
     try {
     synchronized (dicUser) {
     if (CheckValidUsernameLQ(je.get("U").getAsString()) != 0) { //Username moi khong hop le
     JsonObject act = new JsonObject();
     act.addProperty("evt", "UULQ");
     act.addProperty("data", 0);
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     } else {
     //System.out.println("==>Process_UpdateUsernameLQ==>Username cu:" + actionUser.getUsernameLQ()) ;
     int id = userController.GameUUsernameLQ(getConnection(), actionUser.getUserid(), actionUser.getUsernameLQ(), je.get("U").getAsString(), actionUser.getOperatorid()) ;
     if (id > 1) { //Successful
     dicUser.get(playerId).setUserid(id) ;
     dicUser.get(playerId).setRegister(false) ;
     dicUser.get(playerId).setUsername(je.get("U").getAsString()) ;
     dicUser.get(playerId).setUsernameLQ(je.get("U").getAsString()) ;
     dicUser.get(playerId).setLQ(dicUser.get(playerId).getLQ() - id) ;
     JsonObject act = new JsonObject();
     act.addProperty("evt", "UULQ");
     act.addProperty("data", id);
     ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
     serviceRouter.dispatchToPlayer(playerId, csa);
     sendErrorMsg(playerId, "Chúc mừng bạn đã có tên mới trên Làng quạt!") ;
     } else {
     if (id == 1)
     sendErrorMsg(playerId, "Bạn không đủ LQ để đổi tên trên Làng quạt!") ;
     else
     sendErrorMsg(playerId, "Đã có tên này tồn tại trên Làng quạt, bạn hãy chọn tên khác!") ;
     }                   		
     }
     }
     } catch (Exception e) {
     // handle exception
     System.out.println("==>Error==>Process_UpdateUsernameLQ:" + e.getMessage()) ;
     }
     }*/
    //Xu ly Doi ten nhan vat tren Lang quat
    private void Process_ChangeUsernameLQ(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (je.get("evt").getAsString().equals("NRULQ_N")) { //Register Username LQ
                    if (CheckValidUsernameLQ(je.get("U").getAsString()) != 0) {
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "NRULQ");
                        act.addProperty("data", 0);
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        if (actionUser.getUsernameLQ().length() != 0) { //Da co UsernameLQ ==> Khong duoc DK 
                            sendErrorMsg(playerId, "Bạn đã có tên nhân vật trên Làng quạt.");
                        } else {
                            String usernamenew = actionUser.getUsername();
                            if (usernamenew.indexOf("_undefined") > 0) {
                                usernamenew = usernamenew.substring(0, usernamenew.length() - 10);
                            }
                            int id = 0;
                            int source = actionUser.getSource();
                            id = userController.GameIUserinfo(source, actionUser.getUserid() - ServerDefined.userMap.get(source), usernamenew, je.get("U").getAsString().toLowerCase(), actionUser.getOperatorid());
//                    		System.out.println("==>UserR:" + je.get("U").getAsString() + "-" + id);                        	
                            if (id > 0) { //Successful
                                //System.out.println("=====>ID:" + dicUser.get(action.getPlayerId()).getUserid()) ;
                                if (dicUser.get(playerId).getUserid() == 0) {
                                    //Gui ve thong tin Room
                                    JsonObject send1 = new JsonObject();
                                    send1.addProperty("evt", "getLR");
                                    send1.addProperty("pid", playerId);
                                    ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send1));
                                    serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
                                }
                                userController.UpdateUsernameSiamToCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("U").getAsString());
                                dicUser.get(playerId).setUserid(id);
                                dicUser.get(playerId).setRegister(false);
                                dicUser.get(playerId).setUsername(je.get("U").getAsString());
                                dicUser.get(playerId).setUsernameLQ(je.get("U").getAsString());
                                JsonObject act = new JsonObject();
                                act.addProperty("evt", "NRULQ");
                                act.addProperty("data", 1);
                                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa);
                            } else {
                                JsonObject act = new JsonObject();
                                act.addProperty("evt", "NRULQ");
                                act.addProperty("data", 0);
                                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa);
                            }
                        }
                    }
                } else if (je.get("evt").getAsString().equals("NRULQ")) { //Register Username LQ
                    if (CheckValidUsernameLQ(je.get("U").getAsString()) != 0) {
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "NRULQ");
                        act.addProperty("data", 0);
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        if (actionUser.getUsernameLQ().length() != 0) { //Da co UsernameLQ ==> Khong duoc DK 
                            sendErrorMsg(playerId, "Bạn đã có tên nhân vật trên Làng quạt.");
                        } else {
                            String usernamenew = actionUser.getUsername();
                            if (usernamenew.indexOf("_undefined") > 0) {
                                usernamenew = usernamenew.substring(0, usernamenew.length() - 10);
                            }
                            int id = 0;
                            int source = actionUser.getSource();
                            id = userController.GameIUserinfo(source, actionUser.getUserid() - ServerDefined.userMap.get(source), usernamenew, je.get("U").getAsString(), actionUser.getOperatorid());
//                    		System.out.println("==>UserR:" + je.get("U").getAsString() + "-" + id);                        	
                            if (id > 0) { //Successful
                                userController.UpdateUsernameSiamToCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("U").getAsString());
                                dicUser.get(playerId).setUserid(id);
                                dicUser.get(playerId).setRegister(false);
                                dicUser.get(playerId).setUsername(je.get("U").getAsString());
                                dicUser.get(playerId).setUsernameLQ(je.get("U").getAsString());
                                JsonObject act = new JsonObject();
                                act.addProperty("evt", "NRULQ");
                                act.addProperty("data", 1);
                                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa);
                                //Gui thong tin Room
                                JsonObject send1 = new JsonObject();
                                send1.addProperty("evt", "2");
                                send1.addProperty("pid", playerId);
                                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send1));
                                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
                                //Gui ve thong tin Room

                            } else {
                                JsonObject act = new JsonObject();
                                act.addProperty("evt", "NRULQ");
                                act.addProperty("data", 0);
                                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa);
                            }
                        }
                    }
                } else if (je.get("evt").getAsString().equals("CULQ")) { //Check UsernameLQ
                    if ((je.get("U").getAsString().length() > 50) || (je.get("U").getAsString().length() == 0)) {
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "CULQ");
                        act.addProperty("data", 0);
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
//                		if (actionUser.getUsernameLQ().equals("")) {
                        if (actionUser.getUsernameLQ().length() == 0) {
                            int id = userController.GameCheckUserLQ(1, je.get("U").getAsString());
                            if (id > 0) { //Da co username do trong bang
                                JsonObject act = new JsonObject();
                                act.addProperty("evt", "CULQ");
                                act.addProperty("data", 0);
                                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa);
                            } else {
                                JsonObject act = new JsonObject();
                                act.addProperty("evt", "CULQ");
                                act.addProperty("data", 1);
                                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa);
                            }
                        } else {
                            sendErrorMsg(playerId, "Bạn đã có tên nhân vật trong game.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
    }

    //Xu ly Select G
    private void Process_SelectGame(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            long startTime = System.nanoTime();
            int source = actionUser.getSource();

            String ipcurrent = userController.IpServerCurrent(source, playerId - ServerDefined.userMap.get(source));
            if (!ipcurrent.equals(ipAddressServer)) {
                loggerLogin_.info("==>Hack nhe:" + actionUser.getUserid().intValue() + "-" + actionUser.getUsername());
            }
            loggerLogin_.info("IPserver:" + ipAddressServer + "-" + playerId);
            if (actionUser.getIsOnline() != je.get("gameid").getAsShort()) {
//                userController.UpdateIsOnlineToCache(source, playerId - ServerDefined.userMap.get(source), ipAddressServer, je.get("gameid").getAsShort(), actionUser.getOperatorid(), actionUser.getsIP());
                //Lay so luong thu moi
                dicUser.get(playerId).setCMsg(userController.GetNumberNewMailDB(source, actionUser.getUsername(), actionUser.getUsernameOld()));
                loggerLogin_.info("==>GetNumberMail:" + playerId + "-" + dicUser.get(playerId).getCMsg());
            }
            dicUser.get(playerId).setGameid(je.get("gameid").getAsShort());
            actionHandler.getSelectGHandler().processHappyHour(dicUser.get(playerId), userController);

            //Kiem tra lai xem co vua nap tien ==> Bi remove khoi Nick khong
            String strReturn = "";
            strReturn = userController.CheckUserInCache(source, playerId - ServerDefined.userMap.get(source), actionUser.getFacebookid(), actionUser.getGameid(), actionUser.getDeviceId(), actionUser.getSource(), actionUser.getUsername(), ipAddressServer);
            if (strReturn.length() > 1) {
                //System.out.println("==>Lay lai tien sau Pay:" + strReturn) ;
                String[] arrNumber = strReturn.split(";");
                dicUser.get(playerId).setAG(Long.parseLong(arrNumber[0]));
                dicUser.get(playerId).setVIP(Short.parseShort(arrNumber[1]));
                dicUser.get(playerId).setLQ(Integer.parseInt(arrNumber[2]));
                dicUser.get(playerId).setMarkVip(Integer.parseInt(arrNumber[3]));
            }

            //Check xem co du dieu kien khuyen mai thi khuyen mai luon
            if (dicUser.get(playerId).getNewGamer() != 1) {
                PromotionByUid(playerId, false); //Tang tien luon neu can thiet ==> Bo evt="7"
            }
            Logger.getLogger("PromotionHandler").info("==>CountPromotion:" + playerId + "-" + dicUser.get(playerId).getCPromot().intValue());
            JsonObject send = new JsonObject();
            send.addProperty("evt", "0");
            send.addProperty("time", System.currentTimeMillis());

            UserTrans userReturn = dicUser.get(playerId).getUserTrans();
            if (checkOldSiamOper(actionUser)) {
                if (userReturn.getAG() > 2000000000) {
                    userReturn.setAG(2000000000l);
                }
            }
            userReturn.setVippoint(ActionUtils.ConvertVipPercentToMark(dicUser.get(playerId).getSource(),
                    dicUser.get(playerId).getVIP(), dicUser.get(playerId).getMarkVip()));

            if (dicUser.get(playerId).getOperatorid() > Operator.OPERATOR_THAI2
                    && dicUser.get(playerId).getOperatorid() < Operator.OPERATOR_THAI_MAX
                    || (dicUser.get(playerId).getOperatorid() > Operator.OPERATOR_THAI_MAX
                    && dicUser.get(playerId).getOperatorid() < Operator.OPERATOR_INDO_MAX)) {
                String key_bank = KeyCachedDefine.getKeyCachedBank(actionUser);
                Long chip = (Long) UserController.getCacheInstance().get(key_bank);
                if (chip == null) { // get from DB
                    chip = actionHandler.getBank().getChipFromDB(actionUser.getSource(), actionUser.getPid() - ServerDefined.userMap.get((int) actionUser.getSource()), key_bank);
                }
                if (chip != null) {
                    userReturn.setChipbank(chip);
                }
            }

            send.addProperty("data", ActionUtils.gson.toJson(userReturn));
            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(playerId, csa);
            loggerLogin_.info("===>InfoDT:" + ActionUtils.gson.toJson(send));
            //Guide
            if ((actionUser.getLastLogin().longValue() - actionUser.getCreateTime().longValue() < 5000) && (actionUser.getVIP() == 0)) { //Lan dau tao nick
                sendToClient(playerId, "guide", "");
            }
            //Check xem có gửi về 3rdPayment khôngsetGameCount
            loggerLogin_.info("3rdPayment:" + actionUser.getUserid() + "-" + actionUser.getUsername() + "-" + actionUser.getGameCount() + "-" + actionUser.getVIP() + "-" + actionUser.getCPromot());
            if (actionUser.getGameCount() >= gameCountRule_Payment || actionUser.getVIP() >= vipRule_Payment || actionUser.getCPromot() > promotionRule_Payment) {
                sendToClient(playerId, "3rdpayment", "");
            }
            JsonObject sendUS = new JsonObject();
            sendUS.addProperty("evt", "US");
            sendUS.addProperty("data", ActionUtils.gson.toJson(userController.GetUserSettingByUserid(source, playerId - ServerDefined.userMap.get(source))));
            ClientServiceAction csaUS = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(sendUS).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(playerId, csaUS);

            loggerLogin_.info("==>IsRegister:" + dicUser.get(playerId).isRegister() + "-" + dicUser.get(playerId).getUsername());
            //if (!dicUser.get(playerId).isRegister()) {//Dispatch Get ListRoom
            JsonObject send1 = new JsonObject();
            send1.addProperty("evt", "getLR");
            send1.addProperty("pid", playerId);
            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send1));
            serviceRouter.dispatchToGameActivator(je.get("gameid").getAsInt(), request);
            if (je.get("gameid").getAsInt() == GAMEID.HILO) {
                sendToClient(playerId, "hilopro", arrTyleHilo);
            }
            checkTimeToAlertForRestart(playerId, dicUser.get(playerId).getSource());

            //Gui Special Alert cho user
            if (lsAlertPromotion != null && actionUser.getSource() == 1) { //Lang quat
                for (int i = 0; i < lsAlertPromotion.size(); i++) {
                    //System.out.println("===>opea:" + lsAlertPromotion.get(i).getOperator());
                    if ((lsAlertPromotion.get(i).getOperator() == 1) && (actionUser.getOperatorid() >= 110)) {
                        continue;
                    }
                    if ((lsAlertPromotion.get(i).getOperator() == 2) && (actionUser.getOperatorid() < 110)) {
                        continue;
                    }
                    if ((new Date()).after(lsAlertPromotion.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotion.get(i).getEndtime()))) {
                        JSent sendA = new JSent();
                        sendA.setEvt("SAON");
                        sendA.setCmd(lsAlertPromotion.get(i).getDescription());
                        ClientServiceAction csaA = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendA).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csaA);
                    }
                }
            }
            //System.out.println("===>Thong bao");
            if (lsAlertPromotion68 != null && actionUser.getSource() == 2) { //3C
                for (int i = 0; i < lsAlertPromotion68.size(); i++) {
                    if ((new Date()).after(lsAlertPromotion68.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotion68.get(i).getEndtime()))) {
                        JSent sendA = new JSent();
                        sendA.setEvt("SAON");
                        sendA.setCmd(lsAlertPromotion68.get(i).getDescription());
                        ClientServiceAction csaA = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendA).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csaA);
                    }
                }
            }
            //Gui Alert cho User Dau truong
            if (lsAlertPromotionDautruong != null && actionUser.getSource() == 3) { //68 Blue
                for (int i = 0; i < lsAlertPromotionDautruong.size(); i++) {
                    if ((new Date()).after(lsAlertPromotionDautruong.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotionDautruong.get(i).getEndtime()))) {
                        JSent sendA = new JSent();
                        sendA.setEvt("SAON");
                        sendA.setCmd(lsAlertPromotionDautruong.get(i).getDescription());
                        ClientServiceAction csaA = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendA).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csaA);
                    }
                }
            }
            //Gui Alert cho User 52Fun
            if (lsAlertPromotionFun52 != null && actionUser.getSource() == 4) { //52 Fun
                for (int i = 0; i < lsAlertPromotionFun52.size(); i++) {
                    if ((new Date()).after(lsAlertPromotionFun52.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotionFun52.get(i).getEndtime()))) {
                        JSent sendA = new JSent();
                        sendA.setEvt("SAON");
                        sendA.setCmd(lsAlertPromotionFun52.get(i).getDescription());
                        ClientServiceAction csaA = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendA).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csaA);
                    }
                }
            }
            //Gui Alert cho User Siam
//                System.out.println("==>Size Promotion:" + lsAlertPromotionThai.size() + "-" + actionUser.getPid());
            if (lsAlertPromotionThai != null && actionUser.getSource() == ServerSource.THAI_SOURCE) { //Siam
                //String ref = actionUser.getRef() ;
                for (int i = 0; i < lsAlertPromotionThai.size(); i++) {
                    if ((new Date()).after(lsAlertPromotionThai.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotionThai.get(i).getEndtime()))) {
                        //&& (ref.indexOf("cocos_siamplay_ios") == -1) && (ref.indexOf("cocos_dummy_android") == -1)) {
                        JSent sendA = new JSent();
                        sendA.setEvt("SAON");
                        sendA.setCmd(lsAlertPromotionThai.get(i).getDescription());
                        sendA.setBonus(lsAlertPromotionThai.get(i).getP());
                        ClientServiceAction csaA = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendA).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csaA);
                    }
                }
            }
            if (lsAlertPromotionHoki != null && actionUser.getSource() == 10) { //Hoki
                for (int i = 0; i < lsAlertPromotionHoki.size(); i++) {
                    if ((new Date()).after(lsAlertPromotionHoki.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotionHoki.get(i).getEndtime()))) {
//                       	System.out.println("==>Alert Value:" + lsAlertPromotionThai.get(i).getP() + "-" + actionUser.getPid()) ;
                        JSent sendA = new JSent();
                        sendA.setEvt("SAON");
//                            sendA.setCmd(lsAlertPromotionThai.get(i).getDescription());
                        sendA.setCmd(lsAlertPromotionHoki.get(i).getDescription());
                        sendA.setBonus(lsAlertPromotionHoki.get(i).getP());
                        ClientServiceAction csaA = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendA).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csaA);
                    }
                }
            }
            //Neu Table ID bang 0 ==> gui ve cho mot thong bao vao ban luon
            loggerLogin_.info("==>tableid for Reconnect: " + actionUser.getUserid() + "-" + actionUser.getTableId());

            if (actionUser.getTableId() != 0) { //Login lai ==> Gui 1 lenh
                if (source == ServerSource.IND_SOURCE) {
                    loggerLogin_.info("==>Reconnect" + actionUser.getUserid() + "-" + actionUser.getUsername() + "-" + actionUser.getGameid());
                    JoinRequestAction action = new JoinRequestAction(playerId, actionUser.getTableId(), -1, "");
                    serviceRouter.dispatchToGame((int) actionUser.getGameid(), action);
                } else {
                    JSent sendC = new JSent();
                    sendC.setEvt("reconnect");
                    sendC.setCmd(Integer.toString(actionUser.getTableId()));
                    ClientServiceAction csa2 = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendC).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa2);
                }
            }
            //Tai xiu
            if (IsRunServerTaiXiu) {
                long seconds = (System.currentTimeMillis() - taixiuHandler.startTime) / 1000;
//                    System.out.println("==>SelectG:" + taixiuHandler.startTime + "-" + taixiuHandler.statusTaixiu + "-" + seconds) ;
                if (taixiuHandler.statusTaixiu) {
                    JsonObject sendTX = new JsonObject();
                    sendTX.addProperty("evt", "highlow1");
                    sendTX.addProperty("H", taixiuHandler.AgTai);
                    sendTX.addProperty("L", taixiuHandler.AgXiu);
                    sendTX.addProperty("NH", taixiuHandler.NTai);
                    sendTX.addProperty("NL", taixiuHandler.NXiu);
                    sendTX.addProperty("UH", actionUser.getAGHigh());
                    sendTX.addProperty("UL", actionUser.getAGLow());
                    sendTX.addProperty("T", TaiXiuHandler.timePlay - 1 - seconds);
                    sendTX.addProperty("strH", TaiXiuHandler.strHistoryTaixiu);
                    if (actionUser.getTableId() != 0) {
                        sendTX.addProperty("MB", GetMaxBetHighlow((int) actionUser.getGameid(), actionUser.getAS().intValue(), actionUser.getAG().intValue()));
                    } else {
                        sendTX.addProperty("MB", actionUser.getAG().intValue());
                    }
                    ClientServiceAction csataixiu = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendTX).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csataixiu);
                } else {
                    seconds = (System.currentTimeMillis() - taixiuHandler.stopTime) / 1000;
                    JsonObject sendTXW = new JsonObject();
                    sendTXW.addProperty("evt", "highlowwait1");
                    sendTXW.addProperty("T", TaiXiuHandler.timeWait - 1 - seconds);
                    sendTXW.addProperty("strH", TaiXiuHandler.strHistoryTaixiu);
                    if (actionUser.getTableId() != 0) {
                        sendTXW.addProperty("MB", GetMaxBetHighlow((int) actionUser.getGameid(), actionUser.getAS().intValue(), actionUser.getAG().intValue()));
                    } else {
                        sendTXW.addProperty("MB", actionUser.getAG().intValue());
                    }
                    ClientServiceAction csataixiuw = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendTXW).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csataixiuw);
                }
            }
            //}
            loggerLogin_.info("==>SelectGTime:" + playerId + "-" + String.valueOf((System.nanoTime() - startTime) / 1000));
//            reqStats.addMicro((System.nanoTime() - startTime) / 1000);
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_SelectGame:" + e.getMessage());
            Logger.getLogger("Debug_service").error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void processCheckIp_IsOnline_AndOther(int gameid, UserInfo actionUser, int playerId, int source) {
        try {
            loggerLogin_.info("processCheckIp_IsOnline_AndOther: gameId=" + gameid + " pid=" + playerId + " source=" + source + " actionPid=" + actionUser.getUserid());
            String ipcurrent = userController.IpServerCurrent(source, playerId - ServerDefined.userMap.get(source));
            if (!ipcurrent.equals(ipAddressServer)) {
                loggerLogin_.info("==>Hack nhe:" + playerId + "-" + actionUser.getUsername());
            }
            loggerLogin_.info("IPserver:" + ipAddressServer + "-" + playerId);
            if (actionUser.getIsOnline() != gameid) {
//                userController.UpdateIsOnlineToCache(source, playerId - ServerDefined.userMap.get(source), ipAddressServer, (short) gameid, actionUser.getOperatorid(), actionUser.getsIP());
                //Lay so luong thu moi
                dicUser.get(playerId).setCMsg(userController.GetNumberNewMailDB(source, actionUser.getUsername(), actionUser.getUsernameOld()));
            }
            dicUser.get(playerId).setGameid((short) gameid);
            actionHandler.getSelectGHandler().processHappyHour(dicUser.get(playerId), userController);

            String strReturn = userController.CheckUserInCache(source, playerId - ServerDefined.userMap.get(source), actionUser.getFacebookid(), actionUser.getGameid(), actionUser.getDeviceId(), actionUser.getSource(), actionUser.getUsername(), ipAddressServer);
            if (strReturn.length() > 1) {
                String[] arrNumber = strReturn.split(";");
                dicUser.get(playerId).setAG(Long.parseLong(arrNumber[0]));
                dicUser.get(playerId).setVIP(Short.parseShort(arrNumber[1]));
                dicUser.get(playerId).setLQ(Integer.parseInt(arrNumber[2]));
                dicUser.get(playerId).setMarkVip(Integer.parseInt(arrNumber[3]));
            }
            //Check xem user lan dau tao nick
            //boolean isFirst = false;
            if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == ServerSource.IND_SOURCE) {
                if (dicUser.get(playerId).getUsernameLQ().length() == 0) {
                    dicUser.get(playerId).setDefaultName(lsDefaultGame.get(0));
                }
                if (dicUser.get(playerId).getAGNewGamer() > 0) {
                    dicUser.get(playerId).IncrementMark(dicUser.get(playerId).getAGNewGamer());
                    dicUser.get(playerId).setNewGamer(2);
                    dicUser.get(playerId).setAGNewGamer(0);
                    Logger.getLogger("KHUYENMAILOG").info(String.valueOf(actionUser.getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid() + "#0#10#" + String.valueOf(dicUser.get(playerId).getAGNewGamer()) + "#" + String.valueOf((new Date()).getTime()));
                }
            }
            //Check xem co du dieu kien khuyen mai thi khuyen mai luon
            /*if (dicUser.get(playerId).getNewGamer() != 1) {
                PromotionByUid(playerId, false); //Tang tien luon neu can thiet ==> Bo evt="7"
            }*/

            Logger.getLogger("PromotionHandler").info("==>CountPromotion:" + playerId + "-" + dicUser.get(playerId).getCPromot().intValue());

        } catch (Exception e) {
            loggerLogin_.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    //Xu ly Select G2 New
    public void processSelectG2(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            int gameid = je.get("gameid").getAsInt();
            int source = actionUser.getSource();
            if (actionUser.getTableId() != 0) {
                if (actionUser.getGameid() != gameid) {
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "reconnect");
                    send.addProperty("gameid", actionUser.getGameid());
                    ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                    loggerLogin_.info("==>MustReconnect: " + actionUser.getUserid() + "-" + actionUser.getUsername()
                            + " - actionUser.getGameid(): " + actionUser.getGameid() + " - online: " + actionUser.getIsOnline()
                            + " - select gameid: " + gameid + " - actionUser.getTableId(): " + actionUser.getTableId());
                } else {
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "selectG2");
                    send.addProperty("pid", playerId);
                    ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                    serviceRouter.dispatchToGameActivator(gameid, request);
                    loggerLogin_.info("==>Reconnect: " + actionUser.getUserid() + "-" + actionUser.getUsername()
                            + " - actionUser.getGameid(): " + actionUser.getGameid() + " - online: " + actionUser.getIsOnline()
                            + " - select gameid: " + gameid + " - actionUser.getTableId(): " + actionUser.getTableId());
                    JoinRequestAction action = new JoinRequestAction(playerId, actionUser.getTableId(), -1, "");
                    serviceRouter.dispatchToGame((int) actionUser.getGameid(), action);
                }
            } else {
                if (actionUser.getIsOnline() != gameid) {
                    userController.UpdateIsOnlineToCache(source, playerId - ServerDefined.userMap.get(source), ipAddressServer, (short) gameid, actionUser.getOperatorid(), actionUser.getsIP());
                }
                actionHandler.getSelectGHandler().processHappyHour(actionUser, userController);
                dicUser.get(playerId).setGameid((short) gameid);
                JsonObject send = new JsonObject();
                send.addProperty("evt", "selectG2");
                send.addProperty("pid", playerId);
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(gameid, request);
                loggerLogin_.info("==>SelectG2" + actionUser.getUserid() + "-" + actionUser.getUsername() + "-" + actionUser.getGameid() + "-" + actionUser.getTableId());
            }

            //process promotion for selectg2 user
            lsAlertPromotion = userController.GameGetAlertPromotion(actionUser.getSource());

            int userId = actionUser.getUserid() > ServerDefined.userMap.get(ServerSource.MYA_SOURCE)
                    ? actionUser.getUserid() - ServerDefined.userMap.get(ServerSource.MYA_SOURCE) : actionUser.getUserid();
            UserInfo userInfo = userController.GetUserInfoFromCache(actionUser.getSource(), userId);

            if (userInfo != null && userInfo.isOverComeMinVip()) {
                for (int i = 0; i < lsAlertPromotion.size(); i++) {
                    if ((new Date()).after(lsAlertPromotion.get(i).getStarttime())
                            && ((new Date()).before(lsAlertPromotion.get(i).getEndtime()))) {
                        JSent send = new JSent();
                        send.setEvt("SAON");
                        send.setCmd(lsAlertPromotion.get(i).getDescription());
                        ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String updateAuth(String data, String auth) {
        try {
            Logger.getLogger("LoginandDisconnect").info("data: " + data + "auth " + auth);
            if (StringUtils.isNoneEmpty(data)) {
                LoginGameData loginGameData = new Gson().fromJson(data, LoginGameData.class);
                loginGameData.setAuth(auth);

                return new Gson().toJson(loginGameData);
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger("LoginandDisconnect").error("err ==> " + e.getMessage(), e);
        }

        return data;
    }

    public String getLoginGameData(int gameid, UserInfo actionUser, int playerId, int typeLogin) {
        synchronized (dicUser) {
            try {
                long startTime = System.nanoTime();
                int source = actionUser.getSource();
                loggerLogin_.info("===>getLoginGameData: " + playerId + " - gameid: " + gameid);
                processCheckIp_IsOnline_AndOther(gameid, actionUser, playerId, source);
                UserTrans2 userReturn = dicUser.get(playerId).getUserTrans2();
                if (dicUser.get(playerId).getSource() == ServerSource.INDIA_SOURCE) {
                    userReturn.setPD(dicUser.get(playerId).getPromotionDaily());
                    userReturn.setOD(dicUser.get(playerId).getOnlineDaily());
                } else if (dicUser.get(playerId).getSource() == ServerSource.MYA_SOURCE) {
                    userReturn.setPD(dicUser.get(playerId).getPromotionDaily());
                    userReturn.setOD(dicUser.get(playerId).getOnlineDaily());
                }

                if (typeLogin == 3) {
                    userReturn.setGameid(gameid);
                }
                LoginGameData data = new LoginGameData();
                data.setEvt("0");
                data.setTime(System.currentTimeMillis());

                int mail = dicUser.get(playerId).getCMsg();
                if (mail > 99999) {
                    userReturn.setNM(mail % 100000); // so thu tu admin + ag
                    userReturn.setNumFriendMail(mail / 100000); // so thu tu friend
                }

                String userData = ActionUtils.gson.toJson(userReturn);
                data.setData(userData);
                loggerLogin_.info("===>Process_SelectGame2:" + ActionUtils.gson.toJson(userData));

                loggerLogin_.info("==>getLoginGameData==>SelectGTime:" + playerId + "-" + String.valueOf((System.nanoTime() - startTime) / 1000));
//                reqStats.addMicro((System.nanoTime() - startTime) / 1000);
                return ActionUtils.gson.toJson(data);
            } catch (Exception e) {
                loggerLogin_.error(e.getMessage(), e);
                e.printStackTrace();
            }
            return "";
        }
    }

    private void checkTimeToAlertForRestart(int pid, short source) {
        try {
            Calendar c = Calendar.getInstance();
            if ((c.get(Calendar.HOUR_OF_DAY) == 4) && (c.get(Calendar.MINUTE) >= 45) && (c.get(Calendar.MINUTE) <= 59)) {
                JSent send2 = new JSent();
                send2.setEvt("100");
                String msg = "";
                if (source == ServerSource.THAI_SOURCE) // ทีมงานจะปิดปรับปรุงเซิฟร์เวอร์5นาทีในอีก15นาที
                {
                    msg = "ทีมงานจะปิดปรับปรุงเซิฟร์เวอร์5นาทีในอีก " + (59 - c.get(Calendar.MINUTE)) + "นาที";
                } else {
                    msg = "Server sẽ bảo trì 5 phút sau " + (59 - c.get(Calendar.MINUTE)) + " phút nữa";
                }
                send2.setCmd(msg);
                ClientServiceAction csa2 = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send2).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(pid, csa2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Xu ly Select Room
    private void Process_SelectRoom(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                //if (actionUser.getUnlockPass() == 0) return ;
                JsonObject send = new JsonObject();
                send.addProperty("evt", "selectR");
                send.addProperty("pid", playerId);
                send.addProperty("id", je.get("id").getAsInt());
                send.addProperty("oldid", actionUser.getRoomId());
                send.addProperty("ag", actionUser.getAG());
                send.addProperty("vip", actionUser.getVIP());
                send.addProperty("invite", actionUser.isAutoInvite());
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
                dicUser.get(playerId).setAutoInvite(false);
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_SelectRoom:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_SelectRoomOnly(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                //if (actionUser.getUnlockPass() == 0) return ;
                JsonObject send = new JsonObject();
                send.addProperty("evt", "selectROnly");
                send.addProperty("pid", playerId);
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
                dicUser.get(playerId).setAutoInvite(false);
            }
        } catch (Exception e) {
            // handle exception
            System.out.println("==>Error==>Process_SelectRoomOnly:" + e.getMessage());
        }
    }

    private void Process_SelectRoom_Siam(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getTableId() != 0) {
                    return;
                }
                JsonObject send = new JsonObject();
                send.addProperty("evt", "selectR");
                send.addProperty("pid", playerId);
                send.addProperty("id", je.get("id").getAsInt());
                send.addProperty("oldid", actionUser.getRoomId());
                send.addProperty("ag", actionUser.getAG());
                send.addProperty("vip", actionUser.getVIP());
                send.addProperty("invite", actionUser.isAutoInvite());
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
                dicUser.get(playerId).setAutoInvite(false);
                //Select T luon
                Process_SearchTable_Siam(je, actionUser, playerId);
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_SelectRoom:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_SearchTable_Siam(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                JsonObject send = new JsonObject();
                send.addProperty("evt", "searchTSiam");
                send.addProperty("pid", playerId);
                send.addProperty("M", actionUser.getAG());
                send.addProperty("VIP", actionUser.getVIP());
                send.addProperty("lobby", actionUser.getRoomId());
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_SearchTable:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Xu ly Select Table
    private void Process_Playnow(JsonObject je, UserInfo actionUser, int playerId, String evt) {
        try {
            loggerLogin_.info("==>Playnow:" + je.get("M").getAsInt() + "-" + je.get("D").getAsInt() + "-" + actionUser.getGameid() + "-" + je.get("S").getAsInt());
            JsonObject send = new JsonObject();
            send.addProperty("evt", evt);
            send.addProperty("pid", playerId);
            send.addProperty("M", je.get("M").getAsInt());
            send.addProperty("D", je.get("D").getAsInt());
            send.addProperty("S", je.get("S").getAsInt());
            if (je.has("T")) {
                send.addProperty("T", je.get("T").getAsInt());
            } else {
                send.addProperty("T", 0);
            }
            ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
            serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
        } catch (Exception e) {
//            System.out.println("==>Error==>Process_Playnow:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Xu ly Select Table
    private void Process_SearchTable(JsonObject jo, UserInfo actionUser, int playerId, String evt, int newVer, int diamondType) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                JsonObject send = new JsonObject();
                send.addProperty("evt", evt);
                send.addProperty("pid", playerId);
                if (diamondType == 1) {
                    send.addProperty("M", actionUser.getDiamond());
                } else {
                    send.addProperty("M", actionUser.getAG());
                }
                send.addProperty("VIP", actionUser.getVIP());
                send.addProperty("D", diamondType);
                send.addProperty("NewVer", newVer);
                send.addProperty("lobby", actionUser.getRoomId());
                if (jo.has("T")) {
                    send.addProperty("T", jo.get("T").getAsInt());
                }
                if (jo.has("Type")) {
                    send.addProperty("Type", jo.get("Type").getAsInt());
                }
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
//            System.out.println("==>Error==>Process_SearchTable:" + e.getMessage());
        }
    }

    private void Process_SearchTableOV(JsonObject je, UserInfo actionUser, int playerId, int mark) {
        try {
            synchronized (dicUser) {
//            	System.out.println("==>SearchTOV nao:") ;
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                JsonObject send = new JsonObject();
                send.addProperty("evt", "searchTOV");
                send.addProperty("pid", playerId);
                send.addProperty("M", mark);
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_SearchTableOV:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_SearchTableSiamRe(JsonObject je, UserInfo actionUser, int playerId, int newVer) {
        try {
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                JsonObject send = new JsonObject();
                send.addProperty("evt", "searchTSiamRe");
                send.addProperty("pid", playerId);
                send.addProperty("M", actionUser.getAG());
                send.addProperty("VIP", actionUser.getVIP());
                send.addProperty("NewVer", newVer);
                send.addProperty("lobby", actionUser.getRoomId());
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_SearchTable:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Xu ly Select Table Poker Texas
    private void Process_SearchTablePokerTexas(JsonObject je, UserInfo actionUser, int playerId, String evt) {
        try {
            synchronized (dicUser) {
//            	System.out.println("==>SearchT nao:") ;
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                //System.out.println("searchTPokerTexas- "+actionUser.getGameid()+"-"+je.get("M").getAsLong());
                JsonObject send = new JsonObject();
                send.addProperty("evt", evt);
                send.addProperty("pid", playerId);
                send.addProperty("M", je.get("M").getAsLong());
                //send.addProperty("VIP", actionUser.getVIP());
//                System.out.println("==>New Ver searchT:" + newVer) ;
                //send.addProperty("NewVer", newVer);
                //send.addProperty("lobby", actionUser.getRoomId());
                send.addProperty("AutoFill", je.get("AutoFill").getAsBoolean());
                send.addProperty("AutoTopOff", je.get("AutoTopOff").getAsBoolean());
                send.addProperty("BuyIn", je.get("BuyIn").getAsLong());
                if (je.has("Type")) {
                    send.addProperty("Type", je.get("Type").getAsInt());
                }
                if (je.has("Player")) {
                    send.addProperty("Player", je.get("Player").getAsInt());
                }
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Process_GetCurrentPlayerInRoom(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                JsonObject send = new JsonObject();
                send.addProperty("evt", "currentplayer");
                send.addProperty("pid", playerId);
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_SearchTable:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Process_PromotionSiam(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                int source = actionUser.getSource();
                Logger.getLogger("PromotionHandler").info(je.get("evt").getAsString());
                if (je.get("evt").getAsString().equals("promotion")) { //Nhan gold
                    Logger.getLogger("PromotionHandler").info(" " + actionUser.getPid() + " - " + ActionUtils.gson.toJson(je) + " - tableid: " + actionUser.getTableId());
                    if (actionUser.getTableId() > 0) {
                        return;
                    }
                    DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
                    if (!dos.allow("promotion", actionUser.getUserid())) {
                        return;
                    }
                    String strReturn = promotionHandler.RemovePromotionInCache(source, playerId - ServerDefined.userMap.get(source), je.get("T").getAsInt(), je.get("G").getAsInt(), (int) actionUser.getVIP());
                    int goldReceive = Integer.parseInt(strReturn.split(";")[0]);
                    Logger.getLogger("PromotionHandler").info("promotion:" + je.get("T").getAsInt() + "-" + je.get("G").getAsInt() + "-" + goldReceive + "-" + strReturn);
                    if (goldReceive == -1) {
                        JsonObject sendL = new JsonObject(); //User Setting
                        if (actionUser.getSource() == 2) {
                            sendL.addProperty("evt", "firstlogin1");
                        } else {
                            sendL.addProperty("evt", "firstlogin");
                        }
                        sendL.addProperty("CF", actionUser.getCFriendsF());
                        sendL.addProperty("OD", actionUser.getOnlineDaily());
                        String strF = "";
                        for (int i = 0; i < agDailyFriendFace.length; i++) {
                            strF = strF + agDailyFriendFace[i] + ";";
                        }
                        String strCF = "";
                        for (int i = 0; i < conDailyFriendFace.length; i++) {
                            strCF = strCF + conDailyFriendFace[i] + ";";
                        }
                        String strO = "";
                        for (int i = 0; i < agDailyPromotion.length; i++) {
                            strO = strO + agDailyPromotion[i] + ";";
                        }
                        String strR = "";
                        for (int i = 0; i < agDailyRotation.length; i++) {
                            strR = strR + agDailyRotation[i] + ";";
                        }
                        sendL.addProperty("ListCF", strCF);
                        sendL.addProperty("ListF", strF);
                        sendL.addProperty("ListO", strO);
                        sendL.addProperty("ListR", strR);
                        ClientServiceAction csaL = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(sendL).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csaL);
                    } else {
                        if (goldReceive > 0) {
                            dicUser.get(playerId).IncrementMark(goldReceive);
                            userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), goldReceive, actionUser.getVIP(), 0l);
                            //Log ifrs
                            if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                                Logger.getLogger("KHUYENMAILOG").info(String.valueOf(playerId - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid() + "#0#" + String.valueOf(je.get("T").getAsInt() + 20) + "#" + String.valueOf(goldReceive) + "#" + String.valueOf((new Date()).getTime()));
                            }
                        }
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "promotion");
                        jo.addProperty("G", goldReceive);
                        jo.addProperty("T", je.get("T").getAsInt());
                        jo.addProperty("TO", Integer.parseInt(strReturn.split(";")[1])); //Time online con lai
                        jo.addProperty("NV", Integer.parseInt(strReturn.split(";")[2])); //Time online con lai
                        jo.addProperty("NO", Integer.parseInt(strReturn.split(";")[3])); //Time online con lai
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                } else if (je.get("evt").getAsString().equals("promotion_info")) {
                    String promotioninfo = promotionHandler.GetPromotionInfo(source, playerId - ServerDefined.userMap.get(source), actionUser.getDeviceId(), (int) actionUser.getVIP(), actionUser.getRef());
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "promotion_info");
                    Logger.getLogger("PromotionHandler").info("promotioninfo:" + promotioninfo);
                    String[] arrStr = promotioninfo.split(";");
                    jo.addProperty("P", Integer.parseInt(arrStr[0]));
                    jo.addProperty("A", Integer.parseInt(arrStr[1]));
                    jo.addProperty("UV", Integer.parseInt(arrStr[2]));
                    jo.addProperty("O", Integer.parseInt(arrStr[3]));
                    jo.addProperty("V", Integer.parseInt(arrStr[4]));
                    jo.addProperty("C", Integer.parseInt(arrStr[5]));
                    jo.addProperty("T", Integer.parseInt(arrStr[6])); //Time Online
                    jo.addProperty("VC", Integer.parseInt(arrStr[7])); //Video current
                    jo.addProperty("VM", Integer.parseInt(arrStr[8])); //Video max
                    jo.addProperty("OC", Integer.parseInt(arrStr[9])); //Online current
                    jo.addProperty("OM", Integer.parseInt(arrStr[10])); //Online max
                    jo.addProperty("NV", Integer.parseInt(arrStr[11])); //Next Video
                    jo.addProperty("NO", Integer.parseInt(arrStr[12])); //Next Online
                    jo.addProperty("NIV", 10000); //Invite Friend
                    jo.addProperty("InviteMark", 500); //Invite Friend
                    jo.addProperty("InviteNum", 40); //Invite Friend
                    Logger.getLogger("PromotionHandler").info(ActionUtils.gson.toJson(jo));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("evt").getAsString().equals("promotion_online")) { //Nhan gold
                    String agonline = promotionHandler.PromotionOnline(source, playerId - ServerDefined.userMap.get(source), actionUser.getDeviceId(), (int) actionUser.getVIP());
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "promotion_online");
                    jo.addProperty("G", Integer.parseInt(agonline.split(";")[0]));
                    jo.addProperty("NO", Integer.parseInt(agonline.split(";")[1]));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("evt").getAsString().equals("promotion_video_start")) { //Video
                    int viewvideo = promotionHandler.PromotionVideoStart(source, playerId - ServerDefined.userMap.get(source), actionUser.getRef());
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "promotion_video_start");
                    if (viewvideo == 1) {
                        jo.addProperty("V", 1);
                        jo.addProperty("Msg", "");
                    } else {
                        jo.addProperty("V", 0);
                        if (viewvideo == 0) {
                            if (source == ServerSource.THAI_SOURCE) {
                                jo.addProperty("Msg", "คุณดูวิดีโอทั้งหมดในวันนี้แล้ว กรุณารอถึงพรุ่งนี้ค่ะ");
                            } else {
                                jo.addProperty("Msg", "Anda sudah nonton video hari ini. Mohon tunggu sampai besok!");
                            }
                        } else {
                            String str = "";
                            str = actionUtils.getConfigText("strVideoPre1", actionUser.getSource(), actionUser.getUserid()) + String.valueOf(((int) (viewvideo / 60)))
                                    + actionUtils.getConfigText("strVideoPre2", actionUser.getSource(), actionUser.getUserid()) + String.valueOf(viewvideo % 60)
                                    + actionUtils.getConfigText("strVideoSuffix", actionUser.getSource(), actionUser.getUserid());
                            //str = "รออีก " + String.valueOf(((int)(viewvideo / 60))) + " นาที " + String.valueOf(viewvideo % 60) +" วินาที\nถึงจะได้กลับไปดูวิดีโอเพื่อรับชิป" ;
                            jo.addProperty("Msg", str);
                        }
                    }
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("evt").getAsString().equals("promotion_video")) { //Video
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "promotion_video");
                    String strOn = promotionHandler.PromotionVideo(source, playerId - ServerDefined.userMap.get(source), actionUser.getDeviceId());
                    jo.addProperty("G", Integer.parseInt(strOn.split(";")[0]));
                    jo.addProperty("NV", Integer.parseInt(strOn.split(";")[1]));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Process_NewGamer(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                if (actionUser.getNewGamer() == 1) {
                    dicUser.get(playerId).IncrementMark(dicUser.get(playerId).getAGNewGamer());
                    dicUser.get(playerId).setNewGamer(2);
                    if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                        Logger.getLogger("KHUYENMAILOG").info(String.valueOf(actionUser.getUserid() - ServerDefined.userMap.get((int) actionUser.getSource())) + "#"
                                + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid()
                                + "#0#10#" + String.valueOf(dicUser.get(playerId).getAGNewGamer()) + "#"
                                + String.valueOf((new Date()).getTime()));
                        //userController.GameITempPromoteDtNewUser(getConnectionThai(), dicUser.get(playerId).getUserid() - 500000000, dicUser.get(playerId).getAGNewGamer(),0,0, dicUser.get(playerId).getDeviceId()) ;
                    }
                    JsonObject act1 = new JsonObject();
                    act1.addProperty("evt", "newgamerok");
                    act1.addProperty("M", dicUser.get(playerId).getAGNewGamer());
                    dicUser.get(playerId).setAGNewGamer(0);
                    ClientServiceAction csa1 = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act1).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa1);
                    //Send Daily Promotion
                    /*if (!actionUser.isReceiveDailyPromotion()) { //Lan dau Login trong ngay ==> Danh sach qua tang
                     JsonObject sendL = new JsonObject(); //User Setting
                     sendL.addProperty("evt", "firstlogin");
                     sendL.addProperty("CF", actionUser.getCFriendsF());
                     sendL.addProperty("OD", actionUser.getOnlineDaily());
                     String strF = "" ;
                     for (int i = 0; i < agDailyFriendFace.length; i++) {
                     strF = strF + agDailyFriendFace[i] + ";" ;
                     }
                     String strCF = "" ;
                     for (int i = 0; i < conDailyFriendFace.length; i++) {
                     strCF = strCF + conDailyFriendFace[i] + ";" ;
                     }
                     String strO = "";
                     for (int i = 0; i < agDailyPromotion.length; i++) {
                     strO = strO + agDailyPromotion[i] + ";" ;
                     }
                     String strR = "";
                     for (int i = 0; i < agDailyRotation.length; i++) {
                     strR = strR + agDailyRotation[i] + ";" ;
                     }
                     sendL.addProperty("ListCF", strCF);
                     sendL.addProperty("ListF", strF);
                     sendL.addProperty("ListO", strO);
                     sendL.addProperty("ListR", strR);
                     ClientServiceAction csaL = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(sendL).getBytes("UTF-8"));
                     serviceRouter.dispatchToPlayer(playerId, csaL);
                     }*/
                }
            }
        } catch (Exception e) {
            // handle exception
//            System.out.println("==>Error==>Process_NewGamer:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Xu ly Invite
    private void Process_Invite(JsonObject je, UserInfo actionUser, int playerId) {
        try {
//            System.out.println("==>ServicesHandker==>Process_Invite: pid: " + playerId + " - " + ActionUtils.gson.toJson(je) + " - tableID: " + actionUser.getTableId());
            synchronized (dicUser) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                if (actionUser.getTableId() == 0) {
                    return;
                }
                int isD = 0;
                if (je.has("D")) {
                    isD = je.get("D").getAsInt();
                }
                if (je.get("T").getAsString().equals("0")) { //Lay danh sach moi
                    UserInfo meInvite = dicUser.get(playerId);
                    Collection<UserInfo> collectionOfAllPlayer = dicUser.values();
                    List<UserInvite> listReturn = new ArrayList<UserInvite>();
                    int agCon = je.get("AG").getAsInt() * 10;
                    int gameidTemp = meInvite.getGameid();
                    int agUnit = 110;
                    int agmax = 1000000000;
                    if ((gameidTemp == 8021) || (gameidTemp == 8024)) {
                        agCon = je.get("AG").getAsInt() * 250;
                        if (je.get("AG").getAsInt() == 2) {
                            agmax = 10000;
                        } else if (je.get("AG").getAsInt() == 10) {
                            agmax = 25000;
                        } else if (je.get("AG").getAsInt() == 50) {
                            agmax = 100000;
                        } else if (je.get("AG").getAsInt() == 200) {
                            agmax = 400000;
                        } else if (je.get("AG").getAsInt() == 1000) {
                            agmax = 1500000;
                        } else if (je.get("AG").getAsInt() == 5000) {
                            agmax = 7500000;
                        }
                    } else if (gameidTemp == 8031) { // PokerTexas     
                        agCon = je.get("AG").getAsInt() * 100;
                        agmax = je.get("AG").getAsInt() * 600;
                    } else if (IsRunIndo && gameidTemp == 8004) {
                        agCon = je.get("AG").getAsInt() * 10;
                        agmax = je.get("AG").getAsInt() * 100;
                    } else if (gameidTemp == 8041) { //Rummy 
                        if (je.get("AG").getAsInt() == 100) {
                            agCon = je.get("AG").getAsInt() * 5;
                            agmax = je.get("AG").getAsInt() * 300;
                        } else if (je.get("AG").getAsInt() == 500) {
                            agCon = je.get("AG").getAsInt() * 20;
                            agmax = je.get("AG").getAsInt() * 300;
                        } else if (je.get("AG").getAsInt() == 1000) {
                            agCon = je.get("AG").getAsInt() * 30;
                            agmax = je.get("AG").getAsInt() * 300;
                        } else if (je.get("AG").getAsInt() == 5000) {
                            agCon = je.get("AG").getAsInt() * 40;
                            agmax = je.get("AG").getAsInt() * 300;
                        } else {
                            agCon = je.get("AG").getAsInt() * 80;
                            agmax = 1000000000;
                        }
                    } else if (gameidTemp == 8040) { //Rummy 
                        if (je.get("AG").getAsInt() <= 500) {
                            agCon = je.get("AG").getAsInt() * 20;
                            agmax = je.get("AG").getAsInt() * 300;
                        } else {
                            agCon = je.get("AG").getAsInt() * 50;
                            agmax = 1000000000;
                        }
                    }
                    //System.out.println("==>Process_Invite: agCon"+agCon+" agmax "+agmax+" gameidTemp"+gameidTemp) ;
                    Iterator<UserInfo> ix = collectionOfAllPlayer.iterator();
                    while (ix.hasNext()) {
                        UserInfo userinvite = ix.next();
                        // System.out.println("==>Process_Invite1: meInvite.getGameid()"+meInvite.getGameid()+"-"+meInvite.getUsername()+" userinvite.getGameid() "+userinvite.getGameid()+userinvite.getUsername()
                        // +" userinvite.getAG().ID"+userinvite.getUserid()+" - tableid"+userinvite.getTableId()+" userinvite.getAG().intValue() "+userinvite.getAG().intValue()+
                        //" userinvite.getVIP() "+userinvite.getVIP()) ;
                        if (isD == 1) {
                            if ((meInvite.getGameid() == userinvite.getGameid())// && (meInvite.getRoomId() == userinvite.getRoomId())
                                    && (userinvite.getTableId() == 0) && (userinvite.getDiamond() >= agCon) && (userinvite.getDiamond() < agmax)) {
                                if ((userinvite.getVIP() > 0) || (userinvite.getVIP() == 0 && je.get("AG").getAsInt() < agUnit)) {
                                    UserInvite youInvite = new UserInvite();
                                    youInvite.setId(userinvite.getPid());
                                    youInvite.setN(userinvite.getUsername());
                                    youInvite.setAG(userinvite.getDiamond());
                                    youInvite.setV(userinvite.getVIP());
                                    listReturn.add(youInvite);
                                    if (listReturn.size() > 5) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            if ((meInvite.getGameid() == userinvite.getGameid())// && (meInvite.getRoomId() == userinvite.getRoomId())
                                    && (userinvite.getTableId() == 0) && (userinvite.getAG().intValue() >= agCon) && (userinvite.getAG().intValue() < agmax)) {
                                if ((userinvite.getVIP() > 0) || (userinvite.getVIP() == 0 && je.get("AG").getAsInt() < agUnit)) {
                                    UserInvite youInvite = new UserInvite();
                                    youInvite.setId(userinvite.getPid());
                                    youInvite.setN(userinvite.getUsername());
                                    youInvite.setNLQ(userinvite.getUsernameLQ());
                                    youInvite.setAvatar(userinvite.getAvatar());
                                    youInvite.setAG(userinvite.getAG());
                                    youInvite.setV(userinvite.getVIP());
                                    listReturn.add(youInvite);
                                    if (listReturn.size() > 5) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (gameidTemp == GAMEID.BINH && IsRunIndo) {
                        if (mapBot.get(GAMEID.BINH) != null) {
                            synchronized (mapBot.get(GAMEID.BINH).getListBot()) {
                                ix = mapBot.get(GAMEID.BINH).getListBot().iterator();
                                while (ix.hasNext()) {
                                    UserInfo userinvite = ix.next();
                                    if ((meInvite.getGameid() == userinvite.getGameid())// && (meInvite.getRoomId() == userinvite.getRoomId())
                                            && (userinvite.getTableId() == 0) && (userinvite.getAG().intValue() >= agCon) && (userinvite.getAG().intValue() < agmax)) {
                                        if ((userinvite.getVIP() > 0) || (userinvite.getVIP() == 0 && je.get("AG").getAsInt() < agUnit)) {
                                            UserInvite youInvite = new UserInvite();
                                            youInvite.setId(userinvite.getPid());
                                            youInvite.setN(userinvite.getUsername());
                                            youInvite.setAG(userinvite.getAG());
                                            youInvite.setV(userinvite.getVIP());
                                            listReturn.add(youInvite);
                                            if (listReturn.size() > (1 + ActionUtils.random.nextInt(5))) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ix = null;
                    collectionOfAllPlayer = null;

                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "ivp");
                    jo.addProperty("data", ActionUtils.gson.toJson(listReturn));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("T").getAsString().equals("1")) { //Gui thong tin moi den OID
                    JsonObject jo = new JsonObject();
                    UserInfo meInvite = dicUser.get(playerId);
                    jo.addProperty("evt", "ivp");
                    jo.addProperty("N", meInvite.getUsername());
                    jo.addProperty("AG", meInvite.getAG());
                    jo.addProperty("T", "1");
                    jo.addProperty("TID", meInvite.getTableId());
                    jo.addProperty("AGU", je.get("AG").getAsInt());
                    ClientServiceAction csa = new ClientServiceAction(je.get("OID").getAsInt(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(je.get("OID").getAsInt(), csa);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Xuly khac
    private void Process_Other(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
//            	System.out.println("==>OK :" + playerId + "-" + je.get("evt").getAsString()) ;
                if (je.get("evt").getAsString().equals("displayrule_siam")) {
                    userController.GetListDisplayRule(ServerSource.MYA_SOURCE); //Get rule display Function
                } else if (je.get("evt").getAsString().equals("displayrule_52")) {
                    userController.GetListDisplayRule(4); //Get rule display Function
                } else if (je.get("evt").getAsString().equals("2")) {
//                	System.out.println("====>Select Level:" + dicUser.get(actionUser.getUserid()).isRegister()) ;
                    if (!dicUser.get(playerId).isRegister()) {
                        JsonObject send = new JsonObject();
                        send.addProperty("evt", "2");
                        send.addProperty("pid", playerId);
                        send.addProperty("id", je.get("id").getAsInt());
                        ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                        serviceRouter.dispatchToGameActivator(je.get("gameid").getAsInt(), request);
                    }
                } else if (je.get("evt").getAsString().equals("xocdiatool")) { // Han che xocdia
                    GameDataAction gda = new GameDataAction(playerId, 1);
                    gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(je).getBytes("UTF-8")));
                    serviceRouter.dispatchToGame(8013, gda);
                } else if (je.get("evt").getAsString().equals("info_hack")) { // region hacking lang quat xito
                    GameDataAction gda = new GameDataAction(playerId, je.get("tableid").getAsInt());
                    gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(je).getBytes("UTF-8")));
                    serviceRouter.dispatchToGame(je.get("gameid").getAsInt(), gda);
                } else if (je.get("evt").getAsString().equals("info_hack_change")) { // region hacking lang quat xito
                    GameDataAction gda = new GameDataAction(playerId, je.get("tableid").getAsInt());
                    gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(je).getBytes("UTF-8")));
                    serviceRouter.dispatchToGame(je.get("gameid").getAsInt(), gda);
                } else if (je.get("evt").getAsString().equals("lockuser")) { // Duoi khoi Game
                    synchronized (dicUser) {
                        if (dicUser.containsKey(je.get("Uid").getAsInt())) {
                            if (dicUser.get(je.get("Uid").getAsInt()).getTableId() == 0) {
                                sendToClient(dicUser.get(je.get("Uid").getAsInt()).getPid(), "10", "Bạn bị đứt kết nối với máy chủ !");
                                PlayerDisconnected(dicUser.get(je.get("Uid").getAsInt()).getPid(), (int) dicUser.get(je.get("Uid").getAsInt()).getSource());
                                sendToClient(playerId, "9998", "Khoa thanh cong !");
                            } else {
                                sendToClient(playerId, "9998", "User dang o trong ban choi !");
                            }
                        } else {
                            sendToClient(playerId, "9998", "Khong ton tai user !");
                        }
                    }
                } /*else if(je.get("evt").getAsString().equals("startlucky")) { // Setup cho 1 ai do may man trong bai may man
                 nameCardLucky = je.get("name").getAsString().trim() ;
                 isLucky = true ;
                 sendToClient(playerId, "9998", je.get("name").getAsString().trim());
                 } else if(je.get("evt").getAsString().equals("startlucky")) { // Setup cho 1 ai do may man trong bai may man
                 nameCardLucky = je.get("name").getAsString().trim() ;
                 isLucky = true ;
                 sendToClient(playerId, "9998", je.get("name").getAsString().trim());
                 } else if(je.get("evt").getAsString().equals("startsuperlucky")) { // Setup cho 1 ai do may man trong bai may man
                 nameSuperLucky = je.get("name").getAsString().trim() ;
                 sendToClient(playerId, "9998", je.get("name").getAsString().trim());
                 }*/ else if (je.get("evt").getAsString().equals("startroulette")) { // Setup cho 1 ai do may man trong bai may man
                    nameRoulette = je.get("name").getAsString().trim();
                    isRoulette = true;
                    sendToClient(playerId, "9998", je.get("name").getAsString().trim());
                }/* else if(je.get("evt").getAsString().equals("stoplucky")) { // Dung lai khong cho ai may man 
                 isLucky = false ;
                 sendToClient(playerId, "9998", "OK Stop!");
                 }*/ else if (je.get("evt").getAsString().equals("lockchat")) { // Cam chat
                    synchronized (dicUser) {
                        int source = 1;
                        if ((je.get("Uid").getAsInt() < 1000000000) || (je.get("Uid").getAsInt() > 1500000000)) {
                            source = 1;
                        } else if ((je.get("Uid").getAsInt() > 1000000000) && (je.get("Uid").getAsInt() < 1100000000)) {
                            source = 2;
                        } else if ((je.get("Uid").getAsInt() > 1100000000) && (je.get("Uid").getAsInt() < 1500000000)) {
                            source = 3;

                        } else if ((je.get("Uid").getAsInt() > 1200000000) && (je.get("Uid").getAsInt() < 1500000000)) {
                            source = 4;
                        } else if ((je.get("Uid").getAsInt() > 1300000000) && (je.get("Uid").getAsInt() < 1500000000)) {
                            source = 5;
                        } else if ((je.get("Uid").getAsInt() > 500000000) && (je.get("Uid").getAsInt() < 1000000000)) {
                            source = ServerSource.THAI_SOURCE;
                        }
                        userController.UpdateChat(source, je.get("Uid").getAsInt() - ServerDefined.userMap.get(source), 1);
                        if (dicUser.containsKey(je.get("Uid").getAsInt())) {
                            dicUser.get(je.get("Uid").getAsInt()).setChat((short) 1);
                        }
                        sendToClient(playerId, "9998", "Khoa thanh cong !");
                    }
                } else if (je.get("evt").getAsString().equals("alertV")) { // ALARM ALL PLAYER ON SERVER Lang quat
                    //lsAuction = userController.GameGetAuction(getConnection());
                    lsAlert2Player.add(je.get("Data").getAsString());
                    sendErrorMsg(playerId, "OK!");
                } else if (je.get("evt").getAsString().equals("uinfo_mem")) {
                    if (je.has("pid")) {
                        int pid = je.get("pid").getAsInt();
                        String messReturn = new Gson().toJson(dicUser.get(pid));

                        sendErrorMsg(playerId, messReturn);
                    }
                } else if (je.get("evt").getAsString().equals("alertP")) { //alertPromotion
                    //process alert turn on
                    int serverId = je.get("serverId").getAsInt();
                    if (je.get("alertOn").getAsBoolean()) {
                        //Kiem tra co cai nao de truyen ve
                        lsAlertPromotion = userController.GameGetAlertPromotion(serverId);
                        for (int i = 0; i < lsAlertPromotion.size(); i++) {
                            if ((new Date()).after(lsAlertPromotion.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotion.get(i).getEndtime()))) {
                                //Gui ve noi dung cho toan bo Client
                                JSent send = new JSent();
                                send.setEvt("SAON");
                                send.setCmd(lsAlertPromotion.get(i).getDescription());
                                ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayers(filterPidByMinVip(getArrPidByGame(0, lsAlertPromotion.get(i).getOperator(), serverId)), csa); //Lang quat
                            }
                        }
                        sendErrorMsg(playerId, "OK! On" + getArrPidByGame(0, 6000, serverId).length);
                    } else {
                        JSent send = new JSent();
                        send.setEvt("SAOFF");
                        send.setCmd("");
                        ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayers(getArrPidByGame(0, 0, serverId), csa);
                        sendErrorMsg(playerId, "OK! Off" + getArrPidByGame(0, 6000, serverId).length);
                    }

//                    if (je.get("alertOn").getAsInt() == 11) { //Turn on Aert Myanmar
//                        lsAlertPromotion = userController.GameGetAlertPromotion(ServerDefined.MYANMAR_SOURCE);
//                        //Kiem tra co cai nao de truyen ve
//                        for (int i = 0; i < lsAlertPromotion.size(); i++) {
//                            if ((new Date()).after(lsAlertPromotion.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotion.get(i).getEndtime()))) {
//                                //Gui ve noi dung cho toan bo Client
//                                JSent send = new JSent();
//                                send.setEvt("SAON");
//                                send.setCmd(lsAlertPromotion.get(i).getDescription());
//                                ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//                                serviceRouter.dispatchToPlayers(getArrPidByGame(0, lsAlertPromotion.get(i).getOperator(), ServerDefined.MYANMAR_SOURCE), csa); //Lang quat
//                            }
//                        }
//                    }else if (je.get("alertOn").getAsInt() == 12) { //Tat Alert Myanmar
//                        JSent send = new JSent();
//                        send.setEvt("SAOFF");
//                        send.setCmd("");
//                        ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//                        serviceRouter.dispatchToPlayers(getArrPidByGame(0, 0, 1), csa);
//                    }  else if (je.get("alertOn").getAsInt() == 69) { //Bat Alert Siam
//                        lsAlertPromotionThai = userController.GameGetAlertPromotion(ServerDefined.THAI_SOURCE);
////						System.out.println("==>Thai:"+lsAlertPromotionThai.size()) ;
//                        //Kiem tra co cai nao de truyen ve
//                        for (int i = 0; i < lsAlertPromotionThai.size(); i++) {
//                            if ((new Date()).after(lsAlertPromotionThai.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotionThai.get(i).getEndtime()))) {
//                                //Gui ve noi dung cho toan bo Client
//                                JSent send = new JSent();
//                                send.setEvt("SAON");
//                                send.setCmd(lsAlertPromotionThai.get(i).getDescription());
//                                ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//                                serviceRouter.dispatchToPlayers(getArrPidByGame(0, lsAlertPromotionThai.get(i).getOperator(), 4), csa); //52fun
//                            }
//                        }
//                    } else if (je.get("alertOn").getAsInt() == 70) { //Bat Alert Hoki
//                        lsAlertPromotionHoki = userController.GameGetAlertPromotion(ServerDefined.IND_SOURCE);
////						System.out.println("==>Thai:"+lsAlertPromotionThai.size()) ;
//                        //Kiem tra co cai nao de truyen ve
//                        for (int i = 0; i < lsAlertPromotionHoki.size(); i++) {
//                            if ((new Date()).after(lsAlertPromotionHoki.get(i).getStarttime()) && ((new Date()).before(lsAlertPromotionHoki.get(i).getEndtime()))) {
//                                //Gui ve noi dung cho toan bo Client
//                                JSent send = new JSent();
//                                send.setEvt("SAON");
//                                send.setCmd(lsAlertPromotionHoki.get(i).getDescription());
//                                ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//                                serviceRouter.dispatchToPlayers(getArrPidByGame(0, lsAlertPromotionHoki.get(i).getOperator(), 4), csa); //52fun
//                            }
//                        }
//                    } else if (je.get("alertOn").getAsInt() == 2) { //Tat Alert Lang quat
//                        JSent send = new JSent();
//                        send.setEvt("SAOFF");
//                        send.setCmd("");
//                        ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//                        serviceRouter.dispatchToPlayers(getArrPidByGame(0, 0, 1), csa);
//                    } else if (je.get("alertOn").getAsInt() == 11) { //Tat Alert 68blue
//                        JSent send = new JSent();
//                        send.setEvt("SAOFF");
//                        send.setCmd("");
//                        ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//                        serviceRouter.dispatchToPlayers(getArrPidByGame(0, 0, 2), csa);
//                    } else if (je.get("alertOn").getAsInt() == 17) { //Tat Alert Dautruong
//                        JSent send = new JSent();
//                        send.setEvt("SAOFF");
//                        send.setCmd("");
//                        ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
//                        serviceRouter.dispatchToPlayers(getArrPidByGame(0, 0, 3), csa);
//                    }
                } /*else if(je.get("evt").getAsString().equals("alertMarkTable")) { //alertPromotion
                 lsMarkCreateTable = userController.GameGetMarkCreateTable(getConnection()) ;
                 //lsAlertSchedule = userController.GameGetAlertSchedule(getConnection()) ;
                 }*/ else if (je.get("evt").getAsString().equals("alertMatch")) { //alertPromotion
                    lsMatch = userController.GameGetListMatch(1);
                    sendErrorMsg(playerId, "OK!");
                } else if (je.get("evt").getAsString().equals("luckyhoki")) { //alertPromotion
                    userController.GetListLuckyUser(ServerSource.IND_SOURCE);
                    sendErrorMsg(playerId, "Lucky ok!");
                } else if (je.get("evt").getAsString().equals("luckysiam")) { //alertPromotion
                    userController.GetListLuckyUser(ServerSource.MYA_SOURCE);
                    sendErrorMsg(playerId, "Lucky ok!");
                } else if (je.get("evt").getAsString().equals("ping")) {
//    				synchronized (lsUser) {
//    					for(UserInfo ui :lsUser){
//    						if(ui.getPid() == action.getPlayerId()){
//    							ui.getIdle().setTimeIdle((new Date()).getTime());
//    							break;
//    						}
//    					}
//    				}
                } else if (je.get("evt").getAsString().equals("matchThai")) {
                    lsMatchSiam = footballHandler.GameGetListMatch(ServerSource.MYA_SOURCE);
                    sendErrorMsg(playerId, "OK!");
                } else if (je.get("evt").getAsString().equals("alertThai")) { // ALARM ALL PLAYER ON SERVER Thai   
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "salert");
                    jo.addProperty("data", je.get("Data").getAsString());
                    jo.addProperty("name", "admin");
                    ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayers(getArrPidByGame(0, 0, ServerSource.MYA_SOURCE), csa);
                    lsAlert2PlayerThai.add(je.get("Data").getAsString());
                    sendErrorMsg(playerId, "OK!");
                } else if (je.get("evt").getAsString().equals("updateBotThai")) {
                    //userController.GetListBot(ServerDefined.THAI_SOURCE) ;
                    //sendErrorMsg(playerId, "OK!");
                } else if (je.get("evt").getAsString().equals("getuserinfo")) {
                    log_debug.info("==>Get userinfo:");
                    int source = Integer.parseInt(je.get("Source").getAsString());
                    int uid = Integer.parseInt(je.get("Id").getAsString());
                    log_debug.info("==>Get userinfo:" + source + "-" + uid);
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "0");
                    String key_bank = KeyCachedDefine.getKeyCachedBank(userController.GetUserInfoFromCache(source, uid));
                    send.addProperty("bank", (Long) UserController.getCacheInstance().get(key_bank));
                    send.addProperty("data", ActionUtils.gson.toJson(dicUser.get(uid + ServerDefined.userMap.get(source))));
                    send.addProperty("datacache", ActionUtils.gson.toJson(userController.GetUserInfoFromCache(source, uid)));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);

                } else if (je.get("evt").getAsString().equals("getcachedpass")) {
                    int source = Integer.parseInt(je.get("Source").getAsString());
                    String username = je.get("N").getAsString();
//                    System.out.println("==>Get getcachedpass:" + ActionUtils.gson.toJson(je));
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "data");
                    String oldpass = je.get("Pass").getAsString();
                    String keyId = ServerDefined.getKeyCacheId(source) + ActionUtils.ValidString(username) + "___" + String.valueOf(source) + "___" + ActionUtils.ValidString(oldpass);//ActionUtils.ValidString(username) + "_" + ActionUtils.ValidString(oldpass) + "_" + ActionUtils.ValidString(deviceId));
                    send.addProperty("data", username + "-" + keyId + " - " + oldpass);
                    send.addProperty("cached", ActionUtils.gson.toJson(UserController.getCacheInstance().get(keyId)));
//                    System.out.println("==>ChangePass==>AddnewPass:" + username + "-" + keyId + " - " + oldpass);
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("evt").getAsString().equals("cached")) {
                    String key = je.get("key").getAsString();
                    Integer uid = (Integer) UserController.getCacheInstance().get(key);
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "cached");
                    send.addProperty("data", ActionUtils.gson.toJson(uid));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                } else if (je.get("evt").getAsString().equals("getuid")) {
                    int source = Integer.parseInt(je.get("Source").getAsString());
                    String username = je.get("N").getAsString();
                    String pass = je.get("Pass").getAsString();
                    String device = je.get("Device").getAsString();
                    int id = userController.GameGetUseridVietnam(source, username, pass, device);
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "cached");
                    send.addProperty("data", id);
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);

                } else if (je.get("evt").getAsString().equals("jpctrl")) {
                    jackpotHandler.processJacpotCMD(this, je, actionUser.getPid(), actionUser.getSource());
                } else if (je.get("evt").getAsString().equals("jackpotcmd")) {
                    jackpotHandler.processJackpot(this, je, playerId);
                } else if (je.get("evt").getAsString().equals("checkbot")) {
                    botHandler.processBotAction(this, je, playerId);
                } else if (je.get("evt").getAsString().equals("checkPromotion")) {
                    //servicesHandler.processPromotioAction(this,je,playerId);  
                    JsonObject send = new JsonObject();
                    send.addProperty("evt", "cached");
                    send.addProperty("data", ActionUtils.gson.toJson(getClusterProperties(getSeviceRegistry())));
                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                    serviceRouter.dispatchToPlayer(playerId, csa);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println("==>Process_Other: " + playerId + " - " + ActionUtils.gson.toJson(je));
        }
    }

    private void Process_GetListMarkForCreateTable(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                JsonObject send = new JsonObject();
                send.addProperty("evt", "pctable");
                send.addProperty("pid", playerId);
                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
            }
        } catch (Exception e) {
            // handle exception
        }
    }

    //Xu ly cap nhat lai AG
    private void Process_UpdateAGAgain(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                try {
                    int source = 2;
                    int userid = userController.GetUserIDByUsername(source, je.get("uid").getAsString());
                    if (userid > 0) {
                        if (dicUser.containsKey(userid)) {
                            UserAfterPay userTemp = userController.GameGetUserInfoAfterPay(source, playerId - 1000000000);
                            userController.UpdateAGCache(source, playerId - 1000000000, userTemp.getAg() - dicUser.get(userid).getAG().intValue(), userTemp.getVip(), 0l);
                            dicUser.get(userid).setVIP(userTemp.getVip());
                            dicUser.get(userid).setAG(Long.valueOf(userTemp.getAg()));
                            dicUser.get(userid).setLQ(userTemp.getLq());
                            JsonObject act = new JsonObject();
                            act.addProperty("evt", "uag");
                            act.addProperty("ag", userTemp.getAg());
                            act.addProperty("lq", userTemp.getLq());
                            act.addProperty("vip", userTemp.getVip());
                            act.addProperty("dm", userTemp.getDm());
                            ClientServiceAction csa = new ClientServiceAction(userid, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(userid, csa);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Xu ly cap nhat lai AG
    private void Process_UpdateAGfromPayment(String username, int operator) {
        synchronized (dicUser) {
            try {
                if (username == null) {
                    return;
                }
//				if (username.equals("")) return ;
                if (username.length() == 0) {
                    return;
                }
                if (username.equals("null")) {
                    return;
                }
                for (UserInfo usercheck : dicUser.values()) {
                    if ((usercheck.getUsername().equals(username) || usercheck.getUsernameLQ().equals(username))
                            && (usercheck.getOperatorid() == operator)) {
//                        System.out.println("==>SendtoClient:" + usercheck.getPid());
                        UserAfterPay userTemp = null;
                        int source = usercheck.getSource();
                        userTemp = userController.GameGetUserInfoAfterPay(source, usercheck.getUserid() - ServerDefined.userMap.get(source));
                        userController.UpdateAGCache(source, usercheck.getUserid() - ServerDefined.userMap.get(source), userTemp.getAg() - dicUser.get(usercheck.getUserid()).getAG().longValue(), userTemp.getVip(), userTemp.getDm() - dicUser.get(usercheck.getUserid()).getDiamond());
                        dicUser.get(usercheck.getUserid()).setVIP(userTemp.getVip());
                        dicUser.get(usercheck.getUserid()).setAG(Long.valueOf(userTemp.getAg()));
                        dicUser.get(usercheck.getUserid()).setLQ(userTemp.getLq());
                        dicUser.get(usercheck.getUserid()).setDiamond(userTemp.getDm());
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "uag");
                        act.addProperty("ag", userTemp.getAg());
                        act.addProperty("lq", userTemp.getLq());
                        act.addProperty("vip", userTemp.getVip());
                        act.addProperty("dm", userTemp.getDm());
//                        System.out.println("==>Process_UpdateAGfromPayment==>SentoClient:" + ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        ClientServiceAction csa = new ClientServiceAction(usercheck.getUserid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(usercheck.getUserid(), csa);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // handle exception
//                System.out.println("==>Error==>Process_UpdateAGfromPayment:" + e.getMessage());
            }
        }
    }

    //Xu ly Payment INA
    private void Process_PaymentIAP(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                try {
                    if (actionUser.getSource() == ServerSource.INDIA_SOURCE) {
                        iapCmd.processIAP_ANDROID(je, actionUser, serviceRouter);
                    } else {
//                        System.out.println("==>Process_PaymentIAP:" + (new Date()).toString() + "-" + je.get("signedData").getAsString() + "-" + je.get("signature").getAsString());
                        String pakname = "";
                        PaymentIAP obj = ActionUtils.gson.fromJson(je.get("signedData").getAsString(), PaymentIAP.class);
                        try {
                            pakname = je.get("pkgname").getAsString();
                        } catch (Exception e) {
                            // handle exception
                            pakname = "";
                            pakname = obj.getPackageName();
                        }
                        boolean iapresult = Security.verifyPurchase(je.get("signedData").getAsString(), je.get("signature").getAsString(), pakname);
                        int gold = 0;
//        				String strGold = "" ;
//        				String strName = "" ;
//                        System.out.println("==>iapresult:" + iapresult);
                        if (iapresult) {
//                            System.out.println("==>Add Gold:" + obj.getProductId() + "-" + obj.getDeveloperPayload());
                            int source = actionUser.getSource();
                            gold = userController.UpdateAGIAP(source, actionUser.getUsername(), obj.getProductId(), obj.getOrderId(), je.get("signedData").getAsString(), je.get("signature").getAsString(), pakname);
                            if (source != 5) {
                                userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), gold, actionUser.getVIP(), 0l);
                            }
//                            System.out.println("==>Process_PaymentIAP: username " + actionUser.getUsername() + "- gold: " + gold);
                            dicUser.get(playerId).IncrementMark(gold);

                            if (dicUser.get(playerId).getTableId() > 0) {
                                GameDataAction gda = new GameDataAction(playerId, dicUser.get(playerId).getTableId());
                                JsonObject jo = new JsonObject();
                                jo.addProperty("evt", "ag_iap");
                                jo.addProperty("ag", gold);
                                gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jo).getBytes("UTF-8")));
                                serviceRouter.dispatchToGame(dicUser.get(playerId).getGameid(), gda);
                            }
                        }
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "iapResult");
                        if (gold > 0) {
                            if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                                act.addProperty("msg", actionUtils.getConfigText("strPayment_Success_Pre", actionUser.getSource(), actionUser.getUserid()) + gold + actionUtils.getConfigText("strPayment_Success_Sur", actionUser.getSource()));
                            } else {
                                act.addProperty("msg", actionUtils.getConfigText("strPayment_Success_Pre", actionUser.getSource(), actionUser.getUserid()) + gold + actionUtils.getConfigText("strPayment_Success_Sur", actionUser.getSource()));
                            }
                        } else {
                            if (actionUser.getSource() == ServerSource.THAI_SOURCE || actionUser.getSource() == 10) {
                                act.addProperty("msg", actionUtils.getConfigText("strPayment_Err1", actionUser.getSource(), actionUser.getUserid()));
                            } else {
                                act.addProperty("msg", actionUtils.getConfigText("strPayment_Err1", actionUser.getSource(), actionUser.getUserid()));
                            }
                        }
                        act.addProperty("verified", Boolean.toString(iapresult));
                        act.addProperty("goldPlus", gold);
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                } catch (Exception e) {
                    // handle exception
//                    System.out.println("==>Error==>Process_PaymentIAP:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
    }

    //Xu ly Payment INA IOS
    private void Process_PaymentIAP_IOS(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                try {
                    if (actionUser.getSource() == ServerSource.INDIA_SOURCE) {
                        iapCmd.processIAP_IOS(je, actionUser, serviceRouter);
                    } else {
                        Logger.getLogger("IAP_IOS").info("==>Process_PaymentIAP_IOS:" + (new Date()).toString() + "-" + je.get("receipt_encoded64").getAsString());
                        String receipt = je.get("receipt_encoded64").getAsString();
                        //String checkData  = je.get("data").getAsString();
                        int gold = 0;
                        //System.out.println("==>OK Bat dau test1: "+ receipt);
                        String output = VerifyIAPApple.verify(receipt, false);
                        Logger.getLogger("IAP_IOS").info("==>Process_PaymentIAP_IOS1: " + ActionUtils.gson.toJson(actionUser));
                        Logger.getLogger("IAP_IOS").info("==>Process_PaymentIAP_IOS1: - Output: " + output);
                        //if (!output.equals("")) {
                        if (output.length() != 0) {
                            try {
                                JsonObject jsObj = (JsonObject) parser.parse(output);
                                System.out.println("==>21:");
                                System.out.println("==>Output:" + output + ActionUtils.gson.toJson(jsObj));

                                if (jsObj.has("environment")) {
                                    //JsonObject objTemp = (output, IAP_IOS_NEW.class);
                                    //System.out.println("==>Receipt: - status: " + objTemp.getStatus());
                                    if (jsObj.get("status").getAsInt() == 0) {
                                        // purchaseok
                                        // anh lay thong tin nap tien o day
                                        JsonObject obj = jsObj.get("receipt").getAsJsonObject();
                                        //System.out.println("==>Value:" + obj.getBundle_id() + "-" + obj.getReceipt_type());
                                        int source = actionUser.getSource();
                                        if (obj.get("in_app").getAsJsonArray().size() > 0) {
                                            IAP_IOS_ITEM_IN_APP item = ActionUtils.gson.fromJson(obj.get("in_app").getAsJsonArray().get(0).toString(), IAP_IOS_ITEM_IN_APP.class);
                                            gold = userController.AddMoneyIOS(source, actionUser.getUserid() - ServerDefined.userMap.get(source), item, 0);
                                        }

                                    } else if (jsObj.get("status").getAsInt() == 21007) { // sandbox
                                        output = VerifyIAPApple.verify(receipt);
                                        if (output.length() != 0) {
                                            jsObj = (JsonObject) parser.parse(output);
                                            if (jsObj.get("status").getAsInt() == 0) {
                                                JsonObject obj = jsObj.get("receipt").getAsJsonObject();
                                                //System.out.println("==>Value:" + obj.getBundle_id() + "-" + obj.getReceipt_type());
                                                int source = actionUser.getSource();
                                                if (obj.get("in_app").getAsJsonArray().size() > 0) {
                                                    IAP_IOS_ITEM_IN_APP item = ActionUtils.gson.fromJson(obj.get("in_app").getAsJsonArray().get(0).toString(), IAP_IOS_ITEM_IN_APP.class);
                                                    gold = userController.AddMoneyIOS(source, actionUser.getUserid() - ServerDefined.userMap.get(source), item, 1);
                                                }
                                            }
                                        }
                                    }
                                } else if (jsObj.has("status") && jsObj.get("status").getAsInt() == 21007) { // sandbox
                                    output = VerifyIAPApple.verify(receipt);
                                    if (output.length() != 0) {
                                        jsObj = (JsonObject) parser.parse(output);
                                        if (jsObj.get("status").getAsInt() == 0) {
                                            JsonObject obj = jsObj.get("receipt").getAsJsonObject();
                                            //System.out.println("==>Value:" + obj.getBundle_id() + "-" + obj.getReceipt_type());
                                            int source = actionUser.getSource();
                                            if (obj.get("in_app").getAsJsonArray().size() > 0) {
                                                IAP_IOS_ITEM_IN_APP item = ActionUtils.gson.fromJson(obj.get("in_app").getAsJsonArray().get(0).toString(), IAP_IOS_ITEM_IN_APP.class);
                                                gold = userController.AddMoneyIOS(source, actionUser.getUserid() - ServerDefined.userMap.get(source), item, 1);
                                            }
                                        }
                                    }
                                } else {
                                    PaymentIAP_IOSTemp objTemp = ActionUtils.gson.fromJson(output, PaymentIAP_IOSTemp.class);
                                    System.out.println("==>Receipt:" + objTemp.getReceipt() + "-" + objTemp.getStatus());
                                    if (objTemp.getStatus() == 0) {
                                        // purchaseok
                                        // anh lay thong tin nap tien o day
                                        PaymentIAP_IOS obj = objTemp.getReceipt();
                                        System.out.println("==>Value:" + obj.getItem_id() + "-" + obj.getProduct_id());
                                        int source = actionUser.getSource();
                                        gold = userController.AddMoneyIOS(source, actionUser.getUserid() - ServerDefined.userMap.get(source), obj, 0);
                                    } else if (objTemp.getStatus() == 21007) {
                                        output = VerifyIAPApple.verify(receipt);
//        		    					if (!output.equals("")) {
                                        if (output.length() != 0) {
                                            objTemp = ActionUtils.gson.fromJson(output, PaymentIAP_IOSTemp.class);
                                            if (objTemp.getStatus() == 0) {
                                                PaymentIAP_IOS obj = objTemp.getReceipt();
                                                System.out.println("==>SanboxValue:" + obj.getItem_id() + "-" + obj.getProduct_id());
                                                int source = actionUser.getSource();
                                                gold = userController.AddMoneyIOS(source, actionUser.getUserid() - ServerDefined.userMap.get(source), obj, 1);
                                            }
                                        }
                                    }
                                }


                                /* else if (errorcode == 21000) {
                                  // The App Store could not read the JSON object you provided.
                                  } else if (errorcode == 21002) {
                                  // The data in the receipt-data property was malformed or missing.
                                  } else if (errorcode == 21003) {
                                  // The receipt could not be authenticated.
                                  } else if (errorcode == 21004) {
                                  // The shared secret you provided does not match the shared secret on file for your account.
                                  // Only returned for iOS 6 style transaction receipts for auto-renewable subscriptions.
                                  } else if (errorcode == 21005) {
                                  // The receipt server is not currently available.
                                  } else if (errorcode == 21006) {
                                  // This receipt is valid but the subscription has expired. When this status code is returned to your
                                  // server, the receipt data is also decoded and returned as part of the response.
                                  // Only returned for iOS 6 style transaction receipts for auto-renewable subscriptions.
                                  } else if (errorcode == 21007) {
                                  // This receipt is from the test environment, but it was sent to the production environment for
                                  // verification. Send it to the test environment instead.This receipt is from the test environment,
                                  // but it was sent to the production environment for verification. Send it to the test environment
                                  // instead.
                                  } else if (errorcode == 21008) {
                                  // This receipt is from the production environment, but it was sent to the test environment for
                                  // verification. Send it to the production environment instead.
                                  }*/
                            } catch (Exception e) {
                                System.out.println("==>==>Process_PaymentIAP_IOS:" + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("something wrong!");
                        }
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "iap_ios");
                        if (gold > 0) {
                            //if (actionUser.getSource() == 9) {
                            //   act.addProperty("msg", actionUtils.strPayment_Success_Pre_TH + gold + actionUtils.strPayment_Success_Sur_TH);
                            //} else {
                            act.addProperty("msg", actionUtils.getConfigText("strPayment_Success_Pre", actionUser.getSource(), actionUser.getUserid()) + gold + actionUtils.getConfigText("strPayment_Success_Sur", actionUser.getSource()));
                            //}
                            act.addProperty("status", 0);
                            dicUser.get(playerId).IncrementMark(gold);
                            int source = actionUser.getSource();
                            if (source != 1) {
                                userController.UpdateAGCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), gold, actionUser.getVIP(), 0l);
                            }
                        } else if (gold == -2) {
                            //if (actionUser.getSource() == 9) {
                            //    act.addProperty("msg", actionUtils.strPayment_Err2_TH);
                            //} else {
                            act.addProperty("msg", actionUtils.getConfigText("strPayment_Err2", actionUser.getSource(), actionUser.getUserid()));
                            //}
                            act.addProperty("status", 1);
                        } else if (gold == -3) {
                            //if (actionUser.getSource() == 9) {
                            //    act.addProperty("msg", actionUtils.strPayment_Err1_TH);
                            //} else {
                            act.addProperty("msg", actionUtils.getConfigText("strPayment_Err1", actionUser.getSource(), actionUser.getUserid()));
                            //}
                            act.addProperty("status", 1);
                        } else {
                            //if (actionUser.getSource() == 9) {
                            //    act.addProperty("msg", actionUtils.strPayment_Err1_TH);
                            //} else {
                            act.addProperty("msg", actionUtils.getConfigText("strPayment_Err1", actionUser.getSource(), actionUser.getUserid()));
                            // }
                            act.addProperty("status", 1);
                        }
                        act.addProperty("vip", 1);
                        act.addProperty("gold", dicUser.get(playerId).getAG().intValue());
                        String mess = actionUtils.getConfigText("mess_gold_1", actionUser.getSource(), actionUser.getUserid());
                        mess = String.format(mess, dicUser.get(playerId).getAG().intValue());

                        sendErrorMsg(playerId, mess);
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
                } catch (Exception e) {
                    // handle exception
//                    System.out.println("==>Error==>Process_PaymentIAP_IOS:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Process_DisplayRule(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            loggerLogin_.info("==>Start Display Rule:" + actionUser.getUsername());
            String versionid = je.get("VersionId").getAsString();
            String packageId = je.get("PackageId").getAsString();
            String os = je.get("Os").getAsString();
            String publisherid = je.get("PublisherId").getAsString();
            int operatorid = je.get("OperatorId").getAsInt();
            loggerLogin_.info("==>Receive Rule:" + versionid + "-" + packageId + "-" + os + "-" + publisherid + "-" + operatorid);
            DisplayRuleTran objR = new DisplayRuleTran();
            for (int i = 0; i < listDisplayRule.size(); i++) {
//        		loggerLogin_.info("Rule:" + listDisplayRule.get(i).getVersion() + "-" + listDisplayRule.get(i).getPackageid());
                if (listDisplayRule.get(i).getVersion().equals(versionid) && listDisplayRule.get(i).getPackageid().equals(packageId)
                        && (listDisplayRule.get(i).getOperatorid() == operatorid) //&& listDisplayRule.get(i).getPublisherid().equals(publisherid)
                        && listDisplayRule.get(i).getOsid().equals(os)) {
                    objR.setSms(listDisplayRule.get(i).getSms());
                    objR.setCard(listDisplayRule.get(i).getCard());
                    objR.setIap(listDisplayRule.get(i).getIap());
                    objR.setAtm(listDisplayRule.get(i).getAtm());
                    objR.setPayurl(listDisplayRule.get(i).getPayurl());
                    objR.setPaytypesms(listDisplayRule.get(i).getPaytypesms());
                    objR.setPaytypecard(listDisplayRule.get(i).getPaytypecard());
                    objR.setPayprefix(listDisplayRule.get(i).getPayprefix());
                    objR.setPayurlcard(listDisplayRule.get(i).getPayurlcard());
                    objR.setPayurlsms(listDisplayRule.get(i).getPayurlsms());
                    objR.setPayurldisplay(listDisplayRule.get(i).getPayurldisplay());
                    objR.setBank(listDisplayRule.get(i).getBank());
                    if (listDisplayRule.get(i).getListgame().length() > 0) {
                        String[] arrGame = listDisplayRule.get(i).getListgame().split(";");
                        int[] arrR = new int[arrGame.length];
                        for (int j = 0; j < arrGame.length; j++) {
                            arrR[j] = Integer.parseInt(arrGame[j]);
                        }
                        objR.setListgame(arrR);
                    }
                }
            }

        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
    }

    private void Process_InviteFacebook(JsonObject je, UserInfo actionUser, int playerId) {
        synchronized (dicUser) {
            try {
//				System.out.println("==>Process_InviteFacebook:" + actionUser.getFacebookid());
                if (actionUser.getFacebookid() == 0) {
                    return;
                }
                String strInvite = je.get("data").getAsString();
                int source = actionUser.getSource();
                if (source != 1) {
                    userController.CreateFriendFacebookRequest(source, Long.valueOf(strInvite).longValue(), actionUser.getUserid() - ServerDefined.userMap.get(source), actionUser.getFacebookName());
                }
                //sendErrorMsg(playerId, "Bạn đã gửi lời mời thành công, bạn sẽ được tặng Gold khi các bạn của bạn vào chơi! Gold sẽ được trả trong mục ngân hàng.");
            } catch (Exception e) {
                e.printStackTrace();
                // handle exception
//                System.out.println("==>Error==>Process_InviteFacebook:" + e.getMessage());
            }
        }
    }

    private void Process_ChangeUsername_Facebook(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
//				System.out.println("==>Doi ten:" + je.get("U").getAsString()) ;
                if (je.get("evt").getAsString().equals("GIBD")) { //Get Info By Device
//					String did = je.get("did").getAsString().trim() ;
//					if (did.length() > 0) {
                    long gold = 0;
                    int source = actionUser.getSource();
                    if (source != 1) {
                        gold = userController.GameGetGoldByDeviceId(source, actionUser.getDeviceId(), actionUser.getUserid() - ServerDefined.userMap.get(source));
                    }
//                		System.out.println("==>GIBD Gold:" + gold) ;
                    if (gold > 0) {
                        userController.UpdateAGCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), (int) gold / 10, actionUser.getVIP(), 0l);
                        dicUser.get(playerId).IncrementMark((int) gold / 10);
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "GIBD");
                        act.addProperty("Vip", gold % 10);
                        act.addProperty("AG", dicUser.get(playerId).getAG().intValue());
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    } else {
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", "GIBD");
                        act.addProperty("Vip", actionUser.getVIP());
                        act.addProperty("AG", dicUser.get(playerId).getAG().intValue());
                        ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(playerId, csa);
                    }
//					}
                } else if (je.get("evt").getAsString().equals("RUFN")) { //Register Username Facebook Theo kieu moi
//					System.out.println("==>USerName Thai:" + je.get("U").getAsString().trim()) ;
                    if (CheckValidUsernameLQ(je.get("U").getAsString().trim()) != 0) {
                        //if (actionUser.getSource() == 9) {
                        //   sendErrorMsg(playerId, actionUtils.strName_Err1_TH);
                        //} else {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err1", actionUser.getSource(), actionUser.getUserid()));
                        //}
                    } else {
                        if (CheckValidPassLQ(je.get("P").getAsString().trim()) != 0) {
                            //if (actionUser.getSource() == 9) {
                            //    sendErrorMsg(playerId, actionUtils.strName_Err2_TH);
                            //} else {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err2", actionUser.getSource(), actionUser.getUserid()));
                            //}
                        } else {
                            if (actionUser.getUsernameLQ().length() != 0 && !actionUser.getUsernameLQ().equals(je.get("U").getAsString().trim())) { //Da co UsernameFacebook va ten khac ten cu ==> Khong Cap nhat gi ca 
                                //if (actionUser.getSource() == 9) {
                                //    sendErrorMsg(playerId, actionUtils.strName_Err3_TH);
                                //} else {
                                sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err3", actionUser.getSource(), actionUser.getUserid()));
                                //}
                            } else {
//                    			String usernamenew = actionUser.getUsername() ;
                                int id = 0;
                                int source = actionUser.getSource();
                                id = userController.GameIUserinfoFacebook(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("U").getAsString().trim(), je.get("P").getAsString().trim(), actionUser.getDeviceId());
                                //System.out.println("==>UserR:" + je.get("U").getAsString() + "-" + id);                        	
                                if (id > 0) { //Successful
                                    userController.UpdateUsernameSiamToCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("U").getAsString());
                                    dicUser.get(playerId).setUsername(je.get("U").getAsString().trim());
                                    dicUser.get(playerId).setUsernameLQ(je.get("U").getAsString().trim());
                                    JsonObject act = new JsonObject();
                                    act.addProperty("evt", "RUFN");
                                    act.addProperty("U", je.get("U").getAsString().trim());
                                    ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                                    serviceRouter.dispatchToPlayer(playerId, csa);
                                } else {
                                    //if (actionUser.getSource() == 9) {
                                    //    sendErrorMsg(playerId, actionUtils.strName_Err4_TH);
                                    //} else {
                                    sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err4", actionUser.getSource(), actionUser.getUserid()));
                                    //}
                                }
                            }
                        }
                    }
                } else if (je.get("evt").getAsString().equals("RUF")) { //Register Username Facebook
                    String strU = je.get("U").getAsString().toLowerCase();
                    strU = strU.replace(" ", "_");
                    if (actionUser.getSource() == ServerSource.THAI_SOURCE) {
                        if (CheckValidUsernameThai(strU) != 0) {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err1", actionUser.getSource(), actionUser.getUserid()));
                            return;
                        }
                    }
                    if (actionUser.getSource() != ServerSource.THAI_SOURCE) {
                        if (CheckValidUsernameLQ(strU) != 0) {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err1", actionUser.getSource(), actionUser.getUserid()));
                            return;
                        }
                    }
                    if (actionUser.getUsernameLQ().length() != 0) { //Da co UsernameFacebook ==> Khong duoc DK 
                        //if (actionUser.getSource() == ServerDefined.THAI_SOURCE) {
                        //    sendErrorMsg(playerId, actionUtils.strName_Err3_TH);
                        //} else {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err3", actionUser.getSource(), actionUser.getUserid()));
                        //}
                    } else if (actionUser.getUsername().indexOf("fb.") == 0 || actionUser.getUsername().indexOf("z.") == 0 || actionUser.getUsername().indexOf("te.") == 0) {
                        String usernamenew = actionUser.getUsername();
                        int id = 0;
                        int source = actionUser.getSource();
                        id = userController.GameIUserinfo(source, actionUser.getUserid() - ServerDefined.userMap.get(source), usernamenew, strU, actionUser.getOperatorid());
                        //System.out.println("==>UserR:" + je.get("U").getAsString() + "-" + id);                        	
                        if (id > 0) { //Successful
                            userController.UpdateUsernameSiamToCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), strU);
                            dicUser.get(playerId).setUsername(strU);
                            dicUser.get(playerId).setUsernameLQ(strU);
                            String keyMap_ID_Name = ServerDefined.getKeyCacheMapIdName(source) + ActionUtils.MD5(ActionUtils.ValidString(strU));
                            Logger.getLogger("FriendsHandler").info("==>GetUserInfoByUserid==>mapidRUF: keyMap_ID_Name: " + keyMap_ID_Name
                                    + strU + " - uid" + playerId);
                            UserController.getCacheInstance().set(keyMap_ID_Name, playerId, 0);

                            JsonObject act = new JsonObject();
                            act.addProperty("evt", "RUF");
                            act.addProperty("U", strU);
                            ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(playerId, csa);
                            if (lsDefaultGame.size() > 0) {
                                if (lsDefaultGame.get(0).equals(strU)) {
                                    lsDefaultGame.remove(0);
                                }
                            }
                            if ((actionUser.getVIP() >= 2) && (source == ServerSource.THAI_SOURCE || source == 10)) {
                                int ag = 3000;
                                //Tang tien user
                                Logger.getLogger("KHUYENMAILOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid() + "#0#10#" + String.valueOf(ag) + "#" + String.valueOf((new Date()).getTime()));
                                userController.GameUUserVIP(source, playerId - ServerDefined.userMap.get(source), 2, ag, 0);
                            }
                            /*if ((source == ServerDefined.THAI_SOURCE) && (actionUser.getNewGamer() == 1)) {
                                JsonObject act1 = new JsonObject();
                                act1.addProperty("evt", "newgamer");
                                act1.addProperty("M", actionUser.getAGNewGamer());
                                ClientServiceAction csa1 = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act1).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csa1);
                                JsonObject sendUS = new JsonObject(); //User Setting
                                sendUS.addProperty("evt", "US");
                                sendUS.addProperty("data", ActionUtils.gson.toJson(userController.GetUserSettingByUserid(source, playerId - ServerDefined.userMap.get(source))));
                                ClientServiceAction csaUS = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(sendUS).getBytes("UTF-8"));
                                serviceRouter.dispatchToPlayer(playerId, csaUS);
                            }*/
                        } else {
                            // if (actionUser.getSource() == 9) {
                            //    sendErrorMsg(playerId, actionUtils.strName_Err4_TH);
                            // } else {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err4", actionUser.getSource(), actionUser.getUserid()));
                            //}
                            if (lsDefaultGame.size() > 0) {
                                if (lsDefaultGame.get(0).equals(strU)) {
                                    lsDefaultGame.remove(0);
                                }
                            }
                        }
                    } else {
                        sendErrorMsg(playerId, "You do not rename!");
                        return;
                    }
                } else if (je.get("evt").getAsString().equals("CUF")) { //Check UsernameLQ
                    String strU = je.get("U").getAsString();
                    strU = strU.replace(" ", "_");
                    if (actionUser.getSource() == ServerSource.THAI_SOURCE) {
                        if (CheckValidUsernameThai(strU) != 0) {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err1", actionUser.getSource(), actionUser.getUserid()));
                            return;
                        }
                    }
                    if (actionUser.getSource() != ServerSource.THAI_SOURCE) {
                        if (CheckValidUsernameLQ(strU) != 0) {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err1", actionUser.getSource(), actionUser.getUserid()));
                            return;
                        }
                    }
                    if (actionUser.getUsernameLQ().length() == 0) {
                        int id = 0;
                        int source = actionUser.getSource();
                        if (source != 1) {
                            id = userController.GameCheckUserLQ(source, strU);
                        }
                        if (id > 0) { //Da co username do trong bang
//                            if (actionUser.getSource() == ServerDefined.THAI_SOURCE) {
//                                sendErrorMsg(playerId, actionUtils.strName_Err4_TH);
//                            } else {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err4", actionUser.getSource(), actionUser.getUserid()));
                            //}
                        } else {
//                            if (actionUser.getSource() == ServerDefined.THAI_SOURCE) {
//                                sendErrorMsg(playerId, actionUtils.strName_Success_TH);
//                            } else {
                            sendErrorMsg(playerId, actionUtils.getConfigText("strName_Success", actionUser.getSource(), actionUser.getUserid()));
                            // }
                        }
                    } else {
//                        if (actionUser.getSource() == ServerDefined.THAI_SOURCE) {
//                            sendErrorMsg(playerId, actionUtils.strName_Err6_TH);
//                        } else {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err6", actionUser.getSource(), actionUser.getUserid()));
                        //}
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
//            System.out.println("==>Error==>Process_ChangeUsername_Facebook:" + e.getMessage());
        }
    }

    private void Process_RegisterUsername(JsonObject je, UserInfo actionUser, int playerId) {
        try {
            synchronized (dicUser) {
                String strUsername = je.get("U").getAsString();
                String strPassword = je.get("P").getAsString();
                int error = CheckValidUsernameLQ(strUsername);
                if (error == 1) {
//                    if (actionUser.getSource() == ServerDefined.THAI_SOURCE) {
//                        sendErrorMsg(playerId, actionUtils.strName_Err7_TH);
//                    } else {
                    sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err7", actionUser.getSource(), actionUser.getUserid()));
                    //}
                } else if (error == 2) {
//                    if (actionUser.getSource() == ServerDefined.THAI_SOURCE) {
//                        sendErrorMsg(playerId, actionUtils.strName_Err8_TH);
//                    } else {
                    sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err8", actionUser.getSource(), actionUser.getUserid()));
                    //}
                } else if (error > 0) {
//                    if (actionUser.getSource() == ServerDefined.THAI_SOURCE) {
//                        sendErrorMsg(playerId, actionUtils.strName_Err1_TH);
//                    } else {
                    sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err1", actionUser.getSource(), actionUser.getUserid()));
                    //}
                } else {
                    int idUser = 0;
                    int source = actionUser.getSource();
                    if (source != 1) {
                        idUser = userController.GameRegisterUsername(source, strUsername, strPassword, actionUser.getUserid() - ServerDefined.userMap.get(source));
                    }
                    if (idUser < 0) {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strName_Err4", actionUser.getSource(), actionUser.getUserid()));
                    } else if (idUser == 2) {
                        dicUser.get(playerId).setUsername(strUsername);
                        sendErrorMsg(playerId, actionUtils.getConfigText("strName_Success1", actionUser.getSource(), actionUser.getUserid()));
                    } else if (idUser == 3) {
                        sendErrorMsg(playerId, actionUtils.getConfigText("strName_Success1", actionUser.getSource(), actionUser.getUserid()));
                    }
                }
            }
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
    }

    /*public void Process_Promotion(JsonObject je, UserInfo actionUser, int playerId, ServiceRouter serviceRouter) {
        try {
            int source = actionUser.getSource();
            if (je.get("evt").getAsString().equals("receivebonus")) { //Nhan gold len vip
            	int gold = promotionHandler.RemovePromotionInCache(source, playerId- ServerDefined.userMap.get(source), je.get("T").getAsInt(), je.get("G").getAsInt()) ;
            	dicUser.get(playerId).IncrementMark(gold);
                //userController.GameUUserVIP(source, playerId - ServerDefined.userMap.get(source), actionUser.getVIP(), gold, 0);
                if (source != 1) {
                    userController.UpdateAGCache(source, playerId - ServerDefined.userMap.get(source), gold, actionUser.getVIP());
                }
                if (source == ServerDefined.THAI_SOURCE) {
                    Logger.getLogger("KHUYENMAILOG").info(String.valueOf(dicUser.get(playerId).getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(playerId).getAG().intValue() + "#" + dicUser.get(playerId).getGameid() + "#0#11#" + String.valueOf(gold) + "#" + String.valueOf((new Date()).getTime()));
                }
                JsonObject act = new JsonObject();
                act.addProperty("evt", "receiveuvip");
                act.addProperty("AG", gold);
                ClientServiceAction csa = new ClientServiceAction(playerId, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(playerId, csa);
                
            }
        } catch (Exception e) {
            // handle exception
        }
    }*/
    //Xuly Notify Schedule
    /*private void Process_NotifySchedule(JsonObject je, UserInfo actionUser, int playerId) {
     try {
     synchronized (dicUser) {
     sendToClient(playerId, "notischedule", ActionUtils.gson.toJson(lsAlertSchedule)) ;
     }
     } catch (Exception e) {
     // handle exception
     System.out.println("==>Error==>Process_NotifySchedule:" + e.getMessage()) ;
     }
     }*/
 /*
     * evt receive:
     * evt = 'selectG' => Chon Game, Dang nhap he thong
     * evt = 'selectR' ==> Click Room
     * evt = 'selectT' ==> Tim ban choi nhanh
     * evt = 'ivp' ==> Moi ban be
     * 		T = 0 ==> Lay danh sach moi
     * 		T = 1 ==> Gui thong tin moi
     * evt = "7" ==> Gui thong tin lay Promotion
     * evt = "10" ==> Chuyen AG
     * evt = "12" ==> Khoa cap 2
     * 		T = 1 ==> Dat mat khau
     * 		T = 2 ==> Huy mat khau sau 7 ngay
     * 		T = 3 ==> Huy mat khau
     * 		T = 4 ==> Doi mat khau
     * 		T = 5 ==> Mo mat khau
     * evt = "14" ==> Ngan hang
     * 		T = 1 ==> Vay
     * 		T = 2 ==> Tra
     * evt = "15" ==> Message Thu
     * 		T = 1 ==> Doc thu
     * 		T = 2 ==> Nhan thu
     * 		T = 3 ==> Xoa thu
     * 		T = 4 ==> Lay thu he thong
     * 		T = 5 ==> Lay thu nguoi choi
     * 		T = 6 ==> Lay thu chuyen AG
     * 		T = 7 ==> Gui thu
     * 		T = 10 ==> Lay mail he thong (kieu moi)
     * 		T = 11 ==> Lay mail nguoi choi (kieu moi)
     * 		T = 12 ==> Lay cac thu co AG.
     * evt = "16" ==> Chat
     * 		T = 1 ==> The gioi
     * 		T = 2 ==> Game
     * 		T = 3 ==> Bang
     * 		T = 4 ==> Rieng
     * evt = "cv" ==> Doi LQ sang AG
     * evt = "usermobile" ==> Cap nhat thong tin Mobile
     * evt = "pf" ==> Cap nhat Profile.
     * evt = "agtv" ==> Doi AG thanh diem Vip
     * evt = "rl" || "frl" ==> Roulette
     * evt = "fb" ==> Bong da
     * evt = "dg" ==> Dau gia
     * evt = "xs" ==> Xo so
     * evt = "tlucky" || "slucky" || "flucky" ==> Bai may man
     * evt = "dp" || "dpl" ==> Daily Promotion
     * evt = "rename" ==> Doi ten
     * evt = "changepass" ==> Doi mat khau
     * evt = "changea" ==> Doi Avatar
     * evt = "NRULQ_N" || "NRULQ" || "CULQ" ==> Doi ten nhan vat tren lang quat
     * evt = "uag" ==> Lay lai du lieu trong DB
     * evt = "uvip" ==> Up len Vip
     * evt = "pctable" ==> Lay danh sach muc cuoc ban
     */
    @Override
    public void onAction(ServiceAction action) {
        try {

            //for (int i = 0; i < action.getData().length; i++) {
            //	System.out.print(action.getData()[i]) ;
            //}
            String message = new String(action.getData(), "UTF-8");
//            Logger.getLogger("ServiceAction").info(message + " - " + action.getPlayerId());
            JsonObject je = (JsonObject) parser.parse(message);
//            if (action.getPlayerId() == 1000000000)
            //System.out.println("==>User_onAction: pid: "+action.getPlayerId()+ " - " + message);
            //System.out.println("==>getClusterProperties: " +ActionUtils.gson.toJson(getClusterProperties(getSeviceRegistry())));
            boolean checkNotIDEVT = true;
            synchronized (dicUser) {
                if (dicUser.containsKey(action.getPlayerId())) {
                    dicUser.get(action.getPlayerId()).setLastDisconnect(new Date());
                    UserInfo actionUser = dicUser.get(action.getPlayerId());

                    if (je.has("idevt")) {
                        checkNotIDEVT = false;
                        actionHandler.process(je, actionUser, serviceRouter, context);
                    } else if (je.get("evt").getAsString().equals("highlowclose")) {
                        processTaiXiu(action);
                    } else if (je.get("evt").getAsString().equals("bethighlow1")) {
                        processTaiXiu(action);
                    } else if (je.get("evt").getAsString().equals("highlowinfo1")) {
                        processTaiXiu(action);
                    } else if (je.get("evt").getAsString().equals("highlowhistory1")) {
                        processTaiXiu(action);
                    }/* else if (je.get("evt").getAsString().equals("selectG")) {
                        Process_SelectGame(je, actionUser, action.getPlayerId());
                    }*/ else if (je.get("evt").getAsString().equals("selectG2")) {
                        processSelectG2(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("selectR")) { //Select Room New
//                    	System.out.println("======>NhanSelectR:" + (new Date()).toString() + "-" + message + "-" + actionUser.getUsername() + "-" + actionUser.getAG().intValue());
                        Process_SelectRoom(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("selectRSiam")) { //Select Room New ==>
                        Process_SelectRoom_Siam(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("selectROnly")) { //Select Room New ==>
                        Process_SelectRoomOnly(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("searchTOV")) {  //Tim ban nhanh New    
                        Process_SearchTableOV(je, actionUser, action.getPlayerId(), je.get("M").getAsInt());
                    } else if (je.get("evt").getAsString().equals("playnow")) {  //Tim ban nhanh New ==>
                        DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
                        if (dos.allow("playnow", actionUser.getUserid())) {
                            Process_Playnow(je, actionUser, action.getPlayerId(), "playnow");
                        }
                    } else if (je.get("evt").getAsString().equals("playnow_create")) {  //Tim ban nhanh New ==>
                        Process_Playnow(je, actionUser, action.getPlayerId(), "playnow_create");
                    } else if (je.get("evt").getAsString().equals("searchT")) {  //Tim ban nhanh New ==>
                        DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
                        if (dos.allow("playnow", actionUser.getUserid())) {
                            int newVer = 0;
                            int D = 0;
                            //int T = 0;
                            try {
                                if (je.has("NewVer")) {
                                    newVer = je.get("NewVer").getAsInt();
                                }
                                if (je.has("D")) {
                                    D = je.get("D").getAsInt();
                                }
                            } catch (Exception e) {
                                // handle exception
                                e.printStackTrace();
                            }
                            Process_SearchTable(je, actionUser, action.getPlayerId(), "searchT", newVer, D);
                        }
                    } else if (je.get("evt").getAsString().equals("searchTRe")) {  //Tim ban nhanh New ==> 
                        int newVer = 0;
                        int D = 0;
                        try {
                            if (je.has("NewVer")) {
                                newVer = je.get("NewVer").getAsInt();
                            }
                            if (je.has("D")) {
                                D = je.get("D").getAsInt();
                            }
                        } catch (Exception e) {
                            // handle exception
                            e.printStackTrace();
                        }
                        Process_SearchTable(je, actionUser, action.getPlayerId(), "searchTRe", newVer, D);
                    } else if (je.get("evt").getAsString().equals("searchTSiamRe")) {  //Tim ban nhanh New    
                        int newVer = 0;
                        try {
                            newVer = je.get("NewVer").getAsInt();
                        } catch (Exception e) {
                            // handle exception
                        }
                        Process_SearchTableSiamRe(je, actionUser, action.getPlayerId(), newVer);
                    } else if (je.get("evt").getAsString().equals("searchTPokerTexas")) {

                        Process_SearchTablePokerTexas(je, actionUser, action.getPlayerId(), "searchTPokerTexas");
                    } else if (je.get("evt").getAsString().equals("joinTable")) {

                        updatePokerUserInfoByPid(action.getPlayerId(), je.get("AG").getAsLong(), true, false);
                        AutoJoinTable(action.getPlayerId(), je.get("tid").getAsInt(), je.get("gid").getAsInt());
                    } else if (je.get("evt").getAsString().equals("currentplayer")) {
                        Process_GetCurrentPlayerInRoom(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("newgamer")) {
                        Process_NewGamer(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("ivp")) { //Moi ban be
                        Process_Invite(je, actionUser, action.getPlayerId()); //Lay danh sach ban be ngoai game de moi
                    }/* else if(je.get("evt").getAsString().equals("7")) {
                     if (actionUser.getNewGamer() != 1)
                     PromotionByUid(dicUser.get(action.getPlayerId()).getPid(),true);
                     }*/ else if (je.get("evt").getAsString().equals("9")) { // Nhan vat

                    } else if (je.get("evt").getAsString().equals("10")) { // region Chuyển AG
                        if (context.getParentRegistry().getServiceInstance(DosProtector.class).allow("bankaction", actionUser.getUserid())) {
                            Process_TransferAG(je, actionUser, action.getPlayerId());
                        }
                    }/* else if (je.get("evt").getAsString().equals("12")) { // region Chức năng mật khẩu két bạc
                        Process_Lock(je, actionUser, action.getPlayerId());
                    }*/ else if (je.get("evt").getAsString().equals("14")) { // region Chức năng ngân hàng 
                        //Process_Bank(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("15")) { // region Chức năng message
//        				if (actionUser.getSource() == 2) {//ghi Log de thong  ke
//        					userController.GameLogMsgNewTemp(getConnection68(),je.get("T").getAsInt(),actionUser.getUsername(),actionUser.getsIP(), message,action.getPlayerId(), actionUser.getUserid());
//        				}
                        Process_Mail(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("16")) { // region Chat The gioi - Game - Bang - Rieng
                        Process_Chat(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("CVDM2C")) { // chuyen Diamond to AG.
                        Process_ConvertDiamondToAG(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("CVC2DM")) { // chuyen Chip to Diamond.
                        Process_ConvertAGToDiamond(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("usermobile")) { // Update Mobile
                        Process_UpdateMobile(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("pf")) { // profile
                        Process_UpdateProfile(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("agtv")) { // Doi AG => Vip
                        Process_ConvertAGtoVip(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("rl") || je.get("evt").getAsString().equals("rlm")) { // Roulette
                        Process_Roulette(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("frl")) {
                        Process_Roulette(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("fb")) { // Bong da
                        Process_Football(je, actionUser, action.getPlayerId());
                    }/* else if(je.get("evt").getAsString().equals("dg")){ // dau gia 
                     Process_Auction(je, actionUser, action.getPlayerId()) ;
                     }*/ else if (je.get("evt").getAsString().equals("xs")) { // xo so
                        Process_Lottery(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("xst")) { //  xo so
                        Process_Lottery_Tour(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("tlucky")) { // bai may man
//                    	sendErrorMsg(action.getPlayerId(), "Bài may mắn đang trong thời gian bảo trì.");
                        //if (actionUser.getSource() == 3)
                        sendErrorMsg(action.getPlayerId(), "Bài may mắn đang trong thời gian bảo trì.");
//                    	else	
//                    		Process_LuckyCard(je, actionUser, action.getPlayerId()) ;
                    } else if (je.get("evt").getAsString().equals("slucky")) {
//                    	sendErrorMsg(action.getPlayerId(), "Bài may mắn đang trong thời gian bảo trì.");
                        //if (actionUser.getSource() == 3)
                        sendErrorMsg(action.getPlayerId(), "Bài may mắn đang trong thời gian bảo trì.");
//                    	else
//                    		Process_LuckyCard(je, actionUser, action.getPlayerId()) ;
                    } else if (je.get("evt").getAsString().equals("flucky")) {
//                    	sendErrorMsg(action.getPlayerId(), "Bài may mắn đang trong thời gian bảo trì.");
//                    	if (actionUser.getSource() == 3)
                        sendErrorMsg(action.getPlayerId(), "Bài may mắn đang trong thời gian bảo trì.");
//                    	else
//                    		Process_LuckyCard(je, actionUser, action.getPlayerId()) ;
                    } else if (je.get("evt").getAsString().equals("srlucky")) {
//                    	sendErrorMsg(action.getPlayerId(), "Vòng quay may mắn đang trong thời gian bảo trì.");
//                    	if (actionUser.getSource() == 1)
//                    		Process_Rotation_Lucky(je, actionUser, action.getPlayerId()) ;
//                    	else
//                    		sendErrorMsg(action.getPlayerId(), "Vòng quay may mắn đang trong thời gian bảo trì.");
                    } else if (je.get("evt").getAsString().equals("rlucky")) {
//                    	sendErrorMsg(action.getPlayerId(), "Vòng quay may mắn đang trong thời gian bảo trì.");
//                    	if (actionUser.getSource() == 1)
//                    		Process_Rotation_Lucky(je, actionUser, action.getPlayerId()) ;
//                    	else
//                    		sendErrorMsg(action.getPlayerId(), "Vòng quay may mắn đang trong thời gian bảo trì.");
                    } else if (je.get("evt").getAsString().equals("frlucky")) {
//                    	sendErrorMsg(action.getPlayerId(), "Vòng quay may mắn đang trong thời gian bảo trì.");
//                    	if (actionUser.getSource() == 1)
//                    		Process_Rotation_Lucky(je, actionUser, action.getPlayerId()) ;
//                    	else
//                    		sendErrorMsg(action.getPlayerId(), "Vòng quay may mắn đang trong thời gian bảo trì.");
                    } else if (je.get("evt").getAsString().equals("dp")) { //Daily Promotion
                        if (actionUser.getSource() != 3) {
                            Process_DailyPromotion(je, actionUser, action.getPlayerId());
                        }
                    } else if (je.get("evt").getAsString().equals("dpl")) { //Daily Promotion ==> Lucky Promotion
                        if (actionUser.getSource() != 3) {
                            Process_DailyPromotion(je, actionUser, action.getPlayerId());
                        }
                    } else if (je.get("evt").getAsString().equals("rename")) { //Doi ten cho username
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("changepass")) { //Doi pass cho username
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("changea")) { //Doi Avatar cho User
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("agvideo")) { //Tang AG Video
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("ref")) { //ghi nhan Ref
                        String strMail = "";
                        String strVersion = "";
                        try {
                            strMail = je.get("email").getAsString();
                            strVersion = je.get("version").getAsString();
                        } catch (Exception e) {
                            // handle exception
                            //System.out.println("==>Error==>SaveRef:" + e.getMessage());
                        }
                        int source = actionUser.getSource();
                        if (je.has("data") && je.get("data").getAsString() != null && (actionUser.getRef().equals(""))) {
//                        	System.out.println("==>Ref:" + je.get("data").getAsString()) ;
                            userController.GameUpdateRef(source, actionUser.getUserid() - ServerDefined.userMap.get(source), je.get("data").getAsString(), strMail, strVersion);
                        }
                    } else if (je.get("evt").getAsString().equals("uag")) {
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("markvip")) {
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("uvip")) { // Gift VIP
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("NRULQ_N")) { //Register Username LQ
                        Process_ChangeUsernameLQ(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("NRULQ")) { //Register Username LQ
                        Process_ChangeUsernameLQ(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("CULQ")) { //Check UsernameLQ
                        Process_ChangeUsernameLQ(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("UULQ")) {
                        //Process_UpdateUsernameLQ(je, actionUser, action.getPlayerId()) ;
                        sendErrorMsg(action.getPlayerId(), "Chức năng đang được nâng cấp.");
                    } else if (je.get("evt").getAsString().equals("US")) { //Cai dat lai Setting User
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("appid")) {
                        Process_UpdateUser(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("pctable")) {
                        Process_GetListMarkForCreateTable(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("uag68")) { //Lay lai thong tin User tu DB
                        Process_UpdateAGAgain(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("reconnect")) {
                        AutoJoinTable(action.getPlayerId(), je.get("tableid").getAsInt(), je.get("gameid").getAsInt());
                    } else if (je.get("evt").getAsString().equals("jointable")) {
                        ActionJoinTable(action.getPlayerId(), je.get("tableid").getAsInt(), actionUser.getGameid());
                    } else if (je.get("evt").getAsString().equals("lefttable")) {
                        ActionLeftTable(action.getPlayerId(), actionUser.getTableId(), actionUser.getGameid());
                    } else if (je.get("evt").getAsString().equals("iap")) {
                        Process_PaymentIAP(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("iap_ios")) {
                        Process_PaymentIAP_IOS(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("paymentcam")) {
                        // Process_PaymentCam(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("follow") || je.get("evt").getAsString().equals("unfollow")
                            || je.get("evt").getAsString().equals("followlist") || je.get("evt").getAsString().equals("followdetail")
                            || je.get("evt").getAsString().equals("followbyname") || je.get("evt").getAsString().equals("followfind")) {
                        friendHandler.Process_Friends(je, actionUser, action.getPlayerId(), serviceRouter);
                    } else if (je.get("evt").getAsString().equals("promotion") || je.get("evt").getAsString().equals("promotion_info")
                            || je.get("evt").getAsString().equals("promotion_online") || je.get("evt").getAsString().equals("promotion_video")
                            || je.get("evt").getAsString().equals("promotion_video_start")) {
                        Process_PromotionSiam(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("messagedetail") || je.get("evt").getAsString().equals("messagelist")
                            || je.get("evt").getAsString().equals("message") || je.get("evt").getAsString().equals("messageread")
                            || je.get("evt").getAsString().equals("messagedelete") || je.get("evt").getAsString().equals("messagenew")
                            || je.get("evt").getAsString().equals("messagedeleteall") || je.get("evt").getAsString().equals("message2")) {
                        messageHandler.Process_Message(je, actionUser, action.getPlayerId(), serviceRouter);
                    } else if (je.get("evt").getAsString().equals("jackpot") || je.get("evt").getAsString().equals("jackpothistory")) { //Ghi nhan inviteF
                        jackpotHandler.Process_Jackpot(serviceRouter, userController, je, actionUser);
                    } else if (je.get("evt").getAsString().equals("displayrule")) {
//                    	loggerLogin_.info("==>Get Rule Display detail" + actionUser.getUsername());
                        Process_DisplayRule(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("inviteF")) { //Ghi nhan inviteF
                        Process_InviteFacebook(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("RUF")) { //Change Username Facebook
                        Process_ChangeUsername_Facebook(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("CUF")) {
                        Process_ChangeUsername_Facebook(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("CUFN")) {
                        Process_ChangeUsername_Facebook(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("GIBD")) {
                        Process_ChangeUsername_Facebook(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("register_user")) { //Dang ky username moi
                        Process_RegisterUsername(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("updateagpayment")) { //Cap nhat tien nap
//                    	System.out.println("==>Update AG from Payment:" + je.get("u").getAsString()) ;
                        String username = je.get("u").getAsString();
                        Process_UpdateAGfromPayment(username, je.get("o").getAsInt());
                    } //Slot
                    else if (je.get("evt").getAsString().equals("slotstart")) { //Slot moi
//                      Process_Slot(je, actionUser, action.getPlayerId());
                        NewProcessSlot(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("slotminifinish")) { //Slot game mini
                        Process_SlotMini(je, actionUser, action.getPlayerId());
                    } //DailyPromotion Thai
                    else if (je.get("evt").getAsString().equals("siamdaily")) {
                        Process_SiamDailyPromotion(je, actionUser, action.getPlayerId());
                    } //Top Cao thu Thai
                    else if (je.get("evt").getAsString().equals("topgamer")) {
                        DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
                        if (dos.allow("topgamer", actionUser.getUserid())) {
                            Process_TopGamer(je, actionUser, action.getPlayerId());
                        }
                    } //Top Dai gia Thai
                    else if (je.get("evt").getAsString().equals("tophighlow")) {
                        Process_TopHighLow(je, actionUser, action.getPlayerId());
                    } //Top Dai gia Thai
                    else if (je.get("evt").getAsString().equals("toprich")) {
                        DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
                        if (dos.allow("toprich", actionUser.getUserid())) {
                            Process_TopRich(je, actionUser, action.getPlayerId());
                        }
                    } else if (je.get("evt").getAsString().equals("LQConvertDT")) {
                        Process_LQConvertDT(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("GiftCode")) {
                        Process_GiftCode(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("shareImageFb")) { //shareImageFb
                        Process_ShareImageFb(je, actionUser, action.getPlayerId());
                    } else if (je.get("evt").getAsString().equals("datafootball")) {
                        //Process_GetFootballData(je, actionUser, action.getPlayerId()); // Xu ly lay du lieu bong da 
                        footballHandler.Process_GetDataFootball(je, actionUser, action.getPlayerId(), serviceRouter);
                    } else if (je.get("evt").getAsString().equals("historyfootball")) {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "datafootball");
                        jo.addProperty("type", 2);
                        //List<MatchHistory> ls = new ArrayList<MatchHistory>();
                        // System.out.println("Size: "+ls.size());
                        //ls = GameGetHistoryMatch(actionUser.getSource(),actionUser.getUserid() - ServerDefined.userMap.get((int)actionUser.getSource()).intValue());
                        String json = ActionUtils.gson.toJson(footballHandler.GameGetHistoryMatch(actionUser.getSource(), je.get("id").getAsInt()));
                        jo.addProperty("data", json);
//                        System.out.println("Data " + json);
                        ClientServiceAction csa = new ClientServiceAction(action.getPlayerId(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(action.getPlayerId(), csa);
                    } else if (je.get("evt").getAsString().equals("betfootball")) {
                        footballHandler.Process_BetFootball(je, actionUser, action.getPlayerId(), userController, serviceRouter);
                    } else if (je.get("evt").getAsString().equals("logout")) {
                        loggerLogin_.info("==>Logout:" + actionUser.getPid());
                        JsonObject jo = new JsonObject();
                        jo.addProperty("evt", "logout");
                        ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                        clientDisconnected(actionUser.getPid());
                        //ClientRegistryServiceContract registry = getSeviceRegistry().getServiceInstance(ClientRegistryServiceContract.class);

                        //registry.getClientRegistry().logoutClient(client, packet.leaveTables);
                    } else if (je.get("evt").getAsString().equals("checkBotBinh")) {
                        //botBinhHandler.checkBot(je,actionUser);
                    } else if (je.get("evt").getAsString().equals("reloadBotBinh")) {  // Binh                	        
                        //botBinhHandler.reloadBot(je,actionUser,userController);
//                    } else if (je.get("evt").getAsString().equals("checkBotDummy")){               	
//                    	DummyBotHandler.checkBot(je,actionUser);
//                    }else if (je.get("evt").getAsString().equals("checkTableDummy")){               	
//                    	DummyBotHandler.checkTableBot(je,actionUser);
//                    }
//                    else if (je.get("evt").getAsString().equals("reloadBotDummy")){
//                    	DummyBotHandler.reloadBot(je,actionUser);
//                    } else if (je.get("evt").getAsString().equals("checkBotDummyThai")){  	
//                    	DummyThaiBotHandler.checkBot(je,actionUser);
//                    } else if (je.get("evt").getAsString().equals("reloadBotDummyThai")){
//                    	DummyThaiBotHandler.reloadBot(je,actionUser);
//                    } else if (je.get("evt").getAsString().equals("checkBotPoker9K")){                  	
//                    	botPoker9KHandler.checkBot(je,actionUser);
//                    } else if (je.get("evt").getAsString().equals("reloadBotPoker9K")){
//                    	botPoker9KHandler.reloadBot(je,actionUser);
//                    } else if (je.get("evt").getAsString().equals("checkBotPokdeng")){                      
//                        PokdengBotHandler.checkBot(je,actionUser);
//                    } else if (je.get("evt").getAsString().equals("reloadBotPokdeng")){
//                        PokdengBotHandler.reloadBot(je,actionUser);
//                    } else if (je.get("evt").getAsString().equals("checkBotTomCuaCa")){                      
//                        TomCuaCaBotHandler.checkBot(je,actionUser);
//                    } else if (je.get("evt").getAsString().equals("reloadBotTomCuaCa")){
//                        TomCuaCaBotHandler.reloadBot(je,actionUser);
//                    } 
                    } else if (je.get("evt").getAsString().equals("botCreateTable")) {
                        //JsonObject send = new JsonObject();
                        //send.addProperty("evt", "botCreateTable");
                        // System.out.println("==>Binhhh:"+BotHandler.listBots.get(BotHandler.listBots.size()-1).getUserid()+ServerDefined.userMap.get(9));
                        //send.addProperty("pid", BinhBotHandler.listBots.get(BinhBotHandler.listBots.size()-1).getPid());
                        // ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                        //serviceRouter.dispatchToGameActivator(GAMEID.BINH, request);
                    } else if (je.get("evt").getAsString().equals("getGold")) {
                        ProcessAddGoldByCondition(je, actionUser);
                    } else if (je.get("evt").getAsString().equals("createT")) {
                        Process_CreateTable(je, actionUser);
                    } else if (je.get("evt").getAsString().equals(EvtDefine.EVT_GET_CHAT_WOLD)) {
                        JsonObject act = new JsonObject();
                        act.addProperty("evt", EvtDefine.EVT_GET_CHAT_WOLD);
                        act.addProperty("data", chatHandler.getHistoryChatWorld());
                        ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                    } else if (je.get("evt").getAsString().equals(EvtDefine.EVT_PAYMENT_PACKAGE)) {
                        List<PaymentPackage> list = new PaymentPackageHandle(actionUser.getPid(), actionUser.getSource()).doQuery();
                        System.out.println(list);
                        JsonObject send = new JsonObject();
                        send.addProperty("evt", EvtDefine.EVT_PAYMENT_PACKAGE);

                        JsonElement element = GameUtil.gson.toJsonTree(list, new TypeToken<List<PaymentPackage>>() {
                        }.getType());
                        send.add("list", element);

                        ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);

                    } else if (je.get("evt").getAsString().equals(EvtDefine.EVT_GET_PIG_BANK)) {
                        PigBank pig = new PigBankHandle(actionUser.getPid(), actionUser.getSource()).doQuery();
                        JsonObject send = new JsonObject();
                        send.addProperty("evt", EvtDefine.EVT_GET_PIG_BANK);
                        send.add("pig", GameUtil.gson.toJsonTree(pig));

                        ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(send).getBytes("UTF-8"));
                        serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
                    }

                    actionUser = null;
                } else {
                    //Add lai user vao Store
                    /*if(dicUserStore.containsKey(action.getPlayerId())){
                     System.out.println("==>Loi Evt(Not User online)==>Add Nguoc lai:" + (new Date()).toString() + ":" + message + "-" + action.getPlayerId()) ;
                     UserInfo actionUser1 = dicUserStore.get(action.getPlayerId()) ;
                     System.out.println("==>Loi Evt(Not User online)==>Add Nguoc lai1:") ;
                     dicUser.put(actionUser1.getUserid(), actionUser1) ;
                     System.out.println("==>Loi Evt(Not User online)==>Add Nguoc lai2:") ;
                     dicUserStore.remove(actionUser1.getUserid()) ;
                     System.out.println("==>Loi Evt(Not User online)==>Add Nguoc lai3:") ;
                     if(je.get("evt").getAsString().equals("selectG"))
                     Process_SelectGame(je, actionUser1, action.getPlayerId()) ;
                     }
                     System.out.println("==>Loi Evt(Not User online):" + (new Date()).toString() + ":" + message + "-" + action.getPlayerId());
                     */
                    //sendErrorMsg(action.getPlayerId(), "Bạn đã bị chấm dứt kết nối với máy chủ, mời bạn kết nối lại."); //Viet
                    loggerLogin_.info("==>Disconnect By Not dic:" + action.getPlayerId() + "-" + message);
                    if (IsRunThai) {
                        //sendErrorMsg(action.getPlayerId(), "ท่านไม่ได้เชื่อมต่อกับเซิร์ฟเวอร์ กรุณาลองใหม่อีกครั้ง"); //Thai
                        PlayerDisconnected(action.getPlayerId(), ServerSource.THAI_SOURCE);
                    } else if (IsRunIndo) {
                        //sendErrorMsg(action.getPlayerId(), "Tidak terhubung dengan server Silahkan koneksi lagi"); //Thai
                        PlayerDisconnected(action.getPlayerId(), ServerSource.IND_SOURCE);
                    } //else
                    //sendErrorMsg(action.getPlayerId(), "Hiện kết nối của bạn không ổn định, mời bạn kết nối lại."); //Thai
                    //PlayerDisconnected(action.getPlayerId(), 9);
                    if (je.has("idevt")) {
                        checkNotIDEVT = false;
                    }
                }
            }
            if (checkNotIDEVT) {
                Process_Other(je, dicUser.get(action.getPlayerId()), action.getPlayerId());
            }
        } catch (Exception e) {
            try {
                System.out.println("==>Error==>onAction:" + (new Date()).toString() + ":" + new String(action.getData(), "UTF-8") + "-" + e.getMessage());
            } catch (Exception e2) {
                // handle exception
            }
            e.printStackTrace();
        }
    }

    //Xu ly Select Table
    private void Process_CreateTable(JsonObject je, UserInfo actionUser) {
        try {

            DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
            if (dos.allow("createT", actionUser.getUserid())) {
                if (actionUser.getUnlockPass() == 0) {
                    return;
                }
                JsonObject send = new JsonObject();
                send.addProperty("evt", "createT");
                send.addProperty("pid", actionUser.getPid());
                send.addProperty("M", je.get("M").getAsInt());
                send.addProperty("S", je.get("S").getAsInt());

                ActivatorAction<String> request = new ActivatorAction<String>(ActionUtils.gson.toJson(send));
                serviceRouter.dispatchToGameActivator(actionUser.getGameid(), request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean CheckHighLow(int uid) {
        try {
            return taixiuHandler.CheckHighlow(uid);
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }
        return false;
    }

    private void processTaiXiu(ServiceAction action) {
        try {
            if (!IsRunServerTaiXiu) {
                return;
            }
            String message = new String(action.getData(), "UTF-8");
            JsonObject je = (JsonObject) parser.parse(message);
            Logger.getLogger("TaiXiuLog").info("==>User_onAction Taixiu: pid: " + action.getPlayerId() + " - " + message);
            if (dicUser.containsKey(action.getPlayerId())) {
                synchronized (dicUser) {
                    UserInfo actionUser = dicUser.get(action.getPlayerId());
                    int source = (int) actionUser.getSource();
                    Logger.getLogger("TaiXiuLog").info("==>User_onAction Taixiu: pid: " + action.getPlayerId() + " - " + message + "-" + actionUser.getTableId());
                    if (je.get("evt").getAsString().equals("bethighlow1") && (actionUser.getTableId() == 0)) {
                        if (taixiuHandler.statusTaixiu) {
                            int n = je.get("N").getAsInt();
                            int m = je.get("M").getAsInt();
                            if (m <= 0 || m > 100000000) {
                                sendErrorMsg(action.getPlayerId(), actionUtils.getConfigText("strHighlow_ChipError", source, actionUser.getUserid()));
                                return;
                            }
                            if (n != 1 && n != 2) {
                                sendErrorMsg(action.getPlayerId(), actionUtils.getConfigText("strHighlow_BetError", source, actionUser.getUserid()));
//                        		sendErrorMsg(action.getPlayerId(), "Thông tin cửa đặt không hợp lệ.");
                                return;
                            }
                            if (actionUser.getAGHigh() > 0 && n == 1) {
                                sendErrorMsg(action.getPlayerId(), actionUtils.getConfigText("strHighlow_LowError", source, actionUser.getUserid()));
//                        		sendErrorMsg(action.getPlayerId(), "Bạn đã đặt tài nên không được đặt Xỉu.");
                                return;
                            }
                            if (actionUser.getAGLow() > 0 && n == 2) {
                                sendErrorMsg(action.getPlayerId(), actionUtils.getConfigText("strHighlow_HighError", source, actionUser.getUserid()));
//                        		sendErrorMsg(action.getPlayerId(), "Bạn đã đặt xỉu nên không được đặt Tài.");
                                return;
                            }
                            if ((actionUser.getAG().intValue() < m && actionUser.getTableId() == 0)
                                    || (actionUser.getTableId() != 0 && actionUser.getAG().intValue() < m + 1000000)) {
                                sendErrorMsg(action.getPlayerId(), actionUtils.getConfigText("strHighlow_ChipUnder", source, actionUser.getUserid()));
//                        		sendErrorMsg(action.getPlayerId(), "Bạn không đủ số dư để đặt cửa.");
                                return;
                            }
                            taixiuHandler.bet(dicUser.get(action.getPlayerId()), n, m, 0, (int) actionUser.getSource());
                        } else {
                            sendErrorMsg(action.getPlayerId(), actionUtils.getConfigText("strHighlow_BetTimeError", source, actionUser.getUserid()));
                        }
                    } else if (je.get("evt").getAsString().equals("highlowinfo1")) {
                        long seconds = (System.currentTimeMillis() - taixiuHandler.startTime) / 1000;
                        Logger.getLogger("TaiXiuLog").info("==>highlowinfo:" + taixiuHandler.startTime + "-" + taixiuHandler.statusTaixiu + "-" + seconds);
                        if (je.has("State")) {
                            if (je.get("State").getAsInt() == 1) {
                                taixiuHandler.HighlowOpen(actionUser.getUserid());
                            }
                        }
                        if (taixiuHandler.statusTaixiu) {
                            JsonObject sendTX = new JsonObject();
                            sendTX.addProperty("evt", "highlow1");
                            sendTX.addProperty("H", taixiuHandler.AgTai);
                            sendTX.addProperty("L", taixiuHandler.AgXiu);
                            sendTX.addProperty("NH", taixiuHandler.NTai);
                            sendTX.addProperty("NL", taixiuHandler.NXiu);
                            sendTX.addProperty("UH", dicUser.get(action.getPlayerId()).getAGHigh());
                            sendTX.addProperty("UL", dicUser.get(action.getPlayerId()).getAGLow());
                            sendTX.addProperty("T", TaiXiuHandler.timePlay - 2 - seconds);
                            sendTX.addProperty("strH", TaiXiuHandler.strHistoryTaixiu);
                            if (actionUser.getTableId() != 0) {
                                sendTX.addProperty("MB", GetMaxBetHighlow((int) actionUser.getGameid(), actionUser.getAS().intValue(), actionUser.getAG().intValue()));
                            } else {
                                sendTX.addProperty("MB", actionUser.getAG().intValue());
                            }
                            Logger.getLogger("TaiXiuLog").info("==>highlowInfo:" + action.getPlayerId() + "-" + ActionUtils.gson.toJson(sendTX));
                            ClientServiceAction csataixiu = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendTX).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(action.getPlayerId(), csataixiu);
                        } else {
                            seconds = (System.currentTimeMillis() - taixiuHandler.stopTime) / 1000;
                            JsonObject sendTXW = new JsonObject();
                            sendTXW.addProperty("evt", "highlowwait1");
                            sendTXW.addProperty("T", TaiXiuHandler.timeWait - 1 - seconds);
                            sendTXW.addProperty("strH", TaiXiuHandler.strHistoryTaixiu);
                            if (actionUser.getTableId() != 0) {
                                sendTXW.addProperty("MB", GetMaxBetHighlow((int) actionUser.getGameid(), actionUser.getAS().intValue(), actionUser.getAG().intValue()));
                            } else {
                                sendTXW.addProperty("MB", actionUser.getAG().intValue());
                            }
                            ClientServiceAction csataixiuw = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(sendTXW).getBytes("UTF-8"));
                            serviceRouter.dispatchToPlayer(action.getPlayerId(), csataixiuw);
                        }
                        if (!actionUser.isNap()) //Lan gui vao ban
                        {
                            taixiuHandler.HighlowOpen(actionUser.getUserid());
                        } else {
                            dicUser.get(action.getPlayerId()).setNap(false); //Cac
                        }
                    } else if (je.get("evt").getAsString().equals("highlowhistory1")) {
                        taixiuHandler.GetHistory(source, actionUser.getUserid().intValue());
                    } else if (je.get("evt").getAsString().equals("highlowclose")) {
                        taixiuHandler.HighlowClose(actionUser.getUserid().intValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean UpdateBetTaiXiu(int uid, int agHigh, int agLow, long timeBet, long idbet) {
        try {
            synchronized (dicUser.get(uid)) {
                int source = (int) dicUser.get(uid).getSource();
                long ag = (new UserController()).UpdateBetTaixiu(source, uid - ServerDefined.userMap.get(source), agHigh, agLow, timeBet, idbet, dicUser.get(uid));
                if (ag > -1) {
                    dicUser.get(uid).setStatusHighLow(1); //Chuyen ve trang thai ket thuc
                    dicUser.get(uid).setAGHigh(dicUser.get(uid).getAGHigh() + agHigh);
                    dicUser.get(uid).setAGLow(dicUser.get(uid).getAGLow() + agLow);
                    dicUser.get(uid).setAG(ag);
                    Logger.getLogger("TaiXiuLog").info("==>Update BetTaixiu:" + uid + "-" + agHigh + "-" + agLow + "-" + dicUser.get(uid).getAGHigh() + "-" + dicUser.get(uid).getAGLow());
                    return true;// OK
                } else {
                    Logger.getLogger("TaiXiuLog").info("==>Update BetTaixiu Loi:" + uid + "-" + agHigh + "-" + agLow);
                    return false;// update fail
                }
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
            return false;
        }
    }

    public static int GetMaxBetHighlow(int gameid, int mark, int ag) {
        int heso = 40;
        if (gameid == 8007) //Chan
        {
            heso = 50;
        } else if (gameid == 8012) //Tien len
        {
            heso = 80;
        } else if (gameid == 8004 || gameid == 8005) {
            heso = 40;
        } else if (gameid == 8021) {
            heso = 1500;
        } else if (gameid == 8024) {
            heso = 1000;
        } else {
            heso = 30;
        }
        if (ag >= mark * heso) {
            return ag - mark * heso;
        } else {
            return 0;
        }
    }

    public static void UpdateKetquaTaiXiu(int uid, long markwin, long markwinreal, int totalRefund, int[] arr, long timestop, String winN, ServiceRouter serviceRouter) {
        try {
            synchronized (dicUser.get(uid)) {
                dicUser.get(uid).setStatusHighLow(0); //Chuyen ve trang thai ket thuc
                dicUser.get(uid).setAGWinHighLow((int) markwin);
                //long win = markwin - dicUser.get(uid).getAGHigh() - dicUser.get(uid).getAGLow() ;
                int source = (int) dicUser.get(uid).getSource();
                long ret = (new UserController()).UpdateAGTaixiu(source, uid - ServerDefined.userMap.get(source), markwin, markwinreal, totalRefund, arr, dicUser.get(uid).getAGHigh(), dicUser.get(uid).getAGLow(), timestop, TaiXiuHandler.idHighlow);
                dicUser.get(uid).setAG(ret);
                dicUser.get(uid).setStatusHighLow(0);
                dicUser.get(uid).setAGHigh(0);
                dicUser.get(uid).setAGLow(0);
                Logger.getLogger("TaiXiuLog").info("==>Update KetquaTaixiu:" + uid + "-" + dicUser.get(uid).getAG().intValue() + "-" + markwin);
                JsonObject act = new JsonObject();
                act.addProperty("evt", "highlowwin1");
                act.addProperty("W", markwinreal);
                act.addProperty("N", winN); //Ket qua

                act.addProperty("H", taixiuHandler.AgTai);
                act.addProperty("L", taixiuHandler.AgXiu);
                act.addProperty("NH", taixiuHandler.NTai);
                act.addProperty("NL", taixiuHandler.NXiu);
                act.addProperty("UH", taixiuHandler.userTai);
                act.addProperty("UL", taixiuHandler.userXiu);

                act.addProperty("AG", dicUser.get(uid).getAG().intValue());
                if (dicUser.get(uid).getTableId() != 0) {
                    act.addProperty("MB", GetMaxBetHighlow((int) dicUser.get(uid).getGameid(), dicUser.get(uid).getAS().intValue(), dicUser.get(uid).getAG().intValue()));
                    //Gui thong diep den cac user cung ban
                    GameDataAction gda = new GameDataAction(uid, dicUser.get(uid).getTableId());
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "amuvip");
                    if (markwinreal == 0) {
                        jo.addProperty("ag", markwinreal);
                    } else {
                        jo.addProperty("ag", markwin);
                    }
                    gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jo).getBytes("UTF-8")));
                    serviceRouter.dispatchToGame(dicUser.get(uid).getGameid(), gda);
                } else {
                    act.addProperty("MB", dicUser.get(uid).getAG().intValue());
                }
                ClientServiceAction csa = new ClientServiceAction(uid, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(uid, csa);
                Logger.getLogger("TaiXiuLog").info("==>HighLowWin:" + ActionUtils.gson.toJson(act));
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }
    }

    public static void RefundTaiXiu(int uid, int refundGold, int n, ServiceRouter serviceRouter) {
        try {
            synchronized (dicUser.get(uid)) {
                JsonObject act = new JsonObject();
                act.addProperty("evt", "highlowrefund1");
                act.addProperty("R", refundGold);
                act.addProperty("N", n);
                ClientServiceAction csa = new ClientServiceAction(uid, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(uid, csa);
                if (dicUser.get(uid).getTableId() != 0) {
                    //Gui thong diep den cac user cung ban
                    GameDataAction gda = new GameDataAction(uid, dicUser.get(uid).getTableId());
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "amuvip");
                    jo.addProperty("ag", refundGold);
                    gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jo).getBytes("UTF-8")));
                    serviceRouter.dispatchToGame(dicUser.get(uid).getGameid(), gda);
                }
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }
    }

    private void ProcessAddGoldByCondition(JsonObject je, UserInfo actionUser) {
        try {
            synchronized (dicUser) {
                int ag = 9;
                dicUser.get(actionUser.getPid()).IncrementMark(ag);
                userController.UpdateAG((int) actionUser.getSource(), actionUser.getPid() - ServerDefined.userMap.get((int) actionUser.getSource()).intValue(), ag, false);

                JsonObject act = new JsonObject();
                act.addProperty("evt", "getGold");
                act.addProperty("AG", ag);
                ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public LoginHandler locateLoginHandler(LoginRequestAction action) {
        try {
            DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
            loggerLogin_.info(action.getOperatorid() + "-" + action.getUser() + "-" + action.getRemoteAddress().getAddress().getHostAddress() + "-" + action.getPassword());
            if (action.getOperatorid() == Operator.OPERATOR_ADMIN) {
                return adminHandler;
            }
            if ((action.getOperatorid() == Operator.OPERATOR_INDO
                    || action.getOperatorid() == Operator.OPERATOR_INDO1) && IsRunIndo) { //Indo
                UserInfo ulogin = (new Gson()).fromJson(action.getUser(), UserInfo.class);
                loggerLogin_.info("==>Bat dau login Indo:" + ulogin.getUsername() + "-" + ulogin.getDeviceId());
                if (ulogin.getUserIdZing().length() > 0) {
                    if (dicCurrent.containsKey(ulogin.getUserIdZing())) {
                        loggerLogin_.info("==>Login Fast ==> Size Login:" + dicCurrent.size() + "-" + action.getUser());
                        return new ErrorLoginHandler("strConnect_Err3", ServerSource.IND_SOURCE);
                    } else {
                        loggerLogin_.info("==>Add vao List:" + dicCurrent.size() + "-" + action.getUser());
                        dicCurrent.put(ulogin.getUserIdZing(), (new Date()).toString());
                    }
                }
                if (ulogin.getUsertype() == 1) {
                    return new IndoLoginHandler2(context);
                } else if (ulogin.getUsertype() == 2) {
                    return new FacebookIndoLoginHandler2(context);
                } else if (ulogin.getUsername().equals("1")) { //Facebook
                    return indoFacebookHandler;
                } else if (ulogin.getUsername().equals("2")) { //Zing
                    return indoHandler;
                } else { //Login
                    return indoTempHandler;
                }
            } else if ((action.getOperatorid() == Operator.OPERATOR_THAI || action.getOperatorid() == Operator.OPERATOR_THAI1
                    || action.getOperatorid() == Operator.OPERATOR_THAI2 || action.getOperatorid() == Operator.OPERATOR_THAI3
                    || action.getOperatorid() == Operator.OPERATOR_THAI4) && IsRunThai) { //Thai
                UserInfo ulogin = (new Gson()).fromJson(action.getUser(), UserInfo.class);
                loggerLogin_.info("==>Bat dau login Thai:" + ulogin.getUsername() + "-" + ulogin.getDeviceId());
                if (ulogin.getUserIdZing().length() > 0) {
                    if (dicCurrent.containsKey(ulogin.getUserIdZing())) {
                        loggerLogin_.info("==>Login Fast==>Size Login:" + dicCurrent.size() + "-" + action.getUser());
                        return new ErrorLoginHandler("strConnect_Err3", ServerSource.THAI_SOURCE);
                    } else {
                        loggerLogin_.info("==>Add vao List:" + dicCurrent.size() + "-" + action.getUser());
                        dicCurrent.put(ulogin.getUserIdZing(), (new Date()).toString());
                    }
                }

                if (ulogin.getUsertype() == 1) {
                    return new ThaiLoginHandler2(context);
                } else if (ulogin.getUsertype() == 2) {
                    return new FacebookThaiLoginHandler2(context);
                } else if (ulogin.getUsername().equals("1")) { //Facebook
                    return new FacebookThaiNew1LoginHandler(context);
                } else if (ulogin.getUsername().equals("2")) { //Zing
                    return new ThaiLoginHandler(context);
                } else if (ulogin.getUsername().equals("3")) { //
                    return new FacebookThaiPokdengLogin(context);
                } else { //Login
                    return new ErrorLoginHandler("strConnect_Err3", ServerSource.THAI_SOURCE);
                }
            } else if (action.getOperatorid() == Operator.OPERATOR_MYANMAR && IsRunMYM) { //Myanmar
                UserInfo ulogin = (new Gson()).fromJson(action.getUser(), UserInfo.class);

                loggerLogin_.info("==>LoginDTStart:" + action.getUser() + "-" + ulogin.getDeviceId());
                if ((ulogin.getDeviceId().indexOf("web") == -1) && (!dos.allow("loginSpamRule", ulogin.getDeviceId()))) {
                    loggerLogin_.error("==>LoginDTfast==>Return:" + action.getUser());
                    return vietTempHandler;
                }
                //AuthService authService = new AuthServiceImpl();
                if (ulogin.getUsertype() == 1) {
                    return new MyanmarLogin1Handle(context); //, authService);
                } else if (ulogin.getUsertype() == 2) {
                    return new MyanmarFacebookLoginHandle(context); //, authService);
                    //return new MyanmarFacebookLogin1Handle(context); //, authService);
                }

            } else if (action.getOperatorid() == Operator.OPERATOR_INDIA && IsRunIndia) { //India
                UserInfo ulogin = (new Gson()).fromJson(action.getUser(), UserInfo.class);
                loggerLogin_.info("==>LoginDTStart:" + action.getUser() + "-" + ulogin.getDeviceId());
                if ((ulogin.getDeviceId().indexOf("web") == -1) && (!dos.allow("loginSpamRule", ulogin.getDeviceId()))) {
                    loggerLogin_.error("==>LoginDTfast==>Return:" + action.getUser());
                    return vietTempHandler;
                }
                if (ulogin.getUsertype() == 1) {
                    return new IndiaLogin(context);
                } else if (ulogin.getUsertype() == 2) {
                    return new FacebookIndiaLogin(context);
                }
            } else if (action.getOperatorid() == Operator.OPERATOR_BOT) {
                return botLoginHandler;
            }
            loggerLogin_.error("===>Error==>locateLoginHandler:" + action.getOperatorid() + "-" + action.getUser() + "-" + action.getRemoteAddress().getAddress().getHostAddress() + "-" + action.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
            // handle exception
            //System.out.println("===>Error==>locateLoginHandler:" + e.getMessage()) ;
            loggerLogin_.error("===>Error==>locateLoginHandler1:" + action.getOperatorid() + "-" + action.getUser() + "-" + action.getRemoteAddress().getAddress().getHostAddress() + "-" + action.getPassword() + ":" + e.getMessage());
        }
        return new VietTempLoginHandler();// handler ;
    }

    private int CheckValidUsernameThai(String strInput) {
        try {
            if (strInput.length() > 35) {
                return 1;
            }
            if (strInput.trim().length() < 4) {
                return 2;
            }
            char[] arrayNotValid = {' ', '{', '}', '[', ']', '?', '(', ')', '/', '#', '&', '*', '^', '%', '$', '@', '!', '~', '`', '<', '>', '\''};
            char[] chars = strInput.toCharArray();
            for (short x = 0; x < chars.length - 1; x++) {
                for (int i = 0; i < arrayNotValid.length; i++) {
                    if (chars[x] == arrayNotValid[i]) {
                        return 3;
                    }
                }
            }
            return 0;
        } catch (Exception e) {
            // handle exception
            return 9;
        }
    }

    //Check Valid UsernameLQ
    private int CheckValidUsernameLQ(String strInput) {
        try {
            char[] arrayValid = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '.',};
            if (strInput.length() > 20) {
                return 1;
            }
            if (strInput.trim().length() < 6) {
                return 2;
            }
            strInput = strInput.toLowerCase();
            char[] chars = strInput.toCharArray();
            if ((chars[0] == 'f') && (chars[1] == 'b') && (chars[2] == '.')) {
                return 3;
            }
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
            // handle exception
            return 9;
        }
    }

    private int CheckValidPassLQ(String strInput) {
        try {
            char[] arrayValid = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm'};
            char[] arrayNValid = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
            if (strInput.length() > 25) {
                return 1;
            }
            if (strInput.trim().length() < 6) {
                return 2;
            }
            strInput = strInput.toLowerCase();
            char[] chars = strInput.toCharArray();
            boolean tchar = false;
            boolean tnumber = false;
            for (short x = 0; x < arrayNValid.length; x++) {
                boolean t = false;
                for (short i = 0; i < chars.length; i++) {
                    if (arrayNValid[x] == chars[i]) {
                        t = true;
                        break;
                    }
                }
                if (t) {
                    tnumber = true;
                    break;
                }
            }
            if (!tnumber) {
                return 3; //Khong bao gom chu so
            }
            for (short x = 0; x < arrayValid.length; x++) {
                boolean t = false;
                for (short i = 0; i < chars.length; i++) {
                    if (arrayNValid[x] == chars[i]) {
                        t = true;
                        break;
                    }
                }
                if (t) {
                    tchar = true;
                    break;
                }
            }
            if (!tchar) {
                return 4; //Khong bao gom chu cai
            }
            return 0;
        } catch (Exception e) {
            // handle exception
            return 9;
        }
    }

    //region Function Game
    @Override
    public void selectLevel(int pid, int levelid, String data, int gameId) {
        synchronized (dicUser) {
            if (dicUser.containsKey(pid)) {
                dicUser.get(pid).setLevelId((short) levelid);
                //dicUser.get(pid).setGameid(gameId);
            }
        }
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "2");
            jo.addProperty("data", data);
            ClientServiceAction csa = new ClientServiceAction(pid, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(pid, csa);
        } catch (Exception ex) {
//            System.out.println("ERROR: " + ex.getStackTrace());
            ex.printStackTrace();
        }
    }

    public int GetCurrentPlayerInRoom(int roomId, int gameId) {
//        synchronized (dicUser) {
        try {
            int count = 0;
            Set<Integer> keys = dicUser.keySet();
            for (Integer key : keys) {
                if (dicUser.containsKey(key)) {
                    if ((dicUser.get(key).getGameid() == gameId) && (dicUser.get(key).getRoomId() == roomId)) {
                        count++;
                    }
                }
            }
            int countbot = 0;
            countbot = botHandler.getCurrentBotInRoom(roomId, gameId);

            return count + countbot;
        } catch (Exception e) {
            // handle exception
            return 0;
        }
//        }
    }

    public int GetCurrentPlayerInMark(int mark, int gameId) {
//        synchronized (dicUser) {

        try {
            return actionHandler.getMarkTableInfo().getCurrentPlayer(mark, gameId);
//                int count = 0;
//                Set<Integer> keys = dicUser.keySet();
//                for (Integer key : keys) {
//                    if (dicUser.containsKey(key)) {
//                        if ((dicUser.get(key).getGameid() == gameId) && (dicUser.get(key).getAS().intValue() == mark)) {
//                            count++;
//                        }
//                    }
//                }
//                int countbot = 0;
//                countbot = botHandler.processGetCurrentBotInMark(mark,gameId);
//                return count+ countbot;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
//        }
    }

    public int GetCurrentPlayerInMark(long mark, int gameId) {
        try {
            return actionHandler.getMarkTableInfo().getCurrentPlayer(mark, gameId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void confirmSelectRoom(int pid, int roomId, int gameId) {
        synchronized (dicUser) {
            if (dicUser.containsKey(pid)) {
                dicUser.get(pid).setRoomId((short) roomId);
            }
        }
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "selectR");
            jo.addProperty("data", roomId);
            ClientServiceAction csa = new ClientServiceAction(pid, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(pid, csa);
        } catch (Exception ex) {
//            System.out.println("ERROR confirmSelectRoom: " + ex.getStackTrace());
            ex.printStackTrace();
        }
    }

    public void confirmSelectRoom_Only(int pid, int roomId, int tableId, int mark) {
        try {
            boolean checkUser = true;
            if (roomId < 1 || roomId > 4) {
                roomId = 1;
            }
            synchronized (dicUser) {
                if (dicUser.containsKey(pid)) {
                    checkUser = false;
                    dicUser.get(pid).setRoomId(actionHandler.getRoom().getRoomIDbyMark(dicUser.get(pid).getGameid(), mark, roomId));
                    dicUser.get(pid).setTableId(tableId);
                    dicUser.get(pid).setLastDisconnect(new Date());
                    dicUser.get(pid).setAS((long) mark); //Luu muc cuoc ban choi hien tai
                    // Set
                    //Set trang thai de khong luu taixiuhanler
                    dicUser.get(pid).setNap(true);
                    //Update 17/08/2016
                    userController.UpdateTableIdToCache((int) dicUser.get(pid).getSource(), pid
                            - ServerDefined.userMap.get((int) dicUser.get(pid).getSource()).intValue(), tableId);
                    actionHandler.getRoom().processPlayerJoinTable(dicUser.get(pid));
                    actionHandler.getMarkTableInfo().updatePlayerMark(dicUser.get(pid).getGameid(), mark, 1);
                }
            }
            if (checkUser) {
                UserInfo userBot = botHandler.processConfirmRoom(pid, roomId, tableId, mark);
                if (userBot != null) {
                    userBot.setRoomId(actionHandler.getRoom().getRoomIDbyMark(userBot.getGameid(), mark, roomId));
                    actionHandler.getRoom().processPlayerJoinTable(userBot);
                    actionHandler.getMarkTableInfo().updatePlayerMark(userBot.getGameid(), mark, 1);
                    String key = userController.genCacheUserInfoKey((int) userBot.getSource(), userBot.getUserid() - ServerDefined.userMap.get((int) userBot.getSource()));
                    UserController.getCacheInstance().set(key, userBot, 0);
                }
            }
        } catch (Exception e) {
            Logger.getLogger("Debug_service").error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    public void botRejectJoinTable(int pid) {

    }

    @Override
    public void updatePokerGameTypeUser(int pid, short gametype) {
        synchronized (dicUser) {
            if (dicUser.containsKey(pid)) {
                dicUser.get(pid).setGameType9k(gametype);
            }
        }
    }

    @Override
    public void updatePokerUserInfoByPid(int pid, long agBuyIn, boolean autoFill, boolean autoTopOff) {
        botHandler.updateBotInfoBuyIn(pid, agBuyIn, autoFill, autoTopOff);
    }

    @Override
    public String getUserInfoByPid(int pid, int tid) {
        synchronized (dicUser) {
            if (dicUser.containsKey(pid)) {
                if (tid > 0) {
                    dicUser.get(pid).setTableId(tid);
                }
                return ActionUtils.gson.toJson(dicUser.get(pid).getUserGame());
            }
        }
        return botHandler.processGetBotInfoByPid(pid, tid);
    }

    @Override
    public void PlayerLeaveTable(int pid) {
        try {
            boolean checkUser = true;
            loggerLogin_.info("==>PlayerLeaveTable Start:" + pid);
            synchronized (dicUser) {
                if (dicUser.containsKey(pid)) {
                    loggerLogin_.info("==>PlayerLeaveTable Start:" + pid + "-" + dicUser.get(pid).isDisconnect());
                    checkUser = false;
                    dicUser.get(pid).setTableId(0);
                    dicUser.get(pid).setLastDisconnect(new Date());
                    actionHandler.getMarkTableInfo().updatePlayerMark(dicUser.get(pid).getGameid(), dicUser.get(pid).getAS().intValue(), -1);
                    dicUser.get(pid).setAS(0l); //Update 22/11
                    dicUser.get(pid).setNap(false);
                    //Update 09/03/2016
                    userController.UpdateTableIdToCache((int) dicUser.get(pid).getSource(), pid
                            - ServerDefined.userMap.get((int) dicUser.get(pid).getSource()).intValue(), 0);
                    loggerLogin_.info("==>PlayerLeaveTable End:" + pid + "-" + dicUser.get(pid).getTableId());
                    if (dicUser.get(pid).isDisconnect()) {
                        loggerLogin_.info("==>PlayerLeaveTable when disconnect:" + pid);
                        actionHandler.getRoom().processPlayerLeave(dicUser.get(pid), pid);
                        PlayerDisconnected(pid, (int) dicUser.get(pid).getSource());
                    } else {
                        actionHandler.getRoom().processPlayerLeave(dicUser.get(pid), pid);
                    }
                }
            }
            if (checkUser) { //  is bot
                UserInfo userBot = botHandler.updateAvailablebot(pid);
                if (userBot != null) {
                    actionHandler.getRoom().processPlayerLeave(userBot, pid);
                    actionHandler.getMarkTableInfo().updatePlayerMark(userBot.getGameid(), userBot.getAS().intValue(), -1);
                    String key = userController.genCacheUserInfoKey((int) userBot.getSource(), userBot.getUserid() - ServerDefined.userMap.get((int) userBot.getSource()));
                    UserController.getCacheInstance().set(key, userBot, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateResultLast(int pid, int result) {
        synchronized (dicUser) {
            if (dicUser.containsKey(pid)) {
                dicUser.get(pid).setLastResult(result);
            }
        }
    }

    @Override
    public Long UpdateMarkTemp(int pid) {
        synchronized (dicUser) {
            if (dicUser.containsKey(pid)) {
                dicUser.get(pid).setAS(dicUser.get(pid).getAG());
                dicUser.get(pid).setAG(3000l);
                return 5000l;
            }
        }
        return 0l;
    }

    @Override
    public void RestoreUser(int pid) {
        synchronized (dicUser) {
            if (dicUser.containsKey(pid)) {
                //System.out.println("===>AS Ra:"+dicUser.get(pid).getAS());
                dicUser.get(pid).setAG(dicUser.get(pid).getAS());
                //dicUser.get(pid).setAS(5000l) ;
            }
        }
    }

    @Override
    public void UpdateJackpot(int mark, short source) {
        try {
            jackpotHandler.updateJackpot(userController, mark, source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int GetJackpotWin(int markUnit, int vip, String username, int source, int userid, int avatar, int diamondType) {
        try {
            return jackpotHandler.getJackpotWin(this, serviceRouter, userController, markUnit, vip, username, source, userid, avatar, diamondType);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //    int GetJackpotWin(int markUnit, int vip) ;
    @Override
    public Long UpdateMarkChessById(int uid, int mark, int gameid) { //type = 1 ==> Khong up date DB
        synchronized (dicUser) {
            if (dicUser.containsKey(uid)) {
                try {
                    long ret = -1l;
//                    System.out.println("==>UpdateMarkChessById1:" + dicUser.get(uid).getUsername() + "-" + dicUser.get(uid).getSource());
                    int source = dicUser.get(uid).getSource();
                    ret = userController.UpdateAG(source, uid - ServerDefined.userMap.get(source), mark, true);
                    if (ret >= 0) {
                        dicUser.get(uid).setAG(ret);
                        if (dicUser.get(uid).getSource() != ServerSource.THAI_SOURCE) {
                            double gameNo = 0;
                            double gameAmount = 0;
                            if (gameid == 8006 || gameid == 8007) { //Tala + chan
                                gameNo = 5;
                                if (mark > 0) {
                                    gameAmount = mark * 5;
                                } else {
                                    gameAmount = (0 - mark) * 1.5;
                                }
                            } else if (gameid == 8004) { //Binh
                                gameNo = 3;
                                if (mark > 0) {
                                    gameAmount = mark * 4;
                                } else {
                                    gameAmount = (0 - mark) * 1.2;
                                }
                            } else if (gameid == 8005 || gameid == 8012) { //Tien len + Sam
                                gameNo = 1;
                                if (mark > 0) {
                                    gameAmount = mark * 2;
                                } else {
                                    gameAmount = (0 - mark) * 0.6;
                                }
                            } else if (gameid == 8003 || gameid == 8008) { //Xi to + Poker
                                gameNo = 1;
                                if (mark > 0) {
                                    gameAmount = mark;
                                } else {
                                    gameAmount = (0 - mark) * 0.3;
                                }
                            } else if (gameid == 8010) { //Lieng
                                gameNo = 0.1;
                                if (mark > 0) {
                                    gameAmount = mark;
                                } else {
                                    gameAmount = (0 - mark) * 0.3;
                                }
                            } else {
                                gameNo = 0.1;
                                if (mark > 0) {
                                    gameAmount = mark * 0.1;
                                } else {
                                    gameAmount = (0 - mark) * 0.03;
                                }
                            }
                            dicUser.get(uid).setGameNo(dicUser.get(uid).getGameNo() + gameNo);
                            dicUser.get(uid).setGameAmount(dicUser.get(uid).getGameAmount() + gameAmount);
                            userController.UpdateGameCountCache(source, uid - ServerDefined.userMap.get(source), 0, gameNo, gameAmount);
                        }
                    }
                    return dicUser.get(uid).getAG();
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.out.println("ERROR UpdateMarkChessById int:" + e.getMessage() + "-" + uid + "-" + mark);
                }
            } else {
                System.out.println("==>UpdateMarkChessById: uid - " + uid + " - mark: " + mark + " - gameid: " + gameid);
            }
        }
        return 0l;
    }

    @Override
    public long UpdateDiamondById(int uid, long mark, int gameid) { //
        synchronized (dicUser) {
            if (dicUser.containsKey(uid)) {
                try {
                    long ret = -1l;
//                    System.out.println("==>UpdateDiamondById:" + dicUser.get(uid).getUsername() + "-" + dicUser.get(uid).getSource());
                    int source = dicUser.get(uid).getSource();
                    ret = userController.UpdateDiamond(source, uid - ServerDefined.userMap.get(source), mark);
                    if (ret >= 0) {
                        dicUser.get(uid).setDiamond(ret);
                    }
                    return dicUser.get(uid).getDiamond();
                } catch (Exception e) {
//                    System.out.println("ERROR UpdateDiamondById int:" + e.getMessage() + "-" + uid + "-" + mark);
                    e.printStackTrace();
                }
            }
        }
        return 0l;
    }

    @Override
    public int UpdateMarkForHighLow(int uid, int mark, int n, int gameid) { //Update Highlow khi dang danh
        synchronized (dicUser) {
            if (dicUser.containsKey(uid)) {
                try {
                    taixiuHandler.bet(dicUser.get(uid), n, mark, gameid, (int) dicUser.get(uid).getSource());
                } catch (Exception e) {
                    //handle exception
                    e.printStackTrace();
                }
                return dicUser.get(uid).getAG().intValue();
            } else {
                return -2;
            }
        }
    }

    @Override
    public Long UpdateMarkChessById(int uid, long mark, int gameid) { //type = 1 ==> Khong up date DB ==> dang duoc su dung la chinh
        synchronized (dicUser) {
            if (dicUser.containsKey(uid)) {
                try {
                    long ret = -1l;
                    int source = dicUser.get(uid).getSource();
                    ret = userController.UpdateAG(source, uid - ServerDefined.userMap.get(source), mark, true);
                    //Cap nhat so tien thang de cho vao tich luy
                    if (mark > 0) { //Update Gold Win for Accumulation
                        // Cap nhat thoi
                        dicUser.get(uid).setWinAccumulation(dicUser.get(uid).getWinAccumulation() + mark);
                    }

                    if (ret >= 0) {
                        dicUser.get(uid).setAG(ret);
                    }
                    return dicUser.get(uid).getAG();
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.out.println("ERROR UpdateMarkChessById:" + e.getMessage() + "-" + uid + "-" + mark);
                }
            } else {
                System.out.println("==>UpdateMarkChessById: uid - " + uid + " - mark: " + mark + " - gameid: " + gameid);
            }
        }
        return 0l;
    }

    @Override
    public Long UpdateMarkChessById(int uid, long mark, int gameid, int markUnit) { //type = 1 ==> Khong up date DB ==> dang duoc su dung la chinh
        synchronized (dicUser) {
            if (dicUser.containsKey(uid)) {
                try {
                    long ret = -1l;
                    int source = dicUser.get(uid).getSource();
                    ret = userController.UpdateAG(source, uid - ServerDefined.userMap.get(source), mark, true);
                    //Cap nhat so tien thang de cho vao tich luy
                    if (mark > 0) { //Update Gold Win for Accumulation
                        dicUser.get(uid).setWinAccumulation(dicUser.get(uid).getWinAccumulation() + mark);
                    }

                    if (ret >= 0) {
                        dicUser.get(uid).setAG(ret);
                    }
                    return dicUser.get(uid).getAG();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("==>UpdateMarkChessById: uid - " + uid + " - mark: " + mark + " - gameid: " + gameid);
            }
        }
        return 0l;
    }

    @Override
    public void updateFinishGame(int uid, int countGame, int source) {
        synchronized (dicUser) {
            try {
                if (dicUser.containsKey(uid)) {
                    dicUser.get(uid).setGameNo(dicUser.get(uid).getGameNo() + countGame);
                    dicUser.get(uid).setGameCount(dicUser.get(uid).getGameCount() + countGame);
                    userController.UpdateGameCountCache(source, uid - ServerDefined.userMap.get(source), 1, 0, countGame);
                    promotionHandler.processPromotionInviteFaceB(source, uid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Long UpdateRouletteMutil(int uid, String username, int numberwin, int lqbuy, int totallq, String[] arr, int agwin, long ag) {
        synchronized (dicUser) {
            if (dicUser.containsKey(uid)) {
                try {
                    Roulette rl = new Roulette();
                    rl.setNumberWin(numberwin);
                    rl.setLQBuy(lqbuy);
                    rl.setLQTotal(totallq);
                    rl.setArr(arr);
                    rl.setAgWin(agwin);
                    rl.setBuy(true);
                    long ret = 0;
//					do{
                    int source = dicUser.get(uid).getSource();
                    ret = userController.UpdateRouletteMultiDb(source, uid - ServerDefined.userMap.get(source), username, rl);
                    userController.UpdateAGCache(source, uid - ServerDefined.userMap.get(source), 0 - totallq, dicUser.get(uid).getVIP(), 0l);
                    //ret = userController.UpdateRouletteMulti(getConnection(), uid, username, rl);
                    //System.out.println("==>Ret:" + ret) ;
//					} while(ret == 0);
                    dicUser.get(uid).DecrementLQ(totallq);
                    dicUser.get(uid).setGCountNow(dicUser.get(uid).getGCountNow() + 1);
                    return ret;
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.out.println("ERROR UpdateRouletteMutil:" + e.getMessage() + "-" + uid + "-" + agwin);
                }
            }
        }
        return 0l;
    }

    //endregion
    @Override
    public Long UpdateXocdiaDetailLong(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win) {
//        synchronized (dicUser) { //Ghi lai chi tiet van choi Xoc dia vao DB
//            if (dicUser.containsKey(Userid)) {
//                try {
//                    long ret = 0;
//                    int source = dicUser.get(Userid).getSource();
//                    ret = userController.UpdateXocdiaDetailLongValue(source, Userid - ServerDefined.userMap.get(source), TableId, StrNum, StrMark, NumBuy, MarkBuy, Dealer, Result, Win);
//                    return ret;
//                } catch (Exception e) {
//                    System.out.println("ERROR UpdateXocdiaDetail:" + e.getMessage() + "-" + Userid + "-" + Win + "-" + Result);
//                    e.printStackTrace();
//                }
//            }
//        }
        return 0l;
    }

    @Override
    public Long UpdateXocdiaDetail(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, int Win) {
//        synchronized (dicUser) { //Ghi lai chi tiet van choi Xoc dia vao DB
//            if (dicUser.containsKey(Userid)) {
//                try {
//                    long ret = 0;
//                    int source = dicUser.get(Userid).getSource();
//                    ret = userController.UpdateXocdiaDetailLongValue(source, Userid - ServerDefined.userMap.get(source), TableId, StrNum, StrMark, NumBuy, MarkBuy, Dealer, Result, Win);
//                    return ret;
//                } catch (Exception e) {
//                    System.out.println("ERROR UpdateXocdiaDetail:" + e.getMessage() + "-" + Userid + "-" + Win + "-" + Result);
//                    e.printStackTrace();
//                }
//            }
//        }
        return 0l;
    }

    @Override
    public Long UpdateXocdiaDetail(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, int Result, int Win) {
        return 0l;
    }

    @Override
    public Long UpdateXocdiaDetailLongUsing(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, long MarkBuy, int Dealer, int Result, long Win, int gameId) {
//        synchronized (dicUser) { //Ghi lai chi tiet van choi Xoc dia vao DB
//            if (dicUser.containsKey(Userid)) {
//                try {
//                    long ret = 0;
//                    int source = dicUser.get(Userid).getSource();
//                    ret = userController.UpdateXocdiaDetailLong_Using(source, Userid - ServerDefined.userMap.get(source), TableId, StrNum, StrMark, NumBuy, MarkBuy, Dealer, Result, Win, gameId);
//                    return ret;
//                } catch (Exception e) {
//                    System.out.println("ERROR UpdateXocdiaDetail:" + e.getMessage() + "-" + Userid + "-" + Win + "-" + Result);
//                    e.printStackTrace();
//                }
//            }
//        }
        return 0l;
    }

    @Override
    public void UpdateHiloDetail(int Userid, int TableId, String StrNum, String StrMark, int NumBuy, int MarkBuy, int Dealer, String Result, int Win, int GameId) {
//        synchronized (dicUser) { //Ghi lai chi tiet van choi Xoc dia vao DB
//            if (dicUser.containsKey(Userid)) {
//            	try {
////                  long ret = 0;
//                  int source = dicUser.get(Userid).getSource();
////                  ret = 
//                  userController.UpdateHiloDetail(source, Userid - ServerDefined.userMap.get(source), TableId, StrNum, StrMark, NumBuy, MarkBuy, Dealer, Result, Win, GameId);
//                } catch (Exception e) {
//                    System.out.println("ERROR UpdateXocdiaDetail:" + e.getMessage() + "-" + Userid + "-" + Win + "-" + Result);
//                    e.printStackTrace();
//                }
//            }
//        }
//        return 0l;
    }

    @Override
    public void LogTable(int iLevel, int iRevenue, java.sql.Date dtTime, int gameid, int source) {
        try {

            userController.GameLogIUGameRevenue(source, iLevel, iRevenue, gameid, dtTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void LogTable(int iLevel, long iRevenue, java.sql.Date dtTime, int gameid, int source) {
        try {
            userController.GameLogIUGameRevenue(source, iLevel, iRevenue, gameid, dtTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void LogTableLong(long mark, long revenue, Date time, int gameid, int source) {
        try {
            dbCommand.GameLogRevenue(source, mark, revenue, gameid, time);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void LogPlayer(int userid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, int source, int iWinMark, int deviceid) {
        try {
            //System.out.println("==>GameLogIUUserExperienceDtDb: " +GameId +" LogPlayer "+userid+" - source = "+source);
            //if(source == ServerDefined.C3_SOURCE){

            //}else
            userController.GameLogIUUserExperienceDt(source, userid - ServerDefined.userMap.get(source), GameId, iLevel, iWin, dtTime, iWinMark, deviceid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void LogPlayer(int userid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, int source, long iWinMark, int deviceid) {
        try {
            userController.GameLogIUUserExperienceDt(source, userid - ServerDefined.userMap.get(source), GameId, iLevel, iWin, dtTime, iWinMark, deviceid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void LogPlayerLong(int userid, int gameid, long mark, int win, Date dtTime, int source, long markWin, int operator) {
        try {
            dbCommand.GameLogExperience(source, userid - ServerDefined.userMap.get(source), gameid, mark, win, dtTime, markWin, operator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LogPlayer(int userid, int GameId, int iLevel, int iWin, java.sql.Date dtTime, int source, int iWinMark, int deviceid, int diamondType) {
        try {
            //System.out.println("==>GameLogIUUserExperienceDtDb: " +GameId +" LogPlayer "+userid+" - source = "+source);
            //if(source == ServerDefined.C3_SOURCE){

            //}else
            userController.GameLogIUUserExperienceDt_New(source, userid - ServerDefined.userMap.get(source), GameId, iLevel, iWin, dtTime, iWinMark, deviceid, diamondType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //region Logout or Disconnected
    @Override
    public void clientLoggedIn(int playerId, String screenName) {
        PublicClientRegistryService clientRegistryService = context.getParentRegistry().getServiceInstance(PublicClientRegistryService.class);
        loggerLogin_.info("==>Client Login Start: " + screenName + " - pid: " + playerId + "-" + clientRegistryService.getClientStatus(playerId)
                + "-" + clientRegistryService.getRemoteAddress(playerId));
    }

    @Override
    public void clientDisconnected(int playerId) {
        try {
            loggerLogin_.info("==>ClientDisconnectStart:" + playerId);
            try {
                if (dicUser.containsKey(playerId)) {
                    String idzing = dicUser.get(playerId).getUserIdZing();
                    if (dicCurrent.containsKey(idzing)) {
                        loggerLogin_.info("==>Client Disconnect End:" + playerId + "-" + idzing);
                        return;
                    }
                }
                PublicClientRegistryService clientRegistryService = context.getParentRegistry().getServiceInstance(PublicClientRegistryService.class);
                loggerLogin_.info("==>Client Disconnect Start: " + playerId + "-" + clientRegistryService.getClientStatus(playerId) + "-" + clientRegistryService.getRemoteAddress(playerId));
            } catch (Exception e) {
                e.printStackTrace();
            }

//        	ClientRegistry clientRegistry = ((ClientRegistryServiceContract) context.getParentRegistry().getServiceInstance(ClientRegistryServiceContract.class)).getClientRegistry();
//            String sessionId = clientRegistry.getClient(playerId).getSessionId();
//            loggerLogin_.info("==>Client Disconnect:" + playerId + "\t sessionId:" + sessionId);
//        	if (!clientRegistryService.getClientStatus(playerId).equals("CONNECTED"))
//        	{
            boolean checkUser = true;
            int source = 0;
            synchronized (dicUser) {
                if (dicUser.containsKey(playerId)) {
                    dicUser.get(playerId).setDisconnect(true);
                    source = (int) dicUser.get(playerId).getSource();
                    checkUser = false;
                }
            }
            loggerLogin_.info("==>Client Disconnect: " + playerId + " - checkUser: " + checkUser);
//                System.out.println("==>clientDisconnected: checkUser - "+checkUser+" - IsRunBotBinh: "+IsRunBotBinh);
            if (checkUser) {
                botHandler.processBotDisconnect(playerId);
            }// else if(IsRunServerTaiXiu)
            //	PlayerDisconnectedServerTaiXiu(playerId, source);
            else {
                PlayerDisconnected(playerId, source);
            }
//        	}
        } catch (Exception e) {
            // handle exception
            loggerLogin_.error("==>Client Disconnect Error:" + playerId);
            e.printStackTrace();
        }
//    	System.out.println("==>Status:" + clientRegistryService.getClientStatus(playerId));
    }

    @Override
    public void clientLoggedOut(int playerId) {
        PublicClientRegistryService clientRegistryService = context.getParentRegistry().getServiceInstance(PublicClientRegistryService.class);
        loggerLogin_.info("==>Client Logout: " + playerId + "-" + clientRegistryService.getClientStatus(playerId));
    }

    @Override
    public void sendSpecialAlert(int pid, String evt, String data, String name, int source) {
        try {
            //System.out.println("==>Special:" + data) ;
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "salert");
            jo.addProperty("data", data);
            jo.addProperty("name", name);
            ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayers(getArrPidByGame(0, 0, source), csa);
            //serviceRouter.dispatchToPlayers(getArrPidBySourceGame(source,0), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendSpecialAlert_Xocdia(int pid, String evt, String data, String name, int source) {
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", "salert");
            jo.addProperty("data", data);
            jo.addProperty("name", name);
            ClientServiceAction csa = new ClientServiceAction(0, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));

            serviceRouter.dispatchToPlayers(getArrPidByGame(0, 0, source), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void AutoJoinTable(int pid, int tableid, int gameid) {
//        System.out.println("==>ServiceImpl==>AutoJoinTable: pid-" + pid + " tableid-" + tableid + " gameid-" + gameid);
        if (dicUser.containsKey(pid)) {
            System.out.println("==>ServiceImpl==>AutoJoinTable: pid" + dicUser.get(pid).getPid() + " - " + dicUser.get(pid).getGameid() + " - tableid " + dicUser.get(pid).getTableId());
        }
        if (tableid != 0) {
            JoinRequestAction action = new JoinRequestAction(pid, tableid, -1, "");
            serviceRouter.dispatchToGame(gameid, action);
        }
    }

    @Override
    public void AutoLeftTable(int pid, int tableid, int gameid) {
        if (tableid != 0) {
            LeaveAction action = new LeaveAction(pid, tableid);
            serviceRouter.dispatchToGame(gameid, action);
        }
        if (!dicUser.containsKey(pid)) {
            botHandler.updateAvailablebot(pid);
        }
    }

    /**
     * **************** Join + Left Table **************************
     */
    public void ActionJoinTable(int pid, int tableid, int gameid) {
        DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
        if (!dos.allow("joinSpamRule", pid)) {
            return;
        }
        JoinRequestAction action = new JoinRequestAction(pid, tableid, -1, "");
        serviceRouter.dispatchToGame(gameid, action);
    }

    public void ActionLeftTable(int pid, int tableid, int gameid) {
        DosProtector dos = context.getParentRegistry().getServiceInstance(DosProtector.class);
        if (!dos.allow("leftSpamRule", pid)) {
            return;
        }
        LeaveAction action = new LeaveAction(pid, tableid);
        serviceRouter.dispatchToGame(gameid, action);
    }

    public ServiceRouter getServiceRouter() {
        return serviceRouter;
    }

    public void setServiceRouter(ServiceRouter serviceRouter) {
        ServiceImpl.serviceRouter = serviceRouter;
    }

    @Override
    public void AutoInvite(int pid, int tableid, int ag, String name, int agU) {
        try {
            if (tableid != 0) {
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "ivp");
                jo.addProperty("N", name);
                jo.addProperty("AG", ag);
                jo.addProperty("T", "1");
                jo.addProperty("TID", tableid);
                jo.addProperty("AGU", agU);
                ClientServiceAction csa = new ClientServiceAction(pid, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
                serviceRouter.dispatchToPlayer(pid, csa);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendErrorMsg(int pid, String data) {
        try {
            JSent act = new JSent();
            act.setEvt("10");
            act.setCmd(data);
            ClientServiceAction csa = new ClientServiceAction(pid, 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(pid, csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendToClient(int pid, String evt, String data) {
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", evt);
            jo.addProperty("data", data);
            ClientServiceAction csa = new ClientServiceAction(pid, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(pid, csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendToClientJsonData(int pid, String evt, String data, JsonObject json) {
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("evt", evt);
            jo.addProperty("data", data);
            jo.add("json", json);
            ClientServiceAction csa = new ClientServiceAction(pid, 1, ActionUtils.gson.toJson(jo).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(pid, csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processRoom(JsonObject json) {
        try {
            actionHandler.getRoom().processRoomAction(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getLuckyPercent(int source, int userid, int vip, int gameid) {
        try {
            for (int i = 0; i < listUserLucky.size(); i++) { //Lay theo chi tiet userid
                if (listUserLucky.get(i).getUserid() == userid - ServerDefined.userMap.get(source)
                        && listUserLucky.get(i).getGameid() == gameid) {
                    return listUserLucky.get(i).getLuckyPercent();
                }
            }
            for (int i = 0; i < listUserLucky.size(); i++) { //Lay theo Vip
                if (listUserLucky.get(i).getVip() == vip && listUserLucky.get(i).getUserid() == 0
                        && listUserLucky.get(i).getGameid() == gameid) {
                    return listUserLucky.get(i).getLuckyPercent();
                }
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getLuckyPercent(int source, int userid, int vip, int gameid, int ag) {
        try {
            long currentTime = System.currentTimeMillis();
            for (int i = 0; i < listUserLucky.size(); i++) { //Lay theo chi tiet userid
                if (listUserLucky.get(i).getUserid() == userid - ServerDefined.userMap.get(source)
                        && listUserLucky.get(i).getGameid() == gameid
                        && listUserLucky.get(i).getTimestart() < currentTime && currentTime < listUserLucky.get(i).getTimeend()) {
                    if (listUserLucky.get(i).getAgmax() == 0) {
                        return listUserLucky.get(i).getLuckyPercent();
                    } else if (listUserLucky.get(i).getAgmin() <= ag && ag <= listUserLucky.get(i).getAgmax()) {
                        return listUserLucky.get(i).getLuckyPercent();
                    }
                }
            }
            for (int i = 0; i < listUserLucky.size(); i++) { //Lay theo Vip
                if (listUserLucky.get(i).getVip() == vip && listUserLucky.get(i).getUserid() == 0
                        && listUserLucky.get(i).getGameid() == gameid
                        && listUserLucky.get(i).getTimestart() < currentTime && currentTime < listUserLucky.get(i).getTimeend()) {
                    return listUserLucky.get(i).getLuckyPercent();
                }
            }
            for (int i = 0; i < listUserLucky.size(); i++) { //Lay theo AG
                if (listUserLucky.get(i).getAgmin() <= ag && ag <= listUserLucky.get(i).getAgmax() && listUserLucky.get(i).getAgmax() != 0
                        && listUserLucky.get(i).getUserid() == 0
                        && listUserLucky.get(i).getGameid() == gameid
                        && listUserLucky.get(i).getTimestart() < currentTime && currentTime < listUserLucky.get(i).getTimeend()) {
                    return listUserLucky.get(i).getLuckyPercent();
                }
            }
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long ChipAfterTaxforGame(long ag, int vip, int source, int gameid, int currentGold, int currentMarkUnit, int countMsg) {
        try {
            if (gameid == 8004) { //Capsa sunsun ==> Indo
                if (vip > 4) {
                    return (ag * 98) / 100;
                } else {
                    return (ag * 97) / 100;
                }
            } else if (gameid == 8021) { //Dummy
                int percent = 93;
                if (vip == 10) {
                    percent = 97; // return mark * 97/100;
                } else if (vip == 9) {
                    percent = 96; //return mark * 96/100;
                } else if (vip == 8) {
                    percent = 95; //return mark * 95/100;
                } else if (vip == 6 || vip == 5 || vip == 7) {
                    percent = 94; //return mark * 94/100;
                } else if (vip == 4 || vip == 3 || vip == 2 || vip == 1 || vip == 0) {
                    percent = 93; //return mark * 93/100;
                } else {
                    percent = 93; //return mark * 93/100;
                }
                if (currentGold < currentMarkUnit * 500) {
                    percent = percent - 3;
                    if (countMsg > 100) {
                        percent = percent - 10;
                    }
                } else if (currentGold < currentMarkUnit * 750) {
                    percent = percent - 2;
                    if (countMsg > 100) {
                        percent = percent - 5;
                    }
                } else if (currentGold < currentMarkUnit * 1000) {
                    percent = percent - 1;
                    if (countMsg > 100) {
                        percent = percent - 2;
                    }
                }
                return (ag * percent) / 100;
            } else if (gameid == 8024) { //Dummy  thai
                int percent = 93;
                if (vip == 10) {
                    percent = 97; // return mark * 97/100;
                } else if (vip == 9) {
                    percent = 96; //return mark * 96/100;
                } else if (vip == 8) {
                    percent = 95; //return mark * 95/100;
                } else if (vip == 6 || vip == 5 || vip == 7) {
                    percent = 94; //return mark * 94/100;
                } else if (vip == 4 || vip == 3 || vip == 2 || vip == 1 || vip == 0) {
                    percent = 93; //return mark * 93/100;
                } else {
                    percent = 93; //return mark * 93/100;
                }
                if (currentGold < currentMarkUnit * 500) {
                    percent = percent - 2;
                    if (countMsg > 100) {
                        percent = percent - 15;
                    }
                } else if (currentGold < currentMarkUnit * 750) {
                    percent = percent - 1;
                    if (countMsg > 100) {
                        percent = percent - 10;
                    }
                } else if (currentGold < currentMarkUnit * 1000) {
                    if (countMsg > 100) {
                        percent = percent - 5;
                    }
                }
                return (ag * percent) / 100;
            } else if (gameid == 8013 || gameid == 8022 || gameid == 8027 || gameid == 8037) { //Xocdia || Hilo || Minidice || Hiloduong pho
                if (vip > 5) {
                    return (ag * 98) / 100;
                } else if (vip == 5 || vip == 4) {
                    return (ag * 97) / 100;
                } else if (vip == 3) {
                    return (ag * 96) / 100;
                } else {
                    return (ag * 95) / 100;
                }
            } else {
                if (vip == 10) {
                    return (ag * 97) / 100;
                } else if (vip == 9) {
                    return (ag * 96) / 100;
                } else if (vip == 8) {
                    return (ag * 95) / 100;
                } else if ((vip < 8) && (vip > 4)) {
                    return (ag * 94) / 100;
                } else {
                    return (ag * 93) / 100;
                }
            }
        } catch (Exception e) {
            // handle exception
            loggerLogin_.error("==>Error==>S==>CheckPromotion:" + e.getMessage());
            return (ag * 95) / 100;
        }
    }

    @Override
    public boolean CheckPromotion(long ag, int vip, int source) {
        try {
            if (ag < 2500) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void PromotionByUidNew(int pid) { //Tang tien siam theo kieu moi
        try {
            int soAGtang = 0;
            Logger.getLogger("PromotionHandler").info("==>Tang tien kieu moi:" + pid + "-" + dicUser.get(pid).getUsername() + "-" + dicUser.get(pid).getCPromot() + "-" + countPromote(dicUser.get(pid).getVIP(), dicUser.get(pid).getSource(), dicUser.get(pid).getUsertype()) + "-" + dicUser.get(pid).getCPromot());
            int source = dicUser.get(pid).getSource();
            if (!promotionHandler.CheckPromotion(source, pid - ServerDefined.userMap.get(source))) {
                return;
            }
            Logger.getLogger("PromotionHandler").info("==>Tang tien kieu moi ==> Tang thoi:" + pid + "-" + countPromote(dicUser.get(pid).getVIP(), source, dicUser.get(pid).getUsertype()) + "-" + dicUser.get(pid).getCPromot());
            int maxPromotion = countPromote(dicUser.get(pid).getVIP(), source, dicUser.get(pid).getUsertype());
            if (dicUser.get(pid).getCreateTime().longValue() > ActionUtils.getFirstTimeOfDate() && dicUser.get(pid).getSource() == ServerSource.THAI_SOURCE) {
                maxPromotion = 3;
            }
            if (dicUser.get(pid).getCPromot() < maxPromotion) {
                soAGtang = promotionForDT3C52(dicUser.get(pid).getCPromot(), dicUser.get(pid).getPromotionValue(), dicUser.get(pid).getVIP(), source, dicUser.get(pid).getCMsg());
            }
            Logger.getLogger("PromotionHandler").info("==>AG tang:" + pid + "-" + soAGtang);
            if (soAGtang > 0) {
                promotionHandler.CreatePromotion(source, pid - ServerDefined.userMap.get(source), PromotionType.TYPE_NOT_ENOUGH_GOLD, soAGtang, dicUser.get(pid).getDeviceId());
                userController.UpdateCPromotionToCache(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), 1);
                userController.UpdateCPromotionDeviceToCache(source, dicUser.get(pid).getDeviceId(), 1);
                dicUser.get(pid).setCPromot(dicUser.get(pid).getCPromot() + 1);
            }/* else if (dicUser.get(pid).getSource() == 9) {
    			loggerLogin_.info("==>PromotionTomorrow Siam:" + source + "-" + dicUser.get(pid).getUserid() + "-" + dicUser.get(pid).getUnlockPass() + "-" + ActionUtils.getFirstTimeOfDate() + "-" + dicUser.get(pid).getCreateTime().longValue());
            	if (dicUser.get(pid).getUnlockPass() != 2 && (dicUser.get(pid).getCreateTime().longValue()>ActionUtils.getFirstTimeOfDate())) { //Thong bao
            		//Tang tien
            		soAGtang = promotionForTomorrow_ForSiam((int)dicUser.get(pid).getVIP(), source, dicUser.get(pid).getFacebookid().longValue());
            		int error = userController.GameITempPromoteTomorrowDb(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), soAGtang, dicUser.get(pid).getDeviceId());
            		if (error > 0) {
            			sendErrorMsg(dicUser.get(pid).getPid(), "กลับมาเล่นเกมต่อในวันพรุ่งนี้เพื่อจะได้รับ " + soAGtang+ " ชิปจากสยสมเพยล์");
            		}
            		userController.UpdatePromotionTomorrowToCache(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source)) ;
        			dicUser.get(pid).setUnlockPass((short)2);
            	}
    		}*/
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
    }

    @Override
    public Long PromotionByUid(int pid, boolean isAddAG) {
        synchronized (dicUser) {
            try {
                if (dicUser.containsKey(pid)) {
                    if (CheckPromotion(dicUser.get(pid).getAG(), dicUser.get(pid).getVIP(), dicUser.get(pid).getSource())) { //Check tien duoi muc can tang
                        if (dicUser.get(pid).getOperatorid() == Operator.OPERATOR_MYANMAR) {
                            PromotionByUidNew(pid);
                            return 0l;
                        }
                        if (dicUser.get(pid).getOperatorid() == Operator.OPERATOR_3C1 || dicUser.get(pid).getOperatorid() == Operator.OPERATOR_THAI1
                                || dicUser.get(pid).getOperatorid() == Operator.OPERATOR_THAI2
                                || dicUser.get(pid).getOperatorid() == Operator.OPERATOR_THAI3 || dicUser.get(pid).getOperatorid() == Operator.OPERATOR_THAI4
                                || dicUser.get(pid).getOperatorid() == Operator.OPERATOR_INDO || dicUser.get(pid).getOperatorid() == Operator.OPERATOR_INDO1
                                || dicUser.get(pid).getOperatorid() == Operator.OPERATOR_INDIA) { //Tang tien theo kieu moi
                            PromotionByUidNew(pid);
                            return 0l;
                        }
                        int soAGtang = 0;
                        if (dicUser.get(pid).getCPromot() < countPromote(dicUser.get(pid).getVIP(), dicUser.get(pid).getSource(), dicUser.get(pid).getUsertype())) {
                            if ((dicUser.get(pid).getSource() == 2) || (dicUser.get(pid).getSource() == 3) || (dicUser.get(pid).getSource() == 4)) {
                                if (dicUser.get(pid).getVIP() <= 1) {
                                    soAGtang = 1500; //promotionByVIP(dicUser.get(pid).getVIP(), new Date(dicUser.get(pid).getCreateTime()), dicUser.get(pid).getGameCount(), dicUser.get(pid).getCPromot(), dicUser.get(pid).getSource(), dicUser.get(pid).getGameCount());
                                } else {
                                    soAGtang = promotionForDT3C52(dicUser.get(pid).getCPromot(), dicUser.get(pid).getPromotionValue(), dicUser.get(pid).getVIP(), (int) dicUser.get(pid).getSource(), dicUser.get(pid).getCMsg());
                                }
                            } else {
                                soAGtang = promotionForDT3C52(dicUser.get(pid).getCPromot(), dicUser.get(pid).getPromotionValue(), dicUser.get(pid).getVIP(), (int) dicUser.get(pid).getSource(), dicUser.get(pid).getCMsg());
                            }
                        }
                        Logger.getLogger("PromotionHandler").info("AG tang:" + soAGtang + "-" + pid);
                        int error = -1;
                        int source = dicUser.get(pid).getSource();
                        if (soAGtang > 0) {
                            error = userController.GameITempPromoteDtDb(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), soAGtang, 0, 0, dicUser.get(pid).getDeviceId());
//                            if (soAGtang >= 2000000) {
//                                System.out.println("==>Error==>Tang tien roi:" + soAGtang + "-" + dicUser.get(pid).getUsername() + "-" + pid);
//                            }
                            if (error == 0) {
                                dicUser.get(pid).setAG(dicUser.get(pid).getAG() + soAGtang);
                                dicUser.get(pid).setCPromot(dicUser.get(pid).getCPromot() + 1);
                                if (source == ServerSource.THAI_SOURCE) { //Thai    
                                    userController.UpdateAGCache(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), soAGtang, dicUser.get(pid).getVIP(), 0l);
                                    userController.UpdateCPromotionToCache(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), 1);
                                    //Update new 18/08/2016
                                    userController.UpdateCPromotionDeviceToCache(source, dicUser.get(pid).getDeviceId(), 1);
                                    //Ghi Log Khuyen mai
                                    Logger.getLogger("KHUYENMAILOG").info(String.valueOf(dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source)) + "#" + dicUser.get(pid).getAG().intValue() + "#" + dicUser.get(pid).getGameid() + "#0#0#" + String.valueOf(soAGtang) + "#" + String.valueOf((new Date()).getTime()));
                                } else { //DT
                                    userController.UpdateAGCache(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), soAGtang, dicUser.get(pid).getVIP(), 0l);
                                    userController.UpdateCPromotionToCache(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), 1);
                                    userController.UpdateCPromotionDeviceToCache(source, dicUser.get(pid).getDeviceId(), 1);
                                }
                                if (isAddAG) {
                                    sendToClient(dicUser.get(pid).getPid(), "7", String.valueOf(soAGtang));
                                }
                                return Long.parseLong(String.valueOf(soAGtang));
                            }
                        } else {
                            if (source == 4 || source == 3 || source == 2) { //Tang tien + thong bao tang tien neu chua tang
                                loggerLogin_.info("==>PromotionTomorrow:" + source + "-" + dicUser.get(pid).getUserid() + "-" + dicUser.get(pid).getUnlockPass());
                                if (dicUser.get(pid).getUnlockPass() != 2) { //Thong bao
                                    //Tang tien
                                    soAGtang = promotionForTomorrow((int) dicUser.get(pid).getVIP(), source);
                                    error = userController.GameITempPromoteTomorrowDb(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), soAGtang, dicUser.get(pid).getDeviceId());
                                    if (error > 0) {
                                        sendErrorMsg(dicUser.get(pid).getPid(), soAGtang + " Gold là số gold bạn được hệ thống tặng vào ngày mai.");
                                    }
                                    userController.UpdatePromotionTomorrowToCache(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source));
                                    dicUser.get(pid).setUnlockPass((short) 2);
                                }
                            } else { //Hoki + Siam
                                loggerLogin_.info("==>PromotionTomorrow Siam:" + source + "-" + dicUser.get(pid).getUserid() + "-" + dicUser.get(pid).getUnlockPass() + "-" + ActionUtils.getFirstTimeOfDate() + "-" + dicUser.get(pid).getCreateTime().longValue());
                                if (dicUser.get(pid).getUnlockPass() != 2 && (dicUser.get(pid).getCreateTime().longValue() > ActionUtils.getFirstTimeOfDate())) { //Thong bao
                                    //Tang tien
                                    soAGtang = promotionForTomorrow_ForSiam((int) dicUser.get(pid).getVIP(), source, dicUser.get(pid).getFacebookid().longValue());
                                    error = userController.GameITempPromoteTomorrowDb(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source), soAGtang, dicUser.get(pid).getDeviceId());
                                    if (error > 0) {
                                        sendErrorMsg(dicUser.get(pid).getPid(),
                                                String.format(ServiceImpl.actionUtils.getConfigText("ag_promotion1", dicUser.get(pid).getSource(), dicUser.get(pid).getUserid()), soAGtang));
                                    }
                                    userController.UpdatePromotionTomorrowToCache(source, dicUser.get(pid).getUserid() - ServerDefined.userMap.get(source));
                                    dicUser.get(pid).setUnlockPass((short) 2);
                                }
                            }
                        }
                    }
                    return 0l;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0l;
    }

    public static void UpdateUserOnline(UserInfo unew) {
        synchronized (dicUser) {
            if (dicUser.containsKey(unew.getUserid())) {
                dicUser.get(unew.getUserid()).setDisconnect(false);
            }
        }
    }

    public static void UpdateUserOnline_New(UserInfo unew) {
        synchronized (dicUser) {
            if (dicUser.containsKey(unew.getUserid())) {
                dicUser.get(unew.getUserid()).setDisconnect(false);
            } else {
                loggerLogin_.info("==>AdddicinLogin" + unew.getUsername() + "-" + unew.getUserid());
                unew.setDisconnect(false);
                dicUser.put(unew.getUserid(), unew);
            }
        }
    }

    //region Function support
    public static void AddUserOnline(UserInfo unew) {
        try {
            loggerLogin_.info("==>AddUserOnline: " + ActionUtils.gson.toJson(unew) + dicUser.containsKey(unew.getUserid()));
            synchronized (dicUser) {
                if (dicUser.containsKey(unew.getUserid())) {
                    dicUser.remove(unew.getUserid());
                }
                dicUser.put(unew.getUserid(), unew);
            }
            synchronized (lsUserOnl) {
                for (int i = 0; i < lsUserOnl.size(); i++) {
                    if (lsUserOnl.get(i).getN().equals(unew.getUsername())) {
                        lsUserOnl.remove(i);
                        break;
                    }
                }
                lsUserOnl.add(new UserOnl(unew.getUsername(), unew.getPid(), unew.getGameid(), unew.getVIP(), unew.getOperatorid(), unew.getSource(), unew.getRef()));
            }

            synchronized (dicLanguage) {
                int id = unew.getUserid();
                if (id > ServerDefined.userMap.get(((int) unew.getSource()))) {
                    id = id - ServerDefined.userMap.get(((int) unew.getSource()));
                }

                dicLanguage.put(id, unew.getLanguage());
                loggerLogin_.info("Add language: " + id + " " + unew.getLanguage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int CheckUserOnline(int uid, int gid) {
        synchronized (dicUser) {
            if (dicUser.containsKey(uid)) {
                if (dicUser.get(uid).getTableId() == 0) {
                    return dicUser.get(uid).getPid();
                } else {
                    return 0;
                }
            }
        }
        return -1;
    }

    public static int GetTableIdUserOnline(int uid, int gid) {
        synchronized (dicUser) {
            if (dicUser.containsKey(uid)) {
                if (dicUser.get(uid).getGameid() == gid) {
                    return dicUser.get(uid).getTableId();
                } else {
                    return -2;
                }
            }
        }
        return -1;
    }

    /*public static int CheckUserOnline1(int uid, int gid){
     synchronized (dicUser) {
     if(dicUser.containsKey(uid)) return dicUser.get(uid).getPid();
     }
     return -1;
     }*/

 /*public void PlayerDisconnectedServerTaiXiu(int pid, int source) {
           try {
               int uid = 0;
               loggerLogin_.info("==>PlayerDisconnectedServerTaiXiu==>Remove khoi List:" + pid + "-" + source);
               synchronized (dicUser) {
                   if (dicUser.containsKey(pid)) {
                           uid = pid;
                           dicUser.remove(pid);
                       } else {
                           loggerLogin_.info("==>PlayerDisconnectedServerTaiXiu==>Update Disconnect Status:" + pid);
                           dicUser.get(pid).setDisconnect(true);
                       }
               }
               if (uid != 0) {
                       synchronized (lsUserOnl) {
                           for (int i = 0; i < lsUserOnl.size(); i++) {
                               if (lsUserOnl.get(i).getPid() == pid) {
                                   lsUserOnl.remove(i);
                                   break;
                               }
                           }
                       }
                       synchronized (dicUser) {
                           if (dicUser.containsKey(pid)) {
                               dicUser.remove(pid);
                           }
                           if (dicUser.containsKey(uid)) {
                               dicUser.remove(uid);
                           }
                       }
               } else
                   loggerLogin_.info("==>Not exists Dic:" + pid);
           } catch (Exception e) {
               // handle exception
               e.printStackTrace();
           }
       }*/
    public void PlayerDisconnected(int pid, int source) {
        long startPlayerDisconnected = System.nanoTime();
        long currentCCU = 0;
        //add try catch for handle exception when player disconnect
        try {
            int uid = 0;
            loggerLogin_.info("==>Disconnect==>Remove khoi List:" + pid + "-" + source);
            try {
                synchronized (dicUser) {
                    if (dicUser.containsKey(pid)) {
                        loggerLogin_.info("===>IDTable==>Check Remove dicUser:" + pid + "-" + dicUser.get(pid).getTableId() + "-" + dicUser.get(pid).getUsername() + "-" + source);
                        if (dicUser.get(pid).getTableId() == 0) {
                            uid = pid;
                            dicUser.remove(pid);
                        } else {//Ghi lai de do dac he thong
                            loggerLogin_.info("==>Disconnect==>Update Disconnect To Cache:" + pid);
                            dicUser.get(pid).setDisconnect(true);

                            userController.UpdateDisconnectToCache(source, pid - ServerDefined.userMap.get(source));
                        }
                    }
                    currentCCU = dicUser.size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (uid != 0) {
                int error = -1;
                loggerLogin_.info("===>DisconnectUpdateCacheAndDB:" + uid + "-" + source);
                error = userController.UserDisconnected(source, uid - ServerDefined.userMap.get(source));
                if (error == 0) {
                    synchronized (lsUserOnl) {
                        for (int i = 0; i < lsUserOnl.size(); i++) {
                            if (lsUserOnl.get(i).getPid() == pid) {
                                lsUserOnl.remove(i);
                                break;
                            }
                        }
                    }
                    synchronized (dicUser) {
                        if (dicUser.containsKey(pid)) {
                            dicUser.remove(pid);
                        }
                        if (dicUser.containsKey(uid)) {
                            dicUser.remove(uid);
                        }
                    }
                    uid = 0;
                }
            } else {
                loggerLogin_.info("==>Not exists Dic:" + pid);
            }
        } catch (Exception e) {
            Logger.getLogger("Debug_service").error(e.getMessage(), e);
            e.printStackTrace();
        } finally {
            long endTimeDisnnected = System.nanoTime();

            LoginLoggingModel loginLoggingModel = new LoginLoggingModel(
                    "DISCONNECT",
                    pid,
                    currentCCU,
                    startPlayerDisconnected,
                    endTimeDisnnected
            );

            Logger.getLogger("Monitor_loggingandDisconnect").info(loginLoggingModel);
        }
    }

    private void loadMailSystemNew(int pid, int type) {
        dicUser.get(pid).getListMsg().clear();
        int source = dicUser.get(pid).getSource();
        dicUser.get(pid).getListMsg().addAll(userController.GameGetListUserMsgNew(source, dicUser.get(pid).getUsernameOld(), dicUser.get(pid).getUsername(), type));
    }

    private void loadMailTransferAGNew(int pid, int type) {
        dicUser.get(pid).getListMsgAG().clear();
        int source = dicUser.get(pid).getSource();
        dicUser.get(pid).getListMsgAG().addAll(userController.GameGetListUserMsgNew(source, dicUser.get(pid).getUsernameOld(), dicUser.get(pid).getUsername(), type));
    }

    private void loadMailPlayerNew(int pid, int type) {
        dicUser.get(pid).getListMsgPlayer().clear();
        int source = dicUser.get(pid).getSource();
        dicUser.get(pid).getListMsgPlayer().addAll(userController.GameGetListUserMsgNew(source, dicUser.get(pid).getUsernameOld(), dicUser.get(pid).getUsername(), type));
    }

    private short countPromote(int vip, int source, short loainick) {
        if (source == ServerSource.IND_SOURCE) {
            return 3;
        }

        if (source == ServerSource.THAI_SOURCE || source == ServerSource.INDIA_SOURCE) { //Siam
            return (short) ServerDefined.promotion_policy.get(source).get(vip).getNumberP();
        } else {
            return 1;
        }
    }

    //Giới hạn được gọi là hết tiền V3: 7k, V4: 10k, V5: 25k, V6: 50k, V7: 100k, V8: 150k, V9: 350k, V10: 1M
    private boolean CheckPromotionForDT3C52(int source, long ag, int vip) {
        try {
            if (ag < ServerDefined.promotion_policy.get(source).get(vip).getConditionP()) {
                return true;
            }
            /*if ((vip == 10) && (ag < 1000000)) return true;
        	else if ((vip == 9) && (ag < 350000)) return true;
        	else if ((vip == 8) && (ag < 150000)) return true;
        	else if ((vip == 7) && (ag < 100000)) return true;
        	else if ((vip == 6) && (ag < 50000)) return true;
        	else if ((vip == 5) && (ag < 25000)) return true;
        	else if ((vip == 4) && (ag < 10000)) return true;
        	else if ((vip == 3) && (ag < 7000)) return true;
        	else if (ag < 2000) return true;*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean CheckPromotionForSiam(int source, long ag, int vip) {
        try {
            if (ag < ServerDefined.promotion_policy.get(source).get(vip).getConditionP()) {
                return true;
            }
            /*if ((vip == 10) && (ag < 500000)) return true;
        	else if ((vip == 9) && (ag < 200000)) return true;
        	else if ((vip == 8) && (ag < 100000)) return true;
        	else if ((vip == 7) && (ag < 50000)) return true;
        	else if ((vip == 6) && (ag < 30000)) return true;
        	else if ((vip == 5) && (ag < 22500)) return true;
        	else if ((vip == 4) && (ag < 15000)) return true;
        	else if ((vip == 3) && (ag < 10000)) return true;
        	else if ((vip == 2) && (ag < 5000)) return true;
        	else if ((vip == 1) && (ag < 200)) return true;
        	else if (ag < 2000) return true;*/
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
        return false;
    }

    private int promotionForDT3C52(int solan, int basePromotion, int vip, int source, int countMsg) { //Ap dung cho Vip 3 tro len
        if (source == ServerSource.THAI_SOURCE) {
            if (countMsg > 100) {
                return 0;
            }
            return ServerDefined.promotion_policy.get(source).get(vip).getValueP();
            /*if (countMsg > 100) return 0 ;
    		if (solan <= 3) {
    			//int agpromotion = 0 ;
				if (vip == 0)
    				return 2000 ;
    			else if (vip == 1)
    				return 2000 ;
    			else if (vip == 2)
    				return 4000 ;
    			else if (vip == 3)
    				return 10000 ;
    			else if (vip == 4)
    				return 25000 ;
    			else if (vip == 5)
    				return 40000 ;
    			else if (vip == 6)
    				return 60000 ;
    			else if (vip == 7)
    				return 80000 ;
    			else if (vip == 8)
    				return 150000 ;
    			else if (vip == 9)
    				return 250000 ;
    			else 
    				return 500000 ;
        	} else return 0 ;*/
        } else if (source == ServerSource.IND_SOURCE) {
            if (countMsg > 100) {
                return 0;
            }
            return ServerDefined.promotion_policy.get(source).get(vip).getValueP();
            /*if (solan <=3 ){
        		int agpromotion = 0 ;
        		if (solan < 2)
            		agpromotion = 5000;
            	else if (solan < 6)
            		agpromotion = 3000;
            	else 
            		agpromotion = 3000;
        		return agpromotion ;
        	} else return 0 ;*/
        } else {
            if (countMsg > 100) {
                return 0;
            }
            return ServerDefined.promotion_policy.get(source).get(vip).getValueP();
            /*if (solan <=3 ){
    			if (vip == 0)
    				return 1500 ;
    			else if (vip == 1)
    				return 2500 ;
    			else if (vip == 2)
    				return 4000 ;
    			else if (vip == 3)
    				return 10000 ;
    			else if (vip == 4)
    				return 20000 ;
    			else if (vip == 5)
    				return 50000 ;
    			else if (vip == 6)
    				return 100000 ;
    			else if (vip == 7)
    				return 150000 ;
    			else if (vip == 8)
    				return 200000 ;
    			else if (vip == 9)
    				return 500000 ;
    			else 
    				return 1000000 ;
        	} else return 0 ;*/
        }
    }

    private int promotionForTomorrow(int vip, int source) { //Ap dung cho Vip 3 tro len
        return ServerDefined.promotion_policy.get(source).get(vip).getValueTomorrow();
        /*if (vip == 0)
			return 1500 ;
		else if (vip == 1)
			return 2500 ;
		else if (vip == 2)
			return 2500 ;
		else if (vip == 3)
			return 7000 ;
		else if (vip == 4)
			return 15000 ;
		else if (vip == 5)
			return 30000 ;
		else if (vip == 6)
			return 60000 ;
		else if (vip == 7)
			return 100000 ;
		else if (vip == 8)
			return 120000 ;
		else if (vip == 9)
			return 200000 ;
		else
			return 500000 ;*/
    }

    private int promotionForTomorrow_ForSiam(int vip, int source, long fid) { //Ap dung cho Vip 3 tro len
        if (vip == 0 || vip == 1) {
            if (fid != 0) {
                return 10000;
            } else {
                return 2000;
            }
        } else {
            if (fid > 0) {
                return 20000;
            } else {
                return 10000;
            }
        }
    }

    public int[] getArrPidByGame(int game, int operator, int source) { //Operator = 1 - Zing, 2-LQ, 0 -All
        int[] ret = new int[lsUserOnl.size()];

        for (int i = 0; i < lsUserOnl.size(); i++) {
            if ((lsUserOnl.get(i).getSource() != source) && (source != 0)) {
                continue;
            }

            if (game == 0) {
                ret[i] = lsUserOnl.get(i).getPid();
            } else if (lsUserOnl.get(i).getGameid() == game) {
                ret[i] = lsUserOnl.get(i).getPid();
            }
        }
        return ret;
    }

    public int[] filterPidByMinVip(int[] pids) {
        List<Integer> results = new ArrayList<>();
        for (int pid : pids) {
            UserInfo userInfo = userController.GetUserInfoFromCache(ServerSource.MYA_SOURCE, pid > ServerDefined.userMap.get(ServerSource.MYA_SOURCE)
                    ? pid - ServerDefined.userMap.get(ServerSource.MYA_SOURCE) : pid);
            if (userInfo != null && userInfo.isOverComeMinVip()) {
                results.add(pid);
            }
        }

        int[] arr = new int[results.size()];
        for (int i = 0; i <= results.size(); i++) {
            arr[i] = results.get(i);
        }
        return arr;
    }

    public static int[] getArrPidByTaixiu(int source) { //source - Game
        int[] ret = new int[lsUserOnl.size()];
        for (int i = 0; i < lsUserOnl.size(); i++) {
            if ((lsUserOnl.get(i).getSource() != source) && (source != 0)) {
                continue;
            }
            ret[i] = lsUserOnl.get(i).getPid();
        }
        return ret;
    }

    private int[] getArrPidBySourceGame(int game, int source) {
        synchronized (lsUserOnl) {
            int[] ret = new int[lsUserOnl.size()];
            for (int i = 0; i < lsUserOnl.size(); i++) {
                if (lsUserOnl.get(i).getSource() != source) {
                    continue;
                }
                if ((lsUserOnl.get(i).getVip() > -1) || (lsUserOnl.get(i).getOperatorid() != 105)) {
                    if (game == 0) {
                        ret[i] = lsUserOnl.get(i).getPid();
                    } else if (lsUserOnl.get(i).getGameid() == game) {
                        ret[i] = lsUserOnl.get(i).getPid();
                    }
                }
            }
            return ret;
        }
    }

    private int isUserOnl(String uname) {
        synchronized (lsUserOnl) {
            for (int i = 0; i < lsUserOnl.size(); i++) {
                if (lsUserOnl.get(i).getN().equals(uname)) {
                    return lsUserOnl.get(i).getPid();
                }
            }
            return 0;
        }
    }

    private int iSourceUserOnl(String uname) {
        synchronized (lsUserOnl) {
            for (int i = 0; i < lsUserOnl.size(); i++) {
                if (lsUserOnl.get(i).getN().equals(uname)) {
                    return lsUserOnl.get(i).getSource();
                }
            }
            return 0;
        }
    }

    @Override
    public void BotCreateTable(int gameid, int mark) {
        try {
            if (mapBot.get(gameid) != null) {
                mapBot.get(gameid).BotCreateTable(gameid, mark);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void BotCreateTable(int gameid, int mark, int type) {
        try {
            if (mapBot.get(gameid) != null) {
                mapBot.get(gameid).BotCreateTable(gameid, mark, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long UpdateBotDiamondByName(int uid, int source, String name, long mark, int typeU) {
//		 synchronized (BinhBotHandler.dicBots) {
//	            if (BinhBotHandler.dicBots.containsKey(uid)) {
//	                try {
//	                    long ret = -1l;
//	                    System.out.println("==>UpdateDiamondBot:" + BinhBotHandler.dicBots.get(uid).getUsername() + "-" + BinhBotHandler.dicBots.get(uid).getSource());
//	                   // int source = BotHandler.dicBots.get(uid).getSource();
//	                    ret = userController.UpdateDiamond(ServerDefined.C3_SOURCE, uid, mark);
//	                    if (ret >=0)
//	                    	BinhBotHandler.dicBots.get(uid).setDiamond(ret);
//	                    return BinhBotHandler.dicBots.get(uid).getDiamond();
//	                } catch (Exception e) {
//	                    System.out.println("ERROR UpdateDiamondBot:" + e.getMessage() + "-" + uid + "-" + mark);
//	                    e.printStackTrace();
//	                }
//	            }
//	        }
        return 0l;
    }

    @Override
    public Long UpdateBotMinidiceMarkChess(int uid, int source, String name, long mark) {
        try {
            return mapBot.get(GAMEID.MINIDICE).UpdateBotMarkByName(uid, source, name, mark);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Long UpdateBotXocdiaMarkChess(int uid, int source, String name, long mark) {
        try {
            return mapBot.get(GAMEID.XOCDIA).UpdateBotMarkByName(uid, source, name, mark);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Long UpdateBotStreethiloMarkChess(int uid, int source, String name, long mark) {
        try {
            return mapBot.get(GAMEID.STREETHILO).UpdateBotMarkByName(uid, source, name, mark);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Long UpdateBotHiloMarkChess(int uid, int source, String name, long mark) {
        try {
            return mapBot.get(GAMEID.HILO).UpdateBotMarkByName(uid, source, name, mark);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Long UpdateBotDominoMarkChess(int uid, int source, String name, long mark) {
        try {
            return mapBot.get(GAMEID.DOMINOQQ).UpdateBotMarkByName(uid, source, name, mark);
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Long UpdateBotMarkChess(int uid, int source, String name, long mark, int gameId) {
        try {
            if (mapBot.get(gameId) != null) {
                return mapBot.get(gameId).UpdateBotMarkByName(uid, source, name, mark);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0l;
    }

    @Override
    public int getUserMapID(int source) {
        try {
            return ServerDefined.userMap.get(source);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void BotCreateTableForRoom(int gameid, int mark, int roomID) {
        try {
            if (mapBot.get(gameid) != null) {
                mapBot.get(gameid).BotCreateTableForRoom(gameid, mark, roomID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServiceRegistry getSeviceRegistry() {
        return seviceRegistry;
    }

    public void setSeviceRegistry(ServiceRegistry seviceRegistry) {
        this.seviceRegistry = seviceRegistry;
    }

    @Override
    public void returnBot(Integer userid) {
        try {
//			if(IsRunBotBinh)
//			synchronized (BinhBotHandler.listBots) {
//        		for(int i = 0; i < BinhBotHandler.listBots.size(); i++){
//            		if(BinhBotHandler.listBots.get(i).getUserid().intValue() == userid.intValue())
//            		{
//            			BinhBotHandler.listBots.get(i).setIsOnline((short)0);
//            			break;
//            		}
//            	}
//			} 	
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
