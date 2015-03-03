package com.spreadwin.btc;

import com.spreadwin.btc.R;
import com.spreadwin.btc.Bluetooth.BluetoothFragment;
import com.spreadwin.btc.Calllogs.CallLogsFragment;
import com.spreadwin.btc.Music.MusicFragment;
import com.spreadwin.btc.contacts.ContactsFragment;
import com.spreadwin.btc.utils.BtcGlobalData;
import com.spreadwin.btc.utils.ControlVolume;

import android.app.Activity;
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
import android.net.rtp.AudioStream;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnClickListener {
	public static final String TAG = "MainActivity";
	public static final boolean DEBUG = true;

	public static final String mActionSync = "com.spreadwin.btc.sync";
	public static final String mActionA2dp = "com.spreadwin.btc.a2dp";
	public static final String mActionBfp = "com.spreadwin.btc.bfp";
	public static final String mActionCall = "com.spreadwin.btc.call";
	public static final String mActionPair = "com.spreadwin.btc.pair";

	private final int DIALOG1 = 1;
	private final int DIALOG2 = 2;

	private final int mMessageCall = 0;
	private final int mMessageActionCall = 1;
	private final int mMessageShowDeviceName = 2;
	private final int mMessageHideVolume = 3;
	private final int mMessageShowBluetoothName = 4;

	public static int mShowDeviceNameDelayed = 3500;
	private final int mHideVolumeDelayed = 3000;
	private final int mShowNameDelayed = 1000;

	MusicFragment mMusicFragment;
	public static BluetoothFragment mBluetoothFragment;
	CallLogsFragment mCallLogsFragment;
	ContactsFragment mContactsFragment;
	LinearLayout mCallLogsLayout, mContactsLayout, mMusicLayout, mRedialLayout,
			mBluetoothLayout;
	private boolean binded;
	LocalBroadcastManager mLocalBroadcastManager;
	BroadcastReceiver mBroadcastReceiver;
	public static boolean mBroadcast = false;
	public static SyncService.SyncBinder binder;
	ImageButton mDialButton;
	ImageButton mdroppedbutton;
	TextView mNumberText;
	TextView mNameText;
	TextView mBluetoothStatus;
	TextView mBluetoothName;
	AudioManager audioManager;
	Dialog mCallDialog = null;

	// 自定义音量条
	private ControlVolume view_MyControlVolume;

	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;
	String callNumber;
	String callName;

	public Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case mMessageCall:
				mLog("handleMessage 1111callName ==" + callName
						+ "; callNumber ==" + callNumber);
				for (int i = 0; i < 3; i++) {
					if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
						if (callNumber != null) {
							mBluetoothFragment.dialCall(callNumber);
						} else if (callName != null
								&& binder != null
								&& binder.getPhoneBookInfo(
										BtcGlobalData.PB_PHONE).getTelNumber(
										callName) != null) {
							mBluetoothFragment.dialCall(binder
									.getPhoneBookInfo(BtcGlobalData.PB_PHONE)
									.getTelNumber(callName));
						} else if (callName != null
								&& binder != null
								&& binder
										.getPhoneBookInfo(BtcGlobalData.PB_SIM)
										.getTelNumber(callName) != null) {
							mBluetoothFragment.dialCall(binder
									.getPhoneBookInfo(BtcGlobalData.PB_SIM)
									.getTelNumber(callName));
						}
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
				int mStatus = getIntent().getIntExtra("call_status",
						BtcGlobalData.NO_CALL);
				mLog("handler mStatus ==" + mStatus);
				if (mStatus == BtcGlobalData.CALL_IN) {
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
				mLog("handleMessage BtcNative.getPairDeviceName(0) =="
						+ BtcNative.getPairDeviceName(0));
				if (BtcNative.getPairDeviceName(0).length() > 0) {
					mBluetoothStatus.setText(getResources().getString(
							R.string.connect_title)
							+ "--" + BtcNative.getPairDeviceName(0));
				} else {
					mBluetoothStatus.setText(getResources().getString(
							R.string.connect_title));
				}

				break;
			case mMessageHideVolume:
				view_MyControlVolume.setAnimation(AnimationUtils.loadAnimation(
						MainActivity.this, android.R.anim.fade_out));
				view_MyControlVolume.setVisibility(View.GONE);
				break;
			case mMessageShowBluetoothName:
				if (BtcNative.getDeviceName().length() > 0) {
					mBluetoothName.setText(getResources().getString(
							R.string.device_name)
							+ BtcNative.getDeviceName());
				} else {
					handler.sendEmptyMessageDelayed(mMessageShowBluetoothName,
							mShowNameDelayed);
				}
				break;
			default:
				break;
			}
		};
	};

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			binder = (SyncService.SyncBinder) service;
			binded = true;
			if (binder.getBfpStatuss() == BtcGlobalData.BFP_CONNECTED) {
				mBluetoothStatus.setText(getResources().getString(
						R.string.connect_title));
				handler.sendEmptyMessageDelayed(mMessageShowDeviceName,
						mShowDeviceNameDelayed);
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
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		init();
		mLog("MainActivity onCreate1111");
		// 读取状态
		try {
			mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = mPowerManager.newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (binder != null
				&& binder.getBfpStatuss() == BtcGlobalData.BFP_CONNECTED) {
			mBluetoothStatus.setText(getResources().getString(
					R.string.connect_title));
			handler.sendEmptyMessageDelayed(mMessageShowDeviceName,
					mShowDeviceNameDelayed);
		}

		String action = getIntent().getAction();
		mLog("MainActivity onResume action ==" + action);
		if (action != null) {
			if (action.equals("MYACTION.BTC.CALL")) {
				callNumber = getIntent().getStringExtra("call_number");
				callName = getIntent().getStringExtra("call_name");
				if (callNumber != null || callName != null) {
					FragmentManager fm = getFragmentManager();
					FragmentTransaction transaction = fm.beginTransaction();
					if (mBluetoothFragment == null) {
						mBluetoothFragment = new BluetoothFragment();
					}
					transaction.replace(R.id.id_fragment_content,
							mBluetoothFragment);
					transaction.commit();
					handler.sendEmptyMessage(mMessageCall);
				}
			} else if (action.equals(mActionCall)) {
				mLog("MainActivity onResume action222222222 ==" + mActionCall);
				handler.sendEmptyMessageDelayed(mMessageActionCall, 100);
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLog("MainActivity onStart 222222");
		LockScreen();
	}

	private void init() {
		mCallLogsLayout = (LinearLayout) findViewById(R.id.calllogs_layout);
		mContactsLayout = (LinearLayout) findViewById(R.id.contacts_layout);
		mMusicLayout = (LinearLayout) findViewById(R.id.music_layout);
		mRedialLayout = (LinearLayout) findViewById(R.id.redial_layout);
		mBluetoothLayout = (LinearLayout) findViewById(R.id.bluetooth_layout);
		mBluetoothStatus = (TextView) findViewById(R.id.contect_status);
		mBluetoothName = (TextView) findViewById(R.id.bluetoot_name);
		view_MyControlVolume = (ControlVolume) findViewById(R.id.view_MyControlVolume);
		mCallLogsLayout.setOnClickListener(this);
		mContactsLayout.setOnClickListener(this);
		mMusicLayout.setOnClickListener(this);
		mRedialLayout.setOnClickListener(this);
		mBluetoothLayout.setOnClickListener(this);

		mCallLogsFragment = new CallLogsFragment();
		mContactsFragment = new ContactsFragment();
		mBluetoothFragment = new BluetoothFragment();
		mMusicFragment = new MusicFragment();

		Intent intent = new Intent(this, SyncService.class);
		startService(intent);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		setDefaultFragment();
		registerReceiver();
		handler.sendEmptyMessageDelayed(mMessageShowBluetoothName,
				mShowNameDelayed);
	}

	private void registerReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				mLog("mBroadcastReceiver ==" + intent.getAction());
				if (intent.getAction().equals(mActionSync)) {
					int mStatus = intent.getIntExtra("sync_status",
							BtcGlobalData.NOT_SYNC);
					mLog("Receiver mActionSync mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.NEW_SYNC) {
						mCallLogsFragment.notifyDataSetChanged();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mContactsFragment.notifyDataSetChanged();
					} else if (mStatus == BtcGlobalData.IN_SYNC) {

					} else {

					}
				} else if (intent.getAction().equals(mActionA2dp)) {
					int mStatus = intent.getIntExtra("a2dp_status",
							BtcGlobalData.A2DP_DISCONNECT);
					mLog("Receiver mActionA2dp mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.A2DP_DISCONNECT) {
						mMusicFragment
								.setA2dpStatus(BtcGlobalData.A2DP_DISCONNECT);
					} else if (mStatus == BtcGlobalData.A2DP_DISCONNECT) {
						mMusicFragment
								.setA2dpStatus(BtcGlobalData.A2DP_DISCONNECT);
					} else if (mStatus == BtcGlobalData.A2DP_PLAYING) {
						mMusicFragment
								.setA2dpStatus(BtcGlobalData.A2DP_PLAYING);
					}
					mMusicFragment.checkA2dpStatus();
				} else if (intent.getAction().equals(mActionCall)) {
					int mStatus = intent.getIntExtra("call_status",
							BtcGlobalData.NO_CALL);
					mLog("Receiver mActionCall mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.CALL_IN) {
						mShowDialog(DIALOG1);
					} else if (mStatus == BtcGlobalData.CALL_OUT) {
						FragmentManager fm = getFragmentManager();
						FragmentTransaction transaction = fm.beginTransaction();
						if (mBluetoothFragment == null) {
							mBluetoothFragment = new BluetoothFragment();
						}
						transaction.replace(R.id.id_fragment_content,
								mBluetoothFragment);
						transaction.commit();
						mBluetoothFragment
								.setCallStatus(BtcGlobalData.CALL_OUT);
					} else if (mStatus == BtcGlobalData.IN_CALL) {
						mDismissDialog(DIALOG1);
						mBluetoothFragment.setCallStatus(BtcGlobalData.IN_CALL);
					} else {
						mDismissDialog(DIALOG1);
						mBluetoothFragment.setCallStatus(BtcGlobalData.NO_CALL);
					}
				} else if (intent.getAction().equals(mActionPair)) {
					int mStatus = intent.getIntExtra("pair_status",
							BtcGlobalData.NOT_PAIR);
					mLog("Receiver mActionPair mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.NOT_PAIR) {

					} else if (mStatus == BtcGlobalData.IN_PAIR) {

					} else if (mStatus == BtcGlobalData.PAIRRED) {
					}
				} else if (intent.getAction().equals(mActionBfp)) {
					int mStatus = intent.getIntExtra("bfp_status",
							BtcGlobalData.BFP_DISCONNECT);
					mLog("Receiver mActionBfp mStatus ==" + mStatus);
					if (mStatus == BtcGlobalData.BFP_CONNECTED) {
						mBluetoothStatus.setText(getResources().getString(
								R.string.connect_title));
						handler.sendEmptyMessageDelayed(mMessageShowDeviceName,
								mShowDeviceNameDelayed);
					} else if (mStatus == BtcGlobalData.BFP_DISCONNECT) {
						mBluetoothFragment.setCallStatus(BtcGlobalData.NO_CALL);
						mBluetoothStatus.setText(getResources().getString(
								R.string.disconnect_title));
					}
				}

			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(mActionSync);
		intentFilter.addAction(mActionA2dp);
		intentFilter.addAction(mActionCall);
		intentFilter.addAction(mActionPair);
		intentFilter.addAction(mActionBfp);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mBroadcast = true;
		mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,
				intentFilter);
	}

	protected void mDismissDialog(int dIALOG12) {
		if (mCallDialog != null && mCallDialog.isShowing()) {
			mCallDialog.dismiss();
		}
	}

	protected void mShowDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		if (id == DIALOG1) {
			View mCallView = inflater.inflate(R.layout.display_call, null);
			builder.setView(mCallView);
			mDialButton = (ImageButton) mCallView
					.findViewById(R.id.mdial_button);
			mdroppedbutton = (ImageButton) mCallView
					.findViewById(R.id.mdropped_button);
			mNumberText = (TextView) mCallView.findViewById(R.id.number_text);
			mNameText = (TextView) mCallView.findViewById(R.id.name_text);

			String getCallNumber = BtcNative.getCallNumber();
			String getPhoneName = getCallName(getCallNumber);
			mLog("onCreateDialog 1111111111");
			mNameText.setText(getPhoneName);
			mNumberText.setText(getCallNumber);
			mDialButton.setOnClickListener(this);
			mdroppedbutton.setOnClickListener(this);
			if (mCallDialog != null) {
				mCallDialog.dismiss();
				mCallDialog = null;
			}

			mCallDialog = builder.create();
			mCallDialog.setCanceledOnTouchOutside(false);
			mCallDialog.show();

			// Intent mDialogIntent = new Intent(this,DialogActivity.class);
			// mDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// startActivity(mDialogIntent);

		} else if (id == DIALOG2) {
			builder.setTitle("提示");
			builder.setMessage("确定断开连接吗");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mBluetoothFragment
									.setCallStatus(BtcGlobalData.NO_CALL);
							BtcNative.disconnectPhone();
						}
					});
			builder.setNegativeButton("取消", null);
			builder.create().show();

		}

	}

	private String getCallName(String getCallNumber) {
		String mCallName = "";
		if (binder != null) {
			if (binder.getPhoneBookInfo(BtcGlobalData.PB_PHONE).getSize() > 0) {
				mCallName = binder.getPhoneBookInfo(BtcGlobalData.PB_PHONE)
						.getCalllName(getCallNumber);
				return mCallName;
			}
			if (binder.getPhoneBookInfo(BtcGlobalData.PB_SIM).getSize() > 0) {
				mCallName = binder.getPhoneBookInfo(BtcGlobalData.PB_SIM)
						.getCalllName(getCallNumber);
				return mCallName;
			}
		}
		return mCallName;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (binded) {
			unbindService(conn);
		}
		if (mLocalBroadcastManager != null && mBroadcastReceiver != null) {
			mBroadcast = false;
			mLog("onDestroy() 1111111111111 ");
			mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		UnLockScreen();
	}

	private void setDefaultFragment() {
		String action = getIntent().getAction();
		if (action != null && action.equals(mActionCall)) {
			FragmentManager fm = getFragmentManager();
			FragmentTransaction transaction = fm.beginTransaction();
			mMusicFragment = new MusicFragment();
			transaction.replace(R.id.id_fragment_content, mBluetoothFragment);
			transaction.commit();
		} else {
			FragmentManager fm = getFragmentManager();
			FragmentTransaction transaction = fm.beginTransaction();
			mMusicFragment = new MusicFragment();
			transaction.replace(R.id.id_fragment_content, mMusicFragment);
			transaction.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			Toast.makeText(this, "Menu Item refresh selected",
					Toast.LENGTH_SHORT).show();
			// Log.d(TAG, "pair "+ BtcNative.getPairStatus());
			break;
		case R.id.action_about:
			Toast
					.makeText(this, "Menu Item about selected",
							Toast.LENGTH_SHORT).show();
			// Log.d(TAG, "bfp "+ BtcNative.getBfpStatus());
			break;
		case R.id.action_edit:
			Toast.makeText(this, "Menu Item edit selected", Toast.LENGTH_SHORT)
					.show();
			// Log.d(TAG, "call "+ BtcNative.getCallStatus());
			break;
		case R.id.action_search:
			Toast.makeText(this, "Menu Item search selected",
					Toast.LENGTH_SHORT).show();
			// Log.d(TAG, "a2dp "+ BtcNative.getA2dpStatus());
			break;
		case R.id.action_help:
			Toast.makeText(this, "Menu Item  settings selected",
					Toast.LENGTH_SHORT).show();
			// Log.d(TAG, "a2dp "+ BtcNative.getA2dpStatus());
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v == mBluetoothLayout) {
			mDisconnectPhone();
		} else if (v == mDialButton) {
			mLog("onClick mDialButton11111111111");
			answerCall();
		} else if (v == mdroppedbutton) {
			mLog("onClick mdroppedbutton22222222222");
			denyCall();
		} else {
			FragmentManager fm = getFragmentManager();
			FragmentTransaction transaction = fm.beginTransaction();

			switch (v.getId()) {
			case R.id.calllogs_layout:
				if (mCallLogsFragment == null) {
					mCallLogsFragment = new CallLogsFragment();
				}
				transaction
						.replace(R.id.id_fragment_content, mCallLogsFragment);
				break;
			case R.id.contacts_layout:
				if (mContactsFragment == null) {
					mContactsFragment = new ContactsFragment();
				}
				transaction
						.replace(R.id.id_fragment_content, mContactsFragment);
				break;
			case R.id.redial_layout:
				if (mBluetoothFragment == null) {
					mBluetoothFragment = new BluetoothFragment();
				}
				transaction.replace(R.id.id_fragment_content,
						mBluetoothFragment);
				break;
			case R.id.music_layout:
				if (mMusicFragment == null) {
					mMusicFragment = new MusicFragment();
				}
				transaction.replace(R.id.id_fragment_content, mMusicFragment);
				break;
			}
			// transaction.addToBackStack();
			// 事务提交
			transaction.commit();
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
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * true 为加一级蓝牙声音，false为减一级蓝牙声音，范围0-16
	 */
	private void setBTVolume(boolean toggle) {
		int mCurV = BtcNative.getVolume();
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
			view_MyControlVolume.setAnimation(AnimationUtils.loadAnimation(
					this, android.R.anim.fade_in));
			view_MyControlVolume.setVisibility(View.VISIBLE);
			handler.sendEmptyMessageDelayed(mMessageHideVolume,
					mHideVolumeDelayed);
		} else {
			handler.removeMessages(3);
			handler.sendEmptyMessageDelayed(mMessageHideVolume,
					mHideVolumeDelayed);
		}
	}

	private void mDisconnectPhone() {
		if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
			mShowDialog(DIALOG2);
		}

	}

	private void denyCall() {
		BtcNative.denyCall();
		mDismissDialog(DIALOG1);
	}

	private void answerCall() {
		BtcNative.answerCall();
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		if (mBluetoothFragment == null) {
			mBluetoothFragment = new BluetoothFragment();
		}
		transaction.replace(R.id.id_fragment_content, mBluetoothFragment);
		transaction.commit();
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

	public static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}
}