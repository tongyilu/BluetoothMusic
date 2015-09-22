package com.spreadwin.btc.contacts;

import java.util.Comparator;

import android.util.Log;

import com.spreadwin.btc.utils.PhoneBookInfo_new;

/**
 * 
 * @author xiaanming
 *
 */
public class PinyinComparator implements Comparator<PhoneBookInfo_new> {
	public int compare(PhoneBookInfo_new o1, PhoneBookInfo_new o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			int result = o1.getSortLetters().compareTo(o2.getSortLetters());
			if (result == 0) {
				Log.d("compare", "01 getNumber=="+o1.getNumber()+"; Second1 =="+o1.getSecondLetters()+
						"; o2 getNumber=="+o2.getNumber()+"; Second2 =="+o2.getSecondLetters());
				return o1.getSecondLetters().compareTo(o2.getSecondLetters());
			}else{
				return result;				
			}
		}
	}

}
