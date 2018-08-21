package com.athena.database;

import com.athena.log.LoggerKey;
import java.nio.ByteBuffer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.Date;

import org.apache.log4j.Logger;

import com.athena.services.impl.ServiceImpl;
import com.athena.services.ina.IAP_IOS_ITEM_IN_APP;
import com.athena.services.ina.PaymentIAP;
import com.athena.services.ina.Security;
import com.athena.services.ina.VerifyIAPApple;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.google.gson.JsonObject;
import com.vng.tfa.common.SqlService;

public class IAPCommand {

    public static final Logger LOGGER = Logger.getLogger(LoggerKey.IAP_IOS);
    
    public void processIAP_IOS(JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter) {
        try {
            LOGGER.info("==>IAPCommand==>Process_PaymentIAP_IOS:" + (new Date()).toString() + "-" + je.get("receipt_encoded64").getAsString());
            String receipt = je.get("receipt_encoded64").getAsString();
            long gold = 0;
            int vip = actionUser.getVIP();
            int source = actionUser.getSource();
            String output = VerifyIAPApple.verify(receipt, false);
            LOGGER.info("==>IAPCommand==>Process_PaymentIAP_IOS: " + ActionUtils.gson.toJson(actionUser));
            LOGGER.info("==>IAPCommand==>Process_PaymentIAP_IOS: - Output: " + output);
            if (output.length() != 0) {
                try {
                    JsonObject jsObj = (JsonObject) ServiceImpl.parser.parse(output);
                    LOGGER.info("==>IAPCommand==>Process_PaymentIAP_IOS: jsObj " + ActionUtils.gson.toJson(jsObj));
                    if (jsObj.has("environment")) {
                        if (jsObj.get("status").getAsInt() == 0) {
                            JsonObject obj = jsObj.get("receipt").getAsJsonObject();
                            if (obj.get("in_app").getAsJsonArray().size() > 0) {
                                IAP_IOS_ITEM_IN_APP item = ActionUtils.gson.fromJson(obj.get("in_app").getAsJsonArray().get(0).toString(), IAP_IOS_ITEM_IN_APP.class);
                                JsonObject data = AddMoneyIOS(source, actionUser.getUserid() - ServerDefined.userMap.get(source), item, 0);
                                if (data != null) {
                                    gold = data.get("Error").getAsInt();
                                    vip = data.get("Vip").getAsInt();
                                    LOGGER.info("==>IAPCommand==>processIAP_IOS" + actionUser.getPid() + " - chipadd: " + gold + " - vip: " + vip + " - vippoint: " + data.get("Vippoint").getAsInt());
                                }
                            }
                        } else if (jsObj.get("status").getAsInt() == 21007) { // sandbox
                            output = VerifyIAPApple.verify(receipt);
                            LOGGER.info("==>IAPCommand==>Process_PaymentIAP_IOS:sandbox:output " + output);
                            if (output.length() != 0) {
                                jsObj = (JsonObject) ServiceImpl.parser.parse(output);
                                if (jsObj.get("status").getAsInt() == 0) {
                                    JsonObject obj = jsObj.get("receipt").getAsJsonObject();
                                    if (obj.get("in_app").getAsJsonArray().size() > 0) {
                                        IAP_IOS_ITEM_IN_APP item = ActionUtils.gson.fromJson(obj.get("in_app").getAsJsonArray().get(0).toString(), IAP_IOS_ITEM_IN_APP.class);
                                        JsonObject data = AddMoneyIOS(source, actionUser.getUserid() - ServerDefined.userMap.get(source), item, 1);
                                        if (data != null) {
                                            gold = data.get("Error").getAsLong();
                                            vip = data.get("Vip").getAsInt();
                                            LOGGER.info("==>IAPCommand==>processIAP_IOS" + actionUser.getPid() + " - chipadd: " + gold + " - vip: " + vip + " - vippoint: " + data.get("Vippoint").getAsInt());
                                        }
                                    }
                                }
                            }
                        }
                    } else if (jsObj.get("status").getAsInt() == 21007) { // sandbox
                        output = VerifyIAPApple.verify(receipt);
                        LOGGER.info("==>IAPCommand==>Process_PaymentIAP_IOS:sandbox:output " + output);
                        if (output.length() != 0) {
                            jsObj = (JsonObject) ServiceImpl.parser.parse(output);
                            if (jsObj.get("status").getAsInt() == 0) {
                                JsonObject obj = jsObj.get("receipt").getAsJsonObject();
                                if (obj.get("in_app").getAsJsonArray().size() > 0) {
                                    IAP_IOS_ITEM_IN_APP item = ActionUtils.gson.fromJson(obj.get("in_app").getAsJsonArray().get(0).toString(), IAP_IOS_ITEM_IN_APP.class);
                                    JsonObject data = AddMoneyIOS(source, actionUser.getUserid() - ServerDefined.userMap.get(source), item, 1);
                                    if (data != null) {
                                        gold = data.get("Error").getAsLong();
                                        vip = data.get("Vip").getAsInt();
                                        LOGGER.info("==>IAPCommand==>processIAP_IOS" + actionUser.getPid() + " - chipadd: " + gold + " - vip: " + vip + " - vippoint: " + data.get("Vippoint").getAsInt());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("==>IAPCommand==>Process_PaymentIAP_IOS:" + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                LOGGER.info("==>IAPCommand==>something wrong!");
            }
            if (ServiceImpl.dicUser.get(actionUser.getPid()).getTableId() > 0) {
                GameDataAction gda = new GameDataAction(actionUser.getPid(), ServiceImpl.dicUser.get(actionUser.getPid()).getTableId());
                JsonObject jo = new JsonObject();
                jo.addProperty("evt", "ag_iap");
                jo.addProperty("ag", gold);
                jo.addProperty("vip", vip);
                gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jo).getBytes("UTF-8")));
                serviceRouter.dispatchToGame(ServiceImpl.dicUser.get(actionUser.getPid()).getGameid(), gda);
            }
            JsonObject act = new JsonObject();
            act.addProperty("evt", "iap_ios");
            if (gold > 0) {
                act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strPayment_Success_Pre", actionUser.getSource(), actionUser.getUserid())
                        + gold + ServiceImpl.actionUtils.getConfigText("strPayment_Success_Sur", actionUser.getSource(), actionUser.getUserid()));
                act.addProperty("status", 0);
                ServiceImpl.dicUser.get(actionUser.getPid()).IncrementMark(gold);
                ServiceImpl.userController.UpdateAGCache(source, actionUser.getUserid() - ServerDefined.userMap.get(source), gold, (short) vip, 0l);
            } else if (gold == -2) {
                act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strPayment_Err2", actionUser.getSource(), actionUser.getUserid()));
                act.addProperty("status", 1);
            } else if (gold == -3) {
                act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strPayment_Err1", actionUser.getSource(), actionUser.getUserid()));
                act.addProperty("status", 1);
            } else {
                act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strPayment_Err1", actionUser.getSource(), actionUser.getUserid()));
                act.addProperty("status", 1);
            }
            act.addProperty("vip", vip);
            act.addProperty("chip", ServiceImpl.dicUser.get(actionUser.getPid()).getAG().longValue());
            act.addProperty("receipt", receipt);
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Nap tien qua IOS

    public JsonObject AddMoneyIOS(int source, int userid, IAP_IOS_ITEM_IN_APP obj, int sanbox) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call AddMoneyIOS_Using(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }");
            cs.setString("product_id", obj.getProduct_id());
            cs.setString("transaction_id", obj.getTransaction_id());
            cs.setString("original_transaction_id", obj.getOriginal_transaction_id());
            cs.setString("unique_vendor_identifier", obj.getPurchase_date());
            cs.setString("unique_identifier", obj.getPurchase_date_ms());
            cs.setString("bvrs", obj.getPurchase_date_pst());
            cs.setString("item_id", obj.getIs_trial_period());
            cs.setString("bid", obj.getProduct_id());
            cs.setInt("Sanbox", sanbox);
            long tempo1 = Long.parseLong(obj.getOriginal_purchase_date_ms());
            cs.setDate("original_purchase_date", new java.sql.Date(tempo1));
            tempo1 = Long.parseLong(obj.getPurchase_date_ms());
            cs.setDate("purchase_date", new java.sql.Date(tempo1));
            cs.setInt("UserId", userid);
            cs.registerOutParameter("Error", Types.BIGINT);
            cs.registerOutParameter("Vippoint", Types.INTEGER);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.execute();
            JsonObject json = new JsonObject();
            json.addProperty("Error", cs.getLong("Error"));
            json.addProperty("Vippoint", cs.getInt("Vippoint"));
            json.addProperty("Vip", cs.getInt("Vip"));
            return json;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

    public void processIAP_ANDROID(JsonObject je, UserInfo actionUser, ServiceRouter serviceRouter) {
        try {
            LOGGER.info("==>IAPCommand==>processIAP_ANDROID" + actionUser.getPid() + (new Date()).toString() + "-" + je.get("signedData").getAsString() + "-" + je.get("signature").getAsString());
            String pakname = "";
            PaymentIAP obj = ActionUtils.gson.fromJson(je.get("signedData").getAsString(), PaymentIAP.class);
            try {
                pakname = je.get("pkgname").getAsString();
            } catch (Exception e) {
                pakname = obj.getPackageName();
            }
            boolean iapresult = Security.verifyPurchase(je.get("signedData").getAsString(), je.get("signature").getAsString(), pakname);
            long gold = 0;
            int vip = actionUser.getVIP();
            LOGGER.info("==>IAPCommand==>processIAP_ANDROID" + actionUser.getPid() + " - iap: " + iapresult);
            if (iapresult) {
                LOGGER.info("==>IAPCommand==>processIAP_ANDROID" + actionUser.getPid() + "==>Add Gold:" + obj.getProductId() + "-" + obj.getDeveloperPayload());
                int source = actionUser.getSource();
                JsonObject json = UpdateAGIAP(source, actionUser.getUsername(), obj.getProductId(), obj.getOrderId(), je.get("signedData").getAsString(), je.get("signature").getAsString(), pakname);
                if (json != null) {
                    gold = json.get("Error").getAsLong();
                    vip = json.get("Vip").getAsInt();
                    LOGGER.info("==>IAPCommand==>processIAP_IOS" + actionUser.getPid() + " - chipadd: " + gold + " - vip: " + vip + " - vippoint: " + json.get("Vippoint").getAsInt());
                }

                ServiceImpl.userController.UpdateAGCache(source, actionUser.getPid() - ServerDefined.userMap.get(source), gold, (short) vip, 0l);
                LOGGER.info("==>IAPCommand==>processIAP_ANDROID" + actionUser.getPid() + "==>Process_PaymentIAP: username " + actionUser.getUsername() + "- gold: " + gold);
                ServiceImpl.dicUser.get(actionUser.getPid()).IncrementMark(gold);

                if (ServiceImpl.dicUser.get(actionUser.getPid()).getTableId() > 0) {
                    GameDataAction gda = new GameDataAction(actionUser.getPid(), ServiceImpl.dicUser.get(actionUser.getPid()).getTableId());
                    JsonObject jo = new JsonObject();
                    jo.addProperty("evt", "ag_iap");
                    jo.addProperty("ag", gold);
                    jo.addProperty("vip", vip);
                    gda.setData(ByteBuffer.wrap(ActionUtils.gson.toJson(jo).getBytes("UTF-8")));
                    serviceRouter.dispatchToGame(ServiceImpl.dicUser.get(actionUser.getPid()).getGameid(), gda);
                }
            }
            JsonObject act = new JsonObject();
            act.addProperty("evt", "iapResult");
            if (gold > 0) {
                act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strPayment_Success_Pre", actionUser.getSource(), actionUser.getUserid())
                        + gold + ServiceImpl.actionUtils.getConfigText("strPayment_Success_Sur", actionUser.getSource(), actionUser.getUserid()));
            } else {
                act.addProperty("msg", ServiceImpl.actionUtils.getConfigText("strPayment_Err1", actionUser.getSource()));
            }
            act.addProperty("verified", Boolean.toString(iapresult));
            act.addProperty("goldPlus", gold);
            act.addProperty("chip", ServiceImpl.dicUser.get(actionUser.getPid()).getAG());
            act.addProperty("vip", vip);
            act.addProperty("signature", je.get("signature").getAsString());
            ClientServiceAction csa = new ClientServiceAction(actionUser.getPid(), 1, ActionUtils.gson.toJson(act).getBytes("UTF-8"));
            serviceRouter.dispatchToPlayer(actionUser.getPid(), csa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Update Gold InApp Purchase
    public JsonObject UpdateAGIAP(int source, String username, String productid, String orderId, String sigdata, String signature, String pakname) {
        SqlService instance = SqlService.getInstanceBySource(source);
        Connection conn = instance.getDbConnection();
        try {
            CallableStatement cs = conn.prepareCall("{call GameUpdateAGByIAP_Using(?,?,?,?,?,?,?,?) }");
            cs.setString("Username", username);
            cs.setString("ProductId", productid);
            cs.setString("OrderId", orderId);
            cs.setString("Sigdata", sigdata);
            cs.setString("Signature", signature);

            cs.registerOutParameter("Error", Types.BIGINT);
            cs.registerOutParameter("Vip", Types.INTEGER);
            cs.registerOutParameter("Vippoint", Types.INTEGER);
            cs.execute();
            JsonObject obj = new JsonObject();
            obj.addProperty("Error", cs.getLong("Error"));
            obj.addProperty("Vip", cs.getInt("Vip"));
            obj.addProperty("Vippoint", cs.getInt("Vippoint"));
            return obj;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            instance.releaseDbConnection(conn);
        }
    }

}
