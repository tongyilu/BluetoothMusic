package com.spreadwin.btc;

import android.util.Log;

public class BtcNative {
	static String TAG = "BTC";
	
	static {
		Log.d(TAG, "load jni library");
		try {
			System.loadLibrary("btc_spreadwin");  			
		} catch (Exception e) {
			Log.d(TAG, "load jni library is error");
		}
	} 
	
	static native int initBtc();
	public static native int getPairStatus();
	public static native int getBfpStatus();
	public static native int getCallStatus();
	public static native int getA2dpStatus();
	public static native int getSyncStatus();
	static native int startSyncPhoneBook(int type);
	public static native int getPhoneBookRecordNum(int type);
	public static native String getPhoneBookRecordNameByIndex(int type, int index);
	static native String getPhoneBookRecordNumberByIndex(int type, int index);
	static native int writeCommands(int index, String param);
	public static native int playMusic();
	public static native int pauseMusic();
	public static native int lastSong();
	public static native int nextSong();
	public static native int enterPair();
	public static native int disconnectPhone();
	/**
	 * 接听
	 * @return
	 */		
	public static native int answerCall();
	/**
	 * 拒听
	 * @return
	 */		
	public static native int denyCall();
	/**
	 * 挂断
	 * @return
	 */			
	public static native int hangupCall();
	public static native int redialCall();
	public static native int muteCall();
	public static native String getPhoneName();
	public static native String getCallNumber();
	public static native String getPairDeviceName(int index);
	public static native String getPairDeviceMac(int index);
	public static native int dialCall(String number);
	public static native int dtmfCall(String dtmf);
	public static native int setVolume(int vol);
	public static native int getVolume();
	public static native int getPowerStatus();
	public static native String getDeviceName();
	
}
