package com.athena.services.ina;
import java.io.BufferedReader;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;


//import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
//import java.io.InputStreamReader;
import java.net.HttpURLConnection;
//import java.net.URL;

//import com.google.gson.JsonObject;


public class VerifyIAPApple {

	static String sandboxLink = "https://sandbox.itunes.apple.com/verifyReceipt";
	static String realLink = "https://buy.itunes.apple.com/verifyReceipt";
	public static boolean isSandboxIAP  = false;

	public static String verify(String receipt_encoded64, boolean isSandbox) {
		String json = "{\"receipt-data\" : \"" + receipt_encoded64 + "\" }";
        URL url;
        HttpURLConnection connection = null;
		try {
			//Create connection
			if (isSandboxIAP)
				url = new URL(sandboxLink);
			else
				url = new URL(realLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" + Integer.toString(json.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            
            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(json);
            wr.flush();
            wr.close();
            //Get Response	
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
//                response.append('\r');
            }
            rd.close();
            String result1 = response.toString();
            return result1;
		} catch (Exception ex) {
			System.out.println("==>Error:" + ex.getMessage()) ;
			return "";
		}		
	}
	public static String verify(String receipt_encoded64) {
		String json = "{\"receipt-data\" : \"" + receipt_encoded64 + "\" }";
        URL url;
        HttpURLConnection connection = null;
		try {
			url = new URL(sandboxLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" + Integer.toString(json.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            
            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(json);
            wr.flush();
            wr.close();
            //Get Response	
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
//                response.append('\r');
            }
            rd.close();
            String result1 = response.toString();
            return result1;
		} catch (Exception ex) {
//			System.out.println("==>Error:" + ex.getMessage()) ;
			ex.printStackTrace();
			return "";
		}		
	}
}
