package com.spreadwin.btc;

import com.spreadwin.btc.utils.BtcGlobalData;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class DialogFragment extends Fragment implements OnClickListener {
	public static final String TAG = "MainActivity";
	public static final boolean DEBUG = true;

	ImageButton mDialButton;
	ImageButton mdroppedbutton;
	TextView mNumberText;
	private View mRootView;
	public static final String ACTION_BT_CALL_IN = "ACTION_BT_CALL_IN";
	public static final String EXTRA_BT_CALL_IN_NAME = "EXTRA_BT_CALL_IN_NAME";
	public static final String EXTRA_BT_CALL_IN_NUMBER = "EXTRA_BT_CALL_IN_NUMBER";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.display_call, container, false);
		mDialButton = (ImageButton)mRootView.findViewById(R.id.mdial_button);
		mdroppedbutton = (ImageButton) mRootView.findViewById(R.id.mdropped_button);
		mNumberText = (TextView) mRootView.findViewById(R.id.number_text);
		String getPhoneName = BtcNative.getPhoneName();
		String getCallNumber = BtcNative.getCallNumber();
		mNumberText.setText(getPhoneName + getCallNumber);
		mDialButton.setOnClickListener(this);
		mdroppedbutton.setOnClickListener(this);
		Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
		mCallIntent.putExtra(EXTRA_BT_CALL_IN_NAME, getPhoneName);
		mCallIntent.putExtra(EXTRA_BT_CALL_IN_NUMBER, getCallNumber);
		getActivity().sendBroadcast(mCallIntent);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		if (v == mDialButton) {
			mLog("onClick mDialButton11111111111");
			answerCall();
		} else if (v == mdroppedbutton) {
			mLog("onClick mdroppedbutton22222222222");
			denyCall();
		}
	}

	private void answerCall() {
		BtcNative.answerCall();
		Intent mCallIntent = new Intent();
		mCallIntent.setAction(MainActivity.mActionCall);
		mCallIntent.putExtra("call_status", BtcGlobalData.IN_CALL);
		mCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(mCallIntent);
		mDismissDialog();
	}

	private void denyCall() {
		BtcNative.denyCall();
		mDismissDialog();
	}

	void mDismissDialog() {
		// Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
		// sendBroadcast(mCallIntent);
		getActivity().finish();
//		System.exit(-1);
	}

	public static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);
		}
	}
}
