package com.vng.tfa.common;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogUtil {

    public static boolean initialized = false;


    public static void init(String prefix) {
        System.out.println(prefix);
        if (initialized) {
            return;
        }
        System.out.println("ababab");
        String apppath = "./";
        String APP_ENV = prefix;

        System.out.println("apppath=" + apppath);
        System.out.println("appenv=" + APP_ENV);
        if (APP_ENV == null) {
            APP_ENV = "";
        }
        if (APP_ENV != "") {
            APP_ENV = APP_ENV + ".";
        }
        String file = apppath + File.separator + "conf" + File.separator + APP_ENV + "log4j.ini";

        System.out.println("file-log4j=" + file);

        PropertyConfigurator.configure(file);
        initialized = true;
    }

    public static void dumpLog(String content) {
        Logger.getLogger("LogUtil").info(content);
    }

    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }

    public static String stackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String out = sw.toString();
        pw.close();
        return out;
    }

    public static String getTimestamp() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:s");
        return df.format(date);
    }

    public static void setLogLevel(String logger, String level) {
        Logger loggerObj = LogManager.getLogger(logger);
        if (null == loggerObj) {
            return;
        }
        level = level.toUpperCase();
        if (level.equals("DEBUG")) {
            loggerObj.setLevel(Level.DEBUG);
        } else if (level.equals("ERROR")) {
            loggerObj.setLevel(Level.ERROR);
        } else if (level.equals("FATAL")) {
            loggerObj.setLevel(Level.FATAL);
        } else if (level.equals("INFO")) {
            loggerObj.setLevel(Level.INFO);
        } else if (level.equals("OFF")) {
            loggerObj.setLevel(Level.OFF);
        } else if (level.equals("WARN")) {
            loggerObj.setLevel(Level.WARN);
        } else {
            loggerObj.setLevel(Level.ALL);
        }
    }

    public static String throwableToString(Throwable e) {
        StringBuilder sbuf = new StringBuilder("");
        String trace = stackTrace(e);
        sbuf.append("Exception was generated at : " + getTimestamp() + " on thread " + Thread.currentThread().getName());

        sbuf.append(System.getProperty("line.separator"));
        String message = e.getMessage();
        if (message != null) {
            sbuf.append(message);
        }
        sbuf.append(System.getProperty("line.separator")).append(trace);

        return sbuf.toString();
    }

    public static String getLogMessage(String message) {
        StringBuilder sbuf = new StringBuilder("Log started at : " + getTimestamp());

        sbuf.append(File.separator).append(message);

        return sbuf.toString();
    }
}
