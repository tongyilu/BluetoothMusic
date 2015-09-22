package com.spreadwin.btc.utils;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;


import android.content.Context;
import android.net.ParseException;
import android.util.Log;

public class HttpUtil {  
    final static String TAG = "WebSocketService";
    
      
    public static String post(String url, Map<String, String> params) {  
        DefaultHttpClient httpclient = new DefaultHttpClient();  
        String body = null;  
          
        Log.d(TAG,"create httppost:" + url);  
        HttpPost post = postForm(url, params);  
          
        body = invoke(httpclient, post);  
          
        httpclient.getConnectionManager().shutdown();  
          
        return body;  
    }
    
    public static String post(String url, Map<String, String> params, String data) {  
        DefaultHttpClient httpclient = new DefaultHttpClient();  
        String body = null;  
          
        Log.d(TAG,"create httppost:" + url);  
        HttpPost post = postForm(url, params);
        
        HttpEntity entity;
		
		entity = new ByteArrayEntity(data.getBytes());

        post.setEntity(entity);
        
        body = invoke(httpclient, post);  
          
        httpclient.getConnectionManager().shutdown();  
          
        return body;  
    }
      
    public static String get(String url) {  
        DefaultHttpClient httpclient = new DefaultHttpClient();  
        String body = null;  
          
        Log.d(TAG,"create httppost:" + url);  
        HttpGet get = new HttpGet(url);  
        body = invoke(httpclient, get);  
          
        httpclient.getConnectionManager().shutdown();  
          
        return body;  
    }  
          
      
    private static String invoke(DefaultHttpClient httpclient,  
            HttpUriRequest httpost) {  
          
		HttpResponse response = sendRequest(httpclient, httpost);
		if (response != null) {
			String body = paseResponse(response);

			return body;
		} else
			return "";
    }  
  
    private static String paseResponse(HttpResponse response) {  
        Log.d(TAG,"get response from http server..");  
        HttpEntity entity = response.getEntity();  
          
        Log.d(TAG,"response status: " + response.getStatusLine());  
        String charset = EntityUtils.getContentCharSet(entity);  
        Log.d(TAG, "charset: " + charset);  
          
        String body = null;  
        try {  
            body = EntityUtils.toString(entity);  
            Log.d(TAG, "body: " + body);  
        } catch (ParseException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
          
        return body;  
    }  
  
    private static HttpResponse sendRequest(DefaultHttpClient httpclient,  
            HttpUriRequest httpost) {  
        Log.d(TAG,"execute post...");  
        HttpResponse response = null;  
          
        try {  
            response = httpclient.execute(httpost); 
            
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return response;  
    }  
  
    private static HttpPost postForm(String url, Map<String, String> params){  
          
        HttpPost httpost = new HttpPost(url);  
        List<NameValuePair> nvps = new ArrayList <NameValuePair>();  
          
        Set<String> keySet = params.keySet();  
        for(String key : keySet) {  
            nvps.add(new BasicNameValuePair(key, params.get(key)));  
        }  
          
        try {  
            Log.d(TAG,"set utf-8 form entity to httppost");  
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }  
          
        return httpost;  
    }
    
	public static String parseTicketResult(String json) {
		StringBuffer ret = new StringBuffer();
		String ticket = "";
		try {
			JSONTokener tokener = new JSONTokener(json);
			JSONObject joResult = new JSONObject(tokener);
			
			 ticket = joResult.getString("ticket");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return ticket;
	}
			
}