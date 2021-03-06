package com.spreadwin.btc.Music;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.SyncService;
import com.spreadwin.btc.utils.BtcGlobalData;
import com.spreadwin.btc.utils.PreferencesUtils;
import com.spreadwin.btc.view.AlwaysMarqueeTextView;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class MusicFragment extends Fragment implements OnClickListener {
	public static final String TAG = MainActivity.TAG;
	public static final boolean DEBUG = MainActivity.DEBUG;
	public int A2DP_DISCONNECT = BtcGlobalData.A2DP_DISCONNECT;
	public int A2DP_CONNECTED = BtcGlobalData.A2DP_CONNECTED;
	public int A2DP_PLAYING = BtcGlobalData.A2DP_PLAYING;
	public static final String mActionInfoBfp = "com.spreadwin.btc.bfp.info";

	private int mCallStatus = 0;
	public static final int CHECK_A2DP_STATUS = 1;

	ImageButton mMusicPrevious, mMusicPlay, mMusicNext;
	private View mRootView;
	private AlwaysMarqueeTextView mPlayTitle, mPlayArtist;

	private boolean mRight = false;// true:为右边
	private int mPlayer = 0;
	private static String PlayTitle = null;
	private static String PlayArtist = null;
	private static String PlayAlbum = null;
	private int state;

	private boolean isPlaySong = false;// 歌曲信息

	public MusicFragment() {
		this(false);
	}

	public MusicFragment(boolean isRight) {
		mRight = isRight;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLog("MusicFragment onCreate");
	}

	@Override
	public void onStart() {
		super.onStart();
		mPlayer = BtcNative.getA2dpStatus();
		mLog("MusicFragment onStart mCallStatus ==" + mCallStatus + "; mRight ==" + mRight);
		if (!mRight) {
			if (mCallStatus == BtcGlobalData.NO_CALL) {
				openAudioMode();
			} else {
				mCallStatus = BtcGlobalData.NO_CALL;
			}
		}
	}

	public void openAudioMode() {
		mLog("MusicFragment openAudioMode BfpStatus ==" + BtcNative.getBfpStatus() + "; mRight ==" + mRight);
		if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
			if (getActivity() != null) {
				BtAudioManager.getInstance(getActivity()).onBtAudioFocusChange(true);
			}
//			if (mPlayArtist != null && mPlayTitle != null) {
//				if (isPlaySong) {
//					mPlayTitle.setText(SyncService.mTitle == null ? "" : SyncService.mTitle);
//					mPlayArtist
//							.setText(SyncService.mArtist == null ? "" : SyncService.mArtist + " " + SyncService.mAlbum);
//				} else {
//					mPlayTitle.setText("");
//					mPlayArtist.setText("");
//				}
//			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_music, container, false);
		mMusicPrevious = (ImageButton) mRootView.findViewById(R.id.music_previous);
		mMusicPlay = (ImageButton) mRootView.findViewById(R.id.music_play);
		mMusicNext = (ImageButton) mRootView.findViewById(R.id.music_next);
		mPlayTitle = (AlwaysMarqueeTextView) mRootView.findViewById(R.id.play_title);
		mPlayArtist = (AlwaysMarqueeTextView) mRootView.findViewById(R.id.play_artist);
		mMusicPrevious.setOnClickListener(this);
		mMusicPlay.setOnClickListener(this);
		mMusicNext.setOnClickListener(this);
		// checkA2dpStatus();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(mActionInfoBfp);
		// getActivity().registerReceiver(mReceiver, intentFilter);

		mLog("onCreateView isPlaySong ==" + isPlaySong);
		if (isPlaySong) {
			onUpdateSongInfo();
		}
		return mRootView;
	}

	@Override
	public void onResume() {
		onUpdateSongInfo();
		super.onResume();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHECK_A2DP_STATUS:
				mLog("CHECK_A2DP_STATUS111111111");
				checkA2dpStatus();
				break;
			}
		}
	};

	public void checkA2dpStatus() {
		mLog("checkA2dpStatus MainActivity.binder.getA2dpStatus() ==" + BtcNative.getA2dpStatus());
		if (BtcNative.getA2dpStatus() == A2DP_PLAYING) {
			setA2dpStatus(A2DP_PLAYING);
		} else if (BtcNative.getA2dpStatus() == A2DP_CONNECTED) {
			setA2dpStatus(A2DP_CONNECTED);
		} else if (BtcNative.getA2dpStatus() == A2DP_DISCONNECT) {
			setA2dpStatus(A2DP_DISCONNECT);
		}
	}

	public void setA2dpStatus(int status) {
		state = status;
		if (mMusicPlay == null && getActivity() == null) {
			return;
		}
		if (status == A2DP_PLAYING) {
			mMusicPlay.setBackgroundResource(R.drawable.music_button_play);
		} else if (status == A2DP_CONNECTED) {
			mMusicPlay.setBackgroundResource(R.drawable.music_button_play);
		} else if (status == A2DP_DISCONNECT) {
			mMusicPlay.setBackgroundResource(R.drawable.music_button_play);
		}
	}

	@Override
	public void onClick(View v) {
		mLog("onClick 111111111111");
		openAudioMode();
		if (v == mMusicPrevious) {
			mLog("onClick mMusicPrevious");
			mMusicPrevious();
		} else if (v == mMusicPlay) {
			mLog("onClick mMusicPlay");
			mMusicPlay();
		} else if (v == mMusicNext) {
			mLog("onClick mMusicNext");
			mMusicNext();
		}
	}

	private void mMusicPrevious() {
		if (BtcNative.getA2dpStatus() == A2DP_PLAYING || BtcNative.getA2dpStatus() == A2DP_CONNECTED) {
			BtcNative.lastSong();
			checkA2dpStatus();
		}
	}

	private void mMusicNext() {
		if (BtcNative.getA2dpStatus() == A2DP_PLAYING || BtcNative.getA2dpStatus() == A2DP_CONNECTED) {
			BtcNative.nextSong();
			checkA2dpStatus();
		}
	}

	private void mMusicPlay() {
		mLog("onClick BtcNative.getA2dpStatus() ==" + BtcNative.getA2dpStatus());
		if (mPlayer == 2) {
			if (!(BtAudioManager.mLastMode == 0 && BtAudioManager.mMode == 6)) {
				mLog("onClick pauseMusic");
				BtcNative.pauseMusic();
				mPlayer = 1;
			} else {
				openAudioMode();
				mLog("change focus playMusic");
//				if (state != A2DP_PLAYING) {
//					BtcNative.playMusic();
//				}
			}
		} else if (mPlayer == 1 || mPlayer == 0) {
			openAudioMode();
			mLog("onClick playMusic");
			BtcNative.playMusic();
			mPlayer = 2;
		}
	}

	public static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}

	public void setCallStatus(int callStatus) {
		mCallStatus = callStatus;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void setPlayTitle(Intent intent) {
		isPlaySong = true;
		PlayTitle = intent.getStringExtra("mTitle");
		PlayArtist = intent.getStringExtra("mArtist");
		PlayAlbum = intent.getStringExtra("mAlbum");
		onUpdateSongInfo();
	}

	private void onUpdateSongInfo() {
		if (mPlayTitle == null) {
			return;
		}
		mPlayTitle.setVisibility(View.VISIBLE);
		mPlayArtist.setVisibility(View.VISIBLE);
		mPlayTitle.setText(PlayTitle == null ? "" : PlayTitle);
		mPlayArtist.setText(PlayArtist == null ? "" : PlayArtist + " " + PlayAlbum);
	}
}
