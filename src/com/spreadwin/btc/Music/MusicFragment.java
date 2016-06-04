package com.spreadwin.btc.Music;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.utils.BtcGlobalData;
import com.spreadwin.btc.view.AlwaysMarqueeTextView;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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

	// public int mCurStatus = BtcGlobalData.A2DP_DISCONNECT;
	private int mCallStatus = 0;
	public static final int CHECK_A2DP_STATUS = 1;

	ImageButton mMusicPrevious, mMusicPlay, mMusicNext;
	private LayoutInflater mInflater;
	private ViewGroup mContentContainer;
	private View mRootView;
	private AlwaysMarqueeTextView mPlayTitle, mPlayArtist, mPlayAlbum;

	private boolean mRight = false;// true:为右边
	private int mPlayer = 0;

	public MusicFragment() {
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
		mLog("MusicFragment openAudioMode BfpStatus ==" + BtcNative.getBfpStatus());
		if (BtcNative.getBfpStatus() == BtcGlobalData.BFP_CONNECTED) {
			BtAudioManager.getInstance(getActivity()).mAudioFocusGain = false;
			BtAudioManager.getInstance(getActivity()).onBtAudioFocusChange(true);
			BtAudioManager.getInstance(getActivity()).setAudioMode(BtAudioManager.AUDIO_MODE_BT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mRootView = inflater.inflate(R.layout.fragment_music, container, false);
		mMusicPrevious = (ImageButton) mRootView.findViewById(R.id.music_previous);
		mMusicPlay = (ImageButton) mRootView.findViewById(R.id.music_play);
		mMusicNext = (ImageButton) mRootView.findViewById(R.id.music_next);
		mPlayTitle = (AlwaysMarqueeTextView) mRootView.findViewById(R.id.play_title);
		mPlayArtist = (AlwaysMarqueeTextView) mRootView.findViewById(R.id.play_artist);
		mPlayAlbum = (AlwaysMarqueeTextView) mRootView.findViewById(R.id.play_album);
		mMusicPrevious.setOnClickListener(this);
		mMusicPlay.setOnClickListener(this);
		mMusicNext.setOnClickListener(this);
		// handler.sendEmptyMessageDelayed(CHECK_A2DP_STATUS, 1000);
		checkA2dpStatus();
		return mRootView;
	}

//	private void setPlayInfo() {
//		if (!TextUtils.isEmpty(BtcNative.getPlayTitle())) {
//			mPlayTitle.setVisibility(View.VISIBLE);
//			mPlayTitle.setText("歌曲：" + BtcNative.getPlayTitle());
//		} else {
//			mPlayTitle.setVisibility(View.GONE);
//		}
//		if (!TextUtils.isEmpty(BtcNative.getPlayArtist())) {
//			mPlayArtist.setVisibility(View.VISIBLE);
//			mPlayArtist.setText("歌手：" + BtcNative.getPlayArtist());
//		} else {
//			mPlayArtist.setVisibility(View.GONE);
//		}
//		if (!TextUtils.isEmpty(BtcNative.getPlayAlbum())) {
//			mPlayAlbum.setVisibility(View.VISIBLE);
//			mPlayAlbum.setText("专辑：" + BtcNative.getPlayAlbum());
//		} else {
//			mPlayAlbum.setVisibility(View.GONE);
//		}
//	}

	@Override
	public void onResume() {
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
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		mPlayTitle.setText("歌曲：" + BtcNative.getPlayTitle());
//		mPlayArtist.setText("歌手：" + BtcNative.getPlayArtist());
//		mPlayAlbum.setText("专辑：" + BtcNative.getPlayAlbum());
	}

	public void setA2dpStatus(int status) {
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
			mLog("onClick pauseMusic");
			BtcNative.pauseMusic();
			mPlayer = 1;
		} else if (mPlayer == 1 || mPlayer == 0) {
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

}
