package com.spreadwin.btc.Bluetooth;

import com.spreadwin.btc.BtcNative;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;
import com.spreadwin.btc.utils.BtcGlobalData;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
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

public class BluetoothFragment extends Fragment implements OnClickListener {
		public  static final String TAG = MainActivity.TAG;
		public  static final boolean DEBUG = true;	
		private LayoutInflater mInflater;
		private ViewGroup mContentContainer;
		private View mRootView;
		ImageButton mDeleteButton,mNumberOne,mNumberTwo,mNumberThree,mNumberFour,mNumberFive,
					mNumberSix,mNumberSeven,mNumberEight,mNumberNine,mNumberZero,mNumberJin,
					mNumberXing,mDialButton,mDroppedButton;
		TextView mInputText,mIncallText,mIncallTime;
		
		StringBuilder mDisplayNumber = new StringBuilder();
		private int hour = 0;
		private int minute = 0;
		private int second = 0;
		private boolean bool;
		
		
		
		@Override
		public void onCreate(Bundle savedInstanceState) {	
			super.onCreate(savedInstanceState);
			mDisplayNumber.delete(0, mDisplayNumber.length());
			mLog("BluetoothFragment onCreate 11111111111");
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mInflater = inflater;
			mRootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);
			init();
			return mRootView;
		}
		

		private void init() {
			mDeleteButton = (ImageButton) mRootView.findViewById(R.id.delete_button);
			mNumberOne = (ImageButton) mRootView.findViewById(R.id.number_1);
			mNumberTwo = (ImageButton) mRootView.findViewById(R.id.number_2);
			mNumberThree = (ImageButton) mRootView.findViewById(R.id.number_3);
			mNumberFour = (ImageButton) mRootView.findViewById(R.id.number_4);
			mNumberFive = (ImageButton) mRootView.findViewById(R.id.number_5);
			mNumberSix = (ImageButton) mRootView.findViewById(R.id.number_6);
			mNumberSeven = (ImageButton) mRootView.findViewById(R.id.number_7);
			mNumberEight = (ImageButton) mRootView.findViewById(R.id.number_8);
			mNumberNine = (ImageButton) mRootView.findViewById(R.id.number_9);
			mNumberZero = (ImageButton) mRootView.findViewById(R.id.number_0);
			mNumberJin = (ImageButton) mRootView.findViewById(R.id.number_jin);
			mNumberXing = (ImageButton) mRootView.findViewById(R.id.number_xing);
			mDialButton = (ImageButton) mRootView.findViewById(R.id.dial_button);
			mDroppedButton = (ImageButton) mRootView.findViewById(R.id.dropped_button);
			mInputText = (TextView) mRootView.findViewById(R.id.input_text);
			mIncallText = (TextView) mRootView.findViewById(R.id.in_call_name);
			mIncallTime = (TextView) mRootView.findViewById(R.id.in_call_timet);
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
			mDialButton.setOnClickListener(this);
			mDroppedButton.setOnClickListener(this);	
			checkCallStatus();
		}


		@Override
		public void onClick(View v) {
			if (v == mDeleteButton) {
				removeNumber();
			}else if (v == mNumberOne) {
				addNumber("1");
			}else if (v == mNumberTwo) {
				addNumber("2");
			}else if (v == mNumberThree) {
				addNumber("3");
			}else if (v == mNumberFour) {
				addNumber("4");
			}else if (v == mNumberFive) {
				addNumber("5");
			}else if (v == mNumberSix) {
				addNumber("6");
			}else if (v == mNumberSeven) {
				addNumber("7");
			}else if (v == mNumberEight) {
				addNumber("8");
			}else if (v == mNumberNine) {
				addNumber("9");
			}else if (v == mNumberZero) {
				addNumber("0");
			}else if (v == mNumberJin) {
				addNumber("#");
			}else if (v == mNumberXing) {
				addNumber("*");				
			}else if (v == mDialButton) {
				dialCall(mDisplayNumber.toString());
			}else if (v == mDroppedButton) {
				hangupCall();
			}
		}
		
		private void checkCallStatus() {	
			mLog("checkCallStatus2222 BtcNative.getCallStatus()=="+BtcNative.getCallStatus());
			if (BtcNative.getCallStatus() == BtcGlobalData.IN_CALL) {
				setCallStatus(BtcGlobalData.IN_CALL);
			}else if (BtcNative.getCallStatus() == BtcGlobalData.CALL_OUT) {
				setCallStatus(BtcGlobalData.CALL_OUT);
			}else if (BtcNative.getCallStatus() == BtcGlobalData.NO_CALL) {
				setCallStatus(BtcGlobalData.NO_CALL);	
			}else if (BtcNative.getCallStatus() == BtcGlobalData.CALL_IN) {
				setCallStatus(BtcGlobalData.CALL_IN);				
			}
		}

		//挂断电话
		private void hangupCall() {
			BtcNative.hangupCall();		
//			clearInput();
		}
		
		//拨打电话
		public void dialCall(String callNumber) {
			if (callNumber.length() > 0) {
				BtcNative.dialCall(callNumber);
//				checkCallStatus();
//				setCallStatus(BtcGlobalData.CALL_OUT);
			}			
		}

		private void addNumber(String str) {
			BtcNative.dtmfCall(str);
			mDisplayNumber.append(str);		
			mInputText.setText(mDisplayNumber.toString());
		}
		
		public void setCallStatus(int status) {	
			mLog("setCallStatus11111111 =="+isAdded());
			if (!isAdded()) {
				return;
			}
			if (mInputText == null) {
				mLog("mInputText == null");
				return;
			}
			if (status == BtcGlobalData.CALL_IN) {
				bool = false;
				mInputText.setVisibility(View.GONE);
				mDeleteButton.setVisibility(View.INVISIBLE);
				mIncallText.setVisibility(View.VISIBLE);
				mIncallTime.setVisibility(View.VISIBLE);	
				mLog("setCallStatus11111111 BtcNative.getCallNumber() =="+BtcNative.getCallNumber());
				mIncallText.setText(getCallName(BtcNative.getCallNumber())+BtcNative.getCallNumber());
			}else if (status == BtcGlobalData.CALL_OUT) {
				bool = false;
				mInputText.setVisibility(View.VISIBLE);
				mInputText.setText(getCallName(BtcNative.getCallNumber())+BtcNative.getCallNumber());
				mDeleteButton.setVisibility(View.INVISIBLE);
				mIncallText.setVisibility(View.VISIBLE);
				mIncallTime.setVisibility(View.GONE);
				mIncallText.setText("呼叫...");		 		
			}else if (status == BtcGlobalData.NO_CALL){
				bool = false;
				mIncallText.setText("");
				mInputText.setVisibility(View.VISIBLE);
				clearInput();				
				mDeleteButton.setVisibility(View.VISIBLE);
				mIncallText.setVisibility(View.GONE);
				mIncallTime.setVisibility(View.GONE);	
			}else if (status == BtcGlobalData.IN_CALL) {	
				if (mIncallTime.getVisibility() == View.GONE) {
					mInputText.setVisibility(View.VISIBLE);
					mDeleteButton.setVisibility(View.INVISIBLE);
					mIncallText.setVisibility(View.VISIBLE);
					mIncallTime.setVisibility(View.VISIBLE);	
					mLog("setCallStatus11111111 BtcNative.getPhoneName()=="+BtcNative.getPhoneName()+"; BtcNative.getCallNumber() =="+BtcNative.getCallNumber());
					mInputText.setText("");
					mDisplayNumber.delete(0, mDisplayNumber.length());
					mIncallText.setText(getCallName(BtcNative.getCallNumber())+BtcNative.getCallNumber());
				}
				bool = true;
				hour = 0;
				minute = 0;
				second = 0;		
				handler.removeCallbacks(runnable);
				handler.post(runnable);
			}
		}
		
		private String getCallName(String getCallNumber) {
			String mCallName = "";
			if (MainActivity.binder != null) {
				if (MainActivity.binder.getPhoneBookInfo(BtcGlobalData.PB_PHONE).getSize()> 0) {
					mCallName = MainActivity.binder.getPhoneBookInfo(BtcGlobalData.PB_PHONE).getCalllName(getCallNumber);
					return mCallName;				
				}
				if (MainActivity.binder.getPhoneBookInfo(BtcGlobalData.PB_SIM).getSize()> 0) {
					mCallName = MainActivity.binder.getPhoneBookInfo(BtcGlobalData.PB_SIM).getCalllName(getCallNumber);
					return mCallName;				
				}
			}
			return mCallName;
		}
		
		private void clearInput() {
			if (isAdded()) {
				mInputText.setText(getResources().getString(R.string.default_title));				
			}
			mDisplayNumber.delete(0, mDisplayNumber.length());
		}

		Handler handler =new Handler();
		Runnable runnable =new Runnable() {			
			@Override
			public void run() {				
				second++;										
				if (second >= 60) {
					minute++;
					second = second % 60;
				}
				if (minute >= 60) {
					hour++;
					minute = minute % 60;
				}
				mIncallTime.setText(format(hour) + ":" + format(minute) + ":"
							+ format(second));
				if (bool) {
					handler.postDelayed(this, 1000);
				}
			}
		};
		
		/*
		 * 格式化时间
		 */
		public String format(int i) {
			String s = i + "";
			if (s.length() == 1) {
				s = "0" + s;
			}
			return s;
		}
		
		private void removeNumber() {
			mLog("removeNumber111111111111 mDisplayNumber =="+mDisplayNumber.length());
			if (mDisplayNumber.length() > 1) {
				mDisplayNumber.deleteCharAt(mDisplayNumber.length()-1);					
				mInputText.setText(mDisplayNumber.toString());
			}else if(mDisplayNumber.length() == 1) {
				mDisplayNumber.deleteCharAt(mDisplayNumber.length()-1);					
				mInputText.setText(getResources().getString(R.string.default_title));
			}else{
				mInputText.setText(getResources().getString(R.string.default_title));				
			}
		}

		public static  void mLog(String string) {
			if (DEBUG) {
				Log.d(TAG, string);				
			}
		}
}
