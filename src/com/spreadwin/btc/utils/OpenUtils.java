package com.spreadwin.btc.utils;

import android.content.Context;
import android.media.AudioManager;

public class OpenUtils {
	private AudioManager mAudioManager;
	private Context mcontext;

	public OpenUtils(Context context) {
		this.mcontext = context;
		mAudioManager = (AudioManager) mcontext.getSystemService(Context.AUDIO_SERVICE);
	}

	public void setRingerMode(boolean mode) {
		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mode);
	}
}
