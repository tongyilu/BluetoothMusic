package com.spreadwin.btc.utils;

import java.util.ArrayList;

public class PhoneBookInfo_new {


	private String name;   //显示的数据
//	private String number;   //显示的号码
	private ArrayList<String> number = new ArrayList<String>();
 	private String sortLetters;  //显示数据拼音的首字母
 	private String secondLetters;  //显示数据拼音的首字母
	
	public PhoneBookInfo_new(String name , String number) {
		this.name = name;
		this.number.add(number);		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setNumber(String number) {
		this.number.add(number);
	}
	
	public ArrayList<String> getNumber() {
		return number;
	}
	
	public String getSortLetters() {
		return sortLetters;
	}
	public String getSecondLetters() {
		return secondLetters;
	}
	
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	public void setSecondLetters(String secondLetters) {
		this.secondLetters = secondLetters;
	}
}
