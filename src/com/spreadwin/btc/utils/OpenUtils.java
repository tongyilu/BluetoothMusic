package com.spreadwin.btc.utils;

import android.content.Context;
import android.media.AudioManager;

public class OpenUtils {
	private AudioManager mAudioManager;
	private Context context;

	public OpenUtils(){
		
	}
	
	public OpenUtils(Context context) {
		this.context = context;
	}

	/**
	 * 设置静音或正常 AudioManager.RINGER_MODE_SILENT静音0
	 * AudioManager.RINGER_MODE_VIBRATE 静音,但有振动1
	 * AudioManager.RINGER_MODE_NORMAL正常声音,振动开关由setVibrateSetting决定2
	 */
	public void setRingerMode(boolean mode) {
		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mode);
	}
}
