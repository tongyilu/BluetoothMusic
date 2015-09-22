package com.spreadwin.btc.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import com.spreadwin.btc.MainActivity;

import android.util.Log;

public class HttpAssist {
	private static final String TAG = MainActivity.TAG;
	private static final int TIME_OUT = 10 * 10000000; // 超时时间
	private static final String CHARSET = "utf-8"; // 设置编码
	public static final String SUCCESS = "1";
	public static final String FAILURE = "0";
	public static boolean DEBUG = true;
	
	public static String openUrl(String strUrl) {
        //定义获取文件内容的URL   
		InputStream istream = null;  
		URL url = null;
		BufferedReader br = null;
		HttpURLConnection httpConn = null;  
		StringBuffer sb = new StringBuffer();

		LOG("strUrl == "+strUrl);

        try {
//			url = new URL("http://files.qidian.com/Author5/1785020/30186380.txt");
//			url = new URL("http://am.spreadwin.com:9005/gms_gw.txt");
//			url = new URL("http://ctl.spreadwin.com:9005/gms_gw.txt");
			url = new URL(strUrl);
            httpConn = (HttpURLConnection)url.openConnection();
			httpConn.setConnectTimeout(10*1000);
//			httpConn.setRequestMethod("GET");
			httpConn.setReadTimeout(30*1000);	
			int code = httpConn.getResponseCode();		
			String Encoding = httpConn.getContentEncoding();		
			LOG("code == "+code);

			if (code == HttpURLConnection.HTTP_OK) {
            	istream = httpConn.getInputStream();  		                      
            	br = new BufferedReader(    
                    	new InputStreamReader(istream));
            	String data = "";
            	while ((data = br.readLine()) != null) {
            		LOG("data == "+data);
                	sb.append(data+"\n");
            	}
				//断开连接
				httpConn.disconnect();
			} else {
				LOG("Request url failed");
				throw new RuntimeException("Request url failed");
			}
			LOG("sb.toString() == "+sb.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG("MalformedURLException ======"+e);
		} catch (IOException e) {
			e.printStackTrace();
			LOG("IOException ======"+e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG("Exception ======"+e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG("finally Exception ======");
			}
		}
		LOG("return ====== "+sb.toString());
		return sb.toString();		
	}
	public static void LOG(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}
}
