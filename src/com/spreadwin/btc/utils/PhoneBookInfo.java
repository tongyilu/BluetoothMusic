package com.spreadwin.btc.utils;

import java.util.ArrayList;

public class PhoneBookInfo {
	private final ArrayList<String> mTelName = new ArrayList<String>();
	private final ArrayList<String> mTelNumber = new ArrayList<String>();
	private final ArrayList<String> mTelTime = new ArrayList<String>();
	private int mCallLogsType;
	public PhoneBookInfo(int calllogsType) {
		mCallLogsType = calllogsType;
	}
	
	public void add(String name, String number, String time) {
		mTelName.add(name);
		mTelNumber.add(number);
		mTelTime.add(time);		
	}
	private void remove(int location) {
		mTelName.remove(location);
		mTelNumber.remove(location);
		mTelTime.remove(location);

	}
	private void getLocation(int location) {
		if (location < mTelName.size()) {
			
		}
	}
	public void clear() {
		mTelName.clear();
		mTelNumber.clear();
		mTelTime.clear();
	}
	
	public int getType() {
		return mCallLogsType;
	}
	
	public int getSize() {
		if (mTelName == null) {
			return 0;
		}		
		return mTelName.size();
	}
	
	public CharSequence getTelName(int position) {
		return mTelName.get(position);
	}
	
	public String getTelNumber(String callName) {
		int index =mTelName.indexOf(callName);
		if (index != -1) {
			return mTelNumber.get(index);
		}
		return null;
	}
	
	public String getCalllName(String callNumber) {
		int index =mTelNumber.indexOf(callNumber);
		if (index != -1) {
			return mTelName.get(index);
		}
		return "";
	}
	
	public String getTelNumber(int position) {
		return mTelNumber.get(position);
	}
	
	public String getTelTime(int position) {
		return mTelTime.get(position);
	}
	
}
