package com.spreadwin.btc.utils;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.SyncService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 翼卡在线
 * @author Tong Yilu 
 */
public class ECarOnline {
	public static final String TAG = "ecar";
	
    public static final boolean DEBUG = true;    
	
	/**
	 * 翼卡对应接口参数
	 */
	private static String ACTION_ECAR_CALL_SEND = "com.android.ecar.send";//接受来自翼卡的广播ACTION

	private static String ACTION_ECAR_CALL_RECV = "com.android.ecar.recv";//发送给翼卡的广播ACTION	
	
	private static String ECAR_CMD = "ecarSendKey";//CMD参数所用的标示
	
	private static String ECAR_TYPE = "cmdType";//TYPE参数的标示
	
	private static String ECAR_KEYSET = "keySet";//广播的参数
	
	private static String TYPE_STANDCMD = "standCMD";//_TYPE_ = “standCMD”时为普通广播
	
	/**
	 * 查询当前蓝牙状态
	 */
	private String BluetoothQueryState = "BluetoothQueryState";
	
	/**
	 * 蓝牙连接状态
	 */
	private String BluetoothState = "BluetoothState";
	
	/**
	 * 用蓝牙拨打电话接口
	 */
	private String BluetoothMakeCall = "BluetoothMakeCall";
	
	/**
	 * 蓝牙通话状态
	 */
	private String CallState = "CallState";
	
	/**
	 * 蓝牙电话挂机接口
	 */
	private String BluetoothEndCall = "BluetoothEndCall";
	
	/**
	 * true为一键通的一键通功能
	 */
	public static boolean mECarCall = false;
	
	private static final int BT_OFF = 0; //关闭
	
	private static final int BT_DISCONNECT = 1; //未连接蓝牙设备
	
	private static final int BT_CONNECTED = 2; //已连接蓝牙设备
	
	public static final int BT_CALL_IDLE = 3; //挂断
	
	public static final int BT_CALL_RINGING = 4; //来电响铃
	
	public static final int BT_CALL_OFFHOOK = 5; //去电接通
	
	private String state ="state";
    
    private static ECarOnline mECarOnline;
    
    private ECarStatusListener mECarStatusListener;
    
    private BroadcastReceiver mECarReceiver;
    
    private SyncService mSyncService;
    
    public ECarOnline(SyncService syncService) {
    	this.mSyncService = syncService;  
    	registerReceiver();
	}

	public static ECarOnline getInstance(SyncService syncService){
    	if (mECarOnline == null) {
			mECarOnline = new ECarOnline(syncService);
		}
		return mECarOnline;    	
    }
	
	public void registerReceiver() {
		if (mECarReceiver  == null) {
			mLog("ECarOnline registerReceiver");
			mECarReceiver = new BroadcastReceiver(){
				@Override
				public void onReceive(Context context, Intent intent) {
					String cmd = intent.getStringExtra(ECAR_CMD);
					String type = intent.getStringExtra(ECAR_TYPE);				
					mLog("cmd =="+cmd+"; type =="+type);
					if (cmd.equals(BluetoothQueryState)) {					
						onReturnBfpStatus();
					}else if (cmd.equals(BluetoothMakeCall)) {						
						onCallBt(intent);
					}else if (cmd.equals(BluetoothEndCall)) {
						BtcNative.hangupCall();
					}
				}				
			};			
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_ECAR_CALL_SEND);
			mSyncService.registerReceiver(mECarReceiver, filter);
		}
	}
    
	/**
	 * 拨打蓝牙电话
	 * @param intent
	 */
	protected void onCallBt(Intent intent) {
		String temp_key = intent.getStringExtra(ECAR_KEYSET);
		String name = intent.getStringExtra("name");;
		String number = intent.getStringExtra("number");;
		mLog("onCallBt key =="+temp_key+"; name =="+name+"; number =="+number);	
		if (name == null && number == null) {
			mLog("The name and number is empty");
			return;
		}
		if (mSyncService.mBfpStatus == BtcGlobalData.BFP_CONNECTED) {
			onECarCall(name,number);			
		}else{
			onReturnBfpStatus();
		}	
	}
	
	/**
	 * 
	 * @param name
	 * @param number
	 */
	private void onECarCall(String name, String number) {
		
		mLog("dialCall ==" + number);
		if (number.length() > 0) {
			BtcNative.dialCall(number);
		}
		mECarCall = true;
//		Intent mCustomerIntent = new Intent();
//		mCustomerIntent.setAction(mSyncService.ACTION_BTC_CALL);
//		
//		if (number != null) {
//			mLog("onECarCall number=="+number);
//			mCustomerIntent.putExtra("call_number", number);				
//		}
//		if (name != null) {
//			mLog("onECarCall name=="+name);
//			mCustomerIntent.putExtra("call_name", name);
//		}
//		mECarCall = true;
//		mCustomerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		mSyncService.startActivity(mCustomerIntent);			
	}

	/**
	 * 返回蓝牙连接状态
	 */
	public void onReturnBfpStatus() {		
		Intent intent = new Intent(ACTION_ECAR_CALL_RECV);
		intent.putExtra(ECAR_CMD, BluetoothState);
		intent.putExtra(ECAR_TYPE, TYPE_STANDCMD);
		intent.putExtra(ECAR_KEYSET, state);
		if (mSyncService.mBfpStatus+1 == BT_CONNECTED) {
			intent.putExtra(state, String.valueOf(BT_CONNECTED));			
		}else{		
			intent.putExtra(state, String.valueOf(BT_DISCONNECT));	
		}		
		mLog("onReturnBtStatus state =="+intent.getStringExtra(state));
		mSyncService.sendBroadcast(intent);
	}

	/**
	 * 返回蓝牙通话状态
	 * @param mCallStatus 
	 */
	public void onReturnCallState(int mCallStatus) {
		if (!mECarCall) {
			mLog("onReturnCallState mECarCall is false");
			return;
		}
		Intent intent = new Intent(ACTION_ECAR_CALL_RECV);
		intent.putExtra(ECAR_CMD, CallState);
		intent.putExtra(ECAR_TYPE, TYPE_STANDCMD);
		intent.putExtra(ECAR_KEYSET, state);
		switch (mCallStatus) {
			case BtcGlobalData.CALL_IN:
				intent.putExtra(state, String.valueOf(BT_CALL_RINGING));	
				break;			
			case BtcGlobalData.IN_CALL:
				intent.putExtra(state, String.valueOf(BT_CALL_OFFHOOK));			
				break;			
			case BtcGlobalData.NO_CALL:
				intent.putExtra(state, String.valueOf(BT_CALL_IDLE));	
				mECarCall = false;
				break;
		}
		mLog("onReturnCallState state =="+intent.getStringExtra(state));
		mSyncService.sendBroadcast(intent);
	}
	
	public void sendIntent(Intent intent) {
		
	}
	
	public void close() {
		mLog("ECarOnline close");
		if(mECarReceiver != null){
			mSyncService.unregisterReceiver(mECarReceiver);
			mECarReceiver = null;
		}
		mECarOnline = null;
	}
	
	
	public void onECarStatusChangeListener(ECarStatusListener _ecarListener){
		mECarStatusListener = _ecarListener;
		registerReceiver();
	}
	
	public interface ECarStatusListener{
		
		public void onECarStatusChange();
	}
	
	private static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}

}
