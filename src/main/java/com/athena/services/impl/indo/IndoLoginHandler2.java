/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.impl.indo;

import com.athena.database.ServerDefined;
import com.athena.database.UserController;
import com.athena.services.api.ServiceContract;
import com.athena.services.impl.ServiceImpl;
import com.athena.services.promotion.PromotionHandler;
import com.athena.services.utils.ActionUtils;
import com.athena.services.utils.Operator;
import com.athena.services.vo.UserInfo;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceContext;
import com.dst.ServerSource;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 *
 * @author UserXP
 */
public class IndoLoginHandler2 implements LoginHandler {
	//public static Logger log = Logger.getLogger("Siamplaylog");
	private static Logger loggerLogin_ = Logger.getLogger("LoginandDisconnect");
    private UserController uController = new UserController();
    private PromotionHandler promotionController = new PromotionHandler();
    private Gson gson = new Gson();

    private ServiceContext context;
    
    public IndoLoginHandler2(ServiceContext context) {
    	this.context = context ;
	}
//    private ServiceContext context;
//    public ThaiLoginHandler(ServiceContext context) {
//		// TODO Auto-generated constructor stub
//    	this.context = context;
//	}
	//public Environment env = Environment.PRODUCTION;
    @Override
    public LoginResponseAction handle(LoginRequestAction action) {
    	UserInfo ulogin = new UserInfo() ;
    	try {
    		//Thread.sleep(5000);
    		loggerLogin_.info("==>StartLogin Indo:" + (new Date()).toString() +": "+ action.getUser() + "---" + action.getOperatorid() + "-" + action.getPassword());        	
        	ulogin = gson.fromJson(action.getUser(), UserInfo.class);
        	ulogin.setOperatorid((short)action.getOperatorid()) ;
            ulogin.setFacebookid(0l) ;
            ulogin.setGoogleid(0l) ;
            ulogin.setSource((short)10) ;
            ulogin.setsIP(action.getRemoteAddress().getAddress().getHostAddress());
            
            //Check Session MTO from cache
            String keyCache = "zid_" + ulogin.getUserIdZing() + action.getPassword();
            String userIdZingFromCache = uController.getUserIdZingFromCache(keyCache);
            if(userIdZingFromCache == null || userIdZingFromCache.equals("") || !userIdZingFromCache.equals(ulogin.getUserIdZing()))
            {
            	String strCheckLogin = GetContentCheckLogin(ulogin.getUserIdZing(), action.getPassword()) ;
                loggerLogin_.info("==>Start Verify User/Pass:" + strCheckLogin + "-" + ulogin.getUserIdZing());
                if (strCheckLogin.length() == 0) {
                	LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-3);
                    lra.setErrorMessage("Kata sandi dan nama akun tidak sama 0.");
                    ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                    ulogin = null ;
                    return lra;
                }
                com.athena.services.vo.UserLoginThai userThai = ActionUtils.gson.fromJson(strCheckLogin, com.athena.services.vo.UserLoginThai.class) ;
                if (userThai.getReturnCode() != 1) {
                	LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-3);
                    lra.setErrorMessage("Kata sandi dan nama akun tidak sama 1.");
                    ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                    ulogin = null ;
                    return lra;
                }
                uController.addUserIdZingToCache(keyCache, ulogin.getUserIdZing());
            }
            
        	/*****/
        	//Check Login bt
            loggerLogin_.info("==>Verify User/Pass OK ==> Get userid:" + (new Date()).toString() + ": "+ action.getUser() + ulogin.getUserIdZing() + "-" + ulogin.getDeviceId());
        	int userid = uController.GameGetUseridSiam(ServerSource.IND_SOURCE, ulogin.getUserIdZing(), ulogin.getDeviceId()) ;
        	loggerLogin_.info("==>Verify User/Pass OK:" + (new Date()).toString() +": "+ action.getUser()  + ulogin.getUsername() + "-" + userid);
        	if (userid == 0) {
        		LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-3);
                lra.setErrorMessage("Kata sandi dan nama akun tidak sama.");
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
                return lra;
        	} else 
        		ulogin.setUserid(userid); //UserId của 3C
        	loggerLogin_.info("==>Verify User/Pass OK:" + ulogin.getUsername() + "-" + action.getPassword() + "-" + userid);
        	uController.GetUserInfoByUserid(ServerSource.IND_SOURCE, ulogin, ServiceImpl.ipAddressServer);
        	ulogin.setTinyurl(ulogin.getUsername()) ;
        	loggerLogin_.info("==>Pass Cache:" + ulogin.getUserid()) ;
        	ulogin.setUserid(ulogin.getUserid()) ;
//        	PublicClientRegistryService clientRegistryService = context.getParentRegistry().getServiceInstance(PublicClientRegistryService.class);
//        	ulogin.setUsername(ulogin.getUsername().toLowerCase());
        	loggerLogin_.info("==>Connect 2:" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid() + "-" + ulogin.getAG().intValue()) ;
        	if(!ulogin.isBanned()) {
            	if(ulogin.getIsOnline() == 0) {
            		if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
            			uController.RestoreHighLow(ServerSource.IND_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE)) ;
        				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
        				ulogin.setAGHigh(0);
        				ulogin.setAGLow(0);
        			}
            		return processReturnLoginOK(ulogin, action, 1);
                } else if (ulogin.getIdolName().equals(ServiceImpl.ipAddressServer)) {
                	int pid = 0;
                	pid = ServiceImpl.CheckUserOnline(ulogin.getUserid(),ulogin.getGameid());
                	loggerLogin_.info("==>Login==>Check is Online:" + pid) ;
                	if(pid>0) {
                		if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                			uController.RestoreHighLow(ServerSource.IND_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE)) ;
            				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
            				ulogin.setAGHigh(0);
            				ulogin.setAGLow(0);
            			}
                		return processReturnLoginOK(ulogin, action, 2);
                	} else if(pid==0) {
                		/*LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-1);
                        lra.setErrorMessage("Bạn đang ở trong bàn chơi khác.");
                        ulogin = null ;
                        return lra;*/
                		if(ActionUtils.checkReconnectIndo(ulogin.getGameid()))
                		{
//               				 || (ulogin.getGameid() == 8012) || (ulogin.getGameid() == 8010)
//               				 || (ulogin.getGameid() == 8004) || (ulogin.getGameid() == 8007)) { //Ap dung Reconnect voi game poker
                			//int tableid = ulogin.getTableId();
                			int tableid = ServiceImpl.GetTableIdUserOnline(ulogin.getUserid(), ulogin.getGameid()) ;
                       		if (tableid > 0) {
                       			if ((ulogin.getAGHigh() > 0 || ulogin.getAGLow() > 0) && (!ServiceImpl.CheckHighLow(ulogin.getUserid().intValue()))) {
                        			uController.RestoreHighLow(ServerSource.IND_SOURCE,ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE)) ;
                    				ulogin.setAG(ulogin.getAG().longValue() + ulogin.getAGHigh() + ulogin.getAGLow());
                    				ulogin.setAGHigh(0);
                    				ulogin.setAGLow(0);
                    			}
                       			return processReturnLoginOK(ulogin, action, 3);
                       		} else if (tableid < -1) {
   	                    		LoginResponseAction lra = new LoginResponseAction(false, 0);
   	                            lra.setErrorCode(-5); 
   	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
   	                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
   	                            ulogin = null ;
   	                            return lra;
                       		} else {
                       			LoginResponseAction lra = new LoginResponseAction(false, 0);
   	                            lra.setErrorCode(-1);//Ban dang o trong ban choi
   	                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
   	                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
   	                            ulogin = null ;
   	                            return lra;
                       		}
                   		} else {
                    		LoginResponseAction lra = new LoginResponseAction(false, 0);
                            lra.setErrorCode(-1); //Ban dang o trong ban choi
                            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err1",ulogin.getSource()));
                            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                            ulogin = null ;
                            return lra;
                   		}
                	} else {
                		loggerLogin_.info("==>Gui ve client Disconnect");
                		uController.UserDisconnected(ServerSource.IND_SOURCE, ulogin.getUserid());
                		LoginResponseAction lra = new LoginResponseAction(false, 0);
                        lra.setErrorCode(-1); //Ban dang o trong ban choi
                        lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err3",ulogin.getSource()));
                        ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                        ulogin = null ;
                        return lra;
                	}
                } else {
                	if (uController.GetIsOnlineFromDB(ServerSource.IND_SOURCE,ulogin.getUserid()) == 0) {
                		uController.RemoveUserInfoByUserid(ServerSource.IND_SOURCE, ulogin.getUserid() );
                		loggerLogin_.info("==>UserIsOnline:" + gson.toJson(ulogin)) ;
                	}
                    LoginResponseAction lra = new LoginResponseAction(false, 0);
                    lra.setErrorCode(-1);
                    lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err3",ulogin.getSource()));
                    ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                    ulogin = null ;
                    return lra;
                }
            } else {
            	LoginResponseAction lra = new LoginResponseAction(false, 0);
                lra.setErrorCode(-4); //Bi khoa nick vi vi pham
                lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strConnect_Err4",ulogin.getSource()));
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                ulogin = null ;
                return lra ;
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        	loggerLogin_.error("===>Error==>IndoLoginHandler:" + ex.getMessage()) ;
        	LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5); //Gap van de khi connect server
            ret.setErrorMessage("Anda telah keluar akun. Tunggulah 30 menit.");
            loggerLogin_.info("==>Login Indo Zing Finish Fail:" + action.getUser()) ;
            ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
            return ret;
        }
    }
    
    private LoginResponseAction processReturnLoginOK(UserInfo ulogin, LoginRequestAction action, int type) {
		try{
			// type = 0 ; is Register
			// type = 1 ulogin.getIsOnline() == 0
			// type = 2 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
			// type = 3 ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
			// type = 4 //Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
			
			LoginResponseAction lra = new LoginResponseAction(true,"", ulogin.getUserid());
            ulogin.setPid(lra.getPlayerid());
	        lra.setScreenname(ulogin.getUsername());
	        lra.setErrorCode(0);
			if(type == 0){	 // is Register			
	            ServiceImpl.AddUserOnline(ulogin);           
	            lra.setErrorMessage(ServiceImpl.actionUtils.getConfigText("strRegister_Success",ulogin.getSource()));
	            loggerLogin_.info("==>IndoLoginHandler2==>FinishLogin Register:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;	    
			}else if(type == 1){ // ulogin.getIsOnline() == 0
                ServiceImpl.AddUserOnline(ulogin);
                uController.UpdateIsOnlineToCache(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
               //Lay danh sach new Adminpromotion
                if (action.getOperatorid() == Operator.OPERATOR_INDO1)
                	promotionController.GetListPromotionDB(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE)) ;
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                loggerLogin_.info("==>IndoLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername()) ;							
	        }else if (type == 2){ // ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid > 0
                uController.UpdateIsOnlineToCache(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
                ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
                loggerLogin_.info("==>IndoLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername()) ;	        	
	        }else if (type == 3){ //ulogin.getIdolName().equals(ServiceImpl.ipAddressServer) pid = 0 reconnect tableid > 0
		        ServiceImpl.UpdateUserOnline(ulogin);
       			uController.UpdateIsOnlineToCache(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
       			ServiceImpl.dicCurrent.remove(ulogin.getUserIdZing()) ;
       			loggerLogin_.info("==>IndoLoginHandler2==>Tra ve client login thanh cong1:" + action.getUser() + "-" + ulogin.getUsername()) ;
	        }else if (type == 4){ // Pid == -1 ==> Khong co trong dic ==> Add lai vao dic va cho Login
	            ServiceImpl.AddUserOnline(ulogin);
	            uController.UpdateIsOnlineToCache(ServerSource.IND_SOURCE, ulogin.getUserid() - ServerDefined.userMap.get(ServerSource.IND_SOURCE), ServiceImpl.ipAddressServer,ulogin.getGameid(), ulogin.getOperatorid(), ulogin.getsIP());
	
	            loggerLogin_.info("==>IndoLoginHandler2==>FinishLogin==>NotDic:" + action.getUser() + "-" + ulogin.getIsOnline() + "-" + ulogin.getUsername() + "-" + ulogin.getIdolName() + "-" + ServiceImpl.ipAddressServer + "-" + ulogin.getUserid()) ;	  
			}  
			
			ServiceImpl impl = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
			String data = impl.getLoginGameData(ulogin.getGameid(), ulogin, ulogin.getUserid().intValue(),type);
			loggerLogin_.info("==>Data:\n"+data);
			lra.setData(data.getBytes("UTF-8"));
			
            return lra;
		}catch(Exception e){
			e.printStackTrace();
			LoginResponseAction ret = new LoginResponseAction(false, 0);
            ret.setErrorCode(-5);
            //ret.setErrorMessage("Có lỗi xảy ra, xin vui lòng thử lại!");
            ret.setErrorMessage("บัญชีของท่านกำลังออกแล้ว กรุณารอประมาณ 30 นาทีค่ะี");
           	return ret;
		}
	}
    
    private String GetContentCheckLogin(String UserId, String sessionId){
    	try {
    		String secret = "SU4KUY7MfDNzeKJ7XFNAn298XAfTx6n6N6XKXKf2iWN3USf6";
            //UserId = "20160112164001019";
            //sessionId = "GU_xAEkjGoGfbvpGsfg";
            String USER_AGENT = "Mozilla/5.0";
            
            long timeStamp = ((long)new java.util.Date().getTime() / 1000);
            
            byte[] bytesOfMessage = (secret + timeStamp + UserId + sessionId).getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);
            
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < thedigest.length; i++) {
                String hex=Integer.toHexString(0xff & thedigest[i]);
                if(hex.length()==1) buffer.append('0');
                buffer.append(hex);
            }
            
            String sig = buffer.toString();
            String url = "http://checksession.siamplayindo.g6.zing.vn/?do=PartnerSession.checkSession&userID=" + UserId + "&sessionID="+sessionId+"&timestamp="+timeStamp+"&sig="+ sig;
            
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

//            int responseCode = con.getResponseCode();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
                
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            
            in.close();

            return response.toString() ;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
        return "" ;
    }
}
