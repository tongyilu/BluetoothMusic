package com.spreadwin.btc.utils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PhoneBookInfo {
	private final ArrayList<String> mTelName = new ArrayList<String>();
	private final ArrayList<String> mTelNumber = new ArrayList<String>();
	private final ArrayList<String> mTelTime = new ArrayList<String>();
//	Map<String, String> mContacts = new HashMap<String, String>();
//	// 按照键值排序  
//	myComparator comparator = new myComparator(); 
//	Comparator<Object> com=Collator.getInstance(java.util.Locale.CHINA); 
//    Map sortMap= new TreeMap(com); 
//	
	private int mCallLogsType;
	public PhoneBookInfo(int calllogsType) {
		mCallLogsType = calllogsType;
	}
	
	public void add(String name, String number, String time) {
		mTelName.add(name);
		mTelNumber.add(number);
		mTelTime.add(time);	
		
//		mContacts.
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
	
	public String getTelName(int position) {
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
	
	public boolean isNumberExist(String callNumber) {
		int index = mTelNumber.indexOf(callNumber);
		if (index != -1 ) {
			return true;			
		}
		return false;
	}
	
	public String getTelNumber(int position) {
		return mTelNumber.get(position);
	}
	
	public String getTelTime(int position) {
		return mTelTime.get(position);
	}
	
	public String get(int position) {
		return mTelName.get(position)+":"+mTelNumber.get(position);
	}

	public void setArraySort() {	
	    Comparator comparator = Collator.getInstance(java.util.Locale.CHINA);
//	    Arrays.sort(arrStrings, comparator);
//	    mTelName.
		
	}
	
}
