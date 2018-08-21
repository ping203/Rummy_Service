package com.vng.tfa.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import com.reardencommerce.kernel.collections.shared.evictable.ConcurrentLinkedHashMap;

public class Config {

    private static InputStream inputStream;
    public static final String CONFIG_HOME = "conf";
    public static final String CONFIG_FILE = "config.ini";
    //private static Logger logger_ = Logger.getLogger(Config.class);
    public static ConcurrentLinkedHashMap<String, String> listConfig = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, 100);

    public void loadConfig() throws IOException {
        try {
            Properties prop = new Properties();
            String HOME_PATH = "../";
            String propFileName = HOME_PATH + "conf" + File.separator + "configgame.ini";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            // get the property value and print it out
            try {
                listConfig.put("connection_3c", prop.getProperty("connection3C"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connection_lq", prop.getProperty("connectionLQ"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connection_52", prop.getProperty("connection52"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connection_dt", prop.getProperty("connectionDT"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connection_th", prop.getProperty("connectionTH"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connectionLog_3c", prop.getProperty("connection3CLog"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connectionLog_lq", prop.getProperty("connectionLQLog"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connectionLog_52", prop.getProperty("connection52Log"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connectionLog_dt", prop.getProperty("connectionDTLog"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connectionLog_dt", prop.getProperty("connectionDTLog"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connectionLog_th", prop.getProperty("connectionTHLog"));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.MYANMAR_DATABASE_CONNECTION, prop.getProperty(ConfigKey.MYANMAR_DATABASE_CONNECTION));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.MYANMAR_DATABASE_LOG_CONNECTION, prop.getProperty(ConfigKey.MYANMAR_DATABASE_LOG_CONNECTION));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put("connection_india", prop.getProperty("connectionIndia"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("connectionLog_india", prop.getProperty("connectionIndiaLog"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("cachehost", prop.getProperty("cacheHost"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("cacheport", prop.getProperty("cachePort"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("52Fun_Run", prop.getProperty("52Fun_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("Dautruong_Run", prop.getProperty("Dautruong_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("3C_Run", prop.getProperty("3C_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("Thai_Run", prop.getProperty("Thai_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.MYANMAR_GAME_ENABLE, prop.getProperty(ConfigKey.MYANMAR_GAME_ENABLE));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put("Indo_Run", prop.getProperty("Indo_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("India_Run", prop.getProperty("India_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("Taixiu_Run", prop.getProperty("Taixiu_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            //Bot
            try {
                listConfig.put("BotBinh_Run", prop.getProperty("BotBinh_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotPoker9K_Run", prop.getProperty("BotPoker9K_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotPoker9K_Run", prop.getProperty("BotPoker9K_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotPoker9K2345_Run", prop.getProperty("BotPoker9K2345_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotDummy_Run", prop.getProperty("BotDummy_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotDummyThai_Run", prop.getProperty("BotDummyThai_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotMinidice_Run", prop.getProperty("BotMinidice_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotXocdia_Run", prop.getProperty("BotXocdia_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotStreethilo_Run", prop.getProperty("BotStreethilo_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("IAP_Sandbox", prop.getProperty("IAP_Sandbox"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotHilo_Run", prop.getProperty("BotHilo_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotDomino_Run", prop.getProperty("BotDomino_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotTeenpatti_Run", prop.getProperty("BotTeenpatti_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotRummy_Run", prop.getProperty("BotRummy_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotPokdeng_Run", prop.getProperty("BotPokdeng_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotTomCuaCa_Run", prop.getProperty("BotTomCuaCa_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                listConfig.put("BotRemi_Run", prop.getProperty("BotRemi_Run"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            
            try {
                listConfig.put(ConfigKey.MYANMAR_BOT_BURMESE_POKER_ENABLE, prop.getProperty(ConfigKey.MYANMAR_BOT_BURMESE_POKER_ENABLE));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            
            try {
                listConfig.put(ConfigKey.MYANMAR_BOT_SHAN_KOE_MEE_ENABLE, prop.getProperty(ConfigKey.MYANMAR_BOT_SHAN_KOE_MEE_ENABLE));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            
            try {
                listConfig.put(ConfigKey.MYANMAR_BOT_TOM_CUA_CA_ENABLE, prop.getProperty(ConfigKey.MYANMAR_BOT_TOM_CUA_CA_ENABLE));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.INDO_BOT_SAMGONG_ENABLE, prop.getProperty(ConfigKey.INDO_BOT_SAMGONG_ENABLE));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.MYANMAR_BOT_SHAN_KOE_MEE_V2_ENABLE, prop.getProperty(ConfigKey.MYANMAR_BOT_SHAN_KOE_MEE_V2_ENABLE));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.MYANMAR_BOT_SHOWS_ENABLE, prop.getProperty(ConfigKey.MYANMAR_BOT_SHOWS_ENABLE));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.MYMANMAR_BOT_LUDO, prop.getProperty(ConfigKey.MYMANMAR_BOT_LUDO));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.MYMANMAR_BOT_Domino, prop.getProperty(ConfigKey.MYMANMAR_BOT_Domino));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            try {
                listConfig.put(ConfigKey.MYMANMAR_BOT_CHECKER, prop.getProperty(ConfigKey.MYMANMAR_BOT_CHECKER));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            
            try {
                listConfig.put(ConfigKey.MYANMAR_BOT_RUMMY, prop.getProperty(ConfigKey.MYANMAR_BOT_RUMMY));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
    }

    public static String getParam(String key) {
        String value = (String) listConfig.get(key);
        if (value != null) {
            return value;
        }
        return "";
    }
    /*private static final InstrumentedCache<String, String> localconfig;
  public static final String CONFIG_HOME = "conf";
  public static final String CONFIG_FILE = "config.ini";
  private static Logger logger_ = Logger.getLogger(Config.class);
  static CompositeConfiguration config;
  
  static
  {
    String CONFIG_ITEMS = System.getProperty("cfg_items");
//    String HOME_PATH = System.getProperty("apppath");
//    String APP_ENV = System.getProperty("appenv");
    String HOME_PATH = "./";
    String APP_ENV = "production";
    if ((CONFIG_ITEMS == null) || (CONFIG_ITEMS.equals(""))) {
      CONFIG_ITEMS = "500";
    }
    if (APP_ENV == null) {
      APP_ENV = "";
    }
    if (APP_ENV != "") {
      APP_ENV = APP_ENV + ".";
    }
    localconfig = new InstrumentedCache(Integer.valueOf(CONFIG_ITEMS).intValue());
    
    config = new CompositeConfiguration();
    
    File configFile = new File(HOME_PATH + File.separator + "conf" + File.separator + APP_ENV + "config.ini");
    System.out.println("==>ConfigFile:" + configFile.getPath()) ;
    try
    {
      config.addConfiguration(new HierarchicalINIConfiguration(configFile));
      Iterator<String> ii = config.getKeys();
      while (ii.hasNext())
      {  
        String key = (String)ii.next();
        localconfig.put(key, config.getString(key));
      }
    }
    catch (Exception e)
    {
      System.exit(1);
    }
  }
  
  public static String getHomePath()
  {
    return System.getProperty("apppath");
  }
  
  public static String getParam(String section, String name)
  {
    String key = section + "." + name;
    
    String value = (String)localconfig.get(key);
    if (value != null) {
      return value;
    }
    value = config.getString(key);
    if (value != null) {
      localconfig.put(key, value);
    }
    return value;
  }*/
}
