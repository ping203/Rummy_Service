/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.handler.payment;

import com.athena.database.DataCacheNotFoundException;
import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.vo.UserAfterPay;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.vng.tfa.common.SqlService;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author hoangchau
 */
public class PaymentPackageHandle {

    private final int userId;
    private final short source;

    public PaymentPackageHandle(int pid, short source) {
        this.userId = pid - (int) ServerDefined.userMap.get((int) source);
        this.source = source;
    }

    private List<PaymentPackage> queryDatabase() {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        List<PaymentPackage> packages = new ArrayList<>();
        try {
            CallableStatement cs = conn.prepareCall("{call GameGetPaymentDetail(?) }");
            cs.setInt("UserId", userId);
            ResultSet rs = cs.executeQuery();
            
            

            while (rs.next()) {

                PaymentPackage pkg = new PaymentPackage();
                pkg.setPayType(rs.getInt("PaymentType"));
                pkg.setPkgChip(rs.getInt("PackageChip"));
                pkg.setPkgXValue(rs.getInt("PackageXValue"));
                pkg.setSubcriptionChip(rs.getInt("SubscriptionChip"));
                pkg.setSubcriptionDay(rs.getInt("SubscriptionDay"));
                pkg.setStartTime(rs.getTimestamp("StartTime").getTime());
                pkg.setEndTime(rs.getTimestamp("EndTime").getTime());
                packages.add(pkg);
                
            }
            rs.close();
            cs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            instance.releaseDbConnection(conn);
        }

        return packages;
    }
    

    private List<PaymentPackage> queryCache() throws DataCacheNotFoundException {

        String key = ServerDefined.getKeyCachePaymentPackage(source) + userId;

        List<PaymentPackage> list = (List) UserController.getCacheInstance().get(key);

        if (list == null) {
            throw new DataCacheNotFoundException();
        }

        return list;

    }

    private <T extends Object> void updateCache(T o) {
        try {
            
            String key = ServerDefined.getKeyCachePaymentPackage(source) + userId;
            long expire = (getEndOfDay(new Date()).getTime() - System.currentTimeMillis()) / 1000;
            UserController.getCacheInstance().set(key, o, (int)expire);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 23, 59, 59);
        return calendar.getTime();
    }

    public List doQuery() {
        List list;

        try {
            list = queryCache();
        } catch (DataCacheNotFoundException ex) {
            list = queryDatabase();
            updateCache(list);
        }

        return list;
    }

}
