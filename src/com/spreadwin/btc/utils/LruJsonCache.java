package com.spreadwin.btc.utils;

import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

public class LruJsonCache {
	private LruCache<String, String> mMemoryCache;

	public LruJsonCache() {
		int maxMemory = (int) Runtime.getRuntime().maxMemory() / 10;
		mMemoryCache = new LruCache<String, String>(maxMemory) {
			@Override
			protected int sizeOf(String key, String value) {
				System.out.println(value);
				Log.d("mMemoryCache_value", value);
				return value.length();
			}
		};
	}

	/**
	 * 
	 * @Title: addJsonToMemoryCache
	 * @Description: TODO 添加json内存
	 * @return void
	 */
	public void addJsonToMemoryCache(String key, String jsonString) {
		if (mMemoryCache == null) {
			return;
		}
		if (TextUtils.isEmpty(key)) {
			return;
		}

		if (getJsonFromMemCache(key) == null && jsonString != null) {
			mMemoryCache.put(key, jsonString);
		}
	}

	/**
	 * 从内存缓存中获取一个Json
	 * 
	 * @param key
	 * @return
	 */
	public String getJsonFromMemCache(String key) {
		if (mMemoryCache == null) {
			return null;
		}
		return mMemoryCache.get(key);
	}
}