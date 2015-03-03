package com.spreadwin.btc;

import com.spreadwin.btc.utils.BtcGlobalData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class DialogActivity extends Activity implements OnClickListener {
	public  static final String TAG = "MainActivity";
	public  static final boolean DEBUG = true;
	
	ImageButton mDialButton;
	ImageButton mdroppedbutton;
	TextView mNumberText;
	Dialog mCallDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.display_call);
		mDialButton = (ImageButton) findViewById(R.id.mdial_button);
		mdroppedbutton = (ImageButton) findViewById(R.id.mdropped_button);
		mNumberText = (TextView) findViewById(R.id.number_text);	
		String getPhoneName =BtcNative.getPhoneName();
		String getCallNumber =BtcNative.getCallNumber();
		mNumberText.setText(getPhoneName+getCallNumber);
		mDialButton.setOnClickListener(this);
		mdroppedbutton.setOnClickListener(this);
		
		
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		LayoutInflater inflater = LayoutInflater.from(this);		
//			View mCallView = inflater.inflate(R.layout.display_call, null);
//			builder.setView(mCallView);			
//			mDialButton = (ImageButton) mCallView.findViewById(R.id.mdial_button);
//			mdroppedbutton = (ImageButton) mCallView.findViewById(R.id.mdropped_button);
//			mNumberText = (TextView) mCallView.findViewById(R.id.number_text);			
//
//			String getPhoneName =BtcNative.getPhoneName();
//			String getCallNumber =BtcNative.getCallNumber();
//			mLog("onCreateDialog 1111111111");
//			mNumberText.setText(getPhoneName+getCallNumber);
//			mDialButton.setOnClickListener(this);
//			mdroppedbutton.setOnClickListener(this);
////			if (mCallDialog != null ) {
////				mCallDialog.dismiss();	
////				mCallDialog = null;
////			}			                          
//
//				mCallDialog = builder.create();
//				mCallDialog.setCanceledOnTouchOutside(false);
//				mCallDialog.show();	
	}
	@Override
	public void onClick(View v) {
		if (v == mDialButton) {
			mLog("onClick mDialButton11111111111");
			answerCall();
		}else if (v == mdroppedbutton) {
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
	}
	
	private void denyCall() {
		BtcNative.denyCall();		
//		mDismissDialog(DIALOG1);
	}
	
	public static  void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG, string);				
		}
	}	
}
