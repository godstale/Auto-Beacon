package com.wiredfactory.bluewave.utils;

import android.util.Log;

public class Logs {
	
	private static final String TAG = "BlueWave";
	public static boolean mIsEnabled = true;
	
	
	public static void v(String msg) {
		if(mIsEnabled) {
			Log.v(TAG, msg);
		}
	}
	
	public static void v(String tag, String msg) {
		if(mIsEnabled) {
			Log.v(tag, msg);
		}
	}
	
	public static void d(String msg) {
		if(mIsEnabled) {
			Log.d(TAG, msg);
		}
	}
	
	public static void d(String tag, String msg) {
		if(mIsEnabled) {
			Log.d(tag, msg);
		}
	}
	
	public static void e(String msg) {
		if(mIsEnabled) {
			Log.e(TAG, msg);
		}
	}
	
	public static void e(String tag, String msg) {
		if(mIsEnabled) {
			Log.e(tag, msg);
		}
	}
	
	public static void i(String msg) {
		if(mIsEnabled) {
			Log.e(TAG, msg);
		}
	}
	
	public static void i(String tag, String msg) {
		if(mIsEnabled) {
			Log.e(tag, msg);
		}
	}
	
}
