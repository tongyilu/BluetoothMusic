package com.spreadwin.btc.view;


import android.content.Context;
import android.util.Log;



/**
 * @author : Tong Yilu
 * @version: 2018-7-13
 */
public class CallDialog {
    public static String TAG = "CallDialog";

    public static CallDialog mCallDialog = null;
    
    private static Context mContext;
    
    public CallDialog(Context context) {
        mContext = context;
    }

    public static CallDialog getInstance(Context context) {
        if (mCallDialog == null || mContext != context) {
            mCallDialog = new CallDialog(context);
            return mCallDialog;
        } else {
            return mCallDialog;
        }
    }
    
}
