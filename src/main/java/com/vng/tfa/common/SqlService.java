package com.vng.tfa.common;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.dst.ServerSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.apache.log4j.Logger;

/**
 *
 * @author root
 */
public class SqlService {

    private static Logger logger_ = Logger.getLogger(SqlService.class);
    private static final Lock createLock_ = new ReentrantLock();
    private static final Map<String, SqlService> _instances = new LinkedHashMap();
    private ManagerIF dbmanager = null;
//    public static boolean IsThaiTest = true; 

    public static void main(String[] args) {
//        String a = Config.getParam("Chau", "gacbep");
//        System.out.println(System.getProperty("user.dir"));
//        Connection thai = SqlService.getInstanceBySource((short)9).getDbConnection();
//        //int uId = GameGetUserid_Face(thai, 10201920552975202l, "Tên", "123DV");
//        int uId = GetUserIDByUsername(thai, "fb.10201920552975202");
//        System.out.println(uId);
    }

    public static int GameGetUserid_Face(Connection conn, long facebookId, String facebookName, String deviceId) {
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserId_Face(?,?,?,?) }");
            cs.setLong("FacebookId", facebookId);
            cs.setString("FacebookName", facebookName);
            cs.setString("DeviceId", deviceId);
            cs.registerOutParameter("Userid", Types.INTEGER);
            cs.execute();
            return cs.getInt("Userid");
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public static int GetUserIDByUsername(Connection conn, String username) {
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetUserIDByUserName(?,?) }");
            cs.setString("Username", username);
            cs.registerOutParameter("Error", Types.INTEGER);
            cs.execute();
            return cs.getInt("Error");
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public static SqlService getInstanceBySource(int source) {
        SqlService inst = null;
        switch (source) {
            case ServerSource.INDIA_SOURCE:
                return SqlService.getInstance(Config.getParam(ConfigKey.INDIA_DATABASE_CONNECTION));
            case ServerSource.THAI_SOURCE:
                return SqlService.getInstance(Config.getParam(ConfigKey.SIAM_DATABASE_CONNECTION));
            case ServerSource.MYA_SOURCE:
                return SqlService.getInstance(Config.getParam(ConfigKey.MYANMAR_DATABASE_CONNECTION));
            case ServerSource.IND_SOURCE:
                return SqlService.getInstance(Config.getParam(ConfigKey.INDO_DATABASE_CONNECTION));

        }
        return inst;
    }

    public static SqlService getInstanceLogBySource(int source) {
        SqlService inst = null;
        switch (source) {
            case ServerSource.INDIA_SOURCE:
                return SqlService.getInstance(Config.getParam(ConfigKey.INDIA_DATABASE_LOG_CONNECTION));
            case ServerSource.THAI_SOURCE:
                return SqlService.getInstance(Config.getParam(ConfigKey.SIAM_DATABASE_LOG_CONNECTION));

            case ServerSource.MYA_SOURCE:
                return SqlService.getInstance(Config.getParam(ConfigKey.MYANMAR_DATABASE_LOG_CONNECTION));

            case ServerSource.IND_SOURCE:
                return SqlService.getInstance(Config.getParam(ConfigKey.INDO_DATABASE_LOG_CONNECTION));
            default:

        }
        return inst;
    }

    public static SqlService getInstance(String urlConnection) {
        String key = urlConnection;
        if (!_instances.containsKey(key)) {
            createLock_.lock();
            try {
                if (!_instances.containsKey(key)) {
                    _instances.put(key, new SqlService(urlConnection));
                }
            } catch (Exception ex) {
                logger_.error(ex.getMessage(), ex);
            } finally {
                createLock_.unlock();
            }
        }
        return (SqlService) _instances.get(key);
    }

    public SqlService(String urlConnection) {
        getClass();
        System.out.println("init connection manager with url = " + urlConnection);
        logger_.info("init connection manager with url = " + urlConnection);
        this.dbmanager = ClientManager.getInstance(urlConnection);
    }

    public Connection getDbConnection() {
        boolean ret;
        int i = 1;
        int maxRet = 5;
        Connection conn = null;
        do {
            try {
                conn = (Connection) this.dbmanager.borrowClient();
                ret = false;
            } catch (Exception ex) {
                System.out.println("Retry to get dbConnection..." + i);
                System.out.println("GetConnFail: " + ex.getMessage());
                if (conn != null) {
                    invalidDbConnection(conn);
                }
                i++;
                if (i < maxRet) {
                    ret = true;
                } else {
                    ret = false;
                }
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (ret);
        return conn;
    }

    public void releaseDbConnection(java.sql.Connection conn) {
        this.dbmanager.returnClient(conn);
    }

    public void invalidDbConnection(java.sql.Connection conn) {
        this.dbmanager.invalidClient(conn);
    }

}
