package com.vng.tfa.common;

import com.athena.database.UserController;
import com.athena.log.LoggerKey;
import com.athena.services.utils.ActionUtils;
import com.athena.services.vo.FacebookInviteContent;
import com.athena.services.vo.FacebookInviteData;
import com.cachebase.libs.queue.QueueManager;
import com.cachebase.queue.UserInfoCmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

/**
 *
 * @author root
 */
public class FaceService {

    private static final Lock _createLock = new ReentrantLock();
    private static FaceService _instance;
    private String urlFacebook = "https://graph.facebook.com/v2.5/me?fields=id,name,first_name,locale&method=GET&format=json&suppress_http_code=1&access_token=";
    private String urlFacebookPrivate = "http://fb.athena.vn/?token=";
    private String urlLoginFacebook = "https://graph.facebook.com/v2.6/me?fields=id,name,ids_for_business,token_for_business&format=json&method=get&pretty=0&suppress_http_code=1&access_token=";
    private String urlLoginFacebookPrivate = "http://fb.athena.vn/login.php?token=";

    private String urlFriendFacebook = "https://graph.facebook.com/v2.5/me/friends?fields=installed&limit=100&access_token=";
    private String urlFriendFacebookPrivate = "http://fb.athena.vn/friend.php?token=";

    public static Logger logger_login = Logger.getLogger(LoggerKey.LOGIN_DISCONNECT);

    public static FaceService getInstance(String urlConnection) {
        if (_instance == null) {
            _createLock.lock();
            try {
                if (_instance == null) {
                    _instance = new FaceService();
                }
            } finally {
                _createLock.unlock();
            }
        }
        return _instance;
    }

    public String GetContentFaceInvite(String accesstoken) {
        try {
            //System.out.println("FaceThai==>Get Content For Login:" + accesstoken) ;
            int timeout = 10;
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout((timeout - 6) * 1000)
                    .setConnectionRequestTimeout((timeout - 6) * 1000)
                    .setSocketTimeout(6000).build();
            CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            String url = "https://graph.facebook.com/me/apprequests?access_token=";
            url = url + accesstoken;
            //System.out.println("==>GetContentFaceInvite==>url: "+url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response != null) {
                //System.out.println("==>Cam: ResquestFacebook response != null");
                java.io.InputStream inputstream = response.getEntity().getContent();
                String line = convertStreamToString(inputstream);
                inputstream.close();
                response.close();
                httpclient.close();
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String GetContentLoginFromFaceGraph(String accesstoken) {
        try {
//    		System.out.println("FaceThai==>Get Content For Login:" + accesstoken) ;
            RequestConfig config = getRequestConfig();
            CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            String url = urlFacebook + accesstoken;
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response != null) {
                //System.out.println("==>Cam: ResquestFacebook response != null");
                java.io.InputStream inputstream = response.getEntity().getContent();
                String line = convertStreamToString(inputstream);
                inputstream.close();
                response.close();
                httpclient.close();
                logger_login.info("face" + line);
                return line;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        try {
            RequestConfig config = getRequestConfig();
            CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            String url = urlFacebookPrivate + accesstoken;

            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response != null) {
                //System.out.println("ResquestFacebook response != null");
                java.io.InputStream inputstream = response.getEntity().getContent();
                String line = convertStreamToString(inputstream);
                inputstream.close();
                response.close();
                httpclient.close();
                logger_login.info("service" + line);
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private RequestConfig getRequestConfig() {
        try {
            return RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(9000).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String GetContentLoginFromFaceGraph_New(String accesstoken) {
        try {
//    		System.out.println("FaceThai==>Get Content For Login:" + accesstoken) ;
            int timeout = 8;
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout((timeout - 4) * 1000)
                    .setConnectionRequestTimeout((timeout - 4) * 1000)
                    .setSocketTimeout(6000).build();
            CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            String url = urlLoginFacebook + accesstoken;
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response != null) {
                java.io.InputStream inputstream = response.getEntity().getContent();
                String line = convertStreamToString(inputstream);
                inputstream.close();
                response.close();
                httpclient.close();
                return line;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        try {
            RequestConfig config = getRequestConfig();
            CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            String url = urlLoginFacebookPrivate + accesstoken;

            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response != null) {
                //System.out.println("ResquestFacebook response != null");
                java.io.InputStream inputstream = response.getEntity().getContent();
                String line = convertStreamToString(inputstream);
                inputstream.close();
                response.close();
                httpclient.close();
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String GetContentFriendFromFaceGraph(String accesstoken) {
        try {
            int timeout = 8;
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout((timeout - 4) * 1000)
                    .setConnectionRequestTimeout((timeout - 4) * 1000)
                    .setSocketTimeout(6000).build();
            CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            String url = urlFriendFacebook + accesstoken;
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response != null) {
                java.io.InputStream inputstream = response.getEntity().getContent();
                String line = convertStreamToString(inputstream);
                inputstream.close();
                response.close();
                httpclient.close();
                return line;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        try {
            RequestConfig config = getRequestConfig();
            CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            String url = urlFriendFacebookPrivate + accesstoken;

            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response != null) {
                //System.out.println("ResquestFacebook response != null");
                java.io.InputStream inputstream = response.getEntity().getContent();
                String line = convertStreamToString(inputstream);
                inputstream.close();
                response.close();
                httpclient.close();
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertStreamToString(java.io.InputStream inputstream) {
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputstream));
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total.toString();
    }

    public void processInvite(int source, int userid, String token, long faceid) {
        try {
            String content = GetContentFaceInvite(token);
            logger_login.info("==>Proccess Invite0: " + content.length() + " - userid: " + userid + " - token: " + token + " - faceid: " + faceid);
            if (content.length() > 5) {
                logger_login.info("==>Proccess Invite1: " + content);
                FacebookInviteContent lsInvite = ActionUtils.gson.fromJson(content, FacebookInviteContent.class);
                logger_login.info("==>Proccess Invite2: " + lsInvite.getData().size());
                int total = lsInvite.getData().size();
                int count = 0;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -1);
                Date time = cal.getTime();
                for (int i = total - 1; i >= 0; i--) {
                    FacebookInviteData d = lsInvite.getData().get(i);
                    //System.out.println("==>processInvite==>Check Time: "+format.parse(d.getCreated_time())+"  - curtime: "+time+" - compare: "+format.parse(d.getCreated_time()).after(time));
                    if (format.parse(d.getCreated_time()).after(time)) {
                        logger_login.info("==>Proccess Invite3: appid: " + d.getApplication().getId() + " - toid: " + Long.parseLong(d.getTo().getId()));
                        if (d.getApplication().getId().equals("324897461198400") // https://play.google.com/store/apps/details?id=sea.indo.hokiplay
                                && Long.parseLong(d.getTo().getId()) == faceid) {
                            boolean t = true;
                            for (int j = i; j < total; j++) {
                                FacebookInviteData dc = lsInvite.getData().get(j);
                                if (dc.getFrom().getId().equals(d.getFrom().getId())) {
                                    t = false;
                                    break;
                                }
                            }
                            if (t) {
                                logger_login.info("==>Invite:" + d.getFrom().getId() + "-" + d.getFrom().getName() + "-" + d.getTo().getId() + "-" + d.getTo().getName());
                                if (i == (total - 1)) {
                                    GameSiamFacebookInvite(source, Long.parseLong(d.getFrom().getId()),
                                            Long.parseLong(d.getTo().getId()), d.getFrom().getName(), d.getTo().getName(), 1);
                                } else {
                                    GameSiamFacebookInvite(source, Long.parseLong(d.getFrom().getId()),
                                            Long.parseLong(d.getTo().getId()), d.getFrom().getName(), d.getTo().getName(), 0);
                                }
                            }
                        }
                        count++;
                        if (count > 200) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GameSiamFacebookInvite(int source, long fromid, long toid, String fromuser, String touser, int typeIn) {
        try {
            logger_login.info("==>Que Invite Face:" + fromid + "-" + toid + "-" + fromuser + "-" + touser + "-" + typeIn);
            UserInfoCmd cmd = new UserInfoCmd("gameIFriendInvite", source, fromid, toid, fromuser, touser, typeIn);
            QueueManager.getInstance(UserController.queuename).put(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
