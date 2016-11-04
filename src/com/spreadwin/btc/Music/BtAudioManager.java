package com.spreadwin.btc.Music;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.rtp.AudioStream;
import android.util.Log;

import com.spreadwin.btc.BtcNative;

/**
 * 蓝牙声音管理
 * 
 * @author Tong Yilu
 *
 */
public class BtAudioManager {
	public static final String TAG = "BtAudioManager";

	public static final boolean DEBUG = true;

	private boolean mBtAudioToggle = false;

	private Context mContext;

	public static BtAudioManager mBtAudioManager;
	private AudioManager audioManager;

	public boolean mAudioCall = false;// 通话状态
	private boolean mAudioFocus = false;// audio焦点状态
	private boolean mAudioMute = false;// 静音状态
	
	/**
	 * 临时焦点正常模式
	 */
	private static final int mTempFocusNormal = 1;//临时焦点正常模式
	/**
	 * 临时焦点获取模式
	 */
	public static final int mTempFocusGain = 2;//临时焦点获取模式
	/**
	 * 临时焦点丢失模式
	 */
	public static final int mTempFocusLoss = 3;//临时焦点丢失模式
	public int mTempAudioFocus = mTempFocusNormal;//临时audio焦点

	public boolean mAudioFocusGain = false;

	private int VolumeNormal = 13;
	private int VolumeMute = 0;

	public static int AUDIO_MODE_NORMAL = AudioStream.MODE_NORMAL;// 正常模式
	public static int AUDIO_MODE_BT = 6; // 蓝牙的通路模式
	public static int AUDIO_MODE_CALL = 7;// 蓝牙通话的音频通路
	
	public static int mMode,mLastMode;//当前通路,上一次通路

	public final String ACTION_SCREENSAVER_CLOSE = "ACTION_SCREENSAVER_CLOSE";

	public static BtAudioManager getInstance(Context context) {
		if (mBtAudioManager == null) {
			mLog("BtAudioManager getInstance ==" + context);
			mBtAudioManager = new BtAudioManager(context);
		}
		return mBtAudioManager;
	}

	public BtAudioManager(Context context) {
		mLog("BtAudioManager context ==" + context);
		this.mContext = context;
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * BtAudio是否有效
	 * 
	 * @param enable
	 */
	private void setBtAudioEnable(boolean enable) {
		mLog("setBtAudioEnable mAudioFocusGain ==" + mAudioFocusGain + "; enable==" + enable
					+"; setTempBtAudioFocus =="+mTempAudioFocus);
		if (mAudioFocusGain == enable && mTempAudioFocus == mTempFocusNormal) {
			return;
		}
		if (enable) {
			mAudioFocusGain = true;
			mLog(mAudioFocus+"");
			if (mAudioFocus) {
				audioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			}else{
				audioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			}
			mLog("setBtAudioToggle requestAudioFocus");
//			audioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
//					AudioManager.AUDIOFOCUS_GAIN);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			mAudioFocusGain = false;
		}
	}

	/**
	 * 设置蓝牙声音的开关
	 * 
	 * @param toggle
	 */
	private void setBtAudioToggle(boolean toggle) {
		mLog("setBtAudioToggle mBtAudioToggle==" + mBtAudioToggle + "; toggle ==" + toggle);
		mBtAudioToggle = toggle;
		setBtAudioEnable(toggle);

		if (toggle) {
			mLog("setBtAudioToggle mAudioCall==" + mAudioCall);
			if (mAudioCall) {
				setAudioMode(AUDIO_MODE_CALL);
			} else {
				setAudioMode(AUDIO_MODE_BT);
			}
			BtcNative.setVolume(VolumeNormal);
		} else {
			setAudioMode(AUDIO_MODE_NORMAL);
			BtcNative.setVolume(VolumeMute);
		}
	}

	/**
	 * 设置mediokill掉后重新设置
	 */
	public void setMediaKillMode(){
		audioManager.setParameters("cdr_mode=" + AUDIO_MODE_NORMAL);
		audioManager.setParameters("cdr_mode=" + mMode);
	}
	
	
	/**
	 * 设置音频通路
	 * 
	 * @param mode
	 * @throws 
	 */
	private void setAudioMode(int mode) {
		mLog("setAudioMode mode ==" + mode+"; mTempAudioFocus =="+mTempAudioFocus);
		 if (mode == AudioStream.MODE_NORMAL && mTempAudioFocus != mTempFocusGain) {
			 mTempAudioFocus = mTempFocusNormal;
			 audioManager.abandonAudioFocus(mAudioFocusListener);
		 }
		audioManager.setParameters("cdr_mode=" + mode);
		
		mLastMode = mMode;
		mMode = mode;
	}

	/**
	 * 通话状态改变
	 * 
	 * @param status
	 */
	public void onCallChange(boolean status) {
		mLog("onCallChange status ==" + status);
		mAudioCall = status;
		setBtAudioChange();
		if (status)
			sendBroadcast(ACTION_SCREENSAVER_CLOSE);
	}

	private void sendBroadcast(String action) {
		Intent intent = new Intent(action);
		mContext.sendBroadcast(intent);
	}

	/**
	 * 音频静音状态变化
	 * 
	 * @param status
	 */
	public void onAudioMuteChange(boolean status) {
		mLog("onAudioMuteChange status ==" + status);
		mAudioMute = status;
		setBtAudioChange();
	}

	/**
	 * 音频焦点状态变化
	 * 
	 * @param status
	 */
	public void onBtAudioFocusChange(boolean status) {
		mLog("onBtAudioFocusChange status ==" + status);
		mAudioFocus = status;
		setBtAudioChange();
	}
	
	/**
	 * 获取音频焦点状态
	 * @return
	 */	
	public boolean isBtAudioFocus() {
		return mAudioFocus;
	}
	
	/**
	 * 判断临时焦点是否为获取模式
	 * @return
	 */	
	public boolean isTempBtAudioFocusGain() {
		return mTempAudioFocus == mTempFocusGain;
	}
	
	/**
	 * 设置临时音频焦点，为true时，要主动释放
	 * @param status
	 */
	public void setTempBtAudioFocus(int status) {
		mLog("setTempBtAudioFocus status ==" + status);
		mTempAudioFocus	= status;
	}

	/**
	 * 蓝牙音频状态变化
	 */
	private void setBtAudioChange() {
		mLog("setBtAudioChange mCall ==" + mAudioCall + "; mAudioMute ==" + mAudioMute + "; mAudioFocus =="
				+ mAudioFocus);
		boolean mToggle = false;
		if (mAudioCall && mAudioMute && mAudioFocus) {
			mToggle = true;
		} else if (!mAudioCall && mAudioMute && mAudioFocus) {
			mToggle = false;
		} else if (mAudioCall && !mAudioMute && mAudioFocus) {
			mToggle = true;
		} else if (mAudioCall && mAudioMute && !mAudioFocus) {
			mToggle = true;
		} else if (!mAudioCall && !mAudioMute && mAudioFocus) {
			mToggle = true;
		} else if (!mAudioCall && mAudioMute && !mAudioFocus) {
			mToggle = false;
		} else if (mAudioCall && !mAudioMute && !mAudioFocus) {
			mToggle = true;
		} else if (!mAudioCall && !mAudioMute && !mAudioFocus) {
			mToggle = false;
		}
		setBtAudioToggle(mToggle);
	}

	private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS:
				mLog("mAudioFocusListener AUDIOFOCUS_LOSS");
				onBtAudioFocusChange(false);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				mLog("mAudioFocusListener AUDIOFOCUS_LOSS_TRANSIENT focusChange ==" + focusChange);
				BtcNative.setVolume(VolumeMute);
//				onBtAudioFocusChange(false);
				break;
			case AudioManager.AUDIOFOCUS_GAIN:
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
				mLog("mAudioFocusListener AUDIOFOCUS_GAIN mAudioCall ==" + mAudioCall);
//				if (!mAudioCall)
//					onBtAudioFocusChange(true);
				break;
			}
		}
	};

	private static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}

}
