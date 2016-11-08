package com.spreadwin.btc.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.util.Log;

public class SplitUtil {

	public static int setBluetoothStatus(Activity context) {
		int windowsSize = 0;
		ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		int stackBoxId = manager.getLeftStackId();
		if (stackBoxId > 0) {
			windowsSize = manager.getWindowSizeStatus(stackBoxId);
		}
		return windowsSize;
	}

	static public RecentTaskInfo getTopTaskOfStack(Context ct, int stackId) {
		RecentTaskInfo ret = null;
		if (ct == null) {
			return ret;
		}
		try {
			Object am = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
			Class<?> activityManager = Class.forName("android.app.ActivityManager");
			Method myGetTopTaskOfStack = null;
			if (am != null) {
				myGetTopTaskOfStack = activityManager.getMethod("getTopTaskOfStack", int.class);
				myGetTopTaskOfStack.setAccessible(true);
				ret = (RecentTaskInfo) myGetTopTaskOfStack.invoke(am, stackId);
			}
		} catch (NoSuchMethodException e) {
			Log.d("NoSuchMethodException", "getTopTaskOfStack reflect failed");
			ret = null;
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			ret = null;
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
			ret = null;
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			ret = null;
		}
		return ret;
	}

}
