package com.spreadwin.btc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	/**
	 * 获取现在时间
	 * 
	 * @return返回短时间格式 yyyy-MM-dd
	 */
	public static String getNowDateShort() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(new Date());
	}
}
