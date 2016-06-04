package com.spreadwin.btc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spreadwin.btc.Music.BtAudioManager;
import com.spreadwin.btc.contacts.CharacterParser;
import com.spreadwin.btc.contacts.PinyinComparator;
import com.spreadwin.btc.utils.BtcGlobalData;
import com.spreadwin.btc.utils.DBAdapter;
import com.spreadwin.btc.utils.ECarOnline;
import com.spreadwin.btc.utils.HttpAssist;
import com.spreadwin.btc.utils.HttpUtil;
import com.spreadwin.btc.utils.PhoneBookInfo;
import com.spreadwin.btc.utils.PhoneBookInfo_new;
import com.spreadwin.btc.view.DialogView;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class SyncService extends Service {
	public static final String TAG = "SyncService";
	public static final boolean DEBUG = true;
	public static final String BLUETOOTH_MUTE_CHANGED_ACTION = "android.media.BLUETOOTH_MUTE_CHANGED_ACTION";
	public static final String EXTRA_BLUETOOTH_VOLUME_MUTED = "android.media.EXTRA_BLUETOOTH_VOLUME_MUTED";
	public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
	public static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";

	public static final String ACTION_BT_CALL_ANSWER = "ACTION_BT_CALL_ANSWER";// 接听
	public static final String ACTION_BT_CALL_REJECT = "ACTION_BT_CALL_REJECT";// 拒听
	public static final String ACTION_BT_CALL_HANGUP = "ACTION_BT_CALL_HANGUP";// 挂断
	public static final String ACTION_FACTORY_TEST = "ACTION_FACTORY_TEST";
	public static final String ACTION_FACTORY_RETURN = "ACTION_FACTORY_RETURN";
	public static final String ACTION_BFP_STATUS_UPDATE = "ACTION_BFP_STATUS_UPDATE";
	public static final String ACTION_BFP_STATUS_RETURN = "ACTION_BFP_STATUS_RETURN";
	public static final String ACTION_BFP_CONNECT_CLOSE = "ACTION_BFP_CONNECT_CLOSE";
	
	public static final String ACTION_ACC_OFF = "ACTION_ACC_OFF";
	public static final String ACTION_ACC_ON = "ACTION_ACC_ON";

	public static final String ACTION_BTC_CALL = "MYACTION.BTC.CALL";// 通过蓝牙拨打电话

	public static final String BLUETOOTH_CONNECT_CHANGE = "BLUETOOTH_CONNECT_CHANGE";

	public static final String EXTRA_MUSIC_VOLUME_MUTED = "android.media.EXTRA_MUSIC_VOLUME_MUTED";
	public static final String MUSIC_MUTE_SET_OTHER_ACTION = "android.media.MUSIC_MUTE_SET_OTHER_ACTION";
	public static final String MUSIC_MUTE_RESTORE_ACTION = "android.media.MUSIC_MUTE_RESTORE_ACTION";
	public boolean mOnlyMusic = false;

	/**
	 * 凯立德一键通对应接口
	 */
	public final String ACTION_CALL_CUSTOMER_SERVICE = "CLD.NAVI.MSG.CALL_CUSTOMER_SERVICE";
	public final String ACTION_NOTIFY_SERVICE_CONNECTED = "CLD.NAVI.MSG.NOTIFY_SERVICE_CONNECTED";
	public final String ACTION_CUSTOMER_SERVICE_NUMBER = "CLD.NAVI.MSG.CUSTOMER_SERVICE_NUMBER";
	public final String ACTION_BINDED_NUMBER = "CLD.NAVI.MSG.BINDED_NUMBER";
	public String mCLDNum_key = "CLDNum_key";
	public String mCLDNum_default = "4007883098";
	/**
	 * mCLDCallNum为一键通呼叫中心电话号码
	 */
	public String mCLDCallNum;
	/**
	 * mCLDCallResult为一键通功能的返回结果，0成功，1失败，2不能通过蓝牙呼叫电话
	 */
	public int mCLDCallResult = -1;
	/**
	 * true为 凯立德的一键通功能
	 */
	public boolean mCLDCall = false;

	private boolean mThreadRun = false;

	private int index = 0;
	private int bfpUpdateTime = 0;
	private int mA2dpStatus = BtcGlobalData.A2DP_DISCONNECT;
	public int mBfpStatus = BtcGlobalData.BFP_DISCONNECT;// 蓝牙连接状态
	public int mCallStatus = BtcGlobalData.NO_CALL;
	private int mCallStatusOld = BtcGlobalData.NO_CALL;
	private int mPairStatus = BtcGlobalData.NOT_PAIR;
	private int mPowerStatus = BtcGlobalData.NOT_PAIR;
	private int mSyncStatus = BtcGlobalData.NOT_SYNC;
	private int mUpdateCalllog = BtcGlobalData.NO_CALL;

	private AudioManager audioManager;
	private NotificationManager nm;
	private SyncBinder binder = new SyncBinder();
	private int mUpdateTime = 10000;
	public final int mShowNotification = 1;
	public final int mCancelNotification = 2;
	public final int mPhoneBookSyncBroadcast = 3;
	public final int mNewSyncStatus = 4;
	public final int mAddDatabase = 5;
	public final int mUpdateDataBase = 6;
	private int RecordNum = 0;
	// int mSyncStatus = 0;
	private final ArrayList<PhoneBookInfo> mPhoneBookInfo = new ArrayList<PhoneBookInfo>();
	ArrayList<String> mPhoneBook = new ArrayList<String>();
	PhoneBookInfo mSIMContactsInfo;
	PhoneBookInfo mPhoneContactsInfo;
	PhoneBookInfo mCalloutInfo;
	PhoneBookInfo mCallmissInfo;
	PhoneBookInfo mCallinInfo;
	// LocalBroadcastManager lbm;

	private Messenger mClient;

	boolean mScreenStatus = true;

	List<PhoneBookInfo_new> mContactsInfo = new ArrayList<PhoneBookInfo_new>();
	private CharacterParser characterParser;
	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	int mTempStatus = 0;

	String result;

	boolean upload_toggle = false;

	private int mOldBt = 0;
	private int mDefaultVoice = 13;// 默认声音大小

	public DBAdapter m_DBAdapter = null;

	private ECarOnline mECarOnline;

	private boolean mAccOff = false;

	private WindowManager wm;

	private View view;

	private boolean isSater;

	public static int mNum;

	private boolean isFlage;

	public static boolean isStarFromVoice;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mThreadRun = true;
		mECarOnline = ECarOnline.getInstance(this);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		initBtc();

		m_DBAdapter = new DBAdapter(this);

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
		// lbm = LocalBroadcastManager.getInstance(this);
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();
		syncT.start();
		// mOldBt = BtcNative.getVolume();
		mOldBt = mDefaultVoice;
		onIntentFilter();
	}

	private void onIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(BLUETOOTH_MUTE_CHANGED_ACTION);
		filter.addAction(VOLUME_CHANGED_ACTION);
		filter.addAction(ACTION_BT_CALL_ANSWER);
		filter.addAction(ACTION_BT_CALL_REJECT);
		filter.addAction(ACTION_BT_CALL_HANGUP);
		filter.addAction(ACTION_BFP_CONNECT_CLOSE);
		filter.addAction(ACTION_FACTORY_TEST);
		filter.addAction(ACTION_BFP_STATUS_UPDATE);
		// filter.addAction(ACTION_BFP_CONNECT_CLOSE);
		filter.addAction(ACTION_CALL_CUSTOMER_SERVICE);
		// filter.addAction(ACTION_NOTIFY_SERVICE_CONNECTED);
		filter.addAction(ACTION_CUSTOMER_SERVICE_NUMBER);
		filter.addAction(ACTION_ACC_OFF);
		filter.addAction(ACTION_ACC_ON);
		// filter.addAction(ACTION_ECAR_CALL_SEND);
		registerReceiver(mBatInfoReceiver, filter);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	Thread syncT = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (mThreadRun) {
				// 监听蓝牙模块是否有数据
				mTempStatus = BtcNative.getPowerStatus();
				if (mTempStatus != mPowerStatus) {
					onPowerStatusChange();
				}

				// 监听getPairStatus()
				mTempStatus = BtcNative.getPairStatus();
				if (mTempStatus != mPairStatus) {
					onPairStatusChange();
				}
				// 监听CallStatus
				mTempStatus = BtcNative.getCallStatus();
				if (mTempStatus != mCallStatus) {
					onCallStatusChange();
				}

				// 更新Bfp状态
				mTempStatus = BtcNative.getBfpStatus();
				mLog("mTempStatus ==" + mTempStatus + "; mBfpStatus ==" + mBfpStatus);
				if (mBfpStatus != mTempStatus || bfpUpdateTime >= 3) {
					onBfpStatusChange();
				}

				// Bfp状态一直为BFP_DISCONNECT的时候，30秒后重新enterPair
				if (mTempStatus == BtcGlobalData.BFP_DISCONNECT && index > 30) {
					mLog("mTempStatus ==" + mTempStatus + "; index ==" + index);
					BtcNative.enterPair();
					index = 0;
				} else if (mBfpStatus == BtcGlobalData.BFP_CONNECTED) {
					index = 0;
				} else {
					index++;
				}

				// 更新A2dp状态
				mTempStatus = BtcNative.getA2dpStatus();
				// mLog("mTempStatus ==" + mTempStatus + "; getA2dpStatus =="
				// + mA2dpStatus);
				if (mA2dpStatus != mTempStatus) {
					mLog("sendBroadcast getA2dpStatus");
					onA2dpStatusChange();
				}
				// isFlage = false;

				// 更新Sync呼入状态
				int PB_IN_TYPE = BtcNative.getSyncStatus(BtcGlobalData.PB_IN);
				mTempStatus = PB_IN_TYPE;
				if (PB_IN_TYPE == BtcGlobalData.NEW_SYNC) {
					mLog("mTempStatus ==" + mTempStatus + "; mSyncStatus ==" + mSyncStatus);
					updatePbIn();
				}

				// 更新Sync呼出状态
				int PB_OUT_TYPE = BtcNative.getSyncStatus(BtcGlobalData.PB_OUT);
				mTempStatus = PB_OUT_TYPE;
				if (PB_OUT_TYPE == BtcGlobalData.NEW_SYNC) {
					mLog("mTempStatus ==" + mTempStatus + "; mSyncStatus ==" + mSyncStatus);
					updatePbOut();
					Log.e("------", "onSyncStatusChange end 111111111111");
				}

				// 更新Sync未接状态
				int PB_MISS_TYPE = BtcNative.getSyncStatus(BtcGlobalData.PB_MISS);
				mTempStatus = PB_MISS_TYPE;
				if (PB_MISS_TYPE == BtcGlobalData.NEW_SYNC) {
					mLog("mTempStatus ==" + mTempStatus + "; mSyncStatus ==" + mSyncStatus);
					updatePbMiss();
				}

				// 更新Sync电话本状态
				int PB_PHONE_TYPE = BtcNative.getSyncStatus(BtcGlobalData.PB_PHONE);
				mTempStatus = PB_PHONE_TYPE;
				if (PB_PHONE_TYPE == BtcGlobalData.NEW_SYNC) {
					mLog("mTempStatus ==" + mTempStatus + "; mSyncStatus ==" + mSyncStatus);
					updatePbPhone();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});

	private void updatePbOut() {
		Message message = new Message();
		message.what = mPhoneBookSyncBroadcast;
		// 呼出记录
		RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_OUT);
		mLog("mCalloutInfo ==" + mCalloutInfo.getSize() + "; RecordNum ==" + RecordNum);
		// 通话记录数据有不同更新
		if (mCalloutInfo.getSize() != RecordNum || mUpdateCalllog == BtcGlobalData.PB_OUT) {
			mCalloutInfo.clear();
			mUpdateCalllog = BtcGlobalData.NO_CALL;
			for (int i = 0; i < RecordNum; i++) {
				String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_OUT, i);
				String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_OUT, i);
				mCalloutInfo.add(mName, mNumber, "");
			}
		}

		mLog("mCalloutInfo ==" + mCalloutInfo.getSize());
		message.arg1 = BtcGlobalData.NEW_SYNC;
		handler.sendMessageDelayed(message, 100);
		mSyncStatus = mTempStatus;
		// if (!isFlage) {
		// updatePbPhone();
		// }
	}

	private void updatePbIn() {
		Message message = new Message();
		message.what = mPhoneBookSyncBroadcast;
		mLog("mCallinInfo ==" + mCallinInfo.getSize());
		// 呼入记录
		RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_IN);
		mLog("mCallinInfo ==" + mCallinInfo.getSize() + "; RecordNum ==" + RecordNum);
		if (mCallinInfo.getSize() != RecordNum || mUpdateCalllog == BtcGlobalData.PB_IN) {
			mCallinInfo.clear();
			mUpdateCalllog = BtcGlobalData.NO_CALL;
			for (int i = 0; i < RecordNum; i++) {
				String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_IN, i);
				String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_IN, i);
				mCallinInfo.add(mName, mNumber, "");
			}
		}
		message.arg1 = BtcGlobalData.NEW_SYNC;
		handler.sendMessageDelayed(message, 100);
		mSyncStatus = mTempStatus;
	}

	private void updatePbMiss() {
		Message message = new Message();
		message.what = mPhoneBookSyncBroadcast;
		// 未接记录
		RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_MISS);
		mLog("mCallmissInfo ==" + mCallmissInfo.getSize() + "; RecordNum ==" + RecordNum);
		if (mCallmissInfo.getSize() != RecordNum || mUpdateCalllog == BtcGlobalData.PB_MISS) {
			mCallmissInfo.clear();
			mUpdateCalllog = BtcGlobalData.NO_CALL;
			for (int i = 0; i < RecordNum; i++) {
				String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_MISS, i);
				String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_MISS, i);
				mCallmissInfo.add(mName, mNumber, "");
			}
		}
		message.arg1 = BtcGlobalData.NEW_SYNC;
		handler.sendMessageDelayed(message, 100);
		mSyncStatus = mTempStatus;
		// if (!isFlage) {
		// updatePbPhone();
		// }
	}

	private void updatePbPhone() {
		Message message = new Message();
		message.what = mPhoneBookSyncBroadcast;
		// 添加联系人，SIM卡联系人+手机联系人 和总联系人比较
		RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_PHONE);
		mLog("syncT onSyncStatusChange sRecordNum==" + RecordNum + "; mPhoneBook.size() ==" + mPhoneBook.size()
				+ "mUpdateCalllog====" + mUpdateCalllog);
		// if (mUpdateCalllog == BtcGlobalData.NO_CALL) {
		if (mPhoneBook.size() != RecordNum || isNewContacts()) {
			addContacts();
			// handler.sendEmptyMessage(mAddDatabase);
			Thread mDataThread = new Thread(new Runnable() {
				@Override
				public void run() {
					mLog("mDataThread  is start");
					addDatabase();
					mLog("mDataThread  is end");
				}
			});
			if (!mDataThread.isAlive()) {
				mDataThread.start();
			}
			message.arg2 = BtcGlobalData.NEW_SYNC;
		}
		// }
		message.arg1 = BtcGlobalData.NEW_SYNC;
		handler.sendMessageDelayed(message, 100);
		mSyncStatus = mTempStatus;
		// isFlage = true;
	}

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
		 * 获取List<PhoneBookInfo_new>
		 * 
		 * @return
		 */
		public List<PhoneBookInfo_new> getPhoneBookInfo_new() {
			return mContactsInfo;
		}

		public ArrayList<String> getCallNumberList(String getCallName) {
			ArrayList<String> number = null;
			for (int i = 0; i < mContactsInfo.size(); i++) {
				if (mContactsInfo.get(i).getName().equals(getCallName)
						|| mContactsInfo.get(i).getName().contains(getCallName)) {
					number = mContactsInfo.get(i).getNumber();
					Log.d("ContactsInfo.size()====", i + "");
					break;
				}
			}
			return number;
		}

		public String getCallNumber(String getCallName) {
			String mCallNmber = null;
			for (int i = 0; i < mContactsInfo.size(); i++) {
				Log.d("ContactsInfo.size()====", i + "");
				if (mContactsInfo.get(i).getName().equals(getCallName)
						|| mContactsInfo.get(i).getName().contains(getCallName)) {
					ArrayList<String> number = mContactsInfo.get(i).getNumber();
					if (number.size() >= 2) {
						mCallNmber = "more";
					} else if (number.size() == 1) {
						mCallNmber = number.get(0);
					}
				}
			}
			return mCallNmber;
		}

		public String getCallName(String getCallNumber) {
			String mCallName = "";
			for (int i = 0; i < mContactsInfo.size(); i++) {
				Log.d("ContactsInfo.size()====", i + "");
				ArrayList<String> number = mContactsInfo.get(i).getNumber();
				for (int j = 0; j < number.size(); j++) {
					if (number.get(j).equals(getCallNumber) || (getCallNumber.length() >= 8
							&& number.get(j).contains(getCallNumber.substring(getCallNumber.length() - 8)))) {
						mCallName = mContactsInfo.get(i).getName();
					}
				}
			}
			return mCallName;
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
		 * return A2dpStatus
		 * 
		 * @return
		 */
		public int getA2dpStatus() {
			return BtcNative.getA2dpStatus();
		}

		/**
		 * return A2dpStatus
		 * 
		 * @return
		 */
		public int getmUpdateStatus() {
			return mUpdateCalllog;
		}

		public void setClientMessager(Messenger client) {
			mClient = client;
		}
	}

	public void sendObjMessage(int msg_id, Object obj) {
		if (mClient != null) {
			try {
				mClient.send(Message.obtain(null, msg_id, obj));
			} catch (RemoteException e) {
				mLog("client is dead or quit");
				mClient = null;
			}

		}
	}

	public void sendObjMessage(int msg_id, int arg1, int arg2, Object obj) {
		if (mClient != null) {
			try {

				mClient.send(Message.obtain(null, msg_id, arg1, arg2, obj));
			} catch (RemoteException e) {
				mLog("client is dead or quit");
				mClient = null;
			}

		}
	}

	public void sendMessage(int msg_id, int arg1, int arg2) {
		if (mClient != null) {
			try {
				mClient.send(Message.obtain(null, msg_id, arg1, arg2));
			} catch (RemoteException e) {
				mLog("client is dead or quit");
				mClient = null;
			}
		}
	}

	public void setBtAudioMode(int mode) {
		// 0 normal
		// 6 bt audio
		// 7 bt call
		mLog("setBtAudioMode mode ==" + mode);
		// audioManager.setParameters("cdr_mode=" + mode);
		BtAudioManager.getInstance(this).setAudioMode(mode);
	}

	protected void onBfpStatusChange() {
		Intent mBfpIntent = new Intent();
		mBfpIntent.setAction(MainActivity.mActionBfp);
		if (mTempStatus == BtcGlobalData.BFP_CONNECTED) {
			saySomething("蓝牙已连接");// 语音提示
			handler.sendEmptyMessageDelayed(mShowNotification, MainActivity.mShowDeviceNameDelayed);
			mBfpIntent.putExtra("bfp_status", BtcGlobalData.BFP_CONNECTED);
			// 不自动打开蓝牙音频
			setBtAudioMode(BtAudioManager.AUDIO_MODE_BT);
		} else if (mTempStatus == BtcGlobalData.BFP_DISCONNECT) {
			saySomething("蓝牙已断开");// 语音提示
			m_DBAdapter.close();
			handler.sendEmptyMessageDelayed(mCancelNotification, 1000);
			mBfpIntent.putExtra("bfp_status", BtcGlobalData.BFP_DISCONNECT);
			// 清空联系人和通话记录数据
			mLog("clear phonebook data");
			mPhoneBook.clear();
			mContactsInfo.clear();
			for (int i = 0; i < mPhoneBookInfo.size(); i++) {
				mPhoneBookInfo.get(i).clear();
			}
			setBtAudioMode(BtAudioManager.AUDIO_MODE_NORMAL);
			
		}
		sendObjMessage(1, mBfpIntent);
		// lbm.sendBroadcast(mBfpIntent);
		mBfpStatus = mTempStatus;
		mECarOnline.onReturnBfpStatus();
	}

	Handler handler = new Handler() {
		boolean mdatabase;

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case mShowNotification:
				// 初始化数据库
				m_DBAdapter.open();

				showNotification();
				mSendBluetoothBroadcast(BLUETOOTH_CONNECT_CHANGE, true);
				Thread mDataThread = new Thread(new Runnable() {
					@Override
					public void run() {
						mLog("mDataThread  is start");
						mdatabase = getDatabase();
						mLog("mDataThread  is end");
					}
				});
				if (!mDataThread.isAlive()) {
					mDataThread.start();
				}
				if (mdatabase && isNetworkConnected()) {
					PullContacts();
				}
				break;
			case mCancelNotification:
				// 关闭数据库
				cancelNotification();
				mSendBluetoothBroadcast(BLUETOOTH_CONNECT_CHANGE, false);
				break;
			case mPhoneBookSyncBroadcast:
				mLog("mPhoneBookSyncBroadcast msg.arg1 ==" + msg.arg1);
				Intent mSyncIntent = new Intent();
				mSyncIntent.setAction(MainActivity.mActionSync);
				mSyncIntent.putExtra("sync_status", msg.arg1);
				// lbm.sendBroadcast(mSyncIntent);
				sendObjMessage(1, mSyncIntent);
				// NEW_SYNC状态，更新联系人给语音助手
				if (msg.arg2 == BtcGlobalData.NEW_SYNC) {
					mSendSyncBroadcast();
					mLog("isNetworkConnected ==" + isNetworkConnected());
					if (isNetworkConnected() && upload_toggle) {
						PushContacts();
					}
				}
				break;
			case mAddDatabase:
				addDatabase();
				break;
			}
		}
	};

	public void showNotification() {
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.ic_launcher, "蓝牙", when);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		// define the notification's expand message and intent
		CharSequence contentTitle = "车载蓝牙";
		CharSequence contentText;
		if (BtcNative.getPairDeviceName(0).length() > 0) {
			contentText = getResources().getString(R.string.connect_title) + "--" + BtcNative.getPairDeviceName(0);
		} else {
			contentText = getResources().getString(R.string.connect_title);
		}
		Intent notificationIntent = new Intent();
		notificationIntent.setClassName("com.spreadwin.btc", "com.spreadwin.btc.MainActivity");
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		nm.notify(1, notification);
	}

	protected void mSendBluetoothBroadcast(String action, boolean status) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra("status", status);
		sendBroadcast(intent);
	}

	protected void mSendSyncBroadcast() {
		mLog("mSendSyncBroadcast start");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction("PHONE_BOOK_SYNC");
				if (mPhoneBook.size() > 0) {
					ArrayList<String> temp = new ArrayList<String>();
					int index = 0;
					int total = (int) Math.ceil((float) mPhoneBook.size() / 500);
					mLog("mSendSyncBroadcast mPhoneBook.size() ==" + mPhoneBook.size() + "; total =" + total
							+ "; index =" + index);
					for (int i = 0; i < mPhoneBook.size(); i++) {
						temp.add(mPhoneBook.get(i));
						if (i + 1 == mPhoneBook.size()) {
							intent.putStringArrayListExtra("phonebook", temp);
							// intent.putExtra("completion", true);
							intent.putExtra("index", index++);
							intent.putExtra("total", total);
							mLog("mSendSyncBroadcast sendBroadcast ==" + i + "; total =" + total + "; index =" + index);
							sendBroadcast(intent);
							temp.clear();
						} else if (i != 0 && i % 499 == 0) {
							intent.putStringArrayListExtra("phonebook", temp);
							// intent.putExtra("completion", false);
							intent.putExtra("index", index++);
							intent.putExtra("total", total);
							mLog("mSendSyncBroadcast sendBroadcast ==" + i + "; total =" + total + "; index =" + index);
							sendBroadcast(intent);
							temp.clear();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}

			}
		});
		thread.start();
	}

	public void cancelNotification() {
		mLog("cancelNotification ");
		nm.cancel(1);
	}

	protected void onPowerStatusChange() {
		mPowerStatus = mTempStatus;
		Intent intent = new Intent("MYACTION_CHECK_STATE");
		intent.putExtra("KEY_NAME", "bluetooth");
		sendBroadcast(intent);
	}

	// public void onSyncStatusChange() {
	// mLog("syncT onSyncStatusChange start ==" + mTempStatus);
	// // Intent mSyncIntent = new Intent();
	// // mSyncIntent.setAction(MainActivity.mActionSync);
	// Log.e("------", "onSyncStatusChange start 111111111111");
	// Message message = new Message();
	// message.what = mPhoneBookSyncBroadcast;
	// if (mTempStatus == BtcGlobalData.NEW_SYNC) {
	// upload_toggle = false;
	// // 呼出记录
	// RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_OUT);
	// mLog("mCalloutInfo ==" + mCalloutInfo.getSize() + "; RecordNum ==" +
	// RecordNum);
	// // 通话记录数据有不同更新
	// if (mCalloutInfo.getSize() != RecordNum || mUpdateCalllog ==
	// BtcGlobalData.PB_OUT) {
	// mCalloutInfo.clear();
	// mUpdateCalllog = BtcGlobalData.NO_CALL;
	// for (int i = 0; i < RecordNum; i++) {
	// String mName =
	// BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_OUT, i);
	// String mNumber =
	// BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_OUT, i);
	// mCalloutInfo.add(mName, mNumber, "");
	// }
	// }
	// mLog("mCalloutInfo ==" + mCalloutInfo.getSize());
	// // 未接记录
	// RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_MISS);
	// mLog("mCallmissInfo ==" + mCallmissInfo.getSize() + "; RecordNum ==" +
	// RecordNum);
	// if (mCallmissInfo.getSize() != RecordNum || mUpdateCalllog ==
	// BtcGlobalData.PB_MISS) {
	// mCallmissInfo.clear();
	// mUpdateCalllog = BtcGlobalData.NO_CALL;
	// for (int i = 0; i < RecordNum; i++) {
	// String mName =
	// BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_MISS, i);
	// String mNumber =
	// BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_MISS, i);
	// mCallmissInfo.add(mName, mNumber, "");
	// }
	// }
	// mLog("mCallinInfo ==" + mCallinInfo.getSize());
	// // 呼入记录
	// RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_IN);
	// mLog("mCallinInfo ==" + mCallinInfo.getSize() + "; RecordNum ==" +
	// RecordNum);
	// if (mCallinInfo.getSize() != RecordNum || mUpdateCalllog ==
	// BtcGlobalData.PB_IN) {
	// mCallinInfo.clear();
	// mUpdateCalllog = BtcGlobalData.NO_CALL;
	// for (int i = 0; i < RecordNum; i++) {
	// String mName =
	// BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_IN, i);
	// String mNumber =
	// BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_IN, i);
	// mCallinInfo.add(mName, mNumber, "");
	// }
	// }
	//
	// // 添加联系人，SIM卡联系人+手机联系人 和总联系人比较
	// RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_PHONE);
	// mLog("syncT onSyncStatusChange sRecordNum==" + RecordNum + ";
	// mPhoneBook.size() ==" + mPhoneBook.size());
	// if (mUpdateCalllog == BtcGlobalData.NO_CALL) {
	// if (mPhoneBook.size() != RecordNum || isNewContacts()) {
	// addContacts();
	// // handler.sendEmptyMessage(mAddDatabase);
	// Thread mDataThread = new Thread(new Runnable() {
	// @Override
	// public void run() {
	// mLog("mDataThread is start");
	// addDatabase();
	// mLog("mDataThread is end");
	// }
	// });
	// if (!mDataThread.isAlive()) {
	// mDataThread.start();
	// }
	// message.arg2 = BtcGlobalData.NEW_SYNC;
	// }
	// }
	// Log.e("------", "onSyncStatusChange end 111111111111");
	// message.arg1 = BtcGlobalData.NEW_SYNC;
	// // mSyncIntent.putExtra("sync_status", BtcGlobalData.NEW_SYNC);
	// } else if (mTempStatus == BtcGlobalData.IN_SYNC) {
	// message.arg1 = BtcGlobalData.IN_SYNC;
	// // mSyncIntent.putExtra("sync_status", BtcGlobalData.IN_SYNC);
	// } else if (mTempStatus == BtcGlobalData.NOT_SYNC) {
	// message.arg1 = BtcGlobalData.NOT_SYNC;
	// // mSyncIntent.putExtra("sync_status", BtcGlobalData.NOT_SYNC);
	// }
	//
	// mLog("syncT getPhoneBookRecordNum ==" +
	// BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_PHONE));
	// // lbm.sendBroadcast(mSyncIntent);
	// handler.sendMessageDelayed(message, 100);
	// // handler.sendEmptyMessageDelayed(mPhoneBookSyncBroadcast,
	// // MainActivity.mShowDeviceNameDelayed);
	// mSyncStatus = mTempStatus;
	// }

	private boolean isNewContacts() {
		boolean isNew = false;
		// 读取手机联系人
		RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_PHONE);
		int tempSize = mContactsInfo.size();
		mLog("isNewContacts mContactsInfo ==" + mContactsInfo.size() + "; RecordNum ==" + RecordNum);
		for (int i = 0; i < RecordNum; i++) {
			String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_PHONE, i);
			String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_PHONE, i);
			if (mNumber == null || mNumber.length() == 0) {
				// mLog("isNewContacts mPhoneContactsInfo mName==" + mName
				// + "; mNumber ==" + mNumber);
				continue;
			}
			// mLog("isNewContacts mPhoneContactsInfo mName==" + mName +";length
			// =="+mName.length()+ "; mNumber =="
			// + mNumber+"; length =="+mNumber.length());
			isNew = true;
			for (int j = 0; j < tempSize; j++) {
				// mLog("isNewContacts addContactsInfo setName==" + mName + ";
				// mNumber=="
				// + mNumber);
				if (mName.length() == 0) {
					if (mContactsInfo.get(j).getNumber().indexOf(mNumber) != -1) {
						isNew = false;
						break;
					}
				} else if (mContactsInfo.get(j).getName().equals(mName)
						&& mContactsInfo.get(j).getNumber().indexOf(mNumber) != -1) {
					isNew = false;
					break;
				}
			}

			if (isNew) {
				break;
			}
		}

		mLog("isNewContacts isNew==" + isNew);
		return isNew;
	}

	private void addContacts() {
		mContactsInfo.clear();
		mPhoneBook.clear();
		upload_toggle = true;
		// 添加SIM卡联系人
		RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_SIM);
		for (int i = 0; i < RecordNum; i++) {
			String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_SIM, i);
			String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_SIM, i);
			if (mNumber == null) {
				continue;
			}
			addContactsInfo(mName, mNumber);
		}

		// 添加手机联系人
		RecordNum = BtcNative.getPhoneBookRecordNum(BtcGlobalData.PB_PHONE);
		mLog("mContactsInfo ==" + mContactsInfo.size() + "; RecordNum ==" + RecordNum);
		for (int i = 0; i < RecordNum; i++) {
			String mName = BtcNative.getPhoneBookRecordNameByIndex(BtcGlobalData.PB_PHONE, i);
			String mNumber = BtcNative.getPhoneBookRecordNumberByIndex(BtcGlobalData.PB_PHONE, i);
			mLog("syncT mContactsInfo mName==" + mName + "; mNumber ==" + mNumber);
			if (mNumber == null) {
				mLog("number is null mContactsInfo mName==" + mName + "; mNumber ==" + mNumber);
				continue;
			}
			addContactsInfo(mName, mNumber);
		}
	}

	private void addDatabase() {
		if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
			try {
				m_DBAdapter.DeleteTable();
				mLog("addDatabase mPhoneBook.size()==" + mPhoneBook.size());
				for (int i = 0; i < mPhoneBook.size(); i++) {
					String[] temp = mPhoneBook.get(i).trim().split(":");
					// mLog("addData ["+i+"] =="+temp[0]+":"+temp[1]);
					if (temp.length >= 2) {
						m_DBAdapter.insert(i, temp[0], temp[1], 1);
					}
				}
				saveConnectMac();
			} catch (Exception e) {
				mLog("addDatabase e==" + e);
			}
		}
	}

	private void saveConnectMac() {
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor mEditor = mPreferences.edit();
		mEditor.putString("old_mac", BtcNative.getPairDeviceMac(0));
		mLog("saveConnectMac old_mac ==" + BtcNative.getPairDeviceMac(0));
		mEditor.commit();
	}

	protected boolean isNewMac() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String mac = sp.getString("old_mac", null);
		mLog("isNewMac mac ==" + mac + "; conectMac ==" + BtcNative.getPairDeviceMac(0));
		if (mac == null) {
			return true;
		} else if (mac.equals(BtcNative.getPairDeviceMac(0))) {
			return false;
		} else {
			return true;
		}
	}

	private boolean getDatabase() {
		if (isNewMac()) {
			return true;
		}
		try {
			Cursor c = m_DBAdapter.getAll();
			c.moveToFirst();
			mLog("getDatabase c.getCount() ==" + c.getCount());
			if (c.getCount() <= 0) {
				return true;
			}
			int nameColumnIndex = 2;
			int numberColumnIndex = 3;
			// while (c.moveToNext()) {
			// // 把数据取出
			// String mName = c.getString(nameColumnIndex);
			// String mNumber = c.getString(numberColumnIndex);
			// addContactsInfo(mName, mNumber);
			// }
			do {
				String mName = c.getString(nameColumnIndex);
				String mNumber = c.getString(numberColumnIndex);
				addContactsInfo(mName, mNumber);
			} while (c.moveToNext());
			c.close();

			mLog("getDatabase mPhoneBook.size() ==" + mPhoneBook.size());
			// Intent mSyncIntent = new Intent();
			// mSyncIntent.setAction(MainActivity.mActionSync);
			// mSyncIntent.putExtra("sync_status", BtcGlobalData.NEW_SYNC);
			// lbm.sendBroadcast(mSyncIntent);
			Message message = new Message();
			message.what = mPhoneBookSyncBroadcast;
			// message.arg1联系人是否更新
			message.arg1 = BtcGlobalData.NEW_SYNC;
			// message.arg2联系人是否更新给语音助手
			message.arg2 = BtcGlobalData.NEW_SYNC;
			handler.removeMessages(mPhoneBookSyncBroadcast);
			handler.sendMessageDelayed(message, 100);
			return false;
		} catch (Exception e) {
			mLog("getDatabase e ==" + e);
			return true;
		}

	}

	/**
	 * 更新本地联系人
	 * 
	 * @param mName
	 * @param mNumber
	 */
	private void addContactsInfo(String mName, String mNumber) {
		// 联系人为空时，直接把号码设置联系人
		if (mName.length() == 0) {
			mName = mNumber;
		}
		// 判断是否已经有该联系人
		mLog("addContactsInfo ==" + mName + "; mName lenght ==" + mName.length() + "; mNumber ==" + mNumber);
		mPhoneBook.add(mName + ":" + mNumber);
		for (int i = 0; i < mContactsInfo.size(); i++) {
			if (mContactsInfo.get(i).getName().equals(mName)) {
				mLog("addContactsInfo setName==" + mName + "; mNumber==" + mNumber);
				if (mContactsInfo.get(i).getNumber().indexOf(mNumber) == -1) {
					mContactsInfo.get(i).setNumber(mNumber);
				}
				return;
			}
		}

		PhoneBookInfo_new sortModel = new PhoneBookInfo_new(mName, mNumber);
		// 汉字转换成拼音
		if (mName.length() != 0)

		{
			String pinyin = characterParser.getSelling(mName);
			mLog("addContactsInfo ==" + mName + "; pinyin ==" + pinyin);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// mLog("name =="+mName+"; number =="+mNumber+"; pinyin
			// =="+pinyin+"; sortString =="+sortString);
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}
			if (mName.length() >= 2) {
				String seSecondString = characterParser.getSelling(mName.substring(1, 2)).substring(0, 1).toUpperCase();
				if (seSecondString.matches("[A-Z]")) {
					sortModel.setSecondLetters(seSecondString.toUpperCase());
				} else {
					sortModel.setSecondLetters("#");
				}
			} else {
				sortModel.setSecondLetters("#");
			}
		} else

		{
			sortModel.setSortLetters("#");
			sortModel.setSecondLetters("#");
		}
		mContactsInfo.add(sortModel);
		mNum = mContactsInfo.size();
	}

	/**
	 * A2dp状态变化
	 */
	protected void onA2dpStatusChange() {
		Intent mA2dpIntent = new Intent();
		mA2dpIntent.setAction(MainActivity.mActionA2dp);
		if (mTempStatus == BtcGlobalData.A2DP_DISCONNECT) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_DISCONNECT);
		} else if (mTempStatus == BtcGlobalData.A2DP_CONNECTED) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_CONNECTED);
		} else if (mTempStatus == BtcGlobalData.A2DP_PLAYING) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_PLAYING);
		}
		// lbm.sendBroadcast(mA2dpIntent);
		sendObjMessage(1, mA2dpIntent);
		mA2dpStatus = mTempStatus;
	}

	/**
	 * Pair状态变化
	 */
	protected void onPairStatusChange() {
		Intent mPairIntent = new Intent();
		mPairIntent.setAction(MainActivity.mActionPair);
		if (mTempStatus == BtcGlobalData.NOT_PAIR) {
			mPairIntent.putExtra("pair_status", BtcGlobalData.NOT_PAIR);
		} else if (mTempStatus == BtcGlobalData.IN_PAIR) {
			mPairIntent.putExtra("pair_status", BtcGlobalData.IN_PAIR);
		} else if (mTempStatus == BtcGlobalData.PAIRRED) {
			mPairIntent.putExtra("pair_status", BtcGlobalData.PAIRRED);
		}
		// lbm.sendBroadcast(mPairIntent);
		sendObjMessage(1, mPairIntent);
		mPairStatus = mTempStatus;
	}

	/**
	 * 蓝牙通话状态变化
	 */
	public Handler HandlerCallin = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			showCallDisplay(msg.what);
		}
	};

	protected boolean isFull() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		int leftStackId = am.getRightStackId();
		if (leftStackId > 0 && am.getWindowSizeStatus(leftStackId) == 0) {
			mLog("full");
			return true;
		} else {
			mLog("notfull" + leftStackId + am.getWindowSizeStatus(leftStackId));
			return false;
		}
	}

	protected void onCallStatusChange() {
		mLog("setMute onCallStatusChange ==" + mTempStatus);
		int lastCallStatus = mCallStatus;
		mCallStatus = mTempStatus;

		Intent mCallIntent = new Intent();
		mCallIntent.setAction(MainActivity.mActionCall);
		onChaneAudioFocus(mTempStatus);
		switch (mTempStatus) {
		case BtcGlobalData.CALL_IN:
			// setMute(true,mTempStatus);
			isSater = true;
			BtAudioManager.getInstance(this).onCallChange(true);
			mCallIntent.putExtra("call_status", BtcGlobalData.CALL_IN);
			addCallView();
			break;
		case BtcGlobalData.IN_CALL:
			// setMute(false,mTempStatus);
			BtAudioManager.getInstance(this).onCallChange(true);
			setBtAudioMode(BtAudioManager.AUDIO_MODE_CALL);
			mCallIntent.putExtra("call_status", BtcGlobalData.IN_CALL);
			removeCallView();
			break;
		case BtcGlobalData.CALL_OUT:
			// setMute(false,mTempStatus);
			BtAudioManager.getInstance(this).onCallChange(true);
			if (mCLDCall) {
				mCLDCallResult = 0;
			}
			mCallIntent.putExtra("call_status", BtcGlobalData.CALL_OUT);
			addCallView();
			break;
		case BtcGlobalData.NO_CALL:
			// setMute(false, mTempStatus);
			// setBtAudioMode(BtAudioManager.AUDIO_MODE_NORMAL);
			isSater = false;
			BtAudioManager.getInstance(this).onCallChange(false);
			if (mSyncStatus != BtcGlobalData.IN_SYNC) {
				mLog("startSyncPhoneBook mCallStatusOld ==" + mCallStatusOld + "; lastCallStatus ==" + lastCallStatus);
				if (lastCallStatus == BtcGlobalData.CALL_IN) {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_IN");
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_IN);
					mUpdateCalllog = BtcGlobalData.PB_IN;
				} else if (lastCallStatus == BtcGlobalData.CALL_OUT
						|| (lastCallStatus == BtcGlobalData.IN_CALL && mCallStatusOld == BtcGlobalData.CALL_OUT)) {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_OUT");
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_OUT);
					mUpdateCalllog = BtcGlobalData.PB_OUT;
				} else {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_MISS");
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_MISS);
					mUpdateCalllog = BtcGlobalData.PB_MISS;
				}
			}
			removeCallView();
			mCallIntent.putExtra("call_status", BtcGlobalData.NO_CALL);
			// 凯立德一键通的返回结果
			if (mCLDCall) {
				onCLDCallConnect();
				mCLDCall = false;
			}
			break;
		}
		// 翼卡一键通返回结果
		mECarOnline.onReturnCallState(mTempStatus);

		onMainActivity(mCallIntent);
		mLog("mScreenStatus ==" + mScreenStatus + "; mAccOff ==" + mAccOff);
		if (!mScreenStatus && !mAccOff) {
			wakeUpAndUnlock();
		}
		mCallStatusOld = lastCallStatus;
	}

	private void addCallView() {
		Message msg1 = new Message();
		if (isFull()) {
			msg1.what = 1; // 消息(一个整型值)
		} else {
			msg1.what = 0;
		}
		HandlerCallin.sendMessage(msg1);
	}

	private void removeCallView() {
		try {
			if (wm != null && view != null) {
				finishMainActivity();
				Intent mCallIntent = new Intent("ACTION_BT_CALL_IN");
				sendBroadcast(mCallIntent);
				wm.removeView(view);
				// wm.removeViewImmediate(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finishMainActivity() {
		if (isStarFromVoice) {
			SyncService.isStarFromVoice = false;
			Intent mfinish = new Intent();
			mfinish.setAction(MainActivity.mAcitonFinish);
			sendObjMessage(1, mfinish);
		}
	}

	private void showCallDisplay(int full) {

		mLog("showCallDisplay" + full);

		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();

		// wmParams.type = LayoutParams.TYPE_SYSTEM_ERROR |
		// LayoutParams.TYPE_PHONE;
		// 背景透明
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		// params.format = PixelFormat.TRANSLUCENT;
		params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		// 设置悬浮窗的长得宽
		if (full == 1) {
			params.x = 800;
			params.width = 625;
		} else if (full == 0) {
			params.x = 0;
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
		}
		params.y = 0;
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		DialogView gpsView = new DialogView(this, isSater);
		view = gpsView.getVideoPlayView();
		wm.addView(view, params);
	}

	// 改变AudioFocus
	private void onChaneAudioFocus(int mStatus) {
		// if (mStatus != BtcGlobalData.NO_CALL) {
		// audioManager.requestAudioFocus(null, 10,
		// AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		// } else {
		// audioManager.abandonAudioFocus(null);
		// }

	}

	/**
	 * 启动界面
	 */
	private void onMainActivity(Intent mCallIntent) {
		mLog("ainActivity.mBroadcast ==" + MainActivity.mBroadcast);
		if (MainActivity.mBroadcast) {
			if (mTempStatus != BtcGlobalData.NO_CALL) {
				for (int i = 0; i < 3; i++) {
					mLog("ainActivity.mBroadcast isTopMyself");
					if (isTopMyself()) {
						mLog("ainActivity.mBroadcast isTopMyself==" + true);
						break;
					}
					// mCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// startActivity(mCallIntent);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			sendObjMessage(1, mCallIntent);
			// lbm.sendBroadcast(mCallIntent);
		} else {
			// mCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// startActivity(mCallIntent);
		}
		mLog("ainActivity.mBroadcast ==" + MainActivity.mBroadcast);
		sendBroadcast(mCallIntent);
	}

	/**
	 * 判断自己是不是在显示
	 * 
	 * @return
	 */
	private boolean isTopMyself() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
		RunningTaskInfo rti = runningTasks.get(0);
		ComponentName component = rti.topActivity;
		if (component.getPackageName().equals("com.spreadwin.btc")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断移动网络是否连接
	 * 
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	/*
	 * 从服务器更新数据
	 */
	protected void PullContacts() {
		final String url = "http://yun.spreadwin.com/device/get_phonecontacts.php?mac=";
		final String mac = BtcNative.getPairDeviceMac(0);
		mLog("PullContacts mac==" + mac);
		Thread PullContactsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String temp = HttpAssist.openUrl(url + mac);
					mLog("temp ==" + temp);
					String[] temp1 = temp.split("\n");
					for (int i = 0; i < temp1.length; i++) {
						mLog("temp1 ==" + temp1[i]);
						String[] contacts = temp1[i].trim().split(":");
						// String name = temp1[i].trim().split(":")[0];
						// String number = temp1[i].trim().split(":")[1];
						// mPhoneContactsInfo.add(contacts[0], contacts[1], "");
						addContactsInfo(contacts[0], contacts[1]);
					}
					// addDatabase();
					// Intent mSyncIntent = new Intent();
					// mSyncIntent.setAction(MainActivity.mActionSync);
					// mSyncIntent.putExtra("sync_status",
					// BtcGlobalData.NEW_SYNC);
					// lbm.sendBroadcast(mSyncIntent);
					// handler.removeMessages(mPhoneBookSyncBroadcast);
					// handler.sendEmptyMessageDelayed(mPhoneBookSyncBroadcast,
					// 1000);
					Message message = new Message();
					message.what = mPhoneBookSyncBroadcast;
					message.arg1 = BtcGlobalData.NEW_SYNC;
					// message.arg2联系人是否更新给语音助手
					message.arg2 = BtcGlobalData.NEW_SYNC;
					handler.removeMessages(mPhoneBookSyncBroadcast);
					handler.sendMessageDelayed(message, 100);
				} catch (Exception e) {
					e.printStackTrace();
					mLog("PullContacts e ==" + e);
				}
			}
		});
		PullContactsThread.start();
	}

	/**
	 * 更新数据到服务器
	 */
	private void PushContacts() {
		final String name = BtcNative.getPairDeviceName(0);
		final String mac = BtcNative.getPairDeviceMac(0);
		final StringBuffer contacts = new StringBuffer();
		for (int i = 0; i < mPhoneBook.size(); i++) {
			contacts.append(mPhoneBook.get(i) + "\n");
		}
		mLog("contacts ==" + contacts.toString());
		mLog("name ==" + name);
		mLog("mac ==" + mac);
		final String upload_file_url = "http://yun.spreadwin.com/device/update_phonecontacts.php";
		Thread PushContactsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				mLog("run start===========");
				try {
					Map<String, String> params = new HashMap<String, String>();
					params.put("name", name);
					params.put("mac", mac);
					params.put("contacts", contacts.toString());
					HttpUtil.post(upload_file_url, params);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				mLog("PushDataThread run end===========");
			}
		});
		PushContactsThread.start();
	}

	/*
	 * 广播Receiver
	 */
	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			mLog("mBatInfoReceiver action ==" + action);
			if (Intent.ACTION_SCREEN_ON.equals(action)) {
				mScreenStatus = true;
				mLog("-----------------screen is on...");
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				mLog("----------------- screen is off...");
				mScreenStatus = false;
			} else if (action.equals("ACTION_ACC_OFF")) {
				if (mBfpStatus == BtcGlobalData.BFP_CONNECTED) {
					setBtAudioMode(BtAudioManager.AUDIO_MODE_NORMAL);
					// m_DBAdapter.close();
					// audioManager.setMode(AudioStream.MODE_NORMAL);
					// handler.sendEmptyMessageDelayed(mCancelNotification,
					// 1000);
					// mPhoneBook.clear();
					// mContactsInfo.clear();
					// for (int i = 0; i < mPhoneBookInfo.size(); i++) {
					// mPhoneBookInfo.get(i).clear();
					// }
					// mBfpStatus = BtcGlobalData.BFP_DISCONNECT;
					// Intent mBfpIntent = new Intent();
					// mBfpIntent.setAction(MainActivity.mActionBfp);
					// mBfpIntent.putExtra("bfp_status",
					// BtcGlobalData.BFP_DISCONNECT);
					// // lbm.sendBroadcast(mBfpIntent);
					// sendObjMessage(1, mBfpIntent);
				}
			} else if (action.equals(BLUETOOTH_MUTE_CHANGED_ACTION)) {
				mLog("mBatInfoReceiver MUSIC_MUTE_CHANGED_ACTION =="
						+ (intent.getBooleanExtra(EXTRA_BLUETOOTH_VOLUME_MUTED, false)) + "; mOldBt ==" + mOldBt);
				boolean status = intent.getBooleanExtra(EXTRA_BLUETOOTH_VOLUME_MUTED, false);
				setMute(status, mCallStatus);
			} else if (action.equals(VOLUME_CHANGED_ACTION)) {
				int streamType = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, AudioManager.STREAM_MUSIC);
				mLog("mBatInfoReceiver streamType ==" + streamType);
				if (streamType == AudioManager.STREAM_MUSIC) {
					setMute(false, mCallStatus);
				}
			} else if (action.equals(ACTION_BT_CALL_ANSWER)) {
				BtcNative.answerCall();
			} else if (action.equals(ACTION_BT_CALL_REJECT)) {
				BtcNative.denyCall();
			} else if (action.equals(ACTION_BT_CALL_HANGUP)) {
				BtcNative.hangupCall();
			} else if (action.equals(ACTION_FACTORY_TEST)) {
				Intent mIntent = new Intent(ACTION_FACTORY_RETURN);
				mLog("getPowerStatus ==" + BtcNative.getPowerStatus());
				if (BtcNative.getPowerStatus() == 1) {
					mIntent.putExtra("test_result", true);
				} else {
					mIntent.putExtra("test_result", false);
				}
				sendBroadcast(mIntent);
			} else if (action.equals(ACTION_CALL_CUSTOMER_SERVICE)) {
				int flag = intent.getIntExtra("MSG_ID_CALL_CUSTOMER_SERVICE_RESULT", -1);
				if (flag == -1) {
					onCLDCall();
				}
			} else if (action.equals(ACTION_CUSTOMER_SERVICE_NUMBER)) {
				mCLDCallNum = intent.getStringExtra("MSG_ID_CUSTOMER_SERVICE_NUMBER");
				mLog("SERVICE_NUMBER mCLDCallNum ==" + mCLDCallNum);
				if (mCLDCallNum != null) {
					onPutSetting(mCLDNum_key, mCLDCallNum);
				}
			} else if (action.equals(ACTION_BINDED_NUMBER)) {
				mCLDCallNum = intent.getStringExtra("MSG_ID_BIND_NUMBER");
				mLog("BINDED_NUMBER mCLDCallNum ==" + mCLDCallNum);
				if (mCLDCallNum != null) {
					onPutSetting(mCLDNum_key, mCLDCallNum);
				}
			} else if (action.equals(ACTION_ACC_OFF)) {
				mAccOff = true;
				BtcNative.enterSleep();
			} else if (intent.getAction().equals(ACTION_ACC_ON)) {
				mAccOff = false;
				BtcNative.leaveSleep();
			} else if (action.equals(ACTION_BFP_STATUS_UPDATE)) {
				String mQuery = intent.getStringExtra("status");
				mLog("mQuery ==" + mQuery);
				if (mQuery != null && mQuery.equals("call_update")) {
					Intent mCallIntent = new Intent();
					mCallIntent.setAction(MainActivity.mActionCall);
					mCallIntent.putExtra("call_status", mCallStatus);
					sendBroadcast(mCallIntent);
				} else if (mBfpStatus == BtcGlobalData.BFP_CONNECTED) {
					mSendBluetoothBroadcast(BLUETOOTH_CONNECT_CHANGE, true);
				} else {
					mSendBluetoothBroadcast(BLUETOOTH_CONNECT_CHANGE, false);
				}
			} else if (action.equals(ACTION_BFP_CONNECT_CLOSE)) {
				if (mBfpStatus == BtcGlobalData.BFP_CONNECTED) {
					Intent mCallIntent = new Intent();
					mCallIntent.setAction(MainActivity.mActionCall);
					mCallIntent.putExtra("call_status", BtcGlobalData.NO_CALL);
					sendObjMessage(1, mCallIntent);
					BtcNative.disconnectPhone();
				}
			}
		}
	};

	public void saySomething(String something) {
		Intent i = new Intent("ACTION_SAY_SOMETHING");
		i.putExtra("EXTRA_SAY_SOMETHING", something);
		sendBroadcast(i);
	}

	private void setMute(boolean status, int CallStatus) {
		mLog("setMute status ==" + status + "; CallStatus ==" + CallStatus + ";BtcNative.getVolume() =="
				+ BtcNative.getVolume());
		BtAudioManager.getInstance(this).onAudioMuteChange(status);
		// if (status) {
		// BtcNative.setVolume(0);
		// } else {
		// if (CallStatus == BtcGlobalData.IN_CALL) {
		// mOnlyMusic = true;
		// Intent intent = new Intent(MUSIC_MUTE_SET_OTHER_ACTION);
		// intent.putExtra(EXTRA_MUSIC_VOLUME_MUTED, true);
		// intent.putExtra("only_music", mOnlyMusic);
		// sendBroadcast(intent);
		//
		// } else if (CallStatus == BtcGlobalData.NO_CALL && mOnlyMusic) {
		// mOnlyMusic = false;
		// Intent intent = new Intent(MUSIC_MUTE_RESTORE_ACTION);
		// intent.putExtra("only_music", mOnlyMusic);
		// sendBroadcast(intent);
		// } else if (CallStatus == BtcGlobalData.CALL_IN
		// || BtcNative.getVolume() != 0) {
		// return;
		// }
		// if (mOldBt == 0) {
		// mOldBt = mDefaultVoice;
		// }
		// BtcNative.setVolume(mOldBt);
		// }
	}

	// 凯立德一键通话
	protected void onCLDCall() {
		if (mBfpStatus == BtcGlobalData.BFP_CONNECTED) {
			onCLDCallResult(0);
			mCLDCall = true;
			Intent mCustomerIntent = new Intent();
			mCustomerIntent.setAction(ACTION_BTC_CALL);

			if (mCLDCallNum != null) {
				mLog("onCLDCall mCLDCallNum==" + mCLDCallNum);
				mCustomerIntent.putExtra("call_number", mCLDCallNum);
			} else {
				mLog("onCLDCall mCLDCallNum == null");
				mCustomerIntent.putExtra("call_number", onGetSetting(mCLDNum_key, mCLDNum_default));
			}
			mCustomerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mCustomerIntent.addFlags(0x00000200);
			startActivity(mCustomerIntent);
		} else {
			onCLDCallResult(2);
		}
	}

	/**
	 * flag：0成功，1失败，2不能通过蓝牙呼叫电话(包括蓝牙未绑定、蓝牙绑定未配对等，反正只要不能通过蓝牙打电话 都返回2)
	 */
	public void onCLDCallResult(int flag) {
		mLog("send  CLD.NAVI.MSG.CALL_CUSTOMER_SERVICE flag ==" + flag);
		Intent intent = new Intent(ACTION_CALL_CUSTOMER_SERVICE);
		intent.putExtra("MSG_ID_CALL_CUSTOMER_SERVICE_RESULT", flag);// 拨打电话失败1
		sendBroadcast(intent);
	}

	/**
	 * 通知导航软件去服务器获取一键通目的地地址
	 */
	private void onCLDCallConnect() {
		mLog("send  CLD.NAVI.MSG.NOTIFY_SERVICE_CONNECTED");
		Intent intent = new Intent(ACTION_NOTIFY_SERVICE_CONNECTED);
		sendBroadcast(intent);
	}

	public void wakeUpAndUnlock() {
		mLog("wakeUpAndUnlock ");
		// 获取电源管理器对象
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		PowerManager.WakeLock wl = pm
				.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
		// 点亮屏幕
		wl.acquire();
		// 释放
		wl.release();
	}

	protected void onPutSetting(String key_name, String value) {
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor mEditor = mPreferences.edit();
		mEditor.putString(key_name, value);
		mEditor.commit();
	}

	protected String onGetSetting(String key_name, String mDefValue) {
		String mValue = onGetSetting(key_name);
		if (mValue == null) {
			return mDefValue;
		}
		return mValue;
	}

	protected String onGetSetting(String key_name) {
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String mValue = mPreferences.getString(key_name, null);
		return mValue;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mThreadRun = false;
		m_DBAdapter.close();
		mECarOnline.close();
		unregisterReceiver(mBatInfoReceiver);
	}

	public static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}
}
