package com.athena.services.impl.auth;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.ArrayList;
import java.util.List;



public class AuthServiceImpl implements AuthService{
    Logger _logger = Logger.getLogger("Debug_service");

    public String test(){
        try{
            String url = "https://api.shweyang.com/graph/ccu";

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);

            HttpResponse response = null;
            InputStream inputStream = null;
            try {
                // Execute HTTP Post Request
                response = httpclient.execute(get);

                inputStream = response.getEntity().getContent();
                String body = IOUtils.toString(inputStream, "UTF-8");
                Thread.sleep(1000);

                return body;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(inputStream != null)
                    inputStream.close();
                if(response != null)
                    response.getEntity().consumeContent();
                httpclient.getConnectionManager().shutdown();
            }
        }catch (Exception e){
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
            Logger.getLogger("LoginandDisconnect").info("err "+e.getMessage(), e);
        }

        return "";
    }
    private RequestConfig getRequestConfig() {
        try{
            return RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(9000).build();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String requestAuth(BasicUserAuth userAuth){
            Logger.getLogger("LoginandDisconnect").info("requestAuth: "+ userAuth.toString());
        try{
            String url = "http://10.148.0.4:8080/oauth/token";
            String encoded = DatatypeConverter.printBase64Binary("shweyang:hfgndhhkg".getBytes("UTF-8"));
//            RequestConfig config = getRequestConfig();
            CloseableHttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Authorization", "Basic " + encoded);
            CloseableHttpResponse response = null;
            InputStream inputStream = null;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("grant_type", "password"));
                nameValuePairs.add(new BasicNameValuePair("username", userAuth.toString()));
                nameValuePairs.add(new BasicNameValuePair("password", userAuth.getType().toString()));
                nameValuePairs.add(new BasicNameValuePair("client_id", "shweyang"));
                nameValuePairs.add(new BasicNameValuePair("client_secret", "hfgndhhkg"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);

                inputStream = response.getEntity().getContent();
                String body = IOUtils.toString(inputStream, "UTF-8");

                JSONObject jsonObject = new JSONObject(body);
                return (String)jsonObject.get("access_token");
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(inputStream != null)
                    inputStream.close();
                if(response != null)
                    response.close();
                httpclient.close();
            }
        }catch (Exception e){
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
            Logger.getLogger("LoginandDisconnect").info("err "+e.getMessage(), e);
        }
        return "";
    }
}
