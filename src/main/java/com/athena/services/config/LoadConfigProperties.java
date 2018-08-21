package com.athena.services.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadConfigProperties {
    private static Properties prop = null;

    public static  String getConfig(String key){
        if(prop == null){
            load();
        }

        return prop.getProperty(key);
    }

    private static void load(){
        prop = new Properties();
        InputStream input = null;

        try {
            String filename = "config.properties";
            input = LoadConfigProperties.class.getClassLoader().getResourceAsStream(filename);
            if(input==null){
                System.out.println("Sorry, unable to find " + filename);
                return;
            }
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
