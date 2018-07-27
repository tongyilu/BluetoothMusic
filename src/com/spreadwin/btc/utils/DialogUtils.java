package com.spreadwin.btc.utils;

import com.smartandroid.sa.aysnc.Log;
import com.spreadwin.btc.MainActivity;
import com.spreadwin.btc.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class DialogUtils {
    private static Dialog dialog_bluetooth;
    private static View view;


    public static void showNoBluetoothDialog(final MainActivity mainActivity){
        Log.d("DialogUtils", "showNoBluetoothDialog1111");
        if (dialog_bluetooth != null && dialog_bluetooth.isShowing()){
            return;
        }
        dialog_bluetooth = new Dialog(mainActivity,R.style.MyDialog);
        view = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_no_bluetooth,null);
        dialog_bluetooth.setContentView(view);
        dialog_bluetooth.setCancelable(false);
        dialog_bluetooth.show();
    }

    public static void dismissBluetoothDialog(){
        if (dialog_bluetooth != null && dialog_bluetooth.isShowing()){
            dialog_bluetooth.dismiss();
        }
    }
}
