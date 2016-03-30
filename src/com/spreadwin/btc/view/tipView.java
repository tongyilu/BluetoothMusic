package com.spreadwin.btc.view;

import com.spreadwin.btc.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class tipView implements OnCheckedChangeListener {

	private Button mBt;
	private LinearLayout layoutMain;

	private ImageButton btn_consent;
	private CheckBox mCheckBox;
	private View mView;
	private Context mContext;
	

	public tipView(Context context) {
		this.mContext = context;
		mView = LayoutInflater.from(context).inflate(R.layout.display_call, null);
		initView(mView);
	}

	private void initView(View view) {

		btn_consent = (ImageButton) view.findViewById(R.id.mdropped_button);
//		mCheckBox = (CheckBox) view.findViewById(R.id.radio);
//		layoutMain = (LinearLayout) view.findViewById(R.id.main);
//		mCheckBox.setOnCheckedChangeListener(this);
//            
//       
		btn_consent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WindowManager wm = (WindowManager)mContext.getSystemService(
						Context.WINDOW_SERVICE);
				wm.removeView(mView);
			}
		});
	
	}
	
	

	public View getVideoPlayView() {
		return mView;
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		//mTipWindowDelegate.onCheckedChanged(isChecked);
		
	}
}
