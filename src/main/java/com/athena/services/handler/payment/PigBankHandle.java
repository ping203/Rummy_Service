/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.handler.payment;

import com.athena.database.ServerDefined;
import com.vng.tfa.common.SqlService;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author hoangchau
 */
public class PigBankHandle {
    
    private final int userId; 
    private final short source;
    

    public PigBankHandle(int pid, short source) {
        this.userId = pid - (int)ServerDefined.userMap.get((int)source);
        this.source = source;
    }
    
    
    public PigBank doQuery() {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        PigBank pb = new PigBank();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetPigBank(?) }");
            cs.setInt("UserId", userId);
            
            ResultSet rs = cs.executeQuery();
            while(rs.next()){
                pb.setCurrent(rs.getLong("Pigbank"));
                pb.setMax(rs.getLong("PigbankMax"));
            }
            
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }
        
        return pb;
    }
}
