	package com.spreadwin.btc;

import com.spreadwin.btc.Bluetooth.BluetoothFragment;
import com.spreadwin.btc.Calllogs.CallLogsFragment;
import com.spreadwin.btc.Music.MusicFragment;
import com.spreadwin.btc.contacts.ContactsFragment;
import com.spreadwin.btc.utils.BtcGlobalData;
import com.spreadwin.btc.utils.ControlVolume;
import com.spreadwin.btc.view.CustomDialog;
import com.spreadwin.btc.view.CustomDialogText;

import android.R.bool;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnClickListener, OnLayoutChangeListener {
	public static final String TAG = "MainActivity";
	public static final boolean DEBUG = true;

	public static final String MUSIC_MUTE_CHANGED_ACTION = "android.media.MUSIC_MUTE_CHANGED_ACTION";
	public static final String EXTRA_MUSIC_VOLUME_MUTED = "android.media.EXTRA_MUSIC_VOLUME_MUTED";

	public static final String mActionSync = "com.spreadwin.btc.sync";
	public static final String mActionA2dp = "com.spreadwin.btc.a2dp";
	public static final String mActionBfp = "com.spreadwin.btc.bfp";
	public static final String mActionCall = "com.spreadwin.btc.call";
	public static final String mActionPair = "com.spreadwin.btc.pair";
	public static final String mAcitonFinish = "com.spreadwin.btc.finish";

	public static final String mActionBookInfoOver = "com.spreadwin.btc.over";

	public static final String mActionFull = "android.intent.action.SPLIT_WINDOW_HAS_CHANGED";

	public static final String ACTION_BT_CALL_IN = "ACTION_BT_CALL_IN";
	public static final String EXTRA_BT_CALL_IN_NAME = "EXTRA_BT_CALL_IN_NAME";
	public static final String EXTRA_BT_CALL_IN_NUMBER = "EXTRA_BT_CALL_IN_NUMBER";

	// public static final String ACTION_BT_CALL_ANSWER =
	// "ACTION_BT_CALL_ANSWER";
	// public static final String ACTION_BT_CALL_REJECT =
	// "ACTION_BT_CALL_REJECT";

	private final int DIALOG1 = 1;
	private final int DIALOG2 = 2;
	private final int DIALOG3 = 3;

	private final int mMessageCall = 0;
	private final int mMessageActionCall = 1;
	private final int mMessageShowDeviceName = 2;
	private final int mMessageHideVolume = 3;
	private final int mMessageShowBluetoothName = 4;
	private final int mMessageNotifyData = 5;
	public static int mShowDeviceNameDelayed = 3500;
	private final int mHideVolumeDelayed = 3000;
	private final int mShowNameDelayed = 1000;

	MusicFragment mMusicRightFragment, mMusicFragment;
	public static BluetoothFragment mBluetoothFragment;
	CallLogsFragment mCallLogsFragment;
	ContactsFragment mContactsFragment;

	LinearLayout mCallLogsLayout, mContactsLayout, mMusicLayout, mLeftMenu, mRedialLayout, mBluetoothLayout,
			mMusicLayoutAdd, main, mDescription;
	TextView mCalllogsTitle, mContactsTitle, mRedialTitle, mMusicTitle;
	private boolean binded;
	LocalBroadcastManager mLocalBroadcastManager;
	BroadcastReceiver mBroadcastReceiver;
	BroadcastReceiver mContactsReceiver;
	BroadcastReceiver mVoiceReceiver;
	public static boolean mBroadcast = false;
	public static SyncService.SyncBinder binder;
	ImageButton mDialButton;
	ImageButton mdroppedbutton;
	TextView mNumberText;
	TextView mNameText;
	TextView mBluetoothStatus;
	TextView mBluetoothName;
	TextView mContectText;
	AudioManager audioManager;
	Dialog mCallDialog = null;
	int audioMax;
	FragmentManager mAddBluetoothMusicFm = getFragmentManager();
	FragmentTransaction mAddFragment = mAddBluetoothMusicFm.beginTransaction();
	int white = R.color.white;
	int blue = R.color.blue;
	// 自定义音量条
	private ControlVolume view_MyControlVolume;

	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;
	String callNumber;
	String callName;
	FrameLayout mAddLayout, mFragmetContext;
	// private ViewTreeObserver mVto;

	boolean tempApp = false; // 为true时，活动结束后退到后台

	private String isFull = null;
	// private BtAudioManager mBtAudioManager;
	// boolean phoneCall;
	boolean isOrso;
	boolean isCall;

	// private final int mLeftMode = 1;
	// private final int mRightMode = 2;
	// private final int mFullMode = 3;
	// private int mLayoutMode = 0;

	private CustomDialog.Builder builder;

	final IncomingHandler mIncomingHandler = new IncomingHandler();

	public final Messenger mMessenger = new Messenger(mIncomingHandler);
	
	
	//获取左右边stackID
	int stackLeftId;
	int stackRegihtId;

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			binder = (SyncService.SyncBinder) service;
			binder.setClientMessager(mMessenger);
			binded = true;
			if (binder.getBfpStatuss() == BtcGlobalData.BFP_CONNECTED) {
				mBluetoothLayout.setBackgroundResource(R.drawable.duankailanya_u);
				mBluetoothStatus.setText(getResources().getString(R.string.connect_title));
				handler.sendEmptyMessageDelayed(mMessageShowDeviceName, mShowDeviceNameDelayed);
				updateContacts(binder.getPhoneBookInfo_new().size());
			}
			mLog("onServiceConnected 1111 arg0 ==" + arg0);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mLog("onServiceDisconnected 2222 arg0 ==" + arg0);
			binder = null;
			binded = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		init();
		mLog("MainActivity onCreate1111");
		// 读取状态
		// try {
		// mPowerManager = (PowerManager)
		// getSystemService(Context.POWER_SERVICE);
		// mWakeLock = mPowerManager.newWakeLock(
		// PowerManager.PARTIAL_WAKE_LOCK, TAG);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// mBtAudioManager = BtAudioManager.getInstance(this);
		setVolumeControlStream(10);
	}

	// @Override
	// public void onWindowFocusChanged(boolean hasFocus) {
	// // TODO Auto-generated method stub
	// super.onWindowFocusChanged(hasFocus);
	// }

	@Override
	protected void onResume() {
		super.onResume();
		mLog("MainActivity onResume");
		parserIntent();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mLog("MainActivity onNewIntent action ==" + intent.getAction());
		setIntent(intent);
		if (getIntent().getAction() != null && binder != null) {
			mMusicFragment.setCallStatus(binder.getCallStatus());
			if (mMusicRightFragment != null) {

				mMusicRightFragment.setCallStatus(binder.getCallStatus());
			}
		}
	}

	/**
	 * 解析intent
	 */
	private void parserIntent() {
		tempApp = false;
		String action = getIntent().getAction();
		mLog("MainActivity parserIntent action ==" + action);
		if (action != null) {
			if (action.equals("MYACTION.BTC.CALL")) {
				callNumber = getIntent().getStringExtra("call_number");
				callName = getIntent().getStringExtra("call_name");
				tempApp = true;
				if (callNumber != null || callName != null) {
					FragmentManager fm = getFragmentManager();
					FragmentTransaction transaction = fm.beginTransaction();
					if (mBluetoothFragment == null) {
						mBluetoothFragment = new BluetoothFragment();
					}
					transaction.replace(R.id.id_fragment_content, mBluetoothFragment);
					transaction.commit();
					handler.sendEmptyMessage(mMessageCall);
					SyncService.isStarFromVoice = true;
				}
			} else if (action.equals(mActionCall)) {
				tempApp = true;
				mLog("MainActivity parserIntent action222222222 ==" + mActionCall);
				handler.sendEmptyMessageDelayed(mMessageActionCall, 100);
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// isAppRunning = false;
		// unregisterReceiver(mScreenSizeChangeReceiver);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLog("MainActivity onStart 222222");

	}

	private void init() {
		mCallLogsLayout = (LinearLayout) findViewById(R.id.calllogs_layout);
		mContactsLayout = (LinearLayout) findViewById(R.id.contacts_layout);
		mMusicLayout = (LinearLayout) findViewById(R.id.music_layout);
		mRedialLayout = (LinearLayout) findViewById(R.id.redial_layout);
		mMusicLayoutAdd = (LinearLayout) findViewById(R.id.music_layout_add);
		mBluetoothLayout = (LinearLayout) findViewById(R.id.bluetooth_layout);
		mBluetoothStatus = (TextView) findViewById(R.id.contect_status);
		mBluetoothName = (TextView) findViewById(R.id.bluetoot_name);
		view_MyControlVolume = (ControlVolume) findViewById(R.id.view_MyControlVolume);
		mAddLayout = (FrameLayout) findViewById(R.id.add_bluetooth_music);
		mFragmetContext = (FrameLayout) findViewById(R.id.id_fragment_content);
		mCalllogsTitle = (TextView) findViewById(R.id.calllogs_title);
		mContactsTitle = (TextView) findViewById(R.id.contacts_title);
		mRedialTitle = (TextView) findViewById(R.id.redial_title);
		mMusicTitle = (TextView) findViewById(R.id.music_title);

		mLeftMenu = (LinearLayout) findViewById(R.id.left_menu);
		main = (LinearLayout) findViewById(R.id.main);
		mDescription = (LinearLayout) findViewById(R.id.description);

		mContectText = (TextView) findViewById(R.id.contect_text);
		main.addOnLayoutChangeListener(this);

		ActivityManager am = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
		stackLeftId = am.getLeftStackId();
		stackRegihtId = am.getRightStackId();

		builder = new com.spreadwin.btc.view.CustomDialog.Builder(this);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				handler.sendEmptyMessageDelayed(mMessageShowBluetoothName, mShowNameDelayed);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		mCallLogsLayout.setOnClickListener(this);
		mContactsLayout.setOnClickListener(this);
		mMusicLayout.setOnClickListener(this);
		mRedialLayout.setOnClickListener(this);
		mBluetoothLayout.setOnClickListener(this);
		mBluetoothName.setOnClickListener(this);

		mCallLogsFragment = new CallLogsFragment();
		mContactsFragment = new ContactsFragment();
		mBluetoothFragment = new BluetoothFragment();
		mMusicFragment = new MusicFragment();


		Intent intent = new Intent(this, SyncService.class);
		startService(intent);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		setDefaultFragment();
		registerReceiver();
		handler.sendEmptyMessageDelayed(mMessageShowBluetoothName, mShowNameDelayed);

	}

	private void registerReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				mLog("mBroadcastReceiver ==" + intent.getAction());
				if (intent.getAction().equals(mActionSync)) {
					int mStatus = intent.getIntExtra("sync_status", BtcGlobalData.NOT_SYNC);
					mLog("Receiver mActionSync mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.NEW_SYNC) {
						handler.sendEmptyMessage(mMessageNotifyData);
					} else if (mStatus == BtcGlobalData.IN_SYNC) {
						mCallLogsFragment.showLoading();
						if (binder.getmUpdateStatus() == BtcGlobalData.NO_CALL) {
							// mContactsFragment.showLoading();
						}
					} else {
						mContactsFragment.hideLoading();
					}
				} else if (intent.getAction().equals(mActionA2dp)) {
					int mStatus = intent.getIntExtra("a2dp_status", BtcGlobalData.A2DP_DISCONNECT);
					mLog("Receiver mActionA2dp mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.A2DP_DISCONNECT) {
						mMusicFragment.setA2dpStatus(BtcGlobalData.A2DP_DISCONNECT);
					} else if (mStatus == BtcGlobalData.A2DP_CONNECTED) {
						mMusicFragment.setA2dpStatus(BtcGlobalData.A2DP_CONNECTED);
					} else if (mStatus == BtcGlobalData.A2DP_PLAYING) {
						mMusicFragment.setA2dpStatus(BtcGlobalData.A2DP_PLAYING);
					}
					if (mMusicRightFragment != null) {

						mMusicRightFragment.checkA2dpStatus();
					}
					mMusicFragment.checkA2dpStatus();
				} else if (intent.getAction().equals(mActionCall)) {
					int mStatus = intent.getIntExtra("call_status", BtcGlobalData.NO_CALL);
					mLog("Receiver mActionCall mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.CALL_IN) {
						mShowDialog(DIALOG1);
					} else if (mStatus == BtcGlobalData.CALL_OUT) {
						// setDefaultFragment();
						mBluetoothFragment.setCallStatus(BtcGlobalData.CALL_OUT);
					} else if (mStatus == BtcGlobalData.IN_CALL) {
						mDismissDialog(DIALOG1);
						mBluetoothFragment.setCallStatus(BtcGlobalData.IN_CALL);
						if (tempApp) {
							moveTaskToBack(true);
						}
					} else {
						mDismissDialog(DIALOG1);
						mBluetoothFragment.setCallStatus(BtcGlobalData.NO_CALL);
						mLog("Receiver mActionCall mStatus NO_CALL  tempApp==" + tempApp);
						if (tempApp) {
							moveTaskToBack(true);
						}
					}
				} else if (intent.getAction().equals(mActionPair)) {
					int mStatus = intent.getIntExtra("pair_status", BtcGlobalData.NOT_PAIR);
					mLog("Receiver mActionPair mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.NOT_PAIR) {

					} else if (mStatus == BtcGlobalData.IN_PAIR) {

					} else if (mStatus == BtcGlobalData.PAIRRED) {
					}
				} else if (intent.getAction().equals(mActionBfp)) {
					int mStatus = intent.getIntExtra("bfp_status", BtcGlobalData.BFP_DISCONNECT);
					mLog("Receiver mActionBfp mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.BFP_CONNECTED) {
						mBluetoothLayout.setBackgroundResource(R.drawable.duankailanya_u);
						mBluetoothStatus.setText(getResources().getString(R.string.connect_title));
						handler.sendEmptyMessageDelayed(mMessageShowDeviceName, mShowDeviceNameDelayed);
						mLog("Receiver mMusicFragment222 isVisible ==" + mMusicFragment.isVisible());
						if (SyncService.isTopMyself(getBaseContext(), stackLeftId)
								|| SyncService.isTopMyself(getBaseContext(), stackRegihtId)) {
							if (mMusicFragment.isVisible()) {
								mMusicFragment.openAudioMode();
							}
							if (mMusicRightFragment.isVisible()) {
								if (mMusicRightFragment != null) {
									mMusicRightFragment.openAudioMode();
								}
							}
						}
						// LockScreen();
					} else if (mStatus == BtcGlobalData.BFP_DISCONNECT) {
						// UnLockScreen();

						mBluetoothLayout.setBackgroundResource(R.drawable.duankailanya_d);
						mBluetoothFragment.setCallStatus(BtcGlobalData.NO_CALL);
						mBluetoothStatus.setText(getResources().getString(R.string.disconnect_title));
						handler.sendEmptyMessage(mMessageNotifyData);
						mDismissDialog(DIALOG1);
					}
				} else if (intent.getAction().equals(mAcitonFinish)) {
					MainActivity.this.finish();
				}

			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(mActionSync);
		intentFilter.addAction(mActionA2dp);
		intentFilter.addAction(mActionCall);
		intentFilter.addAction(mActionPair);
		intentFilter.addAction(mActionBfp);
		intentFilter.addAction(mAcitonFinish);
		intentFilter.addAction(mActionBookInfoOver);
		intentFilter.addAction(mActionFull);

		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mBroadcast = true;
		mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, intentFilter);
	}

	public Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case mMessageCall:
				mLog("handleMessage 1111callName ==" + callName + "; callNumber ==" + callNumber);
				for (int i = 0; i < 3; i++) {
					if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
						if (callNumber != null) {
							mBluetoothFragment.dialCall(callNumber);
						} else if (callName != null && binder != null && getCallNumber(callName) != null) {
							mLog("getCallNumber ==" + getCallNumber(callName));
							if (getCallNumber(callName).equals("more")) {
								mShowDialog(DIALOG3);
								mBluetoothLayout.showContextMenu();
								// getWindow().getDecorView().showContextMenu();
							} else {
								mBluetoothFragment.dialCall(getCallNumber(callName));
							}
						}
						// 重置Intent的action信息
						setIntent(new Intent());
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				break;
			case mMessageActionCall:
				int mStatus = getIntent().getIntExtra("call_status", BtcGlobalData.NO_CALL);
				// mLog("handler mStatus ==" + mStatus +
				// "; binder.getCallStatus( )==" + binder.getCallStatus());
				if (mStatus == BtcGlobalData.CALL_IN && binder.getCallStatus() == BtcGlobalData.CALL_IN) {
					mShowDialog(DIALOG1);
				} else if (mStatus == BtcGlobalData.CALL_OUT) {
					mBluetoothFragment.setCallStatus(BtcGlobalData.CALL_OUT);
				} else if (mStatus == BtcGlobalData.IN_CALL) {
					mBluetoothFragment.setCallStatus(BtcGlobalData.IN_CALL);
					mDismissDialog(DIALOG1);
				} else if (mStatus == BtcGlobalData.NO_CALL) {
					mDismissDialog(DIALOG1);
					mBluetoothFragment.setCallStatus(BtcGlobalData.NO_CALL);
				}
				break;

			case mMessageShowDeviceName:
				mLog("handleMessage BtcNative.getPairDeviceName(0) ==" + BtcNative.getPairDeviceName(0));
				if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
					if (BtcNative.getPairDeviceName(0).length() > 0) {
						mBluetoothStatus.setText(BtcNative.getPairDeviceName(0));
					} else {
						mBluetoothStatus.setText(getResources().getString(R.string.connect_title));
					}
				} else {
					mBluetoothStatus.setText(getResources().getString(R.string.disconnect_title));
				}
				break;
			case mMessageHideVolume:
				view_MyControlVolume
						.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));
				view_MyControlVolume.setVisibility(View.GONE);
				break;
			case mMessageShowBluetoothName:
				Log.d("getDeviceName", BtcNative.getDeviceName());
				if (BtcNative.getDeviceName().length() > 0) {
					mBluetoothName.setText(getResources().getString(R.string.device_name) + BtcNative.getDeviceName()
							+ " " + getResources().getString(R.string.help_text));
				} else {
					handler.sendEmptyMessageDelayed(mMessageShowBluetoothName, mShowNameDelayed);
				}
				break;
			case mMessageNotifyData:
				mContactsFragment.notifyDataSetChanged();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mCallLogsFragment.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};

	protected void mDismissDialog(int dIALOG12) {
		if (mCallDialog != null && mCallDialog.isShowing()) {
			Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
			sendBroadcast(mCallIntent);
			// isCall = true;
			setDefaultColor();
			mCallDialog.dismiss();
		}
	}

	protected void mShowDialog(int id) {
		if (id == DIALOG1) {
			if (mCallDialog != null && mCallDialog.isShowing()) {
				return;
			}
		} else if (id == DIALOG2) {
			CustomDialogText.Builder builder = new CustomDialogText.Builder(this);
			builder.setTitle("温馨提示");
			builder.setMessage("确定断开蓝牙连接吗?");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					BtcNative.disconnectPhone();
					dialog.dismiss();
					mLog("BtcNative.disconnectPhone()" + "已断开");
				}
			});
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.crater().show();

		} else if (id == DIALOG3) {
			ListView lv = new ListView(this);
			final String[] number = new String[binder.getCallNumberList(callName).size()];
			for (int i = 0; i < number.length; i++) {
				number[i] = "拨打 :" + binder.getCallNumberList(callName).get(i);
			}
			lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, number));
			// builder.setView(lv);
			// builder.setTitle(callName);
			// mCallDialog = builder.create();
			// mCallDialog.setCanceledOnTouchOutside(false);
			mCallDialog.show();
		}
	}

	public String getCallNumber(String getCallName) {
		String mCallNmber = null;
		if (binder != null) {
			return binder.getCallNumber(getCallName);
		}
		return mCallNmber;
	}

	// private String getCallName(String getCallNumber) {
	// String mCallName = "";
	// if (binder != null) {
	// return binder.getCallName(getCallNumber);
	// }
	// return mCallName;
	// }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (binded) {
			unbindService(conn);
		}
		// Intent intent = new Intent(this, SyncService.class);
		// stopService(intent);
		if (mLocalBroadcastManager != null && mBroadcastReceiver != null) {
			mBroadcast = false;
			mLog("onDestroy()");
			mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
		}
		// if (mVoiceReceiver != null) {
		// unregisterReceiver(mVoiceReceiver);
		// }
		// try {
		//
		// mVto.removeGlobalOnLayoutListener(this);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void setDefaultFragment() {
		setDefaultColor();
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		if (mBluetoothFragment == null) {
			mBluetoothFragment = new BluetoothFragment();
		}
		transaction.replace(R.id.id_fragment_content, mBluetoothFragment);
		try {
			transaction.commitAllowingStateLoss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setDefaultColor() {
		mCalllogsTitle.setTextColor(getResources().getColor(white));
		mContactsTitle.setTextColor(getResources().getColor(white));
		mRedialTitle.setTextColor(getResources().getColor(blue));
		mMusicTitle.setTextColor(getResources().getColor(white));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			Toast.makeText(this, getResources().getString(R.string.help_text), Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setChangeColor(int position) {
		switch (position) {
		case 0:
			mCalllogsTitle.setTextColor(getResources().getColor(blue));
			mContactsTitle.setTextColor(getResources().getColor(white));
			mRedialTitle.setTextColor(getResources().getColor(white));
			mMusicTitle.setTextColor(getResources().getColor(white));
			break;
		case 1:
			mCalllogsTitle.setTextColor(getResources().getColor(white));
			mContactsTitle.setTextColor(getResources().getColor(blue));
			mRedialTitle.setTextColor(getResources().getColor(white));
			mMusicTitle.setTextColor(getResources().getColor(white));
			break;
		case 2:
			setDefaultColor();
			break;
		case 3:
			mCalllogsTitle.setTextColor(getResources().getColor(white));
			mContactsTitle.setTextColor(getResources().getColor(white));
			mRedialTitle.setTextColor(getResources().getColor(white));
			mMusicTitle.setTextColor(getResources().getColor(blue));
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mBluetoothLayout) {
			mDisconnectPhone();
		} else if (v == mDialButton) {
			mLog("onClick mDialButton");
			answerCall();
		} else if (v == mdroppedbutton) {
			mLog("onClick mdroppedbutton");
			denyCall();
		} else if (v == mBluetoothName) {
			builder.crater().show();
		} else {
			FragmentManager fm = getFragmentManager();
			FragmentTransaction transaction = fm.beginTransaction();
			switch (v.getId()) {
			case R.id.calllogs_layout:
				if (mCallLogsFragment == null) {
					mCallLogsFragment = new CallLogsFragment();
				}
				setChangeColor(0);
				if (mCallLogsFragment.isAdded()) {
					return;
				}
				transaction.replace(R.id.id_fragment_content, mCallLogsFragment);
				break;
			case R.id.contacts_layout:
				if (mContactsFragment == null) {
					mContactsFragment = new ContactsFragment();
				}
				setChangeColor(1);
				if (mContactsFragment.isAdded()) {
					return;
				}
				transaction.replace(R.id.id_fragment_content, mContactsFragment);
				break;
			case R.id.redial_layout:
				if (mBluetoothFragment == null) {
					mBluetoothFragment = new BluetoothFragment();
				}
				setChangeColor(2);
				if (mBluetoothFragment.isAdded()) {
					return;
				}
				transaction.replace(R.id.id_fragment_content, mBluetoothFragment);
				break;
			case R.id.music_layout:
				if (mMusicFragment == null) {
					mMusicFragment = new MusicFragment();
				}
				setChangeColor(3);
				if (mMusicFragment.isAdded()) {
					return;
				}
				transaction.replace(R.id.id_fragment_content, mMusicFragment);
				break;
			}
			// transaction.addToBackStack();
			// 事务提交
			// transaction.commit();
			transaction.commitAllowingStateLoss();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		mLog("onKeyDown ==" + keyCode);
		if (keyCode == event.KEYCODE_VOLUME_DOWN) {
			setBTVolume(false);
			return true;
		} else if (keyCode == event.KEYCODE_VOLUME_UP) {
			setBTVolume(true);
			return true;
		} else if (keyCode == event.KEYCODE_BACK) {
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * true 为加一级蓝牙声音，false为减一级蓝牙声音，范围0-16
	 */
	private void setBTVolume(boolean toggle) {
		int mCurV = BtcNative.getVolume();
		// int mCurV = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mLog("setBTVolume mCurV ==" + mCurV);
		mCurV = view_MyControlVolume.setImgBg(mCurV, toggle);
		if (mCurV != BtcNative.getVolume()) {
			// 返回已经处理过的值
			mLog("setBTVolume mCurV2222 ==" + mCurV);
			BtcNative.setVolume(mCurV);
		}
		setVisibility(mCurV);
	}

	// 控制显示当前音量
	private void setVisibility(int cur) {
		if (view_MyControlVolume.getVisibility() == View.GONE) {
			view_MyControlVolume.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
			view_MyControlVolume.setVisibility(View.VISIBLE);
			handler.sendEmptyMessageDelayed(mMessageHideVolume, mHideVolumeDelayed);
		} else {
			handler.removeMessages(3);
			handler.sendEmptyMessageDelayed(mMessageHideVolume, mHideVolumeDelayed);
		}
	}

	private void mDisconnectPhone() {
		if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
			mShowDialog(DIALOG2);
		} else {
			BtcNative.enterPair();
		}

	}

	private void denyCall() {
		BtcNative.denyCall();
		mDismissDialog(DIALOG1);
	}

	private void answerCall() {
		BtcNative.answerCall();
		// setDefaultFragment();
		mDismissDialog(DIALOG1);
	}

	private void LockScreen() {
		if (mWakeLock != null) {
			try {
				if (mWakeLock.isHeld() == false) {
					mWakeLock.acquire();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void UnLockScreen() {

		if (mWakeLock != null) {
			try {
				if (mWakeLock.isHeld()) {
					mWakeLock.release();
					mWakeLock.setReferenceCounted(false);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Intent intent = (Intent) msg.obj;
			mLog("IncomingHandler  Action ==" + intent.getAction());
			if (intent.getAction().equals(mActionSync)) {
				int mStatus = intent.getIntExtra("sync_status", BtcGlobalData.NOT_SYNC);
				mLog("Receiver mActionSync mStatus ==" + mStatus);
				if (mStatus == BtcGlobalData.NEW_SYNC) {
					handler.sendEmptyMessage(mMessageNotifyData);
				} else if (mStatus == BtcGlobalData.IN_SYNC) {
					mCallLogsFragment.showLoading();
					if (binder.getmUpdateStatus() == BtcGlobalData.NO_CALL) {
						mContactsFragment.showLoading();
					}
				} else {
					mContactsFragment.hideLoading();
				}
			} else if (intent.getAction().equals(mActionA2dp)) {
				int mStatus = intent.getIntExtra("a2dp_status", BtcGlobalData.A2DP_DISCONNECT);
				mLog("Receiver mActionA2dp mStatus ==" + mStatus);
				if (mStatus == BtcGlobalData.A2DP_DISCONNECT) {
					mMusicFragment.setA2dpStatus(BtcGlobalData.A2DP_DISCONNECT);
				} else if (mStatus == BtcGlobalData.A2DP_DISCONNECT) {
					mMusicFragment.setA2dpStatus(BtcGlobalData.A2DP_DISCONNECT);
				} else if (mStatus == BtcGlobalData.A2DP_PLAYING) {
					mMusicFragment.setA2dpStatus(BtcGlobalData.A2DP_PLAYING);
				}
			} else if (intent.getAction().equals(mActionCall)) {
				int mStatus = intent.getIntExtra("call_status", BtcGlobalData.NO_CALL);
				mLog("Receiver mActionCall mStatus ==" + mStatus);
				if (mStatus == BtcGlobalData.CALL_IN) {
					mShowDialog(DIALOG1);
				} else if (mStatus == BtcGlobalData.CALL_OUT) {
					// setDefaultFragment();
//					mBluetoothFragment.setCallStatus(BtcGlobalData.CALL_OUT);
				} else if (mStatus == BtcGlobalData.IN_CALL) {
					// 来电时
					mDismissDialog(DIALOG1);
//					mBluetoothFragment.setCallStatus(BtcGlobalData.IN_CALL);
					// if (tempApp) {
					// moveTaskToBack(true);
					// }
				} else {
					mDismissDialog(DIALOG1);
//					mBluetoothFragment.setCallStatus(BtcGlobalData.NO_CALL);
					mLog("Receiver mActionCall mStatus NO_CALL  tempApp==" + tempApp);
					// if (tempApp) {
					// moveTaskToBack(true);
					// }
				}
			} else if (intent.getAction().equals(mActionPair)) {
				int mStatus = intent.getIntExtra("pair_status", BtcGlobalData.NOT_PAIR);
				mLog("Receiver mActionPair mStatus ==" + mStatus);
				if (mStatus == BtcGlobalData.NOT_PAIR) {

				} else if (mStatus == BtcGlobalData.IN_PAIR) {

				} else if (mStatus == BtcGlobalData.PAIRRED) {

				}
			} else if (intent.getAction().equals(mActionBfp)) {
				int mStatus = intent.getIntExtra("bfp_status", BtcGlobalData.BFP_DISCONNECT);
				mLog("Receiver mActionBfp mStatus ==" + mStatus);
				if (mStatus == BtcGlobalData.BFP_CONNECTED) {
					mBluetoothLayout.setBackgroundResource(R.drawable.duankailanya_u);
					mBluetoothStatus.setText(getResources().getString(R.string.connect_title));
					handler.sendEmptyMessageDelayed(mMessageShowDeviceName, mShowDeviceNameDelayed);
					mLog("Receiver mMusicFragment isVisible ==" + mMusicFragment.isVisible());

					if (SyncService.isTopMyself(getBaseContext(), stackLeftId)
							|| SyncService.isTopMyself(getBaseContext(), stackRegihtId)) {
						if (mMusicFragment.isVisible()) {
							mLog("Receiver mMusicFragment 11111111");
							mMusicFragment.openAudioMode();
						}
						if (mMusicRightFragment != null) {
							mLog("Receiver mMusicRightFragment isVisible ==" + mMusicRightFragment.isVisible()
									+ "; getVisibility ==" + mMusicRightFragment.getView().getVisibility()
									+ "; mAddLayout ==" + mAddLayout.getVisibility() + View.GONE);
							if (getFragmentManager().findFragmentById(R.id.add_bluetooth_music) == mMusicRightFragment
									&& mMusicRightFragment.isVisible() && mAddLayout.getVisibility() == View.VISIBLE) {
								mMusicRightFragment.openAudioMode();
							}
						}
					}
					// LockScreen();
				} else if (mStatus == BtcGlobalData.BFP_DISCONNECT) {
					// UnLockScreen();
					updateContacts(0);
					mLog("BtcGlobalData.BFP_DISCONNECT");
					mBluetoothLayout.setBackgroundResource(R.drawable.duankailanya_d);
					mBluetoothFragment.setCallStatus(BtcGlobalData.NO_CALL);
					mBluetoothStatus.setText(getResources().getString(R.string.disconnect_title));
					/**
					 * 音乐界面
					 */
					mMusicFragment.setPlayTitle(intent);
					if (mMusicRightFragment != null) {
						mMusicRightFragment.setPlayTitle(intent);
					}
					handler.sendEmptyMessage(mMessageNotifyData);
					mDismissDialog(DIALOG1);
				}
			} else if (intent.getAction().equals(mAcitonFinish)) {
				MainActivity.this.finish();
			} else if (intent.getAction().equals(mActionBookInfoOver)) {
				if (binder != null) {
					updateContacts(binder.getPhoneBookInfo_new().size());
				}
			} else if (intent.getAction().equals(MusicFragment.mActionInfoBfp)) {
				mMusicFragment.setPlayTitle(intent);
				if (mMusicRightFragment != null) {
					mMusicRightFragment.setPlayTitle(intent);
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}

	public void updateContacts(int i) {
		if (i != 0) {
			mContectText.setText("已更新" + i + "位联系人");
			if (!isOrso) {
				mContectText.setVisibility(View.VISIBLE);
			} else {
				mContectText.setVisibility(View.GONE);
			}
		} else {
			mContectText.setVisibility(View.GONE);
		}
	}

	/**
	 * 半屏
	 */
	public void setScreen() {
		// isOrso = false;
		mMusicLayoutAdd.setVisibility(View.VISIBLE);
		mAddLayout.setVisibility(View.GONE);
		mFragmetContext.setVisibility(View.VISIBLE);
		mLeftMenu.setVisibility(View.VISIBLE);
	}

	/**
	 * 全屏
	 */
	public void setFullScreen() {
		Log.d(TAG, "mFullMode");
		mMusicLayoutAdd.setVisibility(View.GONE);
		mAddLayout.setVisibility(View.VISIBLE);
		mFragmetContext.setVisibility(View.VISIBLE);
		mLeftMenu.setVisibility(View.VISIBLE);
		if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
			mContectText.setVisibility(View.VISIBLE);
		}
		addRightBluetoothMusic();
		if (getFragmentManager().findFragmentById(R.id.id_fragment_content) == mMusicFragment) {
			setDefaultFragment();
		}
	}

	LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);

	LinearLayout.LayoutParams mLeftBottomParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);

	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.WRAP_CONTENT, 1);

	LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.MATCH_PARENT);

	private void onLeftLayout() {
		layoutParams.setMargins(0, 0, 20, 0);// 4个参数按顺序分别是左上右下
		mBluetoothName.setLayoutParams(layoutParams);
		params.setMargins(15, 0, 0, 0);// 4个参数按顺序分别是左上右下
		mBluetoothStatus.setLayoutParams(params);

		leftParams.setMargins(10, 0, 0, 0);
		mLeftMenu.setLayoutParams(leftParams);
		if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
			mContectText.setVisibility(View.VISIBLE);
		}
		Log.d(TAG, "显示联系人");
	}

	private void onRightLayout(boolean isTrue) {
		layoutParams.setMargins(0, 0, 160, 0);// 4个参数按顺序分别是左上右下
		mBluetoothName.setLayoutParams(layoutParams);
		if (!isTrue) {
			mContectText.setVisibility(View.GONE);
		}
		Log.d(TAG, "隐藏联系人");
	}

	private void onFullScreen() {
		params.setMargins(150, 0, 0, 0);// 4个参数按顺序分别是左上右下
		mBluetoothStatus.setLayoutParams(params);
		leftParams.setMargins(100, 20, 0, 0);
		mLeftMenu.setLayoutParams(leftParams);
	}

	@Override
	protected void onActivityMove(boolean isToLeft) {
		if (isToLeft && BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
			onLeftLayout();
			Log.d(TAG, "从副屏切换到主屏显示更新联系人");
		}
		super.onActivityMove(isToLeft);
	}

	@Override
	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
			int oldBottom) {
		// TODO Auto-generated method stub
		if (left == 0 && right == 0 && top == 0 && bottom == 0) {
			return;
		}
		if (left == oldLeft && right == oldRight) {
			return;
		}
		int width = right - left;
		Log.d(TAG, "mRightMode" + width);
		if (width == 540) {
			Log.d(TAG, "mRightMode");
			isOrso = true;
			mFragmetContext.setVisibility(View.GONE);
			mAddLayout.setVisibility(View.VISIBLE);
			mLeftMenu.setVisibility(View.GONE);
			addRightBluetoothMusic();
			onRightLayout(false);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 1);
			params.setMargins(0, 0, 0, 0);// 4个参数按顺序分别是左上右下
			mBluetoothStatus.setLayoutParams(params);
		} else if (width == 600) {
			Log.d(TAG, "mLeftMode");
			isOrso = false;
			// 半屏
			setScreen();
			onLeftLayout();
		} else if (width == 1000) {
			isOrso = false;
			setFullScreen();
			onLeftLayout();
		} else if (width == 1280) {
			// 满屏
			addRightBluetoothMusic();
			isOrso = false;
			setFullScreen();
			onRightLayout(true);
			onFullScreen();
		}

	}

	public void addRightBluetoothMusic() {
		if (mMusicRightFragment == null) {
			mMusicRightFragment = new MusicFragment(true);
			mAddFragment.replace(R.id.add_bluetooth_music, mMusicRightFragment);
			mAddFragment.commitAllowingStateLoss();
		}
		if (mMusicRightFragment != null) {
			mMusicRightFragment.openAudioMode();
		}
	}

}