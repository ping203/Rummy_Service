/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.database;

import java.util.HashMap;
import java.util.Map;

import com.athena.services.vo.PolicyPromotionDetail;
import com.athena.services.vo.PolicyPromotionOnline;
import com.athena.services.config.PromotionOnlineConfig;
import com.athena.services.vo.PolicyPromotionDaily;
import static com.dst.ServerSource.INDIA_SOURCE;
import static com.dst.ServerSource.IND_SOURCE;
import static com.dst.ServerSource.MYA_SOURCE;
import static com.dst.ServerSource.THAI_SOURCE;

/**
 *
 * @author chiennguyen
 */
public class ServerDefined {
    public static final String ADMIN = "Admin";

    public static final int BOT_OPERATOR = 0;
                    
    public static final String KEY_CACHE_USER_INFO  = "keyCache";                
    public static final String KEY_CACHE_DEVICE_NEW  = "keyCacheDeviceNew";
    public static final String KEY_CACHE_USER_SETTING  = "keyCacheUsetting";
    public static final String KEY_CACHE_FRIEND  = "keyCacheFriend";
    public static final String KEY_CACHE_MESSAGE = "keyCacheMessage";
    public static final String KEY_CACHE_PROMOTION = "keyCachePromotion";
    public static final String KEY_CACHE_USER_ID = "keyCacheId";
    public static final String KEY_CACHE_HIGH_LOW = "keyCacheHighlow";
    public static final String KEY_CACHE_FIRST_LOGIN_FB = "keyCacheFirstLoginFb";
    public static final String KEY_CACHE_MAP_ID_NAME = "keyMapidname";
    public static final String KEY_CACHE_PAYMENT_PACKAGE = "keyCachePaymentPackage";
    

    public static Map<Integer, Map<Integer, PolicyPromotionDetail>> promotion_policy = new HashMap<Integer, Map<Integer, PolicyPromotionDetail>>();

    public static Map<Integer, Map<Integer, Integer>> promotion_vip = new HashMap<Integer, Map<Integer, Integer>>() {
        private static final long serialVersionUID = 3109256773218160485L;
        {
            this.put(INDIA_SOURCE, new HashMap<Integer, Integer>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, 0);
                    this.put(1, 1);
                    this.put(2, 10);
                    this.put(3, 500);
                    this.put(4, 2000);
                    this.put(5, 5000);
                    this.put(6, 10000);
                    this.put(7, 30000);
                    this.put(8, 60000);
                    this.put(9, 120000);
                    this.put(10, 200000);
                }
            });
            this.put(THAI_SOURCE, new HashMap<Integer, Integer>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, 0);
                    this.put(1, 1);
                    this.put(2, 10);
                    this.put(3, 500);
                    this.put(4, 2000);
                    this.put(5, 5000);
                    this.put(6, 10000);
                    this.put(7, 30000);
                    this.put(8, 60000);
                    this.put(9, 120000);
                    this.put(10, 200000);
                }
            });

            this.put(MYA_SOURCE, new HashMap<Integer, Integer>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, 0);
                    this.put(1, 1);
                    this.put(2, 10);
                    this.put(3, 500);
                    this.put(4, 2000);
                    this.put(5, 5000);
                    this.put(6, 10000);
                    this.put(7, 30000);
                    this.put(8, 60000);
                    this.put(9, 120000);
                    this.put(10, 200000);
                }
            });

            this.put(IND_SOURCE, new HashMap<Integer, Integer>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, 0);
                    this.put(1, 1);
                    this.put(2, 10);
                    this.put(3, 500);
                    this.put(4, 2000);
                    this.put(5, 5000);
                    this.put(6, 10000);
                    this.put(7, 30000);
                    this.put(8, 60000);
                    this.put(9, 120000);
                    this.put(10, 200000);
                }
            });

        }
    };

    public static Map<Integer, Map<Integer, PolicyPromotionDaily>> dailyreward_policy = new HashMap<Integer, Map<Integer, PolicyPromotionDaily>>() {
        private static final long serialVersionUID = 3109256773218160485L;
        {
            this.put(THAI_SOURCE, new HashMap<Integer, PolicyPromotionDaily>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, new PolicyPromotionDaily(new int[]{1500, 2000, 3000, 4000, 5000, 6000, 7000}, new int[]{0, 0, 3000, 0, 5000, 0, 7000}));
                    this.put(1, new PolicyPromotionDaily(new int[]{1500, 2000, 3000, 4000, 5000, 6000, 7000}, new int[]{0, 0, 3000, 0, 5000, 0, 7000}));
                    this.put(2, new PolicyPromotionDaily(new int[]{4000, 5000, 6000, 7000, 8000, 9000, 10000}, new int[]{0, 0, 6000, 0, 8000, 0, 10000}));
                    this.put(3, new PolicyPromotionDaily(new int[]{10000, 15000, 20000, 25000, 30000, 35000, 40000}, new int[]{0, 0, 20000, 0, 30000, 0, 40000}));
                    this.put(4, new PolicyPromotionDaily(new int[]{20000, 30000, 40000, 50000, 60000, 70000, 80000}, new int[]{0, 0, 30000, 0, 50000, 0, 100000}));
                    this.put(5, new PolicyPromotionDaily(new int[]{30000, 40000, 50000, 60000, 70000, 80000, 90000}, new int[]{0, 0, 50000, 0, 90000, 0, 150000}));
                    this.put(6, new PolicyPromotionDaily(new int[]{40000, 55000, 70000, 85000, 100000, 115000, 130000}, new int[]{0, 0, 80000, 0, 150000, 0, 250000}));
                    this.put(7, new PolicyPromotionDaily(new int[]{50000, 70000, 90000, 110000, 130000, 150000, 170000}, new int[]{0, 0, 100000, 0, 200000, 0, 300000}));
                    this.put(8, new PolicyPromotionDaily(new int[]{100000, 150000, 200000, 250000, 300000, 350000, 400000}, new int[]{0, 0, 200000, 0, 350000, 0, 500000}));
                    this.put(9, new PolicyPromotionDaily(new int[]{200000, 300000, 400000, 500000, 600000, 700000, 800000}, new int[]{0, 0, 500000, 0, 700000, 0, 1000000}));
                    this.put(10, new PolicyPromotionDaily(new int[]{200000, 300000, 400000, 500000, 600000, 700000, 800000}, new int[]{0, 0, 1000000, 0, 2000000, 0, 3000000}));
                }
            });

            this.put(MYA_SOURCE, new HashMap<Integer, PolicyPromotionDaily>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, new PolicyPromotionDaily(new int[]{1500, 2000, 3000, 4000, 5000, 6000, 7000}, new int[]{0, 0, 3000, 0, 5000, 0, 7000}));
                    this.put(1, new PolicyPromotionDaily(new int[]{1500, 2000, 3000, 4000, 5000, 6000, 7000}, new int[]{0, 0, 3000, 0, 5000, 0, 7000}));
                    this.put(2, new PolicyPromotionDaily(new int[]{4000, 5000, 6000, 7000, 8000, 9000, 10000}, new int[]{0, 0, 6000, 0, 8000, 0, 10000}));
                    this.put(3, new PolicyPromotionDaily(new int[]{10000, 15000, 20000, 25000, 30000, 35000, 40000}, new int[]{0, 0, 20000, 0, 30000, 0, 40000}));
                    this.put(4, new PolicyPromotionDaily(new int[]{20000, 30000, 40000, 50000, 60000, 70000, 80000}, new int[]{0, 0, 30000, 0, 50000, 0, 100000}));
                    this.put(5, new PolicyPromotionDaily(new int[]{30000, 40000, 50000, 60000, 70000, 80000, 90000}, new int[]{0, 0, 50000, 0, 90000, 0, 150000}));
                    this.put(6, new PolicyPromotionDaily(new int[]{40000, 55000, 70000, 85000, 100000, 115000, 130000}, new int[]{0, 0, 80000, 0, 150000, 0, 250000}));
                    this.put(7, new PolicyPromotionDaily(new int[]{50000, 70000, 90000, 110000, 130000, 150000, 170000}, new int[]{0, 0, 100000, 0, 200000, 0, 300000}));
                    this.put(8, new PolicyPromotionDaily(new int[]{100000, 150000, 200000, 250000, 300000, 350000, 400000}, new int[]{0, 0, 200000, 0, 350000, 0, 500000}));
                    this.put(9, new PolicyPromotionDaily(new int[]{200000, 300000, 400000, 500000, 600000, 700000, 800000}, new int[]{0, 0, 500000, 0, 700000, 0, 1000000}));
                    this.put(10, new PolicyPromotionDaily(new int[]{200000, 300000, 400000, 500000, 600000, 700000, 800000}, new int[]{0, 0, 1000000, 0, 2000000, 0, 3000000}));
                }
            });

            this.put(IND_SOURCE, new HashMap<Integer, PolicyPromotionDaily>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, new PolicyPromotionDaily(new int[]{3000, 4000, 5000, 6000, 7000, 8000, 9000}, new int[]{0, 0, 2000, 0, 3000, 0, 4000}));
                    this.put(1, new PolicyPromotionDaily(new int[]{3000, 4000, 5000, 6000, 7000, 8000, 9000}, new int[]{0, 0, 2000, 0, 3000, 0, 4000}));
                    this.put(2, new PolicyPromotionDaily(new int[]{8000, 10000, 12000, 14000, 16000, 18000, 20000}, new int[]{0, 0, 10000, 0, 20000, 0, 30000}));
                    this.put(3, new PolicyPromotionDaily(new int[]{10000, 15000, 20000, 25000, 30000, 35000, 40000}, new int[]{0, 0, 20000, 0, 30000, 0, 40000}));
                    this.put(4, new PolicyPromotionDaily(new int[]{20000, 30000, 40000, 50000, 60000, 70000, 80000}, new int[]{0, 0, 30000, 0, 50000, 0, 100000}));
                    this.put(5, new PolicyPromotionDaily(new int[]{30000, 40000, 50000, 60000, 70000, 80000, 90000}, new int[]{0, 0, 50000, 0, 90000, 0, 150000}));
                    this.put(6, new PolicyPromotionDaily(new int[]{40000, 55000, 70000, 85000, 100000, 115000, 130000}, new int[]{0, 0, 80000, 0, 150000, 0, 250000}));
                    this.put(7, new PolicyPromotionDaily(new int[]{50000, 70000, 90000, 110000, 130000, 150000, 170000}, new int[]{0, 0, 100000, 0, 200000, 0, 300000}));
                    this.put(8, new PolicyPromotionDaily(new int[]{100000, 150000, 200000, 250000, 300000, 350000, 400000}, new int[]{0, 0, 200000, 0, 350000, 0, 500000}));
                    this.put(9, new PolicyPromotionDaily(new int[]{200000, 300000, 400000, 500000, 600000, 700000, 800000}, new int[]{0, 0, 500000, 0, 700000, 0, 1000000}));
                    this.put(10, new PolicyPromotionDaily(new int[]{200000, 300000, 400000, 500000, 600000, 700000, 800000}, new int[]{0, 0, 1000000, 0, 2000000, 0, 3000000}));
                }
            });
            this.put(INDIA_SOURCE, new HashMap<Integer, PolicyPromotionDaily>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, new PolicyPromotionDaily(new int[]{12000, 17000, 22000, 27000, 32000, 37000, 42000}, new int[]{0, 0, 25000, 0, 30000, 0, 50000}));
                    this.put(1, new PolicyPromotionDaily(new int[]{12000, 17000, 22000, 27000, 32000, 37000, 42000}, new int[]{0, 0, 25000, 0, 30000, 0, 50000}));
                    this.put(2, new PolicyPromotionDaily(new int[]{50000, 70000, 90000, 110000, 130000, 150000, 170000}, new int[]{0, 0, 50000, 0, 80000, 0, 110000}));
                    this.put(3, new PolicyPromotionDaily(new int[]{70000, 100000, 130000, 160000, 190000, 220000, 250000}, new int[]{0, 0, 70000, 0, 120000, 0, 170000}));
                    this.put(4, new PolicyPromotionDaily(new int[]{100000, 150000, 200000, 250000, 300000, 350000, 400000}, new int[]{0, 0, 100000, 0, 200000, 0, 300000}));
                    this.put(5, new PolicyPromotionDaily(new int[]{150000, 250000, 350000, 450000, 550000, 650000, 750000}, new int[]{0, 0, 150000, 0, 300000, 0, 450000}));
                    this.put(6, new PolicyPromotionDaily(new int[]{250000, 400000, 550000, 700000, 850000, 900000, 950000}, new int[]{0, 0, 250000, 0, 450000, 0, 650000}));
                    this.put(7, new PolicyPromotionDaily(new int[]{400000, 600000, 800000, 1000000, 1200000, 1400000, 1600000}, new int[]{0, 0, 400000, 0, 700000, 0, 1100000}));
                    this.put(8, new PolicyPromotionDaily(new int[]{600000, 900000, 1200000, 1500000, 1800000, 2100000, 2400000}, new int[]{0, 0, 600000, 0, 1100000, 0, 1600000}));
                    this.put(9, new PolicyPromotionDaily(new int[]{1000000, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000}, new int[]{0, 0, 1000000, 0, 1500000, 0, 2000000}));
                    this.put(10, new PolicyPromotionDaily(new int[]{1500000, 2000000, 2500000, 3000000, 3500000, 4000000, 4500000}, new int[]{0, 0, 1500000, 0, 2200000, 0, 2900000}));
                }
            });
        }
    };

    public static Map<Integer, Map<Integer, PolicyPromotionOnline>> online_policy = new HashMap<Integer, Map<Integer, PolicyPromotionOnline>>() {
        private static final long serialVersionUID = 3109256773218160485L;
        {
            this.put(THAI_SOURCE, PromotionOnlineConfig.configSiam);
            
            this.put(MYA_SOURCE, PromotionOnlineConfig.configSiam);
            
            this.put(IND_SOURCE, new HashMap<Integer, PolicyPromotionOnline>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{500, 700, 900, 1100, 1300, 1500}));
                    this.put(1, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{500, 700, 900, 1100, 1300, 1500}));
                    this.put(2, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{2000, 3000, 5000, 7000, 10000, 15000}));
                    this.put(3, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{3000, 5000, 7000, 10000, 15000, 20000}));
                    this.put(4, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{10000, 12000, 15000, 20000, 25000, 30000}));
                    this.put(5, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{15000, 20000, 25000, 30000, 40000, 50000}));
                    this.put(6, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{20000, 30000, 40000, 60000, 80000, 100000}));
                    this.put(7, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{30000, 40000, 50000, 70000, 100000, 150000}));
                    this.put(8, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{50000, 70000, 100000, 150000, 200000, 300000}));
                    this.put(9, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{70000, 100000, 150000, 200000, 300000, 500000}));
                    this.put(10, new PolicyPromotionOnline(6, new int[]{5, 10, 30, 60, 120, 240}, new int[]{100000, 150000, 200000, 300000, 400000, 600000}));
                }
            });
            this.put(INDIA_SOURCE, new HashMap<Integer, PolicyPromotionOnline>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(0, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{10000, 11000, 12000, 15000, 17000, 20000}));
                    this.put(1, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{10000, 11000, 12000, 15000, 17000, 20000}));
                    this.put(2, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{20000, 25000, 30000, 35000, 40000, 50000}));
                    this.put(3, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{50000, 70000, 90000, 110000, 130000, 150000}));
                    this.put(4, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{80000, 110000, 140000, 170000, 200000, 250000}));
                    this.put(5, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{100000, 150000, 200000, 250000, 300000, 370000}));
                    this.put(6, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{150000, 210000, 270000, 330000, 390000, 500000}));
                    this.put(7, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{200000, 300000, 400000, 500000, 600000, 800000}));
                    this.put(8, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{300000, 420000, 540000, 660000, 780000, 1000000}));
                    this.put(9, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{500000, 650000, 800000, 950000, 1100000, 1400000}));
                    this.put(10, new PolicyPromotionOnline(6, new int[]{2, 5, 10, 30, 60, 120}, new int[]{1000000, 1300000, 1600000, 1900000, 2200000, 3000000}));
                }
            });
        }
    };
    public static Map<Integer, Integer> userMap = new HashMap<Integer, Integer>() {
        private static final long serialVersionUID = 3109256773218160485L;
        {
            this.put(INDIA_SOURCE, 60000000);
            this.put(THAI_SOURCE, 500000000);
            this.put(MYA_SOURCE, 100000000);
            this.put(IND_SOURCE, 0);
        }
    };
    public static Map<Integer, Map<String, String>> servers = new HashMap<Integer, Map<String, String>>() {
        private static final long serialVersionUID = 3109256773218160485L;
        {
            
            this.put(THAI_SOURCE, new HashMap<String, String>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put("keyCache", "uinfo_");
                    this.put("keyCacheDeviceNew", "dnew1111_");
                    this.put("keyCacheUsetting", "usetting_");
                    this.put("keyCacheFriend", "friend_");
                    this.put("keyCacheMessage", "message_");
                    this.put("keyCachePromotion", "promotion2_");
                    this.put("keyCacheId", "id_");
                    this.put("keyCacheHighlow", "highlownew1_");
                    this.put("keyCacheFirstLoginFb", "firstloginfbsiam_");
                    this.put("keyMapidname", "mapidnamesiam_");
                }
            });
            
            this.put(MYA_SOURCE, new HashMap<String, String>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put(KEY_CACHE_USER_INFO, "uinfo_mym_");
                    this.put(KEY_CACHE_DEVICE_NEW, "dnew_mym_");
                    this.put(KEY_CACHE_USER_SETTING, "usetting_mym_");
                    this.put(KEY_CACHE_FRIEND, "friend_mym_");
                    this.put(KEY_CACHE_MESSAGE, "message_mym_");
                    this.put(KEY_CACHE_PROMOTION, "promotion2_mym_");
                    this.put(KEY_CACHE_USER_ID, "id_mym_");
                    this.put(KEY_CACHE_HIGH_LOW, "highlownew1_mym_");
                    this.put(KEY_CACHE_FIRST_LOGIN_FB, "firstloginfb_mym_");
                    this.put(KEY_CACHE_MAP_ID_NAME, "mapidname_mym_");
                    this.put(KEY_CACHE_PAYMENT_PACKAGE, "pay_pkg_mym_");
                }
            });
            this.put(IND_SOURCE, new HashMap<String, String>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put("keyCache", "uinfoind_");
                    this.put("keyCacheDeviceNew", "dnewind_");
                    this.put("keyCacheUsetting", "usettingind_");
                    this.put("keyCacheFriend", "friendind_");
                    this.put("keyCacheMessage", "messageind_");
                    this.put("keyCachePromotion", "promotionind_");
                    this.put("keyCacheId", "idind_");
                    this.put("keyCacheHighlow", "highlownew1ind_");
                    this.put("keyCacheFirstLoginFb", "firstloginfbindo_");
                    this.put("keyMapidname", "mapidnameindo_");
                }
            });
            this.put(INDIA_SOURCE, new HashMap<String, String>() {
                private static final long serialVersionUID = 3109256773218160485L;
                {
                    this.put("keyCache", "uinfoindia_");
                    this.put("keyCacheDeviceNew", "dnewindia_");
                    this.put("keyCacheUsetting", "usettingindia_");
                    this.put("keyCacheFriend", "friendindia_");
                    this.put("keyCacheMessage", "messageindia_");
                    this.put("keyCachePromotion", "promotionindia_");
                    this.put("keyCacheId", "idindia_");
                    this.put("keyCacheHighlow", "highlownew1india_");
                    this.put("keyCacheFirstLoginFb", "firstloginfbindia_");
                    this.put("keyMapidname", "mapidnameindia_");
                }
            });
        }
    };

    public static String getKeyMessage(int source) {
        String key = "message_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCacheMessage");
        }
        return key;
    }

    public static String getKeyFriends(int source) {
        String key = "friend_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCacheFriend");
        }
        return key;
    }

    public static String getKeyPromotion(int source) {
        String key = "promotion_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCachePromotion");
        }
        return key;
    }

    public static String getKeyCacheId(int source) {
        String key = "id_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCacheId");
        }
        return key;
    }

    public static String getKeyCache(int source) {
        String key = "uinfo_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCache");
        }
        return key;
    }

    public static String getKeyCacheMapIdName(int source) {
        String key = "mapidname_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyMapidname");
        }
        return key;
    }

    public static String getKeyCacheDevice(int source) {
        String key = "dnew_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCacheDeviceNew");
        }
        return key;
    }

    public static String getKeyCacheFirstLoginFBInDevice(int source) {
        String key = "dnew_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCacheFirstLoginFb");
        }
        return key;
    }

    public static String getKeyCacheUserSetting(int source) {
        String key = "usetting_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCacheUsetting");
        }
        return key;
    }

    public static String getKeyCacheHighlow(int source) {
        String key = "highlownew1_";
        if (servers.containsKey(source)) {
            key = servers.get(source).get("keyCacheHighlow");
        }
        return key;
    }
    
    public static String getKeyCachePaymentPackage(int source) {
        return servers.get(source).get(KEY_CACHE_PAYMENT_PACKAGE);
    }
}
