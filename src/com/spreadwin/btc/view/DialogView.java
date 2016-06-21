package com.spreadwin.btc.view;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.SyncService;
import com.spreadwin.btc.utils.MobileLocation;
import com.spreadwin.btc.utils.OpenUtils;
import com.spreadwin.btc.view.CallLineraLayout.DetachedFromWindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DialogView implements OnClickListener, OnLongClickListener {

	private View mView;
	private Context mContext;
	public static final String TAG = "DialogView";
	public static final boolean DEBUG = true;

	public boolean isMuteState;
	public boolean isHfState;

	private ImageView mDialButton, mMute, mSwitch, mCheckout;
	private ImageView mdroppedbutton;
	private Chronometer mChronometer;
	private TextView mAddressText, mNameText, mCallText;
	public static final String ACTION_BT_CALL_IN = "ACTION_BT_CALL_IN";
	public static final String EXTRA_BT_CALL_IN_NAME = "EXTRA_BT_CALL_IN_NAME";
	public static final String EXTRA_BT_CALL_IN_NUMBER = "EXTRA_BT_CALL_IN_NUMBER";

	private OpenUtils openUtils;
	private boolean isStart;
	private String getCallNumber;
	private String getPhoneName;
	private boolean isScreen;
	private boolean isCheckout;
	private LinearLayout mDial;
	private boolean isAnswer;

	public static final String FINISH_ACTIVITY = "FINISH_ACTIVITY";

	public static final String ANSWER_UP = "ANSWER_UP";

	ImageButton mDeleteButton, mNumberOne, mNumberTwo, mNumberThree, mNumberFour, mNumberFive, mNumberSix, mNumberSeven,
			mNumberEight, mNumberNine, mNumberZero, mNumberJin, mNumberXing;
	TextView mInputText, mIncallText, mIncallTime;

	HorizontalScrollView mHsview;
	RippleBackground rippleBackground;

	StringBuilder mDisplayNumber = new StringBuilder();
	private CallLineraLayout callLayout;
	private Gson msgGson;

	public DialogView(Context context, boolean isFlags, boolean isScreen) {
		this.mContext = context;
		this.isStart = isFlags;
		this.isScreen = isScreen;
		mView = LayoutInflater.from(context).inflate(R.layout.display_call, null);
		initView(mView);
	}

	private void initView(View view) {
		openUtils = new OpenUtils(mContext);
		mDial = (LinearLayout) view.findViewById(R.id.layout_dial);
		mDialButton = (ImageView) view.findViewById(R.id.mdial_button);
		mdroppedbutton = (ImageView) view.findViewById(R.id.mdropped_button);
		mCheckout = (ImageView) view.findViewById(R.id.checkout);
		mAddressText = (TextView) view.findViewById(R.id.address_text);
		mNameText = (TextView) view.findViewById(R.id.name_text);
		mCallText = (TextView) view.findViewById(R.id.call_text);
		mSwitch = (ImageView) view.findViewById(R.id.image_switch);
		mMute = (ImageView) view.findViewById(R.id.mute);
		mChronometer = (Chronometer) view.findViewById(R.id.chronometer);

		mDeleteButton = (ImageButton) view.findViewById(R.id.delete_button);
		mNumberOne = (ImageButton) view.findViewById(R.id.number_1);
		mNumberTwo = (ImageButton) view.findViewById(R.id.number_2);
		mNumberThree = (ImageButton) view.findViewById(R.id.number_3);
		mNumberFour = (ImageButton) view.findViewById(R.id.number_4);
		mNumberFive = (ImageButton) view.findViewById(R.id.number_5);
		mNumberSix = (ImageButton) view.findViewById(R.id.number_6);
		mNumberSeven = (ImageButton) view.findViewById(R.id.number_7);
		mNumberEight = (ImageButton) view.findViewById(R.id.number_8);
		mNumberNine = (ImageButton) view.findViewById(R.id.number_9);
		mNumberZero = (ImageButton) view.findViewById(R.id.number_0);
		mNumberJin = (ImageButton) view.findViewById(R.id.number_jin);
		mNumberXing = (ImageButton) view.findViewById(R.id.number_xing);
		mInputText = (TextView) view.findViewById(R.id.input_text);
		mIncallText = (TextView) view.findViewById(R.id.in_call_name);
		mIncallTime = (TextView) view.findViewById(R.id.in_call_timet);
		mHsview = (HorizontalScrollView) view.findViewById(R.id.scroollview);

		callLayout = (CallLineraLayout) view.findViewById(R.id.call_layout);
		rippleBackground = (RippleBackground)view.findViewById(R.id.content);

		
		rippleBackground.startRippleAnimation();
		getCallNumber = BtcNative.getCallNumber();
		getPhoneName = getCallName(getCallNumber);
		callUrlByGet(getCallNumber);

		if (TextUtils.isEmpty(getPhoneName)) {
			mNameText.setText(getCallNumber);
		} else {
			mNameText.setText(getPhoneName);
		}

		mMute.setOnClickListener(this);
		mSwitch.setOnClickListener(this);
		mDialButton.setOnClickListener(this);
		mdroppedbutton.setOnClickListener(this);
		mCheckout.setOnClickListener(this);

		mDeleteButton.setOnLongClickListener(this);
		mDeleteButton.setOnClickListener(this);
		mNumberOne.setOnClickListener(this);
		mNumberTwo.setOnClickListener(this);
		mNumberThree.setOnClickListener(this);
		mNumberFour.setOnClickListener(this);
		mNumberFive.setOnClickListener(this);
		mNumberSix.setOnClickListener(this);
		mNumberSeven.setOnClickListener(this);
		mNumberEight.setOnClickListener(this);
		mNumberNine.setOnClickListener(this);
		mNumberZero.setOnClickListener(this);
		mNumberJin.setOnClickListener(this);
		mNumberXing.setOnClickListener(this);

		if (!isStart) {
			mCallText.setText("正在呼叫...");
			mMute.setVisibility(View.GONE);
			mDialButton.setVisibility(View.GONE);
			mSwitch.setVisibility(View.GONE);
		} else {
			mCallText.setText("来电");
			mMute.setVisibility(View.VISIBLE);
			mDialButton.setVisibility(View.VISIBLE);
			mSwitch.setVisibility(View.GONE);
			Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
			mCallIntent.putExtra(EXTRA_BT_CALL_IN_NAME, getPhoneName);
			mCallIntent.putExtra(EXTRA_BT_CALL_IN_NUMBER, getCallNumber);
			Log.d(TAG, getPhoneName);
			Log.d(TAG, getCallNumber);
			mContext.sendBroadcast(mCallIntent);
		}
		setChckoutAudio();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(FINISH_ACTIVITY);
		intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		intentFilter.addAction(ANSWER_UP);
		mContext.registerReceiver(mReceiver, intentFilter);
		callLayout.init(mContext, view, new DetachedFromWindow() {

			@Override
			public void onDetachedFromWindow() {
				mContext.unregisterReceiver(mReceiver);
			}
		});
		
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == FINISH_ACTIVITY || action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
				mDismissDialog();
			} else if (action == ANSWER_UP) {
				setCaller();
			}
		}
	};

	public View getVideoPlayView() {
		return mView;
	}

	public void setCaller() {
		isAnswer = true;
		mDisplayNumber.delete(0, mDisplayNumber.length());
		mCheckout.setVisibility(View.VISIBLE);
		mSwitch.setVisibility(View.VISIBLE);
		mMute.setVisibility(View.VISIBLE);
		mAddressText.setVisibility(View.VISIBLE);
		mChronometer.setVisibility(View.VISIBLE);
		mDialButton.setVisibility(View.GONE);
		mAddressText.setText("通话中...");
		mCallText.setVisibility(View.GONE);
		mChronometer.setBase(SystemClock.elapsedRealtime());
		mChronometer.start();
		if (!isScreen && isAnswer) {
			Log.d(TAG, "显示");
			mCheckout.setVisibility(View.VISIBLE);
		} else {
			Log.d(TAG, "隐藏");
			mCheckout.setVisibility(View.GONE);
		}
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
		if (v == mDeleteButton) {
			removeNumber();
		} else if (v == mNumberOne) {
			addNumber("1");
		} else if (v == mNumberTwo) {
			addNumber("2");
		} else if (v == mNumberThree) {
			addNumber("3");
		} else if (v == mNumberFour) {
			addNumber("4");
		} else if (v == mNumberFive) {
			addNumber("5");
		} else if (v == mNumberSix) {
			addNumber("6");
		} else if (v == mNumberSeven) {
			addNumber("7");
		} else if (v == mNumberEight) {
			addNumber("8");
		} else if (v == mNumberNine) {
			addNumber("9");
		} else if (v == mNumberZero) {
			addNumber("0");
		} else if (v == mNumberJin) {
			addNumber("#");
		} else if (v == mNumberXing) {
			addNumber("*");
		}
		switch (v.getId()) {
		case R.id.mdial_button:
			BtcNative.answerCall();
			setCaller();
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
		case R.id.image_switch:
			setChckoutAudio();
			break;
		case R.id.checkout:
			if (isCheckout) {
				isCheckout = false;
				mCheckout.setImageResource(R.drawable.keyboard_d);
				mDial.setVisibility(View.VISIBLE);
			} else {
				isCheckout = true;
				mCheckout.setImageResource(R.drawable.keyboard_u);
				mDial.setVisibility(View.GONE);
			}
			break;
		}
	}
	
	public void setChckoutAudio(){
		BtcNative.changeAudioPath();
		if (BtcNative.getAudioPath() == 0) {
			Log.d("Dialog", "镜子接听===" + BtcNative.getAudioPath());
			mSwitch.setImageResource(R.drawable.switching_02);
		} else {
			Log.d("Dialog", "手机接听===" + BtcNative.getAudioPath());
			mSwitch.setImageResource(R.drawable.switching_01);
		}
	}

	private void removeNumber() {
		if (mDisplayNumber.length() > 1) {
			mDisplayNumber.deleteCharAt(mDisplayNumber.length() - 1);
			mInputText.setText(mDisplayNumber.toString());
		} else if (mDisplayNumber.length() == 1) {
			mDisplayNumber.deleteCharAt(mDisplayNumber.length() - 1);
		}
	}

	private void addNumber(String str) {
		mDisplayNumber.append(str);
		mInputText.setText(mDisplayNumber.toString());
		mInputText.setTextColor(mContext.getResources().getColor(R.color.white));
	}

	public void setMuteImageView(boolean isState) {
		mMute.setImageResource(isState ? R.drawable.mute_d : R.drawable.mute_u);
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
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		((SyncService) mContext).finishMainActivity();
		rippleBackground.stopRippleAnimation();
		if (isStart) {
//			Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
//			mContext.sendBroadcast(mCallIntent);
			Log.d("ACTION_BT_CALL_IN", "发送了" + ACTION_BT_CALL_IN);
		}
		try {
			wm.removeView(mView);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取URL返回的字符串
	 * @param tel
	 */
	public void callUrlByGet(final String tel) {
		tel.replace("+86", "");
		String cache = SyncService.mCache.getJsonFromMemCache("电话号码：" + tel);
		if (!TextUtils.isEmpty(cache)) {
			mAddressText.setVisibility(View.VISIBLE);
			mAddressText.setText(cache);
		} else {
			String url = "http://apis.juhe.cn/mobile/get?phone=" + tel + "&key=0b3d7d7149323e5b0291ad931a19aa6c";
			AsyncHttpClient client = new AsyncHttpClient();
			client.get(url, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, String content) {
					try {
						JSONObject object = new JSONObject(content);
						int error_code = object.getInt("error_code");
						if (error_code == 0) {
							Log.d(TAG, "有效的电话号码:" + tel);
							msgGson = new Gson();
							MobileLocation location = msgGson.fromJson(content, MobileLocation.class);
							String mobile = location.getResult().getProvince() + " " + location.getResult().getCity()+ " " + location.getResult().getCompany();
							SyncService.mCache.addJsonToMemoryCache("电话号码：" + tel, mobile);
							if (!TextUtils.isEmpty(mobile)) {
								mAddressText.setVisibility(View.VISIBLE);
								mAddressText.setText(mobile);
							}
							Log.d(TAG, location.getResult().getProvince() + " " + location.getResult().getCity() + " "
									+ location.getResult().getCompany());
						} else {
							Log.d(TAG, "无效的电话号码:" + tel);
							mAddressText.setVisibility(View.GONE);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(Throwable error, String content) {
					Log.d(TAG, "请求报错："+error + "");
				}
			});
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (v == mDeleteButton) {
			mDisplayNumber.delete(0, mDisplayNumber.length());
			mInputText.setText("");
		}
		return false;
	}

}
