package com.spreadwin.btc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.spreadwin.btc.utils.PhoneBookInfo;
import com.spreadwin.btc.utils.BtcGlobalData;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.media.AudioManager;
import android.net.rtp.AudioStream;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SyncService extends Service {
	public  static final String TAG = MainActivity.TAG;
	public  static final boolean DEBUG = true;
	
	
    int index = 0;    
    int bfpUpdateTime = 0;    
	int mA2dpStatus = BtcGlobalData.A2DP_DISCONNECT;
	int mBfpStatus = BtcGlobalData.BFP_DISCONNECT;
	int mCallStatus = BtcGlobalData.NO_CALL;
	int mCallStatusOld = BtcGlobalData.NO_CALL;
	int mPairStatus = BtcGlobalData.NOT_PAIR;
	int mPowerStatus = BtcGlobalData.NOT_PAIR;
	int mSyncStatus = BtcGlobalData.NOT_SYNC;
	
	AudioManager audioManager;
	NotificationManager nm;
	private SyncBinder binder = new SyncBinder();  
	private int mUpdateTime = 10000;	
	public final int mShowNotification = 1;	
	public final int mCancelNotification = 2;	
	int RecordNum = 0;
//	int mSyncStatus = 0;
	private final ArrayList<PhoneBookInfo> mPhoneBookInfo = new ArrayList<PhoneBookInfo>();		
	ArrayList<String> mPhoneBook = new ArrayList<String>();
	PhoneBookInfo mSIMContactsInfo;
	PhoneBookInfo mPhoneContactsInfo;
	PhoneBookInfo mCalloutInfo;
	PhoneBookInfo mCallmissInfo;
	PhoneBookInfo mCallinInfo;
	LocalBroadcastManager lbm;
	
	int mTempStatus = 0;
	
	@Override
	public IBinder onBind(Intent intent) {		
		return binder;
	}
	
	@Override
	public void onCreate() {	
		super.onCreate();		
		initBtc();
		 audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);    
		 nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//		handler.postDelayed(runnable, mUpdateTime);
	}
	
	private void initBtc() {
		BtcNative.initBtc();	
		mSIMContactsInfo = new PhoneBookInfo(BtcGlobalData.PB_SIM);	
		mPhoneContactsInfo = new PhoneBookInfo(BtcGlobalData.PB_PHONE);
		mCalloutInfo = new PhoneBookInfo(BtcGlobalData.PB_OUT);
		mCallmissInfo = new PhoneBookInfo(BtcGlobalData.PB_MISS);
		mCallinInfo = new PhoneBookInfo(BtcGlobalData.PB_IN);
		mPhoneBookInfo.add(mSIMContactsInfo);
		mPhoneBookInfo.add(mPhoneContactsInfo);
		mPhoneBookInfo.add(mCalloutInfo);
		mPhoneBookInfo.add(mCallmissInfo);
		mPhoneBookInfo.add(mCallinInfo);
		lbm = LocalBroadcastManager.getInstance(this);
		syncT.start();
	}

	@Override
	public void onStart(Intent intent, int startId) {		
		super.onStart(intent, startId);
	}
	
	Thread syncT =new Thread(new Runnable() {		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				//监听蓝牙模块是否有数据
				mTempStatus = BtcNative.getPowerStatus();	
				if (mTempStatus != mPowerStatus) {
					onPowerStatusChange();
				}				
				
				//监听getPairStatus()				
				mTempStatus = BtcNative.getPairStatus();	
				if (mTempStatus != mPairStatus) {
					onPairStatusChange();
				}
				//监听CallStatus
				mTempStatus = BtcNative.getCallStatus();	
				if (mTempStatus != mCallStatus) {
					onCallStatusChange();
				}
				
				//更新Bfp状态
				mTempStatus = BtcNative.getBfpStatus();		
				if (mBfpStatus != mTempStatus || bfpUpdateTime >= 3) {
					onBfpStatusChange();
				}		
				//Bfp状态一直为BFP_DISCONNECT的时候，30秒后重新enterPair
				if (mTempStatus == BtcGlobalData.BFP_DISCONNECT && index > 30 ) {
					BtcNative.enterPair();
					index = 0;
				}else if (mBfpStatus == BtcGlobalData.BFP_CONNECTED){
					index = 0;
				}else{
					index++;
				}
				
				//更新A2dp状态
				mTempStatus =BtcNative.getA2dpStatus();
				mLog("mTempStatus =="+mTempStatus+"; getA2dpStatus =="+mA2dpStatus);
				if (mA2dpStatus != mTempStatus) {					
					mLog("sendBroadcast getA2dpStatus");
					onA2dpStatusChange();
				}
				
				//更新Sync状态
				mTempStatus = BtcNative.getSyncStatus();
				mLog("mTempStatus =="+mTempStatus+"; mSyncStatus =="+mSyncStatus);
				if (mSyncStatus != mTempStatus) {
					onSyncStatusChange();					
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}				
			}
		}
	});
	

	public class SyncBinder extends Binder {  
		      
        /** 
         * 获取ArrayList<PhoneBookInfo>
         *  
         * @return 
         */  
        public ArrayList<PhoneBookInfo> getPhoneBookInfo() {  
            return mPhoneBookInfo;  
        }  
  
        /** 
         * 获取PhoneBookInfo
         *  
         * @return 
         */  
        public PhoneBookInfo getPhoneBookInfo(int index) {  
        	return mPhoneBookInfo.get(index);  
        }  
        
        public int getSyncStatus() {
			return mSyncStatus;
		}
        
        public int getCallStatus() {
        	return mCallStatus;
        }
        
        public int getBfpStatuss() {
        	return mBfpStatus;
        }
        
        /** 
         * return  A2dpStatus
         * @return 
         */  
        public int getA2dpStatus() {  
        	return BtcNative.getA2dpStatus();
        }    
    }  
	

	protected void onBfpStatusChange() {
		Intent mBfpIntent = new Intent();
		mBfpIntent.setAction(MainActivity.mActionBfp);
		if (mTempStatus == BtcGlobalData.BFP_CONNECTED) {				
			audioManager.setMode(5);
			handler.sendEmptyMessageDelayed(mShowNotification, MainActivity.mShowDeviceNameDelayed);
			mBfpIntent.putExtra("bfp_status", BtcGlobalData.BFP_CONNECTED);
		}else if (mTempStatus == BtcGlobalData.BFP_DISCONNECT) {
			audioManager.setMode(AudioStream.MODE_NORMAL);				
			handler.sendEmptyMessageDelayed(mCancelNotification, 1000);
			mBfpIntent.putExtra("bfp_status", BtcGlobalData.BFP_DISCONNECT);	
		}
		lbm.sendBroadcast(mBfpIntent);		
		mBfpStatus = mTempStatus;
		
	}


	Handler handler = new Handler() {  
	      public void handleMessage(android.os.Message msg) {  
	        switch (msg.what) {
			case mShowNotification:
				showNotification();
				break;
			case mCancelNotification:
				cancelNotification();
				break;
			default:
				break;
			}	
	      }
	};
	        
    public void showNotification()
    {
    	mLog("showNotification 11111111111111111");    	
	    long when = System.currentTimeMillis();   
	    Notification notification = new Notification(R.drawable.ic_launcher, "蓝牙", when);   
	    //define the notification's expand message and intent
	    CharSequence contentTitle = "蓝牙音乐";
	    CharSequence contentText ;
	    if (BtcNative.getPairDeviceName(0).length()>0) {
	    	contentText = getResources().getString(R.string.connect_title)+"--"+BtcNative.getPairDeviceName(0);
		}else{
			contentText = getResources().getString(R.string.connect_title);			
		}
	    Intent notificationIntent = new Intent();
	    notificationIntent.setClassName("com.spreadwin.btc", "com.spreadwin.btc.MainActivity");
	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);	   
	    notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);	
	    nm.notify(1, notification);	 
    }
    
    public  void cancelNotification(){        	
    	mLog("cancelNotification 22222222222222");
    	nm.cancel(1);    
    }
    
	protected void onPowerStatusChange() {
		mPowerStatus = mTempStatus;
		Intent intent = new Intent("MYACTION_CHECK_STATE");
		intent.putExtra("KEY_NAME", "bluetooth");
		sendBroadcast(intent);
	}

	protected void onSyncStatusChange() {		
		Intent mSyncIntent = new Intent();
		mSyncIntent.setAction(MainActivity.mActionSync);
		if (mTempStatus == BtcGlobalData.NEW_SYNC) {
			//联系人只读取一次
			RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_SIM);
			if (mSIMContactsInfo.getSize() != RecordNum) {
				for (int i = 0; i < RecordNum; i++) {
					String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_SIM, i);
					String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_SIM, i);
					mSIMContactsInfo.add(mName, mNumber, "");
					mPhoneBook.add(mName+":"+mNumber);
					mLog("syncT mSIMContactsInfo =="+mSIMContactsInfo.getTelName(i));
				}
			}
			RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_PHONE);
			mLog("mPhoneContactsInfo =="+mPhoneContactsInfo.getSize() +"; RecordNum =="+RecordNum);
			if (mPhoneContactsInfo.getSize() != RecordNum) {
				for (int i = 0; i < RecordNum; i++) {
					String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_PHONE, i);
					String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_PHONE, i);
					mLog("syncT mPhoneContactsInfo mName=="+mName);
					mLog("syncT mPhoneContactsInfo mNumber=="+mNumber);
					mPhoneContactsInfo.add(mName, mNumber, "");
					mPhoneBook.add(mName+":"+mNumber);
					mLog("syncT mPhoneContactsInfo =="+mPhoneContactsInfo.getTelName(i));
				}
			}
			RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_OUT);
			mLog("mCalloutInfo =="+mCalloutInfo.getSize() +"; RecordNum =="+RecordNum);
			//通话记录数据有不同更新
			if (mCalloutInfo.getSize() != RecordNum) {
				mCalloutInfo.clear();
				for (int i = 0; i < RecordNum; i++) {
					String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_OUT, i);
					String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_OUT, i);
					mCalloutInfo.add(mName, mNumber, "");
				}
			}
			RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_MISS);
			mLog("mCallmissInfo =="+mCallmissInfo.getSize() +"; RecordNum =="+RecordNum);
			if (mCallmissInfo.getSize() != RecordNum) {
				mCallmissInfo.clear();
				for (int i = 0; i < RecordNum; i++) {
					String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_MISS, i);
					String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_MISS, i);
					mCallmissInfo.add(mName, mNumber, "");
				}
			}
			RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_IN);
			mLog("mCallinInfo =="+mCallinInfo.getSize() +"; RecordNum =="+RecordNum);
			if (mCallinInfo.getSize() != RecordNum) {
				mCallinInfo.clear();
				for (int i = 0; i < RecordNum; i++) {
					String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_IN, i);
					String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_IN, i);
					mCallinInfo.add(mName, mNumber, "");
				}
			}
			mSyncIntent.putExtra("sync_status", BtcGlobalData.NEW_SYNC);
			
			Intent intent = new Intent();
			intent.putStringArrayListExtra("phonebook", mPhoneBook);
			intent.setAction("PHONE_BOOK_SYNC");
			sendBroadcast(intent);
			mLog("yilu3333333333333333333333");	
		}else if (mTempStatus == BtcGlobalData.IN_SYNC) {			
			mSyncIntent.putExtra("sync_status", BtcGlobalData.IN_SYNC);
		}else if (mTempStatus == BtcGlobalData.NOT_SYNC) {
			mSyncIntent.putExtra("sync_status", BtcGlobalData.NOT_SYNC);			
		}
		mLog("syncT getPhoneBookRecordNum =="+BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_PHONE));	
		lbm.sendBroadcast(mSyncIntent);		
		mSyncStatus = mTempStatus;
	}

	protected void onA2dpStatusChange() {
		Intent mA2dpIntent = new Intent();
		mA2dpIntent.setAction(MainActivity.mActionA2dp);
		if (mTempStatus == BtcGlobalData.A2DP_DISCONNECT) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_DISCONNECT);
		}else if (mTempStatus == BtcGlobalData.A2DP_CONNECTED) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_CONNECTED);			
		}else if (mTempStatus == BtcGlobalData.A2DP_PLAYING) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_PLAYING);			
		}
		lbm.sendBroadcast(mA2dpIntent);		
		mA2dpStatus = mTempStatus;
	}

	protected void onPairStatusChange() {
		Intent mPairIntent = new Intent();
		mPairIntent.setAction(MainActivity.mActionPair);	
		if (mTempStatus == BtcGlobalData.NOT_PAIR ) {		
			mPairIntent.putExtra("pair_status", BtcGlobalData.NOT_PAIR);			
		}else  if (mTempStatus == BtcGlobalData.IN_PAIR) {
			mPairIntent.putExtra("pair_status", BtcGlobalData.IN_PAIR);			
		}else if (mTempStatus == BtcGlobalData.PAIRRED) {
			mPairIntent.putExtra("pair_status", BtcGlobalData.PAIRRED);			
		}
		lbm.sendBroadcast(mPairIntent);	
		mPairStatus = mTempStatus;		
	}

	protected void onCallStatusChange() {
		mLog("onCallStatusChange 22222222=="+mTempStatus);
		Intent mCallIntent = new Intent();
		mCallIntent.setAction(MainActivity.mActionCall);	
		if (mTempStatus == BtcGlobalData.CALL_IN ) {		
			mCallIntent.putExtra("call_status", BtcGlobalData.CALL_IN);			
		}else  if (mTempStatus == BtcGlobalData.IN_CALL) {
			mCallIntent.putExtra("call_status", BtcGlobalData.IN_CALL);			
		}else if (mTempStatus == BtcGlobalData.CALL_OUT) {
			mCallIntent.putExtra("call_status", BtcGlobalData.CALL_OUT);			
		}else if (mTempStatus == BtcGlobalData.NO_CALL) {
			if (mSyncStatus != BtcGlobalData.IN_SYNC) {
				if (mCallStatus == BtcGlobalData.IN_CALL && mCallStatusOld == BtcGlobalData.CALL_IN) {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_IN");
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_IN);
				}else if (mCallStatus == BtcGlobalData.CALL_OUT || (mCallStatus == BtcGlobalData.IN_CALL && mCallStatusOld == BtcGlobalData.CALL_OUT)) {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_OUT");
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_OUT);	
				}else {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_MISS");
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_MISS);	
				}
			}
			mCallIntent.putExtra("call_status", BtcGlobalData.NO_CALL);			
		}
		mLog("ainActivity.mBroadcast =="+MainActivity.mBroadcast);	
		if (MainActivity.mBroadcast) {
			for (int i = 0; i < 3; i++) {
				mLog("ainActivity.mBroadcast isTopMyself==2222222");	
				if (isTopMyself()) {
					mLog("ainActivity.mBroadcast isTopMyself=="+true);	
					break;					
				}
				mCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mCallIntent);	
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lbm.sendBroadcast(mCallIntent);	
		}else {
			mCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(mCallIntent);			
		}
		mCallStatusOld = mCallStatus;
		mCallStatus = mTempStatus;		
	}

	private boolean isTopMyself() {
		  ActivityManager am= (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	      List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
	      RunningTaskInfo rti = runningTasks.get(0);
	      ComponentName component = rti.topActivity;
	      if (component.getPackageName().equals("com.spreadwin.btc")) {
	    	  return true;
		 }
		 return false;
	}

	
	public static  void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);				
		}
	}
}
