package com.spreadwin.btc.utils;

import android.app.Activity;
import android.app.ActivityManager;

public class ScreenUtils {
  
	public static int setBluetoothStatus(Activity context){
		int windowsSize = 0;
		ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		int stackBoxId = manager.getLeftStackId();
		if (stackBoxId > 0 ){
			windowsSize = manager.getWindowSizeStatus(stackBoxId);
		}
		return windowsSize;
	}
}
