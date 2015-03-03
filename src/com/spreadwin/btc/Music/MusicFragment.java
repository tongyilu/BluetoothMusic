package com.spreadwin.btc.Music;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.R.layout;
import com.spreadwin.btc.utils.BtcGlobalData;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MusicFragment extends Fragment implements OnClickListener{
		public  static final String TAG = MainActivity.TAG;
		public  static final boolean DEBUG = true;
		public  int A2DP_DISCONNECT = BtcGlobalData.A2DP_DISCONNECT;
	    public  int A2DP_CONNECTED = BtcGlobalData.A2DP_CONNECTED;
	    public  int A2DP_PLAYING = BtcGlobalData.A2DP_PLAYING;
	    
//	    public  int mCurStatus = BtcGlobalData.A2DP_DISCONNECT;
	    
	    public static final int CHECK_A2DP_STATUS = 1;
	    
		ImageButton mMusicPrevious,mMusicPlay,mMusicNext;
		TextView mA2dpText;
		private LayoutInflater mInflater;
		private ViewGroup mContentContainer;
		private View mRootView;
		@Override
		public void onCreate(Bundle savedInstanceState) {		
			super.onCreate(savedInstanceState);
			mLog("MusicFragment onCreate");
		}
		
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mInflater = inflater;
			mRootView = inflater.inflate(R.layout.fragment_music, container, false);
			mMusicPrevious = (ImageButton) mRootView.findViewById(R.id.music_previous);
			mMusicPlay = (ImageButton) mRootView.findViewById(R.id.music_play);
			mMusicNext = (ImageButton) mRootView.findViewById(R.id.music_next);
			mA2dpText = (TextView) mRootView.findViewById(R.id.a2dp_text);
			mMusicPrevious.setOnClickListener(this);
			mMusicPlay.setOnClickListener(this);
			mMusicNext.setOnClickListener(this);			
//			handler.sendEmptyMessageDelayed(CHECK_A2DP_STATUS, 1000);
			checkA2dpStatus();
			return mRootView;
		}
		
		@Override
		public void onResume() {		
			super.onResume();
		}
		
		Handler handler = new Handler(){
        	@Override
        	public void handleMessage(Message msg) {        		
        		switch (msg.what) {
        			case CHECK_A2DP_STATUS :
        				mLog("CHECK_A2DP_STATUS111111111");
        				checkA2dpStatus();
					break;
        		}
        	}    	
		};
		
		
		public void checkA2dpStatus() {	
			mLog("checkA2dpStatus MainActivity.binder.getA2dpStatus() =="+BtcNative.getA2dpStatus());
			if (BtcNative.getA2dpStatus() == A2DP_PLAYING) {
				setA2dpStatus(A2DP_PLAYING);
			}else if (BtcNative.getA2dpStatus() == A2DP_CONNECTED) {
				setA2dpStatus(A2DP_CONNECTED);				
			}else if (BtcNative.getA2dpStatus() == A2DP_DISCONNECT){
				setA2dpStatus(A2DP_DISCONNECT);
			}			
		}

		public void setA2dpStatus(int status) {
			if (mMusicPlay == null && mA2dpText == null && getActivity() == null) {
				return;
			}
			if (status == A2DP_PLAYING) {
				mMusicPlay.setBackgroundResource(R.drawable.music_button_pause);				
				mA2dpText.setText("");
			}else if (status == A2DP_CONNECTED) {
				mMusicPlay.setBackgroundResource(R.drawable.music_play);
				mA2dpText.setText("");
			}else if (status == A2DP_DISCONNECT){
				try {
					mMusicPlay.setBackgroundResource(R.drawable.music_play);	
					mA2dpText.setText(getActivity().getResources().getString(R.string.music_not_play));					
				} catch (Exception e) {
					mLog("e =="+e);
				}
			}
		}

		@Override
		public void onClick(View v) {
			mLog("onClick 111111111111");
			if (v == mMusicPrevious) {
				mLog("onClick mMusicPrevious");
				mMusicPrevious();
			}else if (v == mMusicPlay) {
				mLog("onClick mMusicPlay");	
				mMusicPlay();
			}else if (v == mMusicNext) {
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
			mLog("onClick BtcNative.getA2dpStatus() =="+BtcNative.getA2dpStatus());	
			if (BtcNative.getA2dpStatus() == A2DP_PLAYING) {
				mLog("onClick pauseMusic");	
				BtcNative.pauseMusic();	
				mMusicPlay.setBackgroundResource(R.drawable.music_play);				
			}else if(BtcNative.getA2dpStatus() == A2DP_CONNECTED) {
				mLog("onClick playMusic");	
				BtcNative.playMusic();
				mMusicPlay.setBackgroundResource(R.drawable.music_button_pause);				
			}		
		}

		public static  void mLog(String string) {
			if (DEBUG) {
				Log.d(TAG, string);				
			}
		}

}
