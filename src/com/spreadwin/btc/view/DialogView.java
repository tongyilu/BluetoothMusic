package com.spreadwin.btc.view;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.SyncService;
import com.spreadwin.btc.utils.OpenUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogView implements OnClickListener {

	private View mView;
	private Context mContext;
	public static final String TAG = "DialogView";
	public static final boolean DEBUG = true;

	public boolean isMuteState;
	public boolean isHfState;

	private TextView mMute, mHf;
	private ImageView mDialButton;
	private ImageView mdroppedbutton;
	private TextView mNumberText, mNameText;
	public static final String ACTION_BT_CALL_IN = "ACTION_BT_CALL_IN";
	public static final String EXTRA_BT_CALL_IN_NAME = "EXTRA_BT_CALL_IN_NAME";
	public static final String EXTRA_BT_CALL_IN_NUMBER = "EXTRA_BT_CALL_IN_NUMBER";

	private AnimationDrawable animDown = new AnimationDrawable();
	private DilatingDotsProgressBar mDilatingDotsProgressBar;
	private ImageView imgGameWord;
	private OpenUtils openUtils;
	private boolean isStart;

	public DialogView(Context context, boolean isFlags) {
		this.mContext = context;
		this.isStart = isFlags;
		mView = LayoutInflater.from(context).inflate(R.layout.display_call, null);
		initView(mView);
	}

	private void initView(View view) {
		openUtils = new OpenUtils(mContext);
		mDilatingDotsProgressBar = (DilatingDotsProgressBar) view.findViewById(R.id.progress);
		mDialButton = (ImageView) view.findViewById(R.id.mdial_button);
		mdroppedbutton = (ImageView) view.findViewById(R.id.mdropped_button);
		mNumberText = (TextView) view.findViewById(R.id.number_text);
		mNameText = (TextView) view.findViewById(R.id.name_text);
		mHf = (TextView) view.findViewById(R.id.hf);
		mMute = (TextView) view.findViewById(R.id.mute);
		String getCallNumber = BtcNative.getCallNumber();
		String getPhoneName = getCallName(getCallNumber);
		if (!TextUtils.isEmpty(getPhoneName)) {
			mNameText.setVisibility(View.VISIBLE);
			mNameText.setText(getPhoneName);
		} else {
			mNameText.setVisibility(View.GONE);
		}
		mNumberText.setText(getCallNumber);
		mMute.setOnClickListener(this);
		mHf.setOnClickListener(this);
		mDialButton.setOnClickListener(this);
		mdroppedbutton.setOnClickListener(this);
		imgGameWord = (ImageView) view.findViewById(R.id.icon);
		imgGameWord.setBackgroundResource(R.anim.call_anim);
		animDown = (AnimationDrawable) imgGameWord.getBackground();
		animDown.start();
		animDown.setOneShot(false);
		mDilatingDotsProgressBar.show();
		if (!isStart) {
			mMute.setVisibility(View.GONE);
			mDialButton.setVisibility(View.GONE);
		} else {
			mMute.setVisibility(View.VISIBLE);
			mDialButton.setVisibility(View.VISIBLE);
			Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
			mCallIntent.putExtra(EXTRA_BT_CALL_IN_NAME, getPhoneName);
			mCallIntent.putExtra(EXTRA_BT_CALL_IN_NUMBER, getCallNumber);
			mContext.sendBroadcast(mCallIntent);
		}
	}

	public View getVideoPlayView() {
		return mView;
	}

	// 获取来电时名字
	private String getCallName(String getCallNumber) {
		String mCallName = "";
		if (MainActivity.binder != null) {
			return MainActivity.binder.getCallName(getCallNumber);
		}
		return mCallName;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mdial_button:
			BtcNative.answerCall();
			break;
		case R.id.mdropped_button:
			denyCall();
			break;
		case R.id.mute:
			if (isMuteState) {
				openUtils.setRingerMode(false);
				setMuteImageView(true);
				isMuteState = false;
			} else {
				openUtils.setRingerMode(true);
				setMuteImageView(false);
				isMuteState = true;
			}
			break;
		case R.id.hf:
			if (isHfState) {
				isMuteState = false;
				setHfImage(false);
			} else {
				isMuteState = true;
				setHfImage(true);
			}
			break;
		}
	}

	public void setMuteImageView(boolean isState) {
		Drawable drawable = mContext.getResources().getDrawable(isState ? R.drawable.mute_u : R.drawable.mute_d);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		mMute.setCompoundDrawables(null, drawable, null, null);
	}

	public void setHfImage(boolean isState1) {
		Drawable drawable1 = mContext.getResources()
				.getDrawable(isState1 ? R.drawable.handsfree_u : R.drawable.handsfree_d);
		drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());
		mHf.setCompoundDrawables(null, drawable1, null, null);
	}

	private void denyCall() {
		if (!isStart) {
			BtcNative.hangupCall();
		} else {
			BtcNative.denyCall();
		}
		mDismissDialog();
	}

	private void mDismissDialog() {
		// TODO Auto-generated method stub
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		((SyncService)mContext).finishMainActivity();
		if (isStart) {
			Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
			mContext.sendBroadcast(mCallIntent);
			Log.d("ACTION_BT_CALL_IN", "发送了"+ACTION_BT_CALL_IN);
		}
		wm.removeView(mView);
	}

}
