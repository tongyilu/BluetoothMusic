package com.spreadwin.btc;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class AutoStartReceiver extends BroadcastReceiver {
	public static String TAG ="AutoStartReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "intent.getAction() =="+intent.getAction());	
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

		    Intent startIntent = new Intent(context, SyncService.class);
		    context.startService(startIntent);  
		}
//		else if (intent.getAction().equals("PHONE_BOOK_SYNC")) {
//			ArrayList<String> mPhoneBook = intent.getStringArrayListExtra("phonebook");	
//			Log.d(TAG, "mPhoneBook.size() =="+mPhoneBook.size());	
//			for (int i = 0; i < mPhoneBook.size(); i++) {
//				Log.d(TAG, "mPhoneBook["+i+"] =="+mPhoneBook.get(i));
//			}
//		}
	}

}
