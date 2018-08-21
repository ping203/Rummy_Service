package com.athena.database;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemController implements Serializable{
	public SystemController(){
		
	}
	public int prc_game_getPromotionX2Card(Connection conn){
        try {
            CallableStatement cs = conn.prepareCall("{call prc_game_getPromotionX2Card() }");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
            	return rs.getInt("C");
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return 0;
    }
}
