package com.spreadwin.btc.view;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.utils.BtcGlobalData;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogView implements OnCheckedChangeListener, OnClickListener {

	private View mView;
	private Context mContext;
	public static final String TAG = "MainActivity";
	public static final boolean DEBUG = true;

	ImageView mDialButton;
	ImageView mdroppedbutton;
	TextView mNumberText, mNameText;
	public static final String ACTION_BT_CALL_IN = "ACTION_BT_CALL_IN";
	public static final String EXTRA_BT_CALL_IN_NAME = "EXTRA_BT_CALL_IN_NAME";
	public static final String EXTRA_BT_CALL_IN_NUMBER = "EXTRA_BT_CALL_IN_NUMBER";

	private AnimationDrawable animDown = new AnimationDrawable();
	private ImageView imgGameWord;

	public DialogView(Context context) {
		this.mContext = context;
		mView = LayoutInflater.from(context).inflate(R.layout.display_call, null);
		initView(mView);
	}

	private void initView(View view) {
		mDialButton = (ImageView) view.findViewById(R.id.mdial_button);
		mdroppedbutton = (ImageView) view.findViewById(R.id.mdropped_button);
		mNumberText = (TextView) view.findViewById(R.id.number_text);
		mNameText = (TextView) view.findViewById(R.id.name_text);
		String getPhoneName = BtcNative.getPhoneName();
		String getCallNumber = BtcNative.getCallNumber();
		mNameText.setText(getPhoneName);
		mNumberText.setText(getCallNumber);
		mDialButton.setOnClickListener(this);
		mdroppedbutton.setOnClickListener(this);
		Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
		mCallIntent.putExtra(EXTRA_BT_CALL_IN_NAME, getPhoneName);
		mCallIntent.putExtra(EXTRA_BT_CALL_IN_NUMBER, getCallNumber);
		mContext.sendBroadcast(mCallIntent);
		imgGameWord = (ImageView) view.findViewById(R.id.icon);
		imgGameWord.setBackgroundResource(R.anim.call_anim);
		animDown = (AnimationDrawable) imgGameWord.getBackground();
		animDown.start();
		animDown.setOneShot(false);// 设置是否只播放一遍
	}

	public View getVideoPlayView() {
		return mView;
	}

	@Override
	public void onClick(View v) {
		if (v == mDialButton) {
			answerCall();
		} else if (v == mdroppedbutton) {
			denyCall();
		}
	}

	private void answerCall() {
		BtcNative.answerCall();
		Intent mCallIntent = new Intent();
		mCallIntent.setAction(MainActivity.mActionCall);
		mCallIntent.putExtra("call_status", BtcGlobalData.IN_CALL);
		mCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(mCallIntent);
		mDismissDialog();
	}

	private void denyCall() {
		BtcNative.denyCall();
		mDismissDialog();
	}

	private void mDismissDialog() {
		// TODO Auto-generated method stub
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		wm.removeView(mView);
		Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
		mContext.sendBroadcast(mCallIntent);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

	}
}
