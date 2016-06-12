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

	private ImageView mDialButton,mMute,mSwitch;
	private ImageView mdroppedbutton;
	private TextView mAddressText, mNameText,mCallText;
	public static final String ACTION_BT_CALL_IN = "ACTION_BT_CALL_IN";
	public static final String EXTRA_BT_CALL_IN_NAME = "EXTRA_BT_CALL_IN_NAME";
	public static final String EXTRA_BT_CALL_IN_NUMBER = "EXTRA_BT_CALL_IN_NUMBER";

	private AnimationDrawable animDown = new AnimationDrawable();
	private DilatingDotsProgressBar mDilatingDotsProgressBar;
	private ImageView imgGameWord;
	private OpenUtils openUtils;
	private boolean isStart;
	private String getCallNumber;
	private String getPhoneName;

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
		mAddressText = (TextView) view.findViewById(R.id.address_text);
		mNameText = (TextView) view.findViewById(R.id.name_text);
		mCallText = (TextView) view.findViewById(R.id.call_text);
		mSwitch = (ImageView) view.findViewById(R.id.image_switch);
		mMute = (ImageView) view.findViewById(R.id.mute);
		getCallNumber = BtcNative.getCallNumber();
		getPhoneName = getCallName(getCallNumber);
		if (getPhoneName!=null) {
			mNameText.setText(getPhoneName);
		}else{
			mNameText.setText(getCallNumber);
		}
		callUrlByGet(getCallNumber);
		mMute.setOnClickListener(this);
		mSwitch.setOnClickListener(this);
		mDialButton.setOnClickListener(this);
		mdroppedbutton.setOnClickListener(this);
		imgGameWord = (ImageView) view.findViewById(R.id.icon);
		imgGameWord.setBackgroundResource(R.anim.call_anim);
		animDown = (AnimationDrawable) imgGameWord.getBackground();
		animDown.start();
		animDown.setOneShot(false);
		mDilatingDotsProgressBar.show();
		if (!isStart) {
			mCallText.setText("正在呼叫...");
			mMute.setVisibility(View.GONE);
			mDialButton.setVisibility(View.GONE);
			mSwitch.setVisibility(View.GONE);
		} else {
			mCallText.setText("来电");
			mMute.setVisibility(View.VISIBLE);
			mDialButton.setVisibility(View.VISIBLE);
			mSwitch.setVisibility(View.VISIBLE);
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
		case R.id.image_switch:
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
		mMute.setImageResource(isState ? R.drawable.mute_u : R.drawable.mute_d);
	}

	public void setHfImage(boolean isState1) {
		mSwitch.setImageResource(isState1 ? R.drawable.switching_01 : R.drawable.switching_02);
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
		((SyncService) mContext).finishMainActivity();
		if (isStart) {
			Intent mCallIntent = new Intent(ACTION_BT_CALL_IN);
			mContext.sendBroadcast(mCallIntent);
			Log.d("ACTION_BT_CALL_IN", "发送了" + ACTION_BT_CALL_IN);
		}
		wm.removeView(mView);
	}

	/**
	 * 获取URL返回的字符串
	 * 
	 * @param callurl
	 * @param charset
	 * @return
	 */
	public void callUrlByGet(final String tel) {
		tel.replace("+86", "");
		String cache = SyncService.mCache.getJsonFromMemCache("电话号码："+tel);
		if (!TextUtils.isEmpty(cache)) {
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
							MobileLocation location = new Gson().fromJson(content,new TypeToken<MobileLocation>() {}.getType());
							String mobile = location.getResult().getProvince() + " " + location.getResult().getCity()+ " "+location.getResult().getCompany();
							SyncService.mCache.addJsonToMemoryCache("电话号码："+ tel, mobile);
							mAddressText.setText(mobile);
							Log.d(TAG, location.getResult().getProvince() + " " + location.getResult().getCity()+" "+location.getResult().getCompany());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(Throwable error, String content) {
                    Log.d(TAG, error+"");
				}
			});
		}
	}
}
