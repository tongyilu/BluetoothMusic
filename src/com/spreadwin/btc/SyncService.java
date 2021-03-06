package com.spreadwin.btc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spreadwin.btc.Music.BtAudioManager;
import com.spreadwin.btc.Music.MusicFragment;
import com.spreadwin.btc.contacts.CharacterParser;
import com.spreadwin.btc.utils.BtcGlobalData;
import com.spreadwin.btc.utils.DBAdapter;
import com.spreadwin.btc.utils.ECarOnline;
import com.spreadwin.btc.utils.HttpAssist;
import com.spreadwin.btc.utils.HttpUtil;
import com.spreadwin.btc.utils.LruJsonCache;
import com.spreadwin.btc.utils.PhoneBookInfo;
import com.spreadwin.btc.utils.PhoneBookInfo_new;
import com.spreadwin.btc.utils.PreferencesUtils;
import com.spreadwin.btc.utils.SplitUtil;
import com.spreadwin.btc.view.DialogView;

import android.R.bool;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class SyncService extends Service {
	public static final String TAG = "SyncService";
	public static final boolean DEBUG = true;
	public static final String BLUETOOTH_MUTE_CHANGED_ACTION = "android.media.BLUETOOTH_MUTE_CHANGED_ACTION";
	public static final String EXTRA_BLUETOOTH_VOLUME_MUTED = "android.media.EXTRA_BLUETOOTH_VOLUME_MUTED";
	public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
	public static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
	/************************************* 主界面和语音的交互广播 *********************/
	public static final String ACTION_BT_CALL_ANSWER = "ACTION_BT_CALL_ANSWER";// 接听
	public static final String ACTION_BT_CALL_REJECT = "ACTION_BT_CALL_REJECT";// 拒听
	public static final String ACTION_BT_CALL_HANGUP = "ACTION_BT_CALL_HANGUP";// 挂断
	public static final String ACTION_FACTORY_TEST = "ACTION_FACTORY_TEST";
	public static final String ACTION_FACTORY_RETURN = "ACTION_FACTORY_RETURN";
	public static final String ACTION_BFP_STATUS_UPDATE = "ACTION_BFP_STATUS_UPDATE";
	public static final String ACTION_BFP_STATUS_RETURN = "ACTION_BFP_STATUS_RETURN";
	public static final String ACTION_BFP_CONNECT_CLOSE = "ACTION_BFP_CONNECT_CLOSE";
	public static final String ACTION_MYACTION_BTC_CALL = "MYACTION.BTC.CALL";
	/***********************************************************************/

	/******* 系统广播 ********************/
	public static final String ACTION_ACC_OFF = "ACTION_ACC_OFF";
	public static final String ACTION_ACC_ON = "ACTION_ACC_ON";
	/**********************************/

	public static final String ACTION_BTC_CALL = "MYACTION.BTC.CALL";// 通过蓝牙拨打电话

	public static final String BLUETOOTH_CONNECT_CHANGE = "BLUETOOTH_CONNECT_CHANGE";

	public static final String EXTRA_MUSIC_VOLUME_MUTED = "android.media.EXTRA_MUSIC_VOLUME_MUTED";
	public static final String MUSIC_MUTE_SET_OTHER_ACTION = "android.media.MUSIC_MUTE_SET_OTHER_ACTION";
	public static final String MUSIC_MUTE_RESTORE_ACTION = "android.media.MUSIC_MUTE_RESTORE_ACTION";

	public static final String LOCAL_MUSIC_ACTION = "android.intent.action.SPREADWIN.BLUTOOTHMUSIC";
	public static final String PUSH_MUSIC_PLAY_STATE = "android.intent.action.SPREADWIN.BLUTOOTHMUSIC_STATUS";

	public static final String ACTION_MEDIAKILLED = "com.spreadwin.service.mediakilled";

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

	private boolean mThreadRun = false; // 状态监听线程run控制位

	private int index = 0; // 设备配对倒计时
	private int bfpUpdateTime = 0; // deprecated
	private int mA2dpStatus = BtcGlobalData.A2DP_DISCONNECT; // 蓝牙音乐状态
	public int mBfpStatus = BtcGlobalData.BFP_DISCONNECT;// 蓝牙连接状态
	public int mCallStatus = BtcGlobalData.NO_CALL;// 来电 ，接听，挂断的等状态
	private int mCallStatusOld = BtcGlobalData.NO_CALL;
	private int mPairStatus = BtcGlobalData.NOT_PAIR;
	private int mPowerStatus = BtcGlobalData.NOT_PAIR;
	private int mSyncStatus = BtcGlobalData.NOT_SYNC;// 蓝牙和手机同步状态
	private int mUpdateCalllog = BtcGlobalData.NO_CALL;// 通话记录同步状态

	private NotificationManager nm;
	private SyncBinder binder = new SyncBinder();// service的外部接口
	// private int mUpdaime = 10000;
	/************ hander.msg消息 *************/
	public final int mShowNotification = 1;
	public final int mCancelNotification = 2;
	public final int mPhoneBookSyncBroadcast = 3;
	public final int mAddDatabase = 5;
	public final int mUpdateBookInfoOver = 7;
	/********************************/
	public final int mNewSyncStatus = 4; // deprecated
	public final int mUpdateDataBase = 6; // deprecated
	private int RecordNum = 0; // 临时计数器：改成临时变量
	private final List<PhoneBookInfo> mPhoneBookInfo = Collections.synchronizedList(new ArrayList<PhoneBookInfo>()); // 联系人列表
	ArrayList<String> mPhoneBook = new ArrayList<String>(); // 存联系人 姓名 电话号码
	PhoneBookInfo mSIMContactsInfo;
	PhoneBookInfo mPhoneContactsInfo;
	PhoneBookInfo mCalloutInfo;
	PhoneBookInfo mCallmissInfo;
	PhoneBookInfo mCallinInfo;

	private Messenger mClient;

	boolean mScreenStatus = true;

	List<PhoneBookInfo_new> mContactsInfo = Collections.synchronizedList(new ArrayList<PhoneBookInfo_new>());
	private CharacterParser characterParser;
	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	// private PinyinComparator pinyinComparator;

	int mTempStatus = 0; // 临时储存状态

	String result;

	boolean upload_toggle = false;

	private int mOldBt = 0;// deprecated
	private int mDefaultVoice = 13;// 默认声音大小//deprecated

	public DBAdapter m_DBAdapter = null;

	private ECarOnline mECarOnline;

	private boolean mAccOff = false;

	private WindowManager wm;

	private View view;

	private DialogView mCallView;// 来电的界面

	/**
	 * 来电状态还是呼出状态
	 */
	private boolean isState;

	public static boolean isStarFromVoice;

	public static String mTitle = null;
	public static String mArtist = null;
	public static String mAlbum = null;

	public static LruJsonCache mCache; // 缓存来电归属：换成数据库考虑没网络的情况
	public static boolean isConnect;

	// private OpenUtils openUtils;
	boolean mdatabase; // 是否从数据库读取电话本信息标识位
	// private Handler mHandler = new Handler();

	/************ 数据库和网络获取电话本线程 **************/
	private Runnable mRunnble = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mLog("mDataThread  is start");
			mdatabase = getDatabase();
			mLog("mDataThread  is end");
			if (mdatabase && isNetworkConnected() && isConnect) {
				PullContacts();
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mThreadRun = true;
		mECarOnline = ECarOnline.getInstance(this);
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
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		syncT.start();
		mOldBt = mDefaultVoice;
		mCache = new LruJsonCache();
		onIntentFilter();
	}

	/************ 初始化外界广播 ******************/
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
		filter.addAction(ACTION_MYACTION_BTC_CALL);
		filter.addAction(ACTION_FACTORY_TEST);
		filter.addAction(ACTION_BFP_STATUS_UPDATE);
		filter.addAction(ACTION_CALL_CUSTOMER_SERVICE);
		filter.addAction(ACTION_CUSTOMER_SERVICE_NUMBER);
		filter.addAction(ACTION_ACC_OFF);
		filter.addAction(ACTION_ACC_ON);
		filter.addAction(LOCAL_MUSIC_ACTION);
		filter.addAction(ACTION_MEDIAKILLED);
		registerReceiver(mBatInfoReceiver, filter);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	/********************** 监听底层蓝牙状态线程 *****************/
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

				// 更新歌曲
				if (isPlayTileChenger()) {
					mLog("isPlayTileChenger" + isPlayTileChenger());
					onPlayTitleChenger(true);
				}
				// if (mTitle != BtcNative.getPlayTitle()) {
				// mTitle = BtcNative.getPlayTitle();
				// mArtist = BtcNative.getPlayArtist();
				// mAlbum = BtcNative.getPlayAlbum();
				// Intent mA2dpIntent = new Intent();
				// mA2dpIntent.setAction(MusicFragment.mActionInfoBfp);
				// mA2dpIntent.putExtra("mTitle", BtcNative.getPlayTitle());
				// mA2dpIntent.putExtra("mArtist", BtcNative.getPlayArtist());
				// mA2dpIntent.putExtra("mAlbum", BtcNative.getPlayAlbum());
				// sendBroadcast(mA2dpIntent);
				// }

				// 更新A2dp状态
				mTempStatus = BtcNative.getA2dpStatus();
				 mLog("mTempStatus ==" + mTempStatus + "; getA2dpStatus =="+ mA2dpStatus);
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

	public void onPlayTitleChenger(boolean isCommen) {
		mLog("onPlayTitleChenger");
		if (isCommen) {
			mTitle = BtcNative.getPlayTitle();
			mArtist = BtcNative.getPlayArtist();
			mAlbum = BtcNative.getPlayAlbum();
		} else {
			mTitle = null;
			mArtist = null;
			mAlbum = null;
		}
		Intent mA2dpIntent = new Intent();
		mA2dpIntent.setAction(MusicFragment.mActionInfoBfp);
		mA2dpIntent.putExtra("mTitle", mTitle);
		mA2dpIntent.putExtra("mArtist", mArtist);
		mA2dpIntent.putExtra("mAlbum", mAlbum);
		sendObjMessage(1, mA2dpIntent);
	}

	/**
	 * 歌曲状态
	 */
	protected boolean isPlayTileChenger() {
		if (TextUtils.isEmpty(BtcNative.getPlayTitle()) && !TextUtils.isEmpty(mTitle)) {
			return true;
		} else if (!TextUtils.isEmpty(BtcNative.getPlayTitle()) && !BtcNative.getPlayTitle().equals(mTitle)) {
			return true;
		}
		return false;
	}

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
				String mDateTime = BtcNative.getPhoneBookRecordTimeByIndex(BtcGlobalData.PB_OUT, i);
				if (mNumber == null || mDateTime == null) {
					continue;
				}
				mLog("呼出记录:============" + "姓名：" + mName + "号码：" + mNumber + "时间" + mDateTime);
				String[] str = mDateTime.split("T");
				String time = "";
				// + " "+ str[1].substring(0, 2) + ":" + str[1].substring(2, 4);
				try {
					time = str[0].substring(0, 4) + "-" + str[0].substring(4, 6) + "-" + str[0].substring(6, 8);
				} catch (StringIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				if (isConnect && mName != null && mNumber != null) {
					mCalloutInfo.add(mName, mNumber, time);
				} else {
					mCalloutInfo.clear();
				}
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
				String mDateTime = BtcNative.getPhoneBookRecordTimeByIndex(BtcGlobalData.PB_IN, i);
				if (mNumber == null || mDateTime == null) {
					continue;
				}
				mLog("呼入记录:============" + "姓名：" + mName + "号码：" + mNumber + "时间" + mDateTime);
				String[] str = mDateTime.split("T");
				String time = "";
				// + " "+ str[1].substring(0, 2) + ":" + str[1].substring(2, 4);
				try {
					time = str[0].substring(0, 4) + "-" + str[0].substring(4, 6) + "-" + str[0].substring(6, 8);
				} catch (StringIndexOutOfBoundsException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				mCallinInfo.add(mName, mNumber, time);
			}
		}
		mLog("mCalloutInfo ==" + mCallinInfo.getSize());
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
				String mDateTime = BtcNative.getPhoneBookRecordTimeByIndex(BtcGlobalData.PB_MISS, i);
				if (mNumber == null || mDateTime == null) {
					continue;
				}
				mLog("未接记录:============" + "姓名：" + mName + "号码：" + mNumber + "时间：" + mDateTime);
				String[] str = mDateTime.split("T");
				String time = "";
				// + " "+ str[1].substring(0, 2) + ":" + str[1].substring(2, 4);
				try {
					time = str[0].substring(0, 4) + "-" + str[0].substring(4, 6) + "-" + str[0].substring(6, 8);
				} catch (StringIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				mCallmissInfo.add(mName, mNumber, time);
			}
		}
		mLog("mCalloutInfo ==" + mCallmissInfo.getSize());
		message.arg1 = BtcGlobalData.NEW_SYNC;
		handler.sendMessageDelayed(message, 100);
		mSyncStatus = mTempStatus;
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
		if (!isConnect) {
			mContactsInfo.clear();
			mPhoneBook.clear();
			return;
		}
		// }
		message.arg1 = BtcGlobalData.NEW_SYNC;
		handler.sendMessageDelayed(message, 100);
		mSyncStatus = mTempStatus;
		// isFlage = true;
	}

	/********* service外部接口 ***********/
	public class SyncBinder extends Binder {

		/**
		 * 获取ArrayList<PhoneBookInfo>
		 * 
		 * @return
		 */
		public List<PhoneBookInfo> getPhoneBookInfo() {
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
			mClient = client; // 蓝牙主界面Messenger
		}
	}

	/*************** 蓝牙信息变化更新至蓝牙主界面 ***************/
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

	protected void onBfpStatusChange() {
		Intent mBfpIntent = new Intent();
		mBfpIntent.setAction(MainActivity.mActionBfp);
		if (mTempStatus == BtcGlobalData.BFP_CONNECTED) {
			mLog("start =========== log");
			mSendBluetoothBroadcast(BLUETOOTH_CONNECT_CHANGE, true);
			isConnect = true;
			saySomething("蓝牙已连接");// 语音提示
			handler.sendEmptyMessageDelayed(mShowNotification, MainActivity.mShowDeviceNameDelayed);
			mBfpIntent.putExtra("bfp_status", BtcGlobalData.BFP_CONNECTED);
			// 不自动打开蓝牙音频
			// setBtAudioMode(BtAudioManager.AUDIO_MODE_BT);

		} else if (mTempStatus == BtcGlobalData.BFP_DISCONNECT) {
			saySomething("蓝牙已断开");// 语音提示
			mLog("end =========== log");
			mSendBluetoothBroadcast(BLUETOOTH_CONNECT_CHANGE, false);
			m_DBAdapter.close();
			isConnect = false;
			// 清空歌曲信息
			onPlayTitleChenger(false);
			handler.sendEmptyMessageDelayed(mCancelNotification, 1000);
			mBfpIntent.putExtra("bfp_status", BtcGlobalData.BFP_DISCONNECT);
			// 清空联系人和通话记录数据
			mLog("clear phonebook data");
			mPhoneBook.clear();
			mContactsInfo.clear();
			// mHandler.removeCallbacks(mRunnble);
			for (int i = 0; i < mPhoneBookInfo.size(); i++) {
				mPhoneBookInfo.get(i).clear();
			}

		}
		sendObjMessage(1, mBfpIntent);
		// lbm.sendBroadcast(mBfpIntent);
		mBfpStatus = mTempStatus;
		mECarOnline.onReturnBfpStatus();
	}

	/************** 连接状态监听线程syncT和UI主线的hander *********************/
	Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case mShowNotification:
				// 初始化数据库
				m_DBAdapter.open();
				showNotification();
				// mSendBluetoothBroadcast(BLUETOOTH_CONNECT_CHANGE, true);
				Thread thread = new Thread(mRunnble);
				thread.start();
				// mHandler.post(mRunnble); // 执行从数据库读取电话本
				break;
			case mCancelNotification:
				// 关闭数据库
				cancelNotification();
				// mSendBluetoothBroadcast(BLUETOOTH_CONNECT_CHANGE, false);
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
					//更新联系人给语音助手
					mSendSyncBroadcast();
					mLog("isNetworkConnected ==" + isNetworkConnected());
					if (isNetworkConnected() && upload_toggle) {
						//push到远程服务器上
						PushContacts();
					}
				}
				break;
			case mAddDatabase:
				//将联系人添加到数据库
				addDatabase();
				break;
			case mUpdateBookInfoOver:
				//通知头部更新联系人
				Intent mCallIntent = new Intent();
				mCallIntent.setAction(MainActivity.mActionBookInfoOver);
				sendObjMessage(1, mCallIntent);
				break;
			}
		}
	};

	public void showNotification() {
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.ico, "蓝牙", when);
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
		mLog("card_state==============" + status);
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
				if (mPhoneBook.size() > 0 && isConnect) {
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
				mLog("isNewContacts mPhoneContactsInfo mName==" + mName + "; mNumber ==" + mNumber);
				continue;
			}
			mLog("isNewContacts mPhoneContactsInfo mName==" + mName + ";length==" + mName.length() + "; mNumber =="
					+ mNumber + "; length ==" + mNumber.length());
			isNew = true;
			for (int j = 0; j < tempSize; j++) {
				// mLog("isNewContacts addContactsInfo setName==" + mName +
				// ";mNumber==" + mNumber);
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
		mLog("mContactsInfo ==" + mContactsInfo.size() + "; RecordNum ==" + RecordNum);
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
			// String mNumber = c.getString(numberColumnIndex);return
			// addContactsInfo(mName, mNumber);
			// }
			do {
				String mName = c.getString(nameColumnIndex);
				String mNumber = c.getString(numberColumnIndex);
				addContactsInfo(mName, mNumber);
			} while (c.moveToNext() && isConnect);
			c.close();
			if (!isConnect) {
				mContactsInfo.clear();
				mPhoneBook.clear();
				return true;
			}
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
		} else {
			sortModel.setSortLetters("#");
			sortModel.setSecondLetters("#");
		}
		if (!isConnect) {
			mPhoneBook.clear();
			mContactsInfo.clear();
			return;
		}
		mContactsInfo.add(sortModel);
		// 延迟100ms 发送mActionBookInfoOver
		handler.removeMessages(mUpdateBookInfoOver);
		handler.sendEmptyMessageDelayed(mUpdateBookInfoOver, 100);
	}

	/**
	 * A2dp状态变化
	 */
	protected void onA2dpStatusChange() {
		Intent ibm = new Intent(PUSH_MUSIC_PLAY_STATE);
		Intent mA2dpIntent = new Intent();
		mA2dpIntent.setAction(MainActivity.mActionA2dp);
		if (mTempStatus == BtcGlobalData.A2DP_DISCONNECT) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_DISCONNECT);
		} else if (mTempStatus == BtcGlobalData.A2DP_CONNECTED) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_CONNECTED);
			ibm.putExtra("state", BtcGlobalData.A2DP_CONNECTED);
		} else if (mTempStatus == BtcGlobalData.A2DP_PLAYING) {
			mA2dpIntent.putExtra("a2dp_status", BtcGlobalData.A2DP_PLAYING);
			ibm.putExtra("state", BtcGlobalData.A2DP_PLAYING);
		}
		sendBroadcast(ibm);
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

	protected int isFull() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		int leftStackId = am.getLeftStackId();
		if (leftStackId == -1) {
			leftStackId = 0;
		}
		if (am.getWindowSizeStatus(leftStackId) == 0) {
			mLog("full");
			return 1;
		} else if (am.getWindowSizeStatus(leftStackId) == 1) {
			mLog("notfull" + leftStackId + am.getWindowSizeStatus(leftStackId));
			return 2;
		} else {
			return 3;
		}

	}

	protected void onCallStatusChange() {
		mLog("setMute onCallStatusChange ==" + mTempStatus);
		int lastCallStatus = mCallStatus;
		mLog("mCallStatus onCallStatusChange ==" + mCallStatus);
		mCallStatus = mTempStatus;
		Intent mCallIntent = new Intent();
		mCallIntent.setAction(MainActivity.mActionCall);
		// onChaneAudioFocus(mTempStatus);
		switch (mTempStatus) {
		case BtcGlobalData.CALL_IN:
			isState = true;
			// BtAudioManager.getInstance(this).mAudioFocusGain = true;
			// BtAudioFocus 为false时，设置TempAudioFocus为true,提前获取焦点
			if (!BtAudioManager.getInstance(this).isBtAudioFocus()) {
				BtAudioManager.getInstance(this).setTempBtAudioFocus(BtAudioManager.mTempFocusGain);
			}
			BtAudioManager.getInstance(this).onCallChange(false);

			mCallIntent.putExtra("call_status", BtcGlobalData.CALL_IN);
			mLog("add_window" + BtcGlobalData.CALL_IN);
			addCallView();
			break;
		case BtcGlobalData.IN_CALL:
			// BtAudioManager.getInstance(this).mAudioFocusGain = true;
			BtAudioManager.getInstance(this).onCallChange(true);
			mCallIntent.putExtra("call_status", BtcGlobalData.IN_CALL);
			Intent mIN_CallIntent = new Intent(DialogView.ACTION_BT_CALL_IN);
			sendBroadcast(mIN_CallIntent);
			Log.d("DialogView", "IN_CALL  send broadcast ==" + DialogView.ACTION_BT_CALL_IN);
			removeCallView(true);
			break;
		case BtcGlobalData.CALL_OUT:
			// BtAudioManager.getInstance(this).mAudioFocusGain = true;
			BtAudioManager.getInstance(this).onCallChange(true);
			if (mCLDCall) {
				mCLDCallResult = 0;
			}
			mCallIntent.putExtra("call_status", BtcGlobalData.CALL_OUT);
			mLog("add_window" + BtcGlobalData.CALL_IN);
			isState = false;
			if (!mECarOnline.mECarCall) {
				addCallView();
			}
			break;
		case BtcGlobalData.NO_CALL:
			isState = false;
			// 临时焦点：mTempAudioFocus 为true时，设置失去临时焦点
			if (BtAudioManager.getInstance(this).isTempBtAudioFocusGain()) {
				BtAudioManager.getInstance(this).setTempBtAudioFocus(BtAudioManager.mTempFocusLoss);
			}
			BtAudioManager.getInstance(this).onCallChange(false);

			if (mSyncStatus != BtcGlobalData.IN_SYNC) {
				mLog("startSyncPhoneBook mCallStatusOld ==" + mCallStatusOld + "; lastCallStatus ==" + lastCallStatus);
				if (lastCallStatus == BtcGlobalData.CALL_IN) {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_IN");
//					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_IN);
//					mUpdateCalllog = BtcGlobalData.PB_IN;
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_MISS);
					mUpdateCalllog = BtcGlobalData.PB_MISS;
				} else if (lastCallStatus == BtcGlobalData.CALL_OUT
						|| (lastCallStatus == BtcGlobalData.IN_CALL && mCallStatusOld == BtcGlobalData.CALL_OUT)) {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_OUT");
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_OUT);
					mUpdateCalllog = BtcGlobalData.PB_OUT;
				} else {
					mLog("startSyncPhoneBook ==BtcGlobalData.PB_MISS");
//					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_MISS);
//					mUpdateCalllog = BtcGlobalData.PB_MISS;
					BtcNative.startSyncPhoneBook(BtcGlobalData.PB_IN);
					mUpdateCalllog = BtcGlobalData.PB_IN;
				}
			}
			removeCallView(false);
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
		if (isFull() == 1) {
			msg1.what = 1; // 消息(一个整型值)
		} else if (isFull() == 2) {
			msg1.what = 2;
		} else {
			msg1.what = 3;
		}
		HandlerCallin.sendMessage(msg1);
	}

	private void removeCallView(boolean isCall) {
		finishMainActivity();
		if (isCall) {
			sendBroadcast(new Intent(DialogView.ANSWER_UP));
		} else {
			sendBroadcast(new Intent(DialogView.FINISH_ACTIVITY));
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

	boolean isScreen;

	private void showCallDisplay(int full) {

		mLog("showCallDisplay" + full);

		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();

		// 背景透明
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		// params.format = PixelFormat.TRANSLUCENT;
		params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		// 设置悬浮窗的长得宽
		if (full == 1) {
			params.x = 600;
			params.width = 540;
			isScreen = true;
		} else if (full == 2) {
			params.x = 0;
			params.width = 1000;
			isScreen = false;
			params.gravity = Gravity.LEFT;
		} else if (full == 3) {
			params.x = 0;
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			isScreen = false;
		}
		params.y = 0;
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		if (mCallView == null) {
			mCallView = new DialogView(this, isState, isScreen, binder.getCallName(BtcNative.getCallNumber()));
		} else {
			mCallView.setStatus(isState, isScreen, binder.getCallName(BtcNative.getCallNumber()));
		}
		view = mCallView.getVideoPlayView();
		wm.addView(view, params);
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
					ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
					if (isTopMyself(this,am.getLeftStackId())) {
						mLog("ainActivity.mBroadcast isTopMyself==" + true);
						break;
					}
				}
			}
			sendObjMessage(1, mCallIntent);
		}
		mLog("ainActivity.mBroadcast ==" + MainActivity.mBroadcast);
		sendBroadcast(mCallIntent);
	}

//	/**
//	 * 判断自己是不是在显示
//	 * 
//	 * @return
//	 */
//	public static boolean isTopMyself(Context context) {
//		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//		List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
//		RunningTaskInfo rti = runningTasks.get(0);
//		ComponentName component = rti.topActivity;
//		if (component.getPackageName().equals("com.spreadwin.btc")) {
//			return true;
//		}
//		return false;
//	}
	
	/**
	 * 判断自己是不是在显示
	 * 
	 * @return
	 */
	public static boolean isTopMyself(Context context,int leftStack) {
//		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//		PackageManager pm = (PackageManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		RecentTaskInfo ti = SplitUtil.getTopTaskOfStack(context, leftStack);
		if (ti != null) {
			Intent it = ti.baseIntent;
//			ResolveInfo resolveInfo = pm.resolveActivity(it, 0);
			if ((it.getComponent().getPackageName()).equals("com.spreadwin.btc")) {
				return true;
			}
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
					if (!isConnect) {
						mContactsInfo.clear();
						mPhoneBook.clear();
						return;
					}
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
	 * 广播Receiver：语音,主界面控制，系统广播
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
			} else if (intent.getAction().equals(LOCAL_MUSIC_ACTION)) {
				String state = intent.getStringExtra("state");
				mLog(state);
				if (state.equals("music_last")) {
					mLog("BtcNative.lastSong()");
					BtcNative.lastSong();
				} else if (state.equals("music_next")) {
					mLog("BtcNative.nextSong()");
					BtcNative.nextSong();
				} else if (state.equals("music_pause")) {
					mLog("BtcNative.pauseMusic()");
					BtcNative.pauseMusic();
				} else if (state.equals("music_play")) {
					mLog("BtcNative.playMusic()");
					BtcNative.playMusic();
					BtAudioManager.getInstance(getApplicationContext()).onBtAudioFocusChange(true);
				}
			} else if (intent.getAction().equals(ACTION_MYACTION_BTC_CALL)) {
				dialCall(intent.getStringExtra("call_number"));
			} else if (intent.getAction().equals(ACTION_MEDIAKILLED)) {
				mLog("接受到媒体库挂断广播");
				BtAudioManager.getInstance(getApplicationContext()).setMediaKillMode();
			}
		}
	};

	// 拨打电话
	public void dialCall(String callNumber) {
		mLog("dialCall ==" + callNumber);
		if (callNumber.length() > 0) {
			BtcNative.dialCall(callNumber);
		}
	}

	public void saySomething(String something) {
		Intent i = new Intent("ACTION_SAY_SOMETHING");
		i.putExtra("EXTRA_SAY_SOMETHING", something);
		sendBroadcast(i);
	}

	private void setMute(boolean status, int CallStatus) {
		mLog("setMute status ==" + status + "; CallStatus ==" + CallStatus + ";BtcNative.getVolume() =="
				+ BtcNative.getVolume());
		BtAudioManager.getInstance(this).onAudioMuteChange(status);
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
